package com.fanguskdev.bakiface.domain.camera

import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.render.FaceRender
import kotlinx.coroutines.flow.Flow

/** Which camera of the phone. */
enum class Lens {
    Front,
    Back,
    ;

    val other: Lens get() = if (this == Front) Back else Front
}

/**
 * One frame of the camera with the face the detector found on it, if any, in pixels of [picture] (ADR 0005).
 * [mirrored] says the frame has to be shown flipped, like a mirror, as the front camera is.
 */
data class CameraFrame(
    val picture: Picture,
    val landmarks: FaceLandmarks?,
    val mirrored: Boolean,
    val timestampNanos: Long,
)

/** Why the camera cannot give frames. */
sealed interface CameraError {
    /** The phone has no camera on that side (the iOS simulator has none at all). */
    data object NoCamera : CameraError

    /** The person has not let the app use the camera. */
    data object PermissionDenied : CameraError

    /** The platform failed to start or keep the camera; [reason] is for the log, not for the person. */
    data class Failed(val reason: String) : CameraError
}

/** The camera with face detection (ADR 0005). Implemented by the data layer with each platform's camera. */
interface FaceCameraRepository {
    /**
     * The frames of [lens], each with its face already detected. Cold: the camera starts when it is collected and
     * stops when the collection ends. A failure is emitted, and the flow ends after it.
     */
    fun frames(lens: Lens): Flow<AppResult<CameraFrame, CameraError>>
}

/**
 * A frame ready to be shown (ADR 0005): the camera's [picture], the smoothed face on it, how present that face is
 * and how to draw the chosen effect, if any. [framesPerSecond] is how fast frames are arriving, `null` until it can be
 * told (R-03).
 */
data class PreviewFrame(
    val picture: Picture,
    val mirrored: Boolean,
    val landmarks: FaceLandmarks?,
    val presence: Float,
    val render: FaceRender?,
    val timestampNanos: Long,
    val framesPerSecond: Float? = null,
) {
    /** Whether there is a face on screen right now. */
    val hasFace: Boolean get() = landmarks != null && presence > 0f
}
