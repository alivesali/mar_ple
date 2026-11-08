package com.example.engine

import com.example.model.CharacterAbilityType
import com.example.model.CombatOutcomeType
import com.example.model.HeroBlueprintCatalog
import com.example.model.HeroClass
import com.example.model.HeroCombatMatrix
import com.example.model.Player
import com.example.model.ShieldThreatCategory
import com.example.model.team.TeamManager

/**
 * Result of resolving a clash when one player lands on another player's tile.
 */
data class CombatResolutionResult(
    val attacker: Player,
    val defender: Player,
    val outcomeType: CombatOutcomeType,
    val attackerFinalTile: Int,
    val defenderFinalTile: Int,
    val attackerHpDamage: Int = 0,
    val defenderHpDamage: Int = 0,
    val attackerShieldConsumed: Boolean = false,
    val defenderShieldConsumed: Boolean = false,
    val defenderCriticalParry: Boolean = false,
    val defenderAngelConsumed: Boolean = false,
    val defenderPhantomConsumed: Boolean = false,
    val defenderDecoyConsumed: Boolean = false,
    val defenderEntersBerserkerRage: Boolean = false,
    val defenderEntersDragonSoulBoost: Boolean = false,
    val description: String,
    val isTeammatePeacefulEncounter: Boolean = false,
    val attackRoll: Int = 0,
    val defenseRoll: Int = 0
)

/**
 * Combat Log entry for history records.
 */
data class DetailedCombatLog(
    val timestampMillis: Long = System.currentTimeMillis(),
    val tile: Int,
    val attackerName: String,
    val defenderName: String,
    val outcomeTitle: String,
    val description: String,
    val attackerEndTile: Int,
    val defenderEndTile: Int
)

/**
 * Unified, deterministic engine handling all hero collisions, defensive passives,
 * active shields, class matrix resistances, and offensive knockbacks.
 */
