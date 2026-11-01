import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/** The percentage of lines of logic the tests must cover (ADR 0006). Raise it with the coverage; never lower it. */
val MIN_COVERED_LINES = 94

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    // ktlint (ADR 0006) reads the code people write; the generated accessors are left out by .editorconfig.

    // Sin avisos (ADR 0006): cualquier aviso del compilador de Kotlin, en cualquier plataforma, rompe la construcción.
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.allWarningsAsErrors.set(true)
    }
}

dependencies {
    kover(project(":core:domain"))
    kover(project(":core:data"))
    kover(project(":shared"))
}

kover {
    reports {
        filters {
            excludes {
                // What the JVM tests cannot reach or should not count (ADR 0006): the composables and their previews,
                // the generated resource accessors, the platform's own APIs (CameraX, ML Kit, MediaStore, the system's
                // screens) and the wiring of the container and the navigation. Everything else is logic, and counts.
                annotatedBy("androidx.compose.runtime.Composable", "androidx.compose.ui.tooling.preview.Preview")
                packages(
                    "com.fanguskdev.bakiface.presentation.resources",
                    "com.fanguskdev.bakiface.presentation.platform",
                    "com.fanguskdev.bakiface.presentation.navigation",
                    "com.fanguskdev.bakiface.presentation.di",
                    "com.fanguskdev.bakiface.data.camera",
                    "com.fanguskdev.bakiface.data.di",
                    // Drawn with real Skia by the iOS tests (RenderingOnSkiaTest); the JVM has no canvas to draw on.
                    "com.fanguskdev.bakiface.data.picture",
                )
                classes("com.fanguskdev.bakiface.data.shot.Android*", "*ComposableSingletons*", "*.BuildConfig")
            }
        }
        verify {
            // The minimum of covered lines of the logic (ADR 0006): it only goes up, never down.
            rule {
                minBound(MIN_COVERED_LINES)
            }
        }
    }
}
