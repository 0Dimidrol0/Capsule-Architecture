package io.github.dimidrol.capsule.middleware

import io.github.dimidrol.capsule.core.CapsuleMiddleware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores a timeline of runtime events for debugging.
 */
class DebugTimelineMiddleware<Intent, State, Operation, Result, Effect>(
    private val maxEvents: Int = 200
) : CapsuleMiddleware<Intent, State, Operation, Result, Effect> {

    private val mutableTimeline = MutableStateFlow<List<CapsuleDebugEvent>>(emptyList())

    val timeline: StateFlow<List<CapsuleDebugEvent>> = mutableTimeline.asStateFlow()

    override suspend fun onIntent(intent: Intent) {
        append(CapsuleDebugEvent.IntentReceived(intent))
    }

    override suspend fun onStateChanged(oldState: State, newState: State) {
        append(CapsuleDebugEvent.StateChanged(oldState = oldState, newState = newState))
    }

    override suspend fun onOperationStarted(operation: Operation) {
        append(CapsuleDebugEvent.OperationStarted(operation))
    }

    override suspend fun onOperationResult(operation: Operation, result: Result) {
        append(CapsuleDebugEvent.OperationFinished(operation = operation, result = result))
    }

    override suspend fun onEffect(effect: Effect) {
        append(CapsuleDebugEvent.EffectEmitted(effect))
    }

    override suspend fun onError(operation: Operation?, throwable: Throwable) {
        append(CapsuleDebugEvent.ErrorCaught(operation = operation, throwable = throwable))
    }

    private fun append(event: CapsuleDebugEvent) {
        val updated = buildList {
            addAll(mutableTimeline.value)
            add(event)
        }.takeLast(maxEvents)
        mutableTimeline.value = updated
    }
}
