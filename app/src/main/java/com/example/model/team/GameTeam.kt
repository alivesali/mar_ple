package com.example.model.team

import androidx.compose.ui.graphics.Color

/**
 * Represents a Team in the game.
 *
 * @param id Unique identifier of the team (e.g. "team_alpha", "team_beta").
 * @param name Display name in Persian/English (e.g. "تیم عقاب‌ها", "تیم شیرها").
 * @param bannerColor Distinctive theme color for team markers.
 * @param memberPlayerIds List of player IDs belonging to this team.
 */
data class GameTeam(
    val id: String,
    val name: String,
    val bannerColor: Color,
    val memberPlayerIds: List<Int> = emptyList()
) {
    init {
        require(id.isNotBlank()) { "Team ID must not be blank" }
        require(memberPlayerIds.size == memberPlayerIds.distinct().size) {
            "Team '$id' cannot contain duplicate member IDs: $memberPlayerIds"
        }
    }

    fun containsPlayer(playerId: Int): Boolean = memberPlayerIds.contains(playerId)
}

/**
 * Standard preset team configurations.
 */
enum class TeamMode(val persianTitle: String, val description: String) {
    SOLO_FREE_FOR_ALL("انفرادی (همه علیه همه)", "هر بازیکن در تیم اختصاصی خود بازی می‌کند"),
    DUOS_2V2("تیم‌های دو نفره", "بازیکنان در قالب تیم‌های ۲ نفره همکاری می‌کنند"),
    TRIOS_3V3("تیم‌های سه نفره", "رقابت گروهی در تیم‌های ۳ نفره"),
    CUSTOM_TEAMS("تیم‌های سفارشی", "تعداد و چینش تیم‌ها با انتخاب بازیکنان")
}

/**
 * Manages team membership, teammate relationships, and shared tile rules.
 * Strictly enforces that:
 * - Team IDs are unique.
 * - No player belongs to multiple teams concurrently.
 * - Bidirectional mappings (`teamsMap` and `playerToTeamMap`) remain in strict sync.
 * - Invalid assignments fail explicitly with [IllegalArgumentException].
 */
