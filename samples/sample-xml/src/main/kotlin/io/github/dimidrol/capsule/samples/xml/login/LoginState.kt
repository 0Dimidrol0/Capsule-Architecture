package io.github.dimidrol.capsule.samples.xml.login

import io.github.dimidrol.capsule.core.OperationState

data class LoginState(
    val email: String = "",
    val password: String = "",
    val loginOperation: OperationState<Unit> = OperationState.Idle
)
