# Decisiones de arquitectura (ADR)

Cada decisión que costaría deshacer tiene aquí su ADR: el contexto, la decisión, las alternativas reales con su motivo
de descarte y lo que cuesta cambiarla. Siguen la plantilla `templates/adr.md` del Mayeutic Harness, abreviada.

**Son un borrador anticipado del arnés** (`docs/harness/STATUS.md`): el proyecto está en P0 y estas decisiones existen antes
de G4 porque el dueño pidió construir ya. Las revisa la puerta que las pide. **Aceptada** quiere decir que la dictó el
dueño, Fangusk, con sus palabras; **Propuesta**, que la tomó el agente y el código la sigue mientras el dueño no diga otra
cosa. Solo el dueño pasa una propuesta a aceptada.

## Lo que se pidió, con sus palabras (2026-11-01)

- «basandonos en el apartado de Instragram de filtros faciales, quiero una app que aplique filtros creados basandonos en
  los personajes de Baki Hanma».
- «no quiero que la cara del usuario se sustituya por la del personaje seleccionado, quiero que los rasgos faciales del
  usuario se modifiquen para parecerse al personaje».
- «integrate con mayeutic para el desarrollo».
- Los estándares de arquitectura (MVI con UiState, UiIntent y UiEffect; domain sin Android; data con repositorios y un
  tipo Result propio) y técnicos (Kotlin 2.x, KSP y no kapt, catálogo de versiones, Material 3, `collectAsStateWithLifecycle`,
  colecciones inmutables, navegación con rutas tipadas, Hilt o Koin, coroutines con dispatchers inyectados, sistema de
  diseño propio con modo oscuro y edge-to-edge, accesibilidad, ktlint o detekt, sin avisos ni `!!`, pruebas de
  reductores, ViewModels con Turbine y dobles, casos de uso y UI con ComposeTestRule).

## Índice

| ADR | Decisión | Estado |
|---|---|---|
| [0001](0001-plataformas-y-kotlin-multiplatform.md) | Android e iOS con Kotlin Multiplatform y la UI común en Compose Multiplatform | Aceptada (2026-11-01) |
| [0002](0002-la-pila-y-sus-versiones.md) | La pila y sus versiones estables, sin KSP porque no hay nada que procesar | Aceptada en lo dictado; propuesta en lo demás |
| [0003](0003-capas-mvi-y-regla-de-dependencias.md) | Cinco módulos por capas, MVI y la regla de dependencias | Aceptada en lo dictado; propuesta en el corte |
| [0004](0004-inyeccion-con-koin.md) | Koin para la inyección de dependencias | Propuesta |
| [0005](0005-deformar-los-rasgos.md) | Deformar los rasgos con una malla, no sustituir la cara | Aceptada en el qué; propuesta en el cómo |
| [0006](0006-la-puerta-de-calidad.md) | La puerta de calidad: estilo, pruebas, cobertura que solo sube, lint y R8 | Propuesta |
| [0007](0007-nada-sale-del-telefono.md) | Nada sale del teléfono: sin internet, todo en el dispositivo | Propuesta |
| [0008](0008-el-sistema-de-diseno.md) | El sistema de diseño «Tinta y sudor» | Propuesta |
| [0009](0009-navegacion-tipada.md) | Navigation Compose con rutas tipadas y una sola actividad | Propuesta |
| [0010](0010-demostracion-y-simulador.md) | Las herramientas de depuración (arranque de demostración, contador de fotogramas) y Vision en el simulador | Propuesta |
