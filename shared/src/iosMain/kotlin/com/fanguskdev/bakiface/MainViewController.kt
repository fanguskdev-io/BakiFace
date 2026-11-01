package com.fanguskdev.bakiface

import androidx.compose.ui.window.ComposeUIViewController
import com.fanguskdev.bakiface.presentation.App
import com.fanguskdev.bakiface.presentation.DemoLaunch
import com.fanguskdev.bakiface.presentation.di.bakiFaceModules
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

/**
 * The app for iOS: starts the dependency container once (ADR 0004) and hosts the Compose app. Its name is the one the
 * Swift side of the template calls (`MainViewControllerKt.MainViewController()`), hence the capital letter.
 */
@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin { modules(bakiFaceModules()) }
    }
    val demo = demoLaunch()
    val showFrameRate = isDebugBuild()
    return ComposeUIViewController { App(demo = demo, showFrameRate = showFrameRate) }
}

@OptIn(ExperimentalNativeApi::class)
private fun isDebugBuild(): Boolean = Platform.isDebugBinary

/** The photo and character of a debug launch (ADR 0010): `-demoPhoto <path> -demoEffect <id>` as launch arguments. */
private fun demoLaunch(): DemoLaunch? {
    if (!isDebugBuild()) return null
    val arguments = NSProcessInfo.processInfo.arguments.filterIsInstance<String>()
    fun value(name: String): String? = arguments.indexOf(name).takeIf { it >= 0 }?.let { arguments.getOrNull(it + 1) }
    val photo = value("-demoPhoto") ?: return null
    return DemoLaunch(photo, value("-demoEffect"))
}
