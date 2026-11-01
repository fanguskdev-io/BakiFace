# ADR-0010 · Las herramientas de depuración y Vision en el simulador

| | |
|---|---|
| **Estado** | propuesto |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT, RP |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | alta · código solo de depuración y una rama solo del simulador |

## Contexto

El efecto necesita una cara delante de la cámara, y ni el agente, ni el emulador, ni el simulador de iOS la tienen.
Además, en el simulador de iOS 26.5, Vision falla: sin forzar la CPU no arranca («Could not create inference context»,
error 9), y forzándola devuelve puntos imposibles (sobre la misma foto, los ojos a 14 px cuando Vision en el Mac los
da a unos 100 px).

## Decisión

- **Arranque de demostración, solo en depuración**: abre el editor con una foto y un personaje, sin pasar por la
  cámara. En Android con `adb shell am start -n com.fanguskdev.bakiface/.MainActivity --es bakiface.demo.photo <uri>
  --es bakiface.demo.effect <id>` (la foto, dentro de los archivos de la app); en iOS con los argumentos de arranque
  `-demoPhoto <ruta> -demoEffect <id>`. Las versiones de release lo ignoran (`BuildConfig.DEBUG`,
  `Platform.isDebugBinary`).
- **Contador de fotogramas, solo en depuración**: la cámara muestra junto al logotipo cuántos fotogramas por segundo
  pasan por la detección y el efecto (`FrameRateMeter`, sobre el reloj de la cámara y el último segundo). Es el número
  que pide R-03 en un teléfono real; en el emulador, con su escena sintética, marca unos 23 y no dice nada del efecto.
  Cuenta los fotogramas que llegan al estado de la pantalla: si dibujarlos no diera abasto, Compose se saltaría alguno
  y la pantalla iría por debajo de ese número, así que el temblor también se mira a ojo.
- **Vision comprueba sus puntos en todas partes**: si los ojos caen fuera de la caja de la cara o a menos del 20 % de su
  ancho, no hay cara (y no hay efecto) en lugar de un efecto deformado sobre puntos falsos.
- **Solo en el simulador**: se fuerza la CPU y, si los puntos no pasan la comprobación, se usa una cara media ajustada a
  la caja que sí detecta, para poder probar el flujo de la app. En un iPhone nunca se usa esa cara media.

## Consecuencia

En iOS solo se ha visto el efecto con la cara media del simulador; cómo detecta Vision en un iPhone real queda en R-03.
