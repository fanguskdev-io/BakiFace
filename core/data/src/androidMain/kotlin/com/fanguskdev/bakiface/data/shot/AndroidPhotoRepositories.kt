package com.fanguskdev.bakiface.data.shot

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fanguskdev.bakiface.data.camera.largestFace
import com.fanguskdev.bakiface.data.camera.mlKitFaceDetector
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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException
import kotlin.math.max

private const val TAG = "BakiFacePhotos"
private const val MAX_PHOTO_SIDE = 2048
private const val JPEG_QUALITY = 92
private const val SHARED_FOLDER = "shared"
private const val GALLERY_FOLDER = "BakiFace"
private const val SHARED_FILES_KEPT_MILLIS = 24L * 60 * 60 * 1000

/**
 * Reads a photo picked with the system's photo picker (a `content://` address): decoded upright (ImageDecoder applies
 * the EXIF rotation), no larger than [MAX_PHOTO_SIDE] on its long side, in software memory so it can be drawn
 * anywhere, and with its face found by the accurate ML Kit detector.
 */
internal class AndroidPhotoImportRepository(private val context: Context, private val dispatchers: AppDispatchers) :
    PhotoImportRepository {
    override suspend fun import(source: PhotoSource): AppResult<ImportedPhoto, ImportError> = withContext(dispatchers.io) {
        val bitmap = try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, Uri.parse(source.token))) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = max(info.size.width, info.size.height)
                if (longest > MAX_PHOTO_SIDE) {
                    val scale = MAX_PHOTO_SIDE.toFloat() / longest
                    decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
                }
            }
        } catch (error: IOException) {
            Log.w(TAG, "The picked photo could not be decoded", error)
            return@withContext AppResult.Failure(ImportError.Unreadable)
        } catch (error: SecurityException) {
            Log.w(TAG, "The picked photo could not be read", error)
            return@withContext AppResult.Failure(ImportError.Failed(error.message.orEmpty()))
        }
        val detector = mlKitFaceDetector(live = false)
        val landmarks = try {
            detector.largestFace(bitmap)
        } catch (error: ExecutionException) {
            Log.w(TAG, "The face detector failed on the photo", error)
            null
        } finally {
            detector.close()
        }
        AppResult.Success(ImportedPhoto(BitmapPicture(bitmap.asImageBitmap()), landmarks))
    }
}

/**
 * Writes finished photos as JPEG (ADR 0007): to the gallery through MediaStore, in `Pictures/BakiFace`, which needs no
 * permission from Android 10 on; or to the app's cache for the share sheet, cleaning the files older than a day.
 */
internal class AndroidPhotoExportRepository(private val context: Context, private val dispatchers: AppDispatchers) :
    PhotoExportRepository {
    override suspend fun export(
        picture: Picture,
        render: FaceRender?,
        destination: ExportDestination,
    ): AppResult<ExportedPhoto, ExportError> = withContext(dispatchers.default) {
        val photo = composePicture(picture.asImageBitmap(), render).asAndroidBitmap()
        try {
            when (destination) {
                ExportDestination.Gallery -> saveToGallery(photo)
                ExportDestination.Share -> saveForSharing(photo)
            }
        } catch (error: IOException) {
            AppResult.Failure(ExportError.Failed(error.message.orEmpty()))
        } catch (error: SecurityException) {
            AppResult.Failure(ExportError.PermissionDenied)
        }
    }

    private fun saveToGallery(photo: Bitmap): AppResult<ExportedPhoto, ExportError> {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$GALLERY_FOLDER")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
            ?: return AppResult.Failure(ExportError.Failed("MediaStore did not create the photo"))
        val written = resolver.openOutputStream(uri)?.use { photo.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) } ?: false
        if (!written) {
            resolver.delete(uri, null, null)
            return AppResult.Failure(ExportError.Failed("The photo could not be written"))
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return AppResult.Success(ExportedPhoto(uri.toString()))
    }

    private fun saveForSharing(photo: Bitmap): AppResult<ExportedPhoto, ExportError> {
        val folder = File(context.cacheDir, SHARED_FOLDER).apply { mkdirs() }
        val now = System.currentTimeMillis()
        folder.listFiles()?.filter { now - it.lastModified() > SHARED_FILES_KEPT_MILLIS }?.forEach { it.delete() }
        val file = File(folder, fileName())
        file.outputStream().use { photo.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        return AppResult.Success(ExportedPhoto(file.absolutePath))
    }

    private fun fileName(): String = "BakiFace_${System.currentTimeMillis()}.jpg"
}
