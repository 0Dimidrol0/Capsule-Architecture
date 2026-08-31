package io.github.dimidrol.capsule.samples.compose.login

sealed interface LoginOperation {
    data class SubmitLogin(
        val email: String,
        val password: String
    ) : LoginOperation
}
