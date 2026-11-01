# One-pager · BakiFace

| | |
|---|---|
| **Proyecto** | BakiFace (el nombre del repositorio; el de marca lo decide una persona en MI) |
| **Fecha** | 2026-11-01 |
| **Autor/a** | Fangusk (medido: la identidad de git de este Mac, `Fangusk <fangusk.dev@gmail.com>`; confirmado al contestar la ronda 1 sin corregirlo, 2026-11-01) |
| **Objetivo, en una frase** | Que quien la use se vea en directo, con la cámara del móvil, con sus propios rasgos deformados hasta parecerse a un personaje de Baki Hanma, y pueda guardar o compartir esa foto · propuesto |
| **Talla** | sin declarar (motivo en la sección 8) |
| **Retroactivo** | no |

## 1. Lo que creemos (PV)

Hoy, quien quiere verse «como» un personaje de Baki sube una selfie a una plantilla de IA de CapCut («Ya me vi Baki», de moda en TikTok en 2026) y recibe una imagen fija de un solo personaje, Yujiro, generada a partir de su foto. Creemos que verse en directo, con sus propios rasgos llevados hacia los de cada personaje y no con su cara sustituida, sería más divertido y más suyo. (PV-E1 en parte: el cambio frente a lo que el usuario hace hoy lo da la ronda de contraste, sección 6)

## 2. El usuario, con nombre (PU)

- **Quién:** Fangusk, quien tuvo la idea y la está haciendo: «yo soy la primera persona» (PU-E1, [ronda 1](entrevistas/one-pager-pending.md), 2026-11-01) · reportado. Que sea su único usuario no está dicho: «la primera» no dice «la única»
- **Usuarios con nombre:** (hueco: aparte de Fangusk, ninguno dicho; si habrá más lo propone el agente con la talla)
- **Dónde y cuántos, a ojo:** (hueco) · uno con nombre, Fangusk; sigue sin medir el supuesto del agente: fans de Baki que publican en TikTok; las páginas de descubrimiento de TikTok «Yujiro Hanma Face» y «Baki AI filter» se actualizaron entre agosto y septiembre de 2026, sin cifra medida
- **Plataforma:** Android e iPhone · observado en el repositorio (la plantilla de Kotlin Multiplatform tiene las dos, creada el 2026-11-01); que sea donde ese usuario lo haría hoy (CapCut y TikTok, en el móvil) es supuesto
- **Cómo sabemos que existe:** reportado · Fangusk dice que la usaría (ronda 1), y lo que cuenta quien la hace no cuenta como conversación con alguien de fuera (docs/03 §5 del arnés); reportado · la moda «Ya me vi Baki» en TikTok (páginas de descubrimiento, agosto–septiembre de 2026) y el meme «Veiny Face Yujiro» (Know Your Meme, desde marzo de 2026); que alguien concreto lo quiera en directo, supuesto
- **Para quién no es:** (hueco)

## 3. El problema, en una frase (PU)

> (hueco: la frase en palabras de ese usuario, que dice quien tuvo la idea; borrador del agente, supuesto: «Quiero verme como Yujiro, no pegar su cara encima de la mía»)

- **La última vez:** (hueco: PU-E2, en cola). La búsqueda no encontró una queja pública que cuente una vez concreta (quién, cuándo, qué pasó): sin medir.

## 4. Hipótesis: qué tendría que ser cierto

| # | Hipótesis | Tipo | Cómo la comprobaríamos | Evidencia hoy |
|---|---|---|---|---|
| H1 | Hay fans de Baki que quieren verse con los rasgos de un personaje y hoy lo resuelven con una plantilla de fotos de un solo personaje | problema | dos o más fans, sin sugerírselo, cuentan la última vez que lo hicieron y con qué | supuesto (reportado solo como moda pública) |
| H2 | Ese fan preferiría verse en directo y con varios personajes antes que subir una foto a la plantilla de CapCut | valor | de los fans de H1, cuántos la usan dos veces o más en una semana frente a la plantilla | supuesto |
| H3 | (hueco: quién dedicaría tiempo o pagaría, según cómo se distribuya; sale de la ronda 1 y del perfil) | negocio | (hueco) | (hueco) |
| H4 | Se pueden deformar los rasgos en directo, a 24 fotogramas por segundo o más en un móvil de gama media, con la detección facial del propio teléfono (ML Kit en Android, Vision en iOS) y sin servidor | ejecución | la medida de fotogramas por segundo del borrador anticipado en un Android y un iPhone de Fangusk | supuesto |

*Las hipótesis las ha formulado el agente con lo dicho y con la búsqueda: la ronda que toque las devuelve para que Fangusk las corrija.*

## 5. Criterios de muerte

*(hueco: los dice Fangusk en la ronda de criterios de muerte, en cola; el agente no los inventa)*

| # | Criterio: si en esa fecha… | Fecha | …entonces | Hipótesis que cae | Clase | Estado | Resolución |
|---|---|---|---|---|---|---|---|

**De dónde sale cada uno:** (hueco)

**Pre-mortem:** (hueco: en cola)

## 6. ¿Existe ya? La búsqueda de diez minutos (MC)

