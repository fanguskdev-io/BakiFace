package com.fanguskdev.bakiface.presentation.feature.camera

import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.presentation.Champion
import com.fanguskdev.bakiface.presentation.Ogre
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import com.fanguskdev.bakiface.presentation.feature.effects.toItem
import com.fanguskdev.bakiface.testing.FakePicture
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun preview(withFace: Boolean = true) = PreviewFrame(
    picture = FakePicture(720, 1280),
    mirrored = true,
    landmarks = if (withFace) syntheticFace() else null,
    presence = if (withFace) 1f else 0f,
    render = null,
    timestampNanos = 0L,
)

private fun reduce(vararg results: CameraResult, from: CameraUiState = CameraUiState()): CameraUiState =
    results.fold(from) { state, result -> CameraReducer.reduce(state, result) }

private val catalogue = CameraResult.EffectsLoaded(persistentListOf(Ogre.toItem(), Champion.toItem()))

class CameraReducerTest {
    @Test
    fun theCatalogueComesAfterTheOriginalFace() {
        val state = reduce(catalogue)

        assertEquals(listOf(null, EffectId("yujiro"), EffectId("baki")), state.effects.map { it.id })
        assertTrue(state.isCatalogLoaded)
        assertEquals(EffectItem.Original, state.selectedEffect)
        assertEquals(0, state.selectedIndex)
    }

    @Test
    fun aChoiceBeforeTheCatalogueIsKeptAndThenChecked() {
        val known = reduce(CameraResult.EffectSelected(EffectId("baki")), catalogue)
        val unknown = reduce(CameraResult.EffectSelected(EffectId("nobody")), catalogue)

        assertEquals(EffectId("baki"), known.selectedEffectId)
        assertEquals(2, known.selectedIndex)
        assertNull(unknown.selectedEffectId)
    }

    @Test
    fun afterTheCatalogueAnUnknownChoiceIsTheOriginalFace() {
        assertNull(reduce(catalogue, CameraResult.EffectSelected(EffectId("nobody"))).selectedEffectId)
        assertEquals(EffectId("yujiro"), reduce(catalogue, CameraResult.EffectSelected(EffectId("yujiro"))).selectedEffectId)
    }

    @Test
    fun aRefusedPermissionStopsTheCameraAndForgetsTheFrame() {
        val running = reduce(
            CameraResult.PermissionUpdated(CameraPermission.Granted),
            CameraResult.PreviewStarting,
            CameraResult.FrameArrived(preview()),
        )

        val refused = reduce(CameraResult.PermissionUpdated(CameraPermission.PermanentlyDenied), from = running)

        assertEquals(CameraStatus.Running, running.status)
        assertEquals(CameraStatus.Idle, refused.status)
        assertNull(refused.preview)
    }

    @Test
    fun aFrameAfterTheCameraStoppedIsDropped() {
        val stopped = reduce(CameraResult.PreviewStarting, CameraResult.PreviewStopped)

        assertSame(stopped, CameraReducer.reduce(stopped, CameraResult.FrameArrived(preview())))
    }

    @Test
    fun theCameraErrorsBecomeWhatTheScreenShows() {
        assertEquals(CameraStatus.Unavailable, reduce(CameraResult.CameraFailed(CameraError.NoCamera)).status)
        assertEquals(CameraStatus.Failed, reduce(CameraResult.CameraFailed(CameraError.Failed("busy"))).status)
        val denied = reduce(CameraResult.CameraFailed(CameraError.PermissionDenied))
        assertEquals(CameraPermission.Denied, denied.permission)
        assertEquals(CameraStatus.Idle, denied.status)
    }

    @Test
    fun stoppingKeepsAnUnavailableOrFailedCameraAsItIs() {
        assertEquals(CameraStatus.Unavailable, reduce(CameraResult.CameraFailed(CameraError.NoCamera), CameraResult.PreviewStopped).status)
        assertEquals(CameraStatus.Failed, reduce(CameraResult.CameraFailed(CameraError.Failed("x")), CameraResult.PreviewStopped).status)
    }

    @Test
    fun aPhotoNeedsARunningCameraAMeasuredScreenAndNothingElseGoingOn() {
        val ready = reduce(
            CameraResult.PermissionUpdated(CameraPermission.Granted),
            CameraResult.PreviewStarting,
            CameraResult.FrameArrived(preview()),
            CameraResult.ViewportChanged(1080, 2340),
        )

        assertTrue(ready.canCapture)
        assertTrue(ready.isFaceVisible)
        assertFalse(reduce(CameraResult.Capturing(active = true), from = ready).canCapture)
        assertFalse(reduce(CameraResult.Importing(active = true), from = ready).canCapture)
        assertFalse(reduce(CameraResult.ViewportChanged(0, 0), from = ready).canCapture)
        assertFalse(reduce(CameraResult.FrameArrived(preview(withFace = false)), from = ready).isFaceVisible)
    }

    @Test
    fun turningTheCameraForgetsTheOldFrame() {
        val turned = reduce(CameraResult.PreviewStarting, CameraResult.FrameArrived(preview()), CameraResult.LensChanged(Lens.Back))

        assertEquals(Lens.Back, turned.lens)
        assertNull(turned.preview)
    }

    @Test
    fun theGalleryOpensAndCloses() {
        assertTrue(reduce(CameraResult.GalleryToggled(open = true)).isGalleryOpen)
        assertFalse(reduce(CameraResult.GalleryToggled(open = true), CameraResult.GalleryToggled(open = false)).isGalleryOpen)
    }
}
