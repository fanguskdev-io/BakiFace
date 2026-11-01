package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.FoldRegion
import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.face.FaceGeometry
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val SAMPLES_PER_STRETCH = 5
private const val WAVE_SAMPLES = 14
private const val DEGREES_PER_RADIAN = 180f / PI.toFloat()

/**
 * Where the folds of a [FoldRegion] run on one face (ADR 0011), in the face's frame: Itagaki's way of drawing a face
 * where every muscle shows, laid out from the features. The scatter of each fold (its bend, its reach, a break in a long
 * wave) comes from a seed fixed per fold, never from the frame, so the folds stay put while the face moves.
 */
internal object FoldLayout {
    /** The [count] folds of [region] (twice as many in a region with two halves), each a line of points. */
    fun paths(
        geometry: FaceGeometry,
        region: FoldRegion,
        count: Int,
        seed: Int,
    ): List<List<Point>> {
        if (count <= 0) return emptyList()
        val layout = Layout(geometry, seed * 97 + region.ordinal * 13)
        return when (region) {
            FoldRegion.Forehead -> layout.forehead(count)
            FoldRegion.Frown -> layout.frown(count)
            FoldRegion.CrowsFeet -> layout.bothSides { side -> layout.crowsFeet(side, count) }
            FoldRegion.UnderEyes -> layout.bothSides { side -> layout.underEyes(side, count) }
            FoldRegion.Smile -> layout.bothSides { side -> layout.smile(side, count) }
            FoldRegion.Cheeks -> layout.bothSides { side -> layout.cheeks(side, count) }
            FoldRegion.NoseBridge -> layout.bothSides { side -> layout.noseBridge(side, count) }
            FoldRegion.Mouth -> layout.bothSides { side -> layout.mouth(side, count) }
            FoldRegion.Chin -> layout.chin(count)
            FoldRegion.Neck -> layout.bothSides { side -> layout.neck(side, count) }
        }
    }
}

private class Layout(private val g: FaceGeometry, private val seed: Int) {
    fun bothSides(folds: (Side) -> List<List<Point>>): List<List<Point>> = folds(Side.Left) + folds(Side.Right)

    /** Waves across the forehead, from just above the brows up towards the hair, broken now and then like a lifted brush. */
    fun forehead(count: Int): List<List<Point>> {
        val browTop = min(g.leftBrow.middle.y, g.rightBrow.middle.y)
        val low = browTop - 0.13f
        val high = browTop - 0.72f
        val centerX = (g.leftSide.temple.x + g.rightSide.temple.x) / 2f
        val halfWidth = (g.rightSide.temple.x - g.leftSide.temple.x) / 2f * 0.82f
        return (0 until count).flatMap { fold ->
            val random = random(fold)
            val t = (fold + 0.5f) / count
            val y = lerp(low, high, t)
            val reach = halfWidth * (1f - 0.22f * t) * (0.86f + 0.14f * random.nextFloat())
            val waves = 1.1f + 0.9f * random.nextFloat()
            val phase = random.nextFloat() * 2f * PI.toFloat()
            val amplitude = 0.014f + 0.014f * random.nextFloat()
            val wave = (0..WAVE_SAMPLES).map { step ->
                val u = -1f + 2f * step / WAVE_SAMPLES
                Point(centerX + u * reach, y + amplitude * sin(u * PI.toFloat() * waves + phase) - 0.045f * (1f - u * u))
            }
            if (random.nextFloat() < 0.45f) {
                val gap = 5 + random.nextInt(4)
                listOf(wave.subList(0, gap), wave.subList(gap + 2, wave.size))
            } else {
                listOf(wave)
            }
        }
    }

    /** Upright creases between the brows, leaning apart as they rise, and a scowl across the root of the nose. */
    fun frown(count: Int): List<List<Point>> {
        val bottom = (g.leftBrow.inner.y + g.rightBrow.inner.y) / 2f + 0.06f
        val centerX = (g.leftBrow.inner.x + g.rightBrow.inner.x) / 2f
        val creases = (0 until count).map { fold ->
            val random = random(fold)
            val x = centerX + (if (count == 1) 0f else lerp(-0.1f, 0.1f, fold / (count - 1f))) + jitter(random, 0.015f)
            val rise = 0.24f + 0.08f * random.nextFloat()
            curve(Point(x, bottom), Point(x * 1.25f + jitter(random, 0.01f), bottom - rise * 0.5f), Point(x * 1.6f, bottom - rise))
        }
        if (count < 3) return creases
        val root = bottom + 0.1f
        return creases +
            listOf(curve(Point(centerX - 0.13f, root + 0.02f), Point(centerX, root - 0.015f), Point(centerX + 0.13f, root + 0.02f)))
    }

    /** Lines fanning out from the outer corner of the eye, from up towards the temple to down towards the cheekbone. */
    fun crowsFeet(side: Side, count: Int): List<List<Point>> {
        val corner = eye(side).outer
        return (0 until count).map { fold ->
            val random = random(fold, side)
            val degrees = (if (count == 1) 0f else lerp(-38f, 42f, fold / (count - 1f))) + jitter(random, 6f)
            val angle = degrees / DEGREES_PER_RADIAN
            val direction = Point(side.sign * cos(angle), sin(angle))
            val bend = Point(-direction.y, direction.x) * (0.015f * side.sign)
            val length = 0.13f + 0.06f * random.nextFloat()
            curve(corner + direction * 0.08f, corner + direction * (0.08f + length * 0.5f) + bend, corner + direction * (0.08f + length))
        }
    }

