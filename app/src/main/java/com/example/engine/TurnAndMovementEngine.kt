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
     * Computes the next active turn index among eligible players.
     */
    fun getNextTurnIndex(
        currentIndex: Int,
        playerCount: Int,
        activePlayers: List<Player> = emptyList()
    ): Int {
        if (playerCount <= 1) return 0
        var nextIndex = (currentIndex + 1) % playerCount
        // If player filtering is provided (e.g. alive players), advance to the next alive player
        if (activePlayers.isNotEmpty()) {
            var attempts = 0
            while (attempts < playerCount && !activePlayers[nextIndex].resources.isAlive) {
                nextIndex = (nextIndex + 1) % playerCount
                attempts++
            }
        }
        return nextIndex
    }
}
