package com.fanguskdev.bakiface.data.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.CameraFrame
import com.fanguskdev.bakiface.domain.camera.FaceCameraRepository
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.render.BitmapPicture
import com.google.mlkit.common.MlKitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

private const val TAG = "BakiFaceCamera"
private const val FRAME_POOL_SIZE = 4
private const val BYTES_PER_PIXEL = 4

/**
 * The camera of an Android phone with face detection (ADR 0005): CameraX analyses 720p frames already turned upright
 * and in RGBA, each one is copied into a reusable bitmap and ML Kit finds the face on that same bitmap, so the frame
 * and its face always match. There is no preview view: the presentation draws these frames, warped.
 *
 * The camera runs while the flow is collected, bound to a lifecycle of its own that starts on collection and ends
 * with it.
 */
internal class AndroidFaceCameraRepository(private val context: Context) : FaceCameraRepository {
    override fun frames(lens: Lens): Flow<AppResult<CameraFrame, CameraError>> = callbackFlow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            send(AppResult.Failure(CameraError.PermissionDenied))
            close()
            return@callbackFlow
        }
        val provider = ProcessCameraProvider.awaitInstance(context)
        val selector = if (lens == Lens.Front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        if (!provider.hasCamera(selector)) {
            send(AppResult.Failure(CameraError.NoCamera))
            close()
            return@callbackFlow
        }
        val executor = Executors.newSingleThreadExecutor()
        val detector = mlKitFaceDetector(live = true)
        val pool = FramePool(FRAME_POOL_SIZE)
        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                    .setResolutionStrategy(ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER))
                    .build(),
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setOutputImageRotationEnabled(true)
            .build()
        analysis.setAnalyzer(executor) { proxy ->
            proxy.use {
                val bitmap = pool.copyOf(it)
                val landmarks = try {
                    detector.largestFace(bitmap)
                } catch (error: ExecutionException) {
                    Log.w(TAG, "The face detector failed on a frame", error)
                    null
                } catch (error: MlKitException) {
                    Log.w(TAG, "The face detector is not ready", error)
                    null
                } catch (error: CancellationException) {
                    // The camera is stopping and the detector was closed under this frame: it simply has no face.
                    null
                }
                trySend(
                    AppResult.Success(
                        CameraFrame(
                            picture = BitmapPicture(bitmap.asImageBitmap()),
                            landmarks = landmarks,
                            mirrored = lens == Lens.Front,
                            timestampNanos = it.imageInfo.timestamp,
                        ),
                    ),
                )
            }
        }
        val owner = CameraLifecycleOwner()
        try {
            withContext(Dispatchers.Main) {
                provider.bindToLifecycle(owner, selector, analysis)
                owner.start()
            }
        } catch (error: IllegalStateException) {
            send(AppResult.Failure(CameraError.Failed(error.message.orEmpty())))
        } catch (error: IllegalArgumentException) {
            send(AppResult.Failure(CameraError.Failed(error.message.orEmpty())))
        }
        awaitClose {
            analysis.clearAnalyzer()
            ContextCompat.getMainExecutor(context).execute {
                provider.unbind(analysis)
                owner.stop()
            }
            // On the analysis thread, after the frame in progress: the detector is never closed under a frame.
            executor.execute { detector.close() }
            executor.shutdown()
        }
    }.buffer(Channel.CONFLATED)

    /** The lifecycle the camera is bound to: alive exactly while frames are collected. Touched on the main thread only. */
    private class CameraLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry

        fun start() {
            registry.currentState = Lifecycle.State.RESUMED
        }

        fun stop() {
            registry.currentState = Lifecycle.State.DESTROYED
        }
    }

    /**
     * A few bitmaps the frames are copied into, taken in turn: by the time one is written again the screen has long
     * finished drawing it, and no frame allocates a new one.
     */
    private class FramePool(size: Int) {
        private val bitmaps = arrayOfNulls<Bitmap>(size)
        private var next = 0
        private var packed: ByteBuffer? = null

        fun copyOf(proxy: ImageProxy): Bitmap {
            val width = proxy.width
            val height = proxy.height
            val bitmap = bitmaps[next]?.takeIf { it.width == width && it.height == height }
                ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmaps[next] = it }
            next = (next + 1) % bitmaps.size
            val plane = proxy.planes[0]
            val source = plane.buffer.apply { rewind() }
            val rowBytes = width * BYTES_PER_PIXEL
            if (plane.rowStride == rowBytes) {
                bitmap.copyPixelsFromBuffer(source)
            } else {
                val target =
                    packed?.takeIf { it.capacity() == rowBytes * height }
                        ?: ByteBuffer.allocateDirect(rowBytes * height).also { packed = it }
                target.clear()
                for (row in 0 until height) {
                    source.limit(row * plane.rowStride + rowBytes)
                    source.position(row * plane.rowStride)
                    target.put(source)
                }
                source.limit(source.capacity())
                target.flip()
                bitmap.copyPixelsFromBuffer(target)
            }
            return bitmap
        }
    }
}
