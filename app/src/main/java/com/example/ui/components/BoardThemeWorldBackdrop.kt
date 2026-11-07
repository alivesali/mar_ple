package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.model.BoardTheme
import com.example.model.EffectIntensity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen living world backdrop rendered behind the Snakes & Ladders board.
 * Each of the 5 themes creates a distinct, living world around the game board while keeping
 * gameplay, tiles, numbers, snakes, ladders, and tokens 100% legible.
 */
@Composable
fun BoardThemeWorldBackdrop(
    boardTheme: BoardTheme,
    isEnvironmentEffectsEnabled: Boolean = true,
    particleDensity: EffectIntensity = EffectIntensity.MEDIUM,
    isAmbientAnimationEnabled: Boolean = true,
    backgroundLightingLevel: EffectIntensity = EffectIntensity.MEDIUM,
    isWeatherEffectsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "world_backdrop_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "world_phase"
    )
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "world_breathe"
    )

    val phase = if (isAmbientAnimationEnabled) animProgress else 0.35f
    val pulse = if (isAmbientAnimationEnabled) breathe else 0.5f
    val lightMult = when (backgroundLightingLevel) {
        EffectIntensity.LOW -> 0.55f
        EffectIntensity.MEDIUM -> 1.0f
        EffectIntensity.HIGH -> 1.45f
    }
    val densityMult = when (particleDensity) {
        EffectIntensity.LOW -> 0.5f
        EffectIntensity.MEDIUM -> 1.0f
        EffectIntensity.HIGH -> 1.6f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Deep Atmospheric Sky & Horizon Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(boardTheme.screenBgTop, boardTheme.screenBgBottom)
            ),
            size = size
        )

        // 2. Theme-Specific World Scenery Behind & Around the Board
        when (boardTheme) {
            BoardTheme.BLACK_GOLD -> drawBlackGoldWorld(
                phase = phase,
                pulse = pulse,
                lightMult = lightMult,
                densityMult = densityMult,
                showEffects = isEnvironmentEffectsEnabled
            )
            BoardTheme.JUNGLE -> drawMysticForestWorld(
                phase = phase,
                pulse = pulse,
                lightMult = lightMult,
                densityMult = densityMult,
                showEffects = isEnvironmentEffectsEnabled,
                showWeather = isWeatherEffectsEnabled
            )
            BoardTheme.OCEAN_BEACH -> drawOceanBeachWorld(
                phase = phase,
                pulse = pulse,
                lightMult = lightMult,
                densityMult = densityMult,
                showEffects = isEnvironmentEffectsEnabled,
                showWeather = isWeatherEffectsEnabled
            )
            BoardTheme.DESERT -> drawDesertDunesWorld(
                phase = phase,
                pulse = pulse,
                lightMult = lightMult,
                densityMult = densityMult,
                showEffects = isEnvironmentEffectsEnabled,
                showWeather = isWeatherEffectsEnabled
            )
            BoardTheme.ARCTIC_ICE -> drawArcticIceWorld(
                phase = phase,
                pulse = pulse,
                lightMult = lightMult,
                densityMult = densityMult,
                showEffects = isEnvironmentEffectsEnabled,
                showWeather = isWeatherEffectsEnabled
            )
        }
    }
}

/**
 * Miniature animated world + tile palette showcase for each theme card inside the
 * "Game Themes" Settings section.
 */
