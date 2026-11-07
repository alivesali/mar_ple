package com.example.model

import androidx.compose.ui.graphics.Color

data class SnakeColorPalette(
    val highlight: Color,
    val midBody: Color,
    val shadow: Color,
    val scaleAccent: Color,
    val bellyColor: Color = Color(0xFFFDE68A),
    val eyeGlowColor: Color = Color(0xFFFFB703)
)

/**
 * Complete Visual World Themes for the game board, surrounding environment, frame, tiles, lighting, and weather.
 * Default is THEME 01 — BLACK & GOLD (مشکی و طلایی سلطنتی).
 */
enum class BoardTheme(
    val themeCode: String,
    val englishName: String,
    val persianName: String,
    val subtitle: String,
    val emoji: String,
    val isDarkLuxury: Boolean,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val screenBgTop: Color,
    val screenBgMid: Color,
    val screenBgBottom: Color,
    val cardSurfaceColor: Color,
    val cardInnerSurfaceColor: Color,
    val boardFrameOuterTop: Color,
    val boardFrameOuterMid: Color,
    val boardFrameOuterBottom: Color,
    val boardBorderHighlight: Color,
    val boardBorderMain: Color,
    val boardBorderShadow: Color,
    val tileRecessColor: Color,
    val lightTileTop: Color,
    val lightTileBottom: Color,
    val darkTileTop: Color,
    val darkTileBottom: Color,
    val tilePalette: List<Pair<Color, Color>>,
    val tileBorderColor: Color,
    val tileBorderGoldAlpha: Float,
    val tileTextColorRgb: Triple<Int, Int, Int>,
    val ladderRailStart: Color,
    val ladderRailMid: Color,
    val ladderRailEnd: Color,
    val ladderRungMain: Color,
    val ladderRungShadow: Color,
    val snakePalettes: List<SnakeColorPalette>
) {
    BLACK_GOLD(
        themeCode = "THEME 01",
        englishName = "BLACK & GOLD",
        persianName = "مشکی و طلایی",
        subtitle = "فضای مشکی و طلایی لوکس و سلطنتی • حاشیه طلایی درخشان • خانه‌های مشکی، زغالی و طلایی تیره با ذرات طلایی ظریف",
        emoji = "👑",
        isDarkLuxury = true,
        primaryAccent = Color(0xFFFFD166),
        secondaryAccent = Color(0xFFF59E0B),
        screenBgTop = Color(0xFF141008),
        screenBgMid = Color(0xFF0A0805),
        screenBgBottom = Color(0xFF040302),
        cardSurfaceColor = Color(0xFF17130B),
        cardInnerSurfaceColor = Color(0xFF221C10),
        boardFrameOuterTop = Color(0xFF2E2210),
        boardFrameOuterMid = Color(0xFF181208),
        boardFrameOuterBottom = Color(0xFF090704),
        boardBorderHighlight = Color(0xFFFFF3B0),
        boardBorderMain = Color(0xFFD97706),
        boardBorderShadow = Color(0xFF451A03),
        tileRecessColor = Color(0xFF050402),
        lightTileTop = Color(0xFF282115),
        lightTileBottom = Color(0xFF1B160E),
        darkTileTop = Color(0xFF18140D),
        darkTileBottom = Color(0xFF0E0B07),
        tilePalette = listOf(
            // Deep Obsidian Black
            Pair(Color(0xFF17140E), Color(0xFF0C0A07)),
            // Rich Charcoal & Warm Gold Tint
            Pair(Color(0xFF262015), Color(0xFF19140C)),
            // Dark Antique Bronze-Gold
            Pair(Color(0xFF332611), Color(0xFF1F170A)),
            // Velvet Charcoal
            Pair(Color(0xFF201B13), Color(0xFF120F0A))
        ),
        tileBorderColor = Color(0xFFFFD166),
        tileBorderGoldAlpha = 0.56f,
        tileTextColorRgb = Triple(253, 224, 71),
        ladderRailStart = Color(0xFFFFF3B0),
        ladderRailMid = Color(0xFFF59E0B),
        ladderRailEnd = Color(0xFFB45309),
        ladderRungMain = Color(0xFFFDE047),
        ladderRungShadow = Color(0xFF78350F),
        snakePalettes = listOf(
            SnakeColorPalette(
                highlight = Color(0xFFFF8FA3),
                midBody = Color(0xFFB91C1C),
                shadow = Color(0xFF3B0404),
                scaleAccent = Color(0xFFFFD166),
                bellyColor = Color(0xFFFDE68A),
                eyeGlowColor = Color(0xFFFFEA00)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFFDE047),
                midBody = Color(0xFFB45309),
                shadow = Color(0xFF1C1917),
                scaleAccent = Color(0xFFFFFBEB),
                bellyColor = Color(0xFFFEF08A),
                eyeGlowColor = Color(0xFFFF3D00)
            ),
            SnakeColorPalette(
                highlight = Color(0xFF6EE7B7),
                midBody = Color(0xFF047857),
                shadow = Color(0xFF022C22),
                scaleAccent = Color(0xFFFBBF24),
                bellyColor = Color(0xFFD1FAE5),
                eyeGlowColor = Color(0xFFFFD600)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFE9D5FF),
                midBody = Color(0xFF7E22CE),
                shadow = Color(0xFF2E1065),
                scaleAccent = Color(0xFFFACC15),
                bellyColor = Color(0xFFF3E8FF),
                eyeGlowColor = Color(0xFFFF9100)
            )
        )
    ),

    JUNGLE(
        themeCode = "THEME 02",
        englishName = "MYSTIC FOREST",
        persianName = "جنگل اسرارآمیز",
        subtitle = "درختان کهنسال، رودخانه جنگلی، پرتو خورشید، کرم‌های شب‌تاب، مه ملایم و قاب چوبی خزه‌پوش",
        emoji = "🌲",
        isDarkLuxury = false,
        primaryAccent = Color(0xFF34D399),
        secondaryAccent = Color(0xFFA3E635),
        screenBgTop = Color(0xFF0B261A),
        screenBgMid = Color(0xFF071B12),
        screenBgBottom = Color(0xFF04100A),
        cardSurfaceColor = Color(0xFF112C1F),
        cardInnerSurfaceColor = Color(0xFF193D2C),
        boardFrameOuterTop = Color(0xFF4A3520),
        boardFrameOuterMid = Color(0xFF362515),
        boardFrameOuterBottom = Color(0xFF1F150B),
        boardBorderHighlight = Color(0xFFA7F3D0),
        boardBorderMain = Color(0xFF4D7C0F),
        boardBorderShadow = Color(0xFF27190E),
        tileRecessColor = Color(0xFF1B2E1E),
        lightTileTop = Color(0xFFECFCCB),
        lightTileBottom = Color(0xFFD9F99D),
        darkTileTop = Color(0xFFD1FAE5),
        darkTileBottom = Color(0xFFA7F3D0),
        tilePalette = listOf(
            // سبز روشن طبیعی (Natural Light Green)
            Pair(Color(0xFFECFCCB), Color(0xFFD9F99D)),
            // سبز جنگلی ملایم (Forest Green)
            Pair(Color(0xFFBBF7D0), Color(0xFF86EFAC)),
            // سبز نعنایی (Mint Green)
            Pair(Color(0xFFD1FAE5), Color(0xFFA7F3D0)),
            // سبز زیتونی ملایم (Soft Olive Green)
            Pair(Color(0xFFE4F1C3), Color(0xFFC5DCA0))
        ),
        tileBorderColor = Color(0xFF3F6212),
        tileBorderGoldAlpha = 0.42f,
        tileTextColorRgb = Triple(20, 58, 32),
        // Carved Forest Wood & Bamboo Ladders
        ladderRailStart = Color(0xFFB45309),
        ladderRailMid = Color(0xFF92400E),
        ladderRailEnd = Color(0xFF65A30D),
        ladderRungMain = Color(0xFFD97706),
        ladderRungShadow = Color(0xFF451A03),
        snakePalettes = listOf(
            // Emerald Forest Tree Boa
            SnakeColorPalette(
                highlight = Color(0xFF86EFAC),
                midBody = Color(0xFF15803D),
                shadow = Color(0xFF052E16),
                scaleAccent = Color(0xFFFACC15),
                bellyColor = Color(0xFFDCFCE7),
                eyeGlowColor = Color(0xFFFDE047)
            ),
            // Moss & Golden Bark Viper
            SnakeColorPalette(
                highlight = Color(0xFFBEF264),
                midBody = Color(0xFF4D7C0F),
                shadow = Color(0xFF1A2E05),
                scaleAccent = Color(0xFFFDE047),
                bellyColor = Color(0xFFECFCCB),
                eyeGlowColor = Color(0xFFFFB703)
            ),
            // Rainforest Crimson Orchid Serpent
            SnakeColorPalette(
                highlight = Color(0xFFFDA4AF),
                midBody = Color(0xFFBE123C),
                shadow = Color(0xFF4C0519),
                scaleAccent = Color(0xFFA3E635),
                bellyColor = Color(0xFFFFE4E6),
                eyeGlowColor = Color(0xFF4ADE80)
            )
        )
    ),

    OCEAN_BEACH(
        themeCode = "THEME 03",
        englishName = "OCEAN & BEACH",
        persianName = "ساحلی و دریایی",
        subtitle = "جزیره استوایی و امواج آرام دریا • شن‌های ساحلی، صدف، نخل‌های ظریف و درخشش خورشید روی آب",
        emoji = "🌊",
        isDarkLuxury = false,
        primaryAccent = Color(0xFF22D3EE),
        secondaryAccent = Color(0xFFFDE68A),
        screenBgTop = Color(0xFF073B5C),
        screenBgMid = Color(0xFF0A4F70),
        screenBgBottom = Color(0xFF1E3A4C),
        cardSurfaceColor = Color(0xFF0D3852),
        cardInnerSurfaceColor = Color(0xFF144969),
        boardFrameOuterTop = Color(0xFF0284C7),
        boardFrameOuterMid = Color(0xFF0E7490),
        boardFrameOuterBottom = Color(0xFFD97706),
        boardBorderHighlight = Color(0xFFFEF3C7),
        boardBorderMain = Color(0xFF06B6D4),
        boardBorderShadow = Color(0xFF0C4A6E),
        tileRecessColor = Color(0xFF0C3B54),
        lightTileTop = Color(0xFFF8FAFC),
        lightTileBottom = Color(0xFFE0F2FE),
        darkTileTop = Color(0xFFCCFBF1),
        darkTileBottom = Color(0xFF99F6E4),
        tilePalette = listOf(
            // آبی آسمانی (Sky Blue)
            Pair(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            // فیروزه‌ای (Turquoise)
            Pair(Color(0xFFCCFBF1), Color(0xFF99F6E4)),
            // سفید مرواریدی (Pearl White)
            Pair(Color(0xFFFFFBEB), Color(0xFFF1F5F9)),
            // آبی اقیانوسی ملایم (Ocean Blue)
            Pair(Color(0xFFDBEAFE), Color(0xFF93C5FD)),
            // شنی روشن (Light Sandy Beige)
            Pair(Color(0xFFFEF3C7), Color(0xFFFDE68A))
        ),
        tileBorderColor = Color(0xFF0284C7),
        tileBorderGoldAlpha = 0.40f,
        tileTextColorRgb = Triple(12, 62, 96),
        // Coastal Pearl, Coral & Driftwood Ladders
        ladderRailStart = Color(0xFF38BDF8),
        ladderRailMid = Color(0xFF06B6D4),
        ladderRailEnd = Color(0xFFFBBF24),
        ladderRungMain = Color(0xFFE0F2FE),
        ladderRungShadow = Color(0xFF0369A1),
        snakePalettes = listOf(
            // Lagoon Turquoise Sea Serpent
            SnakeColorPalette(
                highlight = Color(0xFF67E8F9),
                midBody = Color(0xFF0284C7),
                shadow = Color(0xFF082F49),
                scaleAccent = Color(0xFFFDE047),
                bellyColor = Color(0xFFE0F2FE),
                eyeGlowColor = Color(0xFFFEF08A)
            ),
            // Coral Reef Rose Serpent
            SnakeColorPalette(
                highlight = Color(0xFFFDA4AF),
                midBody = Color(0xFFF43F5E),
                shadow = Color(0xFF881337),
                scaleAccent = Color(0xFF5EEAD4),
                bellyColor = Color(0xFFFFE4E6),
                eyeGlowColor = Color(0xFF67E8F9)
            ),
            // Tropical Pearl & Teal Sea Snake
            SnakeColorPalette(
                highlight = Color(0xFF5EEAD4),
                midBody = Color(0xFF0D9488),
                shadow = Color(0xFF134E4A),
                scaleAccent = Color(0xFFFEF08A),
                bellyColor = Color(0xFFCCFBF1),
                eyeGlowColor = Color(0xFFFFD166)
            )
        )
    ),

    DESERT(
        themeCode = "THEME 04",
        englishName = "DESERT & DUNES",
        persianName = "کویر و شنزار",
        subtitle = "تپه‌های شنی در غروب طلایی-نارنجی • صخره‌های کویری، سایه‌های کشیده، موج گرما و قاب ماسه‌سنگی",
        emoji = "🏜️",
        isDarkLuxury = false,
        primaryAccent = Color(0xFFF59E0B),
        secondaryAccent = Color(0xFFEA580C),
        screenBgTop = Color(0xFF5C240E),
        screenBgMid = Color(0xFF3D1708),
        screenBgBottom = Color(0xFF1F0B04),
        cardSurfaceColor = Color(0xFF3E1E0C),
        cardInnerSurfaceColor = Color(0xFF542912),
        boardFrameOuterTop = Color(0xFFC26A27),
        boardFrameOuterMid = Color(0xFF8C4215),
        boardFrameOuterBottom = Color(0xFF4A2008),
        boardBorderHighlight = Color(0xFFFDE68A),
        boardBorderMain = Color(0xFFD97706),
        boardBorderShadow = Color(0xFF5C2309),
        tileRecessColor = Color(0xFF3A1A08),
        lightTileTop = Color(0xFFFFFBEB),
        lightTileBottom = Color(0xFFFEF3C7),
        darkTileTop = Color(0xFFFDE68A),
        darkTileBottom = Color(0xFFFCD34D),
        tilePalette = listOf(
            // کرم شنی (Sandy Cream)
            Pair(Color(0xFFFFFBEB), Color(0xFFFEF3C7)),
            // بژ طبیعی (Warm Beige)
            Pair(Color(0xFFF5EBD6), Color(0xFFE6D5B8)),
            // طلایی مات (Matte Gold)
            Pair(Color(0xFFFEF08A), Color(0xFFFDE047)),
            // قهوه‌ای روشن کویری (Light Sandstone Brown)
            Pair(Color(0xFFEDD5B3), Color(0xFFDAB88B)),
            // نارنجی ملایم غروب (Soft Sunset Orange)
            Pair(Color(0xFFFFEDD5), Color(0xFFFED7AA))
        ),
        tileBorderColor = Color(0xFFB45309),
        tileBorderGoldAlpha = 0.46f,
        tileTextColorRgb = Triple(92, 38, 10),
        // Desert Sandstone & Sun-Forged Copper Ladders
        ladderRailStart = Color(0xFFF59E0B),
        ladderRailMid = Color(0xFFD97706),
        ladderRailEnd = Color(0xFF9A3412),
        ladderRungMain = Color(0xFFFDE68A),
        ladderRungShadow = Color(0xFF78350F),
        snakePalettes = listOf(
            // Sidewinder Horned Desert Viper
            SnakeColorPalette(
                highlight = Color(0xFFFDBA74),
                midBody = Color(0xFFEA580C),
                shadow = Color(0xFF7C2D12),
                scaleAccent = Color(0xFFFEF08A),
                bellyColor = Color(0xFFFFEDD5),
                eyeGlowColor = Color(0xFFFFEA00)
            ),
            // Golden Dune Cobra
            SnakeColorPalette(
                highlight = Color(0xFFFDE047),
                midBody = Color(0xFFB45309),
                shadow = Color(0xFF451A03),
                scaleAccent = Color(0xFFFFEDD5),
                bellyColor = Color(0xFFFEF3C7),
                eyeGlowColor = Color(0xFFFF3D00)
            ),
            // Canyon Terracotta Serpent
            SnakeColorPalette(
                highlight = Color(0xFFFCA5A5),
                midBody = Color(0xFFDC2626),
                shadow = Color(0xFF450A0A),
                scaleAccent = Color(0xFFFBBF24),
                bellyColor = Color(0xFFFEE2E2),
                eyeGlowColor = Color(0xFFFDE047)
            )
        )
    ),

    ARCTIC_ICE(
        themeCode = "THEME 05",
        englishName = "ARCTIC & ICE",
        persianName = "قطب یخی",
        subtitle = "یخچال‌های قطبی، گوشه‌های یخ‌زده و قندیل‌های آویزان • ذرات برف در بالای صفحه و خانه‌های شیشه‌ای یخی",
        emoji = "❄️",
        isDarkLuxury = false,
        primaryAccent = Color(0xFF7DD3FC),
        secondaryAccent = Color(0xFFE0F2FE),
        screenBgTop = Color(0xFF081426),
        screenBgMid = Color(0xFF0E233E),
        screenBgBottom = Color(0xFF06101E),
        cardSurfaceColor = Color(0xFF10243E),
        cardInnerSurfaceColor = Color(0xFF173356),
        boardFrameOuterTop = Color(0xFF7DD3FC),
        boardFrameOuterMid = Color(0xFF0284C7),
        boardFrameOuterBottom = Color(0xFF0C4A6E),
        boardBorderHighlight = Color(0xFFFFFFFF),
        boardBorderMain = Color(0xFF7DD3FC),
        boardBorderShadow = Color(0xFF075985),
        tileRecessColor = Color(0xFF082038),
        lightTileTop = Color(0xFFFFFFFF),
        lightTileBottom = Color(0xFFE0F2FE),
        darkTileTop = Color(0xFFF0F9FF),
        darkTileBottom = Color(0xFFBAE6FD),
        tilePalette = listOf(
            // سفید برفی (Snow White)
            Pair(Color(0xFFFFFFFF), Color(0xFFF0F9FF)),
            // آبی یخی (Ice Blue)
            Pair(Color(0xFFE0F2FE), Color(0xFFBAE6FD)),
            // آبی روشن (Light Polar Blue)
            Pair(Color(0xFFDBEAFE), Color(0xFFBFDBFE)),
            // فیروزه‌ای سرد (Cold Arctic Turquoise)
            Pair(Color(0xFFECFEFF), Color(0xFFA5F3FC)),
            // نقره‌ای بلورین (Crystalline Silver)
            Pair(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
        ),
        tileBorderColor = Color(0xFF38BDF8),
        tileBorderGoldAlpha = 0.50f,
        tileTextColorRgb = Triple(12, 48, 92),
        // Translucent Glacier Crystal & Frosted Silver Ladders
        ladderRailStart = Color(0xFFFFFFFF),
        ladderRailMid = Color(0xFF7DD3FC),
        ladderRailEnd = Color(0xFF38BDF8),
        ladderRungMain = Color(0xFFE0F2FE),
        ladderRungShadow = Color(0xFF0369A1),
        snakePalettes = listOf(
            // Frost Glacier Wyrm
            SnakeColorPalette(
                highlight = Color(0xFFFFFFFF),
                midBody = Color(0xFF0284C7),
                shadow = Color(0xFF0C4A6E),
                scaleAccent = Color(0xFFE0F2FE),
                bellyColor = Color(0xFFF0F9FF),
                eyeGlowColor = Color(0xFF67E8F9)
            ),
            // Aurora Crystal Serpent
            SnakeColorPalette(
                highlight = Color(0xFFE9D5FF),
                midBody = Color(0xFF6366F1),
                shadow = Color(0xFF312E81),
                scaleAccent = Color(0xFFA5F3FC),
                bellyColor = Color(0xFFEEF2FF),
                eyeGlowColor = Color(0xFF38BDF8)
            ),
            // Polar Cyan Frost Adder
            SnakeColorPalette(
                highlight = Color(0xFFA5F3FC),
                midBody = Color(0xFF0891B2),
                shadow = Color(0xFF164E63),
                scaleAccent = Color(0xFFFFFFFF),
                bellyColor = Color(0xFFECFEFF),
                eyeGlowColor = Color(0xFFE0F2FE)
            )
        )
    );

    val defaultEnvironmentEffect: EnvironmentEffectType
        get() = when (this) {
            BLACK_GOLD -> EnvironmentEffectType.GOLDEN_DUST
            JUNGLE -> EnvironmentEffectType.MAGIC_PARTICLES
            OCEAN_BEACH -> EnvironmentEffectType.AURORA
            DESERT -> EnvironmentEffectType.FIRE_EMBERS
            ARCTIC_ICE -> EnvironmentEffectType.SNOWFALL
        }

    companion object {
        fun fromName(name: String?): BoardTheme {
            return entries.find { it.name == name } ?: BLACK_GOLD
        }
    }
}

/**
 * Configurable AI Opponent Difficulty for Single-Player (1-Player) mode.
 */
enum class AiDifficulty(
    val persianTitle: String,
    val description: String,
    val badgeColor: Color
) {
    EASY(
        persianTitle = "آسان (مبتدی)",
        description = "ربات تصمیم‌های ساده می‌گیرد و به ندرت از قابلیت ویژه استفاده می‌کند",
        badgeColor = Color(0xFF10B981)
    ),
    MEDIUM(
        persianTitle = "متوسط (متعادل)",
        description = "ربات در مواقع مناسب قابلیت کاراکترش را فعال می‌کند و هوشمندانه تاس می‌ریزد",
        badgeColor = Color(0xFFF59E0B)
    ),
    HARD(
        persianTitle = "سخت (استراتژیک)",
        description = "ربات بهترین زمان برای احضار پله، انجماد پیشتاز و انتخاب دقیق تاس را محاسبه می‌کند!",
        badgeColor = Color(0xFFEF4444)
    );

    companion object {
        fun fromName(name: String?): AiDifficulty {
            return entries.find { it.name == name } ?: MEDIUM
        }
    }
}

/**
 * Dedicated Visual Themes for Board Snakes (تم ظاهری مارها).
 * Designed to be slender, sleek, and non-distracting so tile numbers and tokens remain crystal-clear.
 */
enum class SnakeVisualTheme(
    val persianName: String,
    val description: String,
    val emoji: String,
    val thicknessScale: Float,
    val waveScale: Float,
    val wiggleScale: Float,
    val bodyAlpha: Float,
    val showScales: Boolean,
    val showHood: Boolean,
    val showFangs: Boolean,
    val customPalettes: List<SnakeColorPalette>? // null = use active BoardTheme palettes
) {
    ROYAL_GOLD_SLIM(
        persianName = "طلایی و مشکی ظریف",
        description = "مارهای باریک و شیک با بدنه ابسیدین و خطوط طلایی ملایم • بدون شلوغی",
        emoji = "👑",
        thicknessScale = 0.88f,
        waveScale = 0.82f,
        wiggleScale = 0.25f,
        bodyAlpha = 0.90f,
        showScales = true,
        showHood = false,
        showFangs = false,
        customPalettes = listOf(
            SnakeColorPalette(
                highlight = Color(0xFFFDE68A),
                midBody = Color(0xFFB45309),
                shadow = Color(0xFF1C1917),
                scaleAccent = Color(0xFFFFFBEB),
                bellyColor = Color(0xFFFEF08A),
                eyeGlowColor = Color(0xFFFFD166)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFFCA5A5),
                midBody = Color(0xFF991B1B),
                shadow = Color(0xFF290909),
                scaleAccent = Color(0xFFFBBF24),
                bellyColor = Color(0xFFFDE68A),
                eyeGlowColor = Color(0xFFFFEA00)
            ),
            SnakeColorPalette(
                highlight = Color(0xFF6EE7B7),
                midBody = Color(0xFF065F46),
                shadow = Color(0xFF022C22),
                scaleAccent = Color(0xFFFCD34D),
                bellyColor = Color(0xFFD1FAE5),
                eyeGlowColor = Color(0xFFFFD166)
            )
        )
    ),

    MINIMAL_SILK(
        persianName = "مینیمال ابریشمی (خلوت)",
        description = "فوق‌العاده نازک، ساده و بدون پولک یا حرکت اضافی • تمرکز ۱۰۰٪ روی خانه‌ها",
        emoji = "✨",
        thicknessScale = 0.72f,
        waveScale = 0.65f,
        wiggleScale = 0.0f,
        bodyAlpha = 0.82f,
        showScales = false,
        showHood = false,
        showFangs = false,
        customPalettes = listOf(
            SnakeColorPalette(
                highlight = Color(0xFFFDE047),
                midBody = Color(0xFFD97706),
                shadow = Color(0xFF451A03),
                scaleAccent = Color(0xFFFFFBEB),
                eyeGlowColor = Color(0xFFFFFBEB)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFFDA4AF),
                midBody = Color(0xFFE11D48),
                shadow = Color(0xFF4C0519),
                scaleAccent = Color(0xFFFFE4E6),
                eyeGlowColor = Color(0xFFFFFBEB)
            ),
            SnakeColorPalette(
                highlight = Color(0xFF93C5FD),
                midBody = Color(0xFF2563EB),
                shadow = Color(0xFF1E3A8A),
                scaleAccent = Color(0xFFDBEAFE),
                eyeGlowColor = Color(0xFFFFFBEB)
            )
        )
    ),

    PHANTOM_SMOKE(
        persianName = "سایه و دود مات",
        description = "مارهای نیمه‌شفاف و ملایم با رنگ‌های تیره و بنفش دودی که هرگز چشم را خسته نمی‌کنند",
        emoji = "🌑",
        thicknessScale = 0.80f,
        waveScale = 0.75f,
        wiggleScale = 0.15f,
        bodyAlpha = 0.76f,
        showScales = true,
        showHood = false,
        showFangs = false,
        customPalettes = listOf(
            SnakeColorPalette(
                highlight = Color(0xFFD8B4FE),
                midBody = Color(0xFF581C87),
                shadow = Color(0xFF1E1B4B),
                scaleAccent = Color(0xFFE9D5FF),
                eyeGlowColor = Color(0xFFC084FC)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFCBD5E1),
                midBody = Color(0xFF475569),
                shadow = Color(0xFF0F172A),
                scaleAccent = Color(0xFF94A3B8),
                eyeGlowColor = Color(0xFF38BDF8)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFFECDD3),
                midBody = Color(0xFF881337),
                shadow = Color(0xFF1F0910),
                scaleAccent = Color(0xFFFDA4AF),
                eyeGlowColor = Color(0xFFFB7185)
            )
        )
    ),

    EMERALD_JADE(
        persianName = "زمرد و یشم ظریف",
        description = "مارهای باریک به رنگ سبز یشمی و فیروزه‌ای ملایم با خال‌های ظریف",
        emoji = "🐍",
        thicknessScale = 0.86f,
        waveScale = 0.80f,
        wiggleScale = 0.25f,
        bodyAlpha = 0.88f,
        showScales = true,
        showHood = false,
        showFangs = false,
        customPalettes = listOf(
            SnakeColorPalette(
                highlight = Color(0xFF6EE7B7),
                midBody = Color(0xFF059669),
                shadow = Color(0xFF064E3B),
                scaleAccent = Color(0xFFA7F3D0),
                eyeGlowColor = Color(0xFFFDE047)
            ),
            SnakeColorPalette(
                highlight = Color(0xFF5EEAD4),
                midBody = Color(0xFF0D9488),
                shadow = Color(0xFF134E4A),
                scaleAccent = Color(0xFF99F6E4),
                eyeGlowColor = Color(0xFFFACC15)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFBEF264),
                midBody = Color(0xFF4D7C0F),
                shadow = Color(0xFF1A2E05),
                scaleAccent = Color(0xFFD9F99D),
                eyeGlowColor = Color(0xFFFDE047)
            )
        )
    ),

    RUBY_COPPER(
        persianName = "یاقوتی و مسی گرم",
        description = "مارهای خوش‌تراش و باریک با طیف یاقوت، مس و کهربای گرم",
        emoji = "♦️",
        thicknessScale = 0.86f,
        waveScale = 0.80f,
        wiggleScale = 0.25f,
        bodyAlpha = 0.88f,
        showScales = true,
        showHood = false,
        showFangs = false,
        customPalettes = listOf(
            SnakeColorPalette(
                highlight = Color(0xFFFDA4AF),
                midBody = Color(0xFFBE123C),
                shadow = Color(0xFF4C0519),
                scaleAccent = Color(0xFFFDE68A),
                eyeGlowColor = Color(0xFFFFD166)
            ),
            SnakeColorPalette(
                highlight = Color(0xFFFDBA74),
                midBody = Color(0xFFC2410C),
                shadow = Color(0xFF431407),
                scaleAccent = Color(0xFFFEF08A),
                eyeGlowColor = Color(0xFFFDE047)
            )
        )
    ),

    ROYAL_COBRA_DETAIL(
        persianName = "کبرای سلطنتی (با جزئیات)",
        description = "نسخه باریک‌شده با تاج کبرا و نیش‌های ظریف، هماهنگ با رنگ‌های تم صفحه",
        emoji = "🐉",
        thicknessScale = 0.94f,
        waveScale = 0.88f,
        wiggleScale = 0.35f,
        bodyAlpha = 0.92f,
        showScales = true,
        showHood = true,
        showFangs = true,
        customPalettes = null
    );

    companion object {
        fun fromName(name: String?): SnakeVisualTheme {
            return entries.find { it.name == name } ?: ROYAL_GOLD_SLIM
        }
    }
}

