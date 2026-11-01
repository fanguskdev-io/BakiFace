package com.fanguskdev.bakiface.presentation.feature.editor

import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import kotlinx.collections.immutable.toImmutableList

/**
 * The only place where the editor's state changes (ADR 0003). The rules: a shot that is gone shows as missing, never
 * as an endless wait; the intensity stays between 0 and 1; one export at a time.
 */
object EditorReducer {
    fun reduce(state: EditorUiState, result: EditorResult): EditorUiState = when (result) {
        is EditorResult.ShotLoaded -> state.copy(isLoading = false, isMissing = result.shot == null, shot = result.shot)
        is EditorResult.EffectsLoaded -> {
            val effects = (listOf(EffectItem.Original) + result.effects.filter { it.id != null }).toImmutableList()
            state.copy(effects = effects, selectedEffectId = result.selectedId?.takeIf { id -> effects.any { it.id == id } })
        }
        is EditorResult.EffectSelected -> state.copy(selectedEffectId = result.id?.takeIf { id -> state.effects.any { it.id == id } })
        is EditorResult.IntensityChanged -> state.copy(intensity = result.intensity.coerceIn(0f, 1f))
        is EditorResult.Rendered -> state.copy(render = result.render)
        is EditorResult.ExportStarted -> if (state.exporting == null) state.copy(exporting = result.destination) else state
        EditorResult.ExportFinished -> state.copy(exporting = null)
    }
}
