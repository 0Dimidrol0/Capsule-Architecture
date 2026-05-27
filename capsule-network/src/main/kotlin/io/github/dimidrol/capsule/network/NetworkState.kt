package io.github.dimidrol.capsule.network

/**
 * Network connection state for policy-aware operations.
 */
sealed interface NetworkState {
    data object Available : NetworkState

    data object Unavailable : NetworkState

    data class Connected(
        val type: NetworkType
    ) : NetworkState
}
