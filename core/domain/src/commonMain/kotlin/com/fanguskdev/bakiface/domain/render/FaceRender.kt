package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.ColorGrade
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.geometry.Point
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The picture's grid after the warp (ADR 0005): [columns] × [rows] cells over a picture of [imageWidth] ×
 * [imageHeight], and in [positions], as x, y pairs row after row, where each of the `(columns + 1) × (rows + 1)`
 * corners of the grid ends up. The corner (i, j) takes the colour of the picture at (i·w/columns, j·h/rows): drawing
 * the picture through this grid is drawing it warped.
 */
class WarpMesh(
    val columns: Int,
    val rows: Int,
    val imageWidth: Int,
    val imageHeight: Int,
    val positions: FloatArray,
) {
    init {
        require(columns > 0 && rows > 0) { "A mesh needs at least one cell" }
        require(positions.size == (columns + 1) * (rows + 1) * 2) { "A ${columns}x$rows mesh has ${vertexCount * 2} coordinates" }
    }

    val vertexCount: Int get() = (columns + 1) * (rows + 1)
}

/** Something drawn on the skin, already in picture pixels and already warped. */
sealed interface SkinMark {
    val color: Argb
    val alpha: Float
    val blend: MarkBlend

    /** A line through [points], [width] pixels wide, with round ends. */
    data class Line(
        val points: ImmutableList<Point>,
        val width: Float,
        override val color: Argb,
        override val alpha: Float,
        override val blend: MarkBlend,
    ) : SkinMark

    /** A soft oval, opaque at the [center] and gone at its edge, turned [angleDegrees]. */
    data class Spot(
        val center: Point,
        val radiusX: Float,
        val radiusY: Float,
        val angleDegrees: Float,
        override val color: Argb,
        override val alpha: Float,
        override val blend: MarkBlend,
    ) : SkinMark
}

/**
 * Everything needed to draw one picture with an effect on it (ADR 0005): the warped [mesh] (none without a face), the
 * [marks] on the skin and the character's [grade], applied by [gradeAmount] (0 leaves the colours as they are).
 */
data class FaceRender(
    val mesh: WarpMesh?,
    val marks: ImmutableList<SkinMark> = persistentListOf(),
    val grade: ColorGrade = ColorGrade.NEUTRAL,
    val gradeAmount: Float = 0f,
)
