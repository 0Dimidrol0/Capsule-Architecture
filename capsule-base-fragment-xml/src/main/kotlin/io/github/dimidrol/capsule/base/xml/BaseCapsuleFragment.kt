package io.github.dimidrol.capsule.base.xml

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/** Inflate binding instance for a fragment view. */
typealias InflateBinding<Binding> = (LayoutInflater, ViewGroup?, Boolean) -> Binding

/** Extracts root [View] from a binding instance. */
typealias BindingRoot<Binding> = (Binding) -> View

/**
 * Lifecycle-safe XML Fragment base for Capsule-driven screens.
 */
abstract class BaseCapsuleFragment<Binding, State, Effect>(
    private val inflate: InflateBinding<Binding>,
    private val bindingRoot: BindingRoot<Binding>,
    private val minActiveState: Lifecycle.State = Lifecycle.State.STARTED
) : Fragment() {

    private var _binding: Binding? = null

    protected val binding: Binding
        get() = requireNotNull(_binding) {
            "Binding is only available between onCreateView and onDestroyView"
        }

    protected abstract val state: StateFlow<State>

    protected abstract val effects: Flow<Effect>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val newBinding = inflate(inflater, container, false)
        _binding = newBinding
        return bindingRoot(newBinding)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBindingCreated(binding)
        observeState()
        observeEffects()
    }

    override fun onDestroyView() {
        onBindingDestroyed(binding)
        _binding = null
        super.onDestroyView()
    }

    protected open fun onBindingCreated(binding: Binding) = Unit

    protected open fun onBindingDestroyed(binding: Binding) = Unit

    protected abstract fun render(state: State)

    protected abstract suspend fun onEffect(effect: Effect)

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
                state.collect { capsuleState ->
                    render(capsuleState)
                }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
                effects.collect { effect ->
                    onEffect(effect)
                }
            }
        }
    }
}
