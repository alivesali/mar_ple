package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class DiceVec3(val x: Float, val y: Float, val z: Float)

data class CubeFaceDef(
    val faceValue: Int,
    val cornerIndices: IntArray,
    val normal: DiceVec3,
    val uAxis: DiceVec3,
    val vAxis: DiceVec3,
    val center: DiceVec3
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CubeFaceDef) return false
        return faceValue == other.faceValue
    }

    override fun hashCode(): Int = faceValue
}

data class DiceStageGeometry(
    val widthPx: Float,
    val heightPx: Float,
    val minDim: Float,
    val cameraDist: Float,
    val cubeScale: Float,
    val centerX: Float,
    val centerY: Float,
    val outerStrokeWidth: Float
)

data class DiceTrajectorySample(
    val rotXDeg: Float,
    val rotYDeg: Float,
    val rotZDeg: Float,
    val airLift: Float,
    val scaleX: Float,
    val scaleY: Float
)

data class StepSequenceFrame(
    val segmentIndex: Int,
    val localProgress: Float,
    val easedProgress: Float,
    val completedStepsCount: Int,
    val hopHeight: Float,
    val scale: Float,
    val squashX: Float,
    val squashY: Float,
    val isFinished: Boolean
)

/**
 * Shared 3D geometry & smooth physics helpers for the rounded 3D die and multi-step pawn walk.
 * Kept pure-Kotlin so both Jetpack Compose rendering and deterministic JVM unit tests execute the exact same math.
 */
object Phase3DiceMath {

    const val CAMERA_DIST: Float = 4.8f
    const val OUTER_HALF_EXTENT: Float = 0.94f
    const val OUTER_CORNER_RADIUS: Float = 0.48f
    const val OUTER_SPHERIZE_AMOUNT: Float = 0.28f
    const val SPHERE_RADIUS: Float = 1.16f

    val cubeFaces: List<CubeFaceDef> = listOf(
        CubeFaceDef(
            faceValue = 1,
            cornerIndices = intArrayOf(0, 1, 2, 3),
            normal = DiceVec3(0f, 0f, 1f),
            uAxis = DiceVec3(1f, 0f, 0f),
            vAxis = DiceVec3(0f, 1f, 0f),
            center = DiceVec3(0f, 0f, 1f)
        ),
        CubeFaceDef(
            faceValue = 2,
            cornerIndices = intArrayOf(5, 4, 7, 6),
            normal = DiceVec3(0f, 0f, -1f),
            uAxis = DiceVec3(-1f, 0f, 0f),
            vAxis = DiceVec3(0f, 1f, 0f),
            center = DiceVec3(0f, 0f, -1f)
        ),
        CubeFaceDef(
            faceValue = 3,
            cornerIndices = intArrayOf(3, 2, 6, 7),
            normal = DiceVec3(0f, 1f, 0f),
            uAxis = DiceVec3(1f, 0f, 0f),
            vAxis = DiceVec3(0f, 0f, -1f),
            center = DiceVec3(0f, 1f, 0f)
        ),
        CubeFaceDef(
            faceValue = 4,
            cornerIndices = intArrayOf(4, 5, 1, 0),
            normal = DiceVec3(0f, -1f, 0f),
            uAxis = DiceVec3(1f, 0f, 0f),
            vAxis = DiceVec3(0f, 0f, 1f),
            center = DiceVec3(0f, -1f, 0f)
        ),
        CubeFaceDef(
            faceValue = 5,
            cornerIndices = intArrayOf(1, 5, 6, 2),
            normal = DiceVec3(1f, 0f, 0f),
            uAxis = DiceVec3(0f, 0f, -1f),
            vAxis = DiceVec3(0f, 1f, 0f),
            center = DiceVec3(1f, 0f, 0f)
        ),
        CubeFaceDef(
            faceValue = 6,
            cornerIndices = intArrayOf(4, 0, 3, 7),
            normal = DiceVec3(-1f, 0f, 0f),
            uAxis = DiceVec3(0f, 0f, 1f),
            vAxis = DiceVec3(0f, 1f, 0f),
            center = DiceVec3(-1f, 0f, 0f)
        )
    )

