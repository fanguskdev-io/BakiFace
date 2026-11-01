package com.fanguskdev.bakiface.domain.face

import kotlin.math.PI
import kotlin.math.abs

private const val NANOS_PER_SECOND = 1_000_000_000f

/**
 * The 1€ filter of Casiez, Roussel and Vogel (CHI 2012), on many values at once that share their clock: it smooths a
 * lot when the values barely move (the detector's trembling) and little when they move fast (the head turning), so the
 * effect neither shakes nor drags behind the face.
 *
 * [minCutoff] is the cut-off frequency at rest, in hertz; [beta] how much it rises per unit of speed, measured in
 * units of `scale` per second (the caller passes the size of the face, so the filter behaves the same near and far);
 * [derivativeCutoff] smooths the speed itself.
 */
class OneEuroFilter(private val minCutoff: Float = 1.0f, private val beta: Float = 1.5f, private val derivativeCutoff: Float = 1.0f) {
    private var previous: FloatArray? = null
    private var previousSpeed: FloatArray? = null
    private var previousNanos = 0L

    /** Forgets the past: the next values are taken as they come. */
    fun reset() {
        previous = null
        previousSpeed = null
    }

    /** The smoothed version of [values], taken at [timestampNanos]. A change in how many values there are resets. */
    fun filter(values: FloatArray, timestampNanos: Long, scale: Float): FloatArray {
        val last = previous
        val lastSpeed = previousSpeed
        if (last == null || lastSpeed == null || last.size != values.size) {
            return start(values, timestampNanos)
        }
        val elapsed = (timestampNanos - previousNanos) / NANOS_PER_SECOND
        if (elapsed <= 0f) return last.copyOf()
        val speedAlpha = alpha(derivativeCutoff, elapsed)
        val result = FloatArray(values.size)
        val speeds = FloatArray(values.size)
        val safeScale = if (scale > 0f) scale else 1f
        for (index in values.indices) {
            val rawSpeed = (values[index] - last[index]) / elapsed
            val speed = lastSpeed[index] + speedAlpha * (rawSpeed - lastSpeed[index])
            val cutoff = minCutoff + beta * abs(speed) / safeScale
            result[index] = last[index] + alpha(cutoff, elapsed) * (values[index] - last[index])
            speeds[index] = speed
        }
        previous = result
        previousSpeed = speeds
        previousNanos = timestampNanos
        return result.copyOf()
    }

    private fun start(values: FloatArray, timestampNanos: Long): FloatArray {
        previous = values.copyOf()
        previousSpeed = FloatArray(values.size)
        previousNanos = timestampNanos
        return values.copyOf()
    }

    private fun alpha(cutoff: Float, elapsed: Float): Float {
        val tau = 1f / (2f * PI.toFloat() * cutoff)
        return 1f / (1f + tau / elapsed)
    }
}