    /** Arcs under the eye, each lower and longer than the one above it. */
    fun underEyes(side: Side, count: Int): List<List<Point>> {
        val eye = eye(side)
        return (0 until count).map { fold ->
            val random = random(fold, side)
            val drop = eye.halfHeight + 0.05f + 0.065f * fold
            curve(
                eye.inner + Point(0f, drop * 0.55f),
                eye.center + Point(jitter(random, 0.02f), drop + 0.02f),
                eye.outer + Point(side.sign * (0.05f + 0.02f * fold), drop * 0.7f - 0.02f),
            )
        }
    }

    /** The brackets from the wing of the nose around the corner of the mouth, each one further out. */
    fun smile(side: Side, count: Int): List<List<Point>> {
        val wing = Point(g.noseBase.x + side.sign * g.noseHalfWidth * 1.05f, g.noseBase.y - 0.06f)
        val corner = if (side == Side.Left) g.mouthLeft else g.mouthRight
        return (0 until count).map { fold ->
            val random = random(fold, side)
            val out = 0.07f + 0.09f * fold + jitter(random, 0.02f)
            val reach = 0.26f + 0.1f * random.nextFloat() + 0.05f * fold
            curve(
                wing + Point(side.sign * (0.02f + 0.04f * fold), -0.03f - 0.04f * fold + jitter(random, 0.02f)),
                Point(corner.x + side.sign * (out + 0.03f), lerp(wing.y, corner.y, 0.5f)),
                Point(corner.x + side.sign * out * 0.95f, corner.y + 0.1f + 0.03f * fold),
                Point(corner.x + side.sign * out * 0.6f, corner.y + reach),
            )
        }
    }

    /** Long curves down the cheek, outside the smile, bowing outwards like muscle under strain. */
    fun cheeks(side: Side, count: Int): List<List<Point>> {
        val faceSide = if (side == Side.Left) g.leftSide else g.rightSide
        return (0 until count).map { fold ->
            val random = random(fold, side)
            val t = (fold + 0.5f) / count
            val top = Point(faceSide.cheekbone.x * lerp(1f, 0.78f, t), faceSide.cheekbone.y + 0.04f + 0.12f * random.nextFloat())
            val bottom = Point(faceSide.jaw.x * lerp(0.9f, 0.68f, t), faceSide.jaw.y - 0.12f + 0.2f * random.nextFloat())
            curve(
                top,
                lerp(top, bottom, 0.3f) + Point(side.sign * (0.05f + 0.03f * random.nextFloat()), 0f),
                lerp(top, bottom, 0.7f) + Point(side.sign * (0.04f + 0.03f * random.nextFloat()), 0f),
                bottom,
            )
        }
    }

    /** Short creases across the bridge of the nose, rising outwards. */
    fun noseBridge(side: Side, count: Int): List<List<Point>> = (0 until count).map { fold ->
        val random = random(fold, side)
        val y = 0.1f + 0.055f * fold
        curve(
            Point(side.sign * 0.05f, y),
            Point(side.sign * 0.12f, y - 0.03f + jitter(random, 0.008f)),
            Point(side.sign * (0.2f + 0.03f * random.nextFloat()), y - 0.06f + 0.02f * fold),
        )
    }

    /** Lines down from the corner of the mouth towards the chin. */
    fun mouth(side: Side, count: Int): List<List<Point>> {
        val corner = if (side == Side.Left) g.mouthLeft else g.mouthRight
        return (0 until count).map { fold ->
            val random = random(fold, side)
            curve(
                corner + Point(side.sign * (0.03f + 0.03f * fold), 0.06f),
                Point(corner.x + side.sign * (0.05f + 0.045f * fold + jitter(random, 0.01f)), lerp(corner.y, g.chin.y, 0.45f)),
                Point(corner.x + side.sign * (0.02f + 0.05f * fold), g.chin.y - 0.14f + 0.02f * fold),
            )
        }
    }

    /** Arcs on the chin, each higher and wider than the last. */
    fun chin(count: Int): List<List<Point>> = (0 until count).map { fold ->
        val random = random(fold)
        val y = g.chin.y - 0.1f - 0.07f * fold
        val halfWidth = 0.16f + 0.04f * fold + jitter(random, 0.02f)
        curve(Point(g.chin.x - halfWidth, y - 0.035f), Point(g.chin.x, y + 0.03f), Point(g.chin.x + halfWidth, y - 0.035f))
    }

    /** The cords of the neck, from under the jaw down towards the hollow of the throat, inside the outline of the neck. */
    fun neck(side: Side, count: Int): List<List<Point>> {
        val faceSide = if (side == Side.Left) g.leftSide else g.rightSide
        return (0 until count).map { fold ->
            val random = random(fold, side)
            val from = Point(faceSide.gonion.x * (0.62f - 0.08f * fold), g.chin.y + 0.02f + 0.05f * fold)
            val to = Point(g.chin.x + side.sign * (0.12f + 0.05f * fold), g.chin.y + 0.7f + 0.1f * random.nextFloat())
            curve(from, lerp(from, to, 0.5f) + Point(side.sign * (0.03f + jitter(random, 0.015f)), 0f), to)
        }
    }

    private fun eye(side: Side) = if (side == Side.Left) g.leftEye else g.rightEye

    private fun curve(vararg anchors: Point): List<Point> = smoothThrough(anchors.toList(), SAMPLES_PER_STRETCH)

    private fun random(fold: Int, side: Side? = null): Random = Random(seed * 1_009 + fold * 31 + (side?.ordinal ?: 2))

    private fun jitter(random: Random, size: Float): Float = (random.nextFloat() - 0.5f) * 2f * size
}
