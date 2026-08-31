package io.github.dimidrol.capsule.debug

/** Configuration for state history storage and the Android debug overlay. */
data class CapsuleDebugConfig(
    val maxSnapshotsPerScreen: Int = 50,
    val maxRenderedStateLength: Int = 600,
    val floatingButtonText: String = "C",
    val stateFormatter: (Any?) -> String = { state -> state.toString() }
) {
    init {
        require(maxSnapshotsPerScreen > 0) { "maxSnapshotsPerScreen must be greater than zero" }
        require(maxRenderedStateLength > 0) { "maxRenderedStateLength must be greater than zero" }
        require(floatingButtonText.isNotBlank()) { "floatingButtonText must not be blank" }
    }
}
