#!/usr/bin/env bash
# scripts/verificar.sh · la puerta de calidad de BakiFace (ADR 0006). La misma orden en la máquina del dueño y en la CI.
#
#   bash scripts/verificar.sh            todo: estilo, pruebas, cobertura, lint, APK y, en macOS, iOS
#   BAKIFACE_SKIP_IOS=1 bash scripts/...  sin la parte de iOS (la CI de Linux no puede compilarla)
#
# Corre cada paso aunque falle el anterior y falla al final si alguno falló. Lo que comprueba, en orden:
#   1. que ningún fichero de Kotlin use «!!» (estándar del dueño: null-safety sin atajos);
#   2. ktlint, las pruebas de la JVM de todas las capas (las de UI con Robolectric), el mínimo de cobertura de Kover,
#      lint de Android y los APK de depuración y de lanzamiento (este, con R8);
#   3. en macOS: las pruebas de todas las capas en el simulador de iOS y la app de iOS compilada con xcodebuild.
set -uo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [[ -z "${JAVA_HOME:-}" && -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi

failed=()

step() {
    local name=$1
    shift
    printf '\n▶ %s\n' "$name"
    if "$@"; then
        printf '✓ %s\n' "$name"
    else
        printf '✗ %s\n' "$name"
        failed+=("$name")
    fi
}

no_double_bang() {
    local found
    found=$(grep -rn '!!' core shared androidApp --include='*.kt' | grep -v '/build/' || true)
    if [[ -n "$found" ]]; then
        printf '%s\n' "$found"
        return 1
    fi
}

step "Sin «!!» en el código" no_double_bang

step "Estilo, pruebas de la JVM, cobertura, lint y APK" ./gradlew --continue \
    ktlintCheck \
    :core:domain:jvmTest :core:render:testAndroidHostTest :core:data:testAndroidHostTest :shared:testAndroidHostTest \
    koverVerify koverXmlReport \
    :androidApp:lintDebug :androidApp:assembleDebug :androidApp:assembleRelease

if [[ "$(uname)" == "Darwin" && "${BAKIFACE_SKIP_IOS:-0}" != "1" ]]; then
    step "Pruebas en el simulador de iOS" ./gradlew --continue \
        :core:domain:iosSimulatorArm64Test :core:render:iosSimulatorArm64Test \
        :core:data:iosSimulatorArm64Test :shared:iosSimulatorArm64Test

    simulator=$(xcrun simctl list devices available | grep -E '^\s+iPhone' | head -1 | grep -oE '[0-9A-F-]{36}')
    step "App de iOS para el simulador (xcodebuild)" xcodebuild -quiet \
        -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
        -destination "platform=iOS Simulator,id=${simulator}" \
        -derivedDataPath build/xcode build
fi

printf '\n'
if (( ${#failed[@]} > 0 )); then
    printf 'La puerta NO pasa. Han fallado:\n'
    printf '  - %s\n' "${failed[@]}"
    exit 1
fi
printf 'La puerta pasa.\n'
