package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.model.AnimationQuality
import com.example.model.AnimationSpeed
import com.example.model.BoardLink
import com.example.model.BoardObstacle
import com.example.model.BoardTheme
import com.example.model.LinkType
import com.example.model.ObstacleType
import com.example.model.Player
import com.example.model.SnakeColorPalette
import com.example.model.SnakeVisualTheme
import com.example.model.tileToGridPosition
import com.example.model.toPersianDigits
import com.example.ui.BoardHighlightEffect
import com.example.ui.HeroBoardFxType
import com.example.ui.TokenMovementPath
import com.example.ui.TokenMovementType
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.HopefulMint
import com.example.ui.theme.RadiantGold
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SnakesAndLaddersBoard(
    boardLinks: List<BoardLink>,
    boardObstacles: List<BoardObstacle>,
    players: List<Player>,
    currentTurnPlayerId: Int?,
    activeMovementPath: TokenMovementPath?,
    highlightEffect: BoardHighlightEffect?,
    stormFlashActive: Boolean,
    maxTiles: Int = 100,
    boardTheme: BoardTheme = BoardTheme.BLACK_GOLD,
    snakeVisualTheme: SnakeVisualTheme = SnakeVisualTheme.ROYAL_GOLD_SLIM,
    snakeThicknessScale: Float = 0.85f,
    isWeatherEffectsEnabled: Boolean = true,
    isAmbientAnimationEnabled: Boolean = true,
    animationQuality: AnimationQuality = AnimationQuality.CINEMATIC,
    animationSpeed: AnimationSpeed = AnimationSpeed.SMOOTH,
    isCameraFollowEnabled: Boolean = true,
    onStepReached: (playerId: Int, tile: Int) -> Unit,
    onMovementFinished: (moveId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeMaxTiles = (maxTiles / 10 * 10).coerceIn(50, 200)
    val totalRows = (safeMaxTiles / 10).coerceAtLeast(5)

    val tokenGridPosAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val tokenScaleAnim = remember { Animatable(1f) }
    val boardShakeAnim = remember { Animatable(0f) }

    // High-precision 60/90/120 FPS token physics states (Jump height, Squash & Stretch, Landing Glow, Camera Follow)
    var tokenJumpHeight by remember { mutableFloatStateOf(0f) }
    var tokenSquashX by remember { mutableFloatStateOf(1f) }
    var tokenSquashY by remember { mutableFloatStateOf(1f) }
    var landingGlowTile by remember { mutableStateOf<Int?>(null) }
    var landingGlowProgress by remember { mutableFloatStateOf(0f) }

    var cameraZoom by remember { mutableFloatStateOf(1f) }
    var cameraFocusCol by remember { mutableFloatStateOf(4.5f) }
    var cameraFocusRow by remember { mutableFloatStateOf(4.5f) }

    val particleSystem = remember { BoardParticleSystem() }
    var particleFrameTick by remember { mutableIntStateOf(0) }
    var activeTrailStartGrid by remember { mutableStateOf<Offset?>(null) }
    var activeTrailEndGrid by remember { mutableStateOf<Offset?>(null) }

    val frameTopColor by animateColorAsState(
        targetValue = boardTheme.boardFrameOuterTop,
        animationSpec = tween(420),
        label = "frame_top"
    )
    val frameMidColor by animateColorAsState(
        targetValue = boardTheme.boardFrameOuterMid,
        animationSpec = tween(420),
        label = "frame_mid"
    )
    val frameBottomColor by animateColorAsState(
        targetValue = boardTheme.boardFrameOuterBottom,
        animationSpec = tween(420),
        label = "frame_bottom"
    )
    val borderMainColor by animateColorAsState(
        targetValue = boardTheme.boardBorderMain,
        animationSpec = tween(420),
        label = "border_main"
    )

    // Emit obstacle burst when highlightEffect triggers on an obstacle tile
    LaunchedEffect(highlightEffect?.tile, highlightEffect?.obstacleType, safeMaxTiles) {
        val effect = highlightEffect ?: return@LaunchedEffect
        val obsType = effect.obstacleType ?: return@LaunchedEffect
        val (r, c) = tileToGridPosition(effect.tile, safeMaxTiles)
        val scaledCount = (30 * animationQuality.particleMultiplier).roundToInt().coerceAtLeast(10)
        particleSystem.emitObstacleBurstAtGrid(
            colFloat = c.toFloat(),
            rowFloat = r.toFloat(),
            obstacleType = obsType,
            count = scaledCount
        )
    }

    // 60/90/120fps particle physics, step/path trail emission & smooth damped camera follow loop
    LaunchedEffect(activeMovementPath?.moveId, highlightEffect?.tile, isCameraFollowEnabled, animationQuality) {
        var lastFrameNanos = 0L
        var trailAccumulator = 0f

        while (activeMovementPath != null ||
            particleSystem.hasActiveParticles ||
            highlightEffect != null ||
            landingGlowTile != null ||
            abs(cameraZoom - 1f) > 0.001f
        ) {
            withFrameNanos { frameNanos ->
                val dt = if (lastFrameNanos == 0L) {
                    0.016f
                } else {
                    ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                }
                lastFrameNanos = frameNanos

                val currentMove = activeMovementPath
                if (currentMove != null) {
                    trailAccumulator += dt
                    val emitInterval = if (currentMove.movementType == TokenMovementType.STEP_BY_STEP) 0.030f else 0.022f
                    if (trailAccumulator >= emitInterval) {
                        trailAccumulator = 0f
                        if (currentMove.movementType == TokenMovementType.STEP_BY_STEP) {
                            if (animationQuality.enableRichTokenTrail) {
                                val movingColor = players.find { it.id == currentMove.playerId }?.color ?: RadiantGold
                                val trailCount = if (animationQuality == AnimationQuality.CINEMATIC) 2 else 1
                                particleSystem.emitStepTrailAtGrid(
                                    colFloat = tokenGridPosAnim.value.x,
                                    rowFloat = tokenGridPosAnim.value.y,
                                    tokenColor = movingColor,
                                    count = trailCount
                                )
                            }
                        } else {
                            val trailCount = (3 * animationQuality.particleMultiplier).roundToInt().coerceAtLeast(1)
                            particleSystem.emitTrailAtGrid(
                                colFloat = tokenGridPosAnim.value.x,
                                rowFloat = tokenGridPosAnim.value.y,
                                movementType = currentMove.movementType,
                                count = trailCount
                            )
                        }
                    }
                }

                // Smoothly damp camera follow toward active moving token (or back to neutral 1.0x when idle)
                val targetZoom = if (isCameraFollowEnabled && currentMove != null) 1.036f else 1f
                val targetCol = if (isCameraFollowEnabled && currentMove != null) tokenGridPosAnim.value.x else 4.5f
                val targetRow = if (isCameraFollowEnabled && currentMove != null) tokenGridPosAnim.value.y else (totalRows - 1) * 0.5f
                val camLerp = (dt * 7.5f).coerceIn(0.02f, 0.35f)
                cameraZoom += (targetZoom - cameraZoom) * camLerp
                cameraFocusCol += (targetCol - cameraFocusCol) * camLerp
                cameraFocusRow += (targetRow - cameraFocusRow) * camLerp

                particleSystem.update(dt)
                particleFrameTick++
            }
        }
        cameraZoom = 1f
    }

    // Execute high-precision withFrameNanos token movement whenever a new TokenMovementPath arrives
    LaunchedEffect(activeMovementPath?.moveId, safeMaxTiles) {
        val movement = activeMovementPath ?: return@LaunchedEffect
        val tiles = movement.pathTiles
        if (tiles.isEmpty()) {
            onMovementFinished(movement.moveId)
            return@LaunchedEffect
        }

        val movingPlayerColor = players.find { it.id == movement.playerId }?.color ?: RadiantGold
        val pMult = animationQuality.particleMultiplier
        val sMult = animationSpeed.durationMultiplier

        val (startRow, startCol) = tileToGridPosition(tiles.first(), safeMaxTiles)
        val startOffset = Offset(startCol.toFloat(), startRow.toFloat())
        tokenGridPosAnim.snapTo(startOffset)
        tokenScaleAnim.snapTo(1f)
        tokenJumpHeight = 0f
        tokenSquashX = 1f
        tokenSquashY = 1f
        landingGlowTile = null
        landingGlowProgress = 0f
        boardShakeAnim.snapTo(0f)

        // Helper for natural squash-and-stretch landing settle + expanding glow ring on destination tile
        suspend fun runNaturalLandingSettle(destTile: Int, destOffset: Offset, isCelebratory: Boolean = false) {
            landingGlowTile = destTile
            landingGlowProgress = 0f
            val sparkCount = ((if (isCelebratory) 18 else 10) * pMult).roundToInt().coerceAtLeast(3)
            particleSystem.emitLandingRingAndSparksAtGrid(
                colFloat = destOffset.x,
                rowFloat = destOffset.y,
                tokenColor = movingPlayerColor,
                isFinalDestination = true,
                sparkCount = sparkCount
            )
            val settleDurationNs = (155L * sMult).toLong().coerceIn(110L, 210L) * 1_000_000L
            val settleStartNs = withFrameNanos { it }
            while (true) {
                val nowNs = withFrameNanos { it }
                val p = ((nowNs - settleStartNs).toFloat() / settleDurationNs.toFloat()).coerceIn(0f, 1f)
                landingGlowProgress = p
                // Squash on impact (p = 0..0.32), then spring rebound (0.32..0.72), then settle to 1.0
                val damp = (1f - p) * (1f - p)
                val squashWave = cos(p * 2.2f * PI.toFloat()) * damp * 0.14f
                tokenSquashX = 1f + squashWave
                tokenSquashY = 1f - squashWave * 0.88f
                tokenJumpHeight = if (p in 0.25f..0.75f) {
                    val b = (p - 0.25f) / 0.50f
                    0.16f * 4f * b * (1f - b)
                } else 0f
                if (p >= 1f) break
            }
            tokenSquashX = 1f
            tokenSquashY = 1f
            tokenJumpHeight = 0f
            landingGlowTile = null
        }

        when (movement.movementType) {
            TokenMovementType.STEP_BY_STEP -> {
                activeTrailStartGrid = null
                activeTrailEndGrid = null
                val stepDurationNs = animationSpeed.stepDurationMs * 1_000_000L

                var prevOffset = startOffset
                for (i in 1 until tiles.size) {
                    val nextTile = tiles[i]
                    val (targetRow, targetCol) = tileToGridPosition(nextTile, safeMaxTiles)
                    val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())
                    val isFirstStep = (i == 1)
                    val isLastStep = (i == tiles.size - 1)

                    val stepStartNs = withFrameNanos { it }
                    while (true) {
                        val nowNs = withFrameNanos { it }
                        val rawP = ((nowNs - stepStartNs).toFloat() / stepDurationNs.toFloat()).coerceIn(0f, 1f)
                        // Smooth cubic Ease-In-Out horizontal interpolation
                        val easeP = rawP * rawP * (3f - 2f * rawP)
                        val curX = prevOffset.x + (targetOffset.x - prevOffset.x) * easeP
                        val curY = prevOffset.y + (targetOffset.y - prevOffset.y) * easeP
                        tokenGridPosAnim.snapTo(Offset(curX, curY))

                        // Parabolic hop arc with initial takeoff lift
                        val hopArc = sin(rawP * PI.toFloat())
                        val takeoffBoost = if (isFirstStep) sin(rawP * PI.toFloat()) * 0.14f else 0f
                        tokenJumpHeight = (hopArc * 0.88f + takeoffBoost).coerceIn(0f, 1.15f)
                        tokenScaleAnim.snapTo(1f + tokenJumpHeight * 0.24f)

                        // Dynamic squash-and-stretch during hop & intermediate tile contact
                        if (rawP < 0.15f && isFirstStep) {
                            // Takeoff anticipation
                            val t = rawP / 0.15f
                            tokenSquashX = 1f + 0.08f * (1f - t)
                            tokenSquashY = 1f - 0.07f * (1f - t)
                        } else if (rawP in 0.20f..0.80f) {
                            // Airborne vertical stretch
                            val stretch = sin(((rawP - 0.20f) / 0.60f) * PI.toFloat()) * 0.065f
                            tokenSquashX = 1f - stretch * 0.65f
                            tokenSquashY = 1f + stretch
                        } else if (rawP > 0.82f && !isLastStep) {
                            // Intermediate tile bounce cushion
                            val c = (rawP - 0.82f) / 0.18f
                            val cushion = sin(c * PI.toFloat()) * 0.075f
                            tokenSquashX = 1f + cushion
                            tokenSquashY = 1f - cushion * 0.85f
                        } else {
                            tokenSquashX = 1f
                            tokenSquashY = 1f
                        }

                        if (rawP >= 1f) break
                    }

                    tokenGridPosAnim.snapTo(targetOffset)
                    prevOffset = targetOffset
                    onStepReached(movement.playerId, nextTile)
                }

                tokenScaleAnim.snapTo(1f)
                val finalTile = tiles.last()
                runNaturalLandingSettle(
                    destTile = finalTile,
                    destOffset = prevOffset,
                    isCelebratory = (finalTile >= safeMaxTiles)
                )
            }

            TokenMovementType.REVERSE_BACKWARD -> {
                activeTrailStartGrid = startOffset
                val (endRow, endCol) = tileToGridPosition(tiles.last(), safeMaxTiles)
                activeTrailEndGrid = Offset(endCol.toFloat(), endRow.toFloat())

                launch {
                    repeat(4) { s ->
                        boardShakeAnim.animateTo(if (s % 2 == 0) 5f else -5f, tween(55))
                    }
                    boardShakeAnim.animateTo(0f, tween(55))
                }

                val stepDurationNs = (animationSpeed.stepDurationMs * 0.88f).toLong().coerceAtLeast(130L) * 1_000_000L
                var prevOffset = startOffset
                for (i in 1 until tiles.size) {
                    val prevTile = tiles[i]
                    val (targetRow, targetCol) = tileToGridPosition(prevTile, safeMaxTiles)
                    val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                    val stepStartNs = withFrameNanos { it }
                    while (true) {
                        val nowNs = withFrameNanos { it }
                        val rawP = ((nowNs - stepStartNs).toFloat() / stepDurationNs.toFloat()).coerceIn(0f, 1f)
                        val easeP = rawP * rawP * (3f - 2f * rawP)
                        val curX = prevOffset.x + (targetOffset.x - prevOffset.x) * easeP
                        val curY = prevOffset.y + (targetOffset.y - prevOffset.y) * easeP
                        tokenGridPosAnim.snapTo(Offset(curX, curY))

                        val hopArc = sin(rawP * PI.toFloat())
                        tokenJumpHeight = hopArc * 0.55f
                        tokenScaleAnim.snapTo(1f - 0.10f * hopArc)
                        tokenSquashX = 1f + 0.06f * hopArc
                        tokenSquashY = 1f - 0.06f * hopArc

                        if (rawP >= 1f) break
                    }
                    tokenGridPosAnim.snapTo(targetOffset)
                    prevOffset = targetOffset
                    onStepReached(movement.playerId, prevTile)
                }
                tokenScaleAnim.snapTo(1f)
                runNaturalLandingSettle(destTile = tiles.last(), destOffset = prevOffset)
                activeTrailStartGrid = null
                activeTrailEndGrid = null
            }

            TokenMovementType.WELL_PLUNGE, TokenMovementType.SHADOW_SWAP -> {
                val destTile = tiles.last()
                val (targetRow, targetCol) = tileToGridPosition(destTile, safeMaxTiles)
                val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                activeTrailStartGrid = startOffset
                activeTrailEndGrid = targetOffset

                particleSystem.emitBurstAtGrid(
                    colFloat = startOffset.x,
                    rowFloat = startOffset.y,
                    movementType = TokenMovementType.WELL_PLUNGE,
                    count = (26 * pMult).roundToInt().coerceAtLeast(8)
                )

                tokenScaleAnim.animateTo(
                    targetValue = 0.08f,
                    animationSpec = tween(durationMillis = (320 * sMult).toInt(), easing = FastOutLinearInEasing)
                )

                tokenGridPosAnim.animateTo(
                    targetValue = targetOffset,
                    animationSpec = tween(durationMillis = (200 * sMult).toInt(), easing = LinearEasing)
                )

                particleSystem.emitBurstAtGrid(
                    colFloat = targetOffset.x,
                    rowFloat = targetOffset.y,
                    movementType = TokenMovementType.WELL_PLUNGE,
                    count = (30 * pMult).roundToInt().coerceAtLeast(10)
                )
                tokenScaleAnim.animateTo(
                    targetValue = 1.28f,
                    animationSpec = tween(durationMillis = (150 * sMult).toInt(), easing = LinearOutSlowInEasing)
                )
                tokenScaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                runNaturalLandingSettle(destTile = destTile, destOffset = targetOffset)
                activeTrailStartGrid = null
                activeTrailEndGrid = null
            }

            TokenMovementType.WIND_GUST, TokenMovementType.LUCK_BOOST, TokenMovementType.TIME_REWIND -> {
                val destTile = tiles.last()
                val (targetRow, targetCol) = tileToGridPosition(destTile, safeMaxTiles)
                val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                activeTrailStartGrid = startOffset
                activeTrailEndGrid = targetOffset

                particleSystem.emitBurstAtGrid(
                    colFloat = startOffset.x,
                    rowFloat = startOffset.y,
                    movementType = movement.movementType,
                    count = (24 * pMult).roundToInt().coerceAtLeast(8)
                )

                val durationNs = (600L * sMult).toLong().coerceAtLeast(420L) * 1_000_000L
                val startNs = withFrameNanos { it }
                while (true) {
                    val nowNs = withFrameNanos { it }
                    val rawP = ((nowNs - startNs).toFloat() / durationNs.toFloat()).coerceIn(0f, 1f)
                    val easeP = rawP * rawP * (3f - 2f * rawP)
                    tokenGridPosAnim.snapTo(
                        Offset(
                            startOffset.x + (targetOffset.x - startOffset.x) * easeP,
                            startOffset.y + (targetOffset.y - startOffset.y) * easeP
                        )
                    )
                    val arc = sin(rawP * PI.toFloat())
                    tokenJumpHeight = arc * 1.25f
                    tokenScaleAnim.snapTo(1f + arc * 0.36f)
                    tokenSquashX = 1f - arc * 0.05f
                    tokenSquashY = 1f + arc * 0.08f
                    if (rawP >= 1f) break
                }
                tokenScaleAnim.snapTo(1f)
                particleSystem.emitBurstAtGrid(
                    colFloat = targetOffset.x,
                    rowFloat = targetOffset.y,
                    movementType = movement.movementType,
                    count = (28 * pMult).roundToInt().coerceAtLeast(10)
                )
                runNaturalLandingSettle(destTile = destTile, destOffset = targetOffset, isCelebratory = true)
                activeTrailStartGrid = null
                activeTrailEndGrid = null
            }

            TokenMovementType.STORM_SHIFT -> {
                val destTile = tiles.last()
                val (targetRow, targetCol) = tileToGridPosition(destTile, safeMaxTiles)
                val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                particleSystem.emitBurstAtGrid(
                    colFloat = startOffset.x,
                    rowFloat = startOffset.y,
                    movementType = TokenMovementType.STORM_SHIFT,
                    count = (34 * pMult).roundToInt().coerceAtLeast(12)
                )

                coroutineScope {
                    launch {
                        repeat(8) { step ->
                            val amp = (1f - step / 8f) * 12f
                            boardShakeAnim.animateTo(
                                targetValue = if (step % 2 == 0) amp else -amp,
                                animationSpec = tween(durationMillis = 55, easing = LinearEasing)
                            )
                        }
                        boardShakeAnim.animateTo(0f, tween(55))
                    }
                    launch {
                        tokenGridPosAnim.animateTo(
                            targetValue = targetOffset,
                            animationSpec = tween(durationMillis = (520 * sMult).toInt(), easing = FastOutSlowInEasing)
                        )
                    }
                    launch {
                        tokenScaleAnim.animateTo(1.30f, tween((260 * sMult).toInt()))
                        tokenScaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                    }
                }
                runNaturalLandingSettle(destTile = destTile, destOffset = targetOffset)
            }

            TokenMovementType.EXECUTIONER_HUNT -> {
                val destTile = tiles.last()
                val (targetRow, targetCol) = tileToGridPosition(destTile, safeMaxTiles)
                val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                activeTrailStartGrid = startOffset
                activeTrailEndGrid = targetOffset

                particleSystem.emitBurstAtGrid(
                    colFloat = startOffset.x,
                    rowFloat = startOffset.y,
                    movementType = TokenMovementType.EXECUTIONER_HUNT,
                    count = (36 * pMult).roundToInt().coerceAtLeast(14)
                )

                coroutineScope {
                    launch {
                        repeat(7) { step ->
                            val amp = (1f - step / 7f) * 14f
                            boardShakeAnim.animateTo(
                                targetValue = if (step % 2 == 0) amp else -amp,
                                animationSpec = tween(durationMillis = 48, easing = LinearEasing)
                            )
                        }
                        boardShakeAnim.animateTo(0f, tween(50))
                    }
                    launch {
                        val huntDurationNs = (480L * sMult).toLong().coerceAtLeast(320L) * 1_000_000L
                        val startNs = withFrameNanos { it }
                        while (true) {
                            val nowNs = withFrameNanos { it }
                            val rawP = ((nowNs - startNs).toFloat() / huntDurationNs.toFloat()).coerceIn(0f, 1f)
                            val easeP = 1f - (1f - rawP) * (1f - rawP)
                            tokenGridPosAnim.snapTo(
                                Offset(
                                    startOffset.x + (targetOffset.x - startOffset.x) * easeP,
                                    startOffset.y + (targetOffset.y - startOffset.y) * easeP
                                )
                            )
                            val arc = sin(rawP * PI.toFloat())
                            tokenJumpHeight = arc * 0.95f
                            tokenScaleAnim.snapTo(1f - arc * 0.16f)
                            tokenSquashX = 1f + arc * 0.14f
                            tokenSquashY = 1f - arc * 0.12f
                            if (rawP >= 1f) break
                        }
                    }
                }
                tokenScaleAnim.snapTo(1f)
                particleSystem.emitBurstAtGrid(
                    colFloat = targetOffset.x,
                    rowFloat = targetOffset.y,
                    movementType = TokenMovementType.EXECUTIONER_HUNT,
                    count = (28 * pMult).roundToInt().coerceAtLeast(10)
                )
                runNaturalLandingSettle(destTile = destTile, destOffset = targetOffset)
                activeTrailStartGrid = null
                activeTrailEndGrid = null
            }

            TokenMovementType.LADDER_CLIMB -> {
                val destTile = tiles.last()
                val (targetRow, targetCol) = tileToGridPosition(destTile, safeMaxTiles)
                val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                activeTrailStartGrid = startOffset
                activeTrailEndGrid = targetOffset

                particleSystem.emitBurstAtGrid(
                    colFloat = startOffset.x,
                    rowFloat = startOffset.y,
                    movementType = TokenMovementType.LADDER_CLIMB,
                    count = (24 * pMult).roundToInt().coerceAtLeast(8)
                )

                // Stepped, rhythmic ladder ascent along the exact ladder rails with rung-by-rung bobbing
                val dx = targetOffset.x - startOffset.x
                val dy = targetOffset.y - startOffset.y
                val dist = hypot(dx, dy).coerceAtLeast(1f)
                val rungs = (dist * 1.9f).roundToInt().coerceIn(4, 9)
                val climbDurationNs = animationSpeed.specialPathDurationMs * 1_000_000L

                val climbStartNs = withFrameNanos { it }
                while (true) {
                    val nowNs = withFrameNanos { it }
                    val rawP = ((nowNs - climbStartNs).toFloat() / climbDurationNs.toFloat()).coerceIn(0f, 1f)
                    // Smooth Ease-In-Out blended with subtle rung-by-rung climbing cadence
                    val baseEase = rawP * rawP * (3f - 2f * rawP)
                    val rungPulse = sin(rawP * rungs * 2f * PI.toFloat()) * 0.012f * sin(rawP * PI.toFloat())
                    val climbT = (baseEase + rungPulse).coerceIn(0f, 1f)

                    tokenGridPosAnim.snapTo(
                        Offset(
                            startOffset.x + dx * climbT,
                            startOffset.y + dy * climbT
                        )
                    )

                    val rungHop = abs(sin(rawP * rungs * PI.toFloat()))
                    val overallArc = sin(rawP * PI.toFloat())
                    tokenJumpHeight = overallArc * 0.48f + rungHop * 0.26f
                    tokenScaleAnim.snapTo(1f + overallArc * 0.25f + rungHop * 0.06f)

                    val stretch = (rungHop - 0.5f) * 0.09f
                    tokenSquashX = 1f - stretch * 0.6f
                    tokenSquashY = 1f + stretch

                    if (rawP >= 1f) break
                }

                tokenGridPosAnim.snapTo(targetOffset)
                tokenScaleAnim.snapTo(1f)
                particleSystem.emitBurstAtGrid(
                    colFloat = targetOffset.x,
                    rowFloat = targetOffset.y,
                    movementType = TokenMovementType.LADDER_CLIMB,
                    count = (32 * pMult).roundToInt().coerceAtLeast(12)
                )
                runNaturalLandingSettle(destTile = destTile, destOffset = targetOffset, isCelebratory = true)
                activeTrailStartGrid = null
                activeTrailEndGrid = null
            }

            TokenMovementType.SNAKE_SLIDE -> {
                val startTile = tiles.first()
                val destTile = tiles.last()
                val (targetRow, targetCol) = tileToGridPosition(destTile, safeMaxTiles)
                val targetOffset = Offset(targetCol.toFloat(), targetRow.toFloat())

                activeTrailStartGrid = startOffset
                activeTrailEndGrid = targetOffset

                particleSystem.emitBurstAtGrid(
                    colFloat = startOffset.x,
                    rowFloat = startOffset.y,
                    movementType = TokenMovementType.SNAKE_SLIDE,
                    count = (26 * pMult).roundToInt().coerceAtLeast(8)
                )

                // Match the exact 2D S-curve spine of drawThreeDimensionalSnake in grid coordinates
                val matchedSnakeIdx = boardLinks
                    .filter { it.type == LinkType.SNAKE }
                    .indexOfFirst { it.startTile == startTile }
                    .coerceAtLeast(0)
                val waveDir = if (matchedSnakeIdx % 2 == 0) 1f else -1f
                val dxGrid = targetOffset.x - startOffset.x
                val dyGrid = targetOffset.y - startOffset.y
                // Note: visual Y on screen is inverted relative to rowFromBottom, so screen-perpendicular normal in grid space is (-dy, dx)/len
                val lenGrid = hypot(dxGrid, dyGrid).coerceAtLeast(0.01f)
                val perpCol = -dyGrid / lenGrid
                val perpRow = dxGrid / lenGrid
                val waveAmpGrid = 0.28f * snakeVisualTheme.waveScale * waveDir

                val slideDurationNs = animationSpeed.specialPathDurationMs * 1_000_000L

                coroutineScope {
                    launch {
                        repeat(5) { step ->
                            val amplitude = (1f - step / 5f) * 7f
                            boardShakeAnim.animateTo(
                                targetValue = if (step % 2 == 0) amplitude else -amplitude,
                                animationSpec = tween(durationMillis = 65, easing = LinearEasing)
                            )
                        }
                        boardShakeAnim.animateTo(0f, animationSpec = tween(durationMillis = 65))
                    }
                    launch {
                        val slideStartNs = withFrameNanos { it }
                        while (true) {
                            val nowNs = withFrameNanos { it }
                            val rawP = ((nowNs - slideStartNs).toFloat() / slideDurationNs.toFloat()).coerceIn(0f, 1f)
                            // Natural gravity acceleration along the S-curve spine with soft arrival deceleration
                            val smoothP = rawP * rawP * (3f - 2f * rawP)
                            val u = (0.32f * rawP * rawP + 0.68f * smoothP).coerceIn(0f, 1f)

                            val baseCol = startOffset.x + dxGrid * u
                            val baseRow = startOffset.y + dyGrid * u
                            val envelope = sin(u * PI.toFloat())
                            val slither = sin(u * PI.toFloat() * 2.1f) * waveAmpGrid * envelope

                            tokenGridPosAnim.snapTo(
                                Offset(
                                    x = baseCol + perpCol * slither,
                                    y = baseRow + perpRow * slither
                                )
                            )

                            tokenJumpHeight = sin(rawP * PI.toFloat()) * 0.14f
                            tokenScaleAnim.snapTo(1f - 0.12f * sin(rawP * PI.toFloat()))

                            // Slight sleek elongation while sliding down the curve
                            val slideStretch = sin(rawP * PI.toFloat()) * 0.08f
                            tokenSquashX = 1f - slideStretch * 0.5f
                            tokenSquashY = 1f + slideStretch

                            if (rawP >= 1f) break
                        }
                    }
                }

                tokenGridPosAnim.snapTo(targetOffset)
                tokenScaleAnim.snapTo(1f)
                particleSystem.emitBurstAtGrid(
                    colFloat = targetOffset.x,
                    rowFloat = targetOffset.y,
                    movementType = TokenMovementType.SNAKE_SLIDE,
                    count = (22 * pMult).roundToInt().coerceAtLeast(8)
                )
                runNaturalLandingSettle(destTile = destTile, destOffset = targetOffset)
                activeTrailStartGrid = null
                activeTrailEndGrid = null
            }
        }

        onMovementFinished(movement.moveId)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "board_shimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )

    val tokenPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "token_pulse"
    )

    val numberPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.LEFT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val specialLabelPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val ladderStartTiles = remember(boardLinks) {
        boardLinks.filter { it.type == LinkType.LADDER }.map { it.startTile }.toSet()
    }
    val snakeHeadTiles = remember(boardLinks) {
        boardLinks.filter { it.type == LinkType.SNAKE }.map { it.startTile }.toSet()
    }
    val obstaclesByTile = remember(boardObstacles) {
        boardObstacles.associateBy { it.tile }
    }
    val playersByTile = remember(players) {
        players.groupBy { it.currentTile }
    }
    val leaderPlayerId = remember(players) {
        players.maxByOrNull { it.currentTile }?.takeIf { it.currentTile > 1 }?.id
    }
    val effectiveShimmer = if (isAmbientAnimationEnabled) shimmerPhase else 0.35f

    // 3D Luxury Tabletop Stage Container with Living Theme Frame Ornaments — Maximized Real Layout Allocation
    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(boardShakeAnim.value.roundToInt(), 0) }
            .graphicsLayer {
                rotationX = 1.2f
                cameraDistance = 28f * density
            }
            .shadow(18.dp, RoundedCornerShape(20.dp), spotColor = borderMainColor)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        frameTopColor,
                        frameMidColor,
                        frameBottomColor
                    )
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        boardTheme.boardBorderHighlight,
                        borderMainColor,
                        boardTheme.boardBorderShadow,
                        boardTheme.boardBorderHighlight,
                        borderMainColor
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("game_board_100"),
        contentAlignment = Alignment.Center
    ) {
        // Inner Game Grid Surface — Maximized tile area with minimal frame inset & GPU-accelerated gentle Camera Follow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(15.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val zoom = cameraZoom.coerceIn(1f, 1.06f)
                        scaleX = zoom
                        scaleY = zoom
                        if (zoom > 1.001f) {
                            val normX = (cameraFocusCol / 9f).coerceIn(0f, 1f)
                            val normY = 1f - (cameraFocusRow / (totalRows - 1).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                            val maxPanX = size.width * (zoom - 1f) * 0.45f
                            val maxPanY = size.height * (zoom - 1f) * 0.45f
                            translationX = ((0.5f - normX) * size.width * 0.045f).coerceIn(-maxPanX, maxPanX)
                            translationY = ((0.5f - normY) * size.height * 0.045f).coerceIn(-maxPanY, maxPanY)
                        }
                    }
            ) {
                @Suppress("UNUSED_EXPRESSION")
                particleFrameTick

                val cellW = size.width / 10f
                val cellH = size.height / totalRows.toFloat()
                val minCellDim = minOf(cellW, cellH)
                val isDarkLuxury = boardTheme.isDarkLuxury
                val palette = boardTheme.tilePalette

                // 1. Draw all maxTiles (50/100/150/200) 3D Beveled Theme Tiles
                for (tile in 1..safeMaxTiles) {
                    val (rowFromBottom, colFromLeft) = tileToGridPosition(tile, safeMaxTiles)
                    val visualRow = (totalRows - 1) - rowFromBottom
                    val left = colFromLeft * cellW
                    val top = visualRow * cellH

                    val isLightTile = (rowFromBottom + colFromLeft) % 2 == 0
                    val paletteIndex = (rowFromBottom * 2 + colFromLeft) % palette.size
                    val (themePaletteTop, themePaletteBottom) = palette[paletteIndex]

                    val isLadderStart = tile in ladderStartTiles
                    val isSnakeHead = tile in snakeHeadTiles
                    val obstacleOnTile = obstaclesByTile[tile]
                    val occupyingPlayers = playersByTile[tile]
                    val isTileOccupied = !occupyingPlayers.isNullOrEmpty()

                    val (tileTopColor, tileBottomColor) = when {
                        tile == safeMaxTiles -> Pair(Color(0xFFFDE047), Color(0xFFB45309))
                        tile == 1 -> if (isDarkLuxury) {
                            Pair(Color(0xFF064E3B), Color(0xFF022C22))
                        } else {
                            Pair(Color(0xFFE6FCF5), Color(0xFF96F2D7))
                        }
                        obstacleOnTile != null -> {
                            val tint = obstacleOnTile.type.badgeColor
                            if (isDarkLuxury) {
                                Pair(
                                    Color(
                                        red = (0.14f + tint.red * 0.22f).coerceIn(0f, 1f),
                                        green = (0.12f + tint.green * 0.22f).coerceIn(0f, 1f),
                                        blue = (0.09f + tint.blue * 0.22f).coerceIn(0f, 1f)
                                    ),
                                    Color(0xFF0E0B07)
                                )
                            } else {
                                Pair(
                                    Color(
                                        red = (themePaletteTop.red * 0.82f + tint.red * 0.18f).coerceIn(0f, 1f),
                                        green = (themePaletteTop.green * 0.82f + tint.green * 0.18f).coerceIn(0f, 1f),
                                        blue = (themePaletteTop.blue * 0.82f + tint.blue * 0.18f).coerceIn(0f, 1f)
                                    ),
                                    Color(
                                        red = (themePaletteBottom.red * 0.72f + tint.red * 0.28f).coerceIn(0f, 1f),
                                        green = (themePaletteBottom.green * 0.72f + tint.green * 0.28f).coerceIn(0f, 1f),
                                        blue = (themePaletteBottom.blue * 0.72f + tint.blue * 0.28f).coerceIn(0f, 1f)
                                    )
                                )
                            }
                        }
                        isLadderStart -> if (isDarkLuxury) {
                            Pair(Color(0xFF133829), Color(0xFF0A1F17))
                        } else {
                            Pair(
                                Color(
                                    red = (themePaletteTop.red * 0.75f + 0.20f).coerceIn(0f, 1f),
                                    green = (themePaletteTop.green * 0.75f + 0.25f).coerceIn(0f, 1f),
                                    blue = (themePaletteTop.blue * 0.75f + 0.22f).coerceIn(0f, 1f)
                                ),
                                Color(
                                    red = (themePaletteBottom.red * 0.70f + 0.10f).coerceIn(0f, 1f),
                                    green = (themePaletteBottom.green * 0.70f + 0.28f).coerceIn(0f, 1f),
                                    blue = (themePaletteBottom.blue * 0.70f + 0.18f).coerceIn(0f, 1f)
                                )
                            )
                        }
                        isSnakeHead -> if (isDarkLuxury) {
                            Pair(Color(0xFF3F1118), Color(0xFF1F070B))
                        } else {
                            Pair(
                                Color(
                                    red = (themePaletteTop.red * 0.76f + 0.24f).coerceIn(0f, 1f),
                                    green = (themePaletteTop.green * 0.76f + 0.12f).coerceIn(0f, 1f),
                                    blue = (themePaletteTop.blue * 0.76f + 0.15f).coerceIn(0f, 1f)
                                ),
                                Color(
                                    red = (themePaletteBottom.red * 0.72f + 0.26f).coerceIn(0f, 1f),
                                    green = (themePaletteBottom.green * 0.72f + 0.08f).coerceIn(0f, 1f),
                                    blue = (themePaletteBottom.blue * 0.72f + 0.12f).coerceIn(0f, 1f)
                                )
                            )
                        }
                        else -> Pair(themePaletteTop, themePaletteBottom)
                    }

                    // Deep recess channel between 3D tiles
                    drawRect(
                        color = boardTheme.tileRecessColor,
                        topLeft = Offset(left, top),
                        size = Size(cellW, cellH)
                    )

                    val padX = cellW * if (isTileOccupied) 0.020f else 0.032f
                    val padY = cellH * if (isTileOccupied) 0.020f else 0.032f
                    val tileRectTopLeft = Offset(left + padX, top + padY)
                    val tileRectSize = Size(cellW - padX * 2f, cellH - padY * 2.3f)
                    val tileCorner = CornerRadius(minCellDim * 0.13f, minCellDim * 0.13f)

                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(tileTopColor, tileBottomColor),
                            start = Offset(left, top),
                            end = Offset(left + cellW, top + cellH)
                        ),
                        topLeft = tileRectTopLeft,
                        size = tileRectSize,
                        cornerRadius = tileCorner
                    )

                    // Theme-Specific Tile Material Detail
                    drawThemeTileMaterialDetail(
                        boardTheme = boardTheme,
                        left = left,
                        top = top,
                        cellW = cellW,
                        cellH = cellH,
                        minCellDim = minCellDim,
                        isLightTile = isLightTile,
                        tileRectTopLeft = tileRectTopLeft,
                        tileRectSize = tileRectSize,
                        tileCorner = tileCorner
                    )

                    // Occupied Tile Luminous Elevation & Glow (خانه‌ای که مهره بازیکن روی آن قرار دارد برجسته‌تر و روشن‌تر دیده شود)
                    if (isTileOccupied) {
                        val activeOccupant = occupyingPlayers.find { it.id == currentTurnPlayerId } ?: occupyingPlayers.first()
                        val occupantGlowColor = activeOccupant.color
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDarkLuxury) 0.24f else 0.42f),
                                    occupantGlowColor.copy(alpha = if (isDarkLuxury) 0.30f else 0.24f),
                                    Color.Transparent
                                ),
                                center = Offset(left + cellW * 0.5f, top + cellH * 0.5f),
                                radius = minCellDim * 0.65f
                            ),
                            topLeft = tileRectTopLeft,
                            size = tileRectSize,
                            cornerRadius = tileCorner
                        )
                    }

                    // Theme-Synchronized Inner Border on Every Tile
                    val tileBorderColor = when {
                        isTileOccupied -> {
                            val activeOccupant = occupyingPlayers.find { it.id == currentTurnPlayerId } ?: occupyingPlayers.first()
                            activeOccupant.color.copy(alpha = 0.92f)
                        }
                        tile == safeMaxTiles -> Color(0xFFFFF3B0)
                        isSnakeHead -> Color(0xFFFF4D6D).copy(alpha = 0.78f)
                        isLadderStart -> Color(0xFF34D399).copy(alpha = 0.78f)
                        obstacleOnTile != null -> obstacleOnTile.type.badgeColor.copy(alpha = 0.75f)
                        else -> boardTheme.tileBorderColor
                    }
                    drawRoundRect(
                        color = tileBorderColor,
                        topLeft = tileRectTopLeft,
                        size = tileRectSize,
                        cornerRadius = tileCorner,
                        style = Stroke(width = if (isTileOccupied) 2.2f else if (isDarkLuxury) 1.3f else 1.1f)
                    )

                    // Top-left 3D specular bevel highlight
                    drawLine(
                        color = when {
                            boardTheme == BoardTheme.ARCTIC_ICE -> Color.White.copy(alpha = 0.92f)
                            isDarkLuxury -> RadiantGold.copy(alpha = 0.38f)
                            else -> Color.White.copy(alpha = 0.74f)
                        },
                        start = Offset(left + padX * 2.2f, top + padY * 1.6f),
                        end = Offset(left + cellW - padX * 2.2f, top + padY * 1.6f),
                        strokeWidth = if (boardTheme == BoardTheme.ARCTIC_ICE) 1.7f else 1.3f,
                        cap = StrokeCap.Round
                    )

                    // Draw High-Detail Special Obstacle 3D Medallion & Artwork on its tile!
                    if (obstacleOnTile != null) {
                        drawAnimatedObstacleOnTile(
                            obstacle = obstacleOnTile,
                            tileCenter = Offset(left + cellW * 0.52f, top + cellH * 0.54f),
                            cellW = minCellDim,
                            shimmerPhase = effectiveShimmer,
                            labelPaint = specialLabelPaint
                        )
                    } else if (isLadderStart) {
                        val c = Offset(left + cellW * 0.5f, top + cellH * 0.54f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(HopefulEmerald.copy(alpha = 0.45f), Color.Transparent),
                                center = c,
                                radius = minCellDim * 0.44f
                            ),
                            radius = minCellDim * 0.44f,
                            center = c
                        )
                        drawCircle(
                            color = RadiantGold.copy(alpha = 0.55f),
                            radius = minCellDim * 0.28f,
                            center = c,
                            style = Stroke(width = 1.4f)
                        )
                    } else if (isSnakeHead) {
                        val c = Offset(left + cellW * 0.5f, top + cellH * 0.54f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(DangerCrimson.copy(alpha = 0.50f), Color.Transparent),
                                center = c,
                                radius = minCellDim * 0.44f
                            ),
                            radius = minCellDim * 0.44f,
                            center = c
                        )
                        drawCircle(
                            color = DangerCrimson.copy(alpha = 0.65f),
                            radius = minCellDim * 0.30f,
                            center = c,
                            style = Stroke(width = 1.5f)
                        )
                    } else if (tile == safeMaxTiles) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF3B0).copy(alpha = 0.85f), Color.Transparent),
                                center = Offset(left + cellW * 0.5f, top + cellH * 0.5f),
                                radius = minCellDim * 0.48f
                            ),
                            radius = minCellDim * 0.48f,
                            center = Offset(left + cellW * 0.5f, top + cellH * 0.5f)
                        )
                    }

                    // Clear, embossed Persian tile numbers with subtle shadow
                    numberPaint.textSize = (minCellDim * 0.26f).coerceAtLeast(10f)
                    val (tr, tg, tb) = boardTheme.tileTextColorRgb
                    numberPaint.color = when {
                        tile == safeMaxTiles -> android.graphics.Color.rgb(69, 26, 3)
                        isSnakeHead -> if (isDarkLuxury) android.graphics.Color.rgb(255, 143, 163) else android.graphics.Color.rgb(185, 28, 28)
                        isLadderStart -> if (isDarkLuxury) android.graphics.Color.rgb(110, 231, 183) else android.graphics.Color.rgb(4, 120, 87)
                        else -> android.graphics.Color.rgb(tr, tg, tb)
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        tile.toPersianDigits(),
                        left + cellW * 0.09f,
                        top + cellH * 0.31f,
                        numberPaint
                    )

                    if (tile == safeMaxTiles) {
                        specialLabelPaint.textSize = minCellDim * 0.34f
                        drawContext.canvas.nativeCanvas.drawText(
                            "👑",
                            left + cellW * 0.58f,
                            top + cellH * 0.78f,
                            specialLabelPaint
                        )
                    } else if (tile == 1) {
                        specialLabelPaint.textSize = minCellDim * 0.22f
                        specialLabelPaint.color = if (isDarkLuxury) {
                            android.graphics.Color.rgb(253, 224, 71)
                        } else {
                            android.graphics.Color.rgb(6, 95, 70)
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            "شروع",
                            left + cellW * 0.52f,
                            top + cellH * 0.80f,
                            specialLabelPaint
                        )
                    }
                }

                // 2. Draw Theme-Matched 3D Ladders
                val ladders = boardLinks.filter { it.type == LinkType.LADDER }
                for (ladder in ladders) {
                    drawThreeDimensionalLadder(
                        startCenter = tileCenter(ladder.startTile, cellW, cellH, safeMaxTiles, totalRows),
                        endCenter = tileCenter(ladder.endTile, cellW, cellH, safeMaxTiles, totalRows),
                        cellW = minCellDim,
                        shimmerPhase = effectiveShimmer,
                        boardTheme = boardTheme
                    )
                }

                // 3. Draw Slender, Non-Distracting 3D Serpents according to selected SnakeVisualTheme
                val snakes = boardLinks.filter { it.type == LinkType.SNAKE }
                snakes.forEachIndexed { idx, snake ->
                    drawThreeDimensionalSnake(
                        headCenter = tileCenter(snake.startTile, cellW, cellH, safeMaxTiles, totalRows),
                        tailCenter = tileCenter(snake.endTile, cellW, cellH, safeMaxTiles, totalRows),
                        cellW = minCellDim,
                        snakeVariant = idx,
                        shimmerPhase = effectiveShimmer,
                        isNearWormZone = snake.isNearWormZone,
                        boardTheme = boardTheme,
                        snakeVisualTheme = snakeVisualTheme,
                        userThicknessScale = snakeThicknessScale
                    )
                }

                // 4. Draw Special Highlight Effect (Climb glow, Snake bite, Obstacle activation ring, or Hero Ability FX)
                if (highlightEffect != null) {
                    val effectCenter = tileCenter(highlightEffect.tile, cellW, cellH, safeMaxTiles, totalRows)
                    val effectColor = highlightEffect.obstacleType?.badgeColor
                        ?: if (highlightEffect.isPositive) HopefulMint else DangerCrimson
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                effectColor.copy(alpha = 0.76f * highlightEffect.intensity),
                                Color.Transparent
                            ),
                            center = effectCenter,
                            radius = minCellDim * 1.3f
                        ),
                        radius = minCellDim * 1.3f,
                        center = effectCenter
                    )

                    when (highlightEffect.heroFxType) {
                        HeroBoardFxType.EXECUTIONER_SLASH -> {
                            val slashLen = minCellDim * 0.72f
                            drawLine(
                                color = DangerCrimson,
                                start = Offset(effectCenter.x - slashLen, effectCenter.y - slashLen),
                                end = Offset(effectCenter.x + slashLen, effectCenter.y + slashLen),
                                strokeWidth = 4.2f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFF10B981),
                                start = Offset(effectCenter.x + slashLen, effectCenter.y - slashLen),
                                end = Offset(effectCenter.x - slashLen, effectCenter.y + slashLen),
                                strokeWidth = 4.2f,
                                cap = StrokeCap.Round
                            )
                        }
                        HeroBoardFxType.SHIELD_BLOCK_SUCCESS -> {
                            drawCircle(
                                color = RadiantGold,
                                radius = minCellDim * 0.62f,
                                center = effectCenter,
                                style = Stroke(width = 3.6f)
                            )
                            drawCircle(
                                color = Color(0xFF38BDF8),
                                radius = minCellDim * 0.46f,
                                center = effectCenter,
                                style = Stroke(width = 2.4f)
                            )
                        }
                        HeroBoardFxType.SHIELD_BREAK_FAIL -> {
                            for (seg in 0 until 6) {
                                val a = (seg * PI / 3.0).toFloat()
                                val p1 = Offset(effectCenter.x + cos(a) * minCellDim * 0.25f, effectCenter.y + sin(a) * minCellDim * 0.25f)
                                val p2 = Offset(effectCenter.x + cos(a) * minCellDim * 0.65f, effectCenter.y + sin(a) * minCellDim * 0.65f)
                                drawLine(
                                    color = DangerCrimson,
                                    start = p1,
                                    end = p2,
                                    strokeWidth = 2.6f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        HeroBoardFxType.DRAGON_SOUL_AURA, HeroBoardFxType.BERSERKER_RAGE -> {
                            drawCircle(
                                color = Color(0xFFF97316),
                                radius = minCellDim * 0.58f,
                                center = effectCenter,
                                style = Stroke(width = 3.2f)
                            )
                        }
                        HeroBoardFxType.GUARDIAN_ANGEL_WINGS -> {
                            drawCircle(
                                color = Color(0xFFFDE047),
                                radius = minCellDim * 0.56f,
                                center = effectCenter,
                                style = Stroke(width = 3.0f)
                            )
                        }
                        HeroBoardFxType.ILLUSION_MIRAGE, HeroBoardFxType.PHANTOM_PHASE, HeroBoardFxType.TRICKSTER_TRANSFER, HeroBoardFxType.TIME_REWIND -> {
                            drawCircle(
                                color = Color(0xFFA855F7),
                                radius = minCellDim * 0.60f,
                                center = effectCenter,
                                style = Stroke(width = 3.0f)
                            )
                        }
                        HeroBoardFxType.NONE -> Unit
                    }
                }

                // 4.5. Draw Active Illusionist Decoy Holograms on Board Tiles
                for (player in players) {
                    val decoyTile = player.illusionDecoyTile
                    if (decoyTile != null && player.illusionTurnsRemaining > 0 && decoyTile in 1..safeMaxTiles) {
                        val decoyCenter = tileCenter(decoyTile, cellW, cellH, safeMaxTiles, totalRows)
                        val decoyPulse = 0.55f + 0.35f * sin(effectiveShimmer * 2f * PI.toFloat())
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = 0.38f * decoyPulse),
                            radius = minCellDim * 0.32f,
                            center = decoyCenter
                        )
                        drawCircle(
                            color = player.color.copy(alpha = 0.85f),
                            radius = minCellDim * 0.25f,
                            center = decoyCenter,
                            style = Stroke(width = 2.2f)
                        )
                    }
                }

                // 5. Draw Stationary 3D Pedestal Player Tokens
                val animatingPlayerId = activeMovementPath?.playerId
                val staticPlayersByTile = players
                    .filter { it.id != animatingPlayerId }
                    .groupBy { it.currentTile }

                for ((tile, tilePlayers) in staticPlayersByTile) {
                    val baseCenter = tileCenter(tile, cellW, cellH, safeMaxTiles, totalRows)
                    tilePlayers.forEachIndexed { idx, player ->
                        val offset = computeMultiTokenOffset(idx, tilePlayers.size, minCellDim)
                        val isCurrentTurn = player.id == currentTurnPlayerId
                        val isLeader = player.id == leaderPlayerId
                        drawThreeDimensionalPawnToken(
                            center = baseCenter + offset,
                            radius = if (tilePlayers.size > 3) minCellDim * 0.22f else minCellDim * 0.27f,
                            player = player,
                            isCurrentTurn = isCurrentTurn,
                            isLeader = isLeader,
                            pulseScale = if (isCurrentTurn) tokenPulse else 1f,
                            shimmerPhase = effectiveShimmer,
                            namePaint = specialLabelPaint
                        )
                    }
                }

                // 6. Draw Glowing Ascent/Descent/Obstacle Trajectory & Particle Effects
                particleSystem.drawParticlesAndPathGlow(
                    drawScope = this,
                    cellW = cellW,
                    cellH = cellH,
                    startGridPos = activeTrailStartGrid,
                    currentGridPos = if (activeMovementPath != null) tokenGridPosAnim.value else null,
                    endGridPos = activeTrailEndGrid,
                    movementType = activeMovementPath?.movementType,
                    totalRows = totalRows
                )

                // 6.5. Draw Destination Tile Landing Glow Ring (افکت نور هنگام توقف روی خانه مقصد)
                val activeLandingTile = landingGlowTile
                if (activeLandingTile != null) {
                    val (lRow, lCol) = tileToGridPosition(activeLandingTile, safeMaxTiles)
                    val lVisRow = (totalRows - 1) - lRow
                    val lCenter = Offset((lCol + 0.5f) * cellW, (lVisRow + 0.54f) * cellH)
                    val glowAlpha = (1f - landingGlowProgress).coerceIn(0f, 1f)
                    val glowRadius = minCellDim * (0.28f + 0.52f * landingGlowProgress)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RadiantGold.copy(alpha = glowAlpha * 0.65f),
                                Color.White.copy(alpha = glowAlpha * 0.45f),
                                Color.Transparent
                            ),
                            center = lCenter,
                            radius = glowRadius * 1.35f
                        ),
                        radius = glowRadius * 1.35f,
                        center = lCenter
                    )
                    drawCircle(
                        color = RadiantGold.copy(alpha = glowAlpha * 0.85f),
                        radius = glowRadius,
                        center = lCenter,
                        style = Stroke(width = (minCellDim * 0.055f * glowAlpha).coerceAtLeast(1.2f))
                    )
                }

                // 7. Draw Actively Animating 3D Token with Parabolic Lift, Dynamic Shadow & Squash-and-Stretch
                if (activeMovementPath != null) {
                    val movingPlayer = players.find { it.id == activeMovementPath.playerId }
                    if (movingPlayer != null) {
                        val colFloat = tokenGridPosAnim.value.x
                        val rowFloat = tokenGridPosAnim.value.y
                        val visualRow = (totalRows - 1).toFloat() - rowFloat

                        val hopLiftPx = tokenJumpHeight.coerceAtLeast(0f) * cellH * 0.52f

                        val groundSpot = Offset(
                            x = ((colFloat + 0.5f) * cellW).coerceIn(cellW * 0.22f, size.width - cellW * 0.22f),
                            y = (visualRow + 0.56f) * cellH
                        )
                        val airborneCenter = Offset(groundSpot.x, groundSpot.y - hopLiftPx)

                        // Dynamic height-responsive ground shadow under the jumping token
                        val shadowScale = (1f - tokenJumpHeight * 0.26f).coerceIn(0.48f, 1f)
                        val shadowAlpha = (0.52f - tokenJumpHeight * 0.22f).coerceIn(0.18f, 0.56f)
                        val shadowRadius = minCellDim * 0.26f * shadowScale
                        drawOval(
                            color = Color.Black.copy(alpha = shadowAlpha),
                            topLeft = Offset(groundSpot.x - shadowRadius, groundSpot.y - shadowRadius * 0.45f),
                            size = Size(shadowRadius * 2f, shadowRadius * 0.9f)
                        )

                        drawThreeDimensionalPawnToken(
                            center = airborneCenter,
                            radius = minCellDim * 0.29f * tokenScaleAnim.value,
                            player = movingPlayer,
                            isCurrentTurn = true,
                            isLeader = movingPlayer.id == leaderPlayerId,
                            pulseScale = 1.0f,
                            shimmerPhase = effectiveShimmer,
                            squashScaleX = tokenSquashX,
                            squashScaleY = tokenSquashY,
                            namePaint = specialLabelPaint
                        )
                    }
                }

                // 8. Storm Lightning Overlay Flash when STORM obstacle triggers!
                if (stormFlashActive) {
                    val flashAlpha = (0.18f + 0.22f * sin(effectiveShimmer * 12f * PI.toFloat())).coerceIn(0f, 0.45f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE879F9).copy(alpha = flashAlpha),
                                Color(0xFF6366F1).copy(alpha = flashAlpha * 0.7f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                            radius = size.width * 0.75f
                        ),
                        size = size
                    )
                }
            }
        }

        // 9. Living Theme Frame & Perimeter Ornaments Overlay (Frozen Corners, Icicles, Upper Snow, Forest Leaves, Shells, Sandstone, Gold Filigree)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBoardFrameThemeOrnaments(
                boardTheme = boardTheme,
                shimmerPhase = effectiveShimmer,
                isWeatherEffectsEnabled = isWeatherEffectsEnabled
            )
        }
    }
}

