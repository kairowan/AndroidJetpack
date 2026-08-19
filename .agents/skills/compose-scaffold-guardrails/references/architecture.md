# KMP / CMP scaffold architecture

`docs/architecture.md` is the human-facing source of truth. This file records the maintenance boundaries enforced by the repository skill and scripts.

## Dependency direction

```text
Android app ─────────────┐
iOS CMP host + shared_ios├─> core-ui / feature commonMain
future Desktop/Web host ┘        │
                                 ├─> feature-owned state + presenters
                                 ├─> domain-feed
                                 └─> core state/model/navigation/design

Android composition root ─> core_network OkHttp + core_data + core_player
iOS composition root     ─> core_network Foundation + core_data + native media adapters
```

Dependencies point from a platform host toward shared contracts. Shared code never depends on an application host or a platform implementation.

## Target ownership

| Layer | Targets | Responsibility |
| --- | --- | --- |
| `app` | Android | Application graph, Activity, Navigation 3 entries |
| `core_ui` | Android/iOS/Desktop/Wasm | Theme, tokens, common components, UI contracts and shell UI |
| `feature_home` | Android/iOS/Desktop/Wasm | Home Screen, page model, presenter, state and Android adapters |
| `feature_media` | Android/iOS/Desktop/Wasm | Shorts/Detail Screens, state and Android player adapters |
| `core_state`, `core_model`, `core_navigation` | Android/iOS/Desktop/Wasm | Portable state and business-independent rules |
| `domain-feed` | Android/iOS/Desktop/Wasm | Narrow repository role and opaque paging session |
| `core_network` | Android/iOS | Portable HTTP policies plus OkHttp/Foundation engines |
| `core_data` | Android/iOS | Backend DTOs, shared JSON parsing, URL policy and domain mapping |
| `core_playback` | Android/iOS/Desktop/Wasm | Portable playback contracts, presets, state and planning |
| `core_player` | Android | Media3 player implementation and Compose surface |
| `shared_ios` | iOS | CMP composition root, UIKit/AVPlayer slots and framework export |

Desktop and Wasm are compile targets, not runnable applications yet. Add their hosts and drivers only when requested; do not pretend platform support by copying Android implementations.

## Screen flow

```text
platform Route or composition root
  -> shared state holder / presenter
  -> immutable page model
  -> common Screen(values, callbacks, platform slots)
```

- A `Screen` owns only element state such as scroll, pager, animation, and control visibility.
- Shared state holders own loading, paging, retry, selection, and recoverable page state.
- Android ViewModels provide lifecycle retention and delegate business actions to the shared holder.
- Routes collect lifecycle-aware state and inject Coil, Media3, orientation, system bars, and navigation callbacks.
- The iOS composition root collects shared state and injects UIKit/AVPlayer slots from `iosMain`; SwiftUI only hosts its Compose controller.
- A platform slot is a Composable lambda for capabilities that cannot be shared without importing a platform SDK.

Do not add a base ViewModel or generic event hierarchy. The current thin wrapper plus shared state holder is the reusable pattern.

## Data and paging

`FeedPageRepository` is the only role a Feed feature consumes. `FeedPager` coordinates opaque `continuationToken` values; only `core_data` knows that the backend token may contain a next-page URL.

The Android `AppContainer` creates one process-wide `NetworkClient`, lazy repository, and player factory. `Application` exposes only domain/player roles through `AppDependencies`. The iOS composition root creates one `NetworkClient` for the lifetime of its Compose controller and shares it between the repository and image platform slot.

`core_network` common code owns query encoding, portable request bodies, request/response models, interceptors, configurable idempotent retry, bounded server `Retry-After` hints, the total per-attempt timeout, standard HTTP cache policy/fallback, and coroutine cancellation. Cache misses have one portable error; network-first fallback applies only to transport failures, timeouts, 408, 429, and 5xx. Android owns OkHttp calls; iOS owns Foundation URLSession tasks. Platform engines cancel their native call but must not replace the common timeout budget or swallow an outer lifecycle/caller timeout. Keep speculative request registries, polling, racing, and business conversion out of this base module until a real caller requires them. `core_data` must not import either engine.

Repository suspend functions are main-safe and preserve cancellation. A cancellable native request cancels its underlying Call or task. Transport exceptions remain network-layer details and are returned as `Result` failures only after cancellation is excluded.

## Navigation

- `AppRoute` and `AppNavigationState` are portable.
- `AppTopLevelDestination` is the only top-level label/order mapping.
- `AppNavHost` owns the Android back stack and maps shared routes to Navigation 3 destinations.
- `NavDisplay` installs saveable-state and ViewModel-store decorators.
- Root Back delegates to the Activity host.
- User-visible fallback copy comes from Android resources.

## Platform isolation

Portable `commonMain` may use Compose Multiplatform packages such as `androidx.compose.*`, but not:

- `android.*` or Android lifecycle/activity APIs;
- Media3, Coil, Retrofit, or OkHttp;
- `platform.*`, Foundation, UIKit, or AVFoundation.

`domain-feed` has a stricter boundary: no platform types, transport annotations, DTOs, URLs, or network clients.

## Security

- Gradle Wrapper uses HTTPS and a pinned SHA-256.
- Android denies cleartext and backup by default.
- Generic URL/scheme validation happens in `core_network`; backend host allowlists and paging URLs are validated inside `core_data`.
- Trust-all managers, unsafe hostname verifiers, hardcoded secrets, and domain-wide cleartext exceptions are forbidden.

## Change rule

Run the YAGNI ladder after tracing the complete caller flow:

1. delete or reuse;
2. use Kotlin/Compose/platform behavior already available;
3. use an installed dependency;
4. only then write the smallest new code.

Leave one focused regression test for non-trivial shared state, paging, mapping, or navigation behavior. Do not add scaffolding solely to satisfy a naming convention, and do not rename the historical underscore modules without a dedicated migration.

## Verification

- `scripts/check_architecture.sh`: fast static boundary and repository-shape checks.
- `scripts/verify_scaffold.sh`: architecture, shared tests/compiles, Android lint, Debug/Release packages.
- `scripts/verify_device_tests.sh`: connected Android tests when a device is present.
- Xcode builds `shared_ios` through `embedAndSignAppleFrameworkForXcode`.

Temporary artifacts are not part of the scaffold. Remove generated reports and build output after verification; keep only durable regression tests.
