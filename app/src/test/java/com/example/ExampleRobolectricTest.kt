package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AnimationQuality
import com.example.model.AnimationSpeed
import com.example.model.BoardTheme
import com.example.model.CharacterRoster
import com.example.model.EffectIntensity
import com.example.model.EnvironmentEffectType
import com.example.model.HeroBlueprintCatalog
import com.example.model.HeroClass
import com.example.model.MusicGenre
import com.example.ui.GameViewModel
import com.example.ui.theme.PlayerTokenColors
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
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertTrue(appName.isNotBlank())
    }

    @Test
    fun `player token colors and active turn synchronize with real game state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)
        viewModel.setPlayerCount(3)
        viewModel.updatePlayerName(0, "Luna")
        viewModel.updatePlayerName(1, "Jack")
        viewModel.updatePlayerName(2, "Nova")
        viewModel.startGame()

        val state = viewModel.uiState.value
        assertEquals(3, state.players.size)
        assertEquals(0, state.currentTurnIndex)

        val activePlayer = state.currentPlayer
        assertNotNull(activePlayer)
        assertEquals("Luna", activePlayer?.name)
        assertEquals(PlayerTokenColors[0], activePlayer?.color)
        assertEquals(PlayerTokenColors[1], state.players[1].color)
        assertEquals(PlayerTokenColors[2], state.players[2].color)
    }

    @Test
    fun `environment effects studio and music genres support preview selection intensity and reset`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)

        // Verify all 12 environment effects and 9 selectable music genres exist
        assertEquals(12, EnvironmentEffectType.entries.size)
        assertEquals(9, MusicGenre.selectableGenres.size)

        // Preview an environment effect without committing selection
        viewModel.previewEnvironmentEffect(EnvironmentEffectType.GALAXY)
        assertEquals(EnvironmentEffectType.GALAXY, viewModel.uiState.value.previewEnvironmentEffect)
        assertEquals(EnvironmentEffectType.GALAXY, viewModel.uiState.value.effectiveEnvironmentEffect)

        // Select an environment effect and intensity
        viewModel.selectEnvironmentEffect(EnvironmentEffectType.AURORA)
        viewModel.setEnvironmentEffectIntensity(EffectIntensity.HIGH)
        assertNull(viewModel.uiState.value.previewEnvironmentEffect)
        assertEquals(EnvironmentEffectType.AURORA, viewModel.uiState.value.selectedEnvironmentEffect)
        assertEquals(EffectIntensity.HIGH, viewModel.uiState.value.environmentEffectIntensity)
        assertTrue(viewModel.uiState.value.isEnvironmentEffectsEnabled)

        // Select a music genre and adjust SFX volume
        viewModel.selectMusicGenre(MusicGenre.DARK_EPIC)
        viewModel.setSfxVolume(0.72f)
        assertEquals(MusicGenre.DARK_EPIC, viewModel.uiState.value.selectedMusicGenre)
        assertEquals(0.72f, viewModel.uiState.value.sfxVolume, 0.001f)

        // Toggle environment effects off
        viewModel.toggleEnvironmentEffects()
        assertFalse(viewModel.uiState.value.isEnvironmentEffectsEnabled)

        // Reset studio to defaults (Golden Dust + Medium + Royal Music)
        viewModel.resetStudioToDefaults()
        val resetState = viewModel.uiState.value
        assertTrue(resetState.isEnvironmentEffectsEnabled)
        assertEquals(EnvironmentEffectType.GOLDEN_DUST, resetState.selectedEnvironmentEffect)
        assertEquals(EffectIntensity.MEDIUM, resetState.environmentEffectIntensity)
        assertEquals(MusicGenre.ROYAL, resetState.selectedMusicGenre)
    }

    @Test
    fun `five game board themes and granular environment controls work seamlessly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)

        // Verify exact 5 themes order and multi-color tile palettes
        val themes = BoardTheme.entries
        assertEquals(5, themes.size)
        assertEquals("THEME 01", themes[0].themeCode)
        assertEquals("BLACK & GOLD", themes[0].englishName)
        assertEquals("THEME 02", themes[1].themeCode)
        assertEquals("MYSTIC FOREST", themes[1].englishName)
        assertEquals(4, themes[1].tilePalette.size)
        assertEquals("THEME 03", themes[2].themeCode)
        assertEquals("OCEAN & BEACH", themes[2].englishName)
        assertEquals(5, themes[2].tilePalette.size)
        assertEquals("THEME 04", themes[3].themeCode)
        assertEquals("DESERT & DUNES", themes[3].englishName)
        assertEquals(5, themes[3].tilePalette.size)
        assertEquals("THEME 05", themes[4].themeCode)
        assertEquals("ARCTIC & ICE", themes[4].englishName)
        assertEquals(5, themes[4].tilePalette.size)

        // Preview Arctic Ice theme
        viewModel.previewBoardTheme(BoardTheme.ARCTIC_ICE)
        assertEquals(BoardTheme.ARCTIC_ICE, viewModel.uiState.value.previewBoardTheme)
        assertEquals(BoardTheme.ARCTIC_ICE, viewModel.uiState.value.effectiveBoardTheme)

        // Apply Mystic Forest theme
        viewModel.setBoardTheme(BoardTheme.JUNGLE)
        assertNull(viewModel.uiState.value.previewBoardTheme)
        assertEquals(BoardTheme.JUNGLE, viewModel.uiState.value.boardTheme)
        assertEquals(BoardTheme.JUNGLE, viewModel.uiState.value.effectiveBoardTheme)
        assertEquals(BoardTheme.JUNGLE.defaultEnvironmentEffect, viewModel.uiState.value.selectedEnvironmentEffect)

        // Test granular environment controls (e.g., turning off Weather Effects & Ambient Animation)
        viewModel.toggleWeatherEffects()
        assertFalse(viewModel.uiState.value.isWeatherEffectsEnabled)
        viewModel.toggleAmbientAnimation()
        assertFalse(viewModel.uiState.value.isAmbientAnimationEnabled)
        viewModel.setBackgroundLightingLevel(EffectIntensity.HIGH)
        assertEquals(EffectIntensity.HIGH, viewModel.uiState.value.backgroundLightingLevel)
    }

    @Test
    fun `animation quality speed camera follow and 3D dice physics trigger work properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)

        // Verify all 3 AnimationQuality modes and 3 AnimationSpeed modes exist
        assertEquals(3, AnimationQuality.entries.size)
        assertEquals(3, AnimationSpeed.entries.size)

        // Default values
        assertEquals(AnimationQuality.CINEMATIC, viewModel.uiState.value.animationQuality)
        assertEquals(AnimationSpeed.SMOOTH, viewModel.uiState.value.animationSpeed)
        assertTrue(viewModel.uiState.value.isCameraFollowEnabled)

        // Change animation quality, speed, and camera follow
        viewModel.setAnimationQuality(AnimationQuality.PERFORMANCE)
        viewModel.setAnimationSpeed(AnimationSpeed.NORMAL)
        viewModel.toggleCameraFollow()

        assertEquals(AnimationQuality.PERFORMANCE, viewModel.uiState.value.animationQuality)
        assertEquals(AnimationSpeed.NORMAL, viewModel.uiState.value.animationSpeed)
        assertFalse(viewModel.uiState.value.isCameraFollowEnabled)

        // Start game and verify dice roll pre-computes true random value (1..6) and increments trigger ID
        viewModel.startGame()
        val beforeTriggerId = viewModel.uiState.value.diceRollTriggerId
        viewModel.rollDice()

        val rollingState = viewModel.uiState.value
        assertEquals(beforeTriggerId + 1, rollingState.diceRollTriggerId)
        assertTrue(rollingState.diceValue in 1..6)
    }

    @Test
    fun `hero system architecture defines 8 classes and 16 balanced hero blueprints`() {
        assertEquals(8, HeroClass.entries.size)
        assertEquals(16, HeroBlueprintCatalog.allHeroBlueprints.size)

        CharacterRoster.allCharacters.forEach { character ->
            val blueprint = character.heroBlueprint
            assertEquals(character.id, blueprint.id)
            assertEquals(36, blueprint.stats.totalScore)
            assertTrue(blueprint.strengths.isNotEmpty())
            assertTrue(blueprint.weaknesses.isNotEmpty())
            assertTrue(blueprint.primaryAbility.cooldownTurns in 3..10)
            assertTrue(blueprint.primaryAbility.maxUsesPerMatch in 2..5)
        }
    }

    @Test
    fun `leader swap ability charges every 10 personal turns with 2 turn window and expires to turn 20 if unused`() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "raven") // SHADOW_WALKER (Leader Swap)
        viewModel.selectCharacterForSlot(1, "jack")  // GUARDIAN_SHIELD
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(0, 18)
        viewModel.setPlayerPositionForTest(1, 64)

        // Before personal turn 10, Leader Swap is not charged
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 9)
        val p0Turn9 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(9, p0Turn9.personalTurnsTaken)
        assertEquals(0, p0Turn9.leaderSwapWindowTurnsRemaining)
        assertFalse(p0Turn9.canTriggerActiveAbilityNow)

        // At personal turn 10, Leader Swap opens a 2-personal-turn window!
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 1)
        val p0Turn10 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(10, p0Turn10.personalTurnsTaken)
        assertEquals(2, p0Turn10.leaderSwapWindowTurnsRemaining)
        assertTrue(p0Turn10.canTriggerActiveAbilityNow)

        // Personal turn 11 (2nd turn of the window): still has 1 turn of opportunity remaining
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 1)
        val p0Turn11 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(11, p0Turn11.personalTurnsTaken)
        assertEquals(1, p0Turn11.leaderSwapWindowTurnsRemaining)
        assertTrue(p0Turn11.canTriggerActiveAbilityNow)

        // Personal turn 12 (unused after 2 personal turns): window expires and next charge is scheduled for turn 20
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 1)
        val p0Turn12 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(12, p0Turn12.personalTurnsTaken)
        assertEquals(0, p0Turn12.leaderSwapWindowTurnsRemaining)
        assertEquals(20, p0Turn12.leaderSwapNextChargeTurn)
        assertFalse(p0Turn12.canTriggerActiveAbilityNow)

        // Advance 8 more personal turns to reach personal turn 20 -> recharges again!
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 8)
        val p0Turn20 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(20, p0Turn20.personalTurnsTaken)
        assertEquals(2, p0Turn20.leaderSwapWindowTurnsRemaining)
        assertTrue(p0Turn20.canTriggerActiveAbilityNow)

        // Execute Leader Swap on personal turn 20 -> swaps positions with #1 leader (tile 64 <-> tile 18) and schedules next charge at 20 + 10 = 30
        val swapped = viewModel.triggerLeaderSwapForTest(0)
        assertTrue(swapped)
        val afterSwapP0 = viewModel.uiState.value.players.first { it.id == 0 }
        val afterSwapP1 = viewModel.uiState.value.players.first { it.id == 1 }
        assertEquals(64, afterSwapP0.currentTile)
        assertEquals(18, afterSwapP1.currentTile)
        assertEquals(0, afterSwapP0.leaderSwapWindowTurnsRemaining)
        assertEquals(30, afterSwapP0.leaderSwapNextChargeTurn)
    }

    @Test
    fun `executioner co-tile hunt knocks victim back by half their tile without resetting to start and respects shield`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "blade") // SNAKE_HUNTER (Executioner)
        viewModel.selectCharacterForSlot(1, "luna")  // Unshielded rival
        viewModel.startGame()

        // Both land on tile 40 -> Executioner hunts rival! Knockback = 40 / 2 = 20 steps -> new tile = 20
        viewModel.setPlayerPositionForTest(0, 40)
        viewModel.setPlayerPositionForTest(1, 40)
        val hunted = viewModel.triggerCoTileExecutionerHuntForTest(hunterId = 0, landedTile = 40, deterministicRollOverride = 10)
        assertTrue(hunted)
        assertEquals(20, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)

        // Even on tile 2 or 3, victim never resets to tile 1 (minimum tile is 2)
        viewModel.setPlayerPositionForTest(0, 3)
        viewModel.setPlayerPositionForTest(1, 3)
        val huntedAtLowTile = viewModel.triggerCoTileExecutionerHuntForTest(hunterId = 0, landedTile = 3, deterministicRollOverride = 10)
        assertTrue(huntedAtLowTile)
        assertEquals(2, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)
    }

    @Test
    fun `guardian shield defense probability scales with hero stats and critical parry preserves shield`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "jack") // GUARDIAN_SHIELD (Defense 10, Resistance 9)
        viewModel.startGame()

        // Critical Parry (roll = 5 < criticalParryPreserveShieldPercent) blocks AND keeps shield charged
        val parryBlocked = viewModel.evaluateShieldDefenseForTest(
            defenderId = 0,
            threatCategory = com.example.model.ShieldThreatCategory.SNAKE,
            deterministicRollOverride = 5
        )
        assertTrue(parryBlocked)
        assertTrue(viewModel.uiState.value.players.first { it.id == 0 }.isShieldCharged)

        // Standard block (roll = 40) blocks and consumes the shield charge
        val normalBlocked = viewModel.evaluateShieldDefenseForTest(
            defenderId = 0,
            threatCategory = com.example.model.ShieldThreatCategory.SNAKE,
            deterministicRollOverride = 40
        )
        assertTrue(normalBlocked)
        assertFalse(viewModel.uiState.value.players.first { it.id == 0 }.isShieldCharged)
    }

    @Test
    fun `hero combat interaction system evaluates priority defenses and records combat log entries`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "blade") // SNAKE_HUNTER / Warrior
        viewModel.selectCharacterForSlot(1, "luna")  // LADDER_MAGE / Mage (with Illusion Mirage decoy)
        viewModel.startGame()

        // Give Luna an active Illusionist Decoy
        viewModel.setPlayerPositionForTest(1, 30)
        viewModel.setPlayerDecoyForTest(playerId = 1, decoyTile = 30, turnsRemaining = 2)
        viewModel.setPlayerPositionForTest(0, 30)

        // Blade lands on Luna's tile 30. Luna's Illusionist decoy should absorb the hit!
        val combatHappened = viewModel.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 30,
            forcedAttackRoll = 10,
            forcedDefenseRoll = 90
        )
        assertTrue(combatHappened)

        // Luna's real tile should NOT change because the decoy diverted the attack
        assertEquals(30, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)

        // Combat log must have recorded the event!
        val logs = viewModel.uiState.value.combatLogs
        assertTrue(logs.isNotEmpty())
        val lastLog = logs.last()
        assertEquals(com.example.model.CombatOutcomeType.ILLUSION_DIVERTED, lastLog.outcomeType)
        assertTrue(lastLog.description.contains("بدل توهمی"))

        // Clear logs functionality
        viewModel.openCombatLogModal()
        assertTrue(viewModel.uiState.value.isCombatLogOpen)
        viewModel.clearCombatLogs()
        assertTrue(viewModel.uiState.value.combatLogs.isEmpty())
        viewModel.closeCombatLogModal()
        assertFalse(viewModel.uiState.value.isCombatLogOpen)
    }

    @Test
    fun `heavy anchor heroes resist executioner knockback with anchor immune combat log`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "blade") // Executioner
        viewModel.selectCharacterForSlot(1, "kai")   // HALF_REVERSE / Heavy Anchor
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(0, 50)
        viewModel.setPlayerPositionForTest(1, 50)

        // Blade collides with Kai on tile 50. Kai's heavy anchor resists displacement!
        val combatHappened = viewModel.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 50,
            forcedAttackRoll = 10,
            forcedDefenseRoll = 99
        )
        assertTrue(combatHappened)
        assertEquals(50, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)

        val lastLog = viewModel.uiState.value.combatLogs.last()
        assertEquals(com.example.model.CombatOutcomeType.ANCHOR_IMMUNE, lastLog.outcomeType)
        assertTrue(lastLog.description.contains("لنگر"))
    }

    @Test
    fun `pawn skin catalog contains all 8 required categories and 32 rich original skins`() {
        val categories = com.example.model.PawnSkinCategory.entries
        assertEquals(8, categories.size)

        val allSkins = com.example.model.PawnSkinCatalog.allSkins
        assertEquals(32, allSkins.size)

        categories.forEach { cat ->
            val skinsInCat = com.example.model.PawnSkinCatalog.getSkinsByCategory(cat)
            assertEquals(4, skinsInCat.size)
            skinsInCat.forEach { skin ->
                assertTrue(skin.id.isNotBlank())
                assertTrue(skin.persianName.isNotBlank())
                assertTrue(skin.englishName.isNotBlank())
                assertNotNull(skin.primaryColor)
            }
        }
    }

    @Test
    fun `equipping custom pawn skin updates slot state and active in-game players independently from hero choice`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)
        viewModel.selectCharacterForSlot(0, "blade") // Executioner
        viewModel.selectCharacterForSlot(1, "jack")  // Guardian

        // Equip luxury gold skin for slot 0 and elemental fire for slot 1
        viewModel.equipPawnSkinForSlot(0, "luxury_pure_gold")
        viewModel.equipPawnSkinForSlot(1, "elemental_living_flame")

        assertEquals("luxury_pure_gold", viewModel.uiState.value.playerPawnSkinIds[0])
        assertEquals("elemental_living_flame", viewModel.uiState.value.playerPawnSkinIds[1])

        viewModel.startGame()

        val player0 = viewModel.uiState.value.players.first { it.id == 0 }
        val player1 = viewModel.uiState.value.players.first { it.id == 1 }

        // Pawn skin resolved correctly
        assertEquals("luxury_pure_gold", player0.customPawnSkinId)
        assertEquals("طلای ناب سلطنتی", player0.pawnSkin.persianName)
        assertEquals(com.example.model.PawnSkinCategory.LUXURY, player0.pawnSkin.category)

        assertEquals("elemental_living_flame", player1.customPawnSkinId)
        assertEquals("شعله جاویدان آتش", player1.pawnSkin.persianName)
        assertEquals(com.example.model.PawnSkinCategory.ELEMENTAL, player1.pawnSkin.category)

        // Equipping during match dynamically updates player's pawn immediately
        viewModel.equipPawnSkinForSlot(0, "fantasy_magic_orb")
        val updatedP0 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals("fantasy_magic_orb", updatedP0.customPawnSkinId)
        assertEquals("گوی اسرارآمیز ارکان", updatedP0.pawnSkin.persianName)
    }

    @Test
    fun `equipping pawn skin for all players updates all active player pawns simultaneously`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)
        viewModel.setPlayerCount(3)
        viewModel.startGame()

        viewModel.equipPawnSkinForAll("special_cosmic_nebula")

        viewModel.uiState.value.playerPawnSkinIds.forEach { skinId ->
            assertEquals("special_cosmic_nebula", skinId)
        }

        viewModel.uiState.value.players.forEach { player ->
            assertEquals("special_cosmic_nebula", player.customPawnSkinId)
            assertEquals("سحابی کیهانی ژرف", player.pawnSkin.persianName)
        }
    }
}




