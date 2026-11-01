package com.fanguskdev.bakiface.presentation.feature.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fanguskdev.bakiface.domain.camera.PreviewFrame
import com.fanguskdev.bakiface.domain.effect.EffectHint
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.PhotoSource
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.presentation.DemoLaunch
import com.fanguskdev.bakiface.presentation.designsystem.BakiFaceTheme
import com.fanguskdev.bakiface.presentation.designsystem.BakiIcons
import com.fanguskdev.bakiface.presentation.designsystem.BakiTheme
import com.fanguskdev.bakiface.presentation.designsystem.Bone
import com.fanguskdev.bakiface.presentation.designsystem.CaptionBox
import com.fanguskdev.bakiface.presentation.designsystem.Ink
import com.fanguskdev.bakiface.presentation.designsystem.InkButton
import com.fanguskdev.bakiface.presentation.designsystem.InkIconButton
import com.fanguskdev.bakiface.presentation.designsystem.KanjiSeal
import com.fanguskdev.bakiface.presentation.designsystem.ShutterRing
import com.fanguskdev.bakiface.presentation.designsystem.concentrationLines
import com.fanguskdev.bakiface.presentation.designsystem.delaGothicOne
import com.fanguskdev.bakiface.presentation.feature.effects.EffectCarousel
import com.fanguskdev.bakiface.presentation.feature.effects.EffectGallerySheet
import com.fanguskdev.bakiface.presentation.feature.effects.EffectItem
import com.fanguskdev.bakiface.presentation.feature.effects.accentColor
import com.fanguskdev.bakiface.presentation.feature.effects.alias
import com.fanguskdev.bakiface.presentation.feature.effects.displayName
import com.fanguskdev.bakiface.presentation.platform.rememberAppSettingsOpener
import com.fanguskdev.bakiface.presentation.platform.rememberCameraPermissionController
import com.fanguskdev.bakiface.presentation.platform.rememberPhotoPicker
import com.fanguskdev.bakiface.presentation.platform.rememberReduceMotion
import com.fanguskdev.bakiface.presentation.resources.Res
import com.fanguskdev.bakiface.presentation.resources.app_name
import com.fanguskdev.bakiface.presentation.resources.camera_all_effects
import com.fanguskdev.bakiface.presentation.resources.camera_failed_body
import com.fanguskdev.bakiface.presentation.resources.camera_failed_title
import com.fanguskdev.bakiface.presentation.resources.camera_flip
import com.fanguskdev.bakiface.presentation.resources.camera_frame_rate
import com.fanguskdev.bakiface.presentation.resources.camera_gallery
import com.fanguskdev.bakiface.presentation.resources.camera_preview_description
import com.fanguskdev.bakiface.presentation.resources.camera_preview_plain_description
import com.fanguskdev.bakiface.presentation.resources.camera_retry
import com.fanguskdev.bakiface.presentation.resources.camera_shutter
import com.fanguskdev.bakiface.presentation.resources.camera_shutter_plain
import com.fanguskdev.bakiface.presentation.resources.camera_unavailable_body
import com.fanguskdev.bakiface.presentation.resources.camera_unavailable_title
import com.fanguskdev.bakiface.presentation.resources.hint_find_face
import com.fanguskdev.bakiface.presentation.resources.hint_open_mouth
import com.fanguskdev.bakiface.presentation.resources.message_capture_failed
import com.fanguskdev.bakiface.presentation.resources.message_catalog_failed
import com.fanguskdev.bakiface.presentation.resources.message_import_failed
import com.fanguskdev.bakiface.presentation.resources.message_import_unreadable
import com.fanguskdev.bakiface.presentation.resources.permission_allow
import com.fanguskdev.bakiface.presentation.resources.permission_body
import com.fanguskdev.bakiface.presentation.resources.permission_denied_body
import com.fanguskdev.bakiface.presentation.resources.permission_open_settings
import com.fanguskdev.bakiface.presentation.resources.permission_title
import com.fanguskdev.bakiface.render.PictureFit
import com.fanguskdev.bakiface.render.asImageBitmap
import com.fanguskdev.bakiface.render.drawPictureWithEffect
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val NAME_PLATE_MILLIS = 1_800L
private const val MOUTH_HINT_MILLIS = 3_500L
private const val FACE_HINT_DELAY_MILLIS = 1_500L

/**
 * The camera, wired (ADR 0003): collects the [CameraViewModel] state with the lifecycle, turns its one-off effects into
 * system actions and navigation, and tells it when the screen is on view. Everything drawn is in [CameraScreen].
 */
