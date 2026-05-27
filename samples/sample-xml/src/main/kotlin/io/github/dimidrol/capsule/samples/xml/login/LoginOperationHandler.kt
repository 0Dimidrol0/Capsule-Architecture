package io.github.dimidrol.capsule.samples.xml.login

class LoginOperationHandler(
    private val authRepository: AuthRepository
) {
    suspend fun handle(operation: LoginOperation): LoginResult = when (operation) {
        is LoginOperation.SubmitLogin -> {
            runCatching {
                authRepository.login(operation.email, operation.password)
            }.fold(
                onSuccess = { LoginResult.LoginSuccess },
                onFailure = { LoginResult.LoginFailed(it) }
            )
        }
    }
}
