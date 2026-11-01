rootProject.name = "BakiFace"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Las capas y su regla de dependencias (ADR 0003): domain no depende de nadie; render y data, de domain;
// shared (la presentación) junta las tres y es el framework de iOS; androidApp solo arranca.
include(":core:domain")
include(":core:testing")
include(":core:render")
include(":core:data")
include(":shared")
include(":androidApp")
