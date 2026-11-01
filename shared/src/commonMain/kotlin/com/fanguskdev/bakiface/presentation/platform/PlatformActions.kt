package com.fanguskdev.bakiface.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.presentation.feature.camera.CameraPermission

/** Asks the system for the camera. */
@Stable
fun interface CameraPermissionController {
    fun request()
}

/**
 * Reports the camera permission to [onChanged] when the screen comes to the front (also back from the system's
 * settings) and after every request; [CameraPermissionController.request] shows the system's question. Each platform
 * reads and asks in its own way (ADR 0003: the UI is the only layer that talks to the system's screens).
 */
@Composable
expect fun rememberCameraPermissionController(onChanged: (CameraPermission) -> Unit): CameraPermissionController

/** Opens the system's photo picker; [onPicked] gets the chosen photo, nothing if the person backs out. */
@Composable
expect fun rememberPhotoPicker(onPicked: (PhotoSource) -> Unit): () -> Unit

/** Opens the system's share sheet with the JPEG at a path of the app's cache. */
@Composable
expect fun rememberShareSheet(): (path: String) -> Unit

/** Opens the system's settings page of this app, where the camera can be turned on again. */
@Composable
expect fun rememberAppSettingsOpener(): () -> Unit

/** Whether the person asked the system for less motion: the screens then skip their decorative animation. */
@Composable
expect fun rememberReduceMotion(): Boolean
