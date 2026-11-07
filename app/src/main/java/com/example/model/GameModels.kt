package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PlayerColorNames
import com.example.ui.theme.PlayerTokenColors
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

enum class LinkType {
    SNAKE,
    LADDER
}

data class BoardLink(
    val startTile: Int,
    val endTile: Int,
    val type: LinkType,
    val isNearWormZone: Boolean = false
)

enum class DifficultyLevel(
    val persianTitle: String,
    val subtitle: String,
    val badgeColor: Color,
    val ladderCount: Int,
    val snakeCount: Int,
    val minSnakeDrop: Int,
    val maxSnakeDrop: Int,
    val obstacleCountRange: IntRange
) {
    EASY(
        persianTitle = "آسان",
        subtitle = "مارهای کمتر و کوتاه‌تر • پله‌های بیشتر • موانع ملایم",
        badgeColor = Color(0xFF10B981),
        ladderCount = 8,
        snakeCount = 5,
        minSnakeDrop = 6,
        maxSnakeDrop = 18,
        obstacleCountRange = 6..8
    ),
    MEDIUM(
        persianTitle = "متوسط",
        subtitle = "تعادل عالی بین شانس، هیجان و موانع غافلگیرکننده",
        badgeColor = Color(0xFFFFB703),
        ladderCount = 7,
        snakeCount = 7,
        minSnakeDrop = 10,
        maxSnakeDrop = 32,
        obstacleCountRange = 10..12
    ),
    HARD(
        persianTitle = "سخت",
        subtitle = "مارهای بلند و خطرناک • موانع پرتکرار و نفس‌گیر!",
        badgeColor = Color(0xFFEF4444),
        ladderCount = 6,
        snakeCount = 10,
        minSnakeDrop = 16,
        maxSnakeDrop = 52,
        obstacleCountRange = 14..17
    )
}

enum class ObstacleType(
    val persianName: String,
    val shortDescription: String,
    val badgeColor: Color,
    val secondaryColor: Color,
    val isDangerous: Boolean
) {
    WORM(
        persianName = "کرم لزج",
        shortDescription = "تا ۳ نوبت شانس تاس ۱ و ۲ را زیاد می‌کند و مارهای اطراف را خشمگین می‌سازد",
        badgeColor = Color(0xFF84CC16),
        secondaryColor = Color(0xFF3F6212),
        isDangerous = true
    ),
    REVERSE(
        persianName = "خانه معکوس",
        shortDescription = "به اندازه عدد تاسی که آوردی به عقب برمی‌گردی!",
        badgeColor = Color(0xFFF97316),
        secondaryColor = Color(0xFF9A3412),
        isDangerous = true
    ),
    SWAMP(
        persianName = "باتلاق سنگین",
        shortDescription = "یک نوبت کامل گیر می‌کنی و تاس نوبت بعدت نصف می‌شود",
        badgeColor = Color(0xFF65A30D),
        secondaryColor = Color(0xFF1E293B),
        isDangerous = true
    ),
    WELL(
        persianName = "چاه تاریک",
        shortDescription = "سقوط ناگهانی به درون چاه و پرتاب به یک خانه پایین‌تر!",
        badgeColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFF1E1B4B),
        isDangerous = true
    ),
    WIND(
        persianName = "خانه باد",
        shortDescription = "وزش باد اسرارآمیز: ۵۰٪ پرتاب دو برابر جلو یا ۵۰٪ برگشت نصف تاس!",
        badgeColor = Color(0xFF06B6D4),
        secondaryColor = Color(0xFF0E7490),
        isDangerous = false
    ),
    LUCK(
        persianName = "خانه شانس",
        shortDescription = "جعبه جادویی با اثر تصادفی: جهش جلو، تاس دوباره یا عقب‌گرد کوچک!",
        badgeColor = Color(0xFFEAB308),
        secondaryColor = Color(0xFF854D0E),
        isDangerous = false
    ),
    STORM(
        persianName = "خانه طوفان",
        shortDescription = "گردباد سراسری! همهٔ بازیکنان هم‌زمان ۱ خانه به جلو یا عقب پرتاب می‌شوند!",
        badgeColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFF4C1D95),
        isDangerous = true
    )
}

