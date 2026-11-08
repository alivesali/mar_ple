package com.example.engine

import com.example.model.Player
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager

/**
 * Result of checking whether the game or a round has finished.
 */
data class MatchCheckResult(
    val isGameOver: Boolean,
    val winner: Player? = null,
    val winningTeam: GameTeam? = null,
    val winReason: String? = null,
    val rankings: List<Player> = emptyList()
)

/**
 * Engine determining victory conditions, player eliminations, team victories, and rankings.
 */
class MatchResolutionEngine(
    val winningTile: Int = 100
) {
    /**
     * Checks if a player/team reached the final tile or if only one player/team survives.
     * Enforces that dead players are never declared winners.
     */
    fun checkMatchStatus(
        players: List<Player>,
        isEliminationEnabled: Boolean = false,
        teamManager: TeamManager? = null
    ): MatchCheckResult {
        if (players.isEmpty()) {
            return MatchCheckResult(isGameOver = false)
        }

        // Rank alive players first by tile (and HP tie-breaker), followed by dead players
        val sortedRankings = players.sortedWith(
            compareByDescending<Player> { it.resources.isAlive }
                .thenByDescending { it.currentTile }
                .thenByDescending { it.resources.hp }
        )

        // 1. Check final tile victory (Only an ALIVE player can be declared winner!)
        val tileWinner = players.find { it.currentTile >= winningTile && it.resources.isAlive }
        if (tileWinner != null) {
            val winningTeam = if (tileWinner.teamId != null && teamManager != null) {
                teamManager.getTeam(tileWinner.teamId)
            } else null

            val reason = if (winningTeam != null) {
                "پیروزی تیم «${winningTeam.name}»! «${tileWinner.name}» به خانه $winningTile رسید و تیم خود را قهرمان کرد!"
            } else {
                "رسیدن «${tileWinner.name}» به خانه $winningTile و فتح صفحه بازی!"
            }

            return MatchCheckResult(
                isGameOver = true,
                winner = tileWinner,
                winningTeam = winningTeam,
                winReason = reason,
                rankings = sortedRankings
            )
        }

        // 2. Check elimination rules (when HP <= 0)
        val alivePlayers = players.filter { it.resources.isAlive }

        // All players dead: game over with no winner
        if (alivePlayers.isEmpty()) {
            return MatchCheckResult(
                isGameOver = true,
                winner = null,
                winningTeam = null,
                winReason = "تمام بازیکنان در میدان نبرد حذف شدند! مسابقه بدون برنده به پایان رسید.",
                rankings = sortedRankings
            )
        }

        // Check Team elimination if teams are configured
        if (teamManager != null && teamManager.getAllTeams().size > 1 && (isEliminationEnabled || players.any { !it.resources.isAlive })) {
            val teamsWithAliveMembers = teamManager.getAllTeams().filter { team ->
                team.memberPlayerIds.any { memberId ->
                    players.find { it.id == memberId }?.resources?.isAlive == true
                }
            }

            if (teamsWithAliveMembers.size == 1 && teamsWithAliveMembers.size < teamManager.getAllTeams().size) {
                val survivingTeam = teamsWithAliveMembers.first()
                val topPlayer = alivePlayers.filter { survivingTeam.memberPlayerIds.contains(it.id) }
                    .maxByOrNull { it.currentTile }

                return MatchCheckResult(
                    isGameOver = true,
                    winner = topPlayer,
                    winningTeam = survivingTeam,
                    winReason = "تیم «${survivingTeam.name}» تنها تیم بازمانده در میدان نبرد است و برنده شد!",
                    rankings = sortedRankings
                )
            }
        }

        // Check Single player survival in elimination mode
        if (isEliminationEnabled && alivePlayers.size == 1 && players.size > 1) {
            val survivor = alivePlayers.first()
            val winningTeam = survivor.teamId?.let { teamManager?.getTeam(it) }

            return MatchCheckResult(
                isGameOver = true,
                winner = survivor,
                winningTeam = winningTeam,
                winReason = "«${survivor.name}» تنها بازمانده زنده در میدان نبرد!",
                rankings = sortedRankings
            )
        }

        return MatchCheckResult(
            isGameOver = false,
            rankings = sortedRankings
        )
    }
}
