package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.FeaturePoint
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.effect.MarkSpec
import com.fanguskdev.bakiface.domain.face.FaceGeometry
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.lerp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val SAMPLES_PER_SEGMENT = 6
private const val MAX_POWER = 1.6f
private const val DEGREES_PER_RADIAN = 180f / PI.toFloat()

// The colours of the skin marks: dark enough to read on any skin when they multiply, pale when they light it up.
private val SCAR_SHADOW = Argb(0xFF6A2A2AL)
private val SCAR_TISSUE = Argb(0xFFE8A6A0L)
private val SCAR_CREASE = Argb(0xFF7A3434L)
private val STITCH = Argb(0xFF2E1A1AL)
private val VEIN = Argb(0xFF4E3A55L)
private val VEIN_SHADOW = Argb(0xFF5A2E26L)
private val HIGHLIGHT = Argb(0xFFFFFFFFL)
private val WRINKLE = Argb(0xFF5E4030L)

/**
 * Turns the [MarkSpec]s of an effect into [SkinMark]s on one face (ADR 0005): each path is resolved from the features,
 * rounded with a Catmull-Rom spline, carried through the [field] so it follows the warped skin and sized in pixels of
 * the picture. Facial hair is scattered with a seed fixed per mark, so the strands stay put from frame to frame.
 */
internal class MarkComposer(private val geometry: FaceGeometry, private val field: WarpField, private val strength: Float) {
    private val unit = geometry.axes.unit
    private val angleDegrees = atan2(geometry.axes.xAxis.y, geometry.axes.xAxis.x) * DEGREES_PER_RADIAN

    fun compose(specs: List<MarkSpec>): List<SkinMark> = specs.flatMapIndexed { index, spec ->
        val power = (strength * (1f + spec.mouthOpenBoost * geometry.mouthOpenness)).coerceIn(0f, MAX_POWER)
        if (power <= 0f) return@flatMapIndexed emptyList()
        val mirrors = if (spec.mirrored) listOf(false, true) else listOf(false)
        mirrors.flatMap { mirror -> marksOf(spec, mirror, power, seed = index * 2 + if (mirror) 1 else 0) }
    }

    private fun marksOf(
        spec: MarkSpec,
        mirror: Boolean,
        power: Float,
        seed: Int,
    ): List<SkinMark> = when (spec) {
        is MarkSpec.Scar -> scar(spec, mirror, power)
        is MarkSpec.Vein -> vein(spec, mirror, power)
        is MarkSpec.Wrinkle -> wrinkle(spec, mirror, power)
        is MarkSpec.Shade -> listOf(shade(spec, mirror, power))
        is MarkSpec.Hair -> hair(spec, mirror, power, seed)
    }

    /**
     * A healed cut: a soft darker halo where the skin pulls, the raised pinker tissue, the light on its upper edge and the
     * thin crease along the middle.
     */
    private fun scar(spec: MarkSpec.Scar, mirror: Boolean, power: Float): List<SkinMark> {
        val path = smoothPath(spec.path, mirror)
        if (path.size < 2) return emptyList()
        val image = path.map(field::warpToImage).toImmutableList()
        val lit = offsetPath(path, spec.width * -0.3f).map(field::warpToImage).toImmutableList()
        val marks = mutableListOf<SkinMark>(
            SkinMark.Line(image, spec.width * 2.2f * unit, SCAR_SHADOW, 0.22f * power, MarkBlend.Multiply),
            SkinMark.Line(image, spec.width * unit, SCAR_TISSUE, 0.5f * power, MarkBlend.Normal),
            SkinMark.Line(lit, spec.width * 0.35f * unit, HIGHLIGHT, 0.28f * power, MarkBlend.Screen),
            SkinMark.Line(image, spec.width * 0.2f * unit, SCAR_CREASE, 0.4f * power, MarkBlend.Multiply),
        )
        if (spec.stitches) marks += stitches(path, spec.width, power)
        return marks
    }

    private fun stitches(path: List<Point>, width: Float, power: Float): List<SkinMark> {
        val spacing = width * 3.2f
        val halfLength = width * 1.5f
        val stitches = mutableListOf<SkinMark>()
        var travelled = spacing / 2f
        var walked = 0f
        for (index in 0 until path.size - 1) {
            val start = path[index]
            val segment = path[index + 1] - start
            val length = segment.length
            if (length == 0f) continue
            val direction = segment * (1f / length)
            val normal = Point(-direction.y, direction.x)
            while (travelled <= walked + length) {
                val center = start + direction * (travelled - walked)
                val ends =
                    persistentListOf(field.warpToImage(center + normal * halfLength), field.warpToImage(center - normal * halfLength))
                stitches += SkinMark.Line(ends, width * 0.28f * unit, STITCH, 0.6f * power, MarkBlend.Multiply)
                travelled += spacing
            }
            walked += length
        }
        return stitches
    }

