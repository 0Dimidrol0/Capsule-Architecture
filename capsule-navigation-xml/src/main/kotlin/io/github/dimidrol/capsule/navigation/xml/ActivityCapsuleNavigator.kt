package io.github.dimidrol.capsule.navigation.xml

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

/**
 * Activity-level [CapsuleNavigator] helper for XML apps.
 */
class ActivityCapsuleNavigator(
    private val activity: AppCompatActivity,
    private val navHostFragmentId: Int
) : CapsuleNavigator {

    override fun handle(command: CapsuleNavCommand) {
        val navHost = activity.supportFragmentManager.findFragmentById(navHostFragmentId) as? NavHostFragment
            ?: return

        FragmentCapsuleNavigator(navHost.navController).handle(command)
    }
}
