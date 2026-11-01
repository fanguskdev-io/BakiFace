package com.fanguskdev.bakiface.domain.usecase

import app.cash.turbine.test
import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.CameraFrame
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.common.getOrNull
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.CatalogError
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.effect.EffectRepository
import com.fanguskdev.bakiface.domain.effect.Feature
import com.fanguskdev.bakiface.domain.effect.WarpOp
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.picture.Region
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ImportError
import com.fanguskdev.bakiface.domain.shot.ImportedPhoto
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.shot.Shot
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.testing.FakeCamera
import com.fanguskdev.bakiface.testing.FakePhotoExport
import com.fanguskdev.bakiface.testing.FakePhotoImport
import com.fanguskdev.bakiface.testing.FakePicture
import com.fanguskdev.bakiface.testing.FakePictureEditor
import com.fanguskdev.bakiface.testing.FakePreferences
import com.fanguskdev.bakiface.testing.FakeShotRepository
import com.fanguskdev.bakiface.testing.assertNear
import com.fanguskdev.bakiface.testing.syntheticFace
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private val OGRE = Effect(
    id = EffectId("ogre"),
    name = "Ogre",
    kanji = "鬼",
    accent = Argb(0xFFC8102EL),
    warp = persistentListOf(WarpOp.Move(Feature.Jaw, radius = 0.6f, dx = 0.15f)),
)

private fun frame(timestamp: Long, withFace: Boolean = true, mirrored: Boolean = true) = CameraFrame(
    picture = FakePicture(720, 1280),
    landmarks = if (withFace) syntheticFace() else null,
    mirrored = mirrored,
    timestampNanos = timestamp,
)

class ObserveLivePreviewUseCaseTest {
    private val camera = FakeCamera()
    private val useCase = ObserveLivePreviewUseCase(camera, FaceRenderer())

    @Test
    fun theEffectAppearsAsTheFaceFadesIn() = runTest {
        useCase(Lens.Front, MutableStateFlow(OGRE)).test {
            camera.frames.emit(AppResult.Success(frame(0L)))
            val first = assertNotNull(awaitItem().getOrNull())
            camera.frames.emit(AppResult.Success(frame(33_000_000L)))
            val second = assertNotNull(awaitItem().getOrNull())
            assertEquals(0f, first.presence)
            assertNull(first.render?.mesh, "a face that has just appeared is not warped yet")
            assertNull(first.framesPerSecond)
            assertTrue(second.presence > 0f)
            assertNotNull(second.render?.mesh)
            assertTrue(second.mirrored)
            assertNear(30.3f, assertNotNull(second.framesPerSecond), tolerance = 0.1f)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(Lens.Front), camera.lensesAsked)
    }

