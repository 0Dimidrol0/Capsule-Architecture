# Comparison

## MVVM

- Good at lifecycle-aware state holders.
- Often grows into God ViewModel when feature complexity rises.

## MVI

- Strong intent/state modeling.
- Does not always standardize operation runtime and policy hooks.

## Clean Architecture

- Strong layer boundaries and use-case separation.
- Not a runtime execution model by itself.

## MVP

- Explicit presenter control flow.
- More manual lifecycle handling and stream orchestration.

## MVC

- Simple and familiar for small screens.
- Tends to mix concerns in larger features.

## Capsule

Capsule is a feature runtime architecture.

It adds explicit runtime components:

- operation lifecycle
- result pipeline
- effect stream
- middleware hooks
- runtime policies (network now, device-aware later)
