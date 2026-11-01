# ADR-0011 · Lo que tiene que hacer la app, en palabras del dueño

| | |
|---|---|
| **Estado** | aceptado en el qué: lo dictó Fangusk el 2026-11-01 (abajo, literal); propuesto en el cómo |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** HI, AT |
| **Propone · decide** | Fangusk (el qué) y agente (el cómo) · Fangusk |
| **Reversibilidad** | alta · los personajes son datos del catálogo y las marcas nuevas, código del dominio |
| **Espera a** | R-03 y R-05: verlo en sus teléfonos, con su cara y con caras distintas |

## Contexto

En la ronda 2 del arnés, Fangusk dijo «No es relevante, expongo el caso de lo que tiene que hacer la app», y lo expuso:

- «Cuando se inicie la app tiene verse el contenido de la camara secundaria, es decir la que se utiliza para hacer
  selfies tal y como sale en instagram», con una captura de la cámara de historias de Instagram.
- «en la ruleta que tenemos abajo, deberian de aparecer un elemento por cada uno de los siguientes personajes de Baki:
  Yujiro, Baki, Jack, Oliva, Hanayama».
- «la idea es que cuando se seleccione un personaje, se aplique un filtro en la cara del usuario para que tenga rasgos
  faciles similares a los del personaje».
- «te paso algun ejemplo», con tres imágenes de la tendencia «Ya me vi Baki»: caras con todos los músculos marcados en
  pliegues, sonrisas enormes, ojos en blanco, cejas gruesas y cuellos con tendones.
- «y con el boton del medio el usuario pueda hacerse el selfie con ese filtro aplicado».

## Decisión

1. **La cámara de selfies al arrancar.** La app ya empezaba en la frontal. Lo nuevo: la primera vez, el sistema pide el
   permiso en cuanto se abre la app, sobre el visor en negro, sin el panel previo que explicaba para qué es. El panel
   queda para cuando se deniega: explica y deja volver a pedirlo o ir a Ajustes.
2. **Cinco personajes, en ese orden**: Yujiro, Baki, Jack, Oliva y Hanayama. Doppo, Retsu, Kaku y Pickle salen del
   catálogo y de los textos; siguen en el historial de git por si vuelven. Una elección guardada de uno de ellos
   vuelve a «sin efecto».
3. **El aspecto de los ejemplos, en vivo y sobre la cara de la persona** (ADR 0005 sigue mandando: se deforman sus
   rasgos, nunca se sustituye su cara). Tres marcas nuevas en el dominio:
   - **Pliegues** (`folds`): diez regiones (frente, entrecejo, patas de gallo, ojeras, surcos de la sonrisa, mejillas,
     puente de la nariz, comisuras, barbilla y cuello). Cada pliegue se traza a partir de los rasgos y se talla en
     cuatro trazos afilados: sombra, surco, brillo ancho y filo de luz. La luz viene de arriba y de fuera de la cara.
     La dispersión de cada pliegue sale de una semilla fija, así que no tiembla de un fotograma a otro.
   - **Ojos en blanco** (`blank_eyes`): el blanco de párpado a párpado, la sombra del párpado superior y los dos
     párpados por encima.
   - **Cejas de tinta** (`ink_brows`): un trazo grueso en el lado interior que se afila hacia fuera. Se pinta a media
     fuerza y se multiplica el resto, para que el pelo se siga viendo debajo.
   Los cinco personajes se rehacen con ellas, más deformación (la sonrisa tira de las comisuras hacia arriba y hacia
   fuera, y se refuerza al abrir la boca) y un color más contrastado y tostado. Yujiro y Baki llevan los ojos en
   blanco, como los ejemplos.
4. **El botón del medio** ya hacía la foto con el filtro y abría el editor, donde se guarda o se comparte. No cambia.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Generar la imagen con IA, como la plantilla de CapCut | Se parece más a los ejemplos | No es en vivo, necesita servidor (ADR 0007) y rehace la cara entera, lo que el dueño no quiere (ADR 0005) |
| Pliegues escritos a mano en el catálogo, uno a uno | Control total del dibujo | Docenas de trazos por personaje e iguales en todas las caras; las regiones los colocan a partir de los rasgos de cada una |
| Pliegues pintados con desenfoque | Relieve más suave | El desenfoque no es común entre Android e iOS en Compose y cuesta por fotograma; las capas afiladas dan relieve sin él |

## Lo que se aprendió por el camino

Ningún pliegue se veía en el teléfono, aunque las pruebas los encontraban. En el extremo de cada trazo, `sin(π)`
redondea por debajo de cero, y un negativo elevado a 0,7 es NaN. Con una sola esquina NaN, Skia descarta la forma entera
sin avisar. La prueba `noCornerOfAFoldIsLostToARoundingError` lo vigila.

## Qué nos haría cambiar de opinión

Lo que diga Fangusk al verlo en su teléfono. También un coste de dibujo que no quepa en el fotograma: Yujiro son unos
250 trazos por fotograma, y el contador de fotogramas (ADR 0010) mide la cámara, no el dibujo. Si va lento, se quitan
capas o pliegues.
