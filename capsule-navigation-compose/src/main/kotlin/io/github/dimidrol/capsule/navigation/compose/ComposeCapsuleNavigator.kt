package io.github.dimidrol.capsule.navigation.compose

import androidx.navigation.NavController

/**
 * [CapsuleNavigator] implementation backed by [NavController].
 */
class ComposeCapsuleNavigator(
    private val navController: NavController
) : CapsuleNavigator {

    override fun handle(command: CapsuleNavCommand) {
        when (command) {
            CapsuleNavCommand.Back -> navController.popBackStack()
            is CapsuleNavCommand.Navigate -> navController.navigate(command.route)
            is CapsuleNavCommand.PopUpTo -> navController.popBackStack(
                command.route,
                command.inclusive
            )
        }
    }
}
