package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.ObstacleType
import com.example.ui.TokenMovementType
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.HopefulEmerald
import com.example.ui.theme.HopefulMint
import com.example.ui.theme.RadiantGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleShape {
    STAR,
    ORB,
    RING,
    WIND_STREAK
}

data class BoardParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val color: Color,
    val shape: ParticleShape,
    var alpha: Float = 1f,
    var life: Float = 1f,
    val decayPerSecond: Float = 1.45f
)

/**
 * Real-time Canvas particle emitter & renderer for Ladder Climbs, Snake Slides,
 * and all 7 Phase-2 Special Obstacles (Worm, Reverse, Swamp, Well, Wind, Luck, Storm).
 */
class BoardParticleSystem {
    private val particles = mutableListOf<BoardParticle>()
    private val rng = Random(98765)

    val hasActiveParticles: Boolean
        get() = particles.isNotEmpty()

    fun clear() {
        particles.clear()
    }

    private fun paletteForMovement(movementType: TokenMovementType): List<Color> {
        return when (movementType) {
            TokenMovementType.LADDER_CLIMB -> listOf(
                RadiantGold, HopefulMint, HopefulEmerald, Color(0xFFFFF8D6)
            )
            TokenMovementType.SNAKE_SLIDE -> listOf(
                DangerCrimson, Color(0xFFFF4D6D), Color(0xFFA855F7), Color(0xFFFFB703)
            )
            TokenMovementType.REVERSE_BACKWARD -> listOf(
                Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDE047)
            )
            TokenMovementType.WELL_PLUNGE -> listOf(
                Color(0xFF6366F1), Color(0xFF4338CA), Color(0xFF38BDF8), Color(0xFF1E1B4B)
            )
            TokenMovementType.WIND_GUST -> listOf(
                Color(0xFF06B6D4), Color(0xFF67E8F9), Color(0xFFE0F2FE), Color.White
            )
            TokenMovementType.LUCK_BOOST -> listOf(
                RadiantGold, Color(0xFFFDE047), HopefulMint, Color(0xFFF472B6)
            )
            TokenMovementType.STORM_SHIFT -> listOf(
                Color(0xFFA855F7), Color(0xFFE879F9), Color(0xFF38BDF8), Color(0xFFFACC15)
            )
            TokenMovementType.SHADOW_SWAP -> listOf(
                Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFF312E81), Color(0xFFE0E7FF)
            )
            TokenMovementType.EXECUTIONER_HUNT -> listOf(
                DangerCrimson, Color(0xFF10B981), Color(0xFFFF4D6D), Color(0xFFFDE047)
            )
            TokenMovementType.TIME_REWIND -> listOf(
                Color(0xFF94A3B8), Color(0xFF38BDF8), Color(0xFFE2E8F0), RadiantGold
            )
            TokenMovementType.STEP_BY_STEP -> listOf(RadiantGold)
        }
    }

    fun emitObstacleBurstAtGrid(
        colFloat: Float,
        rowFloat: Float,
        obstacleType: ObstacleType,
        count: Int = 28
    ) {
        val palette = when (obstacleType) {
            ObstacleType.WORM -> listOf(Color(0xFF84CC16), Color(0xFFA3E635), Color(0xFF4D7C0F))
            ObstacleType.REVERSE -> listOf(Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDE047))
            ObstacleType.SWAMP -> listOf(Color(0xFF65A30D), Color(0xFF3F6212), Color(0xFF84CC16))
            ObstacleType.WELL -> listOf(Color(0xFF6366F1), Color(0xFF38BDF8), Color(0xFF1E1B4B))
            ObstacleType.WIND -> listOf(Color(0xFF06B6D4), Color(0xFF67E8F9), Color.White)
            ObstacleType.LUCK -> listOf(RadiantGold, Color(0xFFFDE047), HopefulMint)
            ObstacleType.STORM -> listOf(Color(0xFFA855F7), Color(0xFFFACC15), Color(0xFF38BDF8))
        }

        repeat(count) { idx ->
            val angle = (2.0 * PI * idx / count) + rng.nextDouble(-0.2, 0.2)
            val speed = rng.nextFloat() * 2.4f + 0.6f
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()

            val shape = when (obstacleType) {
                ObstacleType.LUCK, ObstacleType.STORM -> ParticleShape.STAR
                ObstacleType.WIND -> ParticleShape.WIND_STREAK
                ObstacleType.SWAMP, ObstacleType.WELL, ObstacleType.WORM -> ParticleShape.RING
                ObstacleType.REVERSE -> ParticleShape.ORB
            }

            particles.add(
                BoardParticle(
                    x = colFloat + rng.nextFloat() * 0.14f - 0.07f,
                    y = rowFloat + rng.nextFloat() * 0.14f - 0.07f,
                    vx = vx,
                    vy = vy,
                    radius = rng.nextFloat() * 0.13f + 0.06f,
                    color = palette[idx % palette.size],
                    shape = shape,
                    alpha = 1f,
                    life = 1f,
                    decayPerSecond = rng.nextFloat() * 0.7f + 1.15f
                )
            )
        }
    }

    fun emitBurstAtGrid(
        colFloat: Float,
        rowFloat: Float,
        movementType: TokenMovementType,
        count: Int = 26
    ) {
        if (movementType == TokenMovementType.STEP_BY_STEP) return
        val palette = paletteForMovement(movementType)
        val isUpward = movementType == TokenMovementType.LADDER_CLIMB ||
            movementType == TokenMovementType.LUCK_BOOST

        repeat(count) { idx ->
            val angle = (2.0 * PI * idx / count) + rng.nextDouble(-0.22, 0.22)
            val speed = rng.nextFloat() * 2.2f + 0.7f
            val vx = (cos(angle) * speed).toFloat()
            val vyBias = if (isUpward) 0.45f else -0.35f
            val vy = (sin(angle) * speed).toFloat() + vyBias

            val shape = when (movementType) {
                TokenMovementType.LADDER_CLIMB, TokenMovementType.LUCK_BOOST ->
                    if (idx % 2 == 0) ParticleShape.STAR else ParticleShape.ORB
                TokenMovementType.WIND_GUST -> ParticleShape.WIND_STREAK
                TokenMovementType.WELL_PLUNGE, TokenMovementType.STORM_SHIFT ->
                    if (idx % 2 == 0) ParticleShape.RING else ParticleShape.STAR
                else -> if (idx % 3 == 0) ParticleShape.RING else ParticleShape.ORB
            }

            particles.add(
                BoardParticle(
                    x = colFloat + rng.nextFloat() * 0.14f - 0.07f,
                    y = rowFloat + rng.nextFloat() * 0.14f - 0.07f,
                    vx = vx,
                    vy = vy,
                    radius = rng.nextFloat() * 0.12f + 0.06f,
                    color = palette[idx % palette.size],
                    shape = shape,
                    alpha = 1f,
                    life = 1f,
                    decayPerSecond = rng.nextFloat() * 0.8f + 1.15f
                )
            )
        }
    }

    fun emitTrailAtGrid(
        colFloat: Float,
        rowFloat: Float,
        movementType: TokenMovementType,
        count: Int = 4
    ) {
        if (movementType == TokenMovementType.STEP_BY_STEP) return
        val palette = paletteForMovement(movementType)
        val isPositive = movementType == TokenMovementType.LADDER_CLIMB ||
            movementType == TokenMovementType.LUCK_BOOST

        repeat(count) {
            val spreadX = (rng.nextFloat() - 0.5f) * 0.42f
            val spreadY = (rng.nextFloat() - 0.5f) * 0.42f
            val vx = if (movementType == TokenMovementType.WIND_GUST) {
                (rng.nextFloat() - 0.2f) * 2.2f
            } else {
                (rng.nextFloat() - 0.5f) * 0.9f
            }
            val vy = if (isPositive) {
                rng.nextFloat() * 1.1f + 0.25f
            } else {
                -(rng.nextFloat() * 0.95f + 0.15f)
            }

            val shape = when (movementType) {
                TokenMovementType.LADDER_CLIMB, TokenMovementType.LUCK_BOOST ->
                    if (rng.nextBoolean()) ParticleShape.STAR else ParticleShape.ORB
                TokenMovementType.WIND_GUST -> ParticleShape.WIND_STREAK
                TokenMovementType.WELL_PLUNGE -> ParticleShape.RING
                else -> if (rng.nextInt(4) == 0) ParticleShape.RING else ParticleShape.ORB
            }

            particles.add(
                BoardParticle(
                    x = colFloat + spreadX,
                    y = rowFloat + spreadY,
                    vx = vx,
                    vy = vy,
                    radius = rng.nextFloat() * 0.11f + 0.055f,
                    color = palette[rng.nextInt(palette.size)],
                    shape = shape,
                    alpha = 1f,
                    life = 1f,
                    decayPerSecond = rng.nextFloat() * 0.7f + 1.45f
                )
            )
        }
    }

    private val sparklePath = Path()

    fun emitStepTrailAtGrid(
        colFloat: Float,
        rowFloat: Float,
        tokenColor: Color,
        count: Int = 2
    ) {
        if (count <= 0) return
        val palette = listOf(tokenColor, RadiantGold, Color.White)
        repeat(count) { idx ->
            val spreadX = (rng.nextFloat() - 0.5f) * 0.24f
            val spreadY = (rng.nextFloat() - 0.5f) * 0.24f
            val vx = (rng.nextFloat() - 0.5f) * 0.55f
            val vy = rng.nextFloat() * 0.45f + 0.08f
            particles.add(
                BoardParticle(
                    x = colFloat + spreadX,
                    y = rowFloat + spreadY,
                    vx = vx,
                    vy = vy,
                    radius = rng.nextFloat() * 0.075f + 0.042f,
                    color = palette[idx % palette.size],
                    shape = if (idx % 2 == 0) ParticleShape.ORB else ParticleShape.STAR,
                    alpha = 0.88f,
                    life = 0.88f,
                    decayPerSecond = 2.35f
                )
            )
        }
    }

    fun emitLandingRingAndSparksAtGrid(
        colFloat: Float,
        rowFloat: Float,
        tokenColor: Color,
        isFinalDestination: Boolean = true,
        sparkCount: Int = 10
    ) {
        // Central expanding luminous landing ring
        particles.add(
            BoardParticle(
                x = colFloat,
                y = rowFloat,
                vx = 0f,
                vy = 0f,
                radius = if (isFinalDestination) 0.34f else 0.22f,
                color = tokenColor,
                shape = ParticleShape.RING,
                alpha = 0.95f,
                life = 0.95f,
                decayPerSecond = if (isFinalDestination) 1.75f else 2.6f
            )
        )
        if (isFinalDestination) {
            particles.add(
                BoardParticle(
                    x = colFloat,
                    y = rowFloat,
                    vx = 0f,
                    vy = 0f,
                    radius = 0.24f,
                    color = RadiantGold,
                    shape = ParticleShape.RING,
                    alpha = 0.85f,
                    life = 0.85f,
                    decayPerSecond = 2.1f
                )
            )
        }

        val palette = listOf(tokenColor, RadiantGold, Color(0xFFFFF8D6))
        repeat(sparkCount.coerceAtLeast(0)) { idx ->
            val angle = (2.0 * PI * idx / sparkCount.coerceAtLeast(1)) + rng.nextDouble(-0.18, 0.18)
            val speed = rng.nextFloat() * 1.45f + 0.45f
            particles.add(
                BoardParticle(
                    x = colFloat + (rng.nextFloat() - 0.5f) * 0.10f,
                    y = rowFloat + (rng.nextFloat() - 0.5f) * 0.10f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat() + 0.18f,
                    radius = rng.nextFloat() * 0.085f + 0.045f,
                    color = palette[idx % palette.size],
                    shape = if (idx % 2 == 0) ParticleShape.STAR else ParticleShape.ORB,
                    alpha = 1f,
                    life = 1f,
                    decayPerSecond = 1.95f
                )
            )
        }
    }

    fun update(dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0.001f, 0.05f)
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= p.decayPerSecond * dt
            p.alpha = p.life.coerceIn(0f, 1f)
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    fun drawParticlesAndPathGlow(
        drawScope: DrawScope,
        cellW: Float,
        cellH: Float,
        startGridPos: Offset?,
        currentGridPos: Offset?,
        endGridPos: Offset?,
        movementType: TokenMovementType?,
        totalRows: Int = 10
    ) = with(drawScope) {
        if (startGridPos != null && currentGridPos != null && endGridPos != null &&
            movementType != null && movementType != TokenMovementType.STEP_BY_STEP
        ) {
            val startPx = gridToCanvasOffset(startGridPos.x, startGridPos.y, cellW, cellH, totalRows)
            val currentPx = gridToCanvasOffset(currentGridPos.x, currentGridPos.y, cellW, cellH, totalRows)
            val endPx = gridToCanvasOffset(endGridPos.x, endGridPos.y, cellW, cellH, totalRows)

            val palette = paletteForMovement(movementType)
            val primaryColor = palette.first()

            drawLine(
                color = primaryColor.copy(alpha = 0.24f),
                start = startPx,
                end = endPx,
                strokeWidth = cellW * 0.40f,
                cap = StrokeCap.Round
            )

            val activeBeamBrush = Brush.linearGradient(
                colors = listOf(
                    palette.last().copy(alpha = 0.20f),
                    primaryColor.copy(alpha = 0.75f),
                    Color.White.copy(alpha = 0.90f)
                ),
                start = startPx,
                end = currentPx
            )

            drawLine(
                brush = activeBeamBrush,
                start = startPx,
                end = currentPx,
                strokeWidth = cellW * 0.22f,
                cap = StrokeCap.Round
            )
        }

        for (p in particles) {
            val center = gridToCanvasOffset(p.x, p.y, cellW, cellH, totalRows)
            val isExpandingRing = p.shape == ParticleShape.RING && p.vx == 0f && p.vy == 0f
            val rPx = if (isExpandingRing) {
                p.radius * cellW * (1.85f - 0.85f * p.alpha)
            } else {
                p.radius * cellW * (0.55f + 0.45f * p.alpha)
            }
            val colorWithAlpha = p.color.copy(alpha = p.alpha)

            drawCircle(
                color = p.color.copy(alpha = p.alpha * 0.32f),
                radius = rPx * 1.9f,
                center = center
            )

            when (p.shape) {
                ParticleShape.ORB -> {
                    drawCircle(color = colorWithAlpha, radius = rPx, center = center)
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha * 0.75f),
                        radius = rPx * 0.4f,
                        center = center
                    )
                }

                ParticleShape.RING -> {
                    drawCircle(
                        color = colorWithAlpha,
                        radius = rPx * 1.15f,
                        center = center,
                        style = Stroke(width = (rPx * 0.32f).coerceAtLeast(1.5f))
                    )
                }

                ParticleShape.STAR -> {
                    drawFourPointSparkle(
                        center = center,
                        radius = rPx * 1.45f,
                        color = colorWithAlpha
                    )
                }

                ParticleShape.WIND_STREAK -> {
                    drawLine(
                        color = colorWithAlpha,
                        start = Offset(center.x - rPx * 1.8f, center.y),
                        end = Offset(center.x + rPx * 1.8f, center.y),
                        strokeWidth = (rPx * 0.55f).coerceAtLeast(2f),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }

    private fun gridToCanvasOffset(
        colFloat: Float,
        rowFloat: Float,
        cellW: Float,
        cellH: Float,
        totalRows: Int = 10
    ): Offset {
        val visualRow = (totalRows - 1).toFloat() - rowFloat
        return Offset(
            x = (colFloat + 0.5f) * cellW,
            y = (visualRow + 0.56f) * cellH
        )
    }

    private fun DrawScope.drawFourPointSparkle(
        center: Offset,
        radius: Float,
        color: Color
    ) {
        val inner = radius * 0.28f
        sparklePath.reset()
        sparklePath.moveTo(center.x, center.y - radius)
        sparklePath.quadraticTo(center.x + inner, center.y - inner, center.x + radius, center.y)
        sparklePath.quadraticTo(center.x + inner, center.y + inner, center.x, center.y + radius)
        sparklePath.quadraticTo(center.x - inner, center.y + inner, center.x - radius, center.y)
        sparklePath.quadraticTo(center.x - inner, center.y - inner, center.x, center.y - radius)
        sparklePath.close()
        drawPath(path = sparklePath, color = color)
        drawCircle(
            color = Color.White.copy(alpha = color.alpha * 0.85f),
            radius = radius * 0.22f,
            center = center
        )
    }
}

