package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
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
import com.example.model.AnimationQuality
import com.example.model.EffectIntensity
import com.example.model.EnvironmentEffectType
import com.example.model.MusicGenre
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Hardware-accelerated, zero-allocation-per-frame Environment Visual Effects Overlay.
 *
 * Designed so that all 12 atmospheric effects enhance immersion while keeping board numbers,
 * snakes, ladders, obstacles, and player tokens 100% legible.
 */
@Composable
fun EnvironmentEffectsOverlay(
    effectType: EnvironmentEffectType,
    intensity: EffectIntensity,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    isCompactPreview: Boolean = false,
    animationQuality: AnimationQuality = AnimationQuality.CINEMATIC
) {
    if (!isEnabled) return

    val maxParticles = 72
    val xNorm = remember { FloatArray(maxParticles) }
    val yNorm = remember { FloatArray(maxParticles) }
    val vxNorm = remember { FloatArray(maxParticles) }
    val vyNorm = remember { FloatArray(maxParticles) }
    val sizeFactor = remember { FloatArray(maxParticles) }
    val phaseOffset = remember { FloatArray(maxParticles) }
    val colorVariant = remember { IntArray(maxParticles) }
    val reusablePath = remember { Path() }

    LaunchedEffect(effectType) {
        val rng = Random(effectType.ordinal * 97 + 31)
        for (i in 0 until maxParticles) {
            xNorm[i] = rng.nextFloat()
            yNorm[i] = rng.nextFloat()
            phaseOffset[i] = rng.nextFloat() * (2f * PI.toFloat())
            sizeFactor[i] = 0.55f + rng.nextFloat() * 0.90f
            colorVariant[i] = i % 3
            configureParticleVelocity(effectType, i, vxNorm, vyNorm, rng)
        }
    }

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(effectType, intensity, isEnabled, animationQuality) {
        var lastNanos = 0L
        val rng = Random(777)
        while (isEnabled) {
            withFrameNanos { frameNanos ->
                val rawDt = if (lastNanos == 0L) 0.016f else ((frameNanos - lastNanos) / 1_000_000_000f)
                lastNanos = frameNanos
                val dt = rawDt.coerceIn(0.001f, 0.05f) * intensity.speedMultiplier
                elapsedSeconds += dt

                val activeCount = ((if (isCompactPreview) effectType.baseParticleCount * 0.55f else effectType.baseParticleCount.toFloat()) *
                    intensity.particleCountMultiplier * animationQuality.particleMultiplier).roundToInt().coerceIn(6, maxParticles)

                for (i in 0 until activeCount) {
                    xNorm[i] += vxNorm[i] * dt
                    yNorm[i] += vyNorm[i] * dt

                    // Wrap around smoothly
                    if (yNorm[i] < -0.08f) {
                        yNorm[i] = 1.06f
                        xNorm[i] = rng.nextFloat()
                    } else if (yNorm[i] > 1.08f) {
                        yNorm[i] = -0.06f
                        xNorm[i] = rng.nextFloat()
                    }

                    if (xNorm[i] < -0.08f) {
                        xNorm[i] = 1.06f
                    } else if (xNorm[i] > 1.08f) {
                        xNorm[i] = -0.06f
                    }
                }
            }
        }
    }

    val activeParticleCount = ((if (isCompactPreview) effectType.baseParticleCount * 0.55f else effectType.baseParticleCount.toFloat()) *
        intensity.particleCountMultiplier * animationQuality.particleMultiplier).roundToInt().coerceIn(6, maxParticles)

    val masterAlpha = (if (isCompactPreview) 0.85f else 0.46f) * intensity.alphaMultiplier

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 1f || h <= 1f) return@Canvas

        val t = elapsedSeconds

        // 1. Background/Edge Atmospheric Layer for specific effects (Aurora, Galaxy, Mystic Fog, Royal Glow, Lightning)
        when (effectType) {
            EnvironmentEffectType.AURORA -> drawAuroraRibbons(w, h, t, masterAlpha, reusablePath)
            EnvironmentEffectType.GALAXY -> drawGalaxyNebula(w, h, t, masterAlpha)
            EnvironmentEffectType.MYSTIC_FOG -> drawMysticFogLayers(w, h, t, masterAlpha)
            EnvironmentEffectType.ROYAL_GLOW -> drawRoyalGlowCrownRays(w, h, t, masterAlpha)
            EnvironmentEffectType.LIGHTNING -> drawLightningBolts(w, h, t, masterAlpha, reusablePath)
            EnvironmentEffectType.FIRE_EMBERS -> drawWarmEmberHorizon(w, h, t, masterAlpha)
            else -> Unit
        }

        // 2. Foreground / Midground Particle Layer
        for (i in 0 until activeParticleCount) {
            val px = xNorm[i] * w
            val py = yNorm[i] * h
            val phase = phaseOffset[i]
            val sScale = sizeFactor[i] * (if (isCompactPreview) 0.78f else 1.0f)
            val baseCol = when (colorVariant[i]) {
                0 -> effectType.primaryColor
                1 -> effectType.secondaryColor
                else -> effectType.glowColor
            }

            when (effectType) {
                EnvironmentEffectType.GOLDEN_DUST -> {
                    val swayX = px + sin(t * 1.8f + phase) * 8f
                    val pulse = 0.45f + 0.55f * ((sin(t * 3.2f + phase) + 1f) * 0.5f)
                    val a = (masterAlpha * pulse).coerceIn(0f, 0.78f)
                    val radius = (2.8f + 2.4f * sScale)
                    drawCircle(
                        color = effectType.glowColor.copy(alpha = a * 0.35f),
                        radius = radius * 2.4f,
                        center = Offset(swayX, py)
                    )
                    drawCircle(
                        color = baseCol.copy(alpha = a),
                        radius = radius,
                        center = Offset(swayX, py)
                    )
                    if (i % 4 == 0) {
                        drawFourPointStar(
                            center = Offset(swayX, py),
                            armLength = radius * 2.2f,
                            color = Color.White.copy(alpha = a * 0.85f)
                        )
                    }
                }

                EnvironmentEffectType.MAGIC_PARTICLES -> {
                    val orbitX = px + cos(t * 2.2f + phase) * 12f
                    val orbitY = py + sin(t * 1.7f + phase) * 8f
                    val pulse = 0.5f + 0.5f * sin(t * 3.6f + phase)
                    val a = (masterAlpha * (0.55f + 0.45f * pulse)).coerceIn(0f, 0.80f)
                    val r = 3.2f + 2.8f * sScale
                    drawCircle(
                        color = baseCol.copy(alpha = a * 0.32f),
                        radius = r * 2.8f,
                        center = Offset(orbitX, orbitY)
                    )
                    drawCircle(
                        color = baseCol.copy(alpha = a),
                        radius = r,
                        center = Offset(orbitX, orbitY)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = a * 0.85f),
                        radius = r * 0.42f,
                        center = Offset(orbitX, orbitY)
                    )
                }

                EnvironmentEffectType.FIRE_EMBERS -> {
                    val flicker = 0.5f + 0.5f * sin(t * 6.5f + phase)
                    val swayX = px + sin(t * 2.8f + phase) * 10f
                    val a = (masterAlpha * (0.5f + 0.5f * flicker)).coerceIn(0f, 0.82f)
                    val r = 2.4f + 2.6f * sScale
                    // Glowing ember tail
                    drawLine(
                        color = effectType.glowColor.copy(alpha = a * 0.45f),
                        start = Offset(swayX, py),
                        end = Offset(swayX - vxNorm[i] * 18f, py + r * 3.2f),
                        strokeWidth = r * 1.1f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = baseCol.copy(alpha = a),
                        radius = r,
                        center = Offset(swayX, py)
                    )
                }

                EnvironmentEffectType.SNOWFALL -> {
                    val swayX = px + sin(t * 1.5f + phase) * 14f
                    val a = (masterAlpha * (0.65f + 0.35f * sin(t * 2.0f + phase))).coerceIn(0f, 0.80f)
                    val r = 2.5f + 3.0f * sScale
                    drawCircle(
                        color = Color.White.copy(alpha = a * 0.28f),
                        radius = r * 1.9f,
                        center = Offset(swayX, py)
                    )
                    drawCircle(
                        color = baseCol.copy(alpha = a),
                        radius = r,
                        center = Offset(swayX, py)
                    )
                    if (i % 3 == 0) {
                        // Delicate 6-spoke crystal cross
                        drawSnowflakeCross(Offset(swayX, py), r * 1.6f, Color.White.copy(alpha = a * 0.75f))
                    }
                }

                EnvironmentEffectType.RAIN -> {
                    val dropLen = (14f + 12f * sScale) * (if (isCompactPreview) 0.7f else 1.0f)
                    val slantX = dropLen * 0.22f
                    val a = (masterAlpha * 0.68f).coerceIn(0f, 0.72f)
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                baseCol.copy(alpha = a)
                            ),
                            start = Offset(px - slantX, py - dropLen),
                            end = Offset(px, py)
                        ),
                        start = Offset(px - slantX, py - dropLen),
                        end = Offset(px, py),
                        strokeWidth = 1.65f * sScale,
                        cap = StrokeCap.Round
                    )
                    // Subtle water ripple when near bottom
                    if (yNorm[i] > 0.88f) {
                        val rippleP = ((yNorm[i] - 0.88f) / 0.12f).coerceIn(0f, 1f)
                        drawOval(
                            color = effectType.secondaryColor.copy(alpha = a * (1f - rippleP) * 0.65f),
                            topLeft = Offset(px - 8f * rippleP, py - 2.5f * rippleP),
                            size = Size(16f * rippleP, 5f * rippleP),
                            style = Stroke(width = 1.2f)
                        )
                    }
                }

                EnvironmentEffectType.GALAXY -> {
                    val twinkle = ((sin(t * 3.8f + phase) + 1f) * 0.5f)
                    val a = (masterAlpha * (0.35f + 0.65f * twinkle)).coerceIn(0f, 0.85f)
                    val r = 2.0f + 2.5f * sScale
                    drawCircle(
                        color = baseCol.copy(alpha = a * 0.35f),
                        radius = r * 2.5f,
                        center = Offset(px, py)
                    )
                    if (i % 3 == 0) {
                        drawFourPointStar(
                            center = Offset(px, py),
                            armLength = r * 2.5f,
                            color = Color.White.copy(alpha = a)
                        )
                    } else {
                        drawCircle(
                            color = Color.White.copy(alpha = a),
                            radius = r * 0.85f,
                            center = Offset(px, py)
                        )
                    }
                }

                EnvironmentEffectType.AURORA -> {
                    val swayX = px + sin(t * 1.4f + phase) * 10f
                    val a = (masterAlpha * 0.55f * ((sin(t * 2.5f + phase) + 1f) * 0.5f)).coerceIn(0f, 0.70f)
                    val r = 2.4f + 2.2f * sScale
                    drawCircle(
                        color = baseCol.copy(alpha = a),
                        radius = r,
                        center = Offset(swayX, py)
                    )
                }

                EnvironmentEffectType.MYSTIC_FOG -> {
                    // Soft drifting wisp puff
                    val driftX = px + sin(t * 0.8f + phase) * 18f
                    val pulse = 0.5f + 0.5f * sin(t * 1.2f + phase)
                    val a = (masterAlpha * 0.26f * pulse).coerceIn(0f, 0.38f)
                    val puffRadius = (22f + 26f * sScale) * (if (isCompactPreview) 0.55f else 1.0f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                baseCol.copy(alpha = a),
                                Color.Transparent
                            ),
                            center = Offset(driftX, py),
                            radius = puffRadius
                        ),
                        radius = puffRadius,
                        center = Offset(driftX, py)
                    )
                }

                EnvironmentEffectType.LIGHTNING -> {
                    // Floating electric ion sparks
                    val jitterX = px + sin(t * 12f + phase) * 4f
                    val jitterY = py + cos(t * 11f + phase) * 4f
                    val a = (masterAlpha * (0.45f + 0.55f * abs(sin(t * 5f + phase)))).coerceIn(0f, 0.80f)
                    val r = 2.0f + 1.8f * sScale
                    drawCircle(
                        color = baseCol.copy(alpha = a),
                        radius = r,
                        center = Offset(jitterX, jitterY)
                    )
                }

                EnvironmentEffectType.ROYAL_GLOW -> {
                    val swayX = px + sin(t * 1.1f + phase) * 9f
                    val pulse = (sin(t * 2.2f + phase) + 1f) * 0.5f
                    val a = (masterAlpha * (0.40f + 0.60f * pulse)).coerceIn(0f, 0.78f)
                    val r = (4.5f + 5.5f * sScale) * (if (isCompactPreview) 0.65f else 1.0f)
                    // Soft luxury bokeh circle + diamond sparkle
                    drawCircle(
                        color = effectType.primaryColor.copy(alpha = a * 0.25f),
                        radius = r * 1.7f,
                        center = Offset(swayX, py)
                    )
                    drawCircle(
                        color = effectType.glowColor.copy(alpha = a * 0.55f),
                        radius = r,
                        center = Offset(swayX, py),
                        style = Stroke(width = 1.2f)
                    )
                    if (i % 3 == 0) {
                        drawFourPointStar(
                            center = Offset(swayX, py),
                            armLength = r * 1.3f,
                            color = effectType.primaryColor.copy(alpha = a)
                        )
                    }
                }

                EnvironmentEffectType.ANCIENT_RUNES -> {
                    val fade = ((sin(t * 1.8f + phase) + 1f) * 0.5f)
                    val a = (masterAlpha * fade * 0.78f).coerceIn(0f, 0.78f)
                    val runeSize = (7.5f + 5.5f * sScale) * (if (isCompactPreview) 0.72f else 1.0f)
                    val rotDeg = (t * 22f + phase * 40f) % 360f
                    drawAncientRuneSymbol(
                        center = Offset(px, py),
                        radius = runeSize,
                        rotationDeg = rotDeg,
                        runeIndex = i % 5,
                        color = baseCol.copy(alpha = a),
                        glowColor = effectType.glowColor.copy(alpha = a * 0.35f),
                        path = reusablePath
                    )
                }

                EnvironmentEffectType.SAKURA -> {
                    val swayX = px + sin(t * 2.0f + phase) * 14f
                    val rotDeg = (t * 58f + phase * 57f) % 360f
                    val a = (masterAlpha * 0.78f).coerceIn(0f, 0.84f)
                    val petalSize = (4.8f + 3.6f * sScale) * (if (isCompactPreview) 0.75f else 1.0f)
                    drawSakuraPetal(
                        center = Offset(swayX, py),
                        radius = petalSize,
                        rotationDeg = rotDeg,
                        color = baseCol.copy(alpha = a),
                        highlightColor = Color.White.copy(alpha = a * 0.65f),
                        path = reusablePath
                    )
                }
            }
        }
    }
}

