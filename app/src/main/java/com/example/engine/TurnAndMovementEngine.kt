package com.example.engine

import com.example.model.Player

/**
 * Result of computing a movement step from a dice roll.
 */
data class MovementTargetResult(
    val startTile: Int,
    val diceRoll: Int,
    val targetTile: Int,
    val stepsPath: List<Int>,
    val isExactRollNeededFailure: Boolean,
    val neededRollForFinish: Int
)

/**
 * Dedicated engine managing turns, movement calculations, exact finish constraints,
 * and turn rotation logic cleanly separated from UI and ViewModel.
 */
class TurnAndMovementEngine(
    val maxTiles: Int = 100,
    val maxConsecutiveSixes: Int = 3
) {
    /**
     * Calculates the movement target given a player's start position and rolled dice.
     * Enforces the rule that reaching tile [maxTiles] requires an exact roll.
     * If the roll exceeds the distance to the final tile, the player cannot move forward past 100.
     */
    fun calculateMovement(
        currentTile: Int,
        diceRoll: Int
    ): MovementTargetResult {
        val distanceToFinish = maxTiles - currentTile

        if (distanceToFinish <= 6 && diceRoll > distanceToFinish) {
            // Overshot: Player stays in place or needs exact roll
            return MovementTargetResult(
                startTile = currentTile,
                diceRoll = diceRoll,
                targetTile = currentTile,
                stepsPath = listOf(currentTile),
                isExactRollNeededFailure = true,
                neededRollForFinish = distanceToFinish
            )
        }

        val destination = (currentTile + diceRoll).coerceAtMost(maxTiles)
        val path = if (destination > currentTile) {
            (currentTile + 1..destination).toList()
        } else {
            listOf(currentTile)
        }

        return MovementTargetResult(
            startTile = currentTile,
            diceRoll = diceRoll,
            targetTile = destination,
            stepsPath = path,
            isExactRollNeededFailure = false,
            neededRollForFinish = distanceToFinish
        )
    }

    /**
     * Determines whether the current player qualifies for an immediate extra turn
     * (e.g. rolled a 6, and has not exceeded the consecutive 6 limit).
     */
    fun shouldGrantExtraTurn(
        diceRoll: Int,
        currentConsecutiveSixes: Int
    ): Boolean {
        if (diceRoll == 6 && currentConsecutiveSixes < maxConsecutiveSixes) {
            return true
        }
        return false
    }

    /**
     * Computes the next active turn index among eligible alive players.
     * Guaranteed bounds-safe under all conditions:
     * - Does not throw IndexOutOfBoundsException when [activePlayers] size differs from [playerCount].
     * - Skips dead players when player states are provided.
     * - Returns 0 or valid index if no alive players or only 1 player remains without crashing.
     * - Safely handles empty lists and player counts of 1, 2, 8.
     */
    fun getNextTurnIndex(
        currentIndex: Int,
        playerCount: Int,
        activePlayers: List<Player> = emptyList()
    ): Int {
        if (activePlayers.isEmpty()) {
            if (playerCount <= 1) return 0
            val safeCurrent = if (currentIndex in 0 until playerCount) currentIndex else 0
            return (safeCurrent + 1) % playerCount
        }

        val total = activePlayers.size
        if (total <= 1) return 0

        val aliveIndices = activePlayers.indices.filter { activePlayers[it].resources.isAlive }
        if (aliveIndices.isEmpty()) {
            // No alive players: return safe bounded index without crashing
            return currentIndex.coerceIn(0, total - 1)
        }
        if (aliveIndices.size == 1) {
            return aliveIndices.first()
        }

        val safeCurrent = currentIndex.coerceIn(0, total - 1)
        var next = (safeCurrent + 1) % total
        var checked = 0
        while (checked < total) {
            if (activePlayers[next].resources.isAlive) {
                return next
            }
            next = (next + 1) % total
            checked++
        }

        return aliveIndices.first()
    }
}
