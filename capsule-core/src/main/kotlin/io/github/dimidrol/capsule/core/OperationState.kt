package io.github.dimidrol.capsule.core

/**
 * Unified operation state model used by features.
 */
sealed interface OperationState<out T> {
    data object Idle : OperationState<Nothing>

    data object Running : OperationState<Nothing>

    data class Success<T>(val data: T) : OperationState<T>

    data class Failed(
        val throwable: Throwable,
        val message: String = throwable.message ?: "Unknown error",
        val canRetry: Boolean = true
    ) : OperationState<Nothing>
}