/**
 * Draws theme-specific material texture on each tile (e.g., glassy ice sheen on ARCTIC_ICE,
 * gold diamond filigree on BLACK_GOLD, leaf/wood vein on JUNGLE, wave arc on OCEAN_BEACH,
 * sandstone strata on DESERT).
 */
private fun DrawScope.drawThemeTileMaterialDetail(
    boardTheme: BoardTheme,
    left: Float,
    top: Float,
    cellW: Float,
    cellH: Float,
    minCellDim: Float,
    isLightTile: Boolean,
    tileRectTopLeft: Offset,
    tileRectSize: Size,
    tileCorner: CornerRadius
) {
    when (boardTheme) {
        BoardTheme.BLACK_GOLD -> {
            // Subtle 24k golden diamond filigree in tile center
            val cx = left + cellW * 0.5f
            val cy = top + cellH * 0.55f
            val dR = minCellDim * 0.09f
            val diamondPath = Path().apply {
                moveTo(cx, cy - dR)
                lineTo(cx + dR, cy)
                lineTo(cx, cy + dR)
                lineTo(cx - dR, cy)
                close()
            }
            drawPath(
                path = diamondPath,
                color = RadiantGold.copy(alpha = if (isLightTile) 0.14f else 0.07f),
                style = Stroke(width = 1.0f)
            )
        }

        BoardTheme.JUNGLE -> {
            // Subtle botanical leaf vein / organic wood grain arc
            val veinPath = Path().apply {
                moveTo(left + cellW * 0.18f, top + cellH * 0.78f)
                quadraticTo(
                    left + cellW * 0.52f,
                    top + cellH * 0.62f,
                    left + cellW * 0.82f,
                    top + cellH * 0.76f
                )
            }
            drawPath(
                path = veinPath,
                color = Color(0xFF2E7D32).copy(alpha = 0.16f),
                style = Stroke(width = 1.1f, cap = StrokeCap.Round)
            )
        }

        BoardTheme.OCEAN_BEACH -> {
            // Gentle coastal wave shimmer curve across bottom of tile
            val wavePath = Path().apply {
                moveTo(left + cellW * 0.14f, top + cellH * 0.75f)
                cubicTo(
                    left + cellW * 0.38f, top + cellH * 0.66f,
                    left + cellW * 0.62f, top + cellH * 0.84f,
                    left + cellW * 0.86f, top + cellH * 0.73f
                )
            }
            drawPath(
                path = wavePath,
                color = Color.White.copy(alpha = 0.42f),
                style = Stroke(width = 1.3f, cap = StrokeCap.Round)
            )
        }

        BoardTheme.DESERT -> {
            // Subtle sandstone wind-ripple line
            val duneLine = Path().apply {
                moveTo(left + cellW * 0.12f, top + cellH * 0.74f)
                quadraticTo(
                    left + cellW * 0.50f,
                    top + cellH * 0.65f,
                    left + cellW * 0.88f,
                    top + cellH * 0.77f
                )
            }
            drawPath(
                path = duneLine,
                color = Color(0xFFD84315).copy(alpha = 0.15f),
                style = Stroke(width = 1.1f, cap = StrokeCap.Round)
            )
        }

        BoardTheme.ARCTIC_ICE -> {
            // Glassy, translucent icy tile surface that reflects light (حالت شیشه‌ای و یخی با انعکاس نور)
            val glassSheenPath = Path().apply {
                moveTo(tileRectTopLeft.x + tileRectSize.width * 0.08f, tileRectTopLeft.y + tileRectSize.height * 0.86f)
                lineTo(tileRectTopLeft.x + tileRectSize.width * 0.66f, tileRectTopLeft.y + tileRectSize.height * 0.06f)
                lineTo(tileRectTopLeft.x + tileRectSize.width * 0.90f, tileRectTopLeft.y + tileRectSize.height * 0.06f)
                lineTo(tileRectTopLeft.x + tileRectSize.width * 0.34f, tileRectTopLeft.y + tileRectSize.height * 0.86f)
                close()
            }
            drawPath(
                path = glassSheenPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.52f),
                        Color(0xFFE0F7FA).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    start = tileRectTopLeft,
                    end = Offset(tileRectTopLeft.x + tileRectSize.width, tileRectTopLeft.y + tileRectSize.height)
                )
            )
            // Inner frost rim highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.65f),
                topLeft = Offset(tileRectTopLeft.x + 1.2f, tileRectTopLeft.y + 1.2f),
                size = Size(
                    (tileRectSize.width - 2.4f).coerceAtLeast(2f),
                    (tileRectSize.height - 2.4f).coerceAtLeast(2f)
                ),
                cornerRadius = tileCorner,
                style = Stroke(width = 0.9f)
            )
        }
    }
}

