package com.fanguskdev.bakiface.data.camera

import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.centroid
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.useContents
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import platform.Vision.VNDetectFaceLandmarksRequest
import platform.Vision.VNFaceLandmarkRegion2D
import platform.Vision.VNFaceObservation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The largest face Vision found with this request, if any. */
@OptIn(ExperimentalForeignApi::class)
internal fun VNDetectFaceLandmarksRequest.largestFace(): VNFaceObservation? = results.orEmpty()
    .filterIsInstance<VNFaceObservation>()
    .maxByOrNull { face -> face.boundingBox.useContents { size.width * size.height } }

/**
 * Vision's landmarks as the domain's, in pixels of a [width] × [height] picture with the origin at the top-left
 * corner (Vision counts from the bottom-left). Each region's points are normalised to the face's bounding box, which
 * is normalised to the picture: both steps are done here, because `pointsInImageOfSize` was seen to scale them wrongly
 * (the eyes of a face 264 px wide came back 14 px apart). The `nose` region is its whole outline: the geometry takes
 * its lower band for the base of the nose and its width for the nostrils.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun VNFaceObservation.toLandmarks(width: Int, height: Int): FaceLandmarks? {
    val (boxX, boxY, boxWidth, boxHeight) = boundingBox.useContents { listOf(origin.x, origin.y, size.width, size.height) }
    val box = PixelBox(
        left = boxX * width,
        top = (1.0 - boxY - boxHeight) * height,
        width = boxWidth * width,
        height = boxHeight * height,
    )
    val found = landmarksIn(width, height, boxX, boxY, boxWidth, boxHeight)
    return when {
        found != null && box.isPlausible(found) -> found
        isSimulator -> averageFaceIn(box)
        else -> null
    }
}

/** The landmarks Vision gives, before checking them. */
@OptIn(ExperimentalForeignApi::class)
private fun VNFaceObservation.landmarksIn(
    width: Int,
    height: Int,
    boxX: Double,
    boxY: Double,
    boxWidth: Double,
    boxHeight: Double,
): FaceLandmarks? {
    val regions = landmarks ?: return null
    fun points(region: VNFaceLandmarkRegion2D?): ImmutableList<Point> {
        if (region == null) return persistentListOf()
        val raw = region.normalizedPoints ?: return persistentListOf()
        return List(region.pointCount.toInt()) { index ->
            val point = raw[index]
            Point(
                ((boxX + point.x * boxWidth) * width).toFloat(),
                ((1.0 - (boxY + point.y * boxHeight)) * height).toFloat(),
            )
        }.toImmutableList()
    }
    val leftEye = points(regions.leftEye)
    val rightEye = points(regions.rightEye)
    if (leftEye.isEmpty() || rightEye.isEmpty()) return null
    return FaceLandmarks(
        faceContour = points(regions.faceContour),
        leftEyebrow = points(regions.leftEyebrow),
        rightEyebrow = points(regions.rightEyebrow),
        leftEye = leftEye,
        rightEye = rightEye,
        noseBridge = points(regions.noseCrest),
        noseBottom = points(regions.nose),
        outerLips = points(regions.outerLips),
        innerLips = points(regions.innerLips),
    )
}

/** The face's bounding box in pixels of the picture, with the origin at the top-left corner. */
private data class PixelBox(
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double,
) {
    /**
     * Whether [landmarks] can belong to the face in this box: both eyes inside it (with a margin) and apart by at least
     * a fifth of its width. Vision on the iOS simulator returns points squeezed into the middle of the box; warping a
     * face with them would distort it at random, so such a result counts as no face.
     */
    fun isPlausible(landmarks: FaceLandmarks): Boolean {
        val left = landmarks.leftEye.centroid() ?: return false
        val right = landmarks.rightEye.centroid() ?: return false
        val margin = width * PLAUSIBLE_MARGIN
        fun inside(point: Point) = point.x in (this.left - margin)..(this.left + width + margin) &&
            point.y in (top - margin)..(top + height + margin)
        return inside(left) && inside(right) && (right - left).length >= width * MIN_EYE_SHARE
    }
}

private const val PLAUSIBLE_MARGIN = 0.1
private const val MIN_EYE_SHARE = 0.2

// Where an average face sits in Vision's bounding box, measured on Vision's own results on a Mac: the eyes on a line
// 29 % down the box and a third of its width apart, which makes the box three eye-distances wide and tall.
private const val EYE_LINE = 0.29
private const val UNITS_PER_BOX = 3.0

/**
 * An average face fitted to [box], for the iOS simulator only, where Vision finds the face but not its features: it
 * lets the effects be seen there. A phone always uses Vision's own landmarks.
 */
private fun averageFaceIn(box: PixelBox): FaceLandmarks {
    fun place(x: Double, y: Double) = Point(
        (box.left + (0.5 + x / UNITS_PER_BOX) * box.width).toFloat(),
        (box.top + (EYE_LINE + y / UNITS_PER_BOX) * box.height).toFloat(),
    )
    fun ring(
        cx: Double,
        cy: Double,
        rx: Double,
        ry: Double,
        count: Int,
    ): ImmutableList<Point> = List(count) { index ->
        val angle = 2.0 * PI * index / count
        place(cx + rx * cos(angle), cy + ry * sin(angle))
    }.toImmutableList()
    fun line(vararg points: Pair<Double, Double>): ImmutableList<Point> = points.map { (x, y) -> place(x, y) }.toImmutableList()
    return FaceLandmarks(
        faceContour = List(17) { index ->
            val angle = PI * index / 16
            place(1.05 * cos(angle), 0.35 + 1.5 * sin(angle))
        }.toImmutableList(),
        leftEyebrow = line(-0.85 to -0.3, -0.7 to -0.38, -0.5 to -0.4, -0.3 to -0.37, -0.18 to -0.32),
        rightEyebrow = line(0.18 to -0.32, 0.3 to -0.37, 0.5 to -0.4, 0.7 to -0.38, 0.85 to -0.3),
        leftEye = ring(-0.5, 0.0, 0.2, 0.1, 8),
        rightEye = ring(0.5, 0.0, 0.2, 0.1, 8),
        noseBridge = line(0.0 to 0.1, 0.0 to 0.4, 0.0 to 0.75),
        noseBottom = line(-0.22 to 0.82, -0.1 to 0.86, 0.0 to 0.87, 0.1 to 0.86, 0.22 to 0.82),
        outerLips = ring(0.0, 1.2, 0.42, 0.12, 14),
        innerLips = line(-0.3 to 1.2, 0.0 to 1.21, 0.3 to 1.2, 0.0 to 1.22),
    )
}