    /**
     * Calibrated resting Euler angles for faces 1..6 such that:
     * 1) The landed face dominates the camera view (`rotNormal.z >= 0.80f`)
     * 2) A distinct upper surface (`center.y` above cube center AND `rotNormal.z > 0.25f`) stays clearly visible!
     */
    fun targetAnglesForFace(faceValue: Int): Triple<Float, Float, Float> {
        return when (faceValue.coerceIn(1, 6)) {
            1 -> Triple(28f, 20f, 0f)
            2 -> Triple(-28f, -160f, 0f)
            3 -> Triple(118f, 20f, 0f)
            4 -> Triple(-62f, -20f, 0f)
            5 -> Triple(36f, -62f, 0f)
            else -> Triple(36f, 62f, 0f)
        }
    }

    fun computeStageGeometry(
        widthPx: Float,
        heightPx: Float,
        airLift: Float
    ): DiceStageGeometry {
        val clampedLift = airLift.coerceIn(0f, 1.05f)
        val minDim = minOf(widthPx, heightPx)
        val cubeScale = minDim * (0.225f + clampedLift * 0.014f)
        val centerX = widthPx * 0.50f
        val centerY = heightPx * 0.545f - clampedLift * (heightPx * 0.105f)
        return DiceStageGeometry(
            widthPx = widthPx,
            heightPx = heightPx,
            minDim = minDim,
            cameraDist = CAMERA_DIST,
            cubeScale = cubeScale,
            centerX = centerX,
            centerY = centerY,
            outerStrokeWidth = minDim * 0.042f
        )
    }

    fun rotate3D(
        v: DiceVec3,
        rotXDeg: Float,
        rotYDeg: Float,
        rotZDeg: Float
    ): DiceVec3 {
        val rx = Math.toRadians(rotXDeg.toDouble()).toFloat()
        val ry = Math.toRadians(rotYDeg.toDouble()).toFloat()
        val rz = Math.toRadians(rotZDeg.toDouble()).toFloat()
        val cosX = cos(rx)
        val sinX = sin(rx)
        val cosY = cos(ry)
        val sinY = sin(ry)
        val cosZ = cos(rz)
        val sinZ = sin(rz)

        val y1 = v.y * cosX - v.z * sinX
        val z1 = v.y * sinX + v.z * cosX
        val x1 = v.x

        val x2 = x1 * cosY + z1 * sinY
        val z2 = -x1 * sinY + z1 * cosY
        val y2 = y1

        val x3 = x2 * cosZ - y2 * sinZ
        val y3 = x2 * sinZ + y2 * cosZ
        val z3 = z2

        return DiceVec3(x3, y3, z3)
    }

    fun projectToScreen(
        v: DiceVec3,
        geometry: DiceStageGeometry
    ): Offset {
        val perspective = geometry.cameraDist / (geometry.cameraDist - v.z).coerceAtLeast(1.3f)
        return Offset(
            x = geometry.centerX + v.x * geometry.cubeScale * perspective,
            y = geometry.centerY - v.y * geometry.cubeScale * perspective
        )
    }

    fun visibleFacesForAngles(
        rotXDeg: Float,
        rotYDeg: Float,
        rotZDeg: Float,
        cameraDist: Float = CAMERA_DIST
    ): List<Triple<CubeFaceDef, DiceVec3, DiceVec3>> {
        return cubeFaces.mapNotNull { face ->
            val rotNormal = rotate3D(face.normal, rotXDeg, rotYDeg, rotZDeg)
            val rotCenter = rotate3D(face.center, rotXDeg, rotYDeg, rotZDeg)
            val viewVecZ = cameraDist - rotCenter.z
            val dotView = rotNormal.x * (-rotCenter.x) +
                rotNormal.y * (-rotCenter.y) +
                rotNormal.z * viewVecZ
            if (dotView <= 0.02f) {
                null
            } else {
                Triple(face, rotNormal, rotCenter)
            }
        }.sortedBy { it.third.z }
    }