@Composable
fun ThemeWorldMiniaturePreview(
    boardTheme: BoardTheme,
    isAmbientAnimationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, boardTheme.boardBorderMain.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
    ) {
        BoardThemeWorldBackdrop(
            boardTheme = boardTheme,
            isEnvironmentEffectsEnabled = true,
            particleDensity = EffectIntensity.MEDIUM,
            isAmbientAnimationEnabled = isAmbientAnimationEnabled,
            backgroundLightingLevel = EffectIntensity.HIGH,
            isWeatherEffectsEnabled = true,
            modifier = Modifier.fillMaxSize()
        )

        // Mini Board Tile Strip in the center-bottom to preview actual tile colors & material
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
                .background(
                    color = boardTheme.boardFrameOuterMid.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(
                    width = 1.dp,
                    color = boardTheme.boardBorderMain.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            boardTheme.tilePalette.forEachIndexed { idx, (topColor, bottomColor) ->
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(topColor, bottomColor)
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = if (idx == 0) boardTheme.primaryAccent else boardTheme.tileBorderColor,
                            shape = RoundedCornerShape(5.dp)
                        )
                )
            }
        }
    }
}

private fun DrawScope.drawBlackGoldWorld(
    phase: Float,
    pulse: Float,
    lightMult: Float,
    densityMult: Float,
    showEffects: Boolean
) {
    val w = size.width
    val h = size.height

    // Royal golden radial crown aura behind the board
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD54F).copy(alpha = (0.14f + 0.05f * pulse) * lightMult.coerceAtMost(1.5f)),
                Color(0xFFB8860B).copy(alpha = 0.05f * lightMult),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.45f),
            radius = w * 0.75f
        ),
        radius = w * 0.75f,
        center = Offset(w * 0.5f, h * 0.45f)
    )

    // Subtle luxury architectural arches / royal geometric lines at top & bottom corners
    val lineColor = Color(0xFFD4AF37).copy(alpha = (0.11f + 0.04f * pulse) * lightMult.coerceAtMost(1.4f))
    for (i in 0..3) {
        val inset = (i + 1) * 26f
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(inset, inset),
            size = Size((w - inset * 2).coerceAtLeast(10f), (h - inset * 2).coerceAtLeast(10f)),
            cornerRadius = CornerRadius(32f, 32f),
            style = Stroke(width = if (i == 0) 1.6f else 0.8f)
        )
    }

    // Golden light reflection beams from corners
    val beamAlpha = (0.09f * lightMult).coerceIn(0f, 0.25f)
    val leftBeam = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.45f, h * 0.35f)
        lineTo(w * 0.20f, h * 0.50f)
        close()
    }
    drawPath(
        path = leftBeam,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFE082).copy(alpha = beamAlpha), Color.Transparent),
            start = Offset.Zero,
            end = Offset(w * 0.45f, h * 0.5f)
        )
    )

    // Fine golden dust particles in the environment
    if (showEffects) {
        val count = (24 * densityMult).toInt().coerceIn(8, 45)
        for (i in 0 until count) {
            val seed = i * 1.618f
            val px = ((sin(seed * 4.3f + phase * 2f * PI.toFloat()) * 0.5f + 0.5f) * w)
            val py = (((seed * 0.23f + phase) % 1f) * h)
            val r = 1.6f + (i % 3) * 1.1f
            val alpha = (0.28f + 0.35f * sin((phase * 2f * PI + i).toFloat()).let { it * 0.5f + 0.5f })
            drawCircle(
                color = Color(0xFFFFECB3).copy(alpha = alpha * 0.35f),
                radius = r * 2.6f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = alpha),
                radius = r,
                center = Offset(px, py)
            )
        }
    }
}

