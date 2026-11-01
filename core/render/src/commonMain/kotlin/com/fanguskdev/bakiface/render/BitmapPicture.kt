package com.fanguskdev.bakiface.render

import androidx.compose.ui.graphics.ImageBitmap
import com.fanguskdev.bakiface.domain.picture.Picture

/**
 * A [Picture] whose pixels are a Compose [ImageBitmap] (ADR 0005): what the data layer hands over and this module
 * draws. The domain sees only its size.
 */
class BitmapPicture(val bitmap: ImageBitmap) : Picture {
    override val width: Int get() = bitmap.width
    override val height: Int get() = bitmap.height
}

/**
 * The pixels of a picture made by the data layer. Every [Picture] in the app is a [BitmapPicture]; anything else is a
 * programming error, and says so.
 */
fun Picture.asImageBitmap(): ImageBitmap =
    (this as? BitmapPicture)?.bitmap ?: error("Only a BitmapPicture can be drawn, not ${this::class.simpleName}")
