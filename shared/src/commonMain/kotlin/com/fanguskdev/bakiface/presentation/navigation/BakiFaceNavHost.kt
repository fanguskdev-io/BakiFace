package com.fanguskdev.bakiface.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.fanguskdev.bakiface.domain.effect.EffectId
import com.fanguskdev.bakiface.domain.shot.ShotId
import com.fanguskdev.bakiface.presentation.DemoLaunch
import com.fanguskdev.bakiface.presentation.feature.camera.CameraDestination
import com.fanguskdev.bakiface.presentation.feature.editor.EditorDestination
import kotlinx.serialization.Serializable

/** The camera, where the app starts (ADR 0009: typed routes, no strings). */
@Serializable
data object CameraRoute

/** The editor of a shot, opened with the character chosen on the camera, if any. */
@Serializable
data class EditorRoute(val shotId: String, val effectId: String? = null)

/** The two screens of the app and how one leads to the other; [demo] and [showFrameRate] come from debug builds only. */
@Composable
fun BakiFaceNavHost(navController: NavHostController = rememberNavController(), demo: DemoLaunch? = null, showFrameRate: Boolean = false) {
    NavHost(navController = navController, startDestination = CameraRoute) {
        composable<CameraRoute> {
            CameraDestination(
                onOpenEditor = { shotId, effectId -> navController.navigate(EditorRoute(shotId.value, effectId?.value)) },
                demo = demo,
                showFrameRate = showFrameRate,
            )
        }
        composable<EditorRoute> { entry ->
            val route = entry.toRoute<EditorRoute>()
            EditorDestination(
                shotId = ShotId(route.shotId),
                effectId = route.effectId?.let(::EffectId),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
