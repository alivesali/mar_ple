package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.engine.DeterministicRandomProvider
import com.example.engine.MatchResolutionEngine
import com.example.model.BoardTheme
import com.example.model.GamePhase
import com.example.model.MusicGenre
import com.example.model.Player
import com.example.model.resource.PlayerResources
import com.example.model.team.TeamManager
import com.example.model.team.TeamMode
import com.example.model.team.TeamVictoryMode
import com.example.ui.GameViewModel
import com.example.ui.screens.GamePlayScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase3BoardVisualsAndAnimationTest {

    @get:Rule
    val composeRule = createComposeRule()

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
    fun `ALL_MEMBERS_FINISH blocks team victory if even one member is permanently eliminated`() {
        val engine = MatchResolutionEngine(winningTile = 100)

        // 2v2: Member 0 reaches 100 while teammate Member 1 was permanently eliminated -> Team 0 MUST NOT win
        val duosManager = TeamManager.createDefaultTeams(4, TeamMode.DUOS_2V2)
        val p0 = Player(id = 0, name = "A1", colorIndex = 0, teamId = "team_duo_0", currentTile = 100)
        val p1Eliminated = Player(
            id = 1,
            name = "A2",
            colorIndex = 1,
            teamId = "team_duo_0",
            currentTile = 45,
            resources = PlayerResources(hp = 0, maxHp = 100)
        )
        val p2 = Player(id = 2, name = "B1", colorIndex = 2, teamId = "team_duo_1", currentTile = 50)
        val p3 = Player(id = 3, name = "B2", colorIndex = 3, teamId = "team_duo_1", currentTile = 60)

        val resDeadFlag = engine.checkMatchStatus(
            players = listOf(p0, p1Eliminated, p2, p3),
            teamManager = duosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0),
            eliminatedOutPlayerIds = setOf(1)
        )
        assertFalse(resDeadFlag.isGameOver)

        // Even if eliminated teammate had reached 100 earlier but sits in eliminatedOutPlayerIds, still blocked
        val resEliminatedId = engine.checkMatchStatus(
            players = listOf(p0, p1Eliminated.copy(resources = PlayerResources(hp = 100), currentTile = 100), p2, p3),
            teamManager = duosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0, 1),
            eliminatedOutPlayerIds = setOf(1)
        )
        assertFalse(resEliminatedId.isGameOver)

        // 3v3: 2 of 3 members reach 100, 1 member permanently eliminated -> blocked for Team 0, rival Team 1 can still win if all 3 finish
        val triosManager = TeamManager.createDefaultTeams(6, TeamMode.TRIOS_3V3)
        val trioPlayers = listOf(
            Player(id = 0, name = "A1", colorIndex = 0, teamId = "team_trio_0", currentTile = 100),
            Player(id = 1, name = "A2", colorIndex = 1, teamId = "team_trio_0", currentTile = 100),
            Player(id = 2, name = "A3", colorIndex = 2, teamId = "team_trio_0", currentTile = 30, resources = PlayerResources(hp = 0)),
            Player(id = 3, name = "B1", colorIndex = 3, teamId = "team_trio_1", currentTile = 100),
            Player(id = 4, name = "B2", colorIndex = 4, teamId = "team_trio_1", currentTile = 100),
            Player(id = 5, name = "B3", colorIndex = 5, teamId = "team_trio_1", currentTile = 100)
        )
        val trioRes = engine.checkMatchStatus(
            players = trioPlayers,
            teamManager = triosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0, 1, 3, 4, 5),
            eliminatedOutPlayerIds = setOf(2)
        )
        assertTrue(trioRes.isGameOver)
        assertEquals("team_trio_1", trioRes.winningTeam?.id)
    }

    @Test
    fun `ALL_MEMBERS_FINISH in ViewModel keeps match open when teammate accepts permanent elimination`() {
        val vm = createTestViewModel(intSequence = listOf(2))
        vm.setPlayerCount(4)
        vm.setTeamMode(TeamMode.DUOS_2V2)
        vm.setTeamVictoryMode(TeamVictoryMode.ALL_MEMBERS_FINISH)
        vm.selectCharacterForSlot(0, "jack")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "blade")
        vm.selectCharacterForSlot(3, "stella")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        // Team 1 player (Blade) eliminates Team 0 player (Luna) and Luna chooses permanent elimination
        vm.setPlayerPositionForTest(2, 35)
        vm.setPlayerPositionForTest(1, 35)
        vm.setPlayerResourcesForTest(1, PlayerResources(hp = 5, maxHp = 100, shield = 0))
        runBlocking {
            vm.triggerCombatInteractionForTest(
                attackerId = 2,
                landedTile = 35,
                forcedAttackRoll = 1,
                forcedDefenseRoll = 99
            )
        }
        assertEquals(1, vm.uiState.value.activeRespawnPromptPlayerId)
        vm.acceptElimination(1)
        assertTrue(1 in vm.uiState.value.eliminatedOutPlayerIds)

        // Now P0 (Jack) reaches tile 100 -> Team 0 cannot win via ALL_MEMBERS_FINISH because teammate Luna is eliminated!
        vm.setPlayerPositionForTest(0, 98)
        vm.setTurnIndexForTest(0)
        vm.rollDice()

        val stateAfterP0 = vm.uiState.value
        assertEquals(GamePhase.PLAYING, stateAfterP0.phase)
        assertTrue(0 in stateAfterP0.finishedPlayerIds)
    }

    @Test
    fun `continued match after victory lets last remaining contender play until finishing`() {
        val vm = createTestViewModel(intSequence = listOf(2, 3, 2, 2))
        vm.setPlayerCount(3)
        vm.selectCharacterForSlot(0, "jack")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "blade")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 98)
        vm.setPlayerPositionForTest(1, 97)
        vm.setPlayerPositionForTest(2, 95)
        vm.setTurnIndexForTest(0)

        // P0 wins first
        vm.rollDice()
        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)
        assertEquals(0, vm.uiState.value.winner?.id)

        // Continue match
        vm.continueMatchAfterVictory()
        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)
        assertTrue(vm.uiState.value.isContinuingAfterVictory)

        // P1 finishes -> P2 is now the LAST remaining contender and MUST still play!
        vm.setTurnIndexForTest(1)
        vm.rollDice()
        val stateWithOneLeft = vm.uiState.value
        assertEquals(GamePhase.PLAYING, stateWithOneLeft.phase)
        assertTrue(stateWithOneLeft.isContinuingAfterVictory)
        assertEquals(2, stateWithOneLeft.currentPlayer?.id)

        // Last player (P2) takes a turn rolling 2 (95 -> 97) and stays in PLAYING
        vm.rollDice()
        val afterMidRoll = vm.uiState.value
        assertEquals(GamePhase.PLAYING, afterMidRoll.phase)
        assertEquals(97, afterMidRoll.players.first { it.id == 2 }.currentTile)
        assertEquals(2, afterMidRoll.currentPlayer?.id)

        // Still one turn away (P2 at 97, needs 3, rolls 2 -> 99); let's manually end or verify manual finalize
        vm.rollDice()
        assertEquals(99, vm.uiState.value.players.first { it.id == 2 }.currentTile)
        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)

        vm.endContinuedMatch()
        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)
        assertEquals(0, vm.uiState.value.recordedWinner?.id)
        assertTrue(vm.uiState.value.hasFinalizedVictory)
    }

    @Test
    fun `stella fortune five honors consecutive sixes streak cap and resets cleanly on turn advance`() {
        // Turn 1: Stella has abilityChargeProgress >= 3; rolls 5 -> Fortune Five triggers bonus turn!
        val vmBonus = createTestViewModel(intSequence = listOf(5, 3))
        vmBonus.setPlayerCount(2)
        vmBonus.selectCharacterForSlot(0, "stella")
        vmBonus.selectCharacterForSlot(1, "jack")
        vmBonus.setMaxTiles(100)
        vmBonus.startGame()
        vmBonus.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())
        vmBonus.setTurnIndexForTest(0)

        vmBonus.rollDice()
        val afterFortuneFive = vmBonus.uiState.value
        assertEquals(0, afterFortuneFive.currentTurnIndex) // Bonus turn kept on Stella
        assertEquals(1, afterFortuneFive.consecutiveSixesCount)
        // Fortune Five consumed the 3+ charge down to 0, then bonus turn's onTurnStart ticked cooldown to 1 (< 3)
        assertEquals(1, afterFortuneFive.players.first { it.id == 0 }.abilityChargeProgress)
        assertEquals(1, afterFortuneFive.players.first { it.id == 0 }.totalSixes)

        // Follow-up roll of 3 clears streak back to 0 and passes turn to Jack
        vmBonus.rollDice()
        assertEquals(1, vmBonus.uiState.value.currentTurnIndex)
        assertEquals(0, vmBonus.uiState.value.consecutiveSixesCount)

        // Streak cap check: 6, 6, 6 reaches cap (3), then rolling 5 MUST NOT grant a 4th bonus turn or consume charge
        val vmCap = createTestViewModel(intSequence = listOf(6, 6, 6, 5))
        vmCap.setPlayerCount(2)
        vmCap.selectCharacterForSlot(0, "stella")
        vmCap.selectCharacterForSlot(1, "jack")
        vmCap.setMaxTiles(100)
        vmCap.startGame()
        vmCap.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())
        vmCap.setTurnIndexForTest(0)

        vmCap.rollDice() // 6 #1 -> streak 1
        assertEquals(1, vmCap.uiState.value.consecutiveSixesCount)
        assertEquals(0, vmCap.uiState.value.currentTurnIndex)

        vmCap.rollDice() // 6 #2 -> streak 2
        assertEquals(2, vmCap.uiState.value.consecutiveSixesCount)
        assertEquals(0, vmCap.uiState.value.currentTurnIndex)

        vmCap.rollDice() // 6 #3 -> streak 3 (cap reached, still gets 3rd roll bonus to roll the 4th time)
        assertEquals(3, vmCap.uiState.value.consecutiveSixesCount)
        assertEquals(0, vmCap.uiState.value.currentTurnIndex)

        val chargeBeforeFourthRoll = vmCap.uiState.value.players.first { it.id == 0 }.abilityChargeProgress
        assertTrue(chargeBeforeFourthRoll >= 3)

        // 4th roll is 5 (Fortune Five): cap reached, so NO bonus turn, charge is preserved, streak resets to 0, turn advances to Jack!
        vmCap.rollDice()
        val afterCapState = vmCap.uiState.value
        assertEquals(1, afterCapState.currentTurnIndex)
        assertEquals(0, afterCapState.consecutiveSixesCount)
        assertFalse(afterCapState.sixRollGlowActive)
        assertEquals(chargeBeforeFourthRoll, afterCapState.players.first { it.id == 0 }.abilityChargeProgress)
        // Tiles moved: 1 + 6 + 6 + 6 + 5 = 24
        assertEquals(24, afterCapState.players.first { it.id == 0 }.currentTile)
    }

    @Test
    fun `3D rounded dice geometry keeps upper face visible and stays strictly within stage bounds`() {
        assertTrue(com.example.ui.components.Phase3DiceMath.OUTER_CORNER_RADIUS >= 0.45f)
        assertTrue(com.example.ui.components.Phase3DiceMath.OUTER_SPHERIZE_AMOUNT >= 0.25f)

        val stages = listOf(
            222f to 258f, // single 74dp x 86dp @ 3x
            168f to 252f  // dual 56dp x 84dp @ 3x
        )

        for ((widthPx, heightPx) in stages) {
            for (face in 1..6) {
                val target = com.example.ui.components.Phase3DiceMath.targetAnglesForFace(face)
                val restingGeo = com.example.ui.components.Phase3DiceMath.computeStageGeometry(
                    widthPx = widthPx,
                    heightPx = heightPx,
                    airLift = 0f
                )
                val visible = com.example.ui.components.Phase3DiceMath.visibleFacesForAngles(
                    rotXDeg = target.first,
                    rotYDeg = target.second,
                    rotZDeg = target.third
                )

                // Primary face must face camera strongly
                val landedFace = visible.first { it.first.faceValue == face }
                assertTrue("Face $face rotNormal.z too small: ${landedFace.second.z}", landedFace.second.z >= 0.80f)

                // A distinct upper face (center.y > 0) must remain clearly visible in top half
                val hasVisibleUpperSurface = visible.any { (otherFace, rotNormal, rotCenter) ->
                    otherFace.faceValue != face &&
                        rotNormal.z > 0.25f &&
                        rotCenter.y > 0.20f
                }
                assertTrue("Face $face missing visible upper surface on ${widthPx}x$heightPx", hasVisibleUpperSurface)

                // Check every sampled frame of a full multi-axis throw never overflows stage bounds
                val startAngles = Triple(target.first, target.second, target.third)
                val endAngles = Triple(target.first + 720f, target.second - 540f, target.third + 360f)
                val margin = restingGeo.outerStrokeWidth * 0.5f

                for (step in 0..40) {
                    val progress = step / 40f
                    val sample = com.example.ui.components.Phase3DiceMath.evaluateDiceTrajectoryFrame(
                        progress = progress,
                        flightFraction = 0.72f,
                        startAngles = startAngles,
                        endAngles = endAngles,
                        targetAngles = target
                    )
                    val geo = com.example.ui.components.Phase3DiceMath.computeStageGeometry(
                        widthPx = widthPx,
                        heightPx = heightPx,
                        airLift = sample.airLift
                    )
                    val facesNow = com.example.ui.components.Phase3DiceMath.visibleFacesForAngles(
                        rotXDeg = sample.rotXDeg,
                        rotYDeg = sample.rotYDeg,
                        rotZDeg = sample.rotZDeg
                    )
                    for ((faceDef, _, _) in facesNow) {
                        val pts = com.example.ui.components.Phase3DiceMath.sampleRoundedFaceOutlineScreenPoints(
                            face = faceDef,
                            halfExtent = com.example.ui.components.Phase3DiceMath.OUTER_HALF_EXTENT,
                            cornerRadius = com.example.ui.components.Phase3DiceMath.OUTER_CORNER_RADIUS,
                            spherizeAmount = com.example.ui.components.Phase3DiceMath.OUTER_SPHERIZE_AMOUNT,
                            normalOffset = 0f,
                            stepsPerCorner = 4,
                            rotXDeg = sample.rotXDeg,
                            rotYDeg = sample.rotYDeg,
                            rotZDeg = sample.rotZDeg,
                            geometry = geo
                        )
                        for (pt in pts) {
                            assertTrue(
                                "Vertex X out of bounds ($pt.x vs 0..$widthPx) at progress=$progress face=$face stage=${widthPx}x$heightPx",
                                pt.x - margin >= 0f && pt.x + margin <= widthPx
                            )
                            assertTrue(
                                "Vertex Y out of bounds ($pt.y vs 0..$heightPx) at progress=$progress face=$face stage=${widthPx}x$heightPx",
                                pt.y - margin >= 0f && pt.y + margin <= heightPx
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `pawn step movement math is monotonic continuous and free of frame jumps`() {
        val totalSteps = 6
        val stepDurationNs = 225_000_000L // ~225ms per step
        val totalDurationNs = stepDurationNs * totalSteps
        var prevContinuousPos = -1f
        var prevCompleted = 0

        for (frameIdx in 0..120) {
            val elapsedNs = (totalDurationNs * frameIdx) / 120L
            val frame = com.example.ui.components.Phase3BoardMovementMath.evaluateStepSequenceFrame(
                elapsedNs = elapsedNs,
                stepDurationNs = stepDurationNs,
                totalSteps = totalSteps,
                isReverse = false
            )
            val continuousPos = if (frame.isFinished) {
                totalSteps.toFloat()
            } else {
                frame.segmentIndex.toFloat() + frame.easedProgress
            }
            assertTrue("Non-monotonic step position at frame=$frameIdx: $prevContinuousPos -> $continuousPos", continuousPos >= prevContinuousPos - 1e-4f)
            assertTrue(frame.completedStepsCount >= prevCompleted)
            assertTrue(frame.hopHeight >= 0f && frame.hopHeight <= 1.2f)
            prevContinuousPos = continuousPos
            prevCompleted = frame.completedStepsCount
        }

        val endFrame = com.example.ui.components.Phase3BoardMovementMath.evaluateStepSequenceFrame(
            elapsedNs = totalDurationNs,
            stepDurationNs = stepDurationNs,
            totalSteps = totalSteps,
            isReverse = false
        )
        assertTrue(endFrame.isFinished)
        assertEquals(totalSteps, endFrame.completedStepsCount)
        assertEquals(1f, endFrame.easedProgress, 0.0001f)
    }

    @Test
    fun `all five board themes and epic victory mythological music genres are complete and selectable`() {
        val vm = createTestViewModel()

        // Verify all 5 Board Themes
        assertEquals(5, BoardTheme.entries.size)
        BoardTheme.entries.forEach { theme ->
            assertTrue(theme.persianName.isNotBlank())
            assertTrue(theme.subtitle.isNotBlank())
            assertTrue(theme.tilePalette.size >= 2)
            vm.setBoardTheme(theme)
            assertEquals(theme, vm.uiState.value.boardTheme)
        }

        // Verify Epic (حماسی), Victory (پیروزی), and Mythological (اسطوره‌ای) music genres
        val selectable = MusicGenre.selectableGenres
        assertEquals(10, selectable.size)
        assertTrue(MusicGenre.EPIC in selectable)
        assertTrue(MusicGenre.VICTORY in selectable)
        assertTrue(MusicGenre.MYTHOLOGICAL in selectable)
        assertTrue(MusicGenre.EPIC.persianName.contains("حماسی"))
        assertTrue(MusicGenre.VICTORY.persianName.contains("پیروزی"))
        assertTrue(MusicGenre.MYTHOLOGICAL.persianName.contains("اسطوره‌ای"))

        for (genre in listOf(MusicGenre.EPIC, MusicGenre.VICTORY, MusicGenre.MYTHOLOGICAL)) {
            vm.previewMusicGenre(genre)
            assertEquals(genre, vm.uiState.value.effectiveMusicGenre)
            vm.selectMusicGenre(genre)
            assertEquals(genre, vm.uiState.value.selectedMusicGenre)
            assertNotEquals(MusicGenre.AUTO_BOARD_THEME, vm.uiState.value.selectedMusicGenre)
        }
    }

    @Test
    fun `gameplay screen hamburger menu expands horizontally and highlights active player turn`() {
        val vm = createTestViewModel()
        vm.setPlayerCount(3)
        vm.selectCharacterForSlot(0, "jack")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "blade")
        vm.startGame()

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyApplicationTheme {
                GamePlayScreen(
                    uiState = vm.uiState.value,
                    onRollDice = {},
                    onActivateAbility = {},
                    onSelectPrecisionOption = {},
                    onStepReached = { _, _ -> },
                    onMovementFinished = {},
                    onSelectBoardTheme = {},
                    onOpenAdvancedSettings = {},
                    onOpenCardManager = {},
                    onFlipRevealSnakeCard = {},
                    onCompleteSnakeCardChallenge = {},
                    onRestartGame = {},
                    onRegenerateBoard = {},
                    onToggleSound = {},
                    onBackToSetup = {}
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(350L)

        composeRule.onNodeWithTag("top_collapsible_menu_trigger_button").assertIsDisplayed()
        composeRule.onNodeWithTag("player_turn_chip_0").assertIsDisplayed()
        composeRule.onNodeWithTag("smart_turn_status_bar").assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithTag("top_collapsible_horizontal_menu").fetchSemanticsNodes().size)

        // Tap hamburger button to expand horizontal tool bar
        composeRule.onNodeWithTag("top_collapsible_menu_trigger_button").performClick()
        composeRule.mainClock.advanceTimeBy(450L)
        composeRule.onNodeWithTag("top_collapsible_horizontal_menu").assertIsDisplayed()

        // Tap close (X) button to collapse horizontal tool bar again
        composeRule.onNodeWithTag("top_collapsible_menu_trigger_button").performClick()
        composeRule.mainClock.advanceTimeBy(450L)
        assertEquals(0, composeRule.onAllNodesWithTag("top_collapsible_horizontal_menu").fetchSemanticsNodes().size)
    }
}
