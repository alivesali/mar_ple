package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BannerType
import com.example.model.BoardTheme
import com.example.model.DiceVisualTheme
import com.example.model.DifficultyLevel
import com.example.model.GameEventBanner
import com.example.model.GameCharacter
import com.example.model.GamePhase
import com.example.model.LinkType
import com.example.model.Player
import com.example.model.SnakeVisualTheme
import com.example.model.toPersianDigits
import com.example.sound.soundscapeInfo
import com.example.ui.GameUiState
import com.example.ui.components.BoardThemeWorldBackdrop
import com.example.ui.components.CharacterPortraitAvatar
import com.example.ui.components.CombatLogModal
import com.example.ui.components.DiceAndTurnPanel
import com.example.ui.components.EnvironmentEffectsOverlay
import com.example.ui.components.HeroDetailShowcaseDialog
import com.example.ui.components.PlayerPawnTokenIcon
import com.example.ui.components.SnakesAndLaddersBoard
import com.example.ui.components.TruthOrDareCardDrawOverlay
import com.example.ui.components.WinnerCelebrationOverlay
import com.example.ui.components.readableTokenTextColor
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.RadiantGold

@Composable
fun GamePlayScreen(
    uiState: GameUiState,
    onRollDice: () -> Unit,
    onActivateAbility: () -> Unit,
    onSelectPrecisionOption: (Int) -> Unit,
    onStepReached: (Int, Int) -> Unit,
    onMovementFinished: (Long) -> Unit,
    onSelectBoardTheme: (BoardTheme) -> Unit,
    onSelectSnakeTheme: (SnakeVisualTheme) -> Unit = {},
    onSelectDiceTheme: (DiceVisualTheme) -> Unit = {},
    onOpenAdvancedSettings: () -> Unit,
    onOpenStudioSettings: () -> Unit = onOpenAdvancedSettings,
    onOpenCardManager: () -> Unit,
    onOpenCombatLog: () -> Unit = {},
    onFlipRevealSnakeCard: () -> Unit,
    onCompleteSnakeCardChallenge: () -> Unit,
    onRestartGame: () -> Unit,
    onRegenerateBoard: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit = {},
    onToggleAmbient: () -> Unit = {},
    onBackToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTopMenuExpanded by remember { mutableStateOf(false) }
    var isThemeSwitcherExpanded by remember { mutableStateOf(false) }

    BackHandler {
        when {
            isThemeSwitcherExpanded -> isThemeSwitcherExpanded = false
            isTopMenuExpanded -> isTopMenuExpanded = false
            else -> onBackToSetup()
        }
    }

    val activeTheme = uiState.effectiveBoardTheme

    val ladderCount = uiState.boardLinks.count { it.type == LinkType.LADDER }
    val snakeCount = uiState.boardLinks.count { it.type == LinkType.SNAKE }
    val obstacleCount = uiState.boardObstacles.size
    val cardCount = uiState.truthOrDareCards.size
    var inspectingHero by remember { mutableStateOf<GameCharacter?>(null) }

    val bgTop by animateColorAsState(
        targetValue = activeTheme.screenBgTop,
        label = "game_bg_top"
    )
    val bgMid by animateColorAsState(
        targetValue = activeTheme.screenBgMid,
        label = "game_bg_mid"
    )
    val bgBottom by animateColorAsState(
        targetValue = activeTheme.screenBgBottom,
        label = "game_bg_bottom"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgTop, bgMid, bgBottom)
                )
            )
    ) {
        // Living Theme World Backdrop behind the game board (with smooth Crossfade transition)
        androidx.compose.animation.Crossfade(
            targetState = activeTheme,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            modifier = Modifier.fillMaxSize(),
            label = "board_theme_world_crossfade"
        ) { targetWorldTheme ->
            BoardThemeWorldBackdrop(
                boardTheme = targetWorldTheme,
                isEnvironmentEffectsEnabled = uiState.isEnvironmentEffectsEnabled,
                particleDensity = uiState.environmentEffectIntensity,
                isAmbientAnimationEnabled = uiState.isAmbientAnimationEnabled,
                backgroundLightingLevel = uiState.backgroundLightingLevel,
                isWeatherEffectsEnabled = uiState.isWeatherEffectsEnabled,
                modifier = Modifier.fillMaxSize()
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            val isWideLayout = maxWidth > 680.dp && maxWidth > maxHeight
            val isSinglePlayerMode = uiState.playerCount == 1

            if (isWideLayout) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Landscape Board Area — Maximum width & height allocation
                    Box(
                        modifier = Modifier
                            .weight(1.38f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        SnakesAndLaddersBoard(
                            maxTiles = uiState.maxTiles,
                            boardTheme = activeTheme,
                            snakeVisualTheme = uiState.snakeVisualTheme,
                            snakeThicknessScale = uiState.snakeThicknessScale,
                            isWeatherEffectsEnabled = uiState.isWeatherEffectsEnabled,
                            isAmbientAnimationEnabled = uiState.isAmbientAnimationEnabled,
                            animationQuality = uiState.animationQuality,
                            animationSpeed = uiState.animationSpeed,
                            isCameraFollowEnabled = uiState.isCameraFollowEnabled,
                            boardLinks = uiState.boardLinks,
                            boardObstacles = uiState.boardObstacles,
                            players = uiState.players,
                            currentTurnPlayerId = uiState.currentPlayer?.id,
                            activeMovementPath = uiState.activeMovementPath,
                            highlightEffect = uiState.highlightEffect,
                            stormFlashActive = uiState.stormFlashActive,
                            onStepReached = onStepReached,
                            onMovementFinished = onMovementFinished,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Tap-outside dismiss overlay when menu or theme switcher is open
                        if (isTopMenuExpanded || isThemeSwitcherExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        isTopMenuExpanded = false
                                        isThemeSwitcherExpanded = false
                                    }
                                    .testTag("top_menu_outside_dismiss_backdrop")
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.88f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        UnifiedTopHeaderWithCollapsibleMenu(
                            boardTheme = activeTheme,
                            difficulty = uiState.difficulty,
                            maxTiles = uiState.maxTiles,
                            ladderCount = ladderCount,
                            snakeCount = snakeCount,
                            obstacleCount = obstacleCount,
                            cardCount = cardCount,
                            players = uiState.players,
                            currentTurnPlayerId = uiState.currentPlayer?.id,
                            leaderPlayerId = uiState.leaderPlayer?.id,
                            isSinglePlayerMode = isSinglePlayerMode,
                            isMenuExpanded = isTopMenuExpanded,
                            isThemeSwitcherOpen = isThemeSwitcherExpanded,
                            isSoundEnabled = uiState.isSoundEnabled,
                            isMusicEnabled = uiState.isMusicEnabled,
                            onToggleMenu = {
                                isTopMenuExpanded = !isTopMenuExpanded
                                if (!isTopMenuExpanded) {
                                    isThemeSwitcherExpanded = false
                                }
                            },
                            onToggleThemeSwitcher = {
                                isThemeSwitcherExpanded = !isThemeSwitcherExpanded
                            },
                            onOpenAdvancedSettings = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onOpenAdvancedSettings()
                            },
                            onOpenStudioSettings = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onOpenStudioSettings()
                            },
                            onOpenCardManager = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onOpenCardManager()
                            },
                            onOpenCombatLog = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onOpenCombatLog()
                            },
                            onInspectHero = { inspectingHero = it },
                            onRestartGame = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onRestartGame()
                            },
                            onRegenerateBoard = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onRegenerateBoard()
                            },
                            onToggleSound = onToggleSound,
                            onToggleMusic = onToggleMusic,
                            onBackToSetup = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onBackToSetup()
                            }
                        )

                        AnimatedVisibility(
                            visible = isThemeSwitcherExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            InGameThemeSwitcherBar(
                                currentTheme = activeTheme,
                                currentSnakeTheme = uiState.snakeVisualTheme,
                                currentDiceTheme = uiState.diceVisualTheme,
                                isMusicEnabled = uiState.isMusicEnabled,
                                isAmbientEnabled = uiState.isAmbientEnabled,
                                onSelectTheme = { theme ->
                                    onSelectBoardTheme(theme)
                                    isThemeSwitcherExpanded = false
                                    isTopMenuExpanded = false
                                },
                                onSelectSnakeTheme = onSelectSnakeTheme,
                                onSelectDiceTheme = onSelectDiceTheme,
                                onToggleMusic = onToggleMusic,
                                onToggleAmbient = onToggleAmbient,
                                onOpenStudioSettings = {
                                    isThemeSwitcherExpanded = false
                                    isTopMenuExpanded = false
                                    onOpenStudioSettings()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        EventFeedbackBanner(
                            banner = uiState.eventBanner,
                            currentPlayer = uiState.currentPlayer,
                            isDiceRolling = uiState.isDiceRolling,
                            isTokenAnimating = uiState.isTokenAnimating,
                            sixRollGlowActive = uiState.sixRollGlowActive,
                            isSinglePlayerMode = isSinglePlayerMode
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        DiceAndTurnPanel(
                            currentPlayer = uiState.currentPlayer,
                            leaderPlayer = uiState.leaderPlayer,
                            diceValue = uiState.diceValue,
                            secondDiceValue = uiState.secondDiceValue,
                            isDiceRolling = uiState.isDiceRolling,
                            isTokenAnimating = uiState.isTokenAnimating,
                            diceRotationDeg = uiState.diceRotationDeg,
                            secondDiceRotationDeg = uiState.secondDiceRotationDeg,
                            diceRollTriggerId = uiState.diceRollTriggerId,
                            sixRollGlowActive = uiState.sixRollGlowActive,
                            pendingPrecisionChoice = uiState.pendingPrecisionChoice,
                            diceVisualTheme = uiState.effectiveDiceTheme,
                            animationQuality = uiState.animationQuality,
                            animationSpeed = uiState.animationSpeed,
                            isSinglePlayerMode = isSinglePlayerMode,
                            onRollDice = {
                                isTopMenuExpanded = false
                                isThemeSwitcherExpanded = false
                                onRollDice()
                            },
                            onActivateAbility = onActivateAbility,
                            onSelectPrecisionOption = onSelectPrecisionOption
                        )
                    }
                }
            } else {
                // Portrait Fullscreen Space-Prioritized Layout:
                // 1. Single-row Top Header (40.dp): Main Collapsible Menu Button + Players Status Strip
                // 2. Game Board (weight(1f, fill = true)): Takes maximum width & height
                // 3. Slim Turn Status Bar (30.dp)
                // 4. Compact Dice & Active Player Panel (96.dp)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UnifiedTopHeaderWithCollapsibleMenu(
                        boardTheme = activeTheme,
                        difficulty = uiState.difficulty,
                        maxTiles = uiState.maxTiles,
                        ladderCount = ladderCount,
                        snakeCount = snakeCount,
                        obstacleCount = obstacleCount,
                        cardCount = cardCount,
                        players = uiState.players,
                        currentTurnPlayerId = uiState.currentPlayer?.id,
                        leaderPlayerId = uiState.leaderPlayer?.id,
                        isSinglePlayerMode = isSinglePlayerMode,
                        isMenuExpanded = isTopMenuExpanded,
                        isThemeSwitcherOpen = isThemeSwitcherExpanded,
                        isSoundEnabled = uiState.isSoundEnabled,
                        isMusicEnabled = uiState.isMusicEnabled,
                        onToggleMenu = {
                            isTopMenuExpanded = !isTopMenuExpanded
                            if (!isTopMenuExpanded) {
                                isThemeSwitcherExpanded = false
                            }
                        },
                        onToggleThemeSwitcher = {
                            isThemeSwitcherExpanded = !isThemeSwitcherExpanded
                        },
                        onOpenAdvancedSettings = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onOpenAdvancedSettings()
                        },
                        onOpenStudioSettings = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onOpenStudioSettings()
                        },
                        onOpenCardManager = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onOpenCardManager()
                        },
                        onOpenCombatLog = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onOpenCombatLog()
                        },
                        onInspectHero = { inspectingHero = it },
                        onRestartGame = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onRestartGame()
                        },
                        onRegenerateBoard = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onRegenerateBoard()
                        },
                        onToggleSound = onToggleSound,
                        onToggleMusic = onToggleMusic,
                        onBackToSetup = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onBackToSetup()
                        }
                    )

                    // Maximized Game Board Area — Takes almost the entire width & all remaining vertical space
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        SnakesAndLaddersBoard(
                            maxTiles = uiState.maxTiles,
                            boardTheme = activeTheme,
                            snakeVisualTheme = uiState.snakeVisualTheme,
                            snakeThicknessScale = uiState.snakeThicknessScale,
                            isWeatherEffectsEnabled = uiState.isWeatherEffectsEnabled,
                            isAmbientAnimationEnabled = uiState.isAmbientAnimationEnabled,
                            animationQuality = uiState.animationQuality,
                            animationSpeed = uiState.animationSpeed,
                            isCameraFollowEnabled = uiState.isCameraFollowEnabled,
                            boardLinks = uiState.boardLinks,
                            boardObstacles = uiState.boardObstacles,
                            players = uiState.players,
                            currentTurnPlayerId = uiState.currentPlayer?.id,
                            activeMovementPath = uiState.activeMovementPath,
                            highlightEffect = uiState.highlightEffect,
                            stormFlashActive = uiState.stormFlashActive,
                            onStepReached = onStepReached,
                            onMovementFinished = onMovementFinished,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Tap-outside dismiss backdrop when collapsible top menu or theme switcher is open
                        if (isTopMenuExpanded || isThemeSwitcherExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        isTopMenuExpanded = false
                                        isThemeSwitcherExpanded = false
                                    }
                                    .testTag("top_menu_outside_dismiss_backdrop")
                            )
                        }

                        // Floating In-Game Theme Switcher Overlay at top of board when opened
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isThemeSwitcherExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            InGameThemeSwitcherBar(
                                currentTheme = activeTheme,
                                currentSnakeTheme = uiState.snakeVisualTheme,
                                currentDiceTheme = uiState.diceVisualTheme,
                                isMusicEnabled = uiState.isMusicEnabled,
                                isAmbientEnabled = uiState.isAmbientEnabled,
                                onSelectTheme = { theme ->
                                    onSelectBoardTheme(theme)
                                    isThemeSwitcherExpanded = false
                                    isTopMenuExpanded = false
                                },
                                onSelectSnakeTheme = onSelectSnakeTheme,
                                onSelectDiceTheme = onSelectDiceTheme,
                                onToggleMusic = onToggleMusic,
                                onToggleAmbient = onToggleAmbient,
                                onOpenStudioSettings = {
                                    isThemeSwitcherExpanded = false
                                    isTopMenuExpanded = false
                                    onOpenStudioSettings()
                                }
                            )
                        }
                    }

                    // Slim Smart Turn & Event Status Bar (30.dp) below the board
                    EventFeedbackBanner(
                        banner = uiState.eventBanner,
                        currentPlayer = uiState.currentPlayer,
                        isDiceRolling = uiState.isDiceRolling,
                        isTokenAnimating = uiState.isTokenAnimating,
                        sixRollGlowActive = uiState.sixRollGlowActive,
                        isSinglePlayerMode = isSinglePlayerMode
                    )

                    // Compact Fixed-Height Bottom Panel (96.dp) — Never expands onto the board!
                    DiceAndTurnPanel(
                        currentPlayer = uiState.currentPlayer,
                        leaderPlayer = uiState.leaderPlayer,
                        diceValue = uiState.diceValue,
                        secondDiceValue = uiState.secondDiceValue,
                        isDiceRolling = uiState.isDiceRolling,
                        isTokenAnimating = uiState.isTokenAnimating,
                        diceRotationDeg = uiState.diceRotationDeg,
                        secondDiceRotationDeg = uiState.secondDiceRotationDeg,
                        diceRollTriggerId = uiState.diceRollTriggerId,
                        sixRollGlowActive = uiState.sixRollGlowActive,
                        pendingPrecisionChoice = uiState.pendingPrecisionChoice,
                        diceVisualTheme = uiState.effectiveDiceTheme,
                        animationQuality = uiState.animationQuality,
                        animationSpeed = uiState.animationSpeed,
                        isSinglePlayerMode = isSinglePlayerMode,
                        onRollDice = {
                            isTopMenuExpanded = false
                            isThemeSwitcherExpanded = false
                            onRollDice()
                        },
                        onActivateAbility = onActivateAbility,
                        onSelectPrecisionOption = onSelectPrecisionOption
                    )
                }
            }
        }

        // Non-blocking atmospheric visual effects overlay (Golden Dust, Magic Particles, Aurora, etc.)
        EnvironmentEffectsOverlay(
            effectType = uiState.effectiveEnvironmentEffect,
            intensity = uiState.environmentEffectIntensity,
            isEnabled = uiState.isEnvironmentEffectsEnabled || uiState.previewEnvironmentEffect != null,
            animationQuality = uiState.animationQuality,
            modifier = Modifier.fillMaxSize()
        )

        // Suspenseful 3D Truth or Dare Card Draw Overlay on Snake Bite!
        if (uiState.activeSnakeCardDraw != null) {
            TruthOrDareCardDrawOverlay(
                activeDraw = uiState.activeSnakeCardDraw,
                onFlipRevealCard = onFlipRevealSnakeCard,
                onCompleteCardChallenge = onCompleteSnakeCardChallenge
            )
        }

        if (uiState.phase == GamePhase.CELEBRATION && uiState.winner != null) {
            WinnerCelebrationOverlay(
                winner = uiState.winner,
                allPlayers = uiState.players,
                onPlayAgainSamePlayers = onRestartGame,
                onOpenAdvancedSettings = onOpenAdvancedSettings,
                onBackToSetup = onBackToSetup
            )
        }

        inspectingHero?.let { hero ->
            HeroDetailShowcaseDialog(
                character = hero,
                onDismissRequest = { inspectingHero = null }
            )
        }
    }
}

