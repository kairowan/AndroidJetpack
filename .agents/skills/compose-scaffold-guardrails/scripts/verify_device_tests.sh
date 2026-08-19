#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$repo_root"

if [[ ! -d app/src/androidTest ]] ||
        ! find app/src/androidTest -type f -name '*.kt' -print -quit | rg -q .; then
    printf 'No Android device-test sources are defined; nothing to run.\n'
    exit 0
fi

if ! command -v adb >/dev/null 2>&1; then
    printf 'FAILED: adb is unavailable; install Android platform-tools before device verification.\n' >&2
    exit 1
fi

device_count="$(adb devices | awk 'NR > 1 && $2 == "device" { count++ } END { print count + 0 }')"
if [[ "$device_count" == "0" ]]; then
    printf 'FAILED: no ready Android device or emulator is connected.\n' >&2
    exit 1
fi

./gradlew --no-daemon --max-workers=4 :app:connectedDebugAndroidTest

printf 'Android device tests passed.\n'
