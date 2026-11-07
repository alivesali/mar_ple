package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.PawnMaterialType
import com.example.model.PawnSkin
import com.example.model.PawnTrailFxType
import com.example.ui.theme.RadiantGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Preview Mode for the interactive Pawn Showcase.
 */
enum class PawnPreviewMode(val persianLabel: String, val iconEmoji: String) {
    IDLE("حالت معلق و تنفس", "🧘"),
    TRAIL_MOTION("ردپای حرکت", "🚀"),
    LANDING_RIPPLE("موج فرود", "💥"),
    VICTORY_BURST("جشن پیروزی", "🏆")
}

/**
 * Interactive 3D/2D Visual Renderer for Pawn Skins.
 * Draws every skin with its signature material, glowing aura, player identity ring,
 * and animated movement/landing effects.
 */
@Composable
fun PawnSkinRenderer(
    skin: PawnSkin,
    playerColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 90.dp,
    previewMode: PawnPreviewMode = PawnPreviewMode.IDLE,
    showPedestal: Boolean = true,
    showAura: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pawn_anim_${skin.id}")

    // Continuous floating/bobbing
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -0.06f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawn_float"
    )

    // Continuous rotation for gems/plasma
    val rotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pawn_spin"
    )

    // Pulse for glow and energy
    val energyPulse by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawn_pulse"
    )

    // One-shot landing / burst driver
    val rippleProgress = remember { Animatable(0f) }
    LaunchedEffect(previewMode) {
        if (previewMode == PawnPreviewMode.LANDING_RIPPLE || previewMode == PawnPreviewMode.VICTORY_BURST) {
            rippleProgress.snapTo(0f)
            rippleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val dynamicY = when (previewMode) {
                PawnPreviewMode.IDLE -> h * 0.48f + floatOffset * h
                PawnPreviewMode.TRAIL_MOTION -> h * 0.44f + floatOffset * h * 0.5f
                PawnPreviewMode.LANDING_RIPPLE -> h * 0.52f
                PawnPreviewMode.VICTORY_BURST -> h * 0.42f - floatOffset * h * 0.8f
            }

            val dynamicX = if (previewMode == PawnPreviewMode.TRAIL_MOTION) {
                w * 0.5f + sin(rotationDeg * (PI / 180f).toFloat()) * (w * 0.12f)
            } else {
                w * 0.5f
            }

            val center = Offset(dynamicX, dynamicY)
            val radius = w * 0.32f

            // 1. Landing Shockwave Ripple (Behind Pedestal)
            if (previewMode == PawnPreviewMode.LANDING_RIPPLE && rippleProgress.value > 0f) {
                drawLandingRipple(
                    center = Offset(w * 0.5f, h * 0.82f),
                    radius = radius * (1.2f + rippleProgress.value * 2.2f),
                    color = skin.glowColor.copy(alpha = (1f - rippleProgress.value) * 0.85f)
                )
            }

            // 2. Base Pedestal with Player Color Identification Ring
            if (showPedestal) {
                drawPawnPedestal(
                    center = Offset(w * 0.5f, h * 0.82f),
                    radius = radius,
                    playerColor = playerColor,
                    skin = skin
                )
            }

            // 3. Ambient Glow Aura
            if (showAura) {
                drawPawnAuraGlow(
                    center = center,
                    radius = radius * 1.55f,
                    skin = skin,
                    pulse = energyPulse
                )
            }

            // 4. Motion Trail Particles (when in motion preview)
            if (previewMode == PawnPreviewMode.TRAIL_MOTION) {
                drawMotionTrailParticles(
                    center = center,
                    radius = radius,
                    trailType = skin.trailType,
                    playerColor = playerColor
                )
            }

            // 5. Main Skin Geometry Rendering based on materialType and skin.id
            drawPawnSkinGeometry(
                center = center,
                radius = radius,
                skin = skin,
                playerColor = playerColor,
                rotationDeg = rotationDeg,
                energyPulse = energyPulse
            )

            // 6. Victory Celebration Sparkles
            if (previewMode == PawnPreviewMode.VICTORY_BURST && rippleProgress.value > 0f) {
                drawVictoryCelebrationBurst(
                    center = center,
                    radius = radius * 1.8f,
                    progress = rippleProgress.value,
                    playerColor = playerColor
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// BASE PEDESTAL & PLAYER COLOR IDENTIFICATION
// -------------------------------------------------------------------------------------------------
fun DrawScope.drawPawnPedestal(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin
) {
    val ovalW = radius * 1.95f
    val ovalH = radius * 0.72f

    // Soft drop shadow
    drawOval(
        color = Color(0xFF090D16).copy(alpha = 0.65f),
        topLeft = Offset(center.x - ovalW * 0.52f, center.y - ovalH * 0.15f),
        size = Size(ovalW * 1.04f, ovalH * 0.95f)
    )

    // Base Tier 1: Metallic Rim
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF3B0), Color(0xFFB45309), RadiantGold),
            start = Offset(center.x - ovalW * 0.5f, center.y),
            end = Offset(center.x + ovalW * 0.5f, center.y)
        ),
        topLeft = Offset(center.x - ovalW * 0.5f, center.y - ovalH * 0.45f),
        size = Size(ovalW, ovalH)
    )

    // Base Tier 2: Player Color Identification Ring (GUARANTEES NO COLOR CONFUSION ON BOARD!)
    val innerW = ovalW * 0.82f
    val innerH = ovalH * 0.78f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(playerColor, playerColor.copy(alpha = 0.85f), Color(0xFF0F172A)),
            center = center,
            radius = innerW * 0.55f
        ),
        topLeft = Offset(center.x - innerW * 0.5f, center.y - innerH * 0.48f),
        size = Size(innerW, innerH)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(center.x - innerW * 0.5f, center.y - innerH * 0.48f),
        size = Size(innerW, innerH),
        style = Stroke(width = radius * 0.08f)
    )
}

