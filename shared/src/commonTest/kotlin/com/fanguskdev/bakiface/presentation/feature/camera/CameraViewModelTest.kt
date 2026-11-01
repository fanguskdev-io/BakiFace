package com.fanguskdev.bakiface.presentation.feature.camera

import app.cash.turbine.test
import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import com.fanguskdev.bakiface.domain.shot.ImportError
import com.fanguskdev.bakiface.domain.shot.ImportedPhoto
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.usecase.CaptureShotUseCase
import com.fanguskdev.bakiface.domain.usecase.GetEffectsUseCase
import com.fanguskdev.bakiface.domain.usecase.ImportPhotoUseCase
import com.fanguskdev.bakiface.domain.usecase.ObserveLivePreviewUseCase
import com.fanguskdev.bakiface.domain.usecase.ObserveSelectedEffectUseCase
import com.fanguskdev.bakiface.domain.usecase.SelectEffectUseCase
import com.fanguskdev.bakiface.presentation.Champion
import com.fanguskdev.bakiface.presentation.Ogre
import com.fanguskdev.bakiface.presentation.cameraFrame
import com.fanguskdev.bakiface.testing.FakeCamera
import com.fanguskdev.bakiface.testing.FakeEffectRepository
import com.fanguskdev.bakiface.testing.FakePhotoImport
import com.fanguskdev.bakiface.testing.FakePicture
import com.fanguskdev.bakiface.testing.FakePictureEditor
import com.fanguskdev.bakiface.testing.FakePreferences
import com.fanguskdev.bakiface.testing.FakeShotRepository
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class CameraViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val camera = FakeCamera()
    private val shots = FakeShotRepository()
    private val effects = FakeEffectRepository(listOf(Ogre, Champion))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        preferences: FakePreferences = FakePreferences(),
        photos: FakePhotoImport = FakePhotoImport(AppResult.Success(ImportedPhoto(FakePicture(800, 1000), syntheticFace()))),
    ) = CameraViewModel(
        getEffects = GetEffectsUseCase(effects),
        observeSelectedEffect = ObserveSelectedEffectUseCase(preferences),
        selectEffect = SelectEffectUseCase(preferences),
        observeLivePreview = ObserveLivePreviewUseCase(camera, FaceRenderer()),
        captureShot = CaptureShotUseCase(FakePictureEditor(), shots),
        importPhoto = ImportPhotoUseCase(photos, shots),
        dispatchers = AppDispatchers(dispatcher, dispatcher),
    )

    private fun TestScope.running(viewModel: CameraViewModel): CameraViewModel {
        viewModel.onIntent(CameraIntent.ScreenShown)
        viewModel.onIntent(CameraIntent.PermissionChanged(CameraPermission.Granted))
        viewModel.onIntent(CameraIntent.ViewportMeasured(1080, 2340))
        camera.frames.tryEmit(AppResult.Success(cameraFrame()))
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun theCatalogueArrivesAndTheLastChoiceComesBack() = runTest(dispatcher) {
        val viewModel = viewModel(FakePreferences(EffectId("baki")))
        advanceUntilIdle()

        assertEquals(listOf(null, EffectId("yujiro"), EffectId("baki")), viewModel.state.value.effects.map { it.id })
        assertEquals(EffectId("baki"), viewModel.state.value.selectedEffectId)
    }

    @Test
    fun aChoiceMadeBeforeTheCatalogueWinsAndIsRemembered() = runTest(dispatcher) {
        val preferences = FakePreferences(EffectId("baki"))
        val viewModel = viewModel(preferences)

        viewModel.onIntent(CameraIntent.SelectEffect(EffectId("yujiro")))
        advanceUntilIdle()

        assertEquals(EffectId("yujiro"), viewModel.state.value.selectedEffectId)
        assertEquals(EffectId("yujiro"), preferences.selectedEffectId.first())
    }

    @Test
    fun aBrokenCatalogueIsSaid() = runTest(dispatcher) {
        effects.result = AppResult.Failure(CatalogError.Invalid("broken"))
        val viewModel = viewModel()

        viewModel.effects.test {
            assertEquals(CameraEffect.ShowMessage(CameraMessage.CatalogFailed), awaitItem())
        }
    }

    @Test
    fun theCameraRunsOnlyWhileTheScreenIsShownAndAllowed() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(CameraIntent.ScreenShown)
        advanceUntilIdle()
        assertEquals(0, camera.running, "no camera without the permission")

        viewModel.onIntent(CameraIntent.PermissionChanged(CameraPermission.Granted))
        camera.frames.emit(AppResult.Success(cameraFrame()))
        advanceUntilIdle()
        assertEquals(1, camera.running)
        assertEquals(CameraStatus.Running, viewModel.state.value.status)
        assertNotNull(viewModel.state.value.preview)

        viewModel.onIntent(CameraIntent.ScreenHidden)
        advanceUntilIdle()
        assertEquals(0, camera.running)
        assertEquals(CameraStatus.Idle, viewModel.state.value.status)
    }

    @Test
    fun aRefusalStopsTheCamera() = runTest(dispatcher) {
        val viewModel = running(viewModel())

        viewModel.onIntent(CameraIntent.PermissionChanged(CameraPermission.PermanentlyDenied))
        advanceUntilIdle()

        assertEquals(0, camera.running)
        assertEquals(CameraPermission.PermanentlyDenied, viewModel.state.value.permission)
    }

    @Test
    fun aPhoneWithoutThatCameraShowsItIsUnavailable() = runTest(dispatcher) {
        camera.frames.tryEmit(AppResult.Failure(CameraError.NoCamera))
        val viewModel = viewModel()

        viewModel.onIntent(CameraIntent.ScreenShown)
        viewModel.onIntent(CameraIntent.PermissionChanged(CameraPermission.Granted))
        advanceUntilIdle()

        assertEquals(CameraStatus.Unavailable, viewModel.state.value.status)
    }

    @Test
    fun turningTheCameraAsksForTheOtherLens() = runTest(dispatcher) {
        val viewModel = running(viewModel())

        viewModel.onIntent(CameraIntent.FlipCamera)
        advanceUntilIdle()

        assertEquals(listOf(Lens.Front, Lens.Back), camera.lensesAsked)
        assertEquals(Lens.Back, viewModel.state.value.lens)
        assertEquals(1, camera.running)
    }

    @Test
    fun retryingStartsTheCameraAgain() = runTest(dispatcher) {
        val viewModel = running(viewModel())

        viewModel.onIntent(CameraIntent.RetryCamera)
        advanceUntilIdle()

        assertEquals(listOf(Lens.Front, Lens.Front), camera.lensesAsked)
    }

    @Test
    fun aPhotoOpensTheEditorWithTheChosenCharacter() = runTest(dispatcher) {
        val viewModel = running(viewModel())
        viewModel.onIntent(CameraIntent.SelectEffect(EffectId("yujiro")))
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(CameraIntent.Capture)
            val opened = assertIs<CameraEffect.OpenEditor>(awaitItem())
            assertEquals(EffectId("yujiro"), opened.effectId)
            assertNotNull(shots.shots[opened.shotId])
        }
        assertFalse(viewModel.state.value.isCapturing)
    }

    @Test
    fun withoutAFrameThereIsNoPhoto() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(CameraIntent.Capture)
        advanceUntilIdle()

        assertEquals(0, shots.shots.size)
    }

    @Test
    fun aPhotoOfTheGalleryOpensTheEditor() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(CameraIntent.PhotoPicked(PhotoSource("content://photo/1")))
            assertIs<CameraEffect.OpenEditor>(awaitItem())
        }
        assertFalse(viewModel.state.value.isImporting)
    }

    @Test
    fun anUnreadablePhotoIsSaid() = runTest(dispatcher) {
        val viewModel = viewModel(photos = FakePhotoImport(AppResult.Failure(ImportError.Unreadable)))

        viewModel.effects.test {
            viewModel.onIntent(CameraIntent.PhotoPicked(PhotoSource("x")))
            assertEquals(CameraEffect.ShowMessage(CameraMessage.ImportUnreadable), awaitItem())
        }
    }

    @Test
    fun aPhotoThatFailsOtherwiseIsSaidToo() = runTest(dispatcher) {
        val viewModel = viewModel(photos = FakePhotoImport(AppResult.Failure(ImportError.Failed("io"))))

        viewModel.effects.test {
            viewModel.onIntent(CameraIntent.PhotoPicked(PhotoSource("x")))
            assertEquals(CameraEffect.ShowMessage(CameraMessage.ImportFailed), awaitItem())
        }
    }

    @Test
    fun theSystemActionsAreAskedForOnce() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(CameraIntent.RequestPermission)
            assertEquals(CameraEffect.RequestCameraPermission, awaitItem())
            viewModel.onIntent(CameraIntent.OpenSettings)
            assertEquals(CameraEffect.OpenAppSettings, awaitItem())
            viewModel.onIntent(CameraIntent.PickPhoto)
            assertEquals(CameraEffect.LaunchPhotoPicker, awaitItem())
        }
    }

    @Test
    fun theGalleryOfCharactersOpensAndCloses() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.onIntent(CameraIntent.OpenGallery)
        assertEquals(true, viewModel.state.value.isGalleryOpen)
        viewModel.onIntent(CameraIntent.CloseGallery)
        assertEquals(false, viewModel.state.value.isGalleryOpen)
    }
}
