package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.graphicsLayer
import com.example.R
import com.example.model.AbilityActivationMode
import com.example.model.AnimationQuality
import com.example.model.AnimationSpeed
import com.example.model.CharacterAbilityType
import com.example.model.DiceSurfacePattern
import com.example.model.DiceVisualTheme
import com.example.model.Player
import com.example.model.PrecisionRollChoice
import com.example.model.toPersianDigits
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.RadiantGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Strictly Fixed-Height Bottom Control Panel (124.dp).
 * Never changes height, never expands upward onto the board, and supports
 * Simultaneous Dual 3D Dice rolling & selection in-place!
 */
@Composable
fun DiceAndTurnPanel(
    currentPlayer: Player?,
    leaderPlayer: Player?,
    diceValue: Int,
    isDiceRolling: Boolean,
    isTokenAnimating: Boolean,
    diceRotationDeg: Float,
    sixRollGlowActive: Boolean,
    pendingPrecisionChoice: PrecisionRollChoice?,
    onRollDice: () -> Unit,
    onActivateAbility: () -> Unit,
    onSelectPrecisionOption: (Int) -> Unit,
    secondDiceValue: Int = 6,
    secondDiceRotationDeg: Float = 0f,
    diceRollTriggerId: Long = 0L,
    diceVisualTheme: DiceVisualTheme = DiceVisualTheme.OBSIDIAN_GOLD,
    animationQuality: AnimationQuality = AnimationQuality.CINEMATIC,
    animationSpeed: AnimationSpeed = AnimationSpeed.SMOOTH,
    isSinglePlayerMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isAiTurn = currentPlayer?.isAiBot == true
    val canRoll = !isDiceRolling && !isTokenAnimating && pendingPrecisionChoice == null && !isAiTurn
    val isDualDiceMode = (currentPlayer?.character?.abilityType == CharacterAbilityType.HIGH_PRECISION &&
        currentPlayer.isActiveAbilityArmedForTurn) || pendingPrecisionChoice != null

    val infiniteTransition = rememberInfiniteTransition(label = "dice_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val rawTokenColor = currentPlayer?.color ?: RadiantGold
    val animatedTokenColor by animateColorAsState(
        targetValue = rawTokenColor,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "active_player_token_color"
    )

    val activeBorderColor by animateColorAsState(
        targetValue = when {
            pendingPrecisionChoice != null -> RadiantGold
            sixRollGlowActive -> RadiantGold
            currentPlayer != null -> currentPlayer.color
            else -> RadiantAmber
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "border_color"
    )

    val readableNameColor = remember(animatedTokenColor) {
        readableTokenTextColor(animatedTokenColor)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp) // STRICTLY COMPACT FIXED HEIGHT — Maximizes board space & never shifts!
            .border(
                width = if (sixRollGlowActive || isDualDiceMode) 1.8.dp else 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        activeBorderColor.copy(alpha = 0.92f),
                        RadiantGold.copy(alpha = 0.72f),
                        Color(0xFF854D0E).copy(alpha = 0.65f),
                        activeBorderColor.copy(alpha = 0.88f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF110E09).copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            animatedTokenColor.copy(alpha = 0.16f),
                            Color(0xFF17130C),
                            Color(0xFF110D08),
                            Color(0xFF19140C)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sleek Vertical Token-Color Accent Bar on the Player Info Side
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(0.86f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                animatedTokenColor,
                                RadiantGold.copy(alpha = 0.85f),
                                animatedTokenColor
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Left/Main Column: Player Identity + Turn Indicator + Ability/Choice Controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPlayer != null) {
                    val character = currentPlayer.character
                    val isHumanSelfInSinglePlayer = isSinglePlayerMode && !currentPlayer.isAiBot && currentPlayer.id == 0
                    val turnTitleText = if (isHumanSelfInSinglePlayer) {
                        "نوبت شما (${currentPlayer.name})"
                    } else {
                        "نوبت ${currentPlayer.name}"
                    }

                    // Top Row: Avatar with Token Color Sync + Glowing Dot + Turn Title + Current Tile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            CharacterPortraitAvatar(
                                character = character,
                                size = 34.dp,
                                isSelected = true,
                                tokenColorOverride = currentPlayer.color,
                                modifier = Modifier.scale(if (canRoll) pulseGlow else 1f)
                            )
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0E0B07))
                                    .border(1.dp, RadiantGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                PlayerPawnTokenIcon(
                                    tokenColor = currentPlayer.color,
                                    isTurnActive = true,
                                    size = 11.dp
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Subtle Glowing Active Turn Dot in exact Token Color
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(animatedTokenColor)
                                        .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                )

                                AnimatedContent(
                                    targetState = turnTitleText,
                                    label = "active_turn_player_title",
                                    modifier = Modifier.weight(1f, fill = false)
                                ) { targetTitle ->
                                    Text(
                                        text = targetTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(animatedTokenColor.copy(alpha = 0.18f))
                                        .border(
                                            width = 1.dp,
                                            color = animatedTokenColor.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "خانه ${currentPlayer.currentTile.toPersianDigits()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = readableNameColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "مهره ${currentPlayer.colorName} • ${character.englishName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = readableNameColor.copy(alpha = 0.92f),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (leaderPlayer != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = null,
                                            tint = RadiantGold,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "${leaderPlayer.name} (${leaderPlayer.currentTile.toPersianDigits()})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFFE599),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Slot inside Fixed-Height Panel:
                    // Either shows the 3 Dual-Dice Selection Buttons (when 2 dice have landed)
                    // OR shows the Character Ability Control bar!
                    if (pendingPrecisionChoice != null) {
                        InPlacePrecisionChoiceRow(
                            choice = pendingPrecisionChoice,
                            onSelectOption = onSelectPrecisionOption
                        )
                    } else {
                        CharacterAbilityTurnControl(
                            player = currentPlayer,
                            canInteract = canRoll,
                            pulseScale = pulseGlow,
                            onActivateAbility = onActivateAbility
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Section: Unclipped 3D Dice Arena (1 Die or 2 Simultaneous 3D Dice) + Turn-Aware Roll Action Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isDualDiceMode) {
                    // SIMULTANEOUS DUAL 3D DICE ARENA (Unclipped top/bottom headroom so dice never clip during throw)
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF0A0805), RoundedCornerShape(14.dp))
                            .border(1.1.dp, animatedTokenColor.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 3.dp, vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val shownDie1 = pendingPrecisionChoice?.die1 ?: diceValue
                        val shownDie2 = pendingPrecisionChoice?.die2 ?: secondDiceValue

                        ThreeDimensionalDiceView(
                            diceValue = shownDie1,
                            isRolling = isDiceRolling,
                            rotationDeg = diceRotationDeg,
                            rollTriggerId = diceRollTriggerId,
                            sixGlow = sixRollGlowActive || shownDie1 == 6,
                            accentColor = RadiantGold,
                            diceVisualTheme = diceVisualTheme,
                            animationQuality = animationQuality,
                            animationSpeed = animationSpeed,
                            boxSize = 56.dp,
                            stageHeight = 84.dp,
                            dieIndex = 0,
                            onClick = {
                                if (pendingPrecisionChoice != null) {
                                    onSelectPrecisionOption(pendingPrecisionChoice.die1)
                                } else if (canRoll) {
                                    onRollDice()
                                }
                            },
                            testTag = "dice_3d_box"
                        )

                        ThreeDimensionalDiceView(
                            diceValue = shownDie2,
                            isRolling = isDiceRolling,
                            rotationDeg = secondDiceRotationDeg + 55f,
                            rollTriggerId = diceRollTriggerId,
                            sixGlow = sixRollGlowActive || shownDie2 == 6,
                            accentColor = Color(0xFF38BDF8),
                            diceVisualTheme = diceVisualTheme,
                            animationQuality = animationQuality,
                            animationSpeed = animationSpeed,
                            boxSize = 56.dp,
                            stageHeight = 84.dp,
                            dieIndex = 1,
                            onClick = {
                                if (pendingPrecisionChoice != null) {
                                    onSelectPrecisionOption(pendingPrecisionChoice.die2)
                                } else if (canRoll) {
                                    onRollDice()
                                }
                            },
                            testTag = "dice_3d_box_second"
                        )
                    }
                } else {
                    // SINGLE 3D LUXURY DIE WITH FULL VERTICAL HEADROOM (Never clipped at top/bottom/sides)
                    ThreeDimensionalDiceView(
                        diceValue = diceValue,
                        isRolling = isDiceRolling,
                        rotationDeg = diceRotationDeg,
                        rollTriggerId = diceRollTriggerId,
                        sixGlow = sixRollGlowActive,
                        accentColor = currentPlayer?.color ?: RadiantGold,
                        diceVisualTheme = diceVisualTheme,
                        animationQuality = animationQuality,
                        animationSpeed = animationSpeed,
                        boxSize = 74.dp,
                        stageHeight = 86.dp,
                        dieIndex = 0,
                        onClick = { if (canRoll) onRollDice() },
                        testTag = "dice_3d_box"
                    )
                }

                // Roll Action Button (or Sum Quick-Action when Dual Dice landed)
                if (pendingPrecisionChoice != null) {
                    Button(
                        onClick = { onSelectPrecisionOption(pendingPrecisionChoice.sum) },
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("roll_dice_button"),
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HopefulEmerald,
                            contentColor = Color(0xFF071A12)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "مجموع ۲ تاس",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${pendingPrecisionChoice.sum.toPersianDigits()} خانه ✨",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    val isHumanSelfInSinglePlayer = isSinglePlayerMode && currentPlayer != null && !currentPlayer.isAiBot && currentPlayer.id == 0
                    val turnSubLabel = when {
                        isAiTurn -> "منتظر نوبت"
                        isDiceRolling -> "در حال چرخش"
                        isTokenAnimating -> "در حال حرکت"
                        isHumanSelfInSinglePlayer -> "نوبت شما"
                        currentPlayer != null -> "نوبت ${currentPlayer.name}"
                        else -> ""
                    }

                    Button(
                        onClick = onRollDice,
                        enabled = canRoll,
                        modifier = Modifier
                            .height(46.dp)
                            .border(
                                width = if (canRoll) 1.5.dp else 1.dp,
                                color = if (canRoll) animatedTokenColor else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("roll_dice_button"),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                sixRollGlowActive -> HopefulEmerald
                                isDualDiceMode -> Color(0xFFEC4899)
                                currentPlayer?.isActiveAbilityArmedForTurn == true -> currentPlayer.character.accentColor
                                else -> RadiantGold
                            },
                            contentColor = Color(0xFF140B02),
                            disabledContainerColor = Color(0xFF1F1A12),
                            disabledContentColor = Color.White.copy(alpha = 0.60f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Casino,
                                    contentDescription = stringResource(R.string.roll_dice_button),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = when {
                                        isAiTurn -> "نوبت حریف"
                                        isDiceRolling && isDualDiceMode -> "چرخش ۲ تاس..."
                                        isDiceRolling -> "پرتاب..."
                                        isTokenAnimating -> "حرکت مهره..."
                                        isDualDiceMode -> "پرتاب ۲ تاس!"
                                        sixRollGlowActive -> "پرتاب جایزه!"
                                        else -> stringResource(R.string.roll_dice_button)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                            if (turnSubLabel.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(animatedTokenColor)
                                    )
                                    Text(
                                        text = turnSubLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact in-place selector for Die 1, Die 2, or Sum that fits inside the fixed 96.dp panel
 * without changing panel height by a single pixel!
 */
@Composable
private fun InPlacePrecisionChoiceRow(
    choice: PrecisionRollChoice,
    onSelectOption: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onSelectOption(choice.die1) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("precision_choice_die1"),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RadiantGold,
                contentColor = Color(0xFF171209)
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "🎲 اول: ${choice.die1.toPersianDigits()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }

        Button(
            onClick = { onSelectOption(choice.die2) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("precision_choice_die2"),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF38BDF8),
                contentColor = Color(0xFF082F49)
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "🎲 دوم: ${choice.die2.toPersianDigits()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }

        Button(
            onClick = { onSelectOption(choice.sum) },
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .testTag("precision_choice_sum"),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HopefulEmerald,
                contentColor = Color(0xFF052E16)
            ),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "مجموع: ${choice.sum.toPersianDigits()}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CharacterAbilityTurnControl(
    player: Player,
    canInteract: Boolean,
    pulseScale: Float,
    onActivateAbility: () -> Unit
) {
    val character = player.character
    val ability = character.abilityType
    val canActivateNow = player.canTriggerActiveAbilityNow && canInteract

    when {
        player.isActiveAbilityArmedForTurn -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(29.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(character.accentColor.copy(alpha = 0.25f))
                    .border(1.2.dp, RadiantGold, RoundedCornerShape(9.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = RadiantGold,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (ability == CharacterAbilityType.HIGH_PRECISION) {
                        "🎲🎲 ۲ تاس آماده پرتاب همزمان! روی دکمه پرتاب کلیک کنید"
                    } else {
                        "${character.displayBadgeEmoji} ${character.displayAbilityTitle}: آماده اجرا با پرتاب تاس!"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        canActivateNow -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(29.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                RadiantGold,
                                character.primaryColor,
                                character.accentColor
                            )
                        )
                    )
                    .border(1.2.dp, Color.White, RoundedCornerShape(9.dp))
                    .clickable { onActivateAbility() }
                    .padding(horizontal = 8.dp)
                    .testTag("activate_character_ability_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF140B24),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = when (ability) {
                        CharacterAbilityType.HIGH_PRECISION ->
                            "🎲🎲 فعال‌سازی پرتاب همزمان ۲ تاس (${character.displayAbilityTitle})"
                        CharacterAbilityType.SHADOW_WALKER ->
                            "🌀 جابه‌جایی با نفر اول! (${player.leaderSwapWindowTurnsRemaining.toPersianDigits()} نوبت فرصت باقی‌مانده)"
                        else ->
                            "فعال‌سازی ${character.displayAbilityTitle} ${character.displayBadgeEmoji}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF140B24),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        else -> {
            val statusLabel = player.buildAbilityStatusBadgeText()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                player.color.copy(alpha = 0.14f),
                                Color(0xFF1C170E),
                                Color(0xFF17130B)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            player.isShieldCharged || player.guardianAngelProtected ->
                                Color(0xFF38BDF8).copy(alpha = 0.78f)
                            player.dragonSoulBoostTurns > 0 || player.berserkerRageTurns > 0 ->
                                Color(0xFFF97316).copy(alpha = 0.80f)
                            else ->
                                player.color.copy(alpha = 0.45f)
                        },
                        shape = RoundedCornerShape(9.dp)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFEF3C7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class Vec3(val x: Float, val y: Float, val z: Float)

private data class CubeFaceDef(
    val faceValue: Int,
    val cornerIndices: IntArray,
    val normal: Vec3,
    val uAxis: Vec3,
    val vAxis: Vec3,
    val center: Vec3
)

/**
 * Finds the smooth target angle closest to (or slightly ahead of) [currentDeg]
 * so the 3D die decelerates seamlessly onto the landed face without any sudden angle jump.
 */
private fun smoothLandingTargetAngle(currentDeg: Float, baseTargetDeg: Float): Float {
    var normalized = (currentDeg - baseTargetDeg) % 360f
    if (normalized < 0f) normalized += 360f
    val nearestBase = currentDeg - normalized
    return if (normalized > 180f) nearestBase + 360f else nearestBase
}

/**
 * Ultra-smooth 60/90/120fps True 3D Rounded Luxury Dice Renderer (تاس سه‌بعدی با گوشه‌های کاملاً گرد).
 * Features:
 * - Unclipped 3D rotation stage with calibrated vertical & horizontal safety margins so the die NEVER clips at the top
 * - Continuous `withFrameNanos` 3D physics trajectory:
 *   1. Upward parabolic launch (`پرتاب به سمت بالا`)
 *   2. Multi-axis 3D tumbling around X, Y, Z with high initial speed & smooth quintic deceleration (`چرخش سه‌بعدی حول محورهای X, Y, Z`)
 *   3. Soft landing with 2 damped micro-bounces + subtle squash-and-stretch settle (`فرود نرم + جهش ظریف پس از فرود`)
 *   4. Crisp, unmistakable display of the exact landed face (`1..6`)
 * - Zero per-frame Path allocations for butter-smooth frame rates
 */
@Composable
fun ThreeDimensionalDiceView(
    diceValue: Int,
    isRolling: Boolean,
    rotationDeg: Float,
    sixGlow: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    rollTriggerId: Long = 0L,
    diceVisualTheme: DiceVisualTheme = DiceVisualTheme.OBSIDIAN_GOLD,
    animationQuality: AnimationQuality = AnimationQuality.CINEMATIC,
    animationSpeed: AnimationSpeed = AnimationSpeed.SMOOTH,
    boxSize: Dp = 74.dp,
    stageHeight: Dp = boxSize,
    dieIndex: Int = 0,
    testTag: String = "dice_3d_box",
    modifier: Modifier = Modifier
) {
    val targetAngles = remember(diceValue) {
        when (diceValue.coerceIn(1, 6)) {
            1 -> Triple(-15f, 18f, -3f)
            2 -> Triple(-15f, -162f, 3f)
            3 -> Triple(74f, 15f, 9f)
            4 -> Triple(-106f, -15f, -9f)
            5 -> Triple(-15f, -72f, -7f)
            else -> Triple(-15f, 108f, 7f)
        }
    }

    var currentRotX by remember { mutableFloatStateOf(targetAngles.first) }
    var currentRotY by remember { mutableFloatStateOf(targetAngles.second) }
    var currentRotZ by remember { mutableFloatStateOf(targetAngles.third) }
    var currentAirLift by remember { mutableFloatStateOf(0f) }
    var currentScaleX by remember { mutableFloatStateOf(1f) }
    var currentScaleY by remember { mutableFloatStateOf(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "dice_3d_idle")
    val idleWobble by infiniteTransition.animateFloat(
        initialValue = -3.4f,
        targetValue = 3.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_wobble"
    )

    // Continuous 60/90/120 FPS 3D Physics Trajectory driven by withFrameNanos
    LaunchedEffect(rollTriggerId, isRolling, diceValue) {
        if (isRolling || rollTriggerId > 0L) {
            val startX = currentRotX
            val startY = currentRotY
            val startZ = currentRotZ

            // Multiple full 360-degree 3D revolutions around X, Y, Z landing strictly on targetAngles
            val alignedBaseX = smoothLandingTargetAngle(startX, targetAngles.first)
            val alignedBaseY = smoothLandingTargetAngle(startY, targetAngles.second)
            val alignedBaseZ = smoothLandingTargetAngle(startZ, targetAngles.third)

            val spinsX = if (dieIndex == 0) 3 else 4
            val spinsY = if (dieIndex == 0) 4 else 3
            val spinsZ = 2

            val endX = alignedBaseX + spinsX * 360f
            val endY = alignedBaseY + spinsY * 360f * (if (dieIndex % 2 == 0) 1f else -1f)
            val endZ = alignedBaseZ + spinsZ * 360f * (if (dieIndex % 2 == 0) 1f else -1f)

            // Total trajectory includes flight phase + post-landing micro-bounce settle
            val flightDurationMs = animationSpeed.diceRollDurationMs.coerceAtLeast(450L)
            val settleDurationMs = (190L * animationSpeed.durationMultiplier).toLong().coerceIn(130L, 260L)
            val totalDurationNs = (flightDurationMs + settleDurationMs) * 1_000_000L
            val flightFraction = flightDurationMs.toFloat() / (flightDurationMs + settleDurationMs).toFloat()

            val startTimeNs = withFrameNanos { it }
            while (true) {
                val nowNs = withFrameNanos { it }
                val elapsedNs = (nowNs - startTimeNs).coerceAtLeast(0L)
                val p = (elapsedNs.toFloat() / totalDurationNs.toFloat()).coerceIn(0f, 1f)

                if (p < flightFraction) {
                    // Phase 1: Upward Launch + Thrilling Multi-Axis 3D Mid-Air Tumbling + Smooth Deceleration
                    val u = (p / flightFraction).coerceIn(0f, 1f)

                    // Parabolic launch curve: fast upward throw reaching peak airLift = 1.0, then accelerating down under gravity
                    val parabola = 4f * u * (1f - u)
                    val launchSkew = 1f - 0.16f * u
                    currentAirLift = (parabola * launchSkew).coerceIn(0f, 1f)

                    // Smooth rotational ease-out with high initial angular velocity and organic 3-axis tumbling
                    val easeX = 1f - (1f - u).pow(2.35f)
                    val easeY = 1f - (1f - u).pow(2.55f)
                    val easeZ = 1f - (1f - u).pow(2.15f)

                    currentRotX = startX + (endX - startX) * easeX
                    currentRotY = startY + (endY - startY) * easeY
                    currentRotZ = startZ + (endZ - startZ) * easeZ

                    // Subtle aerodynamic stretch at launch
                    val launchStretch = sin(u * PI.toFloat()) * 0.035f
                    currentScaleX = 1f - launchStretch * 0.5f
                    currentScaleY = 1f + launchStretch
                } else {
                    // Phase 2: Touchdown + 2 Damped Micro-Bounces + Squash & Settle Wobble
                    val v = ((p - flightFraction) / (1f - flightFraction).coerceAtLeast(0.01f)).coerceIn(0f, 1f)

                    val bounceHeight = if (v < 0.58f) {
                        val b1 = v / 0.58f
                        0.22f * 4f * b1 * (1f - b1)
                    } else {
                        val b2 = (v - 0.58f) / 0.42f
                        0.065f * 4f * b2 * (1f - b2)
                    }
                    currentAirLift = bounceHeight

                    // Damped rotational micro-wobble around the landed face
                    val damping = (1f - v) * (1f - v)
                    val wobbleWave = sin(v * 2.5f * PI.toFloat()) * damping
                    currentRotX = targetAngles.first + wobbleWave * 7.5f
                    currentRotY = targetAngles.second - wobbleWave * 5.5f
                    currentRotZ = targetAngles.third + wobbleWave * 4.0f

                    // Subtle impact squash on first touchdown and second micro-touchdown
                    val squashPulse = cos(v * 2.2f * PI.toFloat()).coerceIn(0f, 1f) * damping * 0.055f
                    currentScaleX = 1f + squashPulse
                    currentScaleY = 1f - squashPulse * 0.85f
                }

                if (p >= 1f && !isRolling) {
                    break
                }
            }

            currentRotX = targetAngles.first
            currentRotY = targetAngles.second
            currentRotZ = targetAngles.third
            currentAirLift = 0f
            currentScaleX = 1f
            currentScaleY = 1f
        } else {
            currentRotX = targetAngles.first
            currentRotY = targetAngles.second
            currentRotZ = targetAngles.third
            currentAirLift = 0f
            currentScaleX = 1f
            currentScaleY = 1f
        }
    }

    val cubeFaces = remember {
        listOf(
            CubeFaceDef(
                faceValue = 1,
                cornerIndices = intArrayOf(0, 1, 2, 3),
                normal = Vec3(0f, 0f, 1f),
                uAxis = Vec3(1f, 0f, 0f),
                vAxis = Vec3(0f, 1f, 0f),
                center = Vec3(0f, 0f, 1f)
            ),
            CubeFaceDef(
                faceValue = 2,
                cornerIndices = intArrayOf(5, 4, 7, 6),
                normal = Vec3(0f, 0f, -1f),
                uAxis = Vec3(-1f, 0f, 0f),
                vAxis = Vec3(0f, 1f, 0f),
                center = Vec3(0f, 0f, -1f)
            ),
            CubeFaceDef(
                faceValue = 3,
                cornerIndices = intArrayOf(3, 2, 6, 7),
                normal = Vec3(0f, 1f, 0f),
                uAxis = Vec3(1f, 0f, 0f),
                vAxis = Vec3(0f, 0f, -1f),
                center = Vec3(0f, 1f, 0f)
            ),
            CubeFaceDef(
                faceValue = 4,
                cornerIndices = intArrayOf(4, 5, 1, 0),
                normal = Vec3(0f, -1f, 0f),
                uAxis = Vec3(1f, 0f, 0f),
                vAxis = Vec3(0f, 0f, 1f),
                center = Vec3(0f, -1f, 0f)
            ),
            CubeFaceDef(
                faceValue = 5,
                cornerIndices = intArrayOf(1, 5, 6, 2),
                normal = Vec3(1f, 0f, 0f),
                uAxis = Vec3(0f, 0f, -1f),
                vAxis = Vec3(0f, 1f, 0f),
                center = Vec3(1f, 0f, 0f)
            ),
            CubeFaceDef(
                faceValue = 6,
                cornerIndices = intArrayOf(4, 0, 3, 7),
                normal = Vec3(-1f, 0f, 0f),
                uAxis = Vec3(0f, 0f, 1f),
                vAxis = Vec3(0f, 1f, 0f),
                center = Vec3(-1f, 0f, 0f)
            )
        )
    }

    // Pre-allocated Path objects to avoid GC pressure during 60/90/120 FPS animation frames
    val reusableOuterPath = remember { Path() }
    val reusableInnerPath = remember { Path() }
    val reusableHairlinePath = remember { Path() }

    // Unclipped 3D Rotation Stage: NO .clip() so the die never gets sliced at the top during high throws!
    Box(
        modifier = modifier
            .size(width = boxSize, height = stageHeight)
            .clickable(onClick = onClick)
            .graphicsLayer {
                scaleX = currentScaleX
                scaleY = currentScaleY
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minDim = minOf(w, h)
            val airLift = currentAirLift.coerceIn(0f, 1.05f)
            val bevelSteps = animationQuality.diceBevelSteps

            // 1. Realistic Multi-Layered Soft Contact Shadow & Ambient Glow Underneath the Rounded Die
            val shadowCenterY = h * 0.84f + airLift * h * 0.025f
            val shadowRadiusX = minDim * (0.34f - airLift * 0.095f).coerceAtLeast(0.16f)
            val shadowRadiusY = h * (0.105f - airLift * 0.032f).coerceAtLeast(0.048f)

            val isNeon = diceVisualTheme.surfacePattern == DiceSurfacePattern.NEON_GRID
            val isObsidianLuxury = diceVisualTheme.surfacePattern == DiceSurfacePattern.OBSIDIAN_LUXURY

            // Outer ambient golden/silver/neon aura on the floor
            val ambientShadowColor = when {
                sixGlow || isRolling -> diceVisualTheme.ambientGlowColor.copy(alpha = 0.62f)
                isNeon -> diceVisualTheme.ambientGlowColor.copy(alpha = 0.55f - airLift * 0.14f)
                isObsidianLuxury -> Color(0xFFFFD166).copy(alpha = (0.28f - airLift * 0.08f).coerceAtLeast(0.08f))
                else -> diceVisualTheme.ambientGlowColor.copy(alpha = (0.24f - airLift * 0.08f).coerceAtLeast(0.06f))
            }
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ambientShadowColor,
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, shadowCenterY),
                    radius = shadowRadiusX * 1.48f
                ),
                topLeft = Offset(w * 0.5f - shadowRadiusX * 1.25f, shadowCenterY - shadowRadiusY * 1.25f),
                size = androidx.compose.ui.geometry.Size(shadowRadiusX * 2.5f, shadowRadiusY * 2.5f)
            )

            // Deep natural contact shadow directly under the rounded die
            val coreShadowAlpha = (0.76f - airLift * 0.30f).coerceIn(0.22f, 0.85f)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = coreShadowAlpha),
                        Color.Black.copy(alpha = coreShadowAlpha * 0.48f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, shadowCenterY),
                    radius = shadowRadiusX * 1.1f
                ),
                topLeft = Offset(w * 0.5f - shadowRadiusX, shadowCenterY - shadowRadiusY),
                size = androidx.compose.ui.geometry.Size(shadowRadiusX * 2f, shadowRadiusY * 2f)
            )

            // 2. 3D Rotation & Perspective Projection Setup
            val activeIdle = if (!isRolling && airLift < 0.01f) idleWobble else 0f
            val rx = Math.toRadians((currentRotX + activeIdle * 0.55f).toDouble()).toFloat()
            val ry = Math.toRadians((currentRotY + activeIdle * 0.85f).toDouble()).toFloat()
            val rz = Math.toRadians(currentRotZ.toDouble()).toFloat()
            val cosX = cos(rx)
            val sinX = sin(rx)
            val cosY = cos(ry)
            val sinY = sin(ry)
            val cosZ = cos(rz)
            val sinZ = sin(rz)

            fun rotate3D(v: Vec3): Vec3 {
                val y1 = v.y * cosX - v.z * sinX
                val z1 = v.y * sinX + v.z * cosX
                val x1 = v.x
                val x2 = x1 * cosY + z1 * sinY
                val z2 = -x1 * sinY + z1 * cosY
                val y2 = y1
                val x3 = x2 * cosZ - y2 * sinZ
                val y3 = x2 * sinZ + y2 * cosZ
                val z3 = z2
                return Vec3(x3, y3, z3)
            }

            val cameraDist = 4.6f
            // Calibrated scale & vertical trajectory so that even at peak airLift = 1.0 and 45° diagonal 3D rotation,
            // the top-most corner has generous headroom and is NEVER cut off!
            val cubeScale = minDim * (0.215f + airLift * 0.020f)
            val cubeCenterScreenX = w * 0.50f
            val cubeCenterScreenY = h * 0.555f - airLift * (h * 0.125f)

            fun projectToScreen(v: Vec3): Offset {
                val perspective = cameraDist / (cameraDist - v.z).coerceAtLeast(1.3f)
                return Offset(
                    x = cubeCenterScreenX + v.x * cubeScale * perspective,
                    y = cubeCenterScreenY - v.y * cubeScale * perspective
                )
            }

            val lightDir = Vec3(-0.42f, 0.66f, 0.72f)

            val visibleFaces = cubeFaces.mapNotNull { face ->
                val rotNormal = rotate3D(face.normal)
                val rotCenter = rotate3D(face.center)
                val viewVecZ = cameraDist - rotCenter.z
                val dotView = rotNormal.x * (-rotCenter.x) +
                    rotNormal.y * (-rotCenter.y) +
                    rotNormal.z * viewVecZ
                if (dotView <= 0.02f) {
                    null
                } else {
                    Triple(face, rotNormal, rotCenter.z)
                }
            }.sortedBy { it.third }

            // 3. Volumetric Rounded 3D Core Cushion (fills any corner seam with smooth rounded Black/Gold/Silver body)
            val coreBodyRadius = cubeScale * (cameraDist / (cameraDist - 0.15f)) * 1.18f
            val silverHighlight = Color(0xFFE2E8F0)
            val goldHighlight = diceVisualTheme.edgeOuterColor
            val coreObsidian = diceVisualTheme.faceBottomColor

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        silverHighlight.copy(alpha = 0.85f),
                        goldHighlight.copy(alpha = 0.78f),
                        coreObsidian,
                        Color(0xFF05070B)
                    ),
                    center = Offset(
                        cubeCenterScreenX - coreBodyRadius * 0.28f,
                        cubeCenterScreenY - coreBodyRadius * 0.28f
                    ),
                    radius = coreBodyRadius * 1.32f
                ),
                radius = coreBodyRadius,
                center = Offset(cubeCenterScreenX, cubeCenterScreenY)
            )

            // 4. First Pass: Outer 3D Rounded Chamfer Shell for all visible faces
            // Creates the smooth, pillowed, curved 3D outer body of a real mobile rounded die
            for ((face, rotNormal, _) in visibleFaces) {
                val diffuse = (rotNormal.x * lightDir.x +
                    rotNormal.y * lightDir.y +
                    rotNormal.z * lightDir.z).coerceIn(-0.35f, 1f)
                val brightness = (0.74f + 0.26f * diffuse).coerceIn(0.48f, 1.06f)

                val outerChamferPath = buildRoundedFacePath3D(
                    targetPath = reusableOuterPath,
                    face = face,
                    halfExtent = 0.96f,
                    cornerRadius = 0.42f,
                    spherizeAmount = 0.26f,
                    normalOffset = -0.02f,
                    stepsPerCorner = bevelSteps,
                    rotate3D = ::rotate3D,
                    projectToScreen = ::projectToScreen
                )

                val topLeftChamfer = projectToScreen(
                    rotate3D(
                        Vec3(
                            face.center.x - face.uAxis.x * 0.9f + face.vAxis.x * 0.9f,
                            face.center.y - face.uAxis.y * 0.9f + face.vAxis.y * 0.9f,
                            face.center.z - face.uAxis.z * 0.9f + face.vAxis.z * 0.9f
                        )
                    )
                )
                val bottomRightChamfer = projectToScreen(
                    rotate3D(
                        Vec3(
                            face.center.x + face.uAxis.x * 0.9f - face.vAxis.x * 0.9f,
                            face.center.y + face.uAxis.y * 0.9f - face.vAxis.y * 0.9f,
                            face.center.z + face.uAxis.z * 0.9f - face.vAxis.z * 0.9f
                        )
                    )
                )

                // Smooth rounded bevel body combining Obsidian Black, Gold, and Brushed Silver
                val chamferSilver = diceVisualTheme.edgeInnerColor.copy(
                    red = (diceVisualTheme.edgeInnerColor.red * brightness).coerceIn(0f, 1f),
                    green = (diceVisualTheme.edgeInnerColor.green * brightness).coerceIn(0f, 1f),
                    blue = (diceVisualTheme.edgeInnerColor.blue * brightness).coerceIn(0f, 1f)
                )
                val chamferGold = diceVisualTheme.edgeOuterColor.copy(
                    red = (diceVisualTheme.edgeOuterColor.red * brightness).coerceIn(0f, 1f),
                    green = (diceVisualTheme.edgeOuterColor.green * brightness).coerceIn(0f, 1f),
                    blue = (diceVisualTheme.edgeOuterColor.blue * brightness).coerceIn(0f, 1f)
                )
                val chamferDark = Color(
                    red = (diceVisualTheme.faceBottomColor.red * 0.85f * brightness).coerceIn(0f, 1f),
                    green = (diceVisualTheme.faceBottomColor.green * 0.85f * brightness).coerceIn(0f, 1f),
                    blue = (diceVisualTheme.faceBottomColor.blue * 0.85f * brightness).coerceIn(0f, 1f)
                )

                val chamferBrush = Brush.linearGradient(
                    colors = listOf(
                        chamferSilver,
                        chamferGold,
                        chamferDark,
                        chamferGold.copy(alpha = 0.72f)
                    ),
                    start = topLeftChamfer,
                    end = bottomRightChamfer
                )

                drawPath(
                    path = outerChamferPath,
                    brush = chamferBrush
                )
                // Soft rounded stroke to make the outer silhouette extra plush and rounded
                drawPath(
                    path = outerChamferPath,
                    brush = chamferBrush,
                    style = Stroke(
                        width = minDim * 0.042f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 5. Second Pass: Glossy Rounded Face Cushions, Metallic Bevel Rings, Specular Light & 3D Pips
            for ((face, rotNormal, _) in visibleFaces) {
                val diffuse = (rotNormal.x * lightDir.x +
                    rotNormal.y * lightDir.y +
                    rotNormal.z * lightDir.z).coerceIn(-0.35f, 1f)
                val brightness = (0.75f + 0.25f * diffuse).coerceIn(0.48f, 1.06f)

                val baseTop = diceVisualTheme.faceTopColor
                val baseBottom = diceVisualTheme.faceBottomColor

                val faceTopColor = if (sixGlow && diceVisualTheme.surfacePattern != DiceSurfacePattern.MARBLE_VEINS) {
                    Color(
                        red = (baseTop.red * 0.62f + 0.38f * brightness).coerceIn(0f, 1f),
                        green = (baseTop.green * 0.62f + 0.32f * brightness).coerceIn(0f, 1f),
                        blue = (baseTop.blue * 0.62f + 0.16f * brightness).coerceIn(0f, 1f)
                    )
                } else {
                    Color(
                        red = (baseTop.red * brightness).coerceIn(0f, 1f),
                        green = (baseTop.green * brightness).coerceIn(0f, 1f),
                        blue = (baseTop.blue * brightness).coerceIn(0f, 1f)
                    )
                }

                val faceBottomColor = if (sixGlow && diceVisualTheme.surfacePattern != DiceSurfacePattern.MARBLE_VEINS) {
                    Color(
                        red = (baseBottom.red * 0.68f + 0.28f * brightness).coerceIn(0f, 1f),
                        green = (baseBottom.green * 0.68f + 0.22f * brightness).coerceIn(0f, 1f),
                        blue = (baseBottom.blue * 0.68f + 0.08f * brightness).coerceIn(0f, 1f)
                    )
                } else {
                    Color(
                        red = (baseBottom.red * brightness).coerceIn(0f, 1f),
                        green = (baseBottom.green * brightness).coerceIn(0f, 1f),
                        blue = (baseBottom.blue * brightness).coerceIn(0f, 1f)
                    )
                }

                // Rounded 3D face cushion path (squircle with smooth 3D arcs on all 4 corners)
                val innerFacePath = buildRoundedFacePath3D(
                    targetPath = reusableInnerPath,
                    face = face,
                    halfExtent = 0.81f,
                    cornerRadius = 0.29f,
                    spherizeAmount = 0.12f,
                    normalOffset = 0.015f,
                    stepsPerCorner = bevelSteps,
                    rotate3D = ::rotate3D,
                    projectToScreen = ::projectToScreen
                )

                val faceTopLeft = projectToScreen(
                    rotate3D(
                        Vec3(
                            face.center.x - face.uAxis.x * 0.8f + face.vAxis.x * 0.8f,
                            face.center.y - face.uAxis.y * 0.8f + face.vAxis.y * 0.8f,
                            face.center.z - face.uAxis.z * 0.8f + face.vAxis.z * 0.8f
                        )
                    )
                )
                val faceBottomRight = projectToScreen(
                    rotate3D(
                        Vec3(
                            face.center.x + face.uAxis.x * 0.8f - face.vAxis.x * 0.8f,
                            face.center.y + face.uAxis.y * 0.8f - face.vAxis.y * 0.8f,
                            face.center.z + face.uAxis.z * 0.8f - face.vAxis.z * 0.8f
                        )
                    )
                )

                // Fill the rounded 3D face cushion
                drawPath(
                    path = innerFacePath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            faceTopColor,
                            Color(
                                red = (faceTopColor.red * 0.45f + faceBottomColor.red * 0.55f),
                                green = (faceTopColor.green * 0.45f + faceBottomColor.green * 0.55f),
                                blue = (faceTopColor.blue * 0.45f + faceBottomColor.blue * 0.55f)
                            ),
                            faceBottomColor
                        ),
                        start = faceTopLeft,
                        end = faceBottomRight
                    )
                )

                // Clip surface patterns and glossy reflection strictly inside the rounded face cushion
                clipPath(innerFacePath) {
                    drawFaceMaterialPattern(
                        face = face,
                        diceVisualTheme = diceVisualTheme,
                        rotNormalZ = rotNormal.z,
                        brightness = brightness,
                        rotate3D = ::rotate3D,
                        projectToScreen = ::projectToScreen
                    )

                    // Curved Glossy Specular Reflection across the upper-left of the rounded face
                    if (rotNormal.z > 0.25f) {
                        val specIntensity = when (diceVisualTheme.surfacePattern) {
                            DiceSurfacePattern.OBSIDIAN_LUXURY -> 0.34f * rotNormal.z
                            DiceSurfacePattern.BRUSHED_METAL -> 0.40f * rotNormal.z
                            DiceSurfacePattern.CRYSTAL_FROST -> 0.38f * rotNormal.z
                            DiceSurfacePattern.MARBLE_VEINS -> 0.30f * rotNormal.z
                            DiceSurfacePattern.WOOD_GRAIN -> 0.18f * rotNormal.z
                            DiceSurfacePattern.NEON_GRID -> 0.26f * rotNormal.z
                        }
                        val glossCenter = Offset(
                            x = faceTopLeft.x * 0.65f + faceBottomRight.x * 0.35f,
                            y = faceTopLeft.y * 0.68f + faceBottomRight.y * 0.32f
                        )
                        val glossRadius = hypot(
                            faceBottomRight.x - faceTopLeft.x,
                            faceBottomRight.y - faceTopLeft.y
                        ).coerceAtLeast(12f) * 0.58f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    diceVisualTheme.specularColor.copy(alpha = specIntensity.coerceIn(0f, 0.55f)),
                                    diceVisualTheme.edgeInnerColor.copy(alpha = (specIntensity * 0.35f).coerceIn(0f, 0.25f)),
                                    Color.Transparent
                                ),
                                center = glossCenter,
                                radius = glossRadius
                            ),
                            radius = glossRadius,
                            center = glossCenter
                        )
                    }
                }

                // Dual-Tone Curved Metallic Bevel Rim (Silver + 24K Gold + Obsidian groove)
                val bevelRimBrush = Brush.linearGradient(
                    colors = if (sixGlow) {
                        listOf(
                            Color.White,
                            diceVisualTheme.edgeOuterColor,
                            diceVisualTheme.edgeInnerColor
                        )
                    } else {
                        listOf(
                            diceVisualTheme.edgeInnerColor.copy(alpha = (0.92f * brightness).coerceIn(0.45f, 1f)),
                            diceVisualTheme.edgeOuterColor.copy(alpha = (0.95f * brightness).coerceIn(0.50f, 1f)),
                            Color(0xFF090B10).copy(alpha = 0.75f),
                            diceVisualTheme.edgeOuterColor.copy(alpha = (0.80f * brightness).coerceIn(0.40f, 1f))
                        )
                    },
                    start = faceTopLeft,
                    end = faceBottomRight
                )

                drawPath(
                    path = innerFacePath,
                    brush = bevelRimBrush,
                    style = Stroke(
                        width = minDim * (if (isNeon) 0.024f else 0.020f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Subtle inner silver/gold hairline accent inside the bevel
                val innerHairlinePath = buildRoundedFacePath3D(
                    targetPath = reusableHairlinePath,
                    face = face,
                    halfExtent = 0.74f,
                    cornerRadius = 0.24f,
                    spherizeAmount = 0.10f,
                    normalOffset = 0.018f,
                    stepsPerCorner = bevelSteps,
                    rotate3D = ::rotate3D,
                    projectToScreen = ::projectToScreen
                )
                drawPath(
                    path = innerHairlinePath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            diceVisualTheme.edgeOuterColor.copy(alpha = 0.42f * brightness),
                            diceVisualTheme.edgeInnerColor.copy(alpha = 0.22f * brightness),
                            diceVisualTheme.edgeOuterColor.copy(alpha = 0.35f * brightness)
                        ),
                        start = faceTopLeft,
                        end = faceBottomRight
                    ),
                    style = Stroke(
                        width = minDim * 0.008f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 6. Crisp 3D-Embossed Circular Pips (1..6)
                drawProjectedFacePips(
                    face = face,
                    diceVisualTheme = diceVisualTheme,
                    rotNormalZ = rotNormal.z,
                    brightness = brightness,
                    sixGlow = sixGlow,
                    rotate3D = ::rotate3D,
                    projectToScreen = ::projectToScreen
                )
            }
        }
    }
}

/**
 * Constructs a smooth 3D-projected rounded square (squircle) path on [face],
 * sampling circular corner arcs in the face's (u, v) plane and optionally blending
 * with a 3D sphere vector ([spherizeAmount]) so the corners and edges curve naturally in 3D space.
 */
private fun buildRoundedFacePath3D(
    targetPath: Path,
    face: CubeFaceDef,
    halfExtent: Float,
    cornerRadius: Float,
    spherizeAmount: Float,
    normalOffset: Float,
    stepsPerCorner: Int = 6,
    rotate3D: (Vec3) -> Vec3,
    projectToScreen: (Vec3) -> Offset
): Path {
    targetPath.reset()
    val r = cornerRadius.coerceIn(0.05f, halfExtent * 0.92f)
    val inner = halfExtent - r
    val cornerCenters = arrayOf(
        Pair(inner, inner),   // Top-Right (0 .. PI/2)
        Pair(-inner, inner),  // Top-Left (PI/2 .. PI)
        Pair(-inner, -inner), // Bottom-Left (PI .. 3*PI/2)
        Pair(inner, -inner)   // Bottom-Right (3*PI/2 .. 2*PI)
    )
    val safeSteps = stepsPerCorner.coerceIn(3, 8)
    var isFirst = true

    for (cIdx in 0 until 4) {
        val (cu, cv) = cornerCenters[cIdx]
        val startAngle = cIdx * (PI.toFloat() * 0.5f)
        for (step in 0..safeSteps) {
            val theta = startAngle + (step.toFloat() / safeSteps.toFloat()) * (PI.toFloat() * 0.5f)
            val u = cu + r * cos(theta)
            val v = cv + r * sin(theta)

            val rawX = face.center.x * (1f + normalOffset) + face.uAxis.x * u + face.vAxis.x * v
            val rawY = face.center.y * (1f + normalOffset) + face.uAxis.y * u + face.vAxis.y * v
            val rawZ = face.center.z * (1f + normalOffset) + face.uAxis.z * u + face.vAxis.z * v

            val len = sqrt(rawX * rawX + rawY * rawY + rawZ * rawZ).coerceAtLeast(0.001f)
            val sphereRadius = 1.22f
            val sx = (rawX / len) * sphereRadius
            val sy = (rawY / len) * sphereRadius
            val sz = (rawZ / len) * sphereRadius

            val p3d = Vec3(
                x = rawX * (1f - spherizeAmount) + sx * spherizeAmount,
                y = rawY * (1f - spherizeAmount) + sy * spherizeAmount,
                z = rawZ * (1f - spherizeAmount) + sz * spherizeAmount
            )
            val pt2d = projectToScreen(rotate3D(p3d))
            if (isFirst) {
                targetPath.moveTo(pt2d.x, pt2d.y)
                isFirst = false
            } else {
                targetPath.lineTo(pt2d.x, pt2d.y)
            }
        }
    }
    targetPath.close()
    return targetPath
}

/**
 * Draws authentic 3D-projected surface textures directly inside each rounded cube face
 * so the luxury sheen, wood grain, marble veins, neon circuitry, metallic brush, or crystal facets
 * rotate naturally in 3D perspective.
 */
private fun DrawScope.drawFaceMaterialPattern(
    face: CubeFaceDef,
    diceVisualTheme: DiceVisualTheme,
    rotNormalZ: Float,
    brightness: Float,
    rotate3D: (Vec3) -> Vec3,
    projectToScreen: (Vec3) -> Offset
) {
    fun facePoint2D(u: Float, v: Float): Offset {
        val p3d = Vec3(
            x = face.center.x * 1.02f + face.uAxis.x * u + face.vAxis.x * v,
            y = face.center.y * 1.02f + face.uAxis.y * u + face.vAxis.y * v,
            z = face.center.z * 1.02f + face.uAxis.z * u + face.vAxis.z * v
        )
        return projectToScreen(rotate3D(p3d))
    }

    val strokeUnit = size.width * 0.011f
    val visibility = rotNormalZ.coerceIn(0.25f, 1f)

    when (diceVisualTheme.surfacePattern) {
        DiceSurfacePattern.WOOD_GRAIN -> {
            val grainDark = Color(0xFF2B1706).copy(alpha = 0.38f * visibility)
            val grainLight = Color(0xFFD99B66).copy(alpha = 0.22f * visibility)
            val bands = listOf(-0.54f, -0.22f, 0.12f, 0.44f)
            for ((idx, vBand) in bands.withIndex()) {
                val pStart = facePoint2D(-0.72f, vBand)
                val pMid = facePoint2D(0.0f, (vBand + if (idx % 2 == 0) 0.08f else -0.08f).coerceIn(-0.72f, 0.72f))
                val pEnd = facePoint2D(0.72f, vBand)
                val grainPath = Path().apply {
                    moveTo(pStart.x, pStart.y)
                    quadraticTo(pMid.x, pMid.y, pEnd.x, pEnd.y)
                }
                drawPath(
                    path = grainPath,
                    color = if (idx % 2 == 0) grainDark else grainLight,
                    style = Stroke(width = strokeUnit * 1.1f, cap = StrokeCap.Round)
                )
            }
        }

        DiceSurfacePattern.MARBLE_VEINS -> {
            val veinSlate = Color(0xFF64748B).copy(alpha = 0.32f * visibility)
            val veinGold = Color(0xFFD97706).copy(alpha = 0.26f * visibility)
            val v1Start = facePoint2D(-0.72f, -0.45f)
            val v1Mid = facePoint2D(-0.08f, 0.12f)
            val v1End = facePoint2D(0.70f, 0.56f)
            val path1 = Path().apply {
                moveTo(v1Start.x, v1Start.y)
                quadraticTo(v1Mid.x, v1Mid.y, v1End.x, v1End.y)
            }
            drawPath(path = path1, color = veinSlate, style = Stroke(width = strokeUnit * 1.1f, cap = StrokeCap.Round))

            val v2Start = facePoint2D(-0.38f, 0.68f)
            val v2Mid = facePoint2D(0.12f, -0.08f)
            val v2End = facePoint2D(0.72f, -0.42f)
            val path2 = Path().apply {
                moveTo(v2Start.x, v2Start.y)
                quadraticTo(v2Mid.x, v2Mid.y, v2End.x, v2End.y)
            }
            drawPath(path = path2, color = veinGold, style = Stroke(width = strokeUnit * 0.9f, cap = StrokeCap.Round))
        }

        DiceSurfacePattern.NEON_GRID -> {
            val inset = 0.64f
            drawLine(
                color = diceVisualTheme.edgeOuterColor.copy(alpha = 0.22f * visibility),
                start = facePoint2D(-inset, 0f),
                end = facePoint2D(inset, 0f),
                strokeWidth = strokeUnit * 0.85f
            )
            drawLine(
                color = diceVisualTheme.edgeOuterColor.copy(alpha = 0.22f * visibility),
                start = facePoint2D(0f, -inset),
                end = facePoint2D(0f, inset),
                strokeWidth = strokeUnit * 0.85f
            )
        }

        DiceSurfacePattern.BRUSHED_METAL -> {
            val sheenColor = Color.White.copy(alpha = 0.20f * visibility * brightness)
            drawLine(
                color = sheenColor,
                start = facePoint2D(-0.62f, -0.68f),
                end = facePoint2D(0.68f, 0.62f),
                strokeWidth = strokeUnit * 2.0f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = sheenColor.copy(alpha = 0.11f * visibility),
                start = facePoint2D(-0.68f, -0.28f),
                end = facePoint2D(0.28f, 0.68f),
                strokeWidth = strokeUnit * 1.2f,
                cap = StrokeCap.Round
            )
        }

        DiceSurfacePattern.CRYSTAL_FROST -> {
            val frostColor = Color.White.copy(alpha = 0.30f * visibility)
            val topMid = facePoint2D(0f, 0.68f)
            val rightMid = facePoint2D(0.68f, 0f)
            val bottomMid = facePoint2D(0f, -0.68f)
            val leftMid = facePoint2D(-0.68f, 0f)
            val facetPath = Path().apply {
                moveTo(topMid.x, topMid.y)
                lineTo(rightMid.x, rightMid.y)
                lineTo(bottomMid.x, bottomMid.y)
                lineTo(leftMid.x, leftMid.y)
                close()
            }
            drawPath(
                path = facetPath,
                color = frostColor,
                style = Stroke(width = strokeUnit * 0.95f, join = StrokeJoin.Round)
            )
        }

        DiceSurfacePattern.OBSIDIAN_LUXURY -> {
            // Subtle diagonal metallic gold & silver luxury sheen across the obsidian face
            val goldSheen = diceVisualTheme.edgeOuterColor.copy(alpha = 0.14f * visibility * brightness)
            val silverSheen = diceVisualTheme.edgeInnerColor.copy(alpha = 0.16f * visibility * brightness)
            drawLine(
                color = silverSheen,
                start = facePoint2D(-0.60f, 0.64f),
                end = facePoint2D(0.64f, -0.60f),
                strokeWidth = strokeUnit * 2.1f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = goldSheen,
                start = facePoint2D(-0.66f, 0.28f),
                end = facePoint2D(0.28f, -0.66f),
                strokeWidth = strokeUnit * 1.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Renders crisp, regular, 3D-embossed circular pips (1 to 6) with recessed sockets,
 * metallic Silver & Gold bezels, 3D spherical radial shading, and specular highlights.
 */
private fun DrawScope.drawProjectedFacePips(
    face: CubeFaceDef,
    diceVisualTheme: DiceVisualTheme,
    rotNormalZ: Float,
    brightness: Float,
    sixGlow: Boolean,
    rotate3D: (Vec3) -> Vec3,
    projectToScreen: (Vec3) -> Offset
) {
    // Balanced spacing inside the rounded 3D face cushion so all 1..6 pips look crisp and regular
    val s = 0.42f
    val uvCoords: List<Pair<Float, Float>> = when (face.faceValue) {
        1 -> listOf(Pair(0f, 0f))
        2 -> listOf(Pair(-s, s), Pair(s, -s))
        3 -> listOf(Pair(-s, s), Pair(0f, 0f), Pair(s, -s))
        4 -> listOf(Pair(-s, s), Pair(s, s), Pair(-s, -s), Pair(s, -s))
        5 -> listOf(Pair(-s, s), Pair(s, s), Pair(0f, 0f), Pair(-s, -s), Pair(s, -s))
        else -> listOf(
            Pair(-s, s), Pair(s, s),
            Pair(-s, 0f), Pair(s, 0f),
            Pair(-s, -s), Pair(s, -s)
        )
    }

    val isLightFace = diceVisualTheme.surfacePattern == DiceSurfacePattern.MARBLE_VEINS
    val isNeon = diceVisualTheme.surfacePattern == DiceSurfacePattern.NEON_GRID

    val pipMainColor = when {
        sixGlow && !isLightFace && !isNeon -> Color(0xFFFFEA00)
        face.faceValue == 1 -> diceVisualTheme.pipOneColor
        face.faceValue == 6 -> diceVisualTheme.pipSixColor
        else -> diceVisualTheme.pipDefaultColor
    }

    val basePipRadius3D = if (face.faceValue == 1) 0.215f else 0.155f
    val foreshortening = rotNormalZ.coerceIn(0.36f, 1.0f)

    for ((u, v) in uvCoords) {
        val pos3D = Vec3(
            x = face.center.x * 1.035f + face.uAxis.x * u + face.vAxis.x * v,
            y = face.center.y * 1.035f + face.uAxis.y * u + face.vAxis.y * v,
            z = face.center.z * 1.035f + face.uAxis.z * u + face.vAxis.z * v
        )
        val edge3D = Vec3(
            x = pos3D.x + face.uAxis.x * basePipRadius3D,
            y = pos3D.y + face.uAxis.y * basePipRadius3D,
            z = pos3D.z + face.uAxis.z * basePipRadius3D
        )

        val center2D = projectToScreen(rotate3D(pos3D))
        val edge2D = projectToScreen(rotate3D(edge3D))
        val radiusPx = (hypot(edge2D.x - center2D.x, edge2D.y - center2D.y) *
            (0.68f + 0.32f * foreshortening)).coerceAtLeast(1.8f)

        if (isNeon) {
            drawCircle(
                color = pipMainColor.copy(alpha = 0.44f * foreshortening),
                radius = radiusPx * 1.48f,
                center = center2D
            )
        } else {
            // 1. Deep 3D recessed socket shadow around the pip
            drawCircle(
                color = Color.Black.copy(alpha = (if (isLightFace) 0.38f else 0.76f) * brightness),
                radius = radiusPx * 1.26f,
                center = Offset(center2D.x + radiusPx * 0.08f, center2D.y + radiusPx * 0.12f)
            )
        }

        // 2. Metallic Silver & Gold Bezel Ring around the pip
        val bezelRadius = radiusPx * 1.12f
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    diceVisualTheme.edgeInnerColor.copy(alpha = 0.92f * brightness),
                    diceVisualTheme.pipRimColor.copy(alpha = 0.90f * brightness),
                    Color(0xFF475569).copy(alpha = 0.75f * brightness)
                ),
                start = Offset(center2D.x - bezelRadius, center2D.y - bezelRadius),
                end = Offset(center2D.x + bezelRadius, center2D.y + bezelRadius)
            ),
            radius = bezelRadius,
            center = center2D
        )

        // 3. 3D Embossed Spherical Pip Core (with radial lighting gradient for genuine 3D dome relief)
        val coreRadius = radiusPx * 0.94f
        val pipHighlightColor = Color(
            red = (pipMainColor.red * 0.55f + 0.45f).coerceIn(0f, 1f),
            green = (pipMainColor.green * 0.55f + 0.45f).coerceIn(0f, 1f),
            blue = (pipMainColor.blue * 0.55f + 0.45f).coerceIn(0f, 1f)
        )
        val pipShadowColor = Color(
            red = (pipMainColor.red * 0.52f).coerceIn(0f, 1f),
            green = (pipMainColor.green * 0.52f).coerceIn(0f, 1f),
            blue = (pipMainColor.blue * 0.52f).coerceIn(0f, 1f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    pipHighlightColor,
                    pipMainColor,
                    pipShadowColor
                ),
                center = Offset(
                    center2D.x - coreRadius * 0.26f,
                    center2D.y - coreRadius * 0.26f
                ),
                radius = coreRadius * 1.35f
            ),
            radius = coreRadius,
            center = center2D
        )

        // 4. Crisp Top-Left Specular Glint on the embossed pip
        if (rotNormalZ > 0.35f) {
            drawCircle(
                color = Color.White.copy(alpha = (0.78f * rotNormalZ).coerceIn(0f, 0.9f)),
                radius = radiusPx * 0.30f,
                center = Offset(center2D.x - radiusPx * 0.26f, center2D.y - radiusPx * 0.26f)
            )
        }
    }
}

