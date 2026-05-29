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

```kotlin
dependencies {
    implementation("io.github.dimidrol:capsule-core:0.1.0-SNAPSHOT")
    implementation("io.github.dimidrol:capsule-base-viewmodel:0.1.0-SNAPSHOT")
    implementation("io.github.dimidrol:capsule-base-fragment-xml:0.1.0-SNAPSHOT")
    implementation("io.github.dimidrol:capsule-middleware:0.1.0-SNAPSHOT")
    implementation("io.github.dimidrol:capsule-network:0.1.0-SNAPSHOT")
    implementation("io.github.dimidrol:capsule-navigation-compose:0.1.0-SNAPSHOT")
    implementation("io.github.dimidrol:capsule-navigation-xml:0.1.0-SNAPSHOT")
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

- `samples/sample-compose/login/LoginContract.kt`
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

## Roadmap

### 0.1.0

- `capsule-core`
- base runtime
- operation state
- middleware interface
- compose/xml samples

### 0.2.0

- better policies
- retry/cancel policies
- improved testing DSL

### 0.3.0

- devtools/debug timeline
- state history
- time travel experiments

### 0.4.0

- DeviceMonitor integration
- thermal/memory/battery policies

### 0.5.0

- real sample app
- documentation website
- Maven Central release polish

## License

Apache License 2.0. See [LICENSE](LICENSE).
