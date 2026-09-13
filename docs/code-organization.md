# Vertical slices y Clean Architecture

Tickly se organiza primero por funcionalidad: `features/<feature>/{presentation,domain,data}`. Cada feature reúne su presentación, reglas y adaptadores; las capas se distribuyen entre los módulos y source sets existentes según la plataforma. No se duplican modelos de negocio en Swift ni se crean capas vacías para repetir el árbol en cada módulo.

Antes de agregar o mover código, identifica la feature propietaria y aplica estas responsabilidades:

| Capa | Contenido | Dependencias permitidas |
| --- | --- | --- |
| `domain` | Modelos, reglas de negocio y contratos que necesita la lógica | Kotlin y otros contratos de dominio explícitos; nunca UI, almacenamiento o APIs nativas |
| `data` | Implementaciones de contratos, persistencia y adaptadores de plataforma (avisos, sonido) | Dominio y APIs de la plataforma; nunca pantallas o composición de la aplicación |
| `presentation` | Pantallas, estado y coordinación de acciones de usuario | Dominio y contratos inyectados; no construir repositorios ni acceder directamente al almacenamiento |

`app` es la raíz de composición: crea implementaciones concretas, inyecta dependencias y conecta navegación/ciclo de vida. No aloja reglas del temporizador. `core` contiene únicamente código realmente compartido entre features, como diseño y localización.

## Distribución física

Las rutas Kotlin parten de `src/<sourceSet>/kotlin/com/argote/tickly/`. Los paquetes reflejan sus carpetas y los tests reflejan el paquete de la capa que verifican.

| Feature / capa | Kotlin | iOS nativo |
| --- | --- | --- |
| Timer / domain | `sharedLogic/.../features/timer/domain` | Reglas desde `SharedLogic`; contratos de adaptadores nativos en `Features/Timer/Domain` |
| Timer / data | Adaptadores Android en `features/timer/data` del módulo que dispone de sus APIs | `Features/Timer/Data` |
| Timer / presentation | `sharedUI/.../features/timer/presentation` | `Features/Timer/Presentation` |
| Settings / domain | `sharedLogic/.../features/settings/domain` | Reglas desde `SharedLogic`; contrato de sonido en `Features/Settings/Domain` |
| Settings / data | Adaptadores Android en `features/settings/data` | `Features/Settings/Data` |
| Settings / presentation | `sharedUI/.../features/settings/presentation` | `Features/Settings/Presentation` |

Por ejemplo, `domain` de Timer vive una sola vez en `sharedLogic`, mientras sus implementaciones de datos viven en los targets nativos. La feature completa tiene tres capas aunque una carpeta de plataforma no vuelva a declarar el dominio compartido. Los protocolos Swift describen los contratos de los adaptadores nativos; no reimplementan `TimerEngine` ni `TimerSettings`.

Se conservan `sharedLogic`, `sharedUI`, `androidApp` y el target SwiftUI. `sharedUI` mantiene su nombre, aunque hoy solo tiene target Android; iOS sigue usando UI nativa según el [ADR 0001](adr/0001-platform-native-ui.md).

## Reglas de diseño

- Las dependencias apuntan hacia contratos y reglas, no hacia implementaciones concretas. Los adaptadores se eligen en `app` y se inyectan en presentación.
- El motor recibe el tiempo como argumento: el dominio no consulta relojes del sistema ni conoce Compose, SwiftUI, permisos o almacenamiento nativo.
- Timer consume el contrato validado `TimerSettings` de Settings. Una pantalla de Settings recibe sus valores y callbacks; no depende del estado de presentación de Timer.
- Ajustes y temporizador se guardan juntos en el snapshot existente. No se crea un segundo repositorio de ajustes que pueda desincronizarlos. La previsualización nativa de sonidos sí pertenece a los adaptadores de Settings.
- Cada clase o tipo principal de nivel superior (interfaz, enum, object, struct o protocolo) vive en un archivo con su mismo nombre. Los tipos anidados que son detalles internos permanecen con su propietario; las funciones auxiliares no necesitan archivos individuales. Las clases se separan por responsabilidad. Un caso de uso nuevo debe encapsular comportamiento real, no limitarse a reenviar cada método del motor.
- Cada función `@Composable` con nombre vive en su propio archivo Kotlin y cada vista SwiftUI en su propio archivo Swift, ambos con el nombre del componente. Los fragmentos de UI extraídos reciben valores y callbacks sin cambiar el propietario del estado o de los efectos. Los helpers de UI que deban cruzar archivos mantienen visibilidad de módulo (`internal` en Kotlin; interna en Swift), no pública.
- Los detalles no visuales de una pantalla permanecen privados cuando solo tienen un consumidor. No se introducen interfaces públicas, contenedores DI ni routers solo para mover archivos.
- `core/design` y `core/localization` no dependen de features concretas. Una feature no importa `app`; presentación no importa `data`.

Estas capas son convenciones de dependencias dentro de los módulos existentes, no nuevos límites binarios. Un módulo independiente solo se justifica por una necesidad concreta de aislamiento.

## Compatibilidad y validación

La reorganización conserva claves de preferencias, snapshot v1, recursos de sonido, identificadores de avisos, coordinación del ciclo de vida y correcciones visuales/de área táctil. Xcode descubre los archivos Swift mediante su carpeta sincronizada.

`androidApp` tiene una sola activity: `com.argote.tickly.app.MainActivity`, usada tanto por el launcher como por las notificaciones. El adaptador Android de notificaciones puede referenciar esta entrada de plataforma; esta excepción no permite que `data` dependa de lógica de composición o presentación. Se eliminó la entrada antigua `com.argote.tickly.MainActivity` por decisión explícita: los avisos ya emitidos que apunten a ella pierden compatibilidad.

Los receivers de avisos conservan adaptadores en sus paquetes originales porque los `PendingIntent` de alarmas persistidos contienen el nombre del componente. Estas entradas estables delegan a la feature y solo una recibe los broadcasts de arranque/actualización.

Al cambiar un contrato, actualiza sus adaptadores y consumidores en ambas plataformas. Verifica pruebas de dominio, persistencia y coordinación, tests Android de notificaciones y compilaciones Android/iOS. No basta con que las carpetas tengan el nombre correcto: debe conservarse la dirección de las dependencias y el comportamiento.
