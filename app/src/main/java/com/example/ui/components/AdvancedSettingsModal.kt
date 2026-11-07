package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.AiDifficulty
import com.example.model.AnimationQuality
import com.example.model.AnimationSpeed
import com.example.model.BoardGenerator
import com.example.model.BoardTheme
import com.example.model.CardCategory
import com.example.model.CharacterAbilityType
import com.example.model.CharacterGender
import com.example.model.CustomAbilityEffectType
import com.example.model.CustomAbilityTarget
import com.example.model.CustomCharacterSpec
import com.example.model.DiceVisualTheme
import com.example.model.DifficultyLevel
import com.example.model.EffectIntensity
import com.example.model.EnvironmentEffectType
import com.example.model.GameCharacter
import com.example.model.HeldItemVisual
import com.example.model.MusicGenre
import com.example.model.ObstacleType
import com.example.model.SettingsProfile
import com.example.model.SnakeVisualTheme
import com.example.model.TruthOrDareCard
import com.example.model.recommendedDiceTheme
import com.example.model.toPersianDigits
import com.example.sound.soundscapeInfo
import com.example.ui.GameUiState
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.RadiantGold
import kotlin.math.roundToInt

enum class AdvancedSettingsTab(val title: String, val emoji: String) {
    BOARD_AND_THEME("صفحه و تم", "🎨"),
    EFFECTS_AND_MUSIC("استودیو جلوه و صدا", "✨"),
    CUSTOM_CHARACTER("ساخت کاراکتر", "🦸"),
    CARDS_MANAGER("کارت‌ها", "🃏"),
    PROFILES("پروفایل‌ها", "💾")
}

private data class CustomHeroColorPreset(
    val label: String,
    val primaryArgb: Long,
    val secondaryArgb: Long,
    val hairArgb: Long,
    val skinArgb: Long
)