class TeamManager(
    initialTeams: List<GameTeam> = emptyList()
) {
    private val teamsMap: MutableMap<String, GameTeam> = mutableMapOf()
    private val playerToTeamMap: MutableMap<Int, String> = mutableMapOf()

    init {
        // 1. Verify unique team IDs
        val teamIds = initialTeams.map { it.id }
        require(teamIds.size == teamIds.distinct().size) {
            "Team IDs must be unique across all teams. Given: $teamIds"
        }

        // 2. Verify no player is in multiple teams
        val allMemberIds = initialTeams.flatMap { it.memberPlayerIds }
        require(allMemberIds.size == allMemberIds.distinct().size) {
            "A player cannot belong to multiple teams concurrently. Given duplicate member assignments: $allMemberIds"
        }

        // 3. Populate maps in sync
        initialTeams.forEach { team ->
            teamsMap[team.id] = team
            team.memberPlayerIds.forEach { playerId ->
                playerToTeamMap[playerId] = team.id
            }
        }
    }

    /**
     * Returns all currently registered teams.
     */
    fun getAllTeams(): List<GameTeam> = teamsMap.values.toList()

    /**
     * Gets a team by ID.
     */
    fun getTeam(teamId: String): GameTeam? = teamsMap[teamId]

    /**
     * Gets the team of a specific player.
     */
    fun getTeamForPlayer(playerId: Int): GameTeam? {
        val teamId = playerToTeamMap[playerId] ?: return null
        return teamsMap[teamId]
    }

    /**
     * Checks if two players are on the same team (teammates).
     * If either player has no team assigned or is in Solo mode, they are considered enemies.
     */
    fun areTeammates(player1Id: Int, player2Id: Int): Boolean {
        if (player1Id == player2Id) return true
        val team1 = playerToTeamMap[player1Id] ?: return false
        val team2 = playerToTeamMap[player2Id] ?: return false
        return team1 == team2
    }

    /**
     * Assigns a player to a team.
     * Fails explicitly if [teamId] does not exist.
     * Cleanly removes the player from their previous team (if any) and updates both data structures.
     */
    fun assignPlayerToTeam(playerId: Int, teamId: String) {
        val targetTeam = teamsMap[teamId]
            ?: throw IllegalArgumentException("Target team '$teamId' not found in registered teams")

        // Remove from old team if any
        val oldTeamId = playerToTeamMap[playerId]
        if (oldTeamId != null && oldTeamId != teamId) {
            val oldTeam = teamsMap[oldTeamId]
            if (oldTeam != null) {
                teamsMap[oldTeamId] = oldTeam.copy(
                    memberPlayerIds = oldTeam.memberPlayerIds.filter { it != playerId }
                )
            }
        }

        playerToTeamMap[playerId] = teamId
        if (!targetTeam.memberPlayerIds.contains(playerId)) {
            teamsMap[teamId] = targetTeam.copy(
                memberPlayerIds = targetTeam.memberPlayerIds + playerId
            )
        }
    }

    /**
     * Checks if multiple players can occupy the same board tile peacefully.
     * Teammates can occupy the exact same tile without triggering combat!
     * Enemies landing on the same tile will trigger a clash.
     */
    fun canOccupySameTilePeacefully(player1Id: Int, player2Id: Int): Boolean {
        return areTeammates(player1Id, player2Id)
    }

    /**
     * Creates default teams for a player count based on [TeamMode].
     */
    companion object {
        val TeamColors = listOf(
            Color(0xFF3B82F6), // Blue
            Color(0xFFEF4444), // Red
            Color(0xFF10B981), // Green
            Color(0xFFF59E0B), // Amber
            Color(0xFF8B5CF6), // Purple
            Color(0xFFEC4899), // Pink
            Color(0xFF06B6D4), // Cyan
            Color(0xFF84CC16)  // Lime
        )

        fun createDefaultTeams(playerCount: Int, mode: TeamMode): TeamManager {
            require(playerCount >= 1) {
                "playerCount must be at least 1, but was: $playerCount"
            }

            val teams = when (mode) {
                TeamMode.SOLO_FREE_FOR_ALL -> {
                    (0 until playerCount).map { idx ->
                        GameTeam(
                            id = "team_solo_$idx",
                            name = "تیم انفرادی ${idx + 1}",
                            bannerColor = TeamColors[idx % TeamColors.size],
                            memberPlayerIds = listOf(idx)
                        )
                    }
                }
                TeamMode.DUOS_2V2 -> {
                    val teamCount = (playerCount + 1) / 2
                    (0 until teamCount).map { teamIdx ->
                        val members = listOfNotNull(
                            teamIdx * 2,
                            if (teamIdx * 2 + 1 < playerCount) teamIdx * 2 + 1 else null
                        )
                        GameTeam(
                            id = "team_duo_$teamIdx",
                            name = if (teamIdx == 0) "تیم عقاب‌ها" else if (teamIdx == 1) "تیم شیرها" else "تیم ${teamIdx + 1}",
                            bannerColor = TeamColors[teamIdx % TeamColors.size],
                            memberPlayerIds = members
                        )
                    }
                }
                TeamMode.TRIOS_3V3 -> {
                    val teamCount = (playerCount + 2) / 3
                    (0 until teamCount).map { teamIdx ->
                        val members = (0 until 3).mapNotNull { offset ->
                            val pId = teamIdx * 3 + offset
                            if (pId < playerCount) pId else null
                        }
                        GameTeam(
                            id = "team_trio_$teamIdx",
                            name = "تیم ستاره ${teamIdx + 1}",
                            bannerColor = TeamColors[teamIdx % TeamColors.size],
                            memberPlayerIds = members
                        )
                    }
                }
                TeamMode.CUSTOM_TEAMS -> {
                    // Default to 2 balanced teams
                    val team1Members = (0 until playerCount step 2).toList()
                    val team2Members = (1 until playerCount step 2).toList()
                    listOf(
                        GameTeam("team_custom_1", "تیم آریا", TeamColors[0], team1Members),
                        GameTeam("team_custom_2", "تیم کاوه", TeamColors[1], team2Members)
                    )
                }
            }
            return TeamManager(teams)
        }
    }
}
