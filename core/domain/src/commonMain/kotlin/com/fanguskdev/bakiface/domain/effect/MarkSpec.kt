package com.fanguskdev.bakiface.domain.effect

import kotlinx.collections.immutable.ImmutableList

/** How a mark mixes with the skin under it: painted over, darkening it or lightening it. */
enum class MarkBlend {
    Normal,
    Multiply,
    Screen,
}

/** Where on the face a set of [MarkSpec.Folds] is laid (ADR 0011). */
enum class FoldRegion {
    /** Waves across the forehead, above the brows. */
    Forehead,

    /** Upright creases between the brows. */
    Frown,

    /** Lines fanning out from the outer corner of each eye. */
    CrowsFeet,

    /** Arcs under each eye. */
    UnderEyes,

    /** The deep brackets from the wings of the nose around the mouth. */
    Smile,

    /** Long curves down each cheek, outside the smile. */
    Cheeks,

    /** Short creases across the bridge of the nose. */
    NoseBridge,

    /** Lines down from the corners of the mouth. */
    Mouth,

    /** Arcs on the chin. */
    Chin,

    /** The cords of the neck, under the jaw. */
    Neck,
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
     * The creases of a face drawn the way Itagaki draws every muscle (ADR 0011): [count] folds in [region], on both
     * halves of the face where the region has two, laid out from the features with a scatter fixed per fold so they stay
     * put from frame to frame. Each is a dark crease with its shadow and the light on its upper edge; [depth] goes from 0
     * (faint) to 1 (carved).
     */
    data class Folds(
        val region: FoldRegion,
        val count: Int,
        val depth: Float,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec {
        override val mirrored: Boolean get() = false
    }

    /** Both eyes painted [color] from lid to lid: the blank stare of the Ogre (ADR 0011). */
    data class BlankEyes(val color: Argb, val alpha: Float, override val mouthOpenBoost: Float = 0f) : MarkSpec {
        override val mirrored: Boolean get() = false
    }

    /** Both eyebrows redrawn as strokes of ink, [width] thick at the inner end and thinning outwards (ADR 0011). */
    data class InkBrows(
        val width: Float,
        val color: Argb,
        val alpha: Float,
        override val mouthOpenBoost: Float = 0f,
    ) : MarkSpec {
        override val mirrored: Boolean get() = false
    }

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
