package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AbilityActivationMode
import com.example.model.AiDifficulty
import com.example.model.BoardTheme
import com.example.model.CardCategory
import com.example.model.CharacterRoster
import com.example.model.DifficultyLevel
import com.example.model.GameCharacter
import com.example.model.ObstacleType
import com.example.model.toPersianDigits
import com.example.sound.soundscapeInfo
import com.example.ui.GameUiState
import com.example.ui.components.AdvancedSettingsTab
import com.example.ui.components.EnvironmentEffectsOverlay
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.components.CharacterPortraitAvatar
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.PlayerColorNames
import com.example.ui.theme.PlayerTokenColors
import com.example.ui.theme.RadiantGold

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SetupScreen(
    uiState: GameUiState,
    onSelectPlayerCount: (Int) -> Unit,
    onUpdatePlayerName: (Int, String) -> Unit,
    onSelectSetupSlot: (Int) -> Unit,
    onSelectCharacterForSlot: (Int, String) -> Unit,
    onResetPlayerName: (Int) -> Unit,
    onSelectDifficulty: (DifficultyLevel) -> Unit,
    onToggleObstacleType: (ObstacleType) -> Unit,
    onSelectBoardTheme: (BoardTheme) -> Unit,
    onSelectMaxTiles: (Int) -> Unit,
    onSelectAiDifficulty: (AiDifficulty) -> Unit,
    onOpenAdvancedSettings: (AdvancedSettingsTab) -> Unit,
    onOpenCardManager: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit = {},
    onToggleAmbient: () -> Unit = {},
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "start_btn_pulse")
    val ctaScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cta_scale"
    )

    val expandedCardIds = remember { mutableStateMapOf<String, Boolean>() }
    val activeSlot = uiState.activeSetupPlayerSlot.coerceIn(0, (uiState.playerCount - 1).coerceAtLeast(0))
    val activeSlotTokenColor = PlayerTokenColors[activeSlot % PlayerTokenColors.size]
    val activeSlotColorTitle = PlayerColorNames[activeSlot % PlayerColorNames.size]
    val activeSlotCharacterId = uiState.playerCharacterIds.getOrElse(activeSlot) {
        CharacterRoster.allCharacters[activeSlot % CharacterRoster.allCharacters.size].id
    }
    val activeSlotCharacter = uiState.resolveCharacterById(activeSlotCharacterId)
    val activeSlotName = uiState.playerNamesInput.getOrElse(activeSlot) { activeSlotCharacter.englishName }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF281549),
                        MysticDeepPlum,
                        Color(0xFF120922)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Subtle atmospheric visual effects in the setup lobby
        EnvironmentEffectsOverlay(
            effectType = uiState.effectiveEnvironmentEffect,
            intensity = uiState.environmentEffectIntensity,
            isEnabled = uiState.isEnvironmentEffectsEnabled || uiState.previewEnvironmentEffect != null,
            modifier = Modifier.fillMaxSize()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Banner Card with Title, Sound & Advanced Settings Toggle
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = RadiantGold)
                        .border(2.dp, RadiantGold.copy(alpha = 0.75f), RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1793272921000),
                            contentDescription = stringResource(R.string.app_name),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF1A0F2E).copy(alpha = 0.35f),
                                            MysticDeepPlum.copy(alpha = 0.80f),
                                            Color(0xFF120922).copy(alpha = 0.96f)
                                        )
                                    )
                                )
                        )

                        // Top-left: Theme & Tile Badge
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .border(1.dp, RadiantGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { onOpenAdvancedSettings(AdvancedSettingsTab.BOARD_AND_THEME) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = uiState.boardTheme.emoji, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${uiState.boardTheme.persianName} • ${uiState.maxTiles.toPersianDigits()} خانه",
                                style = MaterialTheme.typography.labelSmall,
                                color = RadiantGold,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Top-right: Advanced Settings + Music Toggle + Sound Toggle
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onOpenAdvancedSettings(AdvancedSettingsTab.BOARD_AND_THEME) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.50f))
                                    .border(1.dp, RadiantGold.copy(alpha = 0.6f), CircleShape)
                                    .testTag("setup_advanced_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "تنظیمات پیشرفته",
                                    tint = RadiantGold
                                )
                            }

                            IconButton(
                                onClick = onToggleMusic,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.50f))
                                    .border(
                                        1.dp,
                                        if (uiState.isMusicEnabled) RadiantGold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .testTag("setup_music_toggle")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isMusicEnabled) {
                                        Icons.Filled.MusicNote
                                    } else {
                                        Icons.Filled.MusicOff
                                    },
                                    contentDescription = if (uiState.isMusicEnabled) {
                                        "موسیقی پس‌زمینه روشن"
                                    } else {
                                        "موسیقی پس‌زمینه خاموش"
                                    },
                                    tint = if (uiState.isMusicEnabled) RadiantGold else Color.White.copy(alpha = 0.6f)
                                )
                            }

                            IconButton(
                                onClick = onToggleSound,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.50f))
                                    .testTag("setup_sound_toggle")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSoundEnabled) {
                                        Icons.Filled.VolumeUp
                                    } else {
                                        Icons.Filled.VolumeOff
                                    },
                                    contentDescription = if (uiState.isSoundEnabled) {
                                        stringResource(R.string.sound_on)
                                    } else {
                                        stringResource(R.string.sound_off)
                                    },
                                    tint = if (uiState.isSoundEnabled) RadiantGold else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Casino,
                                    contentDescription = null,
                                    tint = RadiantGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 1.5. Phase 5 Quick Board Theme & Tile Size + Advanced Settings Hub Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, uiState.boardTheme.primaryAccent.copy(alpha = 0.65f), RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                    tint = uiState.boardTheme.primaryAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "تم صفحه و اندازه جدول بازی",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RadiantGold.copy(alpha = 0.18f))
                                    .border(1.dp, RadiantGold, RoundedCornerShape(10.dp))
                                    .clickable { onOpenAdvancedSettings(AdvancedSettingsTab.BOARD_AND_THEME) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("open_full_settings_chip")
                            ) {
                                Text(
                                    text = "⚙️ پنل تنظیمات پیشرفته",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // 4 Visual Themes Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BoardTheme.entries.forEach { theme ->
                                val isSelected = uiState.boardTheme == theme
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    theme.boardFrameOuterTop,
                                                    theme.boardFrameOuterBottom
                                                )
                                            )
                                        )
                                        .border(
                                            width = if (isSelected) 2.2.dp else 1.dp,
                                            color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectBoardTheme(theme) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                        .testTag("quick_theme_${theme.name}"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(text = theme.emoji, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = theme.persianName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) RadiantGold else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Active Theme Soundscape & Ambient Nature Mixer Row
                        val activeSoundscape = uiState.boardTheme.soundscapeInfo()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MysticCardSurface)
                                .border(1.dp, uiState.boardTheme.primaryAccent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = activeSoundscape.badgeEmoji, style = MaterialTheme.typography.bodyMedium)
                                    Column {
                                        Text(
                                            text = "موسیقی تم: ${activeSoundscape.musicTitle}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = RadiantGold,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "افکت محیطی: ${activeSoundscape.ambientTitle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.82f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (uiState.isMusicEnabled) HopefulEmerald.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.08f)
                                        )
                                        .border(
                                            1.dp,
                                            if (uiState.isMusicEnabled) HopefulEmerald else Color.White.copy(alpha = 0.2f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onToggleMusic() }
                                        .padding(vertical = 6.dp)
                                        .testTag("setup_toggle_music_chip"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (uiState.isMusicEnabled) "🎵 موسیقی تم: روشن" else "🔇 موسیقی تم: خاموش",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (uiState.isMusicEnabled) HopefulEmerald else Color.White.copy(alpha = 0.65f),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (uiState.isAmbientEnabled) uiState.boardTheme.primaryAccent.copy(alpha = 0.25f)
                                            else Color.White.copy(alpha = 0.08f)
                                        )
                                        .border(
                                            1.dp,
                                            if (uiState.isAmbientEnabled) uiState.boardTheme.primaryAccent else Color.White.copy(alpha = 0.2f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onToggleAmbient() }
                                        .padding(vertical = 6.dp)
                                        .testTag("setup_toggle_ambient_chip"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (uiState.isAmbientEnabled) "🍃 افکت محیطی: روشن" else "🍃 افکت محیطی: خاموش",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (uiState.isAmbientEnabled) RadiantGold else Color.White.copy(alpha = 0.65f),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Direct Luxury Black & Gold Studio Button (Environment Effects + 9 Music Genres)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF281E0B),
                                                Color(0xFF171106)
                                            )
                                        )
                                    )
                                    .border(1.2.dp, RadiantGold.copy(alpha = 0.85f), RoundedCornerShape(11.dp))
                                    .clickable { onOpenAdvancedSettings(AdvancedSettingsTab.EFFECTS_AND_MUSIC) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("setup_open_effects_music_studio_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "✨🎵 استودیو جلوه‌های محیطی و موسیقی",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RadiantGold,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "جلوه فعال: ${uiState.selectedEnvironmentEffect.emoji} ${uiState.selectedEnvironmentEffect.persianName} • موسیقی: ${uiState.selectedMusicGenre.emoji} ${uiState.selectedMusicGenre.persianName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RadiantGold)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ورود به استودیو",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF140D04),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // 4 Board Tile Sizes Row (50 / 100 / 150 / 200)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50, 100, 150, 200).forEach { tiles ->
                                val isSelected = uiState.maxTiles == tiles
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) uiState.boardTheme.primaryAccent else MysticCardSurface
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectMaxTiles(tiles) }
                                        .testTag("quick_tiles_$tiles"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${tiles.toPersianDigits()} خانه",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) Color(0xFF140B24) else Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Player Count Selector (1 to 8 Players) + Single-Player AI Difficulty
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.select_players_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (count in 1..8) {
                                val isSelected = uiState.playerCount == count
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected) RadiantGold else MysticCardSurface,
                                    label = "count_bg_$count"
                                )
                                val contentColor = if (isSelected) Color(0xFF1A0F2E) else Color.White

                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(bgColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else RadiantGold.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectPlayerCount(count) }
                                        .testTag("player_count_chip_$count"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (count == 1) "۱ نفر 🤖" else "${count.toPersianDigits()} نفر",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = contentColor
                                    )
                                }
                            }
                        }

                        // Single-Player AI Opponent Configuration when playerCount == 1
                        AnimatedVisibility(
                            visible = uiState.playerCount == 1,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E1B4B))
                                    .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SmartToy,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "حالت تک‌نفره: رقابت در برابر هوش مصنوعی هوشمند",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AiDifficulty.entries.forEach { aiLevel ->
                                        val isSelected = uiState.aiDifficulty == aiLevel
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFF38BDF8) else MysticCardSurface
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { onSelectAiDifficulty(aiLevel) }
                                                .testTag("setup_ai_diff_${aiLevel.name}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "🤖 ${aiLevel.persianTitle}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = uiState.aiDifficulty.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Player Slot Selector & Custom Name + Selected Hero Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.players_names_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = RadiantGold,
                            fontWeight = FontWeight.Bold
                        )

                        // Horizontal strip of active players (1..playerCount)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(uiState.playerCount) { idx ->
                                val isSlotSelected = idx == activeSlot
                                val tokenColor = PlayerTokenColors[idx % PlayerTokenColors.size]
                                val charId = uiState.playerCharacterIds.getOrElse(idx) {
                                    CharacterRoster.allCharacters[idx % CharacterRoster.allCharacters.size].id
                                }
                                val charObj = uiState.resolveCharacterById(charId)
                                val customName = uiState.playerNamesInput.getOrElse(idx) { charObj.englishName }
                                    .ifBlank { charObj.englishName }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSlotSelected) {
                                                tokenColor.copy(alpha = 0.25f)
                                            } else {
                                                MysticCardSurface.copy(alpha = 0.7f)
                                            }
                                        )
                                        .border(
                                            width = if (isSlotSelected) 2.2.dp else 1.dp,
                                            color = if (isSlotSelected) tokenColor else Color.White.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { onSelectSetupSlot(idx) }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag("player_setup_slot_$idx"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        CharacterPortraitAvatar(
                                            character = charObj,
                                            size = 42.dp,
                                            isSelected = isSlotSelected
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(tokenColor)
                                                .border(1.5.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (idx + 1).toPersianDigits(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF140B24),
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = customName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${charObj.englishName} (${charObj.displayBadgeEmoji})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSlotSelected) RadiantGold else Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }

                        // Active Slot Custom Name Editor + Selected Character Preview
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MysticCardSurface.copy(alpha = 0.85f))
                                .border(1.5.dp, activeSlotTokenColor.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CharacterPortraitAvatar(
                                    character = activeSlotCharacter,
                                    size = 62.dp,
                                    isSelected = true
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(activeSlotTokenColor)
                                                .border(1.dp, Color.White, CircleShape)
                                        )
                                        Text(
                                            text = "بازیکن ${(activeSlot + 1).toPersianDigits()} ($activeSlotColorTitle)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = activeSlotTokenColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "${activeSlotCharacter.englishName} — ${activeSlotCharacter.persianTitle}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${activeSlotCharacter.displayBadgeEmoji} ${activeSlotCharacter.displayAbilityTitle}: ${activeSlotCharacter.displayAbilityShortSummary}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = RadiantGold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = activeSlotName,
                                    onValueChange = { onUpdatePlayerName(activeSlot, it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("player_name_input_$activeSlot"),
                                    singleLine = true,
                                    label = {
                                        Text(
                                            text = "نام دلخواه بازیکن ${(activeSlot + 1).toPersianDigits()} (پیش‌فرض: ${activeSlotCharacter.englishName})",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = activeSlotTokenColor,
                                        unfocusedBorderColor = activeSlotTokenColor.copy(alpha = 0.45f),
                                        focusedLabelColor = activeSlotTokenColor,
                                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = activeSlotTokenColor
                                    )
                                )

                                IconButton(
                                    onClick = { onResetPlayerName(activeSlot) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MysticIndigoSurface)
                                        .border(1.dp, RadiantGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .testTag("reset_player_name_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "نام پیش‌فرض قهرمان",
                                        tint = RadiantGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Character Selection Roster Grid (16 Default + Custom Characters)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.character_roster_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = stringResource(R.string.character_roster_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }

                            Button(
                                onClick = { onOpenAdvancedSettings(AdvancedSettingsTab.CUSTOM_CHARACTER) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HopefulEmerald,
                                    contentColor = Color(0xFF082218)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("open_custom_char_creator_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ساخت قهرمان",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Render all characters (custom + 16 default) in rows of 2 cards
                        val allCharacters = uiState.allAvailableCharacters
                        val rows = remember(allCharacters) { allCharacters.chunked(2) }
                        rows.forEach { rowPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                rowPair.forEach { character ->
                                    val takenBySlot = (0 until uiState.playerCount).firstOrNull { slotIdx ->
                                        uiState.playerCharacterIds.getOrNull(slotIdx) == character.id
                                    }
                                    val isSelectedForActiveSlot = takenBySlot == activeSlot
                                    val isTakenByOtherPlayer = takenBySlot != null && takenBySlot != activeSlot
                                    val isExpanded = expandedCardIds[character.id] == true

                                    CharacterSelectionCard(
                                        character = character,
                                        isSelectedForActiveSlot = isSelectedForActiveSlot,
                                        takenByPlayerSlot = takenBySlot,
                                        isTakenByOtherPlayer = isTakenByOtherPlayer,
                                        isExpanded = isExpanded,
                                        onSelectCharacter = {
                                            if (!isTakenByOtherPlayer) {
                                                onSelectCharacterForSlot(activeSlot, character.id)
                                            }
                                        },
                                        onToggleExpand = {
                                            expandedCardIds[character.id] = !isExpanded
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowPair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 5. Difficulty Level Selector (Easy, Medium, Hard)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.select_difficulty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = RadiantGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DifficultyLevel.entries.forEach { level ->
                                val isSelected = uiState.difficulty == level
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected) level.badgeColor else MysticCardSurface,
                                    label = "diff_bg_${level.name}"
                                )
                                val contentColor = if (isSelected) Color(0xFF140B24) else Color.White

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(bgColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color.White else level.badgeColor.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onSelectDifficulty(level) }
                                        .testTag("difficulty_chip_${level.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level.persianTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = contentColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.difficulty.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 6. Special Obstacles Control Panel (Simple toggle chips for all 7 obstacles)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.obstacles_control_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isEnabled) {
                                                obstacle.badgeColor.copy(alpha = 0.25f)
                                            } else {
                                                MysticCardSurface.copy(alpha = 0.55f)
                                            }
                                        )
                                        .border(
                                            width = if (isEnabled) 1.8.dp else 1.dp,
                                            color = if (isEnabled) {
                                                obstacle.badgeColor
                                            } else {
                                                Color.White.copy(alpha = 0.18f)
                                            },
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onToggleObstacleType(obstacle) }
                                        .padding(horizontal = 12.dp)
                                        .testTag("obstacle_toggle_${obstacle.name}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = obstacle.persianName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.5f)
                                    )
                                    if (isEnabled) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = obstacle.badgeColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6.5. Phase 4 & 5 Truth or Dare Cards + Advanced Settings Hub
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, RadiantGold.copy(alpha = 0.65f), RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🃏 کارت‌های جرئت یا حقیقت (هنگام افتادن روی مار)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "کارت‌ها، کاراکترهای سفارشی و پروفایل‌های شما برای همیشه در حافظه ذخیره می‌شوند",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Category Count Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CardCategory.entries.forEach { cat ->
                                val catCount = uiState.truthOrDareCards.count { it.category == cat }
                                val isCatEnabled = cat in uiState.enabledCardCategories
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isCatEnabled) cat.primaryColor.copy(alpha = 0.20f) else MysticCardSurface
                                        )
                                        .border(
                                            1.dp,
                                            if (isCatEnabled) cat.primaryColor else Color.White.copy(alpha = 0.15f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(text = cat.badgeEmoji, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = cat.shortTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${catCount.toPersianDigits()} کارت",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cat.primaryColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenCardManager,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_card_manager_setup_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RadiantGold,
                                    contentColor = Color(0xFF1A0F2E)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Style,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "مدیریت کارت‌ها (${uiState.truthOrDareCards.size.toPersianDigits()})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = { onOpenAdvancedSettings(AdvancedSettingsTab.PROFILES) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_profiles_setup_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MysticCardSurface,
                                    contentColor = RadiantGold
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "پروفایل‌ها (${uiState.settingsProfiles.size.toPersianDigits()})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // 7. Start Game CTA Button
            item {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .scale(ctaScale)
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = HopefulEmerald)
                        .testTag("start_game_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HopefulEmerald,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.start_game_button),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CharacterSelectionCard(
    character: GameCharacter,
    isSelectedForActiveSlot: Boolean,
    takenByPlayerSlot: Int?,
    isTakenByOtherPlayer: Boolean,
    isExpanded: Boolean,
    onSelectCharacter: () -> Unit,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slotColor = if (takenByPlayerSlot != null) {
        PlayerTokenColors[takenByPlayerSlot % PlayerTokenColors.size]
    } else {
        character.accentColor
    }

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelectedForActiveSlot -> RadiantGold
            takenByPlayerSlot != null -> slotColor.copy(alpha = 0.75f)
            else -> character.primaryColor.copy(alpha = 0.45f)
        },
        label = "char_card_border_${character.id}"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelectedForActiveSlot -> character.primaryColor.copy(alpha = 0.26f)
            isTakenByOtherPlayer -> MysticCardSurface.copy(alpha = 0.48f)
            else -> MysticCardSurface.copy(alpha = 0.90f)
        },
        label = "char_card_bg_${character.id}"
    )

    Card(
        modifier = modifier
            .alpha(if (isTakenByOtherPlayer) 0.72f else 1f)
            .border(
                width = if (isSelectedForActiveSlot) 2.5.dp else 1.3.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = {
                    if (isTakenByOtherPlayer) {
                        onToggleExpand()
                    } else {
                        onSelectCharacter()
                    }
                },
                onLongClick = onToggleExpand
            )
            .testTag("character_card_${character.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelectedForActiveSlot) 10.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Portrait Avatar with held item + English Name + Gender & Player Slot Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CharacterPortraitAvatar(
                    character = character,
                    size = 58.dp,
                    isSelected = isSelectedForActiveSlot
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = character.englishName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (takenByPlayerSlot != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(slotColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "بازیکن ${(takenByPlayerSlot + 1).toPersianDigits()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF140B24),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            Text(
                                text = if (character.isCustom) "🛠️ سفارشی" else character.gender.persianLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (character.isCustom) HopefulEmerald else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (character.isCustom) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        }
                    }

                    Text(
                        text = character.persianTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = character.accentColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${character.displayBadgeEmoji} ${character.displayAbilityTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Short Ability Summary + Expand Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.26f))
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("character_expand_${character.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = character.displayAbilityShortSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.weight(1f),
                    maxLines = if (isExpanded) 3 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "توضیحات کامل قابلیت",
                    tint = RadiantGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Smooth Expandable Full Description
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(240)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(character.primaryColor.copy(alpha = 0.18f))
                        .border(1.dp, character.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modeText = if (character.isCustom) {
                        "🛠️ قابلیت اختصاصی ساخته‌شده توسط بازیکن"
                    } else {
                        when (character.abilityType.activationMode) {
                            AbilityActivationMode.PASSIVE -> "✨ قابلیت دائمی و خودکار"
                            AbilityActivationMode.AUTO_CHARGE -> "🛡️ شارژ خودکار هر ۳ نوبت"
                            AbilityActivationMode.ACTIVE_COOLDOWN -> "⚡ قابلیت فعال (کول‌داون ۳ نوبت)"
                            AbilityActivationMode.ACTIVE_ONCE -> "🌟 قابلیت ویژه (یک‌بار در هر بازی)"
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = character.accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = modeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = character.accentColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = character.displayAbilityFullDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
            }
        }
    }
}
