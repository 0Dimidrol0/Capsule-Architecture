package io.github.dimidrol.capsule.samples.xml.login

import androidx.lifecycle.SavedStateHandle
import io.github.dimidrol.capsule.base.viewmodel.BaseCapsuleViewModel
import io.github.dimidrol.capsule.core.Capsule
import io.github.dimidrol.capsule.core.CapsuleConfig
import kotlinx.coroutines.CoroutineScope

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle
) : BaseCapsuleViewModel<LoginIntent, LoginState, LoginEffect>() {
    override fun buildCapsule(scope: CoroutineScope): Capsule<LoginIntent, LoginState, LoginEffect> {
        return LoginCapsule(
            initialState = LoginState(
                email = savedStateHandle[KEY_EMAIL] ?: "",
                password = savedStateHandle[KEY_PASSWORD] ?: ""
            ),
            scope = scope,
            operationHandler = LoginOperationHandler(FakeAuthRepository()),
            config = CapsuleConfig()
        )
    }

    override fun onBeforeSend(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> savedStateHandle[KEY_EMAIL] = intent.value
            is LoginIntent.PasswordChanged -> savedStateHandle[KEY_PASSWORD] = intent.value
            else -> Unit
        }
    }

    private companion object {
        const val KEY_EMAIL = "login_email"
        const val KEY_PASSWORD = "login_password"
    }
}
