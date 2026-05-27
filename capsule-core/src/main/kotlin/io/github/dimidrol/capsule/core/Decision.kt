package io.github.dimidrol.capsule.core

/**
 * Immutable result of feature decision-making.
 */
data class Decision<State, Operation, Effect>(
    val state: State,
    val operation: Operation? = null,
    val effect: Effect? = null
) {
    companion object {
        fun <State, Operation, Effect> state(state: State): Decision<State, Operation, Effect> =
            Decision(state = state)

        fun <State, Operation, Effect> none(state: State): Decision<State, Operation, Effect> =
            Decision(state = state)

        fun <State, Operation, Effect> operation(
            state: State,
            operation: Operation
        ): Decision<State, Operation, Effect> = Decision(state = state, operation = operation)

        fun <State, Operation, Effect> effect(
            state: State,
            effect: Effect
        ): Decision<State, Operation, Effect> = Decision(state = state, effect = effect)
    }
}
