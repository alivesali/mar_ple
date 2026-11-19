package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MysticGameDatabase
import com.example.data.TruthOrDareRepository
import com.example.engine.DeterministicRandomProvider
import com.example.model.BoardTheme
import com.example.model.CardCategory
import com.example.model.DifficultyLevel
import com.example.model.GamePhase
import com.example.model.MatchHistoryRecord
import com.example.model.MatchParticipantRecord
import com.example.model.MatchStatisticsCalculator
import com.example.model.resource.PlayerResources
import com.example.model.team.TeamMode
import com.example.model.team.TeamVictoryMode
import com.example.ui.GameViewModel
import com.example.ui.components.MatchHistoryModal
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase4MatchHistoryAndStatsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var repository: TruthOrDareRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = TruthOrDareRepository(context)
        runBlocking {
            repository.clearMatchHistory()
        }
    }

    private fun createTestViewModel(
        intSequence: List<Int> = emptyList(),
        defaultIntFallback: Int = 1
    ): GameViewModel {
        val rng = DeterministicRandomProvider(
            intSequence = intSequence
        ).apply {
            this.defaultIntFallback = defaultIntFallback
        }
        return GameViewModel(
            context = context,
            randomProvider = rng,
            skipAnimationDelaysForTest = true
        )
    }

    private suspend fun awaitHistoryCount(expectedCount: Int, timeoutMs: Long = 3000L): List<MatchHistoryRecord> {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            val list = repository.getAllMatchHistoryOnce()
            if (list.size == expectedCount) {
                return list
            }
            delay(25L)
        }
        return repository.getAllMatchHistoryOnce()
    }

    @Test
    fun `unfinished match is never recorded and app relaunch discards active match`() = runBlocking {
        val vm = createTestViewModel(intSequence = listOf(4, 3))
        vm.setPlayerCount(2)
        vm.updatePlayerName(0, "آرش")
        vm.updatePlayerName(1, "سارا")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)
        vm.rollDice()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(5, vm.uiState.value.players.first { it.id == 0 }.currentTile)

        // User exits mid-match back to setup
        vm.returnToSetup()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(GamePhase.SETUP, vm.uiState.value.phase)

        // Start another match and simulate app relaunch mid-game by creating a brand new ViewModel
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())
        vm.setPlayerPositionForTest(0, 77)
        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)

        val relaunchedVm = createTestViewModel()
        shadowOf(Looper.getMainLooper()).idle()

        // Relaunch ALWAYS opens at SETUP with NO auto-save or incomplete match history
        assertEquals(GamePhase.SETUP, relaunchedVm.uiState.value.phase)
        assertTrue(relaunchedVm.uiState.value.players.isEmpty())
        delay(100L)
        assertEquals(0, repository.getAllMatchHistoryOnce().size)
    }

    @Test
    fun `completed match is recorded exactly once even if multiple end paths run`() = runBlocking {
        // 2-player solo match: P0 moves from 98 to 100 -> non-continuable victory
        val vm = createTestViewModel(intSequence = listOf(2))
        vm.setPlayerCount(2)
        vm.updatePlayerName(0, "کیان")
        vm.updatePlayerName(1, "نازنین")
        vm.selectCharacterForSlot(0, "jack")
        vm.selectCharacterForSlot(1, "luna")
        vm.setMaxTiles(100)
        vm.setDifficulty(DifficultyLevel.HARD)
        vm.setBoardTheme(BoardTheme.ARCTIC_ICE)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 98)
        vm.setPlayerPositionForTest(1, 64)
        vm.setTurnIndexForTest(0)

        vm.rollDice()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)

        // Trigger multiple end paths repeatedly on the exact same completed match
        vm.finalizeVictoryResult()
        vm.finalizeVictoryResult()
        vm.returnToSetup()
        shadowOf(Looper.getMainLooper()).idle()

        val saved = awaitHistoryCount(1)
        assertEquals(1, saved.size)
        val record = saved.first()
        assertFalse(record.isDraw)
        assertEquals("کیان", record.winnerName)
        assertEquals(100, record.maxTiles)
        assertEquals(DifficultyLevel.HARD, record.difficulty)
        assertEquals(BoardTheme.ARCTIC_ICE, record.boardTheme)
        assertEquals(2, record.participants.size)
        assertEquals("کیان", record.participants[0].playerName)
        assertEquals(1, record.participants[0].rank)
        assertTrue(record.participants[0].isWinner)
        assertEquals("نازنین", record.participants[1].playerName)
        assertEquals(2, record.participants[1].rank)
        assertFalse(record.participants[1].isWinner)
    }

    @Test
    fun `post-victory continuation waits until final end and deduplicates record`() = runBlocking {
        // 3-player match: P0 wins, canContinueAfterVictory == true
        val vm = createTestViewModel(intSequence = listOf(2, 3, 4))
        vm.setPlayerCount(3)
        vm.updatePlayerName(0, "پویا")
        vm.updatePlayerName(1, "رها")
        vm.updatePlayerName(2, "مانی")
        vm.setMaxTiles(100)
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 98)
        vm.setPlayerPositionForTest(1, 97)
        vm.setPlayerPositionForTest(2, 80)
        vm.setTurnIndexForTest(0)

        vm.rollDice()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)
        assertTrue(vm.uiState.value.canContinueAfterVictory)

        // Continue match for remaining rankings -> still not duplicated/saved prematurely
        vm.continueMatchAfterVictory()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)
        delay(80L)
        assertEquals(0, repository.getAllMatchHistoryOnce().size)

        // P1 reaches tile 100
        vm.setTurnIndexForTest(1)
        vm.rollDice()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(GamePhase.PLAYING, vm.uiState.value.phase)

        // User ends continued match manually, then calls finalize & restart
        vm.endContinuedMatch()
        vm.finalizeVictoryResult()
        vm.restartCurrentMatch()
        shadowOf(Looper.getMainLooper()).idle()

        val saved = awaitHistoryCount(1)
        assertEquals(1, saved.size)
        val record = saved.first()
        assertEquals("پویا", record.winnerName)
        assertEquals(listOf("پویا", "رها", "مانی"), record.participants.map { it.playerName })
        assertEquals(listOf(1, 2, 3), record.participants.map { it.rank })
    }

    @Test
    fun `draw match when all players eliminated is saved cleanly without winner`() = runBlocking {
        val vm = createTestViewModel()
        vm.setPlayerCount(2)
        vm.selectCharacterForSlot(0, "blade")
        vm.selectCharacterForSlot(1, "luna")
        vm.updatePlayerName(0, "الف")
        vm.updatePlayerName(1, "ب")
        vm.startGame()
        vm.setBoardLayoutForTest(links = emptyList(), obstacles = emptyList())

        vm.setPlayerPositionForTest(0, 18)
        vm.setPlayerPositionForTest(1, 18)
        vm.setPlayerResourcesForTest(0, PlayerResources(hp = 100, maxHp = 100))
        vm.setPlayerResourcesForTest(1, PlayerResources(hp = 5, maxHp = 100, shield = 0))

        vm.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 18,
            forcedAttackRoll = 1,
            forcedDefenseRoll = 99
        )
        assertEquals(1, vm.uiState.value.activeRespawnPromptPlayerId)

        vm.setPlayerResourcesForTest(0, PlayerResources(hp = 0, maxHp = 100))
        vm.acceptElimination(1)
        assertEquals(0, vm.uiState.value.activeRespawnPromptPlayerId)
        vm.acceptElimination(0)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(GamePhase.CELEBRATION, vm.uiState.value.phase)
        assertTrue(vm.uiState.value.isMatchDraw)

        val saved = awaitHistoryCount(1)
        assertEquals(1, saved.size)
        assertTrue(saved.first().isDraw)
        assertNull(saved.first().winnerName)
        assertTrue(saved.first().participants.all { !it.isWinner && it.isEliminated })
    }

    @Test
    fun `history is ordered newest first and supports single deletion and clear all`() = runBlocking {
        val older = sampleMatch(uuid = "u1", completedAt = 1_000L, winner = "علی")
        val middle = sampleMatch(uuid = "u2", completedAt = 2_000L, winner = "زهرا")
        val newer = sampleMatch(uuid = "u3", completedAt = 3_000L, winner = "امیر")

        assertTrue(repository.saveCompletedMatch(older))
        assertTrue(repository.saveCompletedMatch(newer))
        assertTrue(repository.saveCompletedMatch(middle))
        // Duplicate UUID must be ignored at DB level too
        assertFalse(repository.saveCompletedMatch(middle))

        val ordered = repository.getAllMatchHistoryOnce()
        assertEquals(listOf("u3", "u2", "u1"), ordered.map { it.matchUuid })

        // Delete middle record
        val middleId = ordered.first { it.matchUuid == "u2" }.id
        repository.deleteMatchHistory(middleId)
        val afterSingleDelete = repository.getAllMatchHistoryOnce()
        assertEquals(listOf("u3", "u1"), afterSingleDelete.map { it.matchUuid })

        // Clear all
        repository.clearMatchHistory()
        assertTrue(repository.getAllMatchHistoryOnce().isEmpty())
    }

    @Test
    fun `statistics calculator computes player and team stats accurately`() {
        val m1 = MatchHistoryRecord(
            id = 1L,
            matchUuid = "m1",
            completedAtMillis = 1000L,
            maxTiles = 100,
            difficulty = DifficultyLevel.MEDIUM,
            boardTheme = BoardTheme.BLACK_GOLD,
            teamMode = TeamMode.DUOS_2V2,
            teamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH,
            playerCount = 4,
            isDraw = false,
            winnerPlayerId = 0,
            winnerName = "آریا",
            winningTeamId = "team_duo_0",
            winningTeamName = "تیم عقاب‌ها",
            participants = listOf(
                participant(0, "آریا", rank = 1, isWinner = true, teamId = "team_duo_0", teamName = "تیم عقاب‌ها"),
                participant(1, "دارا", rank = 2, isWinner = true, teamId = "team_duo_0", teamName = "تیم عقاب‌ها"),
                participant(2, "سارا", rank = 3, isWinner = false, teamId = "team_duo_1", teamName = "تیم شیرها"),
                participant(3, "مینا", rank = 4, isWinner = false, teamId = "team_duo_1", teamName = "تیم شیرها")
            )
        )
        val m2 = MatchHistoryRecord(
            id = 2L,
            matchUuid = "m2",
            completedAtMillis = 2000L,
            maxTiles = 100,
            difficulty = DifficultyLevel.MEDIUM,
            boardTheme = BoardTheme.JUNGLE,
            teamMode = TeamMode.DUOS_2V2,
            teamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH,
            playerCount = 4,
            isDraw = false,
            winnerPlayerId = 2,
            winnerName = "سارا",
            winningTeamId = "team_duo_1",
            winningTeamName = "تیم شیرها",
            participants = listOf(
                participant(2, "سارا", rank = 1, isWinner = true, teamId = "team_duo_1", teamName = "تیم شیرها"),
                participant(3, "مینا", rank = 2, isWinner = true, teamId = "team_duo_1", teamName = "تیم شیرها"),
                participant(0, "آریا", rank = 3, isWinner = false, teamId = "team_duo_0", teamName = "تیم عقاب‌ها"),
                participant(1, "دارا", rank = 4, isWinner = false, teamId = "team_duo_0", teamName = "تیم عقاب‌ها")
            )
        )
        val m3 = MatchHistoryRecord(
            id = 3L,
            matchUuid = "m3",
            completedAtMillis = 3000L,
            maxTiles = 120,
            difficulty = DifficultyLevel.HARD,
            boardTheme = BoardTheme.OCEAN_BEACH,
            teamMode = TeamMode.SOLO_FREE_FOR_ALL,
            teamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH,
            playerCount = 2,
            isDraw = true,
            participants = listOf(
                participant(0, "آریا", rank = 1, isWinner = false, isEliminated = true),
                participant(1, "سارا", rank = 2, isWinner = false, isEliminated = true)
            )
        )

        val records = listOf(m3, m2, m1)
        val overview = MatchStatisticsCalculator.computeOverview(records)
        assertEquals(3, overview.totalMatches)
        assertEquals(2, overview.decisiveMatches)
        assertEquals(1, overview.drawMatches)
        assertEquals(2, overview.teamMatches)

        val playerStats = MatchStatisticsCalculator.computePlayerStats(records)
        val arya = playerStats.first { it.playerName == "آریا" }
        assertEquals(3, arya.matchesPlayed)
        assertEquals(1, arya.wins)
        assertEquals(1, arya.draws)
        assertEquals(33, arya.winRatePercent)
        assertEquals(1, arya.bestRank)

        val sara = playerStats.first { it.playerName == "سارا" }
        assertEquals(3, sara.matchesPlayed)
        assertEquals(1, sara.wins)
        assertEquals(1, sara.bestRank)

        val teamStats = MatchStatisticsCalculator.computeTeamStats(records)
        assertEquals(2, teamStats.size)
        val eagles = teamStats.first { it.teamName == "تیم عقاب‌ها" }
        val lions = teamStats.first { it.teamName == "تیم شیرها" }
        assertEquals(2, eagles.matchesPlayed)
        assertEquals(1, eagles.wins)
        assertEquals(50, eagles.winRatePercent)
        assertEquals(1, eagles.bestRank)
        assertEquals(2, lions.matchesPlayed)
        assertEquals(1, lions.wins)
        assertEquals(50, lions.winRatePercent)
        assertEquals(1, lions.bestRank)
    }

    @Test
    fun `room database migration v3 to v4 preserves all existing cards custom characters and profiles`() = runBlocking {
        val dbName = "migration_v3_v4_test_${System.nanoTime()}.db"
        context.deleteDatabase(dbName)

        // Create and populate authentic v3 SQLite database
        val helper = object : SQLiteOpenHelper(context, dbName, null, 3) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `truth_or_dare_cards` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `custom_characters` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `englishName` TEXT NOT NULL,
                        `persianTitle` TEXT NOT NULL,
                        `genderName` TEXT NOT NULL,
                        `primaryColorArgb` INTEGER NOT NULL,
                        `secondaryColorArgb` INTEGER NOT NULL,
                        `hairColorArgb` INTEGER NOT NULL,
                        `skinToneArgb` INTEGER NOT NULL,
                        `heldItemName` TEXT NOT NULL,
                        `targetName` TEXT NOT NULL,
                        `effectTypeName` TEXT NOT NULL,
                        `percentageOrValue` INTEGER NOT NULL,
                        `customDescription` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `settings_profiles` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `profileName` TEXT NOT NULL,
                        `maxTiles` INTEGER NOT NULL,
                        `difficultyName` TEXT NOT NULL,
                        `boardThemeName` TEXT NOT NULL,
                        `aiDifficultyName` TEXT NOT NULL,
                        `playerCount` INTEGER NOT NULL,
                        `enabledObstaclesCsv` TEXT NOT NULL,
                        `enabledCategoriesCsv` TEXT NOT NULL,
                        `environmentEffectName` TEXT NOT NULL,
                        `effectIntensityName` TEXT NOT NULL,
                        `isEnvironmentEffectsEnabled` INTEGER NOT NULL,
                        `musicGenreName` TEXT NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }

        val v3Db = helper.writableDatabase
        v3Db.execSQL(
            """
            INSERT INTO `truth_or_dare_cards` (`title`, `body`, `categoryName`, `createdAtMillis`)
            VALUES ('کارت آزمایشی', 'یک راز خنده‌دار تعریف کن', '${CardCategory.FAMILY.name}', 1000)
            """.trimIndent()
        )
        v3Db.execSQL(
            """
            INSERT INTO `custom_characters` (
                `id`, `englishName`, `persianTitle`, `genderName`,
                `primaryColorArgb`, `secondaryColorArgb`, `hairColorArgb`, `skinToneArgb`,
                `heldItemName`, `targetName`, `effectTypeName`, `percentageOrValue`,
                `customDescription`, `createdAtMillis`
            ) VALUES (
                'custom_hero_preserved', 'Zal', 'زال قهرمان', 'MALE',
                4294901760, 4294950912, 4294967295, 4294034045,
                'ROYAL_SWORD', 'OVERALL_MOVEMENT', 'BONUS_STEPS_BOOST', 25,
                'توضیح سفارشی', 2000
            )
            """.trimIndent()
        )
        v3Db.execSQL(
            """
            INSERT INTO `settings_profiles` (
                `profileName`, `maxTiles`, `difficultyName`, `boardThemeName`,
                `aiDifficultyName`, `playerCount`, `enabledObstaclesCsv`, `enabledCategoriesCsv`,
                `environmentEffectName`, `effectIntensityName`, `isEnvironmentEffectsEnabled`,
                `musicGenreName`, `createdAtMillis`
            ) VALUES (
                'پروفایل طلایی', 120, 'HARD', 'ARCTIC_ICE',
                'HARD', 4, 'STORM,WELL', 'TRUTH,DARE',
                'GOLDEN_DUST', 'HIGH', 1, 'ROYAL', 3000
            )
            """.trimIndent()
        )
        v3Db.close()
        helper.close()

        // Open with Room v4 and MIGRATION_3_4
        val migratedDb = Room.databaseBuilder(context, MysticGameDatabase::class.java, dbName)
            .addMigrations(MysticGameDatabase.MIGRATION_3_4)
            .build()

        try {
            val cards = migratedDb.truthOrDareCardDao().observeAllCards().first()
            assertEquals(1, cards.size)
            assertEquals("کارت آزمایشی", cards.first().title)

            val customChars = migratedDb.customCharacterDao().observeCustomCharacters().first()
            assertEquals(1, customChars.size)
            assertEquals("custom_hero_preserved", customChars.first().id)
            assertEquals("Zal", customChars.first().englishName)

            val profiles = migratedDb.settingsProfileDao().observeSettingsProfiles().first()
            assertEquals(1, profiles.size)
            assertEquals("پروفایل طلایی", profiles.first().profileName)
            assertEquals(120, profiles.first().maxTiles)

            // Verify newly migrated match_history table also writes & reads cleanly
            val inserted = migratedDb.matchHistoryDao().insertMatchHistory(
                com.example.data.MatchHistoryEntity.fromDomainModel(
                    sampleMatch(uuid = "migrated_m1", completedAt = 5000L, winner = "قهرمان مهاجرت")
                )
            )
            assertTrue(inserted > 0L)
            val historyRows = migratedDb.matchHistoryDao().getAllMatchHistoryOnce()
            assertEquals(1, historyRows.size)
            assertEquals("قهرمان مهاجرت", historyRows.first().winnerName)
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun `setup screen exposes match history button and modal supports detail stats and clear confirmation`() {
        val m1 = sampleMatch(uuid = "ui_m1", completedAt = 5000L, winner = "مهسا")
        var uiState by mutableStateOf(
            com.example.ui.GameUiState(
                matchHistory = listOf(m1)
            )
        )
        var historyOpenedCount = 0

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            MyApplicationTheme {
                SetupScreen(
                    uiState = uiState,
                    onSelectPlayerCount = {},
                    onUpdatePlayerName = { _, _ -> },
                    onSelectSetupSlot = {},
                    onSelectCharacterForSlot = { _, _ -> },
                    onResetPlayerName = {},
                    onSelectDifficulty = {},
                    onToggleObstacleType = {},
                    onSelectBoardTheme = {},
                    onSelectMaxTiles = {},
                    onSelectAiDifficulty = {},
                    onOpenAdvancedSettings = {},
                    onOpenCardManager = {},
                    onToggleSound = {},
                    onOpenMatchHistory = {
                        historyOpenedCount += 1
                        uiState = uiState.copy(isMatchHistoryOpen = true)
                    },
                    onStartGame = {}
                )

                if (uiState.isMatchHistoryOpen) {
                    MatchHistoryModal(
                        matchHistory = uiState.matchHistory,
                        selectedMatchDetail = uiState.selectedMatchHistoryDetail,
                        onSelectMatchDetail = { uiState = uiState.copy(selectedMatchHistoryDetail = it) },
                        onDeleteMatchRecord = { id ->
                            uiState = uiState.copy(
                                matchHistory = uiState.matchHistory.filterNot { it.id == id },
                                selectedMatchHistoryDetail = null
                            )
                        },
                        onClearAllHistory = {
                            uiState = uiState.copy(
                                matchHistory = emptyList(),
                                selectedMatchHistoryDetail = null
                            )
                        },
                        onDismiss = { uiState = uiState.copy(isMatchHistoryOpen = false) }
                    )
                }
            }
        }

        composeRule.mainClock.advanceTimeBy(300L)

        // Open Match History from top header button on SetupScreen
        composeRule.onNodeWithTag("setup_match_history_header_button").assertIsDisplayed().performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        assertEquals(1, historyOpenedCount)
        composeRule.onNodeWithTag("match_history_modal").assertIsDisplayed()

        // Switch to Statistics tab and verify winner stats render
        composeRule.onNodeWithTag("match_history_tab_stats").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("match_stats_list").assertIsDisplayed()
        composeRule.onNodeWithText("مهسا").assertIsDisplayed()

        // Switch back to list, tap match card to open detail view
        composeRule.onNodeWithTag("match_history_tab_list").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("match_history_item_${m1.id}").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("match_history_detail_view").assertIsDisplayed()

        // Navigate back from detail view
        composeRule.onNodeWithTag("match_history_detail_back_button").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("match_history_list").assertIsDisplayed()

        // Clear all with explicit confirmation
        composeRule.onNodeWithTag("clear_all_match_history_button").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("confirm_clear_match_history_button").assertIsDisplayed().performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("match_history_empty_state").assertIsDisplayed()
        assertTrue(uiState.matchHistory.isEmpty())
    }

    private fun participant(
        id: Int,
        name: String,
        rank: Int,
        isWinner: Boolean,
        isEliminated: Boolean = false,
        teamId: String? = null,
        teamName: String? = null
    ) = MatchParticipantRecord(
        playerId = id,
        playerName = name,
        characterId = "jack",
        characterName = "Jack",
        characterTitle = "نگهبان سپردار",
        characterBadgeEmoji = "🛡️",
        pawnSkinId = "classic_standard",
        teamId = teamId,
        teamName = teamName,
        rank = rank,
        finalTile = if (isWinner) 100 else 60,
        isWinner = isWinner,
        isEliminated = isEliminated
    )

    private fun sampleMatch(
        uuid: String,
        completedAt: Long,
        winner: String
    ) = MatchHistoryRecord(
        id = completedAt,
        matchUuid = uuid,
        completedAtMillis = completedAt,
        maxTiles = 100,
        difficulty = DifficultyLevel.MEDIUM,
        boardTheme = BoardTheme.BLACK_GOLD,
        teamMode = TeamMode.SOLO_FREE_FOR_ALL,
        teamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH,
        playerCount = 2,
        isDraw = false,
        winnerPlayerId = 0,
        winnerName = winner,
        winnerCharacterTitle = "نگهبان سپردار",
        winReason = "رسیدن به خانه ۱۰۰",
        participants = listOf(
            participant(0, winner, rank = 1, isWinner = true),
            participant(1, "حریف", rank = 2, isWinner = false)
        )
    )
}
