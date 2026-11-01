# ADR-0004 · Koin para la inyección de dependencias

| | |
|---|---|
| **Estado** | propuesto · el dueño dio a elegir «Hilt \| Koin» |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | media · tres módulos de Koin (`dataModule`, `domainModule`, `presentationModule`) |

## Contexto

La UI y los ViewModels son comunes a Android e iOS (ADR 0001). Hilt es solo de Android: genera código con KSP sobre
clases de Android y no existe en Kotlin/Native.

## Decisión

Koin 4.2.2, con su DSL y sin anotaciones: `dataModule()` (repositorios y lo de cada plataforma en `platformDataModule()`),
`domainModule` (los casos de uso, `factory`) y `presentationModule` (los ViewModels, con `viewModel { }`). Las pantallas
los piden con `koinViewModel()`; el editor recibe la foto y el personaje de su ruta con `parametersOf`. Koin arranca una
vez: en `BakiFaceApplication` en Android y en `MainViewController()` en iOS. No hay *service locators* a mano.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Hilt | Comprobación en compilación | No funciona en iOS; obligaría a otra inyección allí |
| kotlin-inject o Metro | Comprobación en compilación en KMP | Exigen KSP o un plugin del compilador y el dueño no los nombró |
| Inyección a mano | Sin dependencias | El dueño pidió «sin service locators manuales» |
