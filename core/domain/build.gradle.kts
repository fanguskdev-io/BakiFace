import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// La capa de dominio (ADR 0003): Kotlin puro, sin Android, sin Compose y sin ningún SDK de plataforma.
// El objetivo jvm() sirve a Android (que consume su variante JVM) y a las pruebas rápidas.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kover)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.collections.immutable)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
