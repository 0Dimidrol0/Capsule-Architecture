package io.github.dimidrol.capsule.samples.compose.login

sealed interface LoginIntent {
    data class EmailChanged(val value: String) : LoginIntent

    data class PasswordChanged(val value: String) : LoginIntent

    data object SubmitClicked : LoginIntent

    data object ForgotPasswordClicked : LoginIntent

    data object RegisterClicked : LoginIntent
}
