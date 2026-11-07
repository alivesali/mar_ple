package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Interactive animation stances for Hero Showcase and In-Game VFX preview.
 */
enum class HeroAnimationStance(
    val persianLabel: String,
    val iconEmoji: String,
    val descriptionPersian: String
) {
    IDLE(
        persianLabel = "آماده‌باش",
        iconEmoji = "🧘",
        descriptionPersian = "تنفس آرام، نوسان هاله جادویی و گردش ذرات هماهنگ با عنصر قهرمان"
    ),
    ATTACK(
        persianLabel = "حمله و یورش",
        iconEmoji = "⚔️",
        descriptionPersian = "جهش رو به جلو، رهایش پرتابه یا ضربه سلاح با رد نوری خیره‌کننده"
    ),
    DEFENSE(
        persianLabel = "سپر و پایداری",
        iconEmoji = "🛡️",
        descriptionPersian = "استقرار تدافعی، احضار سد دفاعی نورانی و جرقه بازتاب ضربات"
    ),
    ABILITY(
        persianLabel = "فعال‌سازی قدرت",
        iconEmoji = "✨",
        descriptionPersian = "انفجار هاله اختصاصی، چرخش نمادهای جادویی و تمرکز انرژی قهرمان"
    ),
    VICTORY(
        persianLabel = "پیروزی",
        iconEmoji = "🏆",
        descriptionPersian = "ژست قهرمانانه فاتح میدان، بالارفتن سلاح و رقص نورهای زرین"
    ),
    DEFEAT(
        persianLabel = "استقامت در شکست",
        iconEmoji = "🥀",
        descriptionPersian = "فرود زانو بر زمین با حفظ استقامت و شعله امیدی که در چشمانش باقی می‌ماند"
    )
}

/**
 * Visual Render Quality mode for responsive device optimization.
 */
enum class HeroRenderQuality(
    val persianLabel: String,
    val iconEmoji: String,
    val descriptionPersian: String
) {
    CINEMATIC_3D(
        persianLabel = "سه‌بعدی سینمایی",
        iconEmoji = "✨",
        descriptionPersian = "سایه‌زنی لایه‌ای، نورپردازی جهت‌دار، بازتاب متریال و ذرات پویای متحرک"
    ),
    LIGHTWEIGHT_2D(
        persianLabel = "دوبعدی سبک",
        iconEmoji = "⚡",
        descriptionPersian = "طراحی وکتور سریع و سبک برای بالاترین نرخ فریم و صرفه‌جویی باتری"
    )
}

/**
 * Complete Visual Identity, Attire, Lore Story, and Animation Profile of a Hero.
 */
data class HeroVisualProfile(
    val heroId: String,
    val englishName: String,
    val persianTitle: String,
    val heroicRole: String,
    val combatStyle: String,
    val loreStoryShort: String,
    val entranceAnimName: String,
    val abilityAnimName: String,
    val victoryAnimName: String,
    val defeatAnimName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentGlowColor: Color,
    val armorColor: Color,
    val eyeColor: Color,
    val hairColor: Color,
    val weaponVisual: HeldItemVisual,
    val weaponNamePersian: String,
    val particleEmoji: String,
    val elementName: String,
    val quotePersian: String
)

/**
 * Catalog of high-quality original Fantasy Hero identities.
 */
object HeroVisualCatalog {

