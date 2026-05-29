package io.github.dimidrol.capsule.samples.xml.login

import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.github.dimidrol.capsule.base.xml.BaseCapsuleFragment
import io.github.dimidrol.capsule.core.OperationState
import io.github.dimidrol.capsule.navigation.xml.CapsuleNavCommand
import io.github.dimidrol.capsule.navigation.xml.FragmentCapsuleNavigator
import io.github.dimidrol.capsule.samples.xml.R
import io.github.dimidrol.capsule.samples.xml.databinding.FragmentLoginBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class LoginFragment : BaseCapsuleFragment<FragmentLoginBinding, LoginState, LoginEffect>(
    inflate = FragmentLoginBinding::inflate,
    bindingRoot = { it.root }
) {

    private val viewModel: LoginViewModel by viewModels()

    private val navigator by lazy(LazyThreadSafetyMode.NONE) {
        FragmentCapsuleNavigator(findNavController())
    }

    override val state: StateFlow<LoginState>
        get() = viewModel.state

    override val effects: Flow<LoginEffect>
        get() = viewModel.effects

    override fun onBindingCreated(binding: FragmentLoginBinding) {
        super.onBindingCreated(binding)
        bindIntents()
    }

    override suspend fun onEffect(effect: LoginEffect) {
        when (effect) {
            LoginEffect.NavigateToHome -> navigator.handle(CapsuleNavCommand.Navigate(R.id.homeFragment))
            LoginEffect.NavigateToForgotPassword -> navigator.handle(CapsuleNavCommand.Navigate(R.id.forgotFragment))
            LoginEffect.NavigateToRegistration -> navigator.handle(CapsuleNavCommand.Navigate(R.id.registerFragment))
            is LoginEffect.ShowMessage -> {
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun render(state: LoginState) {
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
}