/**
 * Draws theme-specific perimeter & corner ornaments around the board frame:
 * - ARCTIC_ICE: Frozen/frosted corners & edges, ice crystals in frame corners, hanging icicles along outer edges, and moving snow particles in the upper area of the board.
 * - JUNGLE: Corner tree branches, botanical leaves, moss patches, and firefly glints.
 * - OCEAN_BEACH: Corner seashells, pearl droplets, and seafoam wave crests.
 * - DESERT: Layered sandstone corner motifs and drifting golden sand motes.
 * - BLACK_GOLD: Royal 24k gold corner filigree & edge light reflections.
 */
private fun DrawScope.drawBoardFrameThemeOrnaments(
    boardTheme: BoardTheme,
    shimmerPhase: Float,
    isWeatherEffectsEnabled: Boolean
) {
    val w = size.width
    val h = size.height
    val phaseRad = shimmerPhase * 2f * PI.toFloat()

    when (boardTheme) {
        BoardTheme.ARCTIC_ICE -> {
            // 1. Heavily Frozen / Frosted Board Corners & Edges (گوشه‌ها و لبه‌های صفحه بازی کاملاً یخ‌زده)
            val cornerSize = w * 0.14f
            val corners = listOf(
                Offset(0f, 0f) to Pair(1f, 1f),
                Offset(w, 0f) to Pair(-1f, 1f),
                Offset(0f, h) to Pair(1f, -1f),
                Offset(w, h) to Pair(-1f, -1f)
            )
            for ((origin, dir) in corners) {
                val (dx, dy) = dir
                val frostPatch = Path().apply {
                    moveTo(origin.x, origin.y)
                    lineTo(origin.x + dx * cornerSize, origin.y)
                    quadraticTo(
                        origin.x + dx * cornerSize * 0.45f,
                        origin.y + dy * cornerSize * 0.45f,
                        origin.x,
                        origin.y + dy * cornerSize
                    )
                    close()
                }
                drawPath(
                    path = frostPatch,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.82f),
                            Color(0xFFB2EBF2).copy(alpha = 0.55f),
                            Color(0xFF4DD0E1).copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = origin,
                        radius = cornerSize * 1.1f
                    )
                )

                // Ice Crystal Star in each corner of the frame (بلورهای یخ در گوشه‌های قاب صفحه)
                val crystalCenter = Offset(origin.x + dx * cornerSize * 0.30f, origin.y + dy * cornerSize * 0.30f)
                val crystalR = w * 0.026f
                for (arm in 0 until 6) {
                    val angle = arm * (PI / 3.0)
                    val end = Offset(
                        crystalCenter.x + (cos(angle) * crystalR).toFloat(),
                        crystalCenter.y + (sin(angle) * crystalR).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.92f),
                        start = crystalCenter,
                        end = end,
                        strokeWidth = 2.0f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Translucent 3D Icicles Hanging from Outer Edges of the Board (قندیل‌های یخی آویزان از لبه‌های بیرونی صفحه)
            val iciclePositions = listOf(
                0.06f to 0.048f,
                0.11f to 0.032f,
                0.16f to 0.054f,
                0.22f to 0.028f,
                0.46f to 0.036f,
                0.51f to 0.052f,
                0.56f to 0.030f,
                0.78f to 0.034f,
                0.84f to 0.056f,
                0.89f to 0.036f,
                0.94f to 0.048f
            )
            for ((xFrac, lenFrac) in iciclePositions) {
                val ix = w * xFrac
                val iLen = h * lenFrac
                val halfBase = w * 0.012f
                val iciclePath = Path().apply {
                    moveTo(ix - halfBase, 2f)
                    lineTo(ix + halfBase, 2f)
                    lineTo(ix, 2f + iLen)
                    close()
                }
                drawPath(
                    path = iciclePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color(0xFFB2EBF2).copy(alpha = 0.78f),
                            Color(0xFF26C6DA).copy(alpha = 0.55f)
                        ),
                        startY = 0f,
                        endY = iLen
                    )
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.95f),
                    start = Offset(ix - halfBase * 0.3f, 3f),
                    end = Offset(ix, iLen * 0.85f),
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
            }

            // Bottom-edge corner icicles hanging along the outer bottom corners
            val bottomCornerIcicles = listOf(0.03f, 0.07f, 0.93f, 0.97f)
            for (xFrac in bottomCornerIcicles) {
                val ix = w * xFrac
                val topY = h - h * 0.045f
                val iciclePath = Path().apply {
                    moveTo(ix - w * 0.010f, topY)
                    lineTo(ix + w * 0.010f, topY)
                    lineTo(ix, h - 1f)
                    close()
                }
                drawPath(
                    path = iciclePath,
                    color = Color(0xFFE0F7FA).copy(alpha = 0.78f)
                )
            }

            // 3. Moving Snow Particles in the Upper Section of the Game Board (ذرات برف متحرک در بخش بالایی صفحه بازی)
            if (isWeatherEffectsEnabled) {
                val topSnowCount = 18
                for (i in 0 until topSnowCount) {
                    val seed = i * 1.43f
                    val sx = (((seed * 0.27f + sin(phaseRad + i) * 0.03f) % 1f + 1f) % 1f) * w
                    val sy = (((seed * 0.19f + shimmerPhase * 1.3f) % 1f) * (h * 0.24f))
                    val flakeR = 1.8f + (i % 3) * 0.9f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = flakeR,
                        center = Offset(sx, sy)
                    )
                    drawCircle(
                        color = Color(0xFF80DEEA).copy(alpha = 0.38f),
                        radius = flakeR * 2.0f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }

        BoardTheme.JUNGLE -> {
            // Carved wood & moss corner vines + firefly glints
            val cornerPatches = listOf(
                Offset(10f, 10f),
                Offset(w - 10f, 10f),
                Offset(10f, h - 10f),
                Offset(w - 10f, h - 10f)
            )
            cornerPatches.forEachIndexed { idx, pos ->
                drawCircle(
                    color = Color(0xFF2E7D32).copy(alpha = 0.65f),
                    radius = w * 0.032f,
                    center = pos
                )
                drawCircle(
                    color = Color(0xFF81C784).copy(alpha = 0.75f),
                    radius = w * 0.018f,
                    center = pos
                )
                if (isWeatherEffectsEnabled) {
                    val glow = (sin(phaseRad + idx * 1.5f) * 0.5f + 0.5f)
                    drawCircle(
                        color = Color(0xFFCCFF90).copy(alpha = 0.75f * glow),
                        radius = 3.2f,
                        center = Offset(
                            pos.x + (if (idx % 2 == 0) 14f else -14f),
                            pos.y + (if (idx < 2) 14f else -14f)
                        )
                    )
                }
            }
        }

        BoardTheme.OCEAN_BEACH -> {
            // Corner pearls & coastal seafoam accents
            val corners = listOf(
                Offset(12f, 12f),
                Offset(w - 12f, 12f),
                Offset(12f, h - 12f),
                Offset(w - 12f, h - 12f)
            )
            corners.forEach { c ->
                drawCircle(
                    color = Color(0xFFFFCCBC).copy(alpha = 0.85f),
                    radius = w * 0.022f,
                    center = c
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.90f),
                    radius = w * 0.011f,
                    center = Offset(c.x - 2f, c.y - 2f)
                )
            }
        }

        BoardTheme.DESERT -> {
            // Sandstone corner studs & warm sunset rim shimmer
            val corners = listOf(
                Offset(12f, 12f),
                Offset(w - 12f, 12f),
                Offset(12f, h - 12f),
                Offset(w - 12f, h - 12f)
            )
            corners.forEach { c ->
                drawCircle(
                    color = Color(0xFFFFB74D).copy(alpha = 0.78f),
                    radius = w * 0.022f,
                    center = c
                )
                drawCircle(
                    color = Color(0xFF8D4E24).copy(alpha = 0.85f),
                    radius = w * 0.012f,
                    center = c
                )
            }
        }

        BoardTheme.BLACK_GOLD -> {
            // Royal 24k Gold Corner Filigree Studs
            val corners = listOf(
                Offset(12f, 12f),
                Offset(w - 12f, 12f),
                Offset(12f, h - 12f),
                Offset(w - 12f, h - 12f)
            )
            corners.forEach { c ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFF3B0), RadiantGold, Color(0xFF78350F)),
                        center = c,
                        radius = w * 0.025f
                    ),
                    radius = w * 0.020f,
                    center = c
                )
            }
        }
    }
}

