# ADR-0008 · El sistema de diseño «Tinta y sudor»

| | |
|---|---|
| **Estado** | propuesto · el dueño pidió un sistema de diseño propio sobre Material 3, con modo oscuro, edge-to-edge y accesibilidad |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** HI |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | alta · los tokens están en `designsystem/Theme.kt` |

## Contexto

La app vive sobre la cámara a pantalla completa: lo que flota encima tiene que leerse sobre cualquier fondo. Su mundo es
el manga de lucha de Itagaki, impreso en tinta negra sobre papel de revista semanal.

## Decisión

- **Color.** Tinta `#000000`, hueso `#FFFFFF` y papel de periódico `#E4E2DC` de fondo en claro; sangre `#A3121A` (claro)
  y `#EF4A50` (oscuro), reservada para la acción principal de cada pantalla. Gris `#5E5B56` y `#B5B2AC` para lo
  secundario; `#1E1E1E` para lo que se eleva en oscuro. Contraste comprobado: sangre sobre papel 6,1:1, gris sobre papel
  5,2:1, sangre oscura sobre tinta 5,8:1 y sobre `#1E1E1E` 4,6:1 (todos AA para texto normal). Cada personaje tiñe el
  papel de su panel con su color (`paperTint`).
- **Tipografía.** Dela Gothic One (OFL) en títulos, el nombre del personaje y los sellos en kanji, en un subconjunto de
  38 KB (latín, español y los kanji de los sellos: 刃 鬼 牙 侠 鎖 虎 烈 皇 原 素). El resto, en la letra del sistema, que es
  la que mejor se lee pequeña.
- **Forma.** Paneles casi cuadrados, como las viñetas (2–4 dp); solo la hoja que sube del borde es redonda. Trazos de
  tinta de 1, 2 y 3 dp; espacios en una rejilla de 4 dp.
- **Motivos.** Trama de puntos, líneas de concentración y un sello en kanji por personaje. Al cambiar de personaje su
  nombre entra con un golpe (la única animación que no pide nadie); se queda quieto si el sistema pide reducir el
  movimiento.
- **Material 3 debajo.** `BakiFaceTheme` traduce los tokens al `ColorScheme`, la tipografía y las formas de Material 3,
  así que la hoja, el deslizador o el aviso heredan el estilo; los tokens propios se leen con `BakiTheme.colors`,
  `.spacing` y `.strokes`.
- **Oscuro y edge-to-edge.** El tema sigue al sistema; la cámara llega a los bordes en las dos plataformas y los
  controles respetan los márgenes del sistema.
- **Accesibilidad.** Todo lo que se toca mide al menos 48 dp; cada icono lleva su descripción; el carrusel anuncia el
  personaje y si está elegido, y vibra suave al pasar de uno a otro.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Material 3 con color dinámico | Cero trabajo de diseño | El tono lo pondría el fondo de pantalla del teléfono, no Baki |
| La estética de Instagram (degradados, iconos finos) | Familiar | Es la de otra app; el dueño quiere filtros de Baki, no una copia de Instagram |
