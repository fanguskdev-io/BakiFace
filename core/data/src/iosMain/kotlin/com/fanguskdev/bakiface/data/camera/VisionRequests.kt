package com.fanguskdev.bakiface.data.camera

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreML.MLAllComputeDevices
import platform.CoreML.MLCPUComputeDevice
import platform.CoreML.MLComputeDeviceProtocolProtocol
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.NSProcessInfo
import platform.Vision.VNComputeStageMain
import platform.Vision.VNComputeStagePostProcessing
import platform.Vision.VNDetectFaceLandmarksRequest
import platform.Vision.VNImageRequestHandler
import platform.Vision.setComputeDevice

/** Whether the app runs in the iOS simulator, which has neither a Neural Engine nor a camera. */
internal val isSimulator: Boolean = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null

/**
 * A request for the landmarks of the faces of a picture. In the simulator Vision cannot create its inference context
 * on the GPU or the Neural Engine (error 9), so there it runs on the CPU; a phone keeps the fast default.
 */
@OptIn(ExperimentalForeignApi::class)
@Suppress("DEPRECATION")
internal fun faceLandmarksRequest(): VNDetectFaceLandmarksRequest = VNDetectFaceLandmarksRequest().apply {
    if (isSimulator) {
        // The per-stage setting alone leaves the face rectangle detection inside the request on the GPU, which the
        // simulator cannot use: usesCPUOnly, deprecated but still honoured, moves every inner request to the CPU.
        usesCPUOnly = true
        val cpu = MLAllComputeDevices().firstOrNull { it is MLCPUComputeDevice } as? MLComputeDeviceProtocolProtocol
        if (cpu != null) {
            setComputeDevice(cpu, forComputeStage = VNComputeStageMain)
            setComputeDevice(cpu, forComputeStage = VNComputeStagePostProcessing)
        }
    }
}

/** Runs [request] on this picture; a failure is logged and leaves the request without results. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun VNImageRequestHandler.performLogged(request: VNDetectFaceLandmarksRequest) {
    memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        if (!performRequests(listOf(request), error.ptr)) {
            log("Vision could not look for faces: ${error.value?.localizedDescription ?: "unknown error"}")
        }
    }
}

/**
 * Writes [message] to the system log. The message is formatted here and passed as the format itself, with its `%`
 * escaped: a Kotlin string is not a valid variadic argument of NSLog, and passing one crashes the app.
 */
@OptIn(BetaInteropApi::class)
internal fun log(message: String) {
    NSLog("BakiFace: ${message.replace("%", "%%")}")
}
