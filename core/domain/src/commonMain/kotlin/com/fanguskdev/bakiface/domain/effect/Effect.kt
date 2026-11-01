package com.fanguskdev.bakiface.domain.effect

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.jvm.JvmInline

/** The stable name of an effect, the one the preferences store and the presentation translates. */
@JvmInline
value class EffectId(val value: String)

/** A colour, packed as `0xAARRGGBB`. */
@JvmInline
value class Argb(val value: Long) {
    val alpha: Int get() = ((value shr 24) and 0xFF).toInt()
    val red: Int get() = ((value shr 16) and 0xFF).toInt()
    val green: Int get() = ((value shr 8) and 0xFF).toInt()
    val blue: Int get() = (value and 0xFF).toInt()
}

/** A hint the camera shows when the effect reacts to something the person does. */
enum class EffectHint {
    /** Opening the mouth makes the effect stronger. */
    OpenMouth,
}

/**
 * A character of Baki as a set of changes to the person's own face (ADR 0005): never a mask, never another face. The
 * [warp] moves and scales the features, the [marks] add what lives on the skin (scars, veins, wrinkles, shadows,
 * facial hair) and the [grade] gives the picture the character's colour.
 */
data class Effect(
    val id: EffectId,
    val name: String,
    val kanji: String,
    val accent: Argb,
    val warp: ImmutableList<WarpOp> = persistentListOf(),
    val marks: ImmutableList<MarkSpec> = persistentListOf(),
    val grade: ColorGrade = ColorGrade.NEUTRAL,
    val hint: EffectHint? = null,
)

/**
 * The colour of a character, applied to the whole picture: [contrast] and [saturation] multiply (1 leaves them as
 * they are), [warmth] pushes towards orange (positive) or blue (negative), and [tint] is mixed in by [tintAmount].
 */
data class ColorGrade(
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val warmth: Float = 0f,
    val tint: Argb? = null,
    val tintAmount: Float = 0f,
) {
    companion object {
        val NEUTRAL = ColorGrade()
    }
}
