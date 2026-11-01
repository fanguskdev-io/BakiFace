package com.fanguskdev.bakiface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fanguskdev.bakiface.presentation.App
import com.fanguskdev.bakiface.presentation.DemoLaunch

/** The only activity (ADR 0009): edge to edge, with the whole app in Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val demo = if (BuildConfig.DEBUG && savedInstanceState == null) demoLaunch() else null
        setContent {
            App(demo = demo, showFrameRate = BuildConfig.DEBUG)
        }
    }

    /** The photo and character of a debug launch (ADR 0010): `--es bakiface.demo.photo <uri> --es bakiface.demo.effect <id>`. */
    private fun demoLaunch(): DemoLaunch? {
        val photo = intent.getStringExtra(EXTRA_DEMO_PHOTO) ?: return null
        return DemoLaunch(photo, intent.getStringExtra(EXTRA_DEMO_EFFECT))
    }

    private companion object {
        const val EXTRA_DEMO_PHOTO = "bakiface.demo.photo"
        const val EXTRA_DEMO_EFFECT = "bakiface.demo.effect"
    }
}
