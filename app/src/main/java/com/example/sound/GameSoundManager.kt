package com.example.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.BoardTheme
import com.example.model.MusicGenre
import com.example.model.ObstacleType
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class SoundEffect {
    MATCH_START,
    TURN_CHANGE,
    DICE_ROLL_CASCADE,
    DICE_TICK,
    TOKEN_STEP,
    SIX_BONUS,
    LADDER_CLIMB,
    SNAKE_BITE,
    VICTORY_FANFARE,
    // Phase 2 Obstacle Sounds:
    OBSTACLE_WORM,
    OBSTACLE_REVERSE,
    OBSTACLE_SWAMP,
    OBSTACLE_WELL,
    OBSTACLE_WIND,
    OBSTACLE_LUCK,
    OBSTACLE_STORM,
    // Phase 4 Truth or Dare Card Sounds:
    CARD_DRAW_SUSPENSE,
    CARD_FLIP_REVEAL
}

/**
 * Low-latency audio & haptic manager powered by Android [SoundPool] + [ThemeMusicManager]
 * for dynamic genre/theme-based background music, gentle environmental soundscapes,
 * and dedicated game event sounds.
 */
class GameSoundManager(context: Context) {

    private val appContext = context.applicationContext
    private val sampleRate = 24000

    private val soundPool: SoundPool
    private val soundIdMap = ConcurrentHashMap<SoundEffect, Int>()
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()

    val themeMusicManager: ThemeMusicManager = ThemeMusicManager()

    @Volatile
    var isSoundEnabled: Boolean = true

