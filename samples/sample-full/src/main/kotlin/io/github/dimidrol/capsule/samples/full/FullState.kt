package io.github.dimidrol.capsule.samples.full

import io.github.dimidrol.capsule.core.OperationState

data class FullState(
    val title: String = "Press Refresh to run operation",
    val operationState: OperationState<String> = OperationState.Idle
)