enum class DiceSurfacePattern {
    OBSIDIAN_LUXURY,
    WOOD_GRAIN,
    NEON_GRID,
    MARBLE_VEINS,
    BRUSHED_METAL,
    CRYSTAL_FROST
}

/**
 * 3D Dice Appearance & Material Themes (پوسته و جنس تاس سه‌بعدی).
 * Includes an automatic board-matching mode (`AUTO_MATCH_BOARD`) as well as
 * Wooden, Neon, Marble, Metallic, Obsidian Gold, and Crystal Ice styles.
 */
enum class DiceVisualTheme(
    val persianName: String,
    val englishLabel: String,
    val description: String,
    val emoji: String,
    val surfacePattern: DiceSurfacePattern,
    val faceTopColor: Color,
    val faceBottomColor: Color,
    val edgeOuterColor: Color,
    val edgeInnerColor: Color,
    val pipOneColor: Color,
    val pipSixColor: Color,
    val pipDefaultColor: Color,
    val pipRimColor: Color,
    val specularColor: Color,
    val ambientGlowColor: Color
) {
    AUTO_MATCH_BOARD(
        persianName = "هماهنگ با تم صفحه (خودکار)",
        englishLabel = "Auto-Match Theme",
        description = "تاس سه‌بعدی گرد با ترکیب لوکس مشکی، طلایی و نقره‌ای که به صورت خودکار با تم صفحه بازی هماهنگ می‌شود",
        emoji = "🎨",
        surfacePattern = DiceSurfacePattern.OBSIDIAN_LUXURY,
        faceTopColor = Color(0xFF232733),
        faceBottomColor = Color(0xFF0B0D13),
        edgeOuterColor = Color(0xFFFFD166),
        edgeInnerColor = Color(0xFFE2E8F0),
        pipOneColor = Color(0xFFFFD166),
        pipSixColor = Color(0xFFFFEA00),
        pipDefaultColor = Color(0xFFF1F5F9),
        pipRimColor = Color(0xFFFFD166),
        specularColor = Color(0xFFFFFFFF),
        ambientGlowColor = Color(0xFFFFD166)
    ),

    OBSIDIAN_GOLD(
        persianName = "مشکی، طلایی و نقره‌ای سلطنتی",
        englishLabel = "Black, Gold & Silver",
        description = "تاس سه‌بعدی با گوشه‌های گرد، بدنه مشکی براق، لبه‌های منحنی طلایی و نقره‌ای و نقاط برجسته (پیش‌فرض لوکس)",
        emoji = "👑",
        surfacePattern = DiceSurfacePattern.OBSIDIAN_LUXURY,
        faceTopColor = Color(0xFF232733),
        faceBottomColor = Color(0xFF0B0D13),
        edgeOuterColor = Color(0xFFFFD166),
        edgeInnerColor = Color(0xFFE2E8F0),
        pipOneColor = Color(0xFFFFD166),
        pipSixColor = Color(0xFFFFEA00),
        pipDefaultColor = Color(0xFFF1F5F9),
        pipRimColor = Color(0xFFFFD166),
        specularColor = Color(0xFFFFFFFF),
        ambientGlowColor = Color(0xFFFFB703)
    ),

    WOODEN_CARVED(
        persianName = "چوبی حکاکی‌شده",
        englishLabel = "Carved Wooden",
        description = "چوب گردو و ساج طبیعی با رگه‌های چوبی سه‌بعدی، قاب برنجی و خال‌های فیروزه‌ای و کهربایی (مناسب تم جنگل)",
        emoji = "🪵",
        surfacePattern = DiceSurfacePattern.WOOD_GRAIN,
        faceTopColor = Color(0xFF8B5A2B),
        faceBottomColor = Color(0xFF4A2C11),
        edgeOuterColor = Color(0xFFE6B87D),
        edgeInnerColor = Color(0xFF10B981),
        pipOneColor = Color(0xFF34D399),
        pipSixColor = Color(0xFFFDE047),
        pipDefaultColor = Color(0xFFFFFBEB),
        pipRimColor = Color(0xFFD97706),
        specularColor = Color(0xFFFDE68A),
        ambientGlowColor = Color(0xFF10B981)
    ),

    NEON_CYBER(
        persianName = "نئون درخشان",
        englishLabel = "Cyber Neon",
        description = "تاس سایبرپانک با خطوط نئونی فیروزه‌ای و سرخابی، هاله نورانی و خال‌های پلاسمایی درخشان",
        emoji = "⚡",
        surfacePattern = DiceSurfacePattern.NEON_GRID,
        faceTopColor = Color(0xFF131B38),
        faceBottomColor = Color(0xFF070B19),
        edgeOuterColor = Color(0xFF00F5D4),
        edgeInnerColor = Color(0xFFFF2A85),
        pipOneColor = Color(0xFFFF2A85),
        pipSixColor = Color(0xFF00F5D4),
        pipDefaultColor = Color(0xFF38BDF8),
        pipRimColor = Color(0xFFE0F2FE),
        specularColor = Color(0xFF67E8F9),
        ambientGlowColor = Color(0xFF00F5D4)
    ),

    PEARL_MARBLE(
        persianName = "سنگ مرمر سلطنتی",
        englishLabel = "Royal Marble",
        description = "مرمر سفید و صدفی صیقلی با رگه‌های طبیعی سنگ، قاب طلایی شامپاینی و خال‌های لاجوردی (مناسب تم ساحلی)",
        emoji = "🏛️",
        surfacePattern = DiceSurfacePattern.MARBLE_VEINS,
        faceTopColor = Color(0xFFF8FAFC),
        faceBottomColor = Color(0xFFCBD5E1),
        edgeOuterColor = Color(0xFFD97706),
        edgeInnerColor = Color(0xFF0284C7),
        pipOneColor = Color(0xFFE11D48),
        pipSixColor = Color(0xFF0284C7),
        pipDefaultColor = Color(0xFF1E293B),
        pipRimColor = Color(0xFFF59E0B),
        specularColor = Color(0xFFFFFFFF),
        ambientGlowColor = Color(0xFF38BDF8)
    ),

    METALLIC_CHROME(
        persianName = "فلزی براق (تیتانیوم و مس)",
        englishLabel = "Brushed Metallic",
        description = "آلیاژ فلزی برس‌خورده با انعکاس نور متالیک، پیچ‌های صنعتی در گوشه‌ها و خال‌های آتشین (مناسب تم کویر)",
        emoji = "⚙️",
        surfacePattern = DiceSurfacePattern.BRUSHED_METAL,
        faceTopColor = Color(0xFF64748B),
        faceBottomColor = Color(0xFF1E293B),
        edgeOuterColor = Color(0xFFE2E8F0),
        edgeInnerColor = Color(0xFFF97316),
        pipOneColor = Color(0xFFFF5722),
        pipSixColor = Color(0xFFFACC15),
        pipDefaultColor = Color(0xFFF8FAFC),
        pipRimColor = Color(0xFF94A3B8),
        specularColor = Color(0xFFFFFFFF),
        ambientGlowColor = Color(0xFFF59E0B)
    ),

    CRYSTAL_ICE(
        persianName = "کریستال یخی",
        englishLabel = "Arctic Crystal",
        description = "بلور یخ قطبی با تراش‌های الماسی، درخشش نقره‌ای و خال‌های آبی یخچالی (مناسب تم یخی و قطبی)",
        emoji = "❄️",
        surfacePattern = DiceSurfacePattern.CRYSTAL_FROST,
        faceTopColor = Color(0xFFBAE6FD),
        faceBottomColor = Color(0xFF1E3A8A),
        edgeOuterColor = Color(0xFFFFFFFF),
        edgeInnerColor = Color(0xFF60A5FA),
        pipOneColor = Color(0xFFC084FC),
        pipSixColor = Color(0xFFFFFFFF),
        pipDefaultColor = Color(0xFFE0F2FE),
        pipRimColor = Color(0xFF93C5FD),
        specularColor = Color(0xFFFFFFFF),
        ambientGlowColor = Color(0xFF60A5FA)
    );

    /**
     * Resolves the concrete 3D dice material when [AUTO_MATCH_BOARD] is active.
     */
    fun resolveForBoardTheme(boardTheme: BoardTheme): DiceVisualTheme {
        if (this != AUTO_MATCH_BOARD) return this
        return boardTheme.recommendedDiceTheme()
    }

    companion object {
        fun fromName(name: String?): DiceVisualTheme {
            return entries.find { it.name == name } ?: AUTO_MATCH_BOARD
        }
    }
}

