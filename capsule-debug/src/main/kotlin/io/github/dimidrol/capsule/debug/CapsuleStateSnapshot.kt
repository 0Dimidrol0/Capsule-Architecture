package io.github.dimidrol.capsule.debug

/** Immutable state snapshot captured from a live Capsule screen. */
data class CapsuleStateSnapshot(
    val id: Long,
    val sequence: Long,
    val capturedAtMillis: Long,
    val renderedState: String,
    val state: Any?
)
