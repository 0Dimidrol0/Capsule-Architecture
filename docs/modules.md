# Modules

## Library Modules

- `capsule-core`: pure Kotlin runtime contracts and base runtime.
- `capsule-base-viewmodel`: thin Android ViewModel shell for Capsule features.
- `capsule-base-fragment-xml`: lifecycle-safe Fragment/XML base for Capsule screens.
- `capsule-middleware`: ready-to-use middleware implementations.
- `capsule-network`: network-aware runtime primitives and Android monitor.
- `capsule-navigation-compose`: Compose-friendly effect navigation helpers.
- `capsule-navigation-xml`: Fragment/XML navigation helpers.

## Samples

- `samples/sample-compose`: working Compose login feature.
- `samples/sample-xml`: Fragment/XML login skeleton.
- `samples/sample-full`: all major modules wired together.

## Dependency Rules

Allowed:

```text
capsule-core
    |
    +-- capsule-base-viewmodel
    +-- capsule-base-fragment-xml
    +-- capsule-middleware
    +-- capsule-network
    +-- capsule-navigation-compose
    +-- capsule-navigation-xml
```

Not allowed:

- `capsule-navigation-compose -> capsule-network`
- `capsule-network -> capsule-middleware`
- `capsule-middleware -> capsule-navigation-xml`

Sample apps may combine any modules.
