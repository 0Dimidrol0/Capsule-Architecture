package io.github.dimidrol.capsule.middleware

import io.github.dimidrol.capsule.core.CapsuleMiddleware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Keeps the latest [maxSize] state snapshots for debugging.
 */
class StateHistoryMiddleware<Intent, State, Operation, Result, Effect>(
    private val maxSize: Int = 30
) : CapsuleMiddleware<Intent, State, Operation, Result, Effect> {

    private val mutableHistory = MutableStateFlow<List<State>>(emptyList())

    val history: StateFlow<List<State>> = mutableHistory.asStateFlow()

    override suspend fun onStateChanged(oldState: State, newState: State) {
        val updated = buildList {
            addAll(mutableHistory.value)
            add(newState)
        }.takeLast(maxSize)
        mutableHistory.value = updated
    }
}
