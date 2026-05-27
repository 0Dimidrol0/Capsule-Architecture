package io.github.dimidrol.capsule.samples.xml.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import io.github.dimidrol.capsule.core.OperationState
import io.github.dimidrol.capsule.navigation.xml.CapsuleNavCommand
import io.github.dimidrol.capsule.navigation.xml.FragmentCapsuleNavigator
import io.github.dimidrol.capsule.navigation.xml.collectCapsuleEffects
import io.github.dimidrol.capsule.samples.xml.R
import io.github.dimidrol.capsule.samples.xml.databinding.FragmentLoginBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = requireNotNull(_binding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        bindIntents()
        observeState()
        observeEffects()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindIntents() {
        binding.emailEditText.doAfterTextChanged { text ->
            viewModel.send(LoginIntent.EmailChanged(text?.toString().orEmpty()))
        }
        binding.passwordEditText.doAfterTextChanged { text ->
            viewModel.send(LoginIntent.PasswordChanged(text?.toString().orEmpty()))
        }
        binding.loginButton.setOnClickListener {
            viewModel.send(LoginIntent.SubmitClicked)
        }
        binding.forgotButton.setOnClickListener {
            viewModel.send(LoginIntent.ForgotPasswordClicked)
        }
        binding.registerButton.setOnClickListener {
            viewModel.send(LoginIntent.RegisterClicked)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeEffects() {
        val navigator = FragmentCapsuleNavigator(findNavController())
        collectCapsuleEffects(viewModel.effects) { effect ->
            when (effect) {
                LoginEffect.NavigateToHome -> navigator.handle(CapsuleNavCommand.Navigate(R.id.homeFragment))
                LoginEffect.NavigateToForgotPassword -> navigator.handle(CapsuleNavCommand.Navigate(R.id.forgotFragment))
                LoginEffect.NavigateToRegistration -> navigator.handle(CapsuleNavCommand.Navigate(R.id.registerFragment))
                is LoginEffect.ShowMessage -> Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun render(state: LoginState) {
        if (binding.emailEditText.text?.toString() != state.email) {
            binding.emailEditText.setText(state.email)
        }
        if (binding.passwordEditText.text?.toString() != state.password) {
            binding.passwordEditText.setText(state.password)
        }

        val isLoading = state.loginOperation is OperationState.Running
        binding.progressBar.isVisible = isLoading
        binding.loginButton.isEnabled = !isLoading

        binding.errorText.isVisible = state.loginOperation is OperationState.Failed
        binding.errorText.text = (state.loginOperation as? OperationState.Failed)?.message.orEmpty()
    }
}
