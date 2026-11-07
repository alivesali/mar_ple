package com.example.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.BoardTheme
import com.example.model.MusicGenre
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Metadata describing the musical score and ambient soundscape of each [BoardTheme].
 */
data class ThemeSoundscapeInfo(
    val theme: BoardTheme,
    val musicTitle: String,
    val ambientTitle: String,
    val instrumentsLabel: String,
    val badgeEmoji: String
)

fun BoardTheme.soundscapeInfo(): ThemeSoundscapeInfo = when (this) {
    BoardTheme.JUNGLE -> ThemeSoundscapeInfo(
        theme = this,
        musicTitle = "ملودی کالیمبا و ماریمبای جنگل استوایی",
        ambientTitle = "آواز پرندگان، خش‌خش برگ‌ها و نسیم جنگل",
        instrumentsLabel = "کالیمبا • فلوت چوبی • پرندگان استوایی",
        badgeEmoji = "🌴🐦"
    )
    BoardTheme.OCEAN_BEACH -> ThemeSoundscapeInfo(
        theme = this,
        musicTitle = "آرپژ چنگ ساحلی و گیتار آرام اقیانوس",
        ambientTitle = "موج‌های ملایم ساحل، کف دریا و مرغان دریایی",
        instrumentsLabel = "چنگ آبی • استیل‌پن • امواج ساحل",
        badgeEmoji = "🌊🐚"
    )
    BoardTheme.DESERT -> ThemeSoundscapeInfo(
        theme = this,
        musicTitle = "نوای عود شرقی و نی اسرارآمیز کویر",
        ambientTitle = "نسیم گرم تپه‌های شنی و زنگوله کاروان",
        instrumentsLabel = "عود شرقی • نی کویری • باد شنزار",
        badgeEmoji = "🏜️🐪"
    )
    BoardTheme.ARCTIC_ICE -> ThemeSoundscapeInfo(
        theme = this,
        musicTitle = "سلستا و زنگوله‌های بلورین شفق قطبی",
        ambientTitle = "باد ملایم قطبی و تلالو کریستال‌های یخ",
        instrumentsLabel = "هارپ شیشه‌ای • پد شفق • بلور یخ",
        badgeEmoji = "❄️✨"
    )
    BoardTheme.BLACK_GOLD -> ThemeSoundscapeInfo(
        theme = this,
        musicTitle = "سمفونی چنگ طلایی و تالار سلطنتی",
        ambientTitle = "طنین تالار ابسیدین و درخشش ذرات طلا",
        instrumentsLabel = "چنگ طلایی • ویولنسل • زنگ سلطنتی",
        badgeEmoji = "👑🎻"
    )
}

/**
 * Real-time Procedural Background Music & Ambient Soundscape Engine.
 *
 * Features:
 * 1. Supports all 9 dedicated [MusicGenre] compositions (Heroic, Victory, Mythological, Fantasy,
 *    Dark Epic, Royal, Adventure, Ambient, Cyber) as well as [MusicGenre.AUTO_BOARD_THEME].
 * 2. Single-instance playback guarantee: never plays two tracks simultaneously without crossfading.
 * 3. Smooth equal-power Fade In / Fade Out (~1.1s) when switching genres, previewing tracks in the Studio,
 *    or toggling music on/off.
 * 4. Temporary audio ducking when major board sound effects trigger.
 */
class ThemeMusicManager {

    private val sampleRate = 24000
    private val chunkFrames = 1200 // 50ms stereo buffer per iteration

    @Volatile
    var isMusicEnabled: Boolean = true

    @Volatile
    var isAmbientEnabled: Boolean = true

    @Volatile
    var musicVolume: Float = 0.68f

    @Volatile
    var ambientVolume: Float = 0.64f

    @Volatile
    private var isPausedByLifecycle: Boolean = false

    @Volatile
    private var isReleased: Boolean = false

    @Volatile
    private var activeTheme: BoardTheme = BoardTheme.BLACK_GOLD

    @Volatile
    private var selectedGenre: MusicGenre = MusicGenre.ROYAL

    @Volatile
    private var previewGenre: MusicGenre? = null

    @Volatile
    private var victoryMusicOverride: Boolean = false

    private data class PlaybackSource(
        val genre: MusicGenre,
        val theme: BoardTheme
    )

    @Volatile
    private var currentSource = PlaybackSource(MusicGenre.ROYAL, BoardTheme.BLACK_GOLD)

    @Volatile
    private var previousSource = PlaybackSource(MusicGenre.ROYAL, BoardTheme.BLACK_GOLD)

    @Volatile
    private var crossfadeProgress: Float = 1.0f // 0f = previousSource, 1f = currentSource

    @Volatile
    private var duckingUntilEpochMs: Long = 0L

    private var audioThread: Thread? = null

    private val generatorStateA = ThemeSynthState()
    private val generatorStateB = ThemeSynthState()

    init {
        startEngineThread()
    }

    private fun resolveTargetSource(): PlaybackSource {
        val effectiveGenre = when {
            previewGenre != null -> previewGenre!!
            victoryMusicOverride -> MusicGenre.VICTORY
            else -> selectedGenre
        }
        return PlaybackSource(effectiveGenre, activeTheme)
    }

    @Synchronized
    private fun updatePlaybackSourceIfNeeded() {
        val target = resolveTargetSource()
        if (target != currentSource) {
            previousSource = currentSource
            currentSource = target
            crossfadeProgress = 0.0f
        }
    }

    fun setTheme(theme: BoardTheme) {
        if (theme == activeTheme) return
        activeTheme = theme
        updatePlaybackSourceIfNeeded()
    }

