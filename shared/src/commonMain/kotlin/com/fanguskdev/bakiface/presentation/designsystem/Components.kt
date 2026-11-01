package com.fanguskdev.bakiface.presentation.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * A character's seal: its kanji in ink on the paper of its colour, inside an ink ring. The same seal names the
 * character in the carousel, in the gallery and in the editor.
 */
@Composable
fun KanjiSeal(
    kanji: String,
    accent: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    dark: Boolean = false,
) {
    val fill = accent.paperTint(dark)
    val ink = if (dark) Bone else Ink
    Box(
        modifier = modifier
            .size(size)
            .background(fill, CircleShape)
            .border(BakiTheme.strokes.ink, ink, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = kanji,
            color = ink,
            fontFamily = delaGothicOne(),
            fontSize = (size.value * 0.44f).sp,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A manga caption: bone white with an ink frame, readable over any picture. What the app says over the camera goes in
 * one of these.
 */
@Composable
fun CaptionBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .background(Bone, MaterialTheme.shapes.small)
            .border(BakiTheme.strokes.ink, Ink, MaterialTheme.shapes.small)
            .padding(horizontal = BakiTheme.spacing.m, vertical = BakiTheme.spacing.s),
    ) {
        content()
    }
}

/** A round button over the camera: bone with an ink ring, never smaller than a finger. */
@Composable
fun InkIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(BakiTheme.spacing.touch)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Bone.copy(alpha = if (enabled) 1f else 0.6f), CircleShape)
                .border(BakiTheme.strokes.ink, Ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Ink, modifier = Modifier.size(22.dp))
        }
    }
}

/**
 * A text button with an ink frame. The [primary] one is filled with the accent: one per screen, for the action that
 * finishes what the person came to do.
 */
@Composable
fun InkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = BakiTheme.colors
    val container = when {
        primary -> colors.accent
        else -> colors.raised
    }
    val contentColor = if (primary) colors.onAccent else colors.content
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = BakiTheme.spacing.touch),
        shape = MaterialTheme.shapes.small,
        color = container.copy(alpha = if (enabled) 1f else 0.5f),
        contentColor = contentColor,
        border = BorderStroke(BakiTheme.strokes.ink, colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(PaddingValues(horizontal = BakiTheme.spacing.l, vertical = BakiTheme.spacing.m)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(BakiTheme.spacing.s))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * The shutter: a thick bone ring with an ink edge that sits over the centre of the carousel, so the seal of the chosen
 * character shows inside it. It gives a little under the finger.
 */
@Composable
fun ShutterRing(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "shutter")
    Canvas(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
    ) {
        val ringWidth = 7.dp.toPx()
        val edge = 2.dp.toPx()
        val radius = this.size.minDimension / 2f
        drawCircle(color = Ink, radius = radius - edge / 2f, style = Stroke(width = edge))
        drawCircle(color = Bone, radius = radius - edge - ringWidth / 2f, style = Stroke(width = ringWidth))
        drawCircle(color = Ink, radius = radius - edge * 1.5f - ringWidth, style = Stroke(width = edge))
    }
}

/** Screentone: a grid of fine dots in [color], the grey of printed manga. */
fun Modifier.screentone(color: Color, spacing: Dp = 6.dp, dotRadius: Dp = 0.9.dp): Modifier = drawBehind {
    val step = spacing.toPx()
    val points = ArrayList<Offset>()
    var y = step / 2f
    var row = 0
    while (y < size.height) {
        var x = if (row % 2 == 0) step / 2f else step
        while (x < size.width) {
            points += Offset(x, y)
            x += step
        }
        y += step * 0.87f
        row++
    }
    drawPoints(points, PointMode.Points, color, strokeWidth = dotRadius.toPx() * 2f, cap = StrokeCap.Round)
}

/**
 * Concentration lines (集中線), the ink rays of a manga panel that point at what matters: here, the middle of the
 * space. Drawn the same every time.
 */
fun Modifier.concentrationLines(color: Color, count: Int = 120, seed: Int = 7): Modifier = drawBehind {
    val center = Offset(size.width / 2f, size.height / 2f)
    val reach = max(size.width, size.height)
    val random = Random(seed)
    repeat(count) {
        val angle = 2f * PI.toFloat() * random.nextFloat()
        val inner = reach * (0.32f + 0.18f * random.nextFloat())
        val direction = Offset(cos(angle), sin(angle))
        drawLine(
            color = color,
            start = center + direction * inner,
            end = center + direction * reach,
            strokeWidth = 1f + 4f * random.nextFloat(),
            cap = StrokeCap.Round,
        )
    }
}