data class BoardObstacle(
    val tile: Int,
    val type: ObstacleType
)

data class BoardConfiguration(
    val links: List<BoardLink>,
    val obstacles: List<BoardObstacle>,
    val maxTiles: Int = 100
)

/**
 * Phase 5: Saved Settings Profile stored permanently in Local Storage (Room SQLite).
 */
data class SettingsProfile(
    val id: Long = 0L,
    val profileName: String,
    val maxTiles: Int,
    val difficulty: DifficultyLevel,
    val boardTheme: BoardTheme,
    val aiDifficulty: AiDifficulty,
    val playerCount: Int,
    val enabledObstacles: Set<ObstacleType>,
    val enabledCardCategories: Set<CardCategory>,
    val environmentEffect: EnvironmentEffectType = EnvironmentEffectType.GOLDEN_DUST,
    val effectIntensity: EffectIntensity = EffectIntensity.MEDIUM,
    val isEnvironmentEffectsEnabled: Boolean = true,
    val musicGenre: MusicGenre = MusicGenre.ROYAL,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class Player(
    val id: Int,
    val name: String, // Custom player name or default English character name
    val colorIndex: Int,
    val characterId: String = CharacterRoster.allCharacters[id % CharacterRoster.allCharacters.size].id,
    val customCharacterOverride: GameCharacter? = null,
    val isAiBot: Boolean = false,
    val currentTile: Int = 1,
    val totalSixes: Int = 0,
    val laddersClimbed: Int = 0,
    val snakesBitten: Int = 0,
    val obstaclesTriggered: Int = 0,
    // Phase 2 Status Effects:
    val wormCurseTurnsRemaining: Int = 0,
    val swampSkipTurnsRemaining: Int = 0,
    val halveNextRoll: Boolean = false,
    // Phase 3 & Redesigned Hero Ability States:
    val abilityChargeProgress: Int = 4, // Starts charged for standard active abilities
    val isShieldCharged: Boolean = true,
    val hasUsedOnceAbility: Boolean = false,
    val isActiveAbilityArmedForTurn: Boolean = false,
    val frozenSkipTurnsRemaining: Int = 0,
    // Personal Turn & Leader Swap (Raven) 10-Turn Cycle State:
    val personalTurnsTaken: Int = 0,
    val leaderSwapNextChargeTurn: Int = 10,
    val leaderSwapWindowTurnsRemaining: Int = 0,
    // Per-match usage counter & tactical hero states:
    val abilityUsesCount: Int = 0,
    val previousTurnTile: Int = 1,
    val dragonSoulBoostTurns: Int = 0,
    val berserkerRageTurns: Int = 0,
    val phantomPhaseActive: Boolean = false,
    val guardianAngelProtected: Boolean = false,
    val illusionDecoyTile: Int? = null,
    val illusionTurnsRemaining: Int = 0,
    val gravityCapNextRoll: Boolean = false,
    val pinnedSlowSteps: Int = 0
) {
    val color: Color
        get() = PlayerTokenColors[colorIndex % PlayerTokenColors.size]

    val colorName: String
        get() = PlayerColorNames[colorIndex % PlayerColorNames.size]

    val character: GameCharacter
        get() = customCharacterOverride ?: CharacterRoster.getById(characterId)

    val hasActiveDebuff: Boolean
        get() = wormCurseTurnsRemaining > 0 ||
            swampSkipTurnsRemaining > 0 ||
            halveNextRoll ||
            frozenSkipTurnsRemaining > 0 ||
            gravityCapNextRoll ||
            pinnedSlowSteps > 0

    val remainingAbilityUses: Int
        get() = (character.abilityType.maxUsesPerMatch - abilityUsesCount).coerceAtLeast(0)

    val canTriggerActiveAbilityNow: Boolean
        get() {
            val ability = character.abilityType
            if (ability.maxUsesPerMatch > 0 && abilityUsesCount >= ability.maxUsesPerMatch) {
                return false
            }
            if (ability == CharacterAbilityType.SHADOW_WALKER) {
                return leaderSwapWindowTurnsRemaining > 0 && !isActiveAbilityArmedForTurn
            }
            return when (ability.activationMode) {
                AbilityActivationMode.PASSIVE -> false
                AbilityActivationMode.AUTO_CHARGE -> false
                AbilityActivationMode.ACTIVE_COOLDOWN ->
                    abilityChargeProgress >= ability.cooldownTurns && !isActiveAbilityArmedForTurn
                AbilityActivationMode.ACTIVE_ONCE ->
                    !hasUsedOnceAbility && !isActiveAbilityArmedForTurn
            }
        }

    fun buildAbilityStatusBadgeText(): String {
        val ability = character.abilityType
        if (isActiveAbilityArmedForTurn) {
            return "🔥 آمادهٔ شلیک در این پرتاب!"
        }
        if (ability.maxUsesPerMatch > 0 && abilityUsesCount >= ability.maxUsesPerMatch) {
            return "اتمام سهمیه در این بازی (${ability.maxUsesPerMatch.toPersianDigits()}/${ability.maxUsesPerMatch.toPersianDigits()})"
        }
        if (ability == CharacterAbilityType.SHADOW_WALKER) {
            return if (leaderSwapWindowTurnsRemaining > 0) {
                "⚡ آمادهٔ جابه‌جایی با نفر اول! (فرصت: ${leaderSwapWindowTurnsRemaining.toPersianDigits()} نوبت اختصاصی)"
            } else {
                val turnsLeft = (leaderSwapNextChargeTurn - personalTurnsTaken).coerceAtLeast(1)
                "⏳ شارژ جابه‌جایی پیشتاز: نوبت اختصاصی ${personalTurnsTaken.toPersianDigits()}/${leaderSwapNextChargeTurn.toPersianDigits()} (${turnsLeft.toPersianDigits()} نوبت مانده)"
            }
        }
        return when (ability.activationMode) {
            AbilityActivationMode.PASSIVE -> "⚡ قابلیت همیشگی فعال"
            AbilityActivationMode.AUTO_CHARGE -> if (isShieldCharged) "🛡️ سپر آماده" else "⏳ شارژ سپر: ${abilityChargeProgress.toPersianDigits()}/${ability.cooldownTurns.toPersianDigits()}"
            AbilityActivationMode.ACTIVE_COOLDOWN -> {
                if (abilityChargeProgress >= ability.cooldownTurns) {
                    "⚡ آماده (باقی‌مانده: ${remainingAbilityUses.toPersianDigits()} بار)"
                } else {
                    "⏳ شارژ: ${abilityChargeProgress.toPersianDigits()}/${ability.cooldownTurns.toPersianDigits()} نوبت"
                }
            }
            AbilityActivationMode.ACTIVE_ONCE -> if (!hasUsedOnceAbility) "⚡ آماده (۱ بار در بازی)" else "استفاده شده"
        }
    }
}

enum class GamePhase {
    SETUP,
    PLAYING,
    CELEBRATION
}

enum class BannerType {
    NORMAL,
    SIX_BONUS,
    LADDER_CLIMB,
    SNAKE_BITE,
    EXACT_NEEDED,
    OBSTACLE_DANGER,
    OBSTACLE_SURPRISE,
    STORM_CHAOS,
    CHARACTER_ABILITY
}

data class GameEventBanner(
    val message: String,
    val type: BannerType = BannerType.NORMAL
)

/**
 * Interactive choice state when High Precision (2 dice rolled simultaneously) triggers!
 */
data class PrecisionRollChoice(
    val die1: Int,
    val die2: Int
) {
    val sum: Int get() = die1 + die2
}

fun Int.toPersianDigits(): String {
    val persianChars = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.toString().map { ch ->
        if (ch in '0'..'9') persianChars[ch - '0'] else ch
    }.joinToString("")
}

/**
 * Maps any tile `1..maxTiles` to `(rowFromBottom, colFromLeft)` on a 10-column serpentine grid.
 * Supports 50 (5x10), 100 (10x10), 150 (15x10), 200 (20x10), etc.
 */
fun tileToGridPosition(tile: Int, maxTiles: Int = 100): Pair<Int, Int> {
    val safeMax = maxTiles.coerceIn(50, 200)
    val clamped = tile.coerceIn(1, safeMax) - 1
    val rowFromBottom = clamped / 10
    val colInRow = clamped % 10
    val colFromLeft = if (rowFromBottom % 2 == 0) {
        colInRow
    } else {
        9 - colInRow
    }
    return Pair(rowFromBottom, colFromLeft)
}

object BoardGenerator {

    val supportedTileCounts = listOf(50, 100, 150, 200)

    fun generateBoardConfiguration(
        maxTiles: Int = 100,
        difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
        enabledObstacles: Set<ObstacleType> = ObstacleType.entries.toSet(),
        seed: Long = System.currentTimeMillis()
    ): BoardConfiguration {
        val safeMaxTiles = (maxTiles / 10 * 10).coerceIn(50, 200)
        val totalRows = safeMaxTiles / 10
        val scaleFactor = (safeMaxTiles / 100f).coerceIn(0.55f, 2.0f)

        val random = Random(seed)
        val occupiedTiles = mutableSetOf(1, safeMaxTiles)
        val links = mutableListOf<BoardLink>()
        val obstacles = mutableListOf<BoardObstacle>()

        val scaledLadderCount = (difficulty.ladderCount * scaleFactor).roundToInt().coerceIn(3, 18)
        val scaledSnakeCount = (difficulty.snakeCount * scaleFactor).roundToInt().coerceIn(3, 22)
        val scaledMinSnakeDrop = (difficulty.minSnakeDrop * scaleFactor.coerceAtLeast(0.7f)).roundToInt().coerceAtLeast(5)
        val scaledMaxSnakeDrop = (difficulty.maxSnakeDrop * scaleFactor.coerceAtMost(1.5f)).roundToInt()
            .coerceIn(scaledMinSnakeDrop + 4, safeMaxTiles - 12)

        val baseObsCount = difficulty.obstacleCountRange.random(random)
        val targetObstacleCount = if (enabledObstacles.isEmpty()) {
            0
        } else {
            (baseObsCount * scaleFactor).roundToInt().coerceIn(3, 30)
        }

        val wormLocations = mutableListOf<Int>()
        if (ObstacleType.WORM in enabledObstacles && targetObstacleCount > 0) {
            val wormCount = when (difficulty) {
                DifficultyLevel.EASY -> (1 * scaleFactor).roundToInt().coerceAtLeast(1)
                DifficultyLevel.MEDIUM -> (2 * scaleFactor).roundToInt().coerceAtLeast(1)
                DifficultyLevel.HARD -> (3 * scaleFactor).roundToInt().coerceAtLeast(2)
            }.coerceAtMost(targetObstacleCount)

            for (i in 0 until wormCount) {
                val bandStart = ((safeMaxTiles * (0.18f + 0.25f * (i % 3))).roundToInt()).coerceIn(10, safeMaxTiles - 15)
                val bandEnd = (bandStart + (safeMaxTiles * 0.22f).roundToInt()).coerceIn(bandStart + 4, safeMaxTiles - 6)
                val tile = (bandStart..bandEnd).shuffled(random).firstOrNull {
                    it !in occupiedTiles && !hasCloseObstacle(it, occupiedTiles)
                }
                if (tile != null) {
                    occupiedTiles.add(tile)
                    wormLocations.add(tile)
                    obstacles.add(BoardObstacle(tile = tile, type = ObstacleType.WORM))
                }
            }
        }

        // Generate Ladders proportionally across all rows
        for (i in 0 until scaledLadderCount) {
            val minStartRow = (i * (totalRows - 2) / scaledLadderCount).coerceIn(0, (totalRows - 3).coerceAtLeast(0))
            val maxStartRow = (minStartRow + 2).coerceIn(1, (totalRows - 2).coerceAtLeast(1))
            var placed = false

            for (attempt in 0 until 65) {
                val rStart = (minStartRow..maxStartRow).random(random)
                val climbRows = when (difficulty) {
                    DifficultyLevel.EASY -> (2..4).random(random)
                    DifficultyLevel.MEDIUM -> (1..3).random(random)
                    DifficultyLevel.HARD -> (1..2).random(random)
                }
                val rEnd = (rStart + climbRows).coerceIn(rStart + 1, totalRows - 1)
                val cStart = (0..9).random(random)
                val cEnd = (0..9).random(random)
                if (abs(cStart - cEnd) > 4) continue

                val startTile = gridToTile(rStart, cStart, totalRows)
                val endTile = gridToTile(rEnd, cEnd, totalRows)

                if (startTile in 2..(safeMaxTiles - 10) &&
                    endTile in (startTile + 7)..(safeMaxTiles - 2) &&
                    startTile !in occupiedTiles &&
                    endTile !in occupiedTiles
                ) {
                    occupiedTiles.add(startTile)
                    occupiedTiles.add(endTile)
                    links.add(BoardLink(startTile = startTile, endTile = endTile, type = LinkType.LADDER))
                    placed = true
                    break
                }
            }
            if (!placed) {
                val fallbackStart = (3..(safeMaxTiles - 15)).shuffled(random).firstOrNull { it !in occupiedTiles } ?: 5
                val fallbackEnd = ((fallbackStart + 10)..(safeMaxTiles - 3)).shuffled(random).firstOrNull { it !in occupiedTiles }
                    ?: (fallbackStart + 11).coerceAtMost(safeMaxTiles - 2)
                if (fallbackEnd > fallbackStart) {
                    occupiedTiles.add(fallbackStart)
                    occupiedTiles.add(fallbackEnd)
                    links.add(BoardLink(startTile = fallbackStart, endTile = fallbackEnd, type = LinkType.LADDER))
                }
            }
        }

        // Extra Frenzied Snakes near Worm Zones
        for (wormTile in wormLocations) {
            val nearbyCandidates = ((wormTile - 5)..(wormTile + 6))
                .filter { it in 14..(safeMaxTiles - 3) && it != wormTile && it !in occupiedTiles }
                .shuffled(random)
            val headTile = nearbyCandidates.firstOrNull()
            if (headTile != null) {
                val drop = (scaledMinSnakeDrop..scaledMaxSnakeDrop).random(random)
                val tailCandidate = (headTile - drop).coerceIn(2, headTile - 6)
                val tailTile = ((tailCandidate - 3)..(tailCandidate + 3))
                    .filter { it in 2..(headTile - 6) && it !in occupiedTiles }
                    .firstOrNull()
                if (tailTile != null) {
                    occupiedTiles.add(headTile)
                    occupiedTiles.add(tailTile)
                    links.add(
                        BoardLink(
                            startTile = headTile,
                            endTile = tailTile,
                            type = LinkType.SNAKE,
                            isNearWormZone = true
                        )
                    )
                }
            }
        }

        // Remaining Snakes distributed across rows
        val remainingSnakes = (scaledSnakeCount - links.count { it.type == LinkType.SNAKE }).coerceAtLeast(1)
        for (i in 0 until remainingSnakes) {
            var placed = false
            val forceTopRow = (i == 0)
            for (attempt in 0 until 75) {
                val rHead = if (forceTopRow) (totalRows - 1) else (1 until totalRows).random(random)
                val cHead = if (forceTopRow) (1..6).random(random) else (0..9).random(random)
                val headTile = gridToTile(rHead, cHead, totalRows)
                if (headTile !in 14 until safeMaxTiles || headTile in occupiedTiles) continue

                val desiredDrop = (scaledMinSnakeDrop..scaledMaxSnakeDrop).random(random)
                val rawTail = (headTile - desiredDrop).coerceIn(2, headTile - 6)
                val (rTail, cTail) = tileToGridPosition(rawTail, safeMaxTiles)
                if (rTail >= rHead || abs(cHead - cTail) > 5) continue

                val tailTile = gridToTile(rTail, cTail, totalRows)
                if (tailTile in 2..(headTile - 6) && tailTile !in occupiedTiles) {
                    val nearWorm = wormLocations.any { abs(it - headTile) <= 8 }
                    occupiedTiles.add(headTile)
                    occupiedTiles.add(tailTile)
                    links.add(
                        BoardLink(
                            startTile = headTile,
                            endTile = tailTile,
                            type = LinkType.SNAKE,
                            isNearWormZone = nearWorm
                        )
                    )
                    placed = true
                    break
                }
            }
            if (!placed) {
                val fallbackHead = ((safeMaxTiles - 2) downTo 18).shuffled(random).firstOrNull { it !in occupiedTiles }
                    ?: (safeMaxTiles - 4)
                val fallbackTail = ((fallbackHead - scaledMinSnakeDrop) downTo 3)
                    .firstOrNull { it !in occupiedTiles } ?: (fallbackHead - 10).coerceAtLeast(2)
                if (fallbackHead > fallbackTail) {
                    occupiedTiles.add(fallbackHead)
                    occupiedTiles.add(fallbackTail)
                    links.add(
                        BoardLink(
                            startTile = fallbackHead,
                            endTile = fallbackTail,
                            type = LinkType.SNAKE,
                            isNearWormZone = wormLocations.any { abs(it - fallbackHead) <= 8 }
                        )
                    )
                }
            }
        }

        // Remaining Special Obstacles
        val remainingObstaclesToPlace = (targetObstacleCount - obstacles.size).coerceAtLeast(0)
        if (remainingObstaclesToPlace > 0 && enabledObstacles.isNotEmpty()) {
            val weightedPool = mutableListOf<ObstacleType>()
            for (type in enabledObstacles) {
                val weight = when (difficulty) {
                    DifficultyLevel.EASY -> if (type.isDangerous) 1 else 3
                    DifficultyLevel.MEDIUM -> 2
                    DifficultyLevel.HARD -> if (type.isDangerous) 4 else 1
                }
                repeat(weight) { weightedPool.add(type) }
            }

            val priorityQueue = enabledObstacles.filter { type ->
                obstacles.none { it.type == type }
            }.shuffled(random).toMutableList()

            val usableSpan = (safeMaxTiles - 10).coerceAtLeast(20)
            val sectorCount = 8
            val sectorSize = (usableSpan / sectorCount).coerceAtLeast(4)

            for (idx in 0 until remainingObstaclesToPlace) {
                val chosenType = if (priorityQueue.isNotEmpty()) {
                    priorityQueue.removeAt(0)
                } else {
                    weightedPool.random(random)
                }

                val minValidTile = if (chosenType == ObstacleType.WELL || chosenType == ObstacleType.REVERSE) 10 else 5
                val secIdx = idx % sectorCount
                val secStart = (5 + secIdx * sectorSize).coerceAtLeast(minValidTile)
                val secEnd = (secStart + sectorSize).coerceAtMost(safeMaxTiles - 4)

                val candidateFromSector = if (secEnd >= secStart) {
                    (secStart..secEnd)
                        .filter { it >= minValidTile && it !in occupiedTiles && !hasAdjacentObstacle(it, obstacles) }
                        .shuffled(random)
                        .firstOrNull()
                } else null

                val finalTile = candidateFromSector ?: (minValidTile..(safeMaxTiles - 4))
                    .filter { it !in occupiedTiles && !hasAdjacentObstacle(it, obstacles) }
                    .shuffled(random)
                    .firstOrNull()

                if (finalTile != null) {
                    occupiedTiles.add(finalTile)
                    obstacles.add(BoardObstacle(tile = finalTile, type = chosenType))
                }
            }
        }

        return BoardConfiguration(
            links = links,
            obstacles = obstacles.sortedBy { it.tile },
            maxTiles = safeMaxTiles
        )
    }

    private fun hasCloseObstacle(tile: Int, occupied: Set<Int>): Boolean {
        return (tile - 1 in occupied) || (tile + 1 in occupied)
    }

    private fun hasAdjacentObstacle(tile: Int, obstacles: List<BoardObstacle>): Boolean {
        return obstacles.any { abs(it.tile - tile) <= 1 }
    }

    private fun gridToTile(rowFromBottom: Int, colFromLeft: Int, totalRows: Int = 10): Int {
        val r = rowFromBottom.coerceIn(0, (totalRows - 1).coerceAtLeast(0))
        val c = colFromLeft.coerceIn(0, 9)
        val offsetInRow = if (r % 2 == 0) c else (9 - c)
        return r * 10 + offsetInRow + 1
    }
}