/**
 * Returns the signature 3D dice material that naturally matches each [BoardTheme].
 */
fun BoardTheme.recommendedDiceTheme(): DiceVisualTheme {
    return when (this) {
        BoardTheme.BLACK_GOLD -> DiceVisualTheme.OBSIDIAN_GOLD
        BoardTheme.JUNGLE -> DiceVisualTheme.WOODEN_CARVED
        BoardTheme.OCEAN_BEACH -> DiceVisualTheme.PEARL_MARBLE
        BoardTheme.DESERT -> DiceVisualTheme.METALLIC_CHROME
        BoardTheme.ARCTIC_ICE -> DiceVisualTheme.CRYSTAL_ICE
    }
}

/**
 * Intensity levels for Environment Visual Effects (شدت افکت‌های محیطی).
 */
enum class EffectIntensity(
    val persianTitle: String,
    val englishTitle: String,
    val particleCountMultiplier: Float,
    val alphaMultiplier: Float,
    val speedMultiplier: Float
) {
    LOW(
        persianTitle = "کم",
        englishTitle = "Low",
        particleCountMultiplier = 0.55f,
        alphaMultiplier = 0.65f,
        speedMultiplier = 0.80f
    ),
    MEDIUM(
        persianTitle = "متوسط",
        englishTitle = "Medium",
        particleCountMultiplier = 1.0f,
        alphaMultiplier = 0.90f,
        speedMultiplier = 1.0f
    ),
    HIGH(
        persianTitle = "زیاد",
        englishTitle = "High",
        particleCountMultiplier = 1.50f,
        alphaMultiplier = 1.15f,
        speedMultiplier = 1.25f
    );

    companion object {
        fun fromName(name: String?): EffectIntensity {
            return entries.find { it.name == name } ?: MEDIUM
        }
    }
}

