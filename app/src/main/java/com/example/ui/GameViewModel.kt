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
import com.example.model.MatchHistoryRecord
import com.example.model.MatchParticipantRecord
import com.example.model.MusicGenre
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.PrecisionRollChoice
import com.example.model.SettingsProfile
import com.example.model.ShieldThreatCategory
import com.example.model.SnakeVisualTheme
import com.example.model.TruthOrDareCard
import com.example.model.toPersianDigits
import java.util.UUID
import com.example.engine.AbilityActivationResult
import com.example.engine.BoardObstacleEngine
import com.example.engine.CombatResolutionEngine
import com.example.engine.DefaultRandomProvider
import com.example.engine.DomainBoardFxType
import com.example.engine.DomainMovementMotionType
import com.example.engine.HeroAbilityEngine
import com.example.engine.MatchCheckResult
import com.example.engine.MatchResolutionEngine
import com.example.engine.RandomProvider
import com.example.engine.TurnAndMovementEngine
import com.example.model.resource.ActiveStatusEffect
import com.example.model.resource.PlayerResources
import com.example.model.resource.ResourceConfig
import com.example.model.resource.StatusEffectType
import com.example.model.team.GameTeam
import com.example.model.team.TeamManager
import com.example.model.team.TeamMode
import com.example.model.team.TeamVictoryMode
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

private fun DomainMovementMotionType.toUiMotion(): TokenMovementType = when (this) {
    DomainMovementMotionType.STEP_BY_STEP -> TokenMovementType.STEP_BY_STEP
    DomainMovementMotionType.LADDER_CLIMB -> TokenMovementType.LADDER_CLIMB
    DomainMovementMotionType.SNAKE_SLIDE -> TokenMovementType.SNAKE_SLIDE
    DomainMovementMotionType.REVERSE_BACKWARD -> TokenMovementType.REVERSE_BACKWARD
    DomainMovementMotionType.WELL_PLUNGE -> TokenMovementType.WELL_PLUNGE
    DomainMovementMotionType.WIND_GUST -> TokenMovementType.WIND_GUST
    DomainMovementMotionType.LUCK_BOOST -> TokenMovementType.LUCK_BOOST
    DomainMovementMotionType.STORM_SHIFT -> TokenMovementType.STORM_SHIFT
    DomainMovementMotionType.SHADOW_SWAP -> TokenMovementType.SHADOW_SWAP
    DomainMovementMotionType.EXECUTIONER_HUNT -> TokenMovementType.EXECUTIONER_HUNT
    DomainMovementMotionType.TIME_REWIND -> TokenMovementType.TIME_REWIND
}

private fun DomainBoardFxType.toUiFx(): HeroBoardFxType = when (this) {
    DomainBoardFxType.NONE -> HeroBoardFxType.NONE
    DomainBoardFxType.EXECUTIONER_SLASH -> HeroBoardFxType.EXECUTIONER_SLASH
    DomainBoardFxType.SHIELD_BLOCK_SUCCESS -> HeroBoardFxType.SHIELD_BLOCK_SUCCESS
    DomainBoardFxType.SHIELD_BREAK_FAIL -> HeroBoardFxType.SHIELD_BREAK_FAIL
    DomainBoardFxType.DRAGON_SOUL_AURA -> HeroBoardFxType.DRAGON_SOUL_AURA
    DomainBoardFxType.BERSERKER_RAGE -> HeroBoardFxType.BERSERKER_RAGE
    DomainBoardFxType.TIME_REWIND -> HeroBoardFxType.TIME_REWIND
    DomainBoardFxType.ILLUSION_MIRAGE -> HeroBoardFxType.ILLUSION_MIRAGE
    DomainBoardFxType.GUARDIAN_ANGEL_WINGS -> HeroBoardFxType.GUARDIAN_ANGEL_WINGS
    DomainBoardFxType.TRICKSTER_TRANSFER -> HeroBoardFxType.TRICKSTER_TRANSFER
    DomainBoardFxType.PHANTOM_PHASE -> HeroBoardFxType.PHANTOM_PHASE
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
    val unlockedPawnSkinIds: Set<String> = com.example.model.PawnSkinCatalog.defaultUnlockedSkinIds,
    // Hero & In-Game UI Redesign System:
    val pendingAbilityConfirmationPlayerId: Int? = null,
    val inGameHeroInfoPlayerId: Int? = null,
    val activeHeroClash: com.example.model.HeroClashEvent? = null,
    val isHeroCollectionOpen: Boolean = false,
    val heroCollectionActiveSlot: Int = 0,
    // Phase 1 Architecture: Team Infrastructure:
    val teamMode: TeamMode = TeamMode.SOLO_FREE_FOR_ALL,
    val teamVictoryMode: TeamVictoryMode = TeamVictoryMode.ONE_MEMBER_FINISH,
    val customTeamAssignments: Map<Int, String> = (0 until 8).associateWith {
        TeamManager.CustomTeamPresets[it % 2].first
    },
    val isTeamModeEnabled: Boolean = false,
    // Phase 2 Gameplay Core Integration State:
    val consecutiveSixesCount: Int = 0,
    val isMatchDraw: Boolean = false,
    val winningTeam: GameTeam? = null,
    val winningPlayers: List<Player> = emptyList(),
    val matchWinReason: String? = null,
    val matchRankings: List<Player> = emptyList(),
    // Phase 2.1 Gameplay Rules: Respawn & Post-Victory Continuation
    val pendingRespawnPlayerIds: List<Int> = emptyList(),
    val activeRespawnPromptPlayerId: Int? = null,
    val eliminatedOutPlayerIds: Set<Int> = emptySet(),
    val respawnOfferedPlayerIds: Set<Int> = emptySet(),
    val isContinuingAfterVictory: Boolean = false,
    val hasFinalizedVictory: Boolean = false,
    val recordedWinner: Player? = null,
    val recordedWinningTeam: GameTeam? = null,
    val recordedWinningPlayers: List<Player> = emptyList(),
    val recordedWinReason: String? = null,
    val finishedPlayerIds: Set<Int> = emptySet(),
    val finishOrderPlayerIds: List<Int> = emptyList(),
    // Phase 4 Match History & Statistics:
    val matchHistory: List<MatchHistoryRecord> = emptyList(),
    val isMatchHistoryOpen: Boolean = false,
    val selectedMatchHistoryDetail: MatchHistoryRecord? = null,
    val currentMatchUuid: String = "",
    val hasRecordedCurrentMatch: Boolean = false
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentTurnIndex)

    val leaderPlayer: Player?
        get() = players.filter { it.resources.isAlive }.maxByOrNull { it.currentTile }
            ?: players.maxByOrNull { it.currentTile }

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

    val activeRespawnPlayer: Player?
        get() = activeRespawnPromptPlayerId?.let { id -> players.find { it.id == id } }

    val canContinueAfterVictory: Boolean
        get() {
            if (isMatchDraw || winner == null || hasFinalizedVictory) return false
            val wonIds = (winningPlayers.map { it.id } + winner.id).toSet()
            return players.any {
                it.resources.isAlive &&
                    it.id !in eliminatedOutPlayerIds &&
                    it.id !in finishedPlayerIds &&
                    it.id !in wonIds
            }
        }

    fun resolveCharacterById(id: String): GameCharacter {
        return CharacterRoster.getById(id, customCharacters)
    }
}