// -------------------------------------------------------------------------------------------------
// GLOW AURA
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawPawnAuraGlow(
    center: Offset,
    radius: Float,
    skin: PawnSkin,
    pulse: Float
) {
    val r = radius * pulse
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                skin.glowColor.copy(alpha = 0.45f * pulse),
                skin.primaryColor.copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )
}

// -------------------------------------------------------------------------------------------------
// PAWN SKIN GEOMETRY (Custom Rendering for every skin type)
// -------------------------------------------------------------------------------------------------
fun DrawScope.drawPawnSkinGeometry(
    center: Offset,
    radius: Float,
    skin: PawnSkin,
    playerColor: Color,
    rotationDeg: Float,
    energyPulse: Float
) {
    when {
        // --- 1. ROYAL SKINS: Imperial Crown & Sovereign Pawn ---
        skin.id == "royal_mini_crown" -> {
            drawImperialCrownPawn(center, radius, playerColor)
        }
        skin.id == "royal_sovereign_pawn" -> {
            drawRoyalSovereignPawn(center, radius, playerColor)
        }

        // --- 2. ROYAL SKINS: Winged Victory Figurine ---
        skin.id == "royal_golden_statue" -> {
            drawWingedVictoryPawn(center, radius, playerColor)
        }

        // --- 3. HERO MINIATURE: Replica & 3D Hero Figurine ---
        skin.id == "mini_hero_replica" || skin.id == "mini_hero_3d_figure" -> {
            drawHeroMiniatureReplicaPawn(center, radius, playerColor, skin, rotationDeg)
        }
        skin.materialType == PawnMaterialType.CHIBI_FIGURE || skin.materialType == PawnMaterialType.HERO_MINIATURE -> {
            drawChibiHeroPawn(center, radius, playerColor, skin)
        }

        // --- 4. NATURE: Leaf, Flower & Green Gaia Orb ---
        skin.id == "nature_leaf" -> {
            drawDewdropLeafPawn(center, radius, playerColor)
        }
        skin.id == "nature_magic_flower" -> {
            drawLotusBlossomPawn(center, radius, playerColor)
        }
        skin.id == "nature_green_orb" -> {
            drawEmeraldNatureGaiaPawn(center, radius, playerColor, rotationDeg)
        }

        // --- 5. LUXURY: Precious Gemstones (Emerald & Ruby Cut) ---
        skin.materialType == PawnMaterialType.PRECIOUS_GEMSTONE || skin.id == "luxury_gemstones" -> {
            drawPreciousGemstonesPawn(center, radius, playerColor, rotationDeg)
        }

        // --- 6. ELEMENTAL & FANTASY: Living Flame ---
        skin.materialType == PawnMaterialType.LIVING_FIRE -> {
            drawLivingFlamePawn(center, radius, playerColor, energyPulse)
        }

        // --- 7. LUXURY: Prismatic Diamond & Amethyst Crystal ---
        skin.materialType == PawnMaterialType.PRISMATIC_DIAMOND -> {
            drawPrismaticDiamondPawn(center, radius, playerColor, rotationDeg)
        }
        skin.materialType == PawnMaterialType.AMETHYST_CRYSTAL -> {
            drawCrystalMonolithPawn(center, radius, playerColor, skin, rotationDeg)
        }

        // --- 8. FANTASY & SPECIAL: Luminous Energy, Celestial & Festive ---
        skin.id == "fantasy_luminous_energy" -> {
            drawLuminousEnergyPawn(center, radius, playerColor, rotationDeg, energyPulse)
        }
        skin.id == "special_rare_celestial" -> {
            drawCelestialRarePawn(center, radius, playerColor, rotationDeg)
        }
        skin.id == "special_festive_jubilee" -> {
            drawFestiveJubileePawn(center, radius, playerColor, rotationDeg, energyPulse)
        }
        skin.materialType == PawnMaterialType.PLASMA_ENERGY -> {
            drawPlasmaReactorPawn(center, radius, playerColor, skin, rotationDeg, energyPulse)
        }

        // --- 9. LUXURY: Pure 24K Gold or Silver Metallic Pawn ---
        skin.materialType == PawnMaterialType.PURE_GOLD || skin.materialType == PawnMaterialType.PURE_SILVER -> {
            drawLuxuryPreciousMetalPawn(center, radius, playerColor, skin)
        }

        // --- 10. CLASSIC: Translucent Glass Sphere ---
        skin.materialType == PawnMaterialType.TRANSLUCENT_GLASS -> {
            drawTranslucentGlassPawn(center, radius, playerColor)
        }

        // --- 11. CLASSIC: Minimalist Puck ---
        skin.id == "classic_minimal" -> {
            drawMinimalistPuckPawn(center, radius, playerColor)
        }

        // --- DEFAULT & STANDARD 3D SPHERICAL PAWN WITH PLAYER COLOR & GOLD TRIM ---
        else -> {
            drawStandardSphericalPawn(center, radius, playerColor, skin)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// INDIVIDUAL PAWN SKIN RENDERERS
// -------------------------------------------------------------------------------------------------

private fun DrawScope.drawStandardSphericalPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin
) {
    val darkShade = Color(
        red = (playerColor.red * 0.35f).coerceIn(0f, 1f),
        green = (playerColor.green * 0.35f).coerceIn(0f, 1f),
        blue = (playerColor.blue * 0.35f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val highlight = Color(
        red = (playerColor.red + (1f - playerColor.red) * 0.65f).coerceIn(0f, 1f),
        green = (playerColor.green + (1f - playerColor.green) * 0.65f).coerceIn(0f, 1f),
        blue = (playerColor.blue + (1f - playerColor.blue) * 0.65f).coerceIn(0f, 1f),
        alpha = 1f
    )

    // 3D Sphere Body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(highlight, playerColor, darkShade),
            center = Offset(center.x - radius * 0.32f, center.y - radius * 0.35f),
            radius = radius * 1.35f
        ),
        radius = radius * 0.88f,
        center = center
    )

    // Golden / Accent Edge Trim
    drawCircle(
        color = RadiantGold,
        radius = radius * 0.88f,
        center = center,
        style = Stroke(width = radius * 0.08f)
    )

    // Specular shine catchlight
    drawCircle(
        color = Color.White.copy(alpha = 0.88f),
        radius = radius * 0.26f,
        center = Offset(center.x - radius * 0.28f, center.y - radius * 0.30f)
    )
}

private fun DrawScope.drawImperialCrownPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    val cW = radius * 1.8f
    val cH = radius * 1.4f
    val top = center.y - cH * 0.5f

    val crownPath = Path().apply {
        moveTo(center.x - cW * 0.5f, top + cH * 0.8f) // base left
        lineTo(center.x + cW * 0.5f, top + cH * 0.8f) // base right
        lineTo(center.x + cW * 0.55f, top + cH * 0.25f) // point 5 (right)
        lineTo(center.x + cW * 0.28f, top + cH * 0.50f) // dip right
        lineTo(center.x, top) // center high point
        lineTo(center.x - cW * 0.28f, top + cH * 0.50f) // dip left
        lineTo(center.x - cW * 0.55f, top + cH * 0.25f) // point 1 (left)
        close()
    }

    // Gold gradient fill
    drawPath(
        path = crownPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFF3B0), Color(0xFFFFD700), Color(0xFFB45309)),
            start = Offset(center.x - cW * 0.5f, top),
            end = Offset(center.x + cW * 0.5f, top + cH)
        )
    )
    drawPath(path = crownPath, color = RadiantGold, style = Stroke(width = radius * 0.08f))

    // Player Color Jewel in the Center Peak!
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, playerColor, Color(0xFF0F172A)),
            center = Offset(center.x, top + cH * 0.55f),
            radius = radius * 0.32f
        ),
        radius = radius * 0.30f,
        center = Offset(center.x, top + cH * 0.55f)
    )
    drawCircle(
        color = RadiantGold,
        radius = radius * 0.30f,
        center = Offset(center.x, top + cH * 0.55f),
        style = Stroke(width = radius * 0.07f)
    )
}