/**
 * 12 Selectable Atmospheric Environment Visual Effects (جلوه‌های محیطی قابل انتخاب).
 * Rendered with non-intrusive alpha blending so board tiles, numbers, snakes, ladders, and tokens stay 100% legible.
 */
enum class EnvironmentEffectType(
    val englishName: String,
    val persianName: String,
    val description: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val baseParticleCount: Int
) {
    GOLDEN_DUST(
        englishName = "Golden Dust",
        persianName = "ذرات طلایی شناور",
        description = "ذرات طلایی ۲۴ عیار شناور و درخشان در فضای تالار سلطنتی",
        emoji = "✨",
        primaryColor = Color(0xFFFFD166),
        secondaryColor = Color(0xFFF59E0B),
        glowColor = Color(0xFFFFF3B0),
        baseParticleCount = 34
    ),
    MAGIC_PARTICLES(
        englishName = "Magic Particles",
        persianName = "ذرات جادویی و نورانی",
        description = "گوی‌های جادویی بنفش، فیروزه‌ای و طلایی با رد نورانی لطیف",
        emoji = "🔮",
        primaryColor = Color(0xFFC084FC),
        secondaryColor = Color(0xFF38BDF8),
        glowColor = Color(0xFFF472B6),
        baseParticleCount = 32
    ),
    FIRE_EMBERS(
        englishName = "Fire & Embers",
        persianName = "شعله‌های ظریف و ذرات آتش",
        description = "اخگرهای گرم و ذرات آتشین صعودکننده از پایین صفحه با تلالو کهربایی",
        emoji = "🔥",
        primaryColor = Color(0xFFFF6B35),
        secondaryColor = Color(0xFFFACC15),
        glowColor = Color(0xFFEF4444),
        baseParticleCount = 36
    ),
    SNOWFALL(
        englishName = "Snowfall",
        persianName = "بارش آرام برف",
        description = "دانه‌های بلورین برف با حرکت موجی آرام و درخشش نقره‌ای",
        emoji = "❄️",
        primaryColor = Color(0xFFE0F2FE),
        secondaryColor = Color(0xFF93C5FD),
        glowColor = Color(0xFFFFFFFF),
        baseParticleCount = 38
    ),
    RAIN(
        englishName = "Rain",
        persianName = "باران و قطرات آب",
        description = "بارش لطیف قطرات باران نقره‌ای و آبی با حلقه‌های ظریف شبنم",
        emoji = "🌧️",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFFBAE6FD),
        glowColor = Color(0xFF0284C7),
        baseParticleCount = 42
    ),
    GALAXY(
        englishName = "Galaxy",
        persianName = "ستاره‌ها، سحابی و ذرات کیهانی",
        description = "ستارگان چشمک‌زن، ابرهای سحابی بنفش-طلایی و غبار کهکشانی",
        emoji = "🌌",
        primaryColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFFFFD166),
        glowColor = Color(0xFF6366F1),
        baseParticleCount = 36
    ),
    AURORA(
        englishName = "Aurora",
        persianName = "شفق قطبی و نورهای متحرک",
        description = "پرده موج‌دار شفق قطبی با طیف زمردی، فیروزه‌ای و بنفش درخشان",
        emoji = "🌈",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF06B6D4),
        glowColor = Color(0xFFA855F7),
        baseParticleCount = 24
    ),
    MYSTIC_FOG(
        englishName = "Mystic Fog",
        persianName = "مه جادویی و مرموز",
        description = "لایه‌های نرم مه اسرارآمیز طلایی-سرمه‌ای در حاشیه صفحه بدون پوشاندن خانه‌ها",
        emoji = "🌫️",
        primaryColor = Color(0xFF94A3B8),
        secondaryColor = Color(0xFFFFD166),
        glowColor = Color(0xFF64748B),
        baseParticleCount = 20
    ),
    LIGHTNING(
        englishName = "Lightning",
        persianName = "رعدوبرق و نورهای لحظه‌ای",
        description = "صاعقه‌های ظریف طلایی-آبی و جرقه‌های الکتریکی لحظه‌ای در حاشیه میدان",
        emoji = "⚡",
        primaryColor = Color(0xFF67E8F9),
        secondaryColor = Color(0xFFFDE047),
        glowColor = Color(0xFF38BDF8),
        baseParticleCount = 22
    ),
    ROYAL_GLOW(
        englishName = "Royal Glow",
        persianName = "نورپردازی سلطنتی طلایی",
        description = "پرتوهای تاج سلطنتی و هاله‌های مخملی طلایی در اطراف قاب صفحه بازی",
        emoji = "👑",
        primaryColor = Color(0xFFFFD166),
        secondaryColor = Color(0xFFD97706),
        glowColor = Color(0xFFFFFBEB),
        baseParticleCount = 26
    ),
    ANCIENT_RUNES(
        englishName = "Ancient Runes",
        persianName = "نمادها و نوشته‌های باستانی نورانی",
        description = "حروف و طلسم‌های باستانی نورانی که به آرامی در فضا ظاهر و محو می‌شوند",
        emoji = "📜",
        primaryColor = Color(0xFFFBBF24),
        secondaryColor = Color(0xFF34D399),
        glowColor = Color(0xFFFDE68A),
        baseParticleCount = 24
    ),
    SAKURA(
        englishName = "Sakura",
        persianName = "شکوفه‌های متحرک و آرام",
        description = "گلبرگ‌های لطیف شکوفه گیلاس با چرخش طبیعی در نسیم ملایم",
        emoji = "🌸",
        primaryColor = Color(0xFFF472B6),
        secondaryColor = Color(0xFFFBCFE8),
        glowColor = Color(0xFFFB7185),
        baseParticleCount = 30
    );

    companion object {
        fun fromName(name: String?): EnvironmentEffectType {
            return entries.find { it.name == name } ?: GOLDEN_DUST
        }
    }
}

