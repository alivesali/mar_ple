package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * 8 Categories of Pawn Skins as requested by the user.
 */
enum class PawnSkinCategory(
    val persianTitle: String,
    val emoji: String,
    val descriptionPersian: String
) {
    CLASSIC(
        persianTitle = "کلاسیک",
        emoji = "⚪",
        descriptionPersian = "مهره‌های مینیمال، شیشه‌ای، براق و رنگ‌های استاندارد بازی‌های رومیزی"
    ),
    LUXURY(
        persianTitle = "لوکس و جواهرات",
        emoji = "💎",
        descriptionPersian = "طلا، نقره استرلینگ، الماس تراش‌خورده، کریستال و سنگ‌های قیمتی"
    ),
    FANTASY(
        persianTitle = "فانتزی و جادو",
        emoji = "🔮",
        descriptionPersian = "گوی‌های جادویی، شعله متحرک، انرژی نورانی، کریستال معلق و هسته پلاسما"
    ),
    NATURE(
        persianTitle = "طبیعت و جنگل",
        emoji = "🌿",
        descriptionPersian = "برگ شبنم‌دار، شکوفه نیلوفر، سنگ جنگلی، قارچ فانتزی و گوی سبز طبیعت"
    ),
    ELEMENTAL(
        persianTitle = "عناصر چهارگانه",
        emoji = "🔥",
        descriptionPersian = "شعله آتش، بلور یخ، قطره آب، گردباد باد و صاعقه برق"
    ),
    ROYAL(
        persianTitle = "سلطنتی و امپراتوری",
        emoji = "👑",
        descriptionPersian = "تاج‌های مینیاتوری، مهره سلطنتی، گوی جواهرنشان و مجسمه فتح"
    ),
    HERO_MINIATURES(
        persianTitle = "مینیاتور قهرمانان",
        emoji = "🤺",
        descriptionPersian = "نسخه کوچک‌شده قهرمان، مینیاتور سه‌بعدی، چیبی، جنگجوی کوچک و حیوان همراه"
    ),
    SPECIAL(
        persianTitle = "ویژه و متحرک",
        emoji = "🌌",
        descriptionPersian = "پوسته‌های کمیاب، فصلی، مناسبتی، متحرک و اژدهای زرین"
    )
}

/**
 * Trail Particle FX type generated when a pawn moves across tiles.
 */
enum class PawnTrailFxType(val persianLabel: String, val particleEmoji: String) {
    GOLDEN_DUST("غبار طلایی", "✨"),
    FIRE_EMBERS("جرقه‌های آتش", "🔥"),
    ICE_CRYSTALS("بلورهای یخ", "❄️"),
    LEAF_SWIRL("گرداب برگ", "🍃"),
    LIGHTNING_ARCS("قوس‌های الکتریکی", "⚡"),
    MAGIC_RUNES("رون‌های جادویی", "🔮"),
    BUBBLE_STREAM("حباب‌های آب", "🫧"),
    NEON_GLOW("هاله نئونی", "💫"),
    STAR_SPARKLES("ستاره‌های درخشان", "⭐")
}

/**
 * Visual Material Type determining lighting reflections and surface rendering.
 */
enum class PawnMaterialType {
    MATTE_COLOR,
    TRANSLUCENT_GLASS,
    GLOSSY_METALLIC,
    PURE_GOLD,
    PURE_SILVER,
    PRISMATIC_DIAMOND,
    AMETHYST_CRYSTAL,
    PRECIOUS_GEMSTONE,
    LIVING_FIRE,
    ORGANIC_NATURE,
    PLASMA_ENERGY,
    CHIBI_FIGURE,
    HERO_MINIATURE
}

/**
 * Complete Specification of an Equipable Pawn Skin.
 */