    fun getCurrentTheme(): BoardTheme = activeTheme

    fun setMusicGenre(genre: MusicGenre) {
        selectedGenre = genre
        victoryMusicOverride = false
        previewGenre = null
        updatePlaybackSourceIfNeeded()
    }

    fun getSelectedMusicGenre(): MusicGenre = selectedGenre

    fun startPreviewGenre(genre: MusicGenre?) {
        previewGenre = genre
        updatePlaybackSourceIfNeeded()
    }

    fun getPreviewGenre(): MusicGenre? = previewGenre

    fun setVictoryMusicOverride(active: Boolean) {
        if (victoryMusicOverride == active) return
        victoryMusicOverride = active
        updatePlaybackSourceIfNeeded()
    }

    /**
     * Briefly lowers background music volume so dramatic sound effects stand out.
     */
    fun triggerDucking(durationMs: Long = 900L) {
        val target = System.currentTimeMillis() + durationMs
        if (target > duckingUntilEpochMs) {
            duckingUntilEpochMs = target
        }
    }

    fun pauseForLifecycle() {
        isPausedByLifecycle = true
    }

    fun resumeForLifecycle() {
        isPausedByLifecycle = false
    }

    fun release() {
        isReleased = true
        try {
            audioThread?.interrupt()
        } catch (_: Exception) {
        }
        audioThread = null
    }

