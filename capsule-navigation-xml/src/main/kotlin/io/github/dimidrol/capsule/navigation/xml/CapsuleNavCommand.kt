package io.github.dimidrol.capsule.navigation.xml

import android.os.Bundle
import androidx.navigation.NavOptions

/**
 * Navigation command for Fragment/XML based navigation.
 */
sealed interface CapsuleNavCommand {
    data class Navigate(
        val destinationId: Int,
        val args: Bundle? = null,
        val navOptions: NavOptions? = null
    ) : CapsuleNavCommand

    data object Back : CapsuleNavCommand

    data class PopUpTo(
        val destinationId: Int,
        val inclusive: Boolean = false
    ) : CapsuleNavCommand
}
