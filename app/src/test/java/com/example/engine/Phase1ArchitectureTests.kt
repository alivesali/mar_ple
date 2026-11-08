package com.example.engine

import com.example.model.BoardConfiguration
import com.example.model.BoardLink
import com.example.model.CharacterRoster
import com.example.model.CombatOutcomeType
import com.example.model.HeroBlueprintCatalog
import com.example.model.LinkType
import com.example.model.Player
import com.example.model.resource.ActiveStatusEffect
import com.example.model.resource.PlayerResources
import com.example.model.resource.StatusEffectType
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager
import com.example.model.team.TeamMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 1 Comprehensive Architectural and Domain Logic Unit Tests.
 * Tests cover Player Resources, Bounds Clamping, Atomic Spending, Team Management,
 * Hero Blueprint Integrity, Deterministic Combat, Movement Constraints, and Obstacle Resolution.
 */
class Phase1ArchitectureTests {

    // --- 1. Player Resources & Boundary Clamping Tests ---

    @Test
    fun testPlayerCreationAndDefaultResourceBounds() {
        val resources = PlayerResources()
        assertEquals(100, resources.hp)
        assertEquals(100, resources.maxHp)
        assertEquals(100, resources.mana)
        assertEquals(100, resources.maxMana)
        assertEquals(50, resources.shield)
        assertEquals(50, resources.maxShield)
        assertEquals(0, resources.gold)
        assertEquals(0, resources.crystals)
        assertTrue(resources.isAlive)
        assertFalse(resources.isShieldDepleted)
        assertEquals(1.0f, resources.hpPercentage, 0.001f)
    }

    @Test
    fun testResourceBoundaryClampingAndNonNegative() {
        var resources = PlayerResources(hp = 20, shield = 10)

        // Excessive damage (50 damage): 10 absorbed by shield, 40 attempts on HP -> HP clamps at 0, never negative
        val (damaged, hpLoss) = resources.takeDamage(50)
        assertEquals(0, damaged.shield)
        assertEquals(0, damaged.hp)
        assertEquals(20, hpLoss)
        assertFalse(damaged.isAlive)

        // Healing clamped to maxHp (100)
        val healed = damaged.heal(250)
        assertEquals(100, healed.hp)

        // Shield clamped to maxShield (50)
        val shielded = healed.addShield(120)
        assertEquals(50, shielded.shield)

        // Mana clamped to maxMana (100)
        val withMana = shielded.copy(mana = 80).restoreMana(50)
        assertEquals(100, withMana.mana)
    }

    @Test
    fun testAtomicManaSpending() {
        val resources = PlayerResources(mana = 20)

        // Insufficient mana: atomic operation fails and returns null without partial state alteration
        val failedSpend = resources.spendMana(25)
        assertNull("Spending more mana than available must return null (atomic failure)", failedSpend)
        assertEquals(20, resources.mana)

        // Sufficient mana: succeeds
        val successfulSpend = resources.spendMana(15)
        assertNotNull(successfulSpend)
        assertEquals(5, successfulSpend!!.mana)
    }

    @Test
    fun testShieldAbsorptionMechanic() {
        val resources = PlayerResources(hp = 100, shield = 50)

        // Damage absorbed fully by shield: 30 damage
        val (res1, hpDmg1) = resources.takeDamage(30)
        assertEquals(20, res1.shield)
        assertEquals(100, res1.hp)
        assertEquals(0, hpDmg1)

        // Penetrating damage: 40 damage against 20 remaining shield -> 20 shield lost, 20 hp lost
        val (res2, hpDmg2) = res1.takeDamage(40)
        assertEquals(0, res2.shield)
        assertEquals(80, res2.hp)
        assertEquals(20, hpDmg2)
    }

