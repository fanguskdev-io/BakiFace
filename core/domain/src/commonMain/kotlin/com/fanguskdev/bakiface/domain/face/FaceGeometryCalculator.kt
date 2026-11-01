package com.fanguskdev.bakiface.domain.face

import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.centroid
import com.fanguskdev.bakiface.domain.geometry.lerp
import kotlin.math.abs

/** Below this distance between the eyes, in pixels, a face is too small to change. */
internal const val MIN_EYE_DISTANCE_PX = 12f

// The usual proportions of a face, in units of the distance between the eyes: what stands in for a region the
// detector did not give (Vision, for example, has no forehead in its contour).
private const val DEFAULT_BROW_RISE = 0.35f
private const val DEFAULT_BROW_HALF = 0.3f
private const val DEFAULT_EYE_HALF_WIDTH = 0.2f
private const val DEFAULT_EYE_HALF_HEIGHT = 0.1f
private const val MIN_EYE_HALF_HEIGHT = 0.02f
private const val DEFAULT_NOSE_TIP_Y = 0.75f
private const val DEFAULT_NOSE_HALF_WIDTH = 0.25f
private const val NOSE_BASE_BAND = 0.12f
private const val DEFAULT_MOUTH_Y = 1.2f
private const val DEFAULT_MOUTH_HALF_WIDTH = 0.42f
private const val DEFAULT_LIP_HALF_HEIGHT = 0.1f
private const val LIP_MIDDLE_BAND = 0.18f
private const val INNER_LIP_MIDDLE_BAND = 0.15f
private const val MOUTH_CLOSED_GAP = 0.05f
private const val MOUTH_OPEN_GAP = 0.35f
private const val DEFAULT_CHIN_BELOW_MOUTH = 0.62f
private const val DEFAULT_FACE_HALF_WIDTH = 1.05f
private const val CONTOUR_SIDE_MARGIN = 0.05f
private const val TEMPLE_INSET = 0.94f
private const val CHEEKBONE_SHARE = 0.45f
private const val GONION_SHARE = 0.35f
private const val NECK_INSET = 0.78f
private const val NECK_BELOW_CHIN = 0.55f
private const val FOREHEAD_ABOVE_BROWS = 0.5f

/**
 * The geometry of a face from what a detector found (ADR 0005), or `null` when there is not enough to place it: no
 * eyes, or eyes closer than [MIN_EYE_DISTANCE_PX]. Which eye is which is decided here from where each one is, with
 * the mouth (or the nose) marking "down", so the detector's labels and a mirrored picture do not matter.
 */
fun faceGeometryOf(landmarks: FaceLandmarks): FaceGeometry? {
    val eyeA = landmarks.leftEye.centroid() ?: return null
    val eyeB = landmarks.rightEye.centroid() ?: return null
    if ((eyeB - eyeA).length < MIN_EYE_DISTANCE_PX) return null
    val middle = (eyeA + eyeB) * 0.5f
    val below = landmarks.outerLips.centroid() ?: landmarks.noseBottom.centroid()
    val down = below?.let { (it - middle).normalized() }?.takeIf { it != Point.ZERO } ?: Point(0f, 1f)
    val rightward = Point(down.y, -down.x)
    val labelsMatch = ((eyeA - middle) dot rightward) <= 0f
    val leftCenter = if (labelsMatch) eyeA else eyeB
    val rightCenter = if (labelsMatch) eyeB else eyeA
    val axes = FaceAxes(
        origin = middle,
        xAxis = (rightCenter - leftCenter).normalized(),
        unit = (rightCenter - leftCenter).length,
    )
    return LocalFace(landmarks, axes, labelsMatch).geometry()
}

