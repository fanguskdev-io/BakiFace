package com.fanguskdev.bakiface.data.catalog

import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.ColorGrade
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectHint
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.FeaturePoint
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.effect.MarkSpec
import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.effect.Sides
import com.fanguskdev.bakiface.domain.effect.WarpOp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private const val SUPPORTED_VERSION = 1
private const val OPAQUE = 0xFF000000L

/** A catalogue entry that cannot be turned into the domain; the message says which and why. */
internal class InvalidCatalogException(message: String) : Exception(message)

/**
 * Turns the wire shape of the catalogue into domain [Effect]s (ADR 0003), checking every name against the domain's
 * enums: an unknown feature, operation, side, blend or colour, a duplicated id or a non-positive radius is a
 * [CatalogError.Invalid] that names the effect and the field, never a silent default.
 */
internal object EffectMapper {
    fun toDomain(catalog: CatalogDto): AppResult<ImmutableList<Effect>, CatalogError> = try {
        check(catalog.version == SUPPORTED_VERSION) { "version ${catalog.version} is not $SUPPORTED_VERSION" }
        val effects = catalog.effects.map(::effect)
        val repeated = effects.groupBy { it.id }.filterValues { it.size > 1 }.keys
        check(repeated.isEmpty()) { "repeated ids: ${repeated.joinToString { it.value }}" }
        AppResult.Success(effects.toImmutableList())
    } catch (error: InvalidCatalogException) {
        AppResult.Failure(CatalogError.Invalid(error.message.orEmpty()))
    } catch (error: IllegalStateException) {
        AppResult.Failure(CatalogError.Invalid(error.message.orEmpty()))
    }

    private fun effect(dto: EffectDto): Effect {
        fun fail(field: String, value: Any?): Nothing = throw InvalidCatalogException("${dto.id}: $field «$value» is not valid")
        if (dto.id.isBlank()) fail("id", dto.id)
        return Effect(
            id = EffectId(dto.id),
            name = dto.name,
            kanji = dto.kanji,
            accent = color(dto.accent) ?: fail("accent", dto.accent),
            warp = dto.warp.map { warp(it, ::fail) }.toImmutableList(),
            marks = dto.marks.map { mark(it, ::fail) }.toImmutableList(),
            grade = ColorGrade(
                contrast = dto.grade.contrast,
                saturation = dto.grade.saturation,
                warmth = dto.grade.warmth,
                tint = dto.grade.tint?.let { color(it) ?: fail("tint", it) },
                tintAmount = dto.grade.tintAmount,
            ),
            hint = dto.hint?.let { hint(it) ?: fail("hint", it) },
        )
    }

    private fun warp(dto: WarpOpDto, fail: (String, Any?) -> Nothing): WarpOp {
        val feature = feature(dto.feature) ?: fail("feature", dto.feature)
        val sides = sides(dto.sides) ?: fail("sides", dto.sides)
        if (dto.radius <= 0f) fail("radius", dto.radius)
        return when (dto.op) {
            "move" -> WarpOp.Move(feature, dto.radius, dto.dx, dto.dy, sides, dto.ox, dto.oy, dto.boost)
            "scale" -> WarpOp.Scale(feature, dto.radius, dto.sx, dto.sy, sides, dto.ox, dto.oy, dto.boost)
            else -> fail("op", dto.op)
        }
    }

    private fun mark(dto: MarkDto, fail: (String, Any?) -> Nothing): MarkSpec {
        fun point(point: PointDto?, field: String): FeaturePoint {
            val present = point ?: fail(field, null)
            return FeaturePoint(
                feature = feature(present.feature) ?: fail("$field.feature", present.feature),
                side = side(present.side) ?: fail("$field.side", present.side),
                dx = present.dx,
                dy = present.dy,
            )
        }
        val path = dto.path.mapIndexed { index, point -> point(point, "path[$index]") }.toImmutableList()
        return when (dto.type) {
            "scar" -> MarkSpec.Scar(path, dto.width, dto.stitches, dto.mirrored, dto.boost)
            "vein" -> MarkSpec.Vein(path, dto.width, dto.mirrored, dto.boost)
            "wrinkle" -> MarkSpec.Wrinkle(path, dto.depth, dto.mirrored, dto.boost)
            "shade" -> MarkSpec.Shade(
                at = point(dto.at, "at"),
                radiusX = dto.rx,
                radiusY = dto.ry,
                color = dto.color?.let(::color) ?: fail("color", dto.color),
                alpha = dto.alpha,
                blend = blend(dto.blend) ?: fail("blend", dto.blend),
                mirrored = dto.mirrored,
                mouthOpenBoost = dto.boost,
            )
            "hair" -> MarkSpec.Hair(
                from = point(dto.from, "from"),
                to = point(dto.to, "to"),
                count = dto.count,
                length = dto.length,
                angleDegrees = dto.angle,
                spreadDegrees = dto.spread,
                color = dto.color?.let(::color) ?: fail("color", dto.color),
                width = dto.width,
                mirrored = dto.mirrored,
                mouthOpenBoost = dto.boost,
            )
            else -> fail("type", dto.type)
        }
    }

    /** `#RRGGBB` (opaque) or `#AARRGGBB`. */
    internal fun color(text: String): Argb? {
        val digits = text.removePrefix("#")
        if (!text.startsWith("#") || (digits.length != 6 && digits.length != 8)) return null
        val value = digits.toLongOrNull(16) ?: return null
        return Argb(if (digits.length == 6) OPAQUE or value else value)
    }

    /** `BrowInner` is written `brow_inner` in the catalogue. */
    private val featuresByWireName: Map<String, Feature> =
        Feature.entries.associateBy { it.name.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase() }

    private fun feature(name: String): Feature? = featuresByWireName[name]

    private fun side(name: String): Side? = Side.entries.firstOrNull { it.name.lowercase() == name }

    private fun sides(name: String): Sides? = Sides.entries.firstOrNull { it.name.lowercase() == name }

    private fun blend(name: String): MarkBlend? = MarkBlend.entries.firstOrNull { it.name.lowercase() == name }

    private fun hint(name: String): EffectHint? = when (name) {
        "open_mouth" -> EffectHint.OpenMouth
        else -> null
    }
}
