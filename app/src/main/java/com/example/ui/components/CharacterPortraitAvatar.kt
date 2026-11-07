package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.CharacterGender
import com.example.model.GameCharacter
import com.example.model.HeldItemVisual
import com.example.ui.theme.RadiantGold

/**
 * Returns a brightened, high-contrast tint of [tokenColor] so player names and turn labels
 * remain crisp and legible on dark Black & Gold surfaces while preserving the exact token hue.
 */
fun readableTokenTextColor(tokenColor: Color): Color {
    return Color(
        red = (tokenColor.red * 0.68f + 0.32f).coerceIn(0f, 1f),
        green = (tokenColor.green * 0.68f + 0.32f).coerceIn(0f, 1f),
        blue = (tokenColor.blue * 0.68f + 0.32f).coerceIn(0f, 1f),
        alpha = 1f
    )
}

/**
 * Miniature 3D Pawn Token Icon matching the exact 3D board token (`drawThreeDimensionalPawnToken`)
 * and the player's real `player.color`. Used in the bottom smart status bar and turn panels.
 */
@Composable
fun PlayerPawnTokenIcon(
    tokenColor: Color,
    skin: com.example.model.PawnSkin? = null,
    isTurnActive: Boolean = true,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.52f)
        val r = (w * 0.38f).coerceAtLeast(2f)

        if (isTurnActive) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tokenColor.copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = r * 1.45f
                ),
                radius = r * 1.45f,
                center = center
            )
        }

        // Golden pedestal base (matches board pawn token)
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFF3B0), Color(0xFFB45309), RadiantGold),
                start = Offset(center.x - r, center.y),
                end = Offset(center.x + r, center.y)
            ),
            topLeft = Offset(center.x - r * 0.92f, center.y + r * 0.22f),
            size = Size(r * 1.84f, r * 0.66f)
        )

        val bodyCenter = Offset(center.x, center.y - r * 0.12f)

        if (skin != null) {
            drawPawnSkinGeometry(
                center = bodyCenter,
                radius = r * 0.95f,
                skin = skin,
                playerColor = tokenColor,
                rotationDeg = 0f,
                energyPulse = 1f
            )
        } else {
            // 3D Spherical Pawn Body in exact player.color
            val darkShade = Color(
                red = (tokenColor.red * 0.36f).coerceIn(0f, 1f),
                green = (tokenColor.green * 0.36f).coerceIn(0f, 1f),
                blue = (tokenColor.blue * 0.36f).coerceIn(0f, 1f),
                alpha = 1f
            )
            val brightHighlight = Color(
                red = (tokenColor.red + (1f - tokenColor.red) * 0.65f).coerceIn(0f, 1f),
                green = (tokenColor.green + (1f - tokenColor.green) * 0.65f).coerceIn(0f, 1f),
                blue = (tokenColor.blue + (1f - tokenColor.blue) * 0.65f).coerceIn(0f, 1f),
                alpha = 1f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(brightHighlight, tokenColor, darkShade),
                    center = Offset(bodyCenter.x - r * 0.30f, bodyCenter.y - r * 0.32f),
                    radius = r * 1.32f
                ),
                radius = r * 0.88f,
                center = bodyCenter
            )

            drawCircle(
                color = if (isTurnActive) RadiantGold else RadiantGold.copy(alpha = 0.55f),
                radius = r * 0.88f,
                center = bodyCenter,
                style = Stroke(width = (w * 0.065f).coerceAtLeast(1f))
            )

            // Specular highlight dot
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = r * 0.26f,
                center = Offset(bodyCenter.x - r * 0.26f, bodyCenter.y - r * 0.28f)
            )
        }
    }
}

/**
 * Custom 3D-shaded Character Portrait & Signature Ability Prop Renderer.
 * Draws the character's stylized head, hair, expressive eyes, cape/armor,
 * and visibly renders their signature ability item in hand (e.g., Radiant Shield,
 * Golden Bow, Slayer Blade, Ladder Staff, Shadow Dagger, Jet Boots, etc.).
 */
