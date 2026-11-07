package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import com.example.model.EffectIntensity
import com.example.model.EnvironmentEffectType
import com.example.model.MusicGenre
import com.example.model.toPersianDigits
import com.example.ui.GameUiState
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.RadiantGold
import kotlin.math.roundToInt

enum class StudioSectionTab(val persianTitle: String, val englishTitle: String, val emoji: String) {
    ENVIRONMENT_EFFECTS("جلوه‌های محیطی", "Environment Effects", "✨"),
    MUSIC_AND_THEMES("موسیقی و تم‌های صوتی", "Music & Themes", "🎵")
}

enum class GameEventSoundType(val persianTitle: String, val emoji: String) {
    MATCH_START("شروع بازی", "🎺"),
    DICE_ROLL("پرتاب تاس", "🎲"),
    LADDER_CLIMB("صعود از نردبان", "🪜"),
    SNAKE_SLIDE("پایین آمدن از مار", "🐍"),
    TURN_CHANGE("تغییر نوبت", "🔔"),
    FINAL_VICTORY("خانه آخر و پیروزی", "🏆")
}

private val StudioObsidianBg = Color(0xFF0E0B07)
private val StudioCardDark = Color(0xFF17130C)
private val StudioCardInner = Color(0xFF221C11)
private val StudioGoldBorder = Color(0xFFD97706)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EffectsAndMusicStudioTab(
    uiState: GameUiState,
    onToggleEnvironmentEffects: () -> Unit,
    onSelectEnvironmentEffect: (EnvironmentEffectType) -> Unit,
    onPreviewEnvironmentEffect: (EnvironmentEffectType?) -> Unit,
    onSelectEffectIntensity: (EffectIntensity) -> Unit,
    onSelectMusicGenre: (MusicGenre) -> Unit,
    onPreviewMusicGenre: (MusicGenre?) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleAmbient: () -> Unit,
    onSetMusicVolume: (Float) -> Unit,
    onSetSfxVolume: (Float) -> Unit,
    onSetAmbientVolume: (Float) -> Unit,
    onTriggerEventSoundDemo: (GameEventSoundType) -> Unit,
    onResetStudioToDefaults: () -> Unit
) {
    var activeSection by remember { mutableStateOf(StudioSectionTab.ENVIRONMENT_EFFECTS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioObsidianBg)
            .testTag("effects_and_music_studio_tab")
    ) {
        // Studio Header & Sub-Section Switcher (Black & Gold Luxury Design)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1F190F), StudioObsidianBg)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = RadiantGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "استودیو جلوه‌ها و موسیقی (Effects & Music Studio)",
                            style = MaterialTheme.typography.titleSmall,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "شخصی‌سازی کامل فضای بصری و صوتی بازی با پیش‌نمایش زنده و ذخیره دائمی",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }

                // Reset to Default Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A2113))
                        .border(1.dp, RadiantGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                        .clickable { onResetStudioToDefaults() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("studio_reset_defaults_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = RadiantGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "پیش‌فرض",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Two Distinct Sub-Sections Selector (جلوه‌های محیطی | موسیقی و تم‌های صوتی)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudioSectionTab.entries.forEach { section ->
                    val isSelected = activeSection == section
                    val bgCol by animateColorAsState(
                        targetValue = if (isSelected) RadiantGold else StudioCardDark,
                        label = "studio_subtab_${section.name}"
                    )
                    val txtCol = if (isSelected) Color(0xFF140E05) else Color.White

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgCol)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color.White else RadiantGold.copy(alpha = 0.38f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { activeSection = section }
                            .padding(horizontal = 8.dp)
                            .testTag("studio_subtab_${section.name}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = section.emoji, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = section.persianTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = txtCol,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Sub-Section Content
        when (activeSection) {
            StudioSectionTab.ENVIRONMENT_EFFECTS -> EnvironmentEffectsSectionContent(
                uiState = uiState,
                onToggleEnvironmentEffects = onToggleEnvironmentEffects,
                onSelectEnvironmentEffect = onSelectEnvironmentEffect,
                onPreviewEnvironmentEffect = onPreviewEnvironmentEffect,
                onSelectEffectIntensity = onSelectEffectIntensity
            )
            StudioSectionTab.MUSIC_AND_THEMES -> MusicAndThemesSectionContent(
                uiState = uiState,
                onSelectMusicGenre = onSelectMusicGenre,
                onPreviewMusicGenre = onPreviewMusicGenre,
                onToggleMusic = onToggleMusic,
                onToggleSound = onToggleSound,
                onToggleAmbient = onToggleAmbient,
                onSetMusicVolume = onSetMusicVolume,
                onSetSfxVolume = onSetSfxVolume,
                onSetAmbientVolume = onSetAmbientVolume,
                onTriggerEventSoundDemo = onTriggerEventSoundDemo
            )
        }
    }
}

@Composable
private fun EnvironmentEffectsSectionContent(
    uiState: GameUiState,
    onToggleEnvironmentEffects: () -> Unit,
    onSelectEnvironmentEffect: (EnvironmentEffectType) -> Unit,
    onPreviewEnvironmentEffect: (EnvironmentEffectType?) -> Unit,
    onSelectEffectIntensity: (EffectIntensity) -> Unit
) {
    val activeOrPreviewEffect = uiState.effectiveEnvironmentEffect
    val isPreviewMode = uiState.previewEnvironmentEffect != null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live Interactive Environment Effect Stage + Intensity & Enable Controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(RadiantGold, activeOrPreviewEffect.primaryColor, RadiantGold)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header + Enable/Disable Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ کنترل و پیش‌نمایش زنده جلوه محیطی",
                                style = MaterialTheme.typography.titleSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isPreviewMode) {
                                    "در حال پیش‌نمایش: ${activeOrPreviewEffect.emoji} ${activeOrPreviewEffect.persianName} (${activeOrPreviewEffect.englishName})"
                                } else {
                                    "افکت انتخاب‌شده: ${uiState.selectedEnvironmentEffect.emoji} ${uiState.selectedEnvironmentEffect.persianName}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPreviewMode) Color(0xFF38BDF8) else HopefulEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (uiState.isEnvironmentEffectsEnabled) HopefulEmerald else StudioCardInner
                                )
                                .border(
                                    1.dp,
                                    if (uiState.isEnvironmentEffectsEnabled) Color.White else RadiantGold.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onToggleEnvironmentEffects() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("studio_toggle_env_effects_button")
                        ) {
                            Text(
                                text = if (uiState.isEnvironmentEffectsEnabled) "● افکت محیطی: فعال" else "○ افکت محیطی: غیرفعال",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Live Stage Box showing sample board tiles underneath so player sees non-obscuring clarity
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(108.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF141008), Color(0xFF080603))
                                )
                            )
                            .border(1.dp, RadiantGold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    ) {
                        // Sample miniature board tiles in background to prove clarity
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(98, 99, 100).forEach { tileNum ->
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF211B10))
                                        .border(1.dp, RadiantGold.copy(alpha = 0.40f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tileNum.toPersianDigits(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = RadiantGold.copy(alpha = 0.88f),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Real-time Environment Effect Overlay
                        EnvironmentEffectsOverlay(
                            effectType = activeOrPreviewEffect,
                            intensity = uiState.environmentEffectIntensity,
                            isEnabled = uiState.isEnvironmentEffectsEnabled || isPreviewMode,
                            modifier = Modifier.fillMaxSize(),
                            isCompactPreview = false
                        )

                        // Bottom-right status pill inside stage
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.68f))
                                .border(1.dp, activeOrPreviewEffect.primaryColor.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${activeOrPreviewEffect.emoji} ${activeOrPreviewEffect.englishName} • شدت: ${uiState.environmentEffectIntensity.persianTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPreviewMode) {
                                Text(
                                    text = "✕ بستن پیش‌نمایش",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.clickable { onPreviewEnvironmentEffect(null) }
                                )
                            }
                        }
                    }

                    // Effect Intensity Selector (کم / متوسط / زیاد)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "شدت افکت‌ها:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        EffectIntensity.entries.forEach { level ->
                            val isSelected = uiState.environmentEffectIntensity == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) RadiantGold else StudioCardInner)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else RadiantGold.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectEffectIntensity(level) }
                                    .testTag("effect_intensity_${level.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${level.persianTitle} (${level.englishTitle})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color(0xFF140E05) else Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Grid of 12 Selectable Environment Effect Cards (2 per row)
        item {
            Text(
                text = "🎨 ۱۲ جلوه محیطی قابل انتخاب (بدون پوشاندن خانه‌ها و مهره‌ها):",
                style = MaterialTheme.typography.titleSmall,
                color = RadiantGold,
                fontWeight = FontWeight.ExtraBold
            )
        }

        val effectRows = EnvironmentEffectType.entries.chunked(2)
        items(effectRows.size) { rowIndex ->
            val rowItems = effectRows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { effect ->
                    val isSelected = uiState.selectedEnvironmentEffect == effect
                    val isCurrentlyPreviewing = uiState.previewEnvironmentEffect == effect
                    val isEffectActiveOnBoard = isSelected && uiState.isEnvironmentEffectsEnabled

                    EnvironmentEffectStudioCard(
                        effect = effect,
                        intensity = uiState.environmentEffectIntensity,
                        isSelected = isSelected,
                        isPreviewing = isCurrentlyPreviewing,
                        isActiveOnBoard = isEffectActiveOnBoard,
                        onTogglePreview = {
                            if (isCurrentlyPreviewing) {
                                onPreviewEnvironmentEffect(null)
                            } else {
                                onPreviewEnvironmentEffect(effect)
                            }
                        },
                        onSelect = { onSelectEnvironmentEffect(effect) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EnvironmentEffectStudioCard(
    effect: EnvironmentEffectType,
    intensity: EffectIntensity,
    isSelected: Boolean,
    isPreviewing: Boolean,
    isActiveOnBoard: Boolean,
    onTogglePreview: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isActiveOnBoard -> RadiantGold
        isPreviewing -> Color(0xFF38BDF8)
        isSelected -> RadiantGold.copy(alpha = 0.7f)
        else -> StudioGoldBorder.copy(alpha = 0.32f)
    }

    Card(
        modifier = modifier
            .border(
                width = if (isActiveOnBoard || isPreviewing) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("env_effect_card_${effect.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActiveOnBoard) Color(0xFF211A0E) else StudioCardDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Top Status Row: Emoji + Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = effect.emoji, style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            text = effect.englishName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isActiveOnBoard) RadiantGold else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = effect.persianName,
                            style = MaterialTheme.typography.labelSmall,
                            color = RadiantGold.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Active / Previewing / Inactive Badge
                val statusText = when {
                    isPreviewing -> "پیش‌نمایش"
                    isActiveOnBoard -> "فعال"
                    isSelected -> "انتخاب‌شده"
                    else -> "غیرفعال"
                }
                val statusBg = when {
                    isPreviewing -> Color(0xFF0284C7).copy(alpha = 0.30f)
                    isActiveOnBoard -> HopefulEmerald.copy(alpha = 0.25f)
                    else -> Color.White.copy(alpha = 0.08f)
                }
                val statusTextColor = when {
                    isPreviewing -> Color(0xFF7DD3FC)
                    isActiveOnBoard -> HopefulEmerald
                    else -> Color.White.copy(alpha = 0.60f)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .border(0.8.dp, statusTextColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusTextColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Miniature Visual Canvas Preview Window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0E0B06), Color(0xFF060402))
                        )
                    )
                    .border(1.dp, effect.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            ) {
                EnvironmentEffectsOverlay(
                    effectType = effect,
                    intensity = intensity,
                    isEnabled = true,
                    modifier = Modifier.fillMaxSize(),
                    isCompactPreview = true
                )
            }

            // Description
            Text(
                text = effect.description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Action Buttons: Preview (پیش‌نمایش) & Select (انتخاب)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preview Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isPreviewing) Color(0xFF0284C7) else StudioCardInner
                        )
                        .border(
                            1.dp,
                            if (isPreviewing) Color.White else RadiantGold.copy(alpha = 0.40f),
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onTogglePreview() }
                        .testTag("preview_env_effect_${effect.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = if (isPreviewing) Color.White else RadiantGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isPreviewing) "توقف" else "پیش‌نمایش",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPreviewing) Color.White else RadiantGold,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                // Select Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isActiveOnBoard) RadiantGold else HopefulEmerald.copy(alpha = 0.22f)
                        )
                        .border(
                            1.dp,
                            if (isActiveOnBoard) Color.White else HopefulEmerald,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelect() }
                        .testTag("select_env_effect_${effect.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isActiveOnBoard) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF140E05),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = if (isActiveOnBoard) "فعال ✓" else "انتخاب",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActiveOnBoard) Color(0xFF140E05) else HopefulEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MusicAndThemesSectionContent(
    uiState: GameUiState,
    onSelectMusicGenre: (MusicGenre) -> Unit,
    onPreviewMusicGenre: (MusicGenre?) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleAmbient: () -> Unit,
    onSetMusicVolume: (Float) -> Unit,
    onSetSfxVolume: (Float) -> Unit,
    onSetAmbientVolume: (Float) -> Unit,
    onTriggerEventSoundDemo: (GameEventSoundType) -> Unit
) {
    val effectiveGenre = uiState.effectiveMusicGenre
    val isPreviewingMusic = uiState.previewingMusicGenre != null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Independent Audio Mixer (Background Music, Game SFX, Ambient Nature)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, RadiantGold.copy(alpha = 0.65f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = null,
                                tint = RadiantGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "🎛️ میکسر مستقل صدا و موسیقی (Fade-In / Fade-Out نرم)",
                                style = MaterialTheme.typography.titleSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Active / Previewing Music Status Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioCardInner)
                            .border(1.dp, effectiveGenre.primaryColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPreviewingMusic) {
                                    "🎧 در حال پیش‌نمایش زنده: ${effectiveGenre.emoji} ${effectiveGenre.persianName}"
                                } else {
                                    "🎼 ژانر موسیقی فعال: ${uiState.selectedMusicGenre.emoji} ${uiState.selectedMusicGenre.persianName}"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = RadiantGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "سازبندی: ${effectiveGenre.instrumentsLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }

                        if (isPreviewingMusic) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                                    .clickable { onPreviewMusicGenre(null) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⏹ توقف پیش‌نمایش",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Channel 1: Background Music Volume & Toggle
                    AudioChannelControlRow(
                        title = "🎵 موسیقی پس‌زمینه: ${(uiState.musicVolume * 100).roundToInt().toPersianDigits()}٪",
                        isEnabled = uiState.isMusicEnabled,
                        volume = uiState.musicVolume,
                        activeColor = HopefulEmerald,
                        onToggle = onToggleMusic,
                        onVolumeChange = onSetMusicVolume,
                        toggleTag = "studio_toggle_music_button",
                        sliderTag = "studio_music_volume_slider"
                    )

                    // Channel 2: Sound Effects (SFX) Volume & Toggle
                    AudioChannelControlRow(
                        title = "🔊 صدای افکت‌های بازی (تاس، مار، نردبان): ${(uiState.sfxVolume * 100).roundToInt().toPersianDigits()}٪",
                        isEnabled = uiState.isSoundEnabled,
                        volume = uiState.sfxVolume,
                        activeColor = RadiantGold,
                        onToggle = onToggleSound,
                        onVolumeChange = onSetSfxVolume,
                        toggleTag = "studio_toggle_sfx_button",
                        sliderTag = "studio_sfx_volume_slider"
                    )

                    // Channel 3: Ambient Soundscape Volume & Toggle
                    AudioChannelControlRow(
                        title = "🍃 لایه صدای محیطی تم: ${(uiState.ambientVolume * 100).roundToInt().toPersianDigits()}٪",
                        isEnabled = uiState.isAmbientEnabled,
                        volume = uiState.ambientVolume,
                        activeColor = Color(0xFF38BDF8),
                        onToggle = onToggleAmbient,
                        onVolumeChange = onSetAmbientVolume,
                        toggleTag = "studio_toggle_ambient_button",
                        sliderTag = "studio_ambient_volume_slider"
                    )
                }
            }
        }

        // 2. Dedicated Game Event Sounds Interactive Preview Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, RadiantGold.copy(alpha = 0.40f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔔 صداها و موسیقی اختصاصی اتفاقات بازی (برای تست لمس کنید):",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "هر رویداد بازی (شروع بازی، پرتاب تاس، نردبان، مار، تغییر نوبت و رسیدن به خانه آخر) صدای اختصاصی خودش را دارد.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameEventSoundType.entries.forEach { ev ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StudioCardInner)
                                    .border(1.dp, RadiantGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .clickable { onTriggerEventSoundDemo(ev) }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag("demo_event_sound_${ev.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(text = ev.emoji, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = ev.persianTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Auto-Match Board Theme Option Card
        item {
            val autoGenre = MusicGenre.AUTO_BOARD_THEME
            val isAutoSelected = uiState.selectedMusicGenre == autoGenre
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isAutoSelected) 2.dp else 1.dp,
                        color = if (isAutoSelected) RadiantGold else StudioGoldBorder.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectMusicGenre(autoGenre) }
                    .testTag("music_genre_card_AUTO_BOARD_THEME"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAutoSelected) Color(0xFF211A0E) else StudioCardDark
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "${autoGenre.emoji} ${autoGenre.persianName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isAutoSelected) RadiantGold else Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = autoGenre.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.80f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAutoSelected) RadiantGold else StudioCardInner)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isAutoSelected) "فعال ✓" else "انتخاب",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAutoSelected) Color(0xFF140E05) else RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 4. 9 Dedicated Music Genres Section Header
        item {
            Text(
                text = "🎼 ۹ ژانر حرفه‌ای موسیقی بازی (با پیش‌نمایش صوتی و تصویری):",
                style = MaterialTheme.typography.titleSmall,
                color = RadiantGold,
                fontWeight = FontWeight.ExtraBold
            )
        }

        val genreRows = MusicGenre.selectableGenres.chunked(2)
        items(genreRows.size) { rowIndex ->
            val rowGenres = genreRows[rowIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowGenres.forEach { genre ->
                    val isSelected = uiState.selectedMusicGenre == genre
                    val isPreviewing = uiState.previewingMusicGenre == genre
                    val isActiveNow = (isSelected && uiState.isMusicEnabled) || isPreviewing

                    MusicGenreStudioCard(
                        genre = genre,
                        isSelected = isSelected,
                        isPreviewing = isPreviewing,
                        isActiveNow = isActiveNow,
                        onTogglePreview = {
                            if (isPreviewing) {
                                onPreviewMusicGenre(null)
                            } else {
                                onPreviewMusicGenre(genre)
                            }
                        },
                        onSelect = { onSelectMusicGenre(genre) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowGenres.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AudioChannelControlRow(
    title: String,
    isEnabled: Boolean,
    volume: Float,
    activeColor: Color,
    onToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    toggleTag: String,
    sliderTag: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isEnabled) activeColor else StudioCardInner)
                    .clickable { onToggle() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag(toggleTag)
            ) {
                Text(
                    text = if (isEnabled) "فعال" else "خاموش",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEnabled && activeColor == RadiantGold) Color(0xFF140E05) else Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = RadiantGold,
                activeTrackColor = activeColor
            ),
            modifier = Modifier.testTag(sliderTag)
        )
    }
}

@Composable
private fun MusicGenreStudioCard(
    genre: MusicGenre,
    isSelected: Boolean,
    isPreviewing: Boolean,
    isActiveNow: Boolean,
    onTogglePreview: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isPreviewing -> Color(0xFF38BDF8)
        isSelected -> RadiantGold
        else -> StudioGoldBorder.copy(alpha = 0.32f)
    }

    Card(
        modifier = modifier
            .border(
                width = if (isSelected || isPreviewing) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("music_genre_card_${genre.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF211A0E) else StudioCardDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Header Row: Emoji + Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = genre.emoji, style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            text = genre.englishName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) RadiantGold else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = genre.persianName,
                            style = MaterialTheme.typography.labelSmall,
                            color = RadiantGold.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val badgeLabel = when {
                    isPreviewing -> "در حال پخش"
                    isSelected -> "فعال"
                    else -> "غیرفعال"
                }
                val badgeColor = when {
                    isPreviewing -> Color(0xFF38BDF8)
                    isSelected -> HopefulEmerald
                    else -> Color.White.copy(alpha = 0.55f)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .border(0.8.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Visual Waveform & Equalizer Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A0804))
                    .border(1.dp, genre.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            ) {
                MusicGenreWaveformPreview(
                    genre = genre,
                    isActiveOrPreviewing = isActiveNow,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Instruments & Mood Description
            Text(
                text = genre.description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "🎻 ${genre.instrumentsLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = genre.primaryColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Action Buttons: Preview (پیش‌نمایش) & Select (انتخاب)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Preview Audio Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isPreviewing) Color(0xFF0284C7) else StudioCardInner)
                        .border(
                            1.dp,
                            if (isPreviewing) Color.White else RadiantGold.copy(alpha = 0.40f),
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onTogglePreview() }
                        .testTag("preview_music_genre_${genre.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = if (isPreviewing) Color.White else RadiantGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (isPreviewing) "توقف" else "پیش‌نمایش",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPreviewing) Color.White else RadiantGold,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                // Select Genre Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) RadiantGold else HopefulEmerald.copy(alpha = 0.22f))
                        .border(
                            1.dp,
                            if (isSelected) Color.White else HopefulEmerald,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelect() }
                        .testTag("select_music_genre_${genre.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF140E05),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = if (isSelected) "فعال ✓" else "انتخاب",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color(0xFF140E05) else HopefulEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
