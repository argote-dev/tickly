# Organización por slices

Tickly se organiza primero por funcionalidad, dentro de los módulos existentes. Una slice agrupa el código que cambia por la misma razón; no es un módulo Gradle nuevo ni una obligación de crear capas vacías.

## Mapa

Las rutas Kotlin parten de `src/<sourceSet>/kotlin/com/argote/tickly/`. Los source sets continúan separando código compartido, adaptadores de plataforma y pruebas.

| Grupo | Responsabilidad | Ubicación |
| --- | --- | --- |
| `app` | Arranque, composición de pantallas y conexión con el ciclo de vida | `androidApp`, `sharedUI`; `App` en iOS |
| `features/timer/domain` | Máquina de estados y reglas de enfoque/descanso | `sharedLogic` |
| `features/settings/domain` | Preferencias y validación de valores | `sharedLogic` |
| `features/timer/presentation` | Pantalla y estado de presentación del temporizador | `sharedUI`; `Features/Timer/Presentation` en iOS |
| `features/settings/presentation` | Edición de preferencias y controles propios de ajustes | `sharedUI`; `Features/Settings/Presentation` en iOS |
| `features/timer/notifications` | Adaptadores de avisos del temporizador | `androidApp`; `Features/Timer/Notifications` en iOS |
| `features/settings/platform` | Previsualización nativa de sonidos | `sharedUI/androidMain`; `Features/Settings/Platform` en iOS |
| `core/design` | Tema, acentos y elementos visuales compartidos entre pantallas | `sharedUI`; `Core/Design` en iOS |
| `core/localization` | Catálogo de textos ES/EN usado por varias pantallas | `sharedUI`; `Core/Localization` en iOS |
| `core/platform` | Código de plataforma y ejemplos conservados del starter KMP | `sharedLogic` |

## Reglas de dependencia

- `app` compone las slices; una slice no importa `app`.
- El dominio KMP no conoce Compose, SwiftUI, permisos, relojes del sistema ni almacenamiento nativo. El tiempo continúa entrando al motor como un argumento.
- El temporizador consume el contrato validado `TimerSettings` de la slice de ajustes. Esta dependencia explícita no permite que una pantalla importe detalles privados de otra.
- La navegación entre temporizador y ajustes se conecta desde la composición existente mediante estado/callbacks. No se añade un router ni un contenedor de inyección solo para mover archivos.
- `core/design` y `core/localization` alojan elementos realmente compartidos; no son cajones de utilidades ni dependen de pantallas concretas.
- Los detalles de una pantalla permanecen privados cuando solo tienen un consumidor. Extraer un archivo no exige crear una interfaz pública.
- Las pruebas Kotlin reflejan el paquete de la slice que verifican en su source set de pruebas.

## Alcance y estabilidad

Se conservan `sharedLogic`, `sharedUI`, `androidApp` y el target SwiftUI. `sharedUI` mantiene su nombre, aunque hoy solo tiene target Android; iOS sigue usando UI nativa según el [ADR 0001](adr/0001-platform-native-ui.md).

La organización por carpetas no introduce nuevos límites binarios: las reglas de importación son convenciones del repositorio, no aislamiento forzado por el compilador. Si una funcionalidad requiere aislamiento más adelante, se evaluará un módulo independiente con una necesidad concreta.

Los propietarios actuales del ciclo de vida, persistencia y programación de avisos se conservan. No se cambian claves de preferencias, formato de snapshots, recursos de sonido, identificadores de avisos ni las correcciones de tinte y área táctil. Los componentes Android movidos requieren mantener sus nombres en el manifest coherentes; Xcode descubre los archivos Swift mediante su carpeta sincronizada.

### Entradas Android compatibles

`androidApp` conserva pequeños adaptadores en los paquetes originales para `MainActivity` y los receivers de avisos. Son una excepción intencional al mapa, no una segunda implementación: los `PendingIntent` persistidos incluyen el nombre del componente. Tanto los avisos existentes como los nuevos usan estas entradas estables, que delegan al código de la slice. Solo una entrada recibe los broadcasts de arranque/actualización para evitar ejecución duplicada.
