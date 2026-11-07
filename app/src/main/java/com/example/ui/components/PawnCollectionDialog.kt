package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.model.PawnSkin
import com.example.model.PawnSkinCatalog
import com.example.model.PawnSkinCategory
import com.example.model.toPersianDigits
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.PlayerColorNames
import com.example.ui.theme.PlayerTokenColors
import com.example.ui.theme.RadiantGold

/**
 * Fullscreen / Dialog Pawn Collection & Customization Screen.
 * Allows browsing, testing animations, and equipping custom pawn skins
 * independently for each player slot with guaranteed player color identification.
 */
@Composable
fun PawnCollectionDialog(
    currentEquippedSkinId: String = "classic_standard",
    playerPawnSkinIds: List<String> = emptyList(),
    playerCount: Int = 2,
    initialPlayerSlot: Int = 0,
    playerColorIndices: List<Int> = listOf(0, 1, 2, 3),
    onEquipSkinForSlot: (slot: Int, skinId: String) -> Unit,
    onEquipSkinForAllSlots: (skinId: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var activeSlot by remember { mutableIntStateOf(initialPlayerSlot.coerceIn(0, (playerCount - 1).coerceAtLeast(0))) }
    var selectedCategory by remember { mutableStateOf<PawnSkinCategory?>(null) } // null = All
    val initialSkinId = playerPawnSkinIds.getOrElse(activeSlot) { currentEquippedSkinId }
    var previewSkin by remember { mutableStateOf(PawnSkinCatalog.getSkinById(initialSkinId)) }
    var previewMode by remember { mutableStateOf(PawnPreviewMode.IDLE) }

    val activeSlotEquippedSkinId = playerPawnSkinIds.getOrElse(activeSlot) { currentEquippedSkinId }
    val activePlayerColor = PlayerTokenColors[
        playerColorIndices.getOrElse(activeSlot) { activeSlot } % PlayerTokenColors.size
    ]
    val activeColorName = PlayerColorNames[
        playerColorIndices.getOrElse(activeSlot) { activeSlot } % PlayerColorNames.size
    ]

    val filteredSkins = remember(selectedCategory) {
        if (selectedCategory == null) {
            PawnSkinCatalog.allSkins
        } else {
            PawnSkinCatalog.getSkinsByCategory(selectedCategory!!)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("pawn_collection_dialog"),
            color = Color(0xFF090D16)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Subtle ambient backdrop
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    previewSkin.primaryColor.copy(alpha = 0.22f),
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
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // 1. Header Bar
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
                                Text(
                                    text = "مجموعه و شخصی‌سازی مهره‌ها",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Pawn Collection • ${PawnSkinCatalog.allSkins.size.toPersianDigits()} پوسته زیبا و مستقل از قهرمان",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Player Slot Selector Row (Choose which player is equipping)
                    if (playerCount > 1) {
                        PlayerSlotSelectorRow(
                            playerCount = playerCount,
                            activeSlot = activeSlot,
                            playerColorIndices = playerColorIndices,
                            onSelectSlot = { newSlot ->
                                activeSlot = newSlot
                                val newSlotSkinId = playerPawnSkinIds.getOrElse(newSlot) { currentEquippedSkinId }
                                previewSkin = PawnSkinCatalog.getSkinById(newSlotSkinId)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 3. Top Interactive Pawn Showcase Stage (Selected Skin Preview)
                    PawnPreviewStage(
                        skin = previewSkin,
                        playerColor = activePlayerColor,
                        previewMode = previewMode,
                        onSelectPreviewMode = { previewMode = it },
                        isEquipped = previewSkin.id == activeSlotEquippedSkinId,
                        onEquipForActive = {
                            onEquipSkinForSlot(activeSlot, previewSkin.id)
                        },
                        onEquipForAll = {
                            onEquipSkinForAllSlots(previewSkin.id)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Category Filter Tabs Row
                    CategoryFilterRow(
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Grid of Skins
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(filteredSkins, key = { it.id }) { skin ->
                            val isSelected = skin.id == previewSkin.id
                            val isEquipped = skin.id == activeSlotEquippedSkinId

                            PawnSkinCard(
                                skin = skin,
                                playerColor = activePlayerColor,
                                isSelected = isSelected,
                                isEquipped = isEquipped,
                                onClick = {
                                    previewSkin = skin
                                }
                            )
                        }
                    }

                    // 6. Bottom Done Button
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RadiantGold)
                    ) {
                        Text(
                            text = "تأیید و بازگشت به بازی",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF140B24),
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PLAYER SLOT SELECTOR
// -------------------------------------------------------------------------------------------------
@Composable
private fun PlayerSlotSelectorRow(
    playerCount: Int,
    activeSlot: Int,
    playerColorIndices: List<Int>,
    onSelectSlot: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "تجهیز برای:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Bold
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(playerCount) { slot ->
                val isSelected = slot == activeSlot
                val slotColor = PlayerTokenColors[
                    playerColorIndices.getOrElse(slot) { slot } % PlayerTokenColors.size
                ]

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) slotColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) slotColor else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectSlot(slot) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(slotColor)
                        )
                        Text(
                            text = "بازیکن ${(slot + 1).toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// INTERACTIVE PAWN PREVIEW STAGE
// -------------------------------------------------------------------------------------------------
@Composable
private fun PawnPreviewStage(
    skin: PawnSkin,
    playerColor: Color,
    previewMode: PawnPreviewMode,
    onSelectPreviewMode: (PawnPreviewMode) -> Unit,
    isEquipped: Boolean,
    onEquipForActive: () -> Unit,
    onEquipForAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.90f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(skin.primaryColor.copy(alpha = 0.65f), RadiantGold.copy(alpha = 0.45f))
            )
        )
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
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Large Interactive Pawn Viewport
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF090D16).copy(alpha = 0.75f))
                        .border(1.dp, skin.glowColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PawnSkinRenderer(
                        skin = skin,
                        playerColor = playerColor,
                        size = 105.dp,
                        previewMode = previewMode,
                        showPedestal = true,
                        showAura = true
                    )
                }

                // Skin Info & Equip CTA
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = skin.namePersian,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(skin.primaryColor.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${skin.category.emoji} ${skin.category.persianTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = skin.glowColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        text = skin.descriptionPersian,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "ردپا: ${skin.trailType.particleEmoji} ${skin.trailType.persianLabel} • متریال: ${skin.materialType.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RadiantGold,
                        fontSize = 10.sp
                    )

                    // Equip Buttons
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onEquipForActive,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEquipped) HopefulEmerald else skin.primaryColor
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isEquipped) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("تجهیز شده ✓", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                } else {
                                    Text("تجهیز این مهره", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onEquipForAll,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("برای همه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Preview Stance Mode Chips (Idle, Trail, Landing, Victory)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آزمون حالت:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(PawnPreviewMode.entries) { mode ->
                        val isSelected = mode == previewMode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) skin.primaryColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) skin.glowColor else Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectPreviewMode(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${mode.iconEmoji} ${mode.persianLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// CATEGORY FILTER ROW
// -------------------------------------------------------------------------------------------------
@Composable
private fun CategoryFilterRow(
    selectedCategory: PawnSkinCategory?,
    onSelectCategory: (PawnSkinCategory?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // "All" Chip
        item {
            val isSelected = selectedCategory == null
            CategoryPill(
                label = "همه پوسته‌ها",
                emoji = "🌟",
                isSelected = isSelected,
                onClick = { onSelectCategory(null) }
            )
        }

        items(PawnSkinCategory.entries) { cat ->
            val isSelected = selectedCategory == cat
            CategoryPill(
                label = cat.persianTitle,
                emoji = cat.emoji,
                isSelected = isSelected,
                onClick = { onSelectCategory(cat) }
            )
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) RadiantGold else Color.White.copy(alpha = 0.07f))
            .border(
                width = 1.dp,
                color = if (isSelected) RadiantGold else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color(0xFF140B24) else Color.White,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PAWN SKIN GRID CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun PawnSkinCard(
    skin: PawnSkin,
    playerColor: Color,
    isSelected: Boolean,
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> RadiantGold
            isEquipped -> HopefulEmerald
            else -> skin.primaryColor.copy(alpha = 0.35f)
        },
        label = "pawn_card_border_${skin.id}"
    )

    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("pawn_skin_card_${skin.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) skin.primaryColor.copy(alpha = 0.22f) else MysticCardSurface.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Miniature Pawn Renderer
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                PawnSkinRenderer(
                    skin = skin,
                    playerColor = playerColor,
                    size = 50.dp,
                    showPedestal = true,
                    showAura = false
                )

                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(HopefulEmerald)
                            .size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Text(
                text = skin.namePersian,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 10.5.sp
            )

            Text(
                text = skin.category.persianTitle,
                style = MaterialTheme.typography.labelSmall,
                color = skin.glowColor,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
