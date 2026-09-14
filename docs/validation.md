# Validación de implementación

Esta evidencia documenta la validación realizada durante la implementación de Tickly. El modo de receipts está desactivado de forma predeterminada (`disabled/unmanaged`), por lo que no se ejecutaron revisiones de receipts.

> **Histórico:** la evidencia de este documento registra una política anterior basada en pruebas Gradle/Kotlin y verificaciones manuales. La política actual reserva las pruebas de comportamiento y regresión exclusivamente para Maestro. Aún no hay flujos Maestro versionados; por tanto, ninguna compilación, lint o prueba Kotlin de este documento acredita pruebas bajo la política actual.

## Cierre de tareas

- [x] Implementar el temporizador KMP y la persistencia local offline.
- [x] Implementar personalización, modo oscuro e idiomas español/inglés.
- [x] Implementar UI Material Android y SwiftUI Liquid Glass iOS.
- [x] Corregir la propagación de acentos en iconos y controles.
- [x] Corregir el área táctil del botón principal iOS.
- [x] Organizar el código por slices y documentar sus reglas.
- [x] Validar compilaciones y pruebas según la evidencia de este documento.

**Estado del alcance implementado: cerrado.** Los límites de pruebas en dispositivos físicos, la matriz de plataformas y los avisos del sistema se mantienen explícitos al final; no se consideran validaciones realizadas ni bloquean este cierre documental.

## Ruta y capturas

Implementación delegada por componentes mediante agentes de colaboración, con modelo heredado sin override: motor, Android, iOS y documentación. El agente principal integró los cambios, ejecutó las validaciones y rechazó las entregas parciales de avisos/localización hasta completar las correcciones descritas. No se crearon artefactos SDD.

Capturas verificadas en simuladores: [Android](screenshots/android.png) · [iOS](screenshots/ios.png).

## Compilación y pruebas Gradle

Se ejecutó desde la raíz del repositorio:

```bash
./gradlew \
  :androidApp:assembleDebug \
  :androidApp:testDebugUnitTest \
  :sharedUI:testAndroidHostTest \
  :sharedLogic:testAndroidHostTest \
  :sharedLogic:iosSimulatorArm64Test
```

Resultado: `BUILD SUCCESSFUL`. Registro: `/tmp/tickly-final-gradle.log`.

| Suite | Resultado |
| --- | --- |
| `androidApp:testDebugUnitTest` | 9 pruebas, 0 fallos |
| `sharedUI:testAndroidHostTest` | 1 prueba, 0 fallos |
| `sharedLogic:testAndroidHostTest` | 13 pruebas, 0 fallos |
| `sharedLogic:iosSimulatorArm64Test` | 13 pruebas, 0 fallos |

Las suites de lógica incluyen 11 pruebas del temporizador y 2 pruebas iniciales por plataforma.

## Compilación iOS

Se ejecutó contra un simulador iOS con Xcode:

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,id=6677DC35-96FA-4F74-A9B5-B87C7F1C63AD' \
  -derivedDataPath /tmp/tickly-derived \
  CODE_SIGNING_ALLOWED=NO \
  build