/**
 * High-Detail 3D Medallion + Vector Illustration for all 7 Board Obstacles.
 */
private fun DrawScope.drawAnimatedObstacleOnTile(
    obstacle: BoardObstacle,
    tileCenter: Offset,
    cellW: Float,
    shimmerPhase: Float,
    labelPaint: Paint
) {
    val r = cellW * 0.34f
    val phaseRad = shimmerPhase * 2f * PI.toFloat()
    val badgeColor = obstacle.type.badgeColor

    // Outer Golden & Colored Medallion Ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                badgeColor.copy(alpha = 0.52f),
                Color.Transparent
            ),
            center = tileCenter,
            radius = r * 1.28f
        ),
        radius = r * 1.28f,
        center = tileCenter
    )
    drawCircle(
        color = Color(0xFF0A0805).copy(alpha = 0.78f),
        radius = r * 0.92f,
        center = tileCenter
    )
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF3B0), badgeColor, Color(0xFFB45309)),
            start = Offset(tileCenter.x - r, tileCenter.y - r),
            end = Offset(tileCenter.x + r, tileCenter.y + r)
        ),
        radius = r * 0.92f,
        center = tileCenter,
        style = Stroke(width = cellW * 0.038f)
    )

    when (obstacle.type) {
        ObstacleType.WORM -> {
            // Glowing toxic puddle + 3D segmented parasite worm with fangs
            val slimeRadius = r * (0.72f + 0.06f * sin(phaseRad * 2f))
            drawOval(
                color = Color(0xFF4D7C0F).copy(alpha = 0.55f),
                topLeft = Offset(tileCenter.x - slimeRadius, tileCenter.y - slimeRadius * 0.55f),
                size = Size(slimeRadius * 2f, slimeRadius * 1.1f)
            )
            val segments = 5
            for (i in segments downTo 0) {
                val frac = (i.toFloat() / segments) - 0.5f
                val wiggleY = sin(phaseRad * 2f + i * 0.9f) * (r * 0.16f)
                val segCenter = Offset(
                    x = tileCenter.x + frac * r * 1.05f,
                    y = tileCenter.y - r * 0.05f + wiggleY
                )
                val segR = r * (0.30f - i * 0.022f).coerceAtLeast(0.16f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFECFCCB), Color(0xFF84CC16), Color(0xFF365314)),
                        center = Offset(segCenter.x - segR * 0.3f, segCenter.y - segR * 0.3f),
                        radius = segR * 1.3f
                    ),
                    radius = segR,
                    center = segCenter
                )
                drawCircle(
                    color = Color(0xFFA3E635),
                    radius = segR,
                    center = segCenter,
                    style = Stroke(width = 1.0f)
                )
                if (i == 0) {
                    drawCircle(Color(0xFFFF1E56), radius = segR * 0.32f, center = Offset(segCenter.x - segR * 0.25f, segCenter.y - segR * 0.2f))
                    drawCircle(Color.White, radius = segR * 0.14f, center = Offset(segCenter.x - segR * 0.28f, segCenter.y - segR * 0.24f))
                }
            }
        }

        ObstacleType.REVERSE -> {
            // Dual rotating golden-orange vortex arrows
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEDD5), Color(0xFFF97316), Color(0xFF7C2D12)),
                    center = Offset(tileCenter.x - r * 0.2f, tileCenter.y - r * 0.2f),
                    radius = r * 0.85f
                ),
                radius = r * 0.72f,
                center = tileCenter
            )
            rotate(degrees = -shimmerPhase * 360f, pivot = tileCenter) {
                for (a in 0..1) {
                    rotate(degrees = a * 180f, pivot = tileCenter) {
                        drawArc(
                            color = Color(0xFFFFFBEB),
                            startAngle = 20f,
                            sweepAngle = 125f,
                            useCenter = false,
                            topLeft = Offset(tileCenter.x - r * 0.46f, tileCenter.y - r * 0.46f),
                            size = Size(r * 0.92f, r * 0.92f),
                            style = Stroke(width = r * 0.17f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        ObstacleType.SWAMP -> {
            // Bubbling dark emerald mire with glowing gas bubbles
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF4D7C0F), Color(0xFF1A2E05), Color(0xFF052E16)),
                    center = tileCenter,
                    radius = r * 0.85f
                ),
                topLeft = Offset(tileCenter.x - r * 0.78f, tileCenter.y - r * 0.56f),
                size = Size(r * 1.56f, r * 1.12f)
            )
            val b1Scale = 0.6f + 0.4f * sin(phaseRad)
            val b2Scale = 0.6f + 0.4f * cos(phaseRad)
            for ((bx, by, bs) in listOf(
                Triple(-0.28f, -0.10f, b1Scale),
                Triple(0.26f, 0.08f, b2Scale),
                Triple(0.0f, -0.22f, 1.2f - b1Scale * 0.5f)
            )) {
                val bc = Offset(tileCenter.x + r * bx, tileCenter.y + r * by)
                val br = r * 0.20f * bs
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFECFCCB), Color(0xFF84CC16), Color(0xFF3F6212)),
                        center = Offset(bc.x - br * 0.3f, bc.y - br * 0.3f),
                        radius = br * 1.2f
                    ),
                    radius = br,
                    center = bc
                )
            }
        }

        ObstacleType.WELL -> {
            // 3D Stone Rim Abyssal Vortex Well
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFF1E293B)),
                    center = Offset(tileCenter.x - r * 0.2f, tileCenter.y - r * 0.2f),
                    radius = r * 0.85f
                ),
                radius = r * 0.78f,
                center = tileCenter
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF4F46E5)),
                    center = tileCenter,
                    radius = r * 0.58f
                ),
                radius = r * 0.56f,
                center = tileCenter
            )
            rotate(degrees = shimmerPhase * 360f, pivot = tileCenter) {
                drawArc(
                    color = Color(0xFF818CF8).copy(alpha = 0.8f),
                    startAngle = 0f,
                    sweepAngle = 210f,
                    useCenter = false,
                    topLeft = Offset(tileCenter.x - r * 0.36f, tileCenter.y - r * 0.36f),
                    size = Size(r * 0.72f, r * 0.72f),
                    style = Stroke(width = r * 0.11f, cap = StrokeCap.Round)
                )
            }
        }

        ObstacleType.WIND -> {
            // 4-Blade Silver-Cyan Cyclone Vortex
            rotate(degrees = shimmerPhase * 360f, pivot = tileCenter) {
                for (arm in 0 until 4) {
                    rotate(degrees = arm * 90f, pivot = tileCenter) {
                        drawArc(
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White, Color(0xFF22D3EE), Color(0xFF0284C7))
                            ),
                            startAngle = 0f,
                            sweepAngle = 75f,
                            useCenter = false,
                            topLeft = Offset(tileCenter.x - r * 0.62f, tileCenter.y - r * 0.62f),
                            size = Size(r * 1.24f, r * 1.24f),
                            style = Stroke(width = r * 0.16f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            drawCircle(
                color = Color(0xFFECFEFF),
                radius = r * 0.22f,
                center = tileCenter
            )
        }

        ObstacleType.LUCK -> {
            // 3D Royal Golden Treasure / Four-Point Star Diamond
            val pulse = 0.92f + 0.10f * sin(phaseRad)
            val starR = r * 0.72f * pulse
            val starPath = Path().apply {
                moveTo(tileCenter.x, tileCenter.y - starR)
                lineTo(tileCenter.x + starR * 0.34f, tileCenter.y - starR * 0.34f)
                lineTo(tileCenter.x + starR, tileCenter.y)
                lineTo(tileCenter.x + starR * 0.34f, tileCenter.y + starR * 0.34f)
                lineTo(tileCenter.x, tileCenter.y + starR)
                lineTo(tileCenter.x - starR * 0.34f, tileCenter.y + starR * 0.34f)
                lineTo(tileCenter.x - starR, tileCenter.y)
                lineTo(tileCenter.x - starR * 0.34f, tileCenter.y - starR * 0.34f)
                close()
            }
            drawPath(
                path = starPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFBEB), RadiantGold, Color(0xFFD97706)),
                    start = Offset(tileCenter.x - starR, tileCenter.y - starR),
                    end = Offset(tileCenter.x + starR, tileCenter.y + starR)
                )
            )
            drawCircle(
                color = Color.White,
                radius = starR * 0.22f,
                center = tileCenter
            )
        }

        ObstacleType.STORM -> {
            // Electrified Storm Core with High-Voltage Golden Bolt
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFC084FC), Color(0xFF581C87), Color(0xFF1E1B4B)),
                    center = tileCenter,
                    radius = r * 0.78f
                ),
                radius = r * 0.74f,
                center = tileCenter
            )
            val boltPath = Path().apply {
                moveTo(tileCenter.x + r * 0.16f, tileCenter.y - r * 0.56f)
                lineTo(tileCenter.x - r * 0.24f, tileCenter.y + r * 0.04f)
                lineTo(tileCenter.x + r * 0.10f, tileCenter.y + r * 0.04f)
                lineTo(tileCenter.x - r * 0.18f, tileCenter.y + r * 0.58f)
            }
            drawPath(
                path = boltPath,
                color = Color(0xFFFFF3B0),
                style = Stroke(width = r * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    // Mini Persian Label Pill at bottom of obstacle tile
    val shortTitle = obstacle.type.persianName
    labelPaint.textSize = (cellW * 0.165f).coerceAtLeast(8.5f)
    labelPaint.color = android.graphics.Color.WHITE
    val pillW = cellW * 0.68f
    val pillH = cellW * 0.21f
    val pillTop = tileCenter.y + r * 0.56f
    drawRoundRect(
        color = Color(0xD90A0805),
        topLeft = Offset(tileCenter.x - pillW / 2f, pillTop),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f)
    )
    drawRoundRect(
        color = badgeColor.copy(alpha = 0.85f),
        topLeft = Offset(tileCenter.x - pillW / 2f, pillTop),
        size = Size(pillW, pillH),
        cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
        style = Stroke(width = 1.0f)
    )
    drawContext.canvas.nativeCanvas.drawText(
        shortTitle,
        tileCenter.x,
        pillTop + pillH * 0.76f,
        labelPaint
    )
}