private fun configureParticleVelocity(
    effectType: EnvironmentEffectType,
    index: Int,
    vxNorm: FloatArray,
    vyNorm: FloatArray,
    rng: Random
) {
    when (effectType) {
        EnvironmentEffectType.GOLDEN_DUST -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.03f
            vyNorm[index] = -(0.03f + rng.nextFloat() * 0.045f)
        }
        EnvironmentEffectType.MAGIC_PARTICLES -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.04f
            vyNorm[index] = -(0.02f + rng.nextFloat() * 0.04f)
        }
        EnvironmentEffectType.FIRE_EMBERS -> {
            vxNorm[index] = (rng.nextFloat() - 0.35f) * 0.05f
            vyNorm[index] = -(0.08f + rng.nextFloat() * 0.11f)
        }
        EnvironmentEffectType.SNOWFALL -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.035f
            vyNorm[index] = 0.05f + rng.nextFloat() * 0.065f
        }
        EnvironmentEffectType.RAIN -> {
            vxNorm[index] = 0.07f + rng.nextFloat() * 0.04f
            vyNorm[index] = 0.36f + rng.nextFloat() * 0.24f
        }
        EnvironmentEffectType.GALAXY -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.015f
            vyNorm[index] = (rng.nextFloat() - 0.5f) * 0.015f
        }
        EnvironmentEffectType.AURORA -> {
            vxNorm[index] = 0.02f + rng.nextFloat() * 0.025f
            vyNorm[index] = -(0.015f + rng.nextFloat() * 0.02f)
        }
        EnvironmentEffectType.MYSTIC_FOG -> {
            vxNorm[index] = (if (index % 2 == 0) 1f else -1f) * (0.02f + rng.nextFloat() * 0.025f)
            vyNorm[index] = (rng.nextFloat() - 0.5f) * 0.008f
        }
        EnvironmentEffectType.LIGHTNING -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.04f
            vyNorm[index] = (rng.nextFloat() - 0.5f) * 0.04f
        }
        EnvironmentEffectType.ROYAL_GLOW -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.02f
            vyNorm[index] = -(0.02f + rng.nextFloat() * 0.03f)
        }
        EnvironmentEffectType.ANCIENT_RUNES -> {
            vxNorm[index] = (rng.nextFloat() - 0.5f) * 0.015f
            vyNorm[index] = -(0.022f + rng.nextFloat() * 0.025f)
        }
        EnvironmentEffectType.SAKURA -> {
            vxNorm[index] = 0.035f + rng.nextFloat() * 0.045f
            vyNorm[index] = 0.045f + rng.nextFloat() * 0.055f
        }
    }
}

