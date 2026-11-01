# ADR-0006 · La puerta de calidad

| | |
|---|---|
| **Estado** | propuesto · el dueño pidió «ktlint/detekt, sin warnings, sin !!» y pruebas de reductores, ViewModels, casos de uso y UI; la forma de la puerta la propone el agente |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT, RP |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | alta · un script y un flujo de CI |

## Decisión

Un solo punto de entrada, `scripts/verificar.sh`, que corre lo mismo en el Mac y en la CI y termina en «La puerta pasa»
solo si pasa todo:

| Paso | Qué comprueba |
|---|---|
| Sin `!!` | Ningún `.kt` del repositorio lo usa |
| Estilo | ktlint (plugin de Gradle 14.2.0) con el estilo de IntelliJ IDEA y 140 columnas (`.editorconfig`) |
| Sin avisos | Cada compilación de Kotlin con `allWarningsAsErrors` |
| Pruebas | `core:domain` en la JVM, `core:render`, `core:data` y `shared` en el host de Android (Robolectric para la UI) |
| Cobertura | Kover: al menos el 94 % de las líneas de lógica (fuera quedan los composables, el código que pide un teléfono y lo generado) |
| Lint | El lint de Android con `abortOnError` y las dependencias incluidas |
| APK | Debug y release; el release con R8 en modo completo |
| iOS (solo en el Mac) | Las pruebas de los cuatro módulos en el simulador, con Skia de verdad, y la app con `xcodebuild` |

- **Pruebas con dobles, no mocks**: los fakes viven en `core:testing`; los flujos se prueban con Turbine y los
  dispatchers inyectados (`StandardTestDispatcher`); la UI con `ComposeTestRule` sobre Robolectric.
- **La cobertura solo sube**: el mínimo es la cobertura de hoy redondeada hacia abajo; cuando sube, se sube el mínimo.
- **R8 tiene una regla**: ML Kit arranca sus componentes por reflexión y R8 borraba sus constructores; la app de release
  se caía al detectar la primera cara. `proguard-rules.pro` los conserva.
- **CI `mayeutic`** en GitHub Actions (Ubuntu), con las acciones fijadas por SHA. Salta iOS (`BAKIFACE_SKIP_IOS=1`)
  porque un minuto de macOS cuesta diez de Linux; iOS lo comprueba la puerta en el Mac antes de subir.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| detekt | Más reglas que ktlint | La 1.23.8 no funciona con Kotlin 2.4 ni Gradle 9 y la 2.0 sigue en alfa (2.0.0-alpha.6 el 2026-11-01). Se añade cuando salga estable |
| MockK | Lo nombró el dueño junto a los fakes | El dueño prefiere fakes «cuando sea posible», y aquí siempre lo fue; MockK además no funciona en Kotlin/Native |
| Un trabajo de macOS en la CI | iOS comprobado en cada subida | Diez veces más caro; se añade si el dueño lo quiere |
| Sin umbral de cobertura | Menos fricción | La cobertura bajaría sin que nadie lo decida |
