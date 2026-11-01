package com.fanguskdev.bakiface.domain.render

import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.face.FaceGeometry
import kotlinx.collections.immutable.toImmutableList

/**
 * Works out how to draw one picture with an effect (ADR 0005): the warp of the face, the marks on the skin and the
 * colour. Pure and synchronous: the camera calls it on every frame, off the main thread, and the editor on a photo.
 */
class FaceRenderer(private val meshPlanner: MeshPlanner = MeshPlanner()) {
    /**
     * How to draw a [imageWidth] × [imageHeight] picture with [effect] on the face [geometry], at [intensity] (0 to 1,
     * what the person chose) times [presence] (0 to 1, how sure the camera is that the face is there). Without an
     * effect, nothing to change (`null`); without a face, only the colour of the character.
     */
    fun render(
        geometry: FaceGeometry?,
        effect: Effect?,
        intensity: Float,
        presence: Float,
        imageWidth: Int,
        imageHeight: Int,
    ): FaceRender? {
        if (effect == null) return null
        val amount = intensity.coerceIn(0f, 1f)
        val strength = amount * presence.coerceIn(0f, 1f)
        if (geometry == null || strength <= 0f) return FaceRender(mesh = null, grade = effect.grade, gradeAmount = amount)
        val field = WarpField.build(geometry, effect.warp, strength)
        val mesh = if (field.isEmpty) null else meshPlanner.plan(field, imageWidth, imageHeight)
        val marks = MarkComposer(geometry, field, strength).compose(effect.marks)
        return FaceRender(
            mesh = mesh,
            marks = if (marks.isEmpty()) NO_MARKS else marks.toImmutableList(),
            grade = effect.grade,
            gradeAmount = amount,
        )
    }
}
