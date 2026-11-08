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
@Config(sdk = [34])
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
    fun `pawn skin catalog contains all 8 required categories and 39 rich original skins`() {
        val categories = com.example.model.PawnSkinCategory.entries
        assertEquals(8, categories.size)

        val allSkins = com.example.model.PawnSkinCatalog.allSkins
        assertEquals(39, allSkins.size)

        categories.forEach { cat ->
            val skinsInCat = com.example.model.PawnSkinCatalog.getSkinsByCategory(cat)
            assertTrue(skinsInCat.size >= 4)
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
        assertTrue(player0.pawnSkin.persianName.contains("طلا"))
        assertEquals(com.example.model.PawnSkinCategory.LUXURY, player0.pawnSkin.category)

        assertEquals("elemental_living_flame", player1.customPawnSkinId)
        assertTrue(player1.pawnSkin.persianName.contains("آتش"))
        assertEquals(com.example.model.PawnSkinCategory.ELEMENTAL, player1.pawnSkin.category)

        // Equipping during match dynamically updates player's pawn immediately
        viewModel.equipPawnSkinForSlot(0, "fantasy_magic_orb")
        val updatedP0 = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals("fantasy_magic_orb", updatedP0.customPawnSkinId)
        assertTrue(updatedP0.pawnSkin.persianName.contains("گوی"))
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
            assertTrue(player.pawnSkin.persianName.contains("سحابی"))
        }
    }

    @Test
    fun `unlocking pawn skin and unlocking all skins updates unlockedPawnSkinIds`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)

        viewModel.unlockPawnSkin("special_golden_dragon")
        assertTrue(viewModel.uiState.value.unlockedPawnSkinIds.contains("special_golden_dragon"))

        viewModel.unlockAllPawnSkins()
        assertEquals(39, viewModel.uiState.value.unlockedPawnSkinIds.size)
    }

    @Test
    fun `hero and pawn custom selections persist independently per player slot across matches`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)
        viewModel.setPlayerCount(3)

        // Select distinct hero and pawn skin for slot 0
        viewModel.selectCharacterForSlot(0, "nova")
        viewModel.equipPawnSkinForSlot(0, "luxury_gold")

        // Select distinct hero and pawn skin for slot 1
        viewModel.selectCharacterForSlot(1, "atlas")
        viewModel.equipPawnSkinForSlot(1, "fantasy_magic_orb")

        viewModel.startGame()

        val p0 = viewModel.uiState.value.players[0]
        val p1 = viewModel.uiState.value.players[1]

        assertEquals("nova", p0.character.id)
        assertEquals("luxury_gold", p0.customPawnSkinId)

        assertEquals("atlas", p1.character.id)
        assertEquals("fantasy_magic_orb", p1.customPawnSkinId)

        // Restart and verify selections persist
        val newVm = GameViewModel(context)
        assertEquals("nova", newVm.uiState.value.playerCharacterIds[0])
        assertEquals("luxury_gold", newVm.uiState.value.playerPawnSkinIds[0])
        assertEquals("atlas", newVm.uiState.value.playerCharacterIds[1])
        assertEquals("fantasy_magic_orb", newVm.uiState.value.playerPawnSkinIds[1])
    }

    @Test
    fun `in-game hero info and ability confirmation modal flows work seamlessly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = GameViewModel(context)
        viewModel.setPlayerCount(2)
        viewModel.startGame()

        // In-Game Hero Info modal open & close
        viewModel.openInGameHeroInfo(0)
        assertEquals(0, viewModel.uiState.value.inGameHeroInfoPlayerId)
        viewModel.closeInGameHeroInfo()
        assertNull(viewModel.uiState.value.inGameHeroInfoPlayerId)

        // Ability confirmation flow
        viewModel.requestAbilityActivation(0)
        assertEquals(0, viewModel.uiState.value.pendingAbilityConfirmationPlayerId)
        viewModel.cancelAbilityActivation()
        assertNull(viewModel.uiState.value.pendingAbilityConfirmationPlayerId)

        // Hero Collection dialog open & close
        viewModel.openHeroCollection(1)
        assertTrue(viewModel.uiState.value.isHeroCollectionOpen)
        assertEquals(1, viewModel.uiState.value.heroCollectionActiveSlot)
        viewModel.closeHeroCollection()
        assertFalse(viewModel.uiState.value.isHeroCollectionOpen)

        // Hero Clash event dismissal
        val p0 = viewModel.uiState.value.players[0]
        val p1 = viewModel.uiState.value.players[1]
        val clash = com.example.model.HeroClashEvent(
            attackerId = p0.id,
            attackerName = p0.name,
            attackerCharacter = p0.character,
            defenderId = p1.id,
            defenderName = p1.name,
            defenderCharacter = p1.character,
            abilityName = "تیر نشانه‌گیر",
            tile = 15,
            outcomeType = com.example.model.CombatOutcomeType.ATTACK_HIT,
            defenseStatusDescription = "دفاع پایه",
            resultMessage = "ضربه مستقیم وارد شد",
            attackRoll = 6,
            defenseRoll = 2,
            knockbackSteps = 2
        )
        viewModel.dismissHeroClash()
        assertNull(viewModel.uiState.value.activeHeroClash)
    }

    @Test
    fun `scenario 1 and 2 - executioner attacks guardian shield and shield successfully parries attack`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "blade") // Executioner / SNAKE_HUNTER
        viewModel.selectCharacterForSlot(1, "jack")  // Guardian / GUARDIAN_SHIELD
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(1, 40)
        viewModel.setPlayerPositionForTest(0, 40)

        // Force defense roll to 10 (successful parry)
        val combatHappened = viewModel.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 40,
            forcedAttackRoll = 10,
            forcedDefenseRoll = 10
        )
        assertTrue(combatHappened)

        // Defender remained on tile 40
        assertEquals(40, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)

        val lastLog = viewModel.uiState.value.combatLogs.last()
        assertTrue(
            lastLog.outcomeType == com.example.model.CombatOutcomeType.SHIELD_PARRY ||
            lastLog.outcomeType == com.example.model.CombatOutcomeType.CRITICAL_PARRY
        )
    }

    @Test
    fun `scenario 3 - executioner attacks guardian and shield fails resulting in hunt knockback`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "blade") // Executioner / SNAKE_HUNTER
        viewModel.selectCharacterForSlot(1, "jack")  // Guardian
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(1, 40)
        viewModel.setPlayerPositionForTest(0, 40)

        // Force attack roll to 10 (successful attack) and defense roll to 99 (shield fails)
        val combatHappened = viewModel.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 40,
            forcedAttackRoll = 10,
            forcedDefenseRoll = 99
        )
        assertTrue(combatHappened)

        // Knockback: tile 40 knocked back by 40/2 = 20 steps -> tile 20!
        assertEquals(20, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)

        val lastLog = viewModel.uiState.value.combatLogs.last()
        assertEquals(com.example.model.CombatOutcomeType.ATTACK_HIT, lastLog.outcomeType)
        assertTrue(lastLog.description.contains("پیروز شد"))
    }

    @Test
    fun `scenario 4 - resistant hero encounters negative obstacle and mitigates penalty`() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "kai") // HALF_REVERSE / Controller
        viewModel.selectCharacterForSlot(1, "blade")
        viewModel.startGame()

        val kai = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(com.example.model.CharacterAbilityType.HALF_REVERSE, kai.character.abilityType)
        assertTrue(kai.character.heroBlueprint.resistanceProfile.displacementResistancePercent > 0)
    }

    @Test
    fun `scenario 5 - shadow walker swaps position with leader player cleanly`() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "raven") // SHADOW_WALKER
        viewModel.selectCharacterForSlot(1, "jack")
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(0, 20)
        viewModel.setPlayerPositionForTest(1, 75) // Jack is leader

        // Give Raven an active window
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 10)
        val raven = viewModel.uiState.value.players.first { it.id == 0 }
        assertTrue(raven.leaderSwapWindowTurnsRemaining > 0)

        // Swap with leader
        val success = viewModel.triggerLeaderSwapForTest(0)
        assertTrue(success)

        // Tiles must be cleanly swapped!
        assertEquals(75, viewModel.uiState.value.players.first { it.id == 0 }.currentTile)
        assertEquals(20, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)
    }

    @Test
    fun `scenario 6 and 7 - shadow walker 2-turn window expires when unused and recharges at next 10-turn milestone`() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "raven") // SHADOW_WALKER
        viewModel.selectCharacterForSlot(1, "jack")
        viewModel.startGame()

        // Reach turn 10 milestone
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 10)
        assertEquals(2, viewModel.uiState.value.players.first { it.id == 0 }.leaderSwapWindowTurnsRemaining)

        // Turn 1 unused -> window becomes 1
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 1)
        assertEquals(1, viewModel.uiState.value.players.first { it.id == 0 }.leaderSwapWindowTurnsRemaining)

        // Turn 2 unused -> window expires (0)
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 1)
        assertEquals(0, viewModel.uiState.value.players.first { it.id == 0 }.leaderSwapWindowTurnsRemaining)

        // Advance 8 more turns to reach turn 20
        viewModel.simulatePersonalTurnsForPlayerForTest(0, 8)
        assertEquals(2, viewModel.uiState.value.players.first { it.id == 0 }.leaderSwapWindowTurnsRemaining)
    }

    @Test
    fun `scenario 8 - two non-combatant heroes co-exist peacefully on the same tile`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "luna")   // Mage
        viewModel.selectCharacterForSlot(1, "stella") // Support
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(1, 35)
        viewModel.setPlayerPositionForTest(0, 35)

        val combatHappened = viewModel.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 35,
            forcedAttackRoll = 50,
            forcedDefenseRoll = 50
        )
        assertTrue(combatHappened)

        // Both players coexist on tile 35 without knockback
        assertEquals(35, viewModel.uiState.value.players.first { it.id == 0 }.currentTile)
        assertEquals(35, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)

        val lastLog = viewModel.uiState.value.combatLogs.last()
        assertEquals(com.example.model.CombatOutcomeType.NEUTRAL_COEXIST, lastLog.outcomeType)
        assertTrue(lastLog.description.contains("هم‌گام"))
    }

    @Test
    fun `scenario 9 - conflicting abilities resolve deterministically via priority engine`() = kotlinx.coroutines.runBlocking {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.selectCharacterForSlot(0, "blade") // Executioner attack
        viewModel.selectCharacterForSlot(1, "luna")  // Has Illusion Decoy (Priority 1)
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(1, 45)
        viewModel.setPlayerDecoyForTest(1, decoyTile = 45, turnsRemaining = 2)
        viewModel.setPlayerPositionForTest(0, 45)

        // Attack roll is high (would normally hit), but Priority 1 Decoy diverts it first!
        val combatHappened = viewModel.triggerCombatInteractionForTest(
            attackerId = 0,
            landedTile = 45,
            forcedAttackRoll = 10,
            forcedDefenseRoll = 99
        )
        assertTrue(combatHappened)

        assertEquals(45, viewModel.uiState.value.players.first { it.id == 1 }.currentTile)
        val lastLog = viewModel.uiState.value.combatLogs.last()
        assertEquals(com.example.model.CombatOutcomeType.ILLUSION_DIVERTED, lastLog.outcomeType)
    }

    @Test
    fun `scenario 10 - multi-player shockwave ability affects multiple nearby rivals atomically`() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.setPlayerCount(4)
        viewModel.selectCharacterForSlot(0, "jack")  // Guardian Shield caster
        viewModel.selectCharacterForSlot(1, "nova")  // Nearby rival at tile 22
        viewModel.selectCharacterForSlot(2, "blade") // Nearby rival at tile 23
        viewModel.selectCharacterForSlot(3, "kai")   // Nearby rival with Heavy Anchor at tile 24
        viewModel.startGame()

        viewModel.setPlayerPositionForTest(0, 20) // Jack at 20
        viewModel.setPlayerPositionForTest(1, 22) // Nova at 22 (within 4 tiles)
        viewModel.setPlayerPositionForTest(2, 23) // Blade at 23 (within 4 tiles)
        viewModel.setPlayerPositionForTest(3, 24) // Kai at 24 (within 4 tiles, but Heavy Anchor)

        // Jack activates Guardian Shield
        viewModel.activateCharacterAbility()

        // Nova (22 - 2 = 20) and Blade (23 - 2 = 21) are knocked back by shockwave
        // Kai resists shockwave displacement due to Heavy Anchor!
        val p1 = viewModel.uiState.value.players.first { it.id == 1 }
        val p2 = viewModel.uiState.value.players.first { it.id == 2 }
        val p3 = viewModel.uiState.value.players.first { it.id == 3 }

        assertEquals(20, p1.currentTile)
        assertEquals(21, p2.currentTile)
        assertEquals(24, p3.currentTile) // Unmoved!
    }

    @Test
    fun `multiplayer resilience - ability idempotency and state snapshot prevent duplicate execution`() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.setPlayerCount(2)
        viewModel.selectCharacterForSlot(0, "jack")
        viewModel.selectCharacterForSlot(1, "nova")
        viewModel.startGame()

        val p0Before = viewModel.uiState.value.players.first { it.id == 0 }
        val initialUses = p0Before.abilityUsesCount

        // First activation
        viewModel.activateCharacterAbility()
        val p0AfterFirst = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(initialUses + 1, p0AfterFirst.abilityUsesCount)

        // Attempt duplicate execution immediately (e.g. rapid taps or network retransmission)
        // Since canTriggerActiveAbilityNow is now false (ability discharged), duplicate call does nothing!
        viewModel.activateCharacterAbility()
        val p0AfterDuplicate = viewModel.uiState.value.players.first { it.id == 0 }
        assertEquals(initialUses + 1, p0AfterDuplicate.abilityUsesCount)
    }
}
