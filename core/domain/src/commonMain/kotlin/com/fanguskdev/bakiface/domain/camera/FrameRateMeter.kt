package com.fanguskdev.bakiface.domain.camera

private const val NANOS_PER_SECOND = 1_000_000_000f
private const val DEFAULT_WINDOW_NANOS = 1_000_000_000L

/**
 * How many camera frames per second make it through detection and the effect (R-03), counted over the last
 * [windowNanos] of the camera's own clock. A frame the camera drops because the previous one was still being worked on
 * is never counted, so this is the rate the person sees: the number to read on a real phone (ADR 0005 asks for 24).
 */
class FrameRateMeter(private val windowNanos: Long = DEFAULT_WINDOW_NANOS) {
    private val stamps = ArrayDeque<Long>()

    /** Counts the frame taken at [timestampNanos] and returns the rate over the window, or `null` until it can tell. */
    fun onFrame(timestampNanos: Long): Float? {
        if (stamps.isNotEmpty() && timestampNanos <= stamps.last()) stamps.clear()
        stamps.addLast(timestampNanos)
        while (timestampNanos - stamps.first() > windowNanos) stamps.removeFirst()
        if (stamps.size < 2) return null
        return (stamps.size - 1) * NANOS_PER_SECOND / (timestampNanos - stamps.first())
    }
}
