package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CombatLogEntry
import com.example.model.CombatOutcomeType
import com.example.model.toPersianDigits
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.RadiantGold

@Composable
fun CombatLogModal(
    combatLogs: List<CombatLogEntry>,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60B0616))
                .padding(horizontal = 12.dp, vertical = 20.dp)
                .testTag("combat_log_modal"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                RadiantGold,
                                DangerCrimson,
                                RadiantGold
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MysticDeepPlum)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MysticIndigoSurface)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DangerCrimson.copy(alpha = 0.25f))
                                    .border(1.5.dp, DangerCrimson, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SportsKabaddi,
                                    contentDescription = null,
                                    tint = RadiantGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "گزارش نبردها و تعامل قهرمانان (Combat Log)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "ثبت رویدادهای برخورد، حملات، سپرهای دفاعی و جابه‌جایی‌ها",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (combatLogs.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearLogs,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.10f))
                                        .testTag("clear_combat_logs_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteSweep,
                                        contentDescription = "پاک کردن گزارش نبرد",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .testTag("close_combat_log_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "بستن",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Content List or Empty State
                    if (combatLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = "هنوز برخوردی بین مهره‌ها رخ نداده است",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "هنگامی که مهره‌های مهاجم و مدافع روی یک خانه قرار بگیرند یا قابلیتی اجرا شود، گزارش ثبت می‌گردد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.60f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(combatLogs.reversed(), key = { it.id }) { log ->
                                CombatLogCard(log = log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CombatLogCard(log: CombatLogEntry) {
    val borderColor = when (log.outcomeType) {
        CombatOutcomeType.ATTACK_HIT -> DangerCrimson
        CombatOutcomeType.SHIELD_PARRY, CombatOutcomeType.CRITICAL_PARRY -> RadiantGold
        CombatOutcomeType.ANGEL_PROTECTED, CombatOutcomeType.ILLUSION_DIVERTED -> Color(0xFF38BDF8)
        CombatOutcomeType.RESISTANCE_MITIGATED -> HopefulEmerald
        CombatOutcomeType.POSITION_SWAPPED -> Color(0xFFA855F7)
        CombatOutcomeType.ANCHOR_IMMUNE -> Color(0xFFF97316)
        CombatOutcomeType.ATTACK_FAILED -> Color(0xFF94A3B8)
        CombatOutcomeType.PHANTOM_EVADED -> Color(0xFF06B6D4)
        CombatOutcomeType.NEUTRAL_COEXIST -> Color(0xFF64748B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderColor.copy(alpha = 0.70f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC171124))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header: Outcome Badge + Tile Location + Turn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(borderColor.copy(alpha = 0.22f))
                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${log.outcomeType.emoji} ${log.outcomeType.persianLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = borderColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Text(
                        text = "خانهٔ ${log.tile.toPersianDigits()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "نوبت ${log.turnNumber.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }

            // Description of action
            Text(
                text = log.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.95f),
                fontWeight = FontWeight.SemiBold
            )

            // Combatants summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مهاجم: ${log.attackerName} (${log.attackerHeroClass.badgeEmoji} ${log.attackerHeroClass.persianTitle.take(12)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.70f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "مدافع: ${log.defenderName} (${log.defenderHeroClass.badgeEmoji} ${log.defenderHeroClass.persianTitle.take(12)})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.70f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (log.defenderTileBefore != log.defenderTileAfter) {
                Text(
                    text = "تغییر موقعیت مدافع: خانهٔ ${log.defenderTileBefore.toPersianDigits()} ← ${log.defenderTileAfter.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DangerCrimson,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