    @Test
    fun testStatusEffectApplicationAndTicking() {
        var resources = PlayerResources()
        val freeze = ActiveStatusEffect(StatusEffectType.FREEZE, turnsRemaining = 2)

        resources = resources.applyStatusEffect(freeze)
        assertTrue(resources.hasStatus(StatusEffectType.FREEZE))
        assertEquals(1, resources.statusEffects.size)

        // First turn tick: 1 turn remaining
        resources = resources.tickStatusEffects()
        assertTrue(resources.hasStatus(StatusEffectType.FREEZE))
        assertEquals(1, resources.statusEffects.first().turnsRemaining)

        // Second turn tick: expired and purged
        resources = resources.tickStatusEffects()
        assertFalse(resources.hasStatus(StatusEffectType.FREEZE))
        assertTrue(resources.statusEffects.isEmpty())
    }

    // --- 2. Team Infrastructure Tests ---

    @Test
    fun testTeamManagerAndSharedTileRules() {
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", androidx.compose.ui.graphics.Color.Blue, listOf(0, 1))
        val teamBeta = GameTeam("team_beta", "تیم کاوه", androidx.compose.ui.graphics.Color.Red, listOf(2, 3))
        val manager = TeamManager(listOf(teamAlpha, teamBeta))

        // Teammates check
        assertTrue("Players 0 and 1 must be teammates", manager.areTeammates(0, 1))
        assertTrue("Players 2 and 3 must be teammates", manager.areTeammates(2, 3))
        assertFalse("Players 0 and 2 must NOT be teammates", manager.areTeammates(0, 2))

        // Shared tile peacefully check
        assertTrue("Teammates can occupy same tile without conflict", manager.canOccupySameTilePeacefully(0, 1))
        assertFalse("Enemies cannot occupy same tile peacefully", manager.canOccupySameTilePeacefully(0, 2))

        // Reassignment
        manager.assignPlayerToTeam(0, "team_beta")
        assertTrue("Player 0 reassigned to team beta should be teammate of 2", manager.areTeammates(0, 2))
        assertFalse("Player 0 should no longer be teammate of 1", manager.areTeammates(0, 1))
    }

    @Test
    fun testDefaultTeamGenerationPresets() {
        val duoManager = TeamManager.createDefaultTeams(4, TeamMode.DUOS_2V2)
        assertEquals(2, duoManager.getAllTeams().size)
        assertTrue(duoManager.areTeammates(0, 1))
        assertTrue(duoManager.areTeammates(2, 3))

        val soloManager = TeamManager.createDefaultTeams(4, TeamMode.SOLO_FREE_FOR_ALL)
        assertEquals(4, soloManager.getAllTeams().size)
        assertFalse(soloManager.areTeammates(0, 1))
    }

    // --- 3. Hero Blueprint Catalog & Ability Integrity Tests ---

    @Test
    fun testHeroBlueprintCatalogIntegrity() {
        val blueprints = HeroBlueprintCatalog.allHeroBlueprints

        // Verify exactly 16 blueprints exist
        assertEquals(16, blueprints.size)

        // Verify all IDs are unique
        val ids = blueprints.map { it.id }
        assertEquals(16, ids.toSet().size)

        // Verify all 16 heroes have valid stat budgets (total score = 36)
        blueprints.forEach { hero ->
            assertEquals(
                "Hero '${hero.englishName}' stat total must equal exactly 36 points for competitive balance",
                36,
                hero.stats.totalScore
            )
            assertTrue("Attack stat must be between 1 and 10", hero.stats.attack in 1..10)
            assertTrue("Defense stat must be between 1 and 10", hero.stats.defense in 1..10)
            assertTrue("Resistance stat must be between 1 and 10", hero.stats.resistance in 1..10)
            assertTrue("Mobility stat must be between 1 and 10", hero.stats.mobility in 1..10)
            assertTrue("Control stat must be between 1 and 10", hero.stats.control in 1..10)
            assertTrue("Utility stat must be between 1 and 10", hero.stats.utility in 1..10)
        }
    }

    // --- 4. Deterministic Combat Resolution Tests ---

