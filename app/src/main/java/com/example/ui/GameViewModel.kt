package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TruthOrDareRepository
import com.example.model.ActiveSnakeCardDraw
import com.example.model.AiDifficulty
import com.example.model.AnimationQuality
import com.example.model.AnimationSpeed
import com.example.model.BannerType
import com.example.model.BoardGenerator
import com.example.model.BoardLink
import com.example.model.BoardObstacle
import com.example.model.BoardTheme
import com.example.model.CardCategory
import com.example.model.CharacterAbilityType
import com.example.model.CharacterGender
import com.example.model.CharacterRoster
import com.example.model.CustomAbilityEffectType
import com.example.model.CustomAbilityTarget
import com.example.model.DiceVisualTheme
import com.example.model.DifficultyLevel
import com.example.model.EffectIntensity
import com.example.model.EnvironmentEffectType
import com.example.model.GameCharacter
import com.example.model.GameEventBanner
import com.example.model.GamePhase
import com.example.model.CombatLogEntry
import com.example.model.CombatOutcomeType
import com.example.model.HeroCombatEngine
import com.example.model.HeldItemVisual
import com.example.model.LinkType
import com.example.model.MusicGenre
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.PrecisionRollChoice
import com.example.model.SettingsProfile
import com.example.model.ShieldThreatCategory
import com.example.model.SnakeVisualTheme
import com.example.model.TruthOrDareCard
import com.example.model.toPersianDigits
import com.example.sound.GameSoundManager
import com.example.sound.soundscapeInfo
import com.example.ui.components.AdvancedSettingsTab
import com.example.ui.components.GameEventSoundType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

enum class TokenMovementType {
    STEP_BY_STEP,
    LADDER_CLIMB,
    SNAKE_SLIDE,
    REVERSE_BACKWARD,
    WELL_PLUNGE,
    WIND_GUST,
    LUCK_BOOST,
    STORM_SHIFT,
    SHADOW_SWAP,
    EXECUTIONER_HUNT,
    TIME_REWIND
}

enum class HeroBoardFxType {
    NONE,
    EXECUTIONER_SLASH,
    SHIELD_BLOCK_SUCCESS,
    SHIELD_BREAK_FAIL,
    DRAGON_SOUL_AURA,
    BERSERKER_RAGE,
    TIME_REWIND,
    ILLUSION_MIRAGE,
    GUARDIAN_ANGEL_WINGS,
    TRICKSTER_TRANSFER,
    PHANTOM_PHASE
}

data class TokenMovementPath(
    val moveId: Long,
    val playerId: Int,
    val pathTiles: List<Int>,
    val movementType: TokenMovementType
)

data class BoardHighlightEffect(
    val tile: Int,
    val isPositive: Boolean,
    val intensity: Float,
    val obstacleType: ObstacleType? = null,
    val heroFxType: HeroBoardFxType = HeroBoardFxType.NONE
)

data class GameUiState(
    val phase: GamePhase = GamePhase.SETUP,
    val playerCount: Int = 2,
    val activeSetupPlayerSlot: Int = 0,
    val playerCharacterIds: List<String> = CharacterRoster.allCharacters.take(8).map { it.id },
    val playerNamesInput: List<String> = CharacterRoster.allCharacters.take(8).map { it.defaultEnglishName },
    val maxTiles: Int = 100,
    val boardTheme: BoardTheme = BoardTheme.BLACK_GOLD,
    val previewBoardTheme: BoardTheme? = null,
    val snakeVisualTheme: SnakeVisualTheme = SnakeVisualTheme.ROYAL_GOLD_SLIM,
    val snakeThicknessScale: Float = 0.85f,
    val diceVisualTheme: DiceVisualTheme = DiceVisualTheme.AUTO_MATCH_BOARD,
    val aiDifficulty: AiDifficulty = AiDifficulty.MEDIUM,
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val enabledObstacles: Set<ObstacleType> = ObstacleType.entries.toSet(),
    val players: List<Player> = emptyList(),
    val boardLinks: List<BoardLink> = emptyList(),
    val boardObstacles: List<BoardObstacle> = emptyList(),
    val currentTurnIndex: Int = 0,
    val diceValue: Int = 6,
    val secondDiceValue: Int = 6,
    val isDiceRolling: Boolean = false,
    val isTokenAnimating: Boolean = false,
    val diceRotationDeg: Float = 0f,
    val secondDiceRotationDeg: Float = 0f,
    val diceRollTriggerId: Long = 0L,
    val sixRollGlowActive: Boolean = false,
    val pendingPrecisionChoice: PrecisionRollChoice? = null,
    val activeMovementPath: TokenMovementPath? = null,
    val highlightEffect: BoardHighlightEffect? = null,
    val stormFlashActive: Boolean = false,
    val eventBanner: GameEventBanner = GameEventBanner(
        message = "تاس را بریزید و ماجراجویی را آغاز کنید!",
        type = BannerType.NORMAL
    ),
    val winner: Player? = null,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isAmbientEnabled: Boolean = true,
    val musicVolume: Float = 0.68f,
    val sfxVolume: Float = 0.85f,
    val ambientVolume: Float = 0.64f,
    // Studio: Environment Visual Effects, Animation Quality/Speed & Music Genre State
    val animationQuality: AnimationQuality = AnimationQuality.CINEMATIC,
    val animationSpeed: AnimationSpeed = AnimationSpeed.SMOOTH,
    val isCameraFollowEnabled: Boolean = true,
    val isEnvironmentEffectsEnabled: Boolean = true,
    val selectedEnvironmentEffect: EnvironmentEffectType = EnvironmentEffectType.GOLDEN_DUST,
    val previewEnvironmentEffect: EnvironmentEffectType? = null,
    val environmentEffectIntensity: EffectIntensity = EffectIntensity.MEDIUM,
    val isAmbientAnimationEnabled: Boolean = true,
    val backgroundLightingLevel: EffectIntensity = EffectIntensity.MEDIUM,
    val isWeatherEffectsEnabled: Boolean = true,
    val selectedMusicGenre: MusicGenre = MusicGenre.ROYAL,
    val previewingMusicGenre: MusicGenre? = null,
    // Phase 4 Truth or Dare & Local Storage State:
    val truthOrDareCards: List<TruthOrDareCard> = emptyList(),
    val enabledCardCategories: Set<CardCategory> = CardCategory.entries.toSet(),
    val isCardManagerOpen: Boolean = false,
    val cardSaveFeedbackMessage: String? = null,
    val activeSnakeCardDraw: ActiveSnakeCardDraw? = null,
    // Phase 5 Custom Characters, Settings Profiles & Advanced Settings Modal:
    val customCharacters: List<GameCharacter> = emptyList(),
    val settingsProfiles: List<SettingsProfile> = emptyList(),
    val isAdvancedSettingsOpen: Boolean = false,
    val advancedSettingsInitialTab: AdvancedSettingsTab = AdvancedSettingsTab.BOARD_AND_THEME,
    // Configurable Guardian Shield & Protection Base Probabilities (%):
    val shieldSnakeDefensePercent: Int = 80,
    val shieldObstacleDefensePercent: Int = 78,
    val shieldHuntDefensePercent: Int = 75,
    // Hero Interaction & Combat System State:
    val combatLogs: List<CombatLogEntry> = emptyList(),
    val isCombatLogOpen: Boolean = false,
    // Pawn Customization System:
    val playerPawnSkinIds: List<String> = List(8) { "classic_standard" },
    val isPawnCollectionOpen: Boolean = false,
    val pawnCollectionActiveSlot: Int = 0,
    val unlockedPawnSkinIds: Set<String> = com.example.model.PawnSkinCatalog.defaultUnlockedSkinIds
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentTurnIndex)

    val leaderPlayer: Player?
        get() = players.maxByOrNull { it.currentTile }

    val effectiveBoardTheme: BoardTheme
        get() = previewBoardTheme ?: boardTheme

    val effectiveDiceTheme: DiceVisualTheme
        get() = diceVisualTheme.resolveForBoardTheme(effectiveBoardTheme)

    val effectiveEnvironmentEffect: EnvironmentEffectType
        get() = previewEnvironmentEffect ?: selectedEnvironmentEffect

    val effectiveMusicGenre: MusicGenre
        get() = previewingMusicGenre ?: selectedMusicGenre

    val allAvailableCharacters: List<GameCharacter>
        get() = customCharacters + CharacterRoster.allCharacters

    fun resolveCharacterById(id: String): GameCharacter {
        return CharacterRoster.getById(id, customCharacters)
    }
}

