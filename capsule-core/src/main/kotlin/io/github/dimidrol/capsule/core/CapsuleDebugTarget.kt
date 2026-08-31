package io.github.dimidrol.capsule.core

/** A named state host that can be discovered by optional debug tooling. */
interface CapsuleDebugTarget<State> : CapsuleStateTimeTravel<State> {
    val debugName: String
}
