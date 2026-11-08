package com.example.model

/**
 * =========================================================================================
 * HERO INTERACTION & COMBAT SYSTEM (سیستم مقابله و مبارزهٔ قهرمانان)
 * =========================================================================================
 * Provides a mathematically sound, rule-bound, deterministic combat resolution engine
 * for when two or more hero tokens meet on the board or target each other with abilities.
 *
 * Rules:
 * 1. Independent Probabilities: Attack success and defender counter/resistance are evaluated
 *    independently (no arbitrary probability compounding).
 * 2. Single RNG Roll per Action: Exactly one deterministic or random roll per resolution step.
 * 3. Atomic Execution: State is updated in one clean snapshot; no infinite loops or re-entrancy.
 * 4. Priority-Based Conflict Resolution:
 *    Priority 1: Defensive Passives & Auras (Mirage, Phantom Phase, Guardian Angel)
 *    Priority 2: Active Shields & Parries (Guardian Shield, Heavy Anchor)
 *    Priority 3: Class Trait Resistances (Dragon Soul vs Mage, Survivor vs Poison/Curse)
 *    Priority 4: Offensive Ability Execution (Executioner Hunt, Trickster Swap, Berserker Slam)
 * 5. Full Compatibility Matrix: Every hero pair has a deterministic outcome profile.
 */

enum class CombatOutcomeType(val persianLabel: String, val emoji: String) {
    ATTACK_HIT("حمله موفق", "💥"),
    SHIELD_PARRY("دفع کامل با سپر", "🛡️"),
    CRITICAL_PARRY("دفع بی‌نقص بدون مصرف سپر", "✨"),
    RESISTANCE_MITIGATED("مقاومت فعال شد", "🌿"),
    ILLUSION_DIVERTED("فریب با سراب توهمی", "🪄"),
    PHANTOM_EVADED("عبور شبح‌وار", "👻"),
    ANGEL_PROTECTED("سپر فرشتهٔ نجات", "🌟"),
    ANCHOR_IMMUNE("لنگر سنگین بی‌اثر ساخت", "⚓"),
    ATTACK_FAILED("حمله شکست خورد", "❌"),
    POSITION_SWAPPED("جابه‌جایی موقعیت ثبت شد", "🌀"),
    NEUTRAL_COEXIST("هم‌زیستی مسالمت‌آمیز روی یک خانه", "🤝")
}