/**
 * 9 Selectable Musical Genres + Auto-Theme option (ژانرهای موسیقی و تم‌های صوتی).
 */
enum class MusicGenre(
    val englishName: String,
    val persianName: String,
    val description: String,
    val instrumentsLabel: String,
    val tempoBpm: Int,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    ROYAL(
        englishName = "Royal",
        persianName = "سلطنتی (Royal)",
        description = "موسیقی مجلل و باشکوه با سازهای کلاسیک، چنگ طلایی و ویولنسل مخملی",
        instrumentsLabel = "چنگ طلایی • ویولنسل کلاسیک • هارپسیکورد درباری",
        tempoBpm = 96,
        emoji = "👑",
        primaryColor = Color(0xFFFFD166),
        secondaryColor = Color(0xFFD97706)
    ),
    HEROIC(
        englishName = "Heroic",
        persianName = "قهرمانی (Heroic)",
        description = "موسیقی حماسی با حس قدرت و شجاعت • ارکستر سینمایی، سازهای بادی برنجی و کوبه‌ای قدرتمند",
        instrumentsLabel = "هورن سینمایی • طبل نبرد • زهی حماسی",
        tempoBpm = 124,
        emoji = "⚔️",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFEF4444)
    ),
    VICTORY(
        englishName = "Victory",
        persianName = "پیروزی (Victory)",
        description = "موسیقی باشکوه و پرانرژی • حس موفقیت و برنده شدن • مناسب لحظه رسیدن به خانه آخر",
        instrumentsLabel = "ترومپت پیروزی • ارکستر جشن • زنگ‌های طلایی",
        tempoBpm = 132,
        emoji = "🏆",
        primaryColor = Color(0xFFFDE047),
        secondaryColor = Color(0xFF10B981)
    ),
    MYTHOLOGICAL(
        englishName = "Mythological",
        persianName = "اسطوره‌ای (Mythological)",
        description = "موسیقی رازآلود و باستانی • ترکیب سازهای سنتی، لیر یونانی، عود و ارکستر اساطیری",
        instrumentsLabel = "لیر باستانی • عود و نی اسرارآمیز • کُر اساطیری",
        tempoBpm = 92,
        emoji = "🏛️",
        primaryColor = Color(0xFFEAB308),
        secondaryColor = Color(0xFF14B8A6)
    ),
    FANTASY(
        englishName = "Fantasy",
        persianName = "فانتزی (Fantasy)",
        description = "موسیقی جادویی و خیال‌انگیز • ملودی‌های مرموز سلستا، فلوت کریستالی و پدهای فضایی",
        instrumentsLabel = "سلستا جادویی • فلوت نقره‌ای • چنگ افسانه‌ای",
        tempoBpm = 104,
        emoji = "🧚",
        primaryColor = Color(0xFFC084FC),
        secondaryColor = Color(0xFF38BDF8)
    ),
    DARK_EPIC(
        englishName = "Dark Epic",
        persianName = "حماسی تاریک (Dark Epic)",
        description = "موسیقی عمیق، سنگین و سینمایی با کنترباس تیره، طبل‌های عمیق و همخوانی دراماتیک",
        instrumentsLabel = "کنترباس سنگین • کوبه‌ای تالار سیاه • برنجی عمیق",
        tempoBpm = 88,
        emoji = "🌑",
        primaryColor = Color(0xFF9333EA),
        secondaryColor = Color(0xFFE11D48)
    ),
    ADVENTURE(
        englishName = "Adventure",
        persianName = "ماجراجویی (Adventure)",
        description = "موسیقی پرتحرک و هیجان‌انگیز با ریتم تند، ماریمبا، زهی‌های چابک و پرکاشن سفر",
        instrumentsLabel = "زهی پرتحرک • ماریمبا و فلوت • پرکاشن ماجراجویی",
        tempoBpm = 128,
        emoji = "🧭",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFF59E0B)
    ),
    AMBIENT(
        englishName = "Ambient",
        persianName = "آرامش‌بخش (Ambient)",
        description = "موسیقی پس‌زمینه آرام، لطیف و بدون ایجاد خستگی برای تمرکز طولانی در بازی",
        instrumentsLabel = "پد ابریشمی • پیانو لطیف • زنگوله‌های بادی",
        tempoBpm = 72,
        emoji = "🍃",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF6EE7B7)
    ),
    CYBER(
        englishName = "Cyber",
        persianName = "آینده‌نگر (Cyber)",
        description = "موسیقی الکترونیک و علمی‌تخیلی با سینث‌سایزرهای نئونی، آرپژ فضایی و بیس پالسی",
        instrumentsLabel = "سینث نئونی • آرپژ سایبرپانک • ساب‌بیس پالسی",
        tempoBpm = 122,
        emoji = "🚀",
        primaryColor = Color(0xFF00F5D4),
        secondaryColor = Color(0xFFFF2A85)
    ),
    AUTO_BOARD_THEME(
        englishName = "Auto Match Board",
        persianName = "هماهنگ با تم صفحه (خودکار)",
        description = "تغییر خودکار موسیقی بر اساس تم انتخاب‌شده صفحه بازی (مشکی طلایی، جنگل، ساحل، کویر، قطبی)",
        instrumentsLabel = "سازبندی پویا متناسب با تم بصری صفحه",
        tempoBpm = 100,
        emoji = "🎨",
        primaryColor = Color(0xFFFFD166),
        secondaryColor = Color(0xFF10B981)
    );

    companion object {
        val selectableGenres: List<MusicGenre>
            get() = listOf(
                HEROIC,
                VICTORY,
                MYTHOLOGICAL,
                FANTASY,
                DARK_EPIC,
                ROYAL,
                ADVENTURE,
                AMBIENT,
                CYBER
            )

        fun fromName(name: String?): MusicGenre {
            return entries.find { it.name == name } ?: ROYAL
        }
    }
}

