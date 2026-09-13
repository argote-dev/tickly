#!/usr/bin/env bash
set -euo pipefail

readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly repository_root="$(cd "${script_dir}/.." && pwd)"
readonly mode="${1:-all}"

cd "${repository_root}"

run_android() {
    "${repository_root}/gradlew" --no-daemon \
        qualityKtlintCheck \
        :androidApp:lintDebug \
        :androidApp:testDebugUnitTest \
        :sharedLogic:testAndroidHostTest \
        :sharedUI:testAndroidHostTest \
        :androidApp:assembleDebug
}

run_ios() {
    SWIFTLINT_FIX=0 "${script_dir}/swiftlint.sh"
    "${repository_root}/gradlew" --no-daemon :sharedLogic:iosSimulatorArm64Test
    mkdir -p "${repository_root}/build/reports"
    xcodebuild \
        -project "${repository_root}/iosApp/iosApp.xcodeproj" \
        -scheme iosApp \
        -derivedDataPath "${repository_root}/build/xcode-derived" \
        -configuration Debug \
        -sdk iphonesimulator \
        -destination 'generic/platform=iOS Simulator' \
        ARCHS=arm64 \
        ONLY_ACTIVE_ARCH=YES \
        CODE_SIGNING_ALLOWED=NO \
        CODE_SIGNING_REQUIRED=NO \
        build | tee "${repository_root}/build/reports/xcodebuild-ios.log"
}

case "${mode}" in
    lint)
        "${repository_root}/gradlew" --no-daemon qualityKtlintCheck
        SWIFTLINT_FIX=0 "${script_dir}/swiftlint.sh"
        ;;
    android)
        run_android
        ;;
    ios)
        run_ios
        ;;
    all)
        run_android
        run_ios
        ;;
    *)
        echo "Usage: $(basename "$0") [lint|android|ios|all]" >&2
        exit 2
        ;;
esac
