package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * The three official Truth or Dare card categories requested in Phase 4.
 */
enum class CardCategory(
    val persianTitle: String,
    val shortTitle: String,
    val subtitle: String,
    val badgeEmoji: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    FAMILY(
        persianTitle = "خانوادگی",
        shortTitle = "خانوادگی",
        subtitle = "مناسب همه سنین • شاد و صمیمی",
        badgeEmoji = "🏡",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF064E3B)
    ),
    FRIENDLY(
        persianTitle = "رفاقتی / فرندلی",
        shortTitle = "رفاقتی",
        subtitle = "دوستانه، خنده‌دار و پرانرژی",
        badgeEmoji = "🎉",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFF78350F)
    ),
    ADULT_18_PLUS(
        persianTitle = "سکسی (بزرگسال +۱۸)",
        shortTitle = "سکسی +۱۸",
        subtitle = "مخصوص بزرگسالان – فقط ۱۸+",
        badgeEmoji = "🔥 +۱۸",
        primaryColor = Color(0xFFF43F5E),
        secondaryColor = Color(0xFF881337)
    );

    companion object {
        fun fromName(name: String): CardCategory {
            return entries.find { it.name == name } ?: FRIENDLY
        }
    }
}

/**
 * User-authored Truth or Dare card.
 * Notice: No cards exist by default; every card is created by the user and persisted locally.
 * - [title]: Displayed at the top of the card (e.g., "جرئت" or "حقیقت").
 * - [body]: Displayed below the title (the actual challenge or question).
 * - [category]: One of FAMILY, FRIENDLY, or ADULT_18_PLUS.
 */
data class TruthOrDareCard(
    val id: Long = 0L,
    val title: String,
    val body: String,
    val category: CardCategory,
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * Active state when a player lands on a snake and draws a random Truth or Dare card.
 */
data class ActiveSnakeCardDraw(
    val player: Player,
    val snakeStartTile: Int,
    val snakeEndTile: Int,
    val card: TruthOrDareCard,
    val isRevealed: Boolean = false
)