@Composable
fun CameraDestination(
    onOpenEditor: (ShotId, EffectId?) -> Unit,
    demo: DemoLaunch? = null,
    showFrameRate: Boolean = false,
    viewModel: CameraViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberCameraPermissionController { viewModel.onIntent(CameraIntent.PermissionChanged(it)) }
    val pickPhoto = rememberPhotoPicker { viewModel.onIntent(CameraIntent.PhotoPicked(it)) }
    val openSettings = rememberAppSettingsOpener()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CameraEffect.RequestCameraPermission -> permission.request()
                CameraEffect.OpenAppSettings -> openSettings()
                CameraEffect.LaunchPhotoPicker -> pickPhoto()
                is CameraEffect.OpenEditor -> onOpenEditor(effect.shotId, effect.effectId)
                is CameraEffect.ShowMessage -> snackbar.showSnackbar(getString(effect.message.text))
            }
        }
    }
    LaunchedEffect(demo) {
        if (demo != null) {
            viewModel.onIntent(CameraIntent.SelectEffect(demo.effectId?.let(::EffectId)))
            viewModel.onIntent(CameraIntent.PhotoPicked(PhotoSource(demo.photo)))
        }
    }
    LifecycleStartEffect(viewModel) {
        viewModel.onIntent(CameraIntent.ScreenShown)
        onStopOrDispose { viewModel.onIntent(CameraIntent.ScreenHidden) }
    }
    CameraScreen(state = state, snackbar = snackbar, onIntent = viewModel::onIntent, showFrameRate = showFrameRate)
}

private val CameraMessage.text: StringResource
    get() = when (this) {
        CameraMessage.CaptureFailed -> Res.string.message_capture_failed
        CameraMessage.ImportUnreadable -> Res.string.message_import_unreadable
        CameraMessage.ImportFailed -> Res.string.message_import_failed
        CameraMessage.CatalogFailed -> Res.string.message_catalog_failed
    }

/**
 * The camera screen, drawn from its [state] alone: the live picture with the chosen character, the carousel with the
 * shutter in its middle, and the panels that stand in for the camera when it cannot run. [showFrameRate], which only
 * debug builds turn on, adds how many frames per second reach the screen (R-03).
 */
@Composable
fun CameraScreen(
    state: CameraUiState,
    snackbar: SnackbarHostState,
    onIntent: (CameraIntent) -> Unit,
    showFrameRate: Boolean = false,
) {
    Box(Modifier.fillMaxSize().background(Ink)) {
        when {
            // The system is asking for the camera: the viewfinder waits, black, under its dialog (ADR 0011).
            state.permission == CameraPermission.Unknown -> Unit
            state.permission != CameraPermission.Granted -> PermissionPanel(state.permission, onIntent)
            state.status == CameraStatus.Unavailable -> StatusPanel(
                title = Res.string.camera_unavailable_title,
                body = Res.string.camera_unavailable_body,
                action = null,
                onIntent = onIntent,
            )
            state.status == CameraStatus.Failed -> StatusPanel(
                title = Res.string.camera_failed_title,
                body = Res.string.camera_failed_body,
                action = Res.string.camera_retry to CameraIntent.RetryCamera,
                onIntent = onIntent,
            )
            else -> LivePreview(
                frame = state.preview,
                description = state.selectedEffect.previewDescription(),
                modifier = Modifier.fillMaxSize().onSizeChanged { onIntent(CameraIntent.ViewportMeasured(it.width, it.height)) },
            )
        }
        if (state.status == CameraStatus.Starting) {
            CircularProgressIndicator(color = Bone, modifier = Modifier.align(Alignment.Center))
        }
        CaptureFlash(capturing = state.isCapturing)
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val live = state.permission == CameraPermission.Granted &&
                state.status != CameraStatus.Unavailable &&
                state.status != CameraStatus.Failed
            TopBar(
                showFlip = state.permission == CameraPermission.Granted && state.status != CameraStatus.Unavailable,
                frameRate = if (showFrameRate && live) state.preview?.framesPerSecond?.roundToInt() else null,
                onIntent = onIntent,
            )
            Spacer(Modifier.weight(1f))
            if (live) CameraHints(state)
            SnackbarHost(snackbar, modifier = Modifier.fillMaxWidth())
            if (live) BottomBar(state, onIntent)
        }
        if (state.isGalleryOpen) {
            EffectGallerySheet(
                effects = state.effects,
                selectedId = state.selectedEffectId,
                onChoose = { id ->
                    onIntent(CameraIntent.SelectEffect(id))
                    onIntent(CameraIntent.CloseGallery)
                },
                onDismiss = { onIntent(CameraIntent.CloseGallery) },
            )
        }
    }
}