private fun tileCenter(
    tile: Int,
    cellW: Float,
    cellH: Float,
    maxTiles: Int = 100,
    totalRows: Int = 10
): Offset {
    val (rowFromBottom, colFromLeft) = tileToGridPosition(tile, maxTiles)
    val visualRow = (totalRows - 1) - rowFromBottom
    return Offset(
        x = (colFromLeft + 0.5f) * cellW,
        y = (visualRow + 0.56f) * cellH
    )
}

private fun computeMultiTokenOffset(index: Int, totalOnTile: Int, cellW: Float): Offset {
    if (totalOnTile <= 1) return Offset.Zero
    val spread = cellW * 0.22f
    val angle = (2.0 * PI * index) / totalOnTile
    return Offset(
        x = (cos(angle) * spread).toFloat(),
        y = (sin(angle) * spread).toFloat()
    )
}

/**
 * Royal 3D Metallic/Golden Ladder with Ornate End-Caps, Riveted Rungs, and Ascending Shimmer Chevrons.
 */
private fun DrawScope.drawThreeDimensionalLadder(
    startCenter: Offset,
    endCenter: Offset,
    cellW: Float,
    shimmerPhase: Float,
    boardTheme: BoardTheme
) {
    val dx = endCenter.x - startCenter.x
    val dy = endCenter.y - startCenter.y
    val length = hypot(dx, dy).coerceAtLeast(1f)
    val ux = dx / length
    val uy = dy / length
    val px = -uy
    val py = ux

    val halfRailWidth = cellW * 0.175f
    val leftStart = Offset(startCenter.x + px * halfRailWidth, startCenter.y + py * halfRailWidth)
    val leftEnd = Offset(endCenter.x + px * halfRailWidth, endCenter.y + py * halfRailWidth)
    val rightStart = Offset(startCenter.x - px * halfRailWidth, startCenter.y - py * halfRailWidth)
    val rightEnd = Offset(endCenter.x - px * halfRailWidth, endCenter.y - py * halfRailWidth)

    // 1. Deep 3D Cast Drop Shadow on Board
    val shadowOffset = Offset(cellW * 0.09f, cellW * 0.12f)
    drawLine(
        color = Color.Black.copy(alpha = 0.52f),
        start = leftStart + shadowOffset,
        end = leftEnd + shadowOffset,
        strokeWidth = cellW * 0.115f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.52f),
        start = rightStart + shadowOffset,
        end = rightEnd + shadowOffset,
        strokeWidth = cellW * 0.115f,
        cap = StrokeCap.Round
    )

    // 2. Cylindrical 3D Rungs + Golden Rivet Joints + Ascending Chevrons
    val rungCount = (length / (cellW * 0.32f)).toInt().coerceIn(4, 14)
    for (i in 1 until rungCount) {
        val t = i.toFloat() / rungCount
        val rLeft = Offset(
            x = leftStart.x + (leftEnd.x - leftStart.x) * t,
            y = leftStart.y + (leftEnd.y - leftStart.y) * t
        )
        val rRight = Offset(
            x = rightStart.x + (rightEnd.x - rightStart.x) * t,
            y = rightStart.y + (rightEnd.y - rightStart.y) * t
        )
        val rungMid = Offset((rLeft.x + rRight.x) * 0.5f, (rLeft.y + rRight.y) * 0.5f)

        val distFromShimmer = kotlin.math.abs(t - shimmerPhase)
        val isGlowingRung = distFromShimmer < 0.16f

        // Rung cast shadow
        drawLine(
            color = Color.Black.copy(alpha = 0.42f),
            start = rLeft + shadowOffset * 0.65f,
            end = rRight + shadowOffset * 0.65f,
            strokeWidth = cellW * 0.075f,
            cap = StrokeCap.Round
        )

        // Rung dark bronze under-bevel
        drawLine(
            color = boardTheme.ladderRungShadow,
            start = rLeft + Offset(0f, 2.0f),
            end = rRight + Offset(0f, 2.0f),
            strokeWidth = cellW * 0.074f,
            cap = StrokeCap.Round
        )

        // Main metallic gold rung
        drawLine(
            color = if (isGlowingRung) Color(0xFFFFFBEB) else boardTheme.ladderRungMain,
            start = rLeft,
            end = rRight,
            strokeWidth = if (isGlowingRung) cellW * 0.076f else cellW * 0.060f,
            cap = StrokeCap.Round
        )

        // Top specular gleam on rung
        drawLine(
            color = Color.White.copy(alpha = 0.82f),
            start = rLeft - Offset(0f, 1.2f),
            end = rRight - Offset(0f, 1.2f),
            strokeWidth = cellW * 0.024f,
            cap = StrokeCap.Round
        )

        // Subtle glowing upward chevron on glowing rungs
        if (isGlowingRung) {
            val chevSize = cellW * 0.065f
            val tip = Offset(rungMid.x + ux * chevSize, rungMid.y + uy * chevSize)
            val wingL = Offset(
                rungMid.x - ux * chevSize * 0.4f + px * chevSize,
                rungMid.y - uy * chevSize * 0.4f + py * chevSize
            )
            val wingR = Offset(
                rungMid.x - ux * chevSize * 0.4f - px * chevSize,
                rungMid.y - uy * chevSize * 0.4f - py * chevSize
            )
            drawLine(Color(0xFF10B981), wingL, tip, strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawLine(Color(0xFF10B981), wingR, tip, strokeWidth = 2.4f, cap = StrokeCap.Round)
        }
    }

    // 3. Main 3D Cylindrical Side Rails (Under-bevel + Metallic Gradient + Specular Ridge)
    val railUnderColor = boardTheme.ladderRungShadow
    drawLine(
        color = railUnderColor,
        start = leftStart + Offset(1.8f, 2.5f),
        end = leftEnd + Offset(1.8f, 2.5f),
        strokeWidth = cellW * 0.110f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = railUnderColor,
        start = rightStart + Offset(1.8f, 2.5f),
        end = rightEnd + Offset(1.8f, 2.5f),
        strokeWidth = cellW * 0.110f,
        cap = StrokeCap.Round
    )

    val railBrush = Brush.linearGradient(
        colors = listOf(
            boardTheme.ladderRailStart,
            boardTheme.ladderRailMid,
            boardTheme.ladderRailEnd,
            boardTheme.ladderRailMid
        ),
        start = startCenter,
        end = endCenter
    )
    drawLine(
        brush = railBrush,
        start = leftStart,
        end = leftEnd,
        strokeWidth = cellW * 0.092f,
        cap = StrokeCap.Round
    )
    drawLine(
        brush = railBrush,
        start = rightStart,
        end = rightEnd,
        strokeWidth = cellW * 0.092f,
        cap = StrokeCap.Round
    )

    // Specular highlight along both rails
    drawLine(
        color = Color(0xFFFFFBEB).copy(alpha = 0.78f),
        start = leftStart - Offset(1.2f, 1.2f),
        end = leftEnd - Offset(1.2f, 1.2f),
        strokeWidth = cellW * 0.028f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFFFBEB).copy(alpha = 0.78f),
        start = rightStart - Offset(1.2f, 1.2f),
        end = rightEnd - Offset(1.2f, 1.2f),
        strokeWidth = cellW * 0.028f,
        cap = StrokeCap.Round
    )

    // 4. Theme-Matched Joints along the rails + 4 Ornate Finial Caps at rail ends
    val jointOuterColor = boardTheme.ladderRungShadow
    val jointInnerColor = boardTheme.ladderRailStart
    for (i in 1 until rungCount) {
        val t = i.toFloat() / rungCount
        val rLeft = Offset(
            x = leftStart.x + (leftEnd.x - leftStart.x) * t,
            y = leftStart.y + (leftEnd.y - leftStart.y) * t
        )
        val rRight = Offset(
            x = rightStart.x + (rightEnd.x - rightStart.x) * t,
            y = rightStart.y + (rightEnd.y - rightStart.y) * t
        )
        drawCircle(jointOuterColor, radius = cellW * 0.036f, center = rLeft)
        drawCircle(jointInnerColor, radius = cellW * 0.024f, center = rLeft)
        drawCircle(jointOuterColor, radius = cellW * 0.036f, center = rRight)
        drawCircle(jointInnerColor, radius = cellW * 0.024f, center = rRight)

        // Small botanical vine leaf accent on wooden ladders in Mystic Forest
        if (boardTheme == BoardTheme.JUNGLE && i % 2 == 1) {
            drawOval(
                color = Color(0xFF66BB6A).copy(alpha = 0.88f),
                topLeft = Offset(rLeft.x - cellW * 0.045f, rLeft.y - cellW * 0.022f),
                size = Size(cellW * 0.065f, cellW * 0.036f)
            )
        }
    }

    val capRadius = cellW * 0.072f
    for (capPos in listOf(leftStart, leftEnd, rightStart, rightEnd)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, boardTheme.ladderRailMid, boardTheme.ladderRungShadow),
                center = Offset(capPos.x - capRadius * 0.25f, capPos.y - capRadius * 0.25f),
                radius = capRadius * 1.2f
            ),
            radius = capRadius,
            center = capPos
        )
    }
}

