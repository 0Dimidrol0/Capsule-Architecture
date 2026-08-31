package io.github.dimidrol.capsule.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dimidrol.capsule.core.Capsule
import io.github.dimidrol.capsule.core.CapsuleDebugBridge
import io.github.dimidrol.capsule.core.CapsuleDebugTarget
import io.github.dimidrol.capsule.core.CapsuleStateMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Thin Android [ViewModel] shell around a feature [Capsule].
 *
 * All feature runtime logic remains inside the capsule. Optional debug tooling may preview a
 * historical state through this shell; the capsule's live state remains untouched.
 */
abstract class BaseCapsuleViewModel<Intent, State, Effect> :
    ViewModel(),
    Capsule<Intent, State, Effect>,
    CapsuleDebugTarget<State> {

    private val capsule: Capsule<Intent, State, Effect> by lazy(LazyThreadSafetyMode.NONE) {
        buildCapsule(viewModelScope)
    }

    private val previewState: MutableStateFlow<State> by lazy(LazyThreadSafetyMode.NONE) {
        MutableStateFlow(capsule.state.value)
    }

    private val mutableStateMode = MutableStateFlow(CapsuleStateMode.Live)

    private val presentedState: StateFlow<State> by lazy(LazyThreadSafetyMode.NONE) {
        combine(capsule.state, previewState, mutableStateMode) { liveState, preview, mode ->
            when (mode) {
                CapsuleStateMode.Live -> liveState
                CapsuleStateMode.Preview -> preview
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = capsule.state.value
        )
    }

    private var debugConnectionInitialized = false
    private var debugConnection: AutoCloseable? = null

    final override val state: StateFlow<State>
        get() {
            ensureDebugConnection()
            return presentedState
        }

    final override val liveState: StateFlow<State>
        get() {
            ensureDebugConnection()
            return capsule.state
        }

    final override val stateMode: StateFlow<CapsuleStateMode> = mutableStateMode

    final override val debugName: String
        get() = capsuleDebugName

    final override val effects: Flow<Effect>
        get() = capsule.effects

    final override fun send(intent: Intent) {
        resumeLiveState()
        onBeforeSend(intent)
        capsule.send(intent)
        onAfterSend(intent)
    }

    final override fun previewState(state: State) {
        previewState.value = state
        mutableStateMode.value = CapsuleStateMode.Preview
    }

    final override fun resumeLiveState() {
        mutableStateMode.value = CapsuleStateMode.Live
    }

    /** Name shown by optional Capsule debug tooling. */
    protected open val capsuleDebugName: String
        get() = this::class.java.simpleName

    protected abstract fun buildCapsule(scope: CoroutineScope): Capsule<Intent, State, Effect>

    protected open fun onBeforeSend(intent: Intent) = Unit

    protected open fun onAfterSend(intent: Intent) = Unit

    /** Lifecycle hook for subclasses. Debug resources are already released before this callback. */
    protected open fun onCapsuleViewModelCleared() = Unit

    final override fun onCleared() {
        debugConnection?.close()
        debugConnection = null
        onCapsuleViewModelCleared()
        super.onCleared()
    }

    private fun ensureDebugConnection() {
        if (debugConnectionInitialized) return

        debugConnectionInitialized = true
        debugConnection = CapsuleDebugBridge.connect(this)
    }
}
