package io.github.dimidrol.capsule.middleware

import io.github.dimidrol.capsule.core.CapsuleMiddleware

/**
 * Logs runtime events emitted by a capsule.
 */
class LoggingMiddleware<Intent, State, Operation, Result, Effect>(
    private val log: (String) -> Unit
) : CapsuleMiddleware<Intent, State, Operation, Result, Effect> {

    override suspend fun onIntent(intent: Intent) {
        log("[Capsule] Intent: $intent")
    }

    override suspend fun onStateChanged(oldState: State, newState: State) {
        log("[Capsule] State changed: old=$oldState, new=$newState")
    }

    override suspend fun onOperationStarted(operation: Operation) {
        log("[Capsule] Operation started: $operation")
    }

    override suspend fun onOperationResult(operation: Operation, result: Result) {
        log("[Capsule] Operation result: operation=$operation, result=$result")
    }

    override suspend fun onEffect(effect: Effect) {
        log("[Capsule] Effect: $effect")
    }

    override suspend fun onError(operation: Operation?, throwable: Throwable) {
        log("[Capsule] Error: operation=$operation, message=${throwable.message}")
    }
}
