package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CombatOutcomeType
import com.example.model.HeroAnimationStance
import com.example.model.HeroClashEvent
import com.example.model.HeroRenderQuality
import com.example.model.HeroVisualCatalog
import com.example.model.toPersianDigits
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.RadiantGold

/**
 * Animated High-Fidelity Hero Confrontation Modal (نمایش تعامل و مبارزه قهرمان‌ها)
 * Displays Attacker vs Defender portraits/stances, ability names, defensive statuses,
 * and final resolved outcome banner.
 */
@Composable
fun HeroClashInteractionOverlay(
    clashEvent: HeroClashEvent,
    onDismiss: () -> Unit
) {
    val attackerProfile = remember(clashEvent.attackerCharacter.id) {
        HeroVisualCatalog.getProfile(clashEvent.attackerCharacter.id)
    }
    val defenderProfile = remember(clashEvent.defenderCharacter.id) {
        HeroVisualCatalog.getProfile(clashEvent.defenderCharacter.id)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "clash_pulse")
    val vsScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vs_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hero_clash_interaction_overlay"),
            color = Color(0xDD070A10)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    attackerProfile.primaryColor,
                                    RadiantGold,
                                    defenderProfile.primaryColor
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.96f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "⚔️", fontSize = 24.sp)
                            Text(
                                text = "رویارویی سرنوشت‌ساز قهرمانان",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = RadiantGold
                            )
                            Text(text = "⚡", fontSize = 24.sp)
                        }

                        // Tile Indicator Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.50f))
                                .border(1.dp, RadiantGold.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "نبرد در خانهٔ ${clashEvent.tile.toPersianDigits()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.90f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Combat Arena: Attacker vs Defender
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Attacker Column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(attackerProfile.primaryColor.copy(alpha = 0.16f))
                                    .border(1.5.dp, attackerProfile.primaryColor.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(attackerProfile.primaryColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "مهاجم",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                HeroVisualRenderer(
                                    character = clashEvent.attackerCharacter,
                                    stance = HeroAnimationStance.ATTACK,
                                    quality = HeroRenderQuality.CINEMATIC_3D,
                                    size = 110.dp,
                                    showPedestal = false,
                                    showAuraParticles = true
                                )

                                Text(
                                    text = clashEvent.attackerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = clashEvent.attackerCharacter.heroBlueprint.primaryClass.persianTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = attackerProfile.accentGlowColor,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.40f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "🎯 ${clashEvent.abilityName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RadiantGold,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "تاس حمله: ${clashEvent.attackRoll.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }

                            // Center VS Badge
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .scale(vsScale)
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(RadiantGold, Color(0xFFB8860B), Color(0xFF3B1E08))
                                        )
                                    )
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }

                            // Defender Column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(defenderProfile.primaryColor.copy(alpha = 0.16f))
                                    .border(1.5.dp, defenderProfile.primaryColor.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(defenderProfile.primaryColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "مدافع",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                HeroVisualRenderer(
                                    character = clashEvent.defenderCharacter,
                                    stance = HeroAnimationStance.DEFENSE,
                                    quality = HeroRenderQuality.CINEMATIC_3D,
                                    size = 110.dp,
                                    showPedestal = false,
                                    showAuraParticles = true
                                )

                                Text(
                                    text = clashEvent.defenderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = clashEvent.defenderCharacter.heroBlueprint.primaryClass.persianTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = defenderProfile.accentGlowColor,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.40f))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = clashEvent.defenseStatusDescription,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "تاس دفاع: ${clashEvent.defenseRoll.toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // Outcome Summary Card
                        val outcomeColor = when (clashEvent.outcomeType) {
                            CombatOutcomeType.ATTACK_HIT -> DangerCrimson
                            CombatOutcomeType.SHIELD_PARRY,
                            CombatOutcomeType.CRITICAL_PARRY,
                            CombatOutcomeType.ANGEL_PROTECTED,
                            CombatOutcomeType.ILLUSION_DIVERTED,
                            CombatOutcomeType.PHANTOM_EVADED -> HopefulEmerald
                            else -> RadiantGold
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(outcomeColor.copy(alpha = 0.18f))
                                .border(1.5.dp, outcomeColor.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = clashEvent.outcomeType.emoji,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = clashEvent.outcomeType.persianLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = outcomeColor
                                )
                            }

                            Text(
                                text = clashEvent.resultMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f),
                                textAlign = TextAlign.Center
                            )

                            if (clashEvent.knockbackSteps > 0) {
                                Text(
                                    text = "⚡ مدافع ${clashEvent.knockbackSteps.toPersianDigits()} خانه به عقب رانده شد!",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DangerCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Dismiss / Continue Button (>= 48dp)
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("hero_clash_dismiss_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RadiantGold,
                                contentColor = Color(0xFF140B24)
                            )
                        ) {
                            Text(
                                text = "ادامه نبرد و پرتاب تاس",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
