# BakiFace · para agentes

Lo que no se deduce del código. Si algo de aquí discrepa de su fuente, manda la fuente.

- **Lo que no se negocia**: la app deforma los rasgos de la persona hasta parecerse al personaje; nunca sustituye la
  cara ni le pone una máscara. Son palabras del dueño, Fangusk ([ADR 0005](docs/adr/0005-deformar-los-rasgos.md)).
- **Las decisiones**: [`docs/adr/`](docs/adr/README.md). Las «aceptadas» las dictó el dueño; las «propuestas» las tomó un
  agente y solo el dueño las acepta.
- **Nada sale del teléfono**: sin permiso de internet, sin analítica y sin SDK que llame a la red mientras el dueño no
  lo decida ([ADR 0007](docs/adr/0007-nada-sale-del-telefono.md)).
- **Hecho quiere decir** que `bash scripts/verificar.sh` termina en «La puerta pasa» (en Linux, con
  `BAKIFACE_SKIP_IOS=1`): sin `!!`, sin avisos, ktlint, pruebas, cobertura mínima, lint y las dos apps
  ([ADR 0006](docs/adr/0006-la-puerta-de-calidad.md)).
- **Para ver un efecto sin una cara delante**: el arranque de demostración de las compilaciones de depuración
  ([ADR 0010](docs/adr/0010-demostracion-y-simulador.md)). El efecto en vivo solo se puede juzgar en un teléfono real.

<!-- mayeutic-harness:begin 1.4.0 -->
## Mayeutic Harness

Este proyecto usa el Mayeutic Harness 1.4.0, en ../mayeutic-harness; dónde está y qué sigue, en docs/harness/STATUS.md.
- Antes de tocar docs/harness/, lee docs/paquete-de-contexto.md del arnés y lo que cita; sus skills/ dicen qué hacer en cada paso, aunque no seas Claude Code.
- Decide Fangusk: lo que decide una persona no lo decides tú (docs/07-agentes.md del arnés, §1).
<!-- mayeutic-harness:end -->
