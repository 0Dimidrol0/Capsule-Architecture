package io.github.dimidrol.capsule.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Observe network state changes.
 */
interface NetworkMonitor {
    val state: StateFlow<NetworkState>
}