/** The landmarks moved into the face's frame, and the rules that turn them into a [FaceGeometry]. */
private class LocalFace(landmarks: FaceLandmarks, private val axes: FaceAxes, labelsMatch: Boolean) {
    private val leftEyePoints = (if (labelsMatch) landmarks.leftEye else landmarks.rightEye).map(axes::toLocal)
    private val rightEyePoints = (if (labelsMatch) landmarks.rightEye else landmarks.leftEye).map(axes::toLocal)
    private val brows = listOf(landmarks.leftEyebrow, landmarks.rightEyebrow)
        .map { brow -> brow.map(axes::toLocal) }
        .filter { it.isNotEmpty() }
        .sortedBy { brow -> brow.centroid()?.x ?: 0f }
    private val noseBridge = landmarks.noseBridge.map(axes::toLocal)
    private val noseBottom = landmarks.noseBottom.map(axes::toLocal)
    private val outerLips = landmarks.outerLips.map(axes::toLocal)
    private val innerLips = landmarks.innerLips.map(axes::toLocal)
    private val contour = landmarks.faceContour.map(axes::toLocal)

    fun geometry(): FaceGeometry {
        val leftEye = eye(leftEyePoints, outward = -1f)
        val rightEye = eye(rightEyePoints, outward = 1f)
        val leftBrow = brow(brows.firstOrNull()?.takeIf { brows.size == 2 || it.centroid().isLeft() }, leftEye, outward = -1f)
        val rightBrow = brow(brows.lastOrNull()?.takeIf { brows.size == 2 || !it.centroid().isLeft() }, rightEye, outward = 1f)
        val noseTip = noseBridge.maxByOrNull { it.y }
            ?: noseBottom.centroid()?.let { it + Point(0f, -0.06f) }
            ?: Point(0f, DEFAULT_NOSE_TIP_Y)
        val baseY = noseBottom.maxOfOrNull { it.y }
        val noseBase = noseBottom.filter { baseY != null && it.y >= baseY - NOSE_BASE_BAND }.centroid() ?: (noseTip + Point(0f, 0.08f))
        val noseHalfWidth = if (noseBottom.size >=
            2
        ) {
            (noseBottom.maxOf { it.x } - noseBottom.minOf { it.x }) / 2f
        } else {
            DEFAULT_NOSE_HALF_WIDTH
        }
        val mouthCenter = outerLips.centroid() ?: Point(0f, DEFAULT_MOUTH_Y)
        val mouthLeft = outerLips.minByOrNull { it.x } ?: (mouthCenter + Point(-DEFAULT_MOUTH_HALF_WIDTH, 0f))
        val mouthRight = outerLips.maxByOrNull { it.x } ?: (mouthCenter + Point(DEFAULT_MOUTH_HALF_WIDTH, 0f))
        val lipMiddle = outerLips.filter { abs(it.x - mouthCenter.x) < LIP_MIDDLE_BAND }.ifEmpty { outerLips }
        val upperLipTop = lipMiddle.minByOrNull { it.y } ?: (mouthCenter + Point(0f, -DEFAULT_LIP_HALF_HEIGHT))
        val lowerLipBottom = lipMiddle.maxByOrNull { it.y } ?: (mouthCenter + Point(0f, DEFAULT_LIP_HALF_HEIGHT))
        val chin = contour.maxByOrNull { it.y }?.takeIf { it.y > mouthCenter.y }
            ?: Point(mouthCenter.x, mouthCenter.y + DEFAULT_CHIN_BELOW_MOUTH)
        val sides = SideRules(noseTip = noseTip, mouthCenter = mouthCenter, chin = chin)
        return FaceGeometry(
            axes = axes,
            leftEye = leftEye,
            rightEye = rightEye,
            leftBrow = leftBrow,
            rightBrow = rightBrow,
            noseTip = noseTip,
            noseBase = noseBase,
            noseHalfWidth = noseHalfWidth,
            mouthLeft = mouthLeft,
            mouthRight = mouthRight,
            mouthCenter = mouthCenter,
            upperLipTop = upperLipTop,
            lowerLipBottom = lowerLipBottom,
            mouthOpenness = mouthOpenness(mouthCenter),
            chin = chin,
            leftSide = sides.side(outward = -1f, eye = leftEye, brow = leftBrow, mouthCorner = mouthLeft),
            rightSide = sides.side(outward = 1f, eye = rightEye, brow = rightBrow, mouthCorner = mouthRight),
            forehead = Point(
                (leftBrow.middle.x + rightBrow.middle.x) / 2f,
                minOf(leftBrow.middle.y, rightBrow.middle.y) - FOREHEAD_ABOVE_BROWS,
            ),
        )
    }

