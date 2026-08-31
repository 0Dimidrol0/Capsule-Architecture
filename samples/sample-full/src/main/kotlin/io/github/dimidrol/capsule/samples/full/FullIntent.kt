package io.github.dimidrol.capsule.samples.full

sealed interface FullIntent {
    data object RefreshClicked : FullIntent
}
