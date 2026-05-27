package io.github.dimidrol.capsule.navigation.compose

/**
 * Navigation command emitted as capsule effect.
 */
sealed interface CapsuleNavCommand {
    data class Navigate(val route: String) : CapsuleNavCommand

    data object Back : CapsuleNavCommand

    data class PopUpTo(
        val route: String,
        val inclusive: Boolean = false
    ) : CapsuleNavCommand
}
