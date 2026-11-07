package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * =========================================================================================
 * PHASE 1 FOUNDATION: EXTENSIBLE HERO SYSTEM ARCHITECTURE (معماری توسعه‌پذیر سیستم قهرمانان)
 * =========================================================================================
 * This module defines the extensible data-driven Hero System without breaking existing
 * gameplay or UI flows. It establishes:
 * 1. 8 Hero Classes (Primary & Secondary archetypes).
 * 2. 6-Axis Balanced Hero Stat System (Attack, Defense, Resistance, Mobility, Control, Utility).
 * 3. Active & Passive Ability Specifications with Cooldowns, Charges, Target Scopes, and Counterplay.
 * 4. Real Strengths, Real Weaknesses (Trade-offs), and Resistance Profiles.
 * 5. Dedicated Sound & Visual FX Signatures per Hero.
 */

/**
 * 8 Core Hero Classes (کلاس‌بندی قهرمان‌ها).
 * Each hero has a primary class and an optional secondary class for hybrid identity.
 */
enum class HeroClass(
    val englishTitle: String,
    val persianTitle: String,
    val roleDescription: String,
    val badgeEmoji: String,
    val badgeColor: Color
) {
    WARRIOR(
        englishTitle = "Warrior",
        persianTitle = "جنگجو (Warrior)",
        roleDescription = "تهاجمی، نابودکنندهٔ موانع و مارها، و عقب‌رانندهٔ مستقیم مهره‌های رقیب در مسیر",
        badgeEmoji = "⚔️",
        badgeColor = Color(0xFFEF4444)
    ),
    GUARDIAN(
        englishTitle = "Guardian",
        persianTitle = "مدافع و محافظ (Guardian)",
        roleDescription = "سپرهای دفاعی، بازتاب آسیب، محافظت در برابر تله‌ها و ضدحمله به مهاجمان",
        badgeEmoji = "🛡️",
        badgeColor = Color(0xFF0EA5E9)
    ),
    ASSASSIN(
        englishTitle = "Assassin",
        persianTitle = "ترور و شکارچی (Assassin)",
        roleDescription = "ضربه به بازیکن پیشتاز، کمین روی خانه‌ها، سرقت موقعیت و حذف برتری رقبا",
        badgeEmoji = "🗡️",
        badgeColor = Color(0xFF6366F1)
    ),
    TRICKSTER(
        englishTitle = "Trickster",
        persianTitle = "فریب و جابه‌جایی (Trickster)",
        roleDescription = "جابه‌جایی مهره‌ها، تغییر جهت خانه‌ها، تله‌گذاری و دستکاری تاس",
        badgeEmoji = "🎭",
        badgeColor = Color(0xFFF59E0B)
    ),
    MAGE(
        englishTitle = "Mage",
        persianTitle = "جادو و تغییر میدان (Mage)",
        roleDescription = "احضار پله، جابه‌جایی مارها، کیمیاگری تاس و تغییر قوانین خانه‌های خاص",
        badgeEmoji = "🪄",
        badgeColor = Color(0xFFA855F7)
    ),
    SURVIVOR(
        englishTitle = "Survivor",
        persianTitle = "بقا و پایداری (Survivor)",
        roleDescription = "مقاومت بالا در برابر سم، باتلاق، چاه و مارها؛ تبدیل جریمه‌ها به انرژی حرکت",
        badgeEmoji = "🌿",
        badgeColor = Color(0xFF10B981)
    ),
    CONTROLLER(
        englishTitle = "Controller",
        persianTitle = "کنترل‌گر میدان (Controller)",
        roleDescription = "انجماد، طوفان، لنگر جاذبه و کنترل سرعت و موقعیت تمام بازیکنان روی صفحه",
        badgeEmoji = "❄️",
        badgeColor = Color(0xFF38BDF8)
    ),
    SUPPORT(
        englishTitle = "Support",
        persianTitle = "پشتیبان و اقبال (Support)",
        roleDescription = "تقویت شانس، پاک‌سازی طلسم‌ها، ایجاد خانه‌های برکت و پاداش‌های نوبتی",
        badgeEmoji = "🌟",
        badgeColor = Color(0xFFEAB308)
    )
}

/**
 * 6-Axis Balanced Hero Stat System (سیستم آمار و قدرت متعادل).
 * Each stat is rated on a 1..10 scale, governed by a balanced point budget (Total = 36 points).
 */
data class HeroStats(
    val attack: Int,      // قدرت تهاجمی (Attack): 1..10
    val defense: Int,     // قدرت دفاعی (Defense): 1..10
    val resistance: Int,  // مقاومت در برابر قابلیت‌ها و طلسم‌ها (Resistance): 1..10
    val mobility: Int,    // تحرک و سرعت پیشروی (Mobility): 1..10
    val control: Int,     // کنترل بازی و رقبا (Control): 1..10
    val utility: Int      // کاربرد، شانس و پشتیبانی (Utility): 1..10
) {
    val totalScore: Int
        get() = attack + defense + resistance + mobility + control + utility

    /**
     * Computes passive resistance reduction against incoming enemy hero abilities (0%..45%).
     */
    val enemyAbilityMitigationChancePercent: Int
        get() = (resistance * 4).coerceIn(4, 45)

    /**
     * Computes the effective Shield / Defense success probability (in %) based on:
     * - Configurable base defense rate (`baseConfiguredPercent`, e.g., 75%..85%)
     * - Hero's `defense` (1..10) and `resistance` (1..10) stats
     * - Threat category (SNAKE, NEGATIVE_OBSTACLE, EXECUTIONER_OR_ABILITY)
     */
    fun computeShieldDefenseChance(
        baseConfiguredPercent: Int,
        threatCategory: ShieldThreatCategory
    ): Int {
        val statBonus = when (threatCategory) {
            ShieldThreatCategory.SNAKE -> (defense - 5) * 3 + (resistance - 5) * 2
            ShieldThreatCategory.NEGATIVE_OBSTACLE -> (resistance - 5) * 3 + (defense - 5) * 2
            ShieldThreatCategory.EXECUTIONER_OR_ABILITY -> (defense - 5) * 2 + (resistance - 5) * 3
        }
        return (baseConfiguredPercent + statBonus).coerceIn(25, 96)
    }

    /**
     * Chance (in %) that a successful Shield block is a Critical Parry ("دفع بی‌نقص بدون مصرف سپر"),
     * preserving the shield charge without consuming it.
     */
    val criticalParryPreserveShieldPercent: Int
        get() = ((defense * 2) + resistance).coerceIn(8, 30)
}

enum class ShieldThreatCategory(val persianLabel: String) {
    SNAKE("نیش مار"),
    NEGATIVE_OBSTACLE("آیتم و مانع منفی"),
    EXECUTIONER_OR_ABILITY("قابلیت شکارچی و تهاجم رقیب")
}

/**
 * Defines who or what an ability affects on the board.
 */
enum class HeroAbilityTargetScope(val persianLabel: String) {
    SELF("خود قهرمان"),
    LEADER_OPPONENT("بازیکن پیشتاز"),
    SELECTED_OPPONENT("بازیکن رقیب انتخابی"),
    NEARBY_OPPONENTS("رقبای نزدیک (شعاع ۶ خانه)"),
    ALL_OPPONENTS("تمام رقبا در صفحه"),
    BOARD_LINK_OR_TILE("مار، پله یا خانهٔ صفحه")
}

/**
 * Defines how other players can defend against or counter this hero's ability.
 */
