package com.fanguskdev.bakiface

import android.app.Application
import com.fanguskdev.bakiface.presentation.di.bakiFaceModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/** Starts the dependency container once per process (ADR 0004), with the application as the Android context. */
class BakiFaceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BakiFaceApplication)
            modules(bakiFaceModules())
        }
    }
}