private fun DrawScope.drawWingedVictoryPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    // Left Wing
    val leftWing = Path().apply {
        moveTo(center.x, center.y)
        quadraticTo(center.x - radius * 1.3f, center.y - radius * 1.1f, center.x - radius * 0.9f, center.y - radius * 0.4f)
        close()
    }
    // Right Wing
    val rightWing = Path().apply {
        moveTo(center.x, center.y)
        quadraticTo(center.x + radius * 1.3f, center.y - radius * 1.1f, center.x + radius * 0.9f, center.y - radius * 0.4f)
        close()
    }

    drawPath(leftWing, brush = Brush.linearGradient(listOf(Color(0xFFFFFBEB), RadiantGold)))
    drawPath(rightWing, brush = Brush.linearGradient(listOf(Color(0xFFFFFBEB), RadiantGold)))

    // Central Figurine Body in Player Color
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, playerColor, Color(0xFF140B24)),
            center = Offset(center.x - radius * 0.15f, center.y - radius * 0.2f),
            radius = radius * 0.75f
        ),
        radius = radius * 0.65f,
        center = center
    )
    drawCircle(color = RadiantGold, radius = radius * 0.65f, center = center, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawChibiHeroPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin
) {
    val headR = radius * 0.70f
    val headCenter = Offset(center.x, center.y - radius * 0.25f)

    // Cape in Player Color
    val capePath = Path().apply {
        moveTo(headCenter.x - headR * 0.8f, headCenter.y + headR * 0.5f)
        lineTo(headCenter.x - headR * 1.1f, headCenter.y + headR * 1.4f)
        lineTo(headCenter.x + headR * 1.1f, headCenter.y + headR * 1.4f)
        lineTo(headCenter.x + headR * 0.8f, headCenter.y + headR * 0.5f)
        close()
    }
    drawPath(capePath, color = playerColor)

    // Chibi Head in cute shaded tone
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFDE68A), Color(0xFFF59E0B)),
            center = Offset(headCenter.x - headR * 0.2f, headCenter.y - headR * 0.2f),
            radius = headR * 1.3f
        ),
        radius = headR,
        center = headCenter
    )

    // Chibi Big Eyes
    val eyeY = headCenter.y + headR * 0.08f
    val eyeW = headR * 0.24f
    drawOval(Color(0xFF0F172A), topLeft = Offset(headCenter.x - headR * 0.45f - eyeW * 0.5f, eyeY - eyeW * 0.5f), size = Size(eyeW, eyeW * 1.3f))
    drawOval(Color(0xFF0F172A), topLeft = Offset(headCenter.x + headR * 0.45f - eyeW * 0.5f, eyeY - eyeW * 0.5f), size = Size(eyeW, eyeW * 1.3f))

    // Catchlight sparkles
    drawCircle(Color.White, radius = eyeW * 0.35f, center = Offset(headCenter.x - headR * 0.42f, eyeY - eyeW * 0.25f))
    drawCircle(Color.White, radius = eyeW * 0.35f, center = Offset(headCenter.x + headR * 0.48f, eyeY - eyeW * 0.25f))

    // Golden Helmet Crest / Horns
    drawArc(
        color = RadiantGold,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(headCenter.x - headR, headCenter.y - headR * 1.15f),
        size = Size(headR * 2, headR * 1.1f),
        style = Stroke(width = radius * 0.14f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawDewdropLeafPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    val leafPath = Path().apply {
        moveTo(center.x, center.y - radius * 1.15f) // top tip
        quadraticTo(center.x + radius * 1.1f, center.y, center.x, center.y + radius * 0.95f) // bottom stem
        quadraticTo(center.x - radius * 1.1f, center.y, center.x, center.y - radius * 1.15f)
        close()
    }

    drawPath(
        path = leafPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFBEF264), Color(0xFF65A30D), Color(0xFF14532D)),
            start = Offset(center.x, center.y - radius * 1.15f),
            end = Offset(center.x, center.y + radius * 0.95f)
        )
    )
    drawPath(path = leafPath, color = Color(0xFFECFCCB), style = Stroke(width = radius * 0.06f))

    // Center Vein
    drawLine(
        color = Color(0xFFECFCCB).copy(alpha = 0.65f),
        start = Offset(center.x, center.y - radius * 0.95f),
        end = Offset(center.x, center.y + radius * 0.80f),
        strokeWidth = radius * 0.07f,
        cap = StrokeCap.Round
    )

    // Dewdrop in Player Color
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, playerColor, Color(0xFF0F172A)),
            center = Offset(center.x - radius * 0.1f, center.y - radius * 0.1f),
            radius = radius * 0.45f
        ),
        radius = radius * 0.38f,
        center = center
    )
}