/**
 * Animation & Rendering Quality Levels (کیفیت انیمیشن و رندر).
 * Controls GPU/Canvas particle density, trail richness, 3D dice bevel subdivisions,
 * and lighting glow layers for optimal 60/90/120 FPS performance.
 */
enum class AnimationQuality(
    val englishTitle: String,
    val persianTitle: String,
    val description: String,
    val emoji: String,
    val particleMultiplier: Float,
    val diceBevelSteps: Int,
    val enableRichTokenTrail: Boolean,
    val enableMultiLayerGlow: Boolean
) {
    PERFORMANCE(
        englishTitle = "Performance",
        persianTitle = "روان و سریع (Performance)",
        description = "بیشترین نرخ فریم (60/120 FPS) و کمترین افکت جانبی برای اجرای فوق‌سریع روی تمام دستگاه‌ها",
        emoji = "⚡",
        particleMultiplier = 0.45f,
        diceBevelSteps = 3,
        enableRichTokenTrail = false,
        enableMultiLayerGlow = false
    ),
    BALANCED(
        englishTitle = "Balanced",
        persianTitle = "متعادل (Balanced)",
        description = "تعادل ایده‌آل بین کیفیت بصری بالا، ذرات محیطی و عملکرد روان و پایدار",
        emoji = "⚖️",
        particleMultiplier = 0.85f,
        diceBevelSteps = 5,
        enableRichTokenTrail = true,
        enableMultiLayerGlow = true
    ),
    CINEMATIC(
        englishTitle = "Cinematic",
        persianTitle = "سینمایی (Cinematic)",
        description = "بالاترین کیفیت بصری، سایه‌زنی کامل سه‌بعدی تاس، رد نورانی مهره و ذرات فرود کامل",
        emoji = "🎬",
        particleMultiplier = 1.25f,
        diceBevelSteps = 6,
        enableRichTokenTrail = true,
        enableMultiLayerGlow = true
    );

    companion object {
        fun fromName(name: String?): AnimationQuality {
            return entries.find { it.name == name } ?: CINEMATIC
        }
    }
}

