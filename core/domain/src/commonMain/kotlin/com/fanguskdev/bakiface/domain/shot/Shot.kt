package com.fanguskdev.bakiface.domain.shot

import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.render.FaceRender
import kotlin.jvm.JvmInline

/** The name of a [Shot] while the app keeps it. */
@JvmInline
value class ShotId(val value: String)

/**
 * A still picture to finish in the editor (ADR 0005): a frame taken with the camera, cropped to what the person saw,
 * or a photo from the gallery, with the face found on it, if any.
 */
data class Shot(val id: ShotId, val picture: Picture, val landmarks: FaceLandmarks?)

/** Keeps the shots while the app is open: a shot is too big to travel in a navigation route. */
interface ShotRepository {
    suspend fun save(picture: Picture, landmarks: FaceLandmarks?): Shot

    suspend fun get(id: ShotId): Shot?

    suspend fun remove(id: ShotId)
}

/** A photo the person picked in the system's picker: a content address on Android, a file path on iOS. */
@JvmInline
value class PhotoSource(val token: String)

/** A photo read from the gallery, with the face found on it, if any. */
data class ImportedPhoto(val picture: Picture, val landmarks: FaceLandmarks?)

/** Why a photo could not be brought in. */
sealed interface ImportError {
    /** It is not an image the platform can read. */
    data object Unreadable : ImportError

    /** Something else failed; [reason] is for the log. */
    data class Failed(val reason: String) : ImportError
}

/** Reads photos picked from the gallery and finds the face on them. */
interface PhotoImportRepository {
    suspend fun import(source: PhotoSource): AppResult<ImportedPhoto, ImportError>
}

/** Where a finished photo goes. */
enum class ExportDestination {
    /** The phone's photo gallery. */
    Gallery,

    /** A temporary file for the system's share sheet. */
    Share,
}

/** A finished photo: where it was written ([location], a file path or the gallery's address for it). */
data class ExportedPhoto(val location: String)

/** Why a finished photo could not be written. */
sealed interface ExportError {
    /** The person has not let the app add photos to the gallery. */
    data object PermissionDenied : ExportError

    /** Something else failed; [reason] is for the log. */
    data class Failed(val reason: String) : ExportError
}

/** Draws a picture with its effect at full size and writes it as a JPEG to the gallery or for sharing. */
interface PhotoExportRepository {
    suspend fun export(picture: Picture, render: FaceRender?, destination: ExportDestination): AppResult<ExportedPhoto, ExportError>
}