/**
 * Renders a Slender, Sleek, Non-Distracting 3D Serpent customizable via [SnakeVisualTheme].
 * - Over 50% thinner than before so tile numbers, obstacles, and player tokens remain completely legible.
 * - Tight, graceful S-curve that stays along the path without sweeping across neighboring tiles.
 * - Calm, non-distracting motion and clean styling.
 */
private fun DrawScope.drawThreeDimensionalSnake(
    headCenter: Offset,
    tailCenter: Offset,
    cellW: Float,
    snakeVariant: Int,
    shimmerPhase: Float,
    isNearWormZone: Boolean = false,
    boardTheme: BoardTheme = BoardTheme.BLACK_GOLD,
    snakeVisualTheme: SnakeVisualTheme = SnakeVisualTheme.ROYAL_GOLD_SLIM,
    userThicknessScale: Float = 0.85f
) {
    val dx = tailCenter.x - headCenter.x
    val dy = tailCenter.y - headCenter.y
    val length = hypot(dx, dy).coerceAtLeast(1f)
    val px = -dy / length
    val py = dx / length

    val effectiveThickness = (snakeVisualTheme.thicknessScale * userThicknessScale).coerceIn(0.45f, 1.25f)
    val alpha = snakeVisualTheme.bodyAlpha.coerceIn(0.55f, 1.0f)

    // Subtle, tight S-curve so snakes never sprawl across adjacent tiles or distract the eye
    val waveDir = if (snakeVariant % 2 == 0) 1f else -1f
    val waveAmp = cellW * (if (isNearWormZone) 0.34f else 0.28f) * snakeVisualTheme.waveScale * waveDir

    // Gentle, calm breathing wave (or 0 in MINIMAL_SILK) so it never jitters or distracts
    val liveWave = sin(shimmerPhase * 2f * PI.toFloat() + snakeVariant) *
        (cellW * 0.022f * snakeVisualTheme.wiggleScale)

    fun spinePoint(t: Float): Offset {
        val baseLineX = headCenter.x + dx * t
        val baseLineY = headCenter.y + dy * t
        val envelope = sin(t * PI.toFloat())
        val slither = sin(t * PI.toFloat() * 2.1f) * (waveAmp + liveWave) * envelope
        return Offset(
            x = baseLineX + px * slither,
            y = baseLineY + py * slither
        )
    }

    val themePalettes = snakeVisualTheme.customPalettes ?: boardTheme.snakePalettes
    val palette: SnakeColorPalette = if (isNearWormZone) {
        SnakeColorPalette(
            highlight = Color(0xFFBEF264),
            midBody = Color(0xFFB91C1C),
            shadow = Color(0xFF3B0404),
            scaleAccent = Color(0xFFA3E635),
            bellyColor = Color(0xFFFDE047),
            eyeGlowColor = Color(0xFFA3E635)
        )
    } else {
        themePalettes[snakeVariant % themePalettes.size]
    }
    val highlightCol = palette.highlight.copy(alpha = alpha)
    val midBodyCol = palette.midBody.copy(alpha = alpha)
    val shadowCol = palette.shadow.copy(alpha = alpha)
    val scaleAccentCol = palette.scaleAccent.copy(alpha = alpha)

    val steps = (length / (cellW * 0.045f)).toInt().coerceIn(40, 96)
    val shadowOffset = Offset(cellW * 0.05f, cellW * 0.07f)

    // Slender base radius (~52% thinner than previous heavy snakes)
    val baseBodyRadius = cellW * 0.068f * effectiveThickness

    // Pass 1: Soft, subtle ground shadow
    for (i in steps downTo 0) {
        val t = i.toFloat() / steps
        val pt = spinePoint(t)
        val radiusFactor = when {
            t < 0.12f -> 0.88f + (t / 0.12f) * 0.16f
            else -> (1.04f * (1f - (t - 0.12f) / 0.88f)).coerceAtLeast(0.14f)
        }
        val r = baseBodyRadius * radiusFactor
        drawCircle(
            color = Color.Black.copy(alpha = 0.24f * alpha),
            radius = r * 1.15f,
            center = pt + shadowOffset
        )
    }

    // Pass 1.5: Delicate Golden Tip at tailCenter
    val tailTipRadius = baseBodyRadius * 0.36f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF3B0).copy(alpha = alpha), RadiantGold.copy(alpha = alpha), shadowCol),
            center = tailCenter,
            radius = tailTipRadius * 1.4f
        ),
        radius = tailTipRadius,
        center = tailCenter
    )

    // Pass 2: Slender 3D Cylindrical Body + Optional Subtle Scales
    for (i in steps downTo 1) {
        val t = i.toFloat() / steps
        val pt = spinePoint(t)
        val prevPt = spinePoint((t - 0.015f).coerceAtLeast(0f))
        val segAngle = atan2(pt.y - prevPt.y, pt.x - prevPt.x)
        val segPerp = segAngle + PI.toFloat() / 2f

        val radiusFactor = when {
            t < 0.12f -> 0.90f + (t / 0.12f) * 0.16f
            else -> (1.06f * (1f - (t - 0.12f) / 0.88f)).coerceAtLeast(0.15f)
        }
        val r = baseBodyRadius * radiusFactor

        // Optional slim cobra hood only if enabled in SnakeVisualTheme
        if (snakeVisualTheme.showHood && t in 0.04f..0.16f) {
            val hoodSpread = sin(((t - 0.04f) / 0.12f) * PI.toFloat())
            val wingDist = r * (0.55f * hoodSpread)
            val leftHood = Offset(pt.x + cos(segPerp) * wingDist, pt.y + sin(segPerp) * wingDist)
            val rightHood = Offset(pt.x - cos(segPerp) * wingDist, pt.y - sin(segPerp) * wingDist)
            for (wingPt in listOf(leftHood, rightHood)) {
                drawCircle(
                    color = midBodyCol,
                    radius = r * 0.65f,
                    center = wingPt
                )
            }
        }

        val lightSpot = Offset(pt.x - r * 0.32f, pt.y - r * 0.35f)

        // Crisp thin outer contour
        drawCircle(
            color = shadowCol,
            radius = r * 1.05f,
            center = pt
        )

        // Main slender 3D body segment
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(highlightCol, midBodyCol, shadowCol),
                center = lightSpot,
                radius = r * 1.45f
            ),
            radius = r,
            center = pt
        )

        // Delicate diamond dorsal pattern (only if showScales is enabled)
        if (snakeVisualTheme.showScales && i % 4 == 0 && t in 0.08f..0.88f) {
            val dR = r * 0.42f
            val dPath = Path().apply {
                moveTo(pt.x + cos(segAngle) * dR, pt.y + sin(segAngle) * dR)
                lineTo(pt.x + cos(segPerp) * (dR * 0.58f), pt.y + sin(segPerp) * (dR * 0.58f))
                lineTo(pt.x - cos(segAngle) * dR, pt.y - sin(segAngle) * dR)
                lineTo(pt.x - cos(segPerp) * (dR * 0.58f), pt.y - sin(segPerp) * (dR * 0.58f))
                close()
            }
            drawPath(
                path = dPath,
                color = scaleAccentCol.copy(alpha = 0.72f * alpha)
            )
        }
    }

    // Pass 3: Compact, Sleek 3D Serpent Head
    val neckPt = spinePoint(0.07f)
    val headDirAngle = atan2(headCenter.y - neckPt.y, headCenter.x - neckPt.x)
    val perpAngle = headDirAngle + PI.toFloat() / 2f

    val headRadius = cellW * 0.105f * effectiveThickness

    // Subtle short forked tongue
    val tongueLen = headRadius * 1.65f
    val tongueBase = Offset(
        x = headCenter.x + cos(headDirAngle) * (headRadius * 0.75f),
        y = headCenter.y + sin(headDirAngle) * (headRadius * 0.75f)
    )
    val tongueSplit = Offset(
        x = headCenter.x + cos(headDirAngle) * tongueLen,
        y = headCenter.y + sin(headDirAngle) * tongueLen
    )
    val forkLeft = Offset(
        x = tongueSplit.x + cos(headDirAngle - 0.45f) * (headRadius * 0.42f),
        y = tongueSplit.y + sin(headDirAngle - 0.45f) * (headRadius * 0.42f)
    )
    val forkRight = Offset(
        x = tongueSplit.x + cos(headDirAngle + 0.45f) * (headRadius * 0.42f),
        y = tongueSplit.y + sin(headDirAngle + 0.45f) * (headRadius * 0.42f)
    )

    drawLine(Color(0xFFFF3366).copy(alpha = alpha), tongueBase, tongueSplit, strokeWidth = 1.8f, cap = StrokeCap.Round)
    drawLine(Color(0xFFFF3366).copy(alpha = alpha), tongueSplit, forkLeft, strokeWidth = 1.4f, cap = StrokeCap.Round)
    drawLine(Color(0xFFFF3366).copy(alpha = alpha), tongueSplit, forkRight, strokeWidth = 1.4f, cap = StrokeCap.Round)

    // Optional small fangs only when showFangs is enabled
    if (snakeVisualTheme.showFangs) {
        val fangSpread = headRadius * 0.42f
        val fangBaseForward = headRadius * 0.65f
        val fangTipForward = headRadius * 1.25f
        for (side in listOf(-1f, 1f)) {
            val fBase = Offset(
                x = headCenter.x + cos(headDirAngle) * fangBaseForward + cos(perpAngle) * (fangSpread * side),
                y = headCenter.y + sin(headDirAngle) * fangBaseForward + sin(perpAngle) * (fangSpread * side)
            )
            val fTip = Offset(
                x = headCenter.x + cos(headDirAngle) * fangTipForward + cos(perpAngle) * (fangSpread * side * 0.85f),
                y = headCenter.y + sin(headDirAngle) * fangTipForward + sin(perpAngle) * (fangSpread * side * 0.85f)
            )
            drawLine(
                color = Color(0xFFFFFBEB).copy(alpha = alpha),
                start = fBase,
                end = fTip,
                strokeWidth = 2.0f,
                cap = StrokeCap.Round
            )
        }
    }

    val snoutTip = Offset(
        x = headCenter.x + cos(headDirAngle) * (headRadius * 0.45f),
        y = headCenter.y + sin(headDirAngle) * (headRadius * 0.45f)
    )

    // Head Drop Shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.40f * alpha),
        radius = headRadius * 1.15f,
        center = headCenter + shadowOffset * 0.7f
    )

    // Sleek Cranium & Tapered Snout
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(highlightCol, midBodyCol, shadowCol),
            center = Offset(headCenter.x - headRadius * 0.3f, headCenter.y - headRadius * 0.3f),
            radius = headRadius * 1.35f
        ),
        radius = headRadius,
        center = headCenter
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(highlightCol, midBodyCol, shadowCol),
            center = Offset(snoutTip.x - headRadius * 0.2f, snoutTip.y - headRadius * 0.2f),
            radius = headRadius * 0.95f
        ),
        radius = headRadius * 0.74f,
        center = snoutTip
    )

    // Subtle Crown Dot on Forehead
    if (snakeVisualTheme.showScales) {
        drawCircle(
            color = scaleAccentCol,
            radius = headRadius * 0.22f,
            center = headCenter
        )
    }

    // Compact Glowing Reptile Eyes
    val eyeSpread = headRadius * 0.54f
    val eyeForward = headRadius * 0.18f
    val leftEye = Offset(
        x = headCenter.x + cos(headDirAngle) * eyeForward + cos(perpAngle) * eyeSpread,
        y = headCenter.y + sin(headDirAngle) * eyeForward + sin(perpAngle) * eyeSpread
    )
    val rightEye = Offset(
        x = headCenter.x + cos(headDirAngle) * eyeForward - cos(perpAngle) * eyeSpread,
        y = headCenter.y + sin(headDirAngle) * eyeForward - sin(perpAngle) * eyeSpread
    )
    val eyeRadius = headRadius * 0.28f

    for (eyePos in listOf(leftEye, rightEye)) {
        drawCircle(Color.Black.copy(alpha = 0.75f * alpha), radius = eyeRadius * 1.2f, center = eyePos)
        drawCircle(
            color = palette.eyeGlowColor.copy(alpha = alpha),
            radius = eyeRadius,
            center = eyePos
        )
        drawCircle(
            color = Color.Black.copy(alpha = alpha),
            radius = eyeRadius * 0.42f,
            center = eyePos
        )
    }
}

