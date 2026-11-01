package com.fanguskdev.bakiface.domain.effect

/**
 * One local change to the shape of the face (ADR 0005), centred on a [feature] and fading to nothing at [radius]
 * (units of the distance between the eyes). On a paired feature it applies to the [sides] chosen, mirrored, so
 * "outwards" means away from the middle of the face on each side. [offsetX] (outwards) and [offsetY] (down) move the
 * centre away from the feature. [mouthOpenBoost] makes the change stronger as the mouth opens: at 1, an open mouth
 * doubles it.
 */
sealed interface WarpOp {
    val feature: Feature
    val sides: Sides
    val offsetX: Float
    val offsetY: Float
    val radius: Float
    val mouthOpenBoost: Float

    /** Pushes the skin around the centre by ([dx] outwards, [dy] down). */
    data class Move(
        override val feature: Feature,
        override val radius: Float,
        val dx: Float = 0f,
        val dy: Float = 0f,
        override val sides: Sides = Sides.Both,
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val mouthOpenBoost: Float = 0f,
    ) : WarpOp

    /** Grows ([sx] or [sy] above 1) or shrinks (below 1) the skin around the centre, across and down the face. */
    data class Scale(
        override val feature: Feature,
        override val radius: Float,
        val sx: Float = 1f,
        val sy: Float = 1f,
        override val sides: Sides = Sides.Both,
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val mouthOpenBoost: Float = 0f,
    ) : WarpOp
}
