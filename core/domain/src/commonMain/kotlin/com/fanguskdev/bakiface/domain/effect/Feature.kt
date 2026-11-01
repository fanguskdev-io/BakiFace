package com.fanguskdev.bakiface.domain.effect

/**
 * A place on the face an effect is anchored to (ADR 0005). [paired] features exist on both sides of the face; the
 * others sit on the middle line.
 */
enum class Feature(val paired: Boolean) {
    Eye(paired = true),
    EyeOuter(paired = true),
    EyeInner(paired = true),
    BrowInner(paired = true),
    BrowMid(paired = true),
    BrowOuter(paired = true),
    Temple(paired = true),
    Cheekbone(paired = true),
    Cheek(paired = true),
    Jaw(paired = true),
    Gonion(paired = true),
    Neck(paired = true),
    MouthCorner(paired = true),
    NoseTip(paired = false),
    NoseBase(paired = false),
    Mouth(paired = false),
    UpperLip(paired = false),
    LowerLip(paired = false),
    Chin(paired = false),
    Forehead(paired = false),
}

/**
 * A side of the face, seen in the picture: [Left] is the half nearer the picture's left edge. [sign] is the direction
 * of "outwards" along the face's x axis.
 */
enum class Side(val sign: Float) {
    Left(sign = -1f),
    Right(sign = 1f),
    ;

    val opposite: Side get() = if (this == Left) Right else Left
}

/** Which sides a change to a paired feature applies to. */
enum class Sides {
    Both,
    Left,
    Right,
    ;

    fun includes(side: Side): Boolean = when (this) {
        Both -> true
        Left -> side == Side.Left
        Right -> side == Side.Right
    }
}

/**
 * A point given by where it is from a feature, in units of the distance between the eyes: [dx] to the picture's
 * right, [dy] down the face. [side] picks the half for a paired feature and, for one on the middle line, which
 * way is "outwards" for the marks that care (facial hair).
 */
data class FeaturePoint(
    val feature: Feature,
    val side: Side = Side.Left,
    val dx: Float = 0f,
    val dy: Float = 0f,
)