    val allProfiles: List<HeroVisualProfile> = listOf(
        // 1. Jack — Iron Vanguard
        HeroVisualProfile(
            heroId = "jack",
            englishName = "Jack",
            persianTitle = "شوالیه سپر طلایی (Iron Vanguard)",
            heroicRole = "مدافع سنگین‌وزن و پاسدار صفوف",
            combatStyle = "شمشیرزنی استوار و دفاع غیرقابل نفوذ با سپر سلطنتی",
            loreStoryShort = "جک، فرمانده گارد پادشاهی آیرون‌گارد، زرهی از آلیاژ خورشید و پولاد گداخته بر تن دارد. سپر عظیم او نه تنها سنگین‌ترین نیش‌های مارهای باستانی را مهار می‌کند، بلکه با رهایش موج شوک ارتعاشی، دشمنان را به عقب می‌راند.",
            entranceAnimName = "فرود کوبنده بر زمین با طنین برخورد سپر پولادین",
            abilityAnimName = "احضار گنبد شش‌ضلعی زرین با امواج بازتابنده نور",
            victoryAnimName = "کوبیدن سپر بر زمین و برافراشتن پرچم طلایی فتح",
            defeatAnimName = "تکیه بر لبه سپر با سری برافراشته و زرهی مقاوم",
            primaryColor = Color(0xFF0EA5E9),
            secondaryColor = Color(0xFF0369A1),
            accentGlowColor = Color(0xFFFACC15),
            armorColor = Color(0xFF334155),
            eyeColor = Color(0xFF38BDF8),
            hairColor = Color(0xFF451A03),
            weaponVisual = HeldItemVisual.RADIANT_SHIELD,
            weaponNamePersian = "سپر شیرنشان آیرون‌هارت",
            particleEmoji = "🛡️",
            elementName = "پولاد و نور خورشید",
            quotePersian = "هیچ نیشی از سد این سپر نخواهد گذشت!"
        ),

        // 2. Luna — Celestial Mage
        HeroVisualProfile(
            heroId = "luna",
            englishName = "Luna",
            persianTitle = "معمار پله‌های اختری (Celestial Mage)",
            heroicRole = "جادوگر کیهانی و دستکاری فضا",
            combatStyle = "جادوی فاصله دور، بازتاب نور ماه و احضار پل‌های اثیری",
            loreStoryShort = "لونا در معبد ستاره‌های دنباله‌دار پرورش یافته است. او تاروپود پله‌ها را از غبار سحابی‌های کهکشانی می‌بافد و زمانی که رقیبی به او یورش می‌برد، با خلق سراب‌های توهمی مسیر حمله را در مه اختری گم می‌کند.",
            entranceAnimName = "فرود آرام از گرداب سحابی با درخشش نور ماه",
            abilityAnimName = "چرخش گوی‌های کیهانی و خلق پله‌ای از نور ستاره‌ها",
            victoryAnimName = "چرخش شناور در فضا و فروریختن بارانی از شهاب‌های نقره‌ای",
            defeatAnimName = "تکیه آرام بر عصای بلورین با محو تدریجی نور کیهانی",
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF6B21A8),
            accentGlowColor = Color(0xFFFDE047),
            armorColor = Color(0xFF1E1B4B),
            eyeColor = Color(0xFFE9D5FF),
            hairColor = Color(0xFF312E81),
            weaponVisual = HeldItemVisual.LADDER_STAFF,
            weaponNamePersian = "عصای اختری منشور شب",
            particleEmoji = "🪄",
            elementName = "ستاره‌ها و مه کیهانی",
            quotePersian = "مسیر آسمان‌ها به اشاره اراده من گشوده می‌شود."
        ),

        // 3. Axel — Flash Striker
        HeroVisualProfile(
            heroId = "axel",
            englishName = "Axel",
            persianTitle = "صاعقه‌ران پیست (Flash Striker)",
            heroicRole = "مهاجم آذرخشی با شتاب فوق‌صوت",
            combatStyle = "سرعت بی‌امان، گام سایه نزدیک و ضربات برشی پلاسمایی",
            loreStoryShort = "اکسل مخترع چکمه‌های جت توربو-پلاسما است. در مسابقات سرعت سرزمین، هیچ مانعی جلودار گام‌های رعدآسای او نیست و با درهم شکستن دیوار صوتی، هر رقیب را از ارتعاش گام‌هایش پس می‌زند.",
            entranceAnimName = "هجوم با رد صاعقه سرخ و ترمز سریع با جرقه‌های الکتریکی",
            abilityAnimName = "آزادباش احتراق اگزوزهای توربو و جهش کوانتومی به جلو",
            victoryAnimName = "پرواز دایره‌ای روی هوا با جت‌های سرخ و فرود نمایشی پیروزی",
            defeatAnimName = "نشستن خسته روی یک پا با خروج بخار خنک‌کننده از چکمه‌ها",
            primaryColor = Color(0xFFFF2A5F),
            secondaryColor = Color(0xFF9F1239),
            accentGlowColor = Color(0xFFFACC15),
            armorColor = Color(0xFF18181B),
            eyeColor = Color(0xFFFEF08A),
            hairColor = Color(0xFFFACC15),
            weaponVisual = HeldItemVisual.JET_BOOTS,
            weaponNamePersian = "چکمه‌های مافوق صوت هلیوس",
            particleEmoji = "⚡",
            elementName = "صاعقه و پلاسمای سرخ",
            quotePersian = "قبل از اینکه پلک بزنی، خط پایان را لمس کرده‌ام!"
        ),

