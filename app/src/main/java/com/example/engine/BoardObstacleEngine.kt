package com.example.engine

import com.example.model.BannerType
import com.example.model.BoardConfiguration
import com.example.model.BoardLink
import com.example.model.BoardObstacle
import com.example.model.CharacterAbilityType
import com.example.model.CustomAbilityEffectType
import com.example.model.CustomAbilityTarget
import com.example.model.DifficultyLevel
import com.example.model.LinkType
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.ShieldThreatCategory
import com.example.model.resource.ActiveStatusEffect
import com.example.model.resource.StatusEffectType
import com.example.model.toPersianDigits
import kotlin.math.roundToInt

/**
 * Pure domain movement animation style for obstacle/link outcomes (mapped to UI motion types in ViewModel).
 */
enum class DomainMovementMotionType {
    STEP_BY_STEP,
    LADDER_CLIMB,
    SNAKE_SLIDE,
    REVERSE_BACKWARD,
    WELL_PLUNGE,
    WIND_GUST,
    LUCK_BOOST,
    STORM_SHIFT,
    SHADOW_SWAP,
    EXECUTIONER_HUNT,
    TIME_REWIND
}

/**
 * Pure domain board FX cue for obstacle/defense outcomes (mapped to UI FX types in ViewModel).
 */
enum class DomainBoardFxType {
    NONE,
    EXECUTIONER_SLASH,
    SHIELD_BLOCK_SUCCESS,
    SHIELD_BREAK_FAIL,
    DRAGON_SOUL_AURA,
    BERSERKER_RAGE,
    TIME_REWIND,
    ILLUSION_MIRAGE,
    GUARDIAN_ANGEL_WINGS,
    TRICKSTER_TRANSFER,
    PHANTOM_PHASE
}

/**
 * Result of resolving a tile landing against board links and obstacles.
 */
data class ObstacleResolution(
    val initialTile: Int,
    val resolvedTile: Int,
    val linkType: LinkType? = null,
    val obstacleType: ObstacleType? = null,
    val bannerMessage: String? = null,
    val bannerType: BannerType = BannerType.NORMAL,
    val addedStatusEffects: List<ActiveStatusEffect> = emptyList(),
    val halveNextRoll: Boolean = false,
    val skipNextTurn: Boolean = false,
    val isProtectedByShieldOrPassive: Boolean = false
)

/**
 * Outcome of evaluating passive/active defensive protections (Mirage, Angel, Phantom, Shield).
 */
data class ProtectionDefenseOutcome(
    val isBlocked: Boolean,
    val isCriticalParry: Boolean = false,
    val updatedDefender: Player,
    val heroFxType: DomainBoardFxType,
    val bannerMessage: String?,
    val bannerType: BannerType
)

/**
 * Outcome of resolving a special board obstacle (`WORM`, `REVERSE`, `SWAMP`, `WELL`, `WIND`, `LUCK`, `STORM`).
 */
data class SpecialObstacleOutcome(
    val updatedPlayer: Player,
    val newTile: Int,
    val movementPath: List<Int> = emptyList(),
    val movementType: DomainMovementMotionType? = null,
    val grantExtraTurn: Boolean = false,
    val requiresStormGlobalShift: Boolean = false,
    val heroFxType: DomainBoardFxType = DomainBoardFxType.NONE,
    val isPositiveHighlight: Boolean = true,
    val bannerMessage: String,
    val bannerType: BannerType,
    val playAbilitySound: Boolean = false,
    val playSixBonusChime: Boolean = false,
    val playObstacleSound: ObstacleType? = null
)

/**
 * Outcome of resolving a ladder climb.
 */
data class LadderClimbOutcome(
    val updatedPlayer: Player,
    val finalTile: Int,
    val bannerMessage: String
)

/**
 * Outcome of resolving a snake landing (before interactive Truth-or-Dare draw if unblocked).
 */
data class SnakeSlideOutcome(
    val blockedOrSlain: Boolean,
    val boostedForward: Boolean = false,
    val snakeDestroyed: Boolean = false,
    val updatedPlayer: Player,
    val updatedLinks: List<BoardLink>,
    val finalTile: Int,
    val heroFxType: DomainBoardFxType = DomainBoardFxType.NONE,
    val isPositiveHighlight: Boolean = true,
    val bannerMessage: String,
    val bannerType: BannerType,
    val playAbilitySound: Boolean = false
)

/**
 * Pure domain engine for evaluating Snakes, Ladders, and Special Board Obstacles,
 * cleanly separated from ViewModel and UI rendering.
 */