data class CombatLogEntry(
    val id: Long = System.currentTimeMillis(),
    val turnNumber: Int,
    val attackerId: Int,
    val attackerName: String,
    val attackerHeroClass: HeroClass,
    val defenderId: Int,
    val defenderName: String,
    val defenderHeroClass: HeroClass,
    val tile: Int,
    val outcomeType: CombatOutcomeType,
    val description: String,
    val attackerTileBefore: Int,
    val attackerTileAfter: Int,
    val defenderTileBefore: Int,
    val defenderTileAfter: Int,
    val attackRoll: Int,
    val defenseRoll: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Real-time Hero Clash Event for displaying an animated confrontation modal
 * when an ability or collision resolves between two heroes on the board.
 */
data class HeroClashEvent(
    val attackerId: Int,
    val attackerName: String,
    val attackerCharacter: GameCharacter,
    val defenderId: Int,
    val defenderName: String,
    val defenderCharacter: GameCharacter,
    val abilityName: String,
    val tile: Int,
    val outcomeType: CombatOutcomeType,
    val defenseStatusDescription: String,
    val resultMessage: String,
    val attackRoll: Int,
    val defenseRoll: Int,
    val knockbackSteps: Int = 0
)

/**
 * The Hero Combat Compatibility Matrix entry defining how Class A interacts with Class B.
 */
data class ClassCompatibilityRule(
    val attackerClass: HeroClass,
    val defenderClass: HeroClass,
    val baseSuccessPercent: Int,
    val counterChanceBonusPercent: Int,
    val interactionSummary: String
)

object HeroCombatMatrix {

    /**
     * Complete Compatibility Matrix between the 8 hero classes.
     * Specific pairings highlight the requested interactions:
     * - Executioner (Warrior/Assassin) vs Guardian (Guardian) -> Shield neutralization
     * - Assassin vs Shield Hero -> High parry chance
     * - Trickster vs Time Keeper -> Displacement counter/temporal rewind
     * - Dragon Soul (Survivor/Warrior) vs Mage -> Magic surge absorption
     * - Phantom (Controller/Assassin) vs Trap/Negative obstacles -> Trap evasion
     */
    private val compatibilityRules: List<ClassCompatibilityRule> = listOf(
        // Executioner / Warrior vs Guardian
        ClassCompatibilityRule(
            attackerClass = HeroClass.WARRIOR,
            defenderClass = HeroClass.GUARDIAN,
            baseSuccessPercent = 60,
            counterChanceBonusPercent = 25,
            interactionSummary = "مدافع (Guardian) با سپر نوری احتمال بالایی برای مهار ضربات مستقیم جنگجو (Warrior) دارد."
        ),
        // Assassin vs Guardian
        ClassCompatibilityRule(
            attackerClass = HeroClass.ASSASSIN,
            defenderClass = HeroClass.GUARDIAN,
            baseSuccessPercent = 55,
            counterChanceBonusPercent = 30,
            interactionSummary = "سپر طلایی مدافع، خیزش‌های غافلگیرانهٔ ترور را دفع و مهاجم را دچار موج شوک بازتابی می‌کند."
        ),
        // Trickster vs Controller (Time Keeper / Gravity)
        ClassCompatibilityRule(
            attackerClass = HeroClass.TRICKSTER,
            defenderClass = HeroClass.CONTROLLER,
            baseSuccessPercent = 50,
            counterChanceBonusPercent = 20,
            interactionSummary = "کنترل‌گر زمان می‌تواند از تغییر مکان خود جلوگیری کند یا اثرات جابه‌جایی فریبکار را معکوس سازد."
        ),
        // Warrior vs Mage
        ClassCompatibilityRule(
            attackerClass = HeroClass.WARRIOR,
            defenderClass = HeroClass.MAGE,
            baseSuccessPercent = 75,
            counterChanceBonusPercent = 10,
            interactionSummary = "جادوگر در برابر ضربهٔ نزدیک جنگجو شکننده است، مگر اینکه بدل سراب توهمی داشته باشد."
        ),
        // Mage vs Survivor (Dragon Soul vs Mage)
        ClassCompatibilityRule(
            attackerClass = HeroClass.MAGE,
            defenderClass = HeroClass.SURVIVOR,
            baseSuccessPercent = 45,
            counterChanceBonusPercent = 35,
            interactionSummary = "روح اژدها (Survivor) در برابر جادوی تله‌ها مقاومت کرده و انرژی طلسم را به سرعت تبدیل می‌کند."
        ),
        // Assassin vs Controller
        ClassCompatibilityRule(
            attackerClass = HeroClass.ASSASSIN,
            defenderClass = HeroClass.CONTROLLER,
            baseSuccessPercent = 65,
            counterChanceBonusPercent = 15,
            interactionSummary = "شکارچی می‌تواند پیشتاز کنترل‌گر را زمین‌گیر کند، اما کنترل‌گر سرعت حمله را کاهش می‌دهد."
        ),
        // Trickster vs Support
        ClassCompatibilityRule(
            attackerClass = HeroClass.TRICKSTER,
            defenderClass = HeroClass.SUPPORT,
            baseSuccessPercent = 60,
            counterChanceBonusPercent = 20,
            interactionSummary = "حمایت‌گر با نور محافظتی می‌تواند از انتقال کامل طلسم‌ها به پیشتاز جلوگیری نماید."
        ),
        // Controller vs Guardian
        ClassCompatibilityRule(
            attackerClass = HeroClass.CONTROLLER,
            defenderClass = HeroClass.GUARDIAN,
            baseSuccessPercent = 55,
            counterChanceBonusPercent = 20,
            interactionSummary = "کنترل‌گر با انجماد تلاش می‌کند گارد محافظ را بشکند؛ سپر در صورت موفقیت ضربه را مهار می‌کند."
        )
    )

    fun getCompatibilityRule(attacker: HeroClass, defender: HeroClass): ClassCompatibilityRule {
        return compatibilityRules.find { it.attackerClass == attacker && it.defenderClass == defender }
            ?: ClassCompatibilityRule(
                attackerClass = attacker,
                defenderClass = defender,
                baseSuccessPercent = 65,
                counterChanceBonusPercent = 0,
                interactionSummary = "تعامل استاندارد بر اساس آمار تهاجم، دفاع و مقاومت دو قهرمان."
            )
    }
}

/**
 * Result of resolving a combat collision between two heroes on the same tile.
 */
data class HeroCombatResolution(
    val outcomeType: CombatOutcomeType,
    val attackerId: Int,
    val defenderId: Int,
    val tile: Int,
    val attackerNewTile: Int,
    val defenderNewTile: Int,
    val attackerShieldConsumed: Boolean = false,
    val defenderShieldConsumed: Boolean = false,
    val defenderCriticalParry: Boolean = false,
    val defenderAngelConsumed: Boolean = false,
    val defenderPhantomConsumed: Boolean = false,
    val defenderDecoyConsumed: Boolean = false,
    val defenderEntersBerserkerRage: Boolean = false,
    val defenderEntersDragonSoulBoost: Boolean = false,
    val combatLogMessage: String,
    val bannerMessage: String,
    val attackRoll: Int,
    val defenseRoll: Int
)

/**
 * Deterministic Engine that evaluates collision and combat interactions.
 */
object HeroCombatEngine {

    /**
     * Resolves the interaction when an `attacker` token lands on the same tile as a `defender` token.
     * Evaluates active abilities, passive protections, stat advantages, and generates an atomic outcome.
     *
     * @param attacker The active player landing on the tile.
     * @param defender The occupying player already on that tile.
     * @param tile The tile number where they collide.
     * @param forcedAttackRoll Optional deterministic roll (0..99) for testing.
     * @param forcedDefenseRoll Optional deterministic roll (0..99) for testing.
     * @param baseShieldProbability Base defense rate from settings (e.g. 75%).
     */
    fun resolveTileCollision(
        attacker: Player,
        defender: Player,
        tile: Int,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null,
        baseShieldProbability: Int = 75
    ): HeroCombatResolution {
        val attackerHero = attacker.character.heroBlueprint
        val defenderHero = defender.character.heroBlueprint
        val attackerClass = attackerHero.primaryClass
        val defenderClass = defenderHero.primaryClass

        val attackerStats = attackerHero.stats
        val defenderStats = defenderHero.stats

        val compatibility = HeroCombatMatrix.getCompatibilityRule(attackerClass, defenderClass)

        // Evaluate single attack roll and defense roll
        val attackRoll = forcedAttackRoll ?: kotlin.random.Random.nextInt(100)
        val defenseRoll = forcedDefenseRoll ?: kotlin.random.Random.nextInt(100)

        // -------------------------------------------------------------
        // PRIORITY 1: Absolute Defensive Illusion/Phantom/Angel Auras
        // -------------------------------------------------------------

        // Priority 1A: Illusionist Mirage Decoy (Luna)
        if (defender.illusionDecoyTile != null && defender.illusionTurnsRemaining > 0) {
            val log = "«${attacker.name}» (${attackerHero.persianTitle}) به خانهٔ «${defender.name}» یورش برد، اما ضربه به بدل توهمی برخورد کرد و موقعیت واقعی حفظ شد."
            val banner = "سراب توهم‌زا! 🪄 حملهٔ ${attacker.name} توسط بدل توهمی ${defender.name} جذب شد و خنثی گشت!"
            return HeroCombatResolution(
                outcomeType = CombatOutcomeType.ILLUSION_DIVERTED,
                attackerId = attacker.id,
                defenderId = defender.id,
                tile = tile,
                attackerNewTile = tile,
                defenderNewTile = tile,
                defenderDecoyConsumed = true,
                combatLogMessage = log,
                bannerMessage = banner,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // Priority 1B: Guardian Angel Wings (Stella)
        if (defender.guardianAngelProtected) {
            val log = "«${attacker.name}» به «${defender.name}» یورش برد، اما بال‌های نورانی فرشتهٔ محافظ مانع شد و حمله را خنثی کرد."
            val banner = "سپر فرشتهٔ نجات! 🌟 بال‌های نورانی فرشته از ${defender.name} در برابر حملهٔ ${attacker.name} محافظت کامل کردند!"
            return HeroCombatResolution(
                outcomeType = CombatOutcomeType.ANGEL_PROTECTED,
                attackerId = attacker.id,
                defenderId = defender.id,
                tile = tile,
                attackerNewTile = tile,
                defenderNewTile = tile,
                defenderAngelConsumed = true,
                combatLogMessage = log,
                bannerMessage = banner,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // Priority 1C: Phantom Phase (Zephyr)
        if (defender.phantomPhaseActive) {
            val log = "«${attacker.name}» با «${defender.name}» برخورد کرد، اما ${defender.name} در حالت شبح باد بود و بدون آسیب از کنار او گذشت."
            val banner = "گام شبح باد! 👻🌪️ ${defender.name} شبح‌وار از حملهٔ ${attacker.name} جاخالی داد!"
            return HeroCombatResolution(
                outcomeType = CombatOutcomeType.PHANTOM_EVADED,
                attackerId = attacker.id,
                defenderId = defender.id,
                tile = tile,
                attackerNewTile = tile,
                defenderNewTile = tile,
                defenderPhantomConsumed = true,
                combatLogMessage = log,
                bannerMessage = banner,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // -------------------------------------------------------------
        // PRIORITY 2: Heavy Anchor & Displacement Immunity (Kai / Atlas)
        // -------------------------------------------------------------
        val isDefenderHeavyAnchor = defender.character.abilityType == CharacterAbilityType.HALF_REVERSE ||
            defender.character.abilityType == CharacterAbilityType.EARTH_TITAN

        val isAttackerExecutionerOrWarrior = attacker.character.abilityType == CharacterAbilityType.SNAKE_HUNTER ||
            attackerClass == HeroClass.WARRIOR ||
            attackerClass == HeroClass.ASSASSIN

        // -------------------------------------------------------------
        // PRIORITY 3: Active Guardian Shield Defense (Jack)
        // -------------------------------------------------------------
        val hasChargedShield = defender.isShieldCharged &&
            (defender.character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD || defenderClass == HeroClass.GUARDIAN)

        if (hasChargedShield) {
            val effectiveDefenseChance = defenderStats.computeShieldDefenseChance(
                baseConfiguredPercent = baseShieldProbability + compatibility.counterChanceBonusPercent,
                threatCategory = ShieldThreatCategory.EXECUTIONER_OR_ABILITY
            )

            if (defenseRoll < effectiveDefenseChance) {
                val isCriticalParry = defenseRoll < defenderStats.criticalParryPreserveShieldPercent
                val parryNote = if (isCriticalParry) "دفع بی‌نقص بدون مصرف سپر! ✨" else "سپر مصرف شد (شارژ مجدد: ۳ نوبت)"
                val log = if (isCriticalParry) {
                    "«${defender.name}» با دفع بی‌نقص سپر نورانی، حملهٔ «${attacker.name}» را مهار کرد بدون آنکه سپر مصرف شود."
                } else {
                    "«${defender.name}» با سپر نورانی خود، ضربهٔ «${attacker.name}» را دفع کرد."
                }
                val banner = "سپر حمله را دفع کرد! 🛡️✨ ${defender.name} در برابر یورش ${attacker.name} استقامت کرد — $parryNote"
                return HeroCombatResolution(
                    outcomeType = if (isCriticalParry) CombatOutcomeType.CRITICAL_PARRY else CombatOutcomeType.SHIELD_PARRY,
                    attackerId = attacker.id,
                    defenderId = defender.id,
                    tile = tile,
                    attackerNewTile = tile,
                    defenderNewTile = tile,
                    defenderShieldConsumed = !isCriticalParry,
                    defenderCriticalParry = isCriticalParry,
                    combatLogMessage = log,
                    bannerMessage = banner,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll
                )
            }
        }

        // -------------------------------------------------------------
        // PRIORITY 4: Offensive Attack Resolution
        // -------------------------------------------------------------

        // Case 4A: Executioner Hunt (Blade / SNAKE_HUNTER) or aggressive Warrior/Assassin charge
        if (isAttackerExecutionerOrWarrior) {
            // Check heavy anchor mitigation
            if (isDefenderHeavyAnchor) {
                val log = "«${attacker.name}» قصد عقب راندن «${defender.name}» را داشت، اما زره صخره‌ای و لنگر تعادل ${defender.name} مانع جابه‌جایی شد."
                val banner = "لنگر سنگین بی‌اثر ساخت! ⚓ پایدار ماندن ${defender.name} در برابر ضربهٔ ${attacker.name}!"
                return HeroCombatResolution(
                    outcomeType = CombatOutcomeType.ANCHOR_IMMUNE,
                    attackerId = attacker.id,
                    defenderId = defender.id,
                    tile = tile,
                    attackerNewTile = tile,
                    defenderNewTile = tile,
                    combatLogMessage = log,
                    bannerMessage = banner,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll
                )
            }

            // Stat-based attack chance vs defender resistance
            val successChance = (compatibility.baseSuccessPercent + (attackerStats.attack - defenderStats.resistance) * 4).coerceIn(35, 95)

            if (attackRoll < successChance) {
                // Successful Hunt Knockback!
                val knockbackSteps = (tile / 2).coerceAtLeast(1)
                val newDefenderTile = (tile - knockbackSteps).coerceAtLeast(2)

                val entersBerserker = defender.character.abilityType == CharacterAbilityType.EARTH_TITAN
                val entersDragonSoul = defender.character.abilityType == CharacterAbilityType.PHOENIX_WINGS

                val log = "«${attacker.name}» با شمشیر شکارچی بر «${defender.name}» پیروز شد و او را ${knockbackSteps} خانه به عقب (خانهٔ ${newDefenderTile}) پرتاب کرد."
                val banner = "حمله موفق بود! بازیکن شکار شد! ⚔️💥 ${attacker.name} مهرهٔ ${defender.name} را ${knockbackSteps} خانه به عقب پرتاب کرد!"

                return HeroCombatResolution(
                    outcomeType = CombatOutcomeType.ATTACK_HIT,
                    attackerId = attacker.id,
                    defenderId = defender.id,
                    tile = tile,
                    attackerNewTile = tile,
                    defenderNewTile = newDefenderTile,
                    defenderEntersBerserkerRage = entersBerserker,
                    defenderEntersDragonSoulBoost = entersDragonSoul,
                    combatLogMessage = log,
                    bannerMessage = banner,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll
                )
            } else {
                // Resistance Mitigated / Attack Failed
                val entersDragonSoul = defender.character.abilityType == CharacterAbilityType.PHOENIX_WINGS
                val log = "«${attacker.name}» به «${defender.name}» حمله کرد، اما مقاومت بدنی ${defender.name} ضربه را خنثی کرد."
                val banner = "مقاومت فعال شد! 🌿 ${defender.name} در برابر یورش ${attacker.name} مقاومت کرد و آسیبی ندید!"
                return HeroCombatResolution(
                    outcomeType = CombatOutcomeType.RESISTANCE_MITIGATED,
                    attackerId = attacker.id,
                    defenderId = defender.id,
                    tile = tile,
                    attackerNewTile = tile,
                    defenderNewTile = tile,
                    defenderEntersDragonSoulBoost = entersDragonSoul,
                    combatLogMessage = log,
                    bannerMessage = banner,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll
                )
            }
        }

        // Case 4B: Peaceful Co-existence / Non-combatant collision
        val log = "«${attacker.name}» و «${defender.name}» هر دو در خانهٔ ${tile} هم‌گام شدند."
        val banner = "هم‌نشینی در خانهٔ ${tile}! 🤝 ${attacker.name} و ${defender.name} هر دو در یک خانه قرار گرفتند."
        return HeroCombatResolution(
            outcomeType = CombatOutcomeType.NEUTRAL_COEXIST,
            attackerId = attacker.id,
            defenderId = defender.id,
            tile = tile,
            attackerNewTile = tile,
            defenderNewTile = tile,
            combatLogMessage = log,
            bannerMessage = banner,
            attackRoll = attackRoll,
            defenseRoll = defenseRoll
        )
    }
}
