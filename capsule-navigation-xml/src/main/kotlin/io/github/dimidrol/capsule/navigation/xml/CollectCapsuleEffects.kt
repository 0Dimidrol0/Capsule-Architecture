package io.github.dimidrol.capsule.navigation.xml

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Collects capsule [effects] using [Lifecycle.repeatOnLifecycle].
 */
fun <Effect> Fragment.collectCapsuleEffects(
    effects: Flow<Effect>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    handler: suspend (Effect) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
            effects.collect { effect ->
                handler(effect)
            }
        }
    }
}

/**
 * Collects [CapsuleNavCommand] and dispatches them via [navigator].
 */
fun Fragment.collectCapsuleNavigation(
    effects: Flow<CapsuleNavCommand>,
    navigator: CapsuleNavigator,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
) {
    collectCapsuleEffects(
        effects = effects,
        minActiveState = minActiveState,
        handler = navigator::handle
    )
}
