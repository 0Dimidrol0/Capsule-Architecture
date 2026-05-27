# Architecture

Capsule Architecture is a runtime-first approach for Android features.

## Positioning

- MVVM answers: where to keep UI state.
- MVI answers: intent -> state transition.
- Clean Architecture answers: layer separation.
- Capsule answers: feature runtime orchestration.

Capsule focuses on operations, effects, middleware hooks, and runtime policies.

## Formula

`Feature Capsule = Intent + State + Operation + Result + Effect + Policies + Middleware + Runtime`

## Responsibilities

- UI: renders state and sends intents.
- ViewModel: Android shell for lifecycle and scope.
- Capsule: decisions, operation triggering, state/effect output.
- OperationHandler: side effects and async work.
- Repository: data access.
- Mapper: data transformation.
- Middleware: observability and runtime extension points.

## Runtime Flow

`UI -> ViewModel -> Capsule -> Operation -> OperationHandler -> Repository/API/DB -> Result -> Capsule -> State/Effect -> UI`
