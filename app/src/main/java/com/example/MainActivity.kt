package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GamePhase
import com.example.ui.GameViewModel
import com.example.ui.components.AdvancedSettingsModal
import com.example.ui.components.AdvancedSettingsTab
import com.example.ui.components.TruthOrDareCardManagerModal
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableHighRefreshRateRenderingIfAvailable()
        setContent {
            MyApplicationTheme {
                MysticSnakesAndLaddersApp()
            }
        }
    }

    /**
     * Requests the highest available display refresh rate (60 / 90 / 120 FPS) on supported devices
     * so `withFrameNanos` 3D dice physics and token hops run at native high frame rates.
     */
    private fun enableHighRefreshRateRenderingIfAvailable() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val supportedModes = display?.supportedModes.orEmpty()
                val highestRateMode = supportedModes.maxByOrNull { it.refreshRate }
                if (highestRateMode != null && highestRateMode.refreshRate >= 60f) {
                    val params = window.attributes
                    params.preferredDisplayModeId = highestRateMode.modeId
                    params.preferredRefreshRate = highestRateMode.refreshRate
                    window.attributes = params
                }
            } else {
                val params = window.attributes
                params.preferredRefreshRate = 120f
                window.attributes = params
            }
        }
    }
}

@Composable
fun MysticSnakesAndLaddersApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(context)
    )
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> gameViewModel.pauseBackgroundMusic()
                Lifecycle.Event.ON_RESUME -> gameViewModel.resumeBackgroundMusic()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (uiState.phase == GamePhase.SETUP) MysticDeepPlum else uiState.boardTheme.screenBgMid,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.phase == GamePhase.SETUP,
                animationSpec = tween(durationMillis = 320),
                modifier = Modifier.fillMaxSize(),
                label = "phase_transition"
            ) { isSetupPhase ->
                if (isSetupPhase) {
                    SetupScreen(
                        uiState = uiState,
                        onSelectPlayerCount = gameViewModel::setPlayerCount,
                        onUpdatePlayerName = gameViewModel::updatePlayerName,
                        onSelectSetupSlot = gameViewModel::selectSetupPlayerSlot,
                        onSelectCharacterForSlot = gameViewModel::selectCharacterForSlot,
                        onResetPlayerName = gameViewModel::resetPlayerNameToCharacterDefault,
                        onSelectDifficulty = gameViewModel::setDifficulty,
                        onToggleObstacleType = gameViewModel::toggleObstacleType,
                        onSelectBoardTheme = gameViewModel::setBoardTheme,
                        onSelectMaxTiles = gameViewModel::setMaxTiles,
                        onSelectAiDifficulty = gameViewModel::setAiDifficulty,
                        onOpenAdvancedSettings = gameViewModel::openAdvancedSettings,
                        onOpenCardManager = gameViewModel::openCardManager,
                        onOpenPawnCollection = gameViewModel::openPawnCollection,
                        onToggleSound = gameViewModel::toggleSound,
                        onToggleMusic = gameViewModel::toggleMusic,
                        onToggleAmbient = gameViewModel::toggleAmbient,
                        onPlayHeroStanceSound = gameViewModel::playHeroStanceSound,
                        onStartGame = gameViewModel::startGame
                    )
                } else {
                    GamePlayScreen(
                        uiState = uiState,
                        onRollDice = gameViewModel::rollDice,
                        onActivateAbility = gameViewModel::activateCharacterAbility,
                        onSelectPrecisionOption = gameViewModel::selectPrecisionRollOption,
                        onStepReached = gameViewModel::onTokenStepReached,
                        onMovementFinished = gameViewModel::onTokenMovementFinished,
                        onSelectBoardTheme = gameViewModel::setBoardTheme,
                        onSelectSnakeTheme = gameViewModel::setSnakeVisualTheme,
                        onSelectDiceTheme = gameViewModel::setDiceVisualTheme,
                        onOpenAdvancedSettings = {
                            gameViewModel.openAdvancedSettings(AdvancedSettingsTab.BOARD_AND_THEME)
                        },
                        onOpenStudioSettings = {
                            gameViewModel.openAdvancedSettings(AdvancedSettingsTab.EFFECTS_AND_MUSIC)
                        },
                        onOpenCardManager = gameViewModel::openCardManager,
                        onOpenCombatLog = gameViewModel::openCombatLogModal,
                        onOpenPawnCollection = {
                            gameViewModel.openPawnCollection(uiState.currentPlayer?.id ?: 0)
                        },
                        onOpenHeroCollection = {
                            gameViewModel.openHeroCollection(uiState.currentPlayer?.id ?: 0)
                        },
                        onOpenHeroInfo = gameViewModel::openInGameHeroInfo,
                        onRequestAbilityActivation = gameViewModel::requestAbilityActivation,
                        onFlipRevealSnakeCard = gameViewModel::flipRevealSnakeCard,
                        onCompleteSnakeCardChallenge = gameViewModel::completeSnakeCardChallenge,
                        onRestartGame = gameViewModel::restartCurrentMatch,
                        onRegenerateBoard = gameViewModel::regenerateBoardLayout,
                        onToggleSound = gameViewModel::toggleSound,
                        onToggleMusic = gameViewModel::toggleMusic,
                        onToggleAmbient = gameViewModel::toggleAmbient,
                        onBackToSetup = gameViewModel::returnToSetup
                    )
                }
            }

            // Pawn Customization & Showcase Dialog
            if (uiState.isPawnCollectionOpen) {
                com.example.ui.components.PawnCollectionDialog(
                    currentEquippedSkinId = uiState.playerPawnSkinIds.getOrElse(uiState.pawnCollectionActiveSlot) { "classic_standard" },
                    playerPawnSkinIds = uiState.playerPawnSkinIds,
                    playerCount = uiState.playerCount,
                    initialPlayerSlot = uiState.pawnCollectionActiveSlot,
                    playerColorIndices = if (uiState.phase == GamePhase.SETUP) {
                        List(uiState.playerCount) { it }
                    } else {
                        uiState.players.map { it.colorIndex }
                    },
                    unlockedSkinIds = uiState.unlockedPawnSkinIds,
                    onEquipSkinForSlot = gameViewModel::equipPawnSkinForSlot,
                    onEquipSkinForAllSlots = gameViewModel::equipPawnSkinForAll,
                    onUnlockSkin = gameViewModel::unlockPawnSkin,
                    onUnlockAllSkins = gameViewModel::unlockAllPawnSkins,
                    onDismissRequest = gameViewModel::closePawnCollection
                )
            }

            // Hero Collection & Customization Dialog
            if (uiState.isHeroCollectionOpen) {
                com.example.ui.components.HeroCollectionDialog(
                    currentEquippedCharacterIds = uiState.playerCharacterIds,
                    playerCount = uiState.playerCount,
                    initialPlayerSlot = uiState.heroCollectionActiveSlot,
                    onEquipHeroForSlot = gameViewModel::selectCharacterForSlot,
                    onDismissRequest = gameViewModel::closeHeroCollection
                )
            }

            // Real-time Hero Clash Interaction Overlay
            uiState.activeHeroClash?.let { clashEvent ->
                com.example.ui.components.HeroClashInteractionOverlay(
                    clashEvent = clashEvent,
                    onDismiss = gameViewModel::dismissHeroClash
                )
            }

            // In-Game Active Ability Confirmation & Preview Modal
            uiState.pendingAbilityConfirmationPlayerId?.let { playerId ->
                val targetPlayer = uiState.players.find { it.id == playerId }
                if (targetPlayer != null) {
                    com.example.ui.components.AbilityActivationConfirmModal(
                        player = targetPlayer,
                        onConfirm = { gameViewModel.confirmAbilityActivation(playerId) },
                        onDismiss = gameViewModel::cancelAbilityActivation
                    )
                }
            }

            // In-Game Hero Status HUD & Information Modal
            uiState.inGameHeroInfoPlayerId?.let { playerId ->
                val targetPlayer = uiState.players.find { it.id == playerId }
                if (targetPlayer != null) {
                    com.example.ui.components.InGameHeroInfoModal(
                        player = targetPlayer,
                        isCurrentTurn = uiState.currentPlayer?.id == targetPlayer.id,
                        onActivateAbility = { gameViewModel.requestAbilityActivation(playerId) },
                        onDismiss = gameViewModel::closeInGameHeroInfo
                    )
                }
            }

            // Hero Combat & Interaction Log Modal
            if (uiState.isCombatLogOpen) {
                com.example.ui.components.CombatLogModal(
                    combatLogs = uiState.combatLogs,
                    onClearLogs = gameViewModel::clearCombatLogs,
                    onDismiss = gameViewModel::closeCombatLogModal
                )
            }

            // Phase 4: Truth or Dare Card Manager Modal
            if (uiState.isCardManagerOpen) {
                TruthOrDareCardManagerModal(
                    cards = uiState.truthOrDareCards,
                    enabledCategories = uiState.enabledCardCategories,
                    saveSuccessMessage = uiState.cardSaveFeedbackMessage,
                    onAddCard = gameViewModel::addTruthOrDareCard,
                    onUpdateCard = gameViewModel::updateTruthOrDareCard,
                    onDeleteCard = gameViewModel::deleteTruthOrDareCard,
                    onToggleCategoryEnabled = gameViewModel::toggleCardCategoryEnabled,
                    onDismiss = gameViewModel::closeCardManager
                )
            }

            // Phase 5: Full Advanced Settings & Custom Character Creator + Profiles Modal + Effects & Music Studio
            if (uiState.isAdvancedSettingsOpen) {
                AdvancedSettingsModal(
                    uiState = uiState,
                    initialTab = uiState.advancedSettingsInitialTab,
                    onSelectMaxTiles = gameViewModel::setMaxTiles,
                    onSelectTheme = gameViewModel::setBoardTheme,
                    onSelectSnakeTheme = gameViewModel::setSnakeVisualTheme,
                    onSetSnakeThickness = gameViewModel::setSnakeThicknessScale,
                    onSelectDiceTheme = gameViewModel::setDiceVisualTheme,
                    onSelectDifficulty = gameViewModel::setDifficulty,
                    onSelectAiDifficulty = gameViewModel::setAiDifficulty,
                    onToggleObstacleType = gameViewModel::toggleObstacleType,
                    onSetAllObstaclesEnabled = gameViewModel::setAllObstaclesEnabled,
                    onCreateCustomCharacter = gameViewModel::createCustomCharacter,
                    onDeleteCustomCharacter = gameViewModel::deleteCustomCharacter,
                    onAddCard = gameViewModel::addTruthOrDareCard,
                    onUpdateCard = gameViewModel::updateTruthOrDareCard,
                    onDeleteCard = gameViewModel::deleteTruthOrDareCard,
                    onToggleCardCategory = gameViewModel::toggleCardCategoryEnabled,
                    onSaveSettingsProfile = gameViewModel::saveCurrentSettingsProfile,
                    onLoadSettingsProfile = gameViewModel::loadSettingsProfile,
                    onDeleteSettingsProfile = gameViewModel::deleteSettingsProfile,
                    onToggleMusic = gameViewModel::toggleMusic,
                    onToggleAmbient = gameViewModel::toggleAmbient,
                    onSetMusicVolume = gameViewModel::setMusicVolume,
                    onSetAmbientVolume = gameViewModel::setAmbientVolume,
                    onToggleSound = gameViewModel::toggleSound,
                    onSetSfxVolume = gameViewModel::setSfxVolume,
                    onToggleEnvironmentEffects = gameViewModel::toggleEnvironmentEffects,
                    onSelectEnvironmentEffect = gameViewModel::selectEnvironmentEffect,
                    onPreviewEnvironmentEffect = gameViewModel::previewEnvironmentEffect,
                    onSelectEffectIntensity = gameViewModel::setEnvironmentEffectIntensity,
                    onPreviewTheme = gameViewModel::previewBoardTheme,
                    onToggleAmbientAnimation = gameViewModel::toggleAmbientAnimation,
                    onSelectBackgroundLightingLevel = gameViewModel::setBackgroundLightingLevel,
                    onToggleWeatherEffects = gameViewModel::toggleWeatherEffects,
                    onSelectMusicGenre = gameViewModel::selectMusicGenre,
                    onPreviewMusicGenre = gameViewModel::previewMusicGenre,
                    onTriggerEventSoundDemo = gameViewModel::triggerEventSoundDemo,
                    onResetStudioToDefaults = gameViewModel::resetStudioToDefaults,
                    onSelectAnimationQuality = gameViewModel::setAnimationQuality,
                    onSelectAnimationSpeed = gameViewModel::setAnimationSpeed,
                    onToggleCameraFollow = gameViewModel::toggleCameraFollow,
                    onEquipPawnSkinForSlot = gameViewModel::equipPawnSkinForSlot,
                    onEquipPawnSkinForAll = gameViewModel::equipPawnSkinForAll,
                    onUnlockPawnSkin = gameViewModel::unlockPawnSkin,
                    onUnlockAllPawnSkins = gameViewModel::unlockAllPawnSkins,
                    onEquipHeroForSlot = gameViewModel::selectCharacterForSlot,
                    onDismiss = gameViewModel::closeAdvancedSettings
                )
            }
        }
    }
}