@Composable
fun CharacterPortraitAvatar(
    character: GameCharacter,
    size: Dp = 56.dp,
    isSelected: Boolean = false,
    tokenColorOverride: Color? = null,
    modifier: Modifier = Modifier
) {
    val activeAccent = tokenColorOverride ?: character.primaryColor
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        activeAccent.copy(alpha = if (isSelected) 0.62f else 0.45f),
                        character.secondaryColor.copy(alpha = 0.90f),
                        Color(0xFF140B24)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.2.dp else 1.4.dp,
                brush = if (isSelected && tokenColorOverride != null) {
                    Brush.linearGradient(
                        colors = listOf(tokenColorOverride, RadiantGold, tokenColorOverride)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            if (isSelected) RadiantGold else activeAccent.copy(alpha = 0.75f),
                            if (isSelected) activeAccent else activeAccent.copy(alpha = 0.55f)
                        )
                    )
                },
                shape = CircleShape
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.46f
            val cy = h * 0.48f

            // 1. Background Magical Aura Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeAccent.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = w * 0.46f
                ),
                radius = w * 0.46f,
                center = Offset(cx, cy)
            )

            // 2. Back Hair (for long/flowing hair styles)
            if (character.gender == CharacterGender.FEMALE) {
                drawOval(
                    color = character.hairColor,
                    topLeft = Offset(cx - w * 0.28f, cy - h * 0.24f),
                    size = Size(w * 0.56f, h * 0.58f)
                )
            }

            // 3. 3D Shoulders / Armor / Cape Torso
            val torsoPath = Path().apply {
                moveTo(cx - w * 0.32f, h * 0.95f)
                quadraticTo(cx - w * 0.28f, cy + h * 0.14f, cx, cy + h * 0.14f)
                quadraticTo(cx + w * 0.28f, cy + h * 0.14f, cx + w * 0.32f, h * 0.95f)
                close()
            }
            drawPath(
                path = torsoPath,
                brush = Brush.linearGradient(
                    colors = listOf(character.primaryColor, character.secondaryColor),
                    start = Offset(cx - w * 0.2f, cy + h * 0.14f),
                    end = Offset(cx + w * 0.2f, h)
                )
            )
            // Golden armor collar trim
            drawArc(
                color = RadiantGold,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.16f, cy + h * 0.08f),
                size = Size(w * 0.32f, h * 0.16f),
                style = Stroke(width = w * 0.032f, cap = StrokeCap.Round)
            )

            // 4. Sculpted 3D Head & Face
            val headR = w * 0.22f
            val headCenter = Offset(cx, cy - h * 0.04f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        character.skinTone,
                        character.skinTone.copy(alpha = 0.82f)
                    ),
                    center = Offset(headCenter.x - headR * 0.25f, headCenter.y - headR * 0.25f),
                    radius = headR * 1.35f
                ),
                radius = headR,
                center = headCenter
            )

            // 5. Expressive Eyes & Confident Smile
            val eyeY = headCenter.y + headR * 0.05f
            val leftEyeX = headCenter.x - headR * 0.36f
            val rightEyeX = headCenter.x + headR * 0.36f
            val eyeR = headR * 0.16f

            drawCircle(Color.White, radius = eyeR * 1.2f, center = Offset(leftEyeX, eyeY))
            drawCircle(Color.White, radius = eyeR * 1.2f, center = Offset(rightEyeX, eyeY))
            drawCircle(character.secondaryColor, radius = eyeR * 0.85f, center = Offset(leftEyeX, eyeY))
            drawCircle(character.secondaryColor, radius = eyeR * 0.85f, center = Offset(rightEyeX, eyeY))
            drawCircle(Color.Black, radius = eyeR * 0.45f, center = Offset(leftEyeX, eyeY))
            drawCircle(Color.Black, radius = eyeR * 0.45f, center = Offset(rightEyeX, eyeY))
            drawCircle(Color.White, radius = eyeR * 0.25f, center = Offset(leftEyeX - 1.5f, eyeY - 1.5f))
            drawCircle(Color.White, radius = eyeR * 0.25f, center = Offset(rightEyeX - 1.5f, eyeY - 1.5f))

            // Smile
            drawArc(
                color = Color(0xFF7C2D12),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(headCenter.x - headR * 0.25f, headCenter.y + headR * 0.22f),
                size = Size(headR * 0.50f, headR * 0.28f),
                style = Stroke(width = w * 0.024f, cap = StrokeCap.Round)
            )

            // 6. Stylized Front Hair / Crown / Helmet Crest
            val hairTopPath = Path().apply {
                moveTo(headCenter.x - headR * 1.05f, headCenter.y - headR * 0.1f)
                quadraticTo(
                    headCenter.x,
                    headCenter.y - headR * 1.45f,
                    headCenter.x + headR * 1.05f,
                    headCenter.y - headR * 0.1f
                )
                quadraticTo(
                    headCenter.x + headR * 0.3f,
                    headCenter.y - headR * 0.55f,
                    headCenter.x - headR * 1.05f,
                    headCenter.y - headR * 0.1f
                )
                close()
            }
            drawPath(path = hairTopPath, color = character.hairColor)

            // 7. Signature Ability Prop Held Visibly in Foreground (Right Side of Portrait)
            drawHeldAbilityProp(
                heldItem = character.heldItem,
                primaryColor = character.primaryColor,
                propCenter = Offset(w * 0.74f, h * 0.64f),
                propSize = w * 0.26f
            )
        }
    }
}

