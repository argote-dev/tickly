#!/usr/bin/env bash
set -euo pipefail

readonly swiftlint_version="0.65.1"
readonly swiftlint_sha256="c1e429b0599cf1b516f369a2d9ec04eaf0e436f3c12b637df8851fa52ff694d0"
readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly repository_root="$(cd "${script_dir}/.." && pwd)"
readonly tools_dir="${repository_root}/build/tools/swiftlint"
readonly swiftlint_binary="${tools_dir}/${swiftlint_version}/swiftlint"
readonly archive_url="https://github.com/realm/SwiftLint/releases/download/${swiftlint_version}/portable_swiftlint.zip"

install_swiftlint() (
    command -v curl >/dev/null || { echo "curl is required to download SwiftLint." >&2; exit 1; }
    command -v shasum >/dev/null || { echo "shasum is required to verify SwiftLint." >&2; exit 1; }
    command -v unzip >/dev/null || { echo "unzip is required to install SwiftLint." >&2; exit 1; }

    local temporary_directory archive actual_sha binary
    temporary_directory="$(mktemp -d)"
    trap 'rm -rf "${temporary_directory}"' EXIT
    archive="${temporary_directory}/portable_swiftlint.zip"

    echo "Downloading SwiftLint ${swiftlint_version}..."
    curl --fail --location --retry 3 --silent --show-error --output "${archive}" "${archive_url}"
    actual_sha="$(shasum -a 256 "${archive}" | awk '{print $1}')"
    if [[ "${actual_sha}" != "${swiftlint_sha256}" ]]; then
        echo "SwiftLint checksum verification failed." >&2
        exit 1
    fi

    unzip -q "${archive}" -d "${temporary_directory}/unpacked"
    binary="$(find "${temporary_directory}/unpacked" -type f -name swiftlint -print -quit)"
    if [[ -z "${binary}" ]]; then
        echo "The verified SwiftLint archive did not contain an executable." >&2
        exit 1
    fi

    rm -rf "${tools_dir}/${swiftlint_version}"
    mkdir -p "${tools_dir}/${swiftlint_version}"
    install -m 755 "${binary}" "${swiftlint_binary}"
)

if [[ "$(uname -s)" != "Darwin" ]]; then
    echo "SwiftLint quality checks require macOS." >&2
    exit 1
fi

if [[ ! -x "${swiftlint_binary}" ]]; then
    install_swiftlint
fi

installed_version="$("${swiftlint_binary}" version)"
if [[ "${installed_version}" != "${swiftlint_version}" ]]; then
    echo "Cached SwiftLint version ${installed_version} does not match ${swiftlint_version}." >&2
    exit 1
fi

arguments=(lint --config "${repository_root}/.swiftlint.yml" --strict --no-cache)
if [[ "${SWIFTLINT_FIX:-0}" == "1" ]]; then
    arguments+=(--fix)
fi

cd "${repository_root}"
exec "${swiftlint_binary}" "${arguments[@]}" "$@"
