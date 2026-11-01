package com.fanguskdev.bakiface.domain.geometry

import kotlin.math.sqrt

/** A point, or a vector, in a picture: pixels from its top-left corner, with y growing downwards. */
data class Point(val x: Float, val y: Float) {
    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)

    operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)

    operator fun times(factor: Float): Point = Point(x * factor, y * factor)

    infix fun dot(other: Point): Float = x * other.x + y * other.y

    val length: Float get() = sqrt(x * x + y * y)

    /** The same direction with length one; the zero vector stays zero. */
    fun normalized(): Point {
        val size = length
        return if (size == 0f) ZERO else Point(x / size, y / size)
    }

    companion object {
        val ZERO = Point(0f, 0f)
    }
}

/** The average of the points, or `null` when there are none. */
fun Collection<Point>.centroid(): Point? {
    if (isEmpty()) return null
    var sumX = 0f
    var sumY = 0f
    for (point in this) {
        sumX += point.x
        sumY += point.y
    }
    return Point(sumX / size, sumY / size)
}

/** The value a fraction [t] of the way from [start] to [end]. */
fun lerp(start: Float, end: Float, t: Float): Float = start + (end - start) * t

/** The point a fraction [t] of the way from [start] to [end]. */
fun lerp(start: Point, end: Point, t: Float): Point = Point(lerp(start.x, end.x, t), lerp(start.y, end.y, t))
