package com.fanguskdev.bakiface.domain.picture

/**
 * A picture the platform owns: a frame of the camera or a photo (ADR 0005). The domain only reads its size; the
 * pixels stay with the layers that can draw them, so this module never depends on a graphics library.
 */
interface Picture {
    val width: Int
    val height: Int
}

/** A rectangle of a picture, in pixels from its top-left corner. */
data class Region(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)

/** What the domain needs done to the pixels of a [Picture] and cannot do itself. */
interface PictureEditor {
    /** A new picture with the [region] of [picture], flipped left to right when [mirror] is true. */
    suspend fun crop(picture: Picture, region: Region, mirror: Boolean): Picture
}

/**
 * The part of a [imageWidth] × [imageHeight] picture that fills a [viewWidth] × [viewHeight] view without leaving
 * gaps, centred: what the person sees of a camera frame on the screen.
 */
fun visibleRegion(
    imageWidth: Int,
    imageHeight: Int,
    viewWidth: Int,
    viewHeight: Int,
): Region {
    require(imageWidth > 0 && imageHeight > 0) { "A picture has a size" }
    if (viewWidth <= 0 || viewHeight <= 0) return Region(0, 0, imageWidth, imageHeight)
    val imageRatio = imageWidth.toFloat() / imageHeight
    val viewRatio = viewWidth.toFloat() / viewHeight
    return if (imageRatio > viewRatio) {
        val width = (imageHeight * viewRatio).toInt().coerceIn(1, imageWidth)
        Region((imageWidth - width) / 2, 0, width, imageHeight)
    } else {
        val height = (imageWidth / viewRatio).toInt().coerceIn(1, imageHeight)
        Region(0, (imageHeight - height) / 2, imageWidth, height)
    }
}
