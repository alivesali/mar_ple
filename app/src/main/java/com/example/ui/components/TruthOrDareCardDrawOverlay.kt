package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.ActiveSnakeCardDraw
import com.example.model.toPersianDigits
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.RadiantGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Suspenseful 3D Card Draw & Flip Reveal Overlay triggered when a player lands on a Snake!
 * - First shows the glowing, mysterious back of the card with rotating rays of light.
 * - When tapped, performs a 3D Y-axis flip with light burst to reveal:
 *   1. The Category badge & icon (خانوادگی / رفاقتی / سکسی +۱۸)
 *   2. The Card Title at the top (e.g., «جرئت» or «حقیقت»)
 *   3. The Main Challenge/Question below the title
 *   4. A button to confirm execution and resume the game smoothly.
 */
@Composable
fun TruthOrDareCardDrawOverlay(
    activeDraw: ActiveSnakeCardDraw,
    onFlipRevealCard: () -> Unit,
    onCompleteCardChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val card = activeDraw.card
    val category = card.category
    val player = activeDraw.player

    val infiniteTransition = rememberInfiniteTransition(label = "card_draw_fx")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_rotation"
    )
    val suspensePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "suspense_pulse"
    )

    val flipRotationY by animateFloatAsState(
        targetValue = if (activeDraw.isRevealed) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_flip_3d"
    )

    val isShowingFront = flipRotationY >= 90f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE80E061C))
            .testTag("truth_or_dare_draw_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Radiant Rotating Light Rays & Magical Orbs Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            val maxR = size.minDimension * 0.72f
            val auraColor = if (isShowingFront) category.primaryColor else RadiantGold

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.36f),
                        auraColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = maxR
                ),
                radius = maxR,
                center = Offset(cx, cy)
            )

            rotate(degrees = rayRotation, pivot = Offset(cx, cy)) {
                for (i in 0 until 12) {
                    val angleDeg = i * 30f
                    val rad1 = Math.toRadians((angleDeg - 6f).toDouble())
                    val rad2 = Math.toRadians((angleDeg + 6f).toDouble())
                    val rayPath = Path().apply {
                        moveTo(cx, cy)
                        lineTo(
                            cx + cos(rad1).toFloat() * maxR,
                            cy + sin(rad1).toFloat() * maxR
                        )
                        lineTo(
                            cx + cos(rad2).toFloat() * maxR,
                            cy + sin(rad2).toFloat() * maxR
                        )
                        close()
                    }
                    drawPath(
                        path = rayPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                auraColor.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = maxR
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player & Snake Bite Header Banner
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(MysticIndigoSurface.copy(alpha = 0.95f))
                    .border(1.5.dp, player.color, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CharacterPortraitAvatar(
                    character = player.character,
                    size = 44.dp,
                    isSelected = true
                )
                Column {
                    Text(
                        text = "🐍 نیش مار در خانه ${activeDraw.snakeStartTile.toPersianDigits()}!",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFFF758F),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "نوبت کشیدن کارت برای ${player.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // 3D Flipping Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(if (!activeDraw.isRevealed) suspensePulse else 1f)
                    .graphicsLayer {
                        rotationY = flipRotationY
                        cameraDistance = 14f * density
                    }
            ) {
                if (!isShowingFront) {
                    // --- MYSTERIOUS UNFLIPPED CARD BACK (Suspense State) ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(370.dp)
                            .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = RadiantGold)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(RadiantGold, category.primaryColor, RadiantGold)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .clickable { onFlipRevealCard() }
                            .testTag("card_back_suspense_box"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1F1137)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Category Hint Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(category.primaryColor.copy(alpha = 0.22f))
                                    .border(1.dp, category.primaryColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = category.badgeEmoji, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "دسته: ${category.persianTitle}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // Central Glowing Serpent & Mystery Emblem
                            Box(
                                modifier = Modifier
                                    .size(116.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                RadiantGold.copy(alpha = 0.35f),
                                                category.primaryColor.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .border(2.5.dp, RadiantGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🐍🃏",
                                    style = MaterialTheme.typography.displaySmall
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "کارت اسرارآمیز مار!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "روی کارت بزنید تا بچرخد و مشخص شود چه چالشی در انتظار ${player.name} است!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Button(
                                onClick = onFlipRevealCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("flip_card_reveal_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RadiantGold,
                                    contentColor = Color(0xFF1A0F2E)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TouchApp,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "چرخش و باز کردن کارت ✨",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                } else {
                    // --- REVEALED CARD FACE (Mirrored back to normal reading via rotationY = 180f) ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = 180f
                            }
                            .shadow(28.dp, RoundedCornerShape(28.dp), spotColor = category.primaryColor)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(category.primaryColor, RadiantGold, category.primaryColor)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .testTag("card_front_revealed_box"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MysticIndigoSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Category Badge Ribbon
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(category.primaryColor.copy(alpha = 0.24f))
                                    .border(1.5.dp, category.primaryColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = category.badgeEmoji, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "دسته ${category.persianTitle}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = category.primaryColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // 2. CARD TITLE AT THE TOP (e.g., «جرئت» or «حقیقت»)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                category.secondaryColor,
                                                MysticDeepPlum,
                                                category.secondaryColor
                                            )
                                        )
                                    )
                                    .border(2.dp, RadiantGold, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = RadiantGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = card.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = RadiantGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = RadiantGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // 3. MAIN CARD TEXT BELOW THE TITLE (The Challenge or Question)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MysticCardSurface)
                                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 18.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = card.body,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // 4. Complete Challenge & Resume Game Button
                            Button(
                                onClick = onCompleteCardChallenge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("complete_card_challenge_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HopefulEmerald,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "انجام شد! ادامه بازی",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
