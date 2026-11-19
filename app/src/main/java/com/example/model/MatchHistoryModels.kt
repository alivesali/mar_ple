package com.example.model

import com.example.model.team.TeamMode
import com.example.model.team.TeamVictoryMode
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Snapshot of a single player's outcome inside a completed match.
 */
data class MatchParticipantRecord(
    val playerId: Int,
    val playerName: String,
    val characterId: String,
    val characterName: String,
    val characterTitle: String,
    val characterBadgeEmoji: String,
    val pawnSkinId: String,
    val teamId: String? = null,
    val teamName: String? = null,
    val rank: Int,
    val finalTile: Int,
    val isWinner: Boolean,
    val isEliminated: Boolean,
    val isAiBot: Boolean = false,
    val laddersClimbed: Int = 0,
    val snakesBitten: Int = 0,
    val obstaclesTriggered: Int = 0,
    val totalSixes: Int = 0,
    val remainingHp: Int = 100
)

/**
 * Immutable historical record of a definitively finished match.
 * Unfinished/in-progress matches are never represented or stored here.
 */
data class MatchHistoryRecord(
    val id: Long = 0L,
    val matchUuid: String,
    val completedAtMillis: Long,
    val maxTiles: Int,
    val difficulty: DifficultyLevel,
    val boardTheme: BoardTheme,
    val teamMode: TeamMode,
    val teamVictoryMode: TeamVictoryMode,
    val playerCount: Int,
    val isDraw: Boolean,
    val winnerPlayerId: Int? = null,
    val winnerName: String? = null,
    val winnerCharacterTitle: String? = null,
    val winningTeamId: String? = null,
    val winningTeamName: String? = null,
    val winReason: String? = null,
    val participants: List<MatchParticipantRecord> = emptyList()
) {
    val isTeamMatch: Boolean
        get() = teamMode != TeamMode.SOLO_FREE_FOR_ALL

    fun formattedCompletedAt(): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.US)
        return formatter.format(Date(completedAtMillis)).toPersianDigitsString()
    }
}

