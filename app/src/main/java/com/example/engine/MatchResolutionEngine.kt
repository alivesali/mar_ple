package com.example.engine

import com.example.model.Player

/**
 * Result of checking whether the game or a round has finished.
 */
data class MatchCheckResult(
    val isGameOver: Boolean,
    val winner: Player? = null,
    val winReason: String? = null,
    val rankings: List<Player> = emptyList()
)

/**
 * Engine determining victory conditions, player eliminations, and rankings.
 */
class MatchResolutionEngine(
    val winningTile: Int = 100
) {
    /**
     * Checks if a player reached the final tile or if only one player/team survives.
     */
    fun checkMatchStatus(
        players: List<Player>,
        isEliminationEnabled: Boolean = false
    ): MatchCheckResult {
        // 1. Check tile 100 win
        val tileWinner = players.find { it.currentTile >= winningTile }
        if (tileWinner != null) {
            val sortedRankings = players.sortedByDescending { it.currentTile }
            return MatchCheckResult(
                isGameOver = true,
                winner = tileWinner,
                winReason = "رسیدن به خانه ۱۰۰ و فتح صفحه بازی!",
                rankings = sortedRankings
            )
        }

        // 2. Check elimination mode (if HP falls to 0)
        if (isEliminationEnabled) {
            val alivePlayers = players.filter { it.resources.isAlive }
            if (alivePlayers.size == 1) {
                val survivor = alivePlayers.first()
                return MatchCheckResult(
                    isGameOver = true,
                    winner = survivor,
                    winReason = "آخرین بازمانده میدان نبرد!",
                    rankings = listOf(survivor) + players.filterNot { it.resources.isAlive }
                )
            }
        }

        return MatchCheckResult(
            isGameOver = false,
            rankings = players.sortedByDescending { it.currentTile }
        )
    }
}
