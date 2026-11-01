package com.fanguskdev.bakiface.presentation

import androidx.compose.runtime.Composable
import com.fanguskdev.bakiface.presentation.designsystem.BakiFaceTheme
import com.fanguskdev.bakiface.presentation.navigation.BakiFaceNavHost

/**
 * A photo to open at launch with a character, as if the person had picked it: only debug builds pass one (ADR 0010),
 * so the effects can be checked on an emulator or a simulator, which have no face in front of their camera. [photo] is
 * what the platform's picker would give (a `content://` or `file://` address on Android, a path on iOS) and
 * [effectId] the id of a character of the catalogue.
 */
data class DemoLaunch(val photo: String, val effectId: String?)

/**
 * The whole app: the theme around the two screens. The container is started by each platform before this runs.
 * [showFrameRate] puts the camera's frames per second on screen, for measuring the live effect on a real phone (R-03);
 * like [demo], only debug builds pass it.
 */
@Composable
fun App(demo: DemoLaunch? = null, showFrameRate: Boolean = false) {
    BakiFaceTheme {
        BakiFaceNavHost(demo = demo, showFrameRate = showFrameRate)
    }
}
