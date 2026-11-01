package com.fanguskdev.bakiface.data.catalog

import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.common.errorOrNull
import com.fanguskdev.bakiface.domain.common.getOrNull
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.EffectHint
import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.FoldRegion
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.effect.MarkSpec
import com.fanguskdev.bakiface.domain.effect.Sides
import com.fanguskdev.bakiface.domain.effect.WarpOp
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun effect(warp: String = """{ "op": "move", "feature": "chin", "radius": 0.5 }""", marks: String = "", extra: String = "") =
    """{ "id": "x", "name": "X", "kanji": "X", "accent": "#112233" $extra, "warp": [ $warp ], "marks": [ $marks ] }"""

private fun catalog(vararg effects: String, version: Int = 1) = """{ "version": $version, "effects": [ ${effects.joinToString()} ] }"""

private fun invalidReason(json: String): String = runTestResult(json).let { assertIs<CatalogError.Invalid>(it.errorOrNull()).reason }

private fun runTestResult(json: String) =
    kotlinx.serialization.json.Json.decodeFromString(CatalogDto.serializer(), json).let(EffectMapper::toDomain)

class ShippedCatalogTest {
    private val effects = assertNotNull(runTestResult(EFFECT_CATALOG_JSON).getOrNull())

    @Test
    fun theFiveCharactersAreThereInCarouselOrder() {
        assertEquals(listOf("yujiro", "baki", "jack", "oliva", "hanayama"), effects.map { it.id.value })
    }

    @Test
    fun everyCharacterChangesTheFaceAndHasItsSign() {
        effects.forEach { effect ->
            assertTrue(effect.warp.isNotEmpty(), "${effect.id} reshapes the face")
            assertTrue(effect.marks.isNotEmpty(), "${effect.id} adds something to the skin")
            assertTrue(effect.kanji.isNotBlank() && effect.name.isNotBlank())
            assertEquals(0xFF, effect.accent.alpha, "${effect.id}: the accent is opaque")
        }
    }

    @Test
    fun theOgreTheChampionAndTheBiterReactToTheMouth() {
        assertEquals(setOf("yujiro", "baki", "jack"), effects.filter { it.hint == EffectHint.OpenMouth }.map { it.id.value }.toSet())
    }

    @Test
    fun everyCharacterIsCarvedInFoldsAndTheOgreAndTheChampionStareBlank() {
        effects.forEach { effect -> assertTrue(effect.marks.any { it is MarkSpec.Folds }, "${effect.id} has the folds of the trend") }
        assertEquals(setOf("yujiro", "baki"), effects.filter { e -> e.marks.any { it is MarkSpec.BlankEyes } }.map { it.id.value }.toSet())
    }

    @Test
    fun noChangeIsStrongEnoughToTearTheSkin() {
        effects.flatMap { effect -> effect.warp.map { effect.id to it } }.forEach { (id, op) ->
            when (op) {
                is WarpOp.Move -> assertTrue(
                    kotlin.math.sqrt(op.dx * op.dx + op.dy * op.dy) * (1f + op.mouthOpenBoost) < 0.65f * op.radius,
                    "$id: a move of ${op.feature} folds the mesh",
                )
                is WarpOp.Scale -> assertTrue(op.sx in 0.25f..2f && op.sy in 0.25f..2f, "$id: a scale of ${op.feature} folds the mesh")
            }
        }
    }
}

class EffectMapperTest {
    @Test
    fun everyKindOfChangeAndMarkIsRead() {
        val json = catalog(
            effect(
                warp = """
                    { "op": "move", "feature": "brow_inner", "radius": 0.3, "dx": -0.05, "dy": 0.1, "sides": "left", "ox": 0.1, "oy": 0.2, "boost": 1 },
                    { "op": "scale", "feature": "mouth_corner", "radius": 0.4, "sx": 1.2, "sy": 0.9 }
                """,
                marks = """
                    { "type": "scar", "width": 0.03, "stitches": true, "path": [ { "feature": "eye_outer", "side": "right", "dx": 0.1 } ] },
                    { "type": "vein", "width": 0.02, "mirrored": true, "path": [ { "feature": "temple" } ] },
                    { "type": "wrinkle", "depth": 0.7, "boost": 0.5, "path": [ { "feature": "nose_base" } ] },
                    { "type": "shade", "at": { "feature": "cheek" }, "rx": 0.2, "ry": 0.3, "color": "#80000000", "alpha": 0.4, "blend": "screen" },
                    { "type": "hair", "from": { "feature": "upper_lip" }, "to": { "feature": "mouth_corner" }, "count": 3, "length": 0.1,
                      "angle": 20, "spread": 10, "color": "#222222", "width": 0.01 }
                """,
                extra = """, "hint": "open_mouth", "grade": { "contrast": 1.2, "tint": "#FF0000", "tintAmount": 0.3 }""",
            ),
        )

        val effect = assertNotNull(runTestResult(json).getOrNull()).single()

        assertEquals(WarpOp.Move(Feature.BrowInner, 0.3f, -0.05f, 0.1f, Sides.Left, 0.1f, 0.2f, 1f), effect.warp[0])
        assertEquals(WarpOp.Scale(Feature.MouthCorner, 0.4f, 1.2f, 0.9f), effect.warp[1])
        assertIs<MarkSpec.Scar>(effect.marks[0]).also { assertTrue(it.stitches) }
        assertIs<MarkSpec.Vein>(effect.marks[1]).also { assertTrue(it.mirrored) }
        assertIs<MarkSpec.Wrinkle>(effect.marks[2]).also { assertEquals(0.5f, it.mouthOpenBoost) }
        assertIs<MarkSpec.Shade>(effect.marks[3]).also {
            assertEquals(MarkBlend.Screen, it.blend)
            assertEquals(Argb(0x80000000L), it.color)
        }
        assertIs<MarkSpec.Hair>(effect.marks[4]).also { assertEquals(3, it.count) }
        assertEquals(EffectHint.OpenMouth, effect.hint)
        assertEquals(Argb(0xFFFF0000L), effect.grade.tint)
        assertEquals(Argb(0xFF112233L), effect.accent)
    }

