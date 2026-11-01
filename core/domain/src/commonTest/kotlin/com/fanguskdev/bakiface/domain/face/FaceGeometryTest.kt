package com.fanguskdev.bakiface.domain.face

import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.FeaturePoint
import com.fanguskdev.bakiface.domain.effect.Side
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.testing.assertNear
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FaceGeometryTest {
    @Test
    fun anUprightFaceHasItsFrameBetweenTheEyes() {
        val geometry = assertNotNull(faceGeometryOf(syntheticFace()))

        assertNear(Point(360f, 640f), geometry.axes.origin, tolerance = 0.5f)
        assertNear(120f, geometry.axes.unit, tolerance = 0.5f)
        assertNear(Point(1f, 0f), geometry.axes.xAxis)
        assertNear(Point(0f, 1f), geometry.axes.yAxis)
    }

    @Test
    fun theFeaturesAreWhereAFaceHasThem() {
        val geometry = assertNotNull(faceGeometryOf(syntheticFace()))

        assertNear(Point(-0.5f, 0f), geometry.leftEye.center)
        assertNear(Point(0.5f, 0f), geometry.rightEye.center)
        assertNear(-0.7f, geometry.leftEye.outer.x)
        assertNear(-0.3f, geometry.leftEye.inner.x)
        assertNear(0.1f, geometry.leftEye.halfHeight, tolerance = 0.005f)
        assertNear(-0.85f, geometry.leftBrow.outer.x)
        assertNear(0.18f, geometry.rightBrow.inner.x)
        assertNear(0.75f, geometry.noseTip.y)
        assertNear(0.22f, geometry.noseHalfWidth)
        assertNear(-0.42f, geometry.mouthLeft.x)
        assertNear(0.42f, geometry.mouthRight.x)
        assertNear(1.85f, geometry.chin.y, tolerance = 0.02f)
        assertTrue(geometry.forehead.y < geometry.leftBrow.middle.y, "the forehead is above the brows")
        assertTrue(geometry.leftSide.jaw.x < geometry.mouthLeft.x, "the jaw is outside the mouth")
        assertTrue(geometry.rightSide.gonion.y > geometry.mouthCenter.y, "the angle of the jaw is below the mouth")
        assertTrue(geometry.leftSide.neck.y > geometry.chin.y, "the neck is below the chin")
    }

    @Test
    fun aTiltedFaceGivesTheSameFeaturesInItsOwnFrame() {
        val upright = assertNotNull(faceGeometryOf(syntheticFace()))
        val tilted = assertNotNull(faceGeometryOf(syntheticFace(angleDegrees = 35f)))

        assertNear(upright.chin, tilted.chin, tolerance = 0.02f)
        assertNear(upright.mouthLeft, tilted.mouthLeft, tolerance = 0.02f)
        assertNear(upright.rightBrow.outer, tilted.rightBrow.outer, tolerance = 0.02f)
    }

    @Test
    fun theDetectorsLabelsDoNotDecideWhichEyeIsWhich() {
        val labelled = assertNotNull(faceGeometryOf(syntheticFace()))
        val swapped = assertNotNull(faceGeometryOf(syntheticFace(swapLabels = true)))

        assertEquals(labelled.axes, swapped.axes)
        assertNear(labelled.leftBrow.outer, swapped.leftBrow.outer)
    }

    @Test
    fun anUpsideDownFaceStillHasTheMouthBelowTheEyes() {
        val geometry = assertNotNull(faceGeometryOf(syntheticFace(angleDegrees = 180f)))

        assertNear(Point(-1f, 0f), geometry.axes.xAxis)
        assertNear(1.2f, geometry.mouthCenter.y, tolerance = 0.05f)
    }

    @Test
    fun theMouthOpensFromZeroToOne() {
        assertNear(0f, assertNotNull(faceGeometryOf(syntheticFace(mouthOpen = 0f))).mouthOpenness)
        assertNear(1f, assertNotNull(faceGeometryOf(syntheticFace(mouthOpen = 1f))).mouthOpenness)
    }

    @Test
    fun withoutEyesOrWithTinyOnesThereIsNoFace() {
        assertNull(faceGeometryOf(FaceLandmarks()))
        assertNull(faceGeometryOf(syntheticFace(eyeDistance = 6f)))
    }

    @Test
    fun theMissingRegionsFallBackToTheUsualProportions() {
        val eyesOnly = syntheticFace().let { FaceLandmarks(leftEye = it.leftEye, rightEye = it.rightEye) }

        val geometry = assertNotNull(faceGeometryOf(eyesOnly))

        assertNear(Point(0f, 1.2f), geometry.mouthCenter)
        assertNear(0.75f, geometry.noseTip.y)
        assertNear(-0.35f, geometry.leftBrow.middle.y)
        assertNear(-1.05f, geometry.leftSide.jaw.x)
        assertNear(0f, geometry.mouthOpenness)
    }

    @Test
    fun aSingleEyebrowGoesToItsOwnSide() {
        val face = syntheticFace().let { it.copy(leftEyebrow = persistentListOf(), rightEyebrow = it.rightEyebrow) }

        val geometry = assertNotNull(faceGeometryOf(face))

        assertNear(0.85f, geometry.rightBrow.outer.x)
        assertNear(-0.35f, geometry.leftBrow.middle.y, message = "the other one falls back")
    }

    @Test
    fun aFeaturePointIsResolvedOnItsSideAndMirrored() {
        val geometry = assertNotNull(faceGeometryOf(syntheticFace()))
        val point = FeaturePoint(Feature.Eye, Side.Left, dx = -0.1f, dy = 0.2f)

        assertNear(Point(-0.6f, 0.2f), geometry.resolve(point))
        assertNear(Point(0.6f, 0.2f), geometry.resolve(point, mirror = true))
    }

    @Test
    fun everyFeatureHasAnAnchor() {
        val geometry = assertNotNull(faceGeometryOf(syntheticFace()))

        Feature.entries.forEach { feature ->
            val left = geometry.anchor(feature, Side.Left)
            val right = geometry.anchor(feature, Side.Right)
            if (feature.paired) {
                assertTrue(left.x < right.x, "$feature: the left one is on the left")
            } else {
                assertEquals(left, right, "$feature sits on the middle line")
            }
        }
    }
}
