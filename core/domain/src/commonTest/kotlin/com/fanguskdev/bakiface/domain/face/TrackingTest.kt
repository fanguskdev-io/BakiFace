package com.fanguskdev.bakiface.domain.face

import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.testing.assertNear
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val FRAME = 33_000_000L

class OneEuroFilterTest {
    @Test
    fun theFirstValuesPassUntouched() {
        val filter = OneEuroFilter()

        val result = filter.filter(floatArrayOf(1f, 2f), timestampNanos = 0L, scale = 1f)

        assertEquals(listOf(1f, 2f), result.toList())
    }

    @Test
    fun aTremblingValueIsCalmed() {
        val filter = OneEuroFilter()
        filter.filter(floatArrayOf(100f), 0L, scale = 120f)
        var worst = 0f
        repeat(60) { frame ->
            val noisy = if (frame % 2 == 0) 102f else 98f
            val output = filter.filter(floatArrayOf(noisy), (frame + 1) * FRAME, scale = 120f)[0]
            worst = maxOf(worst, abs(output - 100f))
        }
        assertTrue(worst < 1.5f, "the trembling of ±2 px stays under 1.5 px, was $worst")
    }

    @Test
    fun aFastMovementIsFollowedClosely() {
        val filter = OneEuroFilter()
        filter.filter(floatArrayOf(0f), 0L, scale = 120f)
        var output = 0f
        repeat(10) { frame -> output = filter.filter(floatArrayOf((frame + 1) * 30f), (frame + 1) * FRAME, scale = 120f)[0] }

        assertTrue(300f - output < 60f, "a head moving 900 px/s is less than 60 px behind, was ${300f - output}")
    }

    @Test
    fun aRepeatedTimestampKeepsThePreviousValues() {
        val filter = OneEuroFilter()
        filter.filter(floatArrayOf(5f), FRAME, scale = 1f)

        assertEquals(5f, filter.filter(floatArrayOf(50f), FRAME, scale = 1f)[0])
    }

    @Test
    fun anotherNumberOfValuesStartsAgain() {
        val filter = OneEuroFilter()
        filter.filter(floatArrayOf(5f), 0L, scale = 1f)

        assertEquals(listOf(7f, 8f), filter.filter(floatArrayOf(7f, 8f), FRAME, scale = 1f).toList())
    }

    @Test
    fun resetForgetsThePast() {
        val filter = OneEuroFilter()
        filter.filter(floatArrayOf(5f), 0L, scale = 1f)
        filter.reset()

        assertEquals(40f, filter.filter(floatArrayOf(40f), FRAME, scale = 1f)[0])
    }
}

class FaceTrackerTest {
    @Test
    fun aFaceFadesInOverTheFirstFrames() {
        val tracker = FaceTracker()

        val first = assertNotNull(tracker.track(syntheticFace(), 0L))
        val second = assertNotNull(tracker.track(syntheticFace(), FRAME))
        repeat(10) { tracker.track(syntheticFace(), (it + 2) * FRAME) }
        val settled = assertNotNull(tracker.track(syntheticFace(), 12 * FRAME))

        assertEquals(0f, first.presence)
        assertTrue(second.presence in 0.1f..0.3f, "after one frame the face is partly there, was ${second.presence}")
        assertEquals(1f, settled.presence)
    }

    @Test
    fun aFaceLostForAFrameKeepsItsPlace() {
        val tracker = FaceTracker()
        repeat(10) { tracker.track(syntheticFace(), it * FRAME) }

        val lost = assertNotNull(tracker.track(null, 10 * FRAME))

        assertTrue(lost.presence in 0.8f..0.9f, "one missing frame only dims it, was ${lost.presence}")
        assertNear(syntheticFace().leftEye.first(), lost.landmarks.leftEye.first(), tolerance = 0.5f)
    }

    @Test
    fun aFaceLostForLongerIsForgotten() {
        val tracker = FaceTracker()
        repeat(10) { tracker.track(syntheticFace(), it * FRAME) }

        repeat(8) { tracker.track(null, (10 + it) * FRAME) }
        val gone = tracker.track(null, 20 * FRAME)
        val back = assertNotNull(tracker.track(syntheticFace(center = Point(100f, 100f)), 21 * FRAME))

        assertNull(gone)
        assertEquals(0f, back.presence, "a face found again starts from nothing")
        assertNear(syntheticFace(center = Point(100f, 100f)).leftEye.first(), back.landmarks.leftEye.first(), tolerance = 0.01f)
    }

    @Test
    fun withoutAnyFaceThereIsNothingToTrack() {
        assertNull(FaceTracker().track(null, 0L))
        assertNull(FaceTracker().track(FaceLandmarks(), 0L))
    }

    @Test
    fun theTremblingOfTheDetectorIsSmoothed() {
        val tracker = FaceTracker()
        repeat(30) { frame ->
            val jitter = if (frame % 2 == 0) 2f else -2f
            tracker.track(syntheticFace(center = Point(360f + jitter, 640f)), frame * FRAME)
        }
        val calm = assertNotNull(tracker.track(syntheticFace(center = Point(362f, 640f)), 30 * FRAME))

        assertNear(syntheticFace().leftEye.first(), calm.landmarks.leftEye.first(), tolerance = 1.5f)
    }
}
