# ADR-0002 · La pila y sus versiones

| | |
|---|---|
| **Estado** | aceptado en lo que dictó el dueño (catálogo de versiones, Kotlin 2.x con el compilador de Compose, KSP y no kapt, versiones estables más recientes); propuesto en lo demás |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | alta · cada versión vive en `gradle/libs.versions.toml` |

## Contexto

Las versiones estables más recientes, comprobadas en Maven Central, en el Maven de Google, en el portal de plugins de
Gradle y en services.gradle.org el 2026-11-01: Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.5.0, Gradle 9.8.2,
CameraX 1.6.2, ML Kit face-detection 16.1.7, Koin 4.2.2, Navigation Compose 2.10.0, DataStore 1.3.0, coroutines 1.11.0,
kotlinx.serialization 1.12.0, kotlinx-collections-immutable 0.5.2, Turbine 1.2.1, Robolectric 4.17, Kover 0.9.11 y el
plugin de ktlint 14.2.0.

AGP y Gradle llegaron de la plantilla en 9.1.1 y 9.5.1 y se dieron por buenos sin comprobarlos; los delató el aviso de
la primera ejecución de la CI. AGP 9.5.0 pide Gradle 9.6.0 o posterior, y el Android Studio del dueño (compilación
262.10968.63) trae las herramientas de AGP 9.5.0, así que lo abre sin quejarse.

## Decisión

- **Catálogo de versiones** (`gradle/libs.versions.toml`) y Gradle con Kotlin DSL para todo.
- **Compose Multiplatform hace de BOM**: su plugin alinea Compose en Android e iOS; el BOM de Jetpack en Android lo
  desalinearía. **Material 3 estable**: la 1.9.0 de Compose Multiplatform (la plantilla traía la 1.12.0-alpha03).
- **Sin KSP ni kapt**: nada se genera por anotaciones (Koin con su DSL, sin Room). Si algún día hace falta, será KSP.
- **Android 10 (API 29) como mínimo**: guardar en la galería con MediaStore no pide permisos desde la 29, y lo anterior es
  una franja pequeña de teléfonos; `targetSdk` 37.
- **iOS 18.5 como mínimo**: una pieza de Skia de Compose 1.12 está compilada para 18.5, y con 18.2 el enlazador avisa.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Material 3 1.12.0-alpha03 de la plantilla | Va a la par de Compose 1.12 | Es alfa; el dueño pidió versiones estables |
| minSdk 24 de la plantilla | Más teléfonos | Pide el permiso de almacenamiento y su flujo para guardar en Android 7–9 |

- **AGP no puede pasar del Android Studio del dueño**: uno más nuevo de lo que conoce el IDE rompe la sincronización.
  Antes de subirlo se mira `buildVersion` en `plugins/android/lib/libagp-version.jar` del Android Studio instalado.

## Qué nos haría cambiar de opinión

Que el dueño necesite Android 7–9 (se añadiría el flujo del permiso de almacenamiento) o una versión estable de Material 3
a la par de Compose 1.12.