enum class CounterplayMechanism(
    val persianTitle: String,
    val description: String
) {
    SHIELD_BLOCKABLE(
        persianTitle = "قابل دفع با سپر محافظ",
        description = "سپر نورانی (Guardian Shield) یا مقاومت دفاعی بالا این اثر را کاملاً خنثی یا به خود مهاجم بازتاب می‌دهد."
    ),
    HEAVY_ANCHOR_IMMUNE(
        persianTitle = "بی‌اثر روی قهرمانان سنگین‌وزن و لنگرها",
        description = "قهرمانان دارای لنگر تعادل (Kai) یا زره صخره‌ای (Atlas) در برابر جابه‌جایی اجباری و پرتاب شدن مقاوم‌اند."
    ),
    PURGEABLE_DEBUFF(
        persianTitle = "قابل پاک‌سازی با تاس ۶، خانه شانس یا زمان",
        description = "بازیکن هدف می‌تواند با آوردن تاس ۶، افتادن روی خانه شانس، یا قابلیت پاک‌سازی، طلسم را فوراً بشکند."
    ),
    POSITIONAL_DODGE(
        persianTitle = "قابل پیشگیری با فاصله‌گیری موقعیتی",
        description = "فقط روی بازیکنان در محدوده یا ردیف هدف اثر می‌گذارد؛ حضور روی خانه‌های امن پله یا فاصلهٔ بیش از برد، اثر را خنثی می‌کند."
    ),
    TEMPORARY_DURATION(
        persianTitle = "محدود به زمان و قابل دور زدن",
        description = "اثر ایجادشده روی صفحه دائمی نیست و پس از ۲ نوبت از بین می‌رود یا رقیب می‌تواند از آن به نفع خود عبور کند."
    )
}

/**
 * Specification for a Hero's Primary Active Ability or Secondary Passive Ability.
 */
data class HeroAbilitySpec(
    val id: String,
    val persianName: String,
    val englishName: String,
    val activationMode: AbilityActivationMode,
    val targetScope: HeroAbilityTargetScope,
    val description: String,
    val opponentImpactDescription: String,
    val baseSuccessChancePercent: Int = 100,
    val cooldownTurns: Int = 3,
    val maxUsesPerMatch: Int = 3,
    val counterplay: CounterplayMechanism
)

/**
 * Defines a Hero's Resistance Profile against specific board hazards & rival classes.
 */
data class HeroResistanceProfile(
    val snakeDropReductionPercent: Int = 0,       // کاهش طول سقوط مار
    val reverseAndWindResistancePercent: Int = 0, // مقاومت در برابر خانه معکوس و باد مخالف
    val statusDebuffResistancePercent: Int = 0,   // مقاومت در برابر کرم لزج، باتلاق و انجماد
    val displacementResistancePercent: Int = 0,   // مقاومت در برابر جابه‌جایی سایه و طوفان رقبا
    val vulnerableToClass: HeroClass              // کلاسی که نقطه ضعف مستقیم این قهرمان است
)

/**
 * Dedicated Audio & Visual FX Signature for a Hero.
 */
data class HeroAudioVisualSignature(
    val soundCueId: String,
    val soundDescription: String,
    val animationStyleName: String,
    val animationDescription: String,
    val auraPrimaryColor: Color,
    val auraSecondaryColor: Color,
    val particleSymbol: String
)

/**
 * Complete Extensible Blueprint for a Hero in the game.
 */
data class HeroBlueprint(
    val id: String,
    val englishName: String,
    val persianTitle: String,
    val loreDescription: String,
    val gender: CharacterGender,
    val heldItem: HeldItemVisual,
    val primaryClass: HeroClass,
    val secondaryClass: HeroClass?,
    val stats: HeroStats,
    val primaryAbility: HeroAbilitySpec,
    val passiveAbilityTitle: String,
    val passiveAbilityDescription: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val resistanceProfile: HeroResistanceProfile,
    val fxSignature: HeroAudioVisualSignature
)

/**
 * Registry of all 16Redesigned Hero Blueprints (آماده برای فاز پیاده‌سازی و توسعهٔ قهرمانان جدید).
 */
object HeroBlueprintCatalog {

