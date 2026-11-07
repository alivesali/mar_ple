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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.CharacterGender
import com.example.model.GameCharacter
import com.example.model.HeldItemVisual
import com.example.model.HeroAnimationStance
import com.example.model.HeroRenderQuality
import com.example.model.HeroVisualCatalog
import com.example.model.HeroVisualProfile
import com.example.ui.theme.RadiantGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-End Original Fantasy Hero Character Renderer with Interactive Animation Stances.
 * Supports both Cinematic 3D Layered Mode and Lightweight 2D Mode.
 */
@Composable
fun HeroVisualRenderer(
    character: GameCharacter,
    modifier: Modifier = Modifier,
    stance: HeroAnimationStance = HeroAnimationStance.IDLE,
    quality: HeroRenderQuality = HeroRenderQuality.CINEMATIC_3D,
    size: Dp = 220.dp,
    showPedestal: Boolean = true,
    showAuraParticles: Boolean = true
) {
    val profile = remember(character.id) { HeroVisualCatalog.getProfile(character.id) }

    // Stance transition animation driver
    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(stance) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    // Continuous loop animations for breathing, orbital particles, and weapon gleam
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim_${character.id}")

    val idleBreath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breath"
    )

    val particleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_rotation"
    )

    val weaponGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "weapon_glow"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height

            // Calculate dynamic offsets based on active stance & animation loop
            val breathOffset = when (stance) {
                HeroAnimationStance.IDLE -> sin(idleBreath * PI.toFloat()) * (canvasH * 0.024f)
                HeroAnimationStance.ATTACK -> canvasH * 0.05f * transitionProgress.value
                HeroAnimationStance.DEFENSE -> -canvasH * 0.02f * transitionProgress.value
                HeroAnimationStance.ABILITY -> -canvasH * 0.06f * sin(transitionProgress.value * PI.toFloat() * 0.8f)
                HeroAnimationStance.VICTORY -> -canvasH * 0.04f - sin(idleBreath * PI.toFloat()) * (canvasH * 0.015f)
                HeroAnimationStance.DEFEAT -> canvasH * 0.07f * transitionProgress.value
            }

            val lungeX = when (stance) {
                HeroAnimationStance.ATTACK -> sin(transitionProgress.value * PI.toFloat()) * (canvasW * 0.09f)
                HeroAnimationStance.DEFENSE -> -canvasW * 0.03f * transitionProgress.value
                else -> 0f
            }

            val rootCenter = Offset(canvasW * 0.5f + lungeX, canvasH * 0.52f + breathOffset)

            // 1. Stage Ground Pedestal (Black & Gold Hero Altar)
            if (showPedestal) {
                drawHeroPedestal(
                    center = Offset(canvasW * 0.5f, canvasH * 0.88f),
                    width = canvasW * 0.78f,
                    height = canvasH * 0.18f,
                    profile = profile,
                    quality = quality
                )
            }

            // 2. Background Elemental Aura & Ring
            drawHeroElementalAura(
                center = rootCenter,
                radius = canvasW * 0.44f,
                profile = profile,
                stance = stance,
                progress = transitionProgress.value,
                quality = quality,
                rotationDeg = particleRotation
            )

            // 3. Ambient Elemental Orbiting Particles
            if (showAuraParticles && quality == HeroRenderQuality.CINEMATIC_3D) {
                drawOrbitalParticles(
                    center = rootCenter,
                    radius = canvasW * 0.40f,
                    profile = profile,
                    rotationDeg = particleRotation,
                    stance = stance
                )
            }

            // 4. Stance Specific Effects Under Character (Shield Dome / Shockwave / Magic Runes)
            when (stance) {
                HeroAnimationStance.DEFENSE -> {
                    drawDefensiveBarrier(
                        center = rootCenter,
                        profile = profile,
                        progress = transitionProgress.value,
                        quality = quality
                    )
                }
                HeroAnimationStance.ABILITY -> {
                    drawAbilityRunesBurst(
                        center = rootCenter,
                        profile = profile,
                        progress = transitionProgress.value,
                        rotationDeg = particleRotation
                    )
                }
                HeroAnimationStance.VICTORY -> {
                    drawVictorySparkles(
                        center = rootCenter,
                        profile = profile,
                        pulse = weaponGlowPulse
                    )
                }
                else -> {}
            }

            // 5. Main Hero Body (Torso, Armor, Cloak, Head, Hair, Face)
            drawHeroBodyFigure(
                center = rootCenter,
                canvasW = canvasW,
                canvasH = canvasH,
                character = character,
                profile = profile,
                stance = stance,
                progress = transitionProgress.value,
                quality = quality
            )

            // 6. Signature Weapon & Held Prop (Animated with stance)
            drawHeroWeaponProp(
                center = rootCenter,
                canvasW = canvasW,
                canvasH = canvasH,
                profile = profile,
                stance = stance,
                progress = transitionProgress.value,
                glowPulse = weaponGlowPulse,
                quality = quality
            )

            // 7. Foreground Slashing Trails / Sparks for Attack
            if (stance == HeroAnimationStance.ATTACK) {
                drawAttackSlashArc(
                    center = rootCenter,
                    profile = profile,
                    progress = transitionProgress.value
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// PEDESTAL DRAWING
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawHeroPedestal(
    center: Offset,
    width: Float,
    height: Float,
    profile: HeroVisualProfile,
    quality: HeroRenderQuality
) {
    val top = center.y - height * 0.5f
    val baseRect = Size(width, height)
    val topLeft = Offset(center.x - width * 0.5f, top)

    // Base shadow
    drawOval(
        color = Color(0xFF090D16).copy(alpha = 0.75f),
        topLeft = Offset(center.x - width * 0.54f, top + height * 0.15f),
        size = Size(width * 1.08f, height * 0.95f)
    )

    // Tier 1: Outer Gold Rim
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(RadiantGold, Color(0xFFB45309), Color(0xFF140B24)),
            center = center,
            radius = width * 0.52f
        ),
        topLeft = topLeft,
        size = baseRect
    )

    // Tier 2: Obsidian Inset Surface
    val insetW = width * 0.86f
    val insetH = height * 0.78f
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), profile.primaryColor.copy(alpha = 0.35f)),
            start = Offset(center.x, top),
            end = Offset(center.x, top + insetH)
        ),
        topLeft = Offset(center.x - insetW * 0.5f, center.y - insetH * 0.45f),
        size = Size(insetW, insetH)
    )

    // Inner element glow ring
    drawOval(
        color = profile.accentGlowColor.copy(alpha = if (quality == HeroRenderQuality.CINEMATIC_3D) 0.60f else 0.40f),
        topLeft = Offset(center.x - insetW * 0.5f, center.y - insetH * 0.45f),
        size = Size(insetW, insetH),
        style = Stroke(width = width * 0.018f)
    )
}

