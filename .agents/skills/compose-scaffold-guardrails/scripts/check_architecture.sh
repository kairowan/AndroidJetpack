#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$repo_root"

fail() {
    printf 'FAILED: %s\n' "$1" >&2
    exit 1
}

require_file() {
    [[ -f "$1" ]] || fail "missing required file: $1"
}

require_match() {
    local pattern="$1"
    local path="$2"
    local message="$3"
    rg -q "$pattern" "$path" || fail "$message"
}

forbid_match() {
    local pattern="$1"
    local message="$2"
    shift 2
    if rg -n "$pattern" "$@"; then
        fail "$message"
    fi
}

modules=(
    app
    core_ui
    core_state
    shared_ios
    core_model
    core_network
    core_data
    domain-feed
    core_navigation
    core_playback
    core_player
    feature_home
    feature_media
)

for module in "${modules[@]}"; do
    [[ -d "$module" ]] || fail "settings references a missing module directory: $module"
    require_match "include\\(\":${module}\"\\)" settings.gradle.kts \
        "settings.gradle.kts must include :${module}"
done

for removed_module in \
    Lib_Network \
    core_legacy_network \
    core_designsystem \
    core_design_tokens \
    core_ui_contract \
    shared-ui \
    feature_home_shared \
    feature_media_shared \
    feature_detail \
    feature_shorts; do
    [[ ! -e "$removed_module" ]] || fail "removed legacy module still exists: $removed_module"
done
forbid_match 'Lib_Network|core_legacy_network|core_design(system|_tokens)|core_ui_contract|shared-ui|feature_(home_shared|media_shared|detail|shorts)|includeLegacy' \
    'removed module references must stay deleted' \
    settings.gradle.kts build.gradle.kts gradle

portable_modules=(
    core_ui
    core_state
    core_model
    domain-feed
    core_navigation
    core_playback
    feature_home
    feature_media
)

for module in "${portable_modules[@]}"; do
    build_file="$module/build.gradle.kts"
    require_match 'androidTarget' "$build_file" "$module must target Android"
    require_match 'iosArm64' "$build_file" "$module must target iOS devices"
    require_match 'iosSimulatorArm64' "$build_file" "$module must target Apple Silicon simulators"
    require_match 'jvm\("desktop"\)' "$build_file" "$module must target Desktop"
    require_match 'wasmJs' "$build_file" "$module must target Wasm"
done

for platform_module in core_network core_data; do
    for target in androidTarget iosArm64 iosSimulatorArm64; do
        require_match "$target" "$platform_module/build.gradle.kts" \
            "$platform_module must provide $target"
    done
done
forbid_match 'jvm\("desktop"\)|wasmJs' \
    'network and data drivers are intentionally Android/iOS-only until a real Desktop/Web host exists' \
    core_network/build.gradle.kts core_data/build.gradle.kts
require_match 'iosArm64' shared_ios/build.gradle.kts 'shared_ios must export an iOS framework'
require_match 'iosSimulatorArm64' shared_ios/build.gradle.kts \
    'shared_ios must support Apple Silicon simulators'

common_paths=()
for module in "${modules[@]}"; do
    if [[ -d "$module/src/commonMain" ]]; then
        common_paths+=("$module/src/commonMain")
    fi
done
forbid_match \
    '^[[:space:]]*import[[:space:]]+(android\.|androidx\.(activity|lifecycle|media3)|coil\.|okhttp3\.|retrofit2\.|platform\.)' \
    'commonMain leaked a platform-only API' \
    "${common_paths[@]}" --glob '*.kt'

forbid_match \
    '^[[:space:]]*import[[:space:]]+(android\.|androidx\.|coil\.|okhttp3\.|retrofit2\.|kotlinx\.serialization|platform\.)|https?://|nextPageUrl' \
    'domain-feed must contain only portable business contracts and opaque continuation tokens' \
    domain-feed/src/commonMain --glob '*.kt'

feature_common_paths=(
    feature_home/src/commonMain
    feature_media/src/commonMain
)
forbid_match \
    'viewModel\(|collectAsStateWithLifecycle|LocalContext|EyepetizerRepositoryFactory|VideoPlayerFactory|androidx\.media3|coil\.' \
    'shared Feature code must remain a pure Screen/state/presenter layer' \
    "${feature_common_paths[@]}" --glob '*.kt'
forbid_match \
    'com\.kotlinmvvm\.core\.data|projects\.coreData|project\(":core_data"\)' \
    'Feature modules must depend on domain contracts, not core_data implementations' \
    feature_home feature_media \
    --glob '*.kt' --glob 'build.gradle.kts'

