package io.github.dimidrol.capsule.core

import kotlinx.coroutines.flow.StateFlow

/**
 * Debug-safe state time-travel contract.
 *
 * Previewing a state changes only the state presented to the UI. The Capsule runtime continues
 * processing its live state, so in-flight operations and results are never rewritten by debug UI.
 */
interface CapsuleStateTimeTravel<State> {
    /** State produced by the Capsule runtime, unaffected by preview mode. */
    val liveState: StateFlow<State>

    /** Current presentation mode. */
    val stateMode: StateFlow<CapsuleStateMode>

    /** Presents [state] to the UI without mutating the Capsule runtime. */
    fun previewState(state: State)

    /** Leaves preview mode and immediately presents the latest [liveState]. */
    fun resumeLiveState()
}
