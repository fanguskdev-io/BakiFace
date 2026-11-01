package com.fanguskdev.bakiface.data.camera

import android.graphics.Bitmap
import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.geometry.Point
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private const val MIN_FACE_SIZE = 0.15f

/**
 * ML Kit's face detector, bundled in the app so it works without Google Play services and without a download
 * (ADR 0006). [live] trades a little precision for speed, for the camera; otherwise it is the accurate one, for photos.
 */
internal fun mlKitFaceDetector(live: Boolean): FaceDetector = FaceDetection.getClient(
    FaceDetectorOptions.Builder()
        .setPerformanceMode(if (live) FaceDetectorOptions.PERFORMANCE_MODE_FAST else FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(MIN_FACE_SIZE)
        .build(),
)

/** The largest face of [bitmap], blocking the calling thread (never the main one) until ML Kit answers. */
internal fun FaceDetector.largestFace(bitmap: Bitmap): FaceLandmarks? = Tasks.await(process(InputImage.fromBitmap(bitmap, 0)))
    .maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
    ?.toLandmarks()

/**
 * ML Kit's contours as the domain's landmarks, in pixels of the bitmap: the two lines of each eyebrow join in one
 * region, and the four lines of the lips make the outer and the inner outline. Without contours (ML Kit only gives
 * them for the most prominent face) there is nothing to warp.
 */
internal fun Face.toLandmarks(): FaceLandmarks? {
    fun contour(type: Int): ImmutableList<Point> = getContour(type)?.points.orEmpty().map { Point(it.x, it.y) }.toImmutableList()

    val leftEye = contour(FaceContour.LEFT_EYE)
    val rightEye = contour(FaceContour.RIGHT_EYE)
    if (leftEye.isEmpty() || rightEye.isEmpty()) return null
    return FaceLandmarks(
        faceContour = contour(FaceContour.FACE),
        leftEyebrow = (contour(FaceContour.LEFT_EYEBROW_TOP) + contour(FaceContour.LEFT_EYEBROW_BOTTOM)).toImmutableList(),
        rightEyebrow = (contour(FaceContour.RIGHT_EYEBROW_TOP) + contour(FaceContour.RIGHT_EYEBROW_BOTTOM)).toImmutableList(),
        leftEye = leftEye,
        rightEye = rightEye,
        noseBridge = contour(FaceContour.NOSE_BRIDGE),
        noseBottom = contour(FaceContour.NOSE_BOTTOM),
        outerLips = (contour(FaceContour.UPPER_LIP_TOP) + contour(FaceContour.LOWER_LIP_BOTTOM)).toImmutableList(),
        innerLips = (contour(FaceContour.UPPER_LIP_BOTTOM) + contour(FaceContour.LOWER_LIP_TOP)).toImmutableList(),
    )
}
