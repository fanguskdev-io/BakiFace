# ADR-0007 · Nada sale del teléfono

| | |
|---|---|
| **Estado** | propuesto |
| **Fecha** | 2026-11-01 · **Fase:** P0 · Borrador anticipado, sin ninguna puerta confirmada · **Dimensiones:** HI, RP |
| **Propone · decide** | agente (Claude Code, Opus 5.5) · Fangusk |
| **Reversibilidad** | alta para añadir red; lo que no se puede deshacer es haber enviado una cara |
| **Espera a** | R-02 (datos de la cara) y la decisión del dueño sobre analítica e informes de fallos |

## Contexto

La app procesa caras, que en Europa son datos biométricos cuando sirven para identificar (RGPD, art. 9) y que las dos
tiendas tratan con reglas propias. Si la cara nunca sale del teléfono, casi todo ese riesgo desaparece.

## Decisión

- **Sin internet en Android**: el manifiesto quita `INTERNET` y `ACCESS_NETWORK_STATE`, también los que añaden las
  bibliotecas (`tools:node="remove"`). En iOS la app no hace ninguna llamada de red y no lleva SDK de analítica.
- **La detección, en el teléfono**: el modelo de ML Kit va dentro de la app (`com.google.mlkit:face-detection`, no la
  variante de Play Services que lo descarga) y Vision es del sistema. Las condiciones de ML Kit dicen que puede enviar
  métricas de uso a Google; sin el permiso de internet no puede.
- **La cara no se guarda**: los puntos de la cara viven lo que dura un fotograma. Las fotos capturadas viven en memoria
  (las tres últimas) hasta que la persona pulsa Guardar.
- **La galería, solo para añadir**: guardar usa MediaStore (Android, `Pictures/BakiFace`) y PhotoKit con permiso de solo
  añadir (iOS); la app no puede leer la galería. Importar usa el selector de fotos del sistema, sin permisos. Compartir
  pasa por un archivo en la caché (`FileProvider`) o la hoja de compartir de iOS.
- **Lo único que se recuerda**: el último personaje elegido, en DataStore. Sin copia de seguridad (`allowBackup="false"`
  y reglas de extracción que lo excluyen todo).

## Consecuencias

- No hay informes de fallos ni analítica: los fallos se verán en Android Vitals y en App Store Connect, más tarde y con
  menos detalle.
- Un personaje nuevo exige una versión nueva de la app; el catálogo no se puede actualizar a distancia.

## Alternativas consideradas

| Alternativa | A favor | Por qué se descarta |
|---|---|---|
| Firebase Crashlytics y Analytics | Ver fallos y uso al momento | Pide internet y declarar datos recogidos en las fichas de las tiendas. Es una decisión del dueño en una puerta posterior, no del agente |
| Catálogo remoto | Personajes nuevos sin publicar | Pide internet y un servidor que mantener |
| ML Kit de Play Services | La app pesa unos MB menos | Descarga el modelo la primera vez: sin red no hay efecto |
