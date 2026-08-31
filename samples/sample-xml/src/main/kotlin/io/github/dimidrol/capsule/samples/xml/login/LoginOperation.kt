package io.github.dimidrol.capsule.samples.xml.login

sealed interface LoginOperation {
    data class SubmitLogin(
        val email: String,
        val password: String
    ) : LoginOperation
}
