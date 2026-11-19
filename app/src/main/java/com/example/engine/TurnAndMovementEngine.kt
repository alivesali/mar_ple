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
 * Decision on whether the active player receives an extra roll and updated consecutive-sixes streak.
 */
data class BonusTurnDecision(
    val grantExtraTurn: Boolean,
    val nextConsecutiveSixes: Int
)

/**
 * Metadata for an eliminated or status-skipped player passed during turn selection.
 */
data class SkippedPlayerTurn(
    val playerIndex: Int,
    val playerId: Int,
    val isFrozen: Boolean
)

/**
 * Result of selecting the next playable turn index safely without infinite loops.
 */
data class TurnSelectionResult(
    val nextIndex: Int,
    val skippedPlayers: List<SkippedPlayerTurn>,
    val allAliveSkipped: Boolean,
    val noAlivePlayers: Boolean
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
     * Works dynamically for any board size (50, 100, 150, 200) and any step count (1..12).
     */
    fun calculateMovement(
        currentTile: Int,
        diceRoll: Int
    ): MovementTargetResult {
        val distanceToFinish = (maxTiles - currentTile).coerceAtLeast(0)

        if (distanceToFinish <= 0 || diceRoll > distanceToFinish) {
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
     * Computes bonus turn eligibility and updates consecutive sixes streak,
     * respecting the [maxConsecutiveSixes] cap and preventing double-granting when
     * both a bonus roll (6 / Fortune Five) and Luck obstacle extra turn occur together.
     */
    fun resolveBonusTurn(
        rolledSixOrFortuneFive: Boolean,
        luckExtraTurn: Boolean,
        currentConsecutiveSixes: Int
    ): BonusTurnDecision {
        return when {
            rolledSixOrFortuneFive && currentConsecutiveSixes < maxConsecutiveSixes -> {
                BonusTurnDecision(
                    grantExtraTurn = true,
                    nextConsecutiveSixes = currentConsecutiveSixes + 1
                )
            }
            luckExtraTurn -> {
                BonusTurnDecision(
                    grantExtraTurn = true,
                    nextConsecutiveSixes = 0
                )
            }
            else -> {
                BonusTurnDecision(
                    grantExtraTurn = false,
                    nextConsecutiveSixes = 0
                )
            }
        }
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
        if (activePlayers.isNotEmpty()) {
            var attempts = 0
            while (attempts < playerCount && !activePlayers[nextIndex].resources.isAlive) {
                nextIndex = (nextIndex + 1) % playerCount
                attempts++
            }
        }
        return nextIndex
    }

    /**
     * Advances to the next alive and non-skipped player cleanly.
     * Guarantees termination without infinite loops even when all players are dead,
     * frozen, or stuck in swamps.
     */
    fun resolveNextPlayableTurn(
        currentIndex: Int,
        players: List<Player>,
        finishedPlayerIds: Set<Int> = emptySet()
    ): TurnSelectionResult {
        if (players.isEmpty()) {
            return TurnSelectionResult(
                nextIndex = 0,
                skippedPlayers = emptyList(),
                allAliveSkipped = false,
                noAlivePlayers = true
            )
        }

        val eligiblePlayers = players.filter { it.resources.isAlive && it.id !in finishedPlayerIds }
        if (eligiblePlayers.isEmpty()) {
            return TurnSelectionResult(
                nextIndex = 0,
                skippedPlayers = emptyList(),
                allAliveSkipped = false,
                noAlivePlayers = true
            )
        }

        val count = players.size
        val skipped = mutableListOf<SkippedPlayerTurn>()

        for (step in 1..count) {
            val candidateIdx = (currentIndex + step) % count
            val candidate = players[candidateIdx]
            if (!candidate.resources.isAlive || candidate.id in finishedPlayerIds) continue

            val hasSkip = candidate.swampSkipTurnsRemaining > 0 || candidate.frozenSkipTurnsRemaining > 0
            if (hasSkip) {
                skipped.add(
                    SkippedPlayerTurn(
                        playerIndex = candidateIdx,
                        playerId = candidate.id,
                        isFrozen = candidate.frozenSkipTurnsRemaining > 0
                    )
                )
            } else {
                return TurnSelectionResult(
                    nextIndex = candidateIdx,
                    skippedPlayers = skipped,
                    allAliveSkipped = false,
                    noAlivePlayers = false
                )
            }
        }

        // All eligible players had skip turns: after decrementing one round of skips, select next eligible player.
        val fallbackNextAlive = (1..count)
            .map { (currentIndex + it) % count }
            .firstOrNull { players[it].resources.isAlive && players[it].id !in finishedPlayerIds }
            ?: 0

        return TurnSelectionResult(
            nextIndex = fallbackNextAlive,
            skippedPlayers = skipped,
            allAliveSkipped = true,
            noAlivePlayers = false
        )
    }
}