class BoardObstacleEngine(
    private val randomProvider: RandomProvider = DefaultRandomProvider()
) {
    /**
     * Evaluates the landing tile of a player against board links (ladders & snakes)
     * and special obstacles (swamp, worm, well, reverse, wind, luck, storm).
     */
    fun evaluateLanding(
        tile: Int,
        boardConfig: BoardConfiguration,
        player: Player
    ): ObstacleResolution {
        // 1. Check Ladders and Snakes first
        val matchingLink = boardConfig.links.find { it.startTile == tile }
        if (matchingLink != null) {
            when (matchingLink.type) {
                LinkType.LADDER -> {
                    return ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = matchingLink.endTile,
                        linkType = LinkType.LADDER,
                        bannerMessage = "ایول! از پله بالا رفتی تا خانه ${matchingLink.endTile}! 🚀",
                        bannerType = BannerType.LADDER_CLIMB
                    )
                }
                LinkType.SNAKE -> {
                    val hasSnakeProtection = player.guardianAngelProtected ||
                        (player.isShieldCharged && player.resources.shield > 0)

                    if (hasSnakeProtection) {
                        return ObstacleResolution(
                            initialTile = tile,
                            resolvedTile = tile,
                            linkType = LinkType.SNAKE,
                            bannerMessage = "🛡️ سپر محافظ نیش مار را کاملاً دفع کرد و سر جایت ماندی!",
                            bannerType = BannerType.CHARACTER_ABILITY,
                            isProtectedByShieldOrPassive = true
                        )
                    }

                    return ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = matchingLink.endTile,
                        linkType = LinkType.SNAKE,
                        bannerMessage = "آخ! نیش مار! سقوط به خانه ${matchingLink.endTile} 🐍",
                        bannerType = BannerType.SNAKE_BITE,
                        addedStatusEffects = listOf(
                            ActiveStatusEffect(StatusEffectType.POISON, turnsRemaining = 1, magnitude = 1)
                        )
                    )
                }
            }
        }

        // 2. Check Special Obstacles
        val matchingObstacle = boardConfig.obstacles.find { it.tile == tile }
        if (matchingObstacle != null) {
            return when (matchingObstacle.type) {
                ObstacleType.WORM -> {
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = tile,
                        obstacleType = ObstacleType.WORM,
                        bannerMessage = "🐛 کرم لزج! تا ۲ نوبت بدشانسی و تاس ۱ و ۲ همراهته!",
                        bannerType = BannerType.OBSTACLE_DANGER,
                        addedStatusEffects = listOf(
                            ActiveStatusEffect(StatusEffectType.WORM_CURSE, turnsRemaining = 2, magnitude = 1)
                        )
                    )
                }
                ObstacleType.REVERSE -> {
                    val fallbackTile = (tile - 3).coerceAtLeast(1)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = fallbackTile,
                        obstacleType = ObstacleType.REVERSE,
                        bannerMessage = "↩️ خانه معکوس! ۳ خانه به عقب پرتاب شدی!",
                        bannerType = BannerType.OBSTACLE_DANGER
                    )
                }
                ObstacleType.SWAMP -> {
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = tile,
                        obstacleType = ObstacleType.SWAMP,
                        bannerMessage = "🌿 باتلاق چسبناک! یک نوبت گیر می‌افتی و تاس بعدی‌ات کند می‌شود!",
                        bannerType = BannerType.OBSTACLE_DANGER,
                        skipNextTurn = true,
                        halveNextRoll = true,
                        addedStatusEffects = listOf(
                            ActiveStatusEffect(StatusEffectType.SWAMP_SLOWNESS, turnsRemaining = 1, magnitude = 1)
                        )
                    )
                }
                ObstacleType.WELL -> {
                    val wellDropTile = (tile - 5).coerceAtLeast(1)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = wellDropTile,
                        obstacleType = ObstacleType.WELL,
                        bannerMessage = "🕳️ چاه تاریک! سقوط ناگهانی به خانه $wellDropTile!",
                        bannerType = BannerType.OBSTACLE_DANGER
                    )
                }
                ObstacleType.WIND -> {
                    val isForward = randomProvider.nextBoolean()
                    val delta = if (isForward) 4 else -2
                    val newTile = (tile + delta).coerceIn(1, boardConfig.maxTiles)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = newTile,
                        obstacleType = ObstacleType.WIND,
                        bannerMessage = if (isForward) "💨 باد موافق وزید! ۴ خانه جلو افتادی!" else "💨 باد مخالف! ۲ خانه به عقب رانده شدی!",
                        bannerType = BannerType.OBSTACLE_SURPRISE
                    )
                }
                ObstacleType.LUCK -> {
                    val bonus = randomProvider.nextInt(1, 5)
                    val newTile = (tile + bonus).coerceAtMost(boardConfig.maxTiles)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = newTile,
                        obstacleType = ObstacleType.LUCK,
                        bannerMessage = "🎁 خانه شانس! $bonus خانه جایزه گرفتی!",
                        bannerType = BannerType.OBSTACLE_SURPRISE
                    )
                }
                ObstacleType.STORM -> {
                    val delta = if (randomProvider.nextBoolean()) 2 else -2
                    val newTile = (tile + delta).coerceIn(1, boardConfig.maxTiles)
                    ObstacleResolution(
                        initialTile = tile,
                        resolvedTile = newTile,
                        obstacleType = ObstacleType.STORM,
                        bannerMessage = "🌪️ طوفان سهمگین! باد تو را به خانه $newTile جابه‌جا کرد!",
                        bannerType = BannerType.STORM_CHAOS
                    )
                }
            }
        }

        // Standard tile with no link or obstacle
        return ObstacleResolution(
            initialTile = tile,
            resolvedTile = tile
        )
    }

    /**
     * Evaluates Illusionist Mirage, Guardian Angel, Phantom Phase (active or 35% passive Wind Rider),
     * and Guardian Shield + Critical Parry against a threat category.
     */
    fun evaluateProtectionDefense(
        defender: Player,
        threatCategory: ShieldThreatCategory,
        threatNamePersian: String,
        baseConfiguredPercent: Int,
        deterministicRollOverride: Int? = null
    ): ProtectionDefenseOutcome {
        if (!defender.resources.isAlive) {
            return ProtectionDefenseOutcome(
                isBlocked = false,
                updatedDefender = defender,
                heroFxType = DomainBoardFxType.NONE,
                bannerMessage = null,
                bannerType = BannerType.NORMAL
            )
        }

        // 1. Illusionist Mirage Decoy (for enemy abilities / Executioner hunt)
        if (threatCategory == ShieldThreatCategory.EXECUTIONER_OR_ABILITY &&
            defender.illusionDecoyTile != null &&
            defender.illusionTurnsRemaining > 0
        ) {
            return ProtectionDefenseOutcome(
                isBlocked = true,
                updatedDefender = defender.copy(illusionDecoyTile = null, illusionTurnsRemaining = 0),
                heroFxType = DomainBoardFxType.ILLUSION_MIRAGE,
                bannerMessage = "سراب توهم‌زا (Illusionist)! 🪄 حملهٔ «$threatNamePersian» به بدل توهمی ${defender.name} برخورد کرد و موقعیت واقعی او تغییر نکرد!",
                bannerType = BannerType.CHARACTER_ABILITY
            )
        }

        // 2. Guardian Angel Protection
        if (defender.guardianAngelProtected) {
            return ProtectionDefenseOutcome(
                isBlocked = true,
                updatedDefender = defender.copy(guardianAngelProtected = false),
                heroFxType = DomainBoardFxType.GUARDIAN_ANGEL_WINGS,
                bannerMessage = "سپر فرشتهٔ محافظ (Guardian Angel)! 🌟 بال‌های نورانی از ${defender.name} در برابر «$threatNamePersian» محافظت کامل کرد!",
                bannerType = BannerType.CHARACTER_ABILITY
            )
        }

        // 3. Phantom Phase (active OR 35% passive for Wind Rider against snakes/negative obstacles)
        val canPhantomApply = threatCategory == ShieldThreatCategory.SNAKE ||
            threatCategory == ShieldThreatCategory.NEGATIVE_OBSTACLE
        val passivePhantomProc = !defender.phantomPhaseActive &&
            canPhantomApply &&
            defender.character.abilityType == CharacterAbilityType.WIND_RIDER &&
            deterministicRollOverride == null &&
            randomProvider.nextInt(0, 100) < 35

        if (canPhantomApply && (defender.phantomPhaseActive || passivePhantomProc)) {
            return ProtectionDefenseOutcome(
                isBlocked = true,
                updatedDefender = defender.copy(phantomPhaseActive = false),
                heroFxType = DomainBoardFxType.PHANTOM_PHASE,
                bannerMessage = "گام شبح باد (Phantom)! 👻🌪️ ${defender.name} شبح‌وار از میان «$threatNamePersian» عبور کرد و آسیبی ندید!",
                bannerType = BannerType.CHARACTER_ABILITY
            )
        }

        // 4. Guardian Shield
        val hasChargedShield = defender.isShieldCharged &&
            defender.character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD
        if (!hasChargedShield) {
            return ProtectionDefenseOutcome(
                isBlocked = false,
                updatedDefender = defender,
                heroFxType = DomainBoardFxType.NONE,
                bannerMessage = null,
                bannerType = BannerType.NORMAL
            )
        }

        val stats = defender.character.heroBlueprint.stats
        val effectiveChance = stats.computeShieldDefenseChance(baseConfiguredPercent, threatCategory)
        val roll = deterministicRollOverride ?: randomProvider.nextInt(0, 100)

        return if (roll < effectiveChance) {
            val isCriticalParry = roll < stats.criticalParryPreserveShieldPercent
            val consumptionNote = if (isCriticalParry) {
                "دفع بی‌نقص بدون مصرف سپر! ✨"
            } else {
                "سپر مصرف شد (شارژ مجدد: ۳ نوبت)"
            }
            val updated = if (isCriticalParry) {
                defender.copy(isShieldCharged = true)
            } else {
                defender.copy(isShieldCharged = false, abilityChargeProgress = 0)
            }
            ProtectionDefenseOutcome(
                isBlocked = true,
                isCriticalParry = isCriticalParry,
                updatedDefender = updated,
                heroFxType = DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                bannerMessage = "دفع موفق سپر نورانی (${effectiveChance.toPersianDigits()}٪)! 🛡️✨ ${defender.name} در برابر «$threatNamePersian» محافظت شد — $consumptionNote",
                bannerType = BannerType.CHARACTER_ABILITY
            )
        } else {
            ProtectionDefenseOutcome(
                isBlocked = false,
                updatedDefender = defender,
                heroFxType = DomainBoardFxType.SHIELD_BREAK_FAIL,
                bannerMessage = "شکست دفاع سپر (${effectiveChance.toPersianDigits()}٪)! 💥 شدت «$threatNamePersian» از سپر ${defender.name} عبور کرد (سپر برای نوبت بعد حفظ شد)!",
                bannerType = BannerType.OBSTACLE_DANGER
            )
        }
    }

    /**
     * Resolves a special board obstacle (`WORM`, `REVERSE`, `SWAMP`, `WELL`, `WIND`, `LUCK`, `STORM`)
     * dynamically respecting [maxTiles], hero traits/resistances, and custom character rules.
     */
    fun resolveSpecialObstacle(
        player: Player,
        obstacle: BoardObstacle,
        rolledSteps: Int,
        difficulty: DifficultyLevel,
        maxTiles: Int,
        baseShieldProbability: Int = 78
    ): SpecialObstacleOutcome {
        val countedPlayer = player.copy(obstaclesTriggered = player.obstaclesTriggered + 1)
        val ability = countedPlayer.character.abilityType
        val customSpec = countedPlayer.character.customSpec

        // Custom Character SPECIAL_OBSTACLES immunity/boost
        if (customSpec?.target == CustomAbilityTarget.SPECIAL_OBSTACLES &&
            obstacle.type.isDangerous &&
            randomProvider.nextInt(0, 100) < customSpec.percentageOrValue
        ) {
            val shouldBoost = customSpec.effectType == CustomAbilityEffectType.BONUS_STEPS_BOOST ||
                customSpec.effectType == CustomAbilityEffectType.INCREASE_CHANCE
            val destTile = if (shouldBoost) (obstacle.tile + 2).coerceAtMost(maxTiles) else obstacle.tile
            return SpecialObstacleOutcome(
                updatedPlayer = countedPlayer.copy(currentTile = destTile),
                newTile = destTile,
                movementPath = if (destTile > obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                movementType = if (destTile > obstacle.tile) DomainMovementMotionType.LUCK_BOOST else null,
                heroFxType = DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                isPositiveHighlight = true,
                bannerMessage = if (shouldBoost) {
                    "قدرت سفارشی ${countedPlayer.character.englishName}! ⚡ مانع «${obstacle.type.persianName}» خنثی شد و +۲ خانه به جلو پریدی!"
                } else {
                    "ایمنی سفارشی ${countedPlayer.character.englishName}! 🛡️ مانع «${obstacle.type.persianName}» کامل خنثی شد!"
                },
                bannerType = BannerType.CHARACTER_ABILITY,
                playAbilitySound = true
            )
        }

        // Protection defense for dangerous obstacles
        if (obstacle.type.isDangerous) {
            val protection = evaluateProtectionDefense(
                defender = countedPlayer,
                threatCategory = ShieldThreatCategory.NEGATIVE_OBSTACLE,
                threatNamePersian = obstacle.type.persianName,
                baseConfiguredPercent = baseShieldProbability
            )
            if (protection.isBlocked) {
                return SpecialObstacleOutcome(
                    updatedPlayer = protection.updatedDefender,
                    newTile = obstacle.tile,
                    heroFxType = protection.heroFxType,
                    isPositiveHighlight = true,
                    bannerMessage = protection.bannerMessage.orEmpty(),
                    bannerType = protection.bannerType,
                    playAbilitySound = true
                )
            }
        }

        return when (obstacle.type) {
            ObstacleType.WORM -> {
                when (ability) {
                    CharacterAbilityType.WORM_IMMUNE -> {
                        val destTile = (obstacle.tile + 2).coerceAtMost(maxTiles)
                        SpecialObstacleOutcome(
                            updatedPlayer = countedPlayer.copy(currentTile = destTile),
                            newTile = destTile,
                            movementPath = if (destTile > obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                            movementType = if (destTile > obstacle.tile) DomainMovementMotionType.LUCK_BOOST else null,
                            heroFxType = DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                            isPositiveHighlight = true,
                            bannerMessage = "مقاومت کامل ضدزهر ${countedPlayer.name}! 🧪🛡️ کرم لزج خنثی شد و ۲ خانه به جلو پاداش گرفتی!",
                            bannerType = BannerType.CHARACTER_ABILITY,
                            playAbilitySound = true
                        )
                    }
                    CharacterAbilityType.PHOENIX_WINGS -> {
                        val updated = countedPlayer.copy(
                            dragonSoulBoostTurns = 2,
                            wormCurseTurnsRemaining = 1,
                            resources = countedPlayer.resources.applyStatusEffect(
                                ActiveStatusEffect(StatusEffectType.WORM_CURSE, turnsRemaining = 1, magnitude = 1)
                            )
                        )
                        SpecialObstacleOutcome(
                            updatedPlayer = updated,
                            newTile = obstacle.tile,
                            heroFxType = DomainBoardFxType.DRAGON_SOUL_AURA,
                            isPositiveHighlight = true,
                            bannerMessage = "مقاومت روح اژدها (Dragon Soul)! 🔥🐉 ${countedPlayer.name} اثر کرم را سوزاند و برای پرتاب بعدی +۲ خانه قدرت گرفت!",
                            bannerType = BannerType.CHARACTER_ABILITY,
                            playAbilitySound = true
                        )
                    }
                    else -> {
                        val reducedDuration = if (countedPlayer.character.heroBlueprint.resistanceProfile.statusDebuffResistancePercent >= 40) 2 else 3
                        val updated = countedPlayer.copy(
                            wormCurseTurnsRemaining = reducedDuration,
                            resources = countedPlayer.resources.applyStatusEffect(
                                ActiveStatusEffect(StatusEffectType.WORM_CURSE, turnsRemaining = reducedDuration, magnitude = 1)
                            )
                        )
                        SpecialObstacleOutcome(
                            updatedPlayer = updated,
                            newTile = obstacle.tile,
                            isPositiveHighlight = false,
                            bannerMessage = "اه! ${countedPlayer.name} روی خانهٔ کرم لزج افتاد! 🪱 تا ${reducedDuration.toPersianDigits()} نوبت احتمال آمدن تاس ۱ و ۲ زیاد است!",
                            bannerType = BannerType.OBSTACLE_DANGER,
                            playObstacleSound = ObstacleType.WORM
                        )
                    }
                }
            }

            ObstacleType.REVERSE -> {
                if (ability == CharacterAbilityType.TIME_WEAVER) {
                    val forwardSteps = (rolledSteps / 2).coerceAtLeast(2)
                    val destTile = (obstacle.tile + forwardSteps).coerceAtMost(maxTiles)
                    SpecialObstacleOutcome(
                        updatedPlayer = countedPlayer.copy(currentTile = destTile),
                        newTile = destTile,
                        movementPath = if (destTile > obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                        movementType = if (destTile > obstacle.tile) DomainMovementMotionType.LUCK_BOOST else null,
                        heroFxType = DomainBoardFxType.TIME_REWIND,
                        isPositiveHighlight = true,
                        bannerMessage = "قدرت نگهبان زمان! ⏳ ${countedPlayer.name} خانهٔ معکوس را وارونه کرد و ${forwardSteps.toPersianDigits()} خانه به جلو پرید!",
                        bannerType = BannerType.CHARACTER_ABILITY,
                        playAbilitySound = true
                    )
                } else {
                    val backSteps = when (ability) {
                        CharacterAbilityType.HALF_REVERSE,
                        CharacterAbilityType.EARTH_TITAN -> (rolledSteps / 2).coerceAtLeast(1)
                        CharacterAbilityType.SPEEDSTER -> rolledSteps + 1
                        else -> rolledSteps
                    }
                    val destTile = (obstacle.tile - backSteps).coerceAtLeast(1)
                    val isAtlas = ability == CharacterAbilityType.EARTH_TITAN
                    val updated = countedPlayer.copy(
                        previousTurnTile = obstacle.tile,
                        currentTile = destTile,
                        berserkerRageTurns = if (isAtlas) 2 else countedPlayer.berserkerRageTurns
                    )
                    val msg = when {
                        ability == CharacterAbilityType.HALF_REVERSE ->
                            "لنگر تعادل ${countedPlayer.name}! ⚓ در خانهٔ معکوس فقط نصف تاس (${backSteps.toPersianDigits()} خانه) به عقب برگشتی!"
                        isAtlas ->
                            "مقاومت صخره‌ای و خشم برسرکر! 🪨🔥 ${countedPlayer.name} نصف مسیر را برگشت و برای ۲ نوبت +۲ خانه خشم حرکت گرفت!"
                        else ->
                            "آخ! خانهٔ معکوس! 🔄 ${countedPlayer.name} به اندازهٔ ${backSteps.toPersianDigits()} خانه به عقب (خانه ${destTile.toPersianDigits()}) برمی‌گردد!"
                    }
                    SpecialObstacleOutcome(
                        updatedPlayer = updated,
                        newTile = destTile,
                        movementPath = if (destTile < obstacle.tile) (obstacle.tile downTo destTile).toList() else emptyList(),
                        movementType = if (destTile < obstacle.tile) DomainMovementMotionType.REVERSE_BACKWARD else null,
                        heroFxType = if (isAtlas) DomainBoardFxType.BERSERKER_RAGE else DomainBoardFxType.NONE,
                        isPositiveHighlight = isAtlas,
                        bannerMessage = msg,
                        bannerType = if (ability == CharacterAbilityType.HALF_REVERSE || isAtlas) BannerType.CHARACTER_ABILITY else BannerType.OBSTACLE_DANGER,
                        playObstacleSound = ObstacleType.REVERSE
                    )
                }
            }

            ObstacleType.SWAMP -> {
                when (ability) {
                    CharacterAbilityType.PHOENIX_WINGS -> {
                        SpecialObstacleOutcome(
                            updatedPlayer = countedPlayer.copy(dragonSoulBoostTurns = 2),
                            newTile = obstacle.tile,
                            heroFxType = DomainBoardFxType.DRAGON_SOUL_AURA,
                            isPositiveHighlight = true,
                            bannerMessage = "بال‌های ققنوس و روح اژدها (Dragon Soul)! 🔥🐉 ${countedPlayer.name} از فراز باتلاق پرواز کرد و +۲ خانه قدرت حرکت برای پرتاب بعد گرفت!",
                            bannerType = BannerType.CHARACTER_ABILITY,
                            playAbilitySound = true
                        )
                    }
                    CharacterAbilityType.WORM_IMMUNE -> {
                        val destTile = (obstacle.tile + 2).coerceAtMost(maxTiles)
                        SpecialObstacleOutcome(
                            updatedPlayer = countedPlayer.copy(currentTile = destTile),
                            newTile = destTile,
                            movementPath = if (destTile > obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                            movementType = if (destTile > obstacle.tile) DomainMovementMotionType.LUCK_BOOST else null,
                            heroFxType = DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                            isPositiveHighlight = true,
                            bannerMessage = "هم‌زیستی با طبیعت ${countedPlayer.name}! 🌿 باتلاق به سکوی پرتاب تبدیل شد و ۲ خانه جلو پریدی!",
                            bannerType = BannerType.CHARACTER_ABILITY,
                            playAbilitySound = true
                        )
                    }
                    else -> {
                        val partialResist = countedPlayer.character.heroBlueprint.resistanceProfile.statusDebuffResistancePercent >= 40
                        val updated = countedPlayer.copy(
                            swampSkipTurnsRemaining = if (partialResist) 0 else 1,
                            halveNextRoll = true,
                            resources = countedPlayer.resources.applyStatusEffect(
                                ActiveStatusEffect(StatusEffectType.SWAMP_SLOWNESS, turnsRemaining = 1, magnitude = 1)
                            )
                        )
                        SpecialObstacleOutcome(
                            updatedPlayer = updated,
                            newTile = obstacle.tile,
                            isPositiveHighlight = false,
                            bannerMessage = if (partialResist) {
                                "مقاومت نسبی ${countedPlayer.name} در باتلاق! 🛡️🌿 نوبتت از دست نرفت و فقط تاس بعدی نصف می‌شود!"
                            } else {
                                "باتلاق سنگین! 🌿 ${countedPlayer.name} در گل‌ولای فرو رفت؛ یک دور حرکت نمی‌کند و تاس بعدی‌اش نصف می‌شود!"
                            },
                            bannerType = BannerType.OBSTACLE_DANGER,
                            playObstacleSound = ObstacleType.SWAMP
                        )
                    }
                }
            }

            ObstacleType.WELL -> {
                if (ability == CharacterAbilityType.PHOENIX_WINGS ||
                    ability == CharacterAbilityType.EARTH_TITAN ||
                    ability == CharacterAbilityType.FROST_KNIGHT
                ) {
                    val isPhoenix = ability == CharacterAbilityType.PHOENIX_WINGS
                    val updated = if (isPhoenix) countedPlayer.copy(dragonSoulBoostTurns = 2) else countedPlayer
                    SpecialObstacleOutcome(
                        updatedPlayer = updated,
                        newTile = obstacle.tile,
                        heroFxType = if (isPhoenix) DomainBoardFxType.DRAGON_SOUL_AURA else DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                        isPositiveHighlight = true,
                        bannerMessage = if (isPhoenix) {
                            "پرواز ققنوس و روح اژدها (Dragon Soul)! 🔥🐉 از چاه تاریک عبور کردی و +۲ خانه قدرت حرکت برای پرتاب بعد گرفتی!"
                        } else {
                            "مقاومت ویژهٔ ${countedPlayer.name}! 🛡️ از سقوط به درون چاه تاریک کاملاً در امان ماندی!"
                        },
                        bannerType = BannerType.CHARACTER_ABILITY,
                        playAbilitySound = true
                    )
                } else {
                    val scale = (maxTiles / 100f).coerceIn(0.6f, 1.5f)
                    val baseMaxDrop = when (difficulty) {
                        DifficultyLevel.EASY -> 12
                        DifficultyLevel.MEDIUM -> 22
                        DifficultyLevel.HARD -> 34
                    }
                    val scaledDrop = (baseMaxDrop * scale).roundToInt().coerceAtLeast(6)
                    val minDest = (obstacle.tile - scaledDrop).coerceAtLeast(2)
                    val maxDest = (obstacle.tile - 5).coerceAtLeast(2)
                    val destTile = if (maxDest > minDest) {
                        randomProvider.nextInt(minDest, maxDest + 1)
                    } else {
                        2
                    }.coerceIn(1, maxTiles)

                    SpecialObstacleOutcome(
                        updatedPlayer = countedPlayer.copy(previousTurnTile = obstacle.tile, currentTile = destTile),
                        newTile = destTile,
                        movementPath = if (destTile != obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                        movementType = if (destTile != obstacle.tile) DomainMovementMotionType.WELL_PLUNGE else null,
                        isPositiveHighlight = false,
                        bannerMessage = "سقوط در چاه تاریک! 🕳️ ${countedPlayer.name} به اعماق چاه افتاد و از خانه ${destTile.toPersianDigits()} سر درآورد!",
                        bannerType = BannerType.OBSTACLE_DANGER,
                        playObstacleSound = ObstacleType.WELL
                    )
                }
            }

            ObstacleType.WIND -> {
                val isTailwind = (ability == CharacterAbilityType.WIND_RIDER) || randomProvider.nextBoolean()
                val destTile = if (isTailwind) {
                    val windBoost = if (ability == CharacterAbilityType.GUARDIAN_SHIELD) {
                        (rolledSteps - 2).coerceAtLeast(1)
                    } else {
                        rolledSteps
                    }
                    (obstacle.tile + windBoost).coerceAtMost(maxTiles)
                } else {
                    val pushBack = if (ability == CharacterAbilityType.HALF_REVERSE || ability == CharacterAbilityType.EARTH_TITAN) {
                        (rolledSteps / 4).coerceAtLeast(1)
                    } else {
                        (rolledSteps / 2).coerceAtLeast(1)
                    }
                    (obstacle.tile - pushBack).coerceAtLeast(1)
                }
                val isAtlas = ability == CharacterAbilityType.EARTH_TITAN && !isTailwind
                val updated = countedPlayer.copy(
                    previousTurnTile = if (!isTailwind) obstacle.tile else countedPlayer.previousTurnTile,
                    currentTile = destTile,
                    berserkerRageTurns = if (isAtlas) 2 else countedPlayer.berserkerRageTurns
                )
                SpecialObstacleOutcome(
                    updatedPlayer = updated,
                    newTile = destTile,
                    movementPath = if (destTile != obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                    movementType = if (destTile != obstacle.tile) DomainMovementMotionType.WIND_GUST else null,
                    isPositiveHighlight = isTailwind,
                    bannerMessage = if (isTailwind) {
                        "وزش باد موافق! 🌪️ باد ${countedPlayer.name} را با سرعت تا خانه ${destTile.toPersianDigits()} به جلو پرتاب کرد!"
                    } else {
                        "وزش باد مخالف! 💨 تندباد ${countedPlayer.name} را تا خانه ${destTile.toPersianDigits()} به عقب راند!"
                    },
                    bannerType = BannerType.OBSTACLE_SURPRISE,
                    playObstacleSound = ObstacleType.WIND
                )
            }

            ObstacleType.LUCK -> {
                val isLuckyHero = ability == CharacterAbilityType.FORTUNE_STAR ||
                    customSpec?.target == CustomAbilityTarget.DICE_AND_LUCK
                val luckOutcome = if (isLuckyHero) {
                    randomProvider.nextInt(0, 3)
                } else {
                    randomProvider.nextInt(0, 4)
                }
                when (luckOutcome) {
                    0 -> {
                        val boost = if (isLuckyHero) 4 else 3
                        val destTile = (obstacle.tile + boost).coerceAtMost(maxTiles)
                        SpecialObstacleOutcome(
                            updatedPlayer = countedPlayer.copy(currentTile = destTile),
                            newTile = destTile,
                            movementPath = if (destTile != obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                            movementType = if (destTile != obstacle.tile) DomainMovementMotionType.LUCK_BOOST else null,
                            isPositiveHighlight = true,
                            bannerMessage = "شانس طلایی! 🍀 صندوقچهٔ شانس ${countedPlayer.name} را ${boost.toPersianDigits()} خانه به جلو (خانه ${destTile.toPersianDigits()}) پرتاب کرد!",
                            bannerType = BannerType.OBSTACLE_SURPRISE,
                            playObstacleSound = ObstacleType.LUCK
                        )
                    }
                    1 -> {
                        SpecialObstacleOutcome(
                            updatedPlayer = countedPlayer,
                            newTile = obstacle.tile,
                            grantExtraTurn = true,
                            isPositiveHighlight = true,
                            bannerMessage = "خوش‌شانسی بزرگ! 🍀 ${countedPlayer.name} یک پرتاب تاس جایزه برنده شد!",
                            bannerType = BannerType.SIX_BONUS,
                            playSixBonusChime = true,
                            playObstacleSound = ObstacleType.LUCK
                        )
                    }
                    2 -> {
                        val destTile = (obstacle.tile + 2).coerceAtMost(maxTiles)
                        val updated = countedPlayer.copy(
                            currentTile = destTile,
                            wormCurseTurnsRemaining = 0,
                            swampSkipTurnsRemaining = 0,
                            halveNextRoll = false,
                            gravityCapNextRoll = false,
                            pinnedSlowSteps = 0,
                            resources = countedPlayer.resources.copy(statusEffects = emptyList())
                        )
                        SpecialObstacleOutcome(
                            updatedPlayer = updated,
                            newTile = destTile,
                            movementPath = if (destTile != obstacle.tile) listOf(obstacle.tile, destTile) else emptyList(),
                            movementType = if (destTile != obstacle.tile) DomainMovementMotionType.LUCK_BOOST else null,
                            isPositiveHighlight = true,
                            bannerMessage = "معجون شانس! ✨ تمام طلسم‌های ${countedPlayer.name} پاک شد و ۲ خانه جلو رفت!",
                            bannerType = BannerType.OBSTACLE_SURPRISE,
                            playObstacleSound = ObstacleType.LUCK
                        )
                    }
                    else -> {
                        val destTile = (obstacle.tile - 2).coerceAtLeast(1)
                        val isAtlas = ability == CharacterAbilityType.EARTH_TITAN
                        val updated = countedPlayer.copy(
                            previousTurnTile = obstacle.tile,
                            currentTile = destTile,
                            berserkerRageTurns = if (isAtlas) 2 else countedPlayer.berserkerRageTurns
                        )
                        SpecialObstacleOutcome(
                            updatedPlayer = updated,
                            newTile = destTile,
                            movementPath = if (destTile != obstacle.tile) (obstacle.tile downTo destTile).toList() else emptyList(),
                            movementType = if (destTile != obstacle.tile) DomainMovementMotionType.REVERSE_BACKWARD else null,
                            isPositiveHighlight = false,
                            bannerMessage = "بدشانسی شیرین! 🎲 صندوقچهٔ شانس شوخی کرد و ${countedPlayer.name} دو خانه به عقب برگشت!",
                            bannerType = BannerType.OBSTACLE_DANGER,
                            playObstacleSound = ObstacleType.LUCK
                        )
                    }
                }
            }

            ObstacleType.STORM -> {
                SpecialObstacleOutcome(
                    updatedPlayer = countedPlayer,
                    newTile = obstacle.tile,
                    requiresStormGlobalShift = true,
                    isPositiveHighlight = false,
                    bannerMessage = "طوفان سهمگین! ⚡ گردباد کل صفحه را لرزاند و همهٔ بازیکنان جابجا شدند!",
                    bannerType = BannerType.STORM_CHAOS,
                    playObstacleSound = ObstacleType.STORM
                )
            }
        }
    }

    /**
     * Resolves global STORM displacements for all alive players clamped safely to `1..(maxTiles - 1)`.
     */
    fun resolveStormShifts(
        players: List<Player>,
        maxTiles: Int
    ): Map<Int, Int> {
        val safeUpper = (maxTiles - 1).coerceAtLeast(1)
        return players.associate { p ->
            if (!p.resources.isAlive) {
                p.id to p.currentTile
            } else {
                val delta = when (p.character.abilityType) {
                    CharacterAbilityType.STORM_LORD -> 2
                    CharacterAbilityType.EARTH_TITAN,
                    CharacterAbilityType.HALF_REVERSE -> 0
                    else -> if (randomProvider.nextBoolean()) 1 else -1
                }
                val newTile = (p.currentTile + delta).coerceIn(1, safeUpper)
                p.id to newTile
            }
        }
    }

    /**
     * Resolves climbing a ladder including hero/custom bonuses dynamically clamped to [maxTiles].
     */
    fun resolveLadderClimb(
        player: Player,
        ladder: BoardLink,
        maxTiles: Int
    ): LadderClimbOutcome {
        val ability = player.character.abilityType
        val hasLadderBonus = ability == CharacterAbilityType.PHOENIX_WINGS ||
            ability == CharacterAbilityType.LADDER_MAGE
        val customSpec = player.character.customSpec
        val customLadderSteps = if (customSpec?.target == CustomAbilityTarget.LADDERS &&
            randomProvider.nextInt(0, 100) < customSpec.percentageOrValue
        ) {
            (customSpec.percentageOrValue / 25).coerceIn(1, 4)
        } else 0

        val bonusClimb = when {
            customLadderSteps > 0 -> customLadderSteps
            hasLadderBonus -> 1
            ability == CharacterAbilityType.HALF_REVERSE && (ladder.endTile - ladder.startTile) > 12 -> -1
            else -> 0
        }
        val finalDest = (ladder.endTile + bonusClimb).coerceIn(ladder.startTile + 1, maxTiles)
        val msg = when {
            customLadderSteps > 0 ->
                "قدرت پلهٔ سفارشی ${player.character.englishName}! 🪜 تا خانه ${finalDest.toPersianDigits()} (+${customLadderSteps.toPersianDigits()} خانه بالاتر از پله) اوج گرفت!"
            hasLadderBonus ->
                "هم‌نوایی جادویی روی پله! 🔥 ${player.name} تا خانه ${finalDest.toPersianDigits()} (+۱ خانه بالاتر از پله) اوج گرفت!"
            else ->
                "فوق‌العاده! ${player.name} از پلهٔ خانه ${ladder.startTile.toPersianDigits()} تا ${finalDest.toPersianDigits()} اوج گرفت! 🚀"
        }

        return LadderClimbOutcome(
            updatedPlayer = player.copy(
                laddersClimbed = player.laddersClimbed + 1,
                currentTile = finalDest
            ),
            finalTile = finalDest,
            bannerMessage = msg
        )
    }

    /**
     * Resolves landing on a snake: Custom character immunity/boost, Protection Defense (Angel/Phantom/Shield),
     * Snake Hunter slay chance (65%), and drop reduction calculation.
     */
    fun resolveSnakeSlide(
        player: Player,
        snake: BoardLink,
        boardLinks: List<BoardLink>,
        maxTiles: Int,
        baseShieldProbability: Int = 80
    ): SnakeSlideOutcome {
        val ability = player.character.abilityType
        val customSpec = player.character.customSpec

        // 0. Custom Character SNAKES ability
        if (customSpec?.target == CustomAbilityTarget.SNAKES) {
            val triggered = randomProvider.nextInt(0, 100) < customSpec.percentageOrValue
            if (triggered && (customSpec.effectType == CustomAbilityEffectType.NEUTRALIZE_IMMUNE ||
                    customSpec.effectType == CustomAbilityEffectType.TRIGGER_PROBABILITY)
            ) {
                val remainingLinks = boardLinks.filterNot {
                    it.type == LinkType.SNAKE && it.startTile == snake.startTile
                }
                return SnakeSlideOutcome(
                    blockedOrSlain = true,
                    snakeDestroyed = true,
                    updatedPlayer = player,
                    updatedLinks = remainingLinks,
                    finalTile = snake.startTile,
                    heroFxType = DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                    isPositiveHighlight = true,
                    bannerMessage = "قدرت سفارشی ${player.character.englishName}! 🛡️ نیش مار در خانه ${snake.startTile.toPersianDigits()} خنثی و مار نابود شد!",
                    bannerType = BannerType.CHARACTER_ABILITY,
                    playAbilitySound = true
                )
            } else if (triggered && (customSpec.effectType == CustomAbilityEffectType.BONUS_STEPS_BOOST ||
                    customSpec.effectType == CustomAbilityEffectType.INCREASE_CHANCE)
            ) {
                val boostDest = (snake.startTile + 2).coerceAtMost(maxTiles)
                return SnakeSlideOutcome(
                    blockedOrSlain = true,
                    boostedForward = true,
                    updatedPlayer = player.copy(currentTile = boostDest),
                    updatedLinks = boardLinks,
                    finalTile = boostDest,
                    heroFxType = DomainBoardFxType.SHIELD_BLOCK_SUCCESS,
                    isPositiveHighlight = true,
                    bannerMessage = "معجزهٔ سفارشی ${player.character.englishName}! ✨ نیش مار به +۲ خانه جهش رو به جلو تبدیل شد!",
                    bannerType = BannerType.CHARACTER_ABILITY,
                    playAbilitySound = true
                )
            }
        }

        // 1. Guardian Shield, Guardian Angel, or Phantom Phase
        val protection = evaluateProtectionDefense(
            defender = player,
            threatCategory = ShieldThreatCategory.SNAKE,
            threatNamePersian = "مار خانه ${snake.startTile.toPersianDigits()}",
            baseConfiguredPercent = baseShieldProbability
        )
        if (protection.isBlocked) {
            return SnakeSlideOutcome(
                blockedOrSlain = true,
                updatedPlayer = protection.updatedDefender,
                updatedLinks = boardLinks,
                finalTile = snake.startTile,
                heroFxType = protection.heroFxType,
                isPositiveHighlight = true,
                bannerMessage = protection.bannerMessage.orEmpty(),
                bannerType = protection.bannerType,
                playAbilitySound = true
            )
        }

        // 2. Snake Hunter (Blade): 65% chance to slay and destroy the snake
        if (ability == CharacterAbilityType.SNAKE_HUNTER && randomProvider.nextDouble() < 0.65) {
            val remainingLinks = boardLinks.filterNot {
                it.type == LinkType.SNAKE && it.startTile == snake.startTile
            }
            return SnakeSlideOutcome(
                blockedOrSlain = true,
                snakeDestroyed = true,
                updatedPlayer = player,
                updatedLinks = remainingLinks,
                finalTile = snake.startTile,
                heroFxType = DomainBoardFxType.EXECUTIONER_SLASH,
                isPositiveHighlight = true,
                bannerMessage = "شمشیر شکارچی مار! ⚔️ ${player.name} مار خانه ${snake.startTile.toPersianDigits()} را از صفحه نابود کرد و سقوط نکرد!",
                bannerType = BannerType.CHARACTER_ABILITY,
                playAbilitySound = true
            )
        }

        // 3. Multi-tier snake drop reduction
        val finalTailTile = when {
            ability == CharacterAbilityType.EARTH_TITAN -> {
                val midDrop = (snake.startTile - snake.endTile) / 2
                (snake.startTile - midDrop).coerceAtLeast(snake.endTile)
            }
            ability == CharacterAbilityType.WORM_IMMUNE -> {
                val reducedDrop = ((snake.startTile - snake.endTile) * 0.7f).roundToInt().coerceAtLeast(2)
                (snake.startTile - reducedDrop).coerceAtLeast(snake.endTile)
            }
            ability == CharacterAbilityType.LADDER_MAGE -> {
                (snake.endTile - 2).coerceAtLeast(2)
            }
            customSpec?.target == CustomAbilityTarget.SNAKES &&
                (customSpec.effectType == CustomAbilityEffectType.HALVE_PENALTY ||
                    customSpec.effectType == CustomAbilityEffectType.REDUCE_EFFECT) -> {
                val fullDrop = snake.startTile - snake.endTile
                val reducedDrop = (fullDrop * (100 - customSpec.percentageOrValue.coerceIn(10, 85)) / 100f)
                    .roundToInt()
                    .coerceAtLeast(2)
                (snake.startTile - reducedDrop).coerceIn(snake.endTile, snake.startTile - 1)
            }
            else -> snake.endTile
        }.coerceIn(1, maxTiles)

        val isAtlasBerserker = ability == CharacterAbilityType.EARTH_TITAN
        val isPhoenixDragonSoul = ability == CharacterAbilityType.PHOENIX_WINGS
        val poisonedResources = player.resources.applyStatusEffect(
            ActiveStatusEffect(StatusEffectType.POISON, turnsRemaining = 1, magnitude = 1)
        )
        val updatedPlayer = player.copy(
            snakesBitten = player.snakesBitten + 1,
            previousTurnTile = snake.startTile,
            currentTile = finalTailTile,
            berserkerRageTurns = if (isAtlasBerserker) 2 else player.berserkerRageTurns,
            dragonSoulBoostTurns = if (isPhoenixDragonSoul) 2 else player.dragonSoulBoostTurns,
            resources = poisonedResources
        )
        val bannerMsg = when {
            isAtlasBerserker ->
                "پوست صخره‌ای و خشم برسرکر (Berserker)! 🪨🔥 ${player.name} نصف مسیر را ایستاد (خانه ${finalTailTile.toPersianDigits()}) و برای ۲ نوبت +۲ خانه خشم حرکت گرفت!"
            finalTailTile > snake.endTile ->
                "مقاومت ویژه! 🛡️ ${player.name} جلوی سقوط کامل مار را گرفت و در خانه ${finalTailTile.toPersianDigits()} ایستاد!"
            else ->
                "${player.name} از مار خانه ${snake.startTile.toPersianDigits()} به ${finalTailTile.toPersianDigits()} سر خورد 🐍"
        }

        return SnakeSlideOutcome(
            blockedOrSlain = false,
            updatedPlayer = updatedPlayer,
            updatedLinks = boardLinks,
            finalTile = finalTailTile,
            heroFxType = if (isAtlasBerserker) DomainBoardFxType.BERSERKER_RAGE else DomainBoardFxType.NONE,
            isPositiveHighlight = false,
            bannerMessage = bannerMsg,
            bannerType = BannerType.SNAKE_BITE
        )
    }
}
