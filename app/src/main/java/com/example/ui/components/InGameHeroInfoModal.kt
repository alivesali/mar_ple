package com.example.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AbilityActivationMode
import com.example.model.HeroAnimationStance
import com.example.model.HeroRenderQuality
import com.example.model.HeroVisualCatalog
import com.example.model.Player
import com.example.model.toPersianDigits
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.RadiantGold

/**
 * In-Game Hero Status HUD & Information Modal (صفحه اطلاعات قهرمان داخل بازی)
 * Displays Hero Avatar/figure, Active Ability status (ready vs cooldown turns),
 * remaining uses per match, passive traits, resistances, and defensive statuses.
 */
@Composable
fun InGameHeroInfoModal(
    player: Player,
    isCurrentTurn: Boolean,
    onActivateAbility: () -> Unit,
    onDismiss: () -> Unit
) {
    val character = player.character
    val blueprint = character.heroBlueprint
    val primaryAbility = blueprint.primaryAbility
    val resistance = blueprint.resistanceProfile
    val profile = remember(character.id) { HeroVisualCatalog.getProfile(character.id) }

    val isReady = player.canTriggerActiveAbilityNow
    val remainingUsesText = if (primaryAbility.maxUsesPerMatch > 0) {
        val remaining = (primaryAbility.maxUsesPerMatch - player.abilityUsesCount).coerceAtLeast(0)
        "${remaining.toPersianDigits()} از ${primaryAbility.maxUsesPerMatch.toPersianDigits()} بار"
    } else {
        "نامحدود (با کول‌داون)"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("in_game_hero_info_modal"),
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
                            brush = Brush.verticalGradient(
                                colors = listOf(profile.primaryColor, RadiantGold.copy(alpha = 0.5f))
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Bar with Close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🛡️", fontSize = 20.sp)
                                Text(
                                    text = "شناسنامه داخل بازی قهرمان",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RadiantGold
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .testTag("in_game_hero_info_close")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "بستن",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Hero Header & Stance Figure
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(profile.primaryColor.copy(alpha = 0.18f))
                                .border(1.dp, profile.accentGlowColor.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HeroVisualRenderer(
                                character = character,
                                stance = if (isReady) HeroAnimationStance.ABILITY else HeroAnimationStance.IDLE,
                                quality = HeroRenderQuality.CINEMATIC_3D,
                                size = 88.dp,
                                showPedestal = false,
                                showAuraParticles = true
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(player.color)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = player.colorName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Text(
                                    text = "${blueprint.primaryClass.persianTitle} (${character.englishName})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = profile.accentGlowColor,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "خانه فعلی: ${player.currentTile.toPersianDigits()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // 1. ACTIVE ABILITY STATUS
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .border(1.dp, if (isReady) HopefulEmerald else Color.White.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "⚡ قابلیت فعال: ${primaryAbility.persianName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RadiantGold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isReady) HopefulEmerald.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                                        .border(1.dp, if (isReady) HopefulEmerald else Color.White.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isReady) "آماده استفاده ⚡" else "در حال بازیابی ⏳",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isReady) HopefulEmerald else Color.White.copy(alpha = 0.70f)
                                    )
                                }
                            }

                            Text(
                                text = primaryAbility.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.90f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "وضعیت شارژ: ${player.buildAbilityStatusBadgeText()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.80f)
                                )
                                Text(
                                    text = "فرصت باقیمانده: $remainingUsesText",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = profile.accentGlowColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 1.5 PLAYER RESOURCES (HP / MANA / SHIELD)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 منابع و وضعیت بقا",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RadiantGold
                                )
                                if (player.teamId != null) {
                                    Text(
                                        text = "تیم: ${player.teamId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.70f)
                                    )
                                }
                            }

                            // HP Bar
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("❤️ سلامتی (HP)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4ADE80))
                                    Text("${player.hp.toPersianDigits()} / ${player.resources.maxHp.toPersianDigits()}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                                LinearProgressIndicator(
                                    progress = { player.resources.hpPercentage },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = if (player.hp <= 30) Color(0xFFEF4444) else Color(0xFF22C55E),
                                    trackColor = Color.White.copy(alpha = 0.12f)
                                )
                            }

                            // Mana Bar
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("💧 مانا (Mana)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF60A5FA))
                                    Text("${player.mana.toPersianDigits()} / ${player.resources.maxMana.toPersianDigits()}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                                LinearProgressIndicator(
                                    progress = { player.resources.manaPercentage },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color.White.copy(alpha = 0.12f)
                                )
                            }

                            // Shield Bar
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🛡️ سپر حفاظتی (Shield)", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFBBF24))
                                    Text("${player.shield.toPersianDigits()} / ${player.resources.maxShield.toPersianDigits()}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                }
                                LinearProgressIndicator(
                                    progress = { player.resources.shieldPercentage },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFFF59E0B),
                                    trackColor = Color.White.copy(alpha = 0.12f)
                                )
                            }
                        }

                        // 2. PASSIVE ABILITY
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✨ قابلیت غیرفعال: ${blueprint.passiveAbilityTitle}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = profile.accentGlowColor
                            )
                            Text(
                                text = blueprint.passiveAbilityDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.90f)
                            )
                        }

                        // 3. DEFENSES & RESISTANCES
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🛡️ وضعیت دفاعی و مقاومت‌ها",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = RadiantGold
                            )

                            // Status Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (player.isShieldCharged) HopefulEmerald.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, if (player.isShieldCharged) HopefulEmerald else Color.White.copy(alpha = 0.20f), RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = if (player.isShieldCharged) "🛡️ سپر شارژ کامل" else "🛡️ سپر خالی",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (player.isShieldCharged) HopefulEmerald else Color.White.copy(alpha = 0.60f)
                                    )
                                }

                                if (player.guardianAngelProtected) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(RadiantGold.copy(alpha = 0.20f))
                                            .border(1.dp, RadiantGold, RoundedCornerShape(10.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "🌟 فرشته نجات فعال",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RadiantGold
                                        )
                                    }
                                }

                                if (player.phantomPhaseActive) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF9C27B0).copy(alpha = 0.20f))
                                            .border(1.dp, Color(0xFFBA68C8), RoundedCornerShape(10.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "👻 فاز شبح‌وار فعال",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE1BEE7)
                                        )
                                    }
                                }
                            }

                            // Passive Resistance Traits
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (resistance.displacementResistancePercent > 0) {
                                    Text(
                                        text = "⚓ لنگر سنگین: ${resistance.displacementResistancePercent.toPersianDigits()}٪ مقاومت در برابر جابه‌جایی و پرتاب رقبا",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                if (resistance.snakeDropReductionPercent > 0) {
                                    Text(
                                        text = "🐍 مقاومت سم مار: کاهش ${resistance.snakeDropReductionPercent.toPersianDigits()}٪ از آسیب سقوط مارها",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                                if (resistance.statusDebuffResistancePercent > 0) {
                                    Text(
                                        text = "🔮 مقاومت طلسم و موانع: کاهش ${resistance.statusDebuffResistancePercent.toPersianDigits()}٪ از اثرات منفی موانع",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        // Activate Ability Button if it's current turn and ready
                        if (isCurrentTurn && isReady) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onActivateAbility()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("in_game_hero_activate_now_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RadiantGold,
                                    contentColor = Color(0xFF140B24)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF140B24),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "فعال‌سازی توانایی همین حالا ⚡",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        // Dismiss button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("in_game_hero_info_dismiss"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("بستن شناسنامه", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