// -------------------------------------------------------------------------------------------------
// ELEMENTAL AURA
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawHeroElementalAura(
    center: Offset,
    radius: Float,
    profile: HeroVisualProfile,
    stance: HeroAnimationStance,
    progress: Float,
    quality: HeroRenderQuality,
    rotationDeg: Float
) {
    val auraScale = when (stance) {
        HeroAnimationStance.ABILITY -> 1.35f + 0.15f * progress
        HeroAnimationStance.VICTORY -> 1.25f
        HeroAnimationStance.DEFENSE -> 1.15f
        else -> 1.0f
    }

    val r = radius * auraScale

    // Soft Radial Aura Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                profile.primaryColor.copy(alpha = if (quality == HeroRenderQuality.CINEMATIC_3D) 0.45f else 0.25f),
                profile.secondaryColor.copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = center,
            radius = r
        ),
        radius = r,
        center = center
    )

    // Celestial Magic Rings (Dota 2 / Fantasy Runes Halo)
    val ringR = r * 0.82f
    drawCircle(
        color = profile.accentGlowColor.copy(alpha = 0.35f),
        radius = ringR,
        center = center,
        style = Stroke(width = r * 0.016f)
    )

    // Runic ticks along the aura ring
    val tickCount = 12
    for (i in 0 until tickCount) {
        val angleRad = Math.toRadians((i * (360f / tickCount) + rotationDeg).toDouble())
        val startX = center.x + (ringR - r * 0.04f) * cos(angleRad).toFloat()
        val startY = center.y + (ringR - r * 0.04f) * sin(angleRad).toFloat()
        val endX = center.x + (ringR + r * 0.04f) * cos(angleRad).toFloat()
        val endY = center.y + (ringR + r * 0.04f) * sin(angleRad).toFloat()

        drawLine(
            color = profile.accentGlowColor.copy(alpha = 0.55f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = r * 0.018f,
            cap = StrokeCap.Round
        )
    }
}