    fun sampleFaceVertex3D(
        face: CubeFaceDef,
        u: Float,
        v: Float,
        normalOffset: Float,
        spherizeAmount: Float,
        sphereRadius: Float = SPHERE_RADIUS
    ): DiceVec3 {
        val rawX = face.center.x * (1f + normalOffset) + face.uAxis.x * u + face.vAxis.x * v
        val rawY = face.center.y * (1f + normalOffset) + face.uAxis.y * u + face.vAxis.y * v
        val rawZ = face.center.z * (1f + normalOffset) + face.uAxis.z * u + face.vAxis.z * v

        val len = sqrt(rawX * rawX + rawY * rawY + rawZ * rawZ).coerceAtLeast(0.001f)
        val sx = (rawX / len) * sphereRadius
        val sy = (rawY / len) * sphereRadius
        val sz = (rawZ / len) * sphereRadius

        return DiceVec3(
            x = rawX * (1f - spherizeAmount) + sx * spherizeAmount,
            y = rawY * (1f - spherizeAmount) + sy * spherizeAmount,
            z = rawZ * (1f - spherizeAmount) + sz * spherizeAmount
        )
    }

    fun sampleRoundedFaceOutlineScreenPoints(
        face: CubeFaceDef,
        halfExtent: Float,
        cornerRadius: Float,
        spherizeAmount: Float,
        normalOffset: Float,
        stepsPerCorner: Int,
        rotXDeg: Float,
        rotYDeg: Float,
        rotZDeg: Float,
        geometry: DiceStageGeometry
    ): List<Offset> {
        val r = cornerRadius.coerceIn(0.05f, halfExtent * 0.92f)
        val inner = halfExtent - r
        val cornerCenters = arrayOf(
            Pair(inner, inner),
            Pair(-inner, inner),
            Pair(-inner, -inner),
            Pair(inner, -inner)
        )
        val safeSteps = stepsPerCorner.coerceIn(3, 8)
        val points = ArrayList<Offset>(4 * (safeSteps + 1))

        for (cIdx in 0 until 4) {
            val (cu, cv) = cornerCenters[cIdx]
            val startAngle = cIdx * (PI.toFloat() * 0.5f)
            for (step in 0..safeSteps) {
                val theta = startAngle + (step.toFloat() / safeSteps.toFloat()) * (PI.toFloat() * 0.5f)
                val u = cu + r * cos(theta)
                val v = cv + r * sin(theta)
                val p3d = sampleFaceVertex3D(
                    face = face,
                    u = u,
                    v = v,
                    normalOffset = normalOffset,
                    spherizeAmount = spherizeAmount
                )
                points.add(projectToScreen(rotate3D(p3d, rotXDeg, rotYDeg, rotZDeg), geometry))
            }
        }
        return points
    }

    fun smoothLandingTargetAngle(currentDeg: Float, baseTargetDeg: Float): Float {
        var normalized = (currentDeg - baseTargetDeg) % 360f
        if (normalized < 0f) normalized += 360f
        val nearestBase = currentDeg - normalized
        return if (normalized > 180f) nearestBase + 360f else nearestBase
    }

    fun evaluateDiceTrajectoryFrame(
        progress: Float,
        flightFraction: Float,
        startAngles: Triple<Float, Float, Float>,
        endAngles: Triple<Float, Float, Float>,
        targetAngles: Triple<Float, Float, Float>
    ): DiceTrajectorySample {
        val p = progress.coerceIn(0f, 1f)
        val clampedFlight = flightFraction.coerceIn(0.4f, 0.95f)

        return if (p < clampedFlight) {
            val u = (p / clampedFlight).coerceIn(0f, 1f)
            val parabola = 4f * u * (1f - u)
            val launchSkew = 1f - 0.14f * u
            val airLift = (parabola * launchSkew).coerceIn(0f, 1f)

            val easeX = 1f - (1f - u).pow(2.35f)
            val easeY = 1f - (1f - u).pow(2.55f)
            val easeZ = 1f - (1f - u).pow(2.15f)

            val launchStretch = sin(u * PI.toFloat()) * 0.030f
            DiceTrajectorySample(
                rotXDeg = startAngles.first + (endAngles.first - startAngles.first) * easeX,
                rotYDeg = startAngles.second + (endAngles.second - startAngles.second) * easeY,
                rotZDeg = startAngles.third + (endAngles.third - startAngles.third) * easeZ,
                airLift = airLift,
                scaleX = 1f - launchStretch * 0.5f,
                scaleY = 1f + launchStretch
            )
        } else {
            val v = ((p - clampedFlight) / (1f - clampedFlight).coerceAtLeast(0.01f)).coerceIn(0f, 1f)
            val bounceHeight = if (v < 0.58f) {
                val b1 = v / 0.58f
                0.20f * 4f * b1 * (1f - b1)
            } else {
                val b2 = (v - 0.58f) / 0.42f
                0.06f * 4f * b2 * (1f - b2)
            }

            val damping = (1f - v) * (1f - v)
            val wobbleWave = sin(v * 2.5f * PI.toFloat()) * damping
            val squashPulse = cos(v * 2.2f * PI.toFloat()).coerceIn(0f, 1f) * damping * 0.048f

            DiceTrajectorySample(
                rotXDeg = targetAngles.first + wobbleWave * 6.0f,
                rotYDeg = targetAngles.second - wobbleWave * 4.5f,
                rotZDeg = targetAngles.third + wobbleWave * 3.0f,
                airLift = bounceHeight,
                scaleX = 1f + squashPulse,
                scaleY = 1f - squashPulse * 0.85f
            )
        }
    }
}

