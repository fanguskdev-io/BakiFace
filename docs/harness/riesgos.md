# Registro de riesgos · BakiFace

**Última revisión:** 2026-11-01, en la adopción y con el borrador anticipado · **Abiertos:** 5 · **Vencidos sin acción:** 0

## Escalas

| Probabilidad | Significa | Impacto | Significa |
|---|---|---|---|
| **alta** | más probable que no en seis meses | **alto** | mata o pivota el proyecto; daño a personas; sanción |
| **media** | podría pasar | **medio** | retrasa una fase o cuesta dinero relevante |
| **baja** | sería raro | **bajo** | molesta, se absorbe |

## Riesgos vivos

| Id | Riesgo (qué pasaría y por qué) | Dimensión | Prob. | Impacto | Mitigación (qué hacemos para que no pase o duela menos) | Dueño | Fecha | Estado | Origen |
|---|---|---|---|---|---|---|---|---|---|
| R-01 | Si se publica en Google Play o en el App Store con los nombres o con rasgos reconocibles de los personajes de Baki sin licencia del titular (la obra es de Keisuke Itagaki, editada por Akita Shoten), entonces la tienda la rechaza o la retira. Apple, norma 5.2.1: «Don't use protected third-party material such as trademarks, copyrighted works, or patented ideas in your app without permission». Google Play, política de propiedad intelectual: pone como ejemplo el fan art indistinguible del original y revisa con más atención las apps que usan personajes populares ([Play Console Help 9888072](https://support.google.com/googleplay/android-developer/answer/9888072); lectura de la IA, verificada el 2026-11-01) | LC | alta, si se publica en tiendas | alto | Propuesta: elegir antes de G2 el canal (uso privado, con instalación directa en Android y TestFlight en iPhone; pedir la licencia; o publicar con personajes propios inspirados en arquetipos, sin nombres ni marcas) | Fangusk (propuesto) | — | abierto | medida del agente |
| R-02 | Si la app trata imágenes de caras, entonces maneja datos personales de quien sale en ellas, también de terceros fotografiados. Las tiendas piden declararlo (la sección de seguridad de los datos de Google Play y las etiquetas de privacidad de Apple). Si la geometría facial se usara para identificar a alguien, serían datos biométricos del RGPD (art. 4.14 y art. 9; lectura de la IA, por verificar en EUR-Lex) | DP | media | medio | Propuesta: detectar y deformar solo en el teléfono, sin red ni analítica, fijado en un ADR, y declararlo así en las fichas si se publica | Fangusk (propuesto) | — | abierto | medida del agente |
| R-03 | Si la deformación en directo no llega a 24 fotogramas por segundo o tiembla en un móvil de gama media, entonces el efecto no convence y cae H4. El agente no puede medirlo con una cara de verdad: el emulador de Android y el simulador de iOS no tienen una cámara con una cara delante | AT | media | medio | Propuesta: que Fangusk pruebe el borrador en un Android y un iPhone suyos antes de G5, con el contador de fotogramas que la compilación de depuración muestra junto al logotipo (ADR 0010; en el emulador marca unos 23 con su escena sintética, que no cuenta) | Fangusk (propuesto) | — | abierto | medida del agente |
| R-04 | Si la plantilla «Ya me vi Baki» de CapCut, gratis dentro de CapCut y de moda en TikTok en 2026, ya le da al fan lo que quiere (una imagen graciosa para compartir), entonces la diferencia de esta app (en directo, con varios personajes y con sus propios rasgos) no basta para que la cambie | MC | media | alto | sin mitigación: la ronda de contraste, en cola, dice la diferencia | — | — | abierto | medida del agente (búsqueda de diez minutos) |
| R-05 | Si el detector (ML Kit en Android, Vision en iOS) encuentra peor unas caras que otras (piel oscura, gafas, barba, arrugas, poca luz, cara de perfil), entonces el efecto falla o tiembla justo a una parte de quien lo usa, y la app parece hecha solo para algunos. El borrador solo se ha probado con una foto fija (la *Gioconda*) y con la cara media del simulador | AT | media | medio | Propuesta: antes de G5, probar el borrador con caras variadas (tono de piel, edad, gafas, barba, luz) y anotar en el plan de pruebas cuántas detecta y cómo queda cada personaje | Fangusk (propuesto) | — | abierto | medida del agente |

## Riesgos aceptados

| Id | Riesgo | Por qué se acepta | Quién acepta | Fecha | Se revisa el | Qué lo reabriría |
|---|---|---|---|---|---|---|

## Criterios de muerte enlazados

| Riesgo | Criterio de muerte | Fecha | Estado |
|---|---|---|---|
| R-01 | sin Kn: propuesto a quien decide cuando llegue la ronda de criterios de muerte | — | propuesto a quien decide (ronda de criterios de muerte, en cola) |

## Riesgos cerrados

| Id | Riesgo (qué pasaría y por qué) | Dimensión | Prob. | Impacto | Mitigación | Dueño | Fecha | Cómo se cerró | Origen |
|---|---|---|---|---|---|---|---|---|---|

## Reglas de este fichero

- Los ids `R-nn` no se reciclan.
- En cada puerta, toda fila de «Riesgos vivos» con fecha pasada se resuelve o se vuelve a fechar con motivo escrito; nunca se borra.
