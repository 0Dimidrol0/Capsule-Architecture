package io.github.dimidrol.capsule.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base runtime for feature capsules.
 *
 * It orchestrates intent processing, state updates, effects, operations, and middleware callbacks.
 */
abstract class CapsuleRuntime<Intent, State, Operation, Result, Effect>(
    initialState: State,
    private val scope: CoroutineScope,
    private val config: CapsuleConfig<Intent, State, Operation, Result, Effect> = CapsuleConfig()
) : Capsule<Intent, State, Effect> {

    private val mutableState = MutableStateFlow(initialState)
    private val effectsChannel = Channel<Effect>(config.effectBufferCapacity)
    private val intentChannel = Channel<Intent>(Channel.UNLIMITED)

    final override val state: StateFlow<State> = mutableState.asStateFlow()
    final override val effects: Flow<Effect> = effectsChannel.receiveAsFlow()

    init {
        scope.launch {
            for (intent in intentChannel) {
                processIntent(intent)
            }
        }
    }

    final override fun send(intent: Intent) {
        if (!intentChannel.trySend(intent).isSuccess) {
            scope.launch {
                intentChannel.send(intent)
            }
        }
    }

    protected abstract fun reduce(
        state: State,
        intent: Intent
    ): Decision<State, Operation, Effect>

    protected abstract suspend fun handleOperation(operation: Operation): Result

    protected abstract fun reduceResult(
        state: State,
        result: Result
    ): Decision<State, Operation, Effect>

    private suspend fun processIntent(intent: Intent) {
        notifyIntent(intent)
        try {
            val decision = reduce(mutableState.value, intent)
            applyDecision(decision)
        } catch (throwable: Throwable) {
            notifyError(operation = null, throwable = throwable)
        }
    }

    private suspend fun processOperation(operation: Operation) {
        scope.launch(config.operationDispatcher) {
            notifyOperationStarted(operation)
            try {
                val result = handleOperation(operation)
                withContext(scope.coroutineContext) {
                    notifyOperationResult(operation, result)
                    try {
                        val decision = reduceResult(mutableState.value, result)
                        applyDecision(decision)
                    } catch (throwable: Throwable) {
                        notifyError(operation = operation, throwable = throwable)
                    }
                }
            } catch (throwable: Throwable) {
                withContext(scope.coroutineContext) {
                    notifyError(operation = operation, throwable = throwable)
                }
            }
        }
    }

    private suspend fun applyDecision(decision: Decision<State, Operation, Effect>) {
        updateState(decision.state)
        decision.effect?.let { effect ->
            notifyEffect(effect)
            effectsChannel.send(effect)
        }
        decision.operation?.let { operation ->
            processOperation(operation)
        }
    }

    private suspend fun updateState(newState: State) {
        val oldState = mutableState.value
        mutableState.value = newState
        if (oldState != newState) {
            notifyStateChanged(oldState = oldState, newState = newState)
        }
    }

    private suspend fun notifyIntent(intent: Intent) {
        config.middlewares.forEach { middleware ->
            safeMiddlewareCall { middleware.onIntent(intent) }
        }
    }

    private suspend fun notifyStateChanged(oldState: State, newState: State) {
        config.middlewares.forEach { middleware ->
            safeMiddlewareCall { middleware.onStateChanged(oldState, newState) }
        }
    }

    private suspend fun notifyOperationStarted(operation: Operation) {
        config.middlewares.forEach { middleware ->
            safeMiddlewareCall { middleware.onOperationStarted(operation) }
        }
    }

    private suspend fun notifyOperationResult(operation: Operation, result: Result) {
        config.middlewares.forEach { middleware ->
            safeMiddlewareCall { middleware.onOperationResult(operation, result) }
        }
    }

    private suspend fun notifyEffect(effect: Effect) {
        config.middlewares.forEach { middleware ->
            safeMiddlewareCall { middleware.onEffect(effect) }
        }
    }

    private suspend fun notifyError(operation: Operation?, throwable: Throwable) {
        config.middlewares.forEach { middleware ->
            safeMiddlewareCall { middleware.onError(operation, throwable) }
        }
    }

    private suspend fun safeMiddlewareCall(block: suspend () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                throw throwable
            }
        }
    }
}
