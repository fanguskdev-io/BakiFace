# Licencias de terceros

Lo que la app lleva dentro de otros, con su licencia, y lo que solo se usa para compilarla y probarla. Revisado el
2026-11-01 contra `gradle/libs.versions.toml`.

## Dentro de la app

| Qué | Para qué | Licencia |
|---|---|---|
| Dela Gothic One (The Dela Gothic Project Authors) | La letra de los títulos y los sellos en kanji, en un subconjunto de 38 KB | SIL Open Font License 1.1, sin nombre reservado: el subconjunto conserva el nombre. El texto va dentro de la app (`shared/src/commonMain/composeResources/files/licenses/OFL-DelaGothicOne.txt`) |
| ML Kit Face Detection 16.1.7 (Google) | Encontrar la cara en Android, con el modelo dentro de la app | Gratuito, con las [condiciones de ML Kit](https://developers.google.com/ml-kit/terms); no es software libre |
| Kotlin, kotlinx.coroutines, kotlinx.serialization, kotlinx-collections-immutable (JetBrains) | El lenguaje y sus bibliotecas | Apache 2.0 |
| Compose Multiplatform y Skiko (JetBrains) | La UI en Android e iOS | Apache 2.0 |
| Skia (Google), dentro de Skiko en iOS | El dibujo en iOS | BSD de 3 cláusulas |
| AndroidX: Core, Activity, Lifecycle, Navigation, DataStore y CameraX (Google) | La plataforma de Android, la navegación, las preferencias y la cámara | Apache 2.0 |
| Koin (Kotzilla) | La inyección de dependencias | Apache 2.0 |

Vision, AVFoundation, PhotoKit y UIKit son del sistema de iOS y no se redistribuyen.

## Solo para compilar y probar

| Qué | Licencia |
|---|---|
| kotlin-test, Kover (JetBrains) | Apache 2.0 |
| Turbine (Cash App) | Apache 2.0 |
| AndroidX Test y Compose UI Test (Google) | Apache 2.0 |
| Robolectric | MIT |
| JUnit 4 | Eclipse Public License 1.0 |
| ktlint y su plugin de Gradle | MIT |

## Pendiente antes de publicar

- **Una pantalla de licencias en la app.** Apache 2.0 pide conservar los avisos (`NOTICE`) de lo que se redistribuye y
  la licencia BSD de Skia, reproducir su aviso en la distribución binaria. Hoy la app solo lleva el texto de la OFL.
- **Los personajes no son una licencia de software**, pero son lo que más pesa: *Baki* es obra de Keisuke Itagaki,
  editada por Akita Shoten. La app no lleva ninguna imagen del manga ni del anime: cada marca se dibuja con código. Aun
  así, sus nombres y los rasgos reconocibles de los personajes son R-01 en
  [`harness/riesgos.md`](harness/riesgos.md).
- **La foto de prueba** con la que se revisaron los efectos (la *Gioconda*, de dominio público) no está en el
  repositorio.
