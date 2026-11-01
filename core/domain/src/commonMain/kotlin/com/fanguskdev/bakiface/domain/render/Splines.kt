package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.geometry.Point

/**
 * The points of a Catmull-Rom spline through [anchors], [samplesPerSegment] of them on each stretch between two
 * anchors and the last anchor at the end. Fewer than three anchors are returned as they are.
 */
internal fun smoothThrough(anchors: List<Point>, samplesPerSegment: Int): List<Point> {
    if (anchors.size < 3) return anchors
    val samples = ArrayList<Point>((anchors.size - 1) * samplesPerSegment + 1)
    for (index in 0 until anchors.size - 1) {
        val before = anchors[(index - 1).coerceAtLeast(0)]
        val start = anchors[index]
        val end = anchors[index + 1]
        val after = anchors[(index + 2).coerceAtMost(anchors.size - 1)]
        for (step in 0 until samplesPerSegment) {
            samples += catmullRom(before, start, end, after, step / samplesPerSegment.toFloat())
        }
    }
    samples += anchors.last()
    return samples
}

private fun catmullRom(
    p0: Point,
    p1: Point,
    p2: Point,
    p3: Point,
    t: Float,
): Point {
    val t2 = t * t
    val t3 = t2 * t
    fun axis(
        a: Float,
        b: Float,
        c: Float,
        d: Float,
    ): Float = 0.5f * (2f * b + (-a + c) * t + (2f * a - 5f * b + 4f * c - d) * t2 + (-a + 3f * b - 3f * c + d) * t3)
    return Point(axis(p0.x, p1.x, p2.x, p3.x), axis(p0.y, p1.y, p2.y, p3.y))
}
