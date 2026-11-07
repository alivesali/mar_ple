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
        descriptionPersian = "طلا، نقره استرلینگ، الماس تراش‌خورده و کریستال‌های ارزشمند سلطنتی"
    ),
    FANTASY(
        persianTitle = "فانتزی و جادو",
        emoji = "🔮",
        descriptionPersian = "گوی‌های جادویی شناور، هسته پلاسما، کریستال‌های معلق و انرژی اثیری"
    ),
    NATURE(
        persianTitle = "طبیعت و جنگل",
        emoji = "🌿",
        descriptionPersian = "برگ شبنم‌دار، شکوفه نیلوفر، سنگ‌های خزه‌دار و قارچ‌های شب‌تاب"
    ),
    ELEMENTAL(
        persianTitle = "عناصر چهارگانه",
        emoji = "🔥",
        descriptionPersian = "شعله آتشین، بلور یخبندان، قطره اقیانوس، گردباد باد و صاعقه الکتریکی"
    ),
    ROYAL(
        persianTitle = "سلطنتی و امپراتوری",
        emoji = "👑",
        descriptionPersian = "تاج‌های مینیاتوری طلایی، گوی‌های جواهرنشان و مجسمه‌های فتح"
    ),
    HERO_MINIATURES(
        persianTitle = "مینیاتور قهرمانان",
        emoji = "🤺",
        descriptionPersian = "نسخه مینیاتوری چیبی و پیکره کوچک خود قهرمانان میدان نبرد"
    ),
    SPECIAL(
        persianTitle = "ویژه و متحرک",
        emoji = "🌌",
        descriptionPersian = "سحابی‌های کیهانی، نئون سایبرپانک، شفق قطبی و پوسته‌های مناسبتی"
    )
}

/**
 * Trail Particle FX type generated when a pawn moves across tiles.
 */
