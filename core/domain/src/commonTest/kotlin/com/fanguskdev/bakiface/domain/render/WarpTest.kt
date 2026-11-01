package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.FeaturePoint
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.effect.MarkSpec
import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.effect.Sides
import com.fanguskdev.bakiface.domain.effect.WarpOp
import com.fanguskdev.bakiface.domain.face.FaceGeometry
import com.fanguskdev.bakiface.domain.face.faceGeometryOf
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.testing.assertNear
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun geometry(mouthOpen: Float = 0f, angleDegrees: Float = 0f): FaceGeometry =
    assertNotNull(faceGeometryOf(syntheticFace(mouthOpen = mouthOpen, angleDegrees = angleDegrees)))

class WarpFieldTest {
    private val face = geometry()

    @Test
    fun aMovePushesItsCentreByAllOfIt() {
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.2f)), strength = 1f)

        assertNear(face.chin + Point(0f, 0.2f), field.warpLocal(face.chin))
    }

    @Test
    fun nothingMovesBeyondTheRadius() {
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.2f)), strength = 1f)
        val outside = face.chin + Point(0.6f, 0f)

        assertEquals(outside, field.warpLocal(outside))
    }

    @Test
    fun theWeightFallsSmoothlyHalfWay() {
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Chin, radius = 1f, dy = 0.2f)), strength = 1f)
        val halfWay = face.chin + Point(0.5f, 0f)

        assertNear(halfWay.y + 0.2f * 0.5625f, field.warpLocal(halfWay).y, tolerance = 0.0005f)
    }

    @Test
    fun aPairedMoveGoesOutwardsOnBothSides() {
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Jaw, radius = 0.5f, dx = 0.1f)), strength = 1f)

        assertNear(face.leftSide.jaw.x - 0.1f, field.warpLocal(face.leftSide.jaw).x)
        assertNear(face.rightSide.jaw.x + 0.1f, field.warpLocal(face.rightSide.jaw).x)
    }

    @Test
    fun aMoveCanTouchOneSideOnly() {
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Jaw, radius = 0.5f, dx = 0.1f, sides = Sides.Left)), strength = 1f)

        assertNear(face.leftSide.jaw.x - 0.1f, field.warpLocal(face.leftSide.jaw).x)
        assertEquals(face.rightSide.jaw, field.warpLocal(face.rightSide.jaw))
    }

    @Test
    fun aScaleShrinksAroundItsCentre() {
        val field = WarpField.build(face, listOf(WarpOp.Scale(Feature.Eye, radius = 0.4f, sy = 0.5f)), strength = 1f)
        val lid = face.leftEye.center + Point(0f, 0.1f)
        val weight = (1f - 0.0625f) * (1f - 0.0625f)

        assertNear(face.leftEye.center.y + 0.1f - 0.05f * weight, field.warpLocal(lid).y, tolerance = 0.0005f)
        assertNear(face.leftEye.center, field.warpLocal(face.leftEye.center), message = "the centre stays")
    }

    @Test
    fun anOffsetMovesTheCentreOutwards() {
        val op = WarpOp.Move(Feature.Eye, radius = 0.2f, dy = 0.1f, offsetX = 0.3f)
        val field = WarpField.build(face, listOf(op), strength = 1f)

        assertNear(0.1f, field.warpLocal(face.leftEye.center + Point(-0.3f, 0f)).y)
        assertNear(0.1f, field.warpLocal(face.rightEye.center + Point(0.3f, 0f)).y)
    }

    @Test
    fun anOpenMouthStrengthensWhatItBoosts() {
        val op = WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.1f, mouthOpenBoost = 1f)
        val closed = WarpField.build(geometry(mouthOpen = 0f), listOf(op), strength = 1f)
        val open = geometry(mouthOpen = 1f)
        val opened = WarpField.build(open, listOf(op), strength = 1f)

        assertNear(0.1f, closed.warpLocal(face.chin).y - face.chin.y)
        assertNear(0.2f, opened.warpLocal(open.chin).y - open.chin.y)
    }

    @Test
    fun theStrengthScalesEverythingAndZeroIsNothing() {
        val op = WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.2f)

        assertNear(0.1f, WarpField.build(face, listOf(op), strength = 0.5f).warpLocal(face.chin).y - face.chin.y)
        assertTrue(WarpField.build(face, listOf(op), strength = 0f).isEmpty)
        assertEquals(0f, WarpField.build(face, emptyList(), strength = 1f).reach)
    }

    @Test
    fun theWarpFollowsATiltedFace() {
        val tilted = geometry(angleDegrees = 90f)
        val field = WarpField.build(tilted, listOf(WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.25f)), strength = 1f)

        val moved = field.warpToImage(tilted.chin) - tilted.axes.toImage(tilted.chin)

        assertNear(Point(-30f, 0f), moved, tolerance = 0.1f, message = "a quarter turn makes 'down the face' point left")
    }
}