private fun DrawScope.drawMysticForestWorld(
    phase: Float,
    pulse: Float,
    lightMult: Float,
    densityMult: Float,
    showEffects: Boolean,
    showWeather: Boolean
) {
    val w = size.width
    val h = size.height
    val sway = sin(phase * 2f * PI.toFloat()) * 10f

    // 1. Gentle forest mist in background
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF66BB6A).copy(alpha = (0.14f + 0.04f * pulse) * lightMult.coerceAtMost(1.4f)),
                Color(0xFF1B5E20).copy(alpha = 0.07f * lightMult),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.38f),
            radius = w * 0.8f
        ),
        radius = w * 0.8f,
        center = Offset(w * 0.5f, h * 0.38f)
    )

    // 2. Filtered sunbeams through tree canopy (نور خورشید که از بین شاخه‌ها می‌تابد)
    val sunbeamAlpha = (0.11f * lightMult).coerceIn(0.03f, 0.24f)
    for (b in 0..2) {
        val startX = w * (0.15f + b * 0.28f) + sway * 0.6f
        val beamPath = Path().apply {
            moveTo(startX, 0f)
            lineTo(startX + w * 0.14f, 0f)
            lineTo(startX + w * 0.34f, h * 0.78f)
            lineTo(startX + w * 0.08f, h * 0.78f)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE6EE9C).copy(alpha = sunbeamAlpha),
                    Color(0xFF81C784).copy(alpha = sunbeamAlpha * 0.4f),
                    Color.Transparent
                )
            )
        )
    }

    // 3. Sparkling forest stream/river winding behind & beside the board (جویبار یا رودخانه کوچک)
    val streamPath = Path().apply {
        moveTo(w * 0.05f, h * 0.12f)
        cubicTo(
            w * 0.28f, h * 0.32f,
            w * 0.02f, h * 0.65f,
            w * 0.32f, h * 0.96f
        )
        lineTo(w * 0.45f, h * 0.98f)
        cubicTo(
            w * 0.16f, h * 0.65f,
            w * 0.40f, h * 0.32f,
            w * 0.16f, h * 0.10f
        )
        close()
    }
    drawPath(
        path = streamPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF26C6DA).copy(alpha = 0.22f),
                Color(0xFF00ACC1).copy(alpha = 0.30f),
                Color(0xFF00838F).copy(alpha = 0.25f)
            )
        )
    )
    // Water sparkles on the forest stream
    for (s in 0..9) {
        val t = ((s * 0.11f + phase * 1.4f) % 1f)
        val sx = w * (0.12f + 0.16f * sin(t * 5f))
        val sy = h * (0.14f + t * 0.80f)
        drawCircle(
            color = Color(0xFFE0F7FA).copy(alpha = 0.45f * lightMult.coerceAtMost(1.3f)),
            radius = 2.4f,
            center = Offset(sx, sy)
        )
    }

    // 4. Large ancient forest trees on left & right sides (درختان بزرگ و طبیعی اطراف صفحه)
    drawForestTreeSilhouettes(w, h, sway)

    // 5. Natural stones, moss & ferns at bottom corners (سنگ‌های طبیعی، خزه و گیاهان جنگلی)
    drawForestRocksAndFerns(w, h, sway)

    // 6. Corner branches & leaves swaying gently
    drawForestCornerBranches(w, h, sway)

    // 7. Fireflies, glowing green particles & floating leaves (کرم‌های شب‌تاب و ذرات سبز نورانی)
    if (showEffects) {
        val count = (22 * densityMult).toInt().coerceIn(8, 40)
        for (i in 0 until count) {
            val seed = i * 2.17f
            val fx = ((cos(seed + phase * 2f * PI.toFloat()) * 0.46f + 0.5f) * w)
            val fy = (((seed * 0.19f + 1f - phase * 0.6f) % 1f) * h)
            val glow = (sin((phase * 4f * PI + i).toFloat()) * 0.5f + 0.5f)
            drawCircle(
                color = Color(0xFFB9F6CA).copy(alpha = 0.28f * glow),
                radius = 7.5f,
                center = Offset(fx, fy)
            )
            drawCircle(
                color = Color(0xFFCCFF90).copy(alpha = 0.75f * glow),
                radius = 2.5f,
                center = Offset(fx, fy)
            )
        }
    }

    // Subtle drifting mist ribbons if weather effects are enabled
    if (showWeather) {
        for (m in 0..2) {
            val my = h * (0.22f + m * 0.28f)
            val mx = ((phase * 0.4f + m * 0.33f) % 1f) * w * 0.4f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFA5D6A7).copy(alpha = 0.08f * lightMult.coerceAtMost(1.3f)),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(mx, my),
                size = Size(w * 0.65f, 34f),
                cornerRadius = CornerRadius(18f, 18f)
            )
        }
    }
}

