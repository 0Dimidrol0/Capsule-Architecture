package io.github.dimidrol.capsule.navigation.xml

import androidx.navigation.NavController

/**
 * [CapsuleNavigator] implementation that delegates to [NavController].
 */
class FragmentCapsuleNavigator(
    private val navController: NavController
) : CapsuleNavigator {

    override fun handle(command: CapsuleNavCommand) {
        when (command) {
            CapsuleNavCommand.Back -> navController.popBackStack()
            is CapsuleNavCommand.Navigate -> navController.navigate(
                command.destinationId,
                command.args,
                command.navOptions
            )

            is CapsuleNavCommand.PopUpTo -> navController.popBackStack(
                command.destinationId,
                command.inclusive
            )
        }
    }
}
