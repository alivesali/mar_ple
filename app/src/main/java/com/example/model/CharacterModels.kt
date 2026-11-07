package com.example.model

import androidx.compose.ui.graphics.Color

enum class CharacterGender(val persianLabel: String) {
    MALE("پسر"),
    FEMALE("دختر")
}

enum class AbilityActivationMode(val persianBadge: String) {
    PASSIVE("همیشگی"),
    AUTO_CHARGE("شارژی خودکار"),
    ACTIVE_COOLDOWN("قابل فعال‌سازی (شارژی)"),
    ACTIVE_ONCE("قابل فعال‌سازی (یک‌بار در بازی)")
}

/**
 * Step 1 of Custom Character Creation: What does the custom ability affect?
 */
enum class CustomAbilityTarget(
    val persianTitle: String,
    val emoji: String,
    val badgeColor: Color
) {
    SNAKES(
        persianTitle = "نوع مارها و نیش مار",
        emoji = "🐍",
        badgeColor = Color(0xFFEF4444)
    ),
    LADDERS(
        persianTitle = "پله‌ها و صعود",
        emoji = "🪜",
        badgeColor = Color(0xFF10B981)
    ),
    SPECIAL_OBSTACLES(
        persianTitle = "موانع خاص (کرم، باتلاق، چاه، معکوس، طوفان)",
        emoji = "⚡",
        badgeColor = Color(0xFFA855F7)
    ),
    DICE_AND_LUCK(
        persianTitle = "شانس و اعداد تاس",
        emoji = "🎲",
        badgeColor = Color(0xFFF59E0B)
    ),
    OVERALL_MOVEMENT(
        persianTitle = "حرکت کلی در صفحه",
        emoji = "🚀",
        badgeColor = Color(0xFF06B6D4)
    );

    companion object {
        fun fromName(name: String?): CustomAbilityTarget {
            return entries.find { it.name == name } ?: SNAKES
        }
    }
}

/**
 * Step 2 of Custom Character Creation: What type of effect does it apply?
 */
enum class CustomAbilityEffectType(
    val persianTitle: String,
    val emoji: String
) {
    NEUTRALIZE_IMMUNE(
        persianTitle = "خنثی‌سازی کامل اثر منفی",
        emoji = "🛡️"
    ),
    HALVE_PENALTY(
        persianTitle = "نصف کردن سقوط یا جریمه",
        emoji = "⚓"
    ),
    REDUCE_EFFECT(
        persianTitle = "کاهش اثر منفی",
        emoji = "📉"
    ),
    INCREASE_CHANCE(
        persianTitle = "افزایش شانس و احتمال",
        emoji = "🍀"
    ),
    BONUS_STEPS_BOOST(
        persianTitle = "جهش و حرکت اضافه",
        emoji = "✨"
    ),
    TRIGGER_PROBABILITY(
        persianTitle = "احتمال فعال شدن قدرت ویژه",
        emoji = "🎯"
    );

    companion object {
        fun fromName(name: String?): CustomAbilityEffectType {
            return entries.find { it.name == name } ?: NEUTRALIZE_IMMUNE
        }
    }
}

/**
 * Holds the complete custom ability configuration authored by the player in Phase 5.
 */