/**
 * Animation Speed Presets (سرعت انیمیشن پرتاب تاس و حرکت مهره‌ها).
 * Controls the duration of 3D dice throws, step-by-step token hops, ladder climbs, and snake slides.
 */
enum class AnimationSpeed(
    val englishTitle: String,
    val persianTitle: String,
    val description: String,
    val emoji: String,
    val durationMultiplier: Float,
    val stepDurationMs: Long,
    val specialPathDurationMs: Long,
    val diceRollDurationMs: Long
) {
    NORMAL(
        englishTitle = "Normal",
        persianTitle = "سریع و استاندارد (Normal)",
        description = "حرکت سریع، چابک و بدون مکث مهره‌ها و تاس برای بازی‌های پرسرعت",
        emoji = "🚀",
        durationMultiplier = 0.76f,
        stepDurationMs = 175L,
        specialPathDurationMs = 760L,
        diceRollDurationMs = 580L
    ),
    SMOOTH(
        englishTitle = "Smooth",
        persianTitle = "نرم و طبیعی (Smooth)",
        description = "حرکت نرم، متعادل و فیزیکی با پرش طبیعی روی خانه‌ها و چرخش سه‌بعدی تاس",
        emoji = "🌊",
        durationMultiplier = 1.0f,
        stepDurationMs = 225L,
        specialPathDurationMs = 960L,
        diceRollDurationMs = 740L
    ),
    CINEMATIC(
        englishTitle = "Cinematic",
        persianTitle = "سینمایی و با جزئیات (Cinematic)",
        description = "نمایش کامل جزئیات چرخش سه‌بعدی تاس، فرود نرم، پرش پله‌ای و سر خوردن سینمایی روی مار",
        emoji = "✨",
        durationMultiplier = 1.24f,
        stepDurationMs = 275L,
        specialPathDurationMs = 1180L,
        diceRollDurationMs = 900L
    );

    companion object {
        fun fromName(name: String?): AnimationSpeed {
            return entries.find { it.name == name } ?: SMOOTH
        }
    }
}