    @Test
    fun changingTheEffectRedrawsTheLastFrame() = runTest {
        val effect = MutableStateFlow<Effect?>(OGRE)
        camera.frames.emit(AppResult.Success(frame(0L)))

        useCase(Lens.Back, effect).test {
            assertNotNull(awaitItem().getOrNull()?.render)
            effect.value = null
            val redrawn = assertNotNull(awaitItem().getOrNull())
            assertNull(redrawn.render)
            assertEquals(0L, redrawn.timestampNanos)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFrameWithoutAFaceKeepsTheColourOnly() = runTest {
        camera.frames.emit(AppResult.Success(frame(0L, withFace = false)))

        useCase(Lens.Front, MutableStateFlow(OGRE)).test {
            val preview = assertNotNull(awaitItem().getOrNull())
            assertTrue(!preview.hasFace)
            assertNull(preview.render?.mesh)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailureOfTheCameraReachesTheScreen() = runTest {
        camera.frames.emit(AppResult.Failure(CameraError.NoCamera))

        useCase(Lens.Front, MutableStateFlow(null)).test {
            assertEquals(AppResult.Failure(CameraError.NoCamera), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

class CaptureShotUseCaseTest {
    private val editor = FakePictureEditor()
    private val shots = FakeShotRepository()
    private val useCase = CaptureShotUseCase(editor, shots)

    private fun preview(mirrored: Boolean, presence: Float = 1f) = PreviewFrame(
        picture = FakePicture(720, 1280),
        mirrored = mirrored,
        landmarks = syntheticFace().map { Point(100f, 200f) },
        presence = presence,
        render = null,
        timestampNanos = 0L,
    )

    @Test
    fun theShotIsWhatTheScreenShowsFlippedLikeIt() = runTest {
        val shot = useCase(preview(mirrored = true), viewWidth = 1080, viewHeight = 2340)

        assertEquals(Region(65, 0, 590, 1280), editor.lastRegion)
        assertEquals(true, editor.lastMirror)
        assertEquals(FakePicture(590, 1280), shot.picture)
        assertEquals(Point(555f, 200f), shot.landmarks?.leftEye?.first())
        assertSame(shot, shots.shots[shot.id])
    }

    @Test
    fun theBackCameraIsNotFlipped() = runTest {
        val shot = useCase(preview(mirrored = false), viewWidth = 1080, viewHeight = 2340)

        assertEquals(false, editor.lastMirror)
        assertEquals(Point(35f, 200f), shot.landmarks?.leftEye?.first())
    }

    @Test
    fun aFaceThatHasGoneIsNotKept() = runTest {
        val shot = useCase(preview(mirrored = true, presence = 0f), viewWidth = 1080, viewHeight = 2340)

        assertNull(shot.landmarks)
    }
}

class ShotUseCasesTest {
    private val shots = FakeShotRepository()

    @Test
    fun aPhotoOfTheGalleryBecomesAShot() = runTest {
        val photos = FakePhotoImport(AppResult.Success(ImportedPhoto(FakePicture(1000, 800), syntheticFace())))

        val result = ImportPhotoUseCase(photos, shots)(PhotoSource("content://photo/1"))

        val shot = assertNotNull(result.getOrNull())
        assertEquals(FakePicture(1000, 800), shot.picture)
        assertEquals(listOf(PhotoSource("content://photo/1")), photos.sources)
        assertEquals(shot, GetShotUseCase(shots)(shot.id))
    }

    @Test
    fun anUnreadablePhotoIsAFailureAndNoShot() = runTest {
        val result = ImportPhotoUseCase(FakePhotoImport(AppResult.Failure(ImportError.Unreadable)), shots)(PhotoSource("x"))

        assertEquals(AppResult.Failure(ImportError.Unreadable), result)
        assertTrue(shots.shots.isEmpty())
    }

    @Test
    fun aShotLetGoIsGone() = runTest {
        val shot = shots.save(FakePicture(10, 10), null)

        DiscardShotUseCase(shots)(shot.id)

        assertNull(GetShotUseCase(shots)(shot.id))
        assertNull(GetShotUseCase(shots)(ShotId("never")))
    }

    @Test
    fun theEditorDrawsTheShotAtTheChosenIntensity() {
        val shot = Shot(ShotId("s"), FakePicture(720, 1280), syntheticFace())

        val full = assertNotNull(RenderShotUseCase(FaceRenderer())(shot, OGRE, intensity = 1f))
        val none = assertNotNull(RenderShotUseCase(FaceRenderer())(shot, OGRE, intensity = 0f))

        assertNotNull(full.mesh)
        assertNull(none.mesh)
        assertNull(RenderShotUseCase(FaceRenderer())(shot, effect = null, intensity = 1f))
    }

    @Test
    fun exportingDrawsTheShotAndSendsItWhereAsked() = runTest {
        val photos = FakePhotoExport()
        val shot = Shot(ShotId("s"), FakePicture(720, 1280), syntheticFace())

        val result = ExportShotUseCase(RenderShotUseCase(FaceRenderer()), photos)(shot, OGRE, 0.5f, ExportDestination.Share)

        assertIs<AppResult.Success<*>>(result)
        val (picture, render, destination) = photos.exports.single()
        assertEquals(shot.picture, picture)
        assertEquals(0.5f, render?.gradeAmount)
        assertEquals(ExportDestination.Share, destination)
    }
}

class EffectUseCasesTest {
    @Test
    fun theChoiceIsRememberedAndCanBeUndone() = runTest {
        val preferences = FakePreferences()

        SelectEffectUseCase(preferences)(EffectId("ogre"))
        assertEquals(EffectId("ogre"), ObserveSelectedEffectUseCase(preferences)().first())
        SelectEffectUseCase(preferences)(null)
        assertNull(ObserveSelectedEffectUseCase(preferences)().first())
    }

    @Test
    fun theCatalogueComesFromItsRepository() = runTest {
        val repository = object : EffectRepository {
            override suspend fun effects(): AppResult<ImmutableList<Effect>, CatalogError> = AppResult.Success(persistentListOf(OGRE))
        }

        assertEquals(AppResult.Success(persistentListOf(OGRE)), GetEffectsUseCase(repository)())
    }

    @Test
    fun anEffectWarpsTheFaceItIsGiven() {
        val shot = Shot(ShotId("s"), FakePicture(720, 1280), syntheticFace())
        val mesh = assertNotNull(RenderShotUseCase(FaceRenderer())(shot, OGRE, 1f)?.mesh)
        val moved = mesh.positions.indices.step(2).count { index ->
            val column = (index / 2) % (mesh.columns + 1)
            val row = (index / 2) / (mesh.columns + 1)
            mesh.positions[index] != 720f * column / mesh.columns || mesh.positions[index + 1] != 1280f * row / mesh.rows
        }

        assertTrue(moved > 0, "some corners move")
        assertNear(0f, mesh.positions[0])
    }
}
