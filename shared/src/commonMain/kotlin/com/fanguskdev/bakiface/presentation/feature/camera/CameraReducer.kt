package com.fanguskdev.bakiface.presentation.feature.camera

import com.fanguskdev.bakiface.domain.camera.CameraError
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import kotlinx.collections.immutable.toImmutableList

/**
 * The only place where the camera screen's state changes (ADR 0003): the previous state and what happened give the
 * next state, with no side effects.
 *
 * The rules:
 * - The catalogue always comes after [EffectItem.Original]; a chosen effect that is not in it falls back to the
 *   original face. A choice made before the catalogue arrives is kept until it does, and checked then.
 * - A refused permission stops the camera and forgets the last frame, so nothing of it stays on screen.
 * - A frame only counts while the camera is starting or running: one that arrives after it stopped is dropped.
 * - A camera without the device is [CameraStatus.Unavailable]; a refused permission found by the camera is a
 *   permission, not a failure.
 */
object CameraReducer {
    fun reduce(state: CameraUiState, result: CameraResult): CameraUiState = when (result) {
        is CameraResult.EffectsLoaded -> {
            val effects = (listOf(EffectItem.Original) + result.effects.filter { it.id != null }).toImmutableList()
            state.copy(
                effects = effects,
                isCatalogLoaded = true,
                selectedEffectId = state.selectedEffectId?.takeIf { id -> effects.any { it.id == id } },
            )
        }
        is CameraResult.EffectSelected -> state.copy(
            selectedEffectId = if (state.isCatalogLoaded) result.id?.takeIf { id -> state.effects.any { it.id == id } } else result.id,
        )
        is CameraResult.PermissionUpdated -> if (result.permission == CameraPermission.Granted) {
            state.copy(permission = result.permission)
        } else {
            state.copy(permission = result.permission, status = CameraStatus.Idle, preview = null)
        }
        CameraResult.PreviewStarting -> state.copy(status = CameraStatus.Starting)
        is CameraResult.FrameArrived -> if (state.status == CameraStatus.Starting || state.status == CameraStatus.Running) {
            state.copy(status = CameraStatus.Running, preview = result.frame)
        } else {
            state
        }
        is CameraResult.CameraFailed -> when (result.error) {
            CameraError.NoCamera -> state.copy(status = CameraStatus.Unavailable, preview = null)
            CameraError.PermissionDenied -> state.copy(status = CameraStatus.Idle, permission = CameraPermission.Denied, preview = null)
            is CameraError.Failed -> state.copy(status = CameraStatus.Failed, preview = null)
        }
        CameraResult.PreviewStopped -> state.copy(
            status = if (state.status == CameraStatus.Unavailable ||
                state.status == CameraStatus.Failed
            ) {
                state.status
            } else {
                CameraStatus.Idle
            },
        )
        is CameraResult.ViewportChanged -> state.copy(viewportWidth = result.width, viewportHeight = result.height)
        is CameraResult.LensChanged -> state.copy(lens = result.lens, preview = null)
        is CameraResult.Capturing -> state.copy(isCapturing = result.active)
        is CameraResult.Importing -> state.copy(isImporting = result.active)
        is CameraResult.GalleryToggled -> state.copy(isGalleryOpen = result.open)
    }
}
