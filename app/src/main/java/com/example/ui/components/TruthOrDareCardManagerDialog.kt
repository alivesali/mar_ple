package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import com.example.model.CardCategory
import com.example.model.TruthOrDareCard
import com.example.model.toPersianDigits
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.MysticCardSurface
import com.example.ui.theme.MysticDeepPlum
import com.example.ui.theme.MysticIndigoSurface
import com.example.ui.theme.RadiantGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TruthOrDareCardManagerModal(
    cards: List<TruthOrDareCard>,
    enabledCategories: Set<CardCategory>,
    saveSuccessMessage: String?,
    onAddCard: (title: String, body: String, category: CardCategory) -> Unit,
    onUpdateCard: (card: TruthOrDareCard, newTitle: String, newBody: String, newCategory: CardCategory) -> Unit,
    onDeleteCard: (Long) -> Unit,
    onToggleCategoryEnabled: (CardCategory) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onDismiss()
    }

    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CardCategory.FRIENDLY) }
    var editingCard by remember { mutableStateOf<TruthOrDareCard?>(null) }
    var filterCategory by remember { mutableStateOf<CardCategory?>(null) } // null = همه دسته‌ها
    var validationError by remember { mutableStateOf<String?>(null) }

    val filteredCards = remember(cards, filterCategory) {
        if (filterCategory == null) {
            cards
        } else {
            cards.filter { it.category == filterCategory }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEB100820))
            .testTag("card_manager_modal"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MysticIndigoSurface)
                    .border(1.5.dp, RadiantGold.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RadiantGold.copy(alpha = 0.2f))
                            .border(1.5.dp, RadiantGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Style,
                            contentDescription = null,
                            tint = RadiantGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "مدیریت کارت‌های جرئت یا حقیقت",
                            style = MaterialTheme.typography.titleMedium,
                            color = RadiantGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "ذخیره دائمی روی دستگاه • مجموع کارت‌ها: ${cards.size.toPersianDigits()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MysticCardSurface)
                        .testTag("close_card_manager_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "بستن",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Visual Feedback when a card is saved/updated in Local Storage
            AnimatedVisibility(
                visible = saveSuccessMessage != null,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
            ) {
                if (saveSuccessMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF064E3B))
                            .border(1.5.dp, HopefulEmerald, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("card_save_feedback_banner"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = HopefulEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveSuccessMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Add / Edit Card Form
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.5.dp,
                                color = if (editingCard != null) RadiantGold else selectedCategory.primaryColor.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(22.dp)
                            ),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
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
                                Text(
                                    text = if (editingCard != null) {
                                        "✏️ ویرایش کارت انتخاب‌شده"
                                    } else {
                                        "➕ ساخت کارت جدید (ذخیره در حافظه لوکال)"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (editingCard != null) RadiantGold else Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                if (editingCard != null) {
                                    Text(
                                        text = "لغو ویرایش",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFFFF758F),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                editingCard = null
                                                titleInput = ""
                                                bodyInput = ""
                                                validationError = null
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Step 1: Card Title (Displayed at the top of the card)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "۱. تایتل بالای کارت (مثلاً «جرئت» یا «حقیقت»):",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.Bold
                                )

                                // Quick-fill helper chips for convenience
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("جرئت", "حقیقت", "چالش مار").forEach { preset ->
                                        val isCurrent = titleInput.trim() == preset
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isCurrent) RadiantGold else MysticCardSurface
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isCurrent) Color.White else RadiantGold.copy(alpha = 0.4f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    titleInput = preset
                                                    validationError = null
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .testTag("preset_title_$preset")
                                        ) {
                                            Text(
                                                text = preset,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isCurrent) Color(0xFF1A0F2E) else Color.White
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = titleInput,
                                    onValueChange = {
                                        titleInput = it.take(36)
                                        validationError = null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_title_input"),
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            text = "مثلاً: جرئت یا حقیقت...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.45f)
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = selectedCategory.primaryColor,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = selectedCategory.primaryColor
                                    )
                                )
                            }

                            // Step 2: Main Card Body (Challenge or Question below the title)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "۲. متن اصلی کارت (سوال یا چالشی که زیر تایتل نمایش داده می‌شود):",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = bodyInput,
                                    onValueChange = {
                                        bodyInput = it.take(260)
                                        validationError = null
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("card_body_input"),
                                    minLines = 2,
                                    maxLines = 4,
                                    placeholder = {
                                        Text(
                                            text = "متن چالش یا سوال خود را اینجا بنویسید...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.45f)
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = selectedCategory.primaryColor,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = selectedCategory.primaryColor
                                    )
                                )
                            }

                            // Step 3: Category Selection (Family / Friendly / Adult 18+)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "۳. دسته‌بندی کارت را انتخاب کنید:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CardCategory.entries.forEach { category ->
                                        val isSelected = selectedCategory == category
                                        val bg by animateColorAsState(
                                            targetValue = if (isSelected) {
                                                category.primaryColor.copy(alpha = 0.32f)
                                            } else {
                                                MysticCardSurface
                                            },
                                            label = "cat_select_${category.name}"
                                        )

                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(bg)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) category.primaryColor else Color.White.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable { selectedCategory = category }
                                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                                .testTag("select_card_category_${category.name}"),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = category.badgeEmoji,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = category.shortTitle,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) category.primaryColor else Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            if (validationError != null) {
                                Text(
                                    text = validationError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFF758F),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    val cleanTitle = titleInput.trim()
                                    val cleanBody = bodyInput.trim()
                                    if (cleanTitle.isEmpty()) {
                                        validationError = "لطفاً اول تایتل کارت (مثلاً «جرئت» یا «حقیقت») را وارد کنید."
                                        return@Button
                                    }
                                    if (cleanBody.isEmpty()) {
                                        validationError = "لطفاً متن اصلی کارت (چالش یا سوال) را بنویسید."
                                        return@Button
                                    }

                                    val currentEditing = editingCard
                                    if (currentEditing != null) {
                                        onUpdateCard(currentEditing, cleanTitle, cleanBody, selectedCategory)
                                        editingCard = null
                                    } else {
                                        onAddCard(cleanTitle, cleanBody, selectedCategory)
                                    }
                                    titleInput = ""
                                    bodyInput = ""
                                    validationError = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("save_card_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (editingCard != null) RadiantGold else HopefulEmerald,
                                    contentColor = if (editingCard != null) Color(0xFF1A0F2E) else Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = if (editingCard != null) Icons.Filled.Check else Icons.Filled.Add,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (editingCard != null) {
                                        "ذخیره تغییرات کارت"
                                    } else {
                                        "افزودن و ذخیره دائمی کارت"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // 2. Active Categories Filter for Snake Encounters
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MysticIndigoSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "دسته‌های فعال هنگام افتادن روی مار در بازی:",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CardCategory.entries.forEach { cat ->
                                    val isEnabled = cat in enabledCategories
                                    Row(
                                        modifier = Modifier
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isEnabled) cat.primaryColor.copy(alpha = 0.25f) else MysticCardSurface
                                            )
                                            .border(
                                                width = if (isEnabled) 1.5.dp else 1.dp,
                                                color = if (isEnabled) cat.primaryColor else Color.White.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onToggleCategoryEnabled(cat) }
                                            .padding(horizontal = 12.dp)
                                            .testTag("toggle_active_card_cat_${cat.name}"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = cat.badgeEmoji, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = cat.persianTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isEnabled) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = cat.primaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Category Tabs to View Cards by Category
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "مشاهده لیست کارت‌ها به تفکیک دسته:",
                            style = MaterialTheme.typography.titleMedium,
                            color = RadiantGold,
                            fontWeight = FontWeight.Bold
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isAllSelected = filterCategory == null
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isAllSelected) RadiantGold else MysticCardSurface)
                                    .clickable { filterCategory = null }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "همه (${cards.size.toPersianDigits()})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAllSelected) Color(0xFF1A0F2E) else Color.White
                                )
                            }

                            CardCategory.entries.forEach { cat ->
                                val count = cards.count { it.category == cat }
                                val isSelected = filterCategory == cat
                                Box(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) cat.primaryColor else MysticCardSurface)
                                        .border(1.dp, cat.primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable { filterCategory = cat }
                                        .padding(horizontal = 12.dp)
                                        .testTag("filter_tab_${cat.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${cat.badgeEmoji} ${cat.shortTitle} (${count.toPersianDigits()})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFF140B24) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Empty State or List of Saved User Cards
                if (filteredCards.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MysticCardSurface.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "🃏 هنوز هیچ کارتی در این بخش ساخته نشده است",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RadiantGold,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "طبق قوانین بازی، هیچ کارتی از پیش وجود ندارد و تمام کارت‌های جرئت یا حقیقت توسط خود شما ساخته می‌شوند و برای همیشه در حافظه دستگاه باقی می‌مانند.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredCards, key = { it.id }) { card ->
                        SavedTruthOrDareCardItem(
                            card = card,
                            onEdit = {
                                editingCard = card
                                titleInput = card.title
                                bodyInput = card.body
                                selectedCategory = card.category
                                validationError = null
                            },
                            onDelete = { onDeleteCard(card.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedTruthOrDareCardItem(
    card: TruthOrDareCard,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val category = card.category

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.8.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(category.primaryColor, RadiantGold.copy(alpha = 0.7f))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("saved_card_item_${card.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MysticCardSurface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Header: Category Badge + Edit/Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(category.primaryColor.copy(alpha = 0.22f))
                        .border(1.dp, category.primaryColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = category.badgeEmoji, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = category.persianTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = category.primaryColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MysticIndigoSurface)
                            .testTag("edit_card_${card.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "ویرایش کارت",
                            tint = RadiantGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MysticIndigoSurface)
                            .testTag("delete_card_${card.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "حذف کارت",
                            tint = DangerCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Card Title Displayed Prominently at the Top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                category.secondaryColor.copy(alpha = 0.85f),
                                MysticDeepPlum
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = RadiantGold,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            // Main Challenge / Question Text Displayed Below the Title
            Text(
                text = card.body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