        // 4. Nova — Solar Arbalist
        HeroVisualProfile(
            heroId = "nova",
            englishName = "Nova",
            persianTitle = "تیرانداز ستاره‌شکن (Solar Arbalist)",
            heroicRole = "تک‌تیرانداز دوربرد با دید محیطی",
            combatStyle = "پرتاب تیرهای فوتونی دوگانه و نشانه‌گیری دقیق اهداف متحرک",
            loreStoryShort = "نوا با چشمان عقابی و کمانی مجهز به عدسی‌های تمرکز نور خورشید، دورترین حرکات صفحه بازی را زیر نظر دارد. او می‌تواند با یک تیر نوری سرعت صدرنشین مسابقه را کاهش داده و مسیر امن خود را برگزیند.",
            entranceAnimName = "جهش آکروباتیک به سکوی دیده‌بانی و کشیدن زه کمان",
            abilityAnimName = "تمرکز پرتو خورشیدی روی هدف و شلیک رگبار فوتونی دوقلو",
            victoryAnimName = "چرخاندن حرفه‌ای کمان و شلیک تیر آتش‌بازی طلایی به آسمان",
            defeatAnimName = "برداشتن یک تیر باقیمانده و حفظ تمرکز برای شانس بعدی",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFB45309),
            accentGlowColor = Color(0xFFFDE68A),
            armorColor = Color(0xFF292524),
            eyeColor = Color(0xFFF59E0B),
            hairColor = Color(0xFF9A3412),
            weaponVisual = HeldItemVisual.GOLDEN_BOW,
            weaponNamePersian = "کمان پرتو خورشید سلستیا",
            particleEmoji = "🎯",
            elementName = "پرتوهای خورشید و آتش زرین",
            quotePersian = "یک هدف، یک تیر، یک پیروزی قطعی."
        ),

        // 5. Blade — Serpent Slayer
        HeroVisualProfile(
            heroId = "blade",
            englishName = "Blade",
            persianTitle = "شکارچی افعی‌ها (Serpent Slayer)",
            heroicRole = "شکارچی بی‌رحم و مجازات‌کننده مارها",
            combatStyle = "شمشیرزنی دو دست با تیغه‌های دندانه‌دار و حملات غافلگیرکننده",
            loreStoryShort = "بلید زندگی‌اش را وقف پاکسازی مارهای عظیم‌الجثه کرده است. او شنل ساخته‌شده از پوست اژدهای باستانی بر دوش دارد. هرگاه روی خانه‌ای فرود آید که مهره دیگری در آن حضور دارد، با ضربه سنگین رقیب را به عقب می‌راند.",
            entranceAnimName = "خروج غرش‌آسا از دود با بیرون کشیدن شمشیرهای دندانه‌دار",
            abilityAnimName = "یورش چرخشی مرگبار با رد سبز زمردی و شکافتن هوا",
            victoryAnimName = "فرو بردن تیغه بر زمین و استقرار پایدار شکارچی پیروز",
            defeatAnimName = "تمیز کردن تیغه شمشیر با اخم مصمم و اراده انتقام",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF065F46),
            accentGlowColor = Color(0xFF34D399),
            armorColor = Color(0xFF0F172A),
            eyeColor = Color(0xFF6EE7B7),
            hairColor = Color(0xFF022C22),
            weaponVisual = HeldItemVisual.SLAYER_BLADE,
            weaponNamePersian = "تیغه‌های دوگانه شکارچی اژدها",
            particleEmoji = "⚔️",
            elementName = "فولاد زمردین و زهر شکار",
            quotePersian = "مارها از من می‌گریزند؛ تو نیز بهتر است فاصله بگیری."
        ),

        // 6. Raven — Shadow Assassin
        HeroVisualProfile(
            heroId = "raven",
            englishName = "Raven",
            persianTitle = "ملکه سایه‌ها (Shadow Assassin)",
            heroicRole = "قاتل ماورایی با قابلیت انتقال مکان",
            combatStyle = "حرکت در تاریکی، خنجرهای سایه‌گون و غافلگیری آنی",
            loreStoryShort = "ریون میان نور و تاریکی معلق است. چشمان نیلگون و ردای مهتابی‌اش در دل سایه‌ها ناپدید می‌شود. در لحظه مناسب، او توانایی مبادله جایگاه با صدرنشین را دارد و جریان پیروزی را دگرگون می‌کند.",
            entranceAnimName = "شکل‌گیری از میان حلقه پرهای کلاغ و دود بنفش تاریک",
            abilityAnimName = "انحلال در سایه و ظهور آنی در جایگاه رقیب پیشتاز",
            victoryAnimName = "محو شدن در هاله تاریک همراه با لبخند پیروزمندانه ملکه",
            defeatAnimName = "پوشاندن چهره با شنل سایه برای تجدید قوا در تاریکی",
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF312E81),
            accentGlowColor = Color(0xFFC7D2FE),
            armorColor = Color(0xFF0B0F19),
            eyeColor = Color(0xFFA5B4FC),
            hairColor = Color(0xFF030712),
            weaponVisual = HeldItemVisual.SHADOW_DAGGER,
            weaponNamePersian = "خنجر هلالی تهی‌گاه",
            particleEmoji = "🌑",
            elementName = "سایه مطلق و خلأ نیلوفری",
            quotePersian = "جایی که فکر می‌کنی امن‌ترین است، سایه من نشسته است."
        ),

        // 7. Kai — Storm Walker
        HeroVisualProfile(
            heroId = "kai",
            englishName = "Kai",
            persianTitle = "پیمایشگر طوفان (Storm Walker)",
            heroicRole = "لنگر تعادل و مهارکننده بادهای مخالف",
            combatStyle = "کنترل گرانش، ضربات لنگر زمردی و ایستادگی صخره‌ای",
            loreStoryShort = "کای راهب بلندمرتبه معابد طوفان‌های کوهستان مه است. لنگر مغناطیسی او کشش زمین را مهار می‌کند؛ بنابراین خانه‌های معکوس و بادهای سهمگین او را تنها نیمی از مسیر تکان می‌دهند.",
            entranceAnimName = "فرود ضربتی لنگر یشمین و متوقف شدن بادهای اطراف",
            abilityAnimName = "ایجاد میدان گرانشی چرخشی و پایدارسازی گام‌ها",
            victoryAnimName = "چرخاندن زنجیر لنگر دور بازو و تعظیم متواضعانه پیروزی",
            defeatAnimName = "تکیه بر دسته لنگر و تنفس عمیق کوهستانی",
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF155E75),
            accentGlowColor = Color(0xFFA5F3FC),
            armorColor = Color(0xFF164E63),
            eyeColor = Color(0xFF67E8F9),
            hairColor = Color(0xFF1E293B),
            weaponVisual = HeldItemVisual.GRAVITY_ANCHOR,
            weaponNamePersian = "لنگر گرانشی ژادیت",
            particleEmoji = "⚓",
            elementName = "طوفان کوهستان و یشم سنگین",
            quotePersian = "کوه به تکان هر نسیمی سر خم نمی‌کند."
        ),

        // 8. Ivy — Nature Guardian
        HeroVisualProfile(
            heroId = "ivy",
            englishName = "Ivy",
            persianTitle = "نگهبان بیدار طبیعت (Nature Guardian)",
            heroicRole = "شفاگر و دگرگون‌کننده سموم محیطی",
            combatStyle = "رویاندن ریشه‌های محافظ، هاگ‌های درمانی و زره گیاهی",
            loreStoryShort = "آیوی بانوی کهن‌جنگل‌های سبز است. موهای او با شاخه‌های زنده گل‌های جنگلی آمیخته و هرگونه سم، کرم لزج یا باتلاق برای او منبع تغذیه و رشد است و بلایا را به جهش‌های مثبت بدل می‌سازد.",
            entranceAnimName = "جوانه زدن نیلوفرهای آبی و ظهور آیوی از میان پیچک‌های سبز",
            abilityAnimName = "پرتاب باران هاگ‌های درخشان و شکوفایی گل‌های معطر",
            victoryAnimName = "بالا بردن عصای پیچک با رقص پروانه‌های زمردین پیرامون",
            defeatAnimName = "پناه گرفتن زیر گلبرگ‌های نیلوفر برای تجدید زندگی",
            primaryColor = Color(0xFF84CC16),
            secondaryColor = Color(0xFF3F6212),
            accentGlowColor = Color(0xFFBEF264),
            armorColor = Color(0xFF14532D),
            eyeColor = Color(0xFFA3E635),
            hairColor = Color(0xFF15803D),
            weaponVisual = HeldItemVisual.ANTIDOTE_VIAL,
            weaponNamePersian = "عصای بیدمشک و اکسیر بهار",
            particleEmoji = "🌿",
            elementName = "حیات نباتی و زمرد طبیعت",
            quotePersian = "حتی عمیق‌ترین باتلاق، بستر رویش زیباترین گل‌هاست."
        ),

        // 9. Phoenix — Solar Ascendant
        HeroVisualProfile(
            heroId = "phoenix",
            englishName = "Phoenix",
            persianTitle = "وارث آتشین ققنوس (Solar Ascendant)",
            heroicRole = "پروازگر هوایی و برخاسته از خاکستر",
            combatStyle = "بال‌های آتشین، موج‌های حرارتی و پرواز بر فراز گودال‌ها",
            loreStoryShort = "فونیکس دارنده تاج پرهای آتشین ققنوس است. تله‌ها، چاه‌ها و باتلاق‌ها زیر بال‌های سوزان او بی‌اثرند و هر بار که با مقاومتش مانعی را رد می‌کند، روح آتشینش ۲ نوبت پرتاب بعدی او را شعله‌ور می‌سازد.",
            entranceAnimName = "فرود چون شهاب سوزان با باز شدن بال‌های آتشین ققنوس",
            abilityAnimName = "اوج‌گیری به آسمان و افروختن هاله سرخ خورشیدی بر صفحه",
            victoryAnimName = "چرخش با شکوه در هوا با رقص جرقه‌های طلایی و آتشین",
            defeatAnimName = "جمع کردن بال‌ها به دور خویش و آماده‌سازی برای تولدی دوباره",
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFF9A3412),
            accentGlowColor = Color(0xFFFDE047),
            armorColor = Color(0xFF7C2D12),
            eyeColor = Color(0xFFFBBF24),
            hairColor = Color(0xFFDC2626),
            weaponVisual = HeldItemVisual.PHOENIX_CROWN,
            weaponNamePersian = "تاج خورشیدی اخگر جاودان",
            particleEmoji = "🔥",
            elementName = "آتش مقدس و پرهای ققنوس",
            quotePersian = "از دل آتش برمی‌خیزم تا بر اوج آسمان‌ها فرمان برانم."
        ),

        // 10. Storm — Thunder Warlord
        HeroVisualProfile(
            heroId = "storm",
            englishName = "Storm",
            persianTitle = "فرمانروای تندر (Thunder Warlord)",
            heroicRole = "جنگ‌سالار صاعقه با شوک‌های محیطی",
            combatStyle = "کوبش پتک آذرخش، الکتریسیته زنجیره‌ای و پیشروی در کولاک",
            loreStoryShort = "استورم با پتک رعدافکن خود از بلندترین قله‌های بارانی فرود می‌آید. در خانه‌های طوفانی، انرژی باد را جذب کرده و با صاعقه‌ای مهیب رقبای مغرور را به عقب پرتاب می‌نماید.",
            entranceAnimName = "برخورد آذرخش از آسمان به زمین و ایستادن با چکش بر دوش",
            abilityAnimName = "شارژ پتک با قوس‌های الکتریکی و کوبش موج شوک رعدآسا",
            victoryAnimName = "نگه داشتن پتک رو به ابرها در میان غرش رعد و خنده پیروزی",
            defeatAnimName = "تکیه سنگین به پتک همراه با جرقه‌های کم‌فروغ در نوک آن",
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF1D4ED8),
            accentGlowColor = Color(0xFFE0F2FE),
            armorColor = Color(0xFF1E3A8A),
            eyeColor = Color(0xFF93C5FD),
            hairColor = Color(0xFFE2E8F0),
            weaponVisual = HeldItemVisual.THUNDER_HAMMER,
            weaponNamePersian = "پتک صاعقه‌کوب وولکان",
            particleEmoji = "🌩️",
            elementName = "صاعقه پرولتاژ و ابرهای توفنده",
            quotePersian = "طوفان خشم من هیچ مانعی را تاب نخواهد آورد!"
        ),

        // 11. Stella — Luminary Herald
        HeroVisualProfile(
            heroId = "stella",
            englishName = "Stella",
            persianTitle = "ستاره اقبال (Luminary Herald)",
            heroicRole = "پشتیبان برکت و دگرگون‌ساز سرنوشت تاس",
            combatStyle = "تابش هاله‌های شانس، سپر بال‌های فرشته و هدیه اقبال",
            loreStoryShort = "استلا هادی نور امید در میدان نبرد است. خانه شانس در حضور او همواره زرین‌ترین مواهب را ارزانی می‌دارد و بال‌های زرین فرشته‌اش بازیکنان عقب‌افتاده را از چنگال خطرات نجات می‌دهد.",
            entranceAnimName = "فرود از پرتو نوری سپید با ارتعاش دل‌انگیز زنگوله‌های ستاره‌ای",
            abilityAnimName = "چرخش گوی اقبال و گستردن بال‌های فرشته برای اعطای سپر",
            victoryAnimName = "اوج‌گیری سبکبال در میان بارانی از ستاره‌های زرین",
            defeatAnimName = "در آغوش گرفتن گوی نور با نجوای دعای پیروزی برای نوبت بعد",
            primaryColor = Color(0xFFEAB308),
            secondaryColor = Color(0xFF854D0E),
            accentGlowColor = Color(0xFFFEF08A),
            armorColor = Color(0xFF713F12),
            eyeColor = Color(0xFFFDE047),
            hairColor = Color(0xFFFEF08A),
            weaponVisual = HeldItemVisual.LUCKY_STAR_ORB,
            weaponNamePersian = "گوی ستاره تابان سرنوشت",
            particleEmoji = "🌟",
            elementName = "نور سحرگاه و اقبال زرین",
            quotePersian = "ستارگان همیشه روشن‌ترین مسیر را پیش پایت می‌گذارند."
        ),

        // 12. Zephyr — Mist Phantom
        HeroVisualProfile(
            heroId = "zephyr",
            englishName = "Zephyr",
            persianTitle = "شبح تندباد (Mist Phantom)",
            heroicRole = "شبح بادگریز با عبور نامرئی از تله‌ها",
            combatStyle = "تغییر حالت به گاز اثیری، گام‌های شناور و جاخالی دادن از ضربات",
            loreStoryShort = "زفیر روحی آمیخته با نسیم‌های بهاری است. هیچ نیش مار یا تله‌ای قادر به لمس پیکر او نیست زیرا او با وزش باد به حالت فاز شبح درمی‌آید و رقبای پیشتاز را با تندبادی ناگهانی عقب می‌زند.",
            entranceAnimName = "پیچش گردباد کوچک فیروزه‌ای و پدیدار شدن چشمان شبح",
            abilityAnimName = "تبدیل شدن به دود باد و عبور بی‌صدا از موانع پیش رو",
            victoryAnimName = "اسکیت شناور روی امواج هوا با چرخش تیغه‌های بادی",
            defeatAnimName = "محو شدن در هوا و ظاهر شدن مجدد با گام‌های سبک",
            primaryColor = Color(0xFF2DD4BF),
            secondaryColor = Color(0xFF115E59),
            accentGlowColor = Color(0xFF99F6E4),
            armorColor = Color(0xFF134E4A),
            eyeColor = Color(0xFF5EEAD4),
            hairColor = Color(0xFF0D9488),
            weaponVisual = HeldItemVisual.WIND_GLIDER,
            weaponNamePersian = "تیغه‌های بادبال فیروزه‌ای",
            particleEmoji = "🌪️",
            elementName = "باد اثیری و هوای روان",
            quotePersian = "می‌توانی صدایم را بشنوی، اما لمس کردنم ناممکن است."
        ),

        // 13. Orion — Void Alchemist
        HeroVisualProfile(
            heroId = "orion",
            englishName = "Orion",
            persianTitle = "کیمیاگر کهکشان (Void Alchemist)",
            heroicRole = "استاد معجون‌های دگرگونی و معکوس‌ساز جریمه‌ها",
            combatStyle = "پرتاب بالن‌های واکنش‌دهنده، سرب مذاب و انتقال طلسم‌ها",
            loreStoryShort = "اوریون در آزمایشگاه‌های معلق کیهان فرمول‌های کیمیاگری را کشف کرده است. او هر اثر منفی وارد بر خود را مستقیماً به رقیب پیشتاز منتقل می‌سازد و اعداد پایین تاس را به اعداد پنج و شش تبدیل می‌نماید.",
            entranceAnimName = "پرتاب شیشه معجون جادویی و پدیدار شدن در دود معطر بنفش",
            abilityAnimName = "جوشاندن مایع کیمیا و ریختن سرب مذاب روی تاس‌های رقیب",
            victoryAnimName = "تکان دادن ارلن شیشه‌ای طلایی با فوران جرقه‌های جادویی",
            defeatAnimName = "تنظیم دقیق ماسک برنجی و یادداشت فرمول جدید برای اصلاح",
            primaryColor = Color(0xFFC084FC),
            secondaryColor = Color(0xFF581C87),
            accentGlowColor = Color(0xFFF3E8FF),
            armorColor = Color(0xFF3B0764),
            eyeColor = Color(0xFFE9D5FF),
            hairColor = Color(0xFF2E1065),
            weaponVisual = HeldItemVisual.ALCHEMY_FLASK,
            weaponNamePersian = "بالن کیمیاگری اتریک",
            particleEmoji = "⚗️",
            elementName = "کیمیاگری و بخار اثیری",
            quotePersian = "علم کیمیا تمام طلسم‌های شوم را به طلا بدل می‌کند."
        ),

        // 14. Amber — Frost Valkyrie
        HeroVisualProfile(
            heroId = "amber",
            englishName = "Amber",
            persianTitle = "شوالیه یخ‌شکن (Frost Valkyrie)",
            heroicRole = "نگهبان بلورین با قدرت انجماد رقبا",
            combatStyle = "شمشیرزنی یخچالی، ساخت دیواره‌های یخی و گذر از چاه‌ها",
            loreStoryShort = "امبر والکیری دلاور یخچال‌های جاودان است. شمشیر یخی او سرمایی دارد که حتی چاه‌های تاریک بازی را به یخ بدل می‌کند و رقبای پیشتاز را در زندانی بلورین برای نوبت کامل منجمد می‌سازد.",
            entranceAnimName = "شکستن دیواره قندیل‌های یخی با چرخش شمشیر برفی",
            abilityAnimName = "فرو بردن شمشیر بر زمین و امتداد خط انجماد بلورین به سوی پیشتاز",
            victoryAnimName = "برافراشتن شمشیر یخی که دانه‌های بلورین برف را به اطراف می‌پاشد",
            defeatAnimName = "سپر گرفتن با تیغه پهن شمشیر یخی در برابر سوز سرما",
            primaryColor = Color(0xFF60A5FA),
            secondaryColor = Color(0xFF1E3A8A),
            accentGlowColor = Color(0xFFDBEAFE),
            armorColor = Color(0xFF172554),
            eyeColor = Color(0xFF93C5FD),
            hairColor = Color(0xFFEFF6FF),
            weaponVisual = HeldItemVisual.FROST_SWORD,
            weaponNamePersian = "شمشیر یخبندان آرکتیک",
            particleEmoji = "❄️",
            elementName = "یخ بلورین و کولاک شمالی",
            quotePersian = "شتاب حریفان در برابر سرمای تیغه‌ام منجمد خواهد شد."
        ),

        // 15. Shadow (Chronos) — Temporal Weaver
        HeroVisualProfile(
            heroId = "shadow",
            englishName = "Shadow",
            persianTitle = "مسافر زمان (Temporal Weaver)",
            heroicRole = "نگهبان زمان و بازگرداننده سقوط‌های گذشته",
            combatStyle = "وارونه‌سازی ساعت شنی، جهش به عقب زمان و خنثی‌سازی معکوس",
            loreStoryShort = "کرونوس (شدو) مسافر ابعاد ناپیداست. ساعت شنی عتیقه‌ای در دست دارد که شن‌های زمان را برخلاف جاذبه به گردش درمی‌آورد. اگر در نوبت گذشته سقوط تلخی داشته باشد، با چرخش شنی به خانه پیشین بازمی‌گردد.",
            entranceAnimName = "باز شدن شکاف زمان با ذرات ماسه طلایی و گام نهادن به اکنون",
            abilityAnimName = "وارونه کردن ساعت شنی و معکوس شدن رد حرکات پیشین",
            victoryAnimName = "معلق کردن ساعت شنی در هوا با حلقه اعداد رومی درخشان",
            defeatAnimName = "بررسی جریان شن‌های زمان با لبخندی از دانستن آینده",
            primaryColor = Color(0xFFF43F5E),
            secondaryColor = Color(0xFF881337),
            accentGlowColor = Color(0xFFFECDD3),
            armorColor = Color(0xFF4C0519),
            eyeColor = Color(0xFFFDA4AF),
            hairColor = Color(0xFF18181B),
            weaponVisual = HeldItemVisual.HOURGLASS,
            weaponNamePersian = "ساعت شنی اعصار باستانی",
            particleEmoji = "⏳",
            elementName = "شن‌های زمان و بُعد چهارم",
            quotePersian = "گذشته بازمی‌گردد تا آینده از نو نگاشته شود."
        ),

        // 16. Atlas — Mountain Colossus
        HeroVisualProfile(
            heroId = "atlas",
            englishName = "Atlas",
            persianTitle = "تایتان شکست‌ناپذیر (Mountain Colossus)",
            heroicRole = "غول سنگی با خشم برسرکر و قدرت زلزله",
            combatStyle = "کوبش مشت‌های خاره، زلزله ردیفی و ایمنی در برابر طوفان",
            loreStoryShort = "اطلس از دل گدازه‌ها و سنگ‌های باستانی رشته‌کوه زاگرس برخاسته است. طوفان‌ها و چاه‌ها در برابر وزن سهمگین او ناتوانند و هر آسیب یا نیش ماری تنها خشم او را برای جهش‌های طوفانی برمی‌انگیزد.",
            entranceAnimName = "لرزش زمین با زلزله‌ای سهمگین و برخاستن ستون صخره‌ای اطلس",
            abilityAnimName = "فشردن مشت‌های گرانیتی و انفجار گدازه و سنگ به اطراف",
            victoryAnimName = "کوبیدن دو مشت صخره‌ای به هم با غرش غول‌آسای پیروزی",
            defeatAnimName = "به حالت مجسمه سنگی درآمدن برای بازیافتن پیوند با زمین",
            primaryColor = Color(0xFFD97706),
            secondaryColor = Color(0xFF78350F),
            accentGlowColor = Color(0xFFFDE68A),
            armorColor = Color(0xFF451A03),
            eyeColor = Color(0xFFFBBF24),
            hairColor = Color(0xFF292524),
            weaponVisual = HeldItemVisual.TITAN_GAUNTLET,
            weaponNamePersian = "دستکش‌های گرانیتی قله خاره",
            particleEmoji = "🪨",
            elementName = "زمین سخت و خشم صخره‌ها",
            quotePersian = "زمین زیر پای من استوار است و زلزله، صدای گام‌هایم!"
        )
    )

    fun getProfile(heroId: String): HeroVisualProfile {
        return allProfiles.find { it.heroId == heroId } ?: allProfiles.first()
    }
}