class MeshPlannerTest {
    private val face = geometry()

    @Test
    fun theGridCoversThePictureWithSquareCells() {
        val twelvePixelCells = assertNotNull(faceGeometryOf(syntheticFace(eyeDistance = 108f)))
        val field = WarpField.build(twelvePixelCells, listOf(WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.2f)), strength = 1f)

        val mesh = MeshPlanner().plan(field, width = 720, height = 1280)

        assertEquals(60, mesh.columns)
        assertEquals(107, mesh.rows)
        assertEquals((mesh.columns + 1) * (mesh.rows + 1), mesh.vertexCount)
        assertEquals(0f, mesh.positions[0])
        assertEquals(1280f, mesh.positions.last())
    }

    @Test
    fun cornersFarFromTheFaceStayWhereTheyAre() {
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.2f)), strength = 1f)

        val mesh = MeshPlanner().plan(field, width = 720, height = 1280)

        assertEquals(720f, mesh.positions[mesh.columns * 2])
        assertEquals(0f, mesh.positions[mesh.columns * 2 + 1])
    }

    @Test
    fun aCornerOnTheChinMovesDownTheFace() {
        val chinInImage = face.axes.toImage(face.chin)
        val field = WarpField.build(face, listOf(WarpOp.Move(Feature.Chin, radius = 1.5f, dy = 0.2f)), strength = 1f)
        val mesh = MeshPlanner().plan(field, width = 720, height = 1280)
        val column = kotlin.math.round(chinInImage.x / (720f / mesh.columns)).toInt()
        val row = kotlin.math.round(chinInImage.y / (1280f / mesh.rows)).toInt()
        val index = (row * (mesh.columns + 1) + column) * 2

        val moved = mesh.positions[index + 1] - 1280f * row / mesh.rows

        assertTrue(moved > 20f, "the corner nearest the chin moves more than 20 px down, moved $moved")
    }

    @Test
    fun theCellsNeverGetTooSmallNorTooBig() {
        val tiny = assertNotNull(faceGeometryOf(syntheticFace(eyeDistance = 14f)))
        val huge = assertNotNull(faceGeometryOf(syntheticFace(eyeDistance = 2000f)))
        val op = listOf(WarpOp.Move(Feature.Chin, radius = 0.5f, dy = 0.2f))

        assertTrue(MeshPlanner().plan(WarpField.build(tiny, op, 1f), 720, 1280).rows in 110..111)
        assertTrue(MeshPlanner().plan(WarpField.build(huge, op, 1f), 720, 1280).rows in 24..25)
    }
}

