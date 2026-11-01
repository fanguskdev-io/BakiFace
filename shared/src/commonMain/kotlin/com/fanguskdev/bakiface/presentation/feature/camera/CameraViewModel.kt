package com.fanguskdev.bakiface.presentation.feature.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanguskdev.bakiface.domain.common.AppDispatchers
import com.fanguskdev.bakiface.domain.common.AppResult
import com.fanguskdev.bakiface.domain.effect.Effect
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.ImportError
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.usecase.CaptureShotUseCase
import com.fanguskdev.bakiface.domain.usecase.GetEffectsUseCase
import com.fanguskdev.bakiface.domain.usecase.ImportPhotoUseCase
import com.fanguskdev.bakiface.domain.usecase.ObserveLivePreviewUseCase
import com.fanguskdev.bakiface.domain.usecase.ObserveSelectedEffectUseCase
import com.fanguskdev.bakiface.domain.usecase.SelectEffectUseCase
import com.fanguskdev.bakiface.presentation.feature.effects.toItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The camera screen (ADR 0003): takes the person's intents, runs the use cases and reduces what happens into one
 * [CameraUiState] with [CameraReducer]. One-off actions (asking for the permission, opening the editor, a message)
 * go out through [effects], never through the state.
 *
 * The camera runs only while the screen is shown and the permission granted; it stops as soon as either goes. The
 * character remembered from the last time is restored once, unless the person has already chosen one.
 */
class CameraViewModel(
    private val getEffects: GetEffectsUseCase,
    private val observeSelectedEffect: ObserveSelectedEffectUseCase,
    private val selectEffect: SelectEffectUseCase,
    private val observeLivePreview: ObserveLivePreviewUseCase,
    private val captureShot: CaptureShotUseCase,
    private val importPhoto: ImportPhotoUseCase,
    private val dispatchers: AppDispatchers,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CameraUiState())
    val state: StateFlow<CameraUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<CameraEffect>(Channel.BUFFERED)
    val effects: Flow<CameraEffect> = effectChannel.receiveAsFlow()

    private val catalogue = MutableStateFlow<Map<EffectId, Effect>>(emptyMap())
    private var previewJob: Job? = null
    private var screenShown = false
    private var hasChosenEffect = false

    init {
        viewModelScope.launch {
            when (val result = getEffects()) {
                is AppResult.Success -> {
                    catalogue.value = result.value.associateBy { it.id }
                    reduce(CameraResult.EffectsLoaded(result.value.map { it.toItem() }.toImmutableList()))
                }
                is AppResult.Failure -> effectChannel.send(CameraEffect.ShowMessage(CameraMessage.CatalogFailed))
            }
            val remembered = observeSelectedEffect().first()
            if (!hasChosenEffect) reduce(CameraResult.EffectSelected(remembered))
        }
    }

    fun onIntent(intent: CameraIntent) {
        when (intent) {
            is CameraIntent.PermissionChanged -> {
                reduce(CameraResult.PermissionUpdated(intent.permission))
                if (intent.permission == CameraPermission.Granted) startPreview() else stopPreview()
            }
            CameraIntent.RequestPermission -> send(CameraEffect.RequestCameraPermission)
            CameraIntent.OpenSettings -> send(CameraEffect.OpenAppSettings)
            CameraIntent.ScreenShown -> {
                screenShown = true
                startPreview()
            }
            CameraIntent.ScreenHidden -> {
                screenShown = false
                stopPreview()
            }
            is CameraIntent.ViewportMeasured -> reduce(CameraResult.ViewportChanged(intent.width, intent.height))
            is CameraIntent.SelectEffect -> {
                hasChosenEffect = true
                reduce(CameraResult.EffectSelected(intent.id))
                viewModelScope.launch { selectEffect(state.value.selectedEffectId) }
            }
            CameraIntent.FlipCamera -> {
                stopPreview()
                reduce(CameraResult.LensChanged(state.value.lens.other))
                startPreview()
            }
            CameraIntent.Capture -> capture()
            CameraIntent.RetryCamera -> {
                stopPreview()
                startPreview()
            }
            CameraIntent.PickPhoto -> send(CameraEffect.LaunchPhotoPicker)
            is CameraIntent.PhotoPicked -> import(intent.source)
            CameraIntent.OpenGallery -> reduce(CameraResult.GalleryToggled(open = true))
            CameraIntent.CloseGallery -> reduce(CameraResult.GalleryToggled(open = false))
        }
    }

    private fun startPreview() {
        if (!screenShown || state.value.permission != CameraPermission.Granted || previewJob?.isActive == true) return
        reduce(CameraResult.PreviewStarting)
        val lens = state.value.lens
        val chosenEffect = combine(state.map { it.selectedEffectId }.distinctUntilChanged(), catalogue) { id, effects ->
            id?.let(effects::get)
        }
        previewJob = viewModelScope.launch {
            observeLivePreview(lens, chosenEffect)
                .flowOn(dispatchers.default)
                .collect { result ->
                    when (result) {
                        is AppResult.Success -> reduce(CameraResult.FrameArrived(result.value))
                        is AppResult.Failure -> reduce(CameraResult.CameraFailed(result.error))
                    }
                }
        }
    }

    private fun stopPreview() {
        previewJob?.cancel()
        previewJob = null
        reduce(CameraResult.PreviewStopped)
    }

    private fun capture() {
        val current = state.value
        val frame = current.preview
        if (frame == null || !current.canCapture) return
        reduce(CameraResult.Capturing(active = true))
        viewModelScope.launch {
            try {
                val shot = captureShot(frame, current.viewportWidth, current.viewportHeight)
                effectChannel.send(CameraEffect.OpenEditor(shot.id, current.selectedEffectId))
            } catch (error: IllegalArgumentException) {
                effectChannel.send(CameraEffect.ShowMessage(CameraMessage.CaptureFailed))
            } finally {
                reduce(CameraResult.Capturing(active = false))
            }
        }
    }

    private fun import(source: PhotoSource) {
        if (state.value.isImporting) return
        reduce(CameraResult.Importing(active = true))
        viewModelScope.launch {
            when (val result = importPhoto(source)) {
                is AppResult.Success -> effectChannel.send(CameraEffect.OpenEditor(result.value.id, state.value.selectedEffectId))
                is AppResult.Failure -> effectChannel.send(
                    CameraEffect.ShowMessage(
                        if (result.error == ImportError.Unreadable) CameraMessage.ImportUnreadable else CameraMessage.ImportFailed,
                    ),
                )
            }
            reduce(CameraResult.Importing(active = false))
        }
    }

    private fun send(effect: CameraEffect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    private fun reduce(result: CameraResult) {
        mutableState.update { CameraReducer.reduce(it, result) }
    }
}
