package io.github.dimidrol.capsule.navigation.compose

/**
 * Command handler abstraction used by UI layer.
 */
fun interface CapsuleNavigator {
    fun handle(command: CapsuleNavCommand)
}
