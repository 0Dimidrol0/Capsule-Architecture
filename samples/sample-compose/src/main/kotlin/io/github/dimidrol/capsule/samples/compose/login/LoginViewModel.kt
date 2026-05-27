package io.github.dimidrol.capsule.samples.compose.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.dimidrol.capsule.core.Capsule
import io.github.dimidrol.capsule.core.CapsuleConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val operationHandler = LoginOperationHandler(FakeAuthRepository())

    private val capsule: Capsule<LoginIntent, LoginState, LoginEffect> = LoginCapsule(
        initialState = LoginState(
            email = savedStateHandle[KEY_EMAIL] ?: "",
            password = savedStateHandle[KEY_PASSWORD] ?: ""
        ),
        scope = viewModelScope,
        operationHandler = operationHandler,
        config = CapsuleConfig()
    )

    val state: StateFlow<LoginState> = capsule.state
    val effects: Flow<LoginEffect> = capsule.effects

    fun send(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> savedStateHandle[KEY_EMAIL] = intent.value
            is LoginIntent.PasswordChanged -> savedStateHandle[KEY_PASSWORD] = intent.value
            else -> Unit
        }
        capsule.send(intent)
    }

    private companion object {
        const val KEY_EMAIL = "login_email"
        const val KEY_PASSWORD = "login_password"
    }
}