object Phase3BoardMovementMath {

    /**
     * Evaluates a multi-tile step sequence on a single continuous monotonic timeline (`0..totalSteps`),
     * eliminating per-tile frame resets and overshoot stalls.
     */
    fun evaluateStepSequenceFrame(
        elapsedNs: Long,
        stepDurationNs: Long,
        totalSteps: Int,
        isReverse: Boolean
    ): StepSequenceFrame {
        if (totalSteps <= 0 || stepDurationNs <= 0L) {
            return StepSequenceFrame(
                segmentIndex = 0,
                localProgress = 1f,
                easedProgress = 1f,
                completedStepsCount = totalSteps.coerceAtLeast(0),
                hopHeight = 0f,
                scale = 1f,
                squashX = 1f,
                squashY = 1f,
                isFinished = true
            )
        }

        val clampedElapsed = elapsedNs.coerceAtLeast(0L)
        val totalDurationNs = stepDurationNs * totalSteps
        val continuousStep = (clampedElapsed.toDouble() / stepDurationNs.toDouble())
            .coerceIn(0.0, totalSteps.toDouble())

        val isFinished = clampedElapsed >= totalDurationNs
        val segIndex = if (isFinished) {
            totalSteps - 1
        } else {
            floor(continuousStep).toInt().coerceIn(0, totalSteps - 1)
        }
        val localP = if (isFinished) {
            1f
        } else {
            (continuousStep - segIndex).toFloat().coerceIn(0f, 1f)
        }
        val completedSteps = if (isFinished) {
            totalSteps
        } else {
            floor(continuousStep + 1e-6).toInt().coerceIn(0, totalSteps)
        }

        val easeP = localP * localP * (3f - 2f * localP)
        val hopArc = sin(localP * PI.toFloat())

        if (isReverse) {
            return StepSequenceFrame(
                segmentIndex = segIndex,
                localProgress = localP,
                easedProgress = easeP,
                completedStepsCount = completedSteps,
                hopHeight = hopArc * 0.55f,
                scale = 1f - 0.10f * hopArc,
                squashX = 1f + 0.06f * hopArc,
                squashY = 1f - 0.06f * hopArc,
                isFinished = isFinished
            )
        }

        val isFirstStep = (segIndex == 0)
        val isLastStep = (segIndex == totalSteps - 1)
        val takeoffBoost = if (isFirstStep) hopArc * 0.12f else 0f
        val hopHeight = (hopArc * 0.86f + takeoffBoost).coerceIn(0f, 1.12f)
        val scale = 1f + hopHeight * 0.22f

        val (squashX, squashY) = when {
            localP < 0.15f && isFirstStep -> {
                val t = localP / 0.15f
                (1f + 0.07f * (1f - t)) to (1f - 0.06f * (1f - t))
            }
            localP in 0.20f..0.80f -> {
                val stretch = sin(((localP - 0.20f) / 0.60f) * PI.toFloat()) * 0.060f
                (1f - stretch * 0.65f) to (1f + stretch)
            }
            localP > 0.82f && !isLastStep -> {
                val c = (localP - 0.82f) / 0.18f
                val cushion = sin(c * PI.toFloat()) * 0.065f
                (1f + cushion) to (1f - cushion * 0.82f)
            }
            else -> 1f to 1f
        }

        return StepSequenceFrame(
            segmentIndex = segIndex,
            localProgress = localP,
            easedProgress = easeP,
            completedStepsCount = completedSteps,
            hopHeight = hopHeight,
            scale = scale,
            squashX = squashX,
            squashY = squashY,
            isFinished = isFinished
        )
    }
}
