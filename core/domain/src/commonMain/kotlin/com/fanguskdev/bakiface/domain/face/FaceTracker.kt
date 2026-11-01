package com.fanguskdev.bakiface.domain.face

import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.centroid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private const val DEFAULT_FADE_IN_NANOS = 180_000_000L
private const val DEFAULT_FADE_OUT_NANOS = 260_000_000L

/** The face a [FaceTracker] follows in one frame: its smoothed [landmarks] and how present it is, from 0 to 1. */
data class TrackedFace(val landmarks: FaceLandmarks, val presence: Float)

/**
 * Follows one face through the frames of the camera (ADR 0005): it smooths the detector's points with a
 * [OneEuroFilter] and fades the effect in when a face appears and out when it is lost, instead of switching it on and
 * off. A face lost for a frame or two keeps its place; one lost for longer than the fade is forgotten, and the next
 * one starts from scratch. One tracker per camera session: it remembers the frames it has seen.
 */
class FaceTracker(
    private val filter: OneEuroFilter = OneEuroFilter(),
    private val fadeInNanos: Long = DEFAULT_FADE_IN_NANOS,
    private val fadeOutNanos: Long = DEFAULT_FADE_OUT_NANOS,
) {
    private var lastLandmarks: FaceLandmarks? = null
    private var presence = 0f
    private var lastNanos: Long? = null

    /** The face to draw for a frame taken at [timestampNanos] in which the detector found [landmarks] (or nothing). */
    fun track(landmarks: FaceLandmarks?, timestampNanos: Long): TrackedFace? {
        val elapsed = lastNanos?.let { (timestampNanos - it).coerceAtLeast(0L) } ?: 0L
        lastNanos = timestampNanos
        if (landmarks != null && landmarks.pointCount > 0) {
            presence = if (lastLandmarks == null) {
                0f
            } else {
                (presence + elapsed.toFloat() / fadeInNanos).coerceAtMost(1f)
            }
            val smoothed = smooth(landmarks, timestampNanos)
            lastLandmarks = smoothed
            return TrackedFace(smoothed, presence)
        }
        val kept = lastLandmarks ?: return null
        presence = (presence - elapsed.toFloat() / fadeOutNanos).coerceAtLeast(0f)
        if (presence <= 0f) {
            lastLandmarks = null
            filter.reset()
            return null
        }
        return TrackedFace(kept, presence)
    }

    private fun smooth(landmarks: FaceLandmarks, timestampNanos: Long): FaceLandmarks {
        val regions = landmarks.regions
        val values = FloatArray(landmarks.pointCount * 2)
        var cursor = 0
        for (region in regions) {
            for (point in region) {
                values[cursor++] = point.x
                values[cursor++] = point.y
            }
        }
        val smoothed = filter.filter(values, timestampNanos, scale = eyeDistance(landmarks))
        cursor = 0
        val rebuilt: List<ImmutableList<Point>> = regions.map { region ->
            List(region.size) {
                val point = Point(smoothed[cursor], smoothed[cursor + 1])
                cursor += 2
                point
            }.toImmutableList()
        }
        return FaceLandmarks.fromRegions(rebuilt)
    }

    private fun eyeDistance(landmarks: FaceLandmarks): Float {
        val left = landmarks.leftEye.centroid() ?: return 1f
        val right = landmarks.rightEye.centroid() ?: return 1f
        return (right - left).length.coerceAtLeast(1f)
    }
}
