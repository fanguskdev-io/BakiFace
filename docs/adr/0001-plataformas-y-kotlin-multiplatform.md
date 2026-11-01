# ADR-0001 · Android e iOS con Kotlin Multiplatform

| | |
|---|---|
| **Estado** | aceptado: el dueño creó la plantilla de Kotlin Multiplatform con Compose Multiplatform el 2026-11-01 |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT |
| **Propone · decide** | Fangusk · Fangusk |
| **Reversibilidad** | baja · toda la UI y la lógica son comunes; separarlas sería reescribir una de las dos apps |

## Contexto

| Condiciona | De dónde sale | Evidencia |
|---|---|---|
| La app es para Android e iPhone | La plantilla del repositorio, con `androidApp`, `iosApp` y `shared` | observado |
| El efecto (deformar la cara) tiene que ser el mismo en las dos | La petición del dueño | reportado |
| La cámara y la detección facial son de cada plataforma | CameraX y ML Kit en Android; AVFoundation y Vision en iOS | observado |

## Decisión

Una sola base de Kotlin Multiplatform: el dominio, el dibujo del efecto, los repositorios comunes, las pantallas y el
sistema de diseño se escriben una vez; cada plataforma aporta solo su cámara, su detector facial, su biblioteca de fotos
y sus diálogos del sistema, detrás de las interfaces del dominio (ADR 0003). La UI es Compose Multiplatform en las dos.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Dos apps nativas (Compose + SwiftUI) | Lo más idiomático en cada una | El motor de deformación y los personajes se escribirían y probarían dos veces |
| Flutter o React Native | Una UI común | El dueño ya eligió KMP; la cámara con dibujo por fotograma pide código nativo igualmente |

## Reversibilidad

Rehacer iOS en SwiftUI costaría semanas: la lógica de `core:domain` seguiría sirviendo, solo cambiarían las pantallas.
