package com.example.engine

import com.example.model.CombatOutcomeType
import com.example.model.HeroBlueprintCatalog
import com.example.model.Player
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
    val description: String,
    val isTeammatePeacefulEncounter: Boolean = false
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
 * Engine handling combat resolutions when players land on contested tiles.
 * Supports team friendly-fire immunity, shield absorption, critical parries,
 * knockbacks clamped to bounds, and loop prevention.
 */
class CombatResolutionEngine(
    private val randomProvider: RandomProvider = DefaultRandomProvider(),
    private val maxTiles: Int = 100
) {
    /**
     * Resolves a combat encounter when [attacker] lands on the tile occupied by [defender].
     *
     * @param teamManager Validates whether the two players are teammates.
     * @return [CombatResolutionResult] with updated positions, damage, and shield statuses.
     */
    fun resolveClash(
        attacker: Player,
        defender: Player,
        teamManager: TeamManager? = null
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

        // 2. Fetch hero stats and blueprint archetypes
        val attackerBlueprint = HeroBlueprintCatalog.getBlueprintById(attacker.character.id)
        val defenderBlueprint = HeroBlueprintCatalog.getBlueprintById(defender.character.id)

        val attackerStats = attackerBlueprint.stats
        val defenderStats = defenderBlueprint.stats

        // Base clash stats
        val attackerOffense = attackerStats.attack
        val defenderDefense = defenderStats.defense
        val defenderResistance = defenderStats.resistance

        // 3. Check Defender Shield
        val defenderHasShield = defender.resources.shield > 0 || defender.isShieldCharged

        // Roll defense (0 to 100)
        val defenseRoll = randomProvider.nextInt(0, 100)
        val baseDefenseChance = (50 + (defenderDefense - attackerOffense) * 5 + (defenderResistance - 5) * 3).coerceIn(20, 90)

        // Critical parry threshold
        val criticalParryChance = defenderStats.criticalParryPreserveShieldPercent

        if (defenderHasShield && defenseRoll < baseDefenseChance) {
            // Successful shield defense or critical parry!
            val isCriticalParry = defenseRoll < criticalParryChance

            if (isCriticalParry) {
                // Critical parry: No shield lost, attacker knocked back 2 tiles!
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
                    description = "⚡ دفع بی‌نقص و ضدحمله! «${defender.name}» حملهٔ «${attacker.name}» را بدون مصرف سپر بازتاب داد و او را ۲ خانه به عقب راند!"
                )
            } else {
                // Standard Shield Parry: Defender absorbs damage with shield, stays on tile; attacker knocked back 1 tile
                val (updatedDefenderResources, _) = defender.resources.takeDamage(15)
                val attackerNewTile = (clashTile - 1).coerceAtLeast(1)

                return CombatResolutionResult(
                    attacker = attacker.copy(currentTile = attackerNewTile),
                    defender = defender.copy(resources = updatedDefenderResources, isShieldCharged = false),
                    outcomeType = CombatOutcomeType.SHIELD_PARRY,
                    attackerFinalTile = attackerNewTile,
                    defenderFinalTile = clashTile,
                    defenderShieldConsumed = true,
                    description = "🛡️ دفاع با سپر! «${defender.name}» ضربهٔ «${attacker.name}» را با سپر مهار کرد و خانه را حفظ نمود."
                )
            }
        }

        // 4. Attacker overcomes defender: Defender is knocked back
        val knockbackDistance = 3
        val defenderNewTile = (clashTile - knockbackDistance).coerceAtLeast(1)
        val (updatedDefenderResources, defenderHpDamage) = defender.resources.takeDamage(20)

        return CombatResolutionResult(
            attacker = attacker.copy(currentTile = clashTile),
            defender = defender.copy(currentTile = defenderNewTile, resources = updatedDefenderResources),
            outcomeType = CombatOutcomeType.ATTACK_HIT,
            attackerFinalTile = clashTile,
            defenderFinalTile = defenderNewTile,
            defenderHpDamage = defenderHpDamage,
            description = "💥 ضربه سهمگین! «${attacker.name}» با موفقیت «${defender.name}» را به خانه $defenderNewTile عقب راند!"
        )
    }
}