@Composable
private fun EffectItem.previewDescription(): String = when (id) {
    null -> stringResource(Res.string.camera_preview_plain_description)
    else -> stringResource(Res.string.camera_preview_description, name)
}

/** The camera's frames with the effect drawn on them, filling the screen like a viewfinder. */
@Composable
fun LivePreview(frame: PreviewFrame?, description: String, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = description }) {
        if (frame != null) drawPictureWithEffect(frame.picture.asImageBitmap(), frame.render, PictureFit.Fill, frame.mirrored)
    }
}

/** The name of the app, the debug [frameRate] when there is one, and the button that turns the camera round. */
@Composable
private fun TopBar(showFlip: Boolean, frameRate: Int?, onIntent: (CameraIntent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = BakiTheme.spacing.s, vertical = BakiTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Wordmark(Modifier.padding(start = BakiTheme.spacing.s))
        if (frameRate != null) {
            CaptionBox(Modifier.padding(start = BakiTheme.spacing.m)) {
                Text(stringResource(Res.string.camera_frame_rate, frameRate), style = MaterialTheme.typography.labelMedium, color = Ink)
            }
        }
        Spacer(Modifier.weight(1f))
        if (showFlip) {
            InkIconButton(BakiIcons.FlipCamera, stringResource(Res.string.camera_flip), onClick = { onIntent(CameraIntent.FlipCamera) })
        }
    }
}

/** The name of the app as a sound effect: bone white letters with an ink outline, readable over any picture. */
@Composable
private fun Wordmark(modifier: Modifier = Modifier) {
    val style = TextStyle(fontFamily = delaGothicOne(), fontSize = MaterialTheme.typography.titleLarge.fontSize)
    val name = stringResource(Res.string.app_name)
    Box(modifier) {
        Text(text = name, style = style.copy(drawStyle = Stroke(width = 6f)), color = Ink)
        Text(text = name, style = style, color = Bone)
    }
}

@Composable
private fun BottomBar(state: CameraUiState, onIntent: (CameraIntent) -> Unit) {
    val selected = state.selectedEffect
    var plateFor by remember { mutableStateOf<EffectId?>(null) }
    var plateShown by remember { mutableStateOf(false) }
    LaunchedEffect(state.selectedEffectId) {
        if (plateFor != state.selectedEffectId || plateShown) {
            plateFor = state.selectedEffectId
            plateShown = true
            delay(NAME_PLATE_MILLIS)
            plateShown = false
        }
    }
    Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
        NamePlate(item = selected, visible = plateShown, modifier = Modifier.padding(start = BakiTheme.spacing.l))
        Spacer(Modifier.height(BakiTheme.spacing.s))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = BakiTheme.spacing.xs, vertical = BakiTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            InkIconButton(BakiIcons.Gallery, stringResource(Res.string.camera_gallery), onClick = {
                onIntent(CameraIntent.PickPhoto)
            }, enabled = !state.isImporting)
            EffectCarousel(
                effects = state.effects,
                selectedIndex = state.selectedIndex,
                onSelected = { index -> onIntent(CameraIntent.SelectEffect(state.effects.getOrNull(index)?.id)) },
                modifier = Modifier.weight(1f),
            ) {
                ShutterRing(
                    contentDescription = if (selected.id == null) {
                        stringResource(Res.string.camera_shutter_plain)
                    } else {
                        stringResource(Res.string.camera_shutter, selected.name)
                    },
                    onClick = { onIntent(CameraIntent.Capture) },
                    enabled = state.canCapture,
                )
            }
            InkIconButton(BakiIcons.Characters, stringResource(Res.string.camera_all_effects), onClick = {
                onIntent(CameraIntent.OpenGallery)
            })
        }
    }
}

/**
 * The caption with the chosen character's seal, name and nickname: it lands like a panel when the character changes and
 * goes away on its own. With less motion asked for, it just appears.
 */
@Composable
private fun NamePlate(item: EffectItem, visible: Boolean, modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) fadeIn(tween(0)) else scaleIn(tween(160), initialScale = 1.25f) + fadeIn(tween(120)),
        exit = fadeOut(tween(if (reduceMotion) 0 else 220)),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        CaptionBox {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KanjiSeal(kanji = item.kanji, accent = item.accentColor, size = 40.dp)
                Spacer(Modifier.width(BakiTheme.spacing.s))
                Column {
                    Text(text = item.displayName(), style = MaterialTheme.typography.headlineSmall, color = Ink)
                    item.alias()?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Ink) }
                }
            }
        }
    }
}