private fun DrawScope.drawThreeDimensionalPawnToken(
    center: Offset,
    radius: Float,
    player: Player,
    isCurrentTurn: Boolean,
    isLeader: Boolean,
    pulseScale: Float,
    shimmerPhase: Float,
    squashScaleX: Float = 1f,
    squashScaleY: Float = 1f,
    namePaint: Paint? = null
) = withTransform({
    if (squashScaleX != 1f || squashScaleY != 1f) {
        scale(scaleX = squashScaleX, scaleY = squashScaleY, pivot = Offset(center.x, center.y + radius * 0.35f))
    }
}) {
    val r = radius * pulseScale

    if (isCurrentTurn || isLeader) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (isLeader) RadiantGold.copy(alpha = 0.72f) else player.color.copy(alpha = 0.62f),
                    Color.Transparent
                ),
                center = center,
                radius = r * 1.9f
            ),
            radius = r * 1.9f,
            center = center
        )

        if (isLeader) {
            val sparkAngle = shimmerPhase * 2f * PI.toFloat()
            val sparkPos = Offset(
                x = center.x + cos(sparkAngle) * (r * 1.3f),
                y = center.y + sin(sparkAngle) * (r * 1.3f)
            )
            drawCircle(Color(0xFFFFF3B0), radius = r * 0.25f, center = sparkPos)
        }
    }

    if (player.character.abilityType == com.example.model.CharacterAbilityType.GUARDIAN_SHIELD && player.isShieldCharged) {
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.88f),
            radius = r * 1.20f,
            center = center,
            style = Stroke(width = 2.5f)
        )
    }

    if (player.guardianAngelProtected || player.phantomPhaseActive) {
        drawCircle(
            color = Color(0xFFFDE047).copy(alpha = 0.90f),
            radius = r * 1.26f,
            center = center,
            style = Stroke(width = 2.3f)
        )
    }

    if (player.dragonSoulBoostTurns > 0 || player.berserkerRageTurns > 0) {
        drawCircle(
            color = Color(0xFFF97316).copy(alpha = 0.90f),
            radius = r * 1.30f,
            center = center,
            style = Stroke(width = 2.4f)
        )
    }

    if (player.hasActiveDebuff) {
        val debuffColor = when {
            player.frozenSkipTurnsRemaining > 0 -> Color(0xFF60A5FA)
            player.swampSkipTurnsRemaining > 0 || player.halveNextRoll -> Color(0xFF65A30D)
            player.gravityCapNextRoll != null || player.pinnedSlowSteps > 0 -> Color(0xFFA855F7)
            else -> Color(0xFFA3E635)
        }
        drawCircle(
            color = debuffColor.copy(alpha = 0.85f),
            radius = r * 1.25f,
            center = center,
            style = Stroke(width = 2.2f)
        )
    }

    drawOval(
        color = Color.Black.copy(alpha = 0.55f),
        topLeft = Offset(center.x - r * 1.05f, center.y + r * 0.35f),
        size = Size(r * 2.1f, r * 0.85f)
    )

    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF3B0), Color(0xFFB45309), Color(0xFFFFB703)),
            start = Offset(center.x - r, center.y),
            end = Offset(center.x + r, center.y)
        ),
        topLeft = Offset(center.x - r * 0.95f, center.y + r * 0.18f),
        size = Size(r * 1.9f, r * 0.72f)
    )

    val bodyCenter = Offset(center.x, center.y - r * 0.10f)
    val darkShade = Color(
        red = (player.color.red * 0.38f).coerceIn(0f, 1f),
        green = (player.color.green * 0.38f).coerceIn(0f, 1f),
        blue = (player.color.blue * 0.38f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val brightHighlight = Color(
        red = (player.color.red + (1f - player.color.red) * 0.65f).coerceIn(0f, 1f),
        green = (player.color.green + (1f - player.color.green) * 0.65f).coerceIn(0f, 1f),
        blue = (player.color.blue + (1f - player.color.blue) * 0.65f).coerceIn(0f, 1f),
        alpha = 1f
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(brightHighlight, player.color, darkShade),
            center = Offset(bodyCenter.x - r * 0.32f, bodyCenter.y - r * 0.35f),
            radius = r * 1.35f
        ),
        radius = r * 0.88f,
        center = bodyCenter
    )

    drawCircle(
        color = RadiantGold.copy(alpha = 0.8f),
        radius = r * 0.88f,
        center = bodyCenter,
        style = Stroke(width = 1.7f)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.82f),
        radius = r * 0.28f,
        center = Offset(bodyCenter.x - r * 0.28f, bodyCenter.y - r * 0.30f)
    )

    if (namePaint != null) {
        val shortTag = player.name.take(6)
        namePaint.textSize = (r * 0.68f).coerceAtLeast(10f)
        namePaint.color = android.graphics.Color.WHITE
        val textWidth = namePaint.measureText(shortTag)
        val badgeW = (textWidth + r * 0.55f).coerceAtLeast(r * 1.4f)
        val badgeH = r * 0.82f
        val badgeTop = bodyCenter.y - r * 1.65f

        drawRoundRect(
            color = Color(0xD90A0805),
            topLeft = Offset(center.x - badgeW / 2f, badgeTop),
            size = Size(badgeW, badgeH),
            cornerRadius = CornerRadius(badgeH / 2f, badgeH / 2f)
        )
        drawRoundRect(
            color = player.color,
            topLeft = Offset(center.x - badgeW / 2f, badgeTop),
            size = Size(badgeW, badgeH),
            cornerRadius = CornerRadius(badgeH / 2f, badgeH / 2f),
            style = Stroke(width = 1.4f)
        )
        drawContext.canvas.nativeCanvas.drawText(
            shortTag,
            center.x,
            badgeTop + badgeH * 0.74f,
            namePaint
        )
    }
}
