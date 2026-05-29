package io.github.dimidrol.capsule.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dimidrol.capsule.core.Capsule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin Android [ViewModel] shell around a feature [Capsule].
 *
 * All feature runtime logic remains inside the capsule.
 */
abstract class BaseCapsuleViewModel<Intent, State, Effect> : ViewModel(), Capsule<Intent, State, Effect> {

    private val capsule: Capsule<Intent, State, Effect> by lazy(LazyThreadSafetyMode.NONE) {
        buildCapsule(viewModelScope)
    }

    final override val state: StateFlow<State>
        get() = capsule.state

    final override val effects: Flow<Effect>
        get() = capsule.effects

    final override fun send(intent: Intent) {
        onBeforeSend(intent)
        capsule.send(intent)
        onAfterSend(intent)
    }

    protected abstract fun buildCapsule(scope: CoroutineScope): Capsule<Intent, State, Effect>

    protected open fun onBeforeSend(intent: Intent) = Unit

    protected open fun onAfterSend(intent: Intent) = Unit
}
