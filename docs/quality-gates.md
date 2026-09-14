# Quality gates

Las puertas de calidad son locales y reproducibles. No corrigen ni reformatean código durante una comprobación: cualquier incumplimiento bloquea el comando correspondiente.

## Comandos

Ejecuta los comandos desde cualquier directorio; los scripts calculan la raíz del repositorio.

```bash
# Todas las puertas (valor por defecto)
scripts/quality-gates.sh

# Sólo estilo Kotlin y Swift
scripts/quality-gates.sh lint

# Kotlin/Android: estilo, lint y APK debug
scripts/quality-gates.sh android

# Swift/iOS: SwiftLint y compilación del simulador
scripts/quality-gates.sh ios
```

La puerta Android ejecuta `qualityKtlintCheck`, `:androidApp:lintDebug` y `:androidApp:assembleDebug`.

La puerta iOS ejecuta SwiftLint en modo estricto y sin caché, y una compilación `xcodebuild` para simulador arm64 sin firma.

## Pruebas del producto

Las pruebas de comportamiento y regresión se realizan exclusivamente con Maestro. Aún no hay flujos Maestro versionados, por lo que los gates no ejecutan una suite de pruebas. Las fuentes Kotlin de prueba se conservan como historial, pero no se ejecutan en los gates ni se amplían bajo esta política. Compilar, aplicar lint o ejecutar análisis estático no equivale a ejecutar pruebas Maestro. Cuando existan flujos, ejecútalos con `maestro test <flujo.yaml>` siguiendo la [guía oficial de Maestro](https://github.com/mobile-dev-inc/maestro-docs/blob/main/maestro-cli/run-your-first-test-with-the-maestro-cli.md).

## Política

- **Kotlin:** ktlint 1.8.0, mediante `org.jlleitschuh.gradle.ktlint` 14.2.0, con el estilo `intellij_idea` (la convención Kotlin oficial soportada por ktlint 1.8.0). No hay baseline: los errores de estilo deben ser cero. Se excluyen fuentes generadas y directorios `build`.
- **Compose:** se permite el nombre PascalCase únicamente en funciones anotadas `@Composable`, mediante la excepción explícita de ktlint.
- **Swift:** SwiftLint 0.65.1 se descarga automáticamente en `build/tools/swiftlint/0.65.1` y se verifica con SHA-256 antes de usarlo. La configuración usa las reglas normales por defecto y cubre `iosApp/iosApp` y los scripts Swift; `--strict --no-cache` convierte advertencias en fallos. Para aplicar correcciones de SwiftLint de forma intencional, usa `SWIFTLINT_FIX=1 scripts/swiftlint.sh`; las puertas nunca activan ese modo.
- **Android Lint:** debe terminar sin errores. Sus advertencias se muestran y se gestionan separadamente de los linters de estilo para no ocultar deuda existente con una baseline.
- **Builds:** ambas compilaciones deben terminar correctamente. Los checks de estilo y análisis estático deben terminar sin errores. La evidencia de pruebas se obtiene únicamente de flujos Maestro cuando estén versionados; no se inventó un umbral de cobertura.

## CI y protección de ramas

`.github/workflows/quality.yml` ejecuta las puertas en cada `push` y `pull_request`, con nombres estables **Android quality** e **iOS quality**. El workflow no tiene filtros de rutas, por lo que los cambios de configuración también se validan.

El repositorio privado es [`argote-dev/tickly`](https://github.com/argote-dev/tickly). La protección de rama sigue pendiente: marca ambos checks como requeridos antes de permitir merge (según las capacidades del plan GitHub).

## Herramientas y correcciones

- Android necesita JDK 17, `compileSdk` 37 y la plataforma Android SDK 37.0 (`local.properties` ignorado o `ANDROID_HOME`). `minSdk` 24 y `targetSdk` 36 no cambian.
- iOS necesita macOS Apple Silicon, Xcode 26.6, un runtime de simulador iOS 26 y las herramientas Gradle/Android anteriores. CI fija Xcode 26.6 y usa `macos-26`.
- La primera ejecución descarga dependencias Gradle y SwiftLint; las siguientes reutilizan las herramientas locales. No se instala SwiftLint globalmente.
- Para corregir Kotlin intencionalmente: `./gradlew ktlintFormat`. Después ejecuta otra vez el gate; el formateador no resuelve todas las infracciones de nombres.
- Los scripts pueden invocarse por ruta absoluta desde otro directorio. Los ejemplos anteriores asumen que estás en la raíz.
- Reportes Gradle: `<módulo>/build/reports/`; log Xcode: `build/reports/xcodebuild-ios.log`. CI conserva los artefactos disponibles aunque falle un gate.

Los gates son independientes del modo de receipts de agentes, que continúa **disabled/unmanaged**.

Referencias de configuración: [ktlint Gradle](https://github.com/JLLeitschuh/ktlint-gradle), [SwiftLint](https://github.com/realm/SwiftLint), [estilos de ktlint 1.8.0](https://github.com/pinterest/ktlint/blob/1.8.0/ktlint-rule-engine-core/src/main/kotlin/com/pinterest/ktlint/rule/engine/core/api/editorconfig/CodeStyleEditorConfigProperty.kt).

## Validación histórica de la configuración (13 septiembre 2026)

La siguiente evidencia corresponde a la política anterior, que ejecutaba pruebas Kotlin y KMP en los gates. No describe la política actual de pruebas, exclusivamente basada en Maestro.

- `./scripts/quality-gates.sh all`: correcto; ktlint sin infracciones, SwiftLint sin infracciones en 20 archivos y ambas compilaciones correctas.
- Pruebas: Android app **10**, sharedLogic host **11**, sharedUI host **7**, sharedLogic iOS **11**; **39 ejecuciones, cero fallos/errores/omitidas**.
- Android Lint: **0 errores, 26 advertencias existentes**; no se ocultan ni se consideran resueltas.
- Se comprobó la instalación limpia de SwiftLint con verificación del archivo descargado, la sintaxis Bash y el rechazo de un modo desconocido.
- Pruebas negativas temporales: SwiftLint rechazó espacios finales; ktlint rechazó código mal formateado en scripts raíz y siete ubicaciones de fuentes/tests Kotlin, incluida `iosMain`. Ningún check modificó los archivos. Los probes se retiraron al terminar.
- Implementación directa delegada mediante agentes de colaboración con modelo heredado: exploración, configuración y formato Swift. El agente principal integró la entrega, corrigió la resolución del catálogo Gradle y el valor de estilo inválido, normalizó Kotlin y ejecutó la validación final. No hubo reviews de receipts.
- El workflow está preparado, pero no se ejecutó en GitHub ni se activaron required checks: el repositorio no tiene remoto configurado.

## Dependabot

`.github/dependabot.yml` revisa cada lunes a las 09:00 (`America/Bogota`) las dependencias de Gradle y las acciones de GitHub. Cada ecosistema admite hasta cinco PR abiertas; las actualizaciones *minor* y *patch* se agrupan por ecosistema, mientras que las *major* quedan en PR independientes para revisión explícita. Dependabot no hace *auto-merge*.

El catálogo de versiones Kotlin/Gradle (`gradle/libs.versions.toml`) queda cubierto por el ecosistema Gradle. SwiftLint está fijado manualmente por versión y SHA-256 en el script de descarga, y Xcode/SDK se fijan en CI; ninguno de esos elementos está gestionado por Dependabot.
