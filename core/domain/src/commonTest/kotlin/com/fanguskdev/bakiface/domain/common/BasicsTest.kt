package com.fanguskdev.bakiface.domain.common

import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.effect.Sides
import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.centroid
import com.fanguskdev.bakiface.domain.geometry.lerp
import com.fanguskdev.bakiface.domain.picture.Region
import com.fanguskdev.bakiface.domain.picture.visibleRegion
import com.fanguskdev.bakiface.domain.render.WarpMesh
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppResultTest {
    private val success: AppResult<Int, String> = AppResult.Success(2)
    private val failure: AppResult<Int, String> = AppResult.Failure("no")

    @Test
    fun aSuccessCarriesItsValueThroughTheChain() {
        assertEquals(AppResult.Success(4), success.map { it * 2 })
        assertEquals(AppResult.Success("2!"), success.flatMap { AppResult.Success("$it!") })
        assertEquals(2, success.getOrNull())
        assertNull(success.errorOrNull())
    }

    @Test
    fun aFailureStopsTheChain() {
        assertEquals(failure, failure.map { it * 2 })
        assertEquals(failure, failure.flatMap { AppResult.Success(it) })
        assertNull(failure.getOrNull())
        assertEquals("no", failure.errorOrNull())
    }
}

class GeometryBasicsTest {
    @Test
    fun pointsAddScaleAndMeasure() {
        assertEquals(Point(4f, 6f), Point(1f, 2f) + Point(3f, 4f))
        assertEquals(Point(-2f, -2f), Point(1f, 2f) - Point(3f, 4f))
        assertEquals(Point(2f, 4f), Point(1f, 2f) * 2f)
        assertEquals(11f, Point(1f, 2f) dot Point(3f, 4f))
        assertEquals(5f, Point(3f, 4f).length)
        assertEquals(Point(0.6f, 0.8f), Point(3f, 4f).normalized())
        assertEquals(Point.ZERO, Point.ZERO.normalized())
    }

    @Test
    fun theCentreOfNothingIsNothing() {
        assertNull(emptyList<Point>().centroid())
        assertEquals(Point(1f, 1f), listOf(Point(0f, 0f), Point(2f, 2f)).centroid())
        assertEquals(Point(5f, 10f), lerp(Point(0f, 0f), Point(10f, 20f), 0.5f))
    }

    @Test
    fun aColourHasItsChannels() {
        val colour = Argb(0x80C8102EL)

        assertEquals(0x80, colour.alpha)
        assertEquals(0xC8, colour.red)
        assertEquals(0x10, colour.green)
        assertEquals(0x2E, colour.blue)
    }

    @Test
    fun sidesAndLensesHaveTheirOpposites() {
        assertEquals(Side.Right, Side.Left.opposite)
        assertEquals(Side.Left, Side.Right.opposite)
        assertTrue(Sides.Both.includes(Side.Left))
        assertFalse(Sides.Right.includes(Side.Left))
        assertTrue(Sides.Left.includes(Side.Left))
        assertEquals(Lens.Back, Lens.Front.other)
        assertEquals(Lens.Front, Lens.Back.other)
    }

    @Test
    fun landmarksMoveTogether() {
        val face = syntheticFace()

        val moved = face.map { it + Point(10f, 0f) }

        assertEquals(face.pointCount, moved.pointCount)
        assertEquals(face.leftEye.first() + Point(10f, 0f), moved.leftEye.first())
        assertEquals(0, FaceLandmarks().pointCount)
    }

    @Test
    fun aMeshChecksItsShape() {
        assertFailsWith<IllegalArgumentException> { WarpMesh(0, 1, 1, 1, FloatArray(0)) }
        assertFailsWith<IllegalArgumentException> { WarpMesh(1, 1, 1, 1, FloatArray(3)) }
    }
}

class VisibleRegionTest {
    @Test
    fun aFrameWiderThanTheScreenLosesItsSides() {
        assertEquals(Region(65, 0, 590, 1280), visibleRegion(720, 1280, 1080, 2340))
    }

    @Test
    fun aFrameTallerThanTheScreenLosesItsTopAndBottom() {
        assertEquals(Region(0, 280, 720, 720), visibleRegion(720, 1280, 1000, 1000))
    }

    @Test
    fun theSameShapeLosesNothing() {
        assertEquals(Region(0, 0, 720, 1280), visibleRegion(720, 1280, 360, 640))
    }

    @Test
    fun withoutAScreenTheWholeFrameIsVisible() {
        assertEquals(Region(0, 0, 720, 1280), visibleRegion(720, 1280, 0, 0))
        assertFailsWith<IllegalArgumentException> { visibleRegion(0, 1280, 10, 10) }
    }
}
