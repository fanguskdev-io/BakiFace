# ADR-0005 · Deformar los rasgos con una malla, no sustituir la cara

| | |
|---|---|
| **Estado** | aceptado en el qué: «no quiero que la cara del usuario se sustituya por la del personaje seleccionado, quiero que los rasgos faciales del usuario se modifiquen para parecerse al personaje» (Fangusk, 2026-11-01); propuesto en el cómo |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** AT, HI |
| **Propone · decide** | Fangusk (el qué) y agente (el cómo) · Fangusk |
| **Reversibilidad** | media · el motor es Kotlin común y no sabe qué detector ni qué GPU hay debajo |
| **Espera a** | R-03: medir en teléfonos reales (fotogramas por segundo, temblor, caras que no detecta) |

## Contexto

| Condiciona | De dónde sale | Evidencia |
|---|---|---|
| La cara sigue siendo la de la persona: solo cambian sus rasgos | La petición del dueño | reportado |
| En vivo, como los filtros de Instagram: a la vez que la cámara | La petición del dueño | reportado |
| Lo mismo en Android e iOS | ADR 0001 | observado |
| Sin servidor | ADR 0007 | propuesto |
| Ni el agente ni el emulador tienen una cara delante | El emulador da una escena sintética; el simulador de iOS no tiene cámara | observado |

## Decisión

Cada fotograma pasa por la misma cadena, de la cámara a la pantalla:

1. **Los puntos de la cara.** ML Kit Face Detection con el modelo incluido en la app (contornos completos; modo rápido
   en vivo, preciso con una foto importada) en Android; `VNDetectFaceLandmarksRequest` de Vision en iOS. Las dos salidas
   se convierten a `FaceLandmarks`: nueve regiones (óvalo, cejas, ojos, nariz, labios) en píxeles de la imagen.
2. **El suavizado.** Un filtro *One Euro* por punto (1 Hz en reposo, β = 1,5, normalizado por el tamaño de la cara)
   quita el temblor sin retrasar los movimientos rápidos, y la presencia de la cara entra en 180 ms y sale en 260 ms,
   para que el efecto no aparezca ni desaparezca de golpe.
3. **El marco de la cara.** `FaceGeometry` pone el origen entre los ojos, el eje x a lo largo de ellos y la unidad en
   la distancia entre ojos. Cada personaje se escribe una vez en esas unidades y vale para cualquier tamaño, giro o
   posición de la cara.
4. **El personaje es datos.** El catálogo JSON (`EffectCatalogJson.kt`, con DTO y mapper) da a cada personaje sus
   deformaciones (empujar o escalar una zona anclada a un rasgo, con radio, desplazamiento, lado y refuerzo con la boca
   abierta), sus marcas en la piel (cicatrices, venas, arrugas, sombras, pelo) y su gradación de color (matriz 4 × 5).
5. **La deformación.** `WarpField` suma cambios locales con peso `(1 − t²)²`, nulo y suave en el borde de cada uno, así
   que la piel se dobla sin rasgarse. `MeshPlanner` tiende sobre la imagen una malla de celdas cuadradas, unas nueve por
   distancia entre ojos (entre 24 y 110 a lo largo del lado largo: unas 4 000 esquinas en 720p), y mueve sus esquinas.
   Se dibuja con `Canvas.drawBitmapMesh` en Android y con triángulos `drawVertices` de Skia en iOS.
6. **Las marcas.** Se componen en el marco de la cara, se deforman con el mismo campo para seguir a los rasgos movidos y
   se funden con multiplicar, trama o normal, para que parezcan piel y no pegatinas. Desde ADR 0011 hay también
   pliegues por regiones, ojos en blanco y cejas de tinta, dibujados como formas afiladas y rellenas.
7. **La app dibuja los fotogramas.** CameraX entrega RGBA de 720p ya girado y AVFoundation BGRA de 1280 × 720; la app
   los pinta con el efecto, sin `PreviewView` ni capa de vista previa. Lo que se ve es lo que se captura: la foto es el
   fotograma visible, recortado como en pantalla.

La intensidad del editor escala toda la cadena entre 0 y 100 %.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Sustituir la cara o ponerle una máscara del personaje | Parecido inmediato | El dueño la descartó con esas palabras |
| IA generativa («conviértete en anime», como la plantilla de CapCut) | Resultados vistosos | No es en vivo, necesita servidor (ADR 0007) y rehace la cara entera, lo que el dueño no quiere |
| Malla 3D (ARCore Augmented Faces, ARKit, MediaPipe Face Landmarker con 468 puntos) | Más puntos y profundidad | ARKit pide la cámara TrueDepth y ARCore una lista de teléfonos; MediaPipe añade varios MB y otro SDK por plataforma. Los contornos de ML Kit y Vision bastan para deformaciones locales; es el siguiente paso si R-03 dice que no |
| Sombreador en GPU (AGSL, Metal) | Más rápido por píxel | AGSL solo existe desde Android 13 y no es común con iOS; la malla funciona igual en las dos. Se revisa si R-03 mide que la malla no llega |

## Reversibilidad y coste

Cambiar de detector es escribir otra fuente de `FaceLandmarks`; cambiar el dibujo a GPU es reescribir `drawImageMesh` en
`core:render`. El catálogo y el motor (`WarpField`, `MeshPlanner`, `MarkComposer`) no cambian en ninguno de los dos casos.

## Qué nos haría cambiar de opinión

R-03 en teléfonos reales: menos de 24 fotogramas por segundo en un Android de gama media, un temblor visible con la
cara quieta o caras que el detector pierde (piel oscura, gafas, barba, perfil). Hasta entonces el efecto en vivo solo se
ha visto con la escena sintética del emulador, y el resultado sobre una cara real, con fotos fijas.
