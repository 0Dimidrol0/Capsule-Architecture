package io.github.dimidrol.capsule.samples.compose.login

class LoginOperationHandler(
    private val authRepository: AuthRepository
) {
    suspend fun handle(operation: LoginOperation): LoginResult = when (operation) {
        is LoginOperation.SubmitLogin -> {
            runCatching {
                authRepository.login(operation.email, operation.password)
            }.fold(
                onSuccess = { LoginResult.LoginSuccess },
                onFailure = { throwable -> LoginResult.LoginFailed(throwable) }
            )
        }
    }
}