    val allHeroBlueprints: List<HeroBlueprint> = listOf(
        // 1. JACK — Guardian / Warrior
        HeroBlueprint(
            id = "jack",
            englishName = "Jack",
            persianTitle = "شوالیه سپر طلایی",
            loreDescription = "فرمانده گارد سلطنتی که سپر نورانی‌اش نه‌تنها نیش مرگبارترین افعی‌ها را دفع می‌کند، بلکه موج ضربهٔ آن مهاجمان نزدیک را به عقب می‌راند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.RADIANT_SHIELD,
            primaryClass = HeroClass.GUARDIAN,
            secondaryClass = HeroClass.WARRIOR,
            stats = HeroStats(attack = 5, defense = 9, resistance = 8, mobility = 4, control = 5, utility = 5),
            primaryAbility = HeroAbilitySpec(
                id = "jack_aegis_pulse",
                persianName = "گنبد سپر نورانی و موج بازتاب",
                englishName = "Radiant Aegis & Shockwave",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.NEARBY_OPPONENTS,
                description = "سپر نورانی را برای ۲ نوبت فعال می‌کند (دفع کامل ۱ نیش مار، خانه معکوس یا قابلیت تهاجمی رقیب). هنگام شکستن سپر، موج نور رقبای تا فاصلهٔ ۴ خانه را ۲ خانه به عقب می‌راند.",
                opponentImpactDescription = "اگر رقیبی به جک حمله کند یا در شعاع ۴ خانه‌ای انفجار سپر باشد، ۲ خانه به عقب پرتاب می‌شود.",
                baseSuccessChancePercent = 100,
                cooldownTurns = 4,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.POSITIONAL_DODGE
            ),
            passiveAbilityTitle = "زره 반사 (نگهبان استوار)",
            passiveAbilityDescription = "در صورت ایستادن روی خانه‌ای که هم‌زمان مهرهٔ رقیب حضور دارد، جک ۱ خانه جلوتر می‌ایستد؛ اما به دلیل زره سنگین، در خانه‌های باد موافق ۲ خانه کمتر پرتاب می‌شود.",
            strengths = listOf(
                "ایمنی مطمئن در برابر نیش مارهای بلند و حملات مستقیم رقبا",
                "ضدحملهٔ خودکار (موج شوک ۲ خانه‌ای) علیه رقبای نزدیک"
            ),
            weaknesses = listOf(
                "تحرک پایهٔ کندتر: به دلیل زره سنگین، از باد موافق بهرهٔ کمتری می‌برد و هرگز بیشتر از ۶ خانه در یک پرتاب عادی حرکت نمی‌کند",
                "وقتی سپر در حال شارژ (Cooldown) است، در برابر تله‌های جادویی آسیب‌پذیرتر می‌شود"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 25,
                reverseAndWindResistancePercent = 35,
                statusDebuffResistancePercent = 30,
                displacementResistancePercent = 50,
                vulnerableToClass = HeroClass.MAGE
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_jack_aegis",
                soundDescription = "طنین فلزی سپر سلطنتی همراه با موج کریستالی",
                animationStyleName = "Golden Hexagonal Dome",
                animationDescription = "حلقهٔ شش‌ضلعی طلایی-آبی دور مهره که هنگام دفع خطر با موج نور منفجر می‌شود",
                auraPrimaryColor = Color(0xFF0EA5E9),
                auraSecondaryColor = Color(0xFFFACC15),
                particleSymbol = "🛡️"
            )
        ),

        // 2. LUNA — Mage / Controller
        HeroBlueprint(
            id = "luna",
            englishName = "Luna",
            persianTitle = "معمار پله‌های اختری",
            loreDescription = "جادوگر ستاره‌شناس که می‌تواند پل‌های نوری را در مسیر خود احضار کند و هم‌زمان پلهٔ پیش روی رقیب پیشتاز را طلسم نماید.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.LADDER_STAFF,
            primaryClass = HeroClass.MAGE,
            secondaryClass = HeroClass.CONTROLLER,
            stats = HeroStats(attack = 4, defense = 4, resistance = 5, mobility = 8, control = 7, utility = 8),
            primaryAbility = HeroAbilitySpec(
                id = "luna_astral_stairway",
                persianName = "سراب توهم‌زا (Illusionist) و پله اختری",
                englishName = "Illusionist Mirage & Astral Stairway",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.BOARD_LINK_OR_TILE,
                description = "یک «بدل توهمی (Holo-Mirage)» به مدت ۲ نوبت روی صفحه ایجاد می‌کند که حملات رقبا (شکارچی، صاعقه و انجماد) را به خود جذب کرده و تغییر واقعی در موقعیت لونا ایجاد نمی‌کند؛ هم‌زمان اگر پله‌ای در ۶ خانهٔ جلوتر باشد تا نیمهٔ آن صعود اختری می‌کند!",
                opponentImpactDescription = "فریب دادن قابلیت‌های هدف‌گیر رقبا با بدل توهمی و قفل اختری پله برای ۱ دور.",
                baseSuccessChancePercent = 88,
                cooldownTurns = 4,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.TEMPORARY_DURATION
            ),
            passiveAbilityTitle = "هم‌نوایی با نردبان‌ها و هالهٔ سراب",
            passiveAbilityDescription = "در هر صعود عادی از نردبان، +۱ خانه بالاتر فرود می‌آید؛ اما در صورت نیش خوردن از مار، به دلیل نداشتن زره فیزیکی ۲ خانه بیشتر سقوط می‌کند.",
            strengths = listOf(
                "صعود جهشی بدون نیاز به افتادن دقیق روی پایهٔ پله",
                "محروم کردن بازیکن پیشتاز از پله‌های کلیدی با قفل اختری"
            ),
            weaknesses = listOf(
                "دفاع فیزیکی پایین: در صورت نیش مار، ۲ خانه بیشتر از دم مار به عقب می‌افتد",
                "محدودیت ۲ بار استفاده در کل بازی و Cooldown چهار نوبتی"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = -15,
                reverseAndWindResistancePercent = 15,
                statusDebuffResistancePercent = 25,
                displacementResistancePercent = 10,
                vulnerableToClass = HeroClass.ASSASSIN
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_luna_stairway",
                soundDescription = "آرپژ هارپ کیهانی و زنگولهٔ بلورین",
                animationStyleName = "Starlight Ascent Beam",
                animationDescription = "ستون نور بنفش-طلایی زیر مهره و قفل بنفش درخشان روی پلهٔ هدف",
                auraPrimaryColor = Color(0xFFA855F7),
                auraSecondaryColor = Color(0xFFFDE047),
                particleSymbol = "🪄"
            )
        ),

        // 3. AXEL — Warrior / Mobility (Speedster)
        HeroBlueprint(
            id = "axel",
            englishName = "Axel",
            persianTitle = "صاعقهٔ سرخ پیست",
            loreDescription = "قهرمان تندرو با چکمه‌های جت پلاسمایی که هنگام عبور از کنار رقبا، موج باد ایجاد کرده و تعادل آن‌ها را به هم می‌زند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.JET_BOOTS,
            primaryClass = HeroClass.WARRIOR,
            secondaryClass = HeroClass.TRICKSTER,
            stats = HeroStats(attack = 7, defense = 3, resistance = 4, mobility = 10, control = 5, utility = 7),
            primaryAbility = HeroAbilitySpec(
                id = "axel_sonic_overdrive",
                persianName = "گام سایهٔ کوتاه‌برد (Shadow Step) و شتاب مافوق صوت",
                englishName = "Tactical Shadow Step & Sonic Overdrive",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.NEARBY_OPPONENTS,
                description = "اگر رقیبی در فاصلهٔ نزدیک (۱ تا ۱۰ خانه جلوتر) باشد، اکسل با گام سایهٔ کوتاه‌برد جای خود را با او عوض می‌کند! اگر رقیبی در این برد نباشد، پرتاب بعدی تضمین‌شده ۴ تا ۶ بوده و هر رقیب عبوری را ۲ خانه به عقب پرتاب می‌کند.",
                opponentImpactDescription = "جابه‌جایی تاکتیکی کوتاه‌برد با رقیب نزدیک یا عقب راندن ۲ خانه‌ای رقبای در مسیر.",
                baseSuccessChancePercent = 92,
                cooldownTurns = 4,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.HEAVY_ANCHOR_IMMUNE
            ),
            passiveAbilityTitle = "موتور جت پلاسمایی",
            passiveAbilityDescription = "۳۰٪ شانس پایه برای تقویت تاس به اعداد بالا (۴ تا ۶)؛ اما به دلیل سرعت بیش از حد، در خانهٔ «معکوس» ۱ خانه جریمهٔ لغزش بیشتر می‌بیند.",
            strengths = listOf(
                "بالاترین سرعت پیشروی در صفحه و قابلیت سبقت تهاجمی",
                "ضربه زدن به رقبایی که در مسیر حرکت او قرار دارند"
            ),
            weaknesses = listOf(
                "ترمز ضعیف در تله‌ها: در خانهٔ معکوس ۱ خانه بیشتر به عقب برمی‌گردد و در باتلاق شتابش کاملاً صفر می‌شود",
                "در نزدیکی خانهٔ آخر (نیاز به عدد دقیق)، تاس‌های بالا می‌تواند باعث رد کردن خانهٔ ۱۰۰ شود"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = -20,
                statusDebuffResistancePercent = 10,
                displacementResistancePercent = 20,
                vulnerableToClass = HeroClass.CONTROLLER
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_axel_jetboost",
                soundDescription = "غرش جت پلاسمایی و شکسته شدن دیوار صوتی",
                animationStyleName = "Crimson Lightning Trail",
                animationDescription = "رد آذرخش سرخ-طلایی پشت سر مهره و موج ضربه هنگام عبور از رقبا",
                auraPrimaryColor = Color(0xFFFF2A5F),
                auraSecondaryColor = Color(0xFFFACC15),
                particleSymbol = "⚡"
            )
        ),

        // 4. NOVA — Assassin / Trickster
        HeroBlueprint(
            id = "nova",
            englishName = "Nova",
            persianTitle = "کماندار ستاره‌شکن",
            loreDescription = "تیرانداز افسانه‌ای با کمان طلایی که نه‌تنها مسیر حرکت خود را با دو تاس نشانه می‌گیرد، بلکه یک تیر نوری هم به سمت رقیب پیشتاز شلیک می‌کند.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.GOLDEN_BOW,
            primaryClass = HeroClass.ASSASSIN,
            secondaryClass = HeroClass.TRICKSTER,
            stats = HeroStats(attack = 8, defense = 4, resistance = 4, mobility = 7, control = 6, utility = 7),
            primaryAbility = HeroAbilitySpec(
                id = "nova_twin_star_shot",
                persianName = "تیر دوقلوی خورشیدی و نشانه‌گیری پیشتاز",
                englishName = "Twin-Star Volley & Pinning Arrow",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "۲ تاس هم‌زمان پرتاب می‌کند و بازیکن یکی از دو عدد (یا میانگین گردشده به بالا) را انتخاب می‌کند؛ هم‌زمان اگر دو تاس جفت بیایند یا مجموعشان بالای ۸ باشد، تیر نوری به بازیکن پیشتاز خورده و از تاس بعدی او ۲ واحد کم می‌کند!",
                opponentImpactDescription = "بازیکن پیشتاز هدف «تیر نوری» قرار گرفته و در نوبت بعدی‌اش ۲ خانه کمتر حرکت می‌کند.",
                baseSuccessChancePercent = 95,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.SHIELD_BLOCKABLE
            ),
            passiveAbilityTitle = "چشم عقاب در پایان مسیر",
            passiveAbilityDescription = "در ۶ خانهٔ پایانی صفحه (۹۴ تا ۹۹)، اگر تاس بیشتر از خانهٔ هدف بیاید، با احتمال ۵۰٪ تیر را به لبهٔ خانهٔ آخر قلاب کرده و در جای خود می‌ماند؛ اما در حالت عادی دیگر نمی‌تواند مجموع ۱۲ خانهٔ کامل دو تاس را بدون محدودیت بردارد (حداکثر ۸ خانه در حالت ترکیبی).",
            strengths = listOf(
                "انعطاف فوق‌العاده برای فرار از مارها و فرود دقیق روی پله‌ها",
                "تضعیف سرعت بازیکن پیشتاز از راه دور"
            ),
            weaknesses = listOf(
                "حذف جهش افراطی ۱۲ خانه‌ای قبلی (سقف حرکت ترکیبی حداکثر ۸ خانه برای حفظ تعادل)",
                "زره دفاعی کم در برابر حملات سایه و طوفان"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = 10,
                statusDebuffResistancePercent = 15,
                displacementResistancePercent = 10,
                vulnerableToClass = HeroClass.WARRIOR
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_nova_bowshot",
                soundDescription = "کشیده شدن زه کمان طلایی و سوت تیر شهاب‌سنگی",
                animationStyleName = "Solar Crosshair & Arrow Arc",
                animationDescription = "نشانگر هدف طلایی روی خانهٔ مقصد و پرتاب تیر نورانی به سمت رقیب پیشتاز",
                auraPrimaryColor = Color(0xFFF59E0B),
                auraSecondaryColor = Color(0xFFFEF08A),
                particleSymbol = "🏹"
            )
        ),

        // 5. BLADE — Warrior / Assassin
        HeroBlueprint(
            id = "blade",
            englishName = "Blade",
            persianTitle = "شکارچی افعی‌های باستانی",
            loreDescription = "جنگجوی چابکی که به جای انتظار برای نیش خوردن، می‌تواند مارها را رام کرده و به سمت رقبای خود هدایت کند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.SLAYER_BLADE,
            primaryClass = HeroClass.WARRIOR,
            secondaryClass = HeroClass.ASSASSIN,
            stats = HeroStats(attack = 9, defense = 6, resistance = 6, mobility = 6, control = 5, utility = 4),
            primaryAbility = HeroAbilitySpec(
                id = "blade_serpent_ambush",
                persianName = "شکارچی (Executioner) و خیزش شمشیر",
                englishName = "Executioner Hunt & Serpent Cleave",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.NEARBY_OPPONENTS,
                description = "هرگاه بلید در همان خانه‌ای فرود بیاید که مهرهٔ رقیب قرار دارد، قابلیت «شکارچی (Executioner)» فعال شده و رقیب را به اندازهٔ نصف شمارهٔ خانه‌اش (گردشده به پایین) به عقب می‌راند (بدون بازگشت به خانهٔ ۱ و قابل دفع با سپر)! با فعال‌سازی دستی، بلید +۲ خانه به سمت نزدیک‌ترین رقیب خیز برمی‌دارد تا او را شکار کند.",
                opponentImpactDescription = "در صورت هم‌خانه شدن با بلید، بازیکن شکار شده و به اندازهٔ نصف خانهٔ فعلی‌اش به عقب پرتاب می‌شود.",
                baseSuccessChancePercent = 100,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.SHIELD_BLOCKABLE
            ),
            passiveAbilityTitle = "غریزهٔ شکارچی (Executioner) و شمشیر افعی‌کش",
            passiveAbilityDescription = "فرود روی خانهٔ رقیب فوراً او را شکار کرده و نصف شمارهٔ خانه‌اش عقب می‌راند (هرگز به خانهٔ شروع ۱ برنمی‌گرداند). همچنین روی سر مار با احتمال ۶۵٪ مار را نابود یا سقوطش را ۷۵٪ کم می‌کند.",
            strengths = listOf(
                "تبدیل شدن از یک کاراکتر صرفاً منفعل به شکارچی تهاجمی با قابلیت تله‌گذاری",
                "بالاترین شانس بقا در برابر مارهای خطرناک صفحه"
            ),
            weaknesses = listOf(
                "آسیب‌پذیری کامل در برابر چاه تاریک، باتلاق و طوفان‌های جادویی",
                "احتمال ۴۰٪ خطا در بریدن مارهای غول‌پیکر (سقوط کامل در صورت عدم موفقیت)"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 60,
                reverseAndWindResistancePercent = 10,
                statusDebuffResistancePercent = 5,
                displacementResistancePercent = 20,
                vulnerableToClass = HeroClass.MAGE
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_blade_slash",
                soundDescription = "صدای برش تند شمشیر زمردین و غرش افعی",
                animationStyleName = "Emerald Crescent Slash",
                animationDescription = "هلال نور سبز زمردین که سر مار را می‌برد و نشان دندان مار روی خانهٔ تله",
                auraPrimaryColor = Color(0xFF10B981),
                auraSecondaryColor = Color(0xFF065F46),
                particleSymbol = "⚔️"
            )
        ),

        // 6. RAVEN — Assassin / Trickster
        HeroBlueprint(
            id = "raven",
            englishName = "Raven",
            persianTitle = "ملکهٔ گام‌های سایه",
            loreDescription = "استاد نفوذ در تاریکی که از میان سایه‌ها با رقبا جابه‌جا می‌شود، اما استفاده از جادوی سایه نیازمند شرایط تاکتیکی و رعایت برد است.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.SHADOW_DAGGER,
            primaryClass = HeroClass.ASSASSIN,
            secondaryClass = HeroClass.TRICKSTER,
            stats = HeroStats(attack = 8, defense = 3, resistance = 5, mobility = 8, control = 7, utility = 5),
            primaryAbility = HeroAbilitySpec(
                id = "raven_shadow_rift_swap",
                persianName = "جابه‌جایی با نفر اول (شارژ هر ۱۰ نوبت اختصاصی)",
                englishName = "Leader Shadow Swap (10-Personal-Turn Cycle)",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "بر اساس «نوبت اختصاصی ریون» در هر ۱۰ نوبت شارژ می‌شود (نوبت ۱۰، ۲۰، ۳۰...) و پس از شارژ تا ۲ نوبت اختصاصی فرصت استفاده دارد. در لحظهٔ اجرا موقعیت زندهٔ نفر اول را بررسی کرده و جای ریون را با بازیکن پیشتاز عوض می‌کند! در صورت عدم استفاده در ۲ نوبت، منقضی شده و در نوبت اختصاصی ۲۰ دوباره شارژ می‌شود (و پس از استفاده، ۱۰ نوبت اختصاصی بعد شارژ می‌گردد).",
                opponentImpactDescription = "تعویض مستقیم مکان با بازیکن نفر اول در لحظهٔ اجرا (قابل دفع با سپر محافظ یا لنگر گرانشی).",
                baseSuccessChancePercent = 100,
                cooldownTurns = 10,
                maxUsesPerMatch = 5,
                counterplay = CounterplayMechanism.SHIELD_BLOCKABLE
            ),
            passiveAbilityTitle = "شنل تاریکی و شمارندهٔ نوبت اختصاصی",
            passiveAbilityDescription = "وقتی از نفر اول عقب‌تر است، در تاس‌های فرد +۱ خانه سرعت سایه می‌گیرد؛ وقتی خودش نفر اول بازی است، جابه‌جایی با نفر اول قفل است.",
            strengths = listOf(
                "بهترین قهرمان برای بازگشت به بازی (Comeback) هنگام عقب افتادن",
                "ایجاد فشار روانی دائمی روی بازیکن پیشتاز"
            ),
            weaknesses = listOf(
                "بی‌استفاده شدن قابلیت جابه‌جایی زمانی که خودش نفر اول بازی است",
                "برد محدود (حداکثر ۲۵ خانه) و خنثی شدن توسط سپر جک یا لنگر کای/اطلس"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = 15,
                statusDebuffResistancePercent = 20,
                displacementResistancePercent = 30,
                vulnerableToClass = HeroClass.GUARDIAN
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_raven_shadowrift",
                soundDescription = "زمزمهٔ پرتال تاریک و جهش خنجر سایه",
                animationStyleName = "Void Portal Swap",
                animationDescription = "دو گرداب بنفش-سیاه زیر پای ریون و هدف که مهره‌ها را در هم فرومی‌برد",
                auraPrimaryColor = Color(0xFF6366F1),
                auraSecondaryColor = Color(0xFF1E1B4B),
                particleSymbol = "🌑"
            )
        ),

        // 7. KAI — Controller / Guardian (Redesigned from weak HALF_REVERSE)
        HeroBlueprint(
            id = "kai",
            englishName = "Kai",
            persianTitle = "ارباب لنگر جاذبه",
            loreDescription = "نگهبان اقیانوس‌های عمیق که لنگر گرانشی او نه‌تنها مانع عقب‌گرد خودش می‌شود، بلکه میدان جاذبهٔ سنگینی دور رقبا ایجاد می‌کند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.GRAVITY_ANCHOR,
            primaryClass = HeroClass.CONTROLLER,
            secondaryClass = HeroClass.GUARDIAN,
            stats = HeroStats(attack = 5, defense = 8, resistance = 7, mobility = 4, control = 8, utility = 4),
            primaryAbility = HeroAbilitySpec(
                id = "kai_gravity_well_anchor",
                persianName = "میدان لنگر جاذبه",
                englishName = "Gravity Anchor Field",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "لنگر جاذبه را به سمت بازیکن پیشتاز (یا رقیب جلوتر) پرتاب می‌کند: سقف تاس نوبت بعدی آن رقیب به حداکثر ۴ محدود می‌شود و خود کای ۲ خانه به سمت او کشیده می‌شود!",
                opponentImpactDescription = "سنگین کردن مهرهٔ رقیب پیشتاز به طوری که در نوبت بعدی نمی‌تواند عدد ۵ یا ۶ بیاورد.",
                baseSuccessChancePercent = 90,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.PURGEABLE_DEBUFF
            ),
            passiveAbilityTitle = "تعادل گرانشی پایدار",
            passiveAbilityDescription = "در خانهٔ معکوس و باد مخالف فقط نصف مقدار عادی به عقب برمی‌گردد و در برابر «جابه‌جایی سایهٔ ریون» کاملاً مقاوم است؛ اما به دلیل وزن لنگر، در صعود از نردبان‌ها ۱ خانه پایین‌تر از راس پله فرود می‌آید.",
            strengths = listOf(
                "بازطراحی کامل از یک قابلیت منفعل کم‌اثر به کنترل‌گر قدرتمند سرعت بازیکن پیشتاز",
                "ضدحملهٔ طبیعی در برابر ریون (Shadow Walker) و خانه‌های معکوس"
            ),
            weaknesses = listOf(
                "وزن زیاد روی پله‌ها: در صعود از نردبان‌های بلند ۱ خانه کمتر از انتهای نردبان بالا می‌رود",
                "تحرک پایهٔ متوسط در مسیرهای صاف"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 15,
                reverseAndWindResistancePercent = 60,
                statusDebuffResistancePercent = 25,
                displacementResistancePercent = 85,
                vulnerableToClass = HeroClass.SURVIVOR
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_kai_gravityanchor",
                soundDescription = "فرود سنگین لنگر فلزی در آب عمیق و پالس گرانشی",
                animationStyleName = "Cyan Gravity Rings",
                animationDescription = "حلقه‌های گرانشی فیروزه‌ای سنگین که دور رقیب هدف می‌چرخند",
                auraPrimaryColor = Color(0xFF06B6D4),
                auraSecondaryColor = Color(0xFF155E75),
                particleSymbol = "⚓"
            )
        ),

        // 8. IVY — Survivor / Controller (Redesigned from situational WORM_IMMUNE)
        HeroBlueprint(
            id = "ivy",
            englishName = "Ivy",
            persianTitle = "ملکهٔ زهر و کیمیای سبز",
            loreDescription = "نگهبان جنگل اسرارآمیز که سم کرم‌ها و مارها را به اکسیر حیات تبدیل می‌کند و می‌تواند هاگ‌های خواب‌آور در مسیر رقبا بپاشاند.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.ANTIDOTE_VIAL,
            primaryClass = HeroClass.SURVIVOR,
            secondaryClass = HeroClass.CONTROLLER,
            stats = HeroStats(attack = 6, defense = 6, resistance = 9, mobility = 5, control = 5, utility = 5),
            primaryAbility = HeroAbilitySpec(
                id = "ivy_bio_venom_flask",
                persianName = "اکسیر زیست‌تاب و پرتاب هاگ سمی",
                englishName = "Bio-Venom Flask & Spore Trap",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "تمام طلسم‌های منفی خود را پاک کرده و +۲ خانه شتاب حیاتی می‌گیرد؛ هم‌زمان یک ویال «هاگ کرم لزج» به خانهٔ بازیکن پیشتاز پرتاب می‌کند که به مدت ۱ نوبت احتمال تاس ۱ و ۲ او را افزایش می‌دهد!",
                opponentImpactDescription = "اعمال طلسم کرم لزج (کاهش شانس اعداد بالای تاس) برای ۱ نوبت روی بازیکن پیشتاز.",
                baseSuccessChancePercent = 88,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.PURGEABLE_DEBUFF
            ),
            passiveAbilityTitle = "هم‌زیستی با طبیعت",
            passiveAbilityDescription = "کاملاً ضد طلسم کرم لزج است و روی خانهٔ کرم یا باتلاق به جای جریمه +۲ خانه انرژی می‌گیرد؛ اما در برابر طوفان و صاعقه (Storm) ۱ خانه بیشتر به عقب رانده می‌شود.",
            strengths = listOf(
                "بازطراحی کامل از یک قابلیت وابسته به شانس افتادن روی کرم به یک قهرمان مستقل با قابلیت زهرپاشی به رقبا",
                "بالاترین مقاومت در برابر سموم، کرم و باتلاق"
            ),
            weaknesses = listOf(
                "آسیب‌پذیری در برابر عناصر هوا و رعد (طوفان و باد شدید)",
                "نداشتن سپر در برابر نیش مستقیم مارهای غیرسمی"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 10,
                reverseAndWindResistancePercent = -15,
                statusDebuffResistancePercent = 90,
                displacementResistancePercent = 15,
                vulnerableToClass = HeroClass.CONTROLLER
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_ivy_alchemypop",
                soundDescription = "جوشش معجون گیاهی و پخش شدن هاگ‌های نورانی",
                animationStyleName = "Emerald Spore Cloud",
                animationDescription = "ابر سبز فسفری درخشان که دور مهرهٔ آیوی را شفا داده و به سمت رقیب پرتاب می‌شود",
                auraPrimaryColor = Color(0xFF84CC16),
                auraSecondaryColor = Color(0xFF15803D),
                particleSymbol = "🧪"
            )
        ),

        // 9. PHOENIX — Mobility / Support
        HeroBlueprint(
            id = "phoenix",
            englishName = "Phoenix",
            persianTitle = "امپراتریس بال‌های آتشین",
            loreDescription = "پرندهٔ جاودان آتش که از فراز چاه‌ها و باتلاق‌ها پرواز می‌کند و از خاکستر سقوط خود با قدرت بیشتر برمی‌خیزد.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.PHOENIX_CROWN,
            primaryClass = HeroClass.SURVIVOR,
            secondaryClass = HeroClass.WARRIOR,
            stats = HeroStats(attack = 6, defense = 5, resistance = 6, mobility = 9, control = 4, utility = 6),
            primaryAbility = HeroAbilitySpec(
                id = "phoenix_flame_rebirth",
                persianName = "روح اژدها (Dragon Soul) و بال‌های ققنوس",
                englishName = "Dragon Soul Empowerment & Flame Surge",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.SELF,
                description = "در برابر چاه، باتلاق و اثرات منفی مقاومت می‌کند و هر بار که مقاومتش فعال شود، به مدت ۲ نوبت «روح اژدها (+۲ خانه قدرت حرکت اضافه)» می‌گیرد! با فعال‌سازی دستی نیز بال‌های آتشین را باز کرده و برای ۲ نوبت روح اژدها و ایمنی پرواز را فعال می‌نماید.",
                opponentImpactDescription = "تبدیل هر تله یا حملهٔ دفع‌شده به +۲ خانه شتاب آتشین برای ققنوس.",
                baseSuccessChancePercent = 100,
                cooldownTurns = 4,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.POSITIONAL_DODGE
            ),
            passiveAbilityTitle = "روح اژدها (Dragon Soul) و پرواز از خاکستر",
            passiveAbilityDescription = "پرواز خودکار از روی چاه و باتلاق + ۱ خانه جهش اضافه در هر نردبان؛ هر بار خنثی‌سازی اثر منفی به او ۲ نوبت تقویت «روح اژدها (+۲ خانه)» می‌دهد.",
            strengths = listOf(
                "ایمنی عالی در برابر چاه تاریک و باتلاق سنگین",
                "پاداش دائمی +۱ خانه در تمام نردبان‌ها"
            ),
            weaknesses = listOf(
                "ضدحملهٔ مستقیم توسط عنصر یخ (Amber / Frost Knight)",
                "Cooldown چهار نوبتی برای پرواز فعال"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 10,
                reverseAndWindResistancePercent = 20,
                statusDebuffResistancePercent = 30,
                displacementResistancePercent = 25,
                vulnerableToClass = HeroClass.CONTROLLER
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_phoenix_wings",
                soundDescription = "شعله‌ور شدن آتش طلایی و صدای بال‌زدن ققنوس",
                animationStyleName = "Solar Wing Burst",
                animationDescription = "گسترش دو بال آتشین نارنجی-طلایی در دو طرف مهره هنگام جهش",
                auraPrimaryColor = Color(0xFFF97316),
                auraSecondaryColor = Color(0xFFDC2626),
                particleSymbol = "🔥"
            )
        ),

        // 10. STORM — Controller / Warrior
        HeroBlueprint(
            id = "storm",
            englishName = "Storm",
            persianTitle = "فرمانروای تندر و صاعقه",
            loreDescription = "نگهبان آسمان‌های طوفانی که با چکش صاعقه، زمین بازی را می‌لرزاند و آرایش مهره‌های رقبا را به هم می‌ریزد.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.THUNDER_HAMMER,
            primaryClass = HeroClass.CONTROLLER,
            secondaryClass = HeroClass.WARRIOR,
            stats = HeroStats(attack = 8, defense = 5, resistance = 6, mobility = 5, control = 9, utility = 3),
            primaryAbility = HeroAbilitySpec(
                id = "storm_thunder_cataclysm",
                persianName = "صاعقهٔ زنجیره‌ای فرمانروا",
                englishName = "Chain Lightning Cataclysm",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.ALL_OPPONENTS,
                description = "صاعقه بر صفحه فرود می‌آورد: خودش ۲ خانه به جلو جهش می‌کند و رقبایی که جلوتر از او هستند با احتمال ۸۰٪ به اندازهٔ ۱ تا ۲ خانه به عقب رانده می‌شوند!",
                opponentImpactDescription = "عقب راندن دسته‌جمعی رقبای پیشتاز به اندازهٔ ۱ تا ۲ خانه با صاعقهٔ سراسری.",
                baseSuccessChancePercent = 80,
                cooldownTurns = 4,
                maxUsesPerMatch = 2,
                counterplay = CounterplayMechanism.HEAVY_ANCHOR_IMMUNE
            ),
            passiveAbilityTitle = "قلب تندر",
            passiveAbilityDescription = "در خانه‌های طوفان همیشه +۲ خانه به جلو پرتاب می‌شود؛ اما اگر صاعقهٔ فعالش به قهرمان صخره‌ای (Atlas) یا سپر طلایی (Jack) برخورد کند، برق به خودش برگشته و ۱ خانه از جهش او کم می‌شود.",
            strengths = listOf(
                "کنترل سراسری روی تمام رقبای پیشتاز به صورت هم‌زمان",
                "تبدیل خانه‌های خطرناک طوفان به سکوی پرتاب"
            ),
            weaknesses = listOf(
                "بازتاب صاعقه در صورت برخورد به اطلس (Earth Titan) یا سپر فعال جک",
                "فقط ۲ بار قابل استفاده در کل مسابقه"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = 30,
                statusDebuffResistancePercent = 20,
                displacementResistancePercent = 40,
                vulnerableToClass = HeroClass.GUARDIAN
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_storm_thunder",
                soundDescription = "غرش رعد آسمانی و فرود صاعقهٔ الکتریکی",
                animationStyleName = "Electric Arc Shockwave",
                animationDescription = "فلاش صاعقهٔ آبی-سفید روی صفحه و جرقهٔ الکتریکی روی مهره‌های رقیب",
                auraPrimaryColor = Color(0xFF38BDF8),
                auraSecondaryColor = Color(0xFF1D4ED8),
                particleSymbol = "🌩️"
            )
        ),

        // 11. STELLA — Support / Trickster
        HeroBlueprint(
            id = "stella",
            englishName = "Stella",
            persianTitle = "نگهبان ستارهٔ اقبال",
            loreDescription = "فرشتهٔ خوش‌اقبالی که نور گوی ستاره‌ای او صندوقچه‌های شانس را طلایی می‌کند و می‌تواند شانس تاس رقیب را بدزدد.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.LUCKY_STAR_ORB,
            primaryClass = HeroClass.SUPPORT,
            secondaryClass = HeroClass.TRICKSTER,
            stats = HeroStats(attack = 3, defense = 5, resistance = 6, mobility = 6, control = 6, utility = 10),
            primaryAbility = HeroAbilitySpec(
                id = "stella_starlight_blessing_steal",
                persianName = "فرشتهٔ محافظ (Guardian Angel) و ستارهٔ اقبال",
                englishName = "Guardian Angel Shield & Starlight Blessing",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.SELF,
                description = "پشتیبانی «فرشتهٔ محافظ (Guardian Angel)» را روی بازیکن عقب‌افتاده یا خود استلا فعال می‌کند (حفاظت کامل در برابر ۱ مار، تله یا شکار + ۲ خانه جهش نورانی)؛ همچنین در نوبت شارژ، تاس ۵ هم مانند ۶ جایزهٔ نوبت اضافه می‌دهد!",
                opponentImpactDescription = "ایجاد سپر فرشتهٔ محافظ و سرقت ۱ واحد شانس از رقیب پیشتاز.",
                baseSuccessChancePercent = 100,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.PURGEABLE_DEBUFF
            ),
            passiveAbilityTitle = "فرشتهٔ محافظ و درخشش خوش‌یمن",
            passiveAbilityDescription = "در خانه‌های شانس همیشه بهترین جایزه را می‌گیرد و هر ۳ نوبت تاس ۵ نیز جایزهٔ پرتاب مجدد دارد.",
            strengths = listOf(
                "بالاترین امتیاز Utility و دریافت نوبت‌های جایزه با تاس ۵ و ۶",
                "تبدیل ۱۰۰٪ خانه‌های شانس به پاداش"
            ),
            weaknesses = listOf(
                "قدرت تهاجمی و زره فیزیکی پایین در برابر مارها و شکارچیان",
                "وابستگی به چرخهٔ شارژ ۳ نوبتی برای فعال‌سازی جایزهٔ تاس ۵"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = 20,
                statusDebuffResistancePercent = 35,
                displacementResistancePercent = 15,
                vulnerableToClass = HeroClass.ASSASSIN
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_stella_starbell",
                soundDescription = "زنگوله‌های ستاره‌ای و درخشش کهکشانی",
                animationStyleName = "Golden Supernova Ring",
                animationDescription = "بارش ستاره‌های طلایی دور مهره و پرتو نور از سمت رقیب پیشتاز",
                auraPrimaryColor = Color(0xFFEAB308),
                auraSecondaryColor = Color(0xFFFDE047),
                particleSymbol = "🌟"
            )
        ),

        // 12. ZEPHYR — Mobility / Controller
        HeroBlueprint(
            id = "zephyr",
            englishName = "Zephyr",
            persianTitle = "تندباد فیروزه‌ای",
            loreDescription = "سوارکار بادهای آزاد که جریان هوا را در صفحه تغییر می‌دهد تا خودش به جلو بتازد و رقبا با باد مخالف روبه‌رو شوند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.WIND_GLIDER,
            primaryClass = HeroClass.CONTROLLER,
            secondaryClass = HeroClass.TRICKSTER,
            stats = HeroStats(attack = 5, defense = 4, resistance = 5, mobility = 9, control = 7, utility = 6),
            primaryAbility = HeroAbilitySpec(
                id = "zephyr_tailwind_gale",
                persianName = "شبح باد (Phantom Phase) و تندباد موافق",
                englishName = "Phantom Phase & Tailwind Surge",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "حالت «شبح (Phantom)» را فعال می‌کند تا بدون برخورد از میان تله‌ها، موانع منفی یا نیش مار عبور کند (+۲ خانه باد موافق) و هم‌زمان با تندباد مخالف، بازیکن پیشتاز را ۱ تا ۲ خانه به عقب می‌راند!",
                opponentImpactDescription = "عبور شبح‌وار از تله‌ها و هل دادن بازیکن پیشتاز به عقب با تندباد.",
                baseSuccessChancePercent = 92,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.HEAVY_ANCHOR_IMMUNE
            ),
            passiveAbilityTitle = "گام شبح باد (Phantom) و جریان هوا",
            passiveAbilityDescription = "در خانهٔ باد ۱۰۰٪ مواقع به جلو پرتاب می‌شود، در تاس‌های زوج (۲ و ۴) +۱ خانه پاداش می‌گیرد و ۳۵٪ شانس ذاتی عبور شبح‌وار از تله‌ها دارد.",
            strengths = listOf(
                "پیشروی بسیار سریع و پایدار در تاس‌های زوج (۲ و ۴)",
                "قابلیت عقب راندن رقیب پیشتاز از راه دور با تندباد"
            ),
            weaknesses = listOf(
                "بی‌اثر بودن تندباد روی اطلس و کای",
                "اصلاح تعادل: حذف پاداش اضافه روی تاس ۶ برای جلوگیری از پیشروی بیش از حد"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = 70,
                statusDebuffResistancePercent = 15,
                displacementResistancePercent = 25,
                vulnerableToClass = HeroClass.GUARDIAN
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_zephyr_windgust",
                soundDescription = "وزش تندباد فیروزه‌ای و چرخش گردباد",
                animationStyleName = "Turquoise Vortex",
                animationDescription = "چرخند فیروزه‌ای دور مهرهٔ زفیر و موج باد به سمت رقیب",
                auraPrimaryColor = Color(0xFF2DD4BF),
                auraSecondaryColor = Color(0xFF0D9488),
                particleSymbol = "🌪️"
            )
        ),

        // 13. ORION — Mage / Trickster
        HeroBlueprint(
            id = "orion",
            englishName = "Orion",
            persianTitle = "کیمیاگر کهکشان",
            loreDescription = "دانشمند کیهانی که اعداد تاس را در بوتهٔ کیمیاگری دگرگون می‌سازد و می‌تواند تاس رقیب را به طلا یا سرب تبدیل کند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.ALCHEMY_FLASK,
            primaryClass = HeroClass.MAGE,
            secondaryClass = HeroClass.TRICKSTER,
            stats = HeroStats(attack = 6, defense = 4, resistance = 6, mobility = 6, control = 7, utility = 7),
            primaryAbility = HeroAbilitySpec(
                id = "orion_cosmic_transmutation",
                persianName = "فریبکار کیمیاگر (Trickster) و انتقال اثر منفی",
                englishName = "Trickster Debuff Transfer & Cosmic Transmutation",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "اگر اوریون اثر منفی فعال (کرم، باتلاق، انجماد یا نصف شدن تاس) داشته باشد، با تردستی «فریبکار (Trickster)» آن را مستقیماً به بازیکن پیشتاز منتقل می‌کند! همچنین تاس ۱ و ۲ خود را به ۵ و ۶ تبدیل کرده و روی تاس بعدی پیشتاز معجون سرب می‌ریزد.",
                opponentImpactDescription = "دریافت مستقیم طلسم‌های منفی اوریون و محدود شدن تاس بعدی با معجون سرب.",
                baseSuccessChancePercent = 92,
                cooldownTurns = 3,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.SHIELD_BLOCKABLE
            ),
            passiveAbilityTitle = "تعادل کیمیا",
            passiveAbilityDescription = "هرگز دو بار متوالی تاس ۱ نمی‌آورد؛ اما هر بار که از قابلیت کیمیاگری استفاده می‌کند، اگر در همان حرکت روی سر مار بیفتد، معجون ناپایدار شده و ۱ خانه بیشتر سقوط می‌کند.",
            strengths = listOf(
                "ایمن‌ترین قهرمان در برابر بدشانسی تاس‌های ۱ و ۲",
                "خنثی کردن شانس تاس ۶ بازیکن پیشتاز با معجون سرب"
            ),
            weaknesses = listOf(
                "ریسک ناپایداری کیمیا: اگر در نوبت فعال بودن معجون روی مار بیفتد، آسیب بیشتری می‌بیند",
                "سقف حرکت کیمیاگری حداکثر تا ۶ خانه در یک پرتاب محدود شده است"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = -10,
                reverseAndWindResistancePercent = 20,
                statusDebuffResistancePercent = 40,
                displacementResistancePercent = 15,
                vulnerableToClass = HeroClass.WARRIOR
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_orion_transmute",
                soundDescription = "تبلور کریستال کیمیاگری و انفجار حباب کهکشانی",
                animationStyleName = "Violet Alchemy Sigil",
                animationDescription = "دایرهٔ کیمیاگری بنفش-طلایی زیر تاس و تغییر رنگ عدد تاس",
                auraPrimaryColor = Color(0xFFC084FC),
                auraSecondaryColor = Color(0xFF581C87),
                particleSymbol = "⚗️"
            )
        ),

        // 14. AMBER — Controller / Guardian
        HeroBlueprint(
            id = "amber",
            englishName = "Amber",
            persianTitle = "شوالیهٔ تاج یخی",
            loreDescription = "فرمانده دژ قطبی که با شمشیر یخ‌شکن خود، پیشتازان میدان را در بلور یخ منجمد می‌کند.",
            gender = CharacterGender.FEMALE,
            heldItem = HeldItemVisual.FROST_SWORD,
            primaryClass = HeroClass.CONTROLLER,
            secondaryClass = HeroClass.GUARDIAN,
            stats = HeroStats(attack = 7, defense = 7, resistance = 6, mobility = 4, control = 9, utility = 3),
            primaryAbility = HeroAbilitySpec(
                id = "amber_glacial_prison",
                persianName = "زندان یخی قطبی",
                englishName = "Glacial Prison",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "بازیکن پیشتاز را هدف موج منجمدکننده قرار می‌دهد: اگر هدف سپر نداشته باشد، با احتمال ۸۰٪ برای ۱ نوبت منجمد می‌شود (یا در صورت داشتن مقاومت بالا، تاس بعدی‌اش نصف می‌شود) و خود امبر +۱ خانه روی یخ سر می‌خورد!",
                opponentImpactDescription = "منجمد کردن کامل بازیکن پیشتاز برای ۱ نوبت یا نصف کردن حرکت او.",
                baseSuccessChancePercent = 80,
                cooldownTurns = 5,
                maxUsesPerMatch = 2,
                counterplay = CounterplayMechanism.SHIELD_BLOCKABLE
            ),
            passiveAbilityTitle = "زره بلور یخ",
            passiveAbilityDescription = "روی خانه‌های چاه تاریک سر خورده و سقوط نمی‌کند؛ اما در برابر قهرمان آتشین (Phoenix) یا در تم کویر سوزان، توان انجمادش به کاهش سرعت (-۳ خانه از تاس هدف) تبدیل می‌شود.",
            strengths = listOf(
                "قدرتمندترین ابزار توقف کامل رقیبی که در آستانهٔ پیروزی است",
                "ارتقا از ۱ بار در کل بازی به ۲ بار استفاده با Cooldown پنج‌نوبتی"
            ),
            weaknesses = listOf(
                "قابل دفع با سپر جک و مقاومت حرارتی ققنوس (Phoenix)",
                "سرعت حرکت پایهٔ متوسط (Mobility = 4)"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 15,
                reverseAndWindResistancePercent = 25,
                statusDebuffResistancePercent = 35,
                displacementResistancePercent = 30,
                vulnerableToClass = HeroClass.SURVIVOR
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_amber_frostshatter",
                soundDescription = "تبلور سریع یخ قطبی و صدای شکستن کریستال",
                animationStyleName = "Glacial Crystal Cage",
                animationDescription = "بلورهای یخ آبی-سفید که دور مهرهٔ رقیب هدف شکل می‌گیرند",
                auraPrimaryColor = Color(0xFF60A5FA),
                auraSecondaryColor = Color(0xFF1E3A8A),
                particleSymbol = "❄️"
            )
        ),

        // 15. SHADOW — Mage / Support (Time Weaver)
        HeroBlueprint(
            id = "shadow",
            englishName = "Shadow",
            persianTitle = "نساج ساعت شنی زمان",
            loreDescription = "مسافر ابعاد زمان که عقربه‌های ساعت را به عقب یا جلو می‌کشد تا طلسم‌ها را بشکند و حرکت آخر رقیب را واگردانی کند.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.HOURGLASS,
            primaryClass = HeroClass.MAGE,
            secondaryClass = HeroClass.SUPPORT,
            stats = HeroStats(attack = 5, defense = 5, resistance = 7, mobility = 7, control = 6, utility = 6),
            primaryAbility = HeroAbilitySpec(
                id = "shadow_chrono_rewind",
                persianName = "نگهبان زمان (Time Keeper — بازگردانی نتیجهٔ نوبت قبل)",
                englishName = "Time Keeper Rewind & Chrono Glitch",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.LEADER_OPPONENT,
                description = "عقربه‌های ساعت شنی را به عقب می‌کشد: اگر در نوبت قبلی دچار سقوط از مار یا عقب‌گرد شده باشد، موقعیت خود را به خانهٔ قبل از سقوط برمی‌گرداند و طلسم‌ها را پاک می‌کند؛ در غیر این صورت، خودش +۲ خانه شتاب زمانی گرفته و رقیب پیشتاز را ۲ خانه در زمان به عقب برمی‌گرداند!",
                opponentImpactDescription = "خنثی‌سازی سقوط نوبت قبلی خود یا برگرداندن رقیب پیشتاز به اندازهٔ ۲ خانه به عقب.",
                baseSuccessChancePercent = 92,
                cooldownTurns = 4,
                maxUsesPerMatch = 2,
                counterplay = CounterplayMechanism.SHIELD_BLOCKABLE
            ),
            passiveAbilityTitle = "وارونگی معکوس و حافظهٔ زمانی",
            passiveAbilityDescription = "در خانهٔ معکوس به جای عقب رفتن، نصف عدد تاس را به جلو می‌جهد و موقعیت آغاز هر نوبت خود را در ساعت شنی ذخیره می‌کند.",
            strengths = listOf(
                "تبدیل یکی از خطرناک‌ترین موانع بازی (خانه معکوس) به سکوی پرتاب رو به جلو",
                "پاک‌سازی کامل تمام طلسم‌ها (کرم، باتلاق، انجماد) + عقب راندن رقیب"
            ),
            weaknesses = listOf(
                "Cooldown چهار نوبتی بین هر شکاف زمانی",
                "آسیب‌پذیری در برابر حملات مستقیم فیزیکی جنگجویان (Blade / Axel)"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 0,
                reverseAndWindResistancePercent = 100,
                statusDebuffResistancePercent = 50,
                displacementResistancePercent = 35,
                vulnerableToClass = HeroClass.WARRIOR
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_shadow_chronotick",
                soundDescription = "تیک‌تاک پژواک‌دار ساعت شنی و موج برگشت زمان",
                animationStyleName = "Crimson Hourglass Ripple",
                animationDescription = "حلقه‌های ساعت شنی یاقوتی که در خلاف جهت عقربه‌های ساعت می‌چرخند",
                auraPrimaryColor = Color(0xFFF43F5E),
                auraSecondaryColor = Color(0xFF881337),
                particleSymbol = "⏳"
            )
        ),

        // 16. ATLAS — Guardian / Survivor
        HeroBlueprint(
            id = "atlas",
            englishName = "Atlas",
            persianTitle = "تایتان صخره‌های جاودان",
            loreDescription = "غول صخره‌ای شکست‌ناپذیر که هیچ طوفان یا چاهی قادر به تکان دادن او نیست و با کوبیدن مشت بر زمین، لرزه بر خانه‌های اطراف می‌اندازد.",
            gender = CharacterGender.MALE,
            heldItem = HeldItemVisual.TITAN_GAUNTLET,
            primaryClass = HeroClass.GUARDIAN,
            secondaryClass = HeroClass.SURVIVOR,
            stats = HeroStats(attack = 7, defense = 10, resistance = 9, mobility = 3, control = 5, utility = 2),
            primaryAbility = HeroAbilitySpec(
                id = "atlas_seismic_tremor",
                persianName = "خشم برسرکر (Berserker) و زلزلهٔ تایتان",
                englishName = "Berserker Fury & Seismic Tremor",
                activationMode = AbilityActivationMode.ACTIVE_COOLDOWN,
                targetScope = HeroAbilityTargetScope.NEARBY_OPPONENTS,
                description = "مشت صخره‌ای بر زمین می‌کوبد: رقبای هم‌ردیف یا نزدیک (تا فاصلهٔ ۵ خانه) را ۲ خانه به عقب می‌لغزاند و حالت «خشم برسرکر (+۲ خانه پاداش حرکت در هر پرتاب)» را فعال می‌کند!",
                opponentImpactDescription = "ایجاد زمین‌لرزه در ردیف فعلی که رقبای نزدیک را ۲ خانه به عقب می‌راند.",
                baseSuccessChancePercent = 95,
                cooldownTurns = 4,
                maxUsesPerMatch = 3,
                counterplay = CounterplayMechanism.POSITIONAL_DODGE
            ),
            passiveAbilityTitle = "خشم برسرکر (Berserker) پس از سقوط و ستون زمین",
            passiveAbilityDescription = "ضد طوفان و چاه تاریک بوده و در نیش مار فقط نصف مسیر را پایین می‌رود؛ علاوه بر این، پس از هر نیش مار یا حرکت رو به عقب، به مدت ۲ نوبت وارد «خشم برسرکر» شده و در پرتاب بعدی +۲ خانه قدرت حرکت بیشتر می‌گیرد!",
            strengths = listOf(
                "بالاترین دفاع (۱۰) و مقاومت (۹) در کل بازی؛ غیرقابل تکان دادن توسط طوفان و چاه",
                "نصف شدن دائمی سقوط تمام مارها + قابلیت زلزلهٔ ردیفی علیه رقبا"
            ),
            weaknesses = listOf(
                "ضعف واقعی و ملموس در سرعت: سنگینی صخره‌ای باعث می‌شود تاس ۶ او را ۵ خانه جلو ببرد (هرچند نوبت جایزهٔ ۶ حفظ می‌شود)",
                "کمترین امتیاز تحرک (Mobility = 3) در میان قهرمانان"
            ),
            resistanceProfile = HeroResistanceProfile(
                snakeDropReductionPercent = 50,
                reverseAndWindResistancePercent = 50,
                statusDebuffResistancePercent = 45,
                displacementResistancePercent = 100,
                vulnerableToClass = HeroClass.MAGE
            ),
            fxSignature = HeroAudioVisualSignature(
                soundCueId = "sfx_hero_atlas_quake",
                soundDescription = "غرش صخره و لرزش کوبندهٔ زمین",
                animationStyleName = "Amber Tectonic Shockwave",
                animationDescription = "ترک‌های نورانی کهربایی دور خانهٔ اطلس و لرزش مهره‌های رقیب هم‌ردیف",
                auraPrimaryColor = Color(0xFFD97706),
                auraSecondaryColor = Color(0xFF78350F),
                particleSymbol = "🪨"
            )
        )
    )

    fun getBlueprintById(id: String): HeroBlueprint {
        return allHeroBlueprints.find { it.id == id } ?: allHeroBlueprints.first()
    }
}
