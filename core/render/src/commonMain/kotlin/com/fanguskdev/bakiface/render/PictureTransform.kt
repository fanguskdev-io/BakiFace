package com.fanguskdev.bakiface.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.max
import kotlin.math.min

/** How a picture takes up the space it is drawn in. */
enum class PictureFit {
    /** Covers all of it, cutting what sticks out: the camera. */
    Fill,

    /** Shows all of the picture, leaving bars: the editor and the photo that is saved. */
    Fit,
}

/** Where a picture of a given size lands in a space: picture pixels times [scale], plus the offset. */
data class PictureTransform(val scale: Float, val offsetX: Float, val offsetY: Float)

/** How a [imageWidth] × [imageHeight] picture is placed, centred, in [space] with [fit]. */
fun pictureTransform(
    imageWidth: Int,
    imageHeight: Int,
    space: Size,
    fit: PictureFit,
): PictureTransform {
    if (imageWidth <= 0 || imageHeight <= 0 || space.width <= 0f || space.height <= 0f) return PictureTransform(1f, 0f, 0f)
    val horizontal = space.width / imageWidth
    val vertical = space.height / imageHeight
    val scale = if (fit == PictureFit.Fill) max(horizontal, vertical) else min(horizontal, vertical)
    return PictureTransform(
        scale = scale,
        offsetX = (space.width - imageWidth * scale) / 2f,
        offsetY = (space.height - imageHeight * scale) / 2f,
    )
}

/**
 * Runs [block] with the canvas in picture pixels: placed by [transform] and, if [mirrored], flipped left to right
 * around a picture [imageWidth] wide, like a mirror.
 */
inline fun DrawScope.withPicture(
    transform: PictureTransform,
    mirrored: Boolean,
    imageWidth: Int,
    block: DrawScope.() -> Unit,
) {
    withTransform(
        {
            translate(left = transform.offsetX, top = transform.offsetY)
            if (mirrored) {
                scale(scaleX = -transform.scale, scaleY = transform.scale, pivot = Offset.Zero)
                translate(left = -imageWidth.toFloat())
            } else {
                scale(scaleX = transform.scale, scaleY = transform.scale, pivot = Offset.Zero)
            }
        },
        block,
    )
}
