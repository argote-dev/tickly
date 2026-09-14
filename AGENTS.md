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

Follow Kotlin official style, configured in `gradle.properties`, with four-space indentation. Use the existing `com.argote.tickly` package hierarchy. Name classes and composables in PascalCase, and ordinary functions and properties in camelCase. Keep each top-level class, interface, enum, or object in its own same-named Kotlin file; apply the same rule to Swift classes, structs, enums, and protocols. Keep nested implementation types with their owner. Each named `@Composable` function and SwiftUI view also gets its own same-named file; preserve state/effect ownership when extracting UI components. Use the pinned ktlint and SwiftLint configuration; see `docs/quality-gates.md` for formatting commands and rule policy.

Before delivery, run `./scripts/quality-gates.sh all` on macOS with both toolchains, or run the available platform gate and explicitly report the unvalidated gate. Read `docs/quality-gates.md` when changing quality configuration or preparing a PR.

## Testing Guidelines

Tests use `kotlin.test`, including `@Test` and assertions. Name test classes/files `*Test` and use descriptive test method names. Put platform-independent tests in `commonTest` and platform-dependent tests in the matching source set. Add regression tests for behavior changes. No coverage threshold is configured.

## Commit & Pull Request Guidelines

Use concise, imperative commit subjects and keep changes focused. PRs should describe the change, link relevant issues, list validation performed, and include screenshots for UI changes. No PR template is present.

## GitHub Flow & GitOps

Use GitHub Flow for every change, including documentation, configuration, and Dependabot updates:

1. Start a short-lived, descriptive branch from the current `origin/main`; keep unrelated work on separate branches.
2. Implement and validate on that branch using the quality gates in `docs/quality-gates.md`.
3. Push the branch and open a PR targeting `main`, with validation evidence and any remaining limitations.
4. Address feedback and merge only after approval and successful `Android quality` and `iOS quality` checks. Agents leave merge to the user unless explicitly authorized. Do not push directly to `main` or bypass failed checks.
5. Delete the completed branch after merge. Reverts and hotfixes also go through a branch and PR.

GitOps is the operational direction: keep desired operational state declarative and versioned in Git, change it through PRs, and keep secrets and signing credentials outside Git. Where GitOps is implemented, agents must pull and continuously reconcile that state; a GitHub Actions build alone is CI, not a complete GitOps implementation. Rollbacks should restore a known desired state through a reviewed revert rather than an untracked manual change.

The deployment targets, environments, and reconciliation mechanism are not yet defined. Do not assume Kubernetes, install a reconciler, or configure store publishing without an explicit decision. Branch protection and required checks also remain pending configuration; the workflow above applies even before enforcement is enabled. GitHub PR approval is separate from receipt-driven agent reviews, which remain `disabled/unmanaged` until the user explicitly enables them.

## Security & Configuration

Keep SDK paths in ignored `local.properties`. Keep secrets, signing credentials, generated `build/` output, and machine-specific IDE files out of commits.
