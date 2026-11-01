package com.fanguskdev.bakiface.render

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.ColorGrade
import com.fanguskdev.bakiface.domain.picture.Picture
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun FloatArray.apply(red: Float, green: Float, blue: Float): Triple<Float, Float, Float> {
    fun row(index: Int) =
        this[index * 5] * red + this[index * 5 + 1] * green + this[index * 5 + 2] * blue + this[index * 5 + 3] * 255f + this[index * 5 + 4]
    return Triple(row(0), row(1), row(2))
}

private fun assertClose(expected: Float, actual: Float, tolerance: Float = 0.5f) =
    assertTrue(abs(expected - actual) <= tolerance, "expected $expected ± $tolerance, was $actual")

class GradeMatrixTest {
    @Test
    fun aNeutralGradeOrNoAmountChangesNothing() {
        val identity = floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)

        assertEquals(identity.toList(), gradeMatrixValues(ColorGrade.NEUTRAL, 1f).toList())
        assertEquals(identity.toList(), gradeMatrixValues(ColorGrade(contrast = 2f), 0f).toList())
        assertNull(gradeColorFilter(ColorGrade.NEUTRAL, 1f))
        assertNull(gradeColorFilter(ColorGrade(contrast = 2f), 0f))
    }

    @Test
    fun contrastKeepsTheMiddleGreyAndSpreadsTheRest() {
        val matrix = gradeMatrixValues(ColorGrade(contrast = 1.5f), 1f)

        assertClose(127.5f, matrix.apply(127.5f, 127.5f, 127.5f).first)
        assertClose(202.5f, matrix.apply(177.5f, 177.5f, 177.5f).first)
    }

    @Test
    fun noSaturationIsGrey() {
        val (red, green, blue) = gradeMatrixValues(ColorGrade(saturation = 0f), 1f).apply(200f, 40f, 40f)

        assertClose(red, green)
        assertClose(green, blue)
    }

    @Test
    fun warmthPushesRedUpAndBlueDown() {
        val (red, green, blue) = gradeMatrixValues(ColorGrade(warmth = 0.1f), 1f).apply(100f, 100f, 100f)

        assertClose(106f, red)
        assertClose(100f, green)
        assertClose(94f, blue)
    }

    @Test
    fun aFullTintIsTheTintColourAtThePixelsLight() {
        val (red, green, blue) = gradeMatrixValues(ColorGrade(tint = Argb(0xFFFF0000L), tintAmount = 1f), 1f).apply(255f, 255f, 255f)

        assertClose(255f, red)
        assertClose(0f, green)
        assertClose(0f, blue)
    }

    @Test
    fun halfTheAmountIsHalfTheChange() {
        val full = gradeMatrixValues(ColorGrade(warmth = 0.2f), 1f).apply(100f, 100f, 100f).first
        val half = gradeMatrixValues(ColorGrade(warmth = 0.2f), 0.5f).apply(100f, 100f, 100f).first

        assertClose((full + 100f) / 2f, half)
    }

    @Test
    fun matricesComposeInOrder() {
        val double = floatArrayOf(2f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)
        val plusTen = floatArrayOf(1f, 0f, 0f, 0f, 10f, 0f, 1f, 0f, 0f, 10f, 0f, 0f, 1f, 0f, 10f, 0f, 0f, 0f, 1f, 0f)

        assertClose(30f, concat(double, plusTen).apply(10f, 10f, 10f).first)
        assertClose(40f, concat(plusTen, double).apply(10f, 10f, 10f).first)
    }
}

class PictureTransformTest {
    @Test
    fun fillCoversTheSpaceAndCutsTheSides() {
        val transform = pictureTransform(720, 1280, Size(1080f, 2340f), PictureFit.Fill)

        assertClose(1.828125f, transform.scale, 0.0001f)
        assertClose((1080f - 720f * 1.828125f) / 2f, transform.offsetX, 0.01f)
        assertClose(0f, transform.offsetY, 0.01f)
    }

    @Test
    fun fitShowsAllOfThePicture() {
        val transform = pictureTransform(720, 1280, Size(1080f, 2340f), PictureFit.Fit)

        assertClose(1.5f, transform.scale, 0.0001f)
        assertClose(0f, transform.offsetX, 0.01f)
        assertClose((2340f - 1280f * 1.5f) / 2f, transform.offsetY, 0.01f)
    }

    @Test
    fun anEmptySpaceLeavesThePictureAlone() {
        assertEquals(PictureTransform(1f, 0f, 0f), pictureTransform(720, 1280, Size.Zero, PictureFit.Fill))
    }
}

class BitmapPictureTest {
    @Test
    fun onlyABitmapPictureCanBeDrawn() {
        val stranger = object : Picture {
            override val width = 1
            override val height = 1
        }

        assertFailsWith<IllegalStateException> { stranger.asImageBitmap() }
    }

    @Test
    fun aColourKeepsItsChannels() {
        assertEquals(Color(red = 0xC8, green = 0x10, blue = 0x2E, alpha = 0x80), Argb(0x80C8102EL).toColor())
    }
}