    private fun startEngineThread() {
        val thread = Thread({
            var track: AudioTrack? = null
            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(chunkFrames * 4)

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()

                track = AudioTrack.Builder()
                    .setAudioAttributes(attributes)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(minBufSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                val stereoBuffer = ShortArray(chunkFrames * 2)
                var globalFrameIndex = 0L
                var currentDuckGain = 1.0f
                var smoothMusicGain = 0.0f // Starts at 0 for smooth initial fade-in
                var smoothAmbientGain = 0.0f

                while (!isReleased && !Thread.currentThread().isInterrupted) {
                    val isPreviewActive = previewGenre != null
                    val wantMusic = !isPausedByLifecycle && (isMusicEnabled || isPreviewActive)
                    val wantAmbient = !isPausedByLifecycle && isAmbientEnabled

                    if (!wantMusic && !wantAmbient && smoothMusicGain < 0.002f && smoothAmbientGain < 0.002f) {
                        Thread.sleep(75L)
                        continue
                    }

                    val now = System.currentTimeMillis()
                    val targetDuck = if (now < duckingUntilEpochMs && !isPreviewActive) 0.34f else 1.0f
                    val targetMusicVol = if (wantMusic) {
                        if (isPreviewActive) musicVolume.coerceAtLeast(0.55f).coerceIn(0f, 1f)
                        else musicVolume.coerceIn(0f, 1f)
                    } else 0f
                    val targetAmbientVol = if (wantAmbient) ambientVolume.coerceIn(0f, 1f) else 0f

                    val curSrc = currentSource
                    val prevSrc = previousSource
                    var xfade = crossfadeProgress

                    for (frame in 0 until chunkFrames) {
                        val t = globalFrameIndex.toDouble() / sampleRate
                        globalFrameIndex++

                        currentDuckGain += (targetDuck - currentDuckGain) * 0.0025f
                        // Smooth Fade-In / Fade-Out envelope on enable/disable or volume change
                        smoothMusicGain += (targetMusicVol - smoothMusicGain) * 0.0018f
                        smoothAmbientGain += (targetAmbientVol - smoothAmbientGain) * 0.0018f

                        val samplePair: StereoSample = if (xfade < 0.999f && prevSrc != curSrc) {
                            xfade = (xfade + 1.0f / (sampleRate * 1.10f)).coerceAtMost(1.0f)
                            val prevSample = synthesizeSourceFrame(
                                prevSrc,
                                t,
                                generatorStateA,
                                smoothMusicGain,
                                smoothAmbientGain
                            )
                            val nextSample = synthesizeSourceFrame(
                                curSrc,
                                t,
                                generatorStateB,
                                smoothMusicGain,
                                smoothAmbientGain
                            )
                            val fadeOut = cos(xfade * PI * 0.5).toFloat()
                            val fadeIn = sin(xfade * PI * 0.5).toFloat()
                            StereoSample(
                                left = prevSample.left * fadeOut + nextSample.left * fadeIn,
                                right = prevSample.right * fadeOut + nextSample.right * fadeIn
                            )
                        } else {
                            synthesizeSourceFrame(
                                curSrc,
                                t,
                                generatorStateB,
                                smoothMusicGain,
                                smoothAmbientGain
                            )
                        }

                        val finalLeft = (samplePair.left * currentDuckGain * 0.42f)
                            .coerceIn(-0.95f, 0.95f)
                        val finalRight = (samplePair.right * currentDuckGain * 0.42f)
                            .coerceIn(-0.95f, 0.95f)

                        stereoBuffer[frame * 2] = (finalLeft * Short.MAX_VALUE).toInt().toShort()
                        stereoBuffer[frame * 2 + 1] = (finalRight * Short.MAX_VALUE).toInt().toShort()
                    }

                    crossfadeProgress = xfade
                    track.write(stereoBuffer, 0, stereoBuffer.size)
                }
            } catch (_: Exception) {
                // Ignore AudioTrack hardware exceptions on headless CI / emulators without audio sink
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }, "MysticThemeMusicThread")

        thread.isDaemon = true
        thread.priority = Thread.NORM_PRIORITY - 1
        thread.start()
        audioThread = thread
    }

    private data class StereoSample(val left: Float, val right: Float)

    private class ThemeSynthState {
        val rng = Random(101)
        var lpNoiseLeft = 0.0
        var lpNoiseRight = 0.0
        var birdPhase = 0.0
        var melodyPhase = 0.0
        var subPhase = 0.0
    }

    private fun synthesizeSourceFrame(
        source: PlaybackSource,
        t: Double,
        state: ThemeSynthState,
        musicVol: Float,
        ambientVol: Float
    ): StereoSample {
        // 1. Synthesize Music Layer according to selected MusicGenre (or BoardTheme when AUTO_BOARD_THEME)
        val musicPair = if (musicVol > 0.001f) {
            when (source.genre) {
                MusicGenre.AUTO_BOARD_THEME -> synthesizeBoardThemeMusicOnly(source.theme, t, state, musicVol)
                MusicGenre.ROYAL -> synthesizeRoyalGenreMusic(t, musicVol)
                MusicGenre.HEROIC -> synthesizeHeroicGenreMusic(t, state, musicVol)
                MusicGenre.VICTORY -> synthesizeVictoryGenreMusic(t, musicVol)
                MusicGenre.MYTHOLOGICAL -> synthesizeMythologicalGenreMusic(t, state, musicVol)
                MusicGenre.FANTASY -> synthesizeFantasyGenreMusic(t, musicVol)
                MusicGenre.DARK_EPIC -> synthesizeDarkEpicGenreMusic(t, state, musicVol)
                MusicGenre.ADVENTURE -> synthesizeAdventureGenreMusic(t, state, musicVol)
                MusicGenre.AMBIENT -> synthesizeAmbientGenreMusic(t, musicVol)
                MusicGenre.CYBER -> synthesizeCyberGenreMusic(t, state, musicVol)
            }
        } else {
            StereoSample(0f, 0f)
        }

        // 2. Synthesize Gentle Environmental Ambient Layer according to active BoardTheme
        val ambientPair = if (ambientVol > 0.001f) {
            synthesizeAmbientLayerOnly(source.theme, t, state, ambientVol)
        } else {
            StereoSample(0f, 0f)
        }

        return StereoSample(
            left = musicPair.left + ambientPair.left,
            right = musicPair.right + ambientPair.right
        )
    }

    private fun synthesizeBoardThemeMusicOnly(
        theme: BoardTheme,
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        return when (theme) {
            BoardTheme.JUNGLE -> synthesizeJungleMusicOnly(t, musicVol)
            BoardTheme.OCEAN_BEACH -> synthesizeOceanMusicOnly(t, musicVol)
            BoardTheme.DESERT -> synthesizeDesertMusicOnly(t, st, musicVol)
            BoardTheme.ARCTIC_ICE -> synthesizeArcticMusicOnly(t, musicVol)
            BoardTheme.BLACK_GOLD -> synthesizeRoyalGenreMusic(t, musicVol)
        }
    }

    // =========================================================================
    // 9 DEDICATED MUSICAL GENRES (SYNTHESIZED COMPOSITIONS)
    // =========================================================================

    /**
     * 1. ROYAL (سلطنتی):
     * Noble classical Golden Harp, Harpsichord & Velvet Cello in D Dorian / Royal Court.
     */
    private fun synthesizeRoyalGenreMusic(t: Double, musicVol: Float): StereoSample {
        val royalScale = doubleArrayOf(
            146.83, 174.61, 220.00, 261.63, 293.66, 349.23, 440.00, 523.25, 587.33
        )
        val royalPattern = intArrayOf(0, 2, 4, 5, 6, 4, 3, 2, 1, 3, 5, 8, 6, 5, 4, 2)
        val stepDur = 0.50
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = royalScale[royalPattern[(stepIdx % royalPattern.size).toInt()]]

        val harpEnv = exp(-5.0 * localT) * (1.0 - exp(-160.0 * localT))
        val goldenHarp = (sin(2.0 * PI * freq * localT) * 0.72 +
            sin(2.0 * PI * freq * 2.0 * localT) * 0.22 +
            sin(2.0 * PI * freq * 3.0 * localT) * 0.06) * harpEnv * 0.58

        val velvetPad = (sin(2.0 * PI * 73.42 * t) * 0.38 +
            sin(2.0 * PI * 146.83 * t) * 0.30 +
            sin(2.0 * PI * 220.00 * t) * 0.22) * 0.28

        val pan = 0.5 + 0.3 * sin(stepIdx * 0.85)
        val l = (goldenHarp * pan + velvetPad) * musicVol
        val r = (goldenHarp * (1.0 - pan) + velvetPad) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 2. HEROIC (قهرمانی):
     * Epic cinematic orchestral brass horns, heroic string ostinato, and driving war drums.
     */
    private fun synthesizeHeroicGenreMusic(
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        // Heroic D minor -> Bb major -> F major -> C major anthem
        val heroicScale = doubleArrayOf(
            146.83, 174.61, 196.00, 220.00, 233.08, 261.63, 293.66, 349.23, 392.00, 440.00, 587.33
        )
        val pattern = intArrayOf(0, 3, 6, 7, 9, 6, 7, 5, 4, 6, 8, 10, 9, 8, 6, 3)
        val stepDur = 0.38 // Energetic ~124 BPM march
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = heroicScale[pattern[(stepIdx % pattern.size).toInt()]]

        // Warm cinematic French Horn / Brass synthesis (rich odd & even harmonics with brass swell)
        val brassAttack = 1.0 - exp(-45.0 * localT)
        val brassDecay = exp(-3.4 * localT)
        val brassEnv = brassAttack * brassDecay
        val horn = (sin(2.0 * PI * freq * localT) * 0.52 +
            sin(2.0 * PI * freq * 2.0 * localT) * 0.30 +
            sin(2.0 * PI * freq * 3.0 * localT) * 0.18) * brassEnv * 0.58

        // Driving timpani / war drum on every 2nd step
        var drum = 0.0
        if (stepIdx % 2L == 0L && localT < 0.24) {
            val pitchDrop = 98.0 * exp(-14.0 * localT) + 52.0
            st.subPhase += (2.0 * PI * pitchDrop) / sampleRate
            val drumEnv = exp(-12.0 * localT)
            drum = sin(st.subPhase) * drumEnv * 0.42
        }

        // Heroic low strings fifths pad (D2 + A2 + D3)
        val stringPad = (sin(2.0 * PI * 73.42 * t) * 0.40 +
            sin(2.0 * PI * 110.00 * t) * 0.34 +
            sin(2.0 * PI * 146.83 * t) * 0.26) * 0.28

        val pan = 0.5 + 0.28 * sin(stepIdx * 0.75)
        val l = (horn * pan + drum + stringPad) * musicVol
        val r = (horn * (1.0 - pan) + drum + stringPad) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 3. VICTORY (پیروزی):
     * Triumphant, radiant major fanfare with celebratory brass trumpets, bells, and uplifting chords.
     */
    private fun synthesizeVictoryGenreMusic(t: Double, musicVol: Float): StereoSample {
        // C Major / G Major / F Major radiant celebratory fanfare
        val victoryNotes = doubleArrayOf(
            261.63, 329.63, 392.00, 493.88, 523.25, 587.33, 659.25, 783.99, 1046.50
        )
        val melody = intArrayOf(0, 2, 4, 6, 7, 6, 4, 7, 5, 7, 8, 7, 6, 4, 5, 4)
        val stepDur = 0.32
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = victoryNotes[melody[(stepIdx % melody.size).toInt()]]

        // Bright celebratory trumpet + golden glockenspiel bell layer
        val env = exp(-4.2 * localT) * (1.0 - exp(-190.0 * localT))
        val trumpetBell = (sin(2.0 * PI * freq * localT) * 0.56 +
            sin(2.0 * PI * freq * 2.0 * localT) * 0.28 +
            sin(2.0 * PI * freq * 4.0 * localT) * exp(-10.0 * localT) * 0.16) * env * 0.60

        // Uplifting major triad pad (C3 + E3 + G3 + C4)
        val pad = (sin(2.0 * PI * 130.81 * t) * 0.34 +
            sin(2.0 * PI * 164.81 * t) * 0.26 +
            sin(2.0 * PI * 196.00 * t) * 0.24 +
            sin(2.0 * PI * 261.63 * t) * 0.16) * 0.30

        val pan = 0.5 + 0.35 * cos(stepIdx * 0.9)
        val l = (trumpetBell * pan + pad) * musicVol
        val r = (trumpetBell * (1.0 - pan) + pad) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 4. MYTHOLOGICAL (اسطوره‌ای):
     * Ancient Greek / Persian / Mediterranean mystical scale on Lyre, Oud, and Temple Choir Drone.
     */
    private fun synthesizeMythologicalGenreMusic(
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        // Byzantine / Ancient Double Harmonic & Phrygian Dominant scale
        val mythScale = doubleArrayOf(
            164.81, 174.61, 207.65, 220.00, 246.94, 261.63, 311.13, 329.63, 415.30, 493.88
        )
        val pattern = intArrayOf(0, 2, 3, 4, 6, 7, 6, 4, 3, 2, 1, 0, 4, 7, 8, 7)
        val stepDur = 0.48
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = mythScale[pattern[(stepIdx % pattern.size).toInt()]]

        // Plucked Ancient Lyre & Oud with subtle mystical vibrato
        val vibrato = sin(2.0 * PI * 5.5 * t) * 1.3
        st.melodyPhase += (2.0 * PI * (freq + vibrato)) / sampleRate
        val pluckEnv = exp(-5.6 * localT) * (1.0 - exp(-180.0 * localT))
        val lyre = (sin(st.melodyPhase) * 0.62 +
            sin(st.melodyPhase * 2.0) * 0.25 +
            sin(st.melodyPhase * 3.0) * 0.13) * pluckEnv * 0.58

        // Ancient Temple Choir & Drone (E2 + B2 + E3)
        val choirSwell = 0.55 + 0.45 * sin(2.0 * PI * 0.14 * t)
        val choir = (sin(2.0 * PI * 82.41 * t) * 0.42 +
            sin(2.0 * PI * 123.47 * t) * 0.34 +
            sin(2.0 * PI * 164.81 * t) * 0.24) * choirSwell * 0.32

        val pan = 0.5 + 0.32 * sin(stepIdx * 0.8)
        val l = (lyre * pan + choir) * musicVol
        val r = (lyre * (1.0 - pan) + choir) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 5. FANTASY (فانتزی):
     * Enchanted Lydian Celesta, Silver Flute & Shimmering Fairy Harp.
     */
    private fun synthesizeFantasyGenreMusic(t: Double, musicVol: Float): StereoSample {
        // F Lydian (F, G, A, B, C, D, E, F) - magical, wondrous dreamlike scale
        val lydianScale = doubleArrayOf(
            349.23, 392.00, 440.00, 493.88, 523.25, 587.33, 659.25, 698.46, 880.00, 1046.50
        )
        val pattern = intArrayOf(0, 2, 3, 4, 6, 7, 8, 6, 4, 3, 2, 4, 5, 7, 9, 7)
        val stepDur = 0.44
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = lydianScale[pattern[(stepIdx % pattern.size).toInt()]]

        val celestaEnv = exp(-4.8 * localT) * (1.0 - exp(-210.0 * localT))
        val celesta = (sin(2.0 * PI * freq * localT) * 0.70 +
            sin(2.0 * PI * freq * 2.0 * localT) * exp(-7.0 * localT) * 0.22 +
            sin(2.0 * PI * freq * 3.0 * localT) * exp(-12.0 * localT) * 0.08) * celestaEnv * 0.54

        val shimmer = 0.55 + 0.45 * sin(2.0 * PI * 0.18 * t)
        val fairyPad = (sin(2.0 * PI * 174.61 * t) * 0.36 +
            sin(2.0 * PI * 220.00 * t) * 0.32 +
            sin(2.0 * PI * 329.63 * t) * 0.22) * shimmer * 0.28

        val pan = 0.5 + 0.40 * sin(stepIdx * 1.15)
        val l = (celesta * pan + fairyPad) * musicVol
        val r = (celesta * (1.0 - pan) + fairyPad) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 6. DARK EPIC (حماسی تاریک):
     * Deep, heavy cinematic C harmonic minor / Phrygian bass ostinato, dark brass & thunderous drums.
     */
    private fun synthesizeDarkEpicGenreMusic(
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        // C Harmonic Minor / Phrygian low register
        val darkScale = doubleArrayOf(
            65.41, 77.78, 98.00, 130.81, 138.59, 155.56, 196.00, 207.65, 246.94, 261.63
        )
        val pattern = intArrayOf(3, 5, 6, 7, 6, 5, 4, 3, 0, 3, 6, 8, 9, 7, 6, 4)
        val stepDur = 0.54
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = darkScale[pattern[(stepIdx % pattern.size).toInt()]]

        // Heavy Dark Cello / Low Brass saw-like harmonic blend
        val env = exp(-3.2 * localT) * (1.0 - exp(-80.0 * localT))
        val darkBrass = (sin(2.0 * PI * freq * localT) * 0.50 +
            sin(2.0 * PI * freq * 2.0 * localT) * 0.30 +
            sin(2.0 * PI * freq * 3.0 * localT) * 0.14 +
            sin(2.0 * PI * freq * 4.0 * localT) * 0.06) * env * 0.62

        // Deep hall impact boom every 4 steps
        var impact = 0.0
        if (stepIdx % 4L == 0L && localT < 0.38) {
            val dropFreq = 72.0 * exp(-9.0 * localT) + 38.0
            st.subPhase += (2.0 * PI * dropFreq) / sampleRate
            impact = sin(st.subPhase) * exp(-7.5 * localT) * 0.45
        }

        // Ominous sub drone (C2 + G2)
        val drone = (sin(2.0 * PI * 65.41 * t) * 0.55 +
            sin(2.0 * PI * 98.00 * t) * 0.35) * 0.28

        val pan = 0.5 + 0.25 * sin(stepIdx * 0.6)
        val l = (darkBrass * pan + impact + drone) * musicVol
        val r = (darkBrass * (1.0 - pan) + impact + drone) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 7. ADVENTURE (ماجراجویی):
     * Lively, rhythmic Mixolydian marimba, agile pizzicato strings & upbeat percussion.
     */
    private fun synthesizeAdventureGenreMusic(
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        // G Mixolydian upbeat adventure scale
        val advScale = doubleArrayOf(
            196.00, 220.00, 246.94, 261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 587.33
        )
        val pattern = intArrayOf(0, 2, 4, 7, 5, 7, 8, 7, 4, 2, 3, 6, 4, 2, 1, 0)
        val stepDur = 0.30 // Brisk, exciting rhythm
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = advScale[pattern[(stepIdx % pattern.size).toInt()]]

        val pluckEnv = exp(-9.0 * localT) * (1.0 - exp(-200.0 * localT))
        val pizzicato = (sin(2.0 * PI * freq * localT) * 0.72 +
            sin(2.0 * PI * freq * 2.0 * localT) * 0.20 +
            sin(2.0 * PI * freq * 3.0 * localT) * 0.08) * pluckEnv * 0.58

        // Light shaker / hand-drum groove
        var groove = 0.0
        if (localT < 0.08) {
            val grooveFreq = if (stepIdx % 2L == 0L) 140.0 else 280.0
            st.subPhase += (2.0 * PI * grooveFreq) / sampleRate
            groove = sin(st.subPhase) * exp(-28.0 * localT) * 0.22
        }

        val warmPad = (sin(2.0 * PI * 98.00 * t) * 0.45 +
            sin(2.0 * PI * 146.83 * t) * 0.35) * 0.24

        val pan = 0.5 + 0.36 * sin(stepIdx * 1.1)
        val l = (pizzicato * pan + groove + warmPad) * musicVol
        val r = (pizzicato * (1.0 - pan) + groove + warmPad) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 8. AMBIENT (آرامش‌بخش):
     * Fatigue-free, ultra-soothing warm silk pad, slow acoustic droplets & gentle wind chimes.
     */
    private fun synthesizeAmbientGenreMusic(t: Double, musicVol: Float): StereoSample {
        val calmScale = doubleArrayOf(
            174.61, 220.00, 261.63, 329.63, 392.00, 440.00, 523.25
        )
        val pattern = intArrayOf(0, 2, 3, 4, 6, 4, 3, 1)
        val stepDur = 0.85 // Slow, meditative pace
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = calmScale[pattern[(stepIdx % pattern.size).toInt()]]

        // Soft felt-piano / droplet note with gentle attack
        val attack = 1.0 - exp(-35.0 * localT)
        val decay = exp(-2.8 * localT)
        val softPiano = (sin(2.0 * PI * freq * localT) * 0.82 +
            sin(2.0 * PI * freq * 2.0 * localT) * exp(-6.0 * localT) * 0.18) * attack * decay * 0.42

        // Breathing warm silk pad (Fmaj7)
        val breathL = 0.55 + 0.45 * sin(2.0 * PI * 0.09 * t)
        val breathR = 0.55 + 0.45 * cos(2.0 * PI * 0.09 * t)
        val padBase = sin(2.0 * PI * 87.31 * t) * 0.36 +
            sin(2.0 * PI * 130.81 * t) * 0.30 +
            sin(2.0 * PI * 164.81 * t) * 0.22

        val pan = 0.5 + 0.3 * sin(stepIdx * 0.7)
        val l = (softPiano * pan + padBase * breathL * 0.32) * musicVol
        val r = (softPiano * (1.0 - pan) + padBase * breathR * 0.32) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    /**
     * 9. CYBER (آینده‌نگر):
     * Sci-Fi / Cyberpunk neon synthesizer arpeggiator with pulsing sub-bass and harmonic sweep.
     */
    private fun synthesizeCyberGenreMusic(
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        // A Dorian / Cyberpunk minor synth arpeggio
        val cyberScale = doubleArrayOf(
            110.00, 130.81, 146.83, 164.81, 196.00, 220.00, 261.63, 293.66, 329.63, 392.00, 440.00
        )
        val arpPattern = intArrayOf(0, 5, 7, 8, 5, 7, 10, 8, 1, 6, 8, 9, 6, 8, 10, 7)
        val stepDur = 0.24 // 16th-note synthwave arpeggiator
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = cyberScale[arpPattern[(stepIdx % arpPattern.size).toInt()]]

        // Dynamic filter-swept synth lead (fundamental + 2nd, 3rd, 4th harmonics modulated by LFO)
        val filterMod = 0.35 + 0.65 * ((sin(2.0 * PI * 0.22 * t) + 1.0) * 0.5)
        val synthEnv = exp(-7.5 * localT) * (1.0 - exp(-240.0 * localT))
        val neonSynth = (sin(2.0 * PI * freq * localT) * 0.48 +
            sin(2.0 * PI * freq * 2.0 * localT) * (0.28 * filterMod) +
            sin(2.0 * PI * freq * 3.0 * localT) * (0.16 * filterMod) +
            sin(2.0 * PI * freq * 4.0 * localT) * (0.08 * filterMod)) * synthEnv * 0.55

        // Pulsing sci-fi sub-bass (A1 = 55Hz / F1 = 43.65Hz)
        val bassRoot = if ((stepIdx / 8) % 2L == 0L) 55.0 else 43.65
        st.subPhase += (2.0 * PI * bassRoot) / sampleRate
        val pulseEnv = exp(-5.0 * localT)
        val subBass = (sin(st.subPhase) * 0.75 + sin(st.subPhase * 2.0) * 0.25) * pulseEnv * 0.34

        val pan = 0.5 + 0.42 * sin(stepIdx * 1.4)
        val l = (neonSynth * pan + subBass) * musicVol
        val r = (neonSynth * (1.0 - pan) + subBass) * musicVol
        return StereoSample(l.toFloat(), r.toFloat())
    }

    // =========================================================================
    // BOARD THEME SPECIFIC MUSIC & AMBIENT SOUNDSCAPE GENERATORS
    // =========================================================================

    private fun synthesizeJungleMusicOnly(t: Double, musicVol: Float): StereoSample {
        val stepDur = 0.42
        val stepIndex = (t / stepDur).toLong()
        val stepTime = t % stepDur
        val pentatonicScale = doubleArrayOf(
            261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25
        )
        val pattern = intArrayOf(0, 2, 4, 5, 4, 2, 3, 1, 0, 3, 4, 7, 5, 4, 2, 1)
        val noteFreq = pentatonicScale[pattern[(stepIndex % pattern.size).toInt()]]

        val env = exp(-8.5 * stepTime) * (1.0 - exp(-180.0 * stepTime))
        val marimba = (sin(2.0 * PI * noteFreq * stepTime) * 0.78 +
            sin(2.0 * PI * noteFreq * 4.0 * stepTime) * exp(-28.0 * stepTime) * 0.22) * env

        val breath = 0.55 + 0.45 * sin(2.0 * PI * 0.12 * t)
        val pad = (sin(2.0 * PI * 130.81 * t) * 0.35 +
            sin(2.0 * PI * 196.00 * t) * 0.30 +
            sin(2.0 * PI * 329.63 * t) * 0.20) * breath * 0.26

        val pan = 0.5 + 0.35 * sin(stepIndex * 0.9)
        return StereoSample(
            ((marimba * pan + pad) * musicVol).toFloat(),
            ((marimba * (1.0 - pan) + pad) * musicVol).toFloat()
        )
    }

    private fun synthesizeOceanMusicOnly(t: Double, musicVol: Float): StereoSample {
        val noteDur = 0.48
        val noteIdx = (t / noteDur).toLong()
        val localT = t % noteDur
        val chordProgression = arrayOf(
            doubleArrayOf(174.61, 220.00, 261.63, 329.63, 392.00, 523.25),
            doubleArrayOf(130.81, 196.00, 246.94, 293.66, 392.00, 493.88),
            doubleArrayOf(196.00, 246.94, 293.66, 329.63, 392.00, 587.33)
        )
        val barIdx = ((noteIdx / 6) % chordProgression.size).toInt()
        val arpStep = (noteIdx % 6).toInt()
        val freq = chordProgression[barIdx][arpStep]

        val pluckEnv = exp(-5.2 * localT) * (1.0 - exp(-140.0 * localT))
        val harp = (sin(2.0 * PI * freq * localT) * 0.72 +
            sin(2.0 * PI * freq * 2.0 * localT) * exp(-10.0 * localT) * 0.22 +
            sin(2.0 * PI * freq * 3.0 * localT) * exp(-18.0 * localT) * 0.06) * pluckEnv

        val rootFreq = chordProgression[barIdx][0]
        val pad = (sin(2.0 * PI * rootFreq * t) * 0.35 +
            sin(2.0 * PI * rootFreq * 1.5 * t) * 0.25) * 0.25

        val stereoSpread = 0.5 + 0.38 * sin(arpStep * 1.05)
        return StereoSample(
            ((harp * stereoSpread + pad) * musicVol).toFloat(),
            ((harp * (1.0 - stereoSpread) + pad) * musicVol).toFloat()
        )
    }

    private fun synthesizeDesertMusicOnly(
        t: Double,
        st: ThemeSynthState,
        musicVol: Float
    ): StereoSample {
        val hijazScale = doubleArrayOf(
            146.83, 155.56, 185.00, 196.00, 220.00, 233.08, 261.63, 293.66, 311.13, 369.99, 440.00
        )
        val melodyPattern = intArrayOf(0, 2, 3, 4, 5, 4, 3, 2, 4, 7, 6, 4, 3, 2, 1, 0)
        val stepDur = 0.46
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = hijazScale[melodyPattern[(stepIdx % melodyPattern.size).toInt()]]

        val vibrato = sin(2.0 * PI * 5.8 * t) * 1.4
        val instFreq = freq + vibrato
        st.melodyPhase += (2.0 * PI * instFreq) / sampleRate
        val oudEnv = exp(-6.2 * localT) * (1.0 - exp(-190.0 * localT))
        val oud = (sin(st.melodyPhase) * 0.65 +
            sin(st.melodyPhase * 2.0) * 0.24 +
            sin(st.melodyPhase * 3.0) * 0.11) * oudEnv

        val neySwell = (sin(2.0 * PI * 0.16 * t) + 1.0) * 0.5
        val drone = (sin(2.0 * PI * 73.42 * t) * 0.38 +
            sin(2.0 * PI * 110.00 * t) * 0.28 +
            sin(2.0 * PI * 293.66 * t) * neySwell * 0.16) * 0.30

        val out = ((oud * 0.65 + drone) * musicVol).toFloat()
        return StereoSample(out, out)
    }

    private fun synthesizeArcticMusicOnly(t: Double, musicVol: Float): StereoSample {
        val iceScale = doubleArrayOf(
            440.00, 493.88, 523.25, 587.33, 659.25, 783.99, 880.00, 987.77, 1046.50
        )
        val icePattern = intArrayOf(0, 2, 4, 7, 6, 4, 2, 1, 0, 4, 6, 8, 6, 4, 3, 2)
        val stepDur = 0.52
        val stepIdx = (t / stepDur).toLong()
        val localT = t % stepDur
        val freq = iceScale[icePattern[(stepIdx % icePattern.size).toInt()]]

        val celestaEnv = exp(-4.5 * localT) * (1.0 - exp(-220.0 * localT))
        val celesta = (sin(2.0 * PI * freq * localT) * 0.72 +
            sin(2.0 * PI * freq * 2.0 * localT) * exp(-8.0 * localT) * 0.28) * celestaEnv * 0.55

        val auroraShimmer = 0.55 + 0.45 * sin(2.0 * PI * 0.15 * t)
        val pad = (sin(2.0 * PI * 220.00 * t) * 0.34 +
            sin(2.0 * PI * 261.63 * t) * 0.28 +
            sin(2.0 * PI * 329.63 * t) * 0.24) * auroraShimmer * 0.28

        val pan = 0.5 + 0.4 * sin(stepIdx * 1.3)
        return StereoSample(
            ((celesta * pan + pad) * musicVol).toFloat(),
            ((celesta * (1.0 - pan) + pad) * musicVol).toFloat()
        )
    }

    private fun synthesizeAmbientLayerOnly(
        theme: BoardTheme,
        t: Double,
        st: ThemeSynthState,
        ambientVol: Float
    ): StereoSample {
        return when (theme) {
            BoardTheme.JUNGLE -> {
                val whiteL = st.rng.nextDouble(-1.0, 1.0)
                val whiteR = st.rng.nextDouble(-1.0, 1.0)
                val breezeSwell = 0.04 + 0.03 * sin(2.0 * PI * 0.18 * t)
                st.lpNoiseLeft += breezeSwell * (whiteL - st.lpNoiseLeft)
                st.lpNoiseRight += breezeSwell * (whiteR - st.lpNoiseRight)

                val birdCycle = t % 3.4
                var birdSignal = 0.0
                if (birdCycle < 0.42) {
                    val chirpLocal = birdCycle % 0.21
                    val isSecondChirp = birdCycle >= 0.21
                    val baseFreq = if (isSecondChirp) 2150.0 else 1780.0
                    val chirpFreq = baseFreq + 480.0 * sin(PI * (chirpLocal / 0.21))
                    st.birdPhase += (2.0 * PI * chirpFreq) / sampleRate
                    val birdEnv = sin(PI * (chirpLocal / 0.21))
                    birdSignal = sin(st.birdPhase) * birdEnv * 0.22
                }

                val cricketWindow = (sin(2.0 * PI * 0.4 * t) + 1.0) * 0.5
                val cricket = sin(2.0 * PI * 3800.0 * t) *
                    ((sin(2.0 * PI * 16.0 * t) + 1.0) * 0.5) * cricketWindow * 0.025

                StereoSample(
                    ((st.lpNoiseLeft * 0.32 + birdSignal * 0.75 + cricket) * ambientVol).toFloat(),
                    ((st.lpNoiseRight * 0.32 + birdSignal * 0.45 + cricket) * ambientVol).toFloat()
                )
            }
            BoardTheme.OCEAN_BEACH -> {
                val waveCycle = (t % 7.5) / 7.5
                val waveEnvelope = sin(PI * waveCycle) * sin(PI * waveCycle)
                val filterAlpha = 0.015 + 0.085 * waveEnvelope

                val whiteL = st.rng.nextDouble(-1.0, 1.0)
                val whiteR = st.rng.nextDouble(-1.0, 1.0)
                st.lpNoiseLeft += filterAlpha * (whiteL - st.lpNoiseLeft)
                st.lpNoiseRight += filterAlpha * (whiteR - st.lpNoiseRight)

                val surfVolume = 0.12 + 0.42 * waveEnvelope
                val gullCycle = t % 6.2
                var gullSignal = 0.0
                if (gullCycle in 1.2..1.65) {
                    val p = (gullCycle - 1.2) / 0.45
                    val gullFreq = 1180.0 + 220.0 * sin(PI * p) - 180.0 * p
                    st.birdPhase += (2.0 * PI * gullFreq) / sampleRate
                    gullSignal = sin(st.birdPhase) * sin(PI * p) * 0.11
                }

                StereoSample(
                    ((st.lpNoiseLeft * surfVolume + gullSignal * 0.55) * ambientVol).toFloat(),
                    ((st.lpNoiseRight * surfVolume + gullSignal * 0.80) * ambientVol).toFloat()
                )
            }
            BoardTheme.DESERT -> {
                val windMod = 0.5 + 0.5 * sin(2.0 * PI * 0.11 * t)
                val alpha = 0.022 + 0.045 * windMod
                val whiteL = st.rng.nextDouble(-1.0, 1.0)
                val whiteR = st.rng.nextDouble(-1.0, 1.0)
                st.lpNoiseLeft += alpha * (whiteL - st.lpNoiseLeft)
                st.lpNoiseRight += alpha * (whiteR - st.lpNoiseRight)

                val bellCycle = t % 2.76
                var caravanBell = 0.0
                if (bellCycle < 0.65) {
                    val bellEnv = exp(-6.8 * bellCycle)
                    caravanBell = (sin(2.0 * PI * 1760.0 * bellCycle) * 0.6 +
                        sin(2.0 * PI * 2637.0 * bellCycle) * 0.4) * bellEnv * 0.11
                }

                StereoSample(
                    ((st.lpNoiseLeft * (0.22 + 0.18 * windMod) + caravanBell) * ambientVol).toFloat(),
                    ((st.lpNoiseRight * (0.22 + 0.18 * windMod) + caravanBell * 0.85) * ambientVol).toFloat()
                )
            }
            BoardTheme.ARCTIC_ICE -> {
                val windSweep = 0.5 + 0.5 * sin(2.0 * PI * 0.14 * t)
                val whiteL = st.rng.nextDouble(-1.0, 1.0)
                val whiteR = st.rng.nextDouble(-1.0, 1.0)
                val alpha = 0.018 + 0.025 * windSweep
                st.lpNoiseLeft += alpha * (whiteL - st.lpNoiseLeft)
                st.lpNoiseRight += alpha * (whiteR - st.lpNoiseRight)

                val whistleFreq = 580.0 + 160.0 * windSweep
                st.birdPhase += (2.0 * PI * whistleFreq) / sampleRate
                val polarWhistle = sin(st.birdPhase) * (0.04 + 0.04 * windSweep)

                val crystalCycle = t % 1.9
                var crystalSparkle = 0.0
                if (crystalCycle < 0.38) {
                    val env = exp(-11.0 * crystalCycle)
                    crystalSparkle = (sin(2.0 * PI * 2637.0 * crystalCycle) * 0.55 +
                        sin(2.0 * PI * 3520.0 * crystalCycle) * 0.45) * env * 0.12
                }

                StereoSample(
                    ((st.lpNoiseLeft * 0.25 + polarWhistle + crystalSparkle) * ambientVol).toFloat(),
                    ((st.lpNoiseRight * 0.25 + polarWhistle + crystalSparkle * 0.9) * ambientVol).toFloat()
                )
            }
            BoardTheme.BLACK_GOLD -> {
                val hallBreath = 0.5 + 0.5 * sin(2.0 * PI * 0.10 * t)
                val whiteL = st.rng.nextDouble(-1.0, 1.0)
                val whiteR = st.rng.nextDouble(-1.0, 1.0)
                st.lpNoiseLeft += 0.014 * (whiteL - st.lpNoiseLeft)
                st.lpNoiseRight += 0.014 * (whiteR - st.lpNoiseRight)

                val shimmerCycle = t % 2.5
                var goldChime = 0.0
                if (shimmerCycle < 0.45) {
                    val env = exp(-8.5 * shimmerCycle)
                    goldChime = (sin(2.0 * PI * 1975.5 * shimmerCycle) * 0.6 +
                        sin(2.0 * PI * 2959.9 * shimmerCycle) * 0.4) * env * 0.09
                }

                StereoSample(
                    ((st.lpNoiseLeft * 0.18 * hallBreath + goldChime) * ambientVol).toFloat(),
                    ((st.lpNoiseRight * 0.18 * hallBreath + goldChime) * ambientVol).toFloat()
                )
            }
        }
    }
}
