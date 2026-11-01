package com.fanguskdev.bakiface.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fanguskdev.bakiface.presentation.resources.Res
import com.fanguskdev.bakiface.presentation.resources.dela_gothic_one
import org.jetbrains.compose.resources.Font

/** True black: manga ink, and every line drawn over the camera. */
val Ink = Color(0xFF000000)

/** Bone white: the caption boxes and buttons that float over the camera, in both themes. */
val Bone = Color(0xFFFFFFFF)

/**
 * The palette of BakiFace (ADR 0008): ink and newsprint, the paper of the weekly magazines, with blood kept for the one
 * action that matters on each screen. Every role is checked for contrast in both themes.
 */
@Immutable
data class BakiColors(
    val background: Color,
    val content: Color,
    val contentMuted: Color,
    val raised: Color,
    val outline: Color,
    val accent: Color,
    val onAccent: Color,
    val isDark: Boolean,
)

val LightBakiColors = BakiColors(
    background = Color(0xFFE4E2DC),
    content = Ink,
    contentMuted = Color(0xFF5E5B56),
    raised = Bone,
    outline = Ink,
    accent = Color(0xFFA3121A),
    onAccent = Bone,
    isDark = false,
)

val DarkBakiColors = BakiColors(
    background = Ink,
    content = Bone,
    contentMuted = Color(0xFFB5B2AC),
    raised = Color(0xFF1E1E1E),
    outline = Bone,
    accent = Color(0xFFEF4A50),
    onAccent = Ink,
    isDark = true,
)

/** The paper a character is printed on: its accent washed into white (or into black at night). */
fun Color.paperTint(dark: Boolean): Color = if (dark) lerp(Ink, this, 0.32f) else lerp(Bone, this, 0.2f)

/** The steps of space between things, on a 4 dp grid. [touch] is the smallest thing a finger has to hit. */
@Immutable
data class BakiSpacing(
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val touch: Dp = 48.dp,
)

/** The widths of the ink: a hairline, the line of a caption and the frame of a panel. */
@Immutable
data class BakiStrokes(val hairline: Dp = 1.dp, val ink: Dp = 2.dp, val panel: Dp = 3.dp)

val LocalBakiColors = staticCompositionLocalOf { LightBakiColors }
val LocalBakiSpacing = staticCompositionLocalOf { BakiSpacing() }
val LocalBakiStrokes = staticCompositionLocalOf { BakiStrokes() }

/** The design system, read from anywhere inside [BakiFaceTheme]. */
object BakiTheme {
    val colors: BakiColors
        @Composable @ReadOnlyComposable
        get() = LocalBakiColors.current

    val spacing: BakiSpacing
        @Composable @ReadOnlyComposable
        get() = LocalBakiSpacing.current

    val strokes: BakiStrokes
        @Composable @ReadOnlyComposable
        get() = LocalBakiStrokes.current
}

/** Square-ish panels like the ones of a manga page; only the sheet that slides up is rounded. */
val BakiShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Dela Gothic One, the heavy face of the titles and the kanji. A subset: Latin, Spanish and the kanji of the seals. */
@Composable
fun delaGothicOne(): FontFamily = FontFamily(Font(Res.font.dela_gothic_one))

/**
 * Display, headline and large titles in Dela Gothic One, the voice of the characters; everything a person reads to use
 * the app in the system's own face, which reads best at small sizes.
 */
@Composable
fun bakiTypography(): Typography {
    val display = delaGothicOne()
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = display, fontSize = 44.sp, lineHeight = 50.sp, letterSpacing = 0.sp),
        displayMedium = base.displayMedium.copy(fontFamily = display, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = 0.sp),
        displaySmall = base.displaySmall.copy(fontFamily = display, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = display, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = display, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = display, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp),
        titleLarge = base.titleLarge.copy(fontFamily = display, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
    )
}

private fun BakiColors.toColorScheme(): ColorScheme = if (isDark) {
    darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        background = background,
        onBackground = content,
        surface = background,
        onSurface = content,
        surfaceVariant = raised,
        onSurfaceVariant = contentMuted,
        surfaceContainer = raised,
        surfaceContainerHigh = raised,
        surfaceContainerLow = raised,
        outline = outline,
        outlineVariant = contentMuted,
        error = accent,
        onError = onAccent,
    )
} else {
    lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        background = background,
        onBackground = content,
        surface = background,
        onSurface = content,
        surfaceVariant = raised,
        onSurfaceVariant = contentMuted,
        surfaceContainer = raised,
        surfaceContainerHigh = raised,
        surfaceContainerLow = raised,
        outline = outline,
        outlineVariant = contentMuted,
        error = accent,
        onError = onAccent,
    )
}

/** The theme of the whole app (ADR 0008), following the system's light or dark setting. */
@Composable
fun BakiFaceTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkBakiColors else LightBakiColors
    CompositionLocalProvider(
        LocalBakiColors provides colors,
        LocalBakiSpacing provides BakiSpacing(),
        LocalBakiStrokes provides BakiStrokes(),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = bakiTypography(),
            shapes = BakiShapes,
            content = content,
        )
    }
}
