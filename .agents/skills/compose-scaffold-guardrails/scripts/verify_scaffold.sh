#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$repo_root"

bash .agents/skills/compose-scaffold-guardrails/scripts/check_architecture.sh

./gradlew --no-daemon --max-workers=4 \
    :core_state:desktopTest \
    :core_playback:desktopTest \
    :domain-feed:desktopTest \
    :core_navigation:desktopTest \
    :core_ui:desktopTest \
    :feature_home:desktopTest \
    :feature_media:desktopTest \
    :core_network:testDebugUnitTest \
    :core_data:testDebugUnitTest

./gradlew --no-daemon --max-workers=4 \
    :core_ui:compileKotlinWasmJs \
    :feature_home:compileKotlinWasmJs \
    :feature_media:compileKotlinWasmJs \
    :core_playback:compileKotlinWasmJs \
    :feature_media:compileKotlinIosSimulatorArm64 \
    :core_playback:compileKotlinIosSimulatorArm64 \
    :core_network:compileKotlinIosSimulatorArm64 \
    :core_data:compileKotlinIosSimulatorArm64 \
    :shared_ios:compileKotlinIosSimulatorArm64

./gradlew --no-daemon --max-workers=4 :app:lintDebug
./gradlew --no-daemon --max-workers=4 :app:assembleDebug
./gradlew --no-daemon --max-workers=4 :app:assembleRelease :app:bundleRelease

printf 'KMP/CMP scaffold verification passed.\n'
