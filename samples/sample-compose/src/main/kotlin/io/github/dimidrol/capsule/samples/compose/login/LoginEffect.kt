package io.github.dimidrol.capsule.samples.compose.login

sealed interface LoginEffect {
    data object NavigateToHome : LoginEffect

    data object NavigateToForgotPassword : LoginEffect

    data object NavigateToRegistration : LoginEffect

    data class ShowMessage(val message: String) : LoginEffect
}
