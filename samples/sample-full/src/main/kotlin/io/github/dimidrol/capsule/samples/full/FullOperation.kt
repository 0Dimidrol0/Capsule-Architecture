package io.github.dimidrol.capsule.samples.full

sealed interface FullOperation {
    data object LoadDashboard : FullOperation
}
