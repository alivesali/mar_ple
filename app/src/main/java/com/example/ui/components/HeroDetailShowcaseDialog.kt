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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GameCharacter
import com.example.model.HeroAnimationStance
import com.example.model.HeroBlueprint
import com.example.model.HeroClass
import com.example.model.HeroRenderQuality
import com.example.model.HeroStats
import com.example.model.HeroVisualCatalog
import com.example.model.HeroVisualProfile
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.RadiantGold

/**
 * Fullscreen Interactive Showcase Screen for inspecting and previewing a Hero.
 * Displays 3D/2D animated figure, stance controls, lore, 6-axis balance stats,
 * active & passive ability specifications, strengths, and counterplay mechanics.
 */
@Composable
fun HeroDetailShowcaseDialog(
    character: GameCharacter,
    onDismissRequest: () -> Unit,
    onSelectHero: (() -> Unit)? = null,
    isAlreadySelected: Boolean = false,
    onPlaySoundCue: ((String) -> Unit)? = null
) {
    val blueprint: HeroBlueprint = character.heroBlueprint
    val profile: HeroVisualProfile = remember(character.id) { HeroVisualCatalog.getProfile(character.id) }

    var currentStance by remember { mutableStateOf(HeroAnimationStance.IDLE) }
    var renderQuality by remember { mutableStateOf(HeroRenderQuality.CINEMATIC_3D) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hero_showcase_dialog_${character.id}"),
            color = Color(0xFF090D16)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background ambient radial gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    profile.primaryColor.copy(alpha = 0.22f),
                                    Color(0xFF0F172A),
                                    Color(0xFF090D16)
                                ),
                                radius = 900f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Top Bar: Back/Close + Hero Identity Title + Quality Selector
                    HeroShowcaseHeader(
                        character = character,
                        profile = profile,
                        renderQuality = renderQuality,
                        onToggleQuality = {
                            renderQuality = if (renderQuality == HeroRenderQuality.CINEMATIC_3D) {
                                HeroRenderQuality.LIGHTWEIGHT_2D
                            } else {
                                HeroRenderQuality.CINEMATIC_3D
                            }
                        },
                        onDismissRequest = onDismissRequest
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Scrollable Main Showcase Content
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // 1. Hero Stage: Animated 3D/2D Viewport + Interactive Stance Selector
                        item {
                            HeroStageSection(
                                character = character,
                                profile = profile,
                                currentStance = currentStance,
                                renderQuality = renderQuality,
                                onSelectStance = { newStance ->
                                    currentStance = newStance
                                    onPlaySoundCue?.invoke(newStance.name)
                                },
                                onPlaySound = {
                                    onPlaySoundCue?.invoke(currentStance.name)
                                }
                            )
                        }

                        // 2. Personality, Lore Story & Heroic Quote
                        item {
                            HeroLoreSection(profile = profile)
                        }

                        // 3. 6-Axis Balance Stat Radar & Combat Ratings
                        item {
                            HeroStatsSection(
                                stats = blueprint.stats,
                                profile = profile,
                                blueprint = blueprint
                            )
                        }

                        // 4. Primary Active Ability & Passive Trait Cards
                        item {
                            HeroAbilitiesSection(
                                blueprint = blueprint,
                                character = character,
                                profile = profile
                            )
                        }

                        // 5. Strengths & Real Trade-offs (Weaknesses)
                        item {
                            HeroStrengthsWeaknessesSection(
                                blueprint = blueprint,
                                profile = profile
                            )
                        }
                    }

                    // Bottom Action Bar: Select Hero or Close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("بازگشت", fontWeight = FontWeight.Bold)
                        }

                        if (onSelectHero != null) {
                            Button(
                                onClick = {
                                    onSelectHero()
                                    onDismissRequest()
                                },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp)
                                    .testTag("hero_showcase_select_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAlreadySelected) RadiantGold else profile.primaryColor
                                )
                            ) {
                                Text(
                                    text = if (isAlreadySelected) "✓ انتخاب شده برای این اسلات" else "انتخاب ${character.englishName} برای بازی",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAlreadySelected) Color(0xFF140B24) else Color.White,
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

// -------------------------------------------------------------------------------------------------
// HEADER SECTION
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeroShowcaseHeader(
    character: GameCharacter,
    profile: HeroVisualProfile,
    renderQuality: HeroRenderQuality,
    onToggleQuality: () -> Unit,
    onDismissRequest: () -> Unit
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
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.Filled.Close, contentDescription = "بستن", tint = Color.White)
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = character.englishName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "• ${profile.elementName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = profile.accentGlowColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = profile.persianTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Render Mode Toggle Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, profile.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .clickable { onToggleQuality() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(renderQuality.iconEmoji, fontSize = 12.sp)
                Text(
                    text = renderQuality.persianLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// HERO STAGE & STANCE SELECTOR
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeroStageSection(
    character: GameCharacter,
    profile: HeroVisualProfile,
    currentStance: HeroAnimationStance,
    renderQuality: HeroRenderQuality,
    onSelectStance: (HeroAnimationStance) -> Unit,
    onPlaySound: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.88f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(profile.primaryColor.copy(alpha = 0.65f), RadiantGold.copy(alpha = 0.35f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Animated Hero Figure Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                HeroVisualRenderer(
                    character = character,
                    stance = currentStance,
                    quality = renderQuality,
                    size = 230.dp,
                    showPedestal = true,
                    showAuraParticles = true
                )

                // Sound Cue Trigger Button
                IconButton(
                    onClick = onPlaySound,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, RadiantGold.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "آزمون صدای قهرمان",
                        tint = RadiantGold
                    )
                }

                // Active Stance Description Banner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090D16).copy(alpha = 0.82f))
                        .border(1.dp, profile.accentGlowColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${currentStance.iconEmoji} ${currentStance.persianLabel}: ${currentStance.descriptionPersian}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Interactive Stance Buttons Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HeroAnimationStance.entries) { stance ->
                    val isSelected = stance == currentStance
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) profile.primaryColor else Color.White.copy(alpha = 0.06f),
                        label = "stance_btn_bg_${stance.name}"
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.15f),
                        label = "stance_btn_border_${stance.name}"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { onSelectStance(stance) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(stance.iconEmoji, fontSize = 14.sp)
                            Text(
                                text = stance.persianLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LORE & QUOTE SECTION
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeroLoreSection(profile: HeroVisualProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.85f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(profile.primaryColor.copy(alpha = 0.4f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📜", fontSize = 18.sp)
                Text(
                    text = "سرگذشت و هویت قهرمان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = RadiantGold
                )
            }

            Text(
                text = profile.loreStoryShort,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
                lineHeight = 22.sp
            )

            // Quote callout box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, RadiantGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "«${profile.quotePersian}»",
                    style = MaterialTheme.typography.bodySmall,
                    color = RadiantGold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Combat Style & Weapon badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚔️ سبک مبارزه: ${profile.combatStyle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.70f)
                )
                Text(
                    text = "سلاح: ${profile.weaponNamePersian}",
                    style = MaterialTheme.typography.labelSmall,
                    color = profile.accentGlowColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// 6-AXIS BALANCED STATS SECTION
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeroStatsSection(
    stats: HeroStats,
    profile: HeroVisualProfile,
    blueprint: HeroBlueprint
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.85f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RadiantGold.copy(alpha = 0.45f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📊", fontSize = 18.sp)
                    Text(
                        text = "آمار شش‌گانه قدرت و تعادل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Text(
                    text = "مجموع امتیاز: ${stats.totalScore}/60",
                    style = MaterialTheme.typography.labelSmall,
                    color = RadiantGold,
                    fontWeight = FontWeight.Bold
                )
            }

            // 6 Stat Progress Bars
            StatBarRow(label = "قدرت تهاجم (Attack)", value = stats.attack, max = 10, color = Color(0xFFEF4444))
            StatBarRow(label = "قدرت دفاعی (Defense)", value = stats.defense, max = 10, color = Color(0xFF0EA5E9))
            StatBarRow(label = "مقاومت جادویی (Resistance)", value = stats.resistance, max = 10, color = Color(0xFFA855F7))
            StatBarRow(label = "سرعت و تحرک (Mobility)", value = stats.mobility, max = 10, color = Color(0xFFF59E0B))
            StatBarRow(label = "کنترل میدان (Control)", value = stats.control, max = 10, color = Color(0xFF06B6D4))
            StatBarRow(label = "کاربرد و اقبال (Utility)", value = stats.utility, max = 10, color = Color(0xFF10B981))

            // Class Badge Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClassBadge(heroClass = blueprint.primaryClass, isPrimary = true)
                blueprint.secondaryClass?.let { sec ->
                    ClassBadge(heroClass = sec, isPrimary = false)
                }
            }
        }
    }
}

@Composable
private fun StatBarRow(
    label: String,
    value: Int,
    max: Int,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "$value / $max",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (value.toFloat() / max).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

@Composable
private fun ClassBadge(heroClass: HeroClass, isPrimary: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(heroClass.badgeColor.copy(alpha = 0.22f))
            .border(1.dp, heroClass.badgeColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(heroClass.badgeEmoji, fontSize = 12.sp)
            Text(
                text = if (isPrimary) "کلاس اصلی: ${heroClass.englishTitle}" else "کلاس فرعی: ${heroClass.englishTitle}",
                style = MaterialTheme.typography.labelSmall,
                color = heroClass.badgeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ABILITIES SECTION
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeroAbilitiesSection(
    blueprint: HeroBlueprint,
    character: GameCharacter,
    profile: HeroVisualProfile
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.85f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(profile.primaryColor.copy(alpha = 0.5f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Active Ability
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(blueprint.primaryClass.badgeEmoji, fontSize = 18.sp)
                Text(
                    text = "قابلیت اصلی: ${blueprint.primaryAbility.persianName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = profile.accentGlowColor
                )
            }

            Text(
                text = blueprint.primaryAbility.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.90f),
                lineHeight = 20.sp
            )

            // Ability Specs: Cooldown & Uses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AbilitySpecPill(title = "زمان شارژ مجدد", value = "${blueprint.primaryAbility.cooldownTurns} نوبت", emoji = "⏱️")
                AbilitySpecPill(title = "سقف استفاده در بازی", value = "${blueprint.primaryAbility.maxUsesPerMatch} بار", emoji = "🎯")
                AbilitySpecPill(title = "هدف", value = blueprint.primaryAbility.targetScope.persianLabel, emoji = "🌐")
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Passive Ability
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🌟", fontSize = 16.sp)
                Text(
                    text = "قابلیت غیرفعال: ${blueprint.passiveAbilityTitle}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RadiantGold
                )
            }
            Text(
                text = blueprint.passiveAbilityDescription,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun AbilitySpecPill(title: String, value: String, emoji: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$emoji $value", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.White.copy(alpha = 0.55f))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STRENGTHS & WEAKNESSES SECTION
// -------------------------------------------------------------------------------------------------
@Composable
private fun HeroStrengthsWeaknessesSection(
    blueprint: HeroBlueprint,
    profile: HeroVisualProfile
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.85f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF10B981).copy(alpha = 0.35f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡ نقاط قوت و تعادل استراتژیک",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            blueprint.strengths.forEach { str ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("✓", color = Color(0xFF10B981), fontWeight = FontWeight.Black)
                    Text(text = str, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                }
            }

            blueprint.weaknesses.forEach { weak ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚠", color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                    Text(text = weak, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.70f))
                }
            }
        }
    }
}
