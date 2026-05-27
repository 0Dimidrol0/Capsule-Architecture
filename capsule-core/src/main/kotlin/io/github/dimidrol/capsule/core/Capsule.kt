package io.github.dimidrol.capsule.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Public contract for a feature [Capsule].
 *
 * A capsule owns the feature runtime and exposes immutable [state] and one-shot [effects].
 */
interface Capsule<Intent, State, Effect> {
    /** Current feature state stream. */
    val state: StateFlow<State>

    /** One-shot side effects stream (navigation, snackbars, etc.). */
    val effects: Flow<Effect>

    /** Sends a new user [intent] to the capsule runtime. */
    fun send(intent: Intent)
}