    @Test
    fun testDeterministicCombatResolutionWithCriticalParry() {
        // Deterministic roll = 5 (within critical parry threshold)
        val deterministicRng = DeterministicRandomProvider(intSequence = listOf(5))
        val engine = CombatResolutionEngine(randomProvider = deterministicRng)

        val attacker = Player(id = 0, name = "Attacker", colorIndex = 0, currentTile = 15)
        val defender = Player(id = 1, name = "Defender", colorIndex = 1, currentTile = 15, isShieldCharged = true)

        val result = engine.resolveClash(attacker, defender)

        assertEquals(CombatOutcomeType.CRITICAL_PARRY, result.outcomeType)
        assertEquals(13, result.attackerFinalTile) // Knocked back 2 tiles
        assertEquals(15, result.defenderFinalTile) // Stays on tile
        assertFalse("Critical parry preserves defender shield", result.defenderShieldConsumed)
    }

    @Test
    fun testDeterministicCombatResolutionDefenderKnockback() {
        // Deterministic roll = 98 (fails shield defense)
        val deterministicRng = DeterministicRandomProvider(intSequence = listOf(98))
        val engine = CombatResolutionEngine(randomProvider = deterministicRng)

        val attacker = Player(id = 0, name = "Attacker", colorIndex = 0, currentTile = 20)
        val defender = Player(id = 1, name = "Defender", colorIndex = 1, currentTile = 20, isShieldCharged = false)

        val result = engine.resolveClash(attacker, defender)

        assertEquals(CombatOutcomeType.ATTACK_HIT, result.outcomeType)
        assertEquals(20, result.attackerFinalTile)
        assertEquals(17, result.defenderFinalTile) // Knocked back 3 tiles (20 - 3 = 17)
        assertTrue(result.defenderHpDamage >= 0)
    }

    @Test
    fun testCombatFriendlyFireImmunityBetweenTeammates() {
        val engine = CombatResolutionEngine()
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", androidx.compose.ui.graphics.Color.Blue, listOf(0, 1))
        val teamManager = TeamManager(listOf(teamAlpha))

        val player0 = Player(id = 0, name = "Teammate 1", colorIndex = 0, currentTile = 30)
        val player1 = Player(id = 1, name = "Teammate 2", colorIndex = 1, currentTile = 30)

        val result = engine.resolveClash(player0, player1, teamManager)

        assertTrue(result.isTeammatePeacefulEncounter)
        assertEquals(CombatOutcomeType.NEUTRAL_COEXIST, result.outcomeType)
        assertEquals(30, result.attackerFinalTile)
        assertEquals(30, result.defenderFinalTile)
        assertEquals(0, result.attackerHpDamage)
        assertEquals(0, result.defenderHpDamage)
    }

    // --- 5. Movement and Exact 100 Finish Constraints ---

    @Test
    fun testTurnAndMovementEngineExact100FinishConstraint() {
        val engine = TurnAndMovementEngine(maxTiles = 100)

        // Player on tile 97, rolls 3 -> exact match, reaches 100
        val exactWin = engine.calculateMovement(currentTile = 97, diceRoll = 3)
        assertEquals(100, exactWin.targetTile)
        assertFalse(exactWin.isExactRollNeededFailure)

        // Player on tile 97, rolls 5 -> overshoots distance 3, cannot move past 100
        val overshoot = engine.calculateMovement(currentTile = 97, diceRoll = 5)
        assertEquals(97, overshoot.targetTile)
        assertTrue(overshoot.isExactRollNeededFailure)
        assertEquals(3, overshoot.neededRollForFinish)
    }

    @Test
    fun testTurnAndMovementEngineExtraTurnOnSix() {
        val engine = TurnAndMovementEngine()

        assertTrue(engine.shouldGrantExtraTurn(diceRoll = 6, currentConsecutiveSixes = 0))
        assertTrue(engine.shouldGrantExtraTurn(diceRoll = 6, currentConsecutiveSixes = 1))
        assertTrue(engine.shouldGrantExtraTurn(diceRoll = 6, currentConsecutiveSixes = 2))
        assertFalse(engine.shouldGrantExtraTurn(diceRoll = 6, currentConsecutiveSixes = 3)) // Cap at 3
        assertFalse(engine.shouldGrantExtraTurn(diceRoll = 5, currentConsecutiveSixes = 0))
    }

