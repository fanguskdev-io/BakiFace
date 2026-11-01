package com.fanguskdev.bakiface.data

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toPixelMap
import com.fanguskdev.bakiface.data.catalog.CatalogEffectRepository
import com.fanguskdev.bakiface.data.picture.BitmapPictureEditor
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.getOrNull
import com.fanguskdev.bakiface.domain.effect.ColorGrade
import com.fanguskdev.bakiface.domain.face.FaceLandmarks
import com.fanguskdev.bakiface.domain.face.faceGeometryOf
import com.fanguskdev.bakiface.domain.geometry.Point
import com.fanguskdev.bakiface.domain.picture.Region
import com.fanguskdev.bakiface.domain.render.FaceRenderer
import com.fanguskdev.bakiface.render.BitmapPicture
import com.fanguskdev.bakiface.render.asImageBitmap
import com.fanguskdev.bakiface.render.composePicture
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A picture with a different colour on each half, so a flip and a warp can be seen in its pixels. */
private fun halves(width: Int, height: Int): ImageBitmap = ImageBitmap(width, height).also { image ->
    val canvas = Canvas(image)
    canvas.drawRect(0f, 0f, width / 2f, height.toFloat(), Paint().apply { color = Color.Red })
    canvas.drawRect(width / 2f, 0f, width.toFloat(), height.toFloat(), Paint().apply { color = Color.Blue })
}

/** A checkerboard of [cell]-pixel squares: any pixel that moves shows. */
private fun checkerboard(width: Int, height: Int, cell: Int = 8): ImageBitmap = ImageBitmap(width, height).also { image ->
    val canvas = Canvas(image)
    val dark = Paint().apply { color = Color(0xFF203040) }
    val light = Paint().apply { color = Color(0xFFE0D0B0) }
    for (row in 0 until height / cell + 1) {
        for (column in 0 until width / cell + 1) {
            val paint = if ((row + column) % 2 == 0) dark else light
            canvas.drawRect(column * cell.toFloat(), row * cell.toFloat(), (column + 1) * cell.toFloat(), (row + 1) * cell.toFloat(), paint)
        }
    }
}

/** A face with the usual proportions centred at ([x], [y]), [eyes] pixels between the eyes. */
private fun face(x: Float, y: Float, eyes: Float): FaceLandmarks {
    fun ring(
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        count: Int,
    ) = List(count) { index ->
        val angle = 2f * PI.toFloat() * index / count
        Point(x + (cx + rx * cos(angle)) * eyes, y + (cy + ry * sin(angle)) * eyes)
    }.toImmutableList()
    return FaceLandmarks(
        faceContour = ring(0f, 0.35f, 1.05f, 1.5f, 36),
        leftEye = ring(-0.5f, 0f, 0.2f, 0.1f, 16),
        rightEye = ring(0.5f, 0f, 0.2f, 0.1f, 16),
        outerLips = ring(0f, 1.2f, 0.42f, 0.12f, 20),
        noseBottom = ring(0f, 0.84f, 0.22f, 0.04f, 8),
    )
}

class RenderingOnSkiaTest {
    private val dispatchers = AppDispatchers(Dispatchers.Default, Dispatchers.Default)

    @Test
    fun aCropIsTheRegionAndAMirrorSwapsItsHalves() = runTest {
        val editor = BitmapPictureEditor(dispatchers)
        val source = BitmapPicture(halves(200, 100))

        val plain = editor.crop(source, Region(50, 0, 100, 100), mirror = false).asImageBitmap().toPixelMap()
        val mirrored = editor.crop(source, Region(50, 0, 100, 100), mirror = true).asImageBitmap().toPixelMap()

        assertEquals(100, plain.width)
        assertEquals(Color.Red, plain[10, 50])
        assertEquals(Color.Blue, plain[90, 50])
        assertEquals(Color.Blue, mirrored[10, 50])
        assertEquals(Color.Red, mirrored[90, 50])
    }

    @Test
    fun everyCharacterChangesThePixelsAroundTheFaceAndNowhereElse() = runTest {
        val effects = assertNotNull(CatalogEffectRepository(dispatchers).effects().getOrNull())
        val picture = checkerboard(720, 1280)
        val geometry = assertNotNull(faceGeometryOf(face(360f, 640f, 120f)))
        val original = picture.toPixelMap()
        val samples = (240..480 step 20).flatMap { x -> (520..900 step 20).map { y -> x to y } }

        effects.forEach { effect ->
            val render = FaceRenderer().render(geometry, effect.copy(grade = ColorGrade.NEUTRAL), 1f, 1f, 720, 1280)
            val photo = composePicture(picture, render).toPixelMap()
            val changed = samples.count { (x, y) -> photo[x, y] != original[x, y] }
            assertTrue(changed > samples.size / 10, "${effect.id.value} changes the face: $changed of ${samples.size} samples")
            assertEquals(original[5, 5], photo[5, 5], "${effect.id.value} leaves the far corner alone")
            assertEquals(original[715, 1275], photo[715, 1275], "${effect.id.value} leaves the other corner alone")
        }
    }
}