- **Términos buscados:** «Baki Hanma face filter effect TikTok Snapchat lens Yujiro» · «Baki filter app Google Play anime face morph realtime» · «Meta Spark AR shutdown January 2025 third-party Instagram effects removed» · **Fecha:** 2026-11-01
- **Lo más parecido encontrado:** la plantilla de IA «Ya me vi Baki» de CapCut, enlazada desde TikTok ([tiktok.com/discover/baki-ai-filter](https://www.tiktok.com/discover/baki-ai-filter), [tiktok.com/discover/yujiro-hanma-face](https://www.tiktok.com/discover/yujiro-hanma-face)): se sube una selfie y devuelve la cara de Yujiro deformada con su sonrisa; precio de la plantilla, sin medir. Detrás, apps de filtros anime de IA en Google Play que trabajan sobre una foto con estilos genéricos, ninguna de Baki ([Anime Filter Face Photo Editor](https://play.google.com/store/apps/details?id=com.livdg.ai.anime.art), [Morpho](https://play.google.com/store/apps/details?id=com.purevision.morpho)). No apareció ninguna lente de Snapchat ni ningún efecto nativo de TikTok de Baki, y desde el 14 de enero de 2025 Instagram no tiene efectos de terceros, porque Meta cerró Spark ([spark.meta.com/learn](https://spark.meta.com/learn/)) (lectura de la IA, verificada el 2026-11-01)
- **Qué tareas de las nuestras cubre:** verse con la cara de un personaje de Baki en una imagen para compartir: sí, en una foto y solo con Yujiro. Verse en directo con la cámara: no. Elegir entre varios personajes: no. Conservar la cara propia deformando sus rasgos, frente a generar una imagen nueva: sin medir
- **Límites de su plan gratuito:** sin medir (CapCut tiene plan gratuito y de pago; el de la plantilla no se midió) · 2026-11-01
- **Nota, valoraciones y quejas:** sin medir: no se leyó la ficha de CapCut ni sus reseñas; «bien hecho» queda sin medir y no cuenta a favor de la idea
- **¿Hace exactamente esto, gratis y bien hecho?** No, por lo medido: no deforma la cara en directo con la cámara ni da a elegir personaje; trabaja sobre una foto y con uno solo. «Gratis» y «bien hecho», sin medir
- **La diferencia, en una frase:** (hueco: sale de la ronda de contraste, en cola, frente a lo que cuente Fangusk que hace hoy)

## 7. Quién lo hace y con cuánto tiempo (EE)

- **Personas:** Fangusk (hueco: horas por semana de las últimas cuatro semanas, EE-E1, en cola)
- **Agentes y herramientas:** Claude Code (Opus 5.5) escribe el código, los ADR de `docs/adr/` y la puerta de calidad, con los estándares que fijó Fangusk el 2026-11-01, como borrador anticipado; Android Studio, el emulador de Android y Xcode con sus simuladores, en este Mac (observado)
- **Tiempo disponible para P1:** (hueco) · **Dependencias externas:** ML Kit en Android, con el modelo dentro de la app (ADR 0007), y el framework Vision en iOS, para detectar los rasgos en el teléfono; las tiendas, si se publica (R-01)
- **Lo que pide P1 en la talla:** (hueco: con la talla sin declarar) · **Antes del:** (hueco)
- **Evidencia de fuera:** (hueco)
- **Presupuesto de la idea:** ninguno, mientras la persona no fije uno

## 8. Talla y motivo

- **Talla:** sin declarar: la ronda de la talla está en cola
- **Por qué:** (hueco: el agente la propone con su motivo cuando se sepa quién la usaría aparte de quien la hace, sección 2, y cómo le llega)

## 9. Declaraciones

| Pregunta | Respuesta | Consecuencia |
|---|---|---|
| ¿Hay software propio, también firmware? (software) | sí: una app de Kotlin Multiplatform para Android e iOS, medido en el repositorio (`androidApp/`, `iosApp/`, `shared/`) · propuesta por el agente | G4 y G5 comprueban sus criterios de contenedores |
| ¿Qué pantalla, mensajes, órdenes o aparato propios usa quien lo usa? (interfaz) | sí: las pantallas de la app (la cámara con el efecto, la galería de efectos y la foto antes de guardarla) · propuesta por el agente | UX y AC se abren, críticas en G3, y AC también en G5 |
| ¿Hay componente físico? (HF) | no: usa la cámara del propio teléfono · propuesta por el agente | HF `n/a` cuando se confirme |
| ¿El producto usa modelos de IA, o sus usuarios son agentes? (IA) | sí: modelos de detección de rasgos faciales que corren en el teléfono (ML Kit en Android, Vision en iOS); no genera imágenes · propuesta por el agente | IA se abre en P1 y es crítica en G3 |
| ¿Cómo le llega a la persona lo que hacéis? (perfil) | `software` · **quién paga:** (hueco) · **a quién se atiende:** (hueco); menores: sin medir · propuesto por el agente | lo que abre `software` en `perfiles` de `scoring/dimensiones.yaml` |
| ¿Qué se ha decidido sobre quién se queda con el dinero que sobra? (sin ánimo de lucro) | (hueco) | — |
| ¿Quién puede salir perjudicado sin ser su usuario? (EI-E1) | (hueco: en cola) | — |
| ¿Se tocan datos de salud, menores o finanzas? (DP) | (hueco: en cola); medido: la app trata imágenes de caras, que se procesan en el teléfono (R-02) | — |
| ¿Desde qué país se vende, se factura o se recibe el dinero de fuera? (LC) | (hueco: en cola) | — |

## 11. Siguiente paso

- **Próxima puerta:** G0 · (hueco: fecha) · **Quién la revisa:** (hueco)
- **Después de G0:** `/harness-interview problema-y-usuarios`
