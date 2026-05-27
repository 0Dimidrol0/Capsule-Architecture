package io.github.dimidrol.capsule.network

/**
 * Strategy describing how operation should behave relative to network availability.
 */
sealed interface NetworkPolicy {
    data object Ignore : NetworkPolicy

    data object RequireConnection : NetworkPolicy

    data object WaitForConnection : NetworkPolicy

    data class RetryOnReconnect(
        val maxAttempts: Int = 3
    ) : NetworkPolicy
}
