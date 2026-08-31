package io.github.dimidrol.capsule.samples.xml.login

sealed interface LoginResult {
    data object LoginSuccess : LoginResult

    data class LoginFailed(val throwable: Throwable) : LoginResult
}
