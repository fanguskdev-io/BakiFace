package com.fanguskdev.bakiface.domain.render

import kotlin.math.ceil
import kotlin.math.max

private const val DEFAULT_CELLS_PER_UNIT = 9f
private const val DEFAULT_MIN_CELLS = 24
private const val DEFAULT_MAX_CELLS = 110

/**
 * Lays the grid of a [WarpMesh] over a picture and moves its corners with a [WarpField] (ADR 0005).
 *
 * The cells are square and about [cellsPerUnit] to the distance between the eyes, so a small face still bends
 * smoothly; across the long side of the picture there are never fewer than [minCells] nor more than [maxCells], which
 * keeps the mesh light (a 720 × 1280 frame with a face at arm's length is about 4 000 corners).
 */
class MeshPlanner(
    private val cellsPerUnit: Float = DEFAULT_CELLS_PER_UNIT,
    private val minCells: Int = DEFAULT_MIN_CELLS,
    private val maxCells: Int = DEFAULT_MAX_CELLS,
) {
    /** The grid of a [width] × [height] picture warped by [field]. */
    fun plan(field: WarpField, width: Int, height: Int): WarpMesh {
        require(width > 0 && height > 0) { "A picture has a size" }
        val longSide = max(width, height).toFloat()
        val cell = (field.axes.unit / cellsPerUnit).coerceIn(longSide / maxCells, longSide / minCells)
        val columns = ceil(width / cell).toInt().coerceAtLeast(1)
        val rows = ceil(height / cell).toInt().coerceAtLeast(1)
        val positions = FloatArray((columns + 1) * (rows + 1) * 2)
        val axes = field.axes
        val reachSquared = field.reach * field.reach
        val displacement = FloatArray(2)
        var cursor = 0
        for (row in 0..rows) {
            val y = height * row / rows.toFloat()
            for (column in 0..columns) {
                val x = width * column / columns.toFloat()
                val offsetX = x - axes.origin.x
                val offsetY = y - axes.origin.y
                val localX = (offsetX * axes.xAxis.x + offsetY * axes.xAxis.y) / axes.unit
                val localY = (-offsetX * axes.xAxis.y + offsetY * axes.xAxis.x) / axes.unit
                var imageX = x
                var imageY = y
                if (!field.isEmpty && localX * localX + localY * localY < reachSquared) {
                    field.displacementLocal(localX, localY, displacement)
                    imageX += (axes.xAxis.x * displacement[0] - axes.xAxis.y * displacement[1]) * axes.unit
                    imageY += (axes.xAxis.y * displacement[0] + axes.xAxis.x * displacement[1]) * axes.unit
                }
                positions[cursor++] = imageX
                positions[cursor++] = imageY
            }
        }
        return WarpMesh(columns, rows, width, height, positions)
    }
}
