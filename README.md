# Capsule Architecture

Capsule is a runtime-aware architecture for Android features.

It is not another MVI.

Capsule is a feature runtime architecture where each feature is an isolated runtime capsule.

`Feature Capsule = Intent + State + Operation + Result + Effect + Policies + Middleware + Runtime`

## Why Capsule?

Most Android teams eventually hit the same issues:

1. God ViewModel
2. Async logic chaos
3. Repeated loading/error/success/retry boilerplate
4. Implicit side effects
5. Complex retry/cancel/lifecycle/network control
6. Weak runtime debugging visibility
7. Hard feature extension with policies/middleware
8. Missing base for future device-aware behavior

Capsule addresses these with an explicit feature runtime.

## Problems It Solves

1. Moves feature logic out of ViewModel into a dedicated runtime.
2. Makes operations and results first-class concepts.
3. Standardizes operation state via `OperationState`.
4. Treats effects as explicit output streams.
5. Adds middleware hooks for runtime observability and behavior policies.
6. Enables future network/device-aware execution.

## Core Concepts

- `UI`: renders `State`, sends `Intent`.
- `ViewModel`: Android lifecycle shell only.
- `Capsule`: runtime decision engine.
- `Operation`: async/side-effect work command.
- `OperationHandler`: executes operation and maps to `Result`.
- `Middleware`: runtime instrumentation and extensions.
- `Policy`: operation constraints (network/retry/etc).

Main flow:

`UI -> ViewModel -> Capsule -> Operation -> OperationHandler -> Repository/API/DB -> Result -> Capsule -> State/Effect -> UI`

## Module Structure

```text
capsule-core
    ^
    +-- capsule-base-viewmodel
    +-- capsule-base-fragment-xml
    +-- capsule-debug
    +-- capsule-middleware
    +-- capsule-network
    +-- capsule-navigation-compose
    +-- capsule-navigation-xml

samples/sample-compose
samples/sample-xml
samples/sample-full
docs
```

Dependency rule:

- Extra library modules depend only on `capsule-core`.
- Extra library modules do not depend on each other.

## Installation

Current version: `0.1.0-SNAPSHOT`

Maven Central group: `io.github.0dimidrol0`. Kotlin packages and Android namespaces remain under
`io.github.dimidrol.capsule` because a Java/Kotlin package segment cannot start with a digit.
Release tags publish every library artifact independently. See [Publishing](docs/publishing.md) for
the required GitHub secrets and release flow.

```kotlin
dependencies {
    implementation("io.github.0dimidrol0:capsule-core:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-base-viewmodel:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-base-fragment-xml:0.1.0-SNAPSHOT")
    debugImplementation("io.github.0dimidrol0:capsule-debug:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-middleware:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-network:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-navigation-compose:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-navigation-xml:0.1.0-SNAPSHOT")
}
```

## Quick Start

```kotlin
class FeatureCapsule(
    scope: CoroutineScope
) : CapsuleRuntime<FeatureIntent, FeatureState, FeatureOperation, FeatureResult, FeatureEffect>(
    initialState = FeatureState(),
    scope = scope,
    config = CapsuleConfig()
) {
    override fun reduce(state: FeatureState, intent: FeatureIntent): Decision<FeatureState, FeatureOperation, FeatureEffect> {
        TODO()
    }

    override suspend fun handleOperation(operation: FeatureOperation): FeatureResult {
        TODO()
    }

    override fun reduceResult(state: FeatureState, result: FeatureResult): Decision<FeatureState, FeatureOperation, FeatureEffect> {
        TODO()
    }
}
```

## Login Sample

See Compose sample login feature:

- `samples/sample-compose/login/LoginIntent.kt`
- `samples/sample-compose/login/LoginState.kt`
- `samples/sample-compose/login/LoginOperation.kt`
- `samples/sample-compose/login/LoginResult.kt`
- `samples/sample-compose/login/LoginEffect.kt`
- `samples/sample-compose/login/LoginCapsule.kt`
- `samples/sample-compose/login/LoginOperationHandler.kt`
- `samples/sample-compose/login/LoginViewModel.kt`
- `samples/sample-compose/login/LoginScreen.kt`

## Middleware

Available module: `capsule-middleware`

- `LoggingMiddleware`
- `TimingMiddleware`
- `StateHistoryMiddleware`
- `DebugTimelineMiddleware`

## Debug State Time Travel

`capsule-debug` discovers active `BaseCapsuleViewModel` screens, keeps a bounded history of their
live states, and exposes an in-app floating inspector. Selecting a snapshot previews it in the UI
without rewriting the Capsule runtime. Press `LIVE`, or send the next intent, to return to the
latest runtime state.

Install it from a debug-only `Application` source set:

```kotlin
class DebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CapsuleDebugService.install(this)
    }
}
```

No overlay permission is required. See [Debugging](docs/debugging.md) for setup and behavior.
The floating button can be dragged anywhere inside the current Activity and keeps its relative
position when the screen or orientation changes.

## Network-Aware Operations

Available module: `capsule-network`

- `NetworkState`
- `NetworkType`
- `NetworkMonitor`
- `NetworkPolicy`
- `AndroidNetworkMonitor`
- `awaitAvailable()` helper

## Compose Navigation

Available module: `capsule-navigation-compose`

- `CapsuleNavCommand`
- `CapsuleNavigator`
- `ComposeCapsuleNavigator`
- `rememberCapsuleNavigator(navController)`
- `HandleCapsuleEffects(...)`

## XML Navigation

Available module: `capsule-navigation-xml`

- `FragmentCapsuleNavigator`
- `ActivityCapsuleNavigator`
- `collectCapsuleEffects(...)`

## License

Apache License 2.0. See [LICENSE](LICENSE).