private fun DrawScope.drawLotusBlossomPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    val petalCount = 6
    for (i in 0 until petalCount) {
        val angleRad = (i * (2 * PI / petalCount)).toFloat()
        val px = center.x + cos(angleRad) * (radius * 0.55f)
        val py = center.y + sin(angleRad) * (radius * 0.55f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFCE7F3), Color(0xFFF472B6), Color(0xFF9D174D)),
                center = Offset(px, py),
                radius = radius * 0.55f
            ),
            radius = radius * 0.45f,
            center = Offset(px, py)
        )
    }

    // Lotus Core in Player Color
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, playerColor, Color(0xFF701A75)),
            center = center,
            radius = radius * 0.5f
        ),
        radius = radius * 0.42f,
        center = center
    )
    drawCircle(color = RadiantGold, radius = radius * 0.42f, center = center, style = Stroke(width = 2.5f))
}

private fun DrawScope.drawLivingFlamePawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    pulse: Float
) {
    val fH = radius * 2.2f * pulse
    val fW = radius * 1.5f

    val flamePath = Path().apply {
        moveTo(center.x, center.y - fH * 0.55f) // top flame tip
        quadraticTo(center.x + fW * 0.6f, center.y, center.x, center.y + fH * 0.45f)
        quadraticTo(center.x - fW * 0.6f, center.y, center.x, center.y - fH * 0.55f)
        close()
    }

    drawPath(
        path = flamePath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFEF08A), Color(0xFFF97316), Color(0xFFDC2626)),
            startY = center.y - fH * 0.55f,
            endY = center.y + fH * 0.45f
        )
    )

    // Inner Flame Core in Player Color
    val innerH = fH * 0.55f
    val innerW = fW * 0.55f
    val innerFlame = Path().apply {
        moveTo(center.x, center.y - innerH * 0.45f)
        quadraticTo(center.x + innerW * 0.5f, center.y, center.x, center.y + innerH * 0.45f)
        quadraticTo(center.x - innerW * 0.5f, center.y, center.x, center.y - innerH * 0.45f)
        close()
    }
    drawPath(innerFlame, brush = Brush.verticalGradient(listOf(Color.White, playerColor)))
}

