import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Test doubles shared by the tests of every layer (ADR 0006): fakes of the domain's ports, never mocks, and faces drawn
// with the usual proportions. Only test source sets depend on it.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
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
            api(project(":core:domain"))
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
    }
}
