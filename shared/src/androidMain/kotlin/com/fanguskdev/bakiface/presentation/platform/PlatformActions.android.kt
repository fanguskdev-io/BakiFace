package com.fanguskdev.bakiface.presentation.platform

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.presentation.feature.camera.CameraPermission
import java.io.File

/**
 * Android tells "not asked yet" from "refused for good" only through the rationale flag after a request, so the
 * controller remembers whether it has asked: before, [CameraPermission.Unknown]; after a refusal, [CameraPermission.Denied]
 * while the system can still ask and [CameraPermission.PermanentlyDenied] when it cannot.
 */
@Composable
actual fun rememberCameraPermissionController(onChanged: (CameraPermission) -> Unit): CameraPermissionController {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnChanged by rememberUpdatedState(onChanged)
    var asked by rememberSaveable { mutableStateOf(false) }
    val status = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED -> CameraPermission.Granted
            !asked -> CameraPermission.Unknown
            activity != null &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CAMERA,
                ) -> CameraPermission.Denied
            else -> CameraPermission.PermanentlyDenied
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        asked = true
        currentOnChanged(status())
    }
    LifecycleResumeEffect(Unit) {
        currentOnChanged(status())
        onPauseOrDispose {}
    }
    return remember(launcher) { CameraPermissionController { launcher.launch(Manifest.permission.CAMERA) } }
}

/** The system's photo picker: no storage permission needed, and it gives a `content://` address to read once. */
@Composable
actual fun rememberPhotoPicker(onPicked: (PhotoSource) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) currentOnPicked(PhotoSource(uri.toString()))
    }
    return remember(launcher) { { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) } }
}

/** Shares through the app's FileProvider (`<package>.fileprovider`, declared by the app), granting only a read. */
@Composable
actual fun rememberShareSheet(): (path: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { path ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, null))
        }
    }
}

@Composable
actual fun rememberAppSettingsOpener(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
            )
        }
    }
}

/** Android's "remove animations" sets the animator duration scale to zero. */
@Composable
actual fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
