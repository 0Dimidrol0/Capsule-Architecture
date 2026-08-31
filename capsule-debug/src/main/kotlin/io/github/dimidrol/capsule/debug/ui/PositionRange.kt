package io.github.dimidrol.capsule.debug.ui

internal data class PositionRange(
    val start: Float,
    val end: Float
) {
    val length: Float
        get() = end - start

    fun fractionFor(position: Float): Float =
        if (length == 0f) 0.5f else ((position - start) / length).coerceIn(0f, 1f)
}