/**
 * Single-row Unified Top Header (40.dp height) that consolidates:
 * - A single sleek round Main Menu Trigger Button (`top_collapsible_menu_trigger_button`) harmonized with the active `BoardTheme`.
 * - A Horizontal Collapsible Menu Drawer (`top_collapsible_horizontal_menu`) that smoothly expands side-by-side when tapped.
 * - The compact `PlayersStatusStrip` occupying the remaining horizontal width in the normal collapsed state.
 */
@Composable
private fun UnifiedTopHeaderWithCollapsibleMenu(
    boardTheme: BoardTheme,
    difficulty: DifficultyLevel,
    maxTiles: Int,
    ladderCount: Int,
    snakeCount: Int,
    obstacleCount: Int,
    cardCount: Int,
    players: List<Player>,
    currentTurnPlayerId: Int?,
    leaderPlayerId: Int?,
    isSinglePlayerMode: Boolean,
    isMenuExpanded: Boolean,
    isThemeSwitcherOpen: Boolean,
    isSoundEnabled: Boolean,
    isMusicEnabled: Boolean,
    onToggleMenu: () -> Unit,
    onToggleThemeSwitcher: () -> Unit,
    onOpenAdvancedSettings: () -> Unit,
    onOpenStudioSettings: () -> Unit,
    onOpenCardManager: () -> Unit,
    onOpenCombatLog: () -> Unit = {},
    onInspectHero: (GameCharacter) -> Unit = {},
    onRestartGame: () -> Unit,
    onRegenerateBoard: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onBackToSetup: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "main_menu_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "menu_button_glow_pulse"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isMenuExpanded) 135f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "menu_trigger_rotation"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isMenuExpanded) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "menu_trigger_scale"
    )
    val accentColor = boardTheme.primaryAccent
    val borderHighlight = boardTheme.boardBorderHighlight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Single Main Collapsible Menu Button (Round, Theme-Harmonized, Subtle Glow & Touch Animation)
        IconButton(
            onClick = onToggleMenu,
            modifier = Modifier
                .size(38.dp)
                .scale(buttonScale)
                .shadow(
                    elevation = if (isMenuExpanded) 12.dp else 6.dp,
                    shape = CircleShape,
                    spotColor = accentColor
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isMenuExpanded) {
                            listOf(
                                accentColor.copy(alpha = 0.36f),
                                boardTheme.boardFrameOuterTop,
                                Color(0xFF120E09)
                            )
                        } else {
                            listOf(
                                boardTheme.boardFrameOuterTop,
                                boardTheme.boardFrameOuterBottom,
                                Color(0xFF0E0B07)
                            )
                        }
                    )
                )
                .border(
                    width = if (isMenuExpanded) 1.8.dp else 1.3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            borderHighlight.copy(alpha = glowPulse),
                            accentColor.copy(alpha = 0.85f),
                            borderHighlight.copy(alpha = glowPulse)
                        )
                    ),
                    shape = CircleShape
                )
                .testTag("top_collapsible_menu_trigger_button")
        ) {
            Icon(
                imageVector = if (isMenuExpanded) Icons.Filled.Close else Icons.Filled.Settings,
                contentDescription = if (isMenuExpanded) "بستن منوی ابزار بازی" else "باز کردن منوی ابزار بازی",
                tint = if (isMenuExpanded) Color.White else accentColor,
                modifier = Modifier
                    .size(19.dp)
                    .rotate(iconRotation)
            )
        }

        // 2. Collapsible Horizontal Menu Drawer (Expands side-by-side when Main Button is clicked)
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = expandHorizontally(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Start
            ) + fadeIn(animationSpec = tween(durationMillis = 220)),
            exit = shrinkHorizontally(
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Start
            ) + fadeOut(animationSpec = tween(durationMillis = 180)),
            modifier = Modifier.weight(1f)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xEB14100A),
                                boardTheme.boardFrameOuterTop.copy(alpha = 0.92f),
                                Color(0xEB100C08)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.72f),
                                borderHighlight.copy(alpha = 0.45f),
                                accentColor.copy(alpha = 0.65f)
                            )
                        ),
                        shape = RoundedCornerShape(19.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("top_collapsible_horizontal_menu"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option 1: Themes (تم‌ها)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.Palette,
                        label = "تم‌ها",
                        contentDescription = "تغییر تم صفحه بازی",
                        tint = if (isThemeSwitcherOpen) Color(0xFF140B02) else accentColor,
                        containerColor = if (isThemeSwitcherOpen) accentColor else Color(0xFF1C160D),
                        borderColor = accentColor.copy(alpha = 0.85f),
                        onClick = onToggleThemeSwitcher,
                        testTag = "ingame_theme_switcher_button"
                    )
                }

                // Option 2: Environment Effects Studio (جلوه‌های محیطی)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.AutoAwesome,
                        label = "جلوه‌ها",
                        contentDescription = "استودیو جلوه‌های محیطی و موسیقی",
                        tint = RadiantGold,
                        containerColor = Color(0xFF1C160D),
                        borderColor = RadiantGold.copy(alpha = 0.75f),
                        onClick = onOpenStudioSettings,
                        testTag = "game_open_studio_button"
                    )
                }

                // Option 3: Advanced Settings (تنظیمات)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.Settings,
                        label = "تنظیمات",
                        contentDescription = "تنظیمات پیشرفته",
                        tint = RadiantGold,
                        containerColor = Color(0xFF1C160D),
                        borderColor = RadiantGold.copy(alpha = 0.55f),
                        onClick = onOpenAdvancedSettings,
                        testTag = "game_open_advanced_settings_button"
                    )
                }

                // Option 4: Music Toggle (موسیقی)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = if (isMusicEnabled) Icons.Filled.MusicNote else Icons.Filled.MusicOff,
                        label = "موسیقی",
                        contentDescription = if (isMusicEnabled) "موسیقی پس‌زمینه روشن" else "موسیقی پس‌زمینه خاموش",
                        tint = if (isMusicEnabled) RadiantGold else Color.White.copy(alpha = 0.50f),
                        containerColor = Color(0xFF1C160D),
                        borderColor = if (isMusicEnabled) RadiantGold.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.18f),
                        onClick = onToggleMusic,
                        testTag = "game_music_toggle"
                    )
                }

                // Option 5: Sound Effects Toggle (صدا)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = if (isSoundEnabled) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                        label = "صدا",
                        contentDescription = stringResource(if (isSoundEnabled) R.string.sound_on else R.string.sound_off),
                        tint = if (isSoundEnabled) RadiantGold else Color.White.copy(alpha = 0.50f),
                        containerColor = Color(0xFF1C160D),
                        borderColor = if (isSoundEnabled) RadiantGold.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.18f),
                        onClick = onToggleSound,
                        testTag = "game_sound_toggle"
                    )
                }

                // Option 6: Change Mode / Regenerate Board (تغییر حالت)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.Shuffle,
                        label = "تغییر حالت",
                        contentDescription = stringResource(R.string.regenerate_board),
                        tint = HopefulEmerald,
                        containerColor = Color(0xFF1C160D),
                        borderColor = HopefulEmerald.copy(alpha = 0.65f),
                        onClick = onRegenerateBoard,
                        testTag = "regenerate_board_button"
                    )
                }

                // Option 7: Restart Game (شروع مجدد بازی)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.Refresh,
                        label = "شروع مجدد",
                        contentDescription = stringResource(R.string.restart_game),
                        tint = RadiantGold,
                        containerColor = Color(0xFF1C160D),
                        borderColor = RadiantGold.copy(alpha = 0.55f),
                        onClick = onRestartGame,
                        testTag = "restart_game_button"
                    )
                }

                // Option 8: Players / Setup (بازیکنان)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.Group,
                        label = "بازیکنان",
                        contentDescription = stringResource(R.string.new_setup),
                        tint = Color.White,
                        containerColor = Color(0xFF1C160D),
                        borderColor = Color.White.copy(alpha = 0.45f),
                        onClick = onBackToSetup,
                        testTag = "change_players_button"
                    )
                }

                // Option 9: Truth or Dare Cards Manager (کارت‌ها)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.Style,
                        label = "کارت‌ها",
                        contentDescription = "مدیریت کارت‌های جرئت یا حقیقت",
                        tint = RadiantGold,
                        containerColor = Color(0xFF1C160D),
                        borderColor = RadiantGold.copy(alpha = 0.45f),
                        onClick = onOpenCardManager,
                        testTag = "game_open_card_manager_button"
                    )
                }

                // Option 10: Hero Combat & Interaction Log (گزارش نبرد)
                item {
                    CollapsibleMenuRoundActionButton(
                        icon = Icons.Filled.SportsKabaddi,
                        label = "نبردها",
                        contentDescription = "گزارش نبردها و تعامل قهرمانان",
                        tint = DangerCrimson,
                        containerColor = Color(0xFF1C160D),
                        borderColor = DangerCrimson.copy(alpha = 0.65f),
                        onClick = onOpenCombatLog,
                        testTag = "game_open_combat_log_button"
                    )
                }

                // Compact Board Summary Pill inside the expanded drawer
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${difficulty.persianTitle} (${maxTiles.toPersianDigits()})",
                            style = MaterialTheme.typography.labelSmall,
                            color = difficulty.badgeColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🪜${ladderCount.toPersianDigits()} 🐍${snakeCount.toPersianDigits()} ⚡${obstacleCount.toPersianDigits()} 🃏${cardCount.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        }

        // 3. Compact Players Status Strip (Shown in normal state alongside the single Main Menu Button)
        if (!isMenuExpanded) {
            PlayersStatusStrip(
                players = players,
                currentTurnPlayerId = currentTurnPlayerId,
                leaderPlayerId = leaderPlayerId,
                isSinglePlayerMode = isSinglePlayerMode,
                onInspectHero = onInspectHero,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CollapsibleMenuRoundActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    contentDescription: String,
    tint: Color,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

/**
 * Interactive In-Game Theme Switcher Bar allowing instant switching between
 * Black & Gold (Default) and 4 other themes right inside the game screen.
 */
@Composable
private fun InGameThemeSwitcherBar(
    currentTheme: BoardTheme,
    currentSnakeTheme: SnakeVisualTheme,
    currentDiceTheme: DiceVisualTheme,
    isMusicEnabled: Boolean,
    isAmbientEnabled: Boolean,
    onSelectTheme: (BoardTheme) -> Unit,
    onSelectSnakeTheme: (SnakeVisualTheme) -> Unit,
    onSelectDiceTheme: (DiceVisualTheme) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleAmbient: () -> Unit,
    onOpenStudioSettings: () -> Unit = {}
) {
    val soundscape = currentTheme.soundscapeInfo()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, RadiantGold.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xF2120E08)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = RadiantGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "تغییر تم، مار، تاس سه‌بعدی و موسیقی داخل بازی:",
                    style = MaterialTheme.typography.labelMedium,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BoardTheme.entries) { theme ->
                    val isSelected = theme == currentTheme
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        theme.boardFrameOuterTop,
                                        theme.boardFrameOuterBottom
                                    )
                                )
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("ingame_theme_chip_${theme.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(text = theme.emoji, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = theme.persianName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) RadiantGold else Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Quick Snake Theme Switcher Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(SnakeVisualTheme.entries) { sTheme ->
                    val isSelected = sTheme == currentSnakeTheme
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) RadiantGold.copy(alpha = 0.22f)
                                else Color.White.copy(alpha = 0.07f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectSnakeTheme(sTheme) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("ingame_snake_theme_chip_${sTheme.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = sTheme.emoji, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "مار: ${sTheme.persianName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick 3D Dice Material Switcher Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(DiceVisualTheme.entries) { dTheme ->
                    val isSelected = dTheme == currentDiceTheme
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) HopefulEmerald.copy(alpha = 0.24f)
                                else Color.White.copy(alpha = 0.07f)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) HopefulEmerald else Color.White.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectDiceTheme(dTheme) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("ingame_dice_theme_chip_${dTheme.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = dTheme.emoji, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "تاس: ${dTheme.persianName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) HopefulEmerald else Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Live Soundscape & Ambient Mixer Row inside the Theme Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${soundscape.badgeEmoji} ${soundscape.musicTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "محیط: ${soundscape.ambientTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isMusicEnabled) HopefulEmerald.copy(alpha = 0.28f)
                                else Color.White.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (isMusicEnabled) HopefulEmerald else Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleMusic() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("ingame_mixer_music_toggle")
                    ) {
                        Text(
                            text = if (isMusicEnabled) "🎵 موسیقی" else "🔇 موسیقی",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMusicEnabled) HopefulEmerald else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isAmbientEnabled) RadiantGold.copy(alpha = 0.25f)
                                else Color.White.copy(alpha = 0.1f)
                            )
                            .border(
                                1.dp,
                                if (isAmbientEnabled) RadiantGold else Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleAmbient() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("ingame_mixer_ambient_toggle")
                    ) {
                        Text(
                            text = if (isAmbientEnabled) "🍃 محیط" else "🚫 محیط",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAmbientEnabled) RadiantGold else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(RadiantGold.copy(alpha = 0.22f))
                            .border(1.dp, RadiantGold, RoundedCornerShape(8.dp))
                            .clickable { onOpenStudioSettings() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("ingame_mixer_open_studio_button")
                    ) {
                        Text(
                            text = "✨ استودیو",
                            style = MaterialTheme.typography.labelSmall,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayersStatusStrip(
    players: List<Player>,
    currentTurnPlayerId: Int?,
    leaderPlayerId: Int?,
    isSinglePlayerMode: Boolean = false,
    onInspectHero: (GameCharacter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentTurnPlayerId, players.size) {
        val activeIndex = players.indexOfFirst { it.id == currentTurnPlayerId }
        if (activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 1.dp)
    ) {
        items(players, key = { it.id }) { player ->
            val isTurn = player.id == currentTurnPlayerId
            val isLeader = player.id == leaderPlayerId && player.currentTile > 1
            val character = player.character
            val tokenColor = player.color
            val readableNameColor = remember(tokenColor) {
                readableTokenTextColor(tokenColor)
            }

            val borderPrimary by animateColorAsState(
                targetValue = if (isTurn) tokenColor else RadiantGold.copy(alpha = 0.22f),
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "player_chip_border_primary_${player.id}"
            )
            val borderSecondary by animateColorAsState(
                targetValue = if (isTurn) RadiantGold.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.08f),
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "player_chip_border_sec_${player.id}"
            )
            val auraAlpha by animateFloatAsState(
                targetValue = if (isTurn) 0.22f else 0.0f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "player_chip_aura_${player.id}"
            )

            val turnStatusLabel = if (isTurn) {
                if (isSinglePlayerMode && !player.isAiBot && player.id == 0) {
                    "نوبت شما"
                } else {
                    "نوبت ${player.name}"
                }
            } else {
                "منتظر"
            }

            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .clickable { onInspectHero(character) }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                tokenColor.copy(alpha = auraAlpha),
                                if (isTurn) Color(0xFF1A1610) else Color(0xFF120F0A),
                                if (isTurn) Color(0xFF15120C) else Color(0xFF0F0C08)
                            )
                        )
                    )
                    .border(
                        width = if (isTurn) 1.5.dp else 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(borderPrimary, borderSecondary, borderPrimary)
                        ),
                        shape = RoundedCornerShape(11.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("player_turn_chip_${player.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Sleek Vertical Token-Color Accent Bar ONLY for the Active Turn Player
                if (isTurn) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .fillMaxHeight(0.82f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(tokenColor)
                    )
                }

                Box(contentAlignment = Alignment.BottomEnd) {
                    CharacterPortraitAvatar(
                        character = character,
                        size = 24.dp,
                        isSelected = isTurn,
                        tokenColorOverride = tokenColor
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0E0B07))
                            .border(0.8.dp, if (isTurn) RadiantGold else Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayerPawnTokenIcon(
                            tokenColor = tokenColor,
                            isTurnActive = isTurn,
                            size = 8.dp
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (isTurn) {
                            Box(
                                modifier = Modifier
                                    .size(5.5.dp)
                                    .clip(CircleShape)
                                    .background(tokenColor)
                                    .border(0.7.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                            )
                        }

                        Text(
                            text = if (player.isAiBot) "🤖 ${player.name}" else player.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isTurn) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isTurn) Color.White else Color.White.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "خانه ${player.currentTile.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTurn) RadiantGold else Color.White.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = turnStatusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isTurn) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isTurn) readableNameColor else Color.White.copy(alpha = 0.48f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "• ${character.displayBadgeEmoji}",
                            style = MaterialTheme.typography.labelSmall,
                            color = character.accentColor.copy(alpha = if (isTurn) 0.95f else 0.65f)
                        )
                    }
                }

                if (player.isShieldCharged) {
                    Text(
                        text = "🛡️",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (player.wormCurseTurnsRemaining > 0) {
                    Text(
                        text = "🪱${player.wormCurseTurnsRemaining.toPersianDigits()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA3E635)
                    )
                }
                if (player.swampSkipTurnsRemaining > 0 || player.halveNextRoll) {
                    Text(
                        text = "🌿",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (player.frozenSkipTurnsRemaining > 0) {
                    Text(
                        text = "❄️",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (isLeader) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = RadiantGold,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Slim Smart Turn & Event Status Bar (30.dp fixed height) below the board.
 */
@Composable
private fun EventFeedbackBanner(
    banner: GameEventBanner,
    currentPlayer: Player? = null,
    isDiceRolling: Boolean = false,
    isTokenAnimating: Boolean = false,
    sixRollGlowActive: Boolean = false,
    isSinglePlayerMode: Boolean = false
) {
    val activeTokenColor = currentPlayer?.color ?: RadiantGold
    val animatedTokenColor by animateColorAsState(
        targetValue = activeTokenColor,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "status_bar_token_color"
    )
    val readableNameColor = remember(animatedTokenColor) {
        readableTokenTextColor(animatedTokenColor)
    }

    val containerColor by animateColorAsState(
        targetValue = when (banner.type) {
            BannerType.SIX_BONUS -> Color(0xFF281C06)
            BannerType.LADDER_CLIMB -> Color(0xFF072D22)
            BannerType.SNAKE_BITE -> Color(0xFF330F18)
            BannerType.EXACT_NEEDED -> Color(0xFF221A0E)
            BannerType.OBSTACLE_DANGER -> Color(0xFF2D1609)
            BannerType.OBSTACLE_SURPRISE -> Color(0xFF082736)
            BannerType.STORM_CHAOS -> Color(0xFF2B074B)
            BannerType.CHARACTER_ABILITY -> Color(0xFF17153B)
            BannerType.NORMAL -> Color(0xFF13100A)
        },
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "banner_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when (banner.type) {
            BannerType.SIX_BONUS -> RadiantGold
            BannerType.LADDER_CLIMB -> HopefulEmerald
            BannerType.SNAKE_BITE -> DangerCrimson
            BannerType.EXACT_NEEDED -> RadiantAmber
            BannerType.OBSTACLE_DANGER -> Color(0xFFF97316)
            BannerType.OBSTACLE_SURPRISE -> Color(0xFF06B6D4)
            BannerType.STORM_CHAOS -> Color(0xFFA855F7)
            BannerType.CHARACTER_ABILITY -> Color(0xFF38BDF8)
            BannerType.NORMAL -> animatedTokenColor.copy(alpha = 0.78f)
        },
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "banner_border"
    )

    val smartStatusMessage = remember(
        banner,
        currentPlayer?.id,
        currentPlayer?.name,
        isDiceRolling,
        isTokenAnimating,
        sixRollGlowActive,
        isSinglePlayerMode
    ) {
        if (currentPlayer == null) {
            banner.message
        } else {
            val isHumanSelf = isSinglePlayerMode && !currentPlayer.isAiBot && currentPlayer.id == 0
            when {
                banner.type != BannerType.NORMAL -> banner.message
                isDiceRolling -> "نوبت ${currentPlayer.name} • در حال پرتاب تاس..."
                isTokenAnimating -> "حرکت مهره ${currentPlayer.name} روی صفحه..."
                sixRollGlowActive -> "جایزه ۶ طلایی! نوبت ${currentPlayer.name} برای پرتاب مجدد تاس"
                currentPlayer.isAiBot -> "نوبت ${currentPlayer.name} برای پرتاب تاس..."
                isHumanSelf -> "نوبت شما (${currentPlayer.name}) برای پرتاب تاس"
                else -> "نوبت ${currentPlayer.name} برای پرتاب تاس"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .border(
                width = 1.1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedTokenColor.copy(alpha = 0.90f),
                        borderColor,
                        RadiantGold.copy(alpha = 0.55f),
                        animatedTokenColor.copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .testTag("smart_turn_status_bar"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            animatedTokenColor.copy(alpha = 0.18f),
                            Color.Transparent,
                            animatedTokenColor.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Active Player Token Badge (Icon + Colored Player Name)
            if (currentPlayer != null) {
                AnimatedContent(
                    targetState = currentPlayer,
                    label = "status_bar_active_player"
                ) { activePlayer ->
                    val tokenCol = activePlayer.color
                    val nameCol = readableTokenTextColor(tokenCol)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFF0B0906).copy(alpha = 0.78f))
                            .border(
                                width = 1.dp,
                                color = tokenCol.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(7.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlayerPawnTokenIcon(
                            tokenColor = tokenCol,
                            isTurnActive = true,
                            size = 12.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(tokenCol)
                        )
                        Text(
                            text = activePlayer.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = nameCol,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            // Smart Turn Prompt / Event Message
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (banner.type == BannerType.SIX_BONUS ||
                    banner.type == BannerType.LADDER_CLIMB ||
                    banner.type == BannerType.OBSTACLE_SURPRISE ||
                    banner.type == BannerType.CHARACTER_ABILITY
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = RadiantGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                AnimatedContent(
                    targetState = smartStatusMessage,
                    label = "banner_text"
                ) { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (banner.type == BannerType.NORMAL) readableNameColor else Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