class FaceRendererTest {
    private val renderer = FaceRenderer()
    private val effect = Effect(
        id = EffectId("test"),
        name = "Test",
        kanji = "試",
        accent = Argb(0xFFC8102EL),
        warp = persistentListOf(WarpOp.Move(Feature.Jaw, radius = 0.6f, dx = 0.15f)),
        marks = persistentListOf(
            MarkSpec.Scar(
                path = persistentListOf(FeaturePoint(Feature.Cheekbone, Side.Left), FeaturePoint(Feature.Cheek, Side.Left)),
                width = 0.03f,
                stitches = true,
            ),
            MarkSpec.Vein(
                path = persistentListOf(
                    FeaturePoint(Feature.Temple, Side.Left, dy = 0.1f),
                    FeaturePoint(Feature.Temple, Side.Left, dy = -0.1f),
                    FeaturePoint(Feature.Temple, Side.Left, dx = 0.1f, dy = -0.3f),
                ),
                width = 0.02f,
                mirrored = true,
            ),
            MarkSpec.Wrinkle(
                path = persistentListOf(FeaturePoint(Feature.NoseBase, Side.Left, dx = -0.2f), FeaturePoint(Feature.MouthCorner)),
                depth = 0.6f,
            ),
            MarkSpec.Shade(FeaturePoint(Feature.Cheek), 0.2f, 0.3f, Argb(0xFF000000L), alpha = 0.5f, blend = MarkBlend.Multiply),
            MarkSpec.Hair(
                from = FeaturePoint(Feature.UpperLip, Side.Left),
                to = FeaturePoint(Feature.MouthCorner, Side.Left),
                count = 12,
                length = 0.12f,
                angleDegrees = 30f,
                spreadDegrees = 20f,
                color = Argb(0xFF222222L),
                width = 0.01f,
                mirrored = true,
            ),
        ),
    )

    @Test
    fun withoutAnEffectThereIsNothingToDraw() {
        assertNull(renderer.render(geometry(), effect = null, intensity = 1f, presence = 1f, imageWidth = 720, imageHeight = 1280))
    }

    @Test
    fun withoutAFaceOnlyTheColourOfTheCharacterIsDrawn() {
        val render = assertNotNull(renderer.render(null, effect, intensity = 0.7f, presence = 1f, imageWidth = 720, imageHeight = 1280))

        assertNull(render.mesh)
        assertTrue(render.marks.isEmpty())
        assertEquals(0.7f, render.gradeAmount)
    }

    @Test
    fun aFaceGetsTheMeshAndEveryMark() {
        val render = assertNotNull(renderer.render(geometry(), effect, intensity = 1f, presence = 1f, imageWidth = 720, imageHeight = 1280))

        assertNotNull(render.mesh)
        val lines = render.marks.filterIsInstance<SkinMark.Line>()
        val spots = render.marks.filterIsInstance<SkinMark.Spot>()
        assertEquals(1, spots.size)
        assertEquals(4 + 4 * 2 + 2 + 24, lines.size - lines.count { it.points.size == 2 && it.color == Argb(0xFF2E1A1AL) })
        assertTrue(lines.any { it.color == Argb(0xFF2E1A1AL) }, "the scar has stitches")
    }

    @Test
    fun theHairStaysPutFromFrameToFrame() {
        val first = assertNotNull(renderer.render(geometry(), effect, 1f, 1f, 720, 1280)).marks
        val second = assertNotNull(renderer.render(geometry(), effect, 1f, 1f, 720, 1280)).marks

        assertEquals(first, second)
    }

    @Test
    fun anAbsentFaceAndZeroIntensityAreTheSame() {
        val absent = assertNotNull(renderer.render(geometry(), effect, intensity = 1f, presence = 0f, imageWidth = 720, imageHeight = 1280))

        assertNull(absent.mesh)
        assertTrue(absent.marks.isEmpty())
    }

    @Test
    fun anEffectWithoutWarpHasNoMesh() {
        val marksOnly = effect.copy(warp = persistentListOf())

        val render = assertNotNull(renderer.render(geometry(), marksOnly, 1f, 1f, 720, 1280))

        assertNull(render.mesh)
        assertTrue(render.marks.isNotEmpty())
    }
}
