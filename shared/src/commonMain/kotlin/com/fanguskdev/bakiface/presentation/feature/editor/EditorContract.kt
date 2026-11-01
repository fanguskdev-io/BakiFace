package com.fanguskdev.bakiface.presentation.feature.editor

import androidx.compose.runtime.Immutable
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.render.FaceRender
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.Shot
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** The editor's one state (ADR 0003): the shot, the chosen character and intensity, and how to draw them. */
@Immutable
data class EditorUiState(
    val isLoading: Boolean = true,
    val isMissing: Boolean = false,
    val shot: Shot? = null,
    val effects: ImmutableList<EffectItem> = persistentListOf(EffectItem.Original),
    val selectedEffectId: EffectId? = null,
    val intensity: Float = 1f,
    val render: FaceRender? = null,
    val exporting: ExportDestination? = null,
) {
    val selectedEffect: EffectItem get() = effects.firstOrNull { it.id == selectedEffectId } ?: EffectItem.Original

    val selectedIndex: Int get() = effects.indexOfFirst { it.id == selectedEffectId }.coerceAtLeast(0)

    val hasFace: Boolean get() = shot?.landmarks != null

    val canExport: Boolean get() = shot != null && exporting == null
}

/** Everything the person can do in the editor, sent to [EditorViewModel.onIntent]. */
sealed interface EditorIntent {
    data class SelectEffect(val id: EffectId?) : EditorIntent

    data class ChangeIntensity(val intensity: Float) : EditorIntent

    data object Save : EditorIntent

    data object Share : EditorIntent

    data object Close : EditorIntent
}

/** One-off actions of the editor, never part of its state. */
sealed interface EditorEffect {
    data object NavigateBack : EditorEffect

    data class OpenShareSheet(val path: String) : EditorEffect

    data class ShowMessage(val message: EditorMessage) : EditorEffect
}

enum class EditorMessage {
    Saved,
    SaveFailed,
    SaveDenied,
    ShareFailed,
}

/** What happened, as the editor's reducer reads it. */
sealed interface EditorResult {
    data class ShotLoaded(val shot: Shot?) : EditorResult

    data class EffectsLoaded(val effects: ImmutableList<EffectItem>, val selectedId: EffectId?) : EditorResult

    data class EffectSelected(val id: EffectId?) : EditorResult

    data class IntensityChanged(val intensity: Float) : EditorResult

    data class Rendered(val render: FaceRender?) : EditorResult

    data class ExportStarted(val destination: ExportDestination) : EditorResult

    data object ExportFinished : EditorResult
}