data class CustomCharacterSpec(
    val target: CustomAbilityTarget,
    val effectType: CustomAbilityEffectType,
    val percentageOrValue: Int, // e.g., 15%..100%
    val customDescription: String = ""
) {
    fun buildPersianAbilityTitle(): String {
        return "${effectType.persianTitle} (${target.emoji})"
    }

    fun buildShortSummary(): String {
        val pct = percentageOrValue.coerceIn(10, 100).toPersianDigits()
        return when (target) {
            CustomAbilityTarget.SNAKES -> when (effectType) {
                CustomAbilityEffectType.NEUTRALIZE_IMMUNE,
                CustomAbilityEffectType.TRIGGER_PROBABILITY -> "$pct٪ شانس خنثی‌سازی کامل نیش مار و نابودی آن!"
                CustomAbilityEffectType.HALVE_PENALTY -> "با احتمال $pct٪ فقط نصف مسیر مار را پایین می‌رود!"
                CustomAbilityEffectType.REDUCE_EFFECT -> "کاهش $pct٪ از طول سقوط در تمام مارها!"
                CustomAbilityEffectType.INCREASE_CHANCE,
                CustomAbilityEffectType.BONUS_STEPS_BOOST -> "هنگام نیش مار، $pct٪ شانس تبدیل نیش به +۲ خانه جهش!"
            }
            CustomAbilityTarget.LADDERS -> {
                val bonusSteps = (percentageOrValue / 25).coerceIn(1, 4).toPersianDigits()
                "در هر پله با احتمال $pct٪ به اندازهٔ +$bonusSteps خانه بالاتر از انتهای پله می‌پرد!"
            }
            CustomAbilityTarget.SPECIAL_OBSTACLES -> when (effectType) {
                CustomAbilityEffectType.NEUTRALIZE_IMMUNE -> "$pct٪ ایمنی کامل در برابر موانع خطرناک (کرم، باتلاق، چاه و معکوس)!"
                CustomAbilityEffectType.HALVE_PENALTY,
                CustomAbilityEffectType.REDUCE_EFFECT -> "کاهش $pct٪ از جریمهٔ تمام موانع خاص!"
                else -> "در موانع خاص با احتمال $pct٪ به جای جریمه، +۲ خانه پاداش می‌گیرد!"
            }
            CustomAbilityTarget.DICE_AND_LUCK -> {
                "$pct٪ شانس تقویت تاس به اعداد بالا (۴، ۵ و ۶) و بهترین جایزه در خانه شانس!"
            }
            CustomAbilityTarget.OVERALL_MOVEMENT -> {
                val extraSteps = (percentageOrValue / 30).coerceIn(1, 3).toPersianDigits()
                "در هر حرکت با احتمال $pct٪ به اندازهٔ +$extraSteps خانه شتاب اضافه می‌گیرد!"
            }
        }
    }

    fun buildFullDescription(): String {
        val baseRule = buildShortSummary()
        return if (customDescription.isNotBlank()) {
            "$customDescription — ($baseRule)"
        } else {
            "قابلیت سفارشی ساخته‌شده توسط بازیکن: روی بخش «${target.persianTitle}» با نوع اثر «${effectType.persianTitle}» و مقدار دقیق ${percentageOrValue.toPersianDigits()}٪ عمل می‌کند. $baseRule"
        }
    }
}

