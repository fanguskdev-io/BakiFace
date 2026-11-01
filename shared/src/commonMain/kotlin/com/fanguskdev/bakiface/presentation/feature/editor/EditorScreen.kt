package com.fanguskdev.bakiface.presentation.feature.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.ExportDestination
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.presentation.designsystem.BakiFaceTheme
import com.fanguskdev.bakiface.presentation.designsystem.BakiIcons
import com.fanguskdev.bakiface.presentation.designsystem.BakiTheme
import com.fanguskdev.bakiface.presentation.designsystem.Ink
import com.fanguskdev.bakiface.presentation.designsystem.InkButton
import com.fanguskdev.bakiface.presentation.designsystem.InkIconButton
import com.fanguskdev.bakiface.presentation.designsystem.screentone
import com.fanguskdev.bakiface.presentation.feature.effects.EffectCarousel
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import com.fanguskdev.bakiface.presentation.feature.effects.displayName
import com.fanguskdev.bakiface.presentation.platform.rememberShareSheet
import com.fanguskdev.bakiface.presentation.resources.Res
import com.fanguskdev.bakiface.presentation.resources.editor_back_to_camera
import com.fanguskdev.bakiface.presentation.resources.editor_close
import com.fanguskdev.bakiface.presentation.resources.editor_intensity
import com.fanguskdev.bakiface.presentation.resources.editor_intensity_value
import com.fanguskdev.bakiface.presentation.resources.editor_missing_body
import com.fanguskdev.bakiface.presentation.resources.editor_missing_title
import com.fanguskdev.bakiface.presentation.resources.editor_no_face
import com.fanguskdev.bakiface.presentation.resources.editor_photo_description
import com.fanguskdev.bakiface.presentation.resources.editor_photo_plain_description
import com.fanguskdev.bakiface.presentation.resources.editor_save
import com.fanguskdev.bakiface.presentation.resources.editor_save_denied
import com.fanguskdev.bakiface.presentation.resources.editor_save_failed
import com.fanguskdev.bakiface.presentation.resources.editor_saved
import com.fanguskdev.bakiface.presentation.resources.editor_share
import com.fanguskdev.bakiface.presentation.resources.editor_share_failed
import com.fanguskdev.bakiface.render.PictureFit
import com.fanguskdev.bakiface.render.asImageBitmap
import com.fanguskdev.bakiface.render.drawPictureWithEffect
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

/** The editor, wired: the [EditorViewModel] of [shotId], its one-off effects and the system's share sheet. */
@Composable
fun EditorDestination(
    shotId: ShotId,
    effectId: EffectId?,
    onBack: () -> Unit,
    viewModel: EditorViewModel = koinViewModel(key = shotId.value) { parametersOf(shotId, effectId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val share = rememberShareSheet()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditorEffect.NavigateBack -> onBack()
                is EditorEffect.OpenShareSheet -> share(effect.path)
                is EditorEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message.text))
            }
        }
    }
    EditorScreen(state = state, snackbar = snackbar, onIntent = viewModel::onIntent)
}

private val EditorMessage.text: StringResource
    get() = when (this) {
        EditorMessage.Saved -> Res.string.editor_saved
        EditorMessage.SaveFailed -> Res.string.editor_save_failed
        EditorMessage.SaveDenied -> Res.string.editor_save_denied
        EditorMessage.ShareFailed -> Res.string.editor_share_failed
    }

/**
 * The finished photo, framed like a manga panel on screentone paper, with the intensity and the characters below it and
 * the two ways out: share it or save it.
 */
