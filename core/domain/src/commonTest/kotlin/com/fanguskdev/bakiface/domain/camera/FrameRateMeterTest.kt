package com.fanguskdev.bakiface.domain.camera

import com.fanguskdev.bakiface.testing.assertNear
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val MILLIS = 1_000_000L

class FrameRateMeterTest {
    @Test
    fun oneFrameIsNotARateYet() {
        assertNull(FrameRateMeter().onFrame(0L))
    }

    @Test
    fun aSteadyCameraGivesItsOwnRate() {
        val meter = FrameRateMeter()
        var rate: Float? = null
        for (frame in 0..90) rate = meter.onFrame(frame * 33 * MILLIS)
        assertNear(30.3f, assertNotNull(rate), tolerance = 0.1f)
    }

    @Test
    fun onlyTheLastSecondCounts() {
        val meter = FrameRateMeter()
        for (frame in 0..30) meter.onFrame(frame * 33 * MILLIS)
        var rate: Float? = null
        for (frame in 1..20) rate = meter.onFrame(990 * MILLIS + frame * 100 * MILLIS)
        assertNear(10f, assertNotNull(rate), tolerance = 0.1f)
    }

    @Test
    fun aClockThatGoesBackStartsAgain() {
        val meter = FrameRateMeter()
        meter.onFrame(500 * MILLIS)
        meter.onFrame(533 * MILLIS)
        assertNull(meter.onFrame(100 * MILLIS), "a restarted camera clock is a new session")
        assertNear(25f, assertNotNull(meter.onFrame(140 * MILLIS)), tolerance = 0.1f)
    }
}