private fun DrawScope.drawFourPointStar(
    center: Offset,
    armLength: Float,
    color: Color
) {
    drawLine(
        color = color,
        start = Offset(center.x - armLength, center.y),
        end = Offset(center.x + armLength, center.y),
        strokeWidth = 1.4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x, center.y - armLength),
        end = Offset(center.x, center.y + armLength),
        strokeWidth = 1.4f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSnowflakeCross(
    center: Offset,
    radius: Float,
    color: Color
) {
    for (angleIdx in 0 until 3) {
        val rad = (angleIdx * 60.0) * PI / 180.0
        val dx = (cos(rad) * radius).toFloat()
        val dy = (sin(rad) * radius).toFloat()
        drawLine(
            color = color,
            start = Offset(center.x - dx, center.y - dy),
            end = Offset(center.x + dx, center.y + dy),
            strokeWidth = 1.1f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawAuroraRibbons(
    w: Float,
    h: Float,
    t: Float,
    masterAlpha: Float,
    path: Path
) {
    val ribbonColors = listOf(
        Color(0xFF10B981),
        Color(0xFF06B6D4),
        Color(0xFFA855F7)
    )
    for (rIdx in 0..2) {
        path.reset()
        val baseY = h * (0.12f + rIdx * 0.09f)
        val waveAmp = h * 0.045f
        val segments = 12
        for (s in 0..segments) {
            val frac = s.toFloat() / segments
            val x = frac * w
            val y = baseY + sin(frac * 4.2f + t * (1.1f + rIdx * 0.25f) + rIdx) * waveAmp
            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = ribbonColors[rIdx].copy(alpha = (masterAlpha * 0.26f).coerceIn(0f, 0.35f)),
            style = Stroke(width = h * 0.045f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawGalaxyNebula(
    w: Float,
    h: Float,
    t: Float,
    masterAlpha: Float
) {
    val c1 = Offset(
        x = w * (0.28f + 0.08f * sin(t * 0.4f)),
        y = h * (0.24f + 0.06f * cos(t * 0.35f))
    )
    val c2 = Offset(
        x = w * (0.74f + 0.08f * cos(t * 0.45f)),
        y = h * (0.76f + 0.06f * sin(t * 0.38f))
    )
    val r = w.coerceAtMost(h) * 0.38f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFA855F7).copy(alpha = (masterAlpha * 0.22f).coerceIn(0f, 0.28f)),
                Color.Transparent
            ),
            center = c1,
            radius = r
        ),
        radius = r,
        center = c1
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD166).copy(alpha = (masterAlpha * 0.18f).coerceIn(0f, 0.24f)),
                Color.Transparent
            ),
            center = c2,
            radius = r
        ),
        radius = r,
        center = c2
    )
}

private fun DrawScope.drawMysticFogLayers(
    w: Float,
    h: Float,
    t: Float,
    masterAlpha: Float
) {
    val pulse = 0.7f + 0.3f * sin(t * 0.9f)
    val edgeAlpha = (masterAlpha * 0.28f * pulse).coerceIn(0f, 0.34f)
    // Top mystic veil
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF94A3B8).copy(alpha = edgeAlpha),
                Color.Transparent
            ),
            startY = 0f,
            endY = h * 0.22f
        ),
        topLeft = Offset.Zero,
        size = Size(w, h * 0.22f)
    )
    // Bottom golden-mist veil
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFFFD166).copy(alpha = edgeAlpha * 0.75f)
            ),
            startY = h * 0.78f,
            endY = h
        ),
        topLeft = Offset(0f, h * 0.78f),
        size = Size(w, h * 0.22f)
    )
}

