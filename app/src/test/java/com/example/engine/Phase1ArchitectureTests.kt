package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.model.AbilityActivationMode
import com.example.model.BoardConfiguration
import com.example.model.BoardLink
import com.example.model.CharacterAbilityType
import com.example.model.CharacterRoster
import com.example.model.CombatOutcomeType
import com.example.model.HeroBlueprintCatalog
import com.example.model.LinkType
import com.example.model.Player
import com.example.model.resource.ActiveStatusEffect
import com.example.model.resource.PlayerResources
import com.example.model.resource.ResourceConfig
import com.example.model.resource.StatusEffectType
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager
import com.example.model.team.TeamMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Phase 1 & 1.1 Comprehensive Architectural, Domain, and Regression Unit Tests.
 * Fully deterministic tests covering:
 * 1. Player Resources, Boundary Clamping, Atomic Spending & Regeneration.
 * 2. Team Management, Validation, Transfers & Teammate Collision Immunity.
 * 3. Hero Blueprint Catalog & Roster Integrity.
 * 4. Deterministic Combat Resolution, Passives, Shields & Multi-Occupant Contests.
 * 5. Movement, Exact 100 Finish Constraints & Safe Turn Rotation.
 * 6. Board Obstacles, Snakes & Ladders.
 * 7. Hero Ability Activation Lifecycle, Mana & Single-Source Usage Tracking.
 * 8. Match Resolution, Team Victories, Eliminations & Proper Rankings.
 * 9. Full Match End-to-End Deterministic Simulation.
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

        // Negative or zero healing is ignored
        val nonHealing = healed.heal(-50)
        assertEquals(100, nonHealing.hp)

        // Shield clamped to maxShield (50)
        val shielded = healed.addShield(120)
        assertEquals(50, shielded.shield)

        // Negative shield addition is ignored
        val nonShielded = shielded.addShield(-30)
        assertEquals(50, nonShielded.shield)

        // Mana clamped to maxMana (100)
        val withMana = shielded.copy(mana = 80).restoreMana(50)
        assertEquals(100, withMana.mana)

        // Negative mana restore is ignored
        val nonRestored = withMana.restoreMana(-40)
        assertEquals(100, nonRestored.mana)
    }

    @Test
    fun testResourceConstructorValidation() {
        // Test invalid negative values in PlayerResources constructor
        try {
            PlayerResources(hp = -1)
            fail("Expected IllegalArgumentException for negative HP")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        try {
            PlayerResources(mana = -10)
            fail("Expected IllegalArgumentException for negative Mana")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        try {
            PlayerResources(shield = -5)
            fail("Expected IllegalArgumentException for negative Shield")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        try {
            PlayerResources(gold = -1)
            fail("Expected IllegalArgumentException for negative Gold")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        try {
            PlayerResources(crystals = -1)
            fail("Expected IllegalArgumentException for negative Crystals")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        // Test ResourceConfig validations
        try {
            ResourceConfig(maxHp = 0)
            fail("Expected IllegalArgumentException for non-positive maxHp")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        try {
            ResourceConfig(manaRegenPerTurn = -5)
            fail("Expected IllegalArgumentException for negative manaRegenPerTurn")
        } catch (e: IllegalArgumentException) {
            // Success
        }

        // Test ActiveStatusEffect validation
        try {
            ActiveStatusEffect(StatusEffectType.FREEZE, turnsRemaining = 0)
            fail("Expected IllegalArgumentException for non-positive turnsRemaining")
        } catch (e: IllegalArgumentException) {
            // Success
        }
    }

    @Test
    fun testAtomicManaSpending() {
        val resources = PlayerResources(mana = 20)

        // Insufficient mana: atomic operation fails and returns null without partial state alteration
        val failedSpend = resources.spendMana(25)
        assertNull("Spending more mana than available must return null (atomic failure)", failedSpend)
        assertEquals(20, resources.mana)

        // Negative mana spend must be rejected
        val negativeSpend = resources.spendMana(-10)
        assertNull("Spending negative mana must return null", negativeSpend)

        // Zero mana spend returns self
        val zeroSpend = resources.spendMana(0)
        assertNotNull(zeroSpend)
        assertEquals(20, zeroSpend!!.mana)

        // Sufficient mana: succeeds
        val successfulSpend = resources.spendMana(15)
        assertNotNull(successfulSpend)
        assertEquals(5, successfulSpend!!.mana)
    }

    @Test
    fun testHarvestableCommoditySpendingAndAdding() {
        val resources = PlayerResources(gold = 100, crystals = 50)

        // Sufficient resources
        val spent = resources.spendHarvestables(goldCost = 40, crystalCost = 20)
        assertNotNull(spent)
        assertEquals(60, spent!!.gold)
        assertEquals(30, spent.crystals)

        // Insufficient gold: atomic rejection
        val failedGold = resources.spendHarvestables(goldCost = 150, crystalCost = 10)
        assertNull(failedGold)

        // Insufficient crystals: atomic rejection
        val failedCrystals = resources.spendHarvestables(goldCost = 10, crystalCost = 80)
        assertNull(failedCrystals)

        // Negative cost rejected
        val negativeCost = resources.spendHarvestables(goldCost = -10, crystalCost = 5)
        assertNull(negativeCost)

        // Adding commodities with safe clamping
        val added = resources.addHarvestables(goldDelta = 50, crystalsDelta = 25)
        assertEquals(150, added.gold)
        assertEquals(75, added.crystals)

        // Negative addition clamped to 0 without unauthorized reduction
        val negativeAdd = resources.addHarvestables(goldDelta = -50, crystalsDelta = -20)
        assertEquals(100, negativeAdd.gold)
        assertEquals(50, negativeAdd.crystals)
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

    @Test
    fun testRegenerateTurnResources() {
        val config = ResourceConfig(manaRegenPerTurn = 15, shieldRegenPerTurn = 10)
        val lowResources = PlayerResources(mana = 80, shield = 35)

        val regened = lowResources.regenerateTurnResources(config)
        assertEquals(95, regened.mana)
        assertEquals(45, regened.shield)

        // Clamped at max
        val fullyRegened = regened.regenerateTurnResources(config)
        assertEquals(100, fullyRegened.mana)
        assertEquals(50, fullyRegened.shield)
    }

    // --- 2. Team Infrastructure & Validation Tests ---

    @Test
    fun testTeamManagerAndSharedTileRules() {
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", Color.Blue, listOf(0, 1))
        val teamBeta = GameTeam("team_beta", "تیم کاوه", Color.Red, listOf(2, 3))
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

        // Verify team member lists and map remain synchronized
        assertEquals(listOf(1), manager.getTeam("team_alpha")?.memberPlayerIds)
        assertEquals(listOf(2, 3, 0), manager.getTeam("team_beta")?.memberPlayerIds)
    }

    @Test
    fun testTeamManagerDuplicateTeamIdRejection() {
        val team1 = GameTeam("team_1", "تیم اول", Color.Blue, listOf(0))
        val team2 = GameTeam("team_1", "تیم تکراری", Color.Red, listOf(1))

        try {
            TeamManager(listOf(team1, team2))
            fail("Expected IllegalArgumentException for duplicate team IDs")
        } catch (e: IllegalArgumentException) {
            // Success
        }
    }

    @Test
    fun testTeamManagerDuplicateMemberRejection() {
        val team1 = GameTeam("team_alpha", "تیم اول", Color.Blue, listOf(0, 1))
        val team2 = GameTeam("team_beta", "تیم دوم", Color.Red, listOf(1, 2)) // Player 1 in both teams!

        try {
            TeamManager(listOf(team1, team2))
            fail("Expected IllegalArgumentException for player assigned to multiple teams concurrently")
        } catch (e: IllegalArgumentException) {
            // Success
        }
    }

    @Test
    fun testTeamManagerInvalidTeamAssignmentRejection() {
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", Color.Blue, listOf(0))
        val manager = TeamManager(listOf(teamAlpha))

        try {
            manager.assignPlayerToTeam(0, "non_existent_team")
            fail("Expected IllegalArgumentException when assigning player to non-existent team")
        } catch (e: IllegalArgumentException) {
            // Success
        }
    }

    @Test
    fun testTeamManagerPlayerWithoutTeam() {
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", Color.Blue, listOf(0))
        val manager = TeamManager(listOf(teamAlpha))

        assertNull("Unassigned player 5 must return null team", manager.getTeamForPlayer(5))
        assertFalse("Unassigned player cannot be teammate with player 0", manager.areTeammates(0, 5))
        assertFalse("Unassigned player cannot be teammate with another unassigned player", manager.areTeammates(5, 6))
    }

    @Test
    fun testDefaultTeamGenerationPresets() {
        // Solo 4 players
        val soloManager = TeamManager.createDefaultTeams(4, TeamMode.SOLO_FREE_FOR_ALL)
        assertEquals(4, soloManager.getAllTeams().size)
        assertFalse(soloManager.areTeammates(0, 1))

        // Duos 4 players
        val duoManager = TeamManager.createDefaultTeams(4, TeamMode.DUOS_2V2)
        assertEquals(2, duoManager.getAllTeams().size)
        assertTrue(duoManager.areTeammates(0, 1))
        assertTrue(duoManager.areTeammates(2, 3))
        assertFalse(duoManager.areTeammates(0, 2))

        // Trios 6 players
        val trioManager = TeamManager.createDefaultTeams(6, TeamMode.TRIOS_3V3)
        assertEquals(2, trioManager.getAllTeams().size)
        assertTrue(trioManager.areTeammates(0, 1))
        assertTrue(trioManager.areTeammates(1, 2))
        assertTrue(trioManager.areTeammates(3, 4))
        assertFalse(trioManager.areTeammates(0, 3))

        // Custom 4 players
        val customManager = TeamManager.createDefaultTeams(4, TeamMode.CUSTOM_TEAMS)
        assertEquals(2, customManager.getAllTeams().size)
        assertTrue(customManager.areTeammates(0, 2))
        assertTrue(customManager.areTeammates(1, 3))

        // Invalid player count
        try {
            TeamManager.createDefaultTeams(0, TeamMode.SOLO_FREE_FOR_ALL)
            fail("Expected IllegalArgumentException for player count < 1")
        } catch (e: IllegalArgumentException) {
            // Success
        }
    }

    // --- 3. Hero Blueprint Catalog & Character Roster Tests ---

    @Test
    fun testHeroBlueprintCatalogIntegrity() {
        val blueprints = HeroBlueprintCatalog.allHeroBlueprints
        assertEquals(16, blueprints.size)

        val ids = blueprints.map { it.id }
        assertEquals(16, ids.toSet().size)

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

    @Test
    fun testCharacterRosterIdLookups() {
        // Valid Axel Speedster lookup
        val axel = CharacterRoster.getById("axel")
        assertEquals("axel", axel.id)
        assertEquals(CharacterAbilityType.SPEEDSTER, axel.abilityType)

        // Invalid lookup returns safe fallback without crash
        val fallback = CharacterRoster.getById("completely_invalid_hero_id_xyz")
        assertNotNull(fallback)
        assertEquals("jack", fallback.id)
    }

    // --- 4. Deterministic Combat Resolution Tests ---

    @Test
    fun testDeterministicCombatResolutionWithCriticalParry() {
        val deterministicRng = DeterministicRandomProvider(intSequence = listOf(50, 5))
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
        val deterministicRng = DeterministicRandomProvider(intSequence = listOf(10, 98))
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
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", Color.Blue, listOf(0, 1))
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

    @Test
    fun testCombatDefensivePassivesAuras() {
        val engine = CombatResolutionEngine()

        // 1. Mirage Decoy
        val attacker = Player(id = 0, name = "Attacker", colorIndex = 0, currentTile = 25)
        val decoyDefender = Player(
            id = 1, name = "Luna", colorIndex = 1, currentTile = 25,
            illusionDecoyTile = 25, illusionTurnsRemaining = 2
        )
        val decoyResult = engine.resolveClash(attacker, decoyDefender)
        assertEquals(CombatOutcomeType.ILLUSION_DIVERTED, decoyResult.outcomeType)
        assertTrue(decoyResult.defenderDecoyConsumed)
        assertEquals(25, decoyResult.defenderFinalTile)

        // 2. Guardian Angel
        val angelDefender = Player(
            id = 2, name = "Stella", colorIndex = 2, currentTile = 25,
            guardianAngelProtected = true
        )
        val angelResult = engine.resolveClash(attacker, angelDefender)
        assertEquals(CombatOutcomeType.ANGEL_PROTECTED, angelResult.outcomeType)
        assertTrue(angelResult.defenderAngelConsumed)

        // 3. Phantom Phase
        val phantomDefender = Player(
            id = 3, name = "Zephyr", colorIndex = 3, currentTile = 25,
            phantomPhaseActive = true
        )
        val phantomResult = engine.resolveClash(attacker, phantomDefender)
        assertEquals(CombatOutcomeType.PHANTOM_EVADED, phantomResult.outcomeType)
        assertTrue(phantomResult.defenderPhantomConsumed)
    }

    @Test
    fun testMultiPlayerContestOnSameTile() {
        val engine = CombatResolutionEngine()
        val attacker = Player(id = 0, name = "Attacker", colorIndex = 0, currentTile = 40)
        val occupants = listOf(
            Player(id = 1, name = "Defender 1", colorIndex = 1, currentTile = 40),
            Player(id = 2, name = "Defender 2", colorIndex = 2, currentTile = 40)
        )

        // First clash resolves against Defender 1
        val res1 = engine.resolveClash(attacker, occupants[0])
        assertNotNull(res1)
        assertEquals(40, res1.attackerFinalTile)

        // Second clash resolves against Defender 2 independently
        val res2 = engine.resolveClash(res1.attacker, occupants[1])
        assertNotNull(res2)
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

    @Test
    fun testGetNextTurnIndexSafetyAndPlayerCountVariants() {
        val engine = TurnAndMovementEngine()

        // 1 player
        assertEquals(0, engine.getNextTurnIndex(0, 1))

        // 2 players standard
        assertEquals(1, engine.getNextTurnIndex(0, 2))
        assertEquals(0, engine.getNextTurnIndex(1, 2))

        // 8 players standard
        assertEquals(7, engine.getNextTurnIndex(6, 8))
        assertEquals(0, engine.getNextTurnIndex(7, 8))

        // Empty activePlayers list: safe fallback without crashing
        assertEquals(1, engine.getNextTurnIndex(0, 4, emptyList()))

        // Filtered / Dead player skipping:
        val p0 = Player(id = 0, name = "P0", colorIndex = 0, resources = PlayerResources(hp = 100))
        val p1Dead = Player(id = 1, name = "P1", colorIndex = 1, resources = PlayerResources(hp = 0))
        val p2 = Player(id = 2, name = "P2", colorIndex = 2, resources = PlayerResources(hp = 80))

        val players = listOf(p0, p1Dead, p2)
        // From P0 (idx 0), next is P1 but P1 is dead -> advances directly to P2 (idx 2)
        assertEquals(2, engine.getNextTurnIndex(0, 3, players))

        // From P2 (idx 2), wraps around to P0 (idx 0)
        assertEquals(0, engine.getNextTurnIndex(2, 3, players))

        // Only 1 player alive
        val p2Dead = p2.copy(resources = PlayerResources(hp = 0))
        val oneAlive = listOf(p0, p1Dead, p2Dead)
        assertEquals(0, engine.getNextTurnIndex(2, 3, oneAlive))

        // All players dead: safe bounded return without crashing
        val allDead = listOf(p0.copy(resources = PlayerResources(hp = 0)), p1Dead, p2Dead)
        val safeResult = engine.getNextTurnIndex(1, 3, allDead)
        assertTrue(safeResult in 0..2)

        // List size smaller than playerCount: NO IndexOutOfBoundsException!
        val shortList = listOf(p0, p2) // Size 2, playerCount 4
        val safeShort = engine.getNextTurnIndex(1, 4, shortList)
        assertTrue(safeShort in 0..1)
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
        assertEquals(42, blockedRes.resolvedTile)
        assertTrue(blockedRes.isProtectedByShieldOrPassive)
    }

    // --- 7. Hero Ability Engine Atomic Activation & Usage Counter Tests ---

    @Test
    fun testHeroAbilityEngineAtomicActivationAndCooldown() {
        val abilityEngine = HeroAbilityEngine()
        val character = CharacterRoster.getById("axel")

        // Assert hero identity and ability before test execution
        assertEquals("axel", character.id)
        assertEquals(CharacterAbilityType.SPEEDSTER, character.abilityType)
        assertEquals(AbilityActivationMode.ACTIVE_COOLDOWN, character.abilityType.activationMode)

        val player = Player(
            id = 0,
            name = "Speedster Player",
            colorIndex = 0,
            characterId = character.id,
            abilityChargeProgress = 4, // Fully charged
            resources = PlayerResources(mana = 50),
            abilityUsesCount = 0
        )

        // Can cast check
        assertTrue(abilityEngine.canCastAbility(player))

        // Cast ability successfully
        val result = abilityEngine.activateAbility(player)
        assertTrue(result is AbilityActivationResult.Success)
        val updatedPlayer = (result as AbilityActivationResult.Success).updatedPlayer

        val expectedManaCost = abilityEngine.getAbilityManaCost(character.abilityType) // 25
        assertEquals(50 - expectedManaCost, updatedPlayer.resources.mana)
        assertTrue(updatedPlayer.isActiveAbilityArmedForTurn)
        assertEquals(0, updatedPlayer.abilityChargeProgress)
        assertEquals("Ability uses counter must be incremented on Player exactly once", 1, updatedPlayer.abilityUsesCount)

        // Immediate second cast attempt should fail (already armed)
        assertFalse(abilityEngine.canCastAbility(updatedPlayer))
        val secondCast = abilityEngine.activateAbility(updatedPlayer)
        assertTrue(secondCast is AbilityActivationResult.Failure)

        // Attempt cast with insufficient mana should fail
        val noManaPlayer = player.copy(resources = PlayerResources(mana = 5))
        assertFalse(abilityEngine.canCastAbility(noManaPlayer))
    }

    @Test
    fun testDeadPlayerCannotCastAbility() {
        val abilityEngine = HeroAbilityEngine()
        val character = CharacterRoster.getById("axel")
        val deadPlayer = Player(
            id = 0,
            name = "Fallen Player",
            colorIndex = 0,
            characterId = character.id,
            abilityChargeProgress = 4,
            resources = PlayerResources(hp = 0, mana = 100) // Dead (hp = 0)
        )

        assertFalse("Dead player must not be eligible to cast ability", abilityEngine.canCastAbility(deadPlayer))
        val result = abilityEngine.activateAbility(deadPlayer)
        assertTrue(result is AbilityActivationResult.Failure)
    }

    @Test
    fun testHeroAbilityOnTurnStartProgression() {
        val abilityEngine = HeroAbilityEngine()
        val character = CharacterRoster.getById("axel")
        val player = Player(
            id = 0,
            name = "Axel",
            colorIndex = 0,
            characterId = character.id,
            abilityChargeProgress = 1,
            isActiveAbilityArmedForTurn = true,
            resources = PlayerResources(mana = 60, shield = 20)
        )

        val turnStarted = abilityEngine.onTurnStart(player)
        assertEquals(2, turnStarted.abilityChargeProgress) // Charge advanced by 1
        assertFalse(turnStarted.isActiveAbilityArmedForTurn) // Disarmed for new turn
        assertTrue(turnStarted.resources.mana > 60) // Mana regenerated
        assertTrue(turnStarted.resources.shield > 20) // Shield regenerated
    }

    // --- 8. Match Resolution Engine Tests ---

    @Test
    fun testMatchResolutionEngineVictoryDetection() {
        val matchEngine = MatchResolutionEngine(winningTile = 100)
        val player1 = Player(id = 0, name = "P1", colorIndex = 0, currentTile = 100, resources = PlayerResources(hp = 100))
        val player2 = Player(id = 1, name = "P2", colorIndex = 1, currentTile = 65, resources = PlayerResources(hp = 100))

        val status = matchEngine.checkMatchStatus(listOf(player1, player2))
        assertTrue(status.isGameOver)
        assertEquals(player1.id, status.winner?.id)
        assertEquals(100, status.rankings.first().currentTile)
    }

    @Test
    fun testDeadPlayerOnTile100NotDeclaredWinner() {
        val matchEngine = MatchResolutionEngine(winningTile = 100)
        // Player 1 on tile 100 is dead (hp = 0)
        val deadPlayer1 = Player(id = 0, name = "Dead P1", colorIndex = 0, currentTile = 100, resources = PlayerResources(hp = 0))
        val alivePlayer2 = Player(id = 1, name = "Alive P2", colorIndex = 1, currentTile = 85, resources = PlayerResources(hp = 100))

        val status = matchEngine.checkMatchStatus(listOf(deadPlayer1, alivePlayer2), isEliminationEnabled = true)
        // Since P1 is dead, P1 is NOT winner!
        assertEquals("Alive player must be declared winner, not dead player", alivePlayer2.id, status.winner?.id)
    }

    @Test
    fun testAllPlayersEliminatedMatchResult() {
        val matchEngine = MatchResolutionEngine(winningTile = 100)
        val dead1 = Player(id = 0, name = "P1", colorIndex = 0, currentTile = 50, resources = PlayerResources(hp = 0))
        val dead2 = Player(id = 1, name = "P2", colorIndex = 1, currentTile = 70, resources = PlayerResources(hp = 0))

        val status = matchEngine.checkMatchStatus(listOf(dead1, dead2), isEliminationEnabled = true)
        assertTrue(status.isGameOver)
        assertNull("No winner when all players are eliminated", status.winner)
    }

    @Test
    fun testTeamVictoryResolution() {
        val matchEngine = MatchResolutionEngine(winningTile = 100)
        val teamAlpha = GameTeam("team_alpha", "تیم آریا", Color.Blue, listOf(0, 1))
        val teamBeta = GameTeam("team_beta", "تیم کاوه", Color.Red, listOf(2, 3))
        val manager = TeamManager(listOf(teamAlpha, teamBeta))

        val p0 = Player(id = 0, name = "P0", colorIndex = 0, currentTile = 100, teamId = "team_alpha", resources = PlayerResources(hp = 100))
        val p1 = Player(id = 1, name = "P1", colorIndex = 1, currentTile = 40, teamId = "team_alpha", resources = PlayerResources(hp = 100))
        val p2 = Player(id = 2, name = "P2", colorIndex = 2, currentTile = 80, teamId = "team_beta", resources = PlayerResources(hp = 100))
        val p3 = Player(id = 3, name = "P3", colorIndex = 3, currentTile = 75, teamId = "team_beta", resources = PlayerResources(hp = 100))

        val status = matchEngine.checkMatchStatus(listOf(p0, p1, p2, p3), teamManager = manager)
        assertTrue(status.isGameOver)
        assertEquals(p0.id, status.winner?.id)
        assertEquals("team_alpha", status.winningTeam?.id)
    }

    // --- 9. Full Match End-to-End Deterministic Simulation ---

    @Test
    fun testFullDeterministicMatchSimulation() {
        val moveEngine = TurnAndMovementEngine(maxTiles = 100)
        val matchEngine = MatchResolutionEngine(winningTile = 100)
        val abilityEngine = HeroAbilityEngine()

        var p1 = Player(
            id = 0,
            name = "Hero P1",
            colorIndex = 0,
            characterId = "axel",
            currentTile = 90,
            abilityChargeProgress = 4,
            resources = PlayerResources(hp = 100, mana = 100)
        )
        var p2 = Player(
            id = 1,
            name = "Hero P2",
            colorIndex = 1,
            characterId = "jack",
            currentTile = 70,
            resources = PlayerResources(hp = 100, mana = 100)
        )

        // Turn 1: P1 casts ability and rolls 6
        assertTrue(abilityEngine.canCastAbility(p1))
        val actResult = abilityEngine.activateAbility(p1)
        assertTrue(actResult is AbilityActivationResult.Success)
        p1 = (actResult as AbilityActivationResult.Success).updatedPlayer

        val move1 = moveEngine.calculateMovement(p1.currentTile, diceRoll = 6)
        assertEquals(96, move1.targetTile)
        p1 = p1.copy(currentTile = move1.targetTile)

        var status = matchEngine.checkMatchStatus(listOf(p1, p2))
        assertFalse(status.isGameOver)

        // Turn 2: P1 rolls exact 4 to reach 100
        val move2 = moveEngine.calculateMovement(p1.currentTile, diceRoll = 4)
        assertEquals(100, move2.targetTile)
        assertFalse(move2.isExactRollNeededFailure)
        p1 = p1.copy(currentTile = move2.targetTile)

        status = matchEngine.checkMatchStatus(listOf(p1, p2))
        assertTrue("P1 reached 100, match must be over", status.isGameOver)
        assertEquals(p1.id, status.winner?.id)
    }
}
