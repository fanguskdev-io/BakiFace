package com.fanguskdev.bakiface.data.shot

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.fanguskdev.bakiface.data.camera.faceLandmarksRequest
import com.fanguskdev.bakiface.data.camera.largestFace
import com.fanguskdev.bakiface.data.camera.performLogged
import com.fanguskdev.bakiface.data.camera.toLandmarks
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.render.FaceRender
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ExportError
import com.fanguskdev.bakiface.domain.shot.ExportedPhoto
import com.fanguskdev.bakiface.domain.shot.ImportError
import com.fanguskdev.bakiface.domain.shot.ImportedPhoto
import com.fanguskdev.bakiface.domain.shot.PhotoExportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoImportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.render.BitmapPicture
import com.fanguskdev.bakiface.render.asImageBitmap
import com.fanguskdev.bakiface.render.composePicture
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.dataWithBytes
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetCreationRequest
import platform.Photos.PHAssetResourceTypePhoto
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.Vision.VNImageRequestHandler
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val MAX_PHOTO_SIDE = 2048.0
private const val JPEG_QUALITY = 92
private const val IMPORT_JPEG_QUALITY = 0.95

/**
 * Reads a photo the picker left in a temporary file: redrawn upright (UIKit applies the EXIF orientation), no larger
 * than [MAX_PHOTO_SIDE] on its long side, with its face found by Vision.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosPhotoImportRepository(private val dispatchers: AppDispatchers) : PhotoImportRepository {
    override suspend fun import(source: PhotoSource): AppResult<ImportedPhoto, ImportError> = withContext(dispatchers.io) {
        val original = UIImage.imageWithContentsOfFile(source.token) ?: return@withContext AppResult.Failure(ImportError.Unreadable)
        val upright = original.uprightAndScaled()
        val cgImage = upright.CGImage ?: return@withContext AppResult.Failure(ImportError.Unreadable)
        val width = CGImageGetWidth(cgImage).toInt()
        val height = CGImageGetHeight(cgImage).toInt()
        val request = faceLandmarksRequest()
        VNImageRequestHandler(cGImage = cgImage, options = emptyMap<Any?, Any>()).performLogged(request)
        val landmarks = request.largestFace()?.toLandmarks(width, height)
        val encoded = UIImageJPEGRepresentation(upright, IMPORT_JPEG_QUALITY)?.toByteArray()
            ?: return@withContext AppResult.Failure(ImportError.Unreadable)
        AppResult.Success(ImportedPhoto(BitmapPicture(Image.makeFromEncoded(encoded).toComposeImageBitmap()), landmarks))
    }

    private fun UIImage.uprightAndScaled(): UIImage {
        val (width, height) = size.useContents { width to height }
        val scale = min(1.0, MAX_PHOTO_SIDE / max(width, height))
        val targetWidth = floor(width * scale)
        val targetHeight = floor(height * scale)
        val format = UIGraphicsImageRendererFormat.defaultFormat().apply { this.scale = 1.0 }
        return UIGraphicsImageRenderer(size = CGSizeMake(targetWidth, targetHeight), format = format).imageWithActions {
            drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
        }
    }
}

/**
 * Writes finished photos as JPEG (ADR 0007): to Photos, asking only to add (PhotoKit's add-only access), or to a
 * temporary file for the share sheet.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosPhotoExportRepository(private val dispatchers: AppDispatchers) : PhotoExportRepository {
    override suspend fun export(
        picture: Picture,
        render: FaceRender?,
        destination: ExportDestination,
    ): AppResult<ExportedPhoto, ExportError> {
        val jpeg = withContext(dispatchers.default) {
            val photo = composePicture(picture.asImageBitmap(), render)
            Image.makeFromBitmap(photo.asSkiaBitmap()).encodeToData(EncodedImageFormat.JPEG, JPEG_QUALITY)?.bytes
        } ?: return AppResult.Failure(ExportError.Failed("The photo could not be encoded"))
        return when (destination) {
            ExportDestination.Share -> withContext(dispatchers.io) { saveForSharing(jpeg) }
            ExportDestination.Gallery -> saveToPhotos(jpeg)
        }
    }

    private fun saveForSharing(jpeg: ByteArray): AppResult<ExportedPhoto, ExportError> {
        val path = NSTemporaryDirectory() + "BakiFace_${(NSDate().timeIntervalSince1970 * 1000).toLong()}.jpg"
        return if (jpeg.toNSData().writeToFile(path, atomically = true)) {
            AppResult.Success(ExportedPhoto(path))
        } else {
            AppResult.Failure(ExportError.Failed("The photo could not be written"))
        }
    }

    private suspend fun saveToPhotos(jpeg: ByteArray): AppResult<ExportedPhoto, ExportError> = suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { status ->
            if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) {
                continuation.resume(AppResult.Failure(ExportError.PermissionDenied))
                return@requestAuthorizationForAccessLevel
            }
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                changeBlock = {
                    PHAssetCreationRequest.creationRequestForAsset().addResourceWithType(
                        PHAssetResourceTypePhoto,
                        data = jpeg.toNSData(),
                        options = null,
                    )
                },
                completionHandler = { success, error ->
                    continuation.resume(
                        if (success) {
                            AppResult.Success(ExportedPhoto("photos"))
                        } else {
                            AppResult.Failure(ExportError.Failed(error?.localizedDescription.orEmpty()))
                        },
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData = usePinned { pinned -> NSData.dataWithBytes(pinned.addressOf(0), size.convert()) }

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, length) }
    return bytes
}