enum class PawnTrailFxType(val persianLabel: String, val particleEmoji: String) {
    GOLDEN_DUST("غبار طلایی", "✨"),
    FIRE_EMBERS("جرقه‌های آتش", "🔥"),
    ICE_CRYSTALS("بلورهای برف", "❄️"),
    LEAF_SWIRL("برگ‌های شناور", "🍃"),
    LIGHTNING_ARCS("قوس‌های صاعقه", "⚡"),
    STAR_SPARKLES("ستاره‌های درخشان", "⭐"),
    BUBBLE_STREAM("حباب‌های آب", "🫧"),
    NEON_GLOW("ردپای نئون", "💫"),
    MAGIC_RUNES("حروف رانیک جادو", "🪄")
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
    LIVING_FIRE,
    ORGANIC_NATURE,
    PLASMA_ENERGY,
    CHIBI_FIGURE
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
    val isDefaultUnlocked: Boolean = true
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
        // 1. CLASSIC CATEGORY
        // ==========================================
        PawnSkin(
            id = "classic_standard",
            namePersian = "مهره کلاسیک استاندارد",
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
            namePersian = "شیشه‌ای شفاف (Glass)",
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
            namePersian = "آینه‌ای براق (Glossy Mirror)",
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
            namePersian = "دیسک مینیمال مدرن",
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
        // 2. LUXURY CATEGORY
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
            rarityTitle = "کمیاب"
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
            rarityTitle = "حماسی"
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

        // ==========================================
        // 3. FANTASY CATEGORY
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
            glowColor = Color(0xFFFEF08A),
            trailType = PawnTrailFxType.FIRE_EMBERS,
            ambientParticleEmoji = "🔥",
            descriptionPersian = "آتش زنده که در مرکز مهره شعله می‌کشد و جرقه‌های پرنده تولید می‌کند"
        ),
        PawnSkin(
            id = "fantasy_plasma_core",
            namePersian = "هسته رآکتور پلاسما",
            nameEnglish = "Plasma Core",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF0E7490),
            glowColor = Color(0xFFA5F3FC),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "⚛️",
            descriptionPersian = "میدان مهارکننده مغناطیسی با حلقه‌های چرخان انرژی کوانتومی"
        ),
        PawnSkin(
            id = "fantasy_floating_crystal",
            namePersian = "مونولیت کریستال معلق",
            nameEnglish = "Floating Monolith",
            category = PawnSkinCategory.FANTASY,
            materialType = PawnMaterialType.AMETHYST_CRYSTAL,
            primaryColor = Color(0xFFC084FC),
            secondaryColor = Color(0xFF581C87),
            glowColor = Color(0xFFFAF5FF),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🔷",
            descriptionPersian = "هرم کریستالی شناور که خلاف جهت جاذبه در هوا تاب می‌خورد"
        ),

        // ==========================================
        // 4. NATURE CATEGORY
        // ==========================================
        PawnSkin(
            id = "nature_leaf",
            namePersian = "برگ سبز شبنم‌دار",
            nameEnglish = "Dewdrop Leaf",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFF84CC16),
            secondaryColor = Color(0xFF3F6212),
            glowColor = Color(0xFFECFCCB),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🍃",
            descriptionPersian = "برگ درخت کهنسال با قطره شبنم زلال که نور را منعکس می‌سازد"
        ),
        PawnSkin(
            id = "nature_magic_flower",
            namePersian = "شکوفه نیلوفر جادویی",
            nameEnglish = "Mystic Lotus",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFFF472B6),
            secondaryColor = Color(0xFF9D174D),
            glowColor = Color(0xFFFCE7F3),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🪷",
            descriptionPersian = "گلبرگ‌های صورتی باز شونده با گرده‌های نورانی معلق"
        ),
        PawnSkin(
            id = "nature_forest_stone",
            namePersian = "سنگ جنگلی خزه‌دار",
            nameEnglish = "Mossy Rune Stone",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFF65A30D),
            secondaryColor = Color(0xFF1E293B),
            glowColor = Color(0xFFD9F99D),
            trailType = PawnTrailFxType.LEAF_SWIRL,
            ambientParticleEmoji = "🪨",
            descriptionPersian = "سنگ صیقلی رودخانه با خطوط رون سبز و خزه زنده"
        ),
        PawnSkin(
            id = "nature_fantasy_shroom",
            namePersian = "قارچ شب‌تاب فانتزی",
            nameEnglish = "Bioluminescent Shroom",
            category = PawnSkinCategory.NATURE,
            materialType = PawnMaterialType.ORGANIC_NATURE,
            primaryColor = Color(0xFF2DD4BF),
            secondaryColor = Color(0xFF0F766E),
            glowColor = Color(0xFFCCFBF1),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🍄",
            descriptionPersian = "کلاهک خال‌دار قارچ ماورایی با نور فیروزه‌ای ملایم در تاریکی"
        ),

        // ==========================================
        // 5. ELEMENTAL CATEGORY
        // ==========================================
        PawnSkin(
            id = "elemental_fire",
            namePersian = "گوی دوزخ آتشین (Fire)",
            nameEnglish = "Inferno Orb",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.LIVING_FIRE,
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFF991B1B),
            glowColor = Color(0xFFFEE2E2),
            trailType = PawnTrailFxType.FIRE_EMBERS,
            ambientParticleEmoji = "🔥",
            descriptionPersian = "هسته‌ای از گدازه جوشان با زبانه‌های آتش سرخ و نارنجی"
        ),
        PawnSkin(
            id = "elemental_ice",
            namePersian = "بلور یخبندان قطبی (Ice)",
            nameEnglish = "Glacial Shard",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.AMETHYST_CRYSTAL,
            primaryColor = Color(0xFF60A5FA),
            secondaryColor = Color(0xFF1D4ED8),
            glowColor = Color(0xFFDBEAFE),
            trailType = PawnTrailFxType.ICE_CRYSTALS,
            ambientParticleEmoji = "❄️",
            descriptionPersian = "یخ شفاف قطبی با ذرات برف ریز که در حرکت پشت سر می‌ریزد"
        ),
        PawnSkin(
            id = "elemental_water",
            namePersian = "قطره آب اقیانوسی (Water)",
            nameEnglish = "Ocean Drop",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.TRANSLUCENT_GLASS,
            primaryColor = Color(0xFF0284C7),
            secondaryColor = Color(0xFF075985),
            glowColor = Color(0xFFBAE6FD),
            trailType = PawnTrailFxType.BUBBLE_STREAM,
            ambientParticleEmoji = "💧",
            descriptionPersian = "قطره آب زلال با امواج دایره‌ای شناور و حباب‌های کوچک"
        ),
        PawnSkin(
            id = "elemental_wind",
            namePersian = "گردباد اثیر فیروزه‌ای (Wind)",
            nameEnglish = "Aero Tempest",
            category = PawnSkinCategory.ELEMENTAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF14B8A6),
            secondaryColor = Color(0xFF0F766E),
            glowColor = Color(0xFF99F6E4),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "🌪️",
            descriptionPersian = "مارپیچ بادهای فیروزه‌ای که با چرخش سریع حرکت را روان می‌سازد"
        ),
        PawnSkin(
            id = "elemental_lightning",
            namePersian = "صاعقه ولتاژ بالا (Lightning)",
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
        // 6. ROYAL CATEGORY
        // ==========================================
        PawnSkin(
            id = "royal_mini_crown",
            namePersian = "تاج امپراتوری مینیاتوری",
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
            namePersian = "مجسمه بالدار فتح",
            nameEnglish = "Winged Victory Statue",
            category = PawnSkinCategory.ROYAL,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFF78350F),
            glowColor = Color(0xFFFFFBEB),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🏆",
            descriptionPersian = "پیکره بالدار طلایی ایستاده روی پایه مرمرین سیاه"
        ),

        // ==========================================
        // 7. HERO MINIATURES CATEGORY
        // ==========================================
        PawnSkin(
            id = "mini_chibi_hero",
            namePersian = "نسخه چیبی قهرمان انتخابی",
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
            namePersian = "پالادین کوچک زره‌پوش",
            nameEnglish = "Tiny Paladin",
            category = PawnSkinCategory.HERO_MINIATURES,
            materialType = PawnMaterialType.GLOSSY_METALLIC,
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF1E293B),
            glowColor = Color(0xFFE2E8F0),
            trailType = PawnTrailFxType.GOLDEN_DUST,
            ambientParticleEmoji = "🛡️",
            descriptionPersian = "مجسمه نقره‌ای شوالیه زره‌پوش با سپر در دست"
        ),
        PawnSkin(
            id = "mini_spirit_companion",
            namePersian = "روح همراه جادویی (Familiar)",
            nameEnglish = "Spirit Familiar",
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
        // 8. SPECIAL CATEGORY
        // ==========================================
        PawnSkin(
            id = "special_cosmic_nebula",
            namePersian = "سحابی کیهانی متحرک",
            nameEnglish = "Cosmic Nebula",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFFEC4899),
            glowColor = Color(0xFFFDF2F8),
            trailType = PawnTrailFxType.STAR_SPARKLES,
            ambientParticleEmoji = "🌌",
            descriptionPersian = "گرداب کهکشانی از ستاره‌ها و گازهای رنگارنگ سحابی در یک گوی",
            rarityTitle = "افسانه‌ای"
        ),
        PawnSkin(
            id = "special_cyber_neon",
            namePersian = "نئون سایبرپانک تپنده",
            nameEnglish = "Cyberpunk Pulse",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFFF43F5E),
            glowColor = Color(0xFFCFFAFE),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "💫",
            descriptionPersian = "حلقه‌های هولوگرافیک دیجیتال با تغییر رنگ خطوط نوری",
            rarityTitle = "ویژه"
        ),
        PawnSkin(
            id = "special_golden_dragon",
            namePersian = "اژدهای کوچک زرین",
            nameEnglish = "Golden Dragonet",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PURE_GOLD,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFB45309),
            glowColor = Color(0xFFFEF3C7),
            trailType = PawnTrailFxType.FIRE_EMBERS,
            ambientParticleEmoji = "🐉",
            descriptionPersian = "اژدهای طلایی پیچیده به دور گوهر نورانی که با هر جهش غرش نوری می‌کند",
            rarityTitle = "افسانه‌ای"
        ),
        PawnSkin(
            id = "special_rainbow_aurora",
            namePersian = "شفق قطبی رنگین‌کمانی",
            nameEnglish = "Prismatic Aurora",
            category = PawnSkinCategory.SPECIAL,
            materialType = PawnMaterialType.PLASMA_ENERGY,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFFA855F7),
            glowColor = Color(0xFFFEF08A),
            trailType = PawnTrailFxType.NEON_GLOW,
            ambientParticleEmoji = "🌈",
            descriptionPersian = "پرده‌های متحرک نورهای شفق شمالی با طیف پیوسته رنگین‌کمان",
            rarityTitle = "فصلی"
        )
    )

    fun getSkinById(id: String): PawnSkin {
        return allSkins.find { it.id == id } ?: allSkins.first()
    }

    fun getSkinsByCategory(category: PawnSkinCategory): List<PawnSkin> {
        return allSkins.filter { it.category == category }
    }
}
