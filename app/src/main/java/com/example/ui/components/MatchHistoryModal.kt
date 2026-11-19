package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.MatchHistoryOverviewStats
import com.example.model.MatchHistoryRecord
import com.example.model.MatchParticipantRecord
import com.example.model.MatchStatisticsCalculator
import com.example.model.PlayerHistoryStats
import com.example.model.TeamHistoryStats
import com.example.model.toPersianDigits
import com.example.model.toPersianDigitsString
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.RadiantAmber
import com.example.ui.theme.RadiantGold
import java.util.Locale

private enum class MatchHistoryTab {
    HISTORY_LIST,
    STATISTICS
}

@Composable
fun MatchHistoryModal(
    matchHistory: List<MatchHistoryRecord>,
    selectedMatchDetail: MatchHistoryRecord?,
    onSelectMatchDetail: (MatchHistoryRecord?) -> Unit,
    onDeleteMatchRecord: (Long) -> Unit,
    onClearAllHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(MatchHistoryTab.HISTORY_LIST) }
    var pendingDeleteRecord by remember { mutableStateOf<MatchHistoryRecord?>(null) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    val overviewStats = remember(matchHistory) {
        MatchStatisticsCalculator.computeOverview(matchHistory)
    }
    val playerStats = remember(matchHistory) {
        MatchStatisticsCalculator.computePlayerStats(matchHistory)
    }
    val teamStats = remember(matchHistory) {
        MatchStatisticsCalculator.computeTeamStats(matchHistory)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE80B0616))
                .padding(horizontal = 12.dp, vertical = 18.dp)
                .testTag("match_history_modal"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 620.dp)
                    .fillMaxHeight(0.90f)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(RadiantGold, HopefulEmerald, RadiantGold)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MysticDeepPlum)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MysticIndigoSurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (selectedMatchDetail != null) {
                                IconButton(
                                    onClick = { onSelectMatchDetail(null) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.10f))
                                        .testTag("match_history_detail_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "بازگشت به فهرست مسابقات",
                                        tint = RadiantGold
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(RadiantGold.copy(alpha = 0.20f))
                                        .border(1.5.dp, RadiantGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = RadiantGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedMatchDetail != null) {
                                        "جزئیات نتیجهٔ مسابقه"
                                    } else {
                                        "تاریخچه مسابقات و آمار عملکرد"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selectedMatchDetail != null) {
                                        selectedMatchDetail.formattedCompletedAt()
                                    } else {
                                        "${matchHistory.size.toPersianDigits()} مسابقهٔ ثبت‌شده"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (selectedMatchDetail == null && matchHistory.isNotEmpty()) {
                                IconButton(
                                    onClick = { showClearAllConfirmDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(DangerCrimson.copy(alpha = 0.16f))
                                        .testTag("clear_all_match_history_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteSweep,
                                        contentDescription = "پاک کردن کل تاریخچه",
                                        tint = DangerCrimson
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .testTag("close_match_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "بستن تاریخچه مسابقات",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    if (selectedMatchDetail != null) {
                        MatchHistoryDetailContent(
                            record = selectedMatchDetail,
                            onRequestDelete = { pendingDeleteRecord = selectedMatchDetail }
                        )
                    } else {
                        // Tabs Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryTabButton(
                                title = "تاریخچه مسابقات (${matchHistory.size.toPersianDigits()})",
                                icon = Icons.Filled.History,
                                isSelected = activeTab == MatchHistoryTab.HISTORY_LIST,
                                onClick = { activeTab = MatchHistoryTab.HISTORY_LIST },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("match_history_tab_list")
                            )
                            HistoryTabButton(
                                title = "آمار بازیکنان و تیم‌ها",
                                icon = Icons.Filled.BarChart,
                                isSelected = activeTab == MatchHistoryTab.STATISTICS,
                                onClick = { activeTab = MatchHistoryTab.STATISTICS },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("match_history_tab_stats")
                            )
                        }

                        when (activeTab) {
                            MatchHistoryTab.HISTORY_LIST -> {
                                MatchHistoryListContent(
                                    matchHistory = matchHistory,
                                    overviewStats = overviewStats,
                                    onSelectMatchDetail = onSelectMatchDetail,
                                    onRequestDelete = { record -> pendingDeleteRecord = record }
                                )
                            }
                            MatchHistoryTab.STATISTICS -> {
                                MatchStatisticsContent(
                                    overviewStats = overviewStats,
                                    playerStats = playerStats,
                                    teamStats = teamStats
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDeleteRecord?.let { targetRecord ->
        AlertDialog(
            onDismissRequest = { pendingDeleteRecord = null },
            containerColor = MysticIndigoSurface,
            titleContentColor = RadiantGold,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "حذف نتیجهٔ مسابقه",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                val summary = if (targetRecord.isDraw) {
                    "مسابقه با تساوی (${targetRecord.formattedCompletedAt()})"
                } else {
                    "برنده: ${targetRecord.winnerName ?: "-"} (${targetRecord.formattedCompletedAt()})"
                }
                Text(
                    text = "آیا مطمئن هستید که می‌خواهید این رکورد را از تاریخچه و آمار حذف کنید؟\n$summary"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMatchRecord(targetRecord.id)
                        pendingDeleteRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_delete_single_match_button")
                ) {
                    Text("حذف", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteRecord = null },
                    modifier = Modifier.testTag("cancel_delete_single_match_button")
                ) {
                    Text("انصراف", color = Color.White.copy(alpha = 0.85f))
                }
            }
        )
    }

    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            containerColor = MysticIndigoSurface,
            titleContentColor = DangerCrimson,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "پاک کردن کل تاریخچه مسابقات",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text("تمام سوابق مسابقات و آمار بازیکنان و تیم‌ها برای همیشه پاک خواهند شد. آیا ادامه می‌دهید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllHistory()
                        showClearAllConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_clear_match_history_button")
                ) {
                    Text("پاک کردن همه", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearAllConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_clear_match_history_button")
                ) {
                    Text("انصراف", color = Color.White.copy(alpha = 0.85f))
                }
            }
        )
    }
}

@Composable
private fun HistoryTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) RadiantGold else MysticCardSurface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else RadiantGold.copy(alpha = 0.30f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF140B24) else RadiantGold,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color(0xFF140B24) else Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OverviewSummaryStrip(stats: MatchHistoryOverviewStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatSummaryChip(
            label = "کل بازی‌ها",
            value = stats.totalMatches.toPersianDigits(),
            accentColor = RadiantGold,
            modifier = Modifier.weight(1f)
        )
        StatSummaryChip(
            label = "برد قطعی",
            value = stats.decisiveMatches.toPersianDigits(),
            accentColor = HopefulEmerald,
            modifier = Modifier.weight(1f)
        )
        StatSummaryChip(
            label = "تساوی",
            value = stats.drawMatches.toPersianDigits(),
            accentColor = RadiantAmber,
            modifier = Modifier.weight(1f)
        )
        StatSummaryChip(
            label = "تیمی",
            value = stats.teamMatches.toPersianDigits(),
            accentColor = Color(0xFF38BDF8),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatSummaryChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MysticCardSurface)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.80f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MatchHistoryListContent(
    matchHistory: List<MatchHistoryRecord>,
    overviewStats: MatchHistoryOverviewStats,
    onSelectMatchDetail: (MatchHistoryRecord) -> Unit,
    onRequestDelete: (MatchHistoryRecord) -> Unit
) {
    if (matchHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("match_history_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = RadiantGold.copy(alpha = 0.40f),
                    modifier = Modifier.size(62.dp)
                )
                Text(
                    text = "هنوز مسابقه‌ای در تاریخچه ثبت نشده است",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "فقط مسابقاتی که به‌طور کامل به پایان برسند در این بخش ذخیره می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.68f),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_history_list"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OverviewSummaryStrip(overviewStats)
        }

        items(matchHistory, key = { it.id.takeIf { id -> id > 0 } ?: it.matchUuid.hashCode().toLong() }) { record ->
            MatchHistorySummaryCard(
                record = record,
                onClick = { onSelectMatchDetail(record) },
                onDeleteClick = { onRequestDelete(record) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchHistorySummaryCard(
    record: MatchHistoryRecord,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val accentColor = when {
        record.isDraw -> RadiantAmber
        record.winningTeamName != null -> HopefulEmerald
        else -> RadiantGold
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.4.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("match_history_item_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.22f))
                            .border(1.2.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (record.isDraw) Icons.Filled.Shield else Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                record.isDraw -> "تساوی / پایان بدون برنده"
                                record.winningTeamName != null -> "تیم برنده: ${record.winningTeamName}"
                                else -> "قهرمان: ${record.winnerName ?: "نامشخص"}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = accentColor,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!record.isDraw && record.winningTeamName != null && !record.winnerName.isNullOrBlank()) {
                            Text(
                                text = "برندهٔ نهایی: ${record.winnerName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Text(
                            text = record.formattedCompletedAt(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("delete_match_history_${record.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "حذف این مسابقه",
                        tint = DangerCrimson.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings tags
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoTagChip("${record.boardTheme.emoji} ${record.boardTheme.persianName}")
                InfoTagChip("📏 ${record.maxTiles.toPersianDigits()} خانه")
                InfoTagChip("🎯 ${record.difficulty.persianTitle}")
                InfoTagChip(
                    if (record.isTeamMatch) "🤝 ${record.teamMode.persianTitle}"
                    else "👥 ${record.playerCount.toPersianDigits()} بازیکن"
                )
            }

            // Top participants preview
            val topPreview = record.participants.take(3).joinToString(" • ") { p ->
                val rankEmoji = when {
                    p.isEliminated -> "💀"
                    !record.isDraw && p.rank == 1 -> "🥇"
                    !record.isDraw && p.rank == 2 -> "🥈"
                    !record.isDraw && p.rank == 3 -> "🥉"
                    else -> "${p.rank.toPersianDigits()}."
                }
                "$rankEmoji ${p.playerName}"
            }
            if (topPreview.isNotBlank()) {
                Text(
                    text = topPreview,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoTagChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.90f),
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchHistoryDetailContent(
    record: MatchHistoryRecord,
    onRequestDelete: () -> Unit
) {
    val accentColor = when {
        record.isDraw -> RadiantAmber
        record.winningTeamName != null -> HopefulEmerald
        else -> RadiantGold
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_history_detail_view"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Result Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.8.dp, accentColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when {
                            record.isDraw -> "⚖️ مسابقه بدون برنده به پایان رسید"
                            record.winningTeamName != null -> "🛡️ تیم قهرمان: ${record.winningTeamName}"
                            else -> "🏆 قهرمان مسابقه: ${record.winnerName ?: "-"}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    if (!record.isDraw && !record.winnerName.isNullOrBlank()) {
                        val titlePart = record.winnerCharacterTitle?.let { " ($it)" }.orEmpty()
                        Text(
                            text = "نفر اول: ${record.winnerName}$titlePart",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!record.winReason.isNullOrBlank()) {
                        Text(
                            text = record.winReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoTagChip("${record.boardTheme.emoji} ${record.boardTheme.persianName}")
                        InfoTagChip("📏 ${record.maxTiles.toPersianDigits()} خانه")
                        InfoTagChip("🎯 سختی: ${record.difficulty.persianTitle}")
                        InfoTagChip("🤝 ${record.teamMode.persianTitle}")
                        if (record.isTeamMatch) {
                            InfoTagChip("🏅 ${record.teamVictoryMode.persianTitle}")
                        }
                    }
                }
            }
        }

        // Rankings Header
        item {
            Text(
                text = "جدول رتبه‌بندی نهایی و جزئیات بازیکنان",
                style = MaterialTheme.typography.titleSmall,
                color = RadiantGold,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(record.participants, key = { "${it.playerId}_${it.rank}" }) { participant ->
            ParticipantDetailCard(
                participant = participant,
                maxTiles = record.maxTiles,
                isDraw = record.isDraw
            )
        }

        item {
            OutlinedButton(
                onClick = onRequestDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_delete_match_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, DangerCrimson)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = DangerCrimson
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حذف این مسابقه از تاریخچه",
                    color = DangerCrimson,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ParticipantDetailCard(
    participant: MatchParticipantRecord,
    maxTiles: Int,
    isDraw: Boolean
) {
    val rankLabel = when {
        participant.isEliminated -> "💀 حذف‌شده"
        !isDraw && participant.rank == 1 -> "🥇 رتبه ۱"
        !isDraw && participant.rank == 2 -> "🥈 رتبه ۲"
        !isDraw && participant.rank == 3 -> "🥉 رتبه ۳"
        else -> "رتبه ${participant.rank.toPersianDigits()}"
    }
    val highlightColor = when {
        participant.isWinner && !isDraw -> RadiantGold
        participant.isEliminated -> DangerCrimson
        else -> Color.White.copy(alpha = 0.25f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, highlightColor.copy(alpha = 0.65f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = rankLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (participant.isWinner && !isDraw) RadiantGold else Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${participant.characterBadgeEmoji} ${participant.playerName}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!participant.teamName.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HopefulEmerald.copy(alpha = 0.20f))
                            .border(1.dp, HopefulEmerald.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = participant.teamName,
                            style = MaterialTheme.typography.labelSmall,
                            color = HopefulEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "قهرمان: ${participant.characterTitle.ifBlank { participant.characterName }} • خانهٔ ${participant.finalTile.toPersianDigits()} از ${maxTiles.toPersianDigits()}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )

            Text(
                text = "🪜 نردبان: ${participant.laddersClimbed.toPersianDigits()} • 🐍 مار: ${participant.snakesBitten.toPersianDigits()} • 🎲 شش: ${participant.totalSixes.toPersianDigits()} • ❤️ جان: ${participant.remainingHp.toPersianDigits()}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.70f)
            )
        }
    }
}

@Composable
private fun MatchStatisticsContent(
    overviewStats: MatchHistoryOverviewStats,
    playerStats: List<PlayerHistoryStats>,
    teamStats: List<TeamHistoryStats>
) {
    if (overviewStats.totalMatches == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("match_stats_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = RadiantGold.copy(alpha = 0.40f),
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    text = "آمار عملکرد پس از پایان اولین مسابقه نمایش داده می‌شود",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("match_stats_list"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OverviewSummaryStrip(overviewStats)
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = RadiantGold,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "آمار عملکرد بازیکنان",
                    style = MaterialTheme.typography.titleSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        items(playerStats, key = { "player_${it.playerName}" }) { stat ->
            PlayerStatCard(stat = stat)
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = HopefulEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "آمار عملکرد تیم‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    color = HopefulEmerald,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (teamStats.isEmpty()) {
            item {
                Text(
                    text = "هنوز مسابقهٔ تیمی ثبت نشده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        } else {
            items(teamStats, key = { "team_${it.teamId}_${it.teamName}" }) { teamStat ->
                TeamStatCard(stat = teamStat)
            }
        }
    }
}

@Composable
private fun PlayerStatCard(stat: PlayerHistoryStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, RadiantGold.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.playerName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(HopefulEmerald.copy(alpha = 0.22f))
                        .border(1.dp, HopefulEmerald, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "درصد برد: ${stat.winRatePercent.toPersianDigits()}٪",
                        style = MaterialTheme.typography.labelSmall,
                        color = HopefulEmerald,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox("تعداد بازی", stat.matchesPlayed.toPersianDigits(), Modifier.weight(1f))
                MetricBox("برد", stat.wins.toPersianDigits(), Modifier.weight(1f))
                MetricBox("بهترین رتبه", stat.bestRank.toPersianDigits(), Modifier.weight(1f))
                MetricBox(
                    "میانگین رتبه",
                    String.format(Locale.US, "%.1f", stat.averageRank).toPersianDigitsString(),
                    Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeamStatCard(stat: TeamHistoryStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, HopefulEmerald.copy(alpha = 0.55f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ ${stat.teamName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = HopefulEmerald,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "درصد برد: ${stat.winRatePercent.toPersianDigits()}٪",
                    style = MaterialTheme.typography.labelMedium,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox("تعداد بازی", stat.matchesPlayed.toPersianDigits(), Modifier.weight(1f))
                MetricBox("تعداد برد", stat.wins.toPersianDigits(), Modifier.weight(1f))
                MetricBox("بهترین رتبه", stat.bestRank.toPersianDigits(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = RadiantGold,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
