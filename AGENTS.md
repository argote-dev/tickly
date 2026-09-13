# Repository Guidelines

## Project Structure & Module Organization

- `androidApp/`: Android entry point; Kotlin code is under `src/main/kotlin`, resources under `src/main/res`.
- `sharedLogic/`: Shared Kotlin logic in `src/commonMain/kotlin`; platform implementations in `androidMain` and `iosMain`.
- `sharedUI/`: Shared Compose UI in `src/commonMain/kotlin`; shared assets in `src/commonMain/composeResources`.
- `iosApp/iosApp/`: SwiftUI entry point and `Assets.xcassets`; open the iOS project in Xcode.
- Tests live in module source sets such as `commonTest`, `androidHostTest`, and `iosTest`. Dependency versions are centralized in `gradle/libs.versions.toml`.

Keep reusable logic in `sharedLogic` and reusable UI in `sharedUI`; isolate platform APIs in platform-specific source sets.

Before adding or moving source files, read `docs/code-organization.md`. Organize by `features/<feature>/{presentation,domain,data}` across existing modules and source sets, with `app` for dependency composition and `core` only for cross-feature code. Keep domain independent of platform APIs, inject data adapters into presentation, and keep screens independent of other features' presentation state. Kotlin packages mirror folders; Swift uses `Features/<Feature>`. Keep tests with the corresponding slice's package and do not introduce empty layers or new Gradle modules solely to organize files.

## Build, Test, and Development Commands

Run commands from the repository root using the Gradle wrapper:

- `./gradlew :androidApp:assembleDebug`: Build the Android debug APK.
- `./gradlew :sharedUI:testAndroidHostTest :sharedLogic:testAndroidHostTest`: Run Android host tests for both shared modules.
- `./gradlew :sharedLogic:iosSimulatorArm64Test`: Run shared logic tests on an ARM64 iOS simulator; requires macOS and Xcode.

Run Android through the IDE's run configuration. Open `iosApp/` in Xcode to build and run iOS.

## Coding Style & Naming Conventions

Follow Kotlin official style, configured in `gradle.properties`, with four-space indentation. Use the existing `com.argote.tickly` package hierarchy. Name classes and composables in PascalCase, and ordinary functions and properties in camelCase. Keep each top-level class, interface, enum, or object in its own same-named Kotlin file; apply the same rule to Swift classes, structs, enums, and protocols. Keep nested implementation types with their owner. Each named `@Composable` function and SwiftUI view also gets its own same-named file; preserve state/effect ownership when extracting UI components. Use IDE formatting; no dedicated formatter or lint configuration is present.

## Testing Guidelines

Tests use `kotlin.test`, including `@Test` and assertions. Name test classes/files `*Test` and use descriptive test method names. Put platform-independent tests in `commonTest` and platform-dependent tests in the matching source set. Add regression tests for behavior changes. No coverage threshold is configured.

## Commit & Pull Request Guidelines

This repository was initialized with its first commit on 12 September 2026, so there is no earlier history from which to infer conventions. Use concise, imperative commit subjects and keep changes focused. PRs should describe the change, link relevant issues, list validation performed, and include screenshots for UI changes. No PR template is present.

## Security & Configuration

Keep SDK paths in ignored `local.properties`. Keep secrets, signing credentials, generated `build/` output, and machine-specific IDE files out of commits.