private fun DrawScope.drawHeldAbilityProp(
    heldItem: HeldItemVisual,
    primaryColor: Color,
    propCenter: Offset,
    propSize: Float
) {
    // Glowing halo behind the held weapon/item
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(RadiantGold.copy(alpha = 0.65f), Color.Transparent),
            center = propCenter,
            radius = propSize * 0.95f
        ),
        radius = propSize * 0.95f,
        center = propCenter
    )

    when (heldItem) {
        HeldItemVisual.RADIANT_SHIELD -> {
            // 3D Heater Shield in hand
            val shieldPath = Path().apply {
                moveTo(propCenter.x - propSize * 0.55f, propCenter.y - propSize * 0.55f)
                lineTo(propCenter.x + propSize * 0.55f, propCenter.y - propSize * 0.55f)
                lineTo(propCenter.x + propSize * 0.50f, propCenter.y + propSize * 0.05f)
                quadraticTo(propCenter.x, propCenter.y + propSize * 0.72f, propCenter.x, propCenter.y + propSize * 0.72f)
                quadraticTo(propCenter.x, propCenter.y + propSize * 0.72f, propCenter.x - propSize * 0.50f, propCenter.y + propSize * 0.05f)
                close()
            }
            drawPath(
                path = shieldPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF1E3A8A)),
                    start = Offset(propCenter.x - propSize * 0.5f, propCenter.y - propSize * 0.5f),
                    end = Offset(propCenter.x + propSize * 0.5f, propCenter.y + propSize * 0.6f)
                )
            )
            drawPath(
                path = shieldPath,
                color = RadiantGold,
                style = Stroke(width = propSize * 0.14f, join = StrokeJoin.Round)
            )
            // Cross emblem on shield
            drawLine(
                color = Color.White,
                start = Offset(propCenter.x, propCenter.y - propSize * 0.32f),
                end = Offset(propCenter.x, propCenter.y + propSize * 0.35f),
                strokeWidth = propSize * 0.12f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(propCenter.x - propSize * 0.28f, propCenter.y - propSize * 0.05f),
                end = Offset(propCenter.x + propSize * 0.28f, propCenter.y - propSize * 0.05f),
                strokeWidth = propSize * 0.12f,
                cap = StrokeCap.Round
            )
        }

        HeldItemVisual.LADDER_STAFF -> {
            // Golden Ladder-Weaver Staff
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(propCenter.x - propSize * 0.2f, propCenter.y + propSize * 0.65f),
                end = Offset(propCenter.x + propSize * 0.2f, propCenter.y - propSize * 0.55f),
                strokeWidth = propSize * 0.16f,
                cap = StrokeCap.Round
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFE879F9), Color(0xFF9333EA)),
                    center = Offset(propCenter.x + propSize * 0.2f, propCenter.y - propSize * 0.55f),
                    radius = propSize * 0.38f
                ),
                radius = propSize * 0.35f,
                center = Offset(propCenter.x + propSize * 0.2f, propCenter.y - propSize * 0.55f)
            )
        }

        HeldItemVisual.SLAYER_BLADE, HeldItemVisual.FROST_SWORD -> {
            val bladeColor = if (heldItem == HeldItemVisual.FROST_SWORD) Color(0xFF93C5FD) else Color(0xFF6EE7B7)
            // Glowing Sword Blade
            drawLine(
                color = bladeColor,
                start = Offset(propCenter.x - propSize * 0.15f, propCenter.y + propSize * 0.35f),
                end = Offset(propCenter.x + propSize * 0.35f, propCenter.y - propSize * 0.68f),
                strokeWidth = propSize * 0.18f,
                cap = StrokeCap.Round
            )
            // Golden Hilt Guard
            drawLine(
                color = RadiantGold,
                start = Offset(propCenter.x - propSize * 0.42f, propCenter.y + propSize * 0.22f),
                end = Offset(propCenter.x + propSize * 0.15f, propCenter.y + propSize * 0.48f),
                strokeWidth = propSize * 0.16f,
                cap = StrokeCap.Round
            )
        }

        HeldItemVisual.GOLDEN_BOW -> {
            drawArc(
                color = RadiantGold,
                startAngle = -75f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(propCenter.x - propSize * 0.5f, propCenter.y - propSize * 0.6f),
                size = Size(propSize, propSize * 1.2f),
                style = Stroke(width = propSize * 0.16f, cap = StrokeCap.Round)
            )
            drawLine(
                color = Color.White,
                start = Offset(propCenter.x - propSize * 0.35f, propCenter.y),
                end = Offset(propCenter.x + propSize * 0.55f, propCenter.y),
                strokeWidth = propSize * 0.12f,
                cap = StrokeCap.Round
            )
        }

        HeldItemVisual.THUNDER_HAMMER -> {
            // Handle
            drawLine(
                color = Color(0xFF92400E),
                start = Offset(propCenter.x, propCenter.y + propSize * 0.65f),
                end = Offset(propCenter.x, propCenter.y - propSize * 0.25f),
                strokeWidth = propSize * 0.16f,
                cap = StrokeCap.Round
            )
            // Hammer Head
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(propCenter.x - propSize * 0.48f, propCenter.y - propSize * 0.58f),
                size = Size(propSize * 0.96f, propSize * 0.48f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        else -> {
            // Glowing Elemental Orb / Vial / Crest
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primaryColor, RadiantGold),
                    center = Offset(propCenter.x - propSize * 0.15f, propCenter.y - propSize * 0.15f),
                    radius = propSize * 0.55f
                ),
                radius = propSize * 0.48f,
                center = propCenter
            )
            drawCircle(
                color = RadiantGold,
                radius = propSize * 0.48f,
                center = propCenter,
                style = Stroke(width = propSize * 0.10f)
            )
        }
    }
}
