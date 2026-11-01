package com.fanguskdev.bakiface.data.catalog

import kotlinx.serialization.Serializable

/** The catalogue as it is written in [EFFECT_CATALOG_JSON]: the wire shape, mapped to the domain by [EffectMapper]. */
@Serializable
internal data class CatalogDto(val version: Int, val effects: List<EffectDto>)

@Serializable
internal data class EffectDto(
    val id: String,
    val name: String,
    val kanji: String,
    val accent: String,
    val hint: String? = null,
    val grade: GradeDto = GradeDto(),
    val warp: List<WarpOpDto> = emptyList(),
    val marks: List<MarkDto> = emptyList(),
)

@Serializable
internal data class GradeDto(
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val warmth: Float = 0f,
    val tint: String? = null,
    val tintAmount: Float = 0f,
)

@Serializable
internal data class WarpOpDto(
    val op: String,
    val feature: String,
    val radius: Float,
    val dx: Float = 0f,
    val dy: Float = 0f,
    val sx: Float = 1f,
    val sy: Float = 1f,
    val sides: String = "both",
    val ox: Float = 0f,
    val oy: Float = 0f,
    val boost: Float = 0f,
)

@Serializable
internal data class PointDto(
    val feature: String,
    val side: String = "left",
    val dx: Float = 0f,
    val dy: Float = 0f,
)

@Serializable
internal data class MarkDto(
    val type: String,
    val region: String? = null,
    val path: List<PointDto> = emptyList(),
    val at: PointDto? = null,
    val from: PointDto? = null,
    val to: PointDto? = null,
    val width: Float = 0f,
    val stitches: Boolean = false,
    val depth: Float = 0.5f,
    val rx: Float = 0f,
    val ry: Float = 0f,
    val color: String? = null,
    val alpha: Float = 1f,
    val blend: String = "normal",
    val count: Int = 0,
    val length: Float = 0f,
    val angle: Float = 0f,
    val spread: Float = 0f,
    val mirrored: Boolean = false,
    val boost: Float = 0f,
)
