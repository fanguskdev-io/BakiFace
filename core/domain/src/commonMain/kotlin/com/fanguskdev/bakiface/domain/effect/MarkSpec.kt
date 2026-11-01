package com.fanguskdev.bakiface.domain.effect

import kotlinx.collections.immutable.ImmutableList

/** How a mark mixes with the skin under it: painted over, darkening it or lightening it. */
enum class MarkBlend {
    Normal,
    Multiply,
    Screen,
}

/**
 * Something an effect adds on the skin (ADR 0005), written from the features so that it follows the face and the
 * warp. [mirrored] draws it again on the other side of the face; [mouthOpenBoost] makes it stronger as the mouth opens.
 * Sizes are in units of the distance between the eyes.
 */
sealed interface MarkSpec {
    val mirrored: Boolean
    val mouthOpenBoost: Float

    /** A healed cut along [path]: a shadow, a paler raised line and, with [stitches], the marks of the stitches. */
    data class Scar(
        val path: ImmutableList<FeaturePoint>,
        val width: Float,
        val stitches: Boolean = false,
        override val mirrored: Boolean = false,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec

    /** A swollen vein along [path]: a darker line with a thin highlight. */
    data class Vein(
        val path: ImmutableList<FeaturePoint>,
        val width: Float,
        override val mirrored: Boolean = false,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec

    /** A fold of the skin along [path]; [depth] from 0 (faint) to 1 (deep). */
    data class Wrinkle(
        val path: ImmutableList<FeaturePoint>,
        val depth: Float,
        override val mirrored: Boolean = false,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec

    /** A soft oval of [color] around [at], to darken a hollow or light up a bone. */
    data class Shade(
        val at: FeaturePoint,
        val radiusX: Float,
        val radiusY: Float,
        val color: Argb,
        val alpha: Float,
        val blend: MarkBlend,
        override val mirrored: Boolean = false,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec

    /**
     * [count] strands of hair rooted between [from] and [to], each [length] long and pointing [angleDegrees] from
     * "down the face" towards "outwards", give or take [spreadDegrees]: eyebrows, a moustache, a beard.
     */
    data class Hair(
        val from: FeaturePoint,
        val to: FeaturePoint,
        val count: Int,
        val length: Float,
        val angleDegrees: Float,
        val spreadDegrees: Float,
        val color: Argb,
        val width: Float,
        override val mirrored: Boolean = false,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec
}