    /**
     * A vein under the skin, not a line on it: a wide soft shadow around it, the darker body of the vein and, on its
     * upper edge, the light that catches the bulge.
     */
    private fun vein(spec: MarkSpec.Vein, mirror: Boolean, power: Float): List<SkinMark> {
        val path = smoothPath(spec.path, mirror)
        if (path.size < 2) return emptyList()
        val image = path.map(field::warpToImage).toImmutableList()
        val lit = offsetPath(path, spec.width * -0.32f).map(field::warpToImage).toImmutableList()
        return listOf(
            SkinMark.Line(image, spec.width * 2.4f * unit, VEIN_SHADOW, 0.12f * power, MarkBlend.Multiply),
            SkinMark.Line(image, spec.width * 1.35f * unit, VEIN_SHADOW, 0.18f * power, MarkBlend.Multiply),
            SkinMark.Line(image, spec.width * 0.7f * unit, VEIN, 0.28f * power, MarkBlend.Multiply),
            SkinMark.Line(lit, spec.width * 0.32f * unit, HIGHLIGHT, 0.24f * power, MarkBlend.Screen),
        )
    }

    private fun wrinkle(spec: MarkSpec.Wrinkle, mirror: Boolean, power: Float): List<SkinMark> {
        val path = smoothPath(spec.path, mirror)
        if (path.size < 2) return emptyList()
        val depth = spec.depth.coerceIn(0f, 1f)
        val lit = offsetPath(path, 0.014f)
        return listOf(
            SkinMark.Line(
                path.map(field::warpToImage).toImmutableList(),
                0.012f * unit * (0.6f + depth * 0.6f),
                WRINKLE,
                (0.18f + 0.25f * depth) * power,
                MarkBlend.Multiply,
            ),
            SkinMark.Line(lit.map(field::warpToImage).toImmutableList(), 0.01f * unit, HIGHLIGHT, 0.1f * depth * power, MarkBlend.Screen),
        )
    }

    private fun shade(spec: MarkSpec.Shade, mirror: Boolean, power: Float): SkinMark = SkinMark.Spot(
        center = field.warpToImage(geometry.resolve(spec.at, mirror)),
        radiusX = spec.radiusX * unit,
        radiusY = spec.radiusY * unit,
        angleDegrees = angleDegrees,
        color = spec.color,
        alpha = (spec.alpha * power).coerceIn(0f, 1f),
        blend = spec.blend,
    )

    private fun hair(
        spec: MarkSpec.Hair,
        mirror: Boolean,
        power: Float,
        seed: Int,
    ): List<SkinMark> {
        val from = geometry.resolve(spec.from, mirror)
        val to = geometry.resolve(spec.to, mirror)
        val side = if (mirror) spec.from.side.opposite else spec.from.side
        val along = (to - from).normalized()
        val across = Point(-along.y, along.x)
        val random = Random(seed)
        return List(spec.count.coerceAtLeast(0)) {
            val root = lerp(from, to, random.nextFloat()) + across * ((random.nextFloat() - 0.5f) * spec.length * 0.3f)
            val angle = (spec.angleDegrees + (random.nextFloat() - 0.5f) * spec.spreadDegrees) / DEGREES_PER_RADIAN
            val direction = Point(side.sign * sin(angle), cos(angle))
            val curl = Point(-direction.y, direction.x) * (spec.length * 0.08f * (random.nextFloat() - 0.5f))
            val length = spec.length * (0.75f + 0.5f * random.nextFloat())
            val strand = persistentListOf(
                field.warpToImage(root),
                field.warpToImage(root + direction * (length * 0.55f) + curl),
                field.warpToImage(root + direction * length),
            )
            val alpha = (power * (0.55f + 0.4f * random.nextFloat())).coerceIn(0f, 1f)
            SkinMark.Line(
                strand,
                spec.width * unit * (0.6f + 0.8f * random.nextFloat()),
                spec.color.shaded(
                    0.82f + 0.3f * random.nextFloat(),
                ),
                alpha,
                MarkBlend.Normal,
            )
        }
    }

    /** The resolved path, rounded through its points with a Catmull-Rom spline. */
    private fun smoothPath(points: ImmutableList<FeaturePoint>, mirror: Boolean): List<Point> {
        val anchors = points.map { geometry.resolve(it, mirror) }
        if (anchors.size < 3) return anchors
        val samples = mutableListOf<Point>()
        for (index in 0 until anchors.size - 1) {
            val before = anchors[(index - 1).coerceAtLeast(0)]
            val start = anchors[index]
            val end = anchors[index + 1]
            val after = anchors[(index + 2).coerceAtMost(anchors.size - 1)]
            for (step in 0 until SAMPLES_PER_SEGMENT) {
                samples += catmullRom(before, start, end, after, step / SAMPLES_PER_SEGMENT.toFloat())
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

    /** The path moved sideways by [distance] (units of the face), towards the chin for a positive one. */
    private fun offsetPath(path: List<Point>, distance: Float): List<Point> = path.mapIndexed { index, point ->
        val before = path[(index - 1).coerceAtLeast(0)]
        val after = path[(index + 1).coerceAtMost(path.size - 1)]
        val tangent = (after - before).normalized()
        val normal = Point(-tangent.y, tangent.x).let { if (it.y < 0f) it * -1f else it }
        point + normal * distance
    }
}

/** The colour with its red, green and blue multiplied by [factor] (below 1 darker, above 1 lighter), alpha kept. */
private fun Argb.shaded(factor: Float): Argb {
    fun channel(value: Int): Long = (value * factor).toInt().coerceIn(0, 255).toLong()
    return Argb((alpha.toLong() shl 24) or (channel(red) shl 16) or (channel(green) shl 8) or channel(blue))
}

/** The shared empty list of marks, for an effect that adds nothing to the skin. */
internal val NO_MARKS: ImmutableList<SkinMark> = persistentListOf()
