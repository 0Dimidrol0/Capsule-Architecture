package io.github.dimidrol.capsule.samples.xml.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import io.github.dimidrol.capsule.core.CapsuleConfig
import io.github.dimidrol.capsule.navigation.xml.FragmentCapsuleNavigator
import io.github.dimidrol.capsule.navigation.xml.collectCapsuleEffects

import kotlinx.coroutines.launch

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseCapsuleFragment<
        ViewModel : BaseViewModel<Capsule, Intent, State, Operation, Result, Effect>,
        Binding : ViewBinding,
        Capsule : CapsuleConfig<Intent, State, Operation, Result, Effect>,
        Intent, State, Operation, Result, Effect>(
    private val inflate: Inflate<Binding>,
) : Fragment() {

    protected abstract val viewModel: ViewModel

    private var _binding: Binding? = null
    protected val binding get() = requireNotNull(_binding)

    protected val navigator by lazy { FragmentCapsuleNavigator(findNavController()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        navigator // create navigator
        observeEffects()
        observeState()
        return binding.root
    }

    private fun observeEffects() {
        collectCapsuleEffects(viewModel.effects) { onEffect(it) }
    }

    abstract fun onEffect(effect: Effect)

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    abstract fun render(state: State)

}