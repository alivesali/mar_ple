package com.example.engine

import kotlin.random.Random

/**
 * Interface abstracting random number generation so game logic and combat
 * can be tested deterministically without real chance or non-reproducible flakes.
 */
interface RandomProvider {
    fun nextInt(from: Int, until: Int): Int
    fun nextDouble(): Double
    fun nextBoolean(): Boolean
}

/**
 * Default implementation backed by standard [kotlin.random.Random].
 */
class DefaultRandomProvider(
    private val random: Random = Random.Default
) : RandomProvider {
    override fun nextInt(from: Int, until: Int): Int = random.nextInt(from, until)
    override fun nextDouble(): Double = random.nextDouble()
    override fun nextBoolean(): Boolean = random.nextBoolean()
}

/**
 * Fully deterministic implementation for Unit Testing.
 * Allows prescribing exact sequences of integers, doubles, and booleans.
 */
class DeterministicRandomProvider(
    private val intSequence: List<Int> = emptyList(),
    private val doubleSequence: List<Double> = emptyList(),
    private val booleanSequence: List<Boolean> = emptyList()
) : RandomProvider {
    private var intIndex = 0
    private var doubleIndex = 0
    private var booleanIndex = 0

    var defaultIntFallback: Int = 1
    var defaultDoubleFallback: Double = 0.5
    var defaultBooleanFallback: Boolean = false

    override fun nextInt(from: Int, until: Int): Int {
        if (intIndex < intSequence.size) {
            val value = intSequence[intIndex++]
            return value.coerceIn(from, until - 1)
        }
        return defaultIntFallback.coerceIn(from, until - 1)
    }

    override fun nextDouble(): Double {
        if (doubleIndex < doubleSequence.size) {
            return doubleSequence[doubleIndex++]
        }
        return defaultDoubleFallback
    }

    override fun nextBoolean(): Boolean {
        if (booleanIndex < booleanSequence.size) {
            return booleanSequence[booleanIndex++]
        }
        return defaultBooleanFallback
    }
}