```

Resultado: `BUILD SUCCEEDED`. Registro: `/tmp/tickly-ios-final-build.log`.

## Smoke tests manuales

### Android

En el emulador `emulator-5554`, con fixture local de enfoque de un minuto y español:

- Se concedieron permisos de notificaciones y de alarmas exactas para la prueba.
- Se inició el intervalo desde la UI y se envió la aplicación a segundo plano con HOME.
- Al finalizar, se comprobó la publicación de la alarma con id `19`, canal `timer_finished_s0_v1` y título **«Enfoque finalizado»**.
- Al volver a la aplicación, el estado era `FINISHED`, el contador de bloques (`cb`) era `1` y el descanso no se había iniciado automáticamente.

### iOS

En el simulador iOS 26.5 se verificaron mediante accesibilidad:

- Inicio y pausa del temporizador.
- Persistencia del estado pausado tras relanzar la aplicación.
- Cambio de idioma de inglés a español.

## Regresión de temas — 12 de septiembre de 2026

- Android: tema semántico completo para los seis acentos, incluidos los contenedores de botones secundarios; el host de permisos comparte el acento seleccionado.
- iOS: tinte explícito en la pantalla, iconos y hoja de ajustes. Los selectores nativos se recrean al cambiar el acento del borrador para evitar conservar el tinte anterior. [Ajustes con acento menta](screenshots/ios-theme-settings.png).
- Reejecutados `:androidApp:assembleDebug`, `:androidApp:testDebugUnitTest`, `:sharedUI:testAndroidHostTest` y `:sharedLogic:testAndroidHostTest`: `BUILD SUCCESSFUL`. La suite de UI ahora contiene **3 pruebas**, en sustitución de la prueba inicial de la tabla anterior: paletas/contenedores, contraste mínimo 4.5:1 de roles de acento y fallback de índices inválidos. Registro: `/tmp/tickly-theme-android-build.log`.
- Recompilación iOS con el comando anterior: `BUILD SUCCEEDED`. Registro: `/tmp/tickly-theme-ios-build.log`. No se modificó el motor del temporizador.
- Regresión visual reproducida dos veces antes de corregir: diferencia de tono CTA/control de 138° en Android y 164° en iOS. Después: naranja Android 6°, verde Android 4°, ámbar iOS 0° y menta iOS 0° (umbral 36°). Verificado también el cambio inmediato de tinte en Ajustes iOS.
- Se restauró el acento naranja/ámbar original en ambos simuladores. Las capturas principales muestran el resultado actualizado.

Comprobación reproducible sobre capturas de la pantalla principal:

```bash
swift scripts/verify-theme-colors.swift captura-android.png android
swift scripts/verify-theme-colors.swift captura-ios.png ios
```

El script heredado compara regiones normalizadas del CTA y un control secundario. Está acotado al layout vertical predeterminado de Android Medium Phone e iPhone 17 Pro, sin banner de permisos; no es una prueba visual universal ni forma parte de la ruta de pruebas actual basada en Maestro.

## Regresión histórica de área táctil iOS — 12 de septiembre de 2026

- Se reprodujo dos veces en la pantalla principal pausada en español: el toque lateral izquierdo del CTA (`x=90, y=600`) no iniciaba el temporizador, mientras que pulsar el texto central sí lo hacía. El área visual de la cápsula era más amplia que su objetivo interactivo.
- Se corrigió el objetivo de interacción de la etiqueta expandida con `contentShape(.interaction, Capsule())`. Tras la corrección, el mismo punto lateral inició y pausó el temporizador en el simulador; el build de Xcode finalizó con `BUILD SUCCEEDED` (`/tmp/tickly-hit-build.log`).
- La investigación de documentación se delegó de forma acotada y con modelo heredado: la documentación oficial de SwiftUI confirma que `ContentShapeKinds.interaction` define el área de *hit testing* y accesibilidad. No se ejecutaron revisiones de receipts (`disabled/unmanaged`).

## Reorganización por slices — 12 de septiembre de 2026

- Código agrupado por `features/timer`, `features/settings`, `app` y `core`, con extracción real de pantallas, tema y textos. [Mapa y reglas](code-organization.md).
- Ruta delegada directa mediante agentes de colaboración con modelo heredado: exploración acotada, escritura por módulos sin solapamiento; integración y validación por el agente principal. Sin SDD ni revisiones de receipts (`disabled/unmanaged`). Se corrigieron imports de Compose detectados al compilar y se ajustó la compatibilidad de componentes Android antes de aceptar la integración.
- Gradle: `:androidApp:assembleDebug`, `:androidApp:testDebugUnitTest`, `:sharedUI:testAndroidHostTest`, `:sharedLogic:testAndroidHostTest` y `:sharedLogic:iosSimulatorArm64Test` terminaron con `BUILD SUCCESSFUL`. Registro: `/tmp/tickly-slices-gradle.log`.
- Resultados actuales: Android app **10**, UI **3**, lógica host **13** y lógica iOS **13** pruebas; **39 ejecuciones**, sin fallos ni omisiones. Las suites de lógica ejecutan casos compartidos en ambas plataformas. La nueva prueba Android protege los nombres de componentes usados en `PendingIntent`.
- Xcode recompiló la estructura final con `BUILD SUCCEEDED`: `/tmp/tickly-slices-ios.log`. El motor, TimerStore y la implementación iOS de avisos conservan sus cuerpos; recursos y configuración de compilación sin cambios.
- Instalación, arranque y navegación a Ajustes en emulador Android correctos desde `com.argote.tickly.app.MainActivity`. No se repitió el smoke interactivo iOS en esta reorganización: el simulador estaba apagado; la evidencia iOS de este cambio es compilación y pruebas de lógica.

## Límites generales de ejecución

- No se realizaron pruebas en dispositivos físicos.
- No se cubrió una matriz completa de fabricantes Android, Doze, reinicios ni modos silenciosos/Focus.
- La entrega de avisos y sus sonidos sigue sujeta a permisos y restricciones del sistema; no se promete una entrega universal.
- No se validó la entrega de notificaciones de iOS mediante pruebas automáticas; el smoke test de notificación en segundo plano corresponde a Android.
- La vibración en segundo plano de iOS está controlada por ajustes explícitos del sistema operativo.
