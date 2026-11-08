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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AbilityActivationMode
import com.example.model.HeroVisualCatalog
import com.example.model.Player
import com.example.model.toPersianDigits
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.RadiantGold

/**
 * Ability Activation Confirmation & Target/Effect Preview Modal
 * (پیش‌نمایش هدف و اثر قابلیت همراه با تأیید نهایی فعال‌سازی)
 */
@Composable
fun AbilityActivationConfirmModal(
    player: Player,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val character = player.character
    val blueprint = character.heroBlueprint
    val primaryAbility = blueprint.primaryAbility
    val profile = remember(character.id) { HeroVisualCatalog.getProfile(character.id) }

    val targetScopeText = primaryAbility.targetScope.persianLabel
    val cooldownDesc = when (primaryAbility.activationMode) {
        AbilityActivationMode.ACTIVE_COOLDOWN -> "⏳ پس از فعال‌سازی، به مدت ${primaryAbility.cooldownTurns.toPersianDigits()} نوبت در حال بازیابی خواهد بود."
        AbilityActivationMode.ACTIVE_ONCE -> "🌟 این توانایی قابلیت ویژه است و تنها ۱ بار در این مسابقه قابل اجراست."
        AbilityActivationMode.AUTO_CHARGE -> "🛡️ با هر ۳ پرتاب تاس، این قابلیت به‌طور خودکار شارژ مجدد می‌شود."
        AbilityActivationMode.PASSIVE -> "✨ این ویژگی به‌صورت منفعل و دائمی فعال است."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ability_activation_confirm_modal"),
            color = Color(0xDD070A10)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(profile.primaryColor, RadiantGold.copy(alpha = 0.6f))
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.96f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header with Player info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CharacterPortraitAvatar(
                                    character = character,
                                    size = 46.dp,
                                    isSelected = true,
                                    tokenColorOverride = player.color
                                )
                                Column {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${blueprint.primaryClass.persianTitle} • ${character.englishName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = profile.accentGlowColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RadiantGold.copy(alpha = 0.20f))
                                    .border(1.dp, RadiantGold, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ آماده فعال‌سازی",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RadiantGold
                                )
                            }
                        }

                        // Ability Title Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(profile.primaryColor.copy(alpha = 0.22f))
                                .border(1.dp, profile.accentGlowColor.copy(alpha = 0.50f), RoundedCornerShape(16.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${character.displayBadgeEmoji} ${primaryAbility.persianName}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = primaryAbility.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.90f),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Target Scope & Effect Details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.40f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🎯", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = "هدف توانایی:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RadiantGold
                                    )
                                    Text(
                                        text = targetScopeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.90f)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⏳", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = "بازیابی و کول‌داون:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = profile.accentGlowColor
                                    )
                                    Text(
                                        text = cooldownDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        // Action Buttons (>= 48dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("ability_confirm_cancel_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("انصراف", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .testTag("ability_confirm_execute_button"),
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "تأیید و اجرا ⚡",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
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
