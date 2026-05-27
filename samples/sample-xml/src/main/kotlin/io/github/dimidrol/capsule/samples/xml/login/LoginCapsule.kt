package io.github.dimidrol.capsule.samples.xml.login

import io.github.dimidrol.capsule.core.CapsuleConfig
import io.github.dimidrol.capsule.core.CapsuleRuntime
import io.github.dimidrol.capsule.core.Decision
import io.github.dimidrol.capsule.core.OperationState
import kotlinx.coroutines.CoroutineScope

class LoginCapsule(
    initialState: LoginState,
    scope: CoroutineScope,
    private val operationHandler: LoginOperationHandler,
    config: CapsuleConfig<LoginIntent, LoginState, LoginOperation, LoginResult, LoginEffect> = CapsuleConfig()
) : CapsuleRuntime<LoginIntent, LoginState, LoginOperation, LoginResult, LoginEffect>(
    initialState = initialState,
    scope = scope,
    config = config
) {
    override fun reduce(
        state: LoginState,
        intent: LoginIntent
    ): Decision<LoginState, LoginOperation, LoginEffect> = when (intent) {
        is LoginIntent.EmailChanged -> Decision.state(state.copy(email = intent.value))
        is LoginIntent.PasswordChanged -> Decision.state(state.copy(password = intent.value))
        LoginIntent.ForgotPasswordClicked -> Decision.effect(state, LoginEffect.NavigateToForgotPassword)
        LoginIntent.RegisterClicked -> Decision.effect(state, LoginEffect.NavigateToRegistration)
        LoginIntent.SubmitClicked -> submit(state)
    }

    override suspend fun handleOperation(operation: LoginOperation): LoginResult {
        return operationHandler.handle(operation)
    }

    override fun reduceResult(
        state: LoginState,
        result: LoginResult
    ): Decision<LoginState, LoginOperation, LoginEffect> = when (result) {
        LoginResult.LoginSuccess -> Decision.effect(
            state.copy(loginOperation = OperationState.Success(Unit)),
            LoginEffect.NavigateToHome
        )

        is LoginResult.LoginFailed -> Decision.effect(
            state.copy(loginOperation = OperationState.Failed(result.throwable)),
            LoginEffect.ShowMessage(result.throwable.message ?: "Login failed")
        )
    }

    private fun submit(state: LoginState): Decision<LoginState, LoginOperation, LoginEffect> {
        if (state.email.isBlank() || state.password.isBlank()) {
            return Decision.effect(state, LoginEffect.ShowMessage("Email and password must not be empty"))
        }
        return Decision.operation(
            state = state.copy(loginOperation = OperationState.Running),
            operation = LoginOperation.SubmitLogin(state.email, state.password)
        )
    }
}
