package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.FoldRegion
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.effect.MarkSpec
import com.fanguskdev.bakiface.domain.face.faceGeometryOf
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.testing.assertNear
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val FACE = assertNotNull(faceGeometryOf(syntheticFace()))
private val PAIRED = setOf(
    FoldRegion.CrowsFeet,
    FoldRegion.UnderEyes,
    FoldRegion.Smile,
    FoldRegion.Cheeks,
    FoldRegion.NoseBridge,
    FoldRegion.Mouth,
    FoldRegion.Neck,
)

/** Whether [point] is inside the closed shape through [outline] (even-odd rule). */
private fun inside(outline: List<Point>, point: Point): Boolean {
    var crossings = 0
    for (index in outline.indices) {
        val a = outline[index]
        val b = outline[(index + 1) % outline.size]
        if ((a.y > point.y) != (b.y > point.y) && point.x < a.x + (point.y - a.y) / (b.y - a.y) * (b.x - a.x)) crossings++
    }
    return crossings % 2 == 1
}

class FoldLayoutTest {
    @Test
    fun everyRegionLaysItsFoldsAndBothHalvesGetThemWhereThereAreTwo() {
        FoldRegion.entries.forEach { region ->
            val paths = FoldLayout.paths(FACE, region, count = 3, seed = 1)
            when (region) {
                FoldRegion.Forehead -> assertTrue(paths.size in 3..6, "a wave breaks in two at most")
                FoldRegion.Frown -> assertEquals(4, paths.size, "three creases and the scowl across the nose")
                in PAIRED -> assertEquals(6, paths.size, "$region has two halves")
                else -> assertEquals(3, paths.size, "$region")
            }
            assertTrue(paths.all { it.size >= 3 }, "$region: every fold is a line")
        }
    }

    @Test
    fun theFoldsStayOnTheFaceAndTheNeck() {
        FoldRegion.entries.forEach { region ->
            FoldLayout.paths(FACE, region, count = 4, seed = 7).flatten().forEach { point ->
                assertTrue(point.x in -1.3f..1.3f && point.y in -1.3f..2.8f, "$region: $point is off the face")
            }
        }
    }

    @Test
    fun eachHalfKeepsItsOwnFolds() {
        val smile = FoldLayout.paths(FACE, FoldRegion.Smile, count = 2, seed = 3)

        assertTrue(smile.take(2).all { path -> path.all { it.x < 0f } }, "the left brackets")
        assertTrue(smile.drop(2).all { path -> path.all { it.x > 0f } }, "the right brackets")
    }

    @Test
    fun theSameSeedLaysTheSameFoldsAndAnotherOneOthers() {
        assertEquals(FoldLayout.paths(FACE, FoldRegion.Forehead, 5, seed = 2), FoldLayout.paths(FACE, FoldRegion.Forehead, 5, seed = 2))
        assertNotEquals(FoldLayout.paths(FACE, FoldRegion.Forehead, 5, seed = 2), FoldLayout.paths(FACE, FoldRegion.Forehead, 5, seed = 3))
    }

    @Test
    fun noFoldsIsNoFolds() {
        assertTrue(FoldLayout.paths(FACE, FoldRegion.Smile, count = 0, seed = 1).isEmpty())
    }
}

class TrendMarksTest {
    private val still = WarpField.build(FACE, emptyList(), 1f)

    private fun compose(vararg specs: MarkSpec, strength: Float = 1f) = MarkComposer(FACE, still, strength).compose(specs.toList())

    @Test
    fun eachFoldIsCarvedInFourStrokesThatEndInAPoint() {
        val marks = compose(MarkSpec.Folds(FoldRegion.Chin, count = 2, depth = 0.8f))

        assertEquals(2 * 4, marks.size)
        val strokes = marks.filterIsInstance<SkinMark.Fill>()
        assertEquals(marks.size, strokes.size)
        assertEquals(
            listOf(MarkBlend.Multiply, MarkBlend.Multiply, MarkBlend.Screen, MarkBlend.Screen),
            strokes.take(4).map { it.blend },
            "the shadow, the crease and the two lights",
        )
        strokes.forEach { stroke ->
            val half = stroke.points.size / 2
            assertNear(stroke.points[half - 1], stroke.points[half], tolerance = 0.001f, message = "the stroke is pointed at its end")
        }
    }

    @Test
    fun noCornerOfAFoldIsLostToARoundingError() {
        // sin(π) rounds below zero as a float, and a negative number to the power 0.7 is NaN: one NaN corner and Skia
        // drew nothing of the whole shape, so not one fold showed on a phone.
        val marks = compose(*FoldRegion.entries.map { MarkSpec.Folds(it, count = 3, depth = 1f) }.toTypedArray())

        marks.filterIsInstance<SkinMark.Fill>().flatMap { it.points }.forEach { assertTrue(it.x.isFinite() && it.y.isFinite(), "$it") }
    }

    @Test
    fun blankEyesCoverBothEyesUnderTheirLids() {
        val marks = compose(MarkSpec.BlankEyes(Argb(0xFFF2EDE4L), alpha = 0.9f))

        val whites = marks.filterIsInstance<SkinMark.Fill>().filter { it.blend == MarkBlend.Normal }
        assertEquals(2, whites.size)
        listOf(FACE.leftEye, FACE.rightEye).zip(whites).forEach { (eye, white) ->
            assertTrue(inside(white.points, FACE.axes.toImage(eye.center)), "the white covers the eye")
            assertEquals(0.9f, white.alpha)
        }
        assertEquals(2 * 2, marks.count { it is SkinMark.Line }, "both lids of both eyes")
    }

    @Test
    fun inkBrowsArePaintedAndThenMultipliedOverEachBrow() {
        val marks = compose(MarkSpec.InkBrows(width = 0.12f, color = Argb(0xFF140D0AL), alpha = 0.8f))

        assertEquals(listOf(MarkBlend.Normal, MarkBlend.Multiply, MarkBlend.Normal, MarkBlend.Multiply), marks.map { it.blend })
        assertTrue(inside((marks[0] as SkinMark.Fill).points, FACE.axes.toImage(FACE.leftBrow.middle)), "over the left brow")
        assertTrue(inside((marks[2] as SkinMark.Fill).points, FACE.axes.toImage(FACE.rightBrow.middle)), "over the right brow")
        assertEquals(0.4f, marks[0].alpha)
    }

    @Test
    fun theMarksOfTheTrendFadeWithTheFace() {
        val marks = compose(MarkSpec.BlankEyes(Argb(0xFFF2EDE4L), alpha = 0.9f), strength = 0.5f)

        assertNear(0.45f, marks.first().alpha)
    }
}
