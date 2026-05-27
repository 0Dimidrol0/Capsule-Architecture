package io.github.dimidrol.capsule.network

import kotlinx.coroutines.flow.first

/**
 * Suspends until network becomes available.
 */
suspend fun NetworkMonitor.awaitAvailable() {
    state.first { networkState ->
        when (networkState) {
            NetworkState.Available -> true
            is NetworkState.Connected -> true
            NetworkState.Unavailable -> false
        }
    }
}

/**
 * Returns true if operation can be executed with the current [policy].
 */
suspend fun NetworkPolicy.canExecute(
    monitor: NetworkMonitor,
    attempt: Int = 1
): Boolean = when (this) {
    NetworkPolicy.Ignore -> true
    NetworkPolicy.RequireConnection -> monitor.state.value != NetworkState.Unavailable
    NetworkPolicy.WaitForConnection -> {
        monitor.awaitAvailable()
        true
    }

    is NetworkPolicy.RetryOnReconnect -> {
        if (attempt > maxAttempts) {
            false
        } else {
            monitor.awaitAvailable()
            true
        }
    }
}
