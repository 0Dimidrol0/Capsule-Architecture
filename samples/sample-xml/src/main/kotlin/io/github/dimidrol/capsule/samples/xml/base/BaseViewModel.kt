package io.github.dimidrol.capsule.samples.xml.base

import androidx.lifecycle.ViewModel
import io.github.dimidrol.capsule.core.CapsuleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

abstract class BaseViewModel<
        Capsule : CapsuleConfig<Intent, State, Operation, Result, Effect>,
        Intent, State, Operation, Result, Effect> : ViewModel() {

    protected abstract val capsule: Capsule

    abstract val state: StateFlow<State>
    abstract val effects: Flow<Effect>

    abstract fun send(intent: Intent)
}