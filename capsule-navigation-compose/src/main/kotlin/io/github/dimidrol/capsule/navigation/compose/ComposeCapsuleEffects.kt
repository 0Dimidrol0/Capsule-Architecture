package io.github.dimidrol.capsule.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * Remembers [CapsuleNavigator] instance bound to current [navController].
 */
@Composable
fun rememberCapsuleNavigator(navController: NavController): CapsuleNavigator = remember(navController) {
    ComposeCapsuleNavigator(navController)
}

/**
 * Collects [effects] in a lifecycle-safe way.
 */
@Composable
fun <Effect> HandleCapsuleEffects(
    effects: Flow<Effect>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    handler: suspend (Effect) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(effects, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            effects.collect { effect ->
                handler(effect)
            }
        }
    }
}

/**
 * Collects and handles [CapsuleNavCommand] effects via [navigator].
 */
@Composable
fun HandleNavigationEffects(
    effects: Flow<CapsuleNavCommand>,
    navigator: CapsuleNavigator,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
) {
    HandleCapsuleEffects(
        effects = effects,
        minActiveState = minActiveState,
        handler = navigator::handle
    )
}
