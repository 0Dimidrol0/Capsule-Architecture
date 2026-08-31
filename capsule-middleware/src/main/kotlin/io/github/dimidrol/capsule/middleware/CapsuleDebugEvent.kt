package io.github.dimidrol.capsule.middleware

/** Debug timeline event for capsule runtime observation. */
sealed interface CapsuleDebugEvent {
    data class IntentReceived(val intent: Any?) : CapsuleDebugEvent

    data class StateChanged(val oldState: Any?, val newState: Any?) : CapsuleDebugEvent

    data class OperationStarted(val operation: Any?) : CapsuleDebugEvent

    data class OperationFinished(val operation: Any?, val result: Any?) : CapsuleDebugEvent

    data class EffectEmitted(val effect: Any?) : CapsuleDebugEvent

    data class ErrorCaught(val operation: Any?, val throwable: Throwable) : CapsuleDebugEvent
}