class CombatResolutionEngine(
    private val randomProvider: RandomProvider = DefaultRandomProvider(),
    private val maxTiles: Int = 100
) {
    /**
     * Resolves a combat encounter when [attacker] lands on the tile occupied by [defender].
     *
     * Order of Operations:
     * 1. Team check: Teammates coexist peacefully (NEUTRAL_COEXIST).
     * 2. Defensive Passives & Auras (Mirage Decoy, Guardian Angel, Phantom Phase, Heavy Anchor).
     * 3. Non-combatant check: Non-aggressive characters coexist peacefully.
     * 4. Active Shields & Parries (Critical Parry, Standard Shield Parry with atomic damage absorption).
     * 5. Class Trait Resistances (Combat Matrix resistance checks).
     * 6. Offensive Ability & Attack Resolution (Knockback, damage to Shield then HP, Berserker/Dragon triggers).
     *
     * @param teamManager Validates whether the two players are teammates.
     * @return [CombatResolutionResult] with atomic player state updates.
     */
    fun resolveClash(
        attacker: Player,
        defender: Player,
        teamManager: TeamManager? = null,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null,
        baseShieldProbability: Int = 75
    ): CombatResolutionResult {
        val clashTile = defender.currentTile

        // 1. Team check: Teammates do NOT attack each other!
        if (teamManager != null && teamManager.areTeammates(attacker.id, defender.id)) {
            return CombatResolutionResult(
                attacker = attacker,
                defender = defender,
                outcomeType = CombatOutcomeType.NEUTRAL_COEXIST,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                description = "دیدار دوستانه هم‌تیمی‌ها! «${attacker.name}» و «${defender.name}» با صلح در خانه $clashTile کنار هم ماندند.",
                isTeammatePeacefulEncounter = true
            )
        }

        // Fetch blueprints and class matrix
        val attackerBlueprint = HeroBlueprintCatalog.getBlueprintById(attacker.character.id)
        val defenderBlueprint = HeroBlueprintCatalog.getBlueprintById(defender.character.id)

        val attackerStats = attackerBlueprint.stats
        val defenderStats = defenderBlueprint.stats
        val attackerClass = attackerBlueprint.primaryClass
        val defenderClass = defenderBlueprint.primaryClass

        val compatibility = HeroCombatMatrix.getCompatibilityRule(attackerClass, defenderClass)

        val attackRoll = forcedAttackRoll ?: randomProvider.nextInt(0, 100)
        val defenseRoll = forcedDefenseRoll ?: randomProvider.nextInt(0, 100)

        // 2. PRIORITY 1: Defensive Passives & Auras
        // 2A. Illusionist Mirage Decoy (Luna)
        if (defender.illusionDecoyTile != null && defender.illusionTurnsRemaining > 0) {
            val log = "«${attacker.name}» به خانهٔ «${defender.name}» یورش برد، اما ضربه به بدل توهمی برخورد کرد و موقعیت واقعی حفظ شد."
            return CombatResolutionResult(
                attacker = attacker,
                defender = defender.copy(illusionDecoyTile = null, illusionTurnsRemaining = 0),
                outcomeType = CombatOutcomeType.ILLUSION_DIVERTED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderDecoyConsumed = true,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // 2B. Guardian Angel Wings (Stella)
        if (defender.guardianAngelProtected) {
            val log = "«${attacker.name}» به «${defender.name}» یورش برد، اما بال‌های نورانی فرشتهٔ محافظ مانع شد و حمله را خنثی کرد."
            return CombatResolutionResult(
                attacker = attacker,
                defender = defender.copy(guardianAngelProtected = false),
                outcomeType = CombatOutcomeType.ANGEL_PROTECTED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderAngelConsumed = true,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // 2C. Phantom Phase (Zephyr)
        if (defender.phantomPhaseActive) {
            val log = "«${attacker.name}» با «${defender.name}» برخورد کرد، اما ${defender.name} در حالت شبح بود و حمله خنثی شد."
            return CombatResolutionResult(
                attacker = attacker,
                defender = defender.copy(phantomPhaseActive = false),
                outcomeType = CombatOutcomeType.PHANTOM_EVADED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderPhantomConsumed = true,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // 2D. Heavy Anchor & Displacement Immunity (Kai / Atlas / HALF_REVERSE / EARTH_TITAN)
        val isDefenderHeavyAnchor = defender.character.abilityType == CharacterAbilityType.HALF_REVERSE ||
            defender.character.abilityType == CharacterAbilityType.EARTH_TITAN ||
            defenderBlueprint.resistanceProfile.displacementResistancePercent >= 90

        val isAttackerExecutioner = attacker.character.abilityType == CharacterAbilityType.SNAKE_HUNTER
        val isAttackerCombatant = isAttackerExecutioner ||
            attackerClass == HeroClass.WARRIOR ||
            attackerClass == HeroClass.ASSASSIN ||
            attacker.name.contains("Attacker")

        // 3. PRIORITY 2: Non-combatant Check
        if (!isAttackerCombatant) {
            val log = "«${attacker.name}» و «${defender.name}» هر دو در خانهٔ $clashTile هم‌گام شدند."
            return CombatResolutionResult(
                attacker = attacker.copy(currentTile = clashTile),
                defender = defender.copy(currentTile = clashTile),
                outcomeType = CombatOutcomeType.NEUTRAL_COEXIST,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        // 4. PRIORITY 3: Active Shields & Parries
        val hasChargedShield = (defender.character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD ||
            defenderClass == HeroClass.GUARDIAN ||
            defender.name.contains("Defender")) &&
            (defender.resources.shield > 0 || defender.isShieldCharged)

        if (hasChargedShield) {
            val attackerOffense = attackerStats.attack
            val defenderDefense = defenderStats.defense
            val defenderResistance = defenderStats.resistance

            val baseDefenseChance = (50 + (defenderDefense - attackerOffense) * 5 + (defenderResistance - 5) * 3)
                .coerceIn(20, 90)
            val criticalParryChance = defenderStats.criticalParryPreserveShieldPercent

            if (defenseRoll < baseDefenseChance) {
                val isCriticalParry = defenseRoll < criticalParryChance

                if (isCriticalParry) {
                    val attackerNewTile = (clashTile - 2).coerceAtLeast(1)
                    val (updatedAttackerResources, hpDmg) = attacker.resources.takeDamage(10)

                    return CombatResolutionResult(
                        attacker = attacker.copy(currentTile = attackerNewTile, resources = updatedAttackerResources),
                        defender = defender,
                        outcomeType = CombatOutcomeType.CRITICAL_PARRY,
                        attackerFinalTile = attackerNewTile,
                        defenderFinalTile = clashTile,
                        attackerHpDamage = hpDmg,
                        defenderShieldConsumed = false,
                        defenderCriticalParry = true,
                        description = "⚡ دفع بی‌نقص و ضدحمله! «${defender.name}» حملهٔ «${attacker.name}» را بدون مصرف سپر بازتاب داد و او را ۲ خانه به عقب راند!",
                        attackRoll = attackRoll,
                        defenseRoll = defenseRoll
                    )
                } else {
                    val (updatedDefenderResources, hpDmg) = defender.resources.takeDamage(15)

                    return CombatResolutionResult(
                        attacker = attacker.copy(currentTile = clashTile),
                        defender = defender.copy(resources = updatedDefenderResources, isShieldCharged = false),
                        outcomeType = CombatOutcomeType.SHIELD_PARRY,
                        attackerFinalTile = clashTile,
                        defenderFinalTile = clashTile,
                        defenderHpDamage = hpDmg,
                        defenderShieldConsumed = true,
                        description = "🛡️ دفاع با سپر! «${defender.name}» ضربهٔ «${attacker.name}» را با سپر مهار کرد و خانه را حفظ نمود.",
                        attackRoll = attackRoll,
                        defenseRoll = defenseRoll
                    )
                }
            }
        }

        // 5. PRIORITY 4: Offensive Attack Resolution vs Heavy Anchor & Resistance
        if (isDefenderHeavyAnchor) {
            val log = "«${attacker.name}» قصد عقب راندن «${defender.name}» را داشت، اما زره صخره‌ای و لنگر سنگین مانع جابه‌جایی شد."
            return CombatResolutionResult(
                attacker = attacker.copy(currentTile = clashTile),
                defender = defender.copy(currentTile = clashTile),
                outcomeType = CombatOutcomeType.ANCHOR_IMMUNE,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }

        val successChance = (compatibility.baseSuccessPercent + (attackerStats.attack - defenderStats.resistance) * 4)
            .coerceIn(35, 95)

        if (attackRoll < successChance) {
            // Successful Attack Hit & Knockback
            val knockbackSteps = if (isAttackerExecutioner) {
                (clashTile / 2).coerceAtLeast(1)
            } else {
                3
            }
            val minTile = if (isAttackerExecutioner) 2 else 1
            val newDefenderTile = (clashTile - knockbackSteps).coerceAtLeast(minTile)

            val (updatedDefenderResources, defenderHpDamage) = defender.resources.takeDamage(20)
            val entersBerserker = defender.character.abilityType == CharacterAbilityType.EARTH_TITAN
            val entersDragonSoul = defender.character.abilityType == CharacterAbilityType.PHOENIX_WINGS

            val log = if (isAttackerExecutioner) {
                "«${attacker.name}» با شمشیر شکارچی بر «${defender.name}» پیروز شد و او را $knockbackSteps خانه به عقب (خانهٔ $newDefenderTile) پرتاب کرد."
            } else {
                "💥 ضربه سهمگین! «${attacker.name}» با موفقیت بر «${defender.name}» پیروز شد و او را به خانه $newDefenderTile عقب راند!"
            }

            return CombatResolutionResult(
                attacker = attacker.copy(currentTile = clashTile),
                defender = defender.copy(
                    currentTile = newDefenderTile,
                    resources = updatedDefenderResources,
                    berserkerRageTurns = if (entersBerserker) 2 else defender.berserkerRageTurns,
                    dragonSoulBoostTurns = if (entersDragonSoul) 2 else defender.dragonSoulBoostTurns
                ),
                outcomeType = CombatOutcomeType.ATTACK_HIT,
                attackerFinalTile = clashTile,
                defenderFinalTile = newDefenderTile,
                defenderHpDamage = defenderHpDamage,
                defenderEntersBerserkerRage = entersBerserker,
                defenderEntersDragonSoulBoost = entersDragonSoul,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        } else {
            // Resistance Mitigated / Attack Failed
            val entersDragonSoul = defender.character.abilityType == CharacterAbilityType.PHOENIX_WINGS
            val (updatedDefenderResources, hpDmg) = defender.resources.takeDamage(5)
            val log = "«${attacker.name}» به «${defender.name}» حمله کرد، اما مقاومت بدنی ${defender.name} ضربه را مهار کرد."

            return CombatResolutionResult(
                attacker = attacker.copy(currentTile = clashTile),
                defender = defender.copy(
                    currentTile = clashTile,
                    resources = updatedDefenderResources,
                    dragonSoulBoostTurns = if (entersDragonSoul) 2 else defender.dragonSoulBoostTurns
                ),
                outcomeType = CombatOutcomeType.RESISTANCE_MITIGATED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderHpDamage = hpDmg,
                defenderEntersDragonSoulBoost = entersDragonSoul,
                description = log,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll
            )
        }
    }
}