private fun DrawScope.drawPrismaticDiamondPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    rotationDeg: Float
) {
    val dW = radius * 1.5f
    val dH = radius * 1.7f

    val diamondPath = Path().apply {
        moveTo(center.x, center.y - dH * 0.5f) // top tip
        lineTo(center.x + dW * 0.5f, center.y - dH * 0.1f) // right edge
        lineTo(center.x, center.y + dH * 0.5f) // bottom tip
        lineTo(center.x - dW * 0.5f, center.y - dH * 0.1f) // left edge
        close()
    }

    drawPath(
        path = diamondPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFF0F9FF), Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7)),
            start = Offset(center.x - dW * 0.5f, center.y - dH * 0.5f),
            end = Offset(center.x + dW * 0.5f, center.y + dH * 0.5f)
        )
    )
    drawPath(path = diamondPath, color = Color.White, style = Stroke(width = radius * 0.08f))

    // Facet cross lines
    drawLine(Color.White.copy(alpha = 0.85f), Offset(center.x - dW * 0.5f, center.y - dH * 0.1f), Offset(center.x + dW * 0.5f, center.y - dH * 0.1f), strokeWidth = 2f)
    drawLine(Color.White.copy(alpha = 0.85f), Offset(center.x, center.y - dH * 0.5f), Offset(center.x, center.y + dH * 0.5f), strokeWidth = 2f)

    // Player Color Inlaid Jewel Heart
    drawCircle(
        color = playerColor,
        radius = radius * 0.28f,
        center = Offset(center.x, center.y - dH * 0.1f)
    )
    drawCircle(
        color = Color.White,
        radius = radius * 0.12f,
        center = Offset(center.x - radius * 0.08f, center.y - dH * 0.1f - radius * 0.08f)
    )
}

private fun DrawScope.drawCrystalMonolithPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin,
    rotationDeg: Float
) {
    val cW = radius * 1.2f
    val cH = radius * 1.9f

    val monolith = Path().apply {
        moveTo(center.x, center.y - cH * 0.55f)
        lineTo(center.x + cW * 0.5f, center.y - cH * 0.35f)
        lineTo(center.x + cW * 0.4f, center.y + cH * 0.45f)
        lineTo(center.x - cW * 0.4f, center.y + cH * 0.45f)
        lineTo(center.x - cW * 0.5f, center.y - cH * 0.35f)
        close()
    }

    drawPath(
        path = monolith,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFF3E8FF), skin.primaryColor, skin.secondaryColor),
            start = Offset(center.x - cW * 0.5f, center.y - cH * 0.5f),
            end = Offset(center.x + cW * 0.5f, center.y + cH * 0.5f)
        )
    )
    drawPath(path = monolith, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = 2.5f))

    // Center Core in Player Color
    drawCircle(
        color = playerColor,
        radius = radius * 0.32f,
        center = center
    )
    drawCircle(color = RadiantGold, radius = radius * 0.32f, center = center, style = Stroke(width = 2f))
}

private fun DrawScope.drawPlasmaReactorPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin,
    rotationDeg: Float,
    pulse: Float
) {
    // Rotating Orbital Ring 1
    val r1 = radius * 1.25f
    val angle1 = Math.toRadians(rotationDeg.toDouble()).toFloat()
    drawOval(
        color = skin.glowColor.copy(alpha = 0.85f),
        topLeft = Offset(center.x - r1 * 0.95f, center.y - r1 * 0.35f),
        size = Size(r1 * 1.9f, r1 * 0.70f),
        style = Stroke(width = radius * 0.09f)
    )

    // Rotating Orbital Ring 2 (Counter angle)
    drawOval(
        color = playerColor.copy(alpha = 0.90f),
        topLeft = Offset(center.x - r1 * 0.35f, center.y - r1 * 0.95f),
        size = Size(r1 * 0.70f, r1 * 1.9f),
        style = Stroke(width = radius * 0.09f)
    )

    // Inner Glowing Fusion Core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, skin.primaryColor, playerColor),
            center = center,
            radius = radius * 0.85f * pulse
        ),
        radius = radius * 0.70f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = radius * 0.25f,
        center = center
    )
}

