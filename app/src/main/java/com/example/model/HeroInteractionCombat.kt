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
 * Fully unified with CombatResolutionEngine.
 */
object HeroCombatEngine {
    private val unifiedEngine = com.example.engine.CombatResolutionEngine()

    /**
     * Resolves the interaction when an `attacker` token lands on the same tile as a `defender` token.
     * Evaluates active abilities, passive protections, stat advantages, and generates an atomic outcome.
     */
    fun resolveTileCollision(
        attacker: Player,
        defender: Player,
        tile: Int,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null,
        baseShieldProbability: Int = 75
    ): HeroCombatResolution {
        val res = unifiedEngine.resolveClash(
            attacker = attacker,
            defender = defender,
            teamManager = null,
            forcedAttackRoll = forcedAttackRoll,
            forcedDefenseRoll = forcedDefenseRoll,
            baseShieldProbability = baseShieldProbability
        )
        return HeroCombatResolution(
            outcomeType = res.outcomeType,
            attackerId = attacker.id,
            defenderId = defender.id,
            tile = tile,
            attackerNewTile = res.attackerFinalTile,
            defenderNewTile = res.defenderFinalTile,
            attackerShieldConsumed = res.attackerShieldConsumed,
            defenderShieldConsumed = res.defenderShieldConsumed,
            defenderCriticalParry = res.defenderCriticalParry,
            defenderAngelConsumed = res.defenderAngelConsumed,
            defenderPhantomConsumed = res.defenderPhantomConsumed,
            defenderDecoyConsumed = res.defenderDecoyConsumed,
            defenderEntersBerserkerRage = res.defenderEntersBerserkerRage,
            defenderEntersDragonSoulBoost = res.defenderEntersDragonSoulBoost,
            combatLogMessage = res.description,
            bannerMessage = res.description,
            attackRoll = res.attackRoll,
            defenseRoll = res.defenseRoll
        )
    }
}
