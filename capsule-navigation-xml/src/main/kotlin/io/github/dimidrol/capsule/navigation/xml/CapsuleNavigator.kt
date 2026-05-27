package io.github.dimidrol.capsule.navigation.xml

/**
 * Command handler abstraction for XML navigation.
 */
fun interface CapsuleNavigator {
    fun handle(command: CapsuleNavCommand)
}
