package com.fanguskdev.bakiface.render

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.fanguskdev.bakiface.domain.effect.ColorGrade

private const val ROWS = 4
private const val COLUMNS = 5
private const val CHANNEL_MAX = 255f
private const val LUMA_RED = 0.2126f
private const val LUMA_GREEN = 0.7152f
private const val LUMA_BLUE = 0.0722f
private const val WARMTH_GAIN = 0.6f

/**
 * The 4 × 5 colour matrix of a [ColorGrade] applied by [amount] (0 is no change, 1 is all of it), row after row, with
 * the offsets in 0–255 as Compose's [ColorMatrix] expects. In order: saturation, contrast around the middle grey,
 * warmth (red up and blue down, or the other way) and the tint mixed into the light of each pixel.
 */
fun gradeMatrixValues(grade: ColorGrade, amount: Float): FloatArray {
    val weight = amount.coerceIn(0f, 1f)
    var matrix = identity()
    matrix = concat(matrix, saturation(grade.saturation))
    matrix = concat(matrix, contrast(grade.contrast))
    matrix = concat(matrix, warmth(grade.warmth))
    val tint = grade.tint
    if (tint != null && grade.tintAmount > 0f) {
        matrix = concat(matrix, tint(tint.red / CHANNEL_MAX, tint.green / CHANNEL_MAX, tint.blue / CHANNEL_MAX, grade.tintAmount))
    }
    val plain = identity()
    return FloatArray(ROWS * COLUMNS) { index -> plain[index] + (matrix[index] - plain[index]) * weight }
}

/** The colour filter of a [ColorGrade] at [amount], or `null` when it changes nothing. */
fun gradeColorFilter(grade: ColorGrade, amount: Float): ColorFilter? {
    if (amount <= 0f || grade == ColorGrade.NEUTRAL) return null
    return ColorFilter.colorMatrix(ColorMatrix(gradeMatrixValues(grade, amount)))
}

private fun identity(): FloatArray = floatArrayOf(
    1f, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private fun saturation(value: Float): FloatArray {
    val inverse = 1f - value
    val red = LUMA_RED * inverse
    val green = LUMA_GREEN * inverse
    val blue = LUMA_BLUE * inverse
    return floatArrayOf(
        red + value, green, blue, 0f, 0f,
        red, green + value, blue, 0f, 0f,
        red, green, blue + value, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
}

private fun contrast(value: Float): FloatArray {
    val offset = CHANNEL_MAX / 2f * (1f - value)
    return floatArrayOf(
        value, 0f, 0f, 0f, offset,
        0f, value, 0f, 0f, offset,
        0f, 0f, value, 0f, offset,
        0f, 0f, 0f, 1f, 0f,
    )
}

private fun warmth(value: Float): FloatArray = floatArrayOf(
    1f + WARMTH_GAIN * value, 0f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f, 0f,
    0f, 0f, 1f - WARMTH_GAIN * value, 0f, 0f,
    0f, 0f, 0f, 1f, 0f,
)

private fun tint(
    red: Float,
    green: Float,
    blue: Float,
    amount: Float,
): FloatArray {
    val keep = 1f - amount
    fun row(channel: Float, own: Int): FloatArray = FloatArray(COLUMNS) { column ->
        val luma = when (column) {
            0 -> LUMA_RED
            1 -> LUMA_GREEN
            2 -> LUMA_BLUE
            else -> 0f
        }
        (if (column == own) keep else 0f) + amount * channel * luma
    }
    return row(red, 0) + row(green, 1) + row(blue, 2) + floatArrayOf(0f, 0f, 0f, 1f, 0f)
}

/** The matrix that applies [first] and then [second]. */
internal fun concat(first: FloatArray, second: FloatArray): FloatArray {
    val result = FloatArray(ROWS * COLUMNS)
    for (row in 0 until ROWS) {
        for (column in 0 until COLUMNS) {
            var sum = if (column == COLUMNS - 1) second[row * COLUMNS + COLUMNS - 1] else 0f
            for (inner in 0 until ROWS) {
                sum += second[row * COLUMNS + inner] * first[inner * COLUMNS + column]
            }
            result[row * COLUMNS + column] = sum
        }
    }
    return result
}
