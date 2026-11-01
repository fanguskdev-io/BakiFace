package com.fanguskdev.bakiface.render

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.asSkiaColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.skiaCanvas
import com.fanguskdev.bakiface.domain.render.WarpMesh
import org.jetbrains.skia.BlendMode
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Paint
import org.jetbrains.skia.SamplingMode
import org.jetbrains.skia.VertexMode

/** The texture coordinates and triangles of a grid, which only depend on its shape: kept for the next frame. */
private class MeshTopology(
    val columns: Int,
    val rows: Int,
    val imageWidth: Int,
    val imageHeight: Int,
) {
    val texCoords = FloatArray((columns + 1) * (rows + 1) * 2).also { coordinates ->
        var cursor = 0
        for (row in 0..rows) {
            for (column in 0..columns) {
                coordinates[cursor++] = imageWidth * column / columns.toFloat()
                coordinates[cursor++] = imageHeight * row / rows.toFloat()
            }
        }
    }
    val indices = ShortArray(columns * rows * 6).also { triangles ->
        var cursor = 0
        val stride = columns + 1
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val topLeft = row * stride + column
                val bottomLeft = topLeft + stride
                triangles[cursor++] = topLeft.toShort()
                triangles[cursor++] = (topLeft + 1).toShort()
                triangles[cursor++] = bottomLeft.toShort()
                triangles[cursor++] = (topLeft + 1).toShort()
                triangles[cursor++] = (bottomLeft + 1).toShort()
                triangles[cursor++] = bottomLeft.toShort()
            }
        }
    }

    fun fits(mesh: WarpMesh): Boolean =
        mesh.columns == columns && mesh.rows == rows && mesh.imageWidth == imageWidth && mesh.imageHeight == imageHeight
}

private var lastTopology: MeshTopology? = null

/**
 * iOS draws the grid with Skia's `drawVertices`: two triangles per cell, textured with the picture through a bitmap
 * shader. A grid has at most a few thousand corners, well inside the 16-bit indices Skia takes.
 */
internal actual fun DrawScope.drawImageMesh(image: ImageBitmap, mesh: WarpMesh, colorFilter: ColorFilter?) {
    val topology = lastTopology?.takeIf { it.fits(mesh) }
        ?: MeshTopology(mesh.columns, mesh.rows, mesh.imageWidth, mesh.imageHeight).also { lastTopology = it }
    val paint = Paint().apply {
        isAntiAlias = true
        shader = image.asSkiaBitmap().makeShader(FilterTileMode.CLAMP, FilterTileMode.CLAMP, SamplingMode.LINEAR, null)
        this.colorFilter = colorFilter?.asSkiaColorFilter()
    }
    drawContext.canvas.skiaCanvas.drawVertices(
        VertexMode.TRIANGLES,
        mesh.positions,
        null,
        topology.texCoords,
        topology.indices,
        BlendMode.MODULATE,
        paint,
    )
}
