package com.fanguskdev.bakiface.domain.face

import com.fanguskdev.bakiface.domain.geometry.Point
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * The outline of the features of one face as the platform's detector sees it (ML Kit on Android, Vision on iOS), in
 * pixels of the picture it was found in (ADR 0005).
 *
 * "Left" and "right" are the detector's labels and are not trusted: [faceGeometryOf] sorts the sides out by looking
 * where each one is. A region the detector does not give is empty, and the geometry falls back to the usual
 * proportions of a face for it.
 */
data class FaceLandmarks(
    val faceContour: ImmutableList<Point> = persistentListOf(),
    val leftEyebrow: ImmutableList<Point> = persistentListOf(),
    val rightEyebrow: ImmutableList<Point> = persistentListOf(),
    val leftEye: ImmutableList<Point> = persistentListOf(),
    val rightEye: ImmutableList<Point> = persistentListOf(),
    val noseBridge: ImmutableList<Point> = persistentListOf(),
    val noseBottom: ImmutableList<Point> = persistentListOf(),
    val outerLips: ImmutableList<Point> = persistentListOf(),
    val innerLips: ImmutableList<Point> = persistentListOf(),
) {
    /** Every region, always in the same order: the shape [LandmarkSmoother] filters. */
    internal val regions: List<ImmutableList<Point>>
        get() = listOf(faceContour, leftEyebrow, rightEyebrow, leftEye, rightEye, noseBridge, noseBottom, outerLips, innerLips)

    /** How many points there are in all the regions together. */
    val pointCount: Int get() = regions.sumOf { it.size }

    /** The same face with every point moved by [transform]: a crop, a mirror, a change of scale. */
    fun map(transform: (Point) -> Point): FaceLandmarks = fromRegions(regions.map { region -> region.map(transform).toImmutableList() })

    internal companion object {
        const val REGION_COUNT = 9

        fun fromRegions(regions: List<ImmutableList<Point>>): FaceLandmarks {
            require(regions.size == REGION_COUNT) { "A face has $REGION_COUNT regions, not ${regions.size}" }
            return FaceLandmarks(
                faceContour = regions[0],
                leftEyebrow = regions[1],
                rightEyebrow = regions[2],
                leftEye = regions[3],
                rightEye = regions[4],
                noseBridge = regions[5],
                noseBottom = regions[6],
                outerLips = regions[7],
                innerLips = regions[8],
            )
        }
    }
}
