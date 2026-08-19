---
name: compose-scaffold-guardrails
description: Enforce this repository's KMP/CMP module boundaries, shared UDF state, platform adapters, Navigation 3 lifecycle scoping, dependency composition, verification, and cleanup rules.
---

# KMP / CMP Scaffold Guardrails

Keep the scaffold small, portable, and honest about which targets are runnable.

## Workflow

1. Read `references/architecture.md` and `docs/architecture.md` before changing application or build code.
2. Trace the affected flow from the platform host through Route or composition root, shared state holder, Screen, domain contract, and platform implementation.
3. Reuse the current module and pattern; do not add a base class, use case, DI framework, module, or dependency for one caller.
4. Implement the smallest change that preserves UDF, cancellation, lifecycle ownership, and platform isolation.
5. Keep one durable regression test for non-trivial state, paging, mapping, or navigation logic.
6. Run `scripts/check_architecture.sh`, the narrowest affected tests, and the relevant platform compile task.
7. Remove temporary probes, logs, sample tests, generated reports, APKs, screenshots, and build output created during verification.

## Non-negotiable rules

- Put reusable business facts, immutable page models, presenters, and state holders in `commonMain`.
- Keep a shared `Screen` pure: immutable values in, callbacks and platform slots out. It must not create a repository, ViewModel, lifecycle owner, player, or network client.
- Keep Android `Route` and `ViewModel` thin. The ViewModel wraps a shared state holder; it does not duplicate its reducer or state.
- Keep the iOS SwiftUI wrapper thin. The `shared_ios` composition root collects shared state and injects Foundation/UIKit/AVPlayer adapters from `iosMain`; platform APIs stay outside common code.
- Scope Android destination ViewModels to decorated Navigation 3 entries. Do not search for a `ViewModelStoreOwner` through `Context`.
- Create process-wide network clients, repositories, and players only in the platform composition root. `Application` exposes `AppDependencies`; `AppContainer` owns Android implementation wiring.
- Features depend on `domain-feed` contracts, never on `core_data` implementations.
- Keep `domain-feed` free of URLs, Retrofit, OkHttp, serialization annotations, Android types, and platform SDKs. Pagination exposes an opaque `continuationToken`.
- Keep `commonMain` free of Android lifecycle, Media3, Coil, Retrofit, OkHttp, and platform SDK imports. `androidx.compose.*` is portable and allowed.
- Keep HTTP engines in `core_network`: its `commonMain` owns portable request/error/policy APIs, `androidMain` owns OkHttp, and `iosMain` owns Foundation URLSession. `core_data` owns backend DTOs, JSON parsing, URL policy, and domain mapping only.
- Preserve coroutine cancellation. Rethrow `CancellationException`; cancellable platform calls must cancel their underlying work when possible.
- Keep top-level destinations in `AppTopLevelDestination` and route mutations in `AppNavigationState`. Android `AppNavHost` maps them to Navigation 3 entries and installs saveable-state and ViewModel-store decorators.
- Put user-visible Android text in resources and provide meaningful accessibility labels for non-decorative content.
- Use `VideoPlayerFactory` from the application graph and Media3 Compose `ContentFrame`; do not embed `PlayerView` with `AndroidView`.
- Deny cleartext and backup by default. Keep the Gradle Wrapper on HTTPS with a SHA-256 pin. Never add trust-all TLS, unsafe hostname verification, secrets, or broad cleartext exceptions.
- Do not add Desktop or Web network/player dependencies before a real host requires them. Portable shared modules must still compile for Android, iOS, Desktop, and Wasm.
- Keep page models, presenters, state holders, and Screens inside their owning Feature module. Do not recreate `*_shared`, token-only, contract-only, or navigation-UI-only modules without an independent consumer boundary.
- The underscore module names are historical public build paths. Do not rename modules merely for style; rename only as an explicit migration with all consumers and CI updated together.
- Prefer deletion over compatibility layers. Remove unused templates, dependencies, helpers, source sets, and generated artifacts after proving they have no callers.

## Target contract

- Android app: complete CMP host, `core_network` OkHttp driver, Coil, and Media3 player.
- iOS app: thin SwiftUI wrapper around the `shared_ios` CMP host, `core_network` Foundation driver, and AVPlayer.
- Desktop/Wasm: shared UI and state compile targets only until executable hosts are requested.
- `core_network`: Android/iOS transport module with portable policies and platform engines; it is intentionally not a Desktop/Wasm target.
- `core_data`: Android/iOS backend adapter with shared DTO parsing/mapping; it is intentionally not a Desktop/Wasm target.
- `feature_media`: portable Shorts/Detail Screens and state plus Android Route/player adapters.
- `core_player`: Android Media3 implementation module.

## Completion gate

Do not call work complete while the architecture checker, affected tests, Android Debug assembly, or an affected target compile fails. Run `scripts/verify_scaffold.sh` for the full local delivery gate. If an environment prevents a check, report the exact command and failure.
