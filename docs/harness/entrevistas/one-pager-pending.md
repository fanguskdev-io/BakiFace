---
type: interview
titulo: Quién querría verse con los rasgos de un personaje de Baki, y qué hace hoy
para: Fangusk
de: agente (Claude Code, Opus 5.5)
dimension: [PU, PV, EE, EI]
preguntas: [PU-E1, PU-E2]
fase: P0
abierta: 2026-11-01
estado: abierta
---

# Quién querría verse con los rasgos de un personaje de Baki, y qué hace hoy

**Para quién es:** Fangusk. Te llevará un minuto por ronda; se contesta en una línea.

## De dónde sale esto

La adopción del arnés (P0, hacia G0). Antes de preguntar se midió el repositorio (una plantilla de Kotlin Multiplatform recién creada, sin historial de git), lo dicho en la conversación del 2026-11-01 y la búsqueda de diez minutos del one-pager §6. No se puede medir quién es el usuario (PU-E1), qué hace hoy ni los criterios de muerte: solo los sabe Fangusk.

## Ronda 1 — Quién lo usaría primero (PU-E1) · 2026-11-01

**Lo que he entendido:** quieres una app para Android e iPhone que, con la cámara, deforme en directo los rasgos de la cara de quien la usa hasta parecerse a personajes de Baki Hanma, sin sustituir su cara, al estilo de los efectos faciales de Instagram. Supongo que contestas tú, Fangusk, y que eres quien decide si esto sigue o se para.

**La pregunta:** ¿quién es la primera persona, con nombre o con un rol concreto, que usaría BakiFace, y en qué momento de su día lo haría?

**Por qué te la hago:** de ella sale el usuario del one-pager y, con él, la talla que te propondré. Si son tú y unas pocas personas a las que se la pasas gratis, sin tiendas, el riesgo de licencia de los personajes (R-01) casi desaparece. Si son fans a los que llega por Google Play o el App Store, ese riesgo pasa a ser candidato a criterio de muerte, porque las dos tiendas piden licencia para usar personajes con derechos de autor.

**Respuesta:** «yo soy la primera persona, no es necesario rol ni nombre, ni mucho menos momento» (2026-11-01)

## Ronda 2 — La última vez (PU-E2) · 2026-11-01

**Lo que he entendido:** la primera persona que usaría BakiFace eres tú, Fangusk, que la estás haciendo, y quedas como quien decide si sigue o se para. El momento del día sobraba: no lo pide PU-E1 y hacía la pregunta doble. «La primera» no lo leo como «la única»: si habrá más gente lo propondré con la talla.

**La pregunta:** cuéntame la última vez que quisiste verte como un personaje de Baki: qué estabas haciendo, qué pasó y qué hiciste después.

**Por qué te la hago:** de esa vez sale el problema del one-pager (§3) en tus palabras y lo que usas hoy, contra lo que se compara BakiFace en la ronda de contraste; una vez concreta, con fecha aproximada, es lo que pide G0.

**Respuesta:** «No es relevante, expongo el caso de lo que tiene que hacer la app» (2026-11-01)

*No cumple el* Busca *de PU-E2 (una vez concreta, con fecha aproximada): queda en «Sin respuesta todavía», en el one-pager §3 (docs/03 §4, regla 1). No se abre la ronda más pequeña que pide esa regla, porque el decisor dice que estas rondas no son relevantes: BakiFace es lo que él dice que tiene que hacer. Las rondas de descubrimiento se paran aquí por esa respuesta (agente, 2026-11-01).*

## En cola

*Parada por la respuesta del decisor a la ronda 2: no se abre ninguna de estas rondas mientras Fangusk no lo pida. Sin la vez concreta de PU-E2 y sin criterios de muerte, G0 no se puede revisar.*

- PU-E8 · Qué te costó esa vez · solo si la de PU-E2 no lo dice
- PU-E7 · Cada cuánto te pasa · su alarma lee el criterio del problema de G0
- Ronda de contraste (PV-E1) · En qué se diferencia esto de lo que haces hoy, con lo que cuentes en PU-E2 · cuando hayas contado qué haces hoy
- Criterios de muerte · Qué haría que esto no mereciera hacerse
- Pre-mortem · Dentro de un año esto ha fracasado: qué pasó
- EE-E1 · Cuántas horas a la semana has dedicado de verdad a esto, o a otro proyecto propio, en las últimas cuatro semanas
- EI-E1 · Quién puede salir perjudicado por esto sin ser su usuario
- DP · Qué datos de otras personas tendría que tocar
- La talla, el perfil y si su único usuario eres tú (one-pager §2) · los propone el agente con su motivo, cuando se sepa cómo te llega y si le llegará a alguien más
- LC · Desde qué país se vendería, se facturaría o se recibiría dinero de fuera · no cuenta en G0
- Cerrar la adopción · la fecha de cada criterio de muerte; los planes con dueño «(propuesto)» de `riesgos.md` (R-01, R-02, R-03, R-05); el objetivo en una frase y su criterio de aceptación

## El objetivo, en una frase

> 

**Confirmada por** — **el** —.

## Cómo sabremos que está bien

(hueco: lo dice Fangusk al confirmar el objetivo)

## Lo que NO se pregunta aquí

- La arquitectura, la pila y los estándares técnicos: los fijó Fangusk el 2026-11-01 y viven en los ADR de `docs/adr/`, como borrador anticipado.
- Si deformar o sustituir la cara: Fangusk lo dijo el 2026-11-01 («no quiero que la cara del usuario se sustituya por la del personaje seleccionado, quiero que los rasgos faciales del usuario se modifiquen para parecerse al personaje»); va a `docs/adr/`.
- La búsqueda de diez minutos: la mide el agente (one-pager §6).

## Consecuencia de no decidir

Sin la vez concreta de la ronda 2, el problema del one-pager sigue siendo un supuesto y G0 no se puede revisar. Mientras tanto, el agente solo da por supuesto lo suyo (medidas y el borrador anticipado del código), marcado como supuesto, y nunca la talla, el objetivo, los criterios de muerte ni la aceptación de un plan.

## Supresiones

