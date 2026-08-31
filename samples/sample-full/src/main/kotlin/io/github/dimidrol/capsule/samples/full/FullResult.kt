package io.github.dimidrol.capsule.samples.full

sealed interface FullResult {
    data class Loaded(val title: String) : FullResult
}