    private fun Point?.isLeft(): Boolean = (this?.x ?: 0f) < 0f

    private fun eye(points: List<Point>, outward: Float): EyeShape {
        val center = points.centroid() ?: Point(outward * 0.5f, 0f)
        if (points.isEmpty()) {
            return EyeShape(
                center = center,
                outer = center + Point(outward * DEFAULT_EYE_HALF_WIDTH, 0f),
                inner = center - Point(outward * DEFAULT_EYE_HALF_WIDTH, 0f),
                halfHeight = DEFAULT_EYE_HALF_HEIGHT,
            )
        }
        return EyeShape(
            center = center,
            outer = points.maxBy { it.x * outward },
            inner = points.minBy { it.x * outward },
            halfHeight = ((points.maxOf { it.y } - points.minOf { it.y }) / 2f).coerceAtLeast(MIN_EYE_HALF_HEIGHT),
        )
    }

    private fun brow(points: List<Point>?, eye: EyeShape, outward: Float): BrowShape {
        if (points.isNullOrEmpty()) {
            val middle = eye.center + Point(0f, -DEFAULT_BROW_RISE)
            return BrowShape(
                inner = middle + Point(-outward * DEFAULT_BROW_HALF, 0.03f),
                middle = middle,
                outer = middle + Point(outward * DEFAULT_BROW_HALF, 0.03f),
            )
        }
        return BrowShape(
            inner = points.minBy { it.x * outward },
            middle = points.centroid() ?: eye.center,
            outer = points.maxBy { it.x * outward },
        )
    }

    private fun mouthOpenness(mouthCenter: Point): Float {
        val middle = innerLips.filter { abs(it.x - mouthCenter.x) < INNER_LIP_MIDDLE_BAND }
        if (middle.size < 2) return 0f
        val gap = middle.maxOf { it.y } - middle.minOf { it.y }
        return ((gap - MOUTH_CLOSED_GAP) / (MOUTH_OPEN_GAP - MOUTH_CLOSED_GAP)).coerceIn(0f, 1f)
    }

    /** Where the outline of the face is, on each side, at each height. */
    private inner class SideRules(private val noseTip: Point, private val mouthCenter: Point, private val chin: Point) {
        fun side(
            outward: Float,
            eye: EyeShape,
            brow: BrowShape,
            mouthCorner: Point,
        ): FaceSide {
            val templeY = brow.middle.y
            val cheekboneY = noseTip.y * 0.5f
            val cheekY = (noseTip.y + mouthCenter.y) / 2f
            val gonionY = mouthCenter.y + (chin.y - mouthCenter.y) * GONION_SHARE
            val jaw = Point(edgeX(mouthCenter.y, outward), mouthCenter.y)
            return FaceSide(
                temple = Point(edgeX(templeY, outward) * TEMPLE_INSET, templeY),
                cheekbone = Point(lerp(eye.outer.x, edgeX(cheekboneY, outward), CHEEKBONE_SHARE), cheekboneY),
                cheek = Point(lerp(mouthCorner.x, edgeX(cheekY, outward), 0.5f), cheekY),
                jaw = jaw,
                gonion = Point(edgeX(gonionY, outward), gonionY),
                neck = Point(jaw.x * NECK_INSET, chin.y + NECK_BELOW_CHIN),
            )
        }

        /** The x of the outline at height [y] on the side [outward] points to, from the contour or, without it, as usual. */
        private fun edgeX(y: Float, outward: Float): Float {
            val side = contour.filter { it.x * outward > CONTOUR_SIDE_MARGIN }.sortedBy { it.y }
            if (side.isEmpty()) return outward * DEFAULT_FACE_HALF_WIDTH
            if (y <= side.first().y) return side.first().x
            if (y >= side.last().y) return side.last().x
            val after = side.indexOfFirst { it.y >= y }
            val start = side[after - 1]
            val end = side[after]
            val span = end.y - start.y
            return if (span == 0f) end.x else lerp(start.x, end.x, (y - start.y) / span)
        }
    }
}
