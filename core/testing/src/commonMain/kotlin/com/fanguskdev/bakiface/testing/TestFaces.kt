package com.fanguskdev.bakiface.testing

import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.picture.Picture
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.assertTrue

/** A picture with a size and nothing else: all the domain ever looks at. */
data class FakePicture(override val width: Int, override val height: Int) : Picture

/**
 * A face drawn with the usual proportions, centred between the eyes at [center], [eyeDistance] pixels between the
 * eyes, turned [angleDegrees] clockwise, with the mouth [mouthOpen] from 0 to 1. With [swapLabels], the detector calls
 * the left eye right (as a mirrored picture would).
 */
fun syntheticFace(
    center: Point = Point(360f, 640f),
    eyeDistance: Float = 120f,
    angleDegrees: Float = 0f,
    mouthOpen: Float = 0f,
    swapLabels: Boolean = false,
): FaceLandmarks {
    val radians = angleDegrees * PI.toFloat() / 180f
    val cosine = cos(radians)
    val sine = sin(radians)
    fun place(x: Float, y: Float): Point = Point(
        center.x + (x * cosine - y * sine) * eyeDistance,
        center.y + (x * sine + y * cosine) * eyeDistance,
    )
    fun ring(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        count: Int,
    ): ImmutableList<Point> = List(count) { index ->
        val angle = 2f * PI.toFloat() * index / count
        place(cx + rx * cos(angle), cy + ry * sin(angle))
    }.toImmutableList()
    fun line(vararg points: Pair<Float, Float>): ImmutableList<Point> = points.map { (x, y) -> place(x, y) }.toImmutableList()

    val leftEye = ring(-0.5f, 0f, 0.2f, 0.1f, 16)
    val rightEye = ring(0.5f, 0f, 0.2f, 0.1f, 16)
    val leftBrow = line(-0.85f to -0.3f, -0.7f to -0.38f, -0.5f to -0.4f, -0.3f to -0.37f, -0.18f to -0.32f)
    val rightBrow = line(0.18f to -0.32f, 0.3f to -0.37f, 0.5f to -0.4f, 0.7f to -0.38f, 0.85f to -0.3f)
    val gap = 0.02f + 0.4f * mouthOpen
    return FaceLandmarks(
        faceContour = ring(0f, 0.35f, 1.05f, 1.5f, 36),
        leftEyebrow = if (swapLabels) rightBrow else leftBrow,
        rightEyebrow = if (swapLabels) leftBrow else rightBrow,
        leftEye = if (swapLabels) rightEye else leftEye,
        rightEye = if (swapLabels) leftEye else rightEye,
        noseBridge = line(0f to 0.1f, 0f to 0.75f),
        noseBottom = line(-0.22f to 0.82f, 0f to 0.86f, 0.22f to 0.82f),
        outerLips = ring(0f, 1.2f + gap / 2f, 0.42f, 0.12f + gap / 2f, 20),
        innerLips = line(-0.35f to 1.2f, 0f to 1.2f, 0.35f to 1.2f, 0.35f to 1.2f + gap, 0f to 1.2f + gap, -0.35f to 1.2f + gap),
    )
}

/** Fails unless [actual] is within [tolerance] of [expected]. */
fun assertNear(
    expected: Float,
    actual: Float,
    tolerance: Float = 0.01f,
    message: String = "",
) {
    assertTrue(abs(expected - actual) <= tolerance, "$message expected $expected ± $tolerance, was $actual")
}

/** Fails unless the two points are within [tolerance] of each other on both axes. */
fun assertNear(
    expected: Point,
    actual: Point,
    tolerance: Float = 0.01f,
    message: String = "",
) {
    assertNear(expected.x, actual.x, tolerance, "$message (x)")
    assertNear(expected.y, actual.y, tolerance, "$message (y)")
}