private val heroColorPresets = listOf(
    CustomHeroColorPreset("یاقوت سرخ", 0xFFF43F5E, 0xFF881337, 0xFF1E1B4B, 0xFFFDE68A),
    CustomHeroColorPreset("زمرد جنگلی", 0xFF10B981, 0xFF065F46, 0xFF451A03, 0xFFFEF3C7),
    CustomHeroColorPreset("کهکشان بنفش", 0xFFA855F7, 0xFF581C87, 0xFF0F172A, 0xFFFDE68A),
    CustomHeroColorPreset("آذرخش طلایی", 0xFFF59E0B, 0xFF9A3412, 0xFFFACC15, 0xFFFCD34D),
    CustomHeroColorPreset("اقیانوس فیروزه‌ای", 0xFF06B6D4, 0xFF155E75, 0xFFE2E8F0, 0xFFFDE68A),
    CustomHeroColorPreset("یخ قطبی", 0xFF60A5FA, 0xFF1E3A8A, 0xFFDBEAFE, 0xFFFEF3C7)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedSettingsModal(
    uiState: GameUiState,
    initialTab: AdvancedSettingsTab = AdvancedSettingsTab.BOARD_AND_THEME,
    onSelectMaxTiles: (Int) -> Unit,
    onSelectTheme: (BoardTheme) -> Unit,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onSelectAiDifficulty: (AiDifficulty) -> Unit,
    onToggleObstacleType: (ObstacleType) -> Unit,
    onSetAllObstaclesEnabled: (Boolean) -> Unit,
    onCreateCustomCharacter: (
        englishName: String,
        persianTitle: String,
        gender: CharacterGender,
        primaryColorArgb: Long,
        secondaryColorArgb: Long,
        hairColorArgb: Long,
        skinToneArgb: Long,
        heldItem: HeldItemVisual,
        target: CustomAbilityTarget,
        effectType: CustomAbilityEffectType,
        percentageOrValue: Int,
        customDescription: String
    ) -> Unit,
    onDeleteCustomCharacter: (String) -> Unit,
    onAddCard: (String, String, CardCategory) -> Unit,
    onUpdateCard: (TruthOrDareCard, String, String, CardCategory) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onToggleCardCategory: (CardCategory) -> Unit,
    onSaveSettingsProfile: (String) -> Unit,
    onLoadSettingsProfile: (SettingsProfile) -> Unit,
    onDeleteSettingsProfile: (Long) -> Unit,
    onSelectSnakeTheme: (SnakeVisualTheme) -> Unit = {},
    onSetSnakeThickness: (Float) -> Unit = {},
    onSelectDiceTheme: (DiceVisualTheme) -> Unit = {},
    onToggleMusic: () -> Unit = {},
    onToggleSound: () -> Unit = {},
    onToggleAmbient: () -> Unit = {},
    onSetMusicVolume: (Float) -> Unit = {},
    onSetSfxVolume: (Float) -> Unit = {},
    onSetAmbientVolume: (Float) -> Unit = {},
    onToggleEnvironmentEffects: () -> Unit = {},
    onSelectEnvironmentEffect: (EnvironmentEffectType) -> Unit = {},
    onPreviewEnvironmentEffect: (EnvironmentEffectType?) -> Unit = {},
    onSelectEffectIntensity: (EffectIntensity) -> Unit = {},
    onPreviewTheme: (BoardTheme?) -> Unit = {},
    onToggleAmbientAnimation: () -> Unit = {},
    onSelectBackgroundLightingLevel: (EffectIntensity) -> Unit = {},
    onToggleWeatherEffects: () -> Unit = {},
    onSelectMusicGenre: (MusicGenre) -> Unit = {},
    onPreviewMusicGenre: (MusicGenre?) -> Unit = {},
    onTriggerEventSoundDemo: (GameEventSoundType) -> Unit = {},
    onResetStudioToDefaults: () -> Unit = {},
    onSelectAnimationQuality: (AnimationQuality) -> Unit = {},
    onSelectAnimationSpeed: (AnimationSpeed) -> Unit = {},
    onToggleCameraFollow: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(initialTab) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE60B0616))
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .testTag("advanced_settings_modal"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 650.dp)
                    .fillMaxHeight(0.94f)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                RadiantGold,
                                uiState.effectiveBoardTheme.primaryAccent,
                                RadiantGold
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RadiantGold.copy(alpha = 0.2f))
                                    .border(1.5.dp, RadiantGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    tint = RadiantGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "پنل تنظیمات پیشرفته و استودیو شخصی‌سازی",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "تم‌های محیطی • استودیو جلوه‌ها و موسیقی • ساخت کاراکتر • کارت‌ها • پروفایل‌ها",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .testTag("close_advanced_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "بستن تنظیمات",
                                tint = Color.White
                            )
                        }
                    }

                    // Green Save Confirmation Toast Banner
                    AnimatedVisibility(
                        visible = uiState.cardSaveFeedbackMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (uiState.cardSaveFeedbackMessage != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HopefulEmerald.copy(alpha = 0.25f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = HopefulEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = uiState.cardSaveFeedbackMessage,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // 5-Tab Navigation Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MysticIndigoSurface.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        AdvancedSettingsTab.entries.forEach { tab ->
                            val isSelected = activeTab == tab
                            val tabBg by animateColorAsState(
                                targetValue = if (isSelected) RadiantGold else MysticCardSurface,
                                label = "settings_tab_${tab.name}"
                            )
                            val textCol = if (isSelected) Color(0xFF1A0F2E) else Color.White

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(tabBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.White else RadiantGold.copy(alpha = 0.25f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { activeTab = tab }
                                    .padding(horizontal = 3.dp)
                                    .testTag("settings_tab_${tab.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = tab.emoji, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textCol,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Tab Content Body
                    Box(modifier = Modifier.weight(1f)) {
                        when (activeTab) {
                            AdvancedSettingsTab.BOARD_AND_THEME -> BoardAndThemeSettingsTab(
                                uiState = uiState,
                                onOpenStudioTab = { activeTab = AdvancedSettingsTab.EFFECTS_AND_MUSIC },
                                onSelectMaxTiles = onSelectMaxTiles,
                                onSelectTheme = onSelectTheme,
                                onPreviewTheme = onPreviewTheme,
                                onToggleEnvironmentEffects = onToggleEnvironmentEffects,
                                onSelectEffectIntensity = onSelectEffectIntensity,
                                onToggleAmbientAnimation = onToggleAmbientAnimation,
                                onSelectBackgroundLightingLevel = onSelectBackgroundLightingLevel,
                                onToggleWeatherEffects = onToggleWeatherEffects,
                                onSelectSnakeTheme = onSelectSnakeTheme,
                                onSetSnakeThickness = onSetSnakeThickness,
                                onSelectDiceTheme = onSelectDiceTheme,
                                onSelectDifficulty = onSelectDifficulty,
                                onSelectAiDifficulty = onSelectAiDifficulty,
                                onToggleObstacleType = onToggleObstacleType,
                                onSetAllObstaclesEnabled = onSetAllObstaclesEnabled,
                                onToggleMusic = onToggleMusic,
                                onToggleAmbient = onToggleAmbient,
                                onSetMusicVolume = onSetMusicVolume,
                                onSetAmbientVolume = onSetAmbientVolume,
                                onSelectAnimationQuality = onSelectAnimationQuality,
                                onSelectAnimationSpeed = onSelectAnimationSpeed,
                                onToggleCameraFollow = onToggleCameraFollow
                            )
                            AdvancedSettingsTab.EFFECTS_AND_MUSIC -> EffectsAndMusicStudioTab(
                                uiState = uiState,
                                onToggleEnvironmentEffects = onToggleEnvironmentEffects,
                                onSelectEnvironmentEffect = onSelectEnvironmentEffect,
                                onPreviewEnvironmentEffect = onPreviewEnvironmentEffect,
                                onSelectEffectIntensity = onSelectEffectIntensity,
                                onSelectMusicGenre = onSelectMusicGenre,
                                onPreviewMusicGenre = onPreviewMusicGenre,
                                onToggleMusic = onToggleMusic,
                                onToggleSound = onToggleSound,
                                onToggleAmbient = onToggleAmbient,
                                onSetMusicVolume = onSetMusicVolume,
                                onSetSfxVolume = onSetSfxVolume,
                                onSetAmbientVolume = onSetAmbientVolume,
                                onTriggerEventSoundDemo = onTriggerEventSoundDemo,
                                onResetStudioToDefaults = onResetStudioToDefaults
                            )
                            AdvancedSettingsTab.CUSTOM_CHARACTER -> CustomCharacterCreatorTab(
                                customCharacters = uiState.customCharacters,
                                onCreateCustomCharacter = onCreateCustomCharacter,
                                onDeleteCustomCharacter = onDeleteCustomCharacter
                            )
                            AdvancedSettingsTab.CARDS_MANAGER -> EmbeddedCardsManagerTab(
                                cards = uiState.truthOrDareCards,
                                enabledCategories = uiState.enabledCardCategories,
                                onAddCard = onAddCard,
                                onUpdateCard = onUpdateCard,
                                onDeleteCard = onDeleteCard,
                                onToggleCategoryEnabled = onToggleCardCategory
                            )
                            AdvancedSettingsTab.PROFILES -> SettingsProfilesTab(
                                uiState = uiState,
                                onSaveProfile = onSaveSettingsProfile,
                                onLoadProfile = onLoadSettingsProfile,
                                onDeleteProfile = onDeleteSettingsProfile
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BoardAndThemeSettingsTab(
    uiState: GameUiState,
    onOpenStudioTab: () -> Unit,
    onSelectMaxTiles: (Int) -> Unit,
    onSelectTheme: (BoardTheme) -> Unit,
    onPreviewTheme: (BoardTheme?) -> Unit,
    onToggleEnvironmentEffects: () -> Unit,
    onSelectEffectIntensity: (EffectIntensity) -> Unit,
    onToggleAmbientAnimation: () -> Unit,
    onSelectBackgroundLightingLevel: (EffectIntensity) -> Unit,
    onToggleWeatherEffects: () -> Unit,
    onSelectSnakeTheme: (SnakeVisualTheme) -> Unit,
    onSetSnakeThickness: (Float) -> Unit,
    onSelectDiceTheme: (DiceVisualTheme) -> Unit,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onSelectAiDifficulty: (AiDifficulty) -> Unit,
    onToggleObstacleType: (ObstacleType) -> Unit,
    onSetAllObstaclesEnabled: (Boolean) -> Unit,
    onToggleMusic: () -> Unit,
    onToggleAmbient: () -> Unit,
    onSetMusicVolume: (Float) -> Unit,
    onSetAmbientVolume: (Float) -> Unit,
    onSelectAnimationQuality: (AnimationQuality) -> Unit,
    onSelectAnimationSpeed: (AnimationSpeed) -> Unit,
    onToggleCameraFollow: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("board_and_theme_settings_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0.0. High-FPS Animation Quality & Speed Settings Card (تنظیمات کیفیت و سرعت انیمیشن — 60/90/120 FPS)
        item {
            AnimationEngineSettingsCard(
                selectedQuality = uiState.animationQuality,
                selectedSpeed = uiState.animationSpeed,
                isCameraFollowEnabled = uiState.isCameraFollowEnabled,
                onSelectQuality = onSelectAnimationQuality,
                onSelectSpeed = onSelectAnimationSpeed,
                onToggleCameraFollow = onToggleCameraFollow
            )
        }
        // 0. Prominent Quick-Launch Card for Effects & Music Studio
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.8.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(RadiantGold, uiState.selectedEnvironmentEffect.primaryColor, RadiantGold)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onOpenStudioTab() }
                    .testTag("open_effects_music_studio_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF19130A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "✨🎵 استودیو جلوه‌های محیطی و موسیقی حرفه‌ای",
                            style = MaterialTheme.typography.titleMedium,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "جلوه فعال: ${uiState.selectedEnvironmentEffect.emoji} ${uiState.selectedEnvironmentEffect.persianName} (${uiState.environmentEffectIntensity.persianTitle}) • موسیقی: ${uiState.selectedMusicGenre.emoji} ${uiState.selectedMusicGenre.persianName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HopefulEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "۱۲ افکت محیطی زنده (Golden Dust، Galaxy، Aurora، Sakura و...) + ۹ ژانر موسیقی سینمایی و پیش‌نمایش زنده",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(RadiantGold)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ورود به استودیو",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF140E05),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 1. Dedicated Game Themes Section (تم‌های محیطی بازی — Game Themes)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_themes_section_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = RadiantGold
                            )
                            Column {
                                Text(
                                    text = "تم‌های محیطی بازی (Game Themes)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "۵ دنیای زنده و کامل با محیط اطراف، قاب اختصاصی، رنگ خانه‌ها و افکت‌های هماهنگ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BoardTheme.entries.forEach { theme ->
                            val isApplied = uiState.boardTheme == theme
                            val isPreviewing = uiState.previewBoardTheme == theme
                            val isHighlighted = isApplied || isPreviewing

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isHighlighted) 2.5.dp else 1.2.dp,
                                        color = when {
                                            isPreviewing -> Color(0xFF38BDF8)
                                            isApplied -> RadiantGold
                                            else -> theme.primaryAccent.copy(alpha = 0.45f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onSelectTheme(theme) }
                                    .testTag("theme_option_${theme.name}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = theme.cardSurfaceColor
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Top Header Row: Theme Code + English/Persian Name + Status Badge
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
                                            Text(
                                                text = theme.emoji,
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                            Column {
                                                Text(
                                                    text = "${theme.themeCode} — ${theme.englishName}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = RadiantGold,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = theme.persianName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }

                                        if (isPreviewing) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF0284C7).copy(alpha = 0.32f))
                                                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "پیش‌نمایش زنده 👁️",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFE0F2FE),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else if (isApplied) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(HopefulEmerald.copy(alpha = 0.25f))
                                                    .border(1.dp, HopefulEmerald, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "تم فعال ✓",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = HopefulEmerald,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }

                                    // Animated Miniature World + Tile Palette Strip Preview
                                    ThemeWorldMiniaturePreview(
                                        boardTheme = theme,
                                        isAmbientAnimationEnabled = uiState.isAmbientAnimationEnabled
                                    )

                                    // Short Description & Soundscape Info
                                    Text(
                                        text = theme.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.88f)
                                    )
                                    val themeSoundscape = theme.soundscapeInfo()
                                    Text(
                                        text = "${themeSoundscape.badgeEmoji} موسیقی و صدای محیطی: ${themeSoundscape.instrumentsLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RadiantGold.copy(alpha = 0.92f),
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Preview & Apply Theme Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onPreviewTheme(theme) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .testTag("preview_theme_btn_${theme.name}"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isPreviewing) Color(0xFF0284C7) else Color.White.copy(alpha = 0.12f),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isPreviewing) "توقف پیش‌نمایش" else "پیش‌نمایش (Preview)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = { onSelectTheme(theme) },
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(40.dp)
                                                .testTag("apply_theme_btn_${theme.name}"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isApplied && !isPreviewing) HopefulEmerald else RadiantGold,
                                                contentColor = Color(0xFF140E05)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isApplied && !isPreviewing) "اعمال شده ✓" else "اعمال تم (Apply Theme)",
                                                style = MaterialTheme.typography.labelMedium,
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
        }

        // 1.1. Dedicated 5 Granular Environment Effects Controls (تنظیمات اختصاصی جلوه‌های محیطی)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("granular_environment_settings_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🌦️ تنظیمات اختصاصی جلوه‌های محیطی (Environment Controls)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "کنترل کامل روی افکت‌های محیطی، تراکم ذرات، انیمیشن محیط اطراف، نورپردازی و افکت‌های آب‌وهوایی (مثلاً می‌توانید تم قطب یخی را انتخاب کنید اما بارش برف را خاموش کنید).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )

                    // Control 1: Environment Effects On/Off (جلوه‌های محیطی: روشن / خاموش)
                    EnvironmentToggleControlRow(
                        title = "۱. جلوه‌های محیطی (Environment Effects)",
                        subtitle = "ذرات شناور و جلوه‌های نوری محیط",
                        isEnabled = uiState.isEnvironmentEffectsEnabled,
                        onToggle = onToggleEnvironmentEffects,
                        testTag = "env_setting_toggle_effects"
                    )

                    // Control 2: Particle Density Low/Medium/High (تراکم ذرات: کم / متوسط / زیاد)
                    EnvironmentThreeLevelControlRow(
                        title = "۲. تراکم ذرات (Particle Density)",
                        selectedLevel = uiState.environmentEffectIntensity,
                        onSelectLevel = onSelectEffectIntensity,
                        tagPrefix = "env_setting_density"
                    )

                    // Control 3: Ambient Animation On/Off (انیمیشن محیط اطراف: روشن / خاموش)
                    EnvironmentToggleControlRow(
                        title = "۳. انیمیشن محیط اطراف (Ambient Animation)",
                        subtitle = "حرکت آرام برگ‌ها، موج‌ها، آب و درخشش محیط",
                        isEnabled = uiState.isAmbientAnimationEnabled,
                        onToggle = onToggleAmbientAnimation,
                        testTag = "env_setting_toggle_ambient_anim"
                    )

                    // Control 4: Background Lighting Low/Medium/High (نورپردازی پس‌زمینه: کم / متوسط / زیاد)
                    EnvironmentThreeLevelControlRow(
                        title = "۴. نورپردازی پس‌زمینه (Background Lighting)",
                        selectedLevel = uiState.backgroundLightingLevel,
                        onSelectLevel = onSelectBackgroundLightingLevel,
                        tagPrefix = "env_setting_lighting"
                    )

                    // Control 5: Weather Effects On/Off (افکت‌های آب‌وهوایی: روشن / خاموش)
                    EnvironmentToggleControlRow(
                        title = "۵. افکت‌های آب‌وهوایی (Weather Effects)",
                        subtitle = "بارش برف در قطب یخی، موج دریا در ساحل، مه جنگل و موج گرمای کویر",
                        isEnabled = uiState.isWeatherEffectsEnabled,
                        onToggle = onToggleWeatherEffects,
                        testTag = "env_setting_toggle_weather"
                    )
                }
            }
        }

        // 1.25. Dedicated Snake Visual Themes & Slender Thickness Control (بدون حواس‌پرتی)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🐍 تم ظاهری و ضخامت مارها (باریک و بدون حواس‌پرتی)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "مارها با طراحی ظریف و غیرمزاحم رندر می‌شوند تا شماره‌های خانه‌ها و مهره‌ها کاملاً واضح باشند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SnakeVisualTheme.entries.chunked(2).forEach { rowSnakeThemes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowSnakeThemes.forEach { sTheme ->
                                    val isSelected = uiState.snakeVisualTheme == sTheme
                                    val previewPalettes = sTheme.customPalettes ?: uiState.boardTheme.snakePalettes
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                width = if (isSelected) 2.2.dp else 1.dp,
                                                color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { onSelectSnakeTheme(sTheme) }
                                            .testTag("snake_theme_option_${sTheme.name}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF281C3D) else MysticCardSurface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Text(
                                                        text = sTheme.emoji,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = sTheme.persianName,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = if (isSelected) RadiantGold else Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = RadiantGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                previewPalettes.take(3).forEach { pal ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(pal.midBody)
                                                            .border(1.dp, pal.highlight, CircleShape)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = sTheme.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.78f),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Snake Thickness Slider (ضخامت مارها)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📏 ضخامت مارها: ${(uiState.snakeThicknessScale * 100).roundToInt().toPersianDigits()}٪",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    uiState.snakeThicknessScale <= 0.70f -> "فوق باریک (مینیمال)"
                                    uiState.snakeThicknessScale <= 0.92f -> "ظریف و استاندارد"
                                    else -> "کلاسیک"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = uiState.snakeThicknessScale,
                            onValueChange = onSetSnakeThickness,
                            valueRange = 0.55f..1.25f,
                            colors = SliderDefaults.colors(
                                thumbColor = RadiantGold,
                                activeTrackColor = HopefulEmerald
                            ),
                            modifier = Modifier.testTag("settings_snake_thickness_slider")
                        )
                    }
                }
            }
        }

        // 1.35. 3D Dice Appearance & Material Selector (چوبی، نئون، مرمر، فلزی، کریستال، طلایی و هماهنگ با تم صفحه)
        item {
            var previewDiceFace by remember { mutableIntStateOf(6) }
            var previewRotationDeg by remember { mutableFloatStateOf(0f) }
            val activeResolvedDiceTheme = uiState.effectiveDiceTheme
            val recommendedForBoard = uiState.boardTheme.recommendedDiceTheme()
            val isAutoMatchSelected = uiState.diceVisualTheme == DiceVisualTheme.AUTO_MATCH_BOARD

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎲 ظاهر و جنس تاس سه‌بعدی (هماهنگ با تم یا انتخاب دستی)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "پوسته سه‌بعدی تاس را از بین چوبی، نئون، سنگ مرمر، فلزی، کریستال یخی یا طلایی انتخاب کنید یا بگذارید خودکار با تم صفحه ست شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.84f)
                    )

                    // Live Interactive 3D Dice Preview Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MysticCardSurface)
                            .border(1.5.dp, activeResolvedDiceTheme.edgeOuterColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThreeDimensionalDiceView(
                            diceValue = previewDiceFace,
                            isRolling = false,
                            rotationDeg = previewRotationDeg,
                            sixGlow = previewDiceFace == 6,
                            accentColor = activeResolvedDiceTheme.edgeOuterColor,
                            diceVisualTheme = activeResolvedDiceTheme,
                            boxSize = 74.dp,
                            onClick = {
                                previewDiceFace = (previewDiceFace % 6) + 1
                                previewRotationDeg += 120f
                            },
                            testTag = "settings_dice_3d_preview"
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${activeResolvedDiceTheme.emoji} پیش‌نمایش زنده: ${activeResolvedDiceTheme.persianName}",
                                style = MaterialTheme.typography.titleSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activeResolvedDiceTheme.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 2
                            )
                            Text(
                                text = "👆 روی تاس سه‌بعدی بزنید تا بچرخد و وجه‌های مختلف را ببینید",
                                style = MaterialTheme.typography.labelSmall,
                                color = HopefulEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Auto-Match Board Theme Option Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isAutoMatchSelected) 2.2.dp else 1.dp,
                                color = if (isAutoMatchSelected) RadiantGold else Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                previewRotationDeg += 90f
                                onSelectDiceTheme(DiceVisualTheme.AUTO_MATCH_BOARD)
                            }
                            .testTag("dice_theme_option_AUTO_MATCH_BOARD"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAutoMatchSelected) Color(0xFF281C3D) else MysticCardSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🎨 هماهنگ با تم صفحه (خودکار)",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isAutoMatchSelected) RadiantGold else Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "تم فعلی (${uiState.boardTheme.persianName}) ← تاس ${recommendedForBoard.emoji} ${recommendedForBoard.persianName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HopefulEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isAutoMatchSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = RadiantGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 6 Explicit 3D Dice Material Options (Obsidian Gold, Wooden, Neon, Marble, Metallic, Crystal Ice)
                    val explicitDiceThemes = remember {
                        DiceVisualTheme.entries.filter { it != DiceVisualTheme.AUTO_MATCH_BOARD }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        explicitDiceThemes.chunked(2).forEach { rowDiceThemes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowDiceThemes.forEach { dTheme ->
                                    val isSelected = uiState.diceVisualTheme == dTheme
                                    val isRecommendedForCurrentBoard = dTheme == recommendedForBoard
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                width = if (isSelected) 2.2.dp else 1.dp,
                                                color = when {
                                                    isSelected -> RadiantGold
                                                    isRecommendedForCurrentBoard -> HopefulEmerald.copy(alpha = 0.65f)
                                                    else -> Color.White.copy(alpha = 0.22f)
                                                },
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                previewRotationDeg += 90f
                                                onSelectDiceTheme(dTheme)
                                            }
                                            .testTag("dice_theme_option_${dTheme.name}"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF281C3D) else MysticCardSurface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
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
                                                    Text(
                                                        text = dTheme.emoji,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = dTheme.persianName,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = if (isSelected) RadiantGold else Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CheckCircle,
                                                        contentDescription = null,
                                                        tint = RadiantGold,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Material color swatch row (Face, Frame, 1-Pip, 6-Pip)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                listOf(
                                                    dTheme.faceTopColor,
                                                    dTheme.edgeOuterColor,
                                                    dTheme.pipOneColor,
                                                    dTheme.pipSixColor
                                                ).forEach { swatch ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(swatch)
                                                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                                    )
                                                }
                                                if (isRecommendedForCurrentBoard) {
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "★ تم فعلی",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = HopefulEmerald,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = dTheme.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.78f),
                                                maxLines = 2
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

        // 1.5. Dynamic Background Music & Ambient Soundscape Mixer
        item {
            val activeSoundscape = uiState.boardTheme.soundscapeInfo()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎵 سیستم موسیقی پس‌زمینه و افکت‌های محیطی (${uiState.boardTheme.persianName})",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MysticCardSurface)
                            .border(1.dp, RadiantGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${activeSoundscape.badgeEmoji} موسیقی فعال: ${activeSoundscape.musicTitle}",
                            style = MaterialTheme.typography.labelLarge,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "🍃 لایه محیطی: ${activeSoundscape.ambientTitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        Text(
                            text = "سازبندی: ${activeSoundscape.instrumentsLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HopefulEmerald
                        )
                    }

                    // Music Volume & Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎼 بلندی موسیقی متن تم: ${(uiState.musicVolume * 100).roundToInt().toPersianDigits()}٪",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (uiState.isMusicEnabled) HopefulEmerald else MysticCardSurface)
                                    .clickable { onToggleMusic() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("settings_toggle_music_button")
                            ) {
                                Text(
                                    text = if (uiState.isMusicEnabled) "فعال" else "خاموش",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Slider(
                            value = uiState.musicVolume,
                            onValueChange = onSetMusicVolume,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = RadiantGold,
                                activeTrackColor = HopefulEmerald
                            ),
                            modifier = Modifier.testTag("settings_music_volume_slider")
                        )
                    }

                    // Ambient Nature Layer Volume & Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🍃 بلندی افکت‌های محیطی ملایم: ${(uiState.ambientVolume * 100).roundToInt().toPersianDigits()}٪",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (uiState.isAmbientEnabled) RadiantGold else MysticCardSurface)
                                    .clickable { onToggleAmbient() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("settings_toggle_ambient_button")
                            ) {
                                Text(
                                    text = if (uiState.isAmbientEnabled) "فعال" else "خاموش",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (uiState.isAmbientEnabled) Color(0xFF1A0F2E) else Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Slider(
                            value = uiState.ambientVolume,
                            onValueChange = onSetAmbientVolume,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = RadiantGold,
                                activeTrackColor = RadiantGold
                            ),
                            modifier = Modifier.testTag("settings_ambient_volume_slider")
                        )
                    }
                }
            }
        }

        // 2. Board Tile Count (50 / 100 / 150 / 200)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📐 تعداد خانه‌های صفحه بازی (بازسازی هوشمند مار و پله‌ها)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "با تغییر تعداد خانه‌ها، مارها، پله‌ها و موانع به صورت خودکار و متناسب با ابعاد جدید بازسازی می‌شوند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BoardGenerator.supportedTileCounts.forEach { count ->
                            val isSelected = uiState.maxTiles == count
                            val label = when (count) {
                                50 -> "سریع"
                                100 -> "کلاسیک"
                                150 -> "حماسه‌ای"
                                else -> "ماراتن"
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) HopefulEmerald else MysticCardSurface)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else HopefulEmerald.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onSelectMaxTiles(count) }
                                    .padding(vertical = 10.dp)
                                    .testTag("max_tiles_option_$count"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${count.toPersianDigits()} خانه",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Board Difficulty + Single-Player AI Difficulty
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔥 سطح سختی بازی و هوش مصنوعی (حالت تک‌نفره)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "سطح سختی مارها و موانع:",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DifficultyLevel.entries.forEach { level ->
                            val isSelected = uiState.difficulty == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) level.badgeColor else MysticCardSurface)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color.White else level.badgeColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectDifficulty(level) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level.persianTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF140B24) else Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = "🤖 سطح هوش مصنوعی ربات حریف (در بازی ۱ نفره):",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AiDifficulty.entries.forEach { aiLevel ->
                            val isSelected = uiState.aiDifficulty == aiLevel
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) aiLevel.badgeColor else MysticCardSurface)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) Color.White else aiLevel.badgeColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectAiDifficulty(aiLevel) }
                                    .testTag("ai_difficulty_option_${aiLevel.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = aiLevel.persianTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF140B24) else Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = uiState.aiDifficulty.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // 4. Individual Obstacle Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
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
                        Text(
                            text = "⚡ مدیریت تک‌تک موانع خاص",
                            style = MaterialTheme.typography.titleMedium,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "همه فعال",
                                style = MaterialTheme.typography.labelSmall,
                                color = HopefulEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HopefulEmerald.copy(alpha = 0.16f))
                                    .clickable { onSetAllObstaclesEnabled(true) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "حالت کلاسیک (بدون مانع)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF758F),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DangerCrimson.copy(alpha = 0.16f))
                                    .clickable { onSetAllObstaclesEnabled(false) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    ObstacleType.entries.forEach { obstacle ->
                        val isEnabled = obstacle in uiState.enabledObstacles
                        val emoji = when (obstacle) {
                            ObstacleType.WORM -> "🪱"
                            ObstacleType.REVERSE -> "🔄"
                            ObstacleType.SWAMP -> "🌿"
                            ObstacleType.WELL -> "🕳️"
                            ObstacleType.WIND -> "🌪️"
                            ObstacleType.LUCK -> "🍀"
                            ObstacleType.STORM -> "⚡"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isEnabled) obstacle.badgeColor.copy(alpha = 0.2f)
                                    else MysticCardSurface.copy(alpha = 0.5f)
                                )
                                .border(
                                    1.2.dp,
                                    if (isEnabled) obstacle.badgeColor else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onToggleObstacleType(obstacle) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = emoji, style = MaterialTheme.typography.titleLarge)
                                Column {
                                    Text(
                                        text = obstacle.persianName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = obstacle.shortDescription,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.78f)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isEnabled) obstacle.badgeColor else Color.White.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isEnabled) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF140B24),
                                        modifier = Modifier.size(18.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomCharacterCreatorTab(
    customCharacters: List<GameCharacter>,
    onCreateCustomCharacter: (
        englishName: String,
        persianTitle: String,
        gender: CharacterGender,
        primaryColorArgb: Long,
        secondaryColorArgb: Long,
        hairColorArgb: Long,
        skinToneArgb: Long,
        heldItem: HeldItemVisual,
        target: CustomAbilityTarget,
        effectType: CustomAbilityEffectType,
        percentageOrValue: Int,
        customDescription: String
    ) -> Unit,
    onDeleteCustomCharacter: (String) -> Unit
) {
    var selectedTarget by remember { mutableStateOf(CustomAbilityTarget.SNAKES) }
    var selectedEffectType by remember { mutableStateOf(CustomAbilityEffectType.NEUTRALIZE_IMMUNE) }
    var percentageSlider by remember { mutableFloatStateOf(65f) }
    var englishNameInput by remember { mutableStateOf("") }
    var persianTitleInput by remember { mutableStateOf("") }
    var customDescInput by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(CharacterGender.MALE) }
    var selectedHeldItem by remember { mutableStateOf(HeldItemVisual.SLAYER_BLADE) }
    var selectedColorIdx by remember { mutableIntStateOf(0) }

    val colorPreset = heroColorPresets[selectedColorIdx % heroColorPresets.size]
    val pctInt = percentageSlider.roundToInt().coerceIn(10, 100)

    val previewSpec = remember(selectedTarget, selectedEffectType, pctInt, customDescInput) {
        CustomCharacterSpec(
            target = selectedTarget,
            effectType = selectedEffectType,
            percentageOrValue = pctInt,
            customDescription = customDescInput
        )
    }

    val previewCharacter = remember(
        englishNameInput,
        persianTitleInput,
        selectedGender,
        colorPreset,
        selectedHeldItem,
        previewSpec
    ) {
        GameCharacter(
            id = "preview_custom",
            defaultEnglishName = englishNameInput.ifBlank { "Vortex" },
            persianTitle = persianTitleInput.ifBlank { previewSpec.buildPersianAbilityTitle() },
            gender = selectedGender,
            primaryColor = Color(colorPreset.primaryArgb),
            secondaryColor = Color(colorPreset.secondaryArgb),
            hairColor = Color(colorPreset.hairArgb),
            skinTone = Color(colorPreset.skinArgb),
            abilityType = CharacterAbilityType.CUSTOM_HERO,
            heldItem = selectedHeldItem,
            isCustom = true,
            customSpec = previewSpec
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Character Card Preview Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, RadiantGold, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CharacterPortraitAvatar(
                        character = previewCharacter,
                        size = 76.dp,
                        isSelected = true
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "پیش‌نمایش زنده قهرمان سفارشی",
                            style = MaterialTheme.typography.labelSmall,
                            color = RadiantGold
                        )
                        Text(
                            text = "${previewCharacter.englishName} (${previewCharacter.gender.persianLabel}) — ${previewCharacter.persianTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${previewCharacter.displayBadgeEmoji} ${previewSpec.buildShortSummary()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = HopefulEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Step 1: Select Ability Target
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "۱. قابلیت روی چه چیزی اثر بگذارد؟",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomAbilityTarget.entries.forEach { target ->
                            val isSelected = selectedTarget == target
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) target.badgeColor.copy(alpha = 0.28f)
                                        else MysticCardSurface
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) target.badgeColor else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTarget = target }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("custom_char_target_${target.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = target.emoji)
                                Text(
                                    text = target.persianTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Select Effect Type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "۲. نوع اثر قابلیت را انتخاب کنید:",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomAbilityEffectType.entries.forEach { effect ->
                            val isSelected = selectedEffectType == effect
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) RadiantGold.copy(alpha = 0.25f)
                                        else MysticCardSurface
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedEffectType = effect }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("custom_char_effect_${effect.name}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = effect.emoji)
                                Text(
                                    text = effect.persianTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Exact Percentage or Value
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "۳. تنظیم درصد یا قدرت دقیق قابلیت:",
                            style = MaterialTheme.typography.titleSmall,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${pctInt.toPersianDigits()}٪",
                            style = MaterialTheme.typography.titleLarge,
                            color = HopefulEmerald,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Slider(
                        value = percentageSlider,
                        onValueChange = { percentageSlider = it },
                        valueRange = 15f..100f,
                        steps = 16,
                        colors = SliderDefaults.colors(
                            thumbColor = RadiantGold,
                            activeTrackColor = HopefulEmerald,
                            inactiveTrackColor = MysticCardSurface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(25, 50, 65, 80, 100).forEach { presetPct ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (pctInt == presetPct) HopefulEmerald
                                        else MysticCardSurface
                                    )
                                    .clickable { percentageSlider = presetPct.toFloat() }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${presetPct.toPersianDigits()}٪",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 4: English Name, Gender, Avatar Item & Short Description
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "۴. نام انگلیسی، جنسیت، تصویر و توضیح کوتاه:",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = englishNameInput,
                            onValueChange = { englishNameInput = it.take(16) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_char_english_name_input"),
                            singleLine = true,
                            label = { Text("نام انگلیسی (مثلاً Vortex)") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RadiantGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = persianTitleInput,
                            onValueChange = { persianTitleInput = it.take(22) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("لقب فارسی (مثلاً شوالیه کهکشان)") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RadiantGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Gender selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "جنسیت:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        CharacterGender.entries.forEach { g ->
                            val isSelected = selectedGender == g
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) RadiantGold else MysticCardSurface)
                                    .clickable { selectedGender = g },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g.persianLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF140B24) else Color.White
                                )
                            }
                        }
                    }

                    // Held Weapon / Prop Visual Selector
                    Text(
                        text = "نماد و سلاح کاراکتر در تصویر:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(HeldItemVisual.entries) { itemVisual ->
                            val isSelected = selectedHeldItem == itemVisual
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) RadiantGold.copy(alpha = 0.26f)
                                        else MysticCardSurface
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) RadiantGold else Color.White.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedHeldItem = itemVisual }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = itemVisual.iconEmoji)
                                Text(
                                    text = itemVisual.persianLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Color Palette Selector
                    Text(
                        text = "رنگ‌بندی لباس و هاله کاراکتر:",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(heroColorPresets.size) { idx ->
                            val preset = heroColorPresets[idx]
                            val isSelected = selectedColorIdx == idx
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(preset.primaryArgb).copy(alpha = 0.25f))
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) RadiantGold else Color(preset.primaryArgb),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedColorIdx = idx }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(preset.primaryArgb))
                                )
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customDescInput,
                        onValueChange = { customDescInput = it.take(120) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("توضیح کوتاه دلخواه (اختیاری)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = {
                            val finalEng = englishNameInput.trim().ifBlank { "Hero${customCharacters.size + 1}" }
                            val finalTitle = persianTitleInput.trim().ifBlank { previewSpec.buildPersianAbilityTitle() }
                            onCreateCustomCharacter(
                                finalEng,
                                finalTitle,
                                selectedGender,
                                colorPreset.primaryArgb,
                                colorPreset.secondaryArgb,
                                colorPreset.hairArgb,
                                colorPreset.skinArgb,
                                selectedHeldItem,
                                selectedTarget,
                                selectedEffectType,
                                pctInt,
                                customDescInput
                            )
                            englishNameInput = ""
                            persianTitleInput = ""
                            customDescInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_custom_character_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HopefulEmerald,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ساخت و ذخیره کاراکتر سفارشی در لیست قهرمانان",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Saved Custom Characters List
        if (customCharacters.isNotEmpty()) {
            item {
                Text(
                    text = "👑 کاراکترهای سفارشی ساخته‌شده توسط شما (${customCharacters.size.toPersianDigits()}):",
                    style = MaterialTheme.typography.titleSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(customCharacters, key = { it.id }) { customChar ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, customChar.primaryColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CharacterPortraitAvatar(
                            character = customChar,
                            size = 52.dp,
                            isSelected = false
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${customChar.englishName} — ${customChar.persianTitle}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = customChar.displayAbilityShortSummary,
                                style = MaterialTheme.typography.labelSmall,
                                color = RadiantGold
                            )
                        }
                        IconButton(onClick = { onDeleteCustomCharacter(customChar.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "حذف کاراکتر",
                                tint = Color(0xFFFF758F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmbeddedCardsManagerTab(
    cards: List<TruthOrDareCard>,
    enabledCategories: Set<CardCategory>,
    onAddCard: (String, String, CardCategory) -> Unit,
    onUpdateCard: (TruthOrDareCard, String, String, CardCategory) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onToggleCategoryEnabled: (CardCategory) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CardCategory.FAMILY) }
    var editingCard by remember { mutableStateOf<TruthOrDareCard?>(null) }
    var filterCategory by remember { mutableStateOf<CardCategory?>(null) }

    val filteredCards = remember(cards, filterCategory) {
        if (filterCategory == null) cards else cards.filter { it.category == filterCategory }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (editingCard == null) "➕ افزودن کارت جدید جرئت یا حقیقت" else "✏️ ویرایش کارت",
                        style = MaterialTheme.typography.titleSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("جرئت", "حقیقت", "چالش مار").forEach { preset ->
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (titleInput == preset) Color(0xFF140B24) else RadiantGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (titleInput == preset) RadiantGold else MysticCardSurface)
                                    .clickable { titleInput = preset }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it.take(30) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("۱. تایتل بالای کارت (مثلاً جرئت یا حقیقت)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = bodyInput,
                        onValueChange = { bodyInput = it.take(220) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("۲. متن اصلی کارت (سؤال یا چالش زیر تایتل)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardCategory.entries.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) cat.primaryColor else MysticCardSurface)
                                    .clickable { selectedCategory = cat },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cat.badgeEmoji} ${cat.shortTitle}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (titleInput.isNotBlank() && bodyInput.isNotBlank()) {
                                val currentEdit = editingCard
                                if (currentEdit != null) {
                                    onUpdateCard(currentEdit, titleInput, bodyInput, selectedCategory)
                                    editingCard = null
                                } else {
                                    onAddCard(titleInput, bodyInput, selectedCategory)
                                }
                                titleInput = ""
                                bodyInput = ""
                            }
                        },
                        enabled = titleInput.isNotBlank() && bodyInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HopefulEmerald)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (editingCard == null) "ذخیره کارت در حافظه" else "بروزرسانی کارت",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Category Filter + Active Category Toggles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (filterCategory == null) RadiantGold else MysticCardSurface)
                        .clickable { filterCategory = null }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "همه (${cards.size.toPersianDigits()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (filterCategory == null) Color(0xFF140B24) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                CardCategory.entries.forEach { cat ->
                    val count = cards.count { it.category == cat }
                    val isSelected = filterCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) cat.primaryColor else MysticCardSurface)
                            .clickable { filterCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${cat.badgeEmoji} ${cat.shortTitle} (${count.toPersianDigits()})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (filteredCards.isEmpty()) {
            item {
                Text(
                    text = "هنوز کارتی در این بخش نساخته‌اید. از فرم بالا کارت‌های دلخواهتان را بسازید!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp)
                )
            }
        } else {
            items(filteredCards, key = { it.id }) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, card.category.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${card.category.badgeEmoji} ${card.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = card.category.primaryColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = card.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    editingCard = card
                                    titleInput = card.title
                                    bodyInput = card.body
                                    selectedCategory = card.category
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "ویرایش",
                                    tint = RadiantGold
                                )
                            }
                            IconButton(onClick = { onDeleteCard(card.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFFF758F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsProfilesTab(
    uiState: GameUiState,
    onSaveProfile: (String) -> Unit,
    onLoadProfile: (SettingsProfile) -> Unit,
    onDeleteProfile: (Long) -> Unit
) {
    var profileNameInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💾 ذخیره پروفایل تنظیمات فعلی",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "تنظیمات فعلی: صفحه ${uiState.maxTiles.toPersianDigits()} تایی • تم ${uiState.boardTheme.persianName} • سختی ${uiState.difficulty.persianTitle} • ${uiState.enabledObstacles.size.toPersianDigits()} مانع فعال",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("مهمانی دوستانه", "ماراتن ۲۰۰ تایی", "بازی سریع ۵۰ تایی").forEach { preset ->
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MysticCardSurface)
                                    .clickable { profileNameInput = preset }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = profileNameInput,
                        onValueChange = { profileNameInput = it.take(28) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true,
                        label = { Text("نام پروفایل تنظیمات (مثلاً مهمانی آخر هفته)") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = {
                            val name = profileNameInput.trim().ifBlank {
                                "پروفایل ${uiState.boardTheme.persianName} (${uiState.maxTiles.toPersianDigits()})"
                            }
                            onSaveProfile(name)
                            profileNameInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_settings_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HopefulEmerald)
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ذخیره پروفایل تنظیمات در حافظه لوکال",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "📂 پروفایل‌های ذخیره‌شده (${uiState.settingsProfiles.size.toPersianDigits()}):",
                style = MaterialTheme.typography.titleSmall,
                color = RadiantGold,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (uiState.settingsProfiles.isEmpty()) {
            item {
                Text(
                    text = "هنوز پروفایلی ذخیره نکرده‌اید. می‌توانید ترکیب دلخواهتان از تعداد خانه‌ها، تم، سختی و موانع را ذخیره کنید تا هر زمان با یک لمس بارگذاری شود!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }
        } else {
            items(uiState.settingsProfiles, key = { it.id }) { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, profile.boardTheme.primaryAccent, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticCardSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = null,
                                    tint = RadiantGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = profile.profileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${profile.boardTheme.emoji} تم ${profile.boardTheme.persianName} • ${profile.maxTiles.toPersianDigits()} خانه • سختی ${profile.difficulty.persianTitle} • ${profile.enabledObstacles.size.toPersianDigits()} مانع",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { onLoadProfile(profile) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RadiantGold,
                                    contentColor = Color(0xFF140B24)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "بارگذاری",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            IconButton(onClick = { onDeleteProfile(profile.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "حذف پروفایل",
                                    tint = Color(0xFFFF758F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnvironmentToggleControlRow(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (isEnabled) HopefulEmerald.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isEnabled) HopefulEmerald else Color.White.copy(alpha = 0.14f))
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (isEnabled) "روشن ✓" else "خاموش",
                style = MaterialTheme.typography.labelMedium,
                color = if (isEnabled) Color(0xFF052E16) else Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun EnvironmentThreeLevelControlRow(
    title: String,
    selectedLevel: EffectIntensity,
    onSelectLevel: (EffectIntensity) -> Unit,
    tagPrefix: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EffectIntensity.entries.forEach { level ->
                val isSelected = selectedLevel == level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) RadiantGold else Color.White.copy(alpha = 0.10f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.White else RadiantGold.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectLevel(level) }
                        .testTag("${tagPrefix}_${level.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${level.persianTitle} (${level.englishTitle})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color(0xFF140E05) else Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimationEngineSettingsCard(
    selectedQuality: AnimationQuality,
    selectedSpeed: AnimationSpeed,
    isCameraFollowEnabled: Boolean,
    onSelectQuality: (AnimationQuality) -> Unit,
    onSelectSpeed: (AnimationSpeed) -> Unit,
    onToggleCameraFollow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(RadiantGold, Color(0xFF38BDF8), RadiantGold)
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("animation_engine_settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "🎬 موتور انیمیشن و رندر حرفه‌ای (60/90/120 FPS)",
                        style = MaterialTheme.typography.titleMedium,
                        color = RadiantGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "کنترل کیفیت بصری، سرعت حرکت مهره‌ها و فیزیک سه‌بعدی تاس",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.80f)
                    )
                }
            }

            // 1. Animation Quality Selector (Performance / Balanced / Cinematic)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "کیفیت انیمیشن (Animation Quality)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = selectedQuality.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimationQuality.entries.forEach { quality ->
                        val isSelected = selectedQuality == quality
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) RadiantGold else Color.White.copy(alpha = 0.10f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else RadiantGold.copy(alpha = 0.30f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectQuality(quality) }
                                .testTag("anim_quality_${quality.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${quality.emoji} ${quality.englishTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFF140E05) else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = quality.persianTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFF291D0A) else Color.White.copy(alpha = 0.78f),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 2. Animation Speed Selector (Normal / Smooth / Cinematic)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "سرعت انیمیشن (Animation Speed)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = selectedSpeed.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = HopefulEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimationSpeed.entries.forEach { speed ->
                        val isSelected = selectedSpeed == speed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) HopefulEmerald else Color.White.copy(alpha = 0.10f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else HopefulEmerald.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectSpeed(speed) }
                                .testTag("anim_speed_${speed.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${speed.emoji} ${speed.englishTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFF052E16) else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = speed.persianTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color(0xFF064E3B) else Color.White.copy(alpha = 0.78f),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. Gentle Camera Follow Toggle
            EnvironmentToggleControlRow(
                title = "🎥 دنبال کردن ملایم مهره با دوربین (Camera Follow)",
                subtitle = "فوکوس و حرکت بسیار نرم صفحه همراه با حرکت مهره‌ها، صعود از نردبان و سر خوردن از مار",
                isEnabled = isCameraFollowEnabled,
                onToggle = onToggleCameraFollow,
                testTag = "camera_follow_switch"
            )
        }
    }
}