class GameViewModel(
    context: Context,
    private val soundManager: GameSoundManager = GameSoundManager(context),
    private val cardRepository: TruthOrDareRepository = TruthOrDareRepository(context)
) : ViewModel() {

    private val prefs = context.getSharedPreferences("mystic_snakes_session_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(loadInitialSetupState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var moveSequenceCounter = 0L
    private var pendingMovementCompletion: CompletableDeferred<Unit>? = null
    private var pendingSnakeCardCompletion: CompletableDeferred<Unit>? = null
    private var feedbackClearJob: Job? = null
    private var aiTurnJob: Job? = null

    init {
        viewModelScope.launch {
            cardRepository.allCards.collect { savedCards ->
                _uiState.update { it.copy(truthOrDareCards = savedCards) }
            }
        }
        viewModelScope.launch {
            cardRepository.customCharacters.collect { savedCustomChars ->
                _uiState.update { st ->
                    val updatedPlayers = st.players.map { p ->
                        val resolved = CharacterRoster.getById(p.characterId, savedCustomChars)
                        p.copy(customCharacterOverride = if (resolved.isCustom) resolved else null)
                    }
                    st.copy(
                        customCharacters = savedCustomChars,
                        players = updatedPlayers
                    )
                }
            }
        }
        viewModelScope.launch {
            cardRepository.settingsProfiles.collect { savedProfiles ->
                _uiState.update { it.copy(settingsProfiles = savedProfiles) }
            }
        }
    }

    private fun loadInitialSetupState(): GameUiState {
        val savedCount = prefs.getInt("player_count", 2).coerceIn(1, 8)
        val savedSound = prefs.getBoolean("sound_enabled", true)
        val savedMusic = prefs.getBoolean("music_enabled", true)
        val savedAmbient = prefs.getBoolean("ambient_enabled", true)
        val savedMusicVol = prefs.getFloat("music_volume", 0.68f).coerceIn(0f, 1f)
        val savedSfxVol = prefs.getFloat("sfx_volume", 0.85f).coerceIn(0f, 1f)
        val savedAmbientVol = prefs.getFloat("ambient_volume", 0.64f).coerceIn(0f, 1f)
        val savedEnvEffectsEnabled = prefs.getBoolean("env_effects_enabled", true)
        val savedEnvEffect = EnvironmentEffectType.fromName(
            prefs.getString("selected_env_effect", EnvironmentEffectType.GOLDEN_DUST.name)
        )
        val savedEnvIntensity = EffectIntensity.fromName(
            prefs.getString("env_effect_intensity", EffectIntensity.MEDIUM.name)
        )
        val savedAmbientAnimEnabled = prefs.getBoolean("ambient_animation_enabled", true)
        val savedBgLightingLevel = EffectIntensity.fromName(
            prefs.getString("bg_lighting_level", EffectIntensity.MEDIUM.name)
        )
        val savedWeatherEffectsEnabled = prefs.getBoolean("weather_effects_enabled", true)
        val savedAnimQuality = AnimationQuality.fromName(
            prefs.getString("animation_quality", AnimationQuality.CINEMATIC.name)
        )
        val savedAnimSpeed = AnimationSpeed.fromName(
            prefs.getString("animation_speed", AnimationSpeed.SMOOTH.name)
        )
        val savedCameraFollow = prefs.getBoolean("camera_follow_enabled", true)
        val savedMusicGenre = MusicGenre.fromName(
            prefs.getString("selected_music_genre", MusicGenre.ROYAL.name)
        )
        val savedMaxTiles = prefs.getInt("max_tiles", 100).coerceIn(50, 200)
        val savedTheme = BoardTheme.fromName(prefs.getString("board_theme_v2", BoardTheme.BLACK_GOLD.name))
        val savedSnakeTheme = SnakeVisualTheme.fromName(
            prefs.getString("snake_visual_theme", SnakeVisualTheme.ROYAL_GOLD_SLIM.name)
        )
        val savedSnakeThickness = prefs.getFloat("snake_thickness_scale", 0.85f).coerceIn(0.55f, 1.25f)
        val savedDiceTheme = DiceVisualTheme.fromName(
            prefs.getString("dice_visual_theme", DiceVisualTheme.AUTO_MATCH_BOARD.name)
        )
        val savedAiDiff = AiDifficulty.fromName(prefs.getString("ai_difficulty", AiDifficulty.MEDIUM.name))

        soundManager.isSoundEnabled = savedSound
        soundManager.updateSfxVolume(savedSfxVol)
        soundManager.setMusicEnabled(savedMusic)
        soundManager.setAmbientEnabled(savedAmbient)
        soundManager.setMusicVolume(savedMusicVol)
        soundManager.setAmbientVolume(savedAmbientVol)
        soundManager.updateThemeMusic(savedTheme, playTransitionMotif = false)
        soundManager.setMusicGenre(savedMusicGenre, playTransitionMotif = false)

        val defaultChars = CharacterRoster.allCharacters.take(8)

        val savedCharIds = MutableList(8) { idx ->
            prefs.getString("player_char_$idx", null) ?: defaultChars[idx].id
        }

        val seenIds = mutableSetOf<String>()
        for (i in 0 until 8) {
            val id = savedCharIds[i]
            if (id in seenIds) {
                val replacement = CharacterRoster.allCharacters.first { it.id !in seenIds }.id
                savedCharIds[i] = replacement
                seenIds.add(replacement)
            } else {
                seenIds.add(id)
            }
        }

        val savedNames = List(8) { idx ->
            val charDefault = CharacterRoster.getById(savedCharIds[idx]).defaultEnglishName
            prefs.getString("player_name_$idx", null)?.takeIf { it.isNotBlank() } ?: charDefault
        }

        val savedDiffName = prefs.getString("difficulty_level", DifficultyLevel.MEDIUM.name)
        val difficulty = DifficultyLevel.entries.find { it.name == savedDiffName } ?: DifficultyLevel.MEDIUM

        val savedObstaclesRaw = prefs.getStringSet("enabled_obstacles", null)
        val enabledObstacles = if (savedObstaclesRaw == null) {
            ObstacleType.entries.toSet()
        } else {
            ObstacleType.entries.filter { it.name in savedObstaclesRaw }.toSet()
        }

        val enabledCardCategories = cardRepository.getEnabledCardCategories()

        val initialBoard = BoardGenerator.generateBoardConfiguration(
            maxTiles = savedMaxTiles,
            difficulty = difficulty,
            enabledObstacles = enabledObstacles
        )

        val savedShieldSnakePct = prefs.getInt("shield_snake_def_pct", 80).coerceIn(40, 95)
        val savedShieldObstaclePct = prefs.getInt("shield_obstacle_def_pct", 78).coerceIn(40, 95)
        val savedShieldHuntPct = prefs.getInt("shield_hunt_def_pct", 75).coerceIn(40, 95)

        return GameUiState(
            phase = GamePhase.SETUP,
            playerCount = savedCount,
            activeSetupPlayerSlot = 0,
            playerCharacterIds = savedCharIds,
            playerNamesInput = savedNames,
            maxTiles = savedMaxTiles,
            boardTheme = savedTheme,
            snakeVisualTheme = savedSnakeTheme,
            snakeThicknessScale = savedSnakeThickness,
            diceVisualTheme = savedDiceTheme,
            aiDifficulty = savedAiDiff,
            difficulty = difficulty,
            enabledObstacles = enabledObstacles,
            enabledCardCategories = enabledCardCategories,
            isSoundEnabled = savedSound,
            isMusicEnabled = savedMusic,
            isAmbientEnabled = savedAmbient,
            musicVolume = savedMusicVol,
            sfxVolume = savedSfxVol,
            ambientVolume = savedAmbientVol,
            animationQuality = savedAnimQuality,
            animationSpeed = savedAnimSpeed,
            isCameraFollowEnabled = savedCameraFollow,
            isEnvironmentEffectsEnabled = savedEnvEffectsEnabled,
            selectedEnvironmentEffect = savedEnvEffect,
            previewEnvironmentEffect = null,
            environmentEffectIntensity = savedEnvIntensity,
            isAmbientAnimationEnabled = savedAmbientAnimEnabled,
            backgroundLightingLevel = savedBgLightingLevel,
            isWeatherEffectsEnabled = savedWeatherEffectsEnabled,
            selectedMusicGenre = savedMusicGenre,
            previewingMusicGenre = null,
            boardLinks = initialBoard.links,
            boardObstacles = initialBoard.obstacles,
            shieldSnakeDefensePercent = savedShieldSnakePct,
            shieldObstacleDefensePercent = savedShieldObstaclePct,
            shieldHuntDefensePercent = savedShieldHuntPct
        )
    }

    // --- Phase 5 Advanced Settings, Themes, Custom Characters & Profiles ---

    fun openAdvancedSettings(initialTab: AdvancedSettingsTab = AdvancedSettingsTab.BOARD_AND_THEME) {
        soundManager.playDiceTick(3)
        _uiState.update {
            it.copy(
                isAdvancedSettingsOpen = true,
                advancedSettingsInitialTab = initialTab,
                cardSaveFeedbackMessage = null
            )
        }
    }

    fun closeAdvancedSettings() {
        soundManager.playDiceTick(1)
        soundManager.previewMusicGenre(null)
        _uiState.update {
            it.copy(
                isAdvancedSettingsOpen = false,
                previewBoardTheme = null,
                previewEnvironmentEffect = null,
                previewingMusicGenre = null,
                cardSaveFeedbackMessage = null
            )
        }
    }

    fun setMaxTiles(newMaxTiles: Int) {
        val clamped = (newMaxTiles / 10 * 10).coerceIn(50, 200)
        prefs.edit().putInt("max_tiles", clamped).apply()
        soundManager.playDiceTick(4)
        _uiState.update { state ->
            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = clamped,
                difficulty = state.difficulty,
                enabledObstacles = state.enabledObstacles
            )
            val clampedPlayers = state.players.map { p ->
                p.copy(currentTile = p.currentTile.coerceIn(1, clamped - 1))
            }
            state.copy(
                maxTiles = clamped,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles,
                players = clampedPlayers,
                eventBanner = GameEventBanner(
                    message = "صفحه بازی به ${clamped.toPersianDigits()} خانه تغییر یافت و مار و پله‌ها بازسازی شدند! ✨",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("تعداد خانه‌های صفحه روی ${clamped.toPersianDigits()} خانه تنظیم شد! ✅")
    }

    fun setBoardTheme(theme: BoardTheme) {
        prefs.edit()
            .putString("board_theme_v2", theme.name)
            .putString("selected_env_effect", theme.defaultEnvironmentEffect.name)
            .apply()
        soundManager.updateThemeMusic(theme, playTransitionMotif = true)
        val soundscape = theme.soundscapeInfo()
        _uiState.update {
            it.copy(
                boardTheme = theme,
                previewBoardTheme = null,
                selectedEnvironmentEffect = theme.defaultEnvironmentEffect,
                previewEnvironmentEffect = null,
                eventBanner = GameEventBanner(
                    message = "تم «${theme.emoji} ${theme.persianName}» (${theme.englishName}) و فضای محیطی آن اعمال شد! ✨",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("تم «${theme.persianName}» + جلوه محیطی و صدای (${soundscape.ambientTitle}) اعمال شد! ✨")
    }

    fun previewBoardTheme(theme: BoardTheme?) {
        soundManager.playDiceTick(2)
        _uiState.update { state ->
            val nextPreview = if (state.previewBoardTheme == theme) null else theme
            state.copy(
                previewBoardTheme = nextPreview,
                previewEnvironmentEffect = nextPreview?.defaultEnvironmentEffect
            )
        }
        val activePreview = _uiState.value.previewBoardTheme
        if (activePreview != null) {
            showCardSaveFeedback("پیش‌نمایش زنده تم «${activePreview.persianName}» فعال شد! 👁️")
        }
    }

    fun setSnakeVisualTheme(snakeTheme: SnakeVisualTheme) {
        prefs.edit().putString("snake_visual_theme", snakeTheme.name).apply()
        soundManager.playDiceTick(3)
        _uiState.update {
            it.copy(
                snakeVisualTheme = snakeTheme,
                eventBanner = GameEventBanner(
                    message = "تم ظاهری مارها به «${snakeTheme.emoji} ${snakeTheme.persianName}» تغییر کرد! 🐍",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("تم مارها: «${snakeTheme.persianName}» فعال شد! 🐍")
    }

    fun setSnakeThicknessScale(scale: Float) {
        val clamped = scale.coerceIn(0.55f, 1.25f)
        prefs.edit().putFloat("snake_thickness_scale", clamped).apply()
        _uiState.update {
            it.copy(snakeThicknessScale = clamped)
        }
    }

    fun setDiceVisualTheme(diceTheme: DiceVisualTheme) {
        prefs.edit().putString("dice_visual_theme", diceTheme.name).apply()
        soundManager.playDiceTick(4)
        _uiState.update { state ->
            val resolved = diceTheme.resolveForBoardTheme(state.boardTheme)
            val label = if (diceTheme == DiceVisualTheme.AUTO_MATCH_BOARD) {
                "هماهنگ خودکار با تم (${resolved.persianName})"
            } else {
                diceTheme.persianName
            }
            state.copy(
                diceVisualTheme = diceTheme,
                eventBanner = GameEventBanner(
                    message = "ظاهر تاس سه‌بعدی به «${diceTheme.emoji} $label» تغییر کرد! 🎲",
                    type = BannerType.NORMAL
                )
            )
        }
        val currentBoard = _uiState.value.boardTheme
        val resolvedName = diceTheme.resolveForBoardTheme(currentBoard).persianName
        val feedback = if (diceTheme == DiceVisualTheme.AUTO_MATCH_BOARD) {
            "تاس سه‌بعدی روی حالت هماهنگ با تم صفحه ($resolvedName) قرار گرفت! 🎲✅"
        } else {
            "پوسته تاس سه‌بعدی «${diceTheme.persianName}» فعال شد! 🎲✅"
        }
        showCardSaveFeedback(feedback)
    }

    fun setAiDifficulty(aiDiff: AiDifficulty) {
        prefs.edit().putString("ai_difficulty", aiDiff.name).apply()
        soundManager.playDiceTick(aiDiff.ordinal + 2)
        _uiState.update { it.copy(aiDifficulty = aiDiff) }
        showCardSaveFeedback("سطح هوش مصنوعی روی «${aiDiff.persianTitle}» تنظیم شد! 🤖")
    }

    fun setShieldSnakeDefensePercent(percent: Int) {
        val clamped = percent.coerceIn(40, 95)
        prefs.edit().putInt("shield_snake_def_pct", clamped).apply()
        _uiState.update { it.copy(shieldSnakeDefensePercent = clamped) }
    }

    fun setShieldObstacleDefensePercent(percent: Int) {
        val clamped = percent.coerceIn(40, 95)
        prefs.edit().putInt("shield_obstacle_def_pct", clamped).apply()
        _uiState.update { it.copy(shieldObstacleDefensePercent = clamped) }
    }

    fun setShieldHuntDefensePercent(percent: Int) {
        val clamped = percent.coerceIn(40, 95)
        prefs.edit().putInt("shield_hunt_def_pct", clamped).apply()
        _uiState.update { it.copy(shieldHuntDefensePercent = clamped) }
    }

    fun setAllObstaclesEnabled(enabled: Boolean) {
        soundManager.playDiceTick(if (enabled) 5 else 1)
        val newSet = if (enabled) ObstacleType.entries.toSet() else emptySet()
        prefs.edit().putStringSet("enabled_obstacles", newSet.map { it.name }.toSet()).apply()
        _uiState.update { state ->
            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = state.maxTiles,
                difficulty = state.difficulty,
                enabledObstacles = newSet
            )
            state.copy(
                enabledObstacles = newSet,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles
            )
        }
    }

    fun createCustomCharacter(
        englishName: String,
        persianTitle: String,
        gender: CharacterGender,
        primaryColorArgb: Long,
        secondaryColorArgb: Long,
        hairColorArgb: Long,
        skinToneArgb: Long,
        heldItem: HeldItemVisual,
        target: CustomAbilityTarget,
        effectType: CustomAbilityEffectType,
        percentageOrValue: Int,
        customDescription: String
    ) {
        viewModelScope.launch {
            val newCharId = cardRepository.saveCustomCharacter(
                englishName = englishName,
                persianTitle = persianTitle,
                gender = gender,
                primaryColorArgb = primaryColorArgb,
                secondaryColorArgb = secondaryColorArgb,
                hairColorArgb = hairColorArgb,
                skinToneArgb = skinToneArgb,
                heldItem = heldItem,
                target = target,
                effectType = effectType,
                percentageOrValue = percentageOrValue,
                customDescription = customDescription
            )
            soundManager.playCardSavedSound()
            // Automatically assign to current active setup slot so player can immediately play with it
            val activeSlot = _uiState.value.activeSetupPlayerSlot.coerceIn(0, 7)
            val updatedIds = _uiState.value.playerCharacterIds.toMutableList()
            updatedIds[activeSlot] = newCharId
            val updatedNames = _uiState.value.playerNamesInput.toMutableList()
            updatedNames[activeSlot] = englishName.trim()
            prefs.edit()
                .putString("player_char_$activeSlot", newCharId)
                .putString("player_name_$activeSlot", englishName.trim())
                .apply()

            _uiState.update {
                it.copy(
                    playerCharacterIds = updatedIds,
                    playerNamesInput = updatedNames
                )
            }
            showCardSaveFeedback("کاراکتر سفارشی «${englishName.trim()}» ساخته و در حافظه ذخیره شد! 🦸✅")
        }
    }

    fun deleteCustomCharacter(characterId: String) {
        viewModelScope.launch {
            cardRepository.deleteCustomCharacter(characterId)
            soundManager.playDiceTick(0)
            _uiState.update { st ->
                val updatedIds = st.playerCharacterIds.mapIndexed { idx, id ->
                    if (id == characterId) {
                        val fallback = CharacterRoster.allCharacters[idx % CharacterRoster.allCharacters.size].id
                        prefs.edit().putString("player_char_$idx", fallback).apply()
                        fallback
                    } else id
                }
                st.copy(playerCharacterIds = updatedIds)
            }
            showCardSaveFeedback("کاراکتر سفارشی از حافظه حذف شد.")
        }
    }

    fun saveCurrentSettingsProfile(profileName: String) {
        val state = _uiState.value
        viewModelScope.launch {
            cardRepository.saveSettingsProfile(
                profileName = profileName,
                maxTiles = state.maxTiles,
                difficulty = state.difficulty,
                boardTheme = state.boardTheme,
                aiDifficulty = state.aiDifficulty,
                playerCount = state.playerCount,
                enabledObstacles = state.enabledObstacles,
                enabledCardCategories = state.enabledCardCategories,
                environmentEffect = state.selectedEnvironmentEffect,
                effectIntensity = state.environmentEffectIntensity,
                isEnvironmentEffectsEnabled = state.isEnvironmentEffectsEnabled,
                musicGenre = state.selectedMusicGenre
            )
            soundManager.playCardSavedSound()
            showCardSaveFeedback("پروفایل تنظیمات «${profileName.trim()}» (شامل جلوه و موسیقی) ذخیره شد! 💾✅")
        }
    }

    fun loadSettingsProfile(profile: SettingsProfile) {
        prefs.edit()
            .putInt("max_tiles", profile.maxTiles)
            .putString("difficulty_level", profile.difficulty.name)
            .putString("board_theme_v2", profile.boardTheme.name)
            .putString("ai_difficulty", profile.aiDifficulty.name)
            .putInt("player_count", profile.playerCount)
            .putStringSet("enabled_obstacles", profile.enabledObstacles.map { it.name }.toSet())
            .putString("selected_env_effect", profile.environmentEffect.name)
            .putString("env_effect_intensity", profile.effectIntensity.name)
            .putBoolean("env_effects_enabled", profile.isEnvironmentEffectsEnabled)
            .putString("selected_music_genre", profile.musicGenre.name)
            .apply()
        cardRepository.saveEnabledCardCategories(profile.enabledCardCategories)

        soundManager.updateThemeMusic(profile.boardTheme, playTransitionMotif = false)
        soundManager.setMusicGenre(profile.musicGenre, playTransitionMotif = true)
        _uiState.update { state ->
            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = profile.maxTiles,
                difficulty = profile.difficulty,
                enabledObstacles = profile.enabledObstacles
            )
            state.copy(
                maxTiles = profile.maxTiles,
                difficulty = profile.difficulty,
                boardTheme = profile.boardTheme,
                aiDifficulty = profile.aiDifficulty,
                playerCount = profile.playerCount,
                enabledObstacles = profile.enabledObstacles,
                enabledCardCategories = profile.enabledCardCategories,
                selectedEnvironmentEffect = profile.environmentEffect,
                previewEnvironmentEffect = null,
                environmentEffectIntensity = profile.effectIntensity,
                isEnvironmentEffectsEnabled = profile.isEnvironmentEffectsEnabled,
                selectedMusicGenre = profile.musicGenre,
                previewingMusicGenre = null,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles,
                eventBanner = GameEventBanner(
                    message = "پروفایل «${profile.profileName}» با موفقیت بارگذاری شد! ✨",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("پروفایل «${profile.profileName}» بارگذاری و اعمال شد! ✅")
    }

    fun deleteSettingsProfile(profileId: Long) {
        viewModelScope.launch {
            cardRepository.deleteSettingsProfile(profileId)
            soundManager.playDiceTick(0)
            showCardSaveFeedback("پروفایل تنظیمات حذف شد.")
        }
    }

    // --- Studio: Environment Visual Effects & Music Genre Controls ---

    fun toggleEnvironmentEffects() {
        val newState = !_uiState.value.isEnvironmentEffectsEnabled
        prefs.edit().putBoolean("env_effects_enabled", newState).apply()
        soundManager.playDiceTick(if (newState) 5 else 1)
        _uiState.update {
            it.copy(
                isEnvironmentEffectsEnabled = newState,
                eventBanner = GameEventBanner(
                    message = if (newState) {
                        "جلوه‌های محیطی (${it.selectedEnvironmentEffect.persianName}) فعال شد! ✨"
                    } else {
                        "جلوه‌های محیطی غیرفعال شد."
                    },
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback(
            if (newState) "جلوه‌های محیطی فعال شد! ✨" else "جلوه‌های محیطی خاموش شد."
        )
    }

    fun selectEnvironmentEffect(effect: EnvironmentEffectType) {
        prefs.edit()
            .putString("selected_env_effect", effect.name)
            .putBoolean("env_effects_enabled", true)
            .apply()
        soundManager.playDiceTick(4)
        _uiState.update {
            it.copy(
                selectedEnvironmentEffect = effect,
                previewEnvironmentEffect = null,
                isEnvironmentEffectsEnabled = true,
                eventBanner = GameEventBanner(
                    message = "جلوه محیطی «${effect.emoji} ${effect.persianName}» (${effect.englishName}) فعال شد! ✨",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("جلوه محیطی «${effect.persianName}» انتخاب و ذخیره شد! ✨✅")
    }

    fun previewEnvironmentEffect(effect: EnvironmentEffectType?) {
        soundManager.playDiceTick(2)
        _uiState.update { state ->
            val nextPreview = if (state.previewEnvironmentEffect == effect) null else effect
            state.copy(previewEnvironmentEffect = nextPreview)
        }
    }

    fun setEnvironmentEffectIntensity(intensity: EffectIntensity) {
        prefs.edit().putString("env_effect_intensity", intensity.name).apply()
        soundManager.playDiceTick(intensity.ordinal + 2)
        _uiState.update {
            it.copy(
                environmentEffectIntensity = intensity,
                isEnvironmentEffectsEnabled = true,
                eventBanner = GameEventBanner(
                    message = "تراکم ذرات محیطی روی حالت «${intensity.persianTitle}» قرار گرفت! ✨",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("تراکم ذرات محیطی: «${intensity.persianTitle}» ذخیره شد! ✅")
    }

    fun toggleAmbientAnimation() {
        val newState = !_uiState.value.isAmbientAnimationEnabled
        prefs.edit().putBoolean("ambient_animation_enabled", newState).apply()
        soundManager.playDiceTick(if (newState) 4 else 1)
        _uiState.update {
            it.copy(
                isAmbientAnimationEnabled = newState,
                eventBanner = GameEventBanner(
                    message = if (newState) "انیمیشن محیط اطراف (Ambient Animation) فعال شد! 🌿" else "انیمیشن محیط اطراف متوقف شد.",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback(
            if (newState) "انیمیشن محیط اطراف فعال شد! ✅" else "انیمیشن محیط اطراف غیرفعال شد."
        )
    }

    fun setBackgroundLightingLevel(level: EffectIntensity) {
        prefs.edit().putString("bg_lighting_level", level.name).apply()
        soundManager.playDiceTick(level.ordinal + 2)
        _uiState.update {
            it.copy(
                backgroundLightingLevel = level,
                eventBanner = GameEventBanner(
                    message = "نورپردازی پس‌زمینه روی سطح «${level.persianTitle}» تنظیم شد! 💡",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("نورپردازی پس‌زمینه: «${level.persianTitle}» ذخیره شد! 💡✅")
    }

    fun toggleWeatherEffects() {
        val newState = !_uiState.value.isWeatherEffectsEnabled
        prefs.edit().putBoolean("weather_effects_enabled", newState).apply()
        soundManager.playDiceTick(if (newState) 5 else 1)
        _uiState.update {
            it.copy(
                isWeatherEffectsEnabled = newState,
                eventBanner = GameEventBanner(
                    message = if (newState) "افکت‌های آب‌وهوایی (برف، موج، مه و گرما) فعال شد! ❄️🌊" else "افکت‌های آب‌وهوایی غیرفعال شد.",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback(
            if (newState) "افکت‌های آب‌وهوایی فعال شد! ❄️✅" else "افکت‌های آب‌وهوایی خاموش شد."
        )
    }

    fun setAnimationQuality(quality: AnimationQuality) {
        prefs.edit().putString("animation_quality", quality.name).apply()
        soundManager.playDiceTick(quality.ordinal + 3)
        _uiState.update {
            it.copy(
                animationQuality = quality,
                eventBanner = GameEventBanner(
                    message = "کیفیت انیمیشن و رندر روی حالت «${quality.emoji} ${quality.persianTitle}» تنظیم شد! ✨",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("کیفیت انیمیشن: «${quality.persianTitle}» ذخیره شد! 🎬✅")
    }

    fun openCombatLogModal() {
        soundManager.playDiceTick(3)
        _uiState.update { it.copy(isCombatLogOpen = true) }
    }

    fun closeCombatLogModal() {
        _uiState.update { it.copy(isCombatLogOpen = false) }
    }

    fun clearCombatLogs() {
        soundManager.playDiceTick(1)
        _uiState.update { it.copy(combatLogs = emptyList()) }
        showCardSaveFeedback("تاریخچه نبردهای بازی پاک‌سازی شد! 🧹")
    }

    // Pawn Customization System Methods:
    fun openPawnCollection(slot: Int = 0) {
        soundManager.playDiceTick(4)
        _uiState.update {
            val safeSlot = slot.coerceIn(0, (it.playerCount - 1).coerceAtLeast(0))
            it.copy(
                isPawnCollectionOpen = true,
                pawnCollectionActiveSlot = safeSlot
            )
        }
    }

    fun closePawnCollection() {
        soundManager.playDiceTick(1)
        _uiState.update { it.copy(isPawnCollectionOpen = false) }
    }

    fun equipPawnSkinForSlot(slot: Int, skinId: String) {
        soundManager.playDiceTick(5)
        _uiState.update { state ->
            val updatedSkinIds = state.playerPawnSkinIds.toMutableList()
            while (updatedSkinIds.size <= slot) {
                updatedSkinIds.add("classic_standard")
            }
            updatedSkinIds[slot] = skinId

            val updatedPlayers = state.players.map { player ->
                if (player.id == slot) {
                    player.copy(customPawnSkinId = skinId)
                } else {
                    player
                }
            }

            state.copy(
                playerPawnSkinIds = updatedSkinIds,
                players = updatedPlayers
            )
        }
    }

    fun equipPawnSkinForAll(skinId: String) {
        soundManager.playDiceTick(6)
        _uiState.update { state ->
            val updatedSkinIds = List(8) { skinId }
            val updatedPlayers = state.players.map { player ->
                player.copy(customPawnSkinId = skinId)
            }
            state.copy(
                playerPawnSkinIds = updatedSkinIds,
                players = updatedPlayers
            )
        }
    }

    fun unlockPawnSkin(skinId: String) {
        soundManager.playDiceTick(6)
        _uiState.update { state ->
            val updated = state.unlockedPawnSkinIds + skinId
            prefs.edit().putStringSet("unlocked_pawn_skins", updated).apply()
            state.copy(unlockedPawnSkinIds = updated)
        }
        showCardSaveFeedback("پوسته جدید با موفقیت بازگشایی شد! 🎉")
    }

    fun unlockAllPawnSkins() {
        soundManager.playDiceTick(6)
        val allIds = com.example.model.PawnSkinCatalog.allSkins.map { it.id }.toSet()
        prefs.edit().putStringSet("unlocked_pawn_skins", allIds).apply()
        _uiState.update { it.copy(unlockedPawnSkinIds = allIds) }
        showCardSaveFeedback("تمام ۳۹ پوسته مهره بازگشایی شدند! 👑")
    }

    fun setAnimationSpeed(speed: AnimationSpeed) {
        prefs.edit().putString("animation_speed", speed.name).apply()
        soundManager.playDiceTick(speed.ordinal + 3)
        _uiState.update {
            it.copy(
                animationSpeed = speed,
                eventBanner = GameEventBanner(
                    message = "سرعت انیمیشن تاس و حرکت مهره‌ها روی «${speed.emoji} ${speed.persianTitle}» قرار گرفت! 🚀",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("سرعت انیمیشن: «${speed.persianTitle}» ذخیره شد! ⚡✅")
    }

    fun toggleCameraFollow() {
        val newState = !_uiState.value.isCameraFollowEnabled
        prefs.edit().putBoolean("camera_follow_enabled", newState).apply()
        soundManager.playDiceTick(if (newState) 5 else 2)
        _uiState.update {
            it.copy(
                isCameraFollowEnabled = newState,
                eventBanner = GameEventBanner(
                    message = if (newState) {
                        "دنبال کردن ملایم مهره با دوربین هنگام حرکت فعال شد! 🎥✨"
                    } else {
                        "دنبال کردن مهره با دوربین غیرفعال شد (صفحه ثابت)."
                    },
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback(
            if (newState) "حرکت نرم دوربین با مهره فعال شد! 🎥✅" else "دوربین صفحه روی حالت ثابت قرار گرفت."
        )
    }

    fun selectMusicGenre(genre: MusicGenre) {
        prefs.edit()
            .putString("selected_music_genre", genre.name)
            .putBoolean("music_enabled", true)
            .apply()
        soundManager.setMusicEnabled(true)
        soundManager.setMusicGenre(genre, playTransitionMotif = true)
        _uiState.update {
            it.copy(
                selectedMusicGenre = genre,
                previewingMusicGenre = null,
                isMusicEnabled = true,
                eventBanner = GameEventBanner(
                    message = "تم موسیقی «${genre.emoji} ${genre.persianName}» با تغییر نرم (Crossfade) فعال شد! 🎵",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("تم موسیقی «${genre.persianName}» انتخاب و در حافظه ذخیره شد! 🎵✅")
    }

    fun previewMusicGenre(genre: MusicGenre?) {
        val currentPreview = _uiState.value.previewingMusicGenre
        val nextPreview = if (currentPreview == genre) null else genre
        if (nextPreview != null && !_uiState.value.isMusicEnabled) {
            soundManager.setMusicEnabled(true)
            prefs.edit().putBoolean("music_enabled", true).apply()
            _uiState.update { it.copy(isMusicEnabled = true) }
        }
        soundManager.previewMusicGenre(nextPreview)
        _uiState.update { it.copy(previewingMusicGenre = nextPreview) }
        if (nextPreview != null) {
            showCardSaveFeedback("در حال پخش پیش‌نمایش «${nextPreview.persianName}»... 🎧")
        }
    }

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        soundManager.updateSfxVolume(clamped)
        prefs.edit().putFloat("sfx_volume", clamped).apply()
        _uiState.update {
            it.copy(
                sfxVolume = clamped,
                isSoundEnabled = clamped > 0.01f || it.isSoundEnabled
            )
        }
    }

    fun triggerEventSoundDemo(eventType: GameEventSoundType) {
        if (!_uiState.value.isSoundEnabled) {
            soundManager.isSoundEnabled = true
            prefs.edit().putBoolean("sound_enabled", true).apply()
            _uiState.update { it.copy(isSoundEnabled = true) }
        }
        when (eventType) {
            GameEventSoundType.MATCH_START -> soundManager.playMatchStartFanfare()
            GameEventSoundType.DICE_ROLL -> soundManager.playDiceRollCascade()
            GameEventSoundType.LADDER_CLIMB -> soundManager.playLadderClimb()
            GameEventSoundType.SNAKE_SLIDE -> soundManager.playSnakeSlide()
            GameEventSoundType.TURN_CHANGE -> soundManager.playTurnChangeChime()
            GameEventSoundType.FINAL_VICTORY -> soundManager.playVictoryFanfare()
        }
        showCardSaveFeedback("پخش صدای رویداد: «${eventType.emoji} ${eventType.persianTitle}» 🔊")
    }

    fun resetStudioToDefaults() {
        prefs.edit()
            .putBoolean("env_effects_enabled", true)
            .putString("selected_env_effect", EnvironmentEffectType.GOLDEN_DUST.name)
            .putString("env_effect_intensity", EffectIntensity.MEDIUM.name)
            .putString("animation_quality", AnimationQuality.CINEMATIC.name)
            .putString("animation_speed", AnimationSpeed.SMOOTH.name)
            .putBoolean("camera_follow_enabled", true)
            .putString("selected_music_genre", MusicGenre.ROYAL.name)
            .putBoolean("music_enabled", true)
            .putBoolean("sound_enabled", true)
            .putBoolean("ambient_enabled", true)
            .putFloat("music_volume", 0.68f)
            .putFloat("sfx_volume", 0.85f)
            .putFloat("ambient_volume", 0.64f)
            .apply()

        soundManager.isSoundEnabled = true
        soundManager.updateSfxVolume(0.85f)
        soundManager.setMusicEnabled(true)
        soundManager.setAmbientEnabled(true)
        soundManager.setMusicVolume(0.68f)
        soundManager.setAmbientVolume(0.64f)
        soundManager.previewMusicGenre(null)
        soundManager.setMusicGenre(MusicGenre.ROYAL, playTransitionMotif = true)

        _uiState.update {
            it.copy(
                animationQuality = AnimationQuality.CINEMATIC,
                animationSpeed = AnimationSpeed.SMOOTH,
                isCameraFollowEnabled = true,
                isEnvironmentEffectsEnabled = true,
                selectedEnvironmentEffect = EnvironmentEffectType.GOLDEN_DUST,
                previewEnvironmentEffect = null,
                environmentEffectIntensity = EffectIntensity.MEDIUM,
                selectedMusicGenre = MusicGenre.ROYAL,
                previewingMusicGenre = null,
                isMusicEnabled = true,
                isSoundEnabled = true,
                isAmbientEnabled = true,
                musicVolume = 0.68f,
                sfxVolume = 0.85f,
                ambientVolume = 0.64f,
                eventBanner = GameEventBanner(
                    message = "استودیو جلوه‌ها، کیفیت انیمیشن و موسیقی به حالت پیش‌فرض بازگردانی شد! ✨🎵",
                    type = BannerType.NORMAL
                )
            )
        }
        showCardSaveFeedback("تنظیمات استودیو جلوه‌ها و موسیقی به حالت پیش‌فرض بازگردانی شد! 🔄✅")
    }

    // --- Phase 4 Truth or Dare Card Management & Snake Draw Methods ---

    fun openCardManager() {
        soundManager.playDiceTick(2)
        _uiState.update { it.copy(isCardManagerOpen = true, cardSaveFeedbackMessage = null) }
    }

    fun closeCardManager() {
        soundManager.playDiceTick(1)
        _uiState.update { it.copy(isCardManagerOpen = false, cardSaveFeedbackMessage = null) }
    }

    fun addTruthOrDareCard(title: String, body: String, category: CardCategory) {
        if (title.isBlank() || body.isBlank()) return
        viewModelScope.launch {
            cardRepository.addCard(title = title, body = body, category = category)
            soundManager.playCardSavedSound()
            showCardSaveFeedback("کارت «${title.trim()}» در دسته ${category.shortTitle} ذخیره شد! ✅")
        }
    }

    fun updateTruthOrDareCard(
        card: TruthOrDareCard,
        newTitle: String,
        newBody: String,
        newCategory: CardCategory
    ) {
        if (newTitle.isBlank() || newBody.isBlank()) return
        viewModelScope.launch {
            cardRepository.updateCard(
                id = card.id,
                title = newTitle,
                body = newBody,
                category = newCategory,
                createdAtMillis = card.createdAtMillis
            )
            soundManager.playCardSavedSound()
            showCardSaveFeedback("تغییرات کارت «${newTitle.trim()}» با موفقیت ذخیره شد! ✅")
        }
    }

    fun deleteTruthOrDareCard(cardId: Long) {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            soundManager.playDiceTick(0)
            showCardSaveFeedback("کارت از حافظه حذف شد.")
        }
    }

    fun toggleCardCategoryEnabled(category: CardCategory) {
        soundManager.playDiceTick(category.ordinal)
        _uiState.update { state ->
            val updated = state.enabledCardCategories.toMutableSet()
            if (category in updated) {
                if (updated.size > 1) {
                    updated.remove(category)
                }
            } else {
                updated.add(category)
            }
            cardRepository.saveEnabledCardCategories(updated)
            state.copy(enabledCardCategories = updated)
        }
    }

    fun playHeroStanceSound(stanceName: String) {
        soundManager.playHeroStanceSound(stanceName)
    }

    private fun showCardSaveFeedback(message: String) {
        feedbackClearJob?.cancel()
        _uiState.update { it.copy(cardSaveFeedbackMessage = message) }
        feedbackClearJob = viewModelScope.launch {
            delay(2800L)
            _uiState.update { it.copy(cardSaveFeedbackMessage = null) }
        }
    }

    fun flipRevealSnakeCard() {
        val currentDraw = _uiState.value.activeSnakeCardDraw ?: return
        if (currentDraw.isRevealed) return
        soundManager.playCardFlipRevealSound()
        _uiState.update {
            it.copy(
                activeSnakeCardDraw = currentDraw.copy(isRevealed = true)
            )
        }
    }

    fun completeSnakeCardChallenge() {
        soundManager.playSixBonusChime()
        _uiState.update { it.copy(activeSnakeCardDraw = null) }
        pendingSnakeCardCompletion?.complete(Unit)
    }

    fun setPlayerCount(count: Int) {
        val clamped = count.coerceIn(1, 8)
        prefs.edit().putInt("player_count", clamped).apply()
        soundManager.playDiceTick(clamped)
        _uiState.update {
            it.copy(
                playerCount = clamped,
                activeSetupPlayerSlot = it.activeSetupPlayerSlot.coerceIn(0, clamped - 1)
            )
        }
    }

    fun selectSetupPlayerSlot(slotIndex: Int) {
        val clamped = slotIndex.coerceIn(0, _uiState.value.playerCount - 1)
        soundManager.playDiceTick(clamped)
        _uiState.update { it.copy(activeSetupPlayerSlot = clamped) }
    }

    fun selectCharacterForSlot(playerSlot: Int, characterId: String) {
        if (playerSlot !in 0..7) return
        val state = _uiState.value
        val takenByOtherActive = (0 until state.playerCount).any { idx ->
            idx != playerSlot && state.playerCharacterIds[idx] == characterId
        }
        if (takenByOtherActive) return

        val oldChar = state.resolveCharacterById(state.playerCharacterIds[playerSlot])
        val newChar = state.resolveCharacterById(characterId)
        val currentName = state.playerNamesInput[playerSlot].trim()
        val shouldUpdateDefaultName = currentName.isEmpty() ||
            currentName.equals(oldChar.defaultEnglishName, ignoreCase = true)

        val updatedCharIds = state.playerCharacterIds.toMutableList()
        val existingSlot = updatedCharIds.indexOf(characterId)
        if (existingSlot != -1 && existingSlot != playerSlot) {
            updatedCharIds[existingSlot] = oldChar.id
            prefs.edit().putString("player_char_$existingSlot", oldChar.id).apply()
        }
        updatedCharIds[playerSlot] = characterId
        prefs.edit().putString("player_char_$playerSlot", characterId).apply()

        val updatedNames = state.playerNamesInput.toMutableList()
        if (shouldUpdateDefaultName) {
            updatedNames[playerSlot] = newChar.defaultEnglishName
            prefs.edit().putString("player_name_$playerSlot", newChar.defaultEnglishName).apply()
        }

        soundManager.playAbilityActivateSound()

        val nextSlot = if (playerSlot + 1 < state.playerCount) playerSlot + 1 else playerSlot

        _uiState.update {
            it.copy(
                playerCharacterIds = updatedCharIds,
                playerNamesInput = updatedNames,
                activeSetupPlayerSlot = nextSlot
            )
        }
    }

    fun updatePlayerName(index: Int, newName: String) {
        if (index !in 0..7) return
        val sanitized = newName.take(18)
        prefs.edit().putString("player_name_$index", sanitized).apply()
        _uiState.update { state ->
            val updated = state.playerNamesInput.toMutableList()
            updated[index] = sanitized
            state.copy(playerNamesInput = updated)
        }
    }

    fun resetPlayerNameToCharacterDefault(index: Int) {
        if (index !in 0..7) return
        val state = _uiState.value
        val charId = state.playerCharacterIds[index]
        val defaultName = state.resolveCharacterById(charId).defaultEnglishName
        updatePlayerName(index, defaultName)
    }

    fun setDifficulty(difficulty: DifficultyLevel) {
        prefs.edit().putString("difficulty_level", difficulty.name).apply()
        soundManager.playDiceTick(difficulty.ordinal + 2)
        _uiState.update { state ->
            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = state.maxTiles,
                difficulty = difficulty,
                enabledObstacles = state.enabledObstacles
            )
            state.copy(
                difficulty = difficulty,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles
            )
        }
    }

    fun toggleObstacleType(type: ObstacleType) {
        soundManager.playDiceTick(type.ordinal)
        _uiState.update { state ->
            val updated = state.enabledObstacles.toMutableSet()
            if (type in updated) {
                updated.remove(type)
            } else {
                updated.add(type)
            }
            prefs.edit().putStringSet(
                "enabled_obstacles",
                updated.map { it.name }.toSet()
            ).apply()

            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = state.maxTiles,
                difficulty = state.difficulty,
                enabledObstacles = updated
            )
            state.copy(
                enabledObstacles = updated,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles
            )
        }
    }

    fun toggleSound() {
        val newState = !soundManager.isSoundEnabled
        soundManager.isSoundEnabled = newState
        prefs.edit().putBoolean("sound_enabled", newState).apply()
        if (newState) {
            soundManager.playDiceTick(2)
        }
        _uiState.update { it.copy(isSoundEnabled = newState) }
    }

    fun toggleMusic() {
        val newState = !_uiState.value.isMusicEnabled
        soundManager.setMusicEnabled(newState)
        prefs.edit().putBoolean("music_enabled", newState).apply()
        if (newState) {
            soundManager.playDiceTick(3)
        }
        _uiState.update { it.copy(isMusicEnabled = newState) }
    }

    fun toggleAmbient() {
        val newState = !_uiState.value.isAmbientEnabled
        soundManager.setAmbientEnabled(newState)
        prefs.edit().putBoolean("ambient_enabled", newState).apply()
        if (newState) {
            soundManager.playDiceTick(4)
        }
        _uiState.update { it.copy(isAmbientEnabled = newState) }
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        soundManager.setMusicVolume(clamped)
        prefs.edit().putFloat("music_volume", clamped).apply()
        _uiState.update {
            it.copy(
                musicVolume = clamped,
                isMusicEnabled = clamped > 0.01f || it.isMusicEnabled
            )
        }
    }

    fun setAmbientVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        soundManager.setAmbientVolume(clamped)
        prefs.edit().putFloat("ambient_volume", clamped).apply()
        _uiState.update {
            it.copy(
                ambientVolume = clamped,
                isAmbientEnabled = clamped > 0.01f || it.isAmbientEnabled
            )
        }
    }

    fun pauseBackgroundMusic() {
        soundManager.pauseBackgroundMusic()
    }

    fun resumeBackgroundMusic() {
        soundManager.resumeBackgroundMusic()
    }

    fun regenerateBoardLayout() {
        if (_uiState.value.isDiceRolling || _uiState.value.isTokenAnimating) return
        soundManager.playDiceTick(4)
        _uiState.update { state ->
            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = state.maxTiles,
                difficulty = state.difficulty,
                enabledObstacles = state.enabledObstacles
            )
            state.copy(
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles,
                eventBanner = GameEventBanner(
                    message = "چیدمان صفحه ${state.maxTiles.toPersianDigits()} تایی (${state.difficulty.persianTitle}) از نو ساخته شد!",
                    type = BannerType.NORMAL
                )
            )
        }
    }

    fun startGame() {
        aiTurnJob?.cancel()
        pendingMovementCompletion?.complete(Unit)
        pendingSnakeCardCompletion?.complete(Unit)
        val current = _uiState.value

        val humanPlayers = (0 until current.playerCount).map { idx ->
            val charId = current.playerCharacterIds.getOrElse(idx) {
                CharacterRoster.allCharacters[idx].id
            }
            val character = current.resolveCharacterById(charId)
            val rawName = current.playerNamesInput.getOrNull(idx)?.trim().orEmpty()
            val finalName = rawName.ifEmpty { character.defaultEnglishName }
            val skinId = current.playerPawnSkinIds.getOrElse(idx) { "classic_standard" }
            Player(
                id = idx,
                name = finalName,
                colorIndex = idx,
                characterId = charId,
                customCharacterOverride = if (character.isCustom) character else null,
                isAiBot = false,
                currentTile = 1,
                abilityChargeProgress = character.abilityType.cooldownTurns.coerceAtLeast(3),
                isShieldCharged = (character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD),
                customPawnSkinId = skinId
            )
        }

        // Phase 5: In Single-Player mode (playerCount == 1), add an intelligent AI opponent!
        val activePlayers = if (current.playerCount == 1) {
            val humanCharId = humanPlayers.first().characterId
            val aiCandidates = CharacterRoster.allCharacters.filter { it.id != humanCharId }
            val aiCharacter = when (current.aiDifficulty) {
                AiDifficulty.EASY -> aiCandidates.firstOrNull { it.abilityType == CharacterAbilityType.GUARDIAN_SHIELD }
                AiDifficulty.MEDIUM -> aiCandidates.firstOrNull { it.abilityType == CharacterAbilityType.SPEEDSTER }
                AiDifficulty.HARD -> aiCandidates.firstOrNull { it.abilityType == CharacterAbilityType.HIGH_PRECISION }
            } ?: aiCandidates.first()

            val aiShortLevel = current.aiDifficulty.persianTitle.substringBefore(" ")
            val aiSkinId = current.playerPawnSkinIds.getOrElse(1) { "fantasy_plasma" }
            val aiBot = Player(
                id = 1,
                name = "🤖 ربات ($aiShortLevel)",
                colorIndex = 1,
                characterId = aiCharacter.id,
                customCharacterOverride = null,
                isAiBot = true,
                currentTile = 1,
                abilityChargeProgress = aiCharacter.abilityType.cooldownTurns.coerceAtLeast(3),
                isShieldCharged = (aiCharacter.abilityType == CharacterAbilityType.GUARDIAN_SHIELD),
                customPawnSkinId = aiSkinId
            )
            listOf(humanPlayers.first(), aiBot)
        } else {
            humanPlayers
        }

        val boardConfig = BoardGenerator.generateBoardConfiguration(
            maxTiles = current.maxTiles,
            difficulty = current.difficulty,
            enabledObstacles = current.enabledObstacles
        )

        soundManager.setVictoryMusicOverride(false)
        soundManager.playMatchStartFanfare()

        val firstPlayer = activePlayers.first()
        _uiState.update {
            it.copy(
                phase = GamePhase.PLAYING,
                players = activePlayers,
                boardLinks = boardConfig.links,
                boardObstacles = boardConfig.obstacles,
                currentTurnIndex = 0,
                diceValue = 6,
                isDiceRolling = false,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                activeSnakeCardDraw = null,
                winner = null,
                eventBanner = GameEventBanner(
                    message = "بازی در صفحه ${current.maxTiles.toPersianDigits()} تایی آغاز شد! نوبت ${firstPlayer.name} است 🎲",
                    type = BannerType.NORMAL
                )
            )
        }
    }

    fun restartCurrentMatch() {
        aiTurnJob?.cancel()
        pendingMovementCompletion?.complete(Unit)
        pendingSnakeCardCompletion?.complete(Unit)
        val current = _uiState.value
        if (current.players.isEmpty()) {
            startGame()
            return
        }
        val resetPlayers = current.players.map { p ->
            p.copy(
                currentTile = 1,
                totalSixes = 0,
                laddersClimbed = 0,
                snakesBitten = 0,
                obstaclesTriggered = 0,
                wormCurseTurnsRemaining = 0,
                swampSkipTurnsRemaining = 0,
                halveNextRoll = false,
                abilityChargeProgress = p.character.abilityType.cooldownTurns.coerceAtLeast(3),
                isShieldCharged = (p.character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD),
                hasUsedOnceAbility = false,
                isActiveAbilityArmedForTurn = false,
                frozenSkipTurnsRemaining = 0,
                personalTurnsTaken = 0,
                leaderSwapNextChargeTurn = 10,
                leaderSwapWindowTurnsRemaining = 0,
                abilityUsesCount = 0,
                previousTurnTile = 1,
                dragonSoulBoostTurns = 0,
                berserkerRageTurns = 0,
                phantomPhaseActive = false,
                guardianAngelProtected = false,
                illusionDecoyTile = null,
                illusionTurnsRemaining = 0,
                gravityCapNextRoll = false,
                pinnedSlowSteps = 0
            )
        }
        val newBoard = BoardGenerator.generateBoardConfiguration(
            maxTiles = current.maxTiles,
            difficulty = current.difficulty,
            enabledObstacles = current.enabledObstacles
        )
        soundManager.setVictoryMusicOverride(false)
        soundManager.playMatchStartFanfare()
        _uiState.update {
            it.copy(
                phase = GamePhase.PLAYING,
                players = resetPlayers,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles,
                currentTurnIndex = 0,
                diceValue = 6,
                isDiceRolling = false,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                activeSnakeCardDraw = null,
                winner = null,
                eventBanner = GameEventBanner(
                    message = "بازی از نو آغاز شد! نوبت ${resetPlayers.first().name} است 🎲",
                    type = BannerType.NORMAL
                )
            )
        }
    }

    fun returnToSetup() {
        aiTurnJob?.cancel()
        pendingMovementCompletion?.complete(Unit)
        pendingSnakeCardCompletion?.complete(Unit)
        soundManager.setVictoryMusicOverride(false)
        soundManager.playDiceTick(1)
        _uiState.update {
            it.copy(
                phase = GamePhase.SETUP,
                isDiceRolling = false,
                isTokenAnimating = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                sixRollGlowActive = false,
                activeSnakeCardDraw = null,
                winner = null
            )
        }
    }

    /**
     * Advances a single player's personal turn counter (`personalTurnsTaken`) and updates:
     * - The 10-personal-turn Leader Swap state machine (`leaderSwapNextChargeTurn`, `leaderSwapWindowTurnsRemaining`):
     *   * Charges at personal turn 10 (20, 30, or `usedTurn + 10`) with a 2-personal-turn opportunity window (`leaderSwapWindowTurnsRemaining = 2`).
     *   * If not used within those 2 personal turns, the window expires (`leaderSwapWindowTurnsRemaining = 0`) and next charge is set to the next multiple of 10 (e.g., turn 20).
     * - Cooldown progress (`abilityChargeProgress`) and Shield recharge (`isShieldCharged`).
     * - Temporary duration buffs (`illusionTurnsRemaining`).
     */
    private fun advancePersonalTurnState(player: Player, wasAbilityArmed: Boolean): Player {
        val ability = player.character.abilityType
        val newPersonalTurn = player.personalTurnsTaken + 1

        // 1. Update 10-Personal-Turn Leader Swap state machine
        var nextChargeTurn = player.leaderSwapNextChargeTurn
        var windowRemaining = player.leaderSwapWindowTurnsRemaining

        if (ability == CharacterAbilityType.SHADOW_WALKER) {
            if (windowRemaining > 0) {
                // Player had an active 2-turn window and completed a personal turn without using it
                windowRemaining -= 1
                if (windowRemaining == 0) {
                    // Window expired after 2 personal turns! Next charge is at the next 10-turn milestone (e.g. turn 20)
                    nextChargeTurn = ((newPersonalTurn / 10) + 1) * 10
                }
            } else if (newPersonalTurn >= nextChargeTurn) {
                // Reached personal turn 10 (or 20, 30, or T+10 after previous use): open 2-personal-turn opportunity window!
                windowRemaining = 2
            }
        }

        // 2. Standard cooldown progress & Guardian Shield 3-turn recharge
        val maxCooldown = ability.cooldownTurns.coerceAtLeast(3)
        val newChargeProgress = if (wasAbilityArmed) {
            0
        } else {
            (player.abilityChargeProgress + 1).coerceAtMost(maxCooldown)
        }
        val rechargedShield = if (ability == CharacterAbilityType.GUARDIAN_SHIELD && !player.isShieldCharged) {
            newChargeProgress >= 3
        } else {
            player.isShieldCharged
        }

        val newIllusionTurns = (player.illusionTurnsRemaining - 1).coerceAtLeast(0)
        val newIllusionTile = if (newIllusionTurns > 0) player.illusionDecoyTile else null

        return player.copy(
            personalTurnsTaken = newPersonalTurn,
            leaderSwapNextChargeTurn = nextChargeTurn,
            leaderSwapWindowTurnsRemaining = windowRemaining,
            abilityChargeProgress = newChargeProgress,
            isShieldCharged = rechargedShield,
            illusionTurnsRemaining = newIllusionTurns,
            illusionDecoyTile = newIllusionTile
        )
    }

    /**
     * Evaluates Guardian Shield, Guardian Angel, or Illusionist Mirage defense for [defenderId]
     * against [threatCategory] (SNAKE, NEGATIVE_OBSTACLE, EXECUTIONER_OR_ABILITY).
     *
     * Returns `true` if the threat was successfully blocked/neutralized, or `false` if defense failed/absent.
     */
    private suspend fun evaluateShieldOrProtectionDefense(
        defenderId: Int,
        threatCategory: ShieldThreatCategory,
        threatTile: Int,
        threatNamePersian: String,
        deterministicRollOverride: Int? = null,
        skipDelay: Boolean = false
    ): Boolean {
        val state = _uiState.value
        val defender = state.players.find { it.id == defenderId } ?: return false

        // 1. Check Illusionist Mirage Decoy (for enemy abilities / Executioner hunt)
        if (threatCategory == ShieldThreatCategory.EXECUTIONER_OR_ABILITY &&
            defender.illusionDecoyTile != null &&
            defender.illusionTurnsRemaining > 0
        ) {
            soundManager.playAbilityActivateSound()
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == defenderId) {
                        p.copy(illusionDecoyTile = null, illusionTurnsRemaining = 0)
                    } else p
                }
                st.copy(
                    players = updated,
                    highlightEffect = BoardHighlightEffect(
                        tile = threatTile,
                        isPositive = true,
                        intensity = 1f,
                        heroFxType = HeroBoardFxType.ILLUSION_MIRAGE
                    ),
                    eventBanner = GameEventBanner(
                        message = "سراب توهم‌زا (Illusionist)! 🪄 حملهٔ «$threatNamePersian» به بدل توهمی ${defender.name} برخورد کرد و موقعیت واقعی او تغییر نکرد!",
                        type = BannerType.CHARACTER_ABILITY
                    )
                )
            }
            if (!skipDelay) delay(850L)
            return true
        }

        // 2. Check Guardian Angel Protection (`Stella`)
        if (defender.guardianAngelProtected) {
            soundManager.playAbilityActivateSound()
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == defenderId) p.copy(guardianAngelProtected = false) else p
                }
                st.copy(
                    players = updated,
                    highlightEffect = BoardHighlightEffect(
                        tile = threatTile,
                        isPositive = true,
                        intensity = 1f,
                        heroFxType = HeroBoardFxType.GUARDIAN_ANGEL_WINGS
                    ),
                    eventBanner = GameEventBanner(
                        message = "سپر فرشتهٔ محافظ (Guardian Angel)! 🌟 بال‌های نورانی از ${defender.name} در برابر «$threatNamePersian» محافظت کامل کرد!",
                        type = BannerType.CHARACTER_ABILITY
                    )
                )
            }
            if (!skipDelay) delay(850L)
            return true
        }

        // 3. Check Phantom Phase (`Zephyr`) against snakes & negative obstacles
        if (defender.phantomPhaseActive &&
            (threatCategory == ShieldThreatCategory.SNAKE || threatCategory == ShieldThreatCategory.NEGATIVE_OBSTACLE)
        ) {
            soundManager.playAbilityActivateSound()
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == defenderId) p.copy(phantomPhaseActive = false) else p
                }
                st.copy(
                    players = updated,
                    highlightEffect = BoardHighlightEffect(
                        tile = threatTile,
                        isPositive = true,
                        intensity = 1f,
                        heroFxType = HeroBoardFxType.PHANTOM_PHASE
                    ),
                    eventBanner = GameEventBanner(
                        message = "گام شبح باد (Phantom)! 👻🌪️ ${defender.name} شبح‌وار از میان «$threatNamePersian» عبور کرد و آسیبی ندید!",
                        type = BannerType.CHARACTER_ABILITY
                    )
                )
            }
            if (!skipDelay) delay(850L)
            return true
        }

        // 4. Check Guardian Shield (`Jack` or any hero with `isShieldCharged` when `GUARDIAN_SHIELD` ability is active)
        val hasChargedShield = defender.isShieldCharged &&
            defender.character.abilityType == CharacterAbilityType.GUARDIAN_SHIELD

        if (!hasChargedShield) return false

        val baseConfiguredPct = when (threatCategory) {
            ShieldThreatCategory.SNAKE -> state.shieldSnakeDefensePercent
            ShieldThreatCategory.NEGATIVE_OBSTACLE -> state.shieldObstacleDefensePercent
            ShieldThreatCategory.EXECUTIONER_OR_ABILITY -> state.shieldHuntDefensePercent
        }
        val stats = defender.character.heroBlueprint.stats
        val effectiveChance = stats.computeShieldDefenseChance(baseConfiguredPct, threatCategory)
        val roll = deterministicRollOverride ?: Random.nextInt(100)

        return if (roll < effectiveChance) {
            // Defense SUCCEEDED! Check Critical Parry (preserves shield without consuming it)
            val isCriticalParry = roll < stats.criticalParryPreserveShieldPercent
            soundManager.playAbilityActivateSound()
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == defenderId) {
                        if (isCriticalParry) {
                            p.copy(isShieldCharged = true)
                        } else {
                            p.copy(isShieldCharged = false, abilityChargeProgress = 0)
                        }
                    } else p
                }
                val consumptionNote = if (isCriticalParry) {
                    "دفع بی‌نقص بدون مصرف سپر! ✨"
                } else {
                    "سپر مصرف شد (شارژ مجدد: ۳ نوبت)"
                }
                st.copy(
                    players = updated,
                    highlightEffect = BoardHighlightEffect(
                        tile = threatTile,
                        isPositive = true,
                        intensity = 1f,
                        heroFxType = HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                    ),
                    eventBanner = GameEventBanner(
                        message = "دفع موفق سپر نورانی (${effectiveChance.toPersianDigits()}٪)! 🛡️✨ ${defender.name} در برابر «$threatNamePersian» محافظت شد — $consumptionNote",
                        type = BannerType.CHARACTER_ABILITY
                    )
                )
            }
            if (!skipDelay) delay(880L)
            true
        } else {
            // Defense FAILED! Do NOT consume shield charge so player is not doubly penalized, show failure FX
            soundManager.playDiceTick(0)
            _uiState.update { st ->
                st.copy(
                    highlightEffect = BoardHighlightEffect(
                        tile = threatTile,
                        isPositive = false,
                        intensity = 1f,
                        heroFxType = HeroBoardFxType.SHIELD_BREAK_FAIL
                    ),
                    eventBanner = GameEventBanner(
                        message = "شکست دفاع سپر (${effectiveChance.toPersianDigits()}٪)! 💥 شدت «$threatNamePersian» از سپر ${defender.name} عبور کرد (سپر برای نوبت بعد حفظ شد)!",
                        type = BannerType.OBSTACLE_DANGER
                    )
                )
            }
            if (!skipDelay) delay(700L)
            false
        }
    }

    /**
     * Resolves Hero Collision & Combat Interactions on a tile:
     * When two or more hero tokens meet on a tile:
     * - Evaluates active abilities, passive protections, and defense probabilities via HeroCombatEngine.
     * - Dispatches atomic resolution snapshot (defenses, critical parries, knockbacks, berserker rage).
     * - Appends to Combat Log (`combatLogs`).
     * - Updates EventFeedbackBanner with clear outcome messaging.
     */
    private suspend fun resolveTileCombatAndInteraction(
        movingPlayerId: Int,
        landedTile: Int,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null,
        skipAnimationForTest: Boolean = false
    ): Boolean {
        val state = _uiState.value
        val maxTiles = state.maxTiles
        if (landedTile <= 1 || landedTile >= maxTiles) return false

        val attacker = state.players.find { it.id == movingPlayerId } ?: return false
        val rivalsOnTile = state.players.filter { it.id != movingPlayerId && it.currentTile == landedTile }
        if (rivalsOnTile.isEmpty()) return false

        var anyCombatHappened = false

        for (defender in rivalsOnTile) {
            val resolution = HeroCombatEngine.resolveTileCollision(
                attacker = attacker,
                defender = defender,
                tile = landedTile,
                forcedAttackRoll = forcedAttackRoll,
                forcedDefenseRoll = forcedDefenseRoll,
                baseShieldProbability = state.shieldHuntDefensePercent
            )

            // Create combat log entry
            val currentTurnNum = attacker.personalTurnsTaken + 1
            val logEntry = CombatLogEntry(
                turnNumber = currentTurnNum,
                attackerId = attacker.id,
                attackerName = attacker.name,
                attackerHeroClass = attacker.character.heroBlueprint.primaryClass,
                defenderId = defender.id,
                defenderName = defender.name,
                defenderHeroClass = defender.character.heroBlueprint.primaryClass,
                tile = landedTile,
                outcomeType = resolution.outcomeType,
                description = resolution.combatLogMessage,
                attackerTileBefore = landedTile,
                attackerTileAfter = resolution.attackerNewTile,
                defenderTileBefore = landedTile,
                defenderTileAfter = resolution.defenderNewTile,
                attackRoll = resolution.attackRoll,
                defenseRoll = resolution.defenseRoll
            )

            val fxType = when (resolution.outcomeType) {
                CombatOutcomeType.ATTACK_HIT -> HeroBoardFxType.EXECUTIONER_SLASH
                CombatOutcomeType.SHIELD_PARRY, CombatOutcomeType.CRITICAL_PARRY -> HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                CombatOutcomeType.ANGEL_PROTECTED -> HeroBoardFxType.GUARDIAN_ANGEL_WINGS
                CombatOutcomeType.ILLUSION_DIVERTED -> HeroBoardFxType.ILLUSION_MIRAGE
                CombatOutcomeType.PHANTOM_EVADED -> HeroBoardFxType.PHANTOM_PHASE
                CombatOutcomeType.RESISTANCE_MITIGATED -> HeroBoardFxType.DRAGON_SOUL_AURA
                CombatOutcomeType.ANCHOR_IMMUNE -> HeroBoardFxType.SHIELD_BREAK_FAIL
                else -> HeroBoardFxType.NONE
            }

            // Atomic state update
            _uiState.update { st ->
                val updatedPlayers = st.players.map { p ->
                    when (p.id) {
                        defender.id -> p.copy(
                            previousTurnTile = defender.currentTile,
                            isShieldCharged = if (resolution.defenderShieldConsumed) false else p.isShieldCharged,
                            abilityChargeProgress = if (resolution.defenderShieldConsumed) 0 else p.abilityChargeProgress,
                            guardianAngelProtected = if (resolution.defenderAngelConsumed) false else p.guardianAngelProtected,
                            phantomPhaseActive = if (resolution.defenderPhantomConsumed) false else p.phantomPhaseActive,
                            illusionDecoyTile = if (resolution.defenderDecoyConsumed) null else p.illusionDecoyTile,
                            illusionTurnsRemaining = if (resolution.defenderDecoyConsumed) 0 else p.illusionTurnsRemaining,
                            berserkerRageTurns = if (resolution.defenderEntersBerserkerRage) 2 else p.berserkerRageTurns,
                            dragonSoulBoostTurns = if (resolution.defenderEntersDragonSoulBoost) 2 else p.dragonSoulBoostTurns
                        )
                        else -> p
                    }
                }

                val updatedLogs = (st.combatLogs + logEntry).takeLast(60)

                st.copy(
                    players = updatedPlayers,
                    combatLogs = updatedLogs,
                    isTokenAnimating = !skipAnimationForTest && resolution.defenderNewTile != landedTile,
                    highlightEffect = if (fxType != HeroBoardFxType.NONE) {
                        BoardHighlightEffect(
                            tile = landedTile,
                            isPositive = resolution.outcomeType != CombatOutcomeType.ATTACK_HIT,
                            intensity = 1f,
                            heroFxType = fxType
                        )
                    } else null,
                    eventBanner = GameEventBanner(
                        message = resolution.bannerMessage,
                        type = when (resolution.outcomeType) {
                            CombatOutcomeType.ATTACK_HIT -> BannerType.CHARACTER_ABILITY
                            CombatOutcomeType.SHIELD_PARRY, CombatOutcomeType.CRITICAL_PARRY -> BannerType.CHARACTER_ABILITY
                            else -> BannerType.NORMAL
                        }
                    )
                )
            }

            if (resolution.outcomeType == CombatOutcomeType.ATTACK_HIT ||
                resolution.outcomeType == CombatOutcomeType.SHIELD_PARRY ||
                resolution.outcomeType == CombatOutcomeType.CRITICAL_PARRY
            ) {
                soundManager.playAbilityActivateSound()
            }

            if (!skipAnimationForTest && resolution.defenderNewTile < landedTile) {
                awaitComposeTokenMovement(
                    playerId = defender.id,
                    pathTiles = listOf(landedTile, resolution.defenderNewTile),
                    movementType = TokenMovementType.EXECUTIONER_HUNT
                )
            }
            updatePlayerTileInState(defender.id, resolution.defenderNewTile)
            anyCombatHappened = true
        }

        if (!skipAnimationForTest) {
            _uiState.update { it.copy(highlightEffect = null) }
        }
        return anyCombatHappened
    }

    /**
     * Backward-compatible helper for legacy test calls and executioner co-tile hunt
     */
    private suspend fun checkAndExecuteCoTileHunt(
        hunterId: Int,
        landedTile: Int,
        deterministicRollOverride: Int? = null,
        skipAnimationForTest: Boolean = false
    ): Boolean {
        return resolveTileCombatAndInteraction(
            movingPlayerId = hunterId,
            landedTile = landedTile,
            forcedAttackRoll = deterministicRollOverride,
            forcedDefenseRoll = deterministicRollOverride,
            skipAnimationForTest = skipAnimationForTest
        )
    }

    /**
     * Activates the current player's character ability on their turn!
     */
    fun activateCharacterAbility() {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING ||
            state.isDiceRolling ||
            state.isTokenAnimating ||
            state.pendingPrecisionChoice != null
        ) return

        val currentPlayer = state.currentPlayer ?: return
        if (!currentPlayer.canTriggerActiveAbilityNow) return

        val ability = currentPlayer.character.abilityType
        soundManager.playAbilityActivateSound()

        viewModelScope.launch {
            val maxT = _uiState.value.maxTiles

            when (ability) {
                // 1. JACK — Guardian Shield Active Shockwave Pulse & Shield Reinforcement
                CharacterAbilityType.GUARDIAN_SHIELD -> {
                    val nearbyRivals = _uiState.value.players.filter {
                        it.id != currentPlayer.id &&
                            it.currentTile > 1 &&
                            abs(it.currentTile - currentPlayer.currentTile) <= 4
                    }
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(
                                    isShieldCharged = true,
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1
                                )
                                nearbyRivals.any { it.id == p.id } &&
                                    p.character.abilityType != CharacterAbilityType.EARTH_TITAN &&
                                    p.character.abilityType != CharacterAbilityType.HALF_REVERSE -> {
                                    p.copy(
                                        previousTurnTile = p.currentTile,
                                        currentTile = (p.currentTile - 2).coerceAtLeast(2)
                                    )
                                }
                                else -> p
                            }
                        }
                        val pushedNames = nearbyRivals.joinToString("، ") { it.name }
                        val msg = if (nearbyRivals.isNotEmpty()) {
                            "گنبد سپر نورانی و موج بازتاب! 🛡️✨ سپر ${currentPlayer.name} شارژ شد و موج نور، $pushedNames را ۲ خانه به عقب راند!"
                        } else {
                            "گنبد سپر نورانی! 🛡️✨ سپر محافظ ${currentPlayer.name} با حداکثر قدرت شارژ و آمادهٔ دفع خطر شد!"
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = currentPlayer.currentTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                            ),
                            eventBanner = GameEventBanner(
                                message = msg,
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 2. AXEL — Short-Range Tactical Shadow Step (1..10 tiles) or Sonic Overdrive (4..6)
                CharacterAbilityType.SPEEDSTER -> {
                    val nearbyTarget = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                (it.currentTile - currentPlayer.currentTile) in 1..10 &&
                                it.character.abilityType != CharacterAbilityType.HALF_REVERSE &&
                                it.character.abilityType != CharacterAbilityType.EARTH_TITAN
                        }
                        .maxByOrNull { it.currentTile }

                    if (nearbyTarget != null) {
                        val myOld = currentPlayer.currentTile
                        val targetOld = nearbyTarget.currentTile
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    p.copy(
                                        abilityChargeProgress = 0,
                                        abilityUsesCount = p.abilityUsesCount + 1
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                isTokenAnimating = true,
                                highlightEffect = BoardHighlightEffect(
                                    tile = targetOld,
                                    isPositive = true,
                                    intensity = 1f
                                ),
                                eventBanner = GameEventBanner(
                                    message = "گام سایهٔ کوتاه‌برد! ⚡🌑 ${currentPlayer.name} در یک چشم به هم زدن جای خود را با ${nearbyTarget.name} (خانه ${targetOld.toPersianDigits()}) عوض کرد!",
                                    type = BannerType.CHARACTER_ABILITY
                                )
                            )
                        }
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(myOld, targetOld),
                            movementType = TokenMovementType.SHADOW_SWAP
                        )
                        _uiState.update { st ->
                            val swapped = st.players.map { p ->
                                when (p.id) {
                                    currentPlayer.id -> p.copy(currentTile = targetOld)
                                    nearbyTarget.id -> p.copy(previousTurnTile = targetOld, currentTile = myOld)
                                    else -> p
                                }
                            }
                            st.copy(
                                players = swapped,
                                isTokenAnimating = false,
                                highlightEffect = null
                            )
                        }
                    } else {
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    p.copy(
                                        isActiveAbilityArmedForTurn = true,
                                        abilityChargeProgress = 0,
                                        abilityUsesCount = p.abilityUsesCount + 1
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                eventBanner = GameEventBanner(
                                    message = "شتاب مافوق صوت! ⚡ پرتاب بعدی ${currentPlayer.name} تضمینی بین ۴ تا ۶ است و رقبای عبوری ۲ خانه عقب می‌روند!",
                                    type = BannerType.CHARACTER_ABILITY
                                )
                            )
                        }
                    }
                }

                // 3. NOVA — High Precision + Pinning Arrow
                CharacterAbilityType.HIGH_PRECISION -> {
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    isActiveAbilityArmedForTurn = true,
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1
                                )
                            } else p
                        }
                        st.copy(
                            players = updated,
                            eventBanner = GameEventBanner(
                                message = "قابلیت «${ability.persianTitle}» فعال شد! 🎯 حالا تاس بریزید تا ۲ تاس هم‌زمان پرتاب شود و تیر نوری به سمت پیشتاز شلیک گردد!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                }

                // 4. BLADE — Executioner Predator Lunge & Co-Tile Hunt
                CharacterAbilityType.SNAKE_HUNTER -> {
                    val rivalInLungeRange = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                (it.currentTile - currentPlayer.currentTile) in 1..3 &&
                                it.currentTile < maxT
                        }
                        .maxByOrNull { it.currentTile }

                    val destTile = rivalInLungeRange?.currentTile
                        ?: (currentPlayer.currentTile + 2).coerceAtMost(maxT - 1)

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1
                                )
                            } else p
                        }
                        st.copy(
                            players = updated,
                            isTokenAnimating = true,
                            highlightEffect = BoardHighlightEffect(
                                tile = destTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.EXECUTIONER_SLASH
                            ),
                            eventBanner = GameEventBanner(
                                message = if (rivalInLungeRange != null) {
                                    "خیزش شکارچی (Executioner)! ⚔️ ${currentPlayer.name} به خانهٔ ${rivalInLungeRange.name} (${destTile.toPersianDigits()}) یورش برد!"
                                } else {
                                    "خیزش شمشیر افعی‌کش! ⚔️ ${currentPlayer.name} ۲ خانه به جلو خیز برداشت!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }

                    if (destTile > currentPlayer.currentTile) {
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(currentPlayer.currentTile, destTile),
                            movementType = TokenMovementType.STEP_BY_STEP
                        )
                        updatePlayerTileInState(currentPlayer.id, destTile)
                    }

                    // Immediately execute Co-Tile Hunt if Blade landed on a rival!
                    checkAndExecuteCoTileHunt(
                        hunterId = currentPlayer.id,
                        landedTile = destTile
                    )

                    _uiState.update { it.copy(isTokenAnimating = false, highlightEffect = null) }
                }

                // 5. RAVEN — 10-Personal-Turn Leader Swap (with 2-turn window & live target validation)
                CharacterAbilityType.SHADOW_WALKER -> {
                    val liveLeader = _uiState.value.players
                        .filter { it.id != currentPlayer.id && it.currentTile > currentPlayer.currentTile }
                        .maxByOrNull { it.currentTile }

                    if (liveLeader == null) {
                        _uiState.update {
                            it.copy(
                                eventBanner = GameEventBanner(
                                    message = "در حال حاضر شما نفر اول هستید یا هیچ بازیکنی جلوتر از شما نیست! (فرصت جابه‌جایی حفظ شد)",
                                    type = BannerType.OBSTACLE_DANGER
                                )
                            )
                        }
                        return@launch
                    }

                    val myOldTile = currentPlayer.currentTile
                    val leaderOldTile = liveLeader.currentTile
                    val nextPersonalChargeTurn = currentPlayer.personalTurnsTaken + 10

                    // Check if leader blocks with Shield / Angel / Mirage
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    leaderSwapWindowTurnsRemaining = 0,
                                    leaderSwapNextChargeTurn = nextPersonalChargeTurn,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    hasUsedOnceAbility = true
                                )
                            } else p
                        }
                        st.copy(players = updated, isTokenAnimating = true)
                    }

                    val blockedByDefense = evaluateShieldOrProtectionDefense(
                        defenderId = liveLeader.id,
                        threatCategory = ShieldThreatCategory.EXECUTIONER_OR_ABILITY,
                        threatTile = leaderOldTile,
                        threatNamePersian = "جابه‌جایی با نفر اول (${currentPlayer.name})"
                    )
                    if (blockedByDefense) {
                        _uiState.update { it.copy(isTokenAnimating = false, highlightEffect = null) }
                        return@launch
                    }

                    _uiState.update { st ->
                        st.copy(
                            eventBanner = GameEventBanner(
                                message = "جابه‌جایی با نفر اول! 🌑 ${currentPlayer.name} جای خود را با نفر اول (${liveLeader.name} در خانه ${leaderOldTile.toPersianDigits()}) عوض کرد! (شارژ بعدی: نوبت اختصاصی ${nextPersonalChargeTurn.toPersianDigits()})",
                                type = BannerType.CHARACTER_ABILITY
                            ),
                            highlightEffect = BoardHighlightEffect(
                                tile = leaderOldTile,
                                isPositive = true,
                                intensity = 1f
                            )
                        )
                    }

                    awaitComposeTokenMovement(
                        playerId = currentPlayer.id,
                        pathTiles = listOf(myOldTile, leaderOldTile),
                        movementType = TokenMovementType.SHADOW_SWAP
                    )

                    _uiState.update { st ->
                        val swappedPlayers = st.players.map { p ->
                            when (p.id) {
                                currentPlayer.id -> p.copy(currentTile = leaderOldTile)
                                liveLeader.id -> p.copy(
                                    previousTurnTile = leaderOldTile,
                                    currentTile = myOldTile
                                )
                                else -> p
                            }
                        }
                        st.copy(
                            players = swappedPlayers,
                            isTokenAnimating = false,
                            highlightEffect = null,
                            eventBanner = GameEventBanner(
                                message = "جابه‌جایی انجام شد! شارژ بعدی در نوبت اختصاصی ${nextPersonalChargeTurn.toPersianDigits()} فعال می‌شود. حالا تاس بریزید 🎲",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                }

                // 6. KAI — Gravity Anchor Field (caps leader's next roll at 4 & pulls Kai +2 forward)
                CharacterAbilityType.HALF_REVERSE -> {
                    val targetLeader = _uiState.value.players
                        .filter { it.id != currentPlayer.id }
                        .maxByOrNull { it.currentTile }
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost(maxT - 1)

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1
                                )
                                targetLeader != null && p.id == targetLeader.id -> p.copy(
                                    gravityCapNextRoll = true
                                )
                                else -> p
                            }
                        }
                        st.copy(
                            players = updated,
                            isTokenAnimating = true,
                            eventBanner = GameEventBanner(
                                message = if (targetLeader != null) {
                                    "میدان لنگر جاذبه! ⚓ ${currentPlayer.name} ۲ خانه جلو کشیده شد و سقف تاس بعدی ${targetLeader.name} به ۴ محدود گردید!"
                                } else {
                                    "میدان لنگر جاذبه! ⚓ ${currentPlayer.name} ۲ خانه به جلو کشیده شد!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    if (newTile > currentPlayer.currentTile) {
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(currentPlayer.currentTile, newTile),
                            movementType = TokenMovementType.LUCK_BOOST
                        )
                        updatePlayerTileInState(currentPlayer.id, newTile)
                    }
                    _uiState.update { it.copy(isTokenAnimating = false) }
                }

                // 7. IVY — Bio-Venom Antidote & Spore Throw
                CharacterAbilityType.WORM_IMMUNE -> {
                    val targetLeader = _uiState.value.players
                        .filter { it.id != currentPlayer.id }
                        .maxByOrNull { it.currentTile }
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost(maxT - 1)

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    wormCurseTurnsRemaining = 0,
                                    swampSkipTurnsRemaining = 0,
                                    halveNextRoll = false,
                                    pinnedSlowSteps = 0
                                )
                                targetLeader != null &&
                                    p.id == targetLeader.id &&
                                    p.character.abilityType != CharacterAbilityType.WORM_IMMUNE -> p.copy(
                                    wormCurseTurnsRemaining = 2
                                )
                                else -> p
                            }
                        }
                        st.copy(
                            players = updated,
                            isTokenAnimating = true,
                            eventBanner = GameEventBanner(
                                message = if (targetLeader != null) {
                                    "اکسیر زیست‌تاب و هاگ سمی! 🧪 ${currentPlayer.name} پاک‌سازی شد (+۲ خانه) و ${targetLeader.name} را دچار طلسم کرم کرد!"
                                } else {
                                    "اکسیر زیست‌تاب! 🧪 ${currentPlayer.name} تمام طلسم‌ها را پاک کرد و ۲ خانه به جلو پرید!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    if (newTile > currentPlayer.currentTile) {
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(currentPlayer.currentTile, newTile),
                            movementType = TokenMovementType.LUCK_BOOST
                        )
                        updatePlayerTileInState(currentPlayer.id, newTile)
                    }
                    _uiState.update { it.copy(isTokenAnimating = false) }
                }

                // 8. PHOENIX — Dragon Soul Empowerment (+2 bonus steps for 2 turns & fiery leap)
                CharacterAbilityType.PHOENIX_WINGS -> {
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost(maxT - 1)
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    dragonSoulBoostTurns = 2,
                                    wormCurseTurnsRemaining = 0,
                                    swampSkipTurnsRemaining = 0,
                                    halveNextRoll = false
                                )
                            } else p
                        }
                        st.copy(
                            players = updated,
                            isTokenAnimating = true,
                            highlightEffect = BoardHighlightEffect(
                                tile = newTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.DRAGON_SOUL_AURA
                            ),
                            eventBanner = GameEventBanner(
                                message = "فوران روح اژدها (Dragon Soul)! 🔥🐉 ${currentPlayer.name} ۲ خانه پرواز کرد و برای ۲ نوبت قدرت حرکت +۲ خانه گرفت!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    if (newTile > currentPlayer.currentTile) {
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(currentPlayer.currentTile, newTile),
                            movementType = TokenMovementType.LUCK_BOOST
                        )
                        updatePlayerTileInState(currentPlayer.id, newTile)
                    }
                    _uiState.update { it.copy(isTokenAnimating = false, highlightEffect = null) }
                }

                // 9. STELLA — Guardian Angel Protection + Starlight Step
                CharacterAbilityType.FORTUNE_STAR -> {
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost(maxT - 1)
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    guardianAngelProtected = true,
                                    isActiveAbilityArmedForTurn = true
                                )
                            } else p
                        }
                        st.copy(
                            players = updated,
                            isTokenAnimating = true,
                            highlightEffect = BoardHighlightEffect(
                                tile = newTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.GUARDIAN_ANGEL_WINGS
                            ),
                            eventBanner = GameEventBanner(
                                message = "فرشتهٔ محافظ (Guardian Angel)! 🌟👼 سپر بال‌های فرشته دور ${currentPlayer.name} فعال شد (+۲ خانه جهش و ایمنی کامل در برابر خطر بعدی)!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    if (newTile > currentPlayer.currentTile) {
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(currentPlayer.currentTile, newTile),
                            movementType = TokenMovementType.LUCK_BOOST
                        )
                        updatePlayerTileInState(currentPlayer.id, newTile)
                    }
                    _uiState.update { it.copy(isTokenAnimating = false, highlightEffect = null) }
                }

                // 10. ZEPHYR — Phantom Phase (walk through traps/snakes) & Headwind Gust on Leader
                CharacterAbilityType.WIND_RIDER -> {
                    val targetLeader = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.currentTile > 2 &&
                                it.character.abilityType != CharacterAbilityType.EARTH_TITAN &&
                                it.character.abilityType != CharacterAbilityType.HALF_REVERSE
                        }
                        .maxByOrNull { it.currentTile }

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    phantomPhaseActive = true,
                                    isActiveAbilityArmedForTurn = true
                                )
                                targetLeader != null && p.id == targetLeader.id -> p.copy(
                                    previousTurnTile = p.currentTile,
                                    currentTile = (p.currentTile - 2).coerceAtLeast(2)
                                )
                                else -> p
                            }
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = currentPlayer.currentTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.PHANTOM_PHASE
                            ),
                            eventBanner = GameEventBanner(
                                message = if (targetLeader != null) {
                                    "شبح باد (Phantom Phase)! 👻🌪️ ${currentPlayer.name} در برابر تله‌ها و مارها شبح‌وار شد (+۲ خانه باد موافق) و ${targetLeader.name} را ۲ خانه عقب راند!"
                                } else {
                                    "شبح باد (Phantom Phase)! 👻🌪️ ${currentPlayer.name} آمادهٔ عبور شبح‌وار از تله‌ها و مارها شد (+۲ خانه باد موافق)!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 11. ORION — Trickster Debuff Transfer & Cosmic Transmutation
                CharacterAbilityType.ALCHEMIST -> {
                    val targetLeader = _uiState.value.players
                        .filter { it.id != currentPlayer.id }
                        .maxByOrNull { it.currentTile }
                    val hadDebuffToTransfer = currentPlayer.hasActiveDebuff

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(
                                    isActiveAbilityArmedForTurn = true,
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    wormCurseTurnsRemaining = 0,
                                    swampSkipTurnsRemaining = 0,
                                    halveNextRoll = false,
                                    gravityCapNextRoll = false,
                                    pinnedSlowSteps = 0
                                )
                                targetLeader != null && p.id == targetLeader.id -> p.copy(
                                    wormCurseTurnsRemaining = maxOf(p.wormCurseTurnsRemaining, currentPlayer.wormCurseTurnsRemaining),
                                    swampSkipTurnsRemaining = maxOf(p.swampSkipTurnsRemaining, currentPlayer.swampSkipTurnsRemaining),
                                    halveNextRoll = p.halveNextRoll || currentPlayer.halveNextRoll,
                                    gravityCapNextRoll = true,
                                    pinnedSlowSteps = maxOf(p.pinnedSlowSteps, currentPlayer.pinnedSlowSteps)
                                )
                                else -> p
                            }
                        }
                        val transferMsg = if (hadDebuffToTransfer && targetLeader != null) {
                            "فریبکار کیمیاگر (Trickster)! 🎭⚗️ ${currentPlayer.name} تمام اثرات منفی خود را به ${targetLeader.name} منتقل کرد و تاس بعدی خود را کیمیاگری می‌نماید!"
                        } else if (targetLeader != null) {
                            "کیمیاگری و معجون سرب! ⚗️ پرتاب بعدی ${currentPlayer.name} تقویت شده و سقف تاس ${targetLeader.name} به ۴ محدود گردید!"
                        } else {
                            "معجون «${ability.persianTitle}» آماده شد! ⚗️ پرتاب بعدی شما تقویت و کیمیاگری می‌شود!"
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = targetLeader?.currentTile ?: currentPlayer.currentTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.TRICKSTER_TRANSFER
                            ),
                            eventBanner = GameEventBanner(
                                message = transferMsg,
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 12. SHADOW — Time Keeper (Rewinds previous turn setback OR rewinds leader by 2 tiles + chrono boost)
                CharacterAbilityType.TIME_WEAVER -> {
                    val sufferedSetbackLastTurn = currentPlayer.previousTurnTile > currentPlayer.currentTile
                    if (sufferedSetbackLastTurn) {
                        val rewindDest = currentPlayer.previousTurnTile.coerceAtMost(maxT - 1)
                        val startPos = currentPlayer.currentTile
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    p.copy(
                                        abilityChargeProgress = 0,
                                        abilityUsesCount = p.abilityUsesCount + 1,
                                        wormCurseTurnsRemaining = 0,
                                        swampSkipTurnsRemaining = 0,
                                        halveNextRoll = false,
                                        gravityCapNextRoll = false,
                                        pinnedSlowSteps = 0
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                isTokenAnimating = true,
                                highlightEffect = BoardHighlightEffect(
                                    tile = rewindDest,
                                    isPositive = true,
                                    intensity = 1f,
                                    heroFxType = HeroBoardFxType.TIME_REWIND
                                ),
                                eventBanner = GameEventBanner(
                                    message = "نگهبان زمان (Time Keeper)! ⏳✨ ${currentPlayer.name} زمان را به عقب برگرداند و سقوط نوبت قبل را خنثی کرد (بازگشت به خانه ${rewindDest.toPersianDigits()})!",
                                    type = BannerType.CHARACTER_ABILITY
                                )
                            )
                        }
                        awaitComposeTokenMovement(
                            playerId = currentPlayer.id,
                            pathTiles = listOf(startPos, rewindDest),
                            movementType = TokenMovementType.TIME_REWIND
                        )
                        updatePlayerTileInState(currentPlayer.id, rewindDest)
                        _uiState.update { it.copy(isTokenAnimating = false, highlightEffect = null) }
                    } else {
                        val targetLeader = _uiState.value.players
                            .filter { it.id != currentPlayer.id && it.currentTile > 2 }
                            .maxByOrNull { it.currentTile }
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                when {
                                    p.id == currentPlayer.id -> p.copy(
                                        isActiveAbilityArmedForTurn = true,
                                        abilityChargeProgress = 0,
                                        abilityUsesCount = p.abilityUsesCount + 1,
                                        wormCurseTurnsRemaining = 0,
                                        swampSkipTurnsRemaining = 0,
                                        halveNextRoll = false,
                                        gravityCapNextRoll = false,
                                        pinnedSlowSteps = 0
                                    )
                                    targetLeader != null && p.id == targetLeader.id -> p.copy(
                                        previousTurnTile = p.currentTile,
                                        currentTile = (p.currentTile - 2).coerceAtLeast(2)
                                    )
                                    else -> p
                                }
                            }
                            st.copy(
                                players = updated,
                                highlightEffect = BoardHighlightEffect(
                                    tile = currentPlayer.currentTile,
                                    isPositive = true,
                                    intensity = 1f,
                                    heroFxType = HeroBoardFxType.TIME_REWIND
                                ),
                                eventBanner = GameEventBanner(
                                    message = if (targetLeader != null) {
                                        "شکاف زمان (Time Keeper)! ⏳ طلسم‌ها پاک شد (+۲ خانه شتاب زمانی در پرتاب بعد) و ${targetLeader.name} در زمان ۲ خانه به عقب برگشت!"
                                    } else {
                                        "ساعت شنی «${ability.persianTitle}» فعال شد! ⏳ تمام طلسم‌ها پاک شد و پرتاب بعدی +۲ خانه شتاب زمانی دارد!"
                                    },
                                    type = BannerType.CHARACTER_ABILITY
                                )
                            )
                        }
                        delay(750L)
                        _uiState.update { it.copy(highlightEffect = null) }
                    }
                }

                // 13. LUNA — Illusionist Holo-Mirage Decoy & Astral Stairway Climb
                CharacterAbilityType.LADDER_MAGE -> {
                    val ladders = _uiState.value.boardLinks.filter { it.type == LinkType.LADDER }
                    val nearestAhead = ladders
                        .filter { it.startTile >= currentPlayer.currentTile && it.endTile > currentPlayer.currentTile }
                        .minByOrNull { it.startTile - currentPlayer.currentTile }
                        ?: ladders.filter { it.endTile > currentPlayer.currentTile }
                            .maxByOrNull { it.endTile }

                    val decoyTile = currentPlayer.currentTile
                    val destTile = if (nearestAhead != null) {
                        val halfClimb = ((nearestAhead.endTile - currentPlayer.currentTile) / 2).coerceIn(3, 12)
                        (currentPlayer.currentTile + halfClimb).coerceAtMost(maxT - 1)
                    } else {
                        (currentPlayer.currentTile + 3).coerceAtMost(maxT - 1)
                    }

                    _uiState.update { st ->
                        val updatedPlayers = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    hasUsedOnceAbility = true,
                                    laddersClimbed = p.laddersClimbed + 1,
                                    illusionDecoyTile = decoyTile,
                                    illusionTurnsRemaining = 2
                                )
                            } else p
                        }
                        st.copy(
                            players = updatedPlayers,
                            isTokenAnimating = true,
                            eventBanner = GameEventBanner(
                                message = "سراب توهم‌زا (Illusionist) و پله اختری! 🪄✨ ${currentPlayer.name} یک بدل توهمی در خانه ${decoyTile.toPersianDigits()} گذاشت و تا خانه ${destTile.toPersianDigits()} اوج گرفت!",
                                type = BannerType.CHARACTER_ABILITY
                            ),
                            highlightEffect = BoardHighlightEffect(
                                tile = destTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.ILLUSION_MIRAGE
                            )
                        )
                    }

                    soundManager.playLadderClimb()
                    awaitComposeTokenMovement(
                        playerId = currentPlayer.id,
                        pathTiles = listOf(currentPlayer.currentTile, destTile),
                        movementType = TokenMovementType.LADDER_CLIMB
                    )
                    updatePlayerTileInState(currentPlayer.id, destTile)
                    _uiState.update {
                        it.copy(
                            isTokenAnimating = false,
                            highlightEffect = null,
                            eventBanner = GameEventBanner(
                                message = "${currentPlayer.name} به خانه ${destTile.toPersianDigits()} رسید و بدل توهمی‌اش برای ۲ نوبت فعال است! حالا تاس بریزید 🎲",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                }

                // 14. STORM — Chain Lightning Cataclysm
                CharacterAbilityType.STORM_LORD -> {
                    soundManager.playObstacleSound(ObstacleType.STORM)
                    val myOldTile = currentPlayer.currentTile
                    val myNewTile = (myOldTile + 2).coerceAtMost(maxT - 1)

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    hasUsedOnceAbility = true
                                )
                            } else p
                        }
                        st.copy(
                            players = updated,
                            isTokenAnimating = true,
                            stormFlashActive = true,
                            eventBanner = GameEventBanner(
                                message = "صاعقهٔ زنجیره‌ای فرمانروای طوفان! ⚡ ${currentPlayer.name} ۲ خانه به جلو جهش کرد و رقبای پیشتاز را به عقب راند!",
                                type = BannerType.STORM_CHAOS
                            )
                        )
                    }

                    awaitComposeTokenMovement(
                        playerId = currentPlayer.id,
                        pathTiles = listOf(myOldTile, myNewTile),
                        movementType = TokenMovementType.STORM_SHIFT
                    )

                    _uiState.update { st ->
                        val updatedPlayers = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(currentTile = myNewTile)
                                p.character.abilityType == CharacterAbilityType.EARTH_TITAN ||
                                    p.character.abilityType == CharacterAbilityType.HALF_REVERSE ||
                                    p.guardianAngelProtected ||
                                    (p.illusionDecoyTile != null && p.illusionTurnsRemaining > 0) -> p
                                else -> p.copy(
                                    previousTurnTile = p.currentTile,
                                    currentTile = (p.currentTile - 2).coerceAtLeast(2)
                                )
                            }
                        }
                        st.copy(
                            players = updatedPlayers,
                            isTokenAnimating = false,
                            stormFlashActive = false
                        )
                    }
                }

                // 15. AMBER — Glacial Prison
                CharacterAbilityType.FROST_KNIGHT -> {
                    val targetOpponent = _uiState.value.players
                        .filter { it.id != currentPlayer.id }
                        .maxByOrNull { it.currentTile }

                    val boostTile = (currentPlayer.currentTile + 1).coerceAtMost(maxT - 1)
                    updatePlayerTileInState(currentPlayer.id, boostTile)

                    if (targetOpponent == null) {
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    p.copy(
                                        abilityChargeProgress = 0,
                                        abilityUsesCount = p.abilityUsesCount + 1,
                                        hasUsedOnceAbility = true
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                eventBanner = GameEventBanner(
                                    message = "شمشیر یخی! ❄️ ${currentPlayer.name} روی یخ به جلو سر خورد!",
                                    type = BannerType.CHARACTER_ABILITY
                                )
                            )
                        }
                        return@launch
                    }

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    hasUsedOnceAbility = true
                                )
                            } else p
                        }
                        st.copy(players = updated)
                    }

                    val blocked = evaluateShieldOrProtectionDefense(
                        defenderId = targetOpponent.id,
                        threatCategory = ShieldThreatCategory.EXECUTIONER_OR_ABILITY,
                        threatTile = targetOpponent.currentTile,
                        threatNamePersian = "زندان یخی (${currentPlayer.name})"
                    )
                    if (blocked) {
                        _uiState.update { it.copy(highlightEffect = null) }
                        return@launch
                    }

                    val isHighThermalOrResist = targetOpponent.character.abilityType == CharacterAbilityType.PHOENIX_WINGS ||
                        targetOpponent.character.abilityType == CharacterAbilityType.EARTH_TITAN

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == targetOpponent.id) {
                                if (isHighThermalResist(p)) {
                                    p.copy(
                                        halveNextRoll = true,
                                        dragonSoulBoostTurns = if (p.character.abilityType == CharacterAbilityType.PHOENIX_WINGS) 2 else p.dragonSoulBoostTurns
                                    )
                                } else {
                                    p.copy(frozenSkipTurnsRemaining = 1)
                                }
                            } else p
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = targetOpponent.currentTile,
                                isPositive = false,
                                intensity = 1f
                            ),
                            eventBanner = GameEventBanner(
                                message = if (isHighThermalOrResist) {
                                    "مقاومت نسبی ${targetOpponent.name} در برابر یخ! ❄️🔥 به جای انجماد کامل، فقط تاس بعدی‌اش نصف شد!"
                                } else {
                                    "زندان یخی شوالیه! ❄️ ${targetOpponent.name} منجمد شد و نوبت بعدی خود را از دست می‌دهد!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(850L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 16. ATLAS — Berserker Fury & Seismic Tremor
                CharacterAbilityType.EARTH_TITAN -> {
                    val myRow = (currentPlayer.currentTile - 1) / 10
                    val affectedRivals = _uiState.value.players.filter {
                        it.id != currentPlayer.id &&
                            it.currentTile > 2 &&
                            (((it.currentTile - 1) / 10 == myRow) || abs(it.currentTile - currentPlayer.currentTile) <= 5)
                    }
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> p.copy(
                                    abilityChargeProgress = 0,
                                    abilityUsesCount = p.abilityUsesCount + 1,
                                    berserkerRageTurns = 2
                                )
                                affectedRivals.any { it.id == p.id } -> p.copy(
                                    previousTurnTile = p.currentTile,
                                    currentTile = (p.currentTile - 2).coerceAtLeast(2)
                                )
                                else -> p
                            }
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = currentPlayer.currentTile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.BERSERKER_RAGE
                            ),
                            eventBanner = GameEventBanner(
                                message = if (affectedRivals.isNotEmpty()) {
                                    "زلزلهٔ تایتان و خشم برسرکر (Berserker)! 🪨🔥 رقبای هم‌ردیف ۲ خانه به عقب لغزیدند و ${currentPlayer.name} برای ۲ نوبت +۲ خانه خشم حرکت گرفت!"
                                } else {
                                    "خشم برسرکر (Berserker)! 🪨🔥 ${currentPlayer.name} برای ۲ نوبت بعدی +۲ خانه قدرت حرکت اضافه گرفت!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                else -> {}
            }
        }
    }

    private fun isHighThermalResist(player: Player): Boolean {
        return player.character.abilityType == CharacterAbilityType.PHOENIX_WINGS ||
            player.character.abilityType == CharacterAbilityType.EARTH_TITAN
    }

    /**
     * Called when a player with HIGH_PRECISION selects Die 1, Die 2, or their Sum.
     */
    fun selectPrecisionRollOption(chosenSteps: Int) {
        val state = _uiState.value
        val choice = state.pendingPrecisionChoice ?: return
        val currentPlayer = state.currentPlayer ?: return

        soundManager.playAbilityActivateSound()
        // If doubles or sum >= 8, Nova's Pinning Arrow slows the leader by 2 steps on their next roll!
        val triggersPinningArrow = (choice.die1 == choice.die2 || choice.sum >= 8)
        val leaderTarget = state.players
            .filter { it.id != currentPlayer.id && it.currentTile > currentPlayer.currentTile }
            .maxByOrNull { it.currentTile }

        _uiState.update { st ->
            val updatedPlayers = st.players.map { p ->
                if (triggersPinningArrow && leaderTarget != null && p.id == leaderTarget.id) {
                    p.copy(pinnedSlowSteps = 2)
                } else p
            }
            val arrowNote = if (triggersPinningArrow && leaderTarget != null) {
                " + تیر نشانه‌گیر به ${leaderTarget.name} اصابت کرد (-۲ خانه از تاس بعدی)!"
            } else ""
            st.copy(
                players = updatedPlayers,
                pendingPrecisionChoice = null,
                diceValue = chosenSteps.coerceIn(1, 6),
                isTokenAnimating = true,
                eventBanner = GameEventBanner(
                    message = "انتخاب دقیق ${currentPlayer.name}! 🎯 حرکت به اندازهٔ ${chosenSteps.toPersianDigits()} خانه!$arrowNote",
                    type = BannerType.CHARACTER_ABILITY
                )
            )
        }

        viewModelScope.launch {
            val earnedSix = (choice.die1 == 6 || choice.die2 == 6)
            processPlayerMove(
                playerId = currentPlayer.id,
                rolledValue = if (earnedSix) 6 else chosenSteps.coerceIn(1, 6),
                effectiveSteps = chosenSteps,
                canEarnSixBonus = earnedSix
            )
        }
    }

    fun onTokenStepReached(playerId: Int, reachedTile: Int) {
        val currentMovementType = _uiState.value.activeMovementPath?.movementType
        val isBackward = currentMovementType == TokenMovementType.REVERSE_BACKWARD ||
            currentMovementType == TokenMovementType.EXECUTIONER_HUNT
        soundManager.playStepPop(reachedTile, isBackward = isBackward)
        updatePlayerTileInState(playerId, reachedTile)
    }

    fun onTokenMovementFinished(moveId: Long) {
        if (_uiState.value.activeMovementPath?.moveId == moveId) {
            pendingMovementCompletion?.complete(Unit)
        }
    }

    fun rollDice() {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING ||
            state.isDiceRolling ||
            state.isTokenAnimating ||
            state.pendingPrecisionChoice != null
        ) return
        val currentPlayer = state.currentPlayer ?: return

        viewModelScope.launch {
            if (currentPlayer.swampSkipTurnsRemaining > 0 || currentPlayer.frozenSkipTurnsRemaining > 0) {
                soundManager.playObstacleSound(ObstacleType.SWAMP)
                _uiState.update { st ->
                    val updatedPlayers = st.players.map { p ->
                        if (p.id == currentPlayer.id) {
                            p.copy(
                                swampSkipTurnsRemaining = (p.swampSkipTurnsRemaining - 1).coerceAtLeast(0),
                                frozenSkipTurnsRemaining = (p.frozenSkipTurnsRemaining - 1).coerceAtLeast(0)
                            )
                        } else p
                    }
                    st.copy(
                        players = updatedPlayers,
                        isTokenAnimating = true
                    )
                }
                delay(900L)
                advanceTurnOrGrantBonus(rolledValue = 1, forceExtraTurn = false)
                return@launch
            }

            val ability = currentPlayer.character.abilityType
            val customSpec = currentPlayer.character.customSpec
            val wasAbilityArmed = currentPlayer.isActiveAbilityArmedForTurn
            val isDualRoll = (ability == CharacterAbilityType.HIGH_PRECISION && wasAbilityArmed)

            // Pre-compute the exact random outcome before launching the 3D physics trajectory
            val hadWormCurse = currentPlayer.wormCurseTurnsRemaining > 0 &&
                ability != CharacterAbilityType.WORM_IMMUNE &&
                !(customSpec?.target == CustomAbilityTarget.SPECIAL_OBSTACLES &&
                    Random.nextInt(100) < customSpec.percentageOrValue)

            val rawComputedDie1 = if (isDualRoll) {
                Random.nextInt(1, 7)
            } else {
                when {
                    ability == CharacterAbilityType.SPEEDSTER && wasAbilityArmed -> {
                        Random.nextInt(4, 7) // Guaranteed 4..6 on Sonic Overdrive!
                    }
                    hadWormCurse -> {
                        if (Random.nextFloat() < 0.75f) Random.nextInt(1, 3) else Random.nextInt(1, 7)
                    }
                    ability == CharacterAbilityType.SPEEDSTER -> {
                        if (Random.nextFloat() < 0.42f) Random.nextInt(4, 7) else Random.nextInt(1, 7)
                    }
                    customSpec?.target == CustomAbilityTarget.DICE_AND_LUCK -> {
                        if (Random.nextInt(100) < customSpec.percentageOrValue) {
                            Random.nextInt(4, 7)
                        } else {
                            Random.nextInt(1, 7)
                        }
                    }
                    else -> Random.nextInt(1, 7)
                }
            }
            val preRolledDie1 = if (currentPlayer.gravityCapNextRoll) {
                rawComputedDie1.coerceAtMost(4)
            } else {
                rawComputedDie1
            }
            val preRolledDie2 = Random.nextInt(1, 7)
            val rollDurationMs = state.animationSpeed.diceRollDurationMs

            _uiState.update {
                it.copy(
                    isDiceRolling = true,
                    diceValue = preRolledDie1,
                    secondDiceValue = preRolledDie2,
                    diceRollTriggerId = it.diceRollTriggerId + 1L,
                    diceRotationDeg = it.diceRotationDeg + 1080f,
                    secondDiceRotationDeg = it.secondDiceRotationDeg + 1080f,
                    sixRollGlowActive = false,
                    eventBanner = GameEventBanner(
                        message = if (isDualRoll) {
                            "${currentPlayer.name} هر دو تاس را هم‌زمان پرتاب کرد! 🎲🎲"
                        } else {
                            "${currentPlayer.name} تاس را پرتاب کرد... چه عددی می‌آید؟"
                        },
                        type = if (isDualRoll) BannerType.CHARACTER_ABILITY else BannerType.NORMAL
                    )
                )
            }

            soundManager.playDiceRollCascade()

            val totalSoundTicks = 9
            var elapsedMs = 0L
            for (step in 0 until totalSoundTicks) {
                soundManager.playDiceTick(step)
                val progress = (step + 1).toFloat() / totalSoundTicks.toFloat()
                val targetElapsed = (rollDurationMs * (0.12f + 0.88f * progress * progress)).toLong()
                val waitMs = (targetElapsed - elapsedMs).coerceAtLeast(28L)
                delay(waitMs)
                elapsedMs += waitMs
            }
            val remainingRollWait = (rollDurationMs - elapsedMs).coerceAtLeast(0L)
            if (remainingRollWait > 0L) {
                delay(remainingRollWait)
            }

            // Advance this player's personal turn state (including Raven's 10-personal-turn cycle)
            val playerAfterPersonalTurn = advancePersonalTurnState(currentPlayer, wasAbilityArmed)

            if (isDualRoll) {
                val die1 = preRolledDie1
                val die2 = preRolledDie2
                soundManager.playAbilityActivateSound()
                val choiceObj = PrecisionRollChoice(die1 = die1, die2 = die2)
                _uiState.update { st ->
                    val updatedPlayers = st.players.map { p ->
                        if (p.id == currentPlayer.id) {
                            playerAfterPersonalTurn.copy(
                                previousTurnTile = p.currentTile,
                                isActiveAbilityArmedForTurn = false,
                                abilityChargeProgress = 0,
                                wormCurseTurnsRemaining = (p.wormCurseTurnsRemaining - 1).coerceAtLeast(0),
                                gravityCapNextRoll = false
                            )
                        } else p
                    }
                    st.copy(
                        players = updatedPlayers,
                        diceValue = die1,
                        secondDiceValue = die2,
                        isDiceRolling = false,
                        pendingPrecisionChoice = choiceObj,
                        eventBanner = GameEventBanner(
                            message = "پرتاب همزمان ۲ تاس! 🎯 تاس اول ${die1.toPersianDigits()}، تاس دوم ${die2.toPersianDigits()} (مجموع ${(die1 + die2).toPersianDigits()})",
                            type = BannerType.CHARACTER_ABILITY
                        )
                    )
                }
                if (currentPlayer.isAiBot) {
                    delay(750L)
                    val bestSteps = chooseBestPrecisionOptionForAi(currentPlayer, choiceObj)
                    selectPrecisionRollOption(bestSteps)
                }
                return@launch
            }

            val rawRoll = preRolledDie1

            val hadSwampHalve = currentPlayer.halveNextRoll
            var effectiveSteps = if (hadSwampHalve) {
                (rawRoll / 2).coerceAtLeast(1)
            } else {
                rawRoll
            }

            var abilityBannerNote: String? = null

            // Apply pinned slow steps (from Nova's Pinning Arrow)
            if (currentPlayer.pinnedSlowSteps > 0) {
                effectiveSteps = (effectiveSteps - currentPlayer.pinnedSlowSteps).coerceAtLeast(1)
                abilityBannerNote = "تیر نشانه‌گیر و لنگر! 🎯 از حرکت این نوبت ${currentPlayer.pinnedSlowSteps.toPersianDigits()} خانه کم شد (${effectiveSteps.toPersianDigits()} خانه)!"
            }

            // Apply Hero Active & Passive Roll Modifiers
            if (ability == CharacterAbilityType.ALCHEMIST && wasAbilityArmed) {
                val transmuted = if (rawRoll <= 2) (7 - rawRoll) else (rawRoll + 2)
                effectiveSteps = transmuted
                abilityBannerNote = "کیمیاگری اسرار! ⚗️ تاس ${rawRoll.toPersianDigits()} به ${transmuted.toPersianDigits()} خانه تبدیل شد!"
            } else if (ability == CharacterAbilityType.TIME_WEAVER && wasAbilityArmed) {
                effectiveSteps += 2
                abilityBannerNote = "شتاب نساج زمان! ⏳ +۲ خانه به پرتاب اضافه شد (${effectiveSteps.toPersianDigits()} خانه)!"
            } else if (ability == CharacterAbilityType.WIND_RIDER && wasAbilityArmed) {
                effectiveSteps += 2
                abilityBannerNote = "گردباد شبح باد! 🌪️ +۲ خانه باد موافق به پرتاب اضافه شد (${effectiveSteps.toPersianDigits()} خانه)!"
            } else if (ability == CharacterAbilityType.WIND_RIDER && rawRoll % 2 == 0 && rawRoll < 6 && !hadSwampHalve) {
                effectiveSteps += 1
                abilityBannerNote = "قابلیت بادسوار! 🍃 به خاطر تاس زوج (${rawRoll.toPersianDigits()})، ۱ خانه پاداش باد گرفتی!"
            } else if (ability == CharacterAbilityType.SHADOW_WALKER &&
                rawRoll % 2 == 1 &&
                state.leaderPlayer?.id != currentPlayer.id &&
                !hadSwampHalve
            ) {
                effectiveSteps += 1
                abilityBannerNote = "شنل تاریکی ریون! 🌑 به خاطر تاس فرد در تعقیب پیشتاز، +۱ خانه سرعت سایه گرفتی!"
            } else if (customSpec?.target == CustomAbilityTarget.OVERALL_MOVEMENT &&
                !hadSwampHalve &&
                Random.nextInt(100) < customSpec.percentageOrValue
            ) {
                val bonusSteps = (customSpec.percentageOrValue / 30).coerceIn(1, 3)
                effectiveSteps += bonusSteps
                abilityBannerNote = "قدرت سفارشی ${currentPlayer.character.englishName}! 🚀 +${bonusSteps.toPersianDigits()} خانه جهش اضافه گرفتی!"
            }

            // Apply Dragon Soul Empowerment (+2 steps after Phoenix resists a negative effect!)
            if (currentPlayer.dragonSoulBoostTurns > 0) {
                effectiveSteps += 2
                abilityBannerNote = "تقویت روح اژدها (Dragon Soul)! 🔥🐉 +۲ خانه قدرت حرکت اضافه از مقاومت قبلی دریافت شد (${effectiveSteps.toPersianDigits()} خانه)!"
            }

            // Apply Berserker Fury (+2 steps after Atlas suffers a snake bite or backward movement!)
            if (currentPlayer.berserkerRageTurns > 0) {
                effectiveSteps += 2
                abilityBannerNote = "خشم برسرکر (Berserker)! 🪨🔥 +۲ خانه قدرت حرکت اضافه پس از سقوط قبلی دریافت شد (${effectiveSteps.toPersianDigits()} خانه)!"
            }

            val fortuneFiveBonus = ability == CharacterAbilityType.FORTUNE_STAR &&
                rawRoll == 5 &&
                (currentPlayer.abilityChargeProgress >= 3 || wasAbilityArmed) &&
                !hadSwampHalve

            val earnsBonusTurnFromRoll = (rawRoll == 6 && !hadSwampHalve) || fortuneFiveBonus

            _uiState.update { st ->
                val updatedPlayers = st.players.map { p ->
                    if (p.id == currentPlayer.id) {
                        playerAfterPersonalTurn.copy(
                            previousTurnTile = p.currentTile,
                            wormCurseTurnsRemaining = (p.wormCurseTurnsRemaining - 1).coerceAtLeast(0),
                            halveNextRoll = false,
                            gravityCapNextRoll = false,
                            pinnedSlowSteps = 0,
                            dragonSoulBoostTurns = (p.dragonSoulBoostTurns - 1).coerceAtLeast(0),
                            berserkerRageTurns = (p.berserkerRageTurns - 1).coerceAtLeast(0),
                            isActiveAbilityArmedForTurn = false,
                            abilityChargeProgress = if (fortuneFiveBonus) 0 else playerAfterPersonalTurn.abilityChargeProgress,
                            totalSixes = if (earnsBonusTurnFromRoll) p.totalSixes + 1 else p.totalSixes
                        )
                    } else p
                }
                st.copy(
                    players = updatedPlayers,
                    diceValue = rawRoll,
                    isDiceRolling = false,
                    isTokenAnimating = true,
                    sixRollGlowActive = earnsBonusTurnFromRoll
                )
            }

            if (abilityBannerNote != null) {
                soundManager.playAbilityActivateSound()
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = abilityBannerNote,
                            type = BannerType.CHARACTER_ABILITY
                        )
                    )
                }
                delay(520L)
            } else if (hadSwampHalve) {
                soundManager.playObstacleSound(ObstacleType.SWAMP)
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = "به خاطر سنگینی باتلاق، تاس ${rawRoll.toPersianDigits()} نصف شد و ${effectiveSteps.toPersianDigits()} خانه حرکت می‌کنی! 🥾",
                            type = BannerType.OBSTACLE_DANGER
                        )
                    )
                }
                delay(680L)
            } else if (hadWormCurse && rawRoll <= 2) {
                soundManager.playObstacleSound(ObstacleType.WORM)
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = "طلسم کرم لزج اثر کرد! 🪱 تاس فقط ${rawRoll.toPersianDigits()} آمد!",
                            type = BannerType.OBSTACLE_DANGER
                        )
                    )
                }
                delay(520L)
            } else if (fortuneFiveBonus) {
                soundManager.playSixBonusChime()
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = "ستاره خوش‌اقبال! ⭐ تاس ۵ برای ${currentPlayer.name} مثل ۶ عمل کرد و یک پرتاب جایزه گرفت!",
                            type = BannerType.CHARACTER_ABILITY
                        )
                    )
                }
                delay(450L)
            } else if (rawRoll == 6) {
                soundManager.playSixBonusChime()
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = "شیش طلایی برای ${currentPlayer.name}! ✨ یک نوبت جایزه هم داری!",
                            type = BannerType.SIX_BONUS
                        )
                    )
                }
                delay(380L)
            }

            processPlayerMove(
                playerId = currentPlayer.id,
                rolledValue = if (earnsBonusTurnFromRoll) 6 else rawRoll,
                effectiveSteps = effectiveSteps,
                canEarnSixBonus = earnsBonusTurnFromRoll,
                wasSonicOverdriveArmed = (ability == CharacterAbilityType.SPEEDSTER && wasAbilityArmed)
            )
        }
    }

    private suspend fun processPlayerMove(
        playerId: Int,
        rolledValue: Int,
        effectiveSteps: Int,
        canEarnSixBonus: Boolean,
        wasSonicOverdriveArmed: Boolean = false
    ) {
        val stateBeforeMove = _uiState.value
        val maxTiles = stateBeforeMove.maxTiles
        val player = stateBeforeMove.players.find { it.id == playerId } ?: return
        val startTile = player.currentTile
        val targetTile = startTile + effectiveSteps

        if (targetTile > maxTiles) {
            val needed = maxTiles - startTile
            soundManager.playDiceTick(0)
            _uiState.update {
                it.copy(
                    eventBanner = GameEventBanner(
                        message = "${player.name} عدد ${effectiveSteps.toPersianDigits()} آورد، اما برای رسیدن به خانه ${maxTiles.toPersianDigits()} دقیقاً ${needed.toPersianDigits()} لازم است!",
                        type = BannerType.EXACT_NEEDED
                    )
                )
            }
            delay(950L)
            advanceTurnOrGrantBonus(
                rolledValue = if (canEarnSixBonus) 6 else 1,
                forceExtraTurn = false
            )
            return
        }

        val stepPathTiles = (startTile..targetTile).toList()
        awaitComposeTokenMovement(
            playerId = playerId,
            pathTiles = stepPathTiles,
            movementType = TokenMovementType.STEP_BY_STEP
        )
        updatePlayerTileInState(playerId, targetTile)

        // If Axel's Sonic Overdrive was armed, knock back any rival passed along the path by 2 tiles!
        if (wasSonicOverdriveArmed) {
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id != playerId &&
                        p.currentTile in (startTile + 1)..targetTile &&
                        p.character.abilityType != CharacterAbilityType.EARTH_TITAN &&
                        p.character.abilityType != CharacterAbilityType.HALF_REVERSE
                    ) {
                        p.copy(
                            previousTurnTile = p.currentTile,
                            currentTile = (p.currentTile - 2).coerceAtLeast(2)
                        )
                    } else p
                }
                st.copy(players = updated)
            }
        }

        // Check Executioner Co-Tile Hunt on initial landing tile!
        checkAndExecuteCoTileHunt(hunterId = playerId, landedTile = targetTile)

        var grantLuckExtraTurn = false

        // 1. Check Special Obstacles
        val matchedObstacle = _uiState.value.boardObstacles.find { it.tile == targetTile }
        if (matchedObstacle != null) {
            delay(220L)
            grantLuckExtraTurn = handleObstacleLanding(
                playerId = playerId,
                obstacle = matchedObstacle,
                rolledSteps = effectiveSteps
            )
        }

        // 2. Check Snakes & Ladders
        val tileAfterObstacle = _uiState.value.players.find { it.id == playerId }?.currentTile ?: targetTile
        val matchedLink = _uiState.value.boardLinks.find { it.startTile == tileAfterObstacle }
        if (matchedLink != null) {
            delay(220L)
            val currentPlayerObj = _uiState.value.players.find { it.id == playerId } ?: player
            when (matchedLink.type) {
                LinkType.LADDER -> executeLadderClimb(currentPlayerObj, matchedLink)
                LinkType.SNAKE -> executeSnakeSlide(currentPlayerObj, matchedLink)
            }
        }

        val finalTile = _uiState.value.players.find { it.id == playerId }?.currentTile ?: tileAfterObstacle

        // If the player moved to a new tile after obstacle/ladder/snake, check Executioner Co-Tile Hunt there too!
        if (finalTile != targetTile) {
            checkAndExecuteCoTileHunt(hunterId = playerId, landedTile = finalTile)
        }

        val winningPlayer = _uiState.value.players.find { it.currentTile >= maxTiles }
        if (winningPlayer != null || finalTile >= maxTiles) {
            triggerWinnerCelebration(winningPlayer?.id ?: playerId)
            return
        }

        delay(300L)
        advanceTurnOrGrantBonus(
            rolledValue = if (canEarnSixBonus) 6 else 1,
            forceExtraTurn = grantLuckExtraTurn
        )
    }

    private suspend fun handleObstacleLanding(
        playerId: Int,
        obstacle: BoardObstacle,
        rolledSteps: Int
    ): Boolean {
        val state = _uiState.value
        val maxTiles = state.maxTiles
        val player = state.players.find { it.id == playerId } ?: return false
        val ability = player.character.abilityType
        val customSpec = player.character.customSpec

        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) p.copy(obstaclesTriggered = p.obstaclesTriggered + 1) else p
            }
            st.copy(
                players = updated,
                highlightEffect = BoardHighlightEffect(
                    tile = obstacle.tile,
                    isPositive = !obstacle.type.isDangerous,
                    intensity = 1f,
                    obstacleType = obstacle.type
                )
            )
        }

        // Phase 5 Custom Character: Check SPECIAL_OBSTACLES immunity/bonus!
        if (customSpec?.target == CustomAbilityTarget.SPECIAL_OBSTACLES &&
            obstacle.type.isDangerous &&
            Random.nextInt(100) < customSpec.percentageOrValue
        ) {
            soundManager.playAbilityActivateSound()
            val shouldBoost = customSpec.effectType == CustomAbilityEffectType.BONUS_STEPS_BOOST ||
                customSpec.effectType == CustomAbilityEffectType.INCREASE_CHANCE
            val destTile = if (shouldBoost) (obstacle.tile + 2).coerceAtMost(maxTiles) else obstacle.tile
            _uiState.update {
                it.copy(
                    eventBanner = GameEventBanner(
                        message = if (shouldBoost) {
                            "قدرت سفارشی ${player.character.englishName}! ⚡ مانع «${obstacle.type.persianName}» خنثی شد و +۲ خانه به جلو پریدی!"
                        } else {
                            "ایمنی سفارشی ${player.character.englishName}! 🛡️ مانع «${obstacle.type.persianName}» کامل خنثی شد!"
                        },
                        type = BannerType.CHARACTER_ABILITY
                    )
                )
            }
            if (destTile > obstacle.tile) {
                awaitComposeTokenMovement(
                    playerId = playerId,
                    pathTiles = listOf(obstacle.tile, destTile),
                    movementType = TokenMovementType.LUCK_BOOST
                )
                updatePlayerTileInState(playerId, destTile)
            } else {
                delay(800L)
            }
            _uiState.update { it.copy(highlightEffect = null) }
            return false
        }

        // Check Guardian Shield, Guardian Angel, or Phantom Phase on dangerous obstacles!
        if (obstacle.type.isDangerous) {
            val passivePhantomProc = ability == CharacterAbilityType.WIND_RIDER && Random.nextInt(100) < 35
            if (passivePhantomProc) {
                _uiState.update { st ->
                    val updated = st.players.map { p ->
                        if (p.id == playerId) p.copy(phantomPhaseActive = true) else p
                    }
                    st.copy(players = updated)
                }
            }
            val blockedByProtection = evaluateShieldOrProtectionDefense(
                defenderId = playerId,
                threatCategory = ShieldThreatCategory.NEGATIVE_OBSTACLE,
                threatTile = obstacle.tile,
                threatNamePersian = obstacle.type.persianName
            )
            if (blockedByProtection) {
                _uiState.update { it.copy(highlightEffect = null) }
                return false
            }
        }

        var extraTurnGranted = false

        when (obstacle.type) {
            ObstacleType.WORM -> {
                if (ability == CharacterAbilityType.WORM_IMMUNE) {
                    soundManager.playAbilityActivateSound()
                    val destTile = (obstacle.tile + 2).coerceAtMost(maxTiles)
                    _uiState.update {
                        it.copy(
                            highlightEffect = BoardHighlightEffect(
                                tile = obstacle.tile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                            ),
                            eventBanner = GameEventBanner(
                                message = "مقاومت کامل ضدزهر ${player.name}! 🧪🛡️ کرم لزج خنثی شد و ۲ خانه به جلو پاداش گرفتی!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    if (destTile > obstacle.tile) {
                        awaitComposeTokenMovement(
                            playerId = playerId,
                            pathTiles = listOf(obstacle.tile, destTile),
                            movementType = TokenMovementType.LUCK_BOOST
                        )
                        updatePlayerTileInState(playerId, destTile)
                    }
                } else if (ability == CharacterAbilityType.PHOENIX_WINGS) {
                    // Dragon Soul: Resists Worm to 1 turn and empowers Phoenix with +2 steps!
                    soundManager.playAbilityActivateSound()
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == playerId) p.copy(dragonSoulBoostTurns = 2, wormCurseTurnsRemaining = 1) else p
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = obstacle.tile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.DRAGON_SOUL_AURA
                            ),
                            eventBanner = GameEventBanner(
                                message = "مقاومت روح اژدها (Dragon Soul)! 🔥🐉 ${player.name} اثر کرم را سوزاند و برای پرتاب بعدی +۲ خانه قدرت گرفت!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(850L)
                } else {
                    soundManager.playObstacleSound(ObstacleType.WORM)
                    val reducedDuration = if (player.character.heroBlueprint.resistanceProfile.statusDebuffResistancePercent >= 40) 2 else 3
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == playerId) p.copy(wormCurseTurnsRemaining = reducedDuration) else p
                        }
                        st.copy(
                            players = updated,
                            eventBanner = GameEventBanner(
                                message = "اه! ${player.name} روی خانهٔ کرم لزج افتاد! 🪱 تا ${reducedDuration.toPersianDigits()} نوبت احتمال آمدن تاس ۱ و ۲ زیاد است!",
                                type = BannerType.OBSTACLE_DANGER
                            )
                        )
                    }
                    delay(850L)
                }
            }

            ObstacleType.REVERSE -> {
                when {
                    ability == CharacterAbilityType.TIME_WEAVER -> {
                        soundManager.playAbilityActivateSound()
                        val forwardSteps = (rolledSteps / 2).coerceAtLeast(2)
                        val destTile = (obstacle.tile + forwardSteps).coerceAtMost(maxTiles)
                        _uiState.update {
                            it.copy(
                                highlightEffect = BoardHighlightEffect(
                                    tile = obstacle.tile,
                                    isPositive = true,
                                    intensity = 1f,
                                    heroFxType = HeroBoardFxType.TIME_REWIND
                                ),
                                eventBanner = GameEventBanner(
                                    message = "قدرت نگهبان زمان! ⏳ ${player.name} خانهٔ معکوس را وارونه کرد و ${forwardSteps.toPersianDigits()} خانه به جلو پرید!",
                                    type = BannerType.CHARACTER_ABILITY
                                )
                            )
                        }
                        if (destTile > obstacle.tile) {
                            awaitComposeTokenMovement(
                                playerId = playerId,
                                pathTiles = listOf(obstacle.tile, destTile),
                                movementType = TokenMovementType.LUCK_BOOST
                            )
                            updatePlayerTileInState(playerId, destTile)
                        }
                    }

                    else -> {
                        soundManager.playObstacleSound(ObstacleType.REVERSE)
                        val backSteps = when (ability) {
                            CharacterAbilityType.HALF_REVERSE,
                            CharacterAbilityType.EARTH_TITAN -> (rolledSteps / 2).coerceAtLeast(1)
                            CharacterAbilityType.SPEEDSTER -> rolledSteps + 1 // Speedster brake weakness
                            else -> rolledSteps
                        }
                        val destTile = (obstacle.tile - backSteps).coerceAtLeast(1)
                        val isAtlas = ability == CharacterAbilityType.EARTH_TITAN
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == playerId) {
                                    p.copy(
                                        previousTurnTile = obstacle.tile,
                                        berserkerRageTurns = if (isAtlas) 2 else p.berserkerRageTurns
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                highlightEffect = BoardHighlightEffect(
                                    tile = obstacle.tile,
                                    isPositive = isAtlas,
                                    intensity = 1f,
                                    heroFxType = if (isAtlas) HeroBoardFxType.BERSERKER_RAGE else HeroBoardFxType.NONE
                                ),
                                eventBanner = GameEventBanner(
                                    message = when {
                                        ability == CharacterAbilityType.HALF_REVERSE ->
                                            "لنگر تعادل ${player.name}! ⚓ در خانهٔ معکوس فقط نصف تاس (${backSteps.toPersianDigits()} خانه) به عقب برگشتی!"
                                        isAtlas ->
                                            "مقاومت صخره‌ای و خشم برسرکر! 🪨🔥 ${player.name} نصف مسیر را برگشت و برای ۲ نوبت +۲ خانه خشم حرکت گرفت!"
                                        else ->
                                            "آخ! خانهٔ معکوس! 🔄 ${player.name} به اندازهٔ ${backSteps.toPersianDigits()} خانه به عقب (خانه ${destTile.toPersianDigits()}) برمی‌گردد!"
                                    },
                                    type = if (ability == CharacterAbilityType.HALF_REVERSE || isAtlas) {
                                        BannerType.CHARACTER_ABILITY
                                    } else {
                                        BannerType.OBSTACLE_DANGER
                                    }
                                )
                            )
                        }
                        delay(320L)
                        if (destTile < obstacle.tile) {
                            val backwardPath = (obstacle.tile downTo destTile).toList()
                            awaitComposeTokenMovement(
                                playerId = playerId,
                                pathTiles = backwardPath,
                                movementType = TokenMovementType.REVERSE_BACKWARD
                            )
                            updatePlayerTileInState(playerId, destTile)
                        }
                    }
                }
            }

            ObstacleType.SWAMP -> {
                if (ability == CharacterAbilityType.PHOENIX_WINGS) {
                    // Dragon Soul: Resists Swamp and empowers Phoenix with +2 bonus steps!
                    soundManager.playAbilityActivateSound()
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == playerId) p.copy(dragonSoulBoostTurns = 2) else p
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = obstacle.tile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.DRAGON_SOUL_AURA
                            ),
                            eventBanner = GameEventBanner(
                                message = "بال‌های ققنوس و روح اژدها (Dragon Soul)! 🔥🐉 ${player.name} از فراز باتلاق پرواز کرد و +۲ خانه قدرت حرکت برای پرتاب بعد گرفت!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(850L)
                } else if (ability == CharacterAbilityType.WORM_IMMUNE) {
                    // Ivy's Nature Symbiosis: Immune to Swamp and gains +2 steps!
                    soundManager.playAbilityActivateSound()
                    val destTile = (obstacle.tile + 2).coerceAtMost(maxTiles)
                    _uiState.update {
                        it.copy(
                            highlightEffect = BoardHighlightEffect(
                                tile = obstacle.tile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                            ),
                            eventBanner = GameEventBanner(
                                message = "هم‌زیستی با طبیعت ${player.name}! 🌿 باتلاق به سکوی پرتاب تبدیل شد و ۲ خانه جلو پریدی!",
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    if (destTile > obstacle.tile) {
                        awaitComposeTokenMovement(
                            playerId = playerId,
                            pathTiles = listOf(obstacle.tile, destTile),
                            movementType = TokenMovementType.LUCK_BOOST
                        )
                        updatePlayerTileInState(playerId, destTile)
                    }
                } else {
                    soundManager.playObstacleSound(ObstacleType.SWAMP)
                    val partialResist = player.character.heroBlueprint.resistanceProfile.statusDebuffResistancePercent >= 40
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == playerId) {
                                if (partialResist) {
                                    p.copy(swampSkipTurnsRemaining = 0, halveNextRoll = true)
                                } else {
                                    p.copy(swampSkipTurnsRemaining = 1, halveNextRoll = true)
                                }
                            } else p
                        }
                        st.copy(
                            players = updated,
                            eventBanner = GameEventBanner(
                                message = if (partialResist) {
                                    "مقاومت نسبی ${player.name} در باتلاق! 🛡️🌿 نوبتت از دست نرفت و فقط تاس بعدی نصف می‌شود!"
                                } else {
                                    "باتلاق سنگین! 🌿 ${player.name} در گل‌ولای فرو رفت؛ یک دور حرکت نمی‌کند و تاس بعدی‌اش نصف می‌شود!"
                                },
                                type = BannerType.OBSTACLE_DANGER
                            )
                        )
                    }
                    delay(900L)
                }
            }

            ObstacleType.WELL -> {
                if (ability == CharacterAbilityType.PHOENIX_WINGS ||
                    ability == CharacterAbilityType.EARTH_TITAN ||
                    ability == CharacterAbilityType.FROST_KNIGHT
                ) {
                    val isPhoenix = ability == CharacterAbilityType.PHOENIX_WINGS
                    soundManager.playAbilityActivateSound()
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == playerId && isPhoenix) {
                                p.copy(dragonSoulBoostTurns = 2)
                            } else p
                        }
                        st.copy(
                            players = updated,
                            highlightEffect = BoardHighlightEffect(
                                tile = obstacle.tile,
                                isPositive = true,
                                intensity = 1f,
                                heroFxType = if (isPhoenix) HeroBoardFxType.DRAGON_SOUL_AURA else HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                            ),
                            eventBanner = GameEventBanner(
                                message = if (isPhoenix) {
                                    "پرواز ققنوس و روح اژدها (Dragon Soul)! 🔥🐉 از چاه تاریک عبور کردی و +۲ خانه قدرت حرکت برای پرتاب بعد گرفتی!"
                                } else {
                                    "مقاومت ویژهٔ ${player.name}! 🛡️ از سقوط به درون چاه تاریک کاملاً در امان ماندی!"
                                },
                                type = BannerType.CHARACTER_ABILITY
                            )
                        )
                    }
                    delay(850L)
                } else {
                    soundManager.playObstacleSound(ObstacleType.WELL)
                    val maxDrop = when (_uiState.value.difficulty) {
                        DifficultyLevel.EASY -> 12
                        DifficultyLevel.MEDIUM -> 22
                        DifficultyLevel.HARD -> 34
                    }
                    val minDest = (obstacle.tile - maxDrop).coerceAtLeast(2)
                    val maxDest = (obstacle.tile - 5).coerceAtLeast(2)
                    val destTile = if (maxDest > minDest) {
                        Random.nextInt(minDest, maxDest + 1)
                    } else {
                        2
                    }

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == playerId) p.copy(previousTurnTile = obstacle.tile) else p
                        }
                        st.copy(
                            players = updated,
                            eventBanner = GameEventBanner(
                                message = "سقوط در چاه تاریک! 🕳️ ${player.name} به اعماق چاه افتاد و از خانه ${destTile.toPersianDigits()} سر درآورد!",
                                type = BannerType.OBSTACLE_DANGER
                            )
                        )
                    }
                    awaitComposeTokenMovement(
                        playerId = playerId,
                        pathTiles = listOf(obstacle.tile, destTile),
                        movementType = TokenMovementType.WELL_PLUNGE
                    )
                    updatePlayerTileInState(playerId, destTile)
                }
            }

            ObstacleType.WIND -> {
                soundManager.playObstacleSound(ObstacleType.WIND)
                val isTailwind = (ability == CharacterAbilityType.WIND_RIDER) || Random.nextBoolean()
                val destTile = if (isTailwind) {
                    val windBoost = if (ability == CharacterAbilityType.GUARDIAN_SHIELD) {
                        (rolledSteps - 2).coerceAtLeast(1) // Heavy armor trade-off
                    } else {
                        rolledSteps
                    }
                    (obstacle.tile + windBoost).coerceAtMost(maxTiles)
                } else {
                    val pushBack = if (ability == CharacterAbilityType.HALF_REVERSE || ability == CharacterAbilityType.EARTH_TITAN) {
                        (rolledSteps / 4).coerceAtLeast(1)
                    } else {
                        (rolledSteps / 2).coerceAtLeast(1)
                    }
                    (obstacle.tile - pushBack).coerceAtLeast(1)
                }

                val isAtlas = ability == CharacterAbilityType.EARTH_TITAN && !isTailwind
                _uiState.update { st ->
                    val updated = st.players.map { p ->
                        if (p.id == playerId && !isTailwind) {
                            p.copy(
                                previousTurnTile = obstacle.tile,
                                berserkerRageTurns = if (isAtlas) 2 else p.berserkerRageTurns
                            )
                        } else p
                    }
                    st.copy(
                        players = updated,
                        eventBanner = GameEventBanner(
                            message = if (isTailwind) {
                                "وزش باد موافق! 🌪️ باد ${player.name} را با سرعت تا خانه ${destTile.toPersianDigits()} به جلو پرتاب کرد!"
                            } else {
                                "وزش باد مخالف! 💨 تندباد ${player.name} را تا خانه ${destTile.toPersianDigits()} به عقب راند!"
                            },
                            type = BannerType.OBSTACLE_SURPRISE
                        )
                    )
                }

                if (destTile != obstacle.tile) {
                    awaitComposeTokenMovement(
                        playerId = playerId,
                        pathTiles = listOf(obstacle.tile, destTile),
                        movementType = TokenMovementType.WIND_GUST
                    )
                    updatePlayerTileInState(playerId, destTile)
                }
            }

            ObstacleType.LUCK -> {
                soundManager.playObstacleSound(ObstacleType.LUCK)
                val isLuckyHero = ability == CharacterAbilityType.FORTUNE_STAR ||
                    customSpec?.target == CustomAbilityTarget.DICE_AND_LUCK
                val luckOutcome = if (isLuckyHero) {
                    Random.nextInt(3)
                } else {
                    Random.nextInt(4)
                }
                when (luckOutcome) {
                    0 -> {
                        val boost = if (isLuckyHero) 4 else 3
                        val destTile = (obstacle.tile + boost).coerceAtMost(maxTiles)
                        _uiState.update {
                            it.copy(
                                eventBanner = GameEventBanner(
                                    message = "شانس طلایی! 🍀 صندوقچهٔ شانس ${player.name} را ${boost.toPersianDigits()} خانه به جلو (خانه ${destTile.toPersianDigits()}) پرتاب کرد!",
                                    type = BannerType.OBSTACLE_SURPRISE
                                )
                            )
                        }
                        if (destTile != obstacle.tile) {
                            awaitComposeTokenMovement(
                                playerId = playerId,
                                pathTiles = listOf(obstacle.tile, destTile),
                                movementType = TokenMovementType.LUCK_BOOST
                            )
                            updatePlayerTileInState(playerId, destTile)
                        }
                    }
                    1 -> {
                        extraTurnGranted = true
                        soundManager.playSixBonusChime()
                        _uiState.update {
                            it.copy(
                                sixRollGlowActive = true,
                                eventBanner = GameEventBanner(
                                    message = "خوش‌شانسی بزرگ! 🍀 ${player.name} یک پرتاب تاس جایزه برنده شد!",
                                    type = BannerType.SIX_BONUS
                                )
                            )
                        }
                        delay(750L)
                    }
                    2 -> {
                        val destTile = (obstacle.tile + 2).coerceAtMost(maxTiles)
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == playerId) {
                                    p.copy(
                                        wormCurseTurnsRemaining = 0,
                                        swampSkipTurnsRemaining = 0,
                                        halveNextRoll = false,
                                        gravityCapNextRoll = false,
                                        pinnedSlowSteps = 0
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                eventBanner = GameEventBanner(
                                    message = "معجون شانس! ✨ تمام طلسم‌های ${player.name} پاک شد و ۲ خانه جلو رفت!",
                                    type = BannerType.OBSTACLE_SURPRISE
                                )
                            )
                        }
                        if (destTile != obstacle.tile) {
                            awaitComposeTokenMovement(
                                playerId = playerId,
                                pathTiles = listOf(obstacle.tile, destTile),
                                movementType = TokenMovementType.LUCK_BOOST
                            )
                            updatePlayerTileInState(playerId, destTile)
                        }
                    }
                    else -> {
                        val destTile = (obstacle.tile - 2).coerceAtLeast(1)
                        val isAtlas = ability == CharacterAbilityType.EARTH_TITAN
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == playerId) {
                                    p.copy(
                                        previousTurnTile = obstacle.tile,
                                        berserkerRageTurns = if (isAtlas) 2 else p.berserkerRageTurns
                                    )
                                } else p
                            }
                            st.copy(
                                players = updated,
                                eventBanner = GameEventBanner(
                                    message = "بدشانسی شیرین! 🎲 صندوقچهٔ شانس شوخی کرد و ${player.name} دو خانه به عقب برگشت!",
                                    type = BannerType.OBSTACLE_DANGER
                                )
                            )
                        }
                        if (destTile != obstacle.tile) {
                            awaitComposeTokenMovement(
                                playerId = playerId,
                                pathTiles = (obstacle.tile downTo destTile).toList(),
                                movementType = TokenMovementType.REVERSE_BACKWARD
                            )
                            updatePlayerTileInState(playerId, destTile)
                        }
                    }
                }
            }

            ObstacleType.STORM -> {
                soundManager.playObstacleSound(ObstacleType.STORM)
                _uiState.update {
                    it.copy(
                        stormFlashActive = true,
                        eventBanner = GameEventBanner(
                            message = "طوفان سهمگین! ⚡ گردباد کل صفحه را لرزاند و همهٔ بازیکنان جابجا شدند!",
                            type = BannerType.STORM_CHAOS
                        )
                    )
                }

                val currentPlayers = _uiState.value.players
                val stormShifts = currentPlayers.associate { p ->
                    val delta = when (p.character.abilityType) {
                        CharacterAbilityType.STORM_LORD -> 2
                        CharacterAbilityType.EARTH_TITAN,
                        CharacterAbilityType.HALF_REVERSE -> 0
                        else -> if (Random.nextBoolean()) 1 else -1
                    }
                    val newTile = (p.currentTile + delta).coerceIn(1, maxTiles - 1)
                    p.id to newTile
                }

                val activeStart = player.currentTile
                val activeEnd = stormShifts[playerId] ?: activeStart
                awaitComposeTokenMovement(
                    playerId = playerId,
                    pathTiles = listOf(activeStart, activeEnd),
                    movementType = TokenMovementType.STORM_SHIFT
                )

                _uiState.update { st ->
                    val shiftedPlayers = st.players.map { p ->
                        val dest = stormShifts[p.id] ?: p.currentTile
                        p.copy(currentTile = dest)
                    }
                    st.copy(
                        players = shiftedPlayers,
                        stormFlashActive = false
                    )
                }
            }
        }

        _uiState.update { it.copy(highlightEffect = null) }
        return extraTurnGranted
    }

    private suspend fun executeLadderClimb(player: Player, ladder: BoardLink) {
        val maxTiles = _uiState.value.maxTiles
        soundManager.playLadderClimb()
        val ability = player.character.abilityType
        val hasLadderBonus = ability == CharacterAbilityType.PHOENIX_WINGS || ability == CharacterAbilityType.LADDER_MAGE
        val customSpec = player.character.customSpec
        val customLadderSteps = if (customSpec?.target == CustomAbilityTarget.LADDERS &&
            Random.nextInt(100) < customSpec.percentageOrValue
        ) {
            (customSpec.percentageOrValue / 25).coerceIn(1, 4)
        } else 0

        val bonusClimb = when {
            customLadderSteps > 0 -> customLadderSteps
            hasLadderBonus -> 1
            ability == CharacterAbilityType.HALF_REVERSE && (ladder.endTile - ladder.startTile) > 12 -> -1
            else -> 0
        }
        val finalDest = (ladder.endTile + bonusClimb).coerceIn(ladder.startTile + 1, maxTiles)

        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == player.id) p.copy(laddersClimbed = p.laddersClimbed + 1) else p
            }
            st.copy(
                players = updated,
                eventBanner = GameEventBanner(
                    message = when {
                        customLadderSteps > 0 ->
                            "قدرت پلهٔ سفارشی ${player.character.englishName}! 🪜 تا خانه ${finalDest.toPersianDigits()} (+${customLadderSteps.toPersianDigits()} خانه بالاتر از پله) اوج گرفت!"
                        hasLadderBonus ->
                            "هم‌نوایی جادویی روی پله! 🔥 ${player.name} تا خانه ${finalDest.toPersianDigits()} (+۱ خانه بالاتر از پله) اوج گرفت!"
                        else ->
                            "فوق‌العاده! ${player.name} از پلهٔ خانه ${ladder.startTile.toPersianDigits()} تا ${finalDest.toPersianDigits()} اوج گرفت! 🚀"
                    },
                    type = BannerType.LADDER_CLIMB
                ),
                highlightEffect = BoardHighlightEffect(
                    tile = finalDest,
                    isPositive = true,
                    intensity = 1f
                )
            )
        }

        awaitComposeTokenMovement(
            playerId = player.id,
            pathTiles = listOf(ladder.startTile, finalDest),
            movementType = TokenMovementType.LADDER_CLIMB
        )

        updatePlayerTileInState(player.id, finalDest)
        _uiState.update { it.copy(highlightEffect = null) }
    }

    private suspend fun executeSnakeSlide(player: Player, snake: BoardLink) {
        val ability = player.character.abilityType
        val customSpec = player.character.customSpec
        val maxTiles = _uiState.value.maxTiles

        // 0. Check Phase 5 Custom Character SNAKES ability!
        if (customSpec?.target == CustomAbilityTarget.SNAKES) {
            val triggered = Random.nextInt(100) < customSpec.percentageOrValue
            if (triggered && (customSpec.effectType == CustomAbilityEffectType.NEUTRALIZE_IMMUNE ||
                    customSpec.effectType == CustomAbilityEffectType.TRIGGER_PROBABILITY)
            ) {
                soundManager.playAbilityActivateSound()
                _uiState.update { st ->
                    val remainingLinks = st.boardLinks.filterNot {
                        it.type == LinkType.SNAKE && it.startTile == snake.startTile
                    }
                    st.copy(
                        boardLinks = remainingLinks,
                        highlightEffect = BoardHighlightEffect(
                            tile = snake.startTile,
                            isPositive = true,
                            intensity = 1f,
                            heroFxType = HeroBoardFxType.SHIELD_BLOCK_SUCCESS
                        ),
                        eventBanner = GameEventBanner(
                            message = "قدرت سفارشی ${player.character.englishName}! 🛡️ نیش مار در خانه ${snake.startTile.toPersianDigits()} خنثی و مار نابود شد!",
                            type = BannerType.CHARACTER_ABILITY
                        )
                    )
                }
                delay(900L)
                _uiState.update { it.copy(highlightEffect = null) }
                return
            } else if (triggered && (customSpec.effectType == CustomAbilityEffectType.BONUS_STEPS_BOOST ||
                    customSpec.effectType == CustomAbilityEffectType.INCREASE_CHANCE)
            ) {
                soundManager.playAbilityActivateSound()
                val boostDest = (snake.startTile + 2).coerceAtMost(maxTiles)
                _uiState.update {
                    it.copy(
                        highlightEffect = BoardHighlightEffect(tile = boostDest, isPositive = true, intensity = 1f),
                        eventBanner = GameEventBanner(
                            message = "معجزهٔ سفارشی ${player.character.englishName}! ✨ نیش مار به +۲ خانه جهش رو به جلو تبدیل شد!",
                            type = BannerType.CHARACTER_ABILITY
                        )
                    )
                }
                awaitComposeTokenMovement(
                    playerId = player.id,
                    pathTiles = listOf(snake.startTile, boostDest),
                    movementType = TokenMovementType.LUCK_BOOST
                )
                updatePlayerTileInState(player.id, boostDest)
                _uiState.update { it.copy(highlightEffect = null) }
                return
            }
        }

        // 1. Check Guardian Shield, Guardian Angel, or Phantom Phase!
        val passivePhantomProc = ability == CharacterAbilityType.WIND_RIDER && Random.nextInt(100) < 35
        if (passivePhantomProc) {
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == player.id) p.copy(phantomPhaseActive = true) else p
                }
                st.copy(players = updated)
            }
        }
        val blockedByShieldOrAngel = evaluateShieldOrProtectionDefense(
            defenderId = player.id,
            threatCategory = ShieldThreatCategory.SNAKE,
            threatTile = snake.startTile,
            threatNamePersian = "مار خانه ${snake.startTile.toPersianDigits()}"
        )
        if (blockedByShieldOrAngel) {
            _uiState.update { it.copy(highlightEffect = null) }
            return
        }

        // 2. Check Snake Hunter (Blade): 65% chance to slay and destroy the snake!
        if (ability == CharacterAbilityType.SNAKE_HUNTER && Random.nextFloat() < 0.65f) {
            soundManager.playAbilityActivateSound()
            _uiState.update { st ->
                val remainingLinks = st.boardLinks.filterNot {
                    it.type == LinkType.SNAKE && it.startTile == snake.startTile
                }
                st.copy(
                    boardLinks = remainingLinks,
                    highlightEffect = BoardHighlightEffect(
                        tile = snake.startTile,
                        isPositive = true,
                        intensity = 1f,
                        heroFxType = HeroBoardFxType.EXECUTIONER_SLASH
                    ),
                    eventBanner = GameEventBanner(
                        message = "شمشیر شکارچی مار! ⚔️ ${player.name} مار خانه ${snake.startTile.toPersianDigits()} را از صفحه نابود کرد و سقوط نکرد!",
                        type = BannerType.CHARACTER_ABILITY
                    )
                )
            }
            delay(950L)
            _uiState.update { it.copy(highlightEffect = null) }
            return
        }

        // 3. Check Multi-Tier Snake Drop Reduction (Earth Titan 50%, Ivy 30%, or Custom SNAKES):
        val finalTailTile = when {
            ability == CharacterAbilityType.EARTH_TITAN -> {
                val midDrop = (snake.startTile - snake.endTile) / 2
                (snake.startTile - midDrop).coerceAtLeast(snake.endTile)
            }
            ability == CharacterAbilityType.WORM_IMMUNE -> {
                val reducedDrop = ((snake.startTile - snake.endTile) * 0.7f).roundToInt().coerceAtLeast(2)
                (snake.startTile - reducedDrop).coerceAtLeast(snake.endTile)
            }
            ability == CharacterAbilityType.LADDER_MAGE -> {
                // Luna's low armor trade-off: slides 2 tiles further down
                (snake.endTile - 2).coerceAtLeast(2)
            }
            customSpec?.target == CustomAbilityTarget.SNAKES &&
                (customSpec.effectType == CustomAbilityEffectType.HALVE_PENALTY ||
                    customSpec.effectType == CustomAbilityEffectType.REDUCE_EFFECT) -> {
                val fullDrop = snake.startTile - snake.endTile
                val reducedDrop = (fullDrop * (100 - customSpec.percentageOrValue.coerceIn(10, 85)) / 100f)
                    .roundToInt()
                    .coerceAtLeast(2)
                (snake.startTile - reducedDrop).coerceIn(snake.endTile, snake.startTile - 1)
            }
            else -> snake.endTile
        }

        // 4. Phase 4: Draw a random user-created Truth or Dare card when a human player lands on a Snake!
        val currentState = _uiState.value
        val eligibleCards = currentState.truthOrDareCards.filter {
            it.category in currentState.enabledCardCategories
        }.ifEmpty {
            currentState.truthOrDareCards
        }

        if (eligibleCards.isNotEmpty() && !player.isAiBot) {
            val drawnCard = eligibleCards.random()
            val cardDeferred = CompletableDeferred<Unit>()
            pendingSnakeCardCompletion = cardDeferred

            soundManager.playCardDrawSuspenseSound()
            _uiState.update { st ->
                st.copy(
                    activeSnakeCardDraw = ActiveSnakeCardDraw(
                        player = player,
                        snakeStartTile = snake.startTile,
                        snakeEndTile = finalTailTile,
                        card = drawnCard,
                        isRevealed = false
                    ),
                    highlightEffect = BoardHighlightEffect(
                        tile = snake.startTile,
                        isPositive = false,
                        intensity = 1f
                    ),
                    eventBanner = GameEventBanner(
                        message = "🐍 نیش مار و کشیدن کارت! ${player.name} باید یک کارت «${drawnCard.category.shortTitle}» بکشد!",
                        type = BannerType.SNAKE_BITE
                    )
                )
            }

            cardDeferred.await()
            pendingSnakeCardCompletion = null
        }

        soundManager.playSnakeSlide()
        val isAtlasBerserker = ability == CharacterAbilityType.EARTH_TITAN
        val isPhoenixDragonSoul = ability == CharacterAbilityType.PHOENIX_WINGS
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == player.id) {
                    p.copy(
                        snakesBitten = p.snakesBitten + 1,
                        previousTurnTile = snake.startTile,
                        berserkerRageTurns = if (isAtlasBerserker) 2 else p.berserkerRageTurns,
                        dragonSoulBoostTurns = if (isPhoenixDragonSoul) 2 else p.dragonSoulBoostTurns
                    )
                } else p
            }
            st.copy(
                players = updated,
                eventBanner = GameEventBanner(
                    message = when {
                        isAtlasBerserker ->
                            "پوست صخره‌ای و خشم برسرکر (Berserker)! 🪨🔥 ${player.name} نصف مسیر را ایستاد (خانه ${finalTailTile.toPersianDigits()}) و برای ۲ نوبت +۲ خانه خشم حرکت گرفت!"
                        finalTailTile > snake.endTile ->
                            "مقاومت ویژه! 🛡️ ${player.name} جلوی سقوط کامل مار را گرفت و در خانه ${finalTailTile.toPersianDigits()} ایستاد!"
                        eligibleCards.isEmpty() ->
                            "آخ! نیش مار در خانه ${snake.startTile.toPersianDigits()}! به خانه ${finalTailTile.toPersianDigits()} برگشت 🐍"
                        else ->
                            "${player.name} از مار خانه ${snake.startTile.toPersianDigits()} به ${finalTailTile.toPersianDigits()} سر خورد 🐍"
                    },
                    type = BannerType.SNAKE_BITE
                ),
                highlightEffect = BoardHighlightEffect(
                    tile = snake.startTile,
                    isPositive = false,
                    intensity = 1f,
                    heroFxType = if (isAtlasBerserker) HeroBoardFxType.BERSERKER_RAGE else HeroBoardFxType.NONE
                )
            )
        }

        awaitComposeTokenMovement(
            playerId = player.id,
            pathTiles = listOf(snake.startTile, finalTailTile),
            movementType = TokenMovementType.SNAKE_SLIDE
        )

        updatePlayerTileInState(player.id, finalTailTile)
        _uiState.update { it.copy(highlightEffect = null) }
    }

    private suspend fun awaitComposeTokenMovement(
        playerId: Int,
        pathTiles: List<Int>,
        movementType: TokenMovementType
    ) {
        pendingMovementCompletion?.complete(Unit)
        val moveId = ++moveSequenceCounter
        val deferred = CompletableDeferred<Unit>()
        pendingMovementCompletion = deferred

        _uiState.update {
            it.copy(
                activeMovementPath = TokenMovementPath(
                    moveId = moveId,
                    playerId = playerId,
                    pathTiles = pathTiles,
                    movementType = movementType
                )
            )
        }

        deferred.await()

        _uiState.update {
            if (it.activeMovementPath?.moveId == moveId) {
                it.copy(activeMovementPath = null)
            } else {
                it
            }
        }
    }

    private fun triggerWinnerCelebration(winnerId: Int) {
        aiTurnJob?.cancel()
        soundManager.setVictoryMusicOverride(true)
        soundManager.playVictoryFanfare()
        _uiState.update { state ->
            val winnerPlayer = state.players.find { it.id == winnerId }
            state.copy(
                phase = GamePhase.CELEBRATION,
                isDiceRolling = false,
                isTokenAnimating = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                winner = winnerPlayer,
                eventBanner = GameEventBanner(
                    message = "تبریک! ${winnerPlayer?.name.orEmpty()} (${winnerPlayer?.character?.defaultEnglishName.orEmpty()}) قهرمان شد! 🏆🎉",
                    type = BannerType.SIX_BONUS
                )
            )
        }
    }

    private suspend fun advanceTurnOrGrantBonus(rolledValue: Int, forceExtraTurn: Boolean) {
        val currentState = _uiState.value
        if (currentState.players.isEmpty()) return

        val currentPlayer = currentState.currentPlayer
        if ((rolledValue == 6 || forceExtraTurn) &&
            currentPlayer != null &&
            currentPlayer.swampSkipTurnsRemaining == 0 &&
            currentPlayer.frozenSkipTurnsRemaining == 0
        ) {
            _uiState.update { state ->
                state.copy(
                    isTokenAnimating = false,
                    sixRollGlowActive = true,
                    eventBanner = GameEventBanner(
                        message = if (forceExtraTurn) {
                            "پرتاب جایزه! 🍀 دوباره نوبت ${currentPlayer.name} است!"
                        } else {
                            "جایزهٔ تاس طلایی! 🌟 دوباره نوبت ${currentPlayer.name} است!"
                        },
                        type = BannerType.SIX_BONUS
                    )
                )
            }
            triggerAiTurnIfNeeded()
            return
        }

        val playerCount = currentState.players.size
        var candidateIndex = (currentState.currentTurnIndex + 1) % playerCount

        for (checked in 0 until playerCount) {
            val candidate = _uiState.value.players[candidateIndex]
            if (candidate.swampSkipTurnsRemaining > 0 || candidate.frozenSkipTurnsRemaining > 0) {
                val isFrozen = candidate.frozenSkipTurnsRemaining > 0
                soundManager.playObstacleSound(if (isFrozen) ObstacleType.WIND else ObstacleType.SWAMP)
                _uiState.update { st ->
                    val updated = st.players.mapIndexed { idx, p ->
                        if (idx == candidateIndex) {
                            p.copy(
                                swampSkipTurnsRemaining = (p.swampSkipTurnsRemaining - 1).coerceAtLeast(0),
                                frozenSkipTurnsRemaining = (p.frozenSkipTurnsRemaining - 1).coerceAtLeast(0)
                            )
                        } else p
                    }
                    st.copy(
                        players = updated,
                        currentTurnIndex = candidateIndex,
                        isTokenAnimating = true,
                        sixRollGlowActive = false,
                        eventBanner = GameEventBanner(
                            message = if (isFrozen) {
                                "${candidate.name} بر اثر شمشیر یخی منجمد است و این نوبت را از دست می‌دهد! ❄️"
                            } else {
                                "${candidate.name} در باتلاق گیر کرده است و این دور را از دست می‌دهد! 🌿"
                            },
                            type = BannerType.OBSTACLE_DANGER
                        )
                    )
                }
                delay(1150L)
                candidateIndex = (candidateIndex + 1) % playerCount
            } else {
                break
            }
        }

        soundManager.playTurnChangeChime()

        _uiState.update { state ->
            val nextPlayer = state.players[candidateIndex]
            val statusHint = when {
                nextPlayer.dragonSoulBoostTurns > 0 -> " (روح اژدها فعال: +۲ خانه 🔥)"
                nextPlayer.berserkerRageTurns > 0 -> " (خشم برسرکر فعال: +۲ خانه 🪨🔥)"
                nextPlayer.halveNextRoll -> " (تاس این نوبت نصف می‌شود 🥾)"
                nextPlayer.gravityCapNextRoll -> " (لنگر جاذبه: سقف تاس ۴ است ⚓)"
                nextPlayer.wormCurseTurnsRemaining > 0 -> " (طلسم کرم: ${nextPlayer.wormCurseTurnsRemaining.toPersianDigits()} نوبت 🪱)"
                nextPlayer.canTriggerActiveAbilityNow -> " (قابلیت ${nextPlayer.character.displayAbilityTitle} آماده است ⚡)"
                else -> ""
            }
            state.copy(
                currentTurnIndex = candidateIndex,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                eventBanner = GameEventBanner(
                    message = "نوبت ${nextPlayer.name} (خانه ${nextPlayer.currentTile.toPersianDigits()})$statusHint است 🎲",
                    type = BannerType.NORMAL
                )
            )
        }

        triggerAiTurnIfNeeded()
    }

    /**
     * Phase 5 Single-Player AI Opponent Turn Controller!
     */
    private fun triggerAiTurnIfNeeded() {
        val state = _uiState.value
        val activePlayer = state.currentPlayer ?: return
        if (state.phase != GamePhase.PLAYING || !activePlayer.isAiBot) return

        aiTurnJob?.cancel()
        aiTurnJob = viewModelScope.launch {
            delay(850L)
            val latestState = _uiState.value
            val aiPlayer = latestState.currentPlayer ?: return@launch
            if (latestState.phase != GamePhase.PLAYING || !aiPlayer.isAiBot) return@launch

            if (aiPlayer.canTriggerActiveAbilityNow) {
                val shouldUseAbility = when (latestState.aiDifficulty) {
                    AiDifficulty.EASY -> Random.nextFloat() < 0.25f
                    AiDifficulty.MEDIUM -> Random.nextFloat() < 0.65f
                    AiDifficulty.HARD -> true
                }
                if (shouldUseAbility) {
                    activateCharacterAbility()
                    delay(950L)
                }
            }

            rollDice()
        }
    }

    private fun chooseBestPrecisionOptionForAi(aiPlayer: Player, choice: PrecisionRollChoice): Int {
        val state = _uiState.value
        val options = listOf(choice.die1, choice.die2, choice.sum).distinct()
        if (state.aiDifficulty == AiDifficulty.EASY) {
            return options.random()
        }

        fun scoreOption(steps: Int): Int {
            val dest = aiPlayer.currentTile + steps
            if (dest > state.maxTiles) return -100
            if (dest == state.maxTiles) return 1000
            val link = state.boardLinks.find { it.startTile == dest }
            if (link?.type == LinkType.LADDER) return link.endTile * 3
            if (link?.type == LinkType.SNAKE) return link.endTile - 100
            val obs = state.boardObstacles.find { it.tile == dest }
            if (obs != null && obs.type.isDangerous) return dest - 40
            return dest
        }

        return options.maxByOrNull { scoreOption(it) } ?: choice.sum
    }

    private fun updatePlayerTileInState(playerId: Int, newTile: Int) {
        _uiState.update { state ->
            val updatedPlayers = state.players.map { p ->
                if (p.id == playerId) p.copy(currentTile = newTile) else p
            }
            state.copy(players = updatedPlayers)
        }
    }

    // --- Deterministic Synchronous Helper Functions for Unit Testing ---

    fun setPlayerPositionForTest(playerId: Int, tile: Int, previousTurnTile: Int = tile) {
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) p.copy(currentTile = tile, previousTurnTile = previousTurnTile) else p
            }
            st.copy(players = updated)
        }
    }

    fun setPlayerDecoyForTest(playerId: Int, decoyTile: Int?, turnsRemaining: Int = 2) {
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) p.copy(
                    illusionDecoyTile = decoyTile,
                    illusionTurnsRemaining = turnsRemaining
                ) else p
            }
            st.copy(players = updated)
        }
    }

    fun simulatePersonalTurnsForPlayerForTest(playerId: Int, turnsToAdvance: Int) {
        repeat(turnsToAdvance.coerceAtLeast(0)) {
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == playerId) advancePersonalTurnState(p, wasAbilityArmed = false) else p
                }
                st.copy(players = updated)
            }
        }
    }

    fun triggerLeaderSwapForTest(casterId: Int): Boolean {
        val state = _uiState.value
        val caster = state.players.find { it.id == casterId } ?: return false
        if (caster.character.abilityType != CharacterAbilityType.SHADOW_WALKER) return false
        if (caster.leaderSwapWindowTurnsRemaining <= 0) return false

        val liveLeader = state.players
            .filter { it.id != casterId && it.currentTile > caster.currentTile }
            .maxByOrNull { it.currentTile } ?: return false

        val myOldTile = caster.currentTile
        val leaderOldTile = liveLeader.currentTile
        val nextCharge = caster.personalTurnsTaken + 10

        _uiState.update { st ->
            val updated = st.players.map { p ->
                when (p.id) {
                    casterId -> p.copy(
                        currentTile = leaderOldTile,
                        leaderSwapWindowTurnsRemaining = 0,
                        leaderSwapNextChargeTurn = nextCharge,
                        abilityUsesCount = p.abilityUsesCount + 1
                    )
                    liveLeader.id -> p.copy(
                        previousTurnTile = leaderOldTile,
                        currentTile = myOldTile
                    )
                    else -> p
                }
            }
            st.copy(players = updated)
        }
        return true
    }

    suspend fun triggerCoTileExecutionerHuntForTest(
        hunterId: Int,
        landedTile: Int,
        deterministicRollOverride: Int? = null
    ): Boolean {
        updatePlayerTileInState(hunterId, landedTile)
        return checkAndExecuteCoTileHunt(
            hunterId = hunterId,
            landedTile = landedTile,
            deterministicRollOverride = deterministicRollOverride,
            skipAnimationForTest = true
        )
    }

    suspend fun triggerCombatInteractionForTest(
        attackerId: Int,
        landedTile: Int,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null
    ): Boolean {
        updatePlayerTileInState(attackerId, landedTile)
        return resolveTileCombatAndInteraction(
            movingPlayerId = attackerId,
            landedTile = landedTile,
            forcedAttackRoll = forcedAttackRoll,
            forcedDefenseRoll = forcedDefenseRoll,
            skipAnimationForTest = true
        )
    }

    suspend fun evaluateShieldDefenseForTest(
        defenderId: Int,
        threatCategory: ShieldThreatCategory,
        deterministicRollOverride: Int
    ): Boolean {
        val tile = _uiState.value.players.find { it.id == defenderId }?.currentTile ?: 10
        return evaluateShieldOrProtectionDefense(
            defenderId = defenderId,
            threatCategory = threatCategory,
            threatTile = tile,
            threatNamePersian = threatCategory.persianLabel,
            deterministicRollOverride = deterministicRollOverride,
            skipDelay = true
        )
    }

    override fun onCleared() {
        super.onCleared()
        aiTurnJob?.cancel()
        soundManager.release()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(context.applicationContext) as T
        }
    }
}