/** What to do next, in a caption: look at the camera when there is no face; open the mouth for the characters that react. */
@Composable
private fun CameraHints(state: CameraUiState) {
    var lookingForFace by remember { mutableStateOf(false) }
    LaunchedEffect(state.status, state.isFaceVisible) {
        lookingForFace = false
        if (state.status == CameraStatus.Running && !state.isFaceVisible) {
            delay(FACE_HINT_DELAY_MILLIS)
            lookingForFace = true
        }
    }
    var mouthHint by remember { mutableStateOf(false) }
    LaunchedEffect(state.selectedEffectId) {
        mouthHint = state.selectedEffect.hint == EffectHint.OpenMouth
        if (mouthHint) {
            delay(MOUTH_HINT_MILLIS)
            mouthHint = false
        }
    }
    val hint = when {
        lookingForFace -> Res.string.hint_find_face
        mouthHint && state.isFaceVisible -> Res.string.hint_open_mouth
        else -> null
    }
    Box(Modifier.fillMaxWidth().padding(bottom = BakiTheme.spacing.s), contentAlignment = Alignment.Center) {
        if (hint != null) {
            CaptionBox(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                Text(text = stringResource(hint), style = MaterialTheme.typography.titleMedium, color = Ink)
            }
        }
    }
}

/** A white flash over the picture when the photo is taken. */
@Composable
private fun CaptureFlash(capturing: Boolean) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(capturing) {
        if (capturing) {
            alpha.snapTo(0.85f)
            alpha.animateTo(0f, tween(260))
        }
    }
    if (alpha.value > 0f) Box(Modifier.fillMaxSize().background(Bone.copy(alpha = alpha.value)))
}

/** Asks for the camera with a reason, over the ink rays of a manga panel; offers a photo of the gallery instead. */
@Composable
private fun PermissionPanel(permission: CameraPermission, onIntent: (CameraIntent) -> Unit) {
    val forGood = permission == CameraPermission.PermanentlyDenied
    PanelScaffold(
        title = stringResource(Res.string.permission_title),
        body = stringResource(if (forGood) Res.string.permission_denied_body else Res.string.permission_body),
    ) {
        InkButton(
            text = stringResource(if (forGood) Res.string.permission_open_settings else Res.string.permission_allow),
            onClick = { onIntent(if (forGood) CameraIntent.OpenSettings else CameraIntent.RequestPermission) },
            primary = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(BakiTheme.spacing.m))
        InkButton(
            text = stringResource(Res.string.camera_gallery),
            onClick = { onIntent(CameraIntent.PickPhoto) },
            icon = BakiIcons.Gallery,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatusPanel(
    title: StringResource,
    body: StringResource,
    action: Pair<StringResource, CameraIntent>?,
    onIntent: (CameraIntent) -> Unit,
) {
    PanelScaffold(title = stringResource(title), body = stringResource(body)) {
        if (action != null) {
            InkButton(
                text = stringResource(action.first),
                onClick = { onIntent(action.second) },
                primary = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(BakiTheme.spacing.m))
        }
        InkButton(
            text = stringResource(Res.string.camera_gallery),
            onClick = { onIntent(CameraIntent.PickPhoto) },
            primary = action == null,
            icon = BakiIcons.Gallery,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PanelScaffold(title: String, body: String, actions: @Composable () -> Unit) {
    val colors = BakiTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .concentrationLines(colors.content.copy(alpha = 0.08f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(BakiTheme.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .background(colors.raised, MaterialTheme.shapes.small)
                .padding(BakiTheme.spacing.xl),
            horizontalAlignment = Alignment.Start,
        ) {
            KanjiSeal(kanji = "鬼", accent = colors.accent, size = 56.dp, dark = colors.isDark)
            Spacer(Modifier.height(BakiTheme.spacing.l))
            Text(text = title, style = MaterialTheme.typography.headlineMedium, color = colors.content)
            Spacer(Modifier.height(BakiTheme.spacing.s))
            Text(text = body, style = MaterialTheme.typography.bodyLarge, color = colors.content, textAlign = TextAlign.Start)
            Spacer(Modifier.height(BakiTheme.spacing.xl))
            actions()
        }
    }
}

@Preview
@Composable
private fun PermissionPreview() {
    BakiFaceTheme(darkTheme = false) {
        CameraScreen(
            state = CameraUiState(permission = CameraPermission.Denied),
            snackbar = remember {
                SnackbarHostState()
            },
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun UnavailablePreview() {
    BakiFaceTheme(darkTheme = true) {
        CameraScreen(
            state = CameraUiState(
                permission = CameraPermission.Granted,
                status = CameraStatus.Unavailable,
                effects = persistentListOf(EffectItem.Original),
            ),
            snackbar = remember { SnackbarHostState() },
            onIntent = {},
        )
    }
}
