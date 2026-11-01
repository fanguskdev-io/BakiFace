package com.fanguskdev.bakiface.presentation.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ExportError
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.domain.usecase.DiscardShotUseCase
import com.fanguskdev.bakiface.domain.usecase.ExportShotUseCase
import com.fanguskdev.bakiface.domain.usecase.GetEffectsUseCase
import com.fanguskdev.bakiface.domain.usecase.GetShotUseCase
import com.fanguskdev.bakiface.domain.usecase.RenderShotUseCase
import com.fanguskdev.bakiface.presentation.feature.effects.toItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The editor of a shot (ADR 0003): the character and the intensity can still change, the drawing follows them off the
 * main thread, and the finished photo goes to the gallery or to the share sheet. It starts with [initialEffectId],
 * the character chosen on the camera when the photo was taken.
 */
class EditorViewModel(
    private val shotId: ShotId,
    private val initialEffectId: EffectId?,
    private val getShot: GetShotUseCase,
    private val getEffects: GetEffectsUseCase,
    private val renderShot: RenderShotUseCase,
    private val exportShot: ExportShotUseCase,
    private val discardShot: DiscardShotUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val mutableState = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<EditorEffect>(Channel.BUFFERED)
    val effects: Flow<EditorEffect> = effectChannel.receiveAsFlow()

    private var catalogue: Map<EffectId, Effect> = emptyMap()

    init {
        viewModelScope.launch {
            val shot = getShot(shotId)
            val effects = (getEffects() as? AppResult.Success)?.value.orEmpty()
            catalogue = effects.associateBy { it.id }
            reduce(EditorResult.EffectsLoaded(effects.map { it.toItem() }.toImmutableList(), initialEffectId))
            reduce(EditorResult.ShotLoaded(shot))
            state.map { Triple(it.shot, it.selectedEffectId, it.intensity) }
                .distinctUntilChanged()
                .collectLatest { (current, effectId, intensity) ->
                    if (current != null) {
                        val render = withContext(dispatchers.default) { renderShot(current, effectId?.let(catalogue::get), intensity) }
                        reduce(EditorResult.Rendered(render))
                    }
                }
        }
    }

    fun onIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.SelectEffect -> reduce(EditorResult.EffectSelected(intent.id))
            is EditorIntent.ChangeIntensity -> reduce(EditorResult.IntensityChanged(intent.intensity))
            EditorIntent.Save -> export(ExportDestination.Gallery)
            EditorIntent.Share -> export(ExportDestination.Share)
            EditorIntent.Close -> viewModelScope.launch {
                discardShot(shotId)
                effectChannel.send(EditorEffect.NavigateBack)
            }
        }
    }

    private fun export(destination: ExportDestination) {
        val current = state.value
        val shot = current.shot
        if (shot == null || !current.canExport) return
        reduce(EditorResult.ExportStarted(destination))
        viewModelScope.launch {
            val effect = current.selectedEffectId?.let(catalogue::get)
            val result = exportShot(shot, effect, current.intensity, destination)
            reduce(EditorResult.ExportFinished)
            effectChannel.send(
                when (result) {
                    is AppResult.Success -> if (destination == ExportDestination.Share) {
                        EditorEffect.OpenShareSheet(result.value.location)
                    } else {
                        EditorEffect.ShowMessage(EditorMessage.Saved)
                    }
                    is AppResult.Failure -> EditorEffect.ShowMessage(
                        when {
                            destination == ExportDestination.Share -> EditorMessage.ShareFailed
                            result.error == ExportError.PermissionDenied -> EditorMessage.SaveDenied
                            else -> EditorMessage.SaveFailed
                        },
                    )
                },
            )
        }
    }

    private fun reduce(result: EditorResult) {
        mutableState.update { EditorReducer.reduce(it, result) }
    }
}