    // --- 6. Board Obstacles & Link Evaluation ---

    @Test
    fun testBoardObstacleEngineLadderClimbAndSnakeBite() {
        val engine = BoardObstacleEngine()
        val board = BoardConfiguration(
            links = listOf(
                BoardLink(startTile = 12, endTile = 35, type = LinkType.LADDER),
                BoardLink(startTile = 42, endTile = 18, type = LinkType.SNAKE)
            ),
            obstacles = emptyList(),
            maxTiles = 100
        )

        val player = Player(id = 0, name = "Tester", colorIndex = 0, currentTile = 1)

        // Ladder landing
        val ladderRes = engine.evaluateLanding(tile = 12, boardConfig = board, player = player)
        assertEquals(LinkType.LADDER, ladderRes.linkType)
        assertEquals(35, ladderRes.resolvedTile)

        // Snake landing without protection
        val snakeUnprotected = player.copy(isShieldCharged = false, resources = PlayerResources(shield = 0))
        val snakeRes = engine.evaluateLanding(tile = 42, boardConfig = board, player = snakeUnprotected)
        assertEquals(LinkType.SNAKE, snakeRes.linkType)
        assertEquals(18, snakeRes.resolvedTile)
        assertTrue(snakeRes.addedStatusEffects.any { it.type == StatusEffectType.POISON })

        // Snake landing WITH active shield protection -> blocks bite!
        val snakeProtected = player.copy(isShieldCharged = true, resources = PlayerResources(shield = 50))
        val blockedRes = engine.evaluateLanding(tile = 42, boardConfig = board, player = snakeProtected)
        assertEquals(LinkType.SNAKE, blockedRes.linkType)
        assertEquals(42, blockedRes.resolvedTile) // Kept at tile 42
        assertTrue(blockedRes.isProtectedByShieldOrPassive)
    }

    // --- 7. Hero Ability Engine Atomic Activation Tests ---

    @Test
    fun testHeroAbilityEngineAtomicActivationAndCooldown() {
        val abilityEngine = HeroAbilityEngine()
        val character = CharacterRoster.getById("hero_speedster")
        val player = Player(
            id = 0,
            name = "Speedster Player",
            colorIndex = 0,
            characterId = character.id,
            abilityChargeProgress = 4, // Fully charged
            resources = PlayerResources(mana = 50)
        )

        // Can cast check
        assertTrue(abilityEngine.canCastAbility(player))

        // Cast ability successfully
        val result = abilityEngine.activateAbility(player)
        assertTrue(result is AbilityActivationResult.Success)
        val updatedPlayer = (result as AbilityActivationResult.Success).updatedPlayer

        assertEquals(25, updatedPlayer.resources.mana) // 50 - 25 = 25
        assertTrue(updatedPlayer.isActiveAbilityArmedForTurn)
        assertEquals(0, updatedPlayer.abilityChargeProgress)

        // Immediate second cast attempt should fail (already armed / charge spent)
        assertFalse(abilityEngine.canCastAbility(updatedPlayer))
        val secondCast = abilityEngine.activateAbility(updatedPlayer)
        assertTrue(secondCast is AbilityActivationResult.Failure)

        // Attempt cast with 0 mana should fail
        val noManaPlayer = player.copy(resources = PlayerResources(mana = 0))
        assertFalse(abilityEngine.canCastAbility(noManaPlayer))
    }

    // --- 8. Match Resolution Engine Tests ---

    @Test
    fun testMatchResolutionEngineVictoryDetection() {
        val matchEngine = MatchResolutionEngine(winningTile = 100)
        val player1 = Player(id = 0, name = "P1", colorIndex = 0, currentTile = 100)
        val player2 = Player(id = 1, name = "P2", colorIndex = 1, currentTile = 65)

        val status = matchEngine.checkMatchStatus(listOf(player1, player2))
        assertTrue(status.isGameOver)
        assertEquals(player1.id, status.winner?.id)
        assertEquals(100, status.rankings.first().currentTile)
    }
}
