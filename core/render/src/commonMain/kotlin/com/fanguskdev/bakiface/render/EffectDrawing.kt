package com.fanguskdev.bakiface.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.fanguskdev.bakiface.domain.effect.Argb
import com.fanguskdev.bakiface.domain.effect.MarkBlend
import com.fanguskdev.bakiface.domain.render.FaceRender
import com.fanguskdev.bakiface.domain.render.SkinMark
import com.fanguskdev.bakiface.domain.render.WarpMesh

/**
 * Draws [picture] with its effect [render] in this scope (ADR 0005): the picture through the warped mesh, the marks
 * on the skin and the character's colour, placed with [fit] and, if [mirrored], flipped like a mirror. Without a
 * render, the picture as it is.
 */
fun DrawScope.drawPictureWithEffect(
    picture: ImageBitmap,
    render: FaceRender?,
    fit: PictureFit,
    mirrored: Boolean,
) {
    val transform = pictureTransform(picture.width, picture.height, size, fit)
    val colorFilter = render?.let { gradeColorFilter(it.grade, it.gradeAmount) }
    withPicture(transform, mirrored, picture.width) {
        val mesh = render?.mesh
        if (mesh != null && mesh.imageWidth == picture.width && mesh.imageHeight == picture.height) {
            drawImageMesh(picture, mesh, colorFilter)
        } else {
            drawImage(picture, colorFilter = colorFilter)
        }
        render?.marks?.forEach { drawMark(it) }
    }
}

/**
 * The finished photo: [picture] drawn with [render] at its own size, ready to be encoded. Runs on any thread; nothing
 * of it is on screen.
 */
fun composePicture(picture: ImageBitmap, render: FaceRender?): ImageBitmap {
    val output = ImageBitmap(picture.width, picture.height)
    CanvasDrawScope().draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = Canvas(output),
        size = Size(picture.width.toFloat(), picture.height.toFloat()),
    ) {
        drawPictureWithEffect(picture, render, PictureFit.Fit, mirrored = false)
    }
    return output
}

/**
 * Draws [image] through the warped [mesh]: the corner (i, j) of the mesh takes the colour of the picture at
 * (i·w/columns, j·h/rows) and is drawn where the mesh puts it. Each platform has its own way of drawing a textured
 * grid (ADR 0005).
 */
internal expect fun DrawScope.drawImageMesh(image: ImageBitmap, mesh: WarpMesh, colorFilter: ColorFilter?)

private fun DrawScope.drawMark(mark: SkinMark) {
    val blendMode = mark.blend.toBlendMode()
    val color = mark.color.toColor()
    when (mark) {
        is SkinMark.Line -> drawLineMark(mark, color, blendMode)
        is SkinMark.Fill -> drawFillMark(mark, color, blendMode)
        is SkinMark.Spot -> drawSpot(mark, color, blendMode)
    }
}

private fun DrawScope.drawFillMark(mark: SkinMark.Fill, color: Color, blendMode: BlendMode) {
    val points = mark.points
    if (points.size < 3 || mark.alpha <= 0f) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (index in 1 until points.size) lineTo(points[index].x, points[index].y)
        close()
    }
    drawPath(path = path, color = color, alpha = mark.alpha.coerceAtMost(1f), blendMode = blendMode)
}

private fun DrawScope.drawLineMark(mark: SkinMark.Line, color: Color, blendMode: BlendMode) {
    val points = mark.points
    if (points.size < 2 || mark.width <= 0f || mark.alpha <= 0f) return
    if (points.size == 2) {
        drawLine(
            color = color,
            start = Offset(points[0].x, points[0].y),
            end = Offset(points[1].x, points[1].y),
            strokeWidth = mark.width,
            cap = StrokeCap.Round,
            alpha = mark.alpha.coerceAtMost(1f),
            blendMode = blendMode,
        )
        return
    }
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (index in 1 until points.size) lineTo(points[index].x, points[index].y)
    }
    drawPath(
        path = path,
        color = color,
        alpha = mark.alpha.coerceAtMost(1f),
        style = Stroke(width = mark.width, cap = StrokeCap.Round, join = StrokeJoin.Round),
        blendMode = blendMode,
    )
}

private fun DrawScope.drawSpot(mark: SkinMark.Spot, color: Color, blendMode: BlendMode) {
    if (mark.radiusX <= 0f || mark.radiusY <= 0f || mark.alpha <= 0f) return
    val center = Offset(mark.center.x, mark.center.y)
    val alpha = mark.alpha.coerceAtMost(1f)
    withTransform({
        rotate(degrees = mark.angleDegrees, pivot = center)
        scale(scaleX = 1f, scaleY = mark.radiusY / mark.radiusX, pivot = center)
    }) {
        drawCircle(
            brush = Brush.radialGradient(
                0f to color.copy(alpha = alpha),
                0.55f to color.copy(alpha = alpha * 0.45f),
                1f to color.copy(alpha = 0f),
                center = center,
                radius = mark.radiusX,
            ),
            radius = mark.radiusX,
            center = center,
            blendMode = blendMode,
        )
    }
}

private fun MarkBlend.toBlendMode(): BlendMode = when (this) {
    MarkBlend.Normal -> BlendMode.SrcOver
    MarkBlend.Multiply -> BlendMode.Multiply
    MarkBlend.Screen -> BlendMode.Screen
}

/** The Compose colour of an [Argb]. */
fun Argb.toColor(): Color = Color(red = red, green = green, blue = blue, alpha = alpha)
