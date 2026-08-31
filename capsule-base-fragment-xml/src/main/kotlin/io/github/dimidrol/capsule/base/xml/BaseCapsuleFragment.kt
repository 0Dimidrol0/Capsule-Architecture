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
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.launch

/**
 * Lifecycle-safe XML Fragment base for Capsule-driven screens.
 */
abstract class BaseCapsuleFragment<Binding: ViewBinding, State, Effect>(
    private val inflate: InflateBinding<Binding>,
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindIntents()
        observeState()
        observeEffects()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    protected abstract fun bindIntents()

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