    @Volatile
    var sfxVolume: Float = 0.85f

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
            }
        }

        preloadSynthesizedSounds()
    }

    fun updateThemeMusic(theme: BoardTheme, playTransitionMotif: Boolean = false) {
        themeMusicManager.setTheme(theme)
        if (playTransitionMotif && isSoundEnabled) {
            when (theme) {
                BoardTheme.JUNGLE -> playEffect(SoundEffect.SIX_BONUS, 0.80f, 0.80f, 2, 1.05f)
                BoardTheme.OCEAN_BEACH -> playEffect(SoundEffect.OBSTACLE_WIND, 0.72f, 0.72f, 2, 0.92f)
                BoardTheme.DESERT -> playEffect(SoundEffect.OBSTACLE_LUCK, 0.80f, 0.80f, 2, 0.85f)
                BoardTheme.ARCTIC_ICE -> playEffect(SoundEffect.SIX_BONUS, 0.82f, 0.82f, 2, 1.32f)
                BoardTheme.BLACK_GOLD -> playEffect(SoundEffect.SIX_BONUS, 0.88f, 0.88f, 2, 0.96f)
            }
        }
    }

    fun setMusicGenre(genre: MusicGenre, playTransitionMotif: Boolean = false) {
        themeMusicManager.setMusicGenre(genre)
        if (playTransitionMotif && isSoundEnabled) {
            playEffect(SoundEffect.TURN_CHANGE, 0.85f, 0.85f, 2, 1.05f)
        }
    }

    fun previewMusicGenre(genre: MusicGenre?) {
        themeMusicManager.startPreviewGenre(genre)
    }

    fun setVictoryMusicOverride(active: Boolean) {
        themeMusicManager.setVictoryMusicOverride(active)
    }

    fun setMusicEnabled(enabled: Boolean) {
        themeMusicManager.isMusicEnabled = enabled
    }

    fun setAmbientEnabled(enabled: Boolean) {
        themeMusicManager.isAmbientEnabled = enabled
    }

    fun setMusicVolume(volume: Float) {
        themeMusicManager.musicVolume = volume.coerceIn(0f, 1f)
    }

    fun setAmbientVolume(volume: Float) {
        themeMusicManager.ambientVolume = volume.coerceIn(0f, 1f)
    }

    fun updateSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    fun pauseBackgroundMusic() {
        themeMusicManager.pauseForLifecycle()
    }

    fun resumeBackgroundMusic() {
        themeMusicManager.resumeForLifecycle()
    }

    private fun preloadSynthesizedSounds() {
        try {
            val soundCacheDir = File(appContext.cacheDir, "mystic_snakes_sfx_v4").apply {
                if (!exists()) mkdirs()
            }

            registerEffect(SoundEffect.MATCH_START, File(soundCacheDir, "sfx_match_start.wav")) {
                synthesizeMatchStartFanfarePcm()
            }
            registerEffect(SoundEffect.TURN_CHANGE, File(soundCacheDir, "sfx_turn_change.wav")) {
                synthesizeTurnChangeChimePcm()
            }
            registerEffect(SoundEffect.DICE_ROLL_CASCADE, File(soundCacheDir, "sfx_dice_roll_cascade.wav")) {
                synthesizeDiceRollCascadePcm()
            }
            registerEffect(SoundEffect.DICE_TICK, File(soundCacheDir, "sfx_dice_tick.wav")) {
                synthesizeDiceTickPcm()
            }
            registerEffect(SoundEffect.TOKEN_STEP, File(soundCacheDir, "sfx_token_step.wav")) {
                synthesizeTokenStepPcm()
            }
            registerEffect(SoundEffect.SIX_BONUS, File(soundCacheDir, "sfx_six_bonus.wav")) {
                synthesizeSixBonusPcm()
            }
            registerEffect(SoundEffect.LADDER_CLIMB, File(soundCacheDir, "sfx_ladder_climb.wav")) {
                synthesizeLadderClimbPcm()
            }
            registerEffect(SoundEffect.SNAKE_BITE, File(soundCacheDir, "sfx_snake_bite.wav")) {
                synthesizeSnakeBitePcm()
            }
            registerEffect(SoundEffect.VICTORY_FANFARE, File(soundCacheDir, "sfx_victory_fanfare.wav")) {
                synthesizeVictoryFanfarePcm()
            }
            // Phase 2 Obstacles
            registerEffect(SoundEffect.OBSTACLE_WORM, File(soundCacheDir, "sfx_worm.wav")) {
                synthesizeWormSlimePcm()
            }
            registerEffect(SoundEffect.OBSTACLE_REVERSE, File(soundCacheDir, "sfx_reverse.wav")) {
                synthesizeReverseRewindPcm()
            }
            registerEffect(SoundEffect.OBSTACLE_SWAMP, File(soundCacheDir, "sfx_swamp.wav")) {
                synthesizeSwampSinkPcm()
            }
            registerEffect(SoundEffect.OBSTACLE_WELL, File(soundCacheDir, "sfx_well.wav")) {
                synthesizeWellDropPcm()
            }
            registerEffect(SoundEffect.OBSTACLE_WIND, File(soundCacheDir, "sfx_wind.wav")) {
                synthesizeWindGustPcm()
            }
            registerEffect(SoundEffect.OBSTACLE_LUCK, File(soundCacheDir, "sfx_luck.wav")) {
                synthesizeLuckMysteryPcm()
            }
            registerEffect(SoundEffect.OBSTACLE_STORM, File(soundCacheDir, "sfx_storm.wav")) {
                synthesizeStormThunderPcm()
            }
            // Phase 4 Truth or Dare Card Sounds
            registerEffect(SoundEffect.CARD_DRAW_SUSPENSE, File(soundCacheDir, "sfx_card_suspense.wav")) {
                synthesizeCardSuspensePcm()
            }
            registerEffect(SoundEffect.CARD_FLIP_REVEAL, File(soundCacheDir, "sfx_card_reveal.wav")) {
                synthesizeCardFlipRevealPcm()
            }
        } catch (_: Exception) {
        }
    }

    private fun registerEffect(
        effect: SoundEffect,
        file: File,
        pcmGenerator: () -> ShortArray
    ) {
        if (!file.exists() || file.length() <= 44L) {
            val samples = pcmGenerator()
            writeWavFile(file, samples, sampleRate)
        }
        val soundId = soundPool.load(file.absolutePath, 1)
        if (soundId > 0) {
            soundIdMap[effect] = soundId
        }
    }

    private fun playEffect(
        effect: SoundEffect,
        leftVolume: Float = 0.9f,
        rightVolume: Float = 0.9f,
        priority: Int = 1,
        rate: Float = 1.0f
    ) {
        if (!isSoundEnabled) return
        val masterSfx = sfxVolume.coerceIn(0f, 1f)
        if (masterSfx <= 0.005f) return
        val soundId = soundIdMap[effect] ?: return
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        val scaledLeft = (leftVolume * masterSfx).coerceIn(0f, 1f)
        val scaledRight = (rightVolume * masterSfx).coerceIn(0f, 1f)
        try {
            if (loadedSoundIds.contains(soundId) || loadedSoundIds.isNotEmpty()) {
                soundPool.play(soundId, scaledLeft, scaledRight, priority, 0, clampedRate)
            }
        } catch (_: Exception) {
        }
    }

    fun playMatchStartFanfare() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(900L)
        vibratePattern(longArrayOf(0, 35, 30, 45, 30, 75))
        playEffect(SoundEffect.MATCH_START, 0.96f, 0.96f, 3, 1.0f)
    }

    fun playTurnChangeChime() {
        if (!isSoundEnabled) return
        vibrateShort(14)
        playEffect(SoundEffect.TURN_CHANGE, 0.78f, 0.78f, 1, 1.0f)
    }

    fun playDiceRollCascade() {
        if (!isSoundEnabled) return
        vibratePattern(longArrayOf(0, 18, 28, 16, 32, 22))
        playEffect(SoundEffect.DICE_ROLL_CASCADE, 0.90f, 0.90f, 2, 1.0f)
    }

    fun playDiceTick(stepIndex: Int = 0) {
        if (!isSoundEnabled) return
        vibrateShort(10)
        val pitchRate = (0.90f + (stepIndex % 6) * 0.045f).coerceIn(0.8f, 1.3f)
        playEffect(SoundEffect.DICE_TICK, 0.75f, 0.75f, 1, pitchRate)
    }

    fun playStepPop(tileNumber: Int, isBackward: Boolean = false) {
        if (!isSoundEnabled) return
        vibrateShort(12)
        val normalizedProgress = (tileNumber.coerceIn(1, 100) - 1) / 99f
        val dynamicRate = if (isBackward) {
            (0.78f + normalizedProgress * 0.25f).coerceIn(0.65f, 1.1f)
        } else {
            (0.88f + normalizedProgress * 0.42f).coerceIn(0.8f, 1.4f)
        }
        playEffect(SoundEffect.TOKEN_STEP, 0.85f, 0.85f, 1, dynamicRate)
    }

    fun playSixBonusChime() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(550L)
        vibratePattern(longArrayOf(0, 25, 40, 45))
        playEffect(SoundEffect.SIX_BONUS, 0.92f, 0.92f, 2, 1.0f)
    }

    fun playLadderClimb() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(950L)
        vibratePattern(longArrayOf(0, 20, 35, 25, 35, 40))
        playEffect(SoundEffect.LADDER_CLIMB, 0.95f, 0.95f, 2, 1.0f)
    }

    fun playSnakeSlide() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(950L)
        vibratePattern(longArrayOf(0, 70, 45, 95, 45, 130))
        playEffect(SoundEffect.SNAKE_BITE, 0.95f, 0.95f, 2, 1.0f)
    }

    fun playVictoryFanfare() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(1800L)
        vibratePattern(longArrayOf(0, 60, 50, 60, 50, 80, 60, 180))
        playEffect(SoundEffect.VICTORY_FANFARE, 1.0f, 1.0f, 3, 1.0f)
    }

    fun playAbilityActivateSound() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(650L)
        vibratePattern(longArrayOf(0, 30, 25, 45, 25, 65))
        playEffect(SoundEffect.OBSTACLE_LUCK, 1.0f, 1.0f, 3, 1.15f)
    }

    fun playCardDrawSuspenseSound() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(1100L)
        vibratePattern(longArrayOf(0, 45, 30, 45, 30, 90))
        playEffect(SoundEffect.CARD_DRAW_SUSPENSE, 1.0f, 1.0f, 3, 1.0f)
    }

    fun playCardFlipRevealSound() {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(750L)
        vibratePattern(longArrayOf(0, 35, 25, 75))
        playEffect(SoundEffect.CARD_FLIP_REVEAL, 1.0f, 1.0f, 3, 1.0f)
    }

    fun playCardSavedSound() {
        if (!isSoundEnabled) return
        vibrateShort(22)
        playEffect(SoundEffect.SIX_BONUS, 0.85f, 0.85f, 2, 1.18f)
    }

    fun playObstacleSound(type: ObstacleType) {
        if (!isSoundEnabled) return
        themeMusicManager.triggerDucking(850L)
        when (type) {
            ObstacleType.WORM -> {
                vibratePattern(longArrayOf(0, 35, 30, 45, 30, 60))
                playEffect(SoundEffect.OBSTACLE_WORM, 0.95f, 0.95f, 2, 1.0f)
            }
            ObstacleType.REVERSE -> {
                vibratePattern(longArrayOf(0, 50, 40, 80))
                playEffect(SoundEffect.OBSTACLE_REVERSE, 0.95f, 0.95f, 2, 1.0f)
            }
            ObstacleType.SWAMP -> {
                vibratePattern(longArrayOf(0, 90, 60, 140))
                playEffect(SoundEffect.OBSTACLE_SWAMP, 0.98f, 0.98f, 2, 1.0f)
            }
            ObstacleType.WELL -> {
                vibratePattern(longArrayOf(0, 40, 25, 110, 40, 50))
                playEffect(SoundEffect.OBSTACLE_WELL, 0.95f, 0.95f, 2, 1.0f)
            }
            ObstacleType.WIND -> {
                vibratePattern(longArrayOf(0, 30, 25, 50, 25, 30))
                playEffect(SoundEffect.OBSTACLE_WIND, 0.95f, 0.95f, 2, 1.0f)
            }
            ObstacleType.LUCK -> {
                vibratePattern(longArrayOf(0, 25, 30, 25, 30, 50))
                playEffect(SoundEffect.OBSTACLE_LUCK, 0.95f, 0.95f, 2, 1.0f)
            }
            ObstacleType.STORM -> {
                vibratePattern(longArrayOf(0, 110, 40, 90, 40, 140))
                playEffect(SoundEffect.OBSTACLE_STORM, 1.0f, 1.0f, 3, 1.0f)
            }
        }
    }

    fun release() {
        try {
            themeMusicManager.release()
            soundPool.release()
        } catch (_: Exception) {
        }
    }

    // --- Procedural PCM Sound Effect Generators ---

    private fun synthesizeMatchStartFanfarePcm(): ShortArray {
        data class OpeningNote(val freq: Double, val startMs: Int, val durationMs: Int, val gain: Float)
        // Noble royal herald fanfare: D4 -> F4 -> A4 -> D5 -> F5 + Fifths chord
        val notes = listOf(
            OpeningNote(293.66, 0, 130, 0.55f),
            OpeningNote(349.23, 105, 130, 0.55f),
            OpeningNote(440.00, 210, 140, 0.58f),
            OpeningNote(587.33, 330, 480, 0.65f),
            OpeningNote(880.00, 350, 460, 0.42f),
            OpeningNote(1174.66, 370, 440, 0.32f)
        )
        val totalSamples = (sampleRate * 860) / 1000
        val buffer = ShortArray(totalSamples)
        for (item in notes) {
            val startSample = (item.startMs * sampleRate) / 1000
            val samples = generateNote(
                freqHz = item.freq,
                harmonicRatio = 2.0,
                harmonicAmp = 0.38,
                durationMs = item.durationMs,
                decayRate = if (item.durationMs > 300) 4.5 else 10.5
            )
            mixInto(buffer, samples, startSample, item.gain)
        }
        return buffer
    }

    private fun synthesizeTurnChangeChimePcm(): ShortArray {
        // Gentle two-note golden crystal bell chime (G5 -> D6)
        val notes = listOf(783.99, 1174.66)
        val totalSamples = (sampleRate * 320) / 1000
        val buffer = ShortArray(totalSamples)
        notes.forEachIndexed { idx, freq ->
            val startSample = (idx * 75 * sampleRate) / 1000
            val samples = generateNote(
                freqHz = freq,
                harmonicRatio = 3.0,
                harmonicAmp = 0.20,
                durationMs = 210,
                decayRate = 11.5
            )
            mixInto(buffer, samples, startSample, 0.54f)
        }
        return buffer
    }

    private fun synthesizeDiceRollCascadePcm(): ShortArray {
        // Multi-bounce 3D luxury dice tumble sound
        val bounceOffsetsMs = listOf(0, 62, 118, 168, 212, 250)
        val bounceFreqs = listOf(360.0, 410.0, 460.0, 505.0, 545.0, 610.0)
        val totalSamples = (sampleRate * 340) / 1000
        val buffer = ShortArray(totalSamples)
        val rng = Random(84)
        bounceOffsetsMs.forEachIndexed { idx, startMs ->
            val startSample = (startMs * sampleRate) / 1000
            val durMs = 48
            val count = (sampleRate * durMs) / 1000
            val bounceSamples = ShortArray(count)
            val baseFreq = bounceFreqs[idx]
            for (i in 0 until count) {
                val t = i.toDouble() / sampleRate
                val env = exp(-48.0 * t)
                val tone = sin(2.0 * PI * baseFreq * t) * 0.72 +
                    sin(2.0 * PI * (baseFreq * 2.15) * t) * 0.20
                val click = rng.nextDouble(-1.0, 1.0) * 0.18
                val sample = (tone + click) * env * (0.72 - idx * 0.04)
                bounceSamples[i] = (sample * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            mixInto(buffer, bounceSamples, startSample, 0.85f)
        }
        return buffer
    }

    private fun synthesizeDiceTickPcm(): ShortArray {
        val durationMs = 45
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        val rng = Random(42)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val env = exp(-52.0 * t)
            val woodTone = sin(2.0 * PI * 390.0 * t) * 0.72 + sin(2.0 * PI * 820.0 * t) * 0.18
            val clickNoise = rng.nextDouble(-1.0, 1.0) * 0.22
            val sample = (woodTone + clickNoise) * env * 0.65
            out[i] = (sample * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun synthesizeTokenStepPcm(): ShortArray {
        return generateNote(
            freqHz = 523.25,
            harmonicRatio = 2.0,
            harmonicAmp = 0.30,
            durationMs = 85,
            decayRate = 20.0
        )
    }

    private fun synthesizeSixBonusPcm(): ShortArray {
        val notes = listOf(523.25, 659.25, 783.99, 1046.50)
        val combined = ShortArray((sampleRate * 500) / 1000)
        notes.forEachIndexed { idx, freq ->
            val startSample = (idx * 75 * sampleRate) / 1000
            val noteSamples = generateNote(freq, 3.0, 0.22, 210, 9.0)
            mixInto(combined, noteSamples, startSample, gain = 0.65f)
        }
        return combined
    }

    private fun synthesizeLadderClimbPcm(): ShortArray {
        val scale = listOf(523.25, 587.33, 659.25, 783.99, 880.0, 1046.50, 1174.66, 1318.51)
        val totalSamples = (sampleRate * 920) / 1000
        val buffer = ShortArray(totalSamples)
        scale.forEachIndexed { i, freq ->
            val offset = (i * 85 * sampleRate) / 1000
            val dur = if (i == scale.lastIndex) 280 else 150
            val note = generateNote(freq, 2.0, 0.35, dur, if (i == scale.lastIndex) 5.5 else 11.0)
            mixInto(buffer, note, offset, gain = 0.58f)
        }
        return buffer
    }

    private fun synthesizeSnakeBitePcm(): ShortArray {
        val durationMs = 780
        val numSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        var subPhase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val vibrato = sin(2.0 * PI * 14.0 * t) * 16.0
            val currentFreq = 580.0 * (1.0 - 0.76 * progress) + vibrato
            phase += (2.0 * PI * currentFreq) / sampleRate
            subPhase += (2.0 * PI * (currentFreq * 0.5)) / sampleRate

            val env = sin(PI * progress) * exp(-1.35 * progress)
            val wave = (sin(phase) * 0.65 + sin(subPhase) * 0.35) * env
            buffer[i] = (wave * Short.MAX_VALUE * 0.68).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun synthesizeVictoryFanfarePcm(): ShortArray {
        data class ScoreNote(val freq: Double, val startMs: Int, val durationMs: Int, val gain: Float)
        val melody = listOf(
            ScoreNote(523.25, 0, 140, 0.55f),
            ScoreNote(659.25, 130, 140, 0.55f),
            ScoreNote(783.99, 260, 140, 0.55f),
            ScoreNote(1046.50, 390, 260, 0.65f),
            ScoreNote(783.99, 660, 160, 0.55f),
            ScoreNote(523.25, 830, 750, 0.38f),
            ScoreNote(659.25, 830, 750, 0.38f),
            ScoreNote(783.99, 830, 750, 0.38f),
            ScoreNote(1046.50, 830, 800, 0.50f),
            ScoreNote(1318.51, 860, 770, 0.35f)
        )
        val totalSamples = (sampleRate * 1700) / 1000
        val buffer = ShortArray(totalSamples)
        for (item in melody) {
            val startSample = (item.startMs * sampleRate) / 1000
            val samples = generateNote(
                freqHz = item.freq,
                harmonicRatio = 2.0,
                harmonicAmp = 0.4,
                durationMs = item.durationMs,
                decayRate = if (item.durationMs > 500) 3.2 else 9.5
            )
            mixInto(buffer, samples, startSample, item.gain)
        }
        return buffer
    }

    // 1. WORM: Squishy slimy FM warble
    private fun synthesizeWormSlimePcm(): ShortArray {
        val durationMs = 540
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val squelchMod = sin(2.0 * PI * 19.0 * t) * 95.0 + sin(2.0 * PI * 7.5 * t) * 45.0
            val freq = 260.0 + squelchMod - 60.0 * progress
            phase += (2.0 * PI * freq) / sampleRate
            val env = sin(PI * progress) * exp(-1.2 * progress)
            val sample = (sin(phase) * 0.75 + sin(phase * 2.5) * 0.25) * env * 0.70
            out[i] = (sample * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // 2. REVERSE: Sweet "Oof / Rewind" 4-note descending arpeggio (A5 -> F5 -> D5 -> A4)
    private fun synthesizeReverseRewindPcm(): ShortArray {
        val notes = listOf(880.0, 698.46, 587.33, 440.0)
        val totalSamples = (sampleRate * 560) / 1000
        val buffer = ShortArray(totalSamples)
        notes.forEachIndexed { idx, freq ->
            val offset = (idx * 95 * sampleRate) / 1000
            val note = generateNote(freq, 1.5, 0.35, 175, 10.0)
            mixInto(buffer, note, offset, 0.62f)
        }
        return buffer
    }

    // 3. SWAMP: Heavy bubbling mud sink (low bass thud + rising bubble pops)
    private fun synthesizeSwampSinkPcm(): ShortArray {
        val durationMs = 680
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val bubblePulse = sin(2.0 * PI * 11.0 * t) * 38.0
            val freq = (145.0 - 55.0 * progress) + bubblePulse
            phase += (2.0 * PI * freq) / sampleRate
            val env = exp(-2.2 * progress) * sin(PI * progress.coerceAtMost(0.98))
            val wave = (sin(phase) * 0.8 + sin(phase * 3.0) * 0.2) * env * 0.78
            out[i] = (wave * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // 4. WELL: Fast hollow whistle drop into deep cavern splash + portal pop
    private fun synthesizeWellDropPcm(): ShortArray {
        val durationMs = 720
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        var phase = 0.0
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / count
            val freq = if (progress < 0.72) {
                // Falling whistle 920Hz -> 180Hz
                val p = progress / 0.72
                920.0 * (1.0 - 0.80 * p * p)
            } else {
                // Emerging portal pop 320Hz -> 640Hz
                val p = (progress - 0.72) / 0.28
                320.0 + 360.0 * p
            }
            phase += (2.0 * PI * freq) / sampleRate
            val env = if (progress < 0.72) {
                sin(PI * (progress / 0.72)) * 0.70
            } else {
                sin(PI * ((progress - 0.72) / 0.28)) * 0.80
            }
            out[i] = (sin(phase) * env * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // 5. WIND: Breezy aerodynamic swoosh + airy chime
    private fun synthesizeWindGustPcm(): ShortArray {
        val durationMs = 580
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        val rng = Random(77)
        var lpState = 0.0
        var phase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val white = rng.nextDouble(-1.0, 1.0)
            // Dynamic low-pass filter sweep for wind swoosh
            val alpha = 0.08 + 0.22 * sin(PI * progress)
            lpState += alpha * (white - lpState)

            val whistleFreq = 420.0 + 340.0 * sin(PI * progress)
            phase += (2.0 * PI * whistleFreq) / sampleRate
            val env = sin(PI * progress)
            val combined = (lpState * 0.65 + sin(phase) * 0.35) * env * 0.72
            out[i] = (combined * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // 6. LUCK: Magical mystery box arpeggio shimmer
    private fun synthesizeLuckMysteryPcm(): ShortArray {
        val notes = listOf(587.33, 739.99, 880.0, 1174.66, 1479.98)
        val totalSamples = (sampleRate * 560) / 1000
        val buffer = ShortArray(totalSamples)
        notes.forEachIndexed { idx, freq ->
            val offset = (idx * 65 * sampleRate) / 1000
            val note = generateNote(freq, 3.0, 0.28, 210, 8.5)
            mixInto(buffer, note, offset, 0.58f)
        }
        return buffer
    }

    // 7. STORM: Dramatic thunder crack + swirling vortex rumble
    private fun synthesizeStormThunderPcm(): ShortArray {
        val durationMs = 820
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        val rng = Random(999)
        var rumbleFilter = 0.0
        var vortexPhase = 0.0
        for (i in 0 until count) {
            val progress = i.toDouble() / count
            val noise = rng.nextDouble(-1.0, 1.0)
            rumbleFilter += 0.06 * (noise - rumbleFilter)
            val vortexFreq = 110.0 + 90.0 * sin(2.0 * PI * 4.5 * progress)
            vortexPhase += (2.0 * PI * vortexFreq) / sampleRate
            val env = exp(-1.8 * progress) * (1.0 - 0.2 * progress)
            val sample = (rumbleFilter * 0.72 + sin(vortexPhase) * 0.45) * env * 0.85
            out[i] = (sample * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // 8. CARD DRAW SUSPENSE: Rising mystical tension chord + heartbeat pulse
    private fun synthesizeCardSuspensePcm(): ShortArray {
        val notes = listOf(293.66, 349.23, 440.0, 554.37, 659.25)
        val totalSamples = (sampleRate * 680) / 1000
        val buffer = ShortArray(totalSamples)
        notes.forEachIndexed { idx, freq ->
            val offset = (idx * 95 * sampleRate) / 1000
            val note = generateNote(freq, 2.5, 0.32, 230, 7.5)
            mixInto(buffer, note, offset, 0.56f)
        }
        return buffer
    }

    // 9. CARD FLIP REVEAL: Crisp card snap + radiant triumphant chord reveal
    private fun synthesizeCardFlipRevealPcm(): ShortArray {
        val notes = listOf(523.25, 659.25, 783.99, 987.77, 1318.51)
        val totalSamples = (sampleRate * 620) / 1000
        val buffer = ShortArray(totalSamples)
        notes.forEachIndexed { idx, freq ->
            val offset = (idx * 55 * sampleRate) / 1000
            val note = generateNote(freq, 2.0, 0.36, 260, 6.8)
            mixInto(buffer, note, offset, 0.60f)
        }
        return buffer
    }

    private fun generateNote(
        freqHz: Double,
        harmonicRatio: Double,
        harmonicAmp: Double,
        durationMs: Int,
        decayRate: Double
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val out = ShortArray(count)
        val attackSamples = (sampleRate * 6) / 1000
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
            val env = attack * exp(-decayRate * t)
            val fundamental = sin(2.0 * PI * freqHz * t)
            val overtone = sin(2.0 * PI * (freqHz * harmonicRatio) * t) * harmonicAmp
            val value = ((fundamental + overtone) / (1.0 + harmonicAmp)) * env
            out[i] = (value * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    private fun mixInto(target: ShortArray, source: ShortArray, startOffset: Int, gain: Float) {
        for (i in source.indices) {
            val idx = startOffset + i
            if (idx in target.indices) {
                val mixed = target[idx].toInt() + (source[i].toInt() * gain).toInt()
                target[idx] = mixed.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
        }
    }

    private fun writeWavFile(targetFile: File, pcmData: ShortArray, sampleRateHz: Int) {
        val byteDataSize = pcmData.size * 2
        val totalDataLen = byteDataSize + 36
        val byteRate = sampleRateHz * 2

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(totalDataLen)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRateHz)
            putInt(byteRate)
            putShort(2)
            putShort(16)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(byteDataSize)
        }

        val pcmBuffer = ByteBuffer.allocate(byteDataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in pcmData) {
            pcmBuffer.putShort(sample)
        }

        FileOutputStream(targetFile).use { fos ->
            fos.write(header.array())
            fos.write(pcmBuffer.array())
            fos.flush()
        }
    }

    private fun vibrateShort(ms: Long) {
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(ms)
            }
        } catch (_: Exception) {
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            val v = vibrator ?: return
            if (!v.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, -1)
            }
        } catch (_: Exception) {
        }
    }
}
