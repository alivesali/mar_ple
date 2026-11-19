package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.engine.BoardObstacleEngine
import com.example.engine.DeterministicRandomProvider
import com.example.engine.DomainBoardFxType
import com.example.engine.DomainMovementMotionType
import com.example.engine.MatchResolutionEngine
import com.example.engine.TurnAndMovementEngine
import com.example.model.BoardObstacle
import com.example.model.CharacterAbilityType
import com.example.model.CharacterGender
import com.example.model.CharacterRoster
import com.example.model.CustomAbilityEffectType
import com.example.model.CustomAbilityTarget
import com.example.model.CustomCharacterSpec
import com.example.model.DifficultyLevel
import com.example.model.GameCharacter
import com.example.model.GamePhase
import com.example.model.HeldItemVisual
import com.example.model.HeroBlueprintCatalog
import com.example.model.HeroVisualCatalog
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.resource.PlayerResources
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager
import com.example.model.team.TeamMode
import com.example.model.team.TeamVictoryMode
import com.example.ui.GameViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase21GameplayRulesAndRegressionsTest {

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
    fun `eliminated player opens respawn prompt without premature match end and respawns cleanly from tile 0`() {
        val vm = createTestViewModel(intSequence = listOf(4, 2))
        vm.setPlayerCount(2)
        vm.selectCharacterForSlot(0, "blade")
        vm.selectCharacterForSlot(1, "luna")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        // Equip non-default skin on Player 1 and set low HP for lethal clash
        vm.equipPawnSkinForSlot(1, "crimson_phoenix")
        vm.setPlayerPositionForTest(0, 25)
        vm.setPlayerPositionForTest(1, 25)
        vm.setPlayerResourcesForTest(
            1,
            PlayerResources(hp = 10, maxHp = 100, shield = 0, mana = 15, gold = 75)
        )

        runBlocking {
            vm.triggerCombatInteractionForTest(
                attackerId = 0,
                landedTile = 25,
                forcedAttackRoll = 1,
                forcedDefenseRoll = 99
            )
        }

        val promptState = vm.uiState.value
        assertEquals(GamePhase.PLAYING, promptState.phase)
        assertNull(promptState.winner)
        assertEquals(1, promptState.activeRespawnPromptPlayerId)
        assertFalse(promptState.players.first { it.id == 1 }.resources.isAlive)

        // While respawn prompt is active, rolling dice is blocked
        val turnBeforeBlockedRoll = promptState.currentTurnIndex
        vm.rollDice()
        assertEquals(turnBeforeBlockedRoll, vm.uiState.value.currentTurnIndex)

        // Player 1 chooses to respawn from tile 0
        vm.chooseRespawnFromZero(1)

        val respawnedState = vm.uiState.value
        assertEquals(GamePhase.PLAYING, respawnedState.phase)
        assertNull(respawnedState.activeRespawnPromptPlayerId)
        val p1 = respawnedState.players.first { it.id == 1 }
        assertTrue(p1.resources.isAlive)
        assertEquals(0, p1.currentTile)
        assertEquals(PlayerResources(), p1.resources)
        assertEquals(0, p1.frozenSkipTurnsRemaining)
        assertEquals(0, p1.swampSkipTurnsRemaining)
        assertEquals(0, p1.wormCurseTurnsRemaining)
        assertTrue(p1.resources.statusEffects.isEmpty())
        assertEquals("luna", p1.characterId)
        assertEquals("crimson_phoenix", p1.customPawnSkinId)

        // Subsequent roll from tile 0 moves directly onto tile 4
        vm.setTurnIndexForTest(1)
        vm.rollDice()
        assertEquals(4, vm.uiState.value.players.first { it.id == 1 }.currentTile)
    }

    @Test
    fun `accepting elimination marks player permanently out and prevents duplicate respawn prompts`() {
        val vm = createTestViewModel(intSequence = listOf(3))
        vm.setPlayerCount(3)
        vm.selectCharacterForSlot(0, "blade")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "jack")
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 18)
        vm.setPlayerPositionForTest(1, 18)
        vm.setPlayerResourcesForTest(1, PlayerResources(hp = 5, maxHp = 100, shield = 0))

        runBlocking {
            vm.triggerCombatInteractionForTest(
                attackerId = 0,
                landedTile = 18,
                forcedAttackRoll = 1,
                forcedDefenseRoll = 99
            )
        }

        assertEquals(1, vm.uiState.value.activeRespawnPromptPlayerId)
        vm.acceptElimination(1)

        val afterAccept = vm.uiState.value
        assertNull(afterAccept.activeRespawnPromptPlayerId)
        assertTrue(1 in afterAccept.eliminatedOutPlayerIds)
        assertTrue(1 in afterAccept.respawnOfferedPlayerIds)
        assertEquals(GamePhase.PLAYING, afterAccept.phase)

        // Another move must NOT re-open prompt for P1
        vm.setTurnIndexForTest(0)
        vm.rollDice()
        assertNull(vm.uiState.value.activeRespawnPromptPlayerId)
        assertNotEquals(1, vm.uiState.value.currentPlayer?.id)
    }

    @Test
    fun `team mode respawn preserves player teamId`() {
        val vm = createTestViewModel()
        vm.setPlayerCount(4)
        vm.setTeamMode(TeamMode.DUOS_2V2)
        vm.selectCharacterForSlot(2, "blade")
        vm.selectCharacterForSlot(1, "luna")
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        val originalTeamId = vm.uiState.value.players.first { it.id == 1 }.teamId
        assertNotNull(originalTeamId)

        vm.setPlayerPositionForTest(2, 30)
        vm.setPlayerPositionForTest(1, 30)
        vm.setPlayerResourcesForTest(1, PlayerResources(hp = 5, maxHp = 100, shield = 0))

        runBlocking {
            vm.triggerCombatInteractionForTest(
                attackerId = 2,
                landedTile = 30,
                forcedAttackRoll = 1,
                forcedDefenseRoll = 99
            )
        }

        assertEquals(1, vm.uiState.value.activeRespawnPromptPlayerId)
        vm.chooseRespawnFromZero(1)

        val p1After = vm.uiState.value.players.first { it.id == 1 }
        assertEquals(originalTeamId, p1After.teamId)
        assertEquals(0, p1After.currentTile)
        assertTrue(p1After.resources.isAlive)
    }

    @Test
    fun `team victory supports both ONE_MEMBER_FINISH and ALL_MEMBERS_FINISH in 2v2 and 3v3`() {
        val engine = MatchResolutionEngine(winningTile = 100)

        // 2v2: ALL_MEMBERS_FINISH requires both active members at tile 100
        val duosManager = TeamManager.createDefaultTeams(4, TeamMode.DUOS_2V2)
        val p0Done = Player(id = 0, name = "A1", colorIndex = 0, teamId = "team_duo_0", currentTile = 100)
        val p1Mid = Player(id = 1, name = "A2", colorIndex = 1, teamId = "team_duo_0", currentTile = 55)
        val p2 = Player(id = 2, name = "B1", colorIndex = 2, teamId = "team_duo_1", currentTile = 40)
        val p3 = Player(id = 3, name = "B2", colorIndex = 3, teamId = "team_duo_1", currentTile = 30)

        val oneMemberRes = engine.checkMatchStatus(
            players = listOf(p0Done, p1Mid, p2, p3),
            teamManager = duosManager,
            teamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH
        )
        assertTrue(oneMemberRes.isGameOver)
        assertEquals("team_duo_0", oneMemberRes.winningTeam?.id)

        val allMembersIncomplete = engine.checkMatchStatus(
            players = listOf(p0Done, p1Mid, p2, p3),
            teamManager = duosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0)
        )
        assertFalse(allMembersIncomplete.isGameOver)

        // TurnAndMovementEngine skips already-finished player 0 so teammate p1 can keep playing
        val turnEngine = TurnAndMovementEngine(maxTiles = 100)
        val turnSel = turnEngine.resolveNextPlayableTurn(
            currentIndex = 3,
            players = listOf(p0Done, p1Mid, p2, p3),
            finishedPlayerIds = setOf(0)
        )
        assertEquals(1, turnSel.nextIndex)

        val allMembersComplete = engine.checkMatchStatus(
            players = listOf(p0Done, p1Mid.copy(currentTile = 100), p2, p3),
            teamManager = duosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0, 1)
        )
        assertTrue(allMembersComplete.isGameOver)
        assertEquals("team_duo_0", allMembersComplete.winningTeam?.id)

        // 3v3: ALL_MEMBERS_FINISH works for 3-member squads
        val triosManager = TeamManager.createDefaultTeams(6, TeamMode.TRIOS_3V3)
        val trioPlayers = listOf(
            Player(id = 0, name = "S1", colorIndex = 0, teamId = "team_trio_0", currentTile = 100),
            Player(id = 1, name = "S2", colorIndex = 1, teamId = "team_trio_0", currentTile = 100),
            Player(id = 2, name = "S3", colorIndex = 2, teamId = "team_trio_0", currentTile = 80),
            Player(id = 3, name = "O1", colorIndex = 3, teamId = "team_trio_1", currentTile = 50),
            Player(id = 4, name = "O2", colorIndex = 4, teamId = "team_trio_1", currentTile = 45),
            Player(id = 5, name = "O3", colorIndex = 5, teamId = "team_trio_1", currentTile = 40)
        )
        val triosIncomplete = engine.checkMatchStatus(
            players = trioPlayers,
            teamManager = triosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0, 1)
        )
        assertFalse(triosIncomplete.isGameOver)

        val triosComplete = engine.checkMatchStatus(
            players = trioPlayers.map { if (it.id == 2) it.copy(currentTile = 100) else it },
            teamManager = triosManager,
            teamVictoryMode = TeamVictoryMode.ALL_MEMBERS_FINISH,
            finishedPlayerIds = setOf(0, 1, 2)
        )
        assertTrue(triosComplete.isGameOver)
        assertEquals("team_trio_0", triosComplete.winningTeam?.id)
    }

    @Test
    fun `post-victory continuation preserves recorded winner excludes winners from turns and tracks remaining ranks`() {
        val vm = createTestViewModel(intSequence = listOf(2, 3, 4))
        vm.setPlayerCount(3)
        vm.selectCharacterForSlot(0, "jack")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "blade")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 98)
        vm.setPlayerPositionForTest(1, 97)
        vm.setPlayerPositionForTest(2, 96)
        vm.setTurnIndexForTest(0)

        // Player 0 reaches tile 100 and wins
        vm.rollDice()
        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)
        assertEquals(0, vm.uiState.value.winner?.id)
        assertTrue(vm.uiState.value.canContinueAfterVictory)

        // Continue match for remaining players
        vm.continueMatchAfterVictory()

        val contState = vm.uiState.value
        assertEquals(GamePhase.PLAYING, contState.phase)
        assertTrue(contState.isContinuingAfterVictory)
        assertEquals(0, contState.recordedWinner?.id)
        assertTrue(0 in contState.finishedPlayerIds)
        assertNotEquals(0, contState.currentPlayer?.id)

        // Player 1 reaches tile 100 -> 1 player remains (P2), who MUST still get a chance to play!
        vm.setTurnIndexForTest(1)
        vm.rollDice()

        val midState = vm.uiState.value
        assertEquals(GamePhase.PLAYING, midState.phase)
        assertTrue(midState.isContinuingAfterVictory)
        assertEquals(2, midState.currentPlayer?.id)
        assertTrue(1 in midState.finishedPlayerIds)

        // Last remaining contender (P2) plays and finishes tile 100 -> now rankings complete
        vm.rollDice()

        val finalState = vm.uiState.value
        assertEquals(GamePhase.CELEBRATION, finalState.phase)
        assertEquals(0, finalState.winner?.id)
        assertEquals(0, finalState.recordedWinner?.id)
        assertTrue(finalState.hasFinalizedVictory)
        assertEquals(listOf(0, 1, 2), finalState.matchRankings.map { it.id })
    }

    @Test
    fun `post-victory manual endContinuedMatch finalizes preserved champion`() {
        val vm = createTestViewModel(intSequence = listOf(4))
        vm.setPlayerCount(4)
        vm.selectCharacterForSlot(0, "jack")
        vm.selectCharacterForSlot(1, "luna")
        vm.selectCharacterForSlot(2, "blade")
        vm.selectCharacterForSlot(3, "stella")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 96)
        vm.setTurnIndexForTest(0)
        vm.rollDice()
        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)

        vm.continueMatchAfterVictory()
        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)
        assertTrue(vm.uiState.value.isContinuingAfterVictory)

        vm.endContinuedMatch()
        val ended = vm.uiState.value
        assertEquals(GamePhase.CELEBRATION, ended.phase)
        assertEquals(0, ended.winner?.id)
        assertTrue(ended.hasFinalizedVictory)
        assertFalse(ended.isContinuingAfterVictory)
    }

    @Test
    fun `CUSTOM_TEAMS assignments persist across player count changes startGame and restartCurrentMatch`() {
        val vm = createTestViewModel()
        vm.setPlayerCount(4)
        vm.setTeamMode(TeamMode.CUSTOM_TEAMS)
        vm.setTeamVictoryMode(TeamVictoryMode.ALL_MEMBERS_FINISH)
        vm.assignSetupSlotToTeam(0, "team_custom_3")
        vm.assignSetupSlotToTeam(1, "team_custom_3")
        vm.assignSetupSlotToTeam(2, "team_custom_4")
        vm.assignSetupSlotToTeam(3, "team_custom_4")

        vm.setPlayerCount(3)
        vm.setPlayerCount(4)
        vm.startGame()

        val started = vm.uiState.value
        assertEquals("team_custom_3", started.players.first { it.id == 0 }.teamId)
        assertEquals("team_custom_3", started.players.first { it.id == 1 }.teamId)
        assertEquals("team_custom_4", started.players.first { it.id == 2 }.teamId)
        assertEquals("team_custom_4", started.players.first { it.id == 3 }.teamId)
        assertTrue(vm.teamManager.areTeammates(0, 1))
        assertFalse(vm.teamManager.areTeammates(0, 2))

        vm.restartCurrentMatch()
        val restarted = vm.uiState.value
        assertEquals("team_custom_3", restarted.players.first { it.id == 0 }.teamId)
        assertEquals("team_custom_4", restarted.players.first { it.id == 2 }.teamId)
    }

    @Test
    fun `unlocked pawn skins and team victory mode persist and custom heroes resolve cleanly while invalid ids throw`() {
        val vm1 = createTestViewModel()
        vm1.unlockPawnSkin("dragon_gold")
        vm1.setTeamMode(TeamMode.DUOS_2V2)
        vm1.setTeamVictoryMode(TeamVictoryMode.ALL_MEMBERS_FINISH)

        // Invalid character selection is safely ignored instead of corrupting roster
        val beforeInvalid = vm1.uiState.value.playerCharacterIds.toList()
        vm1.selectCharacterForSlot(0, "non_existent_hero_xyz")
        assertEquals(beforeInvalid, vm1.uiState.value.playerCharacterIds)

        // New ViewModel restores persisted skins & team preferences
        val vm2 = createTestViewModel()
        assertTrue("dragon_gold" in vm2.uiState.value.unlockedPawnSkinIds)
        assertEquals(TeamMode.DUOS_2V2, vm2.uiState.value.teamMode)
        assertEquals(TeamVictoryMode.ALL_MEMBERS_FINISH, vm2.uiState.value.teamVictoryMode)

        // Custom hero blueprint and visual profile resolution
        val customHero = GameCharacter(
            id = "custom_12345",
            defaultEnglishName = "Rustam",
            persianTitle = "رستم سفارشی",
            gender = CharacterGender.MALE,
            primaryColor = androidx.compose.ui.graphics.Color(0xFFEF4444),
            secondaryColor = androidx.compose.ui.graphics.Color(0xFFF59E0B),
            hairColor = androidx.compose.ui.graphics.Color(0xFF1F2937),
            skinTone = androidx.compose.ui.graphics.Color(0xFFF1C27D),
            abilityType = CharacterAbilityType.CUSTOM_HERO,
            heldItem = HeldItemVisual.SLAYER_BLADE,
            isCustom = true,
            customSpec = CustomCharacterSpec(
                target = CustomAbilityTarget.OVERALL_MOVEMENT,
                effectType = CustomAbilityEffectType.BONUS_STEPS_BOOST,
                percentageOrValue = 25,
                customDescription = "قدرت بالا"
            )
        )
        assertNotNull(HeroBlueprintCatalog.getBlueprintForCharacter(customHero))
        assertNotNull(HeroVisualCatalog.getProfileForCharacter(customHero))

        // Invalid IDs fail fast loudly in catalogs
        var rosterThrew = false
        try {
            CharacterRoster.getById("invalid_id")
        } catch (_: IllegalArgumentException) {
            rosterThrew = true
        }
        assertTrue(rosterThrew)

        var blueprintThrew = false
        try {
            HeroBlueprintCatalog.getBlueprintById("invalid_id")
        } catch (_: IllegalArgumentException) {
            blueprintThrew = true
        }
        assertTrue(blueprintThrew)

        var visualThrew = false
        try {
            HeroVisualCatalog.getProfile("invalid_id")
        } catch (_: IllegalArgumentException) {
            visualThrew = true
        }
        assertTrue(visualThrew)
    }

    @Test
    fun `PlayerResources and TeamManager reject invalid states cleanly`() {
        var negativeHpThrew = false
        try {
            PlayerResources(hp = -1)
        } catch (_: IllegalArgumentException) {
            negativeHpThrew = true
        }
        assertTrue(negativeHpThrew)

        var overMaxHpThrew = false
        try {
            PlayerResources(hp = 120, maxHp = 100)
        } catch (_: IllegalArgumentException) {
            overMaxHpThrew = true
        }
        assertTrue(overMaxHpThrew)

        val (shieldTestRes, hpDmgDealt) = PlayerResources(hp = 100, shield = 30).takeDamage(45)
        assertEquals(0, shieldTestRes.shield)
        assertEquals(85, shieldTestRes.hp)
        assertEquals(15, hpDmgDealt)

        var dupTeamThrew = false
        try {
            TeamManager(
                listOf(
                    GameTeam("dup", "A", androidx.compose.ui.graphics.Color.Red, listOf(0)),
                    GameTeam("dup", "B", androidx.compose.ui.graphics.Color.Blue, listOf(1))
                )
            )
        } catch (_: IllegalArgumentException) {
            dupTeamThrew = true
        }
        assertTrue(dupTeamThrew)

        var dupPlayerThrew = false
        try {
            TeamManager(
                listOf(
                    GameTeam("t1", "A", androidx.compose.ui.graphics.Color.Red, listOf(0)),
                    GameTeam("t2", "B", androidx.compose.ui.graphics.Color.Blue, listOf(0))
                )
            )
        } catch (_: IllegalArgumentException) {
            dupPlayerThrew = true
        }
        assertTrue(dupPlayerThrew)
    }

    @Test
    fun `BoardObstacleEngine returns pure domain FX and motion enums decoupled from UI`() {
        val obstacleEngine = BoardObstacleEngine(DeterministicRandomProvider(booleanSequence = listOf(true)))
        val obstacle = BoardObstacle(tile = 12, type = ObstacleType.WIND)
        val player = Player(id = 0, name = "Tester", colorIndex = 0, characterId = "luna", currentTile = 12)
        val res = obstacleEngine.resolveSpecialObstacle(
            player = player,
            obstacle = obstacle,
            rolledSteps = 4,
            difficulty = DifficultyLevel.MEDIUM,
            maxTiles = 100
        )
        assertEquals(DomainMovementMotionType.WIND_GUST, res.movementType)
        assertEquals(DomainBoardFxType.NONE, res.heroFxType)
        assertEquals(16, res.newTile)
    }
}
