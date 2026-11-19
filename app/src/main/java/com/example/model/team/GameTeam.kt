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
 * Rules for how a team wins the match when reaching the final tile.
 */
enum class TeamVictoryMode(val persianTitle: String, val description: String) {
    ONE_MEMBER_FINISH(
        persianTitle = "رسیدن اولین عضو تیم",
        description = "به محض رسیدن اولین عضو فعال تیم به خانهٔ آخر، کل تیم برنده می‌شود"
    ),
    ALL_MEMBERS_FINISH(
        persianTitle = "رسیدن تمام اعضای تیم",
        description = "تمام اعضای فعال تیم باید به خانهٔ آخر برسند تا تیم قهرمان شود"
    )
}

/**
 * Manages team membership, teammate relationships, and shared tile rules.
 */
class TeamManager(
    initialTeams: List<GameTeam> = emptyList()
) {
    private val teamsMap: MutableMap<String, GameTeam> = linkedMapOf()
    private val playerToTeamMap: MutableMap<Int, String> = mutableMapOf()

    init {
        initialTeams.forEach { rawTeam ->
            require(!teamsMap.containsKey(rawTeam.id)) {
                "شناسهٔ تیم «${rawTeam.id}» تکراری است."
            }
            val uniqueMembers = rawTeam.memberPlayerIds.distinct()
            uniqueMembers.forEach { playerId ->
                require(!playerToTeamMap.containsKey(playerId)) {
                    "بازیکن $playerId نمی‌تواند هم‌زمان عضو بیش از یک تیم باشد."
                }
                playerToTeamMap[playerId] = rawTeam.id
            }
            teamsMap[rawTeam.id] = rawTeam.copy(memberPlayerIds = uniqueMembers)
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
     * Assigns a player to an existing team cleanly without duplicate membership.
     */
    fun assignPlayerToTeam(playerId: Int, teamId: String) {
        val targetTeam = teamsMap[teamId]
            ?: throw IllegalArgumentException("تیم با شناسهٔ «$teamId» وجود ندارد.")

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

    companion object {
        val CustomTeamPresets: List<Triple<String, String, Color>> = listOf(
            Triple("team_custom_1", "تیم آریا", Color(0xFF3B82F6)),
            Triple("team_custom_2", "تیم کاوه", Color(0xFFEF4444)),
            Triple("team_custom_3", "تیم سیمرغ", Color(0xFF10B981)),
            Triple("team_custom_4", "تیم زاگرس", Color(0xFFF59E0B))
        )

        private val TeamColors = listOf(
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

        fun createCustomTeams(
            playerCount: Int,
            customAssignments: Map<Int, String>
        ): TeamManager {
            val validPresetIds = CustomTeamPresets.map { it.first }.toSet()
            val buckets = CustomTeamPresets.associate { it.first to mutableListOf<Int>() }
            for (playerId in 0 until playerCount) {
                val chosenTeamId = customAssignments[playerId]?.takeIf { it in validPresetIds }
                    ?: CustomTeamPresets[playerId % 2].first
                buckets.getValue(chosenTeamId).add(playerId)
            }
            val teams = CustomTeamPresets.mapNotNull { (id, name, color) ->
                val members = buckets.getValue(id)
                if (members.isNotEmpty() || id == "team_custom_1" || id == "team_custom_2") {
                    GameTeam(
                        id = id,
                        name = name,
                        bannerColor = color,
                        memberPlayerIds = members.toList()
                    )
                } else {
                    null
                }
            }
            return TeamManager(teams)
        }
    }
}