class GameViewModel(
    context: Context,
    private val soundManager: GameSoundManager = GameSoundManager(context),
    private val cardRepository: TruthOrDareRepository = TruthOrDareRepository(context),
    private val randomProvider: RandomProvider = DefaultRandomProvider(),
    private val skipAnimationDelaysForTest: Boolean = false
) : ViewModel() {

    private val prefs = context.getSharedPreferences("mystic_snakes_session_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(loadInitialSetupState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var moveSequenceCounter = 0L
    private var pendingMovementCompletion: CompletableDeferred<Unit>? = null
    private var pendingSnakeCardCompletion: CompletableDeferred<Unit>? = null
    private var feedbackClearJob: Job? = null
    private var aiTurnJob: Job? = null
    private var hasDeferredPostMoveTurn: Boolean = false
    private var deferredPostMoveSixBonus: Boolean = false
    private var deferredPostMoveLuckExtra: Boolean = false
    private val matchRecordLock = Any()
    private val recordedMatchUuids = mutableSetOf<String>()

    // Phase 1 & Phase 2 Architecture: Modular Domain Engines synchronized with dynamic maxTiles
    var turnMovementEngine = TurnAndMovementEngine(maxTiles = _uiState.value.maxTiles)
        private set
    val obstacleEngine = BoardObstacleEngine(randomProvider = randomProvider)
    val heroAbilityEngine = HeroAbilityEngine()
    var combatResolutionEngine = CombatResolutionEngine(
        randomProvider = randomProvider,
        maxTiles = _uiState.value.maxTiles
    )
        private set
    var matchResolutionEngine = MatchResolutionEngine(winningTile = _uiState.value.maxTiles)
        private set
    var teamManager: TeamManager = buildTeamManager(
        playerCount = _uiState.value.playerCount,
        mode = _uiState.value.teamMode,
        customAssignments = _uiState.value.customTeamAssignments
    )
        private set

    private fun syncEnginesForMaxTiles(maxTiles: Int) {
        val safeMax = (maxTiles / 10 * 10).coerceIn(50, 200)
        turnMovementEngine = TurnAndMovementEngine(maxTiles = safeMax)
        combatResolutionEngine = CombatResolutionEngine(
            randomProvider = randomProvider,
            maxTiles = safeMax
        )
        matchResolutionEngine = MatchResolutionEngine(winningTile = safeMax)
    }

    private suspend fun animDelay(durationMs: Long) {
        if (!skipAnimationDelaysForTest && durationMs > 0L) {
            delay(durationMs)
        }
    }

    init {
        viewModelScope.launch {
            cardRepository.allCards.collect { savedCards ->
                _uiState.update { it.copy(truthOrDareCards = savedCards) }
            }
        }
        viewModelScope.launch {
            cardRepository.customCharacters.collect { savedCustomChars ->
                _uiState.update { st ->
                    val cleanedCharIds = st.playerCharacterIds.toMutableList()
                    val cleanedNames = st.playerNamesInput.toMutableList()
                    val usedIds = cleanedCharIds.toMutableSet()
                    cleanedCharIds.forEachIndexed { idx, id ->
                        if (CharacterRoster.findByIdOrNull(id, savedCustomChars) == null) {
                            val replacement = CharacterRoster.allCharacters.firstOrNull { it.id !in usedIds }
                                ?: CharacterRoster.allCharacters[idx % CharacterRoster.allCharacters.size]
                            usedIds.add(replacement.id)
                            cleanedCharIds[idx] = replacement.id
                            prefs.edit().putString("player_char_$idx", replacement.id).apply()
                            if (cleanedNames[idx].isBlank() || cleanedNames[idx] == "قهرمان سفارشی") {
                                cleanedNames[idx] = replacement.defaultEnglishName
                                prefs.edit().putString("player_name_$idx", replacement.defaultEnglishName).apply()
                            }
                        }
                    }
                    val updatedPlayers = st.players.map { p ->
                        val resolved = CharacterRoster.findByIdOrNull(p.characterId, savedCustomChars)
                        if (resolved != null) {
                            p.copy(customCharacterOverride = if (resolved.isCustom) resolved else null)
                        } else {
                            p
                        }
                    }
                    st.copy(
                        customCharacters = savedCustomChars,
                        playerCharacterIds = cleanedCharIds,
                        playerNamesInput = cleanedNames,
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
        viewModelScope.launch {
            cardRepository.matchHistory.collect { records ->
                _uiState.update { st ->
                    val refreshedDetail = st.selectedMatchHistoryDetail?.let { selected ->
                        records.find { it.id == selected.id || it.matchUuid == selected.matchUuid }
                    }
                    st.copy(
                        matchHistory = records,
                        selectedMatchHistoryDetail = refreshedDetail
                    )
                }
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
            val rawId = prefs.getString("player_char_$idx", null) ?: defaultChars[idx].id
            if (CharacterRoster.findByIdOrNull(rawId) != null || rawId.startsWith("custom_")) {
                rawId
            } else {
                defaultChars[idx].id
            }
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
            val charDefault = CharacterRoster.findByIdOrNull(savedCharIds[idx])?.defaultEnglishName
                ?: "قهرمان سفارشی"
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
        val savedPawnSkinIds = List(8) { idx ->
            prefs.getString("player_pawn_skin_$idx", null) ?: "classic_standard"
        }
        val savedUnlockedPawnSkinIds = prefs.getStringSet("unlocked_pawn_skins", null)
            ?.plus(com.example.model.PawnSkinCatalog.defaultUnlockedSkinIds)
            ?: com.example.model.PawnSkinCatalog.defaultUnlockedSkinIds

        val savedTeamModeName = prefs.getString("team_mode", TeamMode.SOLO_FREE_FOR_ALL.name)
        val savedTeamMode = TeamMode.entries.find { it.name == savedTeamModeName } ?: TeamMode.SOLO_FREE_FOR_ALL

        val savedTeamVictoryName = prefs.getString("team_victory_mode", TeamVictoryMode.ONE_MEMBER_FINISH.name)
        val savedTeamVictoryMode = TeamVictoryMode.entries.find { it.name == savedTeamVictoryName }
            ?: TeamVictoryMode.ONE_MEMBER_FINISH

        val validCustomPresetIds = TeamManager.CustomTeamPresets.map { it.first }.toSet()
        val savedCustomTeamAssignments = (0 until 8).associateWith { idx ->
            val rawTeam = prefs.getString("custom_team_slot_$idx", null)
            if (rawTeam != null && rawTeam in validCustomPresetIds) {
                rawTeam
            } else {
                TeamManager.CustomTeamPresets[idx % 2].first
            }
        }

        return GameUiState(
            phase = GamePhase.SETUP,
            playerCount = savedCount,
            activeSetupPlayerSlot = 0,
            playerCharacterIds = savedCharIds,
            playerPawnSkinIds = savedPawnSkinIds,
            unlockedPawnSkinIds = savedUnlockedPawnSkinIds,
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
            teamMode = savedTeamMode,
            teamVictoryMode = savedTeamVictoryMode,
            customTeamAssignments = savedCustomTeamAssignments,
            isTeamModeEnabled = savedTeamMode != TeamMode.SOLO_FREE_FOR_ALL,
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
        syncEnginesForMaxTiles(clamped)
        _uiState.update { state ->
            val newBoard = BoardGenerator.generateBoardConfiguration(
                maxTiles = clamped,
                difficulty = state.difficulty,
                enabledObstacles = state.enabledObstacles
            )
            val clampedPlayers = state.players.map { p ->
                p.copy(currentTile = p.currentTile.coerceIn(0, clamped - 1))
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
        syncEnginesForMaxTiles(profile.maxTiles)
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
                    message = if (newState) "انیمیشن محیط اطراف فعال شد! 🌿" else "انیمیشن محیط اطراف متوقف شد.",
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
        prefs.edit().putString("player_pawn_skin_$slot", skinId).apply()
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
        for (i in 0 until 8) {
            prefs.edit().putString("player_pawn_skin_$i", skinId).apply()
        }
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

    // Hero Collection Dialog Controls:
    fun openHeroCollection(slot: Int = 0) {
        soundManager.playDiceTick(4)
        _uiState.update {
            val safeSlot = slot.coerceIn(0, (it.playerCount - 1).coerceAtLeast(0))
            it.copy(
                isHeroCollectionOpen = true,
                heroCollectionActiveSlot = safeSlot
            )
        }
    }

    fun closeHeroCollection() {
        soundManager.playDiceTick(1)
        _uiState.update { it.copy(isHeroCollectionOpen = false) }
    }

    // In-Game Hero Info HUD Controls:
    fun openInGameHeroInfo(playerId: Int) {
        soundManager.playDiceTick(3)
        _uiState.update { it.copy(inGameHeroInfoPlayerId = playerId) }
    }

    fun closeInGameHeroInfo() {
        soundManager.playDiceTick(1)
        _uiState.update { it.copy(inGameHeroInfoPlayerId = null) }
    }

    // Ability Confirmation Modal Controls:
    fun requestAbilityActivation(playerId: Int) {
        soundManager.playDiceTick(4)
        _uiState.update { it.copy(pendingAbilityConfirmationPlayerId = playerId) }
    }

    fun cancelAbilityActivation() {
        soundManager.playDiceTick(1)
        _uiState.update { it.copy(pendingAbilityConfirmationPlayerId = null) }
    }

    fun confirmAbilityActivation(playerId: Int) {
        _uiState.update { it.copy(pendingAbilityConfirmationPlayerId = null) }
        activateCharacterAbility()
    }

    // Hero Clash Interaction Overlay:
    fun dismissHeroClash() {
        _uiState.update { it.copy(activeHeroClash = null) }
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
        val currentState = _uiState.value
        teamManager = buildTeamManager(clamped, currentState.teamMode, currentState.customTeamAssignments)
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
        val newChar = CharacterRoster.findByIdOrNull(characterId, state.customCharacters) ?: return
        val takenByOtherActive = (0 until state.playerCount).any { idx ->
            idx != playerSlot && state.playerCharacterIds[idx] == characterId
        }
        if (takenByOtherActive) return

        val oldChar = CharacterRoster.findByIdOrNull(state.playerCharacterIds[playerSlot], state.customCharacters)
            ?: CharacterRoster.allCharacters.first()
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

    // Phase 1 & 2.1 Architecture: Team Infrastructure & Resource Management
    private fun buildTeamManager(
        playerCount: Int,
        mode: TeamMode,
        customAssignments: Map<Int, String>
    ): TeamManager {
        return if (mode == TeamMode.CUSTOM_TEAMS) {
            TeamManager.createCustomTeams(playerCount, customAssignments)
        } else {
            TeamManager.createDefaultTeams(playerCount, mode)
        }
    }

    fun setTeamMode(mode: TeamMode) {
        soundManager.playDiceTick(3)
        prefs.edit().putString("team_mode", mode.name).apply()
        val state = _uiState.value
        teamManager = buildTeamManager(state.playerCount, mode, state.customTeamAssignments)
        _uiState.update {
            it.copy(
                teamMode = mode,
                isTeamModeEnabled = mode != TeamMode.SOLO_FREE_FOR_ALL
            )
        }
        showCardSaveFeedback("حالت تیم‌ها به «${mode.persianTitle}» تغییر کرد! 🛡️")
    }

    fun setTeamVictoryMode(victoryMode: TeamVictoryMode) {
        soundManager.playDiceTick(3)
        prefs.edit().putString("team_victory_mode", victoryMode.name).apply()
        _uiState.update { it.copy(teamVictoryMode = victoryMode) }
        showCardSaveFeedback("شرط پیروزی تیمی: «${victoryMode.persianTitle}» ذخیره شد! 🏆")
    }

    fun assignSetupSlotToTeam(slotIndex: Int, teamId: String) {
        if (slotIndex !in 0..7) return
        val validIds = TeamManager.CustomTeamPresets.map { it.first }.toSet()
        if (teamId !in validIds) return

        soundManager.playDiceTick(2)
        prefs.edit()
            .putString("custom_team_slot_$slotIndex", teamId)
            .putString("team_mode", TeamMode.CUSTOM_TEAMS.name)
            .apply()

        _uiState.update { st ->
            val updatedAssignments = st.customTeamAssignments.toMutableMap().apply {
                put(slotIndex, teamId)
            }
            teamManager = buildTeamManager(st.playerCount, TeamMode.CUSTOM_TEAMS, updatedAssignments)
            st.copy(
                teamMode = TeamMode.CUSTOM_TEAMS,
                isTeamModeEnabled = true,
                customTeamAssignments = updatedAssignments
            )
        }
    }

    fun assignPlayerToTeam(playerId: Int, teamId: String) {
        teamManager.assignPlayerToTeam(playerId, teamId)
        _uiState.update { st ->
            val updated = st.players.map { if (it.id == playerId) it.copy(teamId = teamId) else it }
            st.copy(players = updated)
        }
    }

    fun getPlayerResources(playerId: Int): PlayerResources {
        return _uiState.value.players.find { it.id == playerId }?.resources ?: PlayerResources()
    }

    fun damagePlayer(playerId: Int, amount: Int): Int {
        var actualHpDamage = 0
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) {
                    val (res, dmg) = p.resources.takeDamage(amount)
                    actualHpDamage = dmg
                    p.copy(resources = res)
                } else p
            }
            st.copy(players = updated)
        }
        syncEliminatedPlayersForRespawn()
        checkAndApplyMatchResolution()
        return actualHpDamage
    }

    fun healPlayer(playerId: Int, amount: Int) {
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) p.copy(resources = p.resources.heal(amount)) else p
            }
            st.copy(players = updated)
        }
    }

    fun restorePlayerMana(playerId: Int, amount: Int) {
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) p.copy(resources = p.resources.restoreMana(amount)) else p
            }
            st.copy(players = updated)
        }
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
        syncEnginesForMaxTiles(current.maxTiles)

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
                customPawnSkinId = skinId,
                resources = PlayerResources()
            )
        }

        // Phase 5: In Single-Player mode (playerCount == 1), add an intelligent AI opponent!
        val rawPlayers = if (current.playerCount == 1) {
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
                customPawnSkinId = aiSkinId,
                resources = PlayerResources()
            )
            listOf(humanPlayers.first(), aiBot)
        } else {
            humanPlayers
        }

        teamManager = buildTeamManager(rawPlayers.size, current.teamMode, current.customTeamAssignments)
        val activePlayers = rawPlayers.map { p ->
            p.copy(teamId = teamManager.getTeamForPlayer(p.id)?.id)
        }

        val boardConfig = BoardGenerator.generateBoardConfiguration(
            maxTiles = current.maxTiles,
            difficulty = current.difficulty,
            enabledObstacles = current.enabledObstacles
        )

        hasDeferredPostMoveTurn = false
        deferredPostMoveSixBonus = false
        deferredPostMoveLuckExtra = false
        soundManager.setVictoryMusicOverride(false)
        soundManager.playMatchStartFanfare()

        val matchUuid = UUID.randomUUID().toString()
        val firstPlayer = activePlayers.first()
        _uiState.update {
            it.copy(
                phase = GamePhase.PLAYING,
                players = activePlayers,
                boardLinks = boardConfig.links,
                boardObstacles = boardConfig.obstacles,
                currentTurnIndex = 0,
                consecutiveSixesCount = 0,
                diceValue = 6,
                isDiceRolling = false,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                activeSnakeCardDraw = null,
                combatLogs = emptyList(),
                activeHeroClash = null,
                winner = null,
                isMatchDraw = false,
                winningTeam = null,
                winningPlayers = emptyList(),
                matchWinReason = null,
                matchRankings = emptyList(),
                pendingRespawnPlayerIds = emptyList(),
                activeRespawnPromptPlayerId = null,
                eliminatedOutPlayerIds = emptySet(),
                respawnOfferedPlayerIds = emptySet(),
                isContinuingAfterVictory = false,
                hasFinalizedVictory = false,
                recordedWinner = null,
                recordedWinningTeam = null,
                recordedWinningPlayers = emptyList(),
                recordedWinReason = null,
                finishedPlayerIds = emptySet(),
                finishOrderPlayerIds = emptyList(),
                currentMatchUuid = matchUuid,
                hasRecordedCurrentMatch = false,
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
        if (current.phase == GamePhase.CELEBRATION && (current.winner != null || current.isMatchDraw)) {
            recordCompletedMatchOnce(current)
        }
        if (current.players.isEmpty()) {
            startGame()
            return
        }
        syncEnginesForMaxTiles(current.maxTiles)
        teamManager = buildTeamManager(current.players.size, current.teamMode, current.customTeamAssignments)
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
                pinnedSlowSteps = 0,
                teamId = teamManager.getTeamForPlayer(p.id)?.id,
                resources = PlayerResources()
            )
        }
        val newBoard = BoardGenerator.generateBoardConfiguration(
            maxTiles = current.maxTiles,
            difficulty = current.difficulty,
            enabledObstacles = current.enabledObstacles
        )
        hasDeferredPostMoveTurn = false
        deferredPostMoveSixBonus = false
        deferredPostMoveLuckExtra = false
        soundManager.setVictoryMusicOverride(false)
        soundManager.playMatchStartFanfare()
        val newMatchUuid = UUID.randomUUID().toString()
        _uiState.update {
            it.copy(
                phase = GamePhase.PLAYING,
                players = resetPlayers,
                boardLinks = newBoard.links,
                boardObstacles = newBoard.obstacles,
                currentTurnIndex = 0,
                consecutiveSixesCount = 0,
                diceValue = 6,
                isDiceRolling = false,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                activeSnakeCardDraw = null,
                combatLogs = emptyList(),
                activeHeroClash = null,
                winner = null,
                isMatchDraw = false,
                winningTeam = null,
                winningPlayers = emptyList(),
                matchWinReason = null,
                matchRankings = emptyList(),
                pendingRespawnPlayerIds = emptyList(),
                activeRespawnPromptPlayerId = null,
                eliminatedOutPlayerIds = emptySet(),
                respawnOfferedPlayerIds = emptySet(),
                isContinuingAfterVictory = false,
                hasFinalizedVictory = false,
                recordedWinner = null,
                recordedWinningTeam = null,
                recordedWinningPlayers = emptyList(),
                recordedWinReason = null,
                finishedPlayerIds = emptySet(),
                finishOrderPlayerIds = emptyList(),
                currentMatchUuid = newMatchUuid,
                hasRecordedCurrentMatch = false,
                eventBanner = GameEventBanner(
                    message = "بازی از نو آغاز شد! نوبت ${resetPlayers.first().name} است 🎲",
                    type = BannerType.NORMAL
                )
            )
        }
    }

    fun returnToSetup() {
        val current = _uiState.value
        if (current.phase == GamePhase.CELEBRATION && (current.winner != null || current.isMatchDraw)) {
            recordCompletedMatchOnce(current)
        }
        aiTurnJob?.cancel()
        pendingMovementCompletion?.complete(Unit)
        pendingSnakeCardCompletion?.complete(Unit)
        hasDeferredPostMoveTurn = false
        deferredPostMoveSixBonus = false
        deferredPostMoveLuckExtra = false
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
                winner = null,
                isMatchDraw = false,
                winningTeam = null,
                winningPlayers = emptyList(),
                matchWinReason = null,
                matchRankings = emptyList(),
                pendingRespawnPlayerIds = emptyList(),
                activeRespawnPromptPlayerId = null,
                eliminatedOutPlayerIds = emptySet(),
                respawnOfferedPlayerIds = emptySet(),
                isContinuingAfterVictory = false,
                hasFinalizedVictory = false,
                recordedWinner = null,
                recordedWinningTeam = null,
                recordedWinningPlayers = emptyList(),
                recordedWinReason = null,
                finishedPlayerIds = emptySet(),
                finishOrderPlayerIds = emptyList(),
                currentMatchUuid = "",
                hasRecordedCurrentMatch = false
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

        var nextChargeTurn = player.leaderSwapNextChargeTurn
        var windowRemaining = player.leaderSwapWindowTurnsRemaining

        if (ability == CharacterAbilityType.SHADOW_WALKER) {
            if (windowRemaining > 0) {
                windowRemaining -= 1
                if (windowRemaining == 0) {
                    nextChargeTurn = ((newPersonalTurn / 10) + 1) * 10
                }
            } else if (newPersonalTurn >= nextChargeTurn) {
                windowRemaining = 2
            }
        }

        val newIllusionTurns = (player.illusionTurnsRemaining - 1).coerceAtLeast(0)
        val newIllusionTile = if (newIllusionTurns > 0) player.illusionDecoyTile else null

        return player.copy(
            personalTurnsTaken = newPersonalTurn,
            leaderSwapNextChargeTurn = nextChargeTurn,
            leaderSwapWindowTurnsRemaining = windowRemaining,
            illusionTurnsRemaining = newIllusionTurns,
            illusionDecoyTile = newIllusionTile
        )
    }

    /**
     * Evaluates Guardian Shield, Guardian Angel, Phantom Phase, or Illusionist Mirage defense
     * for [defenderId] by delegating to [obstacleEngine].
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
        if (!defender.resources.isAlive) return false

        val baseConfiguredPct = when (threatCategory) {
            ShieldThreatCategory.SNAKE -> state.shieldSnakeDefensePercent
            ShieldThreatCategory.NEGATIVE_OBSTACLE -> state.shieldObstacleDefensePercent
            ShieldThreatCategory.EXECUTIONER_OR_ABILITY -> state.shieldHuntDefensePercent
        }

        val outcome = obstacleEngine.evaluateProtectionDefense(
            defender = defender,
            threatCategory = threatCategory,
            threatNamePersian = threatNamePersian,
            baseConfiguredPercent = baseConfiguredPct,
            deterministicRollOverride = deterministicRollOverride
        )

        if (outcome.bannerMessage == null) return false

        if (outcome.isBlocked) {
            soundManager.playAbilityActivateSound()
        } else {
            soundManager.playDiceTick(0)
        }

        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == defenderId) outcome.updatedDefender else p
            }
            st.copy(
                players = updated,
                highlightEffect = BoardHighlightEffect(
                    tile = threatTile,
                    isPositive = outcome.isBlocked,
                    intensity = 1f,
                    heroFxType = outcome.heroFxType.toUiFx()
                ),
                eventBanner = GameEventBanner(
                    message = outcome.bannerMessage,
                    type = outcome.bannerType
                )
            )
        }

        if (!skipDelay) {
            animDelay(if (outcome.isBlocked) 850L else 700L)
        }
        return outcome.isBlocked
    }

    /**
     * Resolves Hero Collision & Combat Interactions on [landedTile] via [combatResolutionEngine].
     * Enforces:
     * - Alive checks on both attacker and defenders.
     * - Teammate friendly-fire immunity via [teamManager].
     * - Atomic updates to BOTH attacker (Critical Parry damage & knockback) and defender.
     * - Re-checking attacker status between multi-defender clashes so displaced/dead attackers stop fighting.
     */
    private suspend fun resolveTileCombatAndInteraction(
        movingPlayerId: Int,
        landedTile: Int,
        forcedAttackRoll: Int? = null,
        forcedDefenseRoll: Int? = null,
        skipAnimationForTest: Boolean = false
    ): Boolean {
        val initialState = _uiState.value
        val maxTiles = initialState.maxTiles
        if (landedTile <= 1 || landedTile >= maxTiles) return false

        val initialAttacker = initialState.players.find { it.id == movingPlayerId } ?: return false
        if (!initialAttacker.resources.isAlive) return false

        val rivalIdsOnTile = initialState.players
            .filter { it.id != movingPlayerId && it.resources.isAlive && it.currentTile == landedTile }
            .map { it.id }
        if (rivalIdsOnTile.isEmpty()) return false

        var anyCombatHappened = false
        val effectiveSkipAnim = skipAnimationForTest || skipAnimationDelaysForTest

        for (defenderId in rivalIdsOnTile) {
            val latestState = _uiState.value
            val attacker = latestState.players.find { it.id == movingPlayerId } ?: break
            val defender = latestState.players.find { it.id == defenderId } ?: continue

            // Stop fighting if attacker was knocked off this tile or eliminated in a previous clash
            if (!attacker.resources.isAlive || attacker.currentTile != landedTile) break
            if (!defender.resources.isAlive || defender.currentTile != landedTile) continue

            val resolution = combatResolutionEngine.resolveClash(
                attacker = attacker,
                defender = defender,
                teamManager = teamManager,
                forcedAttackRoll = forcedAttackRoll,
                forcedDefenseRoll = forcedDefenseRoll,
                baseShieldProbability = latestState.shieldHuntDefensePercent,
                enforceCombatantClassFilter = true
            )

            if (resolution.isCombatNoOp && !resolution.isTeammatePeacefulEncounter) continue

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
                description = resolution.description,
                attackerTileBefore = landedTile,
                attackerTileAfter = resolution.attackerFinalTile,
                defenderTileBefore = landedTile,
                defenderTileAfter = resolution.defenderFinalTile,
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

            val clashEvent = com.example.model.HeroClashEvent(
                attackerId = attacker.id,
                attackerName = attacker.name,
                attackerCharacter = attacker.character,
                defenderId = defender.id,
                defenderName = defender.name,
                defenderCharacter = defender.character,
                abilityName = attacker.character.heroBlueprint.primaryAbility.persianName,
                tile = landedTile,
                outcomeType = resolution.outcomeType,
                defenseStatusDescription = when {
                    resolution.isTeammatePeacefulEncounter -> "هم‌تیمی (بدون درگیری) 🤝"
                    resolution.defenderAngelConsumed -> "سپر فرشتهٔ نجات 🌟"
                    resolution.defenderShieldConsumed || resolution.defenderCriticalParry -> "سپر دفاعی محافظ 🛡️"
                    resolution.defenderPhantomConsumed -> "فرار شبح‌وار 👻"
                    resolution.defenderDecoyConsumed -> "سراب توهمی 🪄"
                    defender.character.heroBlueprint.resistanceProfile.displacementResistancePercent > 0 -> "لنگر سنگین زرهی ⚓"
                    else -> "دفاع پایه (${resolution.defenseRoll.toPersianDigits()})"
                },
                resultMessage = resolution.description,
                attackRoll = resolution.attackRoll,
                defenseRoll = resolution.defenseRoll,
                knockbackSteps = (landedTile - resolution.defenderFinalTile).coerceAtLeast(0)
            )

            val defenderMovesInAnim = !effectiveSkipAnim && resolution.defenderFinalTile != landedTile
            val attackerMovesInAnim = !effectiveSkipAnim && resolution.attackerFinalTile != landedTile

            // Atomic state update for BOTH attacker & defender
            _uiState.update { st ->
                val updatedPlayers = st.players.map { p ->
                    when (p.id) {
                        attacker.id -> resolution.attacker.copy(
                            currentTile = if (attackerMovesInAnim) landedTile else resolution.attackerFinalTile
                        )
                        defender.id -> resolution.defender.copy(
                            currentTile = if (defenderMovesInAnim) landedTile else resolution.defenderFinalTile
                        )
                        else -> p
                    }
                }

                val updatedLogs = (st.combatLogs + logEntry).takeLast(60)
                val showModal = !effectiveSkipAnim && !resolution.isTeammatePeacefulEncounter

                st.copy(
                    players = updatedPlayers,
                    combatLogs = updatedLogs,
                    activeHeroClash = if (showModal) clashEvent else st.activeHeroClash,
                    isTokenAnimating = defenderMovesInAnim || attackerMovesInAnim,
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
                            CombatOutcomeType.ATTACK_HIT,
                            CombatOutcomeType.SHIELD_PARRY,
                            CombatOutcomeType.CRITICAL_PARRY -> BannerType.CHARACTER_ABILITY
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

            if (defenderMovesInAnim) {
                awaitComposeTokenMovement(
                    playerId = defender.id,
                    pathTiles = listOf(landedTile, resolution.defenderFinalTile),
                    movementType = TokenMovementType.EXECUTIONER_HUNT
                )
                updatePlayerTileInState(defender.id, resolution.defenderFinalTile)
            }
            if (attackerMovesInAnim) {
                awaitComposeTokenMovement(
                    playerId = attacker.id,
                    pathTiles = listOf(landedTile, resolution.attackerFinalTile),
                    movementType = TokenMovementType.EXECUTIONER_HUNT
                )
                updatePlayerTileInState(attacker.id, resolution.attackerFinalTile)
            }

            anyCombatHappened = true

            // If someone reached 0 HP from combat/critical parry damage, offer respawn decision cleanly before ending
            if (!resolution.attacker.resources.isAlive || !resolution.defender.resources.isAlive) {
                val pendingRespawn = syncEliminatedPlayersForRespawn()
                if (pendingRespawn || checkAndApplyMatchResolution()) break
            }
        }

        if (!effectiveSkipAnim) {
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
     * Activates the current player's character ability on their turn via [heroAbilityEngine]!
     */
    fun activateCharacterAbility() {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING ||
            state.isDiceRolling ||
            state.isTokenAnimating ||
            state.pendingPrecisionChoice != null ||
            state.activeRespawnPromptPlayerId != null
        ) return

        val currentPlayer = state.currentPlayer ?: return
        if (!currentPlayer.resources.isAlive || currentPlayer.id in state.finishedPlayerIds) return
        val ability = currentPlayer.character.abilityType

        // Pre-validate Raven's Leader Swap target BEFORE spending mana or window!
        var preValidatedLeader: Player? = null
        if (ability == CharacterAbilityType.SHADOW_WALKER) {
            preValidatedLeader = state.players
                .filter {
                    it.id != currentPlayer.id &&
                        it.resources.isAlive &&
                        !teamManager.areTeammates(currentPlayer.id, it.id) &&
                        it.currentTile > currentPlayer.currentTile
                }
                .maxByOrNull { it.currentTile }

            if (preValidatedLeader == null) {
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = "در حال حاضر هیچ رقیب زنده‌ای جلوتر از شما نیست! (فرصت جابه‌جایی حفظ شد)",
                            type = BannerType.OBSTACLE_DANGER
                        )
                    )
                }
                return
            }
        }

        val activationResult = heroAbilityEngine.activateAbility(currentPlayer)
        if (activationResult is AbilityActivationResult.Failure) {
            _uiState.update {
                it.copy(
                    eventBanner = GameEventBanner(
                        message = activationResult.reason,
                        type = BannerType.OBSTACLE_DANGER
                    )
                )
            }
            return
        }

        val activatedPlayer = (activationResult as AbilityActivationResult.Success).updatedPlayer
        soundManager.playAbilityActivateSound()

        viewModelScope.launch {
            val maxT = _uiState.value.maxTiles

            when (ability) {
                // 1. JACK — Guardian Shield Active Shockwave Pulse & Shield Reinforcement
                CharacterAbilityType.GUARDIAN_SHIELD -> {
                    val nearbyRivals = _uiState.value.players.filter {
                        it.id != currentPlayer.id &&
                            it.resources.isAlive &&
                            !teamManager.areTeammates(currentPlayer.id, it.id) &&
                            it.currentTile > 1 &&
                            abs(it.currentTile - currentPlayer.currentTile) <= 4
                    }
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> {
                                    val reinforcedRes = if (activatedPlayer.resources.shield < 15) {
                                        activatedPlayer.resources.addShield(15 - activatedPlayer.resources.shield)
                                    } else {
                                        activatedPlayer.resources
                                    }
                                    activatedPlayer.copy(
                                        isShieldCharged = true,
                                        isActiveAbilityArmedForTurn = false,
                                        resources = reinforcedRes
                                    )
                                }
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
                    animDelay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 2. AXEL — Short-Range Tactical Shadow Step (1..10 tiles) or Sonic Overdrive (4..6)
                CharacterAbilityType.SPEEDSTER -> {
                    val nearbyTarget = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id) &&
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
                                    activatedPlayer.copy(isActiveAbilityArmedForTurn = false)
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
                        checkAndApplyMatchResolution()
                    } else {
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    activatedPlayer.copy(isActiveAbilityArmedForTurn = true)
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
                                activatedPlayer.copy(isActiveAbilityArmedForTurn = true)
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
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id) &&
                                (it.currentTile - currentPlayer.currentTile) in 1..3 &&
                                it.currentTile < maxT
                        }
                        .maxByOrNull { it.currentTile }

                    val destTile = rivalInLungeRange?.currentTile
                        ?: (currentPlayer.currentTile + 2).coerceAtMost((maxT - 1).coerceAtLeast(1))

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(isActiveAbilityArmedForTurn = false)
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

                    // Immediately resolve hero clash if Blade lunged onto a rival!
                    resolveTileCombatAndInteraction(
                        movingPlayerId = currentPlayer.id,
                        landedTile = destTile
                    )

                    _uiState.update { it.copy(isTokenAnimating = false, highlightEffect = null) }
                    checkAndApplyMatchResolution()
                }

                // 5. RAVEN — 10-Personal-Turn Leader Swap (with 2-turn window & live target validation)
                CharacterAbilityType.SHADOW_WALKER -> {
                    val liveLeader = preValidatedLeader ?: return@launch
                    val myOldTile = currentPlayer.currentTile
                    val leaderOldTile = liveLeader.currentTile
                    val nextPersonalChargeTurn = activatedPlayer.leaderSwapNextChargeTurn

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
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
                    checkAndApplyMatchResolution()
                }

                // 6. KAI — Gravity Anchor Field (caps leader's next roll at 4 & pulls Kai +2 forward)
                CharacterAbilityType.HALF_REVERSE -> {
                    val targetLeader = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id)
                        }
                        .maxByOrNull { it.currentTile }
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost((maxT - 1).coerceAtLeast(1))

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> activatedPlayer.copy(isActiveAbilityArmedForTurn = false)
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
                    checkAndApplyMatchResolution()
                }

                // 7. IVY — Bio-Venom Antidote & Spore Throw
                CharacterAbilityType.WORM_IMMUNE -> {
                    val targetLeader = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id)
                        }
                        .maxByOrNull { it.currentTile }
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost((maxT - 1).coerceAtLeast(1))

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
                                    wormCurseTurnsRemaining = 0,
                                    swampSkipTurnsRemaining = 0,
                                    halveNextRoll = false,
                                    pinnedSlowSteps = 0,
                                    resources = activatedPlayer.resources.copy(statusEffects = emptyList())
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
                    checkAndApplyMatchResolution()
                }

                // 8. PHOENIX — Dragon Soul Empowerment (+2 bonus steps for 2 turns & fiery leap)
                CharacterAbilityType.PHOENIX_WINGS -> {
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost((maxT - 1).coerceAtLeast(1))
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
                                    dragonSoulBoostTurns = 2,
                                    wormCurseTurnsRemaining = 0,
                                    swampSkipTurnsRemaining = 0,
                                    halveNextRoll = false,
                                    resources = activatedPlayer.resources.copy(statusEffects = emptyList())
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
                    checkAndApplyMatchResolution()
                }

                // 9. STELLA — Guardian Angel Protection + Starlight Step
                CharacterAbilityType.FORTUNE_STAR -> {
                    val newTile = (currentPlayer.currentTile + 2).coerceAtMost((maxT - 1).coerceAtLeast(1))
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(
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
                    checkAndApplyMatchResolution()
                }

                // 10. ZEPHYR — Phantom Phase (walk through traps/snakes) & Headwind Gust on Leader
                CharacterAbilityType.WIND_RIDER -> {
                    val targetLeader = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id) &&
                                it.currentTile > 2 &&
                                it.character.abilityType != CharacterAbilityType.EARTH_TITAN &&
                                it.character.abilityType != CharacterAbilityType.HALF_REVERSE
                        }
                        .maxByOrNull { it.currentTile }

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> activatedPlayer.copy(
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
                    animDelay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 11. ORION — Trickster Debuff Transfer & Cosmic Transmutation
                CharacterAbilityType.ALCHEMIST -> {
                    val targetLeader = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id)
                        }
                        .maxByOrNull { it.currentTile }
                    val hadDebuffToTransfer = currentPlayer.hasActiveDebuff

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = true,
                                    wormCurseTurnsRemaining = 0,
                                    swampSkipTurnsRemaining = 0,
                                    halveNextRoll = false,
                                    gravityCapNextRoll = false,
                                    pinnedSlowSteps = 0,
                                    resources = activatedPlayer.resources.copy(statusEffects = emptyList())
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
                    animDelay(750L)
                    _uiState.update { it.copy(highlightEffect = null) }
                }

                // 12. SHADOW — Time Keeper (Rewinds previous turn setback OR rewinds leader by 2 tiles + chrono boost)
                CharacterAbilityType.TIME_WEAVER -> {
                    val sufferedSetbackLastTurn = currentPlayer.previousTurnTile > currentPlayer.currentTile
                    if (sufferedSetbackLastTurn) {
                        val rewindDest = currentPlayer.previousTurnTile.coerceAtMost((maxT - 1).coerceAtLeast(1))
                        val startPos = currentPlayer.currentTile
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    activatedPlayer.copy(
                                        isActiveAbilityArmedForTurn = false,
                                        wormCurseTurnsRemaining = 0,
                                        swampSkipTurnsRemaining = 0,
                                        halveNextRoll = false,
                                        gravityCapNextRoll = false,
                                        pinnedSlowSteps = 0,
                                        resources = activatedPlayer.resources.copy(statusEffects = emptyList())
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
                        checkAndApplyMatchResolution()
                    } else {
                        val targetLeader = _uiState.value.players
                            .filter {
                                it.id != currentPlayer.id &&
                                    it.resources.isAlive &&
                                    !teamManager.areTeammates(currentPlayer.id, it.id) &&
                                    it.currentTile > 2
                            }
                            .maxByOrNull { it.currentTile }
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                when {
                                    p.id == currentPlayer.id -> activatedPlayer.copy(
                                        isActiveAbilityArmedForTurn = true,
                                        wormCurseTurnsRemaining = 0,
                                        swampSkipTurnsRemaining = 0,
                                        halveNextRoll = false,
                                        gravityCapNextRoll = false,
                                        pinnedSlowSteps = 0,
                                        resources = activatedPlayer.resources.copy(statusEffects = emptyList())
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
                        animDelay(750L)
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
                        (currentPlayer.currentTile + halfClimb).coerceAtMost((maxT - 1).coerceAtLeast(1))
                    } else {
                        (currentPlayer.currentTile + 3).coerceAtMost((maxT - 1).coerceAtLeast(1))
                    }

                    _uiState.update { st ->
                        val updatedPlayers = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
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
                    checkAndApplyMatchResolution()
                }

                // 14. STORM — Chain Lightning Cataclysm
                CharacterAbilityType.STORM_LORD -> {
                    soundManager.playObstacleSound(ObstacleType.STORM)
                    val myOldTile = currentPlayer.currentTile
                    val myNewTile = (myOldTile + 2).coerceAtMost((maxT - 1).coerceAtLeast(1))

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
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
                                !p.resources.isAlive -> p
                                teamManager.areTeammates(currentPlayer.id, p.id) -> p
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
                    checkAndApplyMatchResolution()
                }

                // 15. AMBER — Glacial Prison
                CharacterAbilityType.FROST_KNIGHT -> {
                    val targetOpponent = _uiState.value.players
                        .filter {
                            it.id != currentPlayer.id &&
                                it.resources.isAlive &&
                                !teamManager.areTeammates(currentPlayer.id, it.id)
                        }
                        .maxByOrNull { it.currentTile }

                    val boostTile = (currentPlayer.currentTile + 1).coerceAtMost((maxT - 1).coerceAtLeast(1))

                    if (targetOpponent == null) {
                        _uiState.update { st ->
                            val updated = st.players.map { p ->
                                if (p.id == currentPlayer.id) {
                                    activatedPlayer.copy(
                                        isActiveAbilityArmedForTurn = false,
                                        hasUsedOnceAbility = true,
                                        currentTile = boostTile
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
                        checkAndApplyMatchResolution()
                        return@launch
                    }

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == currentPlayer.id) {
                                activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
                                    hasUsedOnceAbility = true,
                                    currentTile = boostTile
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

                    val isHighThermalOrResist = isHighThermalResist(targetOpponent)

                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            if (p.id == targetOpponent.id) {
                                if (isHighThermalOrResist) {
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
                    animDelay(850L)
                    _uiState.update { it.copy(highlightEffect = null) }
                    checkAndApplyMatchResolution()
                }

                // 16. ATLAS — Berserker Fury & Seismic Tremor
                CharacterAbilityType.EARTH_TITAN -> {
                    val myRow = (currentPlayer.currentTile - 1) / 10
                    val affectedRivals = _uiState.value.players.filter {
                        it.id != currentPlayer.id &&
                            it.resources.isAlive &&
                            !teamManager.areTeammates(currentPlayer.id, it.id) &&
                            it.currentTile > 2 &&
                            (((it.currentTile - 1) / 10 == myRow) || abs(it.currentTile - currentPlayer.currentTile) <= 5)
                    }
                    _uiState.update { st ->
                        val updated = st.players.map { p ->
                            when {
                                p.id == currentPlayer.id -> activatedPlayer.copy(
                                    isActiveAbilityArmedForTurn = false,
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
                    animDelay(750L)
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
        // If doubles or sum >= 8, Nova's Pinning Arrow slows the leading rival by 2 steps on their next roll!
        val triggersPinningArrow = (choice.die1 == choice.die2 || choice.sum >= 8)
        val leaderTarget = state.players
            .filter {
                it.id != currentPlayer.id &&
                    it.resources.isAlive &&
                    !teamManager.areTeammates(currentPlayer.id, it.id) &&
                    it.currentTile > currentPlayer.currentTile
            }
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
            state.pendingPrecisionChoice != null ||
            state.activeRespawnPromptPlayerId != null
        ) return
        val currentPlayer = state.currentPlayer ?: return

        viewModelScope.launch {
            // Eliminated or already-finished players can never roll; immediately pass turn safely
            if (!currentPlayer.resources.isAlive || currentPlayer.id in state.finishedPlayerIds) {
                advanceTurnOrGrantBonus(
                    rolledSixOrFortuneFive = false,
                    luckExtraTurn = false
                )
                return@launch
            }

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
                animDelay(900L)
                advanceTurnOrGrantBonus(
                    rolledSixOrFortuneFive = false,
                    luckExtraTurn = false
                )
                return@launch
            }

            val ability = currentPlayer.character.abilityType
            val customSpec = currentPlayer.character.customSpec
            val wasAbilityArmed = currentPlayer.isActiveAbilityArmedForTurn
            val isDualRoll = (ability == CharacterAbilityType.HIGH_PRECISION && wasAbilityArmed)

            // Pre-compute deterministic/RNG dice outcomes via RandomProvider
            val hadWormCurse = currentPlayer.wormCurseTurnsRemaining > 0 &&
                ability != CharacterAbilityType.WORM_IMMUNE &&
                !(customSpec?.target == CustomAbilityTarget.SPECIAL_OBSTACLES &&
                    randomProvider.nextInt(0, 100) < customSpec.percentageOrValue)

            val rawComputedDie1 = if (isDualRoll) {
                randomProvider.nextInt(1, 7)
            } else {
                when {
                    ability == CharacterAbilityType.SPEEDSTER && wasAbilityArmed -> {
                        randomProvider.nextInt(4, 7) // Guaranteed 4..6 on Sonic Overdrive!
                    }
                    hadWormCurse -> {
                        if (randomProvider.nextDouble() < 0.75) randomProvider.nextInt(1, 3) else randomProvider.nextInt(1, 7)
                    }
                    ability == CharacterAbilityType.SPEEDSTER -> {
                        if (randomProvider.nextDouble() < 0.42) randomProvider.nextInt(4, 7) else randomProvider.nextInt(1, 7)
                    }
                    customSpec?.target == CustomAbilityTarget.DICE_AND_LUCK -> {
                        if (randomProvider.nextInt(0, 100) < customSpec.percentageOrValue) {
                            randomProvider.nextInt(4, 7)
                        } else {
                            randomProvider.nextInt(1, 7)
                        }
                    }
                    else -> randomProvider.nextInt(1, 7)
                }
            }
            val preRolledDie1 = if (currentPlayer.gravityCapNextRoll) {
                rawComputedDie1.coerceAtMost(4)
            } else {
                rawComputedDie1
            }
            val preRolledDie2 = if (isDualRoll) randomProvider.nextInt(1, 7) else preRolledDie1
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

            if (!skipAnimationDelaysForTest) {
                val totalSoundTicks = 9
                var elapsedMs = 0L
                for (step in 0 until totalSoundTicks) {
                    soundManager.playDiceTick(step)
                    val progress = (step + 1).toFloat() / totalSoundTicks.toFloat()
                    val targetElapsed = (rollDurationMs * (0.12f + 0.88f * progress * progress)).toLong()
                    val waitMs = (targetElapsed - elapsedMs).coerceAtLeast(28L)
                    animDelay(waitMs)
                    elapsedMs += waitMs
                }
                val remainingRollWait = (rollDurationMs - elapsedMs).coerceAtLeast(0L)
                if (remainingRollWait > 0L) {
                    animDelay(remainingRollWait)
                }
            }

            // Advance this player's personal turn counter & Raven's 10-personal-turn cycle
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
                    animDelay(750L)
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
                randomProvider.nextInt(0, 100) < customSpec.percentageOrValue
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

            val canStillEarnConsecutiveBonus =
                state.consecutiveSixesCount < turnMovementEngine.maxConsecutiveSixes
            val isFortuneFiveEligible = ability == CharacterAbilityType.FORTUNE_STAR &&
                rawRoll == 5 &&
                (currentPlayer.abilityChargeProgress >= 3 || wasAbilityArmed) &&
                !hadSwampHalve
            val fortuneFiveBonus = isFortuneFiveEligible && canStillEarnConsecutiveBonus
            val rolledSixEligible = (rawRoll == 6 && !hadSwampHalve)
            val earnsBonusTurnFromRoll = (rolledSixEligible || fortuneFiveBonus) && canStillEarnConsecutiveBonus
            val reachedBonusStreakCap = (rolledSixEligible || isFortuneFiveEligible) && !canStillEarnConsecutiveBonus

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
                animDelay(520L)
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
                animDelay(680L)
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
                animDelay(520L)
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
                animDelay(450L)
            } else if (rawRoll == 6 && earnsBonusTurnFromRoll) {
                soundManager.playSixBonusChime()
                _uiState.update {
                    it.copy(
                        eventBanner = GameEventBanner(
                            message = "شیش طلایی برای ${currentPlayer.name}! ✨ یک نوبت جایزه هم داری!",
                            type = BannerType.SIX_BONUS
                        )
                    )
                }
                animDelay(380L)
            }

            processPlayerMove(
                playerId = currentPlayer.id,
                rolledValue = if (earnsBonusTurnFromRoll) 6 else rawRoll,
                effectiveSteps = effectiveSteps,
                canEarnSixBonus = earnsBonusTurnFromRoll || reachedBonusStreakCap,
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
        syncEnginesForMaxTiles(maxTiles)

        val player = stateBeforeMove.players.find { it.id == playerId } ?: return
        if (!player.resources.isAlive) {
            advanceTurnOrGrantBonus(rolledSixOrFortuneFive = false, luckExtraTurn = false)
            return
        }

        val startTile = player.currentTile
        val movement = turnMovementEngine.calculateMovement(
            currentTile = startTile,
            diceRoll = effectiveSteps
        )

        if (movement.isExactRollNeededFailure) {
            soundManager.playDiceTick(0)
            _uiState.update {
                it.copy(
                    eventBanner = GameEventBanner(
                        message = "${player.name} عدد ${effectiveSteps.toPersianDigits()} آورد، اما برای رسیدن به خانه ${maxTiles.toPersianDigits()} دقیقاً ${movement.neededRollForFinish.toPersianDigits()} لازم است!",
                        type = BannerType.EXACT_NEEDED
                    )
                )
            }
            animDelay(950L)
            advanceTurnOrGrantBonus(
                rolledSixOrFortuneFive = canEarnSixBonus,
                luckExtraTurn = false
            )
            return
        }

        val targetTile = movement.targetTile
        val stepPathTiles = if (movement.stepsPath.firstOrNull() == startTile) {
            movement.stepsPath
        } else {
            listOf(startTile) + movement.stepsPath
        }
        awaitComposeTokenMovement(
            playerId = playerId,
            pathTiles = stepPathTiles,
            movementType = TokenMovementType.STEP_BY_STEP
        )
        updatePlayerTileInState(playerId, targetTile)

        // If Axel's Sonic Overdrive was armed, knock back alive enemy rivals passed along the path by 2 tiles
        if (wasSonicOverdriveArmed) {
            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id != playerId &&
                        p.resources.isAlive &&
                        !teamManager.areTeammates(playerId, p.id) &&
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

        var grantLuckExtraTurn = false

        // Deterministic Landing Chain:
        // 1. Special Obstacle -> Check Respawn & Match Resolution
        val matchedObstacle = _uiState.value.boardObstacles.find { it.tile == targetTile }
        if (matchedObstacle != null) {
            animDelay(220L)
            grantLuckExtraTurn = handleObstacleLanding(
                playerId = playerId,
                obstacle = matchedObstacle,
                rolledSteps = effectiveSteps
            )
            if (checkAndApplyMatchResolution()) return
        }

        // 2. Snake or Ladder -> Check Respawn & Match Resolution
        val tileAfterObstacle = _uiState.value.players.find { it.id == playerId }?.currentTile ?: targetTile
        val matchedLink = _uiState.value.boardLinks.find { it.startTile == tileAfterObstacle }
        if (matchedLink != null) {
            animDelay(220L)
            val currentPlayerObj = _uiState.value.players.find { it.id == playerId } ?: player
            when (matchedLink.type) {
                LinkType.LADDER -> executeLadderClimb(currentPlayerObj, matchedLink)
                LinkType.SNAKE -> executeSnakeSlide(currentPlayerObj, matchedLink)
            }
            if (checkAndApplyMatchResolution()) return
        }

        // 3. Hero Collision / Combat on final landed tile
        val finalTile = _uiState.value.players.find { it.id == playerId }?.currentTile ?: tileAfterObstacle
        resolveTileCombatAndInteraction(
            movingPlayerId = playerId,
            landedTile = finalTile
        )

        // Pause turn progression while an eliminated human player decides whether to respawn from tile 0
        if (syncEliminatedPlayersForRespawn() || hasPendingRespawnDecision()) {
            hasDeferredPostMoveTurn = true
            deferredPostMoveSixBonus = canEarnSixBonus
            deferredPostMoveLuckExtra = grantLuckExtraTurn
            _uiState.update { it.copy(isTokenAnimating = false) }
            return
        }

        // 4. Match Resolution check (tile winner / team winner / elimination / draw)
        if (checkAndApplyMatchResolution()) return

        animDelay(300L)
        advanceTurnOrGrantBonus(
            rolledSixOrFortuneFive = canEarnSixBonus,
            luckExtraTurn = grantLuckExtraTurn
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

        val outcome = obstacleEngine.resolveSpecialObstacle(
            player = player,
            obstacle = obstacle,
            rolledSteps = rolledSteps,
            difficulty = state.difficulty,
            maxTiles = maxTiles,
            baseShieldProbability = state.shieldObstacleDefensePercent
        )

        if (outcome.playAbilitySound) soundManager.playAbilityActivateSound()
        if (outcome.playSixBonusChime) soundManager.playSixBonusChime()
        outcome.playObstacleSound?.let { soundManager.playObstacleSound(it) }

        if (outcome.requiresStormGlobalShift) {
            val stormShifts = obstacleEngine.resolveStormShifts(_uiState.value.players, maxTiles)
            val activeStart = player.currentTile
            val activeEnd = stormShifts[playerId] ?: activeStart

            _uiState.update { st ->
                val updated = st.players.map { p ->
                    if (p.id == playerId) outcome.updatedPlayer else p
                }
                st.copy(
                    players = updated,
                    stormFlashActive = true,
                    highlightEffect = BoardHighlightEffect(
                        tile = obstacle.tile,
                        isPositive = false,
                        intensity = 1f,
                        obstacleType = obstacle.type
                    ),
                    eventBanner = GameEventBanner(
                        message = outcome.bannerMessage,
                        type = outcome.bannerType
                    )
                )
            }

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
                    stormFlashActive = false,
                    highlightEffect = null
                )
            }
            return outcome.grantExtraTurn
        }

        val hasMovementAnim = !skipAnimationDelaysForTest &&
            outcome.movementPath.isNotEmpty() &&
            outcome.movementType != null &&
            outcome.newTile != obstacle.tile

        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) {
                    if (hasMovementAnim) {
                        outcome.updatedPlayer.copy(currentTile = obstacle.tile)
                    } else {
                        outcome.updatedPlayer
                    }
                } else p
            }
            st.copy(
                players = updated,
                highlightEffect = BoardHighlightEffect(
                    tile = obstacle.tile,
                    isPositive = outcome.isPositiveHighlight,
                    intensity = 1f,
                    obstacleType = obstacle.type,
                    heroFxType = outcome.heroFxType.toUiFx()
                ),
                eventBanner = GameEventBanner(
                    message = outcome.bannerMessage,
                    type = outcome.bannerType
                )
            )
        }

        if (hasMovementAnim) {
            awaitComposeTokenMovement(
                playerId = playerId,
                pathTiles = outcome.movementPath,
                movementType = outcome.movementType!!.toUiMotion()
            )
            _uiState.update { st ->
                st.copy(
                    players = st.players.map { p ->
                        if (p.id == playerId) outcome.updatedPlayer else p
                    }
                )
            }
        } else {
            animDelay(800L)
        }

        _uiState.update { it.copy(highlightEffect = null) }
        return outcome.grantExtraTurn
    }

    private suspend fun executeLadderClimb(player: Player, ladder: BoardLink) {
        val maxTiles = _uiState.value.maxTiles
        soundManager.playLadderClimb()

        val outcome = obstacleEngine.resolveLadderClimb(
            player = player,
            ladder = ladder,
            maxTiles = maxTiles
        )

        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == player.id) outcome.updatedPlayer.copy(currentTile = ladder.startTile) else p
            }
            st.copy(
                players = updated,
                eventBanner = GameEventBanner(
                    message = outcome.bannerMessage,
                    type = BannerType.LADDER_CLIMB
                ),
                highlightEffect = BoardHighlightEffect(
                    tile = outcome.finalTile,
                    isPositive = true,
                    intensity = 1f
                )
            )
        }

        awaitComposeTokenMovement(
            playerId = player.id,
            pathTiles = listOf(ladder.startTile, outcome.finalTile),
            movementType = TokenMovementType.LADDER_CLIMB
        )

        _uiState.update { st ->
            st.copy(
                players = st.players.map { p ->
                    if (p.id == player.id) outcome.updatedPlayer else p
                },
                highlightEffect = null
            )
        }
    }

    private suspend fun executeSnakeSlide(player: Player, snake: BoardLink) {
        val currentState = _uiState.value
        val outcome = obstacleEngine.resolveSnakeSlide(
            player = player,
            snake = snake,
            boardLinks = currentState.boardLinks,
            maxTiles = currentState.maxTiles,
            baseShieldProbability = currentState.shieldSnakeDefensePercent
        )

        if (outcome.blockedOrSlain) {
            if (outcome.playAbilitySound) soundManager.playAbilityActivateSound()

            val animateBoost = !skipAnimationDelaysForTest &&
                outcome.boostedForward &&
                outcome.finalTile != snake.startTile

            _uiState.update { st ->
                val updatedPlayers = st.players.map { p ->
                    if (p.id == player.id) {
                        if (animateBoost) outcome.updatedPlayer.copy(currentTile = snake.startTile)
                        else outcome.updatedPlayer
                    } else p
                }
                st.copy(
                    players = updatedPlayers,
                    boardLinks = outcome.updatedLinks,
                    highlightEffect = BoardHighlightEffect(
                        tile = outcome.finalTile,
                        isPositive = outcome.isPositiveHighlight,
                        intensity = 1f,
                        heroFxType = outcome.heroFxType.toUiFx()
                    ),
                    eventBanner = GameEventBanner(
                        message = outcome.bannerMessage,
                        type = outcome.bannerType
                    )
                )
            }

            if (animateBoost) {
                awaitComposeTokenMovement(
                    playerId = player.id,
                    pathTiles = listOf(snake.startTile, outcome.finalTile),
                    movementType = TokenMovementType.LUCK_BOOST
                )
                _uiState.update { st ->
                    st.copy(
                        players = st.players.map { p ->
                            if (p.id == player.id) outcome.updatedPlayer else p
                        }
                    )
                }
            } else {
                animDelay(900L)
            }

            _uiState.update { it.copy(highlightEffect = null) }
            return
        }

        // Draw a Truth or Dare card when a human player lands on an unblocked Snake (skipped in headless tests)
        val eligibleCards = currentState.truthOrDareCards.filter {
            it.category in currentState.enabledCardCategories
        }.ifEmpty {
            currentState.truthOrDareCards
        }

        if (!skipAnimationDelaysForTest && eligibleCards.isNotEmpty() && !player.isAiBot) {
            val drawnCard = eligibleCards[randomProvider.nextInt(0, eligibleCards.size)]
            val cardDeferred = CompletableDeferred<Unit>()
            pendingSnakeCardCompletion = cardDeferred

            soundManager.playCardDrawSuspenseSound()
            _uiState.update { st ->
                st.copy(
                    activeSnakeCardDraw = ActiveSnakeCardDraw(
                        player = player,
                        snakeStartTile = snake.startTile,
                        snakeEndTile = outcome.finalTile,
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
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == player.id) outcome.updatedPlayer.copy(currentTile = snake.startTile) else p
            }
            st.copy(
                players = updated,
                eventBanner = GameEventBanner(
                    message = outcome.bannerMessage,
                    type = outcome.bannerType
                ),
                highlightEffect = BoardHighlightEffect(
                    tile = snake.startTile,
                    isPositive = false,
                    intensity = 1f,
                    heroFxType = outcome.heroFxType.toUiFx()
                )
            )
        }

        awaitComposeTokenMovement(
            playerId = player.id,
            pathTiles = listOf(snake.startTile, outcome.finalTile),
            movementType = TokenMovementType.SNAKE_SLIDE
        )

        _uiState.update { st ->
            st.copy(
                players = st.players.map { p ->
                    if (p.id == player.id) outcome.updatedPlayer else p
                },
                highlightEffect = null
            )
        }
    }

    private suspend fun awaitComposeTokenMovement(
        playerId: Int,
        pathTiles: List<Int>,
        movementType: TokenMovementType
    ) {
        if (skipAnimationDelaysForTest) {
            pathTiles.lastOrNull()?.let { dest ->
                updatePlayerTileInState(playerId, dest)
            }
            _uiState.update { it.copy(activeMovementPath = null) }
            return
        }

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

    private fun hasPendingRespawnDecision(): Boolean {
        val st = _uiState.value
        return st.activeRespawnPromptPlayerId != null || st.pendingRespawnPlayerIds.isNotEmpty()
    }

    /**
     * Scans for newly defeated players (`!isAlive`) who have not yet been offered respawn
     * or permanently marked eliminated. Human players enter the respawn decision queue
     * (shown once per elimination event), while dead AI bots are finalized as eliminated.
     *
     * @return true if any respawn decision prompt is active or queued.
     */
    private fun syncEliminatedPlayersForRespawn(): Boolean {
        var isWaitingOnRespawn = false
        _uiState.update { st ->
            if (st.phase != GamePhase.PLAYING || st.players.isEmpty()) {
                isWaitingOnRespawn = st.activeRespawnPromptPlayerId != null
                return@update st
            }

            val deadAiIds = st.players
                .filter {
                    !it.resources.isAlive &&
                        it.isAiBot &&
                        it.id !in st.eliminatedOutPlayerIds
                }
                .map { it.id }

            val newlyDeadHumanIds = st.players
                .filter {
                    !it.resources.isAlive &&
                        !it.isAiBot &&
                        it.id !in st.eliminatedOutPlayerIds &&
                        it.id !in st.respawnOfferedPlayerIds &&
                        it.id !in st.pendingRespawnPlayerIds &&
                        it.id != st.activeRespawnPromptPlayerId
                }
                .map { it.id }

            if (deadAiIds.isEmpty() && newlyDeadHumanIds.isEmpty()) {
                isWaitingOnRespawn = st.activeRespawnPromptPlayerId != null || st.pendingRespawnPlayerIds.isNotEmpty()
                return@update st
            }

            val combinedQueue = (st.pendingRespawnPlayerIds + newlyDeadHumanIds).distinct()
            val nextActivePrompt = st.activeRespawnPromptPlayerId ?: combinedQueue.firstOrNull()
            val remainingQueue = if (st.activeRespawnPromptPlayerId == null && nextActivePrompt != null) {
                combinedQueue.drop(1)
            } else {
                combinedQueue
            }

            isWaitingOnRespawn = nextActivePrompt != null || remainingQueue.isNotEmpty()

            st.copy(
                eliminatedOutPlayerIds = st.eliminatedOutPlayerIds + deadAiIds,
                respawnOfferedPlayerIds = st.respawnOfferedPlayerIds + newlyDeadHumanIds,
                activeRespawnPromptPlayerId = nextActivePrompt,
                pendingRespawnPlayerIds = remainingQueue
            )
        }
        return isWaitingOnRespawn
    }

    /**
     * Option 1 for a defeated player: Respawn at tile 0 with fresh base resources and cleared
     * negative statuses, while preserving selected hero, team, pawn skin, and match statistics.
     */
    fun chooseRespawnFromZero(playerId: Int) {
        val st = _uiState.value
        val isExpectedPlayer = st.activeRespawnPromptPlayerId == playerId || playerId in st.pendingRespawnPlayerIds
        if (!isExpectedPlayer) return

        soundManager.playAbilityActivateSound()
        var nextActiveAfterUpdate: Int? = null
        var respawnedName = ""

        _uiState.update { state ->
            val updatedPlayers = state.players.map { p ->
                if (p.id == playerId) {
                    respawnedName = p.name
                    p.copy(
                        currentTile = 0,
                        previousTurnTile = 0,
                        resources = PlayerResources(),
                        wormCurseTurnsRemaining = 0,
                        swampSkipTurnsRemaining = 0,
                        halveNextRoll = false,
                        frozenSkipTurnsRemaining = 0,
                        gravityCapNextRoll = false,
                        pinnedSlowSteps = 0,
                        isActiveAbilityArmedForTurn = false
                    )
                } else {
                    p
                }
            }
            val remainingQueue = state.pendingRespawnPlayerIds.filter { it != playerId }
            val nextPrompt = if (state.activeRespawnPromptPlayerId == playerId) {
                remainingQueue.firstOrNull()
            } else {
                state.activeRespawnPromptPlayerId
            }
            val finalQueue = if (state.activeRespawnPromptPlayerId == playerId && nextPrompt != null) {
                remainingQueue.drop(1)
            } else {
                remainingQueue
            }
            nextActiveAfterUpdate = nextPrompt

            state.copy(
                players = updatedPlayers,
                activeRespawnPromptPlayerId = nextPrompt,
                pendingRespawnPlayerIds = finalQueue,
                respawnOfferedPlayerIds = state.respawnOfferedPlayerIds - playerId,
                eliminatedOutPlayerIds = state.eliminatedOutPlayerIds - playerId,
                isTokenAnimating = false,
                eventBanner = GameEventBanner(
                    message = "🔄 $respawnedName به بازی بازگشت و از خانهٔ صفر ادامه می‌دهد!",
                    type = BannerType.CHARACTER_ABILITY
                )
            )
        }

        if (nextActiveAfterUpdate == null) {
            finalizeAfterRespawnQueue(decidedPlayerId = playerId, wasEliminatedOut = false)
        }
    }

    /**
     * Option 2 for a defeated player: Accept permanent elimination for this match and continue
     * without this player (never triggering the respawn dialog again for this elimination).
     */
    fun acceptElimination(playerId: Int) {
        val st = _uiState.value
        val isExpectedPlayer = st.activeRespawnPromptPlayerId == playerId || playerId in st.pendingRespawnPlayerIds
        if (!isExpectedPlayer) return

        soundManager.playDiceTick(0)
        var nextActiveAfterUpdate: Int? = null
        var eliminatedName = ""

        _uiState.update { state ->
            eliminatedName = state.players.find { it.id == playerId }?.name.orEmpty()
            val remainingQueue = state.pendingRespawnPlayerIds.filter { it != playerId }
            val nextPrompt = if (state.activeRespawnPromptPlayerId == playerId) {
                remainingQueue.firstOrNull()
            } else {
                state.activeRespawnPromptPlayerId
            }
            val finalQueue = if (state.activeRespawnPromptPlayerId == playerId && nextPrompt != null) {
                remainingQueue.drop(1)
            } else {
                remainingQueue
            }
            nextActiveAfterUpdate = nextPrompt

            state.copy(
                activeRespawnPromptPlayerId = nextPrompt,
                pendingRespawnPlayerIds = finalQueue,
                eliminatedOutPlayerIds = state.eliminatedOutPlayerIds + playerId,
                isTokenAnimating = false,
                eventBanner = GameEventBanner(
                    message = "💀 $eliminatedName حذف خود را پذیرفت و از دور رقابت کنار رفت.",
                    type = BannerType.OBSTACLE_DANGER
                )
            )
        }

        if (nextActiveAfterUpdate == null) {
            finalizeAfterRespawnQueue(decidedPlayerId = playerId, wasEliminatedOut = true)
        }
    }

    private fun finalizeAfterRespawnQueue(decidedPlayerId: Int, wasEliminatedOut: Boolean) {
        if (checkAndApplyMatchResolution()) {
            hasDeferredPostMoveTurn = false
            deferredPostMoveSixBonus = false
            deferredPostMoveLuckExtra = false
            return
        }

        if (hasDeferredPostMoveTurn) {
            val sixBonus = deferredPostMoveSixBonus
            val luckExtra = deferredPostMoveLuckExtra
            hasDeferredPostMoveTurn = false
            deferredPostMoveSixBonus = false
            deferredPostMoveLuckExtra = false
            viewModelScope.launch {
                advanceTurnOrGrantBonus(
                    rolledSixOrFortuneFive = sixBonus,
                    luckExtraTurn = luckExtra
                )
            }
            return
        }

        val currentState = _uiState.value
        if (wasEliminatedOut && currentState.currentPlayer?.id == decidedPlayerId) {
            viewModelScope.launch {
                advanceTurnOrGrantBonus(
                    rolledSixOrFortuneFive = false,
                    luckExtraTurn = false
                )
            }
        }
    }

    /**
     * Option 1 after victory: finalize the match result immediately on the celebration screen.
     */
    fun finalizeVictoryResult() {
        soundManager.playVictoryFanfare()
        _uiState.update { st ->
            val finalWinner = st.recordedWinner ?: st.winner
            val finalTeam = st.recordedWinningTeam ?: st.winningTeam
            val finalWinningPlayers = st.recordedWinningPlayers.ifEmpty { st.winningPlayers }
            val finalReason = st.recordedWinReason ?: st.matchWinReason
            val finalRankings = if (st.players.isNotEmpty()) {
                matchResolutionEngine.sortRankings(
                    players = st.players,
                    preferredWinnerId = finalWinner?.id,
                    winningTeam = finalTeam,
                    finishOrderPlayerIds = st.finishOrderPlayerIds,
                    eliminatedOutPlayerIds = st.eliminatedOutPlayerIds
                )
            } else {
                st.matchRankings
            }
            st.copy(
                phase = GamePhase.CELEBRATION,
                isContinuingAfterVictory = false,
                hasFinalizedVictory = true,
                winner = finalWinner,
                winningTeam = finalTeam,
                winningPlayers = finalWinningPlayers,
                matchWinReason = finalReason,
                matchRankings = finalRankings,
                eventBanner = GameEventBanner(
                    message = "نتیجهٔ نهایی مسابقه ثبت شد! 🏆",
                    type = BannerType.SIX_BONUS
                )
            )
        }
        recordCompletedMatchOnce(_uiState.value)
    }

    /**
     * Option 2 after victory: continue the match so remaining players/teams compete for next ranks,
     * while preserving the recorded winner and excluding winning players from normal turns.
     */
    fun continueMatchAfterVictory() {
        val state = _uiState.value
        if (!state.canContinueAfterVictory) return

        val wonIds = (state.winningPlayers.map { it.id } + listOfNotNull(state.winner?.id)).toSet()
        val updatedFinishedIds = state.finishedPlayerIds + wonIds
        val updatedFinishOrder = (state.finishOrderPlayerIds +
            listOfNotNull(state.winner?.id) +
            state.winningPlayers.map { it.id }).distinct()

        syncEnginesForMaxTiles(state.maxTiles)
        val turnSelection = turnMovementEngine.resolveNextPlayableTurn(
            currentIndex = state.currentTurnIndex,
            players = state.players,
            finishedPlayerIds = updatedFinishedIds
        )

        if (turnSelection.noAlivePlayers) {
            finalizeVictoryResult()
            return
        }

        soundManager.setVictoryMusicOverride(false)
        soundManager.playTurnChangeChime()

        val nextPlayer = state.players[turnSelection.nextIndex]
        _uiState.update { st ->
            st.copy(
                phase = GamePhase.PLAYING,
                isContinuingAfterVictory = true,
                hasFinalizedVictory = false,
                recordedWinner = st.recordedWinner ?: st.winner,
                recordedWinningTeam = st.recordedWinningTeam ?: st.winningTeam,
                recordedWinningPlayers = st.recordedWinningPlayers.ifEmpty { st.winningPlayers },
                recordedWinReason = st.recordedWinReason ?: st.matchWinReason,
                finishedPlayerIds = updatedFinishedIds,
                finishOrderPlayerIds = updatedFinishOrder,
                currentTurnIndex = turnSelection.nextIndex,
                consecutiveSixesCount = 0,
                isDiceRolling = false,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                eventBanner = GameEventBanner(
                    message = "🏁 ادامهٔ مسابقه برای رتبه‌های بعدی! برنده ثبت شد؛ نوبت ${nextPlayer.name} است 🎲",
                    type = BannerType.NORMAL
                )
            )
        }

        triggerAiTurnIfNeeded()
    }

    /**
     * Ends an ongoing post-victory continuation early and displays the full final rankings
     * while keeping the original recorded winner / winning team intact.
     */
    fun endContinuedMatch() {
        val state = _uiState.value
        if (!state.isContinuingAfterVictory) return

        aiTurnJob?.cancel()
        pendingMovementCompletion?.complete(Unit)
        pendingSnakeCardCompletion?.complete(Unit)
        soundManager.setVictoryMusicOverride(true)
        soundManager.playVictoryFanfare()

        val finalWinner = state.recordedWinner ?: state.winner
        val finalTeam = state.recordedWinningTeam ?: state.winningTeam
        val finalWinningPlayers = state.recordedWinningPlayers.ifEmpty { state.winningPlayers }
        val finalReason = state.recordedWinReason ?: state.matchWinReason
        val finalRankings = matchResolutionEngine.sortRankings(
            players = state.players,
            preferredWinnerId = finalWinner?.id,
            winningTeam = finalTeam,
            finishOrderPlayerIds = state.finishOrderPlayerIds,
            eliminatedOutPlayerIds = state.eliminatedOutPlayerIds
        )

        _uiState.update { st ->
            st.copy(
                phase = GamePhase.CELEBRATION,
                isDiceRolling = false,
                isTokenAnimating = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                isContinuingAfterVictory = false,
                hasFinalizedVictory = true,
                winner = finalWinner,
                winningTeam = finalTeam,
                winningPlayers = finalWinningPlayers,
                matchWinReason = finalReason,
                matchRankings = finalRankings,
                eventBanner = GameEventBanner(
                    message = "مسابقه به پایان رسید و رتبه‌بندی نهایی ثبت شد! 🏆",
                    type = BannerType.SIX_BONUS
                )
            )
        }
        recordCompletedMatchOnce(_uiState.value)
    }

    private fun checkAndApplyMatchResolution(isEliminationEnabled: Boolean = true): Boolean {
        val state = _uiState.value
        if (state.phase == GamePhase.CELEBRATION || state.players.isEmpty()) {
            return state.phase == GamePhase.CELEBRATION
        }

        // Do not prematurely declare match end while an eliminated human player is deciding on respawn
        if (syncEliminatedPlayersForRespawn() || hasPendingRespawnDecision()) {
            return false
        }

        syncEnginesForMaxTiles(state.maxTiles)

        val newlyFinishedIds = state.players
            .filter {
                it.resources.isAlive &&
                    it.id !in state.eliminatedOutPlayerIds &&
                    it.currentTile >= state.maxTiles &&
                    it.id !in state.finishOrderPlayerIds
            }
            .map { it.id }

        val updatedFinishOrder = if (newlyFinishedIds.isNotEmpty()) {
            state.finishOrderPlayerIds + newlyFinishedIds
        } else {
            state.finishOrderPlayerIds
        }
        val updatedFinishedIds = state.finishedPlayerIds + newlyFinishedIds

        if (state.isContinuingAfterVictory) {
            val remainingContenders = state.players.filter {
                it.resources.isAlive &&
                    it.id !in state.eliminatedOutPlayerIds &&
                    it.id !in updatedFinishedIds
            }
            val shouldEndContinuedMatch = remainingContenders.isEmpty()

            if (!shouldEndContinuedMatch) {
                if (newlyFinishedIds.isNotEmpty()) {
                    val lastFinisherName = state.players.find { it.id == newlyFinishedIds.last() }?.name.orEmpty()
                    val rankNum = updatedFinishOrder.indexOf(newlyFinishedIds.last()) + 1
                    _uiState.update { st ->
                        st.copy(
                            finishOrderPlayerIds = updatedFinishOrder,
                            finishedPlayerIds = updatedFinishedIds,
                            eventBanner = GameEventBanner(
                                message = "🏅 $lastFinisherName با رسیدن به خانه ${state.maxTiles.toPersianDigits()} رتبهٔ ${rankNum.toPersianDigits()} را قطعی کرد!",
                                type = BannerType.SIX_BONUS
                            )
                        )
                    }
                }
                return false
            }

            aiTurnJob?.cancel()
            soundManager.setVictoryMusicOverride(true)
            soundManager.playVictoryFanfare()

            val finalWinner = state.recordedWinner ?: state.winner
            val finalTeam = state.recordedWinningTeam ?: state.winningTeam
            val finalWinningPlayers = state.recordedWinningPlayers.ifEmpty { state.winningPlayers }
            val finalReason = state.recordedWinReason ?: state.matchWinReason
            val finalRankings = matchResolutionEngine.sortRankings(
                players = state.players,
                preferredWinnerId = finalWinner?.id,
                winningTeam = finalTeam,
                finishOrderPlayerIds = updatedFinishOrder,
                eliminatedOutPlayerIds = state.eliminatedOutPlayerIds
            )

            _uiState.update { st ->
                st.copy(
                    phase = GamePhase.CELEBRATION,
                    isDiceRolling = false,
                    isTokenAnimating = false,
                    pendingPrecisionChoice = null,
                    activeMovementPath = null,
                    highlightEffect = null,
                    stormFlashActive = false,
                    isContinuingAfterVictory = false,
                    hasFinalizedVictory = true,
                    finishOrderPlayerIds = updatedFinishOrder,
                    finishedPlayerIds = updatedFinishedIds,
                    winner = finalWinner,
                    winningTeam = finalTeam,
                    winningPlayers = finalWinningPlayers,
                    matchWinReason = finalReason,
                    matchRankings = finalRankings,
                    eventBanner = GameEventBanner(
                        message = "رتبه‌بندی تمام بازیکنان تکمیل شد! 🏆🎉",
                        type = BannerType.SIX_BONUS
                    )
                )
            }
            recordCompletedMatchOnce(_uiState.value)
            return true
        }

        val resolution = matchResolutionEngine.checkMatchStatus(
            players = state.players,
            isEliminationEnabled = isEliminationEnabled && state.players.size > 1,
            teamManager = teamManager,
            teamVictoryMode = state.teamVictoryMode,
            finishedPlayerIds = updatedFinishedIds,
            eliminatedOutPlayerIds = state.eliminatedOutPlayerIds,
            finishOrderPlayerIds = updatedFinishOrder
        )

        if (!resolution.isGameOver) {
            if (newlyFinishedIds.isNotEmpty()) {
                val finisherName = state.players.find { it.id == newlyFinishedIds.last() }?.name.orEmpty()
                _uiState.update { st ->
                    st.copy(
                        finishOrderPlayerIds = updatedFinishOrder,
                        finishedPlayerIds = updatedFinishedIds,
                        eventBanner = GameEventBanner(
                            message = "🏁 $finisherName به خانه ${state.maxTiles.toPersianDigits()} رسید! طبق قانون «${state.teamVictoryMode.persianTitle}» بازی برای بقیهٔ اعضای تیم ادامه دارد.",
                            type = BannerType.CHARACTER_ABILITY
                        )
                    )
                }
            }
            return false
        }

        aiTurnJob?.cancel()
        soundManager.setVictoryMusicOverride(true)
        if (!resolution.isDrawOrNoWinner) {
            soundManager.playVictoryFanfare()
        }

        val bannerMessage = when {
            resolution.isDrawOrNoWinner ->
                resolution.winReason ?: "مسابقه بدون برنده به پایان رسید!"
            resolution.winningTeam != null ->
                resolution.winReason ?: "تیم «${resolution.winningTeam.name}» قهرمان شد! 🏆🎉"
            else -> {
                val w = resolution.winner
                "تبریک! ${w?.name.orEmpty()} (${w?.character?.persianTitle.orEmpty()}) قهرمان شد! 🏆🎉"
            }
        }

        _uiState.update { st ->
            st.copy(
                phase = GamePhase.CELEBRATION,
                isDiceRolling = false,
                isTokenAnimating = false,
                pendingPrecisionChoice = null,
                activeMovementPath = null,
                highlightEffect = null,
                stormFlashActive = false,
                finishOrderPlayerIds = updatedFinishOrder,
                finishedPlayerIds = updatedFinishedIds,
                winner = resolution.winner,
                isMatchDraw = resolution.isDrawOrNoWinner,
                winningTeam = resolution.winningTeam,
                winningPlayers = resolution.winningPlayers,
                matchWinReason = resolution.winReason,
                matchRankings = resolution.rankings,
                recordedWinner = resolution.winner,
                recordedWinningTeam = resolution.winningTeam,
                recordedWinningPlayers = resolution.winningPlayers,
                recordedWinReason = resolution.winReason,
                hasFinalizedVictory = resolution.isDrawOrNoWinner,
                eventBanner = GameEventBanner(
                    message = bannerMessage,
                    type = if (resolution.isDrawOrNoWinner) BannerType.OBSTACLE_DANGER else BannerType.SIX_BONUS
                )
            )
        }
        val postResolutionState = _uiState.value
        if (postResolutionState.isMatchDraw || !postResolutionState.canContinueAfterVictory) {
            recordCompletedMatchOnce(postResolutionState)
        }
        return true
    }

    /**
     * Records a definitively finished match in Room DB at most once per match UUID.
     * Unfinished / in-progress matches are never saved.
     */
    private fun recordCompletedMatchOnce(snapshot: GameUiState) {
        if (snapshot.phase != GamePhase.CELEBRATION || snapshot.players.isEmpty()) return
        val finalWinner = snapshot.recordedWinner ?: snapshot.winner
        val isDraw = snapshot.isMatchDraw || finalWinner == null
        if (!isDraw && finalWinner == null) return

        val matchUuid = snapshot.currentMatchUuid.takeIf { it.isNotBlank() } ?: return
        synchronized(matchRecordLock) {
            if (snapshot.hasRecordedCurrentMatch || matchUuid in recordedMatchUuids) {
                return
            }
            recordedMatchUuids.add(matchUuid)
        }

        _uiState.update { st ->
            if (st.currentMatchUuid == matchUuid) st.copy(hasRecordedCurrentMatch = true) else st
        }

        val finalTeam = snapshot.recordedWinningTeam ?: snapshot.winningTeam
        val finalWinningPlayers = snapshot.recordedWinningPlayers.ifEmpty { snapshot.winningPlayers }
        val finalReason = snapshot.recordedWinReason ?: snapshot.matchWinReason
        val rankedPlayers = snapshot.matchRankings.ifEmpty {
            matchResolutionEngine.sortRankings(
                players = snapshot.players,
                preferredWinnerId = finalWinner?.id,
                winningTeam = finalTeam,
                finishOrderPlayerIds = snapshot.finishOrderPlayerIds,
                eliminatedOutPlayerIds = snapshot.eliminatedOutPlayerIds
            )
        }

        val winningTeamDisplayName = finalTeam?.name ?: if (!isDraw && snapshot.teamMode != TeamMode.SOLO_FREE_FOR_ALL) {
            finalWinner?.teamId?.let { teamManager.getTeam(it)?.name }
        } else {
            null
        }

        val participantRecords = rankedPlayers.mapIndexed { idx, p ->
            val team = p.teamId?.let { teamManager.getTeam(it) } ?: teamManager.getTeamForPlayer(p.id)
            val isParticipantWinner = !isDraw && (
                p.id == finalWinner?.id ||
                    finalWinningPlayers.any { it.id == p.id } ||
                    (finalTeam != null && snapshot.teamMode != TeamMode.SOLO_FREE_FOR_ALL && finalTeam.containsPlayer(p.id))
                )
            MatchParticipantRecord(
                playerId = p.id,
                playerName = p.name,
                characterId = p.character.id,
                characterName = p.character.defaultEnglishName,
                characterTitle = p.character.persianTitle,
                characterBadgeEmoji = p.character.displayBadgeEmoji,
                pawnSkinId = p.customPawnSkinId,
                teamId = team?.id ?: p.teamId,
                teamName = team?.name,
                rank = idx + 1,
                finalTile = p.currentTile,
                isWinner = isParticipantWinner,
                isEliminated = !p.resources.isAlive || p.id in snapshot.eliminatedOutPlayerIds,
                isAiBot = p.isAiBot,
                laddersClimbed = p.laddersClimbed,
                snakesBitten = p.snakesBitten,
                obstaclesTriggered = p.obstaclesTriggered,
                totalSixes = p.totalSixes,
                remainingHp = p.resources.hp
            )
        }

        val record = MatchHistoryRecord(
            matchUuid = matchUuid,
            completedAtMillis = System.currentTimeMillis(),
            maxTiles = snapshot.maxTiles,
            difficulty = snapshot.difficulty,
            boardTheme = snapshot.boardTheme,
            teamMode = snapshot.teamMode,
            teamVictoryMode = snapshot.teamVictoryMode,
            playerCount = snapshot.players.size,
            isDraw = isDraw,
            winnerPlayerId = if (isDraw) null else finalWinner?.id,
            winnerName = if (isDraw) null else finalWinner?.name,
            winnerCharacterTitle = if (isDraw) null else finalWinner?.character?.persianTitle,
            winningTeamId = if (isDraw || snapshot.teamMode == TeamMode.SOLO_FREE_FOR_ALL) null else finalTeam?.id,
            winningTeamName = if (isDraw || snapshot.teamMode == TeamMode.SOLO_FREE_FOR_ALL) null else winningTeamDisplayName,
            winReason = finalReason,
            participants = participantRecords
        )

        viewModelScope.launch {
            cardRepository.saveCompletedMatch(record)
        }
    }

    fun openMatchHistory() {
        _uiState.update {
            it.copy(
                isMatchHistoryOpen = true,
                selectedMatchHistoryDetail = null
            )
        }
    }

    fun closeMatchHistory() {
        _uiState.update {
            it.copy(
                isMatchHistoryOpen = false,
                selectedMatchHistoryDetail = null
            )
        }
    }

    fun selectMatchHistoryDetail(record: MatchHistoryRecord?) {
        _uiState.update {
            it.copy(selectedMatchHistoryDetail = record)
        }
    }

    fun deleteMatchHistoryRecord(recordId: Long) {
        viewModelScope.launch {
            cardRepository.deleteMatchHistory(recordId)
            _uiState.update { st ->
                val updatedSelected = if (st.selectedMatchHistoryDetail?.id == recordId) null else st.selectedMatchHistoryDetail
                st.copy(selectedMatchHistoryDetail = updatedSelected)
            }
        }
    }

    fun clearAllMatchHistory() {
        viewModelScope.launch {
            cardRepository.clearMatchHistory()
            _uiState.update { it.copy(selectedMatchHistoryDetail = null) }
        }
    }

    private suspend fun advanceTurnOrGrantBonus(
        rolledSixOrFortuneFive: Boolean,
        luckExtraTurn: Boolean
    ) {
        val currentState = _uiState.value
        if (currentState.phase != GamePhase.PLAYING || currentState.players.isEmpty()) return
        if (hasPendingRespawnDecision()) return
        if (checkAndApplyMatchResolution()) return

        val currentPlayer = currentState.currentPlayer
        val bonusDecision = turnMovementEngine.resolveBonusTurn(
            rolledSixOrFortuneFive = rolledSixOrFortuneFive,
            luckExtraTurn = luckExtraTurn,
            currentConsecutiveSixes = currentState.consecutiveSixesCount
        )

        if (bonusDecision.grantExtraTurn &&
            currentPlayer != null &&
            currentPlayer.resources.isAlive &&
            currentPlayer.id !in currentState.eliminatedOutPlayerIds &&
            currentPlayer.id !in currentState.finishedPlayerIds &&
            currentPlayer.swampSkipTurnsRemaining == 0 &&
            currentPlayer.frozenSkipTurnsRemaining == 0
        ) {
            val progressedCurrent = heroAbilityEngine.onTurnStart(currentPlayer)
            _uiState.update { state ->
                val updated = state.players.map { p ->
                    if (p.id == progressedCurrent.id) progressedCurrent else p
                }
                state.copy(
                    players = updated,
                    consecutiveSixesCount = bonusDecision.nextConsecutiveSixes,
                    isTokenAnimating = false,
                    sixRollGlowActive = true,
                    eventBanner = GameEventBanner(
                        message = if (luckExtraTurn && !rolledSixOrFortuneFive) {
                            "پرتاب جایزه! 🍀 دوباره نوبت ${progressedCurrent.name} است!"
                        } else {
                            "جایزهٔ تاس طلایی! 🌟 دوباره نوبت ${progressedCurrent.name} است!"
                        },
                        type = BannerType.SIX_BONUS
                    )
                )
            }
            triggerAiTurnIfNeeded()
            return
        }

        val hitSixCap = rolledSixOrFortuneFive && !bonusDecision.grantExtraTurn
        val turnSelection = turnMovementEngine.resolveNextPlayableTurn(
            currentIndex = currentState.currentTurnIndex,
            players = _uiState.value.players,
            finishedPlayerIds = currentState.finishedPlayerIds
        )

        if (turnSelection.noAlivePlayers) {
            checkAndApplyMatchResolution()
            return
        }

        for (skipped in turnSelection.skippedPlayers) {
            soundManager.playObstacleSound(if (skipped.isFrozen) ObstacleType.WIND else ObstacleType.SWAMP)
            _uiState.update { st ->
                val candidate = st.players.getOrNull(skipped.playerIndex)
                val updated = st.players.mapIndexed { idx, p ->
                    if (idx == skipped.playerIndex) {
                        p.copy(
                            swampSkipTurnsRemaining = (p.swampSkipTurnsRemaining - 1).coerceAtLeast(0),
                            frozenSkipTurnsRemaining = (p.frozenSkipTurnsRemaining - 1).coerceAtLeast(0)
                        )
                    } else p
                }
                st.copy(
                    players = updated,
                    currentTurnIndex = skipped.playerIndex,
                    isTokenAnimating = true,
                    sixRollGlowActive = false,
                    eventBanner = GameEventBanner(
                        message = if (skipped.isFrozen) {
                            "${candidate?.name.orEmpty()} بر اثر شمشیر یخی منجمد است و این نوبت را از دست می‌دهد! ❄️"
                        } else {
                            "${candidate?.name.orEmpty()} در باتلاق گیر کرده است و این دور را از دست می‌دهد! 🌿"
                        },
                        type = BannerType.OBSTACLE_DANGER
                    )
                )
            }
            animDelay(1150L)
        }

        soundManager.playTurnChangeChime()

        _uiState.update { state ->
            val rawNext = state.players.getOrNull(turnSelection.nextIndex)
            val progressedNext = rawNext?.let { heroAbilityEngine.onTurnStart(it) }
            val updatedPlayers = if (progressedNext != null) {
                state.players.mapIndexed { idx, p ->
                    if (idx == turnSelection.nextIndex) progressedNext else p
                }
            } else {
                state.players
            }

            val nextPlayer = progressedNext ?: state.players[turnSelection.nextIndex]
            val statusHint = when {
                nextPlayer.dragonSoulBoostTurns > 0 -> " (روح اژدها فعال: +۲ خانه 🔥)"
                nextPlayer.berserkerRageTurns > 0 -> " (خشم برسرکر فعال: +۲ خانه 🪨🔥)"
                nextPlayer.halveNextRoll -> " (تاس این نوبت نصف می‌شود 🥾)"
                nextPlayer.gravityCapNextRoll -> " (لنگر جاذبه: سقف تاس ۴ است ⚓)"
                nextPlayer.wormCurseTurnsRemaining > 0 -> " (طلسم کرم: ${nextPlayer.wormCurseTurnsRemaining.toPersianDigits()} نوبت 🪱)"
                nextPlayer.canTriggerActiveAbilityNow -> " (قابلیت ${nextPlayer.character.displayAbilityTitle} آماده است ⚡)"
                else -> ""
            }
            val capPrefix = if (hitSixCap) "سقف ۳ شیش متوالی رسید! • " else ""
            state.copy(
                players = updatedPlayers,
                currentTurnIndex = turnSelection.nextIndex,
                consecutiveSixesCount = 0,
                isTokenAnimating = false,
                sixRollGlowActive = false,
                eventBanner = GameEventBanner(
                    message = "${capPrefix}نوبت ${nextPlayer.name} (خانه ${nextPlayer.currentTile.toPersianDigits()})$statusHint است 🎲",
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
        if (!activePlayer.resources.isAlive ||
            activePlayer.id in state.finishedPlayerIds ||
            state.activeRespawnPromptPlayerId != null
        ) return

        aiTurnJob?.cancel()
        aiTurnJob = viewModelScope.launch {
            animDelay(850L)
            val latestState = _uiState.value
            val aiPlayer = latestState.currentPlayer ?: return@launch
            if (latestState.phase != GamePhase.PLAYING ||
                !aiPlayer.isAiBot ||
                !aiPlayer.resources.isAlive ||
                aiPlayer.id in latestState.finishedPlayerIds ||
                latestState.activeRespawnPromptPlayerId != null
            ) return@launch

            if (aiPlayer.canTriggerActiveAbilityNow) {
                val shouldUseAbility = when (latestState.aiDifficulty) {
                    AiDifficulty.EASY -> randomProvider.nextDouble() < 0.25
                    AiDifficulty.MEDIUM -> randomProvider.nextDouble() < 0.65
                    AiDifficulty.HARD -> true
                }
                if (shouldUseAbility) {
                    activateCharacterAbility()
                    animDelay(950L)
                }
            }

            if (_uiState.value.phase == GamePhase.PLAYING &&
                _uiState.value.activeRespawnPromptPlayerId == null
            ) {
                rollDice()
            }
        }
    }

    private fun chooseBestPrecisionOptionForAi(aiPlayer: Player, choice: PrecisionRollChoice): Int {
        val state = _uiState.value
        val options = listOf(choice.die1, choice.die2, choice.sum).distinct()
        if (state.aiDifficulty == AiDifficulty.EASY) {
            return options[randomProvider.nextInt(0, options.size)]
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

    fun setBoardLayoutForTest(links: List<BoardLink>, obstacles: List<BoardObstacle>) {
        _uiState.update { st ->
            st.copy(boardLinks = links, boardObstacles = obstacles)
        }
    }

    fun setPlayerResourcesForTest(playerId: Int, resources: PlayerResources) {
        _uiState.update { st ->
            val updated = st.players.map { p ->
                if (p.id == playerId) p.copy(resources = resources) else p
            }
            st.copy(players = updated)
        }
    }

    fun setTurnIndexForTest(turnIndex: Int) {
        _uiState.update { st ->
            st.copy(currentTurnIndex = turnIndex.coerceIn(0, (st.players.size - 1).coerceAtLeast(0)))
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
