package io.github.dimidrol.capsule.middleware

import io.github.dimidrol.capsule.core.CapsuleMiddleware
import java.util.concurrent.ConcurrentHashMap

/**
 * Measures operation execution time and logs duration.
 */
class TimingMiddleware<Intent, State, Operation, Result, Effect>(
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
    private val log: (String) -> Unit = {}
) : CapsuleMiddleware<Intent, State, Operation, Result, Effect> {

    private val startTimes = ConcurrentHashMap<Operation, Long>()

    override suspend fun onOperationStarted(operation: Operation) {
        startTimes[operation] = nowMillis()
    }

    override suspend fun onOperationResult(operation: Operation, result: Result) {
        val startTime = startTimes.remove(operation) ?: return
        val durationMs = nowMillis() - startTime
        log("[Capsule] Operation duration: operation=$operation, durationMs=$durationMs")
    }

    override suspend fun onError(operation: Operation?, throwable: Throwable) {
        operation?.let { failedOperation ->
            val startTime = startTimes.remove(failedOperation) ?: return
            val durationMs = nowMillis() - startTime
            log(
                "[Capsule] Operation failed after: operation=$failedOperation, durationMs=$durationMs, message=${throwable.message}"
            )
        }
    }
}