private fun DrawScope.drawForestTreeSilhouettes(w: Float, h: Float, sway: Float) {
    val trunkLeft = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.05f)
        quadraticTo(w * 0.11f, h * 0.28f, w * 0.07f, h * 0.62f)
        quadraticTo(w * 0.12f, h * 0.85f, w * 0.16f, h)
        close()
    }
    drawPath(trunkLeft, color = Color(0xFF1B301E).copy(alpha = 0.78f))

    val trunkRight = Path().apply {
        moveTo(w, h)
        lineTo(w, h * 0.06f)
        quadraticTo(w * 0.89f, h * 0.30f, w * 0.93f, h * 0.64f)
        quadraticTo(w * 0.88f, h * 0.86f, w * 0.84f, h)
        close()
    }
    drawPath(trunkRight, color = Color(0xFF172A1A).copy(alpha = 0.78f))

    // Canopy clusters at top left & right
    val canopyCenters = listOf(
        Offset(w * 0.08f + sway * 0.4f, h * 0.08f),
        Offset(w * 0.22f + sway * 0.5f, h * 0.04f),
        Offset(w * 0.92f - sway * 0.4f, h * 0.09f),
        Offset(w * 0.78f - sway * 0.5f, h * 0.04f)
    )
    canopyCenters.forEachIndexed { idx, center ->
        drawCircle(
            color = if (idx % 2 == 0) Color(0xFF1B5E20).copy(alpha = 0.55f) else Color(0xFF2E7D32).copy(alpha = 0.48f),
            radius = w * 0.16f,
            center = center
        )
    }
}

private fun DrawScope.drawForestRocksAndFerns(w: Float, h: Float, sway: Float) {
    // Mossy stones at bottom left & right
    drawOval(
        color = Color(0xFF37474F).copy(alpha = 0.75f),
        topLeft = Offset(w * 0.02f, h * 0.89f),
        size = Size(w * 0.22f, h * 0.09f)
    )
    drawOval(
        color = Color(0xFF43A047).copy(alpha = 0.48f),
        topLeft = Offset(w * 0.04f, h * 0.885f),
        size = Size(w * 0.16f, h * 0.04f)
    )

    drawOval(
        color = Color(0xFF263238).copy(alpha = 0.75f),
        topLeft = Offset(w * 0.76f, h * 0.90f),
        size = Size(w * 0.22f, h * 0.085f)
    )
    drawOval(
        color = Color(0xFF66BB6A).copy(alpha = 0.42f),
        topLeft = Offset(w * 0.79f, h * 0.895f),
        size = Size(w * 0.15f, h * 0.038f)
    )

    // Ferns swaying near corners
    for (f in 0..4) {
        val angle = -25f + f * 12f + sway * 0.5f
        rotate(degrees = angle, pivot = Offset(w * 0.10f, h * 0.92f)) {
            drawOval(
                color = Color(0xFF4CAF50).copy(alpha = 0.55f),
                topLeft = Offset(w * 0.09f, h * 0.82f),
                size = Size(12f, h * 0.10f)
            )
        }
        rotate(degrees = -angle, pivot = Offset(w * 0.90f, h * 0.92f)) {
            drawOval(
                color = Color(0xFF66BB6A).copy(alpha = 0.55f),
                topLeft = Offset(w * 0.89f, h * 0.82f),
                size = Size(12f, h * 0.10f)
            )
        }
    }
}