require_files=(
    app/src/main/java/com/ghn/cocknovel/di/AppDependencies.kt
    app/src/main/java/com/ghn/cocknovel/di/AppContainer.kt
    core_network/src/commonMain/kotlin/com/kotlinmvvm/core/network/NetworkRequest.kt
    core_network/src/commonMain/kotlin/com/kotlinmvvm/core/network/NetworkException.kt
    core_network/src/commonMain/kotlin/com/kotlinmvvm/core/network/NetworkClient.kt
    core_network/src/androidMain/kotlin/com/kotlinmvvm/core/network/AndroidNetworkClientFactory.kt
    core_network/src/iosMain/kotlin/com/kotlinmvvm/core/network/IosNetworkClientFactory.kt
    core_state/src/commonMain/kotlin/com/kotlinmvvm/core/state/PagedStateHolder.kt
    domain-feed/src/commonMain/kotlin/com/kotlinmvvm/domain/feed/paging/FeedPager.kt
    domain-feed/src/commonMain/kotlin/com/kotlinmvvm/domain/feed/repository/FeedPageRepository.kt
    feature_media/src/commonMain/kotlin/com/kotlinmvvm/feature/detail/VideoDetailScreen.kt
    feature_media/src/androidMain/kotlin/com/kotlinmvvm/feature/detail/VideoDetailRoute.kt
    feature_media/src/androidMain/kotlin/com/kotlinmvvm/feature/detail/VideoDetailViewModel.kt
    feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeScreen.kt
    feature_home/src/androidMain/kotlin/com/kotlinmvvm/feature/home/HomeRoute.kt
    feature_home/src/androidMain/kotlin/com/kotlinmvvm/feature/home/HomeViewModel.kt
    feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeFeedStateHolder.kt
    feature_media/src/commonMain/kotlin/com/kotlinmvvm/feature/shorts/ShortsScreen.kt
    feature_media/src/androidMain/kotlin/com/kotlinmvvm/feature/shorts/ShortsRoute.kt
    feature_media/src/androidMain/kotlin/com/kotlinmvvm/feature/shorts/ShortsViewModel.kt
    shared_ios/src/iosMain/kotlin/com/kotlinmvvm/shared/ios/IosAppController.kt
    shared_ios/src/iosMain/kotlin/com/kotlinmvvm/shared/ios/IosPlatformMedia.kt
    iosApp/iosApp/ContentView.swift
    iosApp/iosApp/Info.plist
)
for file in "${require_files[@]}"; do
    require_file "$file"
done

app_file=app/src/main/java/com/ghn/cocknovel/App.kt
container_file=app/src/main/java/com/ghn/cocknovel/di/AppContainer.kt
navigation_file=app/src/main/java/com/ghn/cocknovel/navigation/AppNavigation.kt
require_match 'AppDependencies' "$app_file" 'Application must expose AppDependencies'
forbid_match 'NetworkClient|EyepetizerRepositoryFactory|VideoPlayerFactory' \
    'Application must not assemble platform implementations directly' \
    "$app_file"
require_match 'createAndroidNetworkClient' "$container_file" \
    'AppContainer must create the process-wide Android NetworkClient'
require_match 'EyepetizerRepositoryFactory\.create\(networkClient\)' "$container_file" \
    'AppContainer must create the Android repository'
require_match 'createVideoPlayerFactory' "$container_file" \
    'AppContainer must create the Android player factory'

creation_files="$(rg -l 'EyepetizerRepositoryFactory\.create\(' \
    --glob '*.kt' app shared_ios feature_home feature_media core_ui core_state domain-feed || true)"
