package com.fanguskdev.bakiface.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.presentation.feature.camera.CameraPermission
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/** iOS asks only once: after a "no", only the Settings app can change it, so a refusal is always for good. */
@Composable
actual fun rememberCameraPermissionController(onChanged: (CameraPermission) -> Unit): CameraPermissionController {
    val currentOnChanged by rememberUpdatedState(onChanged)
    LifecycleResumeEffect(Unit) {
        currentOnChanged(cameraPermission())
        onPauseOrDispose {}
    }
    return remember {
        CameraPermissionController {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                dispatch_async(dispatch_get_main_queue()) {
                    currentOnChanged(if (granted) CameraPermission.Granted else CameraPermission.PermanentlyDenied)
                }
            }
        }
    }
}

private fun cameraPermission(): CameraPermission = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
    AVAuthorizationStatusAuthorized -> CameraPermission.Granted
    AVAuthorizationStatusNotDetermined -> CameraPermission.Unknown
    else -> CameraPermission.PermanentlyDenied
}

/**
 * PhotoKit's picker, which needs no permission to read the photo library: the chosen photo is copied to a temporary
 * file, and that path is what travels to the data layer.
 */
@Composable
actual fun rememberPhotoPicker(onPicked: (PhotoSource) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val delegate = remember { PhotoPickerDelegate { path -> currentOnPicked(PhotoSource(path)) } }
    return remember(delegate) {
        {
            val configuration = PHPickerConfiguration().apply {
                filter = PHPickerFilter.imagesFilter
                selectionLimit = 1
            }
            val picker = PHPickerViewController(configuration = configuration)
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class PhotoPickerDelegate(private val onPicked: (String) -> Unit) :
    NSObject(),
    PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
        result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
            val path = "${NSTemporaryDirectory()}picked-${NSUUID().UUIDString}"
            if (data != null && data.writeToFile(path, atomically = true)) {
                dispatch_async(dispatch_get_main_queue()) { onPicked(path) }
            }
        }
    }
}

/** UIKit's share sheet; on iPad it hangs from the middle of the screen. */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberShareSheet(): (path: String) -> Unit = remember {
    { path ->
        val top = topViewController()
        val sheet = UIActivityViewController(activityItems = listOf(NSURL.fileURLWithPath(path)), applicationActivities = null)
        sheet.popoverPresentationController?.let { popover ->
            popover.sourceView = top?.view
            top?.view?.bounds?.useContents { popover.sourceRect = CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0) }
        }
        top?.presentViewController(sheet, animated = true, completion = null)
    }
}

@Composable
actual fun rememberAppSettingsOpener(): () -> Unit = remember {
    {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let { url ->
            UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
        }
    }
}

@Composable
actual fun rememberReduceMotion(): Boolean = remember { UIAccessibilityIsReduceMotionEnabled() }

/** The view controller on top of the key window, the one that can present another. */
private fun topViewController(): UIViewController? {
    val windows = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
    var top = (windows.firstOrNull { it.keyWindow } ?: windows.firstOrNull())?.rootViewController
    while (true) {
        top = top?.presentedViewController ?: break
    }
    return top
}