// -------------------------------------------------------------------------------------------------
// ORBITAL PARTICLES
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawOrbitalParticles(
    center: Offset,
    radius: Float,
    profile: HeroVisualProfile,
    rotationDeg: Float,
    stance: HeroAnimationStance
) {
    val particleCount = if (stance == HeroAnimationStance.ABILITY) 8 else 5
    for (i in 0 until particleCount) {
        val angleRad = Math.toRadians((i * (360f / particleCount) + rotationDeg * (if (i % 2 == 0) 1f else -1f)).toDouble())
        val dist = radius * (0.65f + 0.25f * sin((i * 1.5).toFloat()))
        val px = center.x + dist * cos(angleRad).toFloat()
        val py = center.y + dist * sin(angleRad).toFloat() * 0.85f
        val pRadius = radius * 0.045f

        // Glowing particle orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, profile.accentGlowColor, Color.Transparent),
                center = Offset(px, py),
                radius = pRadius * 1.8f
            ),
            radius = pRadius * 1.8f,
            center = Offset(px, py)
        )
        drawCircle(
            color = Color.White,
            radius = pRadius * 0.65f,
            center = Offset(px, py)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// HERO BODY FIGURE (Torso, Armor Plates, Head, Face, Hair)
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawHeroBodyFigure(
    center: Offset,
    canvasW: Float,
    canvasH: Float,
    character: GameCharacter,
    profile: HeroVisualProfile,
    stance: HeroAnimationStance,
    progress: Float,
    quality: HeroRenderQuality
) {
    val scale = canvasW * 0.0038f
    val cx = center.x
    val cy = center.y

    // Head dimensions
    val headR = 26f * scale
    val headCenter = Offset(cx, cy - 38f * scale)

    // Torso & Shoulders dimensions
    val shoulderY = headCenter.y + headR * 0.85f
    val torsoW = 54f * scale
    val torsoH = 68f * scale

    // 1. Cape / Back Mantle
    val capePath = Path().apply {
        moveTo(cx - torsoW * 0.55f, shoulderY)
        quadraticTo(cx - torsoW * 0.80f, shoulderY + torsoH * 0.70f, cx - torsoW * 0.70f, shoulderY + torsoH * 1.15f)
        lineTo(cx + torsoW * 0.70f, shoulderY + torsoH * 1.15f)
        quadraticTo(cx + torsoW * 0.80f, shoulderY + torsoH * 0.70f, cx + torsoW * 0.55f, shoulderY)
        close()
    }
    drawPath(
        path = capePath,
        brush = Brush.verticalGradient(
            colors = listOf(profile.secondaryColor, Color(0xFF0F172A)),
            startY = shoulderY,
            endY = shoulderY + torsoH * 1.2f
        )
    )
    drawPath(
        path = capePath,
        color = profile.accentGlowColor.copy(alpha = 0.45f),
        style = Stroke(width = 2.5f * scale)
    )

    // Back Hair for Female Heroes
    if (character.gender == CharacterGender.FEMALE) {
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(profile.hairColor, profile.hairColor.copy(alpha = 0.85f)),
                startY = headCenter.y - headR,
                endY = shoulderY + torsoH * 0.75f
            ),
            topLeft = Offset(headCenter.x - headR * 1.35f, headCenter.y - headR * 0.85f),
            size = Size(headR * 2.7f, torsoH * 1.05f)
        )
    }

    // 2. Sculpted Armor Torso
    val armorPath = Path().apply {
        moveTo(cx - torsoW * 0.48f, shoulderY)
        lineTo(cx + torsoW * 0.48f, shoulderY)
        lineTo(cx + torsoW * 0.38f, shoulderY + torsoH * 0.85f)
        quadraticTo(cx, shoulderY + torsoH, cx - torsoW * 0.38f, shoulderY + torsoH * 0.85f)
        close()
    }
    drawPath(
        path = armorPath,
        brush = Brush.linearGradient(
            colors = listOf(profile.armorColor, profile.primaryColor, profile.armorColor),
            start = Offset(cx - torsoW * 0.5f, shoulderY),
            end = Offset(cx + torsoW * 0.5f, shoulderY + torsoH)
        )
    )

    // Armor Trim and Crest Emblems
    drawPath(
        path = armorPath,
        color = RadiantGold,
        style = Stroke(width = 2.8f * scale)
    )

    // Golden Breastplate Chevron V
    val chevronPath = Path().apply {
        moveTo(cx - torsoW * 0.28f, shoulderY + torsoH * 0.15f)
        lineTo(cx, shoulderY + torsoH * 0.45f)
        lineTo(cx + torsoW * 0.28f, shoulderY + torsoH * 0.15f)
    }
    drawLine(
        color = RadiantGold,
        start = Offset(cx - torsoW * 0.28f, shoulderY + torsoH * 0.15f),
        end = Offset(cx, shoulderY + torsoH * 0.45f),
        strokeWidth = 3f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = RadiantGold,
        start = Offset(cx + torsoW * 0.28f, shoulderY + torsoH * 0.15f),
        end = Offset(cx, shoulderY + torsoH * 0.45f),
        strokeWidth = 3f * scale,
        cap = StrokeCap.Round
    )

    // Center Core Arc Gem on Chest
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, profile.accentGlowColor, profile.primaryColor),
            center = Offset(cx, shoulderY + torsoH * 0.45f),
            radius = 7f * scale
        ),
        radius = 7f * scale,
        center = Offset(cx, shoulderY + torsoH * 0.45f)
    )

    // Pauldrons (Left & Right Shoulder Guards)
    val pauldronW = 20f * scale
    val pauldronH = 24f * scale
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(RadiantGold, profile.armorColor),
            start = Offset(cx - torsoW * 0.55f - pauldronW, shoulderY - pauldronH * 0.4f),
            end = Offset(cx - torsoW * 0.55f, shoulderY + pauldronH * 0.6f)
        ),
        topLeft = Offset(cx - torsoW * 0.55f - pauldronW * 0.6f, shoulderY - pauldronH * 0.3f),
        size = Size(pauldronW, pauldronH)
    )
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(RadiantGold, profile.armorColor),
            start = Offset(cx + torsoW * 0.55f, shoulderY - pauldronH * 0.4f),
            end = Offset(cx + torsoW * 0.55f + pauldronW, shoulderY + pauldronH * 0.6f)
        ),
        topLeft = Offset(cx + torsoW * 0.55f - pauldronW * 0.4f, shoulderY - pauldronH * 0.3f),
        size = Size(pauldronW, pauldronH)
    )

    // 3. Head & Face (3D Radial Shaded Skin)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                character.skinTone,
                character.skinTone.copy(alpha = 0.85f)
            ),
            center = Offset(headCenter.x - headR * 0.25f, headCenter.y - headR * 0.25f),
            radius = headR * 1.35f
        ),
        radius = headR,
        center = headCenter
    )

    // 4. Expressive Eyes with Heroic Gaze
    val eyeY = headCenter.y + headR * 0.08f
    val leftEyeX = headCenter.x - headR * 0.38f
    val rightEyeX = headCenter.x + headR * 0.38f
    val eyeW = headR * 0.22f
    val eyeH = headR * 0.16f

    // Eye sockets / White sclera
    drawOval(Color.White, topLeft = Offset(leftEyeX - eyeW * 0.5f, eyeY - eyeH * 0.5f), size = Size(eyeW, eyeH))
    drawOval(Color.White, topLeft = Offset(rightEyeX - eyeW * 0.5f, eyeY - eyeH * 0.5f), size = Size(eyeW, eyeH))

    // Iris in Hero's Element Eye Color
    drawCircle(profile.eyeColor, radius = eyeH * 0.65f, center = Offset(leftEyeX, eyeY))
    drawCircle(profile.eyeColor, radius = eyeH * 0.65f, center = Offset(rightEyeX, eyeY))

    // Pupil
    drawCircle(Color.Black, radius = eyeH * 0.35f, center = Offset(leftEyeX, eyeY))
    drawCircle(Color.Black, radius = eyeH * 0.35f, center = Offset(rightEyeX, eyeY))

    // Specular Catchlight Spark
    drawCircle(Color.White, radius = eyeH * 0.20f, center = Offset(leftEyeX - 1.5f * scale, eyeY - 1.5f * scale))
    drawCircle(Color.White, radius = eyeH * 0.20f, center = Offset(rightEyeX - 1.5f * scale, eyeY - 1.5f * scale))

    // Heroic Eyebrows
    val browY = eyeY - eyeH * 0.95f
    drawLine(
        color = profile.hairColor,
        start = Offset(leftEyeX - eyeW * 0.55f, browY + 1.5f * scale),
        end = Offset(leftEyeX + eyeW * 0.55f, browY - 1.5f * scale),
        strokeWidth = 2.2f * scale,
        cap = StrokeCap.Round
    )
    drawLine(
        color = profile.hairColor,
        start = Offset(rightEyeX - eyeW * 0.55f, browY - 1.5f * scale),
        end = Offset(rightEyeX + eyeW * 0.55f, browY + 1.5f * scale),
        strokeWidth = 2.2f * scale,
        cap = StrokeCap.Round
    )

    // Nose & Smile / Expression
    drawArc(
        color = Color(0xFF7C2D12),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(headCenter.x - headR * 0.25f, headCenter.y + headR * 0.28f),
        size = Size(headR * 0.50f, headR * 0.28f),
        style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round)
    )

    // 5. Stylized Fantasy Hair / Helmet Crest / Crown
    val hairPath = Path().apply {
        moveTo(headCenter.x - headR * 1.15f, headCenter.y)
        quadraticTo(headCenter.x - headR * 0.85f, headCenter.y - headR * 1.35f, headCenter.x, headCenter.y - headR * 1.45f)
        quadraticTo(headCenter.x + headR * 0.85f, headCenter.y - headR * 1.35f, headCenter.x + headR * 1.15f, headCenter.y)
        quadraticTo(headCenter.x + headR * 0.55f, headCenter.y - headR * 0.65f, headCenter.x, headCenter.y - headR * 0.55f)
        quadraticTo(headCenter.x - headR * 0.55f, headCenter.y - headR * 0.65f, headCenter.x - headR * 1.15f, headCenter.y)
        close()
    }
    drawPath(path = hairPath, color = profile.hairColor)

    // Head Circlet / Diadem in Gold
    drawArc(
        color = RadiantGold,
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(headCenter.x - headR * 1.05f, headCenter.y - headR * 0.95f),
        size = Size(headR * 2.1f, headR * 0.85f),
        style = Stroke(width = 3f * scale, cap = StrokeCap.Round)
    )
}

