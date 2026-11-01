import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// El dibujo de un fotograma con su efecto (ADR 0005): la malla deformada, las marcas de piel y el color.
// Lo usan la presentación, para la pantalla, y los datos, para la foto que se guarda. Sin composables.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    android {
        namespace = "com.fanguskdev.bakiface.core.render"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        withHostTest {}
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.compose.ui.graphics)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