private fun DrawScope.drawForestCornerBranches(w: Float, h: Float, sway: Float) {
    // Hanging leafy vine branches at top corners
    for (v in 0..3) {
        val xLeft = w * (0.04f + v * 0.06f) + sway * 0.35f
        val len = h * (0.09f + (v % 2) * 0.04f)
        drawLine(
            color = Color(0xFF33691E).copy(alpha = 0.65f),
            start = Offset(xLeft, 0f),
            end = Offset(xLeft + sway * 0.4f, len),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawOval(
            color = Color(0xFF81C784).copy(alpha = 0.72f),
            topLeft = Offset(xLeft + sway * 0.4f - 6f, len - 6f),
            size = Size(14f, 8f)
        )

        val xRight = w * (0.96f - v * 0.06f) - sway * 0.35f
        drawLine(
            color = Color(0xFF33691E).copy(alpha = 0.65f),
            start = Offset(xRight, 0f),
            end = Offset(xRight - sway * 0.4f, len),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawOval(
            color = Color(0xFFA5D6A7).copy(alpha = 0.72f),
            topLeft = Offset(xRight - sway * 0.4f - 6f, len - 6f),
            size = Size(14f, 8f)
        )
    }
}

private fun DrawScope.drawOceanBeachWorld(
    phase: Float,
    pulse: Float,
    lightMult: Float,
    densityMult: Float,
    showEffects: Boolean,
    showWeather: Boolean
) {
    val w = size.width
    val h = size.height
    val waveShift = sin(phase * 2f * PI.toFloat()) * 14f

    // 1. Turquoise sunlight reflection on the water (انعکاس نور خورشید روی آب)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF80DEEA).copy(alpha = (0.24f + 0.06f * pulse) * lightMult.coerceAtMost(1.4f)),
                Color(0xFF00ACC1).copy(alpha = 0.10f * lightMult),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.16f),
            radius = w * 0.72f
        ),
        radius = w * 0.72f,
        center = Offset(w * 0.5f, h * 0.16f)
    )

    // 2. Calm rolling ocean waves in the upper & middle background (دریا و موج‌های آرام در پس‌زمینه)
    if (showWeather) {
        for (layer in 0..3) {
            val baseY = h * (0.14f + layer * 0.14f) + waveShift * (layer + 1) * 0.25f
            val wavePath = Path().apply {
                moveTo(0f, baseY)
                cubicTo(
                    w * 0.25f, baseY - 18f - layer * 4f,
                    w * 0.75f, baseY + 18f + layer * 4f,
                    w, baseY - 6f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            val waveColor = when (layer) {
                0 -> Color(0xFF0097A7).copy(alpha = 0.26f)
                1 -> Color(0xFF00ACC1).copy(alpha = 0.24f)
                2 -> Color(0xFF26C6DA).copy(alpha = 0.22f)
                else -> Color(0xFF4DD0E1).copy(alpha = 0.22f)
            }
            drawPath(wavePath, color = waveColor)

            // White seafoam crest line
            val foamPath = Path().apply {
                moveTo(0f, baseY)
                cubicTo(
                    w * 0.25f, baseY - 18f - layer * 4f,
                    w * 0.75f, baseY + 18f + layer * 4f,
                    w, baseY - 6f
                )
            }
            drawPath(
                path = foamPath,
                color = Color(0xFFE0F7FA).copy(alpha = 0.35f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round)
            )
        }
    }

    // 3. Warm golden sandy beach shoreline at the bottom & corners (ساحل شنی در اطراف زمین بازی)
    val sandPath = Path().apply {
        moveTo(0f, h * 0.76f + waveShift * 0.3f)
        cubicTo(
            w * 0.32f, h * 0.71f,
            w * 0.68f, h * 0.80f,
            w, h * 0.74f - waveShift * 0.3f
        )
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = sandPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF5E6C8).copy(alpha = 0.38f),
                Color(0xFFE6C994).copy(alpha = 0.52f),
                Color(0xFFD7B377).copy(alpha = 0.62f)
            ),
            startY = h * 0.72f,
            endY = h
        )
    )

    // 4. Delicate palm silhouettes at top-left & top-right corners (نخل‌های ظریف در محیط اطراف)
    drawPalmFronds(w, h, waveShift * 0.5f)

    // 5. Seashells, starfish & sea stones in the corners (صدف‌ها و سنگ‌های دریایی در گوشه‌ها)
    drawBeachShellsAndStarfish(w, h)

    // 6. Tiny bubbles & sparkling marine particles (حباب‌های ریز و ذرات نورانی دریایی)
    if (showEffects) {
        val count = (24 * densityMult).toInt().coerceIn(8, 42)
        for (i in 0 until count) {
            val seed = i * 1.91f
            val bx = ((sin(seed * 3.1f + phase * 2f * PI.toFloat()) * 0.45f + 0.5f) * w)
            val by = (((1f - phase + seed * 0.17f) % 1f) * h)
            val r = 2.2f + (i % 4) * 1.4f
            drawCircle(
                color = Color(0xFFE0F7FA).copy(alpha = 0.42f),
                radius = r,
                center = Offset(bx, by),
                style = Stroke(width = 1.2f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = r * 0.32f,
                center = Offset(bx - r * 0.28f, by - r * 0.28f)
            )
        }
    }
}

