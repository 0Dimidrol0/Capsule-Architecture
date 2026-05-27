package io.github.dimidrol.capsule.samples.full

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.dimidrol.capsule.core.CapsuleConfig
import io.github.dimidrol.capsule.core.CapsuleRuntime
import io.github.dimidrol.capsule.core.Decision
import io.github.dimidrol.capsule.core.OperationState
import io.github.dimidrol.capsule.middleware.DebugTimelineMiddleware
import io.github.dimidrol.capsule.middleware.LoggingMiddleware
import io.github.dimidrol.capsule.middleware.StateHistoryMiddleware
import io.github.dimidrol.capsule.middleware.TimingMiddleware
import io.github.dimidrol.capsule.navigation.compose.HandleCapsuleEffects
import io.github.dimidrol.capsule.network.AndroidNetworkMonitor
import io.github.dimidrol.capsule.network.NetworkState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class FullSampleViewModel(application: Application) : AndroidViewModel(application) {

    private val loggingMiddleware = LoggingMiddleware<
        FullIntent,
        FullState,
        FullOperation,
        FullResult,
        FullEffect
    > { message -> Log.d("CapsuleSample", message) }

    private val timingMiddleware = TimingMiddleware<
        FullIntent,
        FullState,
        FullOperation,
        FullResult,
        FullEffect
    >(
        nowMillis = { System.currentTimeMillis() },
        log = { message -> Log.d("CapsuleSample", message) }
    )

    private val stateHistoryMiddleware = StateHistoryMiddleware<
        FullIntent,
        FullState,
        FullOperation,
        FullResult,
        FullEffect
    >(maxSize = 50)

    private val debugTimelineMiddleware = DebugTimelineMiddleware<
        FullIntent,
        FullState,
        FullOperation,
        FullResult,
        FullEffect
    >(maxEvents = 300)

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

class FullCapsule(
    scope: kotlinx.coroutines.CoroutineScope,
    config: CapsuleConfig<FullIntent, FullState, FullOperation, FullResult, FullEffect>
) : CapsuleRuntime<FullIntent, FullState, FullOperation, FullResult, FullEffect>(
    initialState = FullState(),
    scope = scope,
    config = config
) {
    override fun reduce(
        state: FullState,
        intent: FullIntent
    ): Decision<FullState, FullOperation, FullEffect> = when (intent) {
        FullIntent.RefreshClicked -> Decision.operation(
            state.copy(operationState = OperationState.Running),
            FullOperation.LoadDashboard
        )
    }

    override suspend fun handleOperation(operation: FullOperation): FullResult {
        delay(400)
        return FullResult.Loaded("Capsule runtime operational")
    }

    override fun reduceResult(
        state: FullState,
        result: FullResult
    ): Decision<FullState, FullOperation, FullEffect> = when (result) {
        is FullResult.Loaded -> Decision.effect(
            state = state.copy(
                title = result.title,
                operationState = OperationState.Success(result.title)
            ),
            effect = FullEffect.ShowMessage("Data refreshed")
        )
    }
}

sealed interface FullIntent {
    data object RefreshClicked : FullIntent
}

data class FullState(
    val title: String = "Press Refresh to run operation",
    val operationState: OperationState<String> = OperationState.Idle
)

sealed interface FullOperation {
    data object LoadDashboard : FullOperation
}

sealed interface FullResult {
    data class Loaded(val title: String) : FullResult
}

sealed interface FullEffect {
    data class ShowMessage(val message: String) : FullEffect
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSampleScreen(viewModel: FullSampleViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.stateHistory.collectAsStateWithLifecycle()
    val timeline by viewModel.debugTimeline.collectAsStateWithLifecycle()
    val network by viewModel.networkState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    HandleCapsuleEffects(viewModel.effects) { effect ->
        when (effect) {
            is FullEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = state.title)
            Text(text = "Operation state: ${state.operationState}")
            Text(text = "Network state: $network")
            Text(text = "State history size: ${history.size}")
            Text(text = "Timeline size: ${timeline.size}")

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.send(FullIntent.RefreshClicked) }
            ) {
                Text("Refresh")
            }
        }
    }
}
