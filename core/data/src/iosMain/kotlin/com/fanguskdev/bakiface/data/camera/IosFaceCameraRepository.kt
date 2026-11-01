package com.fanguskdev.bakiface.data.camera

import androidx.compose.ui.graphics.asComposeImageBitmap
import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.CameraFrame
import com.fanguskdev.bakiface.domain.camera.FaceCameraRepository
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.render.BitmapPicture
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1280x720
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferGetPresentationTimeStamp
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreVideo.CVPixelBufferGetBaseAddress
import platform.CoreVideo.CVPixelBufferGetBytesPerRow
import platform.CoreVideo.CVPixelBufferGetHeight
import platform.CoreVideo.CVPixelBufferGetWidth
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferRef
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelBufferLock_ReadOnly
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.ImageIO.kCGImagePropertyOrientationUp
import platform.Vision.VNImageRequestHandler
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import platform.posix.memcpy

private const val FRAME_POOL_SIZE = 4
private const val PORTRAIT_DEGREES = 90.0
private const val NANOS_PER_SECOND = 1_000_000_000.0

/**
 * The camera of an iPhone with face detection (ADR 0005): AVFoundation delivers 720p frames in BGRA, already turned
 * upright and, for the front camera, already flipped like a mirror by the connection itself; Vision finds the face on
 * each frame, and the frame is copied into a reusable Skia bitmap for the presentation to draw, warped.
 *
 * The session runs while the flow is collected. The simulator has no camera: it answers [CameraError.NoCamera].
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosFaceCameraRepository : FaceCameraRepository {
    private val sessionQueue = dispatch_queue_create("com.fanguskdev.bakiface.session", null)
    private val frameQueue = dispatch_queue_create("com.fanguskdev.bakiface.frames", null)

    /** The output holds its delegate weakly: the repository keeps it alive while its session runs. */
    private val runningDelegates = mutableSetOf<FrameDelegate>()

    override fun frames(lens: Lens): Flow<AppResult<CameraFrame, CameraError>> = callbackFlow {
        if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) != AVAuthorizationStatusAuthorized) {
            send(AppResult.Failure(CameraError.PermissionDenied))
            close()
            return@callbackFlow
        }
        val position = if (lens == Lens.Front) AVCaptureDevicePositionFront else AVCaptureDevicePositionBack
        val device = AVCaptureDevice.defaultDeviceWithDeviceType(AVCaptureDeviceTypeBuiltInWideAngleCamera, AVMediaTypeVideo, position)
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
        if (input == null) {
            send(AppResult.Failure(CameraError.NoCamera))
            close()
            return@callbackFlow
        }
        val session = AVCaptureSession()
        session.beginConfiguration()
        session.sessionPreset = AVCaptureSessionPreset1280x720
        val output = AVCaptureVideoDataOutput()
        output.videoSettings = mapOf<Any?, Any?>(pixelFormatKey() to NSNumber(unsignedInt = kCVPixelFormatType_32BGRA))
        output.alwaysDiscardsLateVideoFrames = true
        val delegate = FrameDelegate { frame -> trySend(AppResult.Success(frame)) }
        runningDelegates += delegate
        output.setSampleBufferDelegate(delegate, frameQueue)
        if (!session.canAddInput(input) || !session.canAddOutput(output)) {
            runningDelegates -= delegate
            send(AppResult.Failure(CameraError.Failed("The capture session refused the camera")))
            close()
            return@callbackFlow
        }
        session.addInput(input)
        session.addOutput(output)
        output.connectionWithMediaType(AVMediaTypeVideo)?.let { connection -> orient(connection, mirror = lens == Lens.Front) }
        session.commitConfiguration()
        dispatch_async(sessionQueue) { session.startRunning() }
        awaitClose {
            output.setSampleBufferDelegate(null, null)
            dispatch_async(sessionQueue) { session.stopRunning() }
            runningDelegates -= delegate
        }
    }.buffer(Channel.CONFLATED)

    private fun orient(connection: AVCaptureConnection, mirror: Boolean) {
        if (connection.isVideoRotationAngleSupported(PORTRAIT_DEGREES)) connection.videoRotationAngle = PORTRAIT_DEGREES
        if (connection.supportsVideoMirroring) {
            connection.automaticallyAdjustsVideoMirroring = false
            connection.videoMirrored = mirror
        }
    }

    /** `kCVPixelBufferPixelFormatTypeKey` is a CoreFoundation string; NSString is its toll-free twin. */
    private fun pixelFormatKey(): Any =
        kCVPixelBufferPixelFormatTypeKey?.let { interpretObjCPointer<NSString>(it.rawValue) } ?: "PixelFormatType"

    /** Receives the frames on the frame queue: detects the face, copies the pixels and hands the frame over. */
    private class FrameDelegate(private val onFrame: (CameraFrame) -> Unit) :
        NSObject(),
        AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
        private val request = faceLandmarksRequest()
        private val pool = SkiaFramePool(FRAME_POOL_SIZE)

        @ObjCSignatureOverride
        override fun captureOutput(
            output: AVCaptureOutput,
            didOutputSampleBuffer: CMSampleBufferRef?,
            fromConnection: AVCaptureConnection,
        ) {
            val pixels = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return
            VNImageRequestHandler(cVPixelBuffer = pixels, orientation = kCGImagePropertyOrientationUp, options = emptyMap<Any?, Any>())
                .performLogged(request)
            val width = CVPixelBufferGetWidth(pixels).toInt()
            val height = CVPixelBufferGetHeight(pixels).toInt()
            val landmarks = request.largestFace()?.toLandmarks(width, height)
            val bitmap = pool.copyOf(pixels) ?: return
            val seconds = CMTimeGetSeconds(CMSampleBufferGetPresentationTimeStamp(didOutputSampleBuffer))
            onFrame(
                CameraFrame(
                    picture = BitmapPicture(bitmap.asComposeImageBitmap()),
                    landmarks = landmarks,
                    mirrored = false,
                    timestampNanos = (seconds * NANOS_PER_SECOND).toLong(),
                ),
            )
        }
    }

    /**
     * A few Skia bitmaps the frames are copied into, taken in turn, each with its own byte buffer: by the time one is
     * written again the screen has long finished drawing it.
     */
    private class SkiaFramePool(size: Int) {
        private val bitmaps = Array(size) { Bitmap() }
        private val buffers = arrayOfNulls<ByteArray>(size)
        private var next = 0

        fun copyOf(pixels: CVPixelBufferRef): Bitmap? {
            CVPixelBufferLockBaseAddress(pixels, kCVPixelBufferLock_ReadOnly)
            try {
                val width = CVPixelBufferGetWidth(pixels).toInt()
                val height = CVPixelBufferGetHeight(pixels).toInt()
                val rowBytes = CVPixelBufferGetBytesPerRow(pixels).toInt()
                val base = CVPixelBufferGetBaseAddress(pixels) ?: return null
                val byteCount = rowBytes * height
                val slot = next
                next = (next + 1) % bitmaps.size
                val bytes = buffers[slot]?.takeIf { it.size == byteCount } ?: ByteArray(byteCount).also { buffers[slot] = it }
                bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), base, byteCount.convert()) }
                val bitmap = bitmaps[slot]
                bitmap.installPixels(ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE), bytes, rowBytes)
                return bitmap
            } finally {
                CVPixelBufferUnlockBaseAddress(pixels, kCVPixelBufferLock_ReadOnly)
            }
        }
    }
}
