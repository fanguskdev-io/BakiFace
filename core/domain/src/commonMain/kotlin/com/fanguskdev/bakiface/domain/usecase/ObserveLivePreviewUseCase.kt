package com.fanguskdev.bakiface.domain.usecase

import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.domain.camera.CameraFrame
import com.fanguskdev.bakiface.domain.camera.FaceCameraRepository
import com.fanguskdev.bakiface.domain.camera.FrameRateMeter
import com.fanguskdev.bakiface.domain.camera.Lens
import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.common.map
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.face.FaceTracker
import com.fanguskdev.bakiface.domain.face.TrackedFace
import com.fanguskdev.bakiface.domain.face.faceGeometryOf
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * The camera with the chosen effect on it, frame by frame (ADR 0005): each frame's face is smoothed by a
 * [FaceTracker] of its own session and drawn with the latest [Effect] of the flow passed in. Changing the effect
 * re-draws the last frame at once, without waiting for the next one. Each frame also carries the rate at which frames
 * are arriving, measured by a [FrameRateMeter] of the same session (R-03).
 */
class ObserveLivePreviewUseCase(
    private val camera: FaceCameraRepository,
    private val renderer: FaceRenderer,
    private val newTracker: () -> FaceTracker = { FaceTracker() },
) {
    operator fun invoke(lens: Lens, effect: Flow<Effect?>): Flow<AppResult<PreviewFrame, CameraError>> {
        val tracked = flow {
            val tracker = newTracker()
            val meter = FrameRateMeter()
            camera.frames(lens).collect { result ->
                emit(
                    result.map { frame ->
                        TrackedFrame(
                            frame = frame,
                            face = tracker.track(frame.landmarks, frame.timestampNanos),
                            framesPerSecond = meter.onFrame(frame.timestampNanos),
                        )
                    },
                )
            }
        }
        return combine(tracked, effect) { result, chosen -> result.map { draw(it, chosen) } }
    }

    private fun draw(tracked: TrackedFrame, effect: Effect?): PreviewFrame {
        val face = tracked.face
        val picture = tracked.frame.picture
        val presence = face?.presence ?: 0f
        return PreviewFrame(
            picture = picture,
            mirrored = tracked.frame.mirrored,
            landmarks = face?.landmarks,
            presence = presence,
            render = renderer.render(
                geometry = face?.let { faceGeometryOf(it.landmarks) },
                effect = effect,
                intensity = 1f,
                presence = presence,
                imageWidth = picture.width,
                imageHeight = picture.height,
            ),
            timestampNanos = tracked.frame.timestampNanos,
            framesPerSecond = tracked.framesPerSecond,
        )
    }

    private class TrackedFrame(val frame: CameraFrame, val face: TrackedFace?, val framesPerSecond: Float?)
}
