package com.fanguskdev.bakiface.domain.usecase

import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.face.faceGeometryOf
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.picture.PictureEditor
import com.fanguskdev.bakiface.domain.picture.visibleRegion
import com.fanguskdev.bakiface.domain.render.FaceRender
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ExportError
import com.fanguskdev.bakiface.domain.shot.ExportedPhoto
import com.fanguskdev.bakiface.domain.shot.ImportError
import com.fanguskdev.bakiface.domain.shot.PhotoExportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoImportRepository
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.shot.Shot
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.domain.shot.ShotRepository

/**
 * Keeps what the person sees at the moment of the photo (ADR 0005): the part of the [PreviewFrame] that fills a
 * [viewWidth] × [viewHeight] screen, flipped like the screen shows it, with its smoothed face moved along.
 */
class CaptureShotUseCase(private val editor: PictureEditor, private val shots: ShotRepository) {
    suspend operator fun invoke(frame: PreviewFrame, viewWidth: Int, viewHeight: Int): Shot {
        val region = visibleRegion(frame.picture.width, frame.picture.height, viewWidth, viewHeight)
        val picture = editor.crop(frame.picture, region, frame.mirrored)
        val landmarks = frame.landmarks?.takeIf { frame.hasFace }?.map { point ->
            val x = point.x - region.left
            Point(if (frame.mirrored) region.width - x else x, point.y - region.top)
        }
        return shots.save(picture, landmarks)
    }
}

/** A shot the app is keeping, or `null` when it is gone. */
class GetShotUseCase(private val shots: ShotRepository) {
    suspend operator fun invoke(id: ShotId): Shot? = shots.get(id)
}

/** Lets go of a shot the person has finished with. */
class DiscardShotUseCase(private val shots: ShotRepository) {
    suspend operator fun invoke(id: ShotId) = shots.remove(id)
}

/** Brings a photo of the gallery in as a shot, with the face found on it. */
class ImportPhotoUseCase(private val photos: PhotoImportRepository, private val shots: ShotRepository) {
    suspend operator fun invoke(source: PhotoSource): AppResult<Shot, ImportError> = photos.import(source).let { result ->
        when (result) {
            is AppResult.Success -> AppResult.Success(shots.save(result.value.picture, result.value.landmarks))
            is AppResult.Failure -> result
        }
    }
}

/** How to draw a shot with [Effect] at an intensity from 0 to 1; `null` without an effect. */
class RenderShotUseCase(private val renderer: FaceRenderer) {
    operator fun invoke(shot: Shot, effect: Effect?, intensity: Float): FaceRender? = renderer.render(
        geometry = shot.landmarks?.let(::faceGeometryOf),
        effect = effect,
        intensity = intensity,
        presence = 1f,
        imageWidth = shot.picture.width,
        imageHeight = shot.picture.height,
    )
}

/** Draws a shot with its effect at full size and writes it to the gallery or for sharing. */
class ExportShotUseCase(private val renderShot: RenderShotUseCase, private val photos: PhotoExportRepository) {
    suspend operator fun invoke(
        shot: Shot,
        effect: Effect?,
        intensity: Float,
        destination: ExportDestination,
    ): AppResult<ExportedPhoto, ExportError> = photos.export(shot.picture, renderShot(shot, effect, intensity), destination)
}