while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    case "$file" in
        "$container_file"|shared_ios/src/iosMain/kotlin/com/kotlinmvvm/shared/ios/*) ;;
        *) fail "repository implementation created outside a platform composition boundary: $file" ;;
    esac
done <<< "$creation_files"

ios_controller=shared_ios/src/iosMain/kotlin/com/kotlinmvvm/shared/ios/IosAppController.kt
ios_media=shared_ios/src/iosMain/kotlin/com/kotlinmvvm/shared/ios/IosPlatformMedia.kt
ios_content_view=iosApp/iosApp/ContentView.swift
ios_info_plist=iosApp/iosApp/Info.plist
for shared_screen in feature_home feature_media core_ui; do
    require_match "project\\(\":${shared_screen}\"\\)" shared_ios/build.gradle.kts \
        "shared_ios must compose the shared ${shared_screen} UI"
done
require_match 'ComposeUIViewController' "$ios_controller" \
    'iOS must expose a Compose UIViewController'
require_match 'IosAppController\(\)\.makeViewController' "$ios_content_view" \
    'SwiftUI must remain a thin wrapper around the CMP controller'
require_match 'UIKitViewController' "$ios_media" \
    'iOS video must be injected through a UIKit platform slot'
require_match 'AVPlayerViewController' "$ios_media" \
    'iOS video slot must use AVPlayer'
require_match 'createIosNetworkClient' "$ios_controller" \
    'iOS composition root must create one Foundation-backed NetworkClient'
require_match 'networkClient\.getBytes' "$ios_media" \
    'iOS images must reuse the composition-root NetworkClient'
require_match '<key>UILaunchScreen</key>' "$ios_info_plist" \
    'iOS must declare a launch screen to avoid compatibility-mode letterboxing'
require_match '<key>CADisableMinimumFrameDurationOnPhone</key>' "$ios_info_plist" \
    'Compose iOS rendering requires high refresh rates to be enabled'
require_match 'EXCLUDED_ARCHS\[sdk=iphonesimulator\*\].*x86_64' iosApp/iosApp.xcodeproj/project.pbxproj \
    'Xcode must not request an unsupported Intel simulator framework'
if [[ -e iosApp/iosApp/HomeFeedViewModel.swift ]] || \
    rg -q 'class Ios(HomeFeed|Shorts|VideoDetail|Design)Bridge' shared_ios --glob '*.kt'; then
    fail 'duplicate SwiftUI pages and snapshot bridges must stay deleted'
fi

require_match 'rememberSaveableStateHolderNavEntryDecorator' "$navigation_file" \
    'Navigation 3 must retain destination saveable state'
require_match 'rememberViewModelStoreNavEntryDecorator' "$navigation_file" \
    'Navigation 3 must scope destination ViewModels'
require_match 'onRootBack' "$navigation_file" 'root Back must delegate to the Android host'
require_match 'R\.string\.unknown_destination' "$navigation_file" \
    'navigation fallback text must use Android resources'
forbid_match 'Text\("(Unknown|Invalid)' \
    'user-visible navigation fallback text must not be hardcoded' \
    "$navigation_file"

require_match 'ContentFrame' core_player/src/main/java/com/kotlinmvvm/core/player/ui/PlayerView.kt \
    'the Android video surface must use Media3 Compose ContentFrame'
forbid_match 'PlayerView|AndroidView' \
    'do not embed the legacy Media3 PlayerView in Compose' \
    core_player/src/main/java --glob '*.kt' \
    --glob '!VideoPlayerView.kt' --glob '!PlayerView.kt'
require_file core_player/src/main/java/com/kotlinmvvm/core/player/api/VideoPlayerFactory.kt
require_match 'showSpeedControl' core_player/src/main/java/com/kotlinmvvm/core/player/ui/PlayerControls.kt \
    'the shared Android player controls must honor the portable speed configuration'
require_match 'PlaybackSpeedStepper\.nextSpeed' \
    core_player/src/main/java/com/kotlinmvvm/core/player/ui/PlayerControls.kt \
    'player speed controls must reuse the portable stepping rule'
if rg --files feature_media | rg -q 'BrandedPlayerControls'; then
    fail 'the duplicate Detail player controls must stay deleted'
fi
window_helper=feature_media/src/androidMain/kotlin/com/kotlinmvvm/feature/media/VideoWindowMode.kt
require_file "$window_helper"
forbid_match 'private fun Activity\.applyVideoWindowMode|private tailrec fun Context\.findActivity' \
    'Detail and Shorts must reuse the shared Android video-window adapter' \
    feature_media --glob '*.kt'

repository_file=core_data/src/commonMain/kotlin/com/kotlinmvvm/core/data/repository/EyepetizerRepositoryFactory.kt
android_network=core_network/src/androidMain/kotlin/com/kotlinmvvm/core/network/AndroidNetworkClientFactory.kt
ios_network=core_network/src/iosMain/kotlin/com/kotlinmvvm/core/network/IosNetworkClientFactory.kt
require_match 'catch \(error: CancellationException\)' "$repository_file" \
    'the shared repository must distinguish coroutine cancellation'
require_match 'throw error' "$repository_file" \
    'the shared repository must rethrow coroutine cancellation'
for engine in "$android_network" "$ios_network"; do
    require_match 'suspendCancellableCoroutine' "$engine" \
        "$engine must bridge native calls with cancellable coroutines"
done
require_match 'invokeOnCancellation \{ call\.cancel\(\) \}' "$android_network" \
    'cancelling a coroutine must cancel its OkHttp Call'
require_match 'invokeOnCancellation \{ task\.cancel\(\) \}' "$ios_network" \
    'cancelling a coroutine must cancel its URLSession task'
require_match 'project\(\":core_network\"\)' core_data/build.gradle.kts \
    'core_data must use the shared core_network boundary'
forbid_match 'okhttp3\.|retrofit2\.|NSURLSession|dataTaskWith(Request|URL)' \
    'HTTP engines must stay inside core_network platform source sets' \
    core_data shared_ios app feature_home feature_media \
    --glob '*.kt' --glob '!**/build/**'
forbid_match 'retrofit|converter-gson|jitpack' \
    'deleted Retrofit/Gson-converter/JitPack dependencies must stay removed' \
    settings.gradle.kts gradle core_data app shared_ios --glob '*.kts' --glob '*.toml'

if rg --files core_ui | rg -q '/BaseViewModel\.kt$'; then
    fail 'unused BaseViewModel templates must stay deleted'
fi
if rg --files core_data | rg -q '/BaseApiRepository\.kt$'; then
    fail 'single-caller BaseApiRepository templates must stay deleted'
fi
forbid_match 'ExampleUnitTest|ExampleInstrumentedTest' \
    'generated sample tests must be deleted' \
    . --glob '*.kt' --glob '!**/build/**'

security_manifest=app/src/main/AndroidManifest.xml
require_match 'android:usesCleartextTraffic=\"false\"' "$security_manifest" \
    'Android must deny cleartext traffic'
require_match 'android:allowBackup=\"false\"' "$security_manifest" \
    'Android backup must remain disabled until a reviewed policy exists'
require_match 'cleartextTrafficPermitted=\"false\"' app/src/main/res/xml/network_security_config.xml \
    'network security config must deny cleartext'
forbid_match 'TrustAll|trustAll|ALLOW_ALL_HOSTNAME_VERIFIER|hostnameVerifier[[:space:]]*\{[[:space:]]*true' \
    'trust-all TLS code is forbidden' \
    . --glob '*.kt' --glob '*.java' --glob '!**/build/**'
require_match '^distributionUrl=https' gradle/wrapper/gradle-wrapper.properties \
    'Gradle Wrapper must use HTTPS'
require_match '^distributionSha256Sum=[0-9a-f]{64}$' gradle/wrapper/gradle-wrapper.properties \
    'Gradle Wrapper must pin the distribution SHA-256'
require_match '^org\.gradle\.configuration-cache=true$' gradle.properties \
    'Gradle configuration cache must stay enabled'
require_match '^org\.gradle\.caching=true$' gradle.properties \
    'Gradle build cache must stay enabled'
require_match '^android\.enableJetifier=false$' gradle.properties \
    'Jetifier must stay disabled'

test_files=(
    core_state/src/commonTest/kotlin/com/kotlinmvvm/core/state/PagedStateHolderTest.kt
    domain-feed/src/commonTest/kotlin/com/kotlinmvvm/domain/feed/paging/FeedPagerTest.kt
    core_data/src/commonTest/kotlin/com/kotlinmvvm/core/data/repository/EyepetizerUrlPolicyTest.kt
    core_network/src/commonTest/kotlin/com/kotlinmvvm/core/network/NetworkClientTest.kt
    core_navigation/src/commonTest/kotlin/com/kotlinmvvm/core/navigation/AppNavigationStateTest.kt
    core_playback/src/commonTest/kotlin/com/kotlinmvvm/core/player/state/PlaybackSpeedStepperTest.kt
    core_ui/src/commonTest/kotlin/com/kotlinmvvm/core/ui/model/PagedListBehaviorTest.kt
    feature_media/src/commonTest/kotlin/com/kotlinmvvm/feature/media/MediaPlaybackStateTest.kt
    core_ui/src/commonTest/kotlin/com/kotlinmvvm/core/ui/navigation/AppTopLevelDestinationTest.kt
)
for file in "${test_files[@]}"; do
    require_file "$file"
done

verify_script=.agents/skills/compose-scaffold-guardrails/scripts/verify_scaffold.sh
device_script=.agents/skills/compose-scaffold-guardrails/scripts/verify_device_tests.sh
for script in "$verify_script" "$device_script"; do
    require_file "$script"
    forbid_match 'APP_NAVIGATION_MODE|APP_ENVIRONMENT' \
        'verification scripts must not carry deleted template build properties' \
        "$script"
done
require_match 'compileKotlinWasmJs' "$verify_script" \
    'full verification must compile a portable Wasm target'
require_match 'compileKotlinIosSimulatorArm64' "$verify_script" \
    'full verification must compile an iOS simulator target'
require_match ':app:assembleDebug' "$verify_script" \
    'full verification must assemble Android Debug'
require_match ':app:assembleRelease' "$verify_script" \
    'full verification must assemble Android Release'
require_match ':feature_media:compileKotlinWasmJs' "$verify_script" \
    'full verification must compile the media Screens for Wasm'
require_match ':feature_media:compileKotlinIosSimulatorArm64' "$verify_script" \
    'full verification must compile the media Screens for iOS'
require_match ':core_network:testDebugUnitTest' "$verify_script" \
    'full verification must test the shared network policies'
require_match ':core_network:compileKotlinIosSimulatorArm64' "$verify_script" \
    'full verification must compile the Foundation network engine'

printf 'KMP/CMP architecture checks passed.\n'
