# ADR-0003 · Capas, MVI y regla de dependencias

| | |
|---|---|
| **Estado** | aceptado en la arquitectura (MVI, domain sin Android, data con repositorios y Result propio, flujo unidireccional: estándares del dueño, 2026-11-01); propuesto en el corte en módulos |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT |
| **Propone · decide** | Fangusk (la arquitectura) y agente (los módulos) · Fangusk |
| **Reversibilidad** | media · mover código entre módulos es mecánico; romper el flujo MVI no |

## Decisión

### Los módulos y la regla de dependencias

| Módulo | Qué tiene | Depende de |
|---|---|---|
| `core:domain` | Kotlin puro: modelos, geometría facial, motor de deformación, interfaces de repositorio, casos de uso, `AppResult` | nadie (solo coroutines y colecciones inmutables) |
| `core:render` | El dibujo de un fotograma con su efecto, con los gráficos de Compose, sin composables | domain |
| `core:data` | Los repositorios: catálogo JSON con DTO y mapper, DataStore, fotos en memoria, cámara y detector de cada plataforma | domain, render |
| `core:testing` | Los dobles de las pruebas (fakes, nunca mocks) y caras sintéticas | domain (solo lo usan las pruebas) |
| `shared` | La presentación: pantallas, ViewModels, sistema de diseño, navegación y la raíz de Koin; el framework de iOS | domain, render, data |
| `androidApp` | Solo arranca: `Application` (Koin) y la actividad | shared |

`core:domain` no puede importar Android ni Compose porque su módulo no los tiene: la regla se cumple al compilar.

### MVI en cada pantalla

- **UiState**: una `data class` inmutable por pantalla (`CameraUiState`, `EditorUiState`), expuesta como `StateFlow`.
- **UiIntent**: una `sealed interface` con todo lo que hace la persona; la UI solo llama a `onIntent()`.
- **UiEffect**: los eventos de una vez (pedir un permiso, abrir el editor, un mensaje) salen por un `Channel`, nunca en
  el estado.
- **Reductor puro**: `CameraReducer.reduce(estado, resultado)` y `EditorReducer.reduce(...)`, el único sitio donde cambia
  el estado; el ViewModel ejecuta los casos de uso y reduce lo que devuelven. Sin `Context` ni lógica de UI.
- **Flujo**: UI → Intent → ViewModel → UseCase → Repository → nuevo State → UI.

### Errores

Los fallos esperables viajan como valor (`AppResult.Success` / `AppResult.Failure` con un error tipado:
`CameraError`, `ImportError`, `ExportError`, `CatalogError`); los fallos de programación lanzan excepción.

### Una excepción razonada: los fotogramas

La cámara entrega unos 30 fotogramas por segundo. Pasan por el caso de uso `ObserveLivePreviewUseCase` (suavizado y
deformación calculados fuera del hilo principal) y llegan al estado como `PreviewFrame`; la imagen de cada fotograma
es un `Picture` opaco para el dominio (`BitmapPicture` en `core:render`), así que el dominio sigue sin gráficos.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Un solo módulo con paquetes | Menos configuración de Gradle | La regla «domain sin Android» se fiaría a la disciplina, no al compilador |
| MVVM con varios `StateFlow` por pantalla | Menos ceremonia | El dueño pidió MVI con un único estado y un reductor puro |
| Fotogramas fuera del ViewModel | Sin copias de estado a 30 fps | Rompería el flujo unidireccional que pidió el dueño; el coste medido no lo justifica |