@Composable
fun EditorScreen(state: EditorUiState, snackbar: SnackbarHostState, onIntent: (EditorIntent) -> Unit) {
    val colors = BakiTheme.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .screentone(colors.content.copy(alpha = 0.07f)),
    ) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = BakiTheme.spacing.l)) {
            Row(Modifier.fillMaxWidth().padding(vertical = BakiTheme.spacing.s), verticalAlignment = Alignment.CenterVertically) {
                InkIconButton(BakiIcons.Close, stringResource(Res.string.editor_close), onClick = { onIntent(EditorIntent.Close) })
                Spacer(Modifier.width(BakiTheme.spacing.s))
                Text(text = state.selectedEffect.displayName(), style = MaterialTheme.typography.headlineSmall, color = colors.content)
            }
            PhotoPanel(state, onIntent, Modifier.weight(1f).fillMaxWidth())
            if (state.shot != null) {
                if (!state.hasFace && state.selectedEffectId != null) {
                    Text(
                        text = stringResource(Res.string.editor_no_face),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.content,
                        modifier = Modifier.padding(top = BakiTheme.spacing.s),
                    )
                }
                IntensityRow(state.intensity, enabled = state.selectedEffectId != null) { onIntent(EditorIntent.ChangeIntensity(it)) }
                EffectCarousel(
                    effects = state.effects,
                    selectedIndex = state.selectedIndex,
                    onSelected = { index -> onIntent(EditorIntent.SelectEffect(state.effects.getOrNull(index)?.id)) },
                    dark = colors.isDark,
                ) {
                    val ring = colors.accent
                    Canvas(Modifier.size(70.dp)) {
                        drawCircle(color = ring, radius = size.minDimension / 2f - 2.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = BakiTheme.spacing.m),
                    horizontalArrangement = Arrangement.spacedBy(BakiTheme.spacing.m),
                ) {
                    InkButton(
                        text = stringResource(Res.string.editor_share),
                        onClick = { onIntent(EditorIntent.Share) },
                        icon = BakiIcons.Share,
                        enabled = state.canExport,
                        modifier = Modifier.weight(1f),
                    )
                    InkButton(
                        text = stringResource(Res.string.editor_save),
                        onClick = { onIntent(EditorIntent.Save) },
                        icon = BakiIcons.Save,
                        primary = true,
                        enabled = state.canExport,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing))
    }
}

@Composable
private fun PhotoPanel(state: EditorUiState, onIntent: (EditorIntent) -> Unit, modifier: Modifier = Modifier) {
    val colors = BakiTheme.colors
    Box(
        modifier = modifier
            .background(Ink, MaterialTheme.shapes.small)
            .border(BakiTheme.strokes.panel, colors.outline, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center,
    ) {
        val shot = state.shot
        when {
            shot != null -> {
                val description = state.selectedEffect.photoDescription()
                Canvas(Modifier.fillMaxSize().padding(BakiTheme.strokes.panel).semantics { contentDescription = description }) {
                    drawPictureWithEffect(shot.picture.asImageBitmap(), state.render, PictureFit.Fit, mirrored = false)
                }
                if (state.exporting != null) CircularProgressIndicator(color = colors.accent)
            }
            state.isMissing -> MissingShot(onIntent)
            else -> CircularProgressIndicator(color = colors.accent)
        }
    }
}

@Composable
private fun EffectItem.photoDescription(): String =
    if (id == null) stringResource(Res.string.editor_photo_plain_description) else stringResource(Res.string.editor_photo_description, name)

@Composable
private fun MissingShot(onIntent: (EditorIntent) -> Unit) {
    Column(Modifier.padding(BakiTheme.spacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(Res.string.editor_missing_title),
            style = MaterialTheme.typography.headlineSmall,
            color = BakiTheme.colors.background,
        )
        Spacer(Modifier.height(BakiTheme.spacing.s))
        Text(
            text = stringResource(Res.string.editor_missing_body),
            style = MaterialTheme.typography.bodyLarge,
            color = BakiTheme.colors.background,
        )
        Spacer(Modifier.height(BakiTheme.spacing.l))
        InkButton(text = stringResource(Res.string.editor_back_to_camera), onClick = { onIntent(EditorIntent.Close) }, primary = true)
    }
}

@Composable
private fun IntensityRow(intensity: Float, enabled: Boolean, onChange: (Float) -> Unit) {
    val colors = BakiTheme.colors
    val label = stringResource(Res.string.editor_intensity)
    val value = stringResource(Res.string.editor_intensity_value, (intensity * 100).roundToInt())
    Row(Modifier.fillMaxWidth().padding(top = BakiTheme.spacing.s), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = colors.content)
        Slider(
            value = intensity,
            onValueChange = onChange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = colors.accent,
                activeTrackColor = colors.accent,
                inactiveTrackColor = colors.contentMuted.copy(alpha = 0.35f),
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = BakiTheme.spacing.m)
                .semantics {
                    contentDescription = label
                    stateDescription = value
                },
        )
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = colors.content)
    }
}

@Preview
@Composable
private fun MissingPreview() {
    BakiFaceTheme(darkTheme = false) {
        EditorScreen(state = EditorUiState(isLoading = false, isMissing = true), snackbar = remember { SnackbarHostState() }, onIntent = {})
    }
}

@Preview
@Composable
private fun ExportingPreview() {
    BakiFaceTheme(darkTheme = true) {
        EditorScreen(
            state = EditorUiState(exporting = ExportDestination.Gallery),
            snackbar = remember {
                SnackbarHostState()
            },
            onIntent = {},
        )
    }
}
