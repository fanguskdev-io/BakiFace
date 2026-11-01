package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.FeaturePoint
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.effect.MarkSpec
import com.fanguskdev.bakiface.domain.face.EyeShape
import com.fanguskdev.bakiface.domain.face.FaceGeometry
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.geometry.lerp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val SAMPLES_PER_SEGMENT = 6
private const val FOLD_MIN_WIDTH = 0.009f
private const val FOLD_DEPTH_WIDTH = 0.014f
private const val LIGHT_SIDEWAYS = 0.6f
private const val BLANK_EYE_UPPER = 1.35f
private const val BLANK_EYE_LOWER = 1.05f
private const val BLANK_EYE_SHADE_LINE = 0.35f
private const val TAPER_POWER = 0.7f
private const val EYE_ARC_STEPS = 10
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
private val FOLD_CREASE = Argb(0xFF2E160EL)
private val FOLD_SHADOW = Argb(0xFF5A3424L)
private val LID_SHADOW = Argb(0xFF3A2620L)
private val EYE_SHADE = Argb(0xFF8C8580L)

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
        is MarkSpec.Folds -> folds(spec, power, seed)
        is MarkSpec.BlankEyes -> blankEyes(spec, power)
        is MarkSpec.InkBrows -> inkBrows(spec, power)
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
            SkinMark.Line(lit, spec.width * 0.32f * unit, HIGHLIGHT, 0.12f * power, MarkBlend.Screen),
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

    /**
     * The folds of a region (ADR 0011), each carved in four strokes that taper to a point at both ends: a soft shadow
     * falling away from the light, the thin dark crease, the broad light on the swell of skin beside it and the bright
     * lip of the fold. The light comes from above and from outside the face, the same for every fold, so a crease never
     * changes its lit side halfway along.
     */
    private fun folds(spec: MarkSpec.Folds, power: Float, seed: Int): List<SkinMark> {
        val depth = spec.depth.coerceIn(0f, 1f)
        val width = FOLD_MIN_WIDTH + FOLD_DEPTH_WIDTH * depth
        return FoldLayout.paths(geometry, spec.region, spec.count, seed).filter { it.size >= 2 }.flatMap { path ->
            val outwards = if (path.sumOf { it.x.toDouble() } < 0.0) -1f else 1f
            val light = Point(outwards * LIGHT_SIDEWAYS, -1f).normalized()
            listOf(
                ribbon(
                    offsetTowards(path, light, -width * 1.2f),
                    width * 3.6f,
                    FOLD_SHADOW,
                    (0.09f + 0.1f * depth) * power,
                    MarkBlend.Multiply,
                ),
                ribbon(path, width, FOLD_CREASE, (0.38f + 0.37f * depth) * power, MarkBlend.Multiply),
                ribbon(
                    offsetTowards(path, light, width * 2.2f),
                    width * 3.2f,
                    HIGHLIGHT,
                    (0.05f + 0.08f * depth) * power,
                    MarkBlend.Screen,
                ),
                ribbon(
                    offsetTowards(path, light, width * 1.0f),
                    width * 0.8f,
                    HIGHLIGHT,
                    (0.06f + 0.09f * depth) * power,
                    MarkBlend.Screen,
                ),
            )
        }
    }

    /** The path moved sideways by [distance] (units of the face), towards the side [light] comes from for a positive one. */
    private fun offsetTowards(path: List<Point>, light: Point, distance: Float): List<Point> = path.mapIndexed { index, point ->
        val tangent = (path[(index + 1).coerceAtMost(path.size - 1)] - path[(index - 1).coerceAtLeast(0)]).normalized()
        val normal = Point(-tangent.y, tangent.x).let { if ((it dot light) < 0f) it * -1f else it }
        point + normal * distance
    }

    /**
     * Each eye filled from lid to lid, rolled back: the white, the shade the upper lid casts over its top half and the
     * two lids drawn over it, so it still sits in its socket.
     */
    private fun blankEyes(spec: MarkSpec.BlankEyes, power: Float): List<SkinMark> =
        listOf(geometry.leftEye, geometry.rightEye).flatMap { eye ->
            val upper = eyeArc(eye, lift = BLANK_EYE_UPPER)
            val lower = eyeArc(eye, lift = -BLANK_EYE_LOWER)
            val middle = eyeArc(eye, lift = BLANK_EYE_SHADE_LINE)
            fun image(points: List<Point>) = points.map(field::warpToImage).toImmutableList()
            listOf(
                SkinMark.Fill(image(upper + lower.asReversed()), spec.color, (spec.alpha * power).coerceIn(0f, 1f), MarkBlend.Normal),
                SkinMark.Fill(image(upper + middle.asReversed()), EYE_SHADE, (0.42f * power).coerceIn(0f, 1f), MarkBlend.Multiply),
                SkinMark.Line(image(upper), 0.045f * unit, LID_SHADOW, (0.6f * power).coerceIn(0f, 1f), MarkBlend.Multiply),
                SkinMark.Line(image(lower), 0.022f * unit, LID_SHADOW, (0.35f * power).coerceIn(0f, 1f), MarkBlend.Multiply),
            )
        }

    /**
     * Each brow as one stroke of ink, thickest at its inner end and thinning to a point outwards: painted at half its
     * strength and multiplied at the rest, so the hairs under it still show through the ink.
     */
    private fun inkBrows(spec: MarkSpec.InkBrows, power: Float): List<SkinMark> =
        listOf(geometry.leftBrow, geometry.rightBrow).flatMap { brow ->
            val path = smoothThrough(listOf(brow.inner, brow.middle, brow.outer), SAMPLES_PER_SEGMENT)
            val shape = outline(path) { t -> spec.width * 0.5f * (1f - 0.65f * t) * (0.35f + 0.65f * min(1f, t / 0.12f)) }
            val alpha = (spec.alpha * power).coerceIn(0f, 1f)
            listOf(
                SkinMark.Fill(shape, spec.color, alpha * 0.5f, MarkBlend.Normal),
                SkinMark.Fill(shape, spec.color, alpha, MarkBlend.Multiply),
            )
        }

    /** One side of an eye, from its inner corner to its outer one, [lift] times its half height above (or below) the corners. */
    private fun eyeArc(eye: EyeShape, lift: Float): List<Point> = (0..EYE_ARC_STEPS).map { step ->
        val t = step / EYE_ARC_STEPS.toFloat()
        lerp(eye.inner, eye.outer, t) + Point(0f, -lift * eye.halfHeight * sin(PI.toFloat() * t))
    }

    /** A stroke along [path], [width] wide in its middle and pointed at both ends. */
    private fun ribbon(
        path: List<Point>,
        width: Float,
        color: Argb,
        alpha: Float,
        blend: MarkBlend,
    ): SkinMark = SkinMark.Fill(outline(path) { t -> width * 0.5f * taper(t) }, color, alpha.coerceIn(0f, 1f), blend)

    /**
     * How wide a stroke is a fraction [t] of its length along: nothing at the ends, all of it in the middle. The sine is
     * kept from going below zero, where a float rounds sin(π): a negative power is NaN, and one NaN corner makes the
     * whole shape vanish.
     */
    private fun taper(t: Float): Float = sin(PI.toFloat() * t).coerceAtLeast(0f).pow(TAPER_POWER)

    /** The closed outline, in picture pixels, of a stroke along [path] whose half width at a fraction t of its length is [halfWidth]. */
    private fun outline(path: List<Point>, halfWidth: (Float) -> Float): ImmutableList<Point> {
        val left = ArrayList<Point>(path.size)
        val right = ArrayList<Point>(path.size)
        path.forEachIndexed { index, point ->
            val tangent = (path[(index + 1).coerceAtMost(path.size - 1)] - path[(index - 1).coerceAtLeast(0)]).normalized()
            val normal = Point(-tangent.y, tangent.x) * halfWidth(index / (path.size - 1f))
            left += field.warpToImage(point + normal)
            right += field.warpToImage(point - normal)
        }
        return (left + right.asReversed()).toImmutableList()
    }

    /** The resolved path, rounded through its points with a Catmull-Rom spline. */
    private fun smoothPath(points: ImmutableList<FeaturePoint>, mirror: Boolean): List<Point> =
        smoothThrough(points.map { geometry.resolve(it, mirror) }, SAMPLES_PER_SEGMENT)

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
