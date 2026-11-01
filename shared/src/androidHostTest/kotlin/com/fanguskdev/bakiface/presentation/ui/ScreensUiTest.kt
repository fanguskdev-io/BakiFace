package com.fanguskdev.bakiface.presentation.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.presentation.designsystem.BakiFaceTheme
import com.fanguskdev.bakiface.presentation.feature.camera.CameraIntent
import com.fanguskdev.bakiface.presentation.feature.camera.CameraPermission
import com.fanguskdev.bakiface.presentation.feature.camera.CameraScreen
import com.fanguskdev.bakiface.presentation.feature.camera.CameraStatus
import com.fanguskdev.bakiface.presentation.feature.camera.CameraUiState
import com.fanguskdev.bakiface.presentation.feature.editor.EditorIntent
import com.fanguskdev.bakiface.presentation.feature.editor.EditorScreen
import com.fanguskdev.bakiface.presentation.feature.editor.EditorUiState
import com.fanguskdev.bakiface.presentation.feature.effects.CharacterPanel
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import com.fanguskdev.bakiface.render.BitmapPicture
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The stateless screens, in Spanish, driven by their state and checked by what a person sees and does (ADR 0006).
 * With Robolectric's real graphics, so a frame of the camera can be drawn.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "es")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreensUiTest {
    @get:Rule
    val compose = createComposeRule()

    private val cameraIntents = mutableListOf<CameraIntent>()
    private val editorIntents = mutableListOf<EditorIntent>()

    private fun camera(state: CameraUiState, showFrameRate: Boolean = false) {
        compose.setContent {
            BakiFaceTheme(darkTheme = false) {
                CameraScreen(
                    state = state,
                    snackbar = remember { SnackbarHostState() },
                    onIntent = { cameraIntents += it },
                    showFrameRate = showFrameRate,
                )
            }
        }
    }

    private fun editor(state: EditorUiState) {
        compose.setContent {
            BakiFaceTheme(darkTheme = true) {
                EditorScreen(state = state, snackbar = remember { SnackbarHostState() }, onIntent = { editorIntents += it })
            }
        }
    }

    @Test
    fun whileTheSystemAsksTheViewfinderWaitsWithoutAPanel() {
        camera(CameraUiState(permission = CameraPermission.Unknown))

        compose.onNodeWithText("BakiFace necesita la cámara").assertDoesNotExist()
        compose.onNodeWithText("Permitir la cámara").assertDoesNotExist()
    }

    @Test
    fun aRefusalExplainsWhyAndAsksAgain() {
        camera(CameraUiState(permission = CameraPermission.Denied))

        compose.onNodeWithText("BakiFace necesita la cámara").assertIsDisplayed()
        compose.onNodeWithText("Permitir la cámara").performClick()

        assertEquals(listOf<CameraIntent>(CameraIntent.RequestPermission), cameraIntents)
    }

    @Test
    fun aRefusalForGoodSendsToTheSettings() {
        camera(CameraUiState(permission = CameraPermission.PermanentlyDenied))

        compose.onNodeWithText("Abrir Ajustes").performClick()
        compose.onNodeWithText("Elegir una foto").performClick()

        assertEquals(listOf(CameraIntent.OpenSettings, CameraIntent.PickPhoto), cameraIntents)
    }

    @Test
    fun withoutACameraAPhotoIsOffered() {
        camera(CameraUiState(permission = CameraPermission.Granted, status = CameraStatus.Unavailable))

        compose.onNodeWithText("No hay cámara disponible").assertIsDisplayed()
        compose.onNodeWithText("Elegir una foto").performClick()

        assertEquals(listOf<CameraIntent>(CameraIntent.PickPhoto), cameraIntents)
    }

    @Test
    fun aFailedCameraCanBeTriedAgain() {
        camera(CameraUiState(permission = CameraPermission.Granted, status = CameraStatus.Failed))

        compose.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<CameraIntent>(CameraIntent.RetryCamera), cameraIntents)
    }

    @Test
    fun theLiveCameraHasItsControlsNamed() {
        camera(CameraUiState(permission = CameraPermission.Granted, status = CameraStatus.Running))

        compose.onNodeWithContentDescription("Cambiar de cámara").performClick()
        compose.onNodeWithContentDescription("Todos los personajes").performClick()
        compose.onNodeWithContentDescription("Elegir una foto").performClick()
        compose.onNodeWithContentDescription("Hacer la foto sin efecto").assertIsNotEnabled()

        assertTrue(cameraIntents.first() is CameraIntent.ViewportMeasured, "the viewfinder measures itself first")
        assertEquals(
            listOf(CameraIntent.FlipCamera, CameraIntent.OpenGallery, CameraIntent.PickPhoto),
            cameraIntents.filterNot { it is CameraIntent.ViewportMeasured },
        )
    }

    @Test
    fun aDebugBuildShowsHowManyFramesReachTheScreen() {
        camera(liveAt(framesPerSecond = 29.6f), showFrameRate = true)

        compose.onNodeWithText("30 fps").assertIsDisplayed()
    }

    @Test
    fun aReleaseBuildKeepsTheFrameRateToItself() {
        camera(liveAt(framesPerSecond = 29.6f), showFrameRate = false)

        compose.onNodeWithText("30 fps").assertDoesNotExist()
    }

    private fun liveAt(framesPerSecond: Float) = CameraUiState(
        permission = CameraPermission.Granted,
        status = CameraStatus.Running,
        preview = PreviewFrame(
            picture = BitmapPicture(ImageBitmap(72, 128)),
            mirrored = true,
            landmarks = null,
            presence = 0f,
            render = null,
            timestampNanos = 0L,
            framesPerSecond = framesPerSecond,
        ),
    )

    @Test
    fun aShotThatIsGoneSendsBackToTheCamera() {
        editor(EditorUiState(isLoading = false, isMissing = true))

        compose.onNodeWithText("Esta foto ya no está").assertIsDisplayed()
        compose.onNodeWithText("Volver a la cámara").performClick()

        assertEquals(listOf<EditorIntent>(EditorIntent.Close), editorIntents)
    }

    @Test
    fun theEditorClosesFromItsButton() {
        editor(EditorUiState(exporting = ExportDestination.Gallery))

        compose.onNodeWithContentDescription("Cerrar").performClick()

        assertEquals(listOf<EditorIntent>(EditorIntent.Close), editorIntents)
    }

    @Test
    fun aCharacterPanelShowsItsNicknameAndDescription() {
        var chosen = false
        compose.setContent {
            BakiFaceTheme(darkTheme = false) {
                CharacterPanel(
                    item = EffectItem(EffectId("yujiro"), "Yujiro Hanma", "鬼", Argb(0xFFB3122EL), hint = null),
                    selected = true,
                    onClick = { chosen = true },
                )
            }
        }

        compose.onNodeWithText("El Ogro").assertIsDisplayed()
        compose.onNodeWithText("Yujiro Hanma").performClick()

        assertEquals(true, chosen)
    }
}
