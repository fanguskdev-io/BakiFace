# ADR-0009 · Navigation Compose con rutas tipadas

| | |
|---|---|
| **Estado** | propuesto · el dueño dio a elegir «Navigation 3 o Navigation Compose con rutas tipadas» |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | alta · un archivo (`navigation/BakiFaceNavHost.kt`) |

## Decisión

Navigation Compose 2.10 de Compose Multiplatform, con una actividad en Android y un `UIViewController` en iOS. Dos rutas
`@Serializable`, sin cadenas sueltas: `CameraRoute` y `EditorRoute(shotId, effectId)`. El editor recibe la foto y el
personaje de la ruta y Koin le pasa los dos a su ViewModel (ADR 0004).

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Navigation 3 (1.2.0, estable también en Compose Multiplatform) | La pila es una lista del estado de la app | Con dos pantallas no aporta; atar cada ViewModel a su entrada pide un decorador más, y en iOS guardar la pila exige registrar cada ruta en un módulo de serialización polimórfico. Se cambia en un archivo si la app crece |
