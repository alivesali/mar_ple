package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CharacterRoster
import com.example.model.GameCharacter
import com.example.model.HeroAnimationStance
import com.example.model.HeroBlueprint
import com.example.model.HeroClass
import com.example.model.HeroRenderQuality
import com.example.model.HeroVisualCatalog
import com.example.model.HeroVisualProfile
import com.example.model.toPersianDigits
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.RadiantGold

/**
 * Reusable Hero Collection Content & Management Section
 * Allows players to independently inspect, preview animations, and equip heroes
 * for each player slot with persistent storage across matches.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroCollectionContent(
    currentEquippedCharacterIds: List<String>,
    playerCount: Int,
    initialPlayerSlot: Int = 0,
    onEquipHeroForSlot: (slotIndex: Int, characterId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSlot by remember { mutableIntStateOf(initialPlayerSlot.coerceIn(0, (playerCount - 1).coerceAtLeast(0))) }
    var selectedClassFilter by remember { mutableStateOf<HeroClass?>(null) }
    var inspectingHero by remember { mutableStateOf<GameCharacter?>(null) }

    val allHeroes = remember { CharacterRoster.allCharacters }
    val filteredHeroes = remember(selectedClassFilter) {
        if (selectedClassFilter == null) allHeroes
        else allHeroes.filter { it.heroBlueprint.primaryClass == selectedClassFilter }
    }

    val currentSlotEquippedId = currentEquippedCharacterIds.getOrElse(activeSlot) { allHeroes.first().id }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_collection_content"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Player Slot Selector Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .border(1.dp, RadiantGold.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎯 انتخاب بازیکن برای تجهیز قهرمان:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = RadiantGold
                )
                Text(
                    text = "اسلات ${(activeSlot + 1).toPersianDigits()} از ${playerCount.toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.70f)
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(playerCount) { slotIndex ->
                    val isSlotActive = slotIndex == activeSlot
                    val equippedCharId = currentEquippedCharacterIds.getOrElse(slotIndex) { allHeroes.first().id }
                    val equippedChar = allHeroes.find { it.id == equippedCharId } ?: allHeroes.first()

                    val borderColor by animateColorAsState(
                        targetValue = if (isSlotActive) RadiantGold else Color.White.copy(alpha = 0.20f),
                        label = "slot_border"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSlotActive) RadiantGold.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f))
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { activeSlot = slotIndex }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("hero_collection_slot_$slotIndex")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "بازیکن ${(slotIndex + 1).toPersianDigits()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSlotActive) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isSlotActive) RadiantGold else Color.White
                            )
                            Text(
                                text = "(${equippedChar.englishName})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        // Hero Class Filter Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "🏷️ دسته‌بندی و کلاس قهرمانان:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.80f),
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                item {
                    val isAllSelected = selectedClassFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isAllSelected) RadiantGold else Color.White.copy(alpha = 0.08f))
                            .border(1.dp, if (isAllSelected) RadiantGold else Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                            .clickable { selectedClassFilter = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "همه (${allHeroes.size.toPersianDigits()})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllSelected) Color(0xFF140B24) else Color.White
                        )
                    }
                }

                items(HeroClass.entries.toList()) { heroClass ->
                    val isSelected = selectedClassFilter == heroClass
                    val count = allHeroes.count { it.heroBlueprint.primaryClass == heroClass }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RadiantGold else Color.White.copy(alpha = 0.08f))
                            .border(1.dp, if (isSelected) RadiantGold else Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                            .clickable { selectedClassFilter = heroClass }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${heroClass.badgeEmoji} ${heroClass.persianTitle} (${count.toPersianDigits()})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF140B24) else Color.White
                        )
                    }
                }
            }
        }

        // Hero Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(filteredHeroes, key = { it.id }) { hero ->
                val blueprint = hero.heroBlueprint
                val profile = remember(hero.id) { HeroVisualCatalog.getProfile(hero.id) }
                val isEquipped = currentSlotEquippedId == hero.id
                val isTakenByOtherSlot = currentEquippedCharacterIds.indices.any { it != activeSlot && currentEquippedCharacterIds[it] == hero.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            width = if (isEquipped) 2.dp else 1.dp,
                            brush = if (isEquipped) {
                                Brush.horizontalGradient(listOf(RadiantGold, profile.primaryColor))
                            } else {
                                Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.08f)))
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("hero_collection_card_${hero.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) {
                            profile.primaryColor.copy(alpha = 0.20f)
                        } else {
                            MysticCardSurface.copy(alpha = 0.88f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hero Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                HeroVisualRenderer(
                                    character = hero,
                                    stance = HeroAnimationStance.IDLE,
                                    quality = HeroRenderQuality.CINEMATIC_3D,
                                    size = 64.dp,
                                    showPedestal = false,
                                    showAuraParticles = true
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = hero.englishName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(profile.primaryColor.copy(alpha = 0.40f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${blueprint.primaryClass.badgeEmoji} ${blueprint.primaryClass.persianTitle}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Text(
                                        text = hero.persianTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = profile.accentGlowColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isEquipped) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(HopefulEmerald.copy(alpha = 0.25f))
                                        .border(1.dp, HopefulEmerald, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = HopefulEmerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "تجهیز شده",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HopefulEmerald,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        // Abilities Breakdown: Active & Passive & Cooldown
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "⚡ قابلیت فعال: ${blueprint.primaryAbility.persianName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RadiantGold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RadiantGold.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⏳ کول‌داون: ${blueprint.primaryAbility.cooldownTurns.toPersianDigits()} نوبت",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RadiantGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = blueprint.primaryAbility.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.90f)
                            )

                            Text(
                                text = "✨ غیرفعال: ${blueprint.passiveAbilityTitle} - ${blueprint.passiveAbilityDescription}",
                                style = MaterialTheme.typography.bodySmall,
                                color = profile.accentGlowColor
                            )
                        }

                        // Strengths & Weaknesses Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (blueprint.strengths.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HopefulEmerald.copy(alpha = 0.15f))
                                        .border(1.dp, HopefulEmerald.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "💪 قدرت: ${blueprint.strengths.first()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HopefulEmerald,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (blueprint.weaknesses.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DangerCrimson.copy(alpha = 0.15f))
                                        .border(1.dp, DangerCrimson.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "⚠️ ضعف: ${blueprint.weaknesses.first()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerCrimson,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Bottom Actions: Details/Preview Animation + Equip Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { inspectingHero = hero },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                                    .testTag("hero_preview_button_${hero.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = RadiantGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "پیش‌نمایش انیمیشن",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onEquipHeroForSlot(activeSlot, hero.id) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("hero_equip_button_${hero.id}"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isEquipped && !isTakenByOtherSlot,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEquipped) HopefulEmerald else RadiantGold,
                                    contentColor = Color(0xFF140B24),
                                    disabledContainerColor = if (isEquipped) HopefulEmerald.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.15f),
                                    disabledContentColor = Color.White.copy(alpha = 0.50f)
                                )
                            ) {
                                Text(
                                    text = when {
                                        isEquipped -> "✓ قهرمان فعال اسلات"
                                        isTakenByOtherSlot -> "انتخاب توسط بازیکن دیگر"
                                        else -> "تجهیز برای بازیکن ${(activeSlot + 1).toPersianDigits()}"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    inspectingHero?.let { hero ->
        HeroDetailShowcaseDialog(
            character = hero,
            onDismissRequest = { inspectingHero = null },
            onSelectHero = {
                onEquipHeroForSlot(activeSlot, hero.id)
                inspectingHero = null
            },
            isAlreadySelected = currentSlotEquippedId == hero.id
        )
    }
}

/**
 * Dedicated Standalone Dialog for the Hero Collection
 */
@Composable
fun HeroCollectionDialog(
    currentEquippedCharacterIds: List<String>,
    playerCount: Int,
    initialPlayerSlot: Int = 0,
    onEquipHeroForSlot: (slotIndex: Int, characterId: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("hero_collection_dialog"),
            color = Color(0xFF090D16)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🦸", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = "کلکسیون قهرمانان افسانه‌ای",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = RadiantGold
                                )
                                Text(
                                    text = "مشاهده جزئیات، انیمیشن‌ها و تجهیز قهرمانان به تفکیک بازیکن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.70f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .testTag("hero_collection_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "بستن",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Main Scrollable Content
                    HeroCollectionContent(
                        currentEquippedCharacterIds = currentEquippedCharacterIds,
                        playerCount = playerCount,
                        initialPlayerSlot = initialPlayerSlot,
                        onEquipHeroForSlot = onEquipHeroForSlot,
                        modifier = Modifier.weight(1f)
                    )

                    // Footer Close Button (>= 48dp)
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("hero_collection_done_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadiantGold,
                            contentColor = Color(0xFF140B24)
                        )
                    ) {
                        Text(
                            text = "ذخیره و بازگشت به بازی",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
