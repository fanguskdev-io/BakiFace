package com.fanguskdev.bakiface.presentation.feature.camera

import androidx.compose.runtime.Immutable
import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Whether the app may use the camera, as the platform last reported it. */
enum class CameraPermission {
    /** Not asked yet: the screen explains before the system asks. */
    Unknown,
    Granted,

    /** Refused, but the system can still ask again. */
    Denied,

    /** Refused for good: only the system's settings can change it. */
    PermanentlyDenied,
}

/** What the camera is doing. */
enum class CameraStatus {
    Idle,
    Starting,
    Running,

    /** This device has no camera on that side (the iOS simulator has none at all). */
    Unavailable,
    Failed,
}

/**
 * Everything the camera screen shows, and nothing else (ADR 0003): the one state of the screen, immutable, exposed as a
 * `StateFlow` by [CameraViewModel]. [effects] starts with [EffectItem.Original].
 */
@Immutable
data class CameraUiState(
    val permission: CameraPermission = CameraPermission.Unknown,
    val status: CameraStatus = CameraStatus.Idle,
    val lens: Lens = Lens.Front,
    val effects: ImmutableList<EffectItem> = persistentListOf(EffectItem.Original),
    val isCatalogLoaded: Boolean = false,
    val selectedEffectId: EffectId? = null,
    val preview: PreviewFrame? = null,
    val viewportWidth: Int = 0,
    val viewportHeight: Int = 0,
    val isCapturing: Boolean = false,
    val isImporting: Boolean = false,
    val isGalleryOpen: Boolean = false,
) {
    val selectedEffect: EffectItem get() = effects.firstOrNull { it.id == selectedEffectId } ?: EffectItem.Original

    val selectedIndex: Int get() = effects.indexOfFirst { it.id == selectedEffectId }.coerceAtLeast(0)

    val isFaceVisible: Boolean get() = preview?.hasFace == true

    val canCapture: Boolean get() = status == CameraStatus.Running && preview != null && viewportWidth > 0 && !isCapturing && !isImporting
}

/** Everything the person can do on the camera screen. The screen sends them to [CameraViewModel.onIntent], and only there. */
sealed interface CameraIntent {
    /** The platform reports what the person decided about the camera. */
    data class PermissionChanged(val permission: CameraPermission) : CameraIntent

    data object RequestPermission : CameraIntent

    data object OpenSettings : CameraIntent

    /** The screen is on view: the camera may run. */
    data object ScreenShown : CameraIntent

    /** The screen left the view (another screen, the app in the background): the camera stops. */
    data object ScreenHidden : CameraIntent

    data class ViewportMeasured(val width: Int, val height: Int) : CameraIntent

    data class SelectEffect(val id: EffectId?) : CameraIntent

    data object FlipCamera : CameraIntent

    data object Capture : CameraIntent

    data object RetryCamera : CameraIntent

    data object PickPhoto : CameraIntent

    data class PhotoPicked(val source: PhotoSource) : CameraIntent

    data object OpenGallery : CameraIntent

    data object CloseGallery : CameraIntent
}

/** Something to do once (ADR 0003): never part of the state, delivered through a channel. */
sealed interface CameraEffect {
    data object RequestCameraPermission : CameraEffect

    data object OpenAppSettings : CameraEffect

    data object LaunchPhotoPicker : CameraEffect

    /** Opens the editor on [shotId] with the character chosen at that moment. */
    data class OpenEditor(val shotId: ShotId, val effectId: EffectId?) : CameraEffect

    data class ShowMessage(val message: CameraMessage) : CameraEffect
}

/** What the screen may have to tell the person in passing. */
enum class CameraMessage {
    CaptureFailed,
    ImportUnreadable,
    ImportFailed,
    CatalogFailed,
}

/** What happened, as the reducer reads it: the outcome of an intent or of a use case. */
sealed interface CameraResult {
    data class EffectsLoaded(val effects: ImmutableList<EffectItem>) : CameraResult

    data class EffectSelected(val id: EffectId?) : CameraResult

    data class PermissionUpdated(val permission: CameraPermission) : CameraResult

    data object PreviewStarting : CameraResult

    data class FrameArrived(val frame: PreviewFrame) : CameraResult

    data class CameraFailed(val error: CameraError) : CameraResult

    data object PreviewStopped : CameraResult

    data class ViewportChanged(val width: Int, val height: Int) : CameraResult

    data class LensChanged(val lens: Lens) : CameraResult

    data class Capturing(val active: Boolean) : CameraResult

    data class Importing(val active: Boolean) : CameraResult

    data class GalleryToggled(val open: Boolean) : CameraResult
}
