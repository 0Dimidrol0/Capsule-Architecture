package io.github.dimidrol.capsule.samples.xml.login

import io.github.dimidrol.capsule.core.OperationState

sealed interface LoginIntent {
    data class EmailChanged(val value: String) : LoginIntent

    data class PasswordChanged(val value: String) : LoginIntent

    data object SubmitClicked : LoginIntent

    data object ForgotPasswordClicked : LoginIntent

    data object RegisterClicked : LoginIntent
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val loginOperation: OperationState<Unit> = OperationState.Idle
)

sealed interface LoginOperation {
    data class SubmitLogin(
        val email: String,
        val password: String
    ) : LoginOperation
}

sealed interface LoginResult {
    data object LoginSuccess : LoginResult

    data class LoginFailed(val throwable: Throwable) : LoginResult
}

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect

    data object NavigateToForgotPassword : LoginEffect

    data object NavigateToRegistration : LoginEffect

    data class ShowMessage(val message: String) : LoginEffect
}