// -------------------------------------------------------------------------------------------------
// HERO WEAPON PROP (Interactive with Stance)
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawHeroWeaponProp(
    center: Offset,
    canvasW: Float,
    canvasH: Float,
    profile: HeroVisualProfile,
    stance: HeroAnimationStance,
    progress: Float,
    glowPulse: Float,
    quality: HeroRenderQuality
) {
    val scale = canvasW * 0.0038f

    // Weapon position offsets based on stance
    val (wpX, wpY, wpRotation) = when (stance) {
        HeroAnimationStance.IDLE -> Triple(center.x + 48f * scale, center.y + 12f * scale, 0f)
        HeroAnimationStance.ATTACK -> Triple(center.x + 64f * scale * progress, center.y - 18f * scale * progress, 35f * progress)
        HeroAnimationStance.DEFENSE -> Triple(center.x + 12f * scale, center.y - 8f * scale, -15f)
        HeroAnimationStance.ABILITY -> Triple(center.x + 36f * scale, center.y - 42f * scale * progress, -25f)
        HeroAnimationStance.VICTORY -> Triple(center.x + 45f * scale, center.y - 58f * scale, 55f)
        HeroAnimationStance.DEFEAT -> Triple(center.x + 38f * scale, center.y + 40f * scale, -30f)
    }

    val weaponCenter = Offset(wpX, wpY)
    val weaponSize = 38f * scale

    // Weapon Aura Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                profile.accentGlowColor.copy(alpha = glowPulse * 0.70f),
                Color.Transparent
            ),
            center = weaponCenter,
            radius = weaponSize * 1.45f
        ),
        radius = weaponSize * 1.45f,
        center = weaponCenter
    )

    when (profile.weaponVisual) {
        HeldItemVisual.RADIANT_SHIELD -> {
            // Heavy Lionheart Heater Shield
            val sW = weaponSize * 1.3f
            val sH = weaponSize * 1.7f
            val shieldPath = Path().apply {
                moveTo(weaponCenter.x - sW * 0.5f, weaponCenter.y - sH * 0.5f)
                lineTo(weaponCenter.x + sW * 0.5f, weaponCenter.y - sH * 0.5f)
                lineTo(weaponCenter.x + sW * 0.45f, weaponCenter.y + sH * 0.15f)
                quadraticTo(weaponCenter.x, weaponCenter.y + sH * 0.55f, weaponCenter.x, weaponCenter.y + sH * 0.55f)
                quadraticTo(weaponCenter.x, weaponCenter.y + sH * 0.55f, weaponCenter.x - sW * 0.45f, weaponCenter.y + sH * 0.15f)
                close()
            }
            drawPath(
                path = shieldPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF38BDF8), profile.primaryColor, Color(0xFF1E3A8A)),
                    start = Offset(weaponCenter.x - sW * 0.5f, weaponCenter.y - sH * 0.5f),
                    end = Offset(weaponCenter.x + sW * 0.5f, weaponCenter.y + sH * 0.5f)
                )
            )
            drawPath(path = shieldPath, color = RadiantGold, style = Stroke(width = 3.5f * scale))
            // Cross emblem
            drawLine(
                color = Color.White,
                start = Offset(weaponCenter.x, weaponCenter.y - sH * 0.32f),
                end = Offset(weaponCenter.x, weaponCenter.y + sH * 0.32f),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(weaponCenter.x - sW * 0.30f, weaponCenter.y - sH * 0.05f),
                end = Offset(weaponCenter.x + sW * 0.30f, weaponCenter.y - sH * 0.05f),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
        }

        HeldItemVisual.SLAYER_BLADE, HeldItemVisual.FROST_SWORD -> {
            // Twin Serrated / Glacial Greatsword
            val bH = weaponSize * 2.2f
            val bW = weaponSize * 0.45f
            val swordPath = Path().apply {
                moveTo(weaponCenter.x, weaponCenter.y - bH * 0.6f) // tip
                lineTo(weaponCenter.x + bW * 0.5f, weaponCenter.y + bH * 0.25f)
                lineTo(weaponCenter.x - bW * 0.5f, weaponCenter.y + bH * 0.25f)
                close()
            }
            drawPath(
                path = swordPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, profile.accentGlowColor, profile.primaryColor),
                    start = Offset(weaponCenter.x, weaponCenter.y - bH * 0.6f),
                    end = Offset(weaponCenter.x, weaponCenter.y + bH * 0.25f)
                )
            )
            drawPath(path = swordPath, color = RadiantGold, style = Stroke(width = 1.8f * scale))
            // Crossguard & Hilt
            drawLine(
                color = RadiantGold,
                start = Offset(weaponCenter.x - bW * 1.1f, weaponCenter.y + bH * 0.25f),
                end = Offset(weaponCenter.x + bW * 1.1f, weaponCenter.y + bH * 0.25f),
                strokeWidth = 3.5f * scale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF451A03),
                start = Offset(weaponCenter.x, weaponCenter.y + bH * 0.25f),
                end = Offset(weaponCenter.x, weaponCenter.y + bH * 0.55f),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
        }

        HeldItemVisual.LADDER_STAFF -> {
            // Astral Prism Staff
            val stH = weaponSize * 2.4f
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(RadiantGold, Color(0xFF78350F), RadiantGold),
                    startY = weaponCenter.y - stH * 0.5f,
                    endY = weaponCenter.y + stH * 0.5f
                ),
                start = Offset(weaponCenter.x, weaponCenter.y - stH * 0.5f),
                end = Offset(weaponCenter.x, weaponCenter.y + stH * 0.5f),
                strokeWidth = 3.5f * scale,
                cap = StrokeCap.Round
            )
            // Prism Ring at top
            drawCircle(
                color = RadiantGold,
                radius = weaponSize * 0.45f,
                center = Offset(weaponCenter.x, weaponCenter.y - stH * 0.48f),
                style = Stroke(width = 2.5f * scale)
            )
            // Glowing Floating Crystal Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, profile.accentGlowColor, profile.primaryColor),
                    center = Offset(weaponCenter.x, weaponCenter.y - stH * 0.48f),
                    radius = weaponSize * 0.35f
                ),
                radius = weaponSize * 0.35f,
                center = Offset(weaponCenter.x, weaponCenter.y - stH * 0.48f)
            )
        }

        HeldItemVisual.GOLDEN_BOW -> {
            // Golden Solar Arbalest
            val bowR = weaponSize * 0.9f
            drawArc(
                color = RadiantGold,
                startAngle = 270f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(weaponCenter.x - bowR, weaponCenter.y - bowR),
                size = Size(bowR * 2, bowR * 2),
                style = Stroke(width = 3.8f * scale, cap = StrokeCap.Round)
            )
            // String & Arrow
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(weaponCenter.x, weaponCenter.y - bowR),
                end = Offset(weaponCenter.x, weaponCenter.y + bowR),
                strokeWidth = 1.2f * scale
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, profile.accentGlowColor),
                    start = Offset(weaponCenter.x + bowR * 0.7f, weaponCenter.y),
                    end = Offset(weaponCenter.x - bowR * 0.3f, weaponCenter.y)
                ),
                start = Offset(weaponCenter.x - bowR * 0.3f, weaponCenter.y),
                end = Offset(weaponCenter.x + bowR * 0.7f, weaponCenter.y),
                strokeWidth = 2.8f * scale,
                cap = StrokeCap.Round
            )
        }

        else -> {
            // Generic Glowing Relic / Orb / Gauntlet
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, profile.accentGlowColor, profile.primaryColor),
                    center = weaponCenter,
                    radius = weaponSize * 0.65f
                ),
                radius = weaponSize * 0.65f,
                center = weaponCenter
            )
            drawCircle(
                color = RadiantGold,
                radius = weaponSize * 0.65f,
                center = weaponCenter,
                style = Stroke(width = 2.5f * scale)
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// STANCE SPECIFIC EFFECTS
// -------------------------------------------------------------------------------------------------
private fun DrawScope.drawDefensiveBarrier(
    center: Offset,
    profile: HeroVisualProfile,
    progress: Float,
    quality: HeroRenderQuality
) {
    val bR = 95f * (center.x * 0.007f) * progress
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                profile.primaryColor.copy(alpha = 0.45f * progress),
                profile.accentGlowColor.copy(alpha = 0.25f * progress),
                Color.Transparent
            ),
            center = center,
            radius = bR * 1.3f
        ),
        radius = bR * 1.3f,
        center = center
    )
    drawCircle(
        color = profile.accentGlowColor,
        radius = bR,
        center = center,
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawAbilityRunesBurst(
    center: Offset,
    profile: HeroVisualProfile,
    progress: Float,
    rotationDeg: Float
) {
    val burstR = 120f * (center.x * 0.007f) * progress
    drawCircle(
        color = profile.accentGlowColor.copy(alpha = 0.65f * (1f - progress * 0.5f)),
        radius = burstR,
        center = center,
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawVictorySparkles(
    center: Offset,
    profile: HeroVisualProfile,
    pulse: Float
) {
    val count = 8
    val r = 105f * (center.x * 0.007f)
    for (i in 0 until count) {
        val angleRad = Math.toRadians((i * (360f / count) + pulse * 45f).toDouble())
        val sx = center.x + r * cos(angleRad).toFloat()
        val sy = center.y + r * sin(angleRad).toFloat()
        drawCircle(
            color = RadiantGold,
            radius = 4f * pulse,
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawAttackSlashArc(
    center: Offset,
    profile: HeroVisualProfile,
    progress: Float
) {
    val arcPath = Path().apply {
        val startX = center.x + 20f
        val startY = center.y - 70f
        moveTo(startX, startY)
        quadraticTo(
            center.x + 120f * progress,
            center.y,
            center.x + 30f,
            center.y + 60f
        )
    }
    drawPath(
        path = arcPath,
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.White, profile.accentGlowColor, Color.Transparent),
            startX = center.x,
            endX = center.x + 120f
        ),
        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
