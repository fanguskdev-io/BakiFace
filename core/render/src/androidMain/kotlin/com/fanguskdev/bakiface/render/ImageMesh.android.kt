package com.fanguskdev.bakiface.render

import android.graphics.Paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.fanguskdev.bakiface.domain.render.WarpMesh

/**
 * Android draws a regular textured grid with `Canvas.drawBitmapMesh`, which the GPU accelerates and which maps the
 * whole bitmap evenly over the grid: exactly the [WarpMesh] contract.
 */
internal actual fun DrawScope.drawImageMesh(image: ImageBitmap, mesh: WarpMesh, colorFilter: ColorFilter?) {
    val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
        this.colorFilter = colorFilter?.asAndroidColorFilter()
    }
    drawContext.canvas.nativeCanvas.drawBitmapMesh(
        image.asAndroidBitmap(),
        mesh.columns,
        mesh.rows,
        mesh.positions,
        0,
        null,
        0,
        paint,
    )
}