private fun DrawScope.drawLuxuryPreciousMetalPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin
) {
    val isGold = skin.materialType == PawnMaterialType.PURE_GOLD
    val baseGrad = if (isGold) {
        listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFD97706), Color(0xFF78350F))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569))
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = baseGrad,
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
            radius = radius * 1.4f
        ),
        radius = radius * 0.88f,
        center = center
    )

    // Player Color Inlay Ring
    drawCircle(
        color = playerColor,
        radius = radius * 0.45f,
        center = center
    )
    drawCircle(
        color = if (isGold) RadiantGold else Color.White,
        radius = radius * 0.45f,
        center = center,
        style = Stroke(width = radius * 0.09f)
    )

    // Specular Shine Catchlight
    drawCircle(
        color = Color.White,
        radius = radius * 0.22f,
        center = Offset(center.x - radius * 0.32f, center.y - radius * 0.32f)
    )
}

private fun DrawScope.drawTranslucentGlassPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    // Translucent glass sphere with inner orb in playerColor
    drawCircle(
        color = Color.White.copy(alpha = 0.28f),
        radius = radius * 0.88f,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = radius * 0.88f,
        center = center,
        style = Stroke(width = radius * 0.08f)
    )

    // Inner glowing sphere in playerColor
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, playerColor, Color(0xFF0F172A)),
            center = center,
            radius = radius * 0.55f
        ),
        radius = radius * 0.48f,
        center = center
    )

    // Glass curve reflection highlight
    drawArc(
        color = Color.White,
        startAngle = 180f,
        sweepAngle = 90f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.75f, center.y - radius * 0.75f),
        size = Size(radius * 1.5f, radius * 1.5f),
        style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMinimalistPuckPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    // Clean matte puck
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(playerColor, Color(0xFF090D16)),
            start = Offset(center.x, center.y - radius * 0.8f),
            end = Offset(center.x, center.y + radius * 0.8f)
        ),
        radius = radius * 0.82f,
        center = center
    )
    drawCircle(color = RadiantGold, radius = radius * 0.82f, center = center, style = Stroke(width = radius * 0.08f))

    // Center player icon dot
    drawCircle(color = Color.White, radius = radius * 0.28f, center = center)
}