private fun DrawScope.drawRoyalGlowCrownRays(
    w: Float,
    h: Float,
    t: Float,
    masterAlpha: Float
) {
    val pulse = 0.65f + 0.35f * sin(t * 1.4f)
    val glowAlpha = (masterAlpha * 0.28f * pulse).coerceIn(0f, 0.35f)
    // Top-center royal crown golden aura
    val crownOrigin = Offset(w * 0.5f, 0f)
    val radius = w.coerceAtLeast(h) * 0.45f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFD166).copy(alpha = glowAlpha),
                Color(0xFFD97706).copy(alpha = glowAlpha * 0.4f),
                Color.Transparent
            ),
            center = crownOrigin,
            radius = radius
        ),
        radius = radius,
        center = crownOrigin
    )
}

private fun DrawScope.drawWarmEmberHorizon(
    w: Float,
    h: Float,
    t: Float,
    masterAlpha: Float
) {
    val flicker = 0.7f + 0.3f * sin(t * 3.5f)
    val horizonAlpha = (masterAlpha * 0.24f * flicker).coerceIn(0f, 0.30f)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFFF6B35).copy(alpha = horizonAlpha)
            ),
            startY = h * 0.80f,
            endY = h
        ),
        topLeft = Offset(0f, h * 0.80f),
        size = Size(w, h * 0.20f)
    )
}