private fun DrawScope.drawPalmFronds(w: Float, h: Float, sway: Float) {
    val frondColor = Color(0xFF004D40).copy(alpha = 0.55f)
    val frondHighlight = Color(0xFF26A69A).copy(alpha = 0.42f)
    for (i in 0..3) {
        val angleLeft = 15f + i * 18f + sway * 0.4f
        rotate(degrees = angleLeft, pivot = Offset(0f, h * 0.08f)) {
            drawOval(
                color = if (i % 2 == 0) frondColor else frondHighlight,
                topLeft = Offset(-10f, h * 0.06f),
                size = Size(w * 0.26f, 16f)
            )
        }
        val angleRight = -15f - i * 18f - sway * 0.4f
        rotate(degrees = angleRight, pivot = Offset(w, h * 0.08f)) {
            drawOval(
                color = if (i % 2 == 0) frondColor else frondHighlight,
                topLeft = Offset(w - w * 0.26f + 10f, h * 0.06f),
                size = Size(w * 0.26f, 16f)
            )
        }
    }
}

private fun DrawScope.drawBeachShellsAndStarfish(w: Float, h: Float) {
    // Left corner seashell
    val shellCenter = Offset(w * 0.11f, h * 0.92f)
    drawCircle(
        color = Color(0xFFFFCCBC).copy(alpha = 0.78f),
        radius = 13f,
        center = shellCenter
    )
    for (r in -2..2) {
        drawLine(
            color = Color(0xFFD84315).copy(alpha = 0.45f),
            start = Offset(shellCenter.x, shellCenter.y + 10f),
            end = Offset(shellCenter.x + r * 4.5f, shellCenter.y - 9f),
            strokeWidth = 1.4f
        )
    }

    // Right corner coral starfish
    val starCenter = Offset(w * 0.88f, h * 0.91f)
    for (arm in 0 until 5) {
        val rad = (arm * 72.0 - 90.0) * PI / 180.0
        val end = Offset(
            starCenter.x + (cos(rad) * 14f).toFloat(),
            starCenter.y + (sin(rad) * 14f).toFloat()
        )
        drawLine(
            color = Color(0xFFFF8A65).copy(alpha = 0.82f),
            start = starCenter,
            end = end,
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
    drawCircle(
        color = Color(0xFFFFF3E0),
        radius = 3f,
        center = starCenter
    )
}

private fun DrawScope.drawDesertDunesWorld(
    phase: Float,
    pulse: Float,
    lightMult: Float,
    densityMult: Float,
    showEffects: Boolean,
    showWeather: Boolean
) {
    val w = size.width
    val h = size.height
    val shimmer = sin(phase * 2f * PI.toFloat()) * 8f

    // 1. Warm Sunset Sun with Orange/Copper Glow (خورشید گرم غروب با نور نارنجی و طلایی)
    val sunCenter = Offset(w * 0.76f, h * 0.17f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFE082).copy(alpha = (0.48f + 0.08f * pulse) * lightMult.coerceAtMost(1.4f)),
                Color(0xFFFF9800).copy(alpha = 0.24f * lightMult),
                Color(0xFFE65100).copy(alpha = 0.08f * lightMult),
                Color.Transparent
            ),
            center = sunCenter,
            radius = w * 0.46f
        ),
        radius = w * 0.46f,
        center = sunCenter
    )
    drawCircle(
        color = Color(0xFFFFECB3).copy(alpha = 0.75f * lightMult.coerceAtMost(1.2f)),
        radius = w * 0.085f,
        center = sunCenter
    )

    // 2. Desert Mesa / Cliffs in far horizon (صخره‌ها و سنگ‌های طبیعی کویری)
    val mesaPath = Path().apply {
        moveTo(w * 0.04f, h * 0.34f)
        lineTo(w * 0.10f, h * 0.22f)
        lineTo(w * 0.26f, h * 0.22f)
        lineTo(w * 0.33f, h * 0.34f)
        close()
    }
    drawPath(mesaPath, color = Color(0xFF4E2A14).copy(alpha = 0.65f))

    // 3. Sweeping Layered Sand Dunes with elongated shadows (تپه‌های شنی در پس‌زمینه + سایه‌های کشیده)
    for (d in 0..2) {
        val yBase = h * (0.34f + d * 0.22f)
        val dunePath = Path().apply {
            moveTo(0f, yBase)
            cubicTo(
                w * 0.30f, yBase - 36f + d * 12f,
                w * 0.68f, yBase + 28f - d * 10f,
                w, yBase - 14f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        val duneTop = when (d) {
            0 -> Color(0xFF8D4E24).copy(alpha = 0.42f)
            1 -> Color(0xFFA65E2E).copy(alpha = 0.46f)
            else -> Color(0xFFC67D3B).copy(alpha = 0.50f)
        }
        val duneShadow = Color(0xFF2D1606).copy(alpha = 0.55f)
        drawPath(
            path = dunePath,
            brush = Brush.horizontalGradient(
                colors = listOf(duneTop, duneShadow, duneTop)
            )
        )
    }

    // 4. Heat-wave shimmer lines in the distance (موج گرمای ملایم در دوردست)
    if (showWeather) {
        for (wIdx in 0..2) {
            val wy = h * (0.24f + wIdx * 0.06f)
            val heatPath = Path().apply {
                moveTo(w * 0.1f, wy)
                quadraticTo(w * 0.35f, wy + shimmer, w * 0.6f, wy)
                quadraticTo(w * 0.8f, wy - shimmer, w * 0.92f, wy)
            }
            drawPath(
                path = heatPath,
                color = Color(0xFFFFCC80).copy(alpha = 0.16f * lightMult.coerceAtMost(1.3f)),
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }

    // 5. Drifting golden sand grains in the air (ذرات شن که به‌آرامی در هوا حرکت می‌کنند)
    if (showEffects) {
        val count = (28 * densityMult).toInt().coerceIn(10, 50)
        for (i in 0 until count) {
            val seed = i * 1.37f
            val sx = (((phase * 1.2f + seed * 0.29f) % 1f) * w)
            val sy = ((sin(seed * 2.4f + phase * 2f * PI.toFloat()) * 0.45f + 0.5f) * h)
            drawCircle(
                color = Color(0xFFFFE0B2).copy(alpha = 0.52f),
                radius = 1.8f + (i % 3) * 0.8f,
                center = Offset(sx, sy)
            )
        }
    }
}

private fun DrawScope.drawArcticIceWorld(
    phase: Float,
    pulse: Float,
    lightMult: Float,
    densityMult: Float,
    showEffects: Boolean,
    showWeather: Boolean
) {
    val w = size.width
    val h = size.height

    // 1. Cold blue polar light & white ice reflection (نور آبی سرد و انعکاس سفید روی یخ‌ها)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE0F7FA).copy(alpha = (0.26f + 0.08f * pulse) * lightMult.coerceAtMost(1.45f)),
                Color(0xFF4DD0E1).copy(alpha = 0.14f * lightMult),
                Color(0xFF0288D1).copy(alpha = 0.06f * lightMult),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.25f),
            radius = w * 0.82f
        ),
        radius = w * 0.82f,
        center = Offset(w * 0.5f, h * 0.25f)
    )

    // 2. Jagged Polar Glaciers & Icebergs in Top & Bottom Background (پس‌زمینه قطبی با کوه‌های یخی و یخچال‌ها)
    val leftGlacier = Path().apply {
        moveTo(0f, h * 0.34f)
        lineTo(w * 0.12f, h * 0.09f)
        lineTo(w * 0.24f, h * 0.22f)
        lineTo(w * 0.34f, h * 0.06f)
        lineTo(w * 0.48f, h * 0.34f)
        close()
    }
    drawPath(
        path = leftGlacier,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE1F5FE).copy(alpha = 0.55f),
                Color(0xFF4FC3F7).copy(alpha = 0.32f),
                Color(0xFF01579B).copy(alpha = 0.20f)
            )
        )
    )

    val rightGlacier = Path().apply {
        moveTo(w * 0.52f, h * 0.34f)
        lineTo(w * 0.68f, h * 0.08f)
        lineTo(w * 0.80f, h * 0.20f)
        lineTo(w * 0.91f, h * 0.05f)
        lineTo(w, h * 0.34f)
        close()
    }
    drawPath(
        path = rightGlacier,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.58f),
                Color(0xFF80DEEA).copy(alpha = 0.34f),
                Color(0xFF0277BD).copy(alpha = 0.22f)
            )
        )
    )

    // Bottom ice shelves & frozen snowdrifts around the board
    val bottomIce = Path().apply {
        moveTo(0f, h * 0.84f)
        quadraticTo(w * 0.25f, h * 0.78f, w * 0.5f, h * 0.83f)
        quadraticTo(w * 0.78f, h * 0.88f, w, h * 0.80f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = bottomIce,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE0F7FA).copy(alpha = 0.38f),
                Color(0xFFB2EBF2).copy(alpha = 0.52f)
            )
        )
    )

    // 3. Cold mist & subtle frost vapor (مه سرد و بخار یخ ملایم)
    if (showWeather) {
        for (m in 0..2) {
            val my = h * (0.18f + m * 0.32f)
            val mx = sin(phase * 2f * PI.toFloat() + m) * w * 0.08f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA).copy(alpha = 0.14f * lightMult.coerceAtMost(1.3f)),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(mx, my),
                size = Size(w * 0.9f, 70f)
            )
        }
    }

    // 4. Gentle continuous snowfall & sparkling ice crystals around the board (بارش آرام و پیوسته برف و درخشش بلورهای یخ)
    if (showEffects && showWeather) {
        val snowCount = (34 * densityMult).toInt().coerceIn(12, 60)
        for (i in 0 until snowCount) {
            val seed = i * 1.73f
            val sx = ((sin(seed * 2.9f + phase * 2f * PI.toFloat()) * 0.08f + (seed * 0.31f) % 1f) * w)
            val sy = (((seed * 0.23f + phase * 1.15f) % 1f) * h)
            val radius = 2.0f + (i % 4) * 1.2f
            drawCircle(
                color = Color.White.copy(alpha = 0.82f),
                radius = radius,
                center = Offset(sx, sy)
            )
            drawCircle(
                color = Color(0xFF80DEEA).copy(alpha = 0.35f),
                radius = radius * 2.1f,
                center = Offset(sx, sy)
            )
        }
    }
}
