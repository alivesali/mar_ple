package com.example.engine

import com.example.model.CharacterAbilityType
import com.example.model.CombatOutcomeType
import com.example.model.HeroBlueprintCatalog
import com.example.model.HeroClass
import com.example.model.HeroCombatMatrix
import com.example.model.Player
import com.example.model.ShieldThreatCategory
import com.example.model.team.TeamManager
import com.example.model.toPersianDigits

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
    val attackerTotalDamage: Int = 0,
    val defenderTotalDamage: Int = 0,
    val attackerShieldConsumed: Boolean = false,
    val defenderShieldConsumed: Boolean = false,
    val defenderCriticalParry: Boolean = false,
    val defenderAngelConsumed: Boolean = false,
    val defenderPhantomConsumed: Boolean = false,
    val defenderDecoyConsumed: Boolean = false,
    val defenderEntersBerserkerRage: Boolean = false,
    val defenderEntersDragonSoulBoost: Boolean = false,
    val attackRoll: Int = 0,
    val defenseRoll: Int = 0,
    val description: String,
    val bannerMessage: String = description,
    val isTeammatePeacefulEncounter: Boolean = false,
    val isCombatNoOp: Boolean = false
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
 * Unified Combat Engine resolving all tile collisions and hero confrontations:
 * 1. Alive guard (eliminated heroes cannot attack or be attacked).
 * 2. Team friendly-fire immunity (teammates coexist peacefully).
 * 3. Priority 1 defensive auras (Illusion Mirage, Guardian Angel, Phantom Phase).
 * 4. Priority 2/3 shields & critical parries (Critical Parry reflects 10 real damage via takeDamage(10) and preserves shield).
 * 5. Priority 4 heavy anchor immunity, attack vs resistance resolution, knockback clamped to [1..maxTiles],
 *    and peaceful coexistence for non-combatants.
 */
