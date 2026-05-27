package io.github.dimidrol.capsule.core

/**
 * Runtime middleware hook points for observing and extending capsule execution.
 */
interface CapsuleMiddleware<Intent, State, Operation, Result, Effect> {
    suspend fun onIntent(intent: Intent) = Unit

    suspend fun onStateChanged(
        oldState: State,
        newState: State
    ) = Unit

    suspend fun onOperationStarted(operation: Operation) = Unit

    suspend fun onOperationResult(
        operation: Operation,
        result: Result
    ) = Unit

    suspend fun onEffect(effect: Effect) = Unit

    suspend fun onError(
        operation: Operation?,
        throwable: Throwable
    ) = Unit
}