private fun DrawScope.drawLightningBolts(
    w: Float,
    h: Float,
    t: Float,
    masterAlpha: Float,
    path: Path
) {
    // Periodic lightning strike every ~2.3 seconds
    val cycle = t % 2.3f
    if (cycle < 0.26f) {
        val strikeIntensity = sin((cycle / 0.26f) * PI.toFloat())
        val boltSeed = (t / 2.3f).toInt()
        val startX = w * (0.18f + ((boltSeed * 37) % 64) / 100f)
        path.reset()
        var curX = startX
        var curY = 0f
        path.moveTo(curX, curY)
        val steps = 6
        for (s in 1..steps) {
            val zig = if ((s + boltSeed) % 2 == 0) 1f else -1f
            curX += zig * (w * 0.045f)
            curY = (s.toFloat() / steps) * (h * 0.48f)
            path.lineTo(curX, curY)
        }
        val boltAlpha = (masterAlpha * strikeIntensity * 0.90f).coerceIn(0f, 0.85f)
        drawPath(
            path = path,
            color = Color(0xFF38BDF8).copy(alpha = boltAlpha * 0.45f),
            style = Stroke(width = 6.5f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = Color(0xFFFDE047).copy(alpha = boltAlpha),
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawAncientRuneSymbol(
    center: Offset,
    radius: Float,
    rotationDeg: Float,
    runeIndex: Int,
    color: Color,
    glowColor: Color,
    path: Path
) {
    drawCircle(
        color = glowColor,
        radius = radius * 1.45f,
        center = center
    )
    rotate(degrees = rotationDeg, pivot = center) {
        when (runeIndex) {
            0 -> {
                // Sacred Diamond + Center Dot
                path.reset()
                path.moveTo(center.x, center.y - radius)
                path.lineTo(center.x + radius * 0.75f, center.y)
                path.lineTo(center.x, center.y + radius)
                path.lineTo(center.x - radius * 0.75f, center.y)
                path.close()
                drawPath(path, color = color, style = Stroke(width = 1.5f))
                drawCircle(color = color, radius = 1.6f, center = center)
            }
            1 -> {
                // Ancient Triangle + Inscribed Circle
                path.reset()
                path.moveTo(center.x, center.y - radius)
                path.lineTo(center.x + radius * 0.86f, center.y + radius * 0.6f)
                path.lineTo(center.x - radius * 0.86f, center.y + radius * 0.6f)
                path.close()
                drawPath(path, color = color, style = Stroke(width = 1.5f))
            }
            2 -> {
                // Runic Stave (Vertical + two diagonal branches)
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 1.6f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - radius * 0.4f),
                    end = Offset(center.x + radius * 0.6f, center.y - radius * 0.85f),
                    strokeWidth = 1.4f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y + radius * 0.1f),
                    end = Offset(center.x - radius * 0.6f, center.y - radius * 0.35f),
                    strokeWidth = 1.4f,
                    cap = StrokeCap.Round
                )
            }
            3 -> {
                // Sun-Wheel Rune
                drawCircle(
                    color = color,
                    radius = radius * 0.85f,
                    center = center,
                    style = Stroke(width = 1.4f)
                )
                drawFourPointStar(center, radius * 0.85f, color)
            }
            else -> {
                // Othala-inspired Mystic Loop
                path.reset()
                path.moveTo(center.x - radius * 0.6f, center.y + radius * 0.8f)
                path.lineTo(center.x + radius * 0.6f, center.y - radius * 0.2f)
                path.lineTo(center.x, center.y - radius * 0.85f)
                path.lineTo(center.x - radius * 0.6f, center.y - radius * 0.2f)
                path.lineTo(center.x + radius * 0.6f, center.y + radius * 0.8f)
                drawPath(path, color = color, style = Stroke(width = 1.5f, cap = StrokeCap.Round))
            }
        }
    }
}

private fun DrawScope.drawSakuraPetal(
    center: Offset,
    radius: Float,
    rotationDeg: Float,
    color: Color,
    highlightColor: Color,
    path: Path
) {
    rotate(degrees = rotationDeg, pivot = center) {
        path.reset()
        path.moveTo(center.x, center.y + radius)
        path.cubicTo(
            center.x - radius * 1.1f, center.y + radius * 0.2f,
            center.x - radius * 0.85f, center.y - radius * 0.9f,
            center.x, center.y - radius * 0.45f
        )
        path.cubicTo(
            center.x + radius * 0.85f, center.y - radius * 0.9f,
            center.x + radius * 1.1f, center.y + radius * 0.2f,
            center.x, center.y + radius
        )
        path.close()
        drawPath(path = path, color = color)
        drawCircle(
            color = highlightColor,
            radius = radius * 0.25f,
            center = Offset(center.x - radius * 0.2f, center.y)
        )
    }
}

/**
 * Animated Luxury Equalizer & Waveform Preview for Music Genre Cards in the Studio.
 */
@Composable
fun MusicGenreWaveformPreview(
    genre: MusicGenre,
    isActiveOrPreviewing: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_wave_${genre.name}")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (60_000 / genre.tempoBpm.coerceAtLeast(60)).coerceIn(380, 950),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "genre_wave_phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barCount = 18
        val totalSpacing = w * 0.28f
        val barWidth = ((w - totalSpacing) / barCount).coerceAtLeast(2f)
        val gap = totalSpacing / (barCount + 1)

        // Subtle background glow
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    genre.primaryColor.copy(alpha = 0.14f),
                    genre.secondaryColor.copy(alpha = 0.22f),
                    genre.primaryColor.copy(alpha = 0.14f)
                )
            )
        )

        for (i in 0 until barCount) {
            val normIdx = i.toFloat() / barCount
            val seedAngle = normIdx * PI.toFloat() * 2.5f + genre.ordinal * 0.8f
            val dynamicOsc = if (isActiveOrPreviewing) {
                0.30f + 0.65f * ((sin(wavePhase + seedAngle) + 1f) * 0.5f)
            } else {
                0.22f + 0.42f * ((sin(seedAngle) + 1f) * 0.5f)
            }
            val barH = (h * 0.76f * dynamicOsc).coerceAtLeast(4f)
            val x = gap + i * (barWidth + gap)
            val y = (h - barH) * 0.5f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        genre.primaryColor,
                        genre.secondaryColor
                    ),
                    startY = y,
                    endY = y + barH
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth * 0.5f, barWidth * 0.5f)
            )
        }
    }
}
