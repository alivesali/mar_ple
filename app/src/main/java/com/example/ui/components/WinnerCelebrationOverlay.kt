package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Player
import com.example.model.team.GameTeam
import com.example.model.toPersianDigits
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.PlayerTokenColors
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.RadiantGold
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val xFraction: Float,
    val initialYFraction: Float,
    val speed: Float,
    val sizeDp: Float,
    val color: Color,
    val driftAmp: Float,
    val phaseOffset: Float
)

@Composable
fun WinnerCelebrationOverlay(
    winner: Player?,
    allPlayers: List<Player>,
    onPlayAgainSamePlayers: () -> Unit,
    onOpenAdvancedSettings: () -> Unit,
    onBackToSetup: () -> Unit,
    modifier: Modifier = Modifier,
    isMatchDraw: Boolean = false,
    winningTeam: GameTeam? = null,
    winReason: String? = null,
    maxTiles: Int = 100,
    canContinueMatch: Boolean = false,
    hasFinalizedMatch: Boolean = false,
    usePreSortedRankings: Boolean = false,
    onContinueMatch: () -> Unit = {},
    onFinalizeVictoryResult: () -> Unit = {}
) {
    val accentColor = winningTeam?.bannerColor ?: winner?.color ?: RadiantGold
    val isDrawState = isMatchDraw || winner == null

    val infiniteTransition = rememberInfiniteTransition(label = "victory_celebration")
    val confettiTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_time"
    )

    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophy_scale"
    )

    val confettiParticles = remember {
        val rng = Random(2026)
        List(85) { idx ->
            ConfettiParticle(
                xFraction = rng.nextFloat(),
                initialYFraction = rng.nextFloat(),
                speed = 0.6f + rng.nextFloat() * 0.9f,
                sizeDp = 7f + rng.nextFloat() * 9f,
                color = PlayerTokenColors[idx % PlayerTokenColors.size],
                driftAmp = 14f + rng.nextFloat() * 22f,
                phaseOffset = rng.nextFloat() * 6.28f
            )
        }
    }

    val sortedStandings = remember(allPlayers, usePreSortedRankings) {
        if (usePreSortedRankings) {
            allPlayers
        } else {
            allPlayers.sortedWith(
                compareByDescending<Player> { it.resources.isAlive }
                    .thenByDescending { it.currentTile }
                    .thenByDescending { it.laddersClimbed }
                    .thenBy { it.snakesBitten }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xDD120924))
            .testTag("winner_celebration_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Confetti & Golden Fireworks Canvas (softened or skipped if draw)
        if (!isDrawState) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (p in confettiParticles) {
                    val yNorm = (p.initialYFraction + confettiTime * p.speed) % 1.0f
                    val y = yNorm * size.height
                    val x = (p.xFraction * size.width) +
                        sin(confettiTime * 6.28f + p.phaseOffset) * p.driftAmp

                    rotate(degrees = (confettiTime * 360f + p.phaseOffset * 50f) % 360f, pivot = Offset(x, y)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(x - p.sizeDp / 2f, y - p.sizeDp / 2f),
                            size = Size(p.sizeDp, p.sizeDp * 0.65f)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(16.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(RadiantGold, accentColor, RadiantGold)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MysticIndigoSurface.copy(alpha = 0.97f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Draw Shield OR Glowing Trophy + Winner Character Portrait & Pawn
                if (isDrawState || winner == null) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .scale(trophyScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        DangerCrimson.copy(alpha = 0.40f),
                                        RadiantGold.copy(alpha = 0.25f)
                                    )
                                )
                            )
                            .border(2.5.dp, RadiantGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "پایان نبرد بدون برنده",
                            tint = RadiantGold,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .scale(trophyScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            RadiantGold.copy(alpha = 0.45f),
                                            accentColor.copy(alpha = 0.25f)
                                        )
                                    )
                                )
                                .border(2.5.dp, RadiantGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = stringResource(R.string.winner_title),
                                tint = RadiantGold,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        CharacterPortraitAvatar(
                            character = winner.character,
                            size = 78.dp,
                            isSelected = true
                        )
                        // Equipped Winning Pawn with Victory Burst Animation
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            winner.color.copy(alpha = 0.40f),
                                            RadiantGold.copy(alpha = 0.25f),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                )
                                .border(2.5.dp, RadiantGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            PawnSkinRenderer(
                                skin = winner.pawnSkin,
                                playerColor = winner.color,
                                previewMode = PawnPreviewMode.VICTORY_BURST,
                                size = 72.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isDrawState) "پایان نبرد — تساوی یا حذف همگانی" else stringResource(R.string.winner_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                if (winningTeam != null && !isDrawState) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(winningTeam.bannerColor.copy(alpha = 0.28f))
                            .border(1.5.dp, winningTeam.bannerColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .testTag("winning_team_badge")
                    ) {
                        Text(
                            text = "🛡️ تیم «${winningTeam.name}» قهرمان میدان شد!",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Winner or Draw Details Badge
                if (isDrawState || winner == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DangerCrimson.copy(alpha = 0.18f))
                            .border(1.5.dp, DangerCrimson.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = winReason ?: "تمامی بازیکنان از نبرد حذف شدند و بازی با تساوی پایان یافت.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(winner.color.copy(alpha = 0.22f))
                            .border(1.5.dp, winner.color, RoundedCornerShape(16.dp))
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(winner.color)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Text(
                                text = winner.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "${winner.character.persianTitle} (${winner.character.displayBadgeEmoji})",
                            style = MaterialTheme.typography.labelLarge,
                            color = RadiantGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "♟️ پوسته پیروز: ${winner.pawnSkin.namePersian} (${winner.pawnSkin.ambientParticleEmoji})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Medium
                        )
                        if (!winReason.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = winReason,
                                style = MaterialTheme.typography.labelMedium,
                                color = HopefulEmerald,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Ranked Standings Table
                Text(
                    text = "🏆 جدول رتبه‌بندی و آمار کامل بازیکنان",
                    style = MaterialTheme.typography.labelLarge,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 175.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MysticCardSurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(sortedStandings, key = { _, p -> p.id }) { idx, p ->
                        val isAlive = p.resources.isAlive
                        val medal = when {
                            !isAlive -> "💀"
                            isDrawState -> "${(idx + 1).toPersianDigits()}."
                            idx == 0 -> "🥇"
                            idx == 1 -> "🥈"
                            idx == 2 -> "🥉"
                            else -> "${(idx + 1).toPersianDigits()}."
                        }
                        val rowTextColor = if (isAlive) Color.White else Color.White.copy(alpha = 0.58f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        !isAlive -> DangerCrimson.copy(alpha = 0.10f)
                                        !isDrawState && idx == 0 -> RadiantGold.copy(alpha = 0.14f)
                                        else -> Color.White.copy(alpha = 0.04f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = medal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (idx == 0 && !isDrawState && isAlive) RadiantGold else rowTextColor
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (isAlive) p.color else p.color.copy(alpha = 0.45f))
                                )
                                Text(
                                    text = "${p.name} (${p.character.persianTitle})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = rowTextColor
                                )
                            }
                            val statusPrefix = if (!isAlive) "حذف‌شده • " else ""
                            Text(
                                text = "${statusPrefix}خانه ${p.currentTile.toPersianDigits()}/${maxTiles.toPersianDigits()} • ❤️${p.resources.hp.toPersianDigits()} • 🪜${p.laddersClimbed.toPersianDigits()} • 🐍${p.snakesBitten.toPersianDigits()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = rowTextColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (canContinueMatch && !hasFinalizedMatch && !isDrawState) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onFinalizeVictoryResult,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("finalize_victory_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RadiantGold,
                                contentColor = Color(0xFF140B24)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "پایان مسابقه و ثبت نتیجه"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "پایان مسابقه و ثبت نتیجه",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = onContinueMatch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("continue_after_victory_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, HopefulEmerald)
                        ) {
                            Text(
                                text = "ادامهٔ مسابقه برای رتبه‌بندی بقیه",
                                style = MaterialTheme.typography.labelLarge,
                                color = HopefulEmerald,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Action Buttons: Play Again, Advanced Settings, and Back to Setup
                Button(
                    onClick = onPlayAgainSamePlayers,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("play_again_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HopefulEmerald,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = stringResource(R.string.play_again_same_players)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.play_again_same_players),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenAdvancedSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("victory_open_settings_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RadiantGold)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "تنظیمات",
                            tint = RadiantGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تنظیمات",
                            style = MaterialTheme.typography.labelLarge,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    OutlinedButton(
                        onClick = onBackToSetup,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("back_to_lobby_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RadiantAmber)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Group,
                            contentDescription = stringResource(R.string.back_to_lobby),
                            tint = RadiantGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "صفحه شروع",
                            style = MaterialTheme.typography.labelLarge,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
