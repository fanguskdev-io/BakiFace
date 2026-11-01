---
harness_version: 1.4.0                 # la que declara scoring/*.yaml del arnés; su «Sin publicar» está vacía (commit 27b8092, 2026-10-12)
harness_path: ../mayeutic-harness      # relativa a la raíz de este repositorio
proyecto: BakiFace
estado: activo
talla: sin declarar                    # la decide la persona; su ronda está en cola en entrevistas/one-pager-pending.md
perfil:
  etiquetas: [software]
  propuesto_por: agente
  propuesto_el: 2026-11-01
  confirmado_por: pendiente
  confirmado_el:
fase: P0
proxima_puerta: G0
decisor: Fangusk                       # fijado al contestar la ronda 1 sin corregirlo (docs/05 §2)
fecha_revision:                        # vacía: todavía no hay acta, ni criterio de muerte con fecha, ni riesgo aceptado
actualizado: 2026-11-01
---

# Estado · BakiFace

## Dónde estamos

P0, la chispa. El 2026-11-01 se adoptó el arnés sobre la plantilla recién creada de Kotlin Multiplatform: el one-pager es un borrador con huecos. En la ronda 1, Fangusk dijo que el primer usuario es él; en la 2, que la última vez que quiso verse como un personaje de Baki «no es relevante»: BakiFace es lo que él dice que tiene que hacer. Las rondas de descubrimiento quedan paradas por esa respuesta, con su cola escrita en el hilo. Sin una vez concreta del problema ni criterios de muerte, G0 no se puede revisar y el proyecto sigue en P0; el arnés sigue sirviendo al desarrollo (ADR, puerta de calidad y CI `mayeutic`). A la vez, porque Fangusk lo pidió («integra con mayeutic para el desarrollo», 2026-11-01), el agente construye la app como **borrador anticipado** (docs/07 §1 del arnés): el código y los ADR de [`docs/adr/`](../adr/README.md) existen antes de G4, los revisan las puertas que los piden y no deciden la talla, los criterios de muerte ni ninguna salida.

**Siguiente paso:** ninguno del arnés; el desarrollo sigue por orden de Fangusk. Si un día quiere revisar G0, se abre la primera ronda de la cola de [entrevistas/one-pager-pending.md](entrevistas/one-pager-pending.md).

## Qué se está haciendo

| Qué | Dimensión | Quién | Desde | Estado |
|---|---|---|---|---|
| Adopción del arnés ([hilo](entrevistas/one-pager-pending.md)) | PU, PV, EE, EI | agente (Claude Code, Opus 5.5) | 2026-11-01 | `bloqueado por Fangusk`: en la ronda 2 dijo que las rondas de descubrimiento no son relevantes (2026-11-01) |
| Borrador anticipado de la app: código, puerta de calidad y [ADR](../adr/README.md), pedido por Fangusk | AT | agente (Claude Code, Opus 5.5) | 2026-11-01 | `en curso`: la app completa compila y pasa la puerta en Android e iOS; falta verla en teléfonos reales con caras reales (R-03, R-05). `comprobar` avisa de que se escribió sin criterios de muerte confirmados (docs/07 §1 del arnés): Fangusk pidió construir ya, y el borrador no cuenta como evidencia de ninguna puerta hasta que confirme los del one-pager, cuya ronda está en cola |

## Equipo e hitos (EE)

| Persona o agente | Qué parte hace | Horas por semana (medidas, últimas cuatro semanas; EE-E1) |
|---|---|---|
| Fangusk | la idea, las decisiones y las pruebas en sus teléfonos | (hueco: en cola) |
| Claude Code (Opus 5.5) | el código, los ADR y la puerta de calidad | no aplica: es un agente |

## Qué espera respuesta humana

| Entrevista | Para quién | Ronda abierta | Desde | Qué decide la respuesta |
|---|---|---|---|---|

## Criterios de muerte vivos

| # | Criterio | Fecha | Estado | Resolución |
|---|---|---|---|---|

## Condiciones pendientes

| Condición | Puerta | Dueño | Fecha | Estado |
|---|---|---|---|---|

## Scorecard provisional

| Dimensión | Nota provisional | Confianza | Fichero |
|---|---|---|---|
| PU | 0 · sin cerrar · PU-E1 contestada (Fangusk); PU-E2 sin respuesta: el decisor la ve no relevante | baja | [one-pager §2](one-pager.md#2-el-usuario-con-nombre-pu) |
| PV | 0 · sin cerrar · one-pager §1 | baja | [one-pager §1](one-pager.md#1-lo-que-creemos-pv) |
| MC | 0 · sin cerrar · one-pager §6 | baja | [one-pager §6](one-pager.md#6-existe-ya-la-búsqueda-de-diez-minutos-mc) |
| EE | 0 · sin cerrar · one-pager §7 | baja | [one-pager §7](one-pager.md#7-quién-lo-hace-y-con-cuánto-tiempo-ee) |
| EI | 0 · ninguna | baja | — |

## Actas

| Puerta | Fecha | Salida | Acta |
|---|---|---|---|

## Entregas

| Fecha | Salida | Afirmaciones de la portada | Commit | Versión del arnés | A quién se entregó |
|---|---|---|---|---|---|

## Riesgos que hay que mirar esta semana

- R-01 · licencia de los personajes de Baki si se publica en tiendas · impacto alto, sin plan aceptado.
- R-04 · la plantilla «Ya me vi Baki» de CapCut ya cubre parte de lo que quiere el fan · impacto alto, sin mitigación hasta la ronda de contraste.
- R-03 y R-05 · el efecto en vivo no se ha visto en un teléfono real ni con caras variadas · la compilación de depuración ya muestra los fotogramas por segundo para medirlo.

## Historial de este fichero

| Fecha | Quién | Qué cambió |
|---|---|---|
| 2026-11-01 | agente (Claude Code, Opus 5.5) | Adopción del arnés 1.4.0 en modo nuevo, con la ronda 1 abierta; borrador anticipado de la app pedido por Fangusk |
| 2026-11-01 | agente (Claude Code, Opus 5.5) | Borrador anticipado: la app completa (cámara en vivo, nueve personajes, editor, guardar y compartir) con la puerta de calidad en verde en Android e iOS; ADR 0001–0010, `AGENTS.md` con el bloque del arnés, `docs/licencias.md`; contador de fotogramas para R-03 y R-05 nuevo en el registro de riesgos |
| 2026-11-01 | agente (Claude Code, Opus 5.5) | Ronda 1 contestada: el primer usuario es Fangusk, que queda como decisor (también en el bloque de `AGENTS.md`); abierta la ronda 2 (PU-E2) |
| 2026-11-01 | agente (Claude Code, Opus 5.5) | Ronda 2 contestada sin la vez concreta («No es relevante, expongo el caso de lo que tiene que hacer la app»): rondas de descubrimiento paradas por el decisor, cola escrita en el hilo; P0 sigue abierta |
| 2026-11-01 | agente (Claude Code, Opus 5.5) | Lo que tiene que hacer la app, dictado por Fangusk, en [ADR 0011](../adr/0011-lo-que-tiene-que-hacer-la-app.md): cinco personajes, la cámara de selfies al arrancar y el aspecto de «Ya me vi Baki» (pliegues, ojos en blanco, cejas de tinta) |