fun String.toPersianDigitsString(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val sb = StringBuilder(length)
    for (ch in this) {
        if (ch in '0'..'9') {
            sb.append(persianDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

/**
 * Aggregated performance metrics for a player across finished matches.
 */
data class PlayerHistoryStats(
    val playerName: String,
    val matchesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val winRatePercent: Int,
    val bestRank: Int,
    val averageRank: Double,
    val laddersClimbed: Int,
    val snakesBitten: Int,
    val isAiBot: Boolean = false
)

/**
 * Aggregated performance metrics for a team across finished team matches.
 */
data class TeamHistoryStats(
    val teamId: String,
    val teamName: String,
    val matchesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val winRatePercent: Int,
    val bestRank: Int
)

/**
 * Global summary counters computed from recorded match history.
 */
data class MatchHistoryOverviewStats(
    val totalMatches: Int,
    val decisiveMatches: Int,
    val drawMatches: Int,
    val teamMatches: Int
)

/**
 * Lightweight JSON codec using Android's built-in org.json so Room persists participant rankings
 * cleanly without extra external dependencies.
 */
object MatchParticipantsJsonCodec {

    fun encode(participants: List<MatchParticipantRecord>): String {
        val array = JSONArray()
        participants.forEach { p ->
            val obj = JSONObject().apply {
                put("playerId", p.playerId)
                put("playerName", p.playerName)
                put("characterId", p.characterId)
                put("characterName", p.characterName)
                put("characterTitle", p.characterTitle)
                put("characterBadgeEmoji", p.characterBadgeEmoji)
                put("pawnSkinId", p.pawnSkinId)
                put("teamId", p.teamId ?: JSONObject.NULL)
                put("teamName", p.teamName ?: JSONObject.NULL)
                put("rank", p.rank)
                put("finalTile", p.finalTile)
                put("isWinner", p.isWinner)
                put("isEliminated", p.isEliminated)
                put("isAiBot", p.isAiBot)
                put("laddersClimbed", p.laddersClimbed)
                put("snakesBitten", p.snakesBitten)
                put("obstaclesTriggered", p.obstaclesTriggered)
                put("totalSixes", p.totalSixes)
                put("remainingHp", p.remainingHp)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun decode(rawJson: String): List<MatchParticipantRecord> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(rawJson)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val teamIdRaw = if (obj.isNull("teamId")) null else obj.optString("teamId", "").ifBlank { null }
                    val teamNameRaw = if (obj.isNull("teamName")) null else obj.optString("teamName", "").ifBlank { null }
                    add(
                        MatchParticipantRecord(
                            playerId = obj.optInt("playerId", i),
                            playerName = obj.optString("playerName", "بازیکن"),
                            characterId = obj.optString("characterId", ""),
                            characterName = obj.optString("characterName", ""),
                            characterTitle = obj.optString("characterTitle", ""),
                            characterBadgeEmoji = obj.optString("characterBadgeEmoji", "🎲"),
                            pawnSkinId = obj.optString("pawnSkinId", "classic_standard"),
                            teamId = teamIdRaw,
                            teamName = teamNameRaw,
                            rank = obj.optInt("rank", i + 1),
                            finalTile = obj.optInt("finalTile", 1),
                            isWinner = obj.optBoolean("isWinner", false),
                            isEliminated = obj.optBoolean("isEliminated", false),
                            isAiBot = obj.optBoolean("isAiBot", false),
                            laddersClimbed = obj.optInt("laddersClimbed", 0),
                            snakesBitten = obj.optInt("snakesBitten", 0),
                            obstaclesTriggered = obj.optInt("obstaclesTriggered", 0),
                            totalSixes = obj.optInt("totalSixes", 0),
                            remainingHp = obj.optInt("remainingHp", 100)
                        )
                    )
                }
            }.sortedBy { it.rank }
        }.getOrDefault(emptyList())
    }
}

/**
 * Pure domain calculator deriving overview, player, and team statistics from finished matches.
 */
object MatchStatisticsCalculator {

    private data class MutablePlayerAccumulator(
        val displayName: String,
        var matchesPlayed: Int = 0,
        var wins: Int = 0,
        var draws: Int = 0,
        var bestRank: Int = Int.MAX_VALUE,
        var totalRankSum: Int = 0,
        var laddersClimbed: Int = 0,
        var snakesBitten: Int = 0,
        var isAiBot: Boolean = false
    )

    private data class MutableTeamAccumulator(
        val teamId: String,
        val teamName: String,
        var matchesPlayed: Int = 0,
        var wins: Int = 0,
        var draws: Int = 0,
        var bestRank: Int = Int.MAX_VALUE
    )

    fun computeOverview(records: List<MatchHistoryRecord>): MatchHistoryOverviewStats {
        val total = records.size
        val draws = records.count { it.isDraw }
        val teamMatches = records.count { it.isTeamMatch }
        return MatchHistoryOverviewStats(
            totalMatches = total,
            decisiveMatches = total - draws,
            drawMatches = draws,
            teamMatches = teamMatches
        )
    }

    fun computePlayerStats(records: List<MatchHistoryRecord>): List<PlayerHistoryStats> {
        if (records.isEmpty()) return emptyList()

        val map = linkedMapOf<String, MutablePlayerAccumulator>()
        records.forEach { match ->
            // Deduplicate by normalized player name per match so win rate stays within [0, 100]
            val groupedByName = match.participants.groupBy { participant ->
                normalizeNameKey(participant.playerName)
            }
            groupedByName.forEach { (key, slots) ->
                val primary = slots.minByOrNull { it.rank } ?: return@forEach
                val displayName = primary.playerName.trim().ifBlank { "بازیکن" }
                val acc = map.getOrPut(key) {
                    MutablePlayerAccumulator(
                        displayName = displayName,
                        isAiBot = primary.isAiBot
                    )
                }
                acc.matchesPlayed += 1
                val wonThisMatch = !match.isDraw && slots.any { it.isWinner }
                if (wonThisMatch) {
                    acc.wins += 1
                } else if (match.isDraw) {
                    acc.draws += 1
                }
                val bestMatchRank = slots.minOf { it.rank.coerceAtLeast(1) }
                if (bestMatchRank < acc.bestRank) {
                    acc.bestRank = bestMatchRank
                }
                acc.totalRankSum += bestMatchRank
                acc.laddersClimbed += slots.sumOf { it.laddersClimbed }
                acc.snakesBitten += slots.sumOf { it.snakesBitten }
                if (!primary.isAiBot) {
                    acc.isAiBot = false
                }
            }
        }

        return map.values.map { acc ->
            val winRate = if (acc.matchesPlayed > 0) {
                ((acc.wins * 100.0) / acc.matchesPlayed).roundToInt().coerceIn(0, 100)
            } else {
                0
            }
            val avgRank = if (acc.matchesPlayed > 0) {
                acc.totalRankSum.toDouble() / acc.matchesPlayed
            } else {
                0.0
            }
            PlayerHistoryStats(
                playerName = acc.displayName,
                matchesPlayed = acc.matchesPlayed,
                wins = acc.wins,
                draws = acc.draws,
                winRatePercent = winRate,
                bestRank = if (acc.bestRank == Int.MAX_VALUE) 1 else acc.bestRank,
                averageRank = avgRank,
                laddersClimbed = acc.laddersClimbed,
                snakesBitten = acc.snakesBitten,
                isAiBot = acc.isAiBot
            )
        }.sortedWith(
            compareByDescending<PlayerHistoryStats> { it.wins }
                .thenByDescending { it.winRatePercent }
                .thenBy { it.bestRank }
                .thenByDescending { it.matchesPlayed }
                .thenBy { it.playerName }
        )
    }

    fun computeTeamStats(records: List<MatchHistoryRecord>): List<TeamHistoryStats> {
        if (records.isEmpty()) return emptyList()

        val map = linkedMapOf<String, MutableTeamAccumulator>()
        records.forEach { match ->
            if (!match.isTeamMatch) return@forEach

            val teamGroups = match.participants
                .filter { !it.teamName.isNullOrBlank() || !it.teamId.isNullOrBlank() }
                .groupBy { p ->
                    val resolvedId = p.teamId?.takeIf { it.isNotBlank() }
                    val resolvedName = p.teamName?.takeIf { it.isNotBlank() }
                    resolvedId ?: resolvedName ?: "team_unknown"
                }

            if (teamGroups.isEmpty()) return@forEach

            // Rank teams in this match by whether they won, then by best member rank
            val orderedTeamsInMatch = teamGroups.entries.sortedWith(
                compareByDescending<Map.Entry<String, List<MatchParticipantRecord>>> { entry ->
                    val tId = entry.key
                    val tName = entry.value.firstNotNullOfOrNull { it.teamName }
                    !match.isDraw && (
                        (match.winningTeamId != null && match.winningTeamId == tId) ||
                            (match.winningTeamName != null && match.winningTeamName == tName)
                        )
                }.thenBy { entry ->
                    entry.value.minOfOrNull { it.rank } ?: Int.MAX_VALUE
                }
            )

            orderedTeamsInMatch.forEachIndexed { teamIdx, (teamIdKey, members) ->
                val teamName = members.firstNotNullOfOrNull { it.teamName?.trim()?.takeIf { n -> n.isNotEmpty() } }
                    ?: (if (match.winningTeamId == teamIdKey) match.winningTeamName else null)
                    ?: teamIdKey
                val accKey = "${teamIdKey}::${normalizeNameKey(teamName)}"
                val acc = map.getOrPut(accKey) {
                    MutableTeamAccumulator(
                        teamId = teamIdKey,
                        teamName = teamName
                    )
                }
                acc.matchesPlayed += 1
                val isWinningTeam = !match.isDraw && (
                    (match.winningTeamId != null && match.winningTeamId == teamIdKey) ||
                        (match.winningTeamName != null && match.winningTeamName == teamName)
                    )
                if (isWinningTeam) {
                    acc.wins += 1
                } else if (match.isDraw) {
                    acc.draws += 1
                }
                val matchTeamRank = if (isWinningTeam) 1 else (teamIdx + 1)
                if (matchTeamRank < acc.bestRank) {
                    acc.bestRank = matchTeamRank
                }
            }
        }

        return map.values.map { acc ->
            val winRate = if (acc.matchesPlayed > 0) {
                ((acc.wins * 100.0) / acc.matchesPlayed).roundToInt().coerceIn(0, 100)
            } else {
                0
            }
            TeamHistoryStats(
                teamId = acc.teamId,
                teamName = acc.teamName,
                matchesPlayed = acc.matchesPlayed,
                wins = acc.wins,
                draws = acc.draws,
                winRatePercent = winRate,
                bestRank = if (acc.bestRank == Int.MAX_VALUE) 1 else acc.bestRank
            )
        }.sortedWith(
            compareByDescending<TeamHistoryStats> { it.wins }
                .thenByDescending { it.winRatePercent }
                .thenBy { it.bestRank }
                .thenByDescending { it.matchesPlayed }
                .thenBy { it.teamName }
        )
    }

    private fun normalizeNameKey(raw: String): String {
        return raw.trim().lowercase(Locale.ROOT).ifEmpty { "player" }
    }
}