// -------------------------------------------------------------------------------------------------
// MOTION TRAIL PARTICLES
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawMotionTrailParticles(
    center: Offset,
    radius: Float,
    trailType: PawnTrailFxType,
    playerColor: Color
) {
    val particleCount = 6
    for (i in 1..particleCount) {
        val trailX = center.x - i * (radius * 0.55f)
        val trailY = center.y + (sin((i * 1.2).toFloat()) * radius * 0.25f)
        val pRadius = radius * (0.28f - i * 0.038f).coerceAtLeast(0.08f)
        val alpha = (1f - (i.toFloat() / (particleCount + 1))).coerceIn(0f, 1f)

        val particleColor = when (trailType) {
            PawnTrailFxType.FIRE_EMBERS -> Color(0xFFF97316)
            PawnTrailFxType.ICE_CRYSTALS -> Color(0xFF93C5FD)
            PawnTrailFxType.LEAF_SWIRL -> Color(0xFF84CC16)
            PawnTrailFxType.LIGHTNING_ARCS -> Color(0xFFFDE047)
            PawnTrailFxType.BUBBLE_STREAM -> Color(0xFF38BDF8)
            PawnTrailFxType.NEON_GLOW -> playerColor
            else -> RadiantGold
        }

        drawCircle(
            color = particleColor.copy(alpha = alpha),
            radius = pRadius,
            center = Offset(trailX, trailY)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// LANDING RIPPLE & VICTORY BURST
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawLandingRipple(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawOval(
        color = color,
        topLeft = Offset(center.x - radius, center.y - radius * 0.35f),
        size = Size(radius * 2, radius * 0.70f),
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawVictoryCelebrationBurst(
    center: Offset,
    radius: Float,
    progress: Float,
    playerColor: Color
) {
    val sparkCount = 8
    val curR = radius * progress
    for (i in 0 until sparkCount) {
        val angleRad = (i * (2 * PI / sparkCount)).toFloat()
        val sx = center.x + cos(angleRad) * curR
        val sy = center.y + sin(angleRad) * curR
        drawCircle(
            color = if (i % 2 == 0) RadiantGold else playerColor,
            radius = 4.5f * (1f - progress * 0.5f),
            center = Offset(sx, sy)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// NEW SPECIALIZED PAWN SKIN RENDERERS
// -------------------------------------------------------------------------------------------------

private fun DrawScope.drawRoyalSovereignPawn(
    center: Offset,
    radius: Float,
    playerColor: Color
) {
    // Chess monarch body with royal velvet and gold embellishments
    val bodyH = radius * 1.55f
    val bodyW = radius * 1.15f
    val top = center.y - bodyH * 0.55f

    // 1. Velvet Mantle
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF6366F1), Color(0xFF312E81), Color(0xFF1E1B4B)),
            startY = top,
            endY = top + bodyH
        ),
        topLeft = Offset(center.x - bodyW * 0.5f, top),
        size = Size(bodyW, bodyH)
    )

    // 2. Ermine & Gold Royal Collar
    drawOval(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFFBEB), RadiantGold, Color(0xFFFFFBEB)),
            startX = center.x - bodyW * 0.45f,
            endX = center.x + bodyW * 0.45f
        ),
        topLeft = Offset(center.x - bodyW * 0.45f, top + bodyH * 0.38f),
        size = Size(bodyW * 0.90f, bodyH * 0.22f)
    )

    // 3. Player Accent Sash
    drawOval(
        color = playerColor,
        topLeft = Offset(center.x - bodyW * 0.35f, top + bodyH * 0.62f),
        size = Size(bodyW * 0.70f, bodyH * 0.14f)
    )

    // 4. Crown Headpiece with Golden Cross
    val headR = radius * 0.36f
    val headY = top + headR * 0.5f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFE066), RadiantGold, Color(0xFFB45309)),
            center = Offset(center.x - headR * 0.3f, headY - headR * 0.3f),
            radius = headR * 1.2f
        ),
        radius = headR,
        center = Offset(center.x, headY)
    )

    // Finial cross atop crown
    val crossH = headR * 0.65f
    drawLine(
        color = RadiantGold,
        start = Offset(center.x, headY - headR - crossH),
        end = Offset(center.x, headY - headR + 2f),
        strokeWidth = 3f
    )
    drawLine(
        color = RadiantGold,
        start = Offset(center.x - crossH * 0.45f, headY - headR - crossH * 0.6f),
        end = Offset(center.x + crossH * 0.45f, headY - headR - crossH * 0.6f),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawHeroMiniatureReplicaPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    skin: PawnSkin,
    rotationDeg: Float
) {
    // 3D Miniature Paladin/Hero Figurine with Shield & Crest
    val h = radius * 1.45f
    val w = radius * 1.05f

    // Shield Background in Player Accent Color
    val shieldPath = Path().apply {
        moveTo(center.x, center.y - h * 0.55f)
        lineTo(center.x + w * 0.48f, center.y - h * 0.30f)
        lineTo(center.x + w * 0.42f, center.y + h * 0.15f)
        lineTo(center.x, center.y + h * 0.55f)
        lineTo(center.x - w * 0.42f, center.y + h * 0.15f)
        lineTo(center.x - w * 0.48f, center.y - h * 0.30f)
        close()
    }

    drawPath(
        path = shieldPath,
        brush = Brush.radialGradient(
            colors = listOf(playerColor, playerColor.copy(alpha = 0.85f), Color(0xFF0F172A)),
            center = center,
            radius = radius * 1.2f
        )
    )
    drawPath(
        path = shieldPath,
        brush = Brush.linearGradient(
            colors = listOf(RadiantGold, Color(0xFFFFFBEB), RadiantGold),
            start = Offset(center.x - w * 0.5f, center.y),
            end = Offset(center.x + w * 0.5f, center.y)
        ),
        style = Stroke(width = 3.5f)
    )

    // Golden Knight Crest in Center
    val coreR = radius * 0.35f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF3B0), RadiantGold, Color(0xFFB45309)),
            center = Offset(center.x - coreR * 0.2f, center.y - coreR * 0.2f),
            radius = coreR
        ),
        radius = coreR,
        center = center
    )

    // Crossed Swords emblem
    val bladeLen = coreR * 0.75f
    drawLine(
        color = Color.White,
        start = Offset(center.x - bladeLen, center.y - bladeLen),
        end = Offset(center.x + bladeLen, center.y + bladeLen),
        strokeWidth = 2.5f
    )
    drawLine(
        color = Color.White,
        start = Offset(center.x + bladeLen, center.y - bladeLen),
        end = Offset(center.x - bladeLen, center.y + bladeLen),
        strokeWidth = 2.5f
    )
}

private fun DrawScope.drawEmeraldNatureGaiaPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    rotationDeg: Float
) {
    // Jade Gaia Emerald Orb with Living Vines
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFA7F3D0),
                Color(0xFF10B981),
                Color(0xFF065F46),
                Color(0xFF022C22)
            ),
            center = Offset(center.x - radius * 0.32f, center.y - radius * 0.32f),
            radius = radius * 1.15f
        ),
        radius = radius * 0.88f,
        center = center
    )

    // Player Identification Accent Ring
    drawCircle(
        color = playerColor.copy(alpha = 0.85f),
        radius = radius * 0.94f,
        center = center,
        style = Stroke(width = 3f)
    )

    // Spiral Nature Leaves orbiting
    val leafAngle = (rotationDeg * (PI / 180f)).toFloat()
    for (i in 0 until 4) {
        val angle = leafAngle + i * (PI.toFloat() / 2f)
        val lx = center.x + cos(angle) * (radius * 0.72f)
        val ly = center.y + sin(angle) * (radius * 0.72f)
        drawCircle(
            color = Color(0xFF84CC16),
            radius = radius * 0.16f,
            center = Offset(lx, ly)
        )
        drawCircle(
            color = Color(0xFFECFCCB),
            radius = radius * 0.08f,
            center = Offset(lx - 1f, ly - 1f)
        )
    }
}