data class PawnSkin(
    val id: String,
    val namePersian: String,
    val nameEnglish: String,
    val category: PawnSkinCategory,
    val materialType: PawnMaterialType,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val trailType: PawnTrailFxType,
    val ambientParticleEmoji: String,
    val descriptionPersian: String,
    val rarityTitle: String = "معمولی",
    val isAnimated: Boolean = true,
    val isDefaultUnlocked: Boolean = true,
    val isLocked: Boolean = false,
    val unlockConditionPersian: String = ""
) {
    val persianName: String get() = namePersian
    val englishName: String get() = nameEnglish
}

/**
 * Comprehensive Catalog of original Pawn Skins across all categories.
 */
object PawnSkinCatalog {

    val allSkins: List<PawnSkin> = listOf(
        // ==========================================
        // 1. CLASSIC CATEGORY (4 skins)
        // ==========================================
        PawnSkin(
            id = "classic_standard",
            namePersian = "مهره رنگی ساده (Classic)",
            nameEnglish = "Classic Pawn",
            category = PawnSkinCategory.CLASSIC,
            materialType = PawnMaterialType.MATTE_COLOR,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFB45309),
            glowColor = Color(0xFFFDE68A),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "⚪",
            descriptionPersian = "مهره استاندارد چوبی با کلاهک گرد و پایه طلایی کلاسیک"
        ),
        PawnSkin(
            id = "classic_translucent",
            namePersian = "مهره شفاف (Glass)",
            nameEnglish = "Frosted Glass",
            category = PawnSkinCategory.CLASSIC,
            materialType = PawnMaterialType.TRANSLUCENT_GLASS,
            primaryColor = Color(0xFFE2E8F0),
            secondaryColor = Color(0xFF94A3B8),
            glowColor = Color(0xFFF8FAFC),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "✨",
            descriptionPersian = "مهره شیشه‌ای صیقلی با انکسار نور و بازتاب مات سطح"
        ),
        PawnSkin(
            id = "classic_glossy",
            namePersian = "مهره براق (Glossy Mirror)",
            nameEnglish = "Glossy Sphere",
            category = PawnSkinCategory.CLASSIC,
            materialType = PawnMaterialType.GLOSSY_METALLIC,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF0369A1),
            glowColor = Color(0xFFE0F2FE),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "💫",
            descriptionPersian = "سطح بسیار صیقلی با نقطه بازتاب براق و متریال درخشان"
        ),
        PawnSkin(
            id = "classic_minimal",
            namePersian = "مهره مینیمال (Minimalist Puck)",
            nameEnglish = "Minimalist Puck",
            category = PawnSkinCategory.CLASSIC,
            materialType = PawnMaterialType.MATTE_COLOR,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF047857),
            glowColor = Color(0xFFA7F3D0),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🔘",
            descriptionPersian = "طراحی مدرن و تخت هندسی با حاشیه باریک طلایی"
        ),

        // ==========================================
        // 2. LUXURY CATEGORY (5 skins)
        // ==========================================
        PawnSkin(
            id = "luxury_gold",
            namePersian = "طلای ناب ۲۴ عیار",
            nameEnglish = "Solid Gold",
            category = PawnSkinCategory.LUXURY,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFB45309),
            glowColor = Color(0xFFFFFBEB),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🪙",
            descriptionPersian = "مهره ساخته‌شده از طلای گداخته با درخشش بازتابنده خورشیدی",
            rarityTitle = "کمیاب",
            isLocked = false,
            unlockConditionPersian = "پیروزی در حداقل یک دست بازی"
        ),
        PawnSkin(
            id = "luxury_silver",
            namePersian = "نقره استرلینگ شاهانه",
            nameEnglish = "Sterling Silver",
            category = PawnSkinCategory.LUXURY,
            materialType = PawnMaterialType.PURE_SILVER,
            primaryColor = Color(0xFFE0E7FF),
            secondaryColor = Color(0xFF64748B),
            glowColor = Color(0xFFF1F5F9),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🥈",
            descriptionPersian = "پوشش نقره خالص با بازتاب سرد و حاشیه‌های برلیان"
        ),
        PawnSkin(
            id = "luxury_diamond",
            namePersian = "الماس تراش‌خورده کوه نور",
            nameEnglish = "Prismatic Diamond",
            category = PawnSkinCategory.LUXURY,
            materialType = PawnMaterialType.PRISMATIC_DIAMOND,
            primaryColor = Color(0xFFBAE6FD),
            secondaryColor = Color(0xFF38BDF8),
            glowColor = Color(0xFFF0F9FF),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "💎",
            descriptionPersian = "وجوه الماس هندسی با شکست نور رنگین‌کمانی در هر چرخش",
            rarityTitle = "حماسی",
            isLocked = false,
            unlockConditionPersian = "رسیدن به خانه ۱۰۰ بدون افتادن در مار"
        ),
        PawnSkin(
            id = "luxury_crystal",
            namePersian = "کریستال آمیتیست سلطنتی",
            nameEnglish = "Royal Amethyst",
            category = PawnSkinCategory.LUXURY,
            materialType = PawnMaterialType.AMETHYST_CRYSTAL,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF6B21A8),
            glowColor = Color(0xFFF3E8FF),
            trailType = PawnTrailFxType.MAGIC_RUNES,
            ambientParticleEmoji = "🔮",
            descriptionPersian = "بلور یاقوتی بنفش با نور درونی تپنده و لبه‌های صیقلی"
        ),
        PawnSkin(
            id = "luxury_gemstones",
            namePersian = "سنگ‌های قیمتی زمرد و یاقوت",
            nameEnglish = "Precious Gemstones",
            category = PawnSkinCategory.LUXURY,
            materialType = PawnMaterialType.PRECIOUS_GEMSTONE,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFFEF4444),
            glowColor = Color(0xFFA7F3D0),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "✨",
            descriptionPersian = "مجموعه سنگ‌های قیمتی تراش‌خورده زمرد سلطنتی و یاقوت سرخ با انکسار چندوجهی",
            rarityTitle = "افسانه‌ای",
            isLocked = true,
            unlockConditionPersian = "برد در مود سخت یا بازگشایی با دکمه تست"
        ),

        // ==========================================
        // 3. FANTASY CATEGORY (5 skins)
        // ==========================================
        PawnSkin(
            id = "fantasy_magic_orb",
            namePersian = "گوی جادویی اختری",
            nameEnglish = "Arcane Orb",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF818CF8),
            secondaryColor = Color(0xFF3730A3),
            glowColor = Color(0xFFE0E7FF),
            trailType = PawnTrailFxType.MAGIC_RUNES,
            ambientParticleEmoji = "🪄",
            descriptionPersian = "گوی اثیری معلق در فضا که با انرژی رون‌های جادویی می‌چرخد"
        ),
        PawnSkin(
            id = "fantasy_living_flame",
            namePersian = "شعله متحرک اثیری",
            nameEnglish = "Living Flame",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.LIVING_FIRE,
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFF9A3412),
            glowColor = Color(0xFFFFEDD5),
            trailType = PawnTrailFxType.FIRE_EMBERS,
            ambientParticleEmoji = "🔥",
            descriptionPersian = "آتش زنده کیمیاگری که زبانه می‌کشد و دوده طلایی بر جای می‌گذارد"
        ),
        PawnSkin(
            id = "fantasy_luminous_energy",
            namePersian = "انرژی نورانی تپنده",
            nameEnglish = "Luminous Energy",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFFFBBF24),
            secondaryColor = Color(0xFFD97706),
            glowColor = Color(0xFFFEF3C7),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "☀️",
            descriptionPersian = "هاله نورانی درخشان ساطع‌کننده پرتوهای خورشیدی معلق در هوا",
            rarityTitle = "کمیاب"
        ),
        PawnSkin(
            id = "fantasy_floating_crystal",
            namePersian = "کریستال شناور معلق",
            nameEnglish = "Floating Crystal",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.AMETHYST_CRYSTAL,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF0E7490),
            glowColor = Color(0xFFCFFAFE),
            trailType = PawnTrailFxType.MAGIC_RUNES,
            ambientParticleEmoji = "💎",
            descriptionPersian = "بلور کوارتز فیروزه‌ای معلق با ۳ تکه شناور جانبی ضدجاذبه"
        ),
        PawnSkin(
            id = "fantasy_plasma_core",
            namePersian = "هسته انرژی پلاسما",
            nameEnglish = "Plasma Reactor Core",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF581C87),
            glowColor = Color(0xFFF3E8FF),
            trailType = PawnTrailFxType.LIGHTNING_ARCS,
            ambientParticleEmoji = "⚛️",
            descriptionPersian = "هسته متراکم پلاسما در محفظه هولوگرافیک مهارکننده انرژی کوانتومی",
            rarityTitle = "حماسی"
        ),

        // ==========================================
        // 4. NATURE CATEGORY (5 skins)
        // ==========================================
        PawnSkin(
            id = "nature_leaf",
            namePersian = "برگ شبنم‌دار جنگلی",
            nameEnglish = "Dewdrop Leaf",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFF84CC16),
            secondaryColor = Color(0xFF4D7C0F),
            glowColor = Color(0xFFECFCCB),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🍃",
            descriptionPersian = "برگ جوان بلوط با قطره درخشان آب و رگبرگ‌های ارگانیک زنده"
        ),
        PawnSkin(
            id = "nature_magic_flower",
            namePersian = "گل جادویی نیلوفر آبی",
            nameEnglish = "Magic Lotus Blossom",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFF9D174D),
            glowColor = Color(0xFFFCE7F3),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🌸",
            descriptionPersian = "شکوفه جادویی با گلبرگ‌های لایه‌لایه که عطر بهشت ساطع می‌کند"
        ),
        PawnSkin(
            id = "nature_forest_stone",
            namePersian = "سنگ جنگلی خزه‌دار",
            nameEnglish = "Moss Forest Stone",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.MATTE_COLOR,
            primaryColor = Color(0xFF65A30D),
            secondaryColor = Color(0xFF3F6212),
            glowColor = Color(0xFFD9F99D),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🪨",
            descriptionPersian = "سنگ رودخانه‌ای پوشیده از خزه نرم و گلسنگ‌های جنگل‌های کهن"
        ),
        PawnSkin(
            id = "nature_fantasy_shroom",
            namePersian = "قارچ فانتزی شب‌تاب",
            nameEnglish = "Bioluminescent Shroom",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFF14B8A6),
            secondaryColor = Color(0xFF0F766E),
            glowColor = Color(0xFFCCFBF1),
            trailType = PawnTrailFxType.MAGIC_RUNES,
            ambientParticleEmoji = "🍄",
            descriptionPersian = "کلاهک خال‌دار بنفش-فیروزه‌ای با نور درخشنده بیولومینسنت شبانه"
        ),
        PawnSkin(
            id = "nature_green_orb",
            namePersian = "گوی سبز طبیعت و یشم",
            nameEnglish = "Emerald Gaia Orb",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF065F46),
            glowColor = Color(0xFFA7F3D0),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🌿",
            descriptionPersian = "گوی درخشان زمردین طبیعت با پیچک‌های درهم‌تنیده و نیروی حیات زمین",
            rarityTitle = "کمیاب"
        ),

        // ==========================================
        // 5. ELEMENTAL CATEGORY (5 skins)
        // ==========================================
        PawnSkin(
            id = "elemental_fire",
            namePersian = "آتش دوزخی (Fire)",
            nameEnglish = "Infernal Flame",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.LIVING_FIRE,
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFF991B1B),
            glowColor = Color(0xFFFEE2E2),
            trailType = PawnTrailFxType.FIRE_EMBERS,
            ambientParticleEmoji = "🔥",
            descriptionPersian = "آتشفشان مذاب مینیاتوری با پرتاب اخگرهای سرخ و نارنجی"
        ),
        PawnSkin(
            id = "elemental_ice",
            namePersian = "یخ قطبی ابدی (Ice)",
            nameEnglish = "Glacial Core",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.TRANSLUCENT_GLASS,
            primaryColor = Color(0xFF60A5FA),
            secondaryColor = Color(0xFF1E40AF),
            glowColor = Color(0xFFDBEAFE),
            trailType = PawnTrailFxType.ICE_CRYSTALS,
            ambientParticleEmoji = "❄️",
            descriptionPersian = "قندیل شفاف یخ با دانه‌های برف چرخان و سرمای مه‌آلود"
        ),
        PawnSkin(
            id = "elemental_water",
            namePersian = "آب زلال اقیانوس (Water)",
            nameEnglish = "Ocean Drop",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.TRANSLUCENT_GLASS,
            primaryColor = Color(0xFF0EA5E9),
            secondaryColor = Color(0xFF0369A1),
            glowColor = Color(0xFFE0F2FE),
            trailType = PawnTrailFxType.BUBBLE_STREAM,
            ambientParticleEmoji = "💧",
            descriptionPersian = "قطره زلال موج دریا با حباب‌های ریز شناور و امواج آرام"
        ),
        PawnSkin(
            id = "elemental_wind",
            namePersian = "باد فیروزه‌ای (Wind)",
            nameEnglish = "Zephyr Vortex",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF2DD4BF),
            secondaryColor = Color(0xFF115E59),
            glowColor = Color(0xFFCCFBF1),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "🌪️",
            descriptionPersian = "مارپیچ بادهای فیروزه‌ای که با چرخش سریع حرکت را روان می‌سازد"
        ),
        PawnSkin(
            id = "elemental_lightning",
            namePersian = "برق صاعقه (Lightning)",
            nameEnglish = "Volt Spark",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFFEAB308),
            secondaryColor = Color(0xFF854D0E),
            glowColor = Color(0xFFFEF08A),
            trailType = PawnTrailFxType.LIGHTNING_ARCS,
            ambientParticleEmoji = "⚡",
            descriptionPersian = "جرقه الکتریسیته متحرک با قوس‌های نوری و ترق‌ترق صوتی"
        ),

        // ==========================================
        // 6. ROYAL CATEGORY (4 skins)
        // ==========================================
        PawnSkin(
            id = "royal_mini_crown",
            namePersian = "تاج کوچک امپراتوری",
            nameEnglish = "Imperial Crown",
            category = PawnSkinCategory.ROYAL,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFB45309),
            glowColor = Color(0xFFFEF9C3),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "👑",
            descriptionPersian = "تاج طلایی پنج‌پره نگین‌نشان که با افتخار روی صفحه بازی گام برمی‌دارد"
        ),
        PawnSkin(
            id = "royal_sovereign_pawn",
            namePersian = "مهره سلطنتی شاهین",
            nameEnglish = "Sovereign Royal Pawn",
            category = PawnSkinCategory.ROYAL,
            materialType = PawnMaterialType.GLOSSY_METALLIC,
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF312E81),
            glowColor = Color(0xFFE0E7FF),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "♟️",
            descriptionPersian = "مهره شطرنجی سلطنتی با تاج شاهانه، طوق زربفت و نوار فیروزه‌ای شاهزادگان",
            rarityTitle = "کمیاب"
        ),
        PawnSkin(
            id = "royal_jeweled_orb",
            namePersian = "گوی جواهرنشان شاهی",
            nameEnglish = "Jeweled Globus",
            category = PawnSkinCategory.ROYAL,
            materialType = PawnMaterialType.GLOSSY_METALLIC,
            primaryColor = Color(0xFFDC2626),
            secondaryColor = Color(0xFF7F1D1D),
            glowColor = Color(0xFFFCA5A5),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🔮",
            descriptionPersian = "گوی سلطنتی مزین به یاقوت سرخ و روبان طلایی امپراتور"
        ),
        PawnSkin(
            id = "royal_golden_statue",
            namePersian = "مجسمه طلایی بالدار فتح",
            nameEnglish = "Winged Victory Statue",
            category = PawnSkinCategory.ROYAL,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFF78350F),
            glowColor = Color(0xFFFFFBEB),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🏆",
            descriptionPersian = "پیکره بالدار طلایی ایستاده روی پایه مرمرین سیاه",
            rarityTitle = "افسانه‌ای",
            isLocked = true,
            unlockConditionPersian = "کسب پیروزی با حداقل ۲ نردبان فتح‌شده"
        ),

        // ==========================================
        // 7. HERO MINIATURES CATEGORY (5 skins)
        // ==========================================
        PawnSkin(
            id = "mini_hero_replica",
            namePersian = "نسخه کوچک‌شده خود قهرمان‌ها",
            nameEnglish = "Hero Replica Miniature",
            category = PawnSkinCategory.HERO_MINIATURES,
            materialType = PawnMaterialType.HERO_MINIATURE,
            primaryColor = Color(0xFFF43F5E),
            secondaryColor = Color(0xFF881337),
            glowColor = Color(0xFFFFE4E6),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🎖️",
            descriptionPersian = "پیکره کوچک اختصاصی هماهنگ با نشان، زره و سلاح قهرمان انتخابی",
            rarityTitle = "حماسی"
        ),
        PawnSkin(
            id = "mini_hero_3d_figure",
            namePersian = "مینیاتور سه‌بعدی قهرمان",
            nameEnglish = "3D Hero Figurine",
            category = PawnSkinCategory.HERO_MINIATURES,
            materialType = PawnMaterialType.HERO_MINIATURE,
            primaryColor = Color(0xFF3B82F6),
            secondaryColor = Color(0xFF1D4ED8),
            glowColor = Color(0xFFDBEAFE),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🗿",
            descriptionPersian = "تندیس سه‌بعدی متحرک تراش‌خورده با استایل رزمی قهرمانان افسانه‌ای",
            rarityTitle = "حماسی"
        ),
        PawnSkin(
            id = "mini_chibi_hero",
            namePersian = "نسخه Chibi چیبی بامزه",
            nameEnglish = "Chibi Hero Miniature",
            category = PawnSkinCategory.HERO_MINIATURES,
            materialType = PawnMaterialType.CHIBI_FIGURE,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF0284C7),
            glowColor = Color(0xFFE0F2FE),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🤺",
            descriptionPersian = "پیکره مینیاتوری سه بعدی با سر بزرگ و کلاهخود شبیه قهرمان انتخابی شما"
        ),
        PawnSkin(
            id = "mini_warrior_pawn",
            namePersian = "نسخه جنگجوی کوچک پالادین",
            nameEnglish = "Tiny Warrior Paladin",
            category = PawnSkinCategory.HERO_MINIATURES,
            materialType = PawnMaterialType.GLOSSY_METALLIC,
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF1E293B),
            glowColor = Color(0xFFE2E8F0),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🛡️",
            descriptionPersian = "مجسمه نقره‌ای شوالیه زره‌پوش با سپر و شمشیر در دست"
        ),
        PawnSkin(
            id = "mini_spirit_companion",
            namePersian = "نسخه حیوان همراه قهرمان (Familiar)",
            nameEnglish = "Spirit Companion",
            category = PawnSkinCategory.HERO_MINIATURES,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF4C1D95),
            glowColor = Color(0xFFF3E8FF),
            trailType = PawnTrailFxType.MAGIC_RUNES,
            ambientParticleEmoji = "🦊",
            descriptionPersian = "موجود افسانه‌ای بالدار کوچک که با وفاداری برای هدایت بازیکن حرکت می‌کند"
        ),

        // ==========================================
        // 8. SPECIAL CATEGORY (6 skins)
        // ==========================================
        PawnSkin(
            id = "special_rare_celestial",
            namePersian = "پوسته کمیاب اخترشناسی",
            nameEnglish = "Celestial Rare Orb",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFFEC4899),
            glowColor = Color(0xFFEEF2FF),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🪐",
            descriptionPersian = "پوسته کمیاب ستاره‌شناسان با مدار حلقوی متحرک و دانه‌های کهکشانی",
            rarityTitle = "کمیاب",
            isLocked = true,
            unlockConditionPersian = "پرتاب ۳ بار تاس با عدد شش"
        ),
        PawnSkin(
            id = "special_seasonal_aurora",
            namePersian = "پوسته فصلی شفق قطبی",
            nameEnglish = "Seasonal Aurora Borealis",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFFA855F7),
            glowColor = Color(0xFFFEF08A),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "🌈",
            descriptionPersian = "پوسته فصلی با پرده‌های متحرک نورهای شفق و طیف پیوسته رنگین‌کمان",
            rarityTitle = "فصلی",
            isLocked = false
        ),
        PawnSkin(
            id = "special_festive_jubilee",
            namePersian = "پوسته مناسبتی جشن طلایی",
            nameEnglish = "Festive Jubilee Carnival",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFDC2626),
            glowColor = Color(0xFFFEF3C7),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🎉",
            descriptionPersian = "پوسته مناسبتی جشن و نورافشانی با روبان‌های زرین و آتش‌بازی پیروزی",
            rarityTitle = "مناسبتی",
            isLocked = false
        ),
        PawnSkin(
            id = "special_cosmic_nebula",
            namePersian = "پوسته متحرک سحابی کیهانی",
            nameEnglish = "Cosmic Nebula",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFFEC4899),
            glowColor = Color(0xFFFDF2F8),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🌌",
            descriptionPersian = "گرداب کهکشانی از ستاره‌ها و گازهای رنگارنگ سحابی در یک گوی متحرک",
            rarityTitle = "افسانه‌ای",
            isLocked = true,
            unlockConditionPersian = "پیروزی در یک نبرد با هوش مصنوعی دشوار"
        ),
        PawnSkin(
            id = "special_cyber_neon",
            namePersian = "پوسته متحرک نئون سایبرپانک",
            nameEnglish = "Cyberpunk Pulse",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFFF43F5E),
            glowColor = Color(0xFFCFFAFE),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "💫",
            descriptionPersian = "حلقه‌های هولوگرافیک دیجیتال با تغییر رنگ خطوط نوری تپنده",
            rarityTitle = "ویژه"
        ),
        PawnSkin(
            id = "special_golden_dragon",
            namePersian = "پوسته ویژه اژدهای زرین",
            nameEnglish = "Golden Dragonet",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFB45309),
            glowColor = Color(0xFFFEF3C7),
            trailType = PawnTrailFxType.FIRE_EMBERS,
            ambientParticleEmoji = "🐉",
            descriptionPersian = "اژدهای طلایی پیچیده به دور گوهر نورانی که با هر جهش غرش نوری می‌کند",
            rarityTitle = "افسانه‌ای",
            isLocked = true,
            unlockConditionPersian = "کسب قهرمانی در بازی"
        )
    )

    val defaultUnlockedSkinIds: Set<String> = allSkins
        .filter { !it.isLocked }
        .map { it.id }
        .toSet()

    fun getSkinById(id: String): PawnSkin {
        val resolvedId = when (id) {
            "luxury_pure_gold" -> "luxury_gold"
            "elemental_living_flame" -> "elemental_fire"
            "special_cosmic" -> "special_cosmic_nebula"
            else -> id
        }
        return allSkins.find { it.id == resolvedId } ?: allSkins.find { it.id == id } ?: allSkins.first()
    }

    fun getSkinsByCategory(category: PawnSkinCategory): List<PawnSkin> {
        return allSkins.filter { it.category == category }
    }
}
