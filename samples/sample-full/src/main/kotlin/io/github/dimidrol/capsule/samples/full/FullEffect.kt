package io.github.dimidrol.capsule.samples.full

sealed interface FullEffect {
    data class ShowMessage(val message: String) : FullEffect
}
