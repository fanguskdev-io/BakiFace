package com.fanguskdev.bakiface.presentation.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The few icons of the app, drawn here as ink strokes on a 24-unit grid (ADR 0008): no icon library to carry, and the
 * same weight as the lines of the rest of the design. They are tinted where they are used.
 */
object BakiIcons {
    val Close: ImageVector by lazy {
        stroked("Close") {
            moveTo(6f, 6f)
            lineTo(18f, 18f)
            moveTo(18f, 6f)
            lineTo(6f, 18f)
        }
    }

    val Save: ImageVector by lazy {
        stroked("Save") {
            moveTo(12f, 4f)
            lineTo(12f, 15f)
            moveTo(7.5f, 10.5f)
            lineTo(12f, 15f)
            lineTo(16.5f, 10.5f)
            moveTo(5f, 15f)
            lineTo(5f, 20f)
            lineTo(19f, 20f)
            lineTo(19f, 15f)
        }
    }

    val Share: ImageVector by lazy {
        stroked("Share") {
            circle(6.5f, 12f, 2.5f)
            circle(17.5f, 6f, 2.5f)
            circle(17.5f, 18f, 2.5f)
            moveTo(8.7f, 10.8f)
            lineTo(15.3f, 7.2f)
            moveTo(8.7f, 13.2f)
            lineTo(15.3f, 16.8f)
        }
    }

    val FlipCamera: ImageVector by lazy {
        stroked("FlipCamera") {
            moveTo(19f, 9f)
            arcTo(7.5f, 7.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 5.4f, y1 = 8.4f)
            moveTo(19f, 4.5f)
            lineTo(19f, 9f)
            lineTo(14.5f, 9f)
            moveTo(5f, 15f)
            arcTo(7.5f, 7.5f, 0f, isMoreThanHalf = false, isPositiveArc = false, x1 = 18.6f, y1 = 15.6f)
            moveTo(5f, 19.5f)
            lineTo(5f, 15f)
            lineTo(9.5f, 15f)
        }
    }

    val Gallery: ImageVector by lazy {
        stroked("Gallery") {
            moveTo(4f, 5f)
            lineTo(20f, 5f)
            lineTo(20f, 19f)
            lineTo(4f, 19f)
            close()
            moveTo(4f, 16f)
            lineTo(9.5f, 10.5f)
            lineTo(14f, 15f)
            lineTo(16f, 13f)
            lineTo(20f, 17f)
            circle(15.5f, 8.8f, 1.4f)
        }
    }

    val Characters: ImageVector by lazy {
        stroked("Characters") {
            square(4f, 4f)
            square(13f, 4f)
            square(4f, 13f)
            square(13f, 13f)
        }
    }

    private fun PathBuilder.circle(cx: Float, cy: Float, radius: Float) {
        moveTo(cx - radius, cy)
        arcTo(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx + radius, y1 = cy)
        arcTo(radius, radius, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx - radius, y1 = cy)
    }

    private fun PathBuilder.square(left: Float, top: Float) {
        moveTo(left, top)
        lineTo(left + 7f, top)
        lineTo(left + 7f, top + 7f)
        lineTo(left, top + 7f)
        close()
    }

    private fun stroked(name: String, draw: PathBuilder.() -> Unit): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = draw,
    ).build()
}
