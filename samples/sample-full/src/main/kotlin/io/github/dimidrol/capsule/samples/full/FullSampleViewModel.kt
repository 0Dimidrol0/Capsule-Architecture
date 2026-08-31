package io.github.dimidrol.capsule.samples.full

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.dimidrol.capsule.core.CapsuleConfig
import io.github.dimidrol.capsule.middleware.DebugTimelineMiddleware
import io.github.dimidrol.capsule.middleware.LoggingMiddleware
import io.github.dimidrol.capsule.middleware.StateHistoryMiddleware
import io.github.dimidrol.capsule.middleware.TimingMiddleware
import io.github.dimidrol.capsule.network.AndroidNetworkMonitor
import io.github.dimidrol.capsule.network.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class FullSampleViewModel(application: Application) : AndroidViewModel(application) {
    private val loggingMiddleware =
        LoggingMiddleware<FullIntent, FullState, FullOperation, FullResult, FullEffect> { message ->
            Log.d("CapsuleSample", message)
        }
    private val timingMiddleware =
        TimingMiddleware<FullIntent, FullState, FullOperation, FullResult, FullEffect>(
            nowMillis = { System.currentTimeMillis() },
            log = { message -> Log.d("CapsuleSample", message) }
        )
    private val stateHistoryMiddleware =
        StateHistoryMiddleware<FullIntent, FullState, FullOperation, FullResult, FullEffect>(
            maxSize = 50
        )
    private val debugTimelineMiddleware =
        DebugTimelineMiddleware<FullIntent, FullState, FullOperation, FullResult, FullEffect>(
            maxEvents = 300
        )
    private val networkMonitor = AndroidNetworkMonitor(
        context = application.applicationContext,
        scope = viewModelScope
    )
    private val capsule = FullCapsule(
        scope = viewModelScope,
        config = CapsuleConfig(
            middlewares = listOf(
                loggingMiddleware,
                timingMiddleware,
                stateHistoryMiddleware,
                debugTimelineMiddleware
            )
        )
    )

    val state: StateFlow<FullState> = capsule.state
    val effects: Flow<FullEffect> = capsule.effects
    val stateHistory = stateHistoryMiddleware.history
    val debugTimeline = debugTimelineMiddleware.timeline
    val networkState: StateFlow<NetworkState> = networkMonitor.state

    fun send(intent: FullIntent) {
        capsule.send(intent)
    }
}
