package com.fanguskdev.bakiface.data.picture

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.picture.Picture
import com.fanguskdev.bakiface.domain.picture.PictureEditor
import com.fanguskdev.bakiface.domain.picture.Region
import com.fanguskdev.bakiface.render.BitmapPicture
import com.fanguskdev.bakiface.render.asImageBitmap
import kotlinx.coroutines.withContext

/**
 * Crops and flips pictures with a Compose canvas, the same on both platforms (ADR 0005). The copy is new memory: the
 * camera can reuse its frame as soon as this returns.
 */
class BitmapPictureEditor(private val dispatchers: AppDispatchers) : PictureEditor {
    override suspend fun crop(picture: Picture, region: Region, mirror: Boolean): Picture = withContext(dispatchers.default) {
        val output = ImageBitmap(region.width, region.height)
        val canvas = Canvas(output)
        if (mirror) {
            canvas.translate(region.width.toFloat(), 0f)
            canvas.scale(-1f, 1f)
        }
        canvas.drawImageRect(
            image = picture.asImageBitmap(),
            srcOffset = IntOffset(region.left, region.top),
            srcSize = IntSize(region.width, region.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(region.width, region.height),
            paint = Paint(),
        )
        BitmapPicture(output)
    }
}