enum class CharacterAbilityType(
    val persianTitle: String,
    val shortSummary: String,
    val fullDescription: String,
    val activationMode: AbilityActivationMode,
    val cooldownTurns: Int = 3,
    val maxUsesPerMatch: Int = 3
) {
    SPEEDSTER(
        persianTitle = "گام سایهٔ کوتاه‌برد و شتاب جت",
        shortSummary = "هر ۴ نوبت: جابه‌جایی تاکتیکی با رقیب نزدیک (۱ تا ۱۰ خانه) یا شتاب تضمینی ۴ تا ۶!",
        fullDescription = "کفش‌های جت ۳۰٪ شانس تاس‌های بالا را بیشتر می‌کنند. علاوه بر این، با فعال‌سازی قابلیت (هر ۴ نوبت)، اگر رقیبی در فاصلهٔ ۱ تا ۱۰ خانه جلوتر باشد با گام سایهٔ کوتاه‌برد جای خود را با او عوض می‌کند، وگرنه پرتاب بعدی تضمینی ۴ تا ۶ بوده و رقبای عبوری را ۲ خانه عقب می‌راند!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 4,
        maxUsesPerMatch = 3
    ),
    HIGH_PRECISION(
        persianTitle = "چشم عقاب و تیر نشانه‌گیر",
        shortSummary = "هر ۳ نوبت: پرتاب ۲ تاس هم‌زمان + شلیک تیر کندکننده (-۲ خانه) به بازیکن پیشتاز!",
        fullDescription = "وقتی قابلیت دقت بالا شارژ است، هم‌زمان ۲ تاس پرتاب می‌شود تا عدد تاس اول، دوم یا میانگین تقویت‌شده را انتخاب کنید؛ هم‌زمان اگر دو تاس جفت یا مجموع بالای ۸ باشد، تیر نوری به بازیکن پیشتاز خورده و از تاس بعدی او ۲ خانه کم می‌کند!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    GUARDIAN_SHIELD(
        persianTitle = "سپر نورانی محافظ و موج بازتاب",
        shortSummary = "سپر هوشمند در برابر مار، آیتم منفی و شکارچی (بر اساس Defense/Resistance) + موج شوک فعال!",
        fullDescription = "سپر نورانی بر اساس آمار Defense و Resistance از شما در برابر نیش مار، موانع منفی و قابلیت شکارچی محافظت می‌کند (همراه با احتمال دفع بی‌نقص بدون مصرف سپر!). همچنین می‌توانید سپر را دستی فعال کنید تا رقبای نزدیک (تا ۴ خانه) را ۲ خانه به عقب برانید.",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    WORM_IMMUNE(
        persianTitle = "اکسیر ضدزهر و پرتاب هاگ سمی",
        shortSummary = "خنثی‌سازی کامل کرم و باتلاق (+۲ خانه پاداش) + پرتاب هاگ کرم به بازیکن پیشتاز!",
        fullDescription = "مقاومت چندلایه در برابر سموم: طلسم کرم لزج و باتلاق را کاملاً خنثی کرده و +۲ خانه انرژی می‌گیرد و سقوط مارهای سمی را کاهش می‌دهد. با فعال‌سازی دستی (هر ۳ نوبت)، خود را پاک‌سازی کرده (+۲ خانه) و بازیکن پیشتاز را دچار طلسم کرم می‌کند!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    HALF_REVERSE(
        persianTitle = "لنگر تعادل و میدان جاذبه",
        shortSummary = "نصف شدن عقب‌گرد معکوس/باد + ضد جابه‌جایی سایه + محدود کردن تاس پیشتاز به ۴!",
        fullDescription = "در خانهٔ معکوس و باد مخالف فقط نصف مقدار عادی عقب می‌رود و در برابر جابه‌جایی رقبا مقاوم است. با فعال‌سازی لنگر جاذبه (هر ۳ نوبت)، سقف تاس نوبت بعدی بازیکن پیشتاز را به حداکثر ۴ محدود کرده و خود ۲ خانه جلو کشیده می‌شود!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    SNAKE_HUNTER(
        persianTitle = "شکارچی افسانه‌ای (Executioner)",
        shortSummary = "فرود روی خانهٔ رقیب او را نصف شمارهٔ خانه‌اش به عقب می‌راند + ۶۵٪ نابودی مار!",
        fullDescription = "قابلیت شکارچی (Executioner): هرگاه در همان خانه‌ای فرود بیاید که مهرهٔ رقیب قرار دارد، آن بازیکن را شکار کرده و به اندازهٔ نصف شمارهٔ خانه‌اش به عقب پرتاب می‌کند (بدون بازگشت به خانهٔ ۱ و قابل دفع با سپر)! با فعال‌سازی دستی نیز به سمت رقیب خیز برمی‌دارد.",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    LADDER_MAGE(
        persianTitle = "سراب توهم‌زا (Illusionist) و پله اختری",
        shortSummary = "ایجاد بدل توهمی برای فریب حملات رقبا + صعود اختری از نزدیک‌ترین پله!",
        fullDescription = "با فعال‌سازی عصای اختری (هر ۴ نوبت)، یک «بدل توهمی (Holo-Mirage)» به مدت ۲ نوبت ایجاد می‌کند که حملات هدف‌گیر رقبا را بدون تغییر واقعی موقعیت خنثی می‌سازد و هم‌زمان از نزدیک‌ترین پلهٔ پیش رو صعود جادویی می‌کند!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 4,
        maxUsesPerMatch = 3
    ),
    SHADOW_WALKER(
        persianTitle = "جابه‌جایی با نفر اول (هر ۱۰ نوبت اختصاصی)",
        shortSummary = "شارژ در هر ۱۰ نوبت اختصاصی (با مهلت ۲ نوبت اختصاصی) برای تعویض جا با نفر اول زنده!",
        fullDescription = "بر اساس شمارندهٔ «نوبت‌های اختصاصی خود قهرمان» در هر ۱۰ نوبت (۱۰، ۲۰، ۳۰...) شارژ می‌شود و تا ۲ نوبت اختصاصی فرصت استفاده دارد. در صورت عدم استفاده منقضی شده و در نوبت ۲۰ شارژ می‌شود؛ و در صورت استفاده، ۱۰ نوبت اختصاصی بعد دوباره شارژ می‌گردد!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 10,
        maxUsesPerMatch = 5
    ),
    PHOENIX_WINGS(
        persianTitle = "روح اژدها (Dragon Soul) و بال‌های ققنوس",
        shortSummary = "پرواز از چاه و باتلاق + پس از هر مقاومت موفق، ۲ نوبت تقویت روح اژدها (+۲ خانه)!",
        fullDescription = "در برابر چاه، باتلاق و تله‌ها مقاومت کرده و از روی آن‌ها پرواز می‌کند. هر بار که مقاومتش فعال شود (یا قابلیت را دستی فعال کند)، وارد حالت «روح اژدها (Dragon Soul)» شده و در پرتاب بعدی +۲ خانه قدرت حرکت اضافه می‌گیرد!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 4,
        maxUsesPerMatch = 3
    ),
    STORM_LORD(
        persianTitle = "فرمانروای طوفان و صاعقهٔ زنجیره‌ای",
        shortSummary = "در طوفان همیشه +۲ جلو می‌رود + هر ۴ نوبت صاعقهٔ عقب‌راننده روی رقبای پیشتاز!",
        fullDescription = "در خانه‌های طوفان به جای عقب رفتن همیشه +۲ خانه جلو می‌جهد. علاوه بر این، هر ۴ نوبت می‌تواند صاعقهٔ زنجیره‌ای احضار کند تا خودش ۲ خانه جلو برود و رقبای پیشتاز را ۱ تا ۲ خانه به عقب براند!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 4,
        maxUsesPerMatch = 2
    ),
    FORTUNE_STAR(
        persianTitle = "فرشتهٔ محافظ (Guardian Angel) و ستاره اقبال",
        shortSummary = "محافظت از بازیکن عقب‌افتاده یا خود با سپر فرشته (+۲ خانه) + جایزهٔ تاس ۵ و خانه شانس!",
        fullDescription = "خانهٔ شانس همیشه بهترین جایزه را می‌دهد و هر ۳ نوبت تاس ۵ هم جایزه دارد. با فعال‌سازی «فرشتهٔ محافظ»، یک سپر نورانی فرشته به بازیکن عقب‌افتاده (یا خود استلا) می‌دهد که خطر بعدی را دفع کرده و +۲ خانه جهش می‌دهد!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    WIND_RIDER(
        persianTitle = "شبح باد (Phantom) و تندباد آزاد",
        shortSummary = "عبور شبح‌وار از تله‌ها و مارها بدون جریمه + پاداش تاس زوج و تندباد علیه پیشتاز!",
        fullDescription = "قابلیت شبح (Phantom): با فعال‌سازی حالت شبح باد (و یا با شانس ذاتی ۳۵٪)، بدون برخورد از میان تله‌ها، موانع منفی یا نیش مار عبور می‌کند! همچنین در تاس‌های زوج +۱ خانه پاداش گرفته و رقیب پیشتاز را با تندباد عقب می‌راند.",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    ALCHEMIST(
        persianTitle = "فریبکار کیمیاگر (Trickster — انتقال طلسم)",
        shortSummary = "انتقال مستقیم اثر منفی خود به رقیب پیشتاز + تبدیل تاس ۱ و ۲ به ۵ و ۶!",
        fullDescription = "وقتی قابلیت فریبکار کیمیاگر را فعال کنید، هر اثر منفی فعال روی خودتان (کرم، باتلاق، انجماد یا نصف شدن تاس) فوراً به بازیکن پیشتاز منتقل می‌شود! علاوه بر این، تاس ۱ و ۲ شما به ۵ و ۶ تبدیل شده و روی تاس پیشتاز معجون سرب ریخته می‌شود.",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 3,
        maxUsesPerMatch = 3
    ),
    FROST_KNIGHT(
        persianTitle = "شوالیه یخی و زندان بلورین",
        shortSummary = "هر ۵ نوبت: منجمد کردن بازیکن پیشتاز برای ۱ نوبت کامل + سر خوردن از روی چاه!",
        fullDescription = "روی چاه تاریک یخ بسته و سقوط نمی‌کند. با فعال کردن شمشیر یخی (هر ۵ نوبت، حداکثر ۲ بار در بازی)، بازیکن پیشتاز را در زندان بلورین منجمد می‌کند (یا در صورت داشتن مقاومت بالا، تاس بعدی او را نصف می‌نماید)!",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 5,
        maxUsesPerMatch = 2
    ),
    TIME_WEAVER(
        persianTitle = "نگهبان زمان (Time Keeper — بازگردانی نوبت قبل)",
        shortSummary = "بازگردانی موقعیت قبل از سقوط نوبت گذشته + تبدیل خانه معکوس به جهش جلو!",
        fullDescription = "اگر در نوبت قبلی از مار سقوط کرده یا عقب رفته باشید، با فعال‌سازی ساعت شنی فوراً به خانهٔ قبل از سقوط برمی‌گردید! در غیر این صورت، +۲ خانه شتاب گرفته و رقیب پیشتاز را ۲ خانه در زمان به عقب می‌برید.",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 4,
        maxUsesPerMatch = 2
    ),
    EARTH_TITAN(
        persianTitle = "خشم برسرکر (Berserker) و تایتان صخره‌ای",
        shortSummary = "پس از هر نیش مار یا عقب‌گرد، ۲ نوبت خشم برسرکر (+۲ خانه به تاس) + زلزلهٔ ردیفی!",
        fullDescription = "ضد طوفان و چاه بوده و در نیش مار فقط نصف مسیر را پایین می‌رود. پس از هر نیش مار یا حرکت رو به عقب، وارد «خشم برسرکر» شده و ۲ نوبت بعدی +۲ خانه قدرت حرکت بیشتر می‌گیرد! با فعال‌سازی دستی نیز زلزله ایجاد کرده و رقبای نزدیک را عقب می‌راند.",
        activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
        cooldownTurns = 4,
        maxUsesPerMatch = 3
    ),
    CUSTOM_HERO(
        persianTitle = "قابلیت سفارشی قهرمان",
        shortSummary = "قدرت اختصاصی طراحی‌شده توسط بازیکن",
        fullDescription = "این کاراکتر با سیستم ساخت قهرمان سفارشی طراحی شده است.",
        activationMode = AbilityActivationMode.PASSIVE,
        cooldownTurns = 0,
        maxUsesPerMatch = 0
    );

    val badgeEmoji: String
        get() = when (this) {
            SPEEDSTER -> "⚡"
            HIGH_PRECISION -> "🎯"
            GUARDIAN_SHIELD -> "🛡️"
            WORM_IMMUNE -> "🧪"
            HALF_REVERSE -> "⚓"
            SNAKE_HUNTER -> "⚔️"
            LADDER_MAGE -> "🪄"
            SHADOW_WALKER -> "🌑"
            PHOENIX_WINGS -> "🔥"
            STORM_LORD -> "🌩️"
            FORTUNE_STAR -> "🌟"
            WIND_RIDER -> "🌪️"
            ALCHEMIST -> "⚗️"
            FROST_KNIGHT -> "❄️"
            TIME_WEAVER -> "⏳"
            EARTH_TITAN -> "🪨"
            CUSTOM_HERO -> "👑"
        }
}

enum class HeldItemVisual(val persianLabel: String, val iconEmoji: String) {
    JET_BOOTS("کفش‌های جت", "⚡"),
    GOLDEN_BOW("کمان طلایی", "🏹"),
    RADIANT_SHIELD("سپر نورانی", "🛡️"),
    ANTIDOTE_VIAL("اکسیر ضدزهر", "🧪"),
    GRAVITY_ANCHOR("لنگر جاذبه", "⚓"),
    SLAYER_BLADE("شمشیر شکارچی", "⚔️"),
    LADDER_STAFF("عصای جادویی", "🪄"),
    SHADOW_DAGGER("خنجر سایه", "🗡️"),
    PHOENIX_CROWN("تاج ققنوس", "🔥"),
    THUNDER_HAMMER("چکش صاعقه", "🔨"),
    LUCKY_STAR_ORB("گوی ستاره", "🌟"),
    WIND_GLIDER("بال باد", "🌪️"),
    ALCHEMY_FLASK("معجون کیمیا", "⚗️"),
    FROST_SWORD("شمشیر یخی", "❄️"),
    HOURGLASS("ساعت شنی", "⏳"),
    TITAN_GAUNTLET("دستکش صخره‌ای", "🪨");

    companion object {
        fun fromName(name: String?): HeldItemVisual {
            return entries.find { it.name == name } ?: RADIANT_SHIELD
        }
    }
}

data class GameCharacter(
    val id: String,
    val defaultEnglishName: String,
    val persianTitle: String,
    val gender: CharacterGender,
    val primaryColor: Color,
    val secondaryColor: Color,
    val hairColor: Color,
    val skinTone: Color,
    val abilityType: CharacterAbilityType,
    val heldItem: HeldItemVisual,
    val isCustom: Boolean = false,
    val customSpec: CustomCharacterSpec? = null
) {
    val englishName: String get() = defaultEnglishName
    val accentColor: Color get() = primaryColor

    val displayAbilityTitle: String
        get() = customSpec?.buildPersianAbilityTitle() ?: abilityType.persianTitle

    val displayAbilityShortSummary: String
        get() = customSpec?.buildShortSummary() ?: abilityType.shortSummary

    val displayAbilityFullDescription: String
        get() = customSpec?.buildFullDescription() ?: abilityType.fullDescription

    val displayBadgeEmoji: String
        get() = customSpec?.target?.emoji ?: abilityType.badgeEmoji

    val heroBlueprint: HeroBlueprint
        get() = HeroBlueprintCatalog.getBlueprintById(id)
}

object CharacterRoster {
    val allCharacters: List<GameCharacter> = listOf(
        GameCharacter(
            id = "jack",
            defaultEnglishName = "Jack",
            persianTitle = "قهرمان سپردار",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFF0EA5E9),
            secondaryColor = Color(0xFF0369A1),
            hairColor = Color(0xFF451A03),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.GUARDIAN_SHIELD,
            heldItem = HeldItemVisual.RADIANT_SHIELD
        ),
        GameCharacter(
            id = "luna",
            defaultEnglishName = "Luna",
            persianTitle = "جادوگر پله‌ها",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF6B21A8),
            hairColor = Color(0xFF1E1B4B),
            skinTone = Color(0xFFFEF3C7),
            abilityType = CharacterAbilityType.LADDER_MAGE,
            heldItem = HeldItemVisual.LADDER_STAFF
        ),
        GameCharacter(
            id = "axel",
            defaultEnglishName = "Axel",
            persianTitle = "سریع‌تر از آذرخش",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFFFF2A5F),
            secondaryColor = Color(0xFF9F1239),
            hairColor = Color(0xFFFACC15),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.SPEEDSTER,
            heldItem = HeldItemVisual.JET_BOOTS
        ),
        GameCharacter(
            id = "nova",
            defaultEnglishName = "Nova",
            persianTitle = "تیرانداز دقیق",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFB45309),
            hairColor = Color(0xFF9A3412),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.HIGH_PRECISION,
            heldItem = HeldItemVisual.GOLDEN_BOW
        ),
        GameCharacter(
            id = "blade",
            defaultEnglishName = "Blade",
            persianTitle = "شکارچی مارها",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF065F46),
            hairColor = Color(0xFF0F172A),
            skinTone = Color(0xFFFCD34D),
            abilityType = CharacterAbilityType.SNAKE_HUNTER,
            heldItem = HeldItemVisual.SLAYER_BLADE
        ),
        GameCharacter(
            id = "raven",
            defaultEnglishName = "Raven",
            persianTitle = "ملکه سایه‌ها",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF312E81),
            hairColor = Color(0xFF090D16),
            skinTone = Color(0xFFFEF3C7),
            abilityType = CharacterAbilityType.SHADOW_WALKER,
            heldItem = HeldItemVisual.SHADOW_DAGGER
        ),
        GameCharacter(
            id = "kai",
            defaultEnglishName = "Kai",
            persianTitle = "استاد تعادل",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF155E75),
            hairColor = Color(0xFF1E293B),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.HALF_REVERSE,
            heldItem = HeldItemVisual.GRAVITY_ANCHOR
        ),
        GameCharacter(
            id = "ivy",
            defaultEnglishName = "Ivy",
            persianTitle = "نگهبان طبیعت",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFF84CC16),
            secondaryColor = Color(0xFF3F6212),
            hairColor = Color(0xFF15803D),
            skinTone = Color(0xFFFEF3C7),
            abilityType = CharacterAbilityType.WORM_IMMUNE,
            heldItem = HeldItemVisual.ANTIDOTE_VIAL
        ),
        GameCharacter(
            id = "phoenix",
            defaultEnglishName = "Phoenix",
            persianTitle = "بال‌های آتشین",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFF9A3412),
            hairColor = Color(0xFFDC2626),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.PHOENIX_WINGS,
            heldItem = HeldItemVisual.PHOENIX_CROWN
        ),
        GameCharacter(
            id = "storm",
            defaultEnglishName = "Storm",
            persianTitle = "فرمانروای رعد",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF1D4ED8),
            hairColor = Color(0xFFE2E8F0),
            skinTone = Color(0xFFD97706),
            abilityType = CharacterAbilityType.STORM_LORD,
            heldItem = HeldItemVisual.THUNDER_HAMMER
        ),
        GameCharacter(
            id = "stella",
            defaultEnglishName = "Stella",
            persianTitle = "ستاره اقبال",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFFEAB308),
            secondaryColor = Color(0xFF854D0E),
            hairColor = Color(0xFFFDE047),
            skinTone = Color(0xFFFEF3C7),
            abilityType = CharacterAbilityType.FORTUNE_STAR,
            heldItem = HeldItemVisual.LUCKY_STAR_ORB
        ),
        GameCharacter(
            id = "zephyr",
            defaultEnglishName = "Zephyr",
            persianTitle = "بادسوار تندرو",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFF2DD4BF),
            secondaryColor = Color(0xFF115E59),
            hairColor = Color(0xFF0D9488),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.WIND_RIDER,
            heldItem = HeldItemVisual.WIND_GLIDER
        ),
        GameCharacter(
            id = "orion",
            defaultEnglishName = "Orion",
            persianTitle = "کیمیاگر کهکشان",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFFC084FC),
            secondaryColor = Color(0xFF581C87),
            hairColor = Color(0xFF3B0764),
            skinTone = Color(0xFFFCD34D),
            abilityType = CharacterAbilityType.ALCHEMIST,
            heldItem = HeldItemVisual.ALCHEMY_FLASK
        ),
        GameCharacter(
            id = "amber",
            defaultEnglishName = "Amber",
            persianTitle = "شوالیه یخ‌شکن",
            gender = CharacterGender.FEMALE,
            primaryColor = Color(0xFF60A5FA),
            secondaryColor = Color(0xFF1E3A8A),
            hairColor = Color(0xFFDBEAFE),
            skinTone = Color(0xFFFEF3C7),
            abilityType = CharacterAbilityType.FROST_KNIGHT,
            heldItem = HeldItemVisual.FROST_SWORD
        ),
        GameCharacter(
            id = "shadow",
            defaultEnglishName = "Shadow",
            persianTitle = "مسافر زمان",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFFF43F5E),
            secondaryColor = Color(0xFF881337),
            hairColor = Color(0xFF18181B),
            skinTone = Color(0xFFFDE68A),
            abilityType = CharacterAbilityType.TIME_WEAVER,
            heldItem = HeldItemVisual.HOURGLASS
        ),
        GameCharacter(
            id = "atlas",
            defaultEnglishName = "Atlas",
            persianTitle = "تایتان شکست‌ناپذیر",
            gender = CharacterGender.MALE,
            primaryColor = Color(0xFFD97706),
            secondaryColor = Color(0xFF78350F),
            hairColor = Color(0xFF451A03),
            skinTone = Color(0xFFF59E0B),
            abilityType = CharacterAbilityType.EARTH_TITAN,
            heldItem = HeldItemVisual.TITAN_GAUNTLET
        )
    )

    fun getById(id: String, customCharacters: List<GameCharacter> = emptyList()): GameCharacter {
        return customCharacters.find { it.id == id }
            ?: allCharacters.find { it.id == id }
            ?: allCharacters.first()
    }
}