private fun DrawScope.drawPreciousGemstonesPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    rotationDeg: Float
) {
    // Cut Brilliant Gemstones: Emerald & Ruby Facets with specular dispersion
    val r = radius * 0.88f
    val hexPath = Path()
    val sides = 6
    for (i in 0 until sides) {
        val angle = (i * (2 * PI / sides) + rotationDeg * (PI / 180f)).toFloat()
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
    }
    hexPath.close()

    // Dual Gemstone Body (Emerald Green & Ruby Crimson split)
    drawPath(
        path = hexPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFFDC2626), Color(0xFF991B1B)),
            start = Offset(center.x - r, center.y - r),
            end = Offset(center.x + r, center.y + r)
        )
    )

    // Golden Edge Faceting
    drawPath(
        path = hexPath,
        brush = Brush.linearGradient(
            colors = listOf(RadiantGold, Color(0xFFFFFBEB), RadiantGold),
            start = Offset(center.x - r, center.y),
            end = Offset(center.x + r, center.y)
        ),
        style = Stroke(width = 3f)
    )

    // Central Table Facet
    val innerR = r * 0.50f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.90f), Color(0xFFA7F3D0), playerColor),
            center = Offset(center.x - innerR * 0.25f, center.y - innerR * 0.25f),
            radius = innerR
        ),
        radius = innerR,
        center = center
    )
}

private fun DrawScope.drawLuminousEnergyPawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    rotationDeg: Float,
    energyPulse: Float
) {
    val r = radius * 0.85f * energyPulse

    // 1. Sunburst Radiant Flare Rays
    val rayCount = 12
    for (i in 0 until rayCount) {
        val angle = (i * (2 * PI / rayCount) + rotationDeg * (PI / 180f)).toFloat()
        val len = if (i % 2 == 0) r * 1.35f else r * 1.15f
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFFBEB), RadiantGold, Color.Transparent),
                start = center,
                end = Offset(center.x + cos(angle) * len, center.y + sin(angle) * len)
            ),
            start = center,
            end = Offset(center.x + cos(angle) * len, center.y + sin(angle) * len),
            strokeWidth = 3f
        )
    }

    // 2. Brilliant White-Gold Plasma Sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White,
                Color(0xFFFEF08A),
                Color(0xFFF59E0B),
                playerColor.copy(alpha = 0.85f)
            ),
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )
}

private fun DrawScope.drawCelestialRarePawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    rotationDeg: Float
) {
    // Deep Space Indigo Sphere
    val r = radius * 0.82f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE0E7FF), Color(0xFF6366F1), Color(0xFF312E81), Color(0xFF0F172A)),
            center = Offset(center.x - r * 0.35f, center.y - r * 0.35f),
            radius = r * 1.25f
        ),
        radius = r,
        center = center
    )

    // Tilted Orbital Planetary Ring
    withTransform({
        rotate(degrees = -25f, pivot = center)
    }) {
        drawOval(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    playerColor.copy(alpha = 0.9f),
                    RadiantGold,
                    playerColor.copy(alpha = 0.9f),
                    Color.Transparent
                )
            ),
            topLeft = Offset(center.x - r * 1.45f, center.y - r * 0.32f),
            size = Size(r * 2.90f, r * 0.64f),
            style = Stroke(width = 3.5f)
        )
    }

    // Twinkling Star Point
    val starAngle = (rotationDeg * (PI / 180f)).toFloat()
    val sx = center.x + cos(starAngle) * (r * 1.15f)
    val sy = center.y + sin(starAngle) * (r * 0.45f)
    drawCircle(Color.White, radius = 3.5f, center = Offset(sx, sy))
}

private fun DrawScope.drawFestiveJubileePawn(
    center: Offset,
    radius: Float,
    playerColor: Color,
    rotationDeg: Float,
    energyPulse: Float
) {
    val r = radius * 0.85f

    // Golden Celebration Sphere
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFBEB), RadiantGold, Color(0xFFB45309)),
            center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
            radius = r * 1.2f
        ),
        radius = r,
        center = center
    )

    // Player Identification Ribbon Sash
    drawOval(
        color = playerColor,
        topLeft = Offset(center.x - r * 0.85f, center.y - r * 0.20f),
        size = Size(r * 1.70f, r * 0.40f)
    )

    // Confetti Sparkles
    val confettiColors = listOf(Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF38BDF8), Color(0xFFA855F7))
    for (i in 0 until 6) {
        val a = ((i * 60f + rotationDeg) * (PI / 180f)).toFloat()
        val dist = r * (0.95f + 0.35f * ((i % 2) + 0.5f) * energyPulse)
        drawCircle(
            color = confettiColors[i % confettiColors.size],
            radius = 3f,
            center = Offset(center.x + cos(a) * dist, center.y + sin(a) * dist)
        )
    }
}
