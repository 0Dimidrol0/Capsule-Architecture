package io.github.dimidrol.capsule.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Android implementation of [NetworkMonitor] based on [ConnectivityManager].
 */
class AndroidNetworkMonitor(
    context: Context,
    scope: CoroutineScope
) : NetworkMonitor {

    private val connectivityManager: ConnectivityManager =
        requireNotNull(context.getSystemService()) {
            "ConnectivityManager is not available"
        }

    override val state: StateFlow<NetworkState> = callbackFlow {
        trySend(connectivityManager.currentState())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(connectivityManager.currentState())
            }

            override fun onLost(network: Network) {
                trySend(connectivityManager.currentState())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(networkCapabilities.toNetworkState())
            }

            override fun onUnavailable() {
                trySend(NetworkState.Unavailable)
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = connectivityManager.currentState()
        )
}

private fun ConnectivityManager.currentState(): NetworkState {
    val active = activeNetwork ?: return NetworkState.Unavailable
    val capabilities = getNetworkCapabilities(active) ?: return NetworkState.Unavailable
    return capabilities.toNetworkState()
}

private fun NetworkCapabilities.toNetworkState(): NetworkState = when {
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkState.Connected(NetworkType.Wifi)
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkState.Connected(NetworkType.Cellular)
    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkState.Connected(NetworkType.Ethernet)
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkState.Available
    else -> NetworkState.Connected(NetworkType.Unknown)
}
