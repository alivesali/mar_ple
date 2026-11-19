package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.engine.CombatResolutionEngine
import com.example.engine.DeterministicRandomProvider
import com.example.engine.MatchResolutionEngine
import com.example.engine.TurnAndMovementEngine
import com.example.model.BannerType
import com.example.model.BoardLink
import com.example.model.BoardObstacle
import com.example.model.CombatOutcomeType
import com.example.model.GamePhase
import com.example.model.LinkType
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.resource.PlayerResources
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager
import com.example.model.team.TeamMode
import com.example.ui.GameViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase2GameplayCoreIntegrationTest {

    private fun createTestViewModel(
        intSequence: List<Int> = emptyList(),
        doubleSequence: List<Double> = emptyList(),
        booleanSequence: List<Boolean> = emptyList(),
        defaultIntFallback: Int = 1
    ): GameViewModel {
        val rng = DeterministicRandomProvider(
            intSequence = intSequence,
            doubleSequence = doubleSequence,
            booleanSequence = booleanSequence
        ).apply {
            this.defaultIntFallback = defaultIntFallback
        }
        return GameViewModel(
            context = ApplicationProvider.getApplicationContext(),
            randomProvider = rng,
            skipAnimationDelaysForTest = true
        )
    }

    @Test
    fun `dynamic maxTiles 50 150 200 supports exact finish and overshoot rejection in ViewModel`() {
        listOf(50, 150, 200).forEach { boardSize ->
            val vm = createTestViewModel(intSequence = listOf(5, 3))
            vm.setPlayerCount(2)
            vm.selectCharacterForSlot(0, "elena") // Guardian Angel, no dice-altering passives
            vm.selectCharacterForSlot(1, "luna")
            vm.setMaxTiles(boardSize)
            vm.startGame()

            assertEquals(boardSize, vm.uiState.value.maxTiles)

            // Overshoot at boardSize - 3 with roll = 5 -> stays put and turn advances to Player 1
            vm.setPlayerPositionForTest(0, boardSize - 3)
            vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())
            vm.rollDice()

            assertEquals(boardSize - 3, vm.uiState.value.players.first { it.id == 0 }.currentTile)
            assertEquals(1, vm.uiState.value.currentTurnIndex)
            assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)

            // Back to player 0 turn, exact roll = 3 -> reaches boardSize and wins!
            vm.setTurnIndexForTest(0)
            vm.rollDice()

            val finalState = vm.uiState.value
            assertEquals(GamePhase.CELEBRATION, finalState.phase)
            assertEquals(0, finalState.winner?.id)
            assertEquals(boardSize, finalState.winner?.currentTile)
            assertFalse(finalState.isMatchDraw)
        }
    }

    @Test
    fun `strict landing chain executes Special Obstacle then Snake or Ladder then Combat then MatchResolution`() {
        // Sequence:
        // rollDice = 4 (tile 10 -> 14)
        // Obstacle at 14: REVERSE pushes back by rolledSteps (4) -> tile 10
        // Ladder at 10 -> climbs to tile 28
        // Combat at 28: Blade attacks Nova (defenseRoll = 90, attackRoll = 10 -> hit!)
        val vm = createTestViewModel(
            intSequence = listOf(
                4,  // Player 0 roll
                90, // Defender Nova defenseRoll (high -> does not block)
                10  // Attacker Blade attackRoll (low -> ATTACK_HIT)
            )
        )
        vm.setPlayerCount(2)
        vm.selectCharacterForSlot(0, "blade") // Executioner attacker
        vm.selectCharacterForSlot(1, "nova")  // Rival sitting on ladder top
        vm.setMaxTiles(100)
        vm.startGame()

        vm.setBoardLayoutForTest(
            links = listOf(BoardLink(startTile = 10, endTile = 28, type = LinkType.LADDER)),
            obstacles = listOf(BoardObstacle(tile = 14, type = ObstacleType.REVERSE))
        )
        vm.setPlayerPositionForTest(0, 10)
        vm.setPlayerPositionForTest(1, 28)

        vm.rollDice()

        val state = vm.uiState.value
        val blade = state.players.first { it.id == 0 }
        val nova = state.players.first { it.id == 1 }

        // Blade moved 10 -> 14 (Reverse -4) -> 10 (Ladder) -> 28!
        assertEquals(28, blade.currentTile)
        assertEquals(1, blade.laddersClimbed)
        // Nova was at 28 and got hunted/knocked back by Blade!
        assertTrue("Nova must be knocked back below 28 after ladder-to-combat chain", nova.currentTile < 28)
        assertEquals(1, state.combatLogs.size)
        assertEquals(CombatOutcomeType.ATTACK_HIT, state.combatLogs.last().outcomeType)
        assertEquals(28, state.combatLogs.last().tile)
    }

    @Test
    fun `unified HeroAbilityEngine enforces atomic mana spend, dead guard, and Raven pre-validation`() {
        val vm = createTestViewModel()
        vm.setPlayerCount(2)
        vm.selectCharacterForSlot(0, "raven") // SHADOW_WALKER
        vm.selectCharacterForSlot(1, "nova")
        vm.startGame()

        // Give Raven an active swap window & 60 mana, while Raven is ALREADY ahead of Nova
        vm.setPlayerPositionForTest(0, 40)
        vm.setPlayerPositionForTest(1, 15)
        vm.simulatePersonalTurnsForPlayerForTest(0, 10)
        vm.setPlayerResourcesForTest(0, PlayerResources(hp = 100, mana = 60, shield = 50))

        val windowBefore = vm.uiState.value.players.first { it.id == 0 }.leaderSwapWindowTurnsRemaining
        assertTrue(windowBefore > 0)

        // Pre-validation must fail without spending mana or consuming the window
        vm.activateCharacterAbility()
        val afterInvalidSwap = vm.uiState.value.players.first { it.id == 0 }
        assertEquals(60, afterInvalidSwap.resources.mana)
        assertEquals(windowBefore, afterInvalidSwap.leaderSwapWindowTurnsRemaining)
        assertEquals(0, afterInvalidSwap.abilityUsesCount)

        // Now place Nova ahead at tile 65 -> swap succeeds and spends mana exactly once (25 mana)
        vm.setPlayerPositionForTest(1, 65)
        vm.activateCharacterAbility()
        val afterValidSwap = vm.uiState.value.players.first { it.id == 0 }
        val novaAfterSwap = vm.uiState.value.players.first { it.id == 1 }
        assertEquals(65, afterValidSwap.currentTile)
        assertEquals(40, novaAfterSwap.currentTile)
        assertEquals(35, afterValidSwap.resources.mana) // 60 - 25 = 35
        assertEquals(1, afterValidSwap.abilityUsesCount)
        assertEquals(0, afterValidSwap.leaderSwapWindowTurnsRemaining)

        // Low mana failure check on Axel
        val vm2 = createTestViewModel()
        vm2.setPlayerCount(2)
        vm2.selectCharacterForSlot(0, "axel") // SPEEDSTER (cooldown 3, cost 25)
        vm2.selectCharacterForSlot(1, "luna")
        vm2.startGame()
        vm2.setPlayerResourcesForTest(0, PlayerResources(hp = 100, mana = 10, shield = 50))
        vm2.activateCharacterAbility()

        val axelAfterFail = vm2.uiState.value.players.first { it.id == 0 }
        assertEquals(10, axelAfterFail.resources.mana)
        assertEquals(0, axelAfterFail.abilityUsesCount)
        assertFalse(axelAfterFail.isActiveAbilityArmedForTurn)
        assertEquals(BannerType.OBSTACLE_DANGER, vm2.uiState.value.eventBanner.type)

        // Dead hero cannot cast
        vm2.setPlayerResourcesForTest(0, PlayerResources(hp = 0, mana = 100, shield = 0))
        vm2.activateCharacterAbility()
        assertEquals(0, vm2.uiState.value.players.first { it.id == 0 }.abilityUsesCount)
    }

    @Test
    fun `Critical Parry reflects 10 real damage, preserves shield, and halts multi-defender loop when attacker dies`() = runBlocking {
        val vm = createTestViewModel()
        vm.setPlayerCount(3)
        vm.selectCharacterForSlot(0, "blade") // Executioner attacker
        vm.selectCharacterForSlot(1, "jack")  // Guardian defender 1
        vm.selectCharacterForSlot(2, "stella") // Defender 2 on same tile
        vm.startGame()

        // Attacker Blade has 0 shield and 10 HP (will die on 10 reflected Critical Parry damage!)
        vm.setPlayerResourcesForTest(0, PlayerResources(hp = 10, shield = 0, mana = 100))
        vm.setPlayerPositionForTest(1, 30)
        vm.setPlayerPositionForTest(2, 30)
        vm.setPlayerPositionForTest(0, 30)

        // defenseRoll = 2 triggers CRITICAL_PARRY (< criticalParryPreserveShieldPercent)
        val combatHappened = vm.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 30,
            forcedAttackRoll = 10,
            forcedDefenseRoll = 2
        )
        assertTrue(combatHappened)

        val blade = vm.uiState.value.players.first { it.id == 0 }
        val jack = vm.uiState.value.players.first { it.id == 1 }

        // Reflected damage 10 killed Blade, pushed Blade back 2 tiles, preserved Jack's shield!
        assertFalse("Blade must be dead after taking 10 Critical Parry reflected damage", blade.resources.isAlive)
        assertEquals(0, blade.resources.hp)
        assertEquals(28, blade.currentTile)
        assertTrue("Critical Parry must preserve defender shield", jack.isShieldCharged)
        assertEquals(50, jack.resources.shield)
        // Loop stopped after Blade was knocked off/killed; only 1 combat log recorded
        assertEquals(1, vm.uiState.value.combatLogs.size)
        assertEquals(CombatOutcomeType.CRITICAL_PARRY, vm.uiState.value.combatLogs.first().outcomeType)
    }

    @Test
    fun `teammates in DUOS_2V2 peacefully coexist without combat damage and win together as a team`() = runBlocking {
        val vm = createTestViewModel(intSequence = listOf(4))
        vm.setPlayerCount(4)
        vm.setTeamMode(TeamMode.DUOS_2V2)
        vm.selectCharacterForSlot(0, "blade") // Team Alpha (0, 1)
        vm.selectCharacterForSlot(1, "luna")  // Team Alpha (0, 1)
        vm.selectCharacterForSlot(2, "jack")  // Team Beta (2, 3)
        vm.selectCharacterForSlot(3, "nova")  // Team Beta (2, 3)
        vm.setMaxTiles(100)
        vm.startGame()

        // Teammate combat immunity check on tile 40
        vm.setPlayerPositionForTest(1, 40)
        vm.setPlayerPositionForTest(0, 40)
        vm.triggerCombatInteractionForTest(attackerId = 0, landedTile = 40, forcedAttackRoll = 1, forcedDefenseRoll = 99)

        val p0 = vm.uiState.value.players.first { it.id == 0 }
        val p1 = vm.uiState.value.players.first { it.id == 1 }
        assertEquals(40, p0.currentTile)
        assertEquals(40, p1.currentTile)
        assertEquals(100, p1.resources.hp)
        assertEquals(CombatOutcomeType.NEUTRAL_COEXIST, vm.uiState.value.combatLogs.last().outcomeType)

        // Now player 0 rolls 4 from tile 96 -> reaches 100 -> Team Alpha wins!
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())
        vm.setPlayerPositionForTest(0, 96)
        vm.setTurnIndexForTest(0)
        vm.rollDice()

        val winState = vm.uiState.value
        assertEquals(GamePhase.CELEBRATION, winState.phase)
        assertFalse(winState.isMatchDraw)
        assertNotNull(winState.winningTeam)
        assertEquals(listOf(0, 1), winState.winningPlayers.map { it.id }.sorted())
        assertTrue(winState.matchWinReason?.contains("تیم") == true)
    }

    @Test
    fun `MatchResolutionEngine supports elimination win, dead winner prevention, and Draw when all eliminated`() {
        val engine = MatchResolutionEngine(winningTile = 100)

        // Dead player on tile 100 CANNOT win; alive survivor wins!
        val deadAtFinish = Player(
            id = 0,
            name = "DeadHero",
            colorIndex = 0,
            currentTile = 100,
            resources = PlayerResources(hp = 0, shield = 0)
        )
        val aliveMid = Player(
            id = 1,
            name = "Survivor",
            colorIndex = 1,
            currentTile = 45,
            resources = PlayerResources(hp = 60, shield = 20)
        )
        val survivorRes = engine.checkMatchStatus(listOf(deadAtFinish, aliveMid), isEliminationEnabled = true)
        assertTrue(survivorRes.isGameOver)
        assertFalse(survivorRes.isDrawOrNoWinner)
        assertEquals(1, survivorRes.winner?.id)

        // All players eliminated -> Draw / No Winner
        val deadTwo = aliveMid.copy(resources = PlayerResources(hp = 0, shield = 0))
        val drawRes = engine.checkMatchStatus(listOf(deadAtFinish, deadTwo), isEliminationEnabled = true)
        assertTrue(drawRes.isGameOver)
        assertTrue(drawRes.isDrawOrNoWinner)
        assertNull(drawRes.winner)
        assertTrue(drawRes.winningPlayers.isEmpty())

        // Team elimination victory
        val teamManager = TeamManager.createDefaultTeams(4, TeamMode.DUOS_2V2)
        val p0Dead = Player(id = 0, name = "A1", colorIndex = 0, currentTile = 20, resources = PlayerResources(hp = 0))
        val p1Dead = Player(id = 1, name = "A2", colorIndex = 1, currentTile = 25, resources = PlayerResources(hp = 0))
        val p2Alive = Player(id = 2, name = "B1", colorIndex = 2, currentTile = 30, resources = PlayerResources(hp = 80))
        val p3Alive = Player(id = 3, name = "B2", colorIndex = 3, currentTile = 15, resources = PlayerResources(hp = 50))
        val teamElimRes = engine.checkMatchStatus(
            players = listOf(p0Dead, p1Dead, p2Alive, p3Alive),
            isEliminationEnabled = true,
            teamManager = teamManager
        )
        assertTrue(teamElimRes.isGameOver)
        assertFalse(teamElimRes.isDrawOrNoWinner)
        assertNotNull(teamElimRes.winningTeam)
        assertEquals(listOf(2, 3), teamElimRes.winningPlayers.map { it.id }.sorted())
    }

    @Test
    fun `three consecutive sixes cap transfers turn on fourth six and skipped players advance safely`() {
        // 4 consecutive rolls of 6 for Player 0 (Stella) on clean board
        val vm = createTestViewModel(intSequence = listOf(6, 6, 6, 6))
        vm.setPlayerCount(3)
        vm.selectCharacterForSlot(0, "stella")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "jack")
        vm.setMaxTiles(100)
        vm.startGame()

        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        // 1st six -> bonus turn (streak = 1)
        vm.rollDice()
        assertEquals(0, vm.uiState.value.currentTurnIndex)
        assertEquals(1, vm.uiState.value.consecutiveSixesCount)

        // 2nd six -> bonus turn (streak = 2)
        vm.rollDice()
        assertEquals(0, vm.uiState.value.currentTurnIndex)
        assertEquals(2, vm.uiState.value.consecutiveSixesCount)

        // 3rd six -> bonus turn (streak = 3)
        vm.rollDice()
        assertEquals(0, vm.uiState.value.currentTurnIndex)
        assertEquals(3, vm.uiState.value.consecutiveSixesCount)

        // 4th six -> hits 3-six cap, resets streak to 0 and rotates to Player 1!
        vm.rollDice()
        assertEquals(1, vm.uiState.value.currentTurnIndex)
        assertEquals(0, vm.uiState.value.consecutiveSixesCount)

        // Now verify TurnAndMovementEngine skips dead and frozen players cleanly without infinite loops
        val turnEngine = TurnAndMovementEngine(maxTiles = 100)
        val p0 = Player(id = 0, name = "P0", colorIndex = 0, resources = PlayerResources(hp = 100))
        val p1Dead = Player(id = 1, name = "P1", colorIndex = 1, resources = PlayerResources(hp = 0))
        val p2Frozen = Player(id = 2, name = "P2", colorIndex = 2, frozenSkipTurnsRemaining = 1, resources = PlayerResources(hp = 100))
        val selection = turnEngine.resolveNextPlayableTurn(currentIndex = 0, players = listOf(p0, p1Dead, p2Frozen))

        assertFalse(selection.noAlivePlayers)
        assertEquals(0, selection.nextIndex)
        assertEquals(1, selection.skippedPlayers.size)
        assertEquals(2, selection.skippedPlayers.first().playerId)
        assertTrue(selection.skippedPlayers.first().isFrozen)
    }
}
