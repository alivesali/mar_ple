package com.example.engine

import com.example.model.Player
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager
import com.example.model.team.TeamVictoryMode
import com.example.model.toPersianDigits

/**
 * Result of checking whether the game or a round has finished.
 */
data class MatchCheckResult(
    val isGameOver: Boolean,
    val winner: Player? = null,
    val winningTeam: GameTeam? = null,
    val winningPlayers: List<Player> = emptyList(),
    val isDrawOrNoWinner: Boolean = false,
    val winReason: String? = null,
    val rankings: List<Player> = emptyList()
)

/**
 * Engine determining victory conditions, player eliminations, team victories,
 * draw states, and rankings across any dynamic board size.
 */
class MatchResolutionEngine(
    val winningTile: Int = 100
) {
    /**
     * Checks if an alive player reached [winningTile], if a team won via [teamVictoryMode] or survival,
     * if only one survivor remains in elimination mode, or if all players are eliminated (Draw).
     */
    fun checkMatchStatus(
        players: List<Player>,
        isEliminationEnabled: Boolean = false,
        teamManager: TeamManager? = null,
        teamVictoryMode: TeamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH,
        finishedPlayerIds: Set<Int> = emptySet(),
        eliminatedOutPlayerIds: Set<Int> = emptySet(),
        finishOrderPlayerIds: List<Int> = emptyList()
    ): MatchCheckResult {
        if (players.isEmpty()) {
            return MatchCheckResult(isGameOver = false)
        }

        val alivePlayers = players.filter { it.resources.isAlive && it.id !in eliminatedOutPlayerIds }

        // 1. All players eliminated -> Match ends without a winner (Draw / No Winner)
        if (alivePlayers.isEmpty()) {
            return MatchCheckResult(
                isGameOver = true,
                winner = null,
                winningTeam = null,
                winningPlayers = emptyList(),
                isDrawOrNoWinner = true,
                winReason = "مسابقه بدون برنده به پایان رسید؛ تمام قهرمانان از دور رقابت خارج شدند.",
                rankings = sortRankings(
                    players = players,
                    finishOrderPlayerIds = finishOrderPlayerIds,
                    eliminatedOutPlayerIds = eliminatedOutPlayerIds
                )
            )
        }

        // 2. Check exact tile victory ONLY among alive players (eliminated players can NEVER win)
        val rawFinishers = alivePlayers.filter { it.currentTile >= winningTile }
        if (rawFinishers.isNotEmpty()) {
            val orderedFinishers = (finishOrderPlayerIds.mapNotNull { id -> rawFinishers.find { it.id == id } } +
                rawFinishers.filter { it.id !in finishOrderPlayerIds }).distinctBy { it.id }
            val tileWinner = orderedFinishers.first()
            val winnerTeam = teamManager?.getTeamForPlayer(tileWinner.id)
            val isRealTeamMode = teamManager != null && teamManager.getAllTeams().any { it.memberPlayerIds.size > 1 }

            if (isRealTeamMode && teamManager != null) {
                if (teamVictoryMode == TeamVictoryMode.ONE_MEMBER_FINISH) {
                    val winningFinisher = orderedFinishers.firstOrNull { teamManager.getTeamForPlayer(it.id) != null } ?: tileWinner
                    val resolvedTeam = teamManager.getTeamForPlayer(winningFinisher.id)
                    if (resolvedTeam != null) {
                        val teamMembers = players.filter { resolvedTeam.containsPlayer(it.id) }
                        return MatchCheckResult(
                            isGameOver = true,
                            winner = winningFinisher,
                            winningTeam = resolvedTeam,
                            winningPlayers = teamMembers,
                            isDrawOrNoWinner = false,
                            winReason = "تیم «${resolvedTeam.name}» با رسیدن «${winningFinisher.name}» به خانه ${winningTile.toPersianDigits()} قهرمان شد!",
                            rankings = sortRankings(
                                players = players,
                                preferredWinnerId = winningFinisher.id,
                                winningTeam = resolvedTeam,
                                finishOrderPlayerIds = finishOrderPlayerIds,
                                eliminatedOutPlayerIds = eliminatedOutPlayerIds
                            )
                        )
                    }
                } else {
                    // ALL_MEMBERS_FINISH: Every member of the team must be alive, not permanently eliminated, and at winningTile.
                    val candidateTeams = (orderedFinishers.mapNotNull { teamManager.getTeamForPlayer(it.id) } +
                        teamManager.getAllTeams()).distinctBy { it.id }

                    for (candidateTeam in candidateTeams) {
                        val allTeamMembers = players.filter { candidateTeam.containsPlayer(it.id) }
                        val allMembersAliveAndFinished = allTeamMembers.isNotEmpty() &&
                            allTeamMembers.all {
                                it.resources.isAlive &&
                                    it.id !in eliminatedOutPlayerIds &&
                                    it.currentTile >= winningTile
                            }
                        if (allMembersAliveAndFinished) {
                            val firstTeamFinisher = orderedFinishers.firstOrNull { candidateTeam.containsPlayer(it.id) }
                                ?: allTeamMembers.first()
                            return MatchCheckResult(
                                isGameOver = true,
                                winner = firstTeamFinisher,
                                winningTeam = candidateTeam,
                                winningPlayers = allTeamMembers,
                                isDrawOrNoWinner = false,
                                winReason = "تیم «${candidateTeam.name}» با رسیدن تمام اعضا به خانه ${winningTile.toPersianDigits()} قهرمان شد!",
                                rankings = sortRankings(
                                    players = players,
                                    preferredWinnerId = firstTeamFinisher.id,
                                    winningTeam = candidateTeam,
                                    finishOrderPlayerIds = finishOrderPlayerIds,
                                    eliminatedOutPlayerIds = eliminatedOutPlayerIds
                                )
                            )
                        }
                    }
                }
            } else {
                return MatchCheckResult(
                    isGameOver = true,
                    winner = tileWinner,
                    winningTeam = null,
                    winningPlayers = listOf(tileWinner),
                    isDrawOrNoWinner = false,
                    winReason = "رسیدن به خانه ${winningTile.toPersianDigits()} و فتح صفحه بازی!",
                    rankings = sortRankings(
                        players = players,
                        preferredWinnerId = tileWinner.id,
                        finishOrderPlayerIds = finishOrderPlayerIds,
                        eliminatedOutPlayerIds = eliminatedOutPlayerIds
                    )
                )
            }
        }

        // 3. Check elimination victory (last surviving team or last surviving player)
        if (isEliminationEnabled && players.size > 1) {
            if (teamManager != null) {
                val allTeams = teamManager.getAllTeams()
                val isRealTeamMode = allTeams.any { it.memberPlayerIds.size > 1 }
                if (isRealTeamMode) {
                    val aliveTeams = allTeams.filter { team ->
                        team.memberPlayerIds.any { memberId ->
                            alivePlayers.any { it.id == memberId }
                        }
                    }
                    if (aliveTeams.size == 1 && allTeams.size > 1) {
                        val survivorTeam = aliveTeams.first()
                        val teamMembers = players.filter { survivorTeam.containsPlayer(it.id) }
                        val representative = alivePlayers
                            .filter { survivorTeam.containsPlayer(it.id) }
                            .maxByOrNull { it.currentTile }
                            ?: alivePlayers.first()
                        return MatchCheckResult(
                            isGameOver = true,
                            winner = representative,
                            winningTeam = survivorTeam,
                            winningPlayers = teamMembers,
                            isDrawOrNoWinner = false,
                            winReason = "تیم «${survivorTeam.name}» به عنوان آخرین تیم بازمانده پیروز شد!",
                            rankings = sortRankings(
                                players = players,
                                preferredWinnerId = representative.id,
                                winningTeam = survivorTeam,
                                finishOrderPlayerIds = finishOrderPlayerIds,
                                eliminatedOutPlayerIds = eliminatedOutPlayerIds
                            )
                        )
                    }
                }
            }

            if (alivePlayers.size == 1) {
                val survivor = alivePlayers.first()
                val survivorTeam = teamManager?.getTeamForPlayer(survivor.id)
                val isRealTeam = survivorTeam != null && teamManager.getAllTeams().any { it.memberPlayerIds.size > 1 }
                return MatchCheckResult(
                    isGameOver = true,
                    winner = survivor,
                    winningTeam = if (isRealTeam) survivorTeam else null,
                    winningPlayers = if (isRealTeam && survivorTeam != null) {
                        players.filter { survivorTeam.containsPlayer(it.id) }
                    } else {
                        listOf(survivor)
                    },
                    isDrawOrNoWinner = false,
                    winReason = "آخرین بازمانده میدان نبرد!",
                    rankings = sortRankings(
                        players = players,
                        preferredWinnerId = survivor.id,
                        finishOrderPlayerIds = finishOrderPlayerIds,
                        eliminatedOutPlayerIds = eliminatedOutPlayerIds
                    )
                )
            }
        }

        return MatchCheckResult(
            isGameOver = false,
            rankings = sortRankings(
                players = players,
                finishOrderPlayerIds = finishOrderPlayerIds,
                eliminatedOutPlayerIds = eliminatedOutPlayerIds
            )
        )
    }

    fun sortRankings(
        players: List<Player>,
        preferredWinnerId: Int? = null,
        winningTeam: GameTeam? = null,
        finishOrderPlayerIds: List<Int> = emptyList(),
        eliminatedOutPlayerIds: Set<Int> = emptySet()
    ): List<Player> {
        return players.sortedWith(
            compareByDescending<Player> { it.id == preferredWinnerId }
                .thenBy { player ->
                    val idx = finishOrderPlayerIds.indexOf(player.id)
                    if (idx >= 0) idx else Int.MAX_VALUE
                }
                .thenByDescending { winningTeam?.containsPlayer(it.id) == true }
                .thenByDescending { it.resources.isAlive && it.id !in eliminatedOutPlayerIds }
                .thenByDescending { it.currentTile }
                .thenByDescending { it.laddersClimbed }
                .thenByDescending { it.resources.hp }
        )
    }
}
