# Getting Started

## 1. Add Dependency

```kotlin
dependencies {
    implementation("io.github.0dimidrol0:capsule-core:0.1.0-SNAPSHOT")
    implementation("io.github.0dimidrol0:capsule-base-viewmodel:0.1.0-SNAPSHOT")
}
```

For Fragment/XML screens you can also add:

```kotlin
dependencies {
    implementation("io.github.0dimidrol0:capsule-base-fragment-xml:0.1.0-SNAPSHOT")
}
```

For debug-only state history and time travel:

```kotlin
dependencies {
    debugImplementation("io.github.0dimidrol0:capsule-debug:0.1.0-SNAPSHOT")
}
```

## 2. Define Feature Contract

- `Intent`
- `State`
- `Operation`
- `Result`
- `Effect`

## 3. Implement CapsuleRuntime

Create a capsule by extending `CapsuleRuntime` and implementing:

- `reduce(state, intent)`
- `handleOperation(operation)`
- `reduceResult(state, result)`

## 4. Keep ViewModel Thin

ViewModel should only:

- create capsule
- pass `viewModelScope`
- expose `state` and `effects`
- forward `send(intent)`
- use `SavedStateHandle`

## 5. Plug Middleware

Pass middleware through `CapsuleConfig` for logging, timing, state history, and debug timeline.

## 6. Add Policy Modules

Optionally add `capsule-network` for network-aware execution behavior.

## 7. Add Debug State Time Travel

Install `CapsuleDebugService` in a debug-only `Application`. Active `BaseCapsuleViewModel`
instances are then tracked automatically and appear in the floating state inspector.
