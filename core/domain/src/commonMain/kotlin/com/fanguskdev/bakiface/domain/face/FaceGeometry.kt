package com.fanguskdev.bakiface.domain.face

import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.FeaturePoint
import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.geometry.Point

/**
 * The face's own frame of reference (ADR 0005): the origin between the eyes, x from the eye nearer the picture's left
 * edge to the other one, y down the face, and one unit as long as the distance between the eyes. Effects are written
 * in it, so they follow the face whatever its size, place or tilt in the picture.
 */
data class FaceAxes(val origin: Point, val xAxis: Point, val unit: Float) {
    val yAxis: Point get() = Point(-xAxis.y, xAxis.x)

    /** Where the picture point [point] is in the face's frame. */
    fun toLocal(point: Point): Point {
        val offset = point - origin
        return Point((offset dot xAxis) / unit, (offset dot yAxis) / unit)
    }

    /** Where the face point [local] is in the picture. */
    fun toImage(local: Point): Point = origin + vectorToImage(local)

    /** A vector of the face's frame, in picture pixels. */
    fun vectorToImage(local: Point): Point = Point(
        (xAxis.x * local.x - xAxis.y * local.y) * unit,
        (xAxis.y * local.x + xAxis.x * local.y) * unit,
    )
}

/** One eye, in the face's frame: its centre, its corners and half of how open it is. */
data class EyeShape(
    val center: Point,
    val outer: Point,
    val inner: Point,
    val halfHeight: Float,
)

/** One eyebrow, in the face's frame. */
data class BrowShape(val inner: Point, val middle: Point, val outer: Point)

/** One side of the face, in the face's frame, from the temple down to the neck. */
data class FaceSide(
    val temple: Point,
    val cheekbone: Point,
    val cheek: Point,
    val jaw: Point,
    val gonion: Point,
    val neck: Point,
)

/**
 * Where everything an effect needs is, in the face's frame [axes] (ADR 0005). Built by [faceGeometryOf] from what a
 * detector found; [mouthOpenness] goes from 0 (closed) to 1 (wide open).
 */
data class FaceGeometry(
    val axes: FaceAxes,
    val leftEye: EyeShape,
    val rightEye: EyeShape,
    val leftBrow: BrowShape,
    val rightBrow: BrowShape,
    val noseTip: Point,
    val noseBase: Point,
    val noseHalfWidth: Float,
    val mouthLeft: Point,
    val mouthRight: Point,
    val mouthCenter: Point,
    val upperLipTop: Point,
    val lowerLipBottom: Point,
    val mouthOpenness: Float,
    val chin: Point,
    val leftSide: FaceSide,
    val rightSide: FaceSide,
    val forehead: Point,
) {
    /** Where [feature] is on [side], in the face's frame. Features on the middle line ignore [side]. */
    fun anchor(feature: Feature, side: Side): Point {
        val eye = if (side == Side.Left) leftEye else rightEye
        val brow = if (side == Side.Left) leftBrow else rightBrow
        val faceSide = if (side == Side.Left) leftSide else rightSide
        return when (feature) {
            Feature.Eye -> eye.center
            Feature.EyeOuter -> eye.outer
            Feature.EyeInner -> eye.inner
            Feature.BrowInner -> brow.inner
            Feature.BrowMid -> brow.middle
            Feature.BrowOuter -> brow.outer
            Feature.Temple -> faceSide.temple
            Feature.Cheekbone -> faceSide.cheekbone
            Feature.Cheek -> faceSide.cheek
            Feature.Jaw -> faceSide.jaw
            Feature.Gonion -> faceSide.gonion
            Feature.Neck -> faceSide.neck
            Feature.MouthCorner -> if (side == Side.Left) mouthLeft else mouthRight
            Feature.NoseTip -> noseTip
            Feature.NoseBase -> noseBase
            Feature.Mouth -> mouthCenter
            Feature.UpperLip -> upperLipTop
            Feature.LowerLip -> lowerLipBottom
            Feature.Chin -> chin
            Feature.Forehead -> forehead
        }
    }

    /**
     * Where [point] is in the face's frame; with [mirror], its mirror image on the other half of the face (the other
     * side, and [FeaturePoint.dx] the other way).
     */
    fun resolve(point: FeaturePoint, mirror: Boolean = false): Point = if (mirror) {
        anchor(point.feature, point.side.opposite) + Point(-point.dx, point.dy)
    } else {
        anchor(point.feature, point.side) + Point(point.dx, point.dy)
    }
}
