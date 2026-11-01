package com.fanguskdev.bakiface.presentation.feature.editor

import app.cash.turbine.test
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ExportError
import com.fanguskdev.bakiface.domain.shot.ExportedPhoto
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.domain.usecase.DiscardShotUseCase
import com.fanguskdev.bakiface.domain.usecase.ExportShotUseCase
import com.fanguskdev.bakiface.domain.usecase.GetEffectsUseCase
import com.fanguskdev.bakiface.domain.usecase.GetShotUseCase
import com.fanguskdev.bakiface.domain.usecase.RenderShotUseCase
import com.fanguskdev.bakiface.presentation.Champion
import com.fanguskdev.bakiface.presentation.Ogre
import com.fanguskdev.bakiface.presentation.feature.effects.toItem
import com.fanguskdev.bakiface.testing.FakeEffectRepository
import com.fanguskdev.bakiface.testing.FakePhotoExport
import com.fanguskdev.bakiface.testing.FakePicture
import com.fanguskdev.bakiface.testing.FakeShotRepository
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorReducerTest {
    private fun reduce(vararg results: EditorResult) =
        results.fold(EditorUiState()) { state, result -> EditorReducer.reduce(state, result) }

    @Test
    fun aShotThatIsGoneIsMissingNotLoading() {
        val state = reduce(EditorResult.ShotLoaded(null))

        assertFalse(state.isLoading)
        assertTrue(state.isMissing)
        assertFalse(state.canExport)
    }

    @Test
    fun theCharactersComeAfterTheOriginalFaceWithTheCamerasChoice() {
        val state = reduce(EditorResult.EffectsLoaded(persistentListOf(Ogre.toItem(), Champion.toItem()), EffectId("baki")))

        assertEquals(listOf(null, EffectId("yujiro"), EffectId("baki")), state.effects.map { it.id })
        assertEquals(EffectId("baki"), state.selectedEffectId)
        assertEquals(2, state.selectedIndex)
        assertNull(reduce(EditorResult.EffectsLoaded(persistentListOf(Ogre.toItem()), EffectId("nobody"))).selectedEffectId)
    }

    @Test
    fun theIntensityStaysBetweenZeroAndOne() {
        assertEquals(1f, reduce(EditorResult.IntensityChanged(3f)).intensity)
        assertEquals(0f, reduce(EditorResult.IntensityChanged(-1f)).intensity)
        assertEquals(0.4f, reduce(EditorResult.IntensityChanged(0.4f)).intensity)
    }

    @Test
    fun oneExportAtATime() {
        val saving = reduce(EditorResult.ExportStarted(ExportDestination.Gallery))

        assertEquals(ExportDestination.Gallery, EditorReducer.reduce(saving, EditorResult.ExportStarted(ExportDestination.Share)).exporting)
        assertNull(EditorReducer.reduce(saving, EditorResult.ExportFinished).exporting)
    }

    @Test
    fun anUnknownCharacterIsTheOriginalFace() {
        val state = reduce(
            EditorResult.EffectsLoaded(persistentListOf(Ogre.toItem()), null),
            EditorResult.EffectSelected(EffectId("nobody")),
        )

        assertNull(state.selectedEffectId)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val shots = FakeShotRepository()
    private val photos = FakePhotoExport()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun editor(
        withFace: Boolean = true,
        shotId: ShotId? = null,
        effectId: EffectId? = EffectId("yujiro"),
    ): EditorViewModel {
        val id = shotId ?: shots.save(FakePicture(720, 1280), if (withFace) syntheticFace() else null).id
        val render = RenderShotUseCase(FaceRenderer())
        return EditorViewModel(
            shotId = id,
            initialEffectId = effectId,
            getShot = GetShotUseCase(shots),
            getEffects = GetEffectsUseCase(FakeEffectRepository(listOf(Ogre, Champion))),
            renderShot = render,
            exportShot = ExportShotUseCase(render, photos),
            discardShot = DiscardShotUseCase(shots),
            dispatchers = AppDispatchers(dispatcher, dispatcher),
        )
    }

    @Test
    fun theShotOpensWithTheCamerasCharacterDrawn() = runTest(dispatcher) {
        val viewModel = editor()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.shot)
        assertEquals(EffectId("yujiro"), state.selectedEffectId)
        assertNotNull(state.render?.mesh)
        assertTrue(state.hasFace)
    }

    @Test
    fun aShotThatIsGoneShowsAsMissing() = runTest(dispatcher) {
        val viewModel = editor(shotId = ShotId("gone"))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isMissing)
    }

    @Test
    fun changingTheIntensityRedrawsTheShot() = runTest(dispatcher) {
        val viewModel = editor()
        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.ChangeIntensity(0.4f))
        advanceUntilIdle()

        assertEquals(0.4f, viewModel.state.value.render?.gradeAmount)
    }

    @Test
    fun theOriginalFaceHasNothingToDraw() = runTest(dispatcher) {
        val viewModel = editor()
        advanceUntilIdle()

        viewModel.onIntent(EditorIntent.SelectEffect(null))
        advanceUntilIdle()

        assertNull(viewModel.state.value.render)
    }

    @Test
    fun savingSaysSo() = runTest(dispatcher) {
        val viewModel = editor()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(EditorIntent.Save)
            assertEquals(EditorEffect.ShowMessage(EditorMessage.Saved), awaitItem())
        }
        assertEquals(ExportDestination.Gallery, photos.exports.single().third)
        assertNull(viewModel.state.value.exporting)
    }

    @Test
    fun sharingOpensTheSheetWithTheFile() = runTest(dispatcher) {
        photos.result = AppResult.Success(ExportedPhoto("/cache/shared/photo.jpg"))
        val viewModel = editor()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(EditorIntent.Share)
            assertEquals(EditorEffect.OpenShareSheet("/cache/shared/photo.jpg"), awaitItem())
        }
    }

    @Test
    fun aRefusedGalleryAndOtherFailuresAreTold() = runTest(dispatcher) {
        val viewModel = editor()
        advanceUntilIdle()

        viewModel.effects.test {
            photos.result = AppResult.Failure(ExportError.PermissionDenied)
            viewModel.onIntent(EditorIntent.Save)
            assertEquals(EditorEffect.ShowMessage(EditorMessage.SaveDenied), awaitItem())
            photos.result = AppResult.Failure(ExportError.Failed("disk"))
            viewModel.onIntent(EditorIntent.Save)
            assertEquals(EditorEffect.ShowMessage(EditorMessage.SaveFailed), awaitItem())
            viewModel.onIntent(EditorIntent.Share)
            assertEquals(EditorEffect.ShowMessage(EditorMessage.ShareFailed), awaitItem())
        }
    }

    @Test
    fun closingLetsTheShotGo() = runTest(dispatcher) {
        val viewModel = editor()
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(EditorIntent.Close)
            assertEquals(EditorEffect.NavigateBack, awaitItem())
        }
        assertTrue(shots.shots.isEmpty())
    }
}
