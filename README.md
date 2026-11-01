# BakiFace

Filtros faciales inspirados en los personajes de *Baki Hanma*, para Android e iPhone. No pone la cara del personaje
encima de la tuya: mueve tus propios rasgos (cejas, ojos, nariz, mandíbula, boca) y les añade sus marcas (cicatrices,
venas, arrugas, pelo) hasta que te pareces a él. En vivo con la cámara, o sobre una foto.

> **Estado:** borrador anticipado. El proyecto está en la fase P0 del Mayeutic Harness (ver
> [Desarrollo](#desarrollo-con-el-mayeutic-harness)); el efecto en vivo todavía no se ha probado en un teléfono con una cara
> real delante.

## Lo que hace

- **Cámara a pantalla completa** con los personajes en un carrusel alrededor del obturador, como los filtros de
  Instagram. El efecto sigue a la cara en vivo; con la cámara frontal o la trasera.
- **Nueve personajes**, cada uno con su forma de cambiar la cara:

  | Personaje | Qué le pasa a tu cara |
  |---|---|
  | Baki Hanma | Mirada decidida, mandíbula firme y las marcas de mil combates |
  | Yujiro Hanma | Cejas de furia, sonrisa de demonio y venas a punto de estallar; abre la boca y verás |
  | Jack Hanma | Una mandíbula descomunal y cicatrices cosidas; abre la boca para morder |
  | Kaoru Hanayama | Cara ancha, mirada tranquila y cicatrices de cien peleas sin guantes |
  | Biscuit Oliva | Cuello de toro, cara ancha, bigote espeso y una sonrisa enorme |
  | Doppo Orochi | Nariz ancha, mandíbula cuadrada y un ojo cerrado por una cicatriz |
  | Retsu Kaioh | Rasgos afilados, pómulos altos y una mirada de halcón |
  | Kaku Kaioh | Más de un siglo en tu cara: arrugas, cejas largas y un bigote blanco |
  | Pickle | Frente baja, cejas de piedra, nariz ancha y una mandíbula jurásica |

- **Editor de fotos**: la foto que haces o una de tu galería, con cualquier personaje y su intensidad de 0 a 100 %.
  Se guarda en la galería o se comparte.
- **En español e inglés**, con modo claro y oscuro.
- **Sin conexión**: la cara se detecta y se deforma en el teléfono y nada sale de él
  ([ADR 0007](docs/adr/0007-nada-sale-del-telefono.md)).

## Cómo funciona

Cada fotograma de la cámara pasa por la misma cadena: el detector del sistema encuentra los puntos de la cara (ML Kit en
Android, Vision en iOS), un filtro los suaviza, se calcula un marco de la cara cuya unidad es la distancia entre los
ojos, y el personaje (un conjunto de deformaciones, marcas y un color, descrito en un catálogo JSON) dobla una malla
tendida sobre la imagen. Los detalles y las alternativas descartadas, en [ADR 0005](docs/adr/0005-deformar-los-rasgos.md).

## Estructura

| Módulo | Qué tiene |
|---|---|
| `core:domain` | Kotlin puro: modelos, geometría de la cara, el motor de deformación, interfaces de repositorio y casos de uso |
| `core:render` | El dibujo de una imagen con su efecto, con los gráficos de Compose |
| `core:data` | Repositorios: el catálogo, las preferencias, las fotos, y la cámara y el detector de cada plataforma |
| `core:testing` | Los dobles (fakes) de las pruebas |
| `shared` | Las pantallas (MVI), el sistema de diseño, la navegación y la inyección con Koin; el framework de iOS |
| `androidApp`, `iosApp` | Lo mínimo para arrancar en cada plataforma |

La regla de dependencias, MVI y el manejo de errores: [ADR 0003](docs/adr/0003-capas-mvi-y-regla-de-dependencias.md).
Todas las decisiones: [`docs/adr/`](docs/adr/README.md).

## Compilar y ejecutar

Hace falta Android Studio (trae el JDK 21 que usa Gradle) con el SDK de Android 37, y para iOS un Mac con Xcode (probado
con Xcode 27.2). La app pide Android 10 o iOS 18.5 como mínimo.

Android, en un teléfono conectado o un emulador:

```bash
./gradlew :androidApp:installDebug
```

Si el terminal no encuentra Java, usa el de Android Studio (la puerta de calidad lo hace sola):

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
```

iOS: abre `iosApp/iosApp.xcodeproj` en Xcode y ejecuta el esquema `iosApp`. La primera compilación del framework de
Kotlin tarda unos minutos.

El efecto en vivo solo se ve de verdad en un teléfono: el emulador de Android da una escena sintética y el simulador de
iOS no tiene cámara. Las compilaciones de depuración muestran junto al logotipo cuántos fotogramas por segundo llegan a
la pantalla.

### Ver un efecto sin cámara (solo depuración)

Abre el editor con una foto y un personaje ([ADR 0010](docs/adr/0010-demostracion-y-simulador.md)). En Android, la foto
tiene que estar dentro de los archivos de la app:

```bash
adb push foto.jpg /data/local/tmp/foto.jpg
```

```bash
adb shell "cat /data/local/tmp/foto.jpg | run-as com.fanguskdev.bakiface sh -c 'mkdir -p files/demo && cat > files/demo/foto.jpg'"
```

```bash
adb shell am start -n com.fanguskdev.bakiface/.MainActivity --es bakiface.demo.photo file:///data/user/0/com.fanguskdev.bakiface/files/demo/foto.jpg --es bakiface.demo.effect yujiro
```

En el simulador de iOS, con la app ya instalada:

```bash
xcrun simctl launch booted com.fanguskdev.bakiface.BakiFace -demoPhoto /ruta/absoluta/foto.jpg -demoEffect yujiro
```

Los personajes se llaman `baki`, `yujiro`, `jack`, `hanayama`, `oliva`, `doppo`, `retsu`, `kaku` y `pickle`. En el
simulador, Vision no encuentra bien los puntos de la cara y la app usa una cara media; en un iPhone, nunca.

## La puerta de calidad

```bash
bash scripts/verificar.sh
```

Comprueba que no haya `!!`, el estilo con ktlint, las pruebas de todas las capas (en la JVM y, en el Mac, en el simulador
de iOS), la cobertura mínima con Kover, el lint de Android, los APK de depuración y release (este con R8) y la app de
iOS. Termina en «La puerta pasa» o en la lista de lo que ha fallado. La CI `mayeutic` de GitHub Actions corre lo mismo
salvo iOS ([ADR 0006](docs/adr/0006-la-puerta-de-calidad.md)).

## Desarrollo con el Mayeutic Harness

El proyecto sigue el Mayeutic Harness 1.4.0 (un repositorio hermano, en `../mayeutic-harness`): está en P0, la chispa,
y la siguiente puerta es G0.
Dónde está y qué espera respuesta, en [`docs/harness/STATUS.md`](docs/harness/STATUS.md). El código existe antes de
tiempo porque el dueño pidió construir ya: es un borrador anticipado que revisan las puertas que lo piden, y no decide
la talla, los criterios de muerte ni ninguna salida. Para agentes: [`AGENTS.md`](AGENTS.md).

## Aviso legal

*Baki* y sus personajes son obra de Keisuke Itagaki, editada por Akita Shoten. BakiFace es un proyecto de fan, sin
relación con sus autores ni editores, y no lleva ninguna imagen del manga ni del anime: cada marca se dibuja con código.
Publicarla en Google Play o en el App Store con sus nombres y rasgos sin licencia es el riesgo R-01, abierto, en
[`docs/harness/riesgos.md`](docs/harness/riesgos.md). Las licencias de terceros, en [`docs/licencias.md`](docs/licencias.md).