    @Test
    fun theMarksOfTheTrendAreRead() {
        val json = catalog(
            effect(
                marks = """
                    { "type": "folds", "region": "crows_feet", "count": 4, "depth": 0.9, "boost": 0.3 },
                    { "type": "blank_eyes", "color": "#F2EDE4", "alpha": 0.9 },
                    { "type": "ink_brows", "width": 0.12, "color": "#140D0A", "alpha": 0.8 }
                """,
            ),
        )

        val marks = assertNotNull(runTestResult(json).getOrNull()).single().marks

        assertEquals(MarkSpec.Folds(FoldRegion.CrowsFeet, count = 4, depth = 0.9f, mouthOpenBoost = 0.3f), marks[0])
        assertEquals(MarkSpec.BlankEyes(Argb(0xFFF2EDE4L), alpha = 0.9f), marks[1])
        assertEquals(MarkSpec.InkBrows(width = 0.12f, color = Argb(0xFF140D0AL), alpha = 0.8f), marks[2])
    }

    @Test
    fun aTrendMarkThatCannotBeDrawnIsAnError() {
        assertTrue("region «ear»" in invalidReason(catalog(effect(marks = """{ "type": "folds", "region": "ear", "count": 2 }"""))))
        assertTrue("region «null»" in invalidReason(catalog(effect(marks = """{ "type": "folds", "count": 2 }"""))))
        assertTrue("count «0»" in invalidReason(catalog(effect(marks = """{ "type": "folds", "region": "chin" }"""))))
        assertTrue("color «null»" in invalidReason(catalog(effect(marks = """{ "type": "blank_eyes" }"""))))
        assertTrue("width «0.0»" in invalidReason(catalog(effect(marks = """{ "type": "ink_brows", "color": "#000000" }"""))))
        assertTrue("color «null»" in invalidReason(catalog(effect(marks = """{ "type": "ink_brows", "width": 0.1 }"""))))
    }

    @Test
    fun aWrongNameIsAnErrorThatSaysWhere() {
        assertTrue("feature «ear»" in invalidReason(catalog(effect(warp = """{ "op": "move", "feature": "ear", "radius": 0.5 }"""))))
        assertTrue("op «twist»" in invalidReason(catalog(effect(warp = """{ "op": "twist", "feature": "chin", "radius": 0.5 }"""))))
        assertTrue(
            "sides «up»" in invalidReason(catalog(effect(warp = """{ "op": "move", "feature": "chin", "radius": 0.5, "sides": "up" }"""))),
        )
        assertTrue("radius «0.0»" in invalidReason(catalog(effect(warp = """{ "op": "move", "feature": "chin", "radius": 0 }"""))))
        assertTrue("type «tattoo»" in invalidReason(catalog(effect(marks = """{ "type": "tattoo" }"""))))
        assertTrue(
            "blend «glow»" in invalidReason(
                catalog(effect(marks = """{ "type": "shade", "at": { "feature": "cheek" }, "color": "#000000", "blend": "glow" }""")),
            ),
        )
        assertTrue("color «null»" in invalidReason(catalog(effect(marks = """{ "type": "shade", "at": { "feature": "cheek" } }"""))))
        assertTrue("at «null»" in invalidReason(catalog(effect(marks = """{ "type": "shade", "color": "#000000" }"""))))
        assertTrue(
            "path[0].side «middle»" in
                invalidReason(catalog(effect(marks = """{ "type": "vein", "path": [ { "feature": "chin", "side": "middle" } ] }"""))),
        )
        assertTrue("accent «red»" in invalidReason(catalog(effect().replace("#112233", "red"))))
        assertTrue("hint «wink»" in invalidReason(catalog(effect(extra = """, "hint": "wink" """))))
    }

    @Test
    fun aRepeatedIdOrAnotherVersionIsRejected() {
        assertTrue("repeated ids: x" in invalidReason(catalog(effect(), effect())))
        assertTrue("version 2" in invalidReason(catalog(effect(), version = 2)))
    }

    @Test
    fun coloursAreReadWithOrWithoutAlpha() {
        assertEquals(Argb(0xFF0A0B0CL), EffectMapper.color("#0A0B0C"))
        assertEquals(Argb(0x100A0B0CL), EffectMapper.color("#100A0B0C"))
        assertNull(EffectMapper.color("0A0B0C"))
        assertNull(EffectMapper.color("#0A0B"))
        assertNull(EffectMapper.color("#GGHHII"))
    }
}

class CatalogEffectRepositoryTest {
    @Test
    fun theCatalogueIsReadOnceAndKept() = runTest {
        val repository =
            CatalogEffectRepository(AppDispatchers(StandardTestDispatcher(testScheduler), StandardTestDispatcher(testScheduler)))

        val first = repository.effects()
        val second = repository.effects()

        assertIs<AppResult.Success<*>>(first)
        assertSame(first, second)
    }

    @Test
    fun aBrokenCatalogueIsAnError() = runTest {
        val dispatchers = AppDispatchers(StandardTestDispatcher(testScheduler), StandardTestDispatcher(testScheduler))

        assertIs<CatalogError.Invalid>(CatalogEffectRepository(dispatchers, "{ not json").effects().errorOrNull())
        assertIs<CatalogError.Invalid>(CatalogEffectRepository(dispatchers, """{ "version": 1 }""").effects().errorOrNull())
    }
}
