package io.github.dimidrol.capsule.samples.full

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.dimidrol.capsule.navigation.compose.HandleCapsuleEffects

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

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = state.title)
            Text(text = "Operation state: " + state.operationState)
            Text(text = "Network state: " + network)
            Text(text = "State history size: " + history.size)
            Text(text = "Timeline size: " + timeline.size)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.send(FullIntent.RefreshClicked) }
            ) {
                Text("Refresh")
            }
        }
    }
}