class CombatResolutionEngine(
    private val randomProvider: RandomProvider = DefaultRandomProvider(),
    private val maxTiles: Int = 100
) {
    fun resolveClash(
        attacker: Player,
        defender: Player,
        teamManager: TeamManager? = null,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null,
        baseShieldProbability: Int = 75,
        enforceCombatantClassFilter: Boolean = false
    ): CombatResolutionResult {
        val clashTile = defender.currentTile.coerceIn(1, maxTiles)

        // Step 0: Eliminated players cannot engage in combat
        if (!attacker.resources.isAlive || !defender.resources.isAlive) {
            return CombatResolutionResult(
                attacker = attacker,
                defender = defender,
                outcomeType = CombatOutcomeType.NEUTRAL_COEXIST,
                attackerFinalTile = attacker.currentTile,
                defenderFinalTile = defender.currentTile,
                description = "برخورد بدون درگیری: یکی از قهرمانان از میدان نبرد حذف شده است.",
                isCombatNoOp = true
            )
        }

        // Step 1: Team friendly-fire check — teammates peacefully coexist on the same tile!
        if (teamManager != null && teamManager.areTeammates(attacker.id, defender.id)) {
            return CombatResolutionResult(
                attacker = attacker,
                defender = defender,
                outcomeType = CombatOutcomeType.NEUTRAL_COEXIST,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                description = "دیدار دوستانه هم‌تیمی‌ها! «${attacker.name}» و «${defender.name}» با صلح در خانه ${clashTile.toPersianDigits()} کنار هم ماندند.",
                isTeammatePeacefulEncounter = true,
                isCombatNoOp = true
            )
        }

        // Step 2: Blueprint & matrix lookup
        val attackerBlueprint = HeroBlueprintCatalog.getBlueprintForCharacter(attacker.character)
        val defenderBlueprint = HeroBlueprintCatalog.getBlueprintForCharacter(defender.character)
        val attackerClass = attackerBlueprint.primaryClass
        val defenderClass = defenderBlueprint.primaryClass
        val attackerStats = attackerBlueprint.stats
        val defenderStats = defenderBlueprint.stats
        val compatibility = HeroCombatMatrix.getCompatibilityRule(attackerClass, defenderClass)

        // Single deterministic/RNG defenseRoll then attackRoll
        val defenseRoll = forcedDefenseRoll ?: randomProvider.nextInt(0, 100)
        val attackRoll = forcedAttackRoll ?: randomProvider.nextInt(0, 100)

        // -------------------------------------------------------------
        // PRIORITY 1: Absolute Defensive Auras (Mirage / Angel / Phantom)
        // -------------------------------------------------------------
        if (defender.illusionDecoyTile != null && defender.illusionTurnsRemaining > 0) {
            val updatedDefender = defender.copy(illusionDecoyTile = null, illusionTurnsRemaining = 0)
            val log = "«${attacker.name}» (${attackerBlueprint.persianTitle}) به خانهٔ «${defender.name}» یورش برد، اما ضربه به بدل توهمی برخورد کرد و موقعیت واقعی حفظ شد."
            val banner = "سراب توهم‌زا! 🪄 حملهٔ ${attacker.name} توسط بدل توهمی ${defender.name} جذب شد و خنثی گشت!"
            return CombatResolutionResult(
                attacker = attacker,
                defender = updatedDefender,
                outcomeType = CombatOutcomeType.ILLUSION_DIVERTED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderDecoyConsumed = true,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll,
                description = log,
                bannerMessage = banner
            )
        }

        if (defender.guardianAngelProtected) {
            val updatedDefender = defender.copy(guardianAngelProtected = false)
            val log = "«${attacker.name}» به «${defender.name}» یورش برد، اما بال‌های نورانی فرشتهٔ محافظ مانع شد و حمله را خنثی کرد."
            val banner = "سپر فرشتهٔ نجات! 🌟 بال‌های نورانی فرشته از ${defender.name} در برابر حملهٔ ${attacker.name} محافظت کامل کردند!"
            return CombatResolutionResult(
                attacker = attacker,
                defender = updatedDefender,
                outcomeType = CombatOutcomeType.ANGEL_PROTECTED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderAngelConsumed = true,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll,
                description = log,
                bannerMessage = banner
            )
        }

        if (defender.phantomPhaseActive) {
            val updatedDefender = defender.copy(phantomPhaseActive = false)
            val log = "«${attacker.name}» با «${defender.name}» برخورد کرد، اما ${defender.name} در حالت شبح باد بود و بدون آسیب از کنار او گذشت."
            val banner = "گام شبح باد! 👻🌪️ ${defender.name} شبح‌وار از حملهٔ ${attacker.name} جاخالی داد!"
            return CombatResolutionResult(
                attacker = attacker,
                defender = updatedDefender,
                outcomeType = CombatOutcomeType.PHANTOM_EVADED,
                attackerFinalTile = clashTile,
                defenderFinalTile = clashTile,
                defenderPhantomConsumed = true,
                attackRoll = attackRoll,
                defenseRoll = defenseRoll,
                description = log,
                bannerMessage = banner
            )
        }

        // -------------------------------------------------------------
        // PRIORITY 2/3: Charged Shield Parry & Critical Parry
        // -------------------------------------------------------------
        val defenderHasShield = defender.isShieldCharged && defender.resources.shield > 0
        if (defenderHasShield) {
            val effectiveDefenseChance = if (
                defender.character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD ||
                defenderClass == HeroClass.GUARDIAN
            ) {
                defenderStats.computeShieldDefenseChance(
                    baseConfiguredPercent = baseShieldProbability + compatibility.counterChanceBonusPercent,
                    threatCategory = ShieldThreatCategory.EXECUTIONER_OR_ABILITY
                )
            } else {
                (50 + (defenderStats.defense - attackerStats.attack) * 5 + (defenderStats.resistance - 5) * 3)
                    .coerceIn(20, 90)
            }

            if (defenseRoll < effectiveDefenseChance) {
                val isCriticalParry = defenseRoll < defenderStats.criticalParryPreserveShieldPercent
                if (isCriticalParry) {
                    val attackerNewTile = (clashTile - 2).coerceAtLeast(1)
                    val totalBefore = attacker.resources.shield + attacker.resources.hp
                    val (updatedAttackerResources, attackerHpDmg) = attacker.resources.takeDamage(10)
                    val totalDamageDealt = (totalBefore - (updatedAttackerResources.shield + updatedAttackerResources.hp)).coerceAtLeast(0)
                    val updatedAttacker = attacker.copy(
                        previousTurnTile = clashTile,
                        currentTile = attackerNewTile,
                        resources = updatedAttackerResources
                    )
                    val log = "⚡ دفع بی‌نقص بدون مصرف سپر! «${defender.name}» با ضدحملهٔ سپر نورانی، حملهٔ «${attacker.name}» را مهار کرد، ۱۰ آسیب بازتاب داد و او را ۲ خانه به عقب راند."
                    val banner = "سپر حمله را دفع کرد! 🛡️✨ ${defender.name} در برابر یورش ${attacker.name} استقامت کرد — دفع بی‌نقص بدون مصرف سپر! ✨"
                    return CombatResolutionResult(
                        attacker = updatedAttacker,
                        defender = defender,
                        outcomeType = CombatOutcomeType.CRITICAL_PARRY,
                        attackerFinalTile = attackerNewTile,
                        defenderFinalTile = clashTile,
                        attackerHpDamage = attackerHpDmg,
                        attackerTotalDamage = totalDamageDealt,
                        defenderShieldConsumed = false,
                        defenderCriticalParry = true,
                        attackRoll = attackRoll,
                        defenseRoll = defenseRoll,
                        description = log,
                        bannerMessage = banner
                    )
                } else {
                    val totalBefore = defender.resources.shield + defender.resources.hp
                    val (updatedDefenderResources, defenderHpDmg) = defender.resources.takeDamage(15)
                    val totalDamage = (totalBefore - (updatedDefenderResources.shield + updatedDefenderResources.hp)).coerceAtLeast(0)
                    val attackerNewTile = (clashTile - 1).coerceAtLeast(1)
                    val updatedAttacker = attacker.copy(
                        previousTurnTile = clashTile,
                        currentTile = attackerNewTile
                    )
                    val updatedDefender = defender.copy(
                        resources = updatedDefenderResources,
                        isShieldCharged = false,
                        abilityChargeProgress = 0
                    )
                    val log = "🛡️ دفع کامل با سپر! «${defender.name}» ضربهٔ «${attacker.name}» را با سپر نورانی مهار کرد و «${attacker.name}» را به خانه ${attackerNewTile.toPersianDigits()} عقب راند."
                    val banner = "سپر حمله را دفع کرد! 🛡️✨ ${defender.name} در برابر یورش ${attacker.name} استقامت کرد — سپر مصرف شد (شارژ مجدد: ۳ نوبت)"
                    return CombatResolutionResult(
                        attacker = updatedAttacker,
                        defender = updatedDefender,
                        outcomeType = CombatOutcomeType.SHIELD_PARRY,
                        attackerFinalTile = attackerNewTile,
                        defenderFinalTile = clashTile,
                        defenderHpDamage = defenderHpDmg,
                        defenderTotalDamage = totalDamage,
                        defenderShieldConsumed = true,
                        attackRoll = attackRoll,
                        defenseRoll = defenseRoll,
                        description = log,
                        bannerMessage = banner
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // PRIORITY 4: Offensive Attack Resolution vs Non-Combatant Coexistence
        // -------------------------------------------------------------
        val isPrimaryCombatAttacker = attacker.character.abilityType == CharacterAbilityType.SNAKE_HUNTER ||
            attackerClass == HeroClass.WARRIOR ||
            attackerClass == HeroClass.ASSASSIN

        if (isPrimaryCombatAttacker || !enforceCombatantClassFilter) {
            val isDefenderHeavyAnchor = defender.character.abilityType == CharacterAbilityType.HALF_REVERSE ||
                defender.character.abilityType == CharacterAbilityType.EARTH_TITAN

            if (isDefenderHeavyAnchor) {
                val log = "«${attacker.name}» قصد عقب راندن «${defender.name}» را داشت، اما زره صخره‌ای و لنگر تعادل ${defender.name} مانع جابه‌جایی شد."
                val banner = "لنگر سنگین بی‌اثر ساخت! ⚓ پایدار ماندن ${defender.name} در برابر ضربهٔ ${attacker.name}!"
                return CombatResolutionResult(
                    attacker = attacker,
                    defender = defender,
                    outcomeType = CombatOutcomeType.ANCHOR_IMMUNE,
                    attackerFinalTile = clashTile,
                    defenderFinalTile = clashTile,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll,
                    description = log,
                    bannerMessage = banner
                )
            }

            val successChance = (compatibility.baseSuccessPercent + (attackerStats.attack - defenderStats.resistance) * 4)
                .coerceIn(35, 95)

            if (attackRoll < successChance) {
                val knockbackSteps = if (isPrimaryCombatAttacker) {
                    (clashTile / 2).coerceAtLeast(1)
                } else {
                    3
                }
                val newDefenderTile = (clashTile - knockbackSteps).coerceAtLeast(2).coerceAtMost(maxTiles)

                val entersBerserker = defender.character.abilityType == CharacterAbilityType.EARTH_TITAN
                val entersDragonSoul = defender.character.abilityType == CharacterAbilityType.PHOENIX_WINGS
                val defTotalBefore = defender.resources.shield + defender.resources.hp
                val (updatedDefenderResources, defenderHpDamage) = defender.resources.takeDamage(20)
                val defTotalDmg = (defTotalBefore - (updatedDefenderResources.shield + updatedDefenderResources.hp)).coerceAtLeast(0)

                val updatedAttacker = attacker.copy(currentTile = clashTile)
                val updatedDefender = defender.copy(
                    previousTurnTile = clashTile,
                    currentTile = newDefenderTile,
                    resources = updatedDefenderResources,
                    berserkerRageTurns = if (entersBerserker) 2 else defender.berserkerRageTurns,
                    dragonSoulBoostTurns = if (entersDragonSoul) 2 else defender.dragonSoulBoostTurns
                )

                val log = "«${attacker.name}» با شمشیر شکارچی بر «${defender.name}» پیروز شد و او را ${knockbackSteps.toPersianDigits()} خانه به عقب (خانهٔ ${newDefenderTile.toPersianDigits()}) پرتاب کرد."
                val banner = "حمله موفق بود! بازیکن شکار شد! ⚔️💥 ${attacker.name} مهرهٔ ${defender.name} را ${knockbackSteps.toPersianDigits()} خانه به عقب پرتاب کرد!"

                return CombatResolutionResult(
                    attacker = updatedAttacker,
                    defender = updatedDefender,
                    outcomeType = CombatOutcomeType.ATTACK_HIT,
                    attackerFinalTile = clashTile,
                    defenderFinalTile = newDefenderTile,
                    defenderHpDamage = defenderHpDamage,
                    defenderTotalDamage = defTotalDmg,
                    defenderEntersBerserkerRage = entersBerserker,
                    defenderEntersDragonSoulBoost = entersDragonSoul,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll,
                    description = log,
                    bannerMessage = banner
                )
            } else {
                val entersDragonSoul = defender.character.abilityType == CharacterAbilityType.PHOENIX_WINGS
                val updatedDefender = if (entersDragonSoul) {
                    defender.copy(dragonSoulBoostTurns = 2)
                } else {
                    defender
                }
                val log = "«${attacker.name}» به «${defender.name}» حمله کرد، اما مقاومت بدنی ${defender.name} ضربه را خنثی کرد."
                val banner = "مقاومت فعال شد! 🌿 ${defender.name} در برابر یورش ${attacker.name} مقاومت کرد و آسیبی ندید!"
                return CombatResolutionResult(
                    attacker = attacker,
                    defender = updatedDefender,
                    outcomeType = CombatOutcomeType.RESISTANCE_MITIGATED,
                    attackerFinalTile = clashTile,
                    defenderFinalTile = clashTile,
                    defenderEntersDragonSoulBoost = entersDragonSoul,
                    attackRoll = attackRoll,
                    defenseRoll = defenseRoll,
                    description = log,
                    bannerMessage = banner
                )
            }
        }

        // Case 4B: Peaceful Co-existence / Non-combatant collision
        val log = "«${attacker.name}» و «${defender.name}» هر دو در خانهٔ ${clashTile.toPersianDigits()} هم‌گام شدند."
        val banner = "هم‌نشینی در خانهٔ ${clashTile.toPersianDigits()}! 🤝 ${attacker.name} و ${defender.name} هر دو در یک خانه قرار گرفتند."
        return CombatResolutionResult(
            attacker = attacker,
            defender = defender,
            outcomeType = CombatOutcomeType.NEUTRAL_COEXIST,
            attackerFinalTile = clashTile,
            defenderFinalTile = clashTile,
            attackRoll = attackRoll,
            defenseRoll = defenseRoll,
            description = log,
            bannerMessage = banner
        )
    }
}
