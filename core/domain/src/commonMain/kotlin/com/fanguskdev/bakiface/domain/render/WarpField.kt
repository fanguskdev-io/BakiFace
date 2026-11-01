package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.effect.WarpOp
import com.fanguskdev.bakiface.domain.face.FaceAxes
import com.fanguskdev.bakiface.domain.face.FaceGeometry
import com.fanguskdev.bakiface.domain.geometry.Point
import kotlin.math.sqrt

/**
 * How far every point of the face moves under an effect (ADR 0005), built from its [WarpOp]s at a given strength.
 *
 * Each change weighs `(1 − t²)²`, where t is the distance to its centre over its radius: full at the centre, nothing
 * at the edge, and smooth at both, so the skin bends instead of tearing. A move adds its push times that weight; a
 * scale adds `(s − 1)` times the offset from its centre times that weight. The changes add up, each one measured from
 * where the point was, never from where another change left it.
 */
class WarpField private constructor(
    val axes: FaceAxes,
    private val isScale: BooleanArray,
    private val centerX: FloatArray,
    private val centerY: FloatArray,
    private val radius: FloatArray,
    private val amountX: FloatArray,
    private val amountY: FloatArray,
) {
    /** How far from the origin, in units of the face, anything can move. Farther, nothing does. */
    val reach: Float = centerX.indices.maxOfOrNull { index ->
        sqrt(centerX[index] * centerX[index] + centerY[index] * centerY[index]) + radius[index]
    } ?: 0f

    val isEmpty: Boolean get() = centerX.isEmpty()

    /** The displacement, in units of the face, of the face point ([x], [y]), written into [out] as x, y. */
    fun displacementLocal(x: Float, y: Float, out: FloatArray) {
        var moveX = 0f
        var moveY = 0f
        for (index in centerX.indices) {
            val offsetX = x - centerX[index]
            val offsetY = y - centerY[index]
            val distanceSquared = offsetX * offsetX + offsetY * offsetY
            val radiusSquared = radius[index] * radius[index]
            if (distanceSquared >= radiusSquared) continue
            val falloff = 1f - distanceSquared / radiusSquared
            val weight = falloff * falloff
            if (isScale[index]) {
                moveX += weight * amountX[index] * offsetX
                moveY += weight * amountY[index] * offsetY
            } else {
                moveX += weight * amountX[index]
                moveY += weight * amountY[index]
            }
        }
        out[0] = moveX
        out[1] = moveY
    }

    /** Where the face point [local] ends up, still in the face's frame. */
    fun warpLocal(local: Point): Point {
        val out = FloatArray(2)
        displacementLocal(local.x, local.y, out)
        return Point(local.x + out[0], local.y + out[1])
    }

    /** Where the face point [local] ends up, in picture pixels. */
    fun warpToImage(local: Point): Point = axes.toImage(warpLocal(local))

    companion object {
        /**
         * The field of [ops] on [geometry] at [strength] (the intensity times how present the face is), with each change
         * made stronger by its [WarpOp.mouthOpenBoost] times how open the mouth is.
         */
        fun build(geometry: FaceGeometry, ops: List<WarpOp>, strength: Float): WarpField {
            val resolved = ops.flatMap { op -> resolve(geometry, op, strength) }
            return WarpField(
                axes = geometry.axes,
                isScale = BooleanArray(resolved.size) { resolved[it].isScale },
                centerX = FloatArray(resolved.size) { resolved[it].center.x },
                centerY = FloatArray(resolved.size) { resolved[it].center.y },
                radius = FloatArray(resolved.size) { resolved[it].radius },
                amountX = FloatArray(resolved.size) { resolved[it].amountX },
                amountY = FloatArray(resolved.size) { resolved[it].amountY },
            )
        }

        private fun resolve(geometry: FaceGeometry, op: WarpOp, strength: Float): List<ResolvedOp> {
            val power = strength * (1f + op.mouthOpenBoost * geometry.mouthOpenness)
            if (power <= 0f || op.radius <= 0f) return emptyList()
            val sides = if (op.feature.paired) Side.entries.filter(op.sides::includes) else listOf(Side.Right)
            return sides.map { side ->
                val outward = if (op.feature.paired) side.sign else 1f
                val center = geometry.anchor(op.feature, side) + Point(outward * op.offsetX, op.offsetY)
                when (op) {
                    is WarpOp.Move -> ResolvedOp(false, center, op.radius, outward * op.dx * power, op.dy * power)
                    is WarpOp.Scale -> ResolvedOp(true, center, op.radius, (op.sx - 1f) * power, (op.sy - 1f) * power)
                }
            }
        }
    }

    private class ResolvedOp(
        val isScale: Boolean,
        val center: Point,
        val radius: Float,
        val amountX: Float,
        val amountY: Float,
    )
}
