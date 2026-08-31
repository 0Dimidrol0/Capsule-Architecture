package io.github.dimidrol.capsule.samples.full

import io.github.dimidrol.capsule.core.CapsuleConfig
import io.github.dimidrol.capsule.core.CapsuleRuntime
import io.github.dimidrol.capsule.core.Decision
import io.github.dimidrol.capsule.core.OperationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class FullCapsule(
    scope: CoroutineScope,
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
