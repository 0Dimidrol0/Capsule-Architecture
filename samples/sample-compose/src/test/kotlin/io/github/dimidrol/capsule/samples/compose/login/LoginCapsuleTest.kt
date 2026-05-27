package io.github.dimidrol.capsule.samples.compose.login

import app.cash.turbine.test
import io.github.dimidrol.capsule.core.CapsuleConfig
import io.github.dimidrol.capsule.core.OperationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class LoginCapsuleTest {

    @Test
    fun `empty submit emits ShowMessage`() = runTest {
        val capsule = createCapsule(authRepository = FakeAuthRepository())

        capsule.effects.test {
            capsule.send(LoginIntent.SubmitClicked)
            runCurrent()

            assertEquals(
                LoginEffect.ShowMessage("Email and password must not be empty"),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful login sets Success and emits NavigateToHome`() = runTest {
        val capsule = createCapsule(authRepository = FakeAuthRepository())

        capsule.send(LoginIntent.EmailChanged("demo@capsule.dev"))
        capsule.send(LoginIntent.PasswordChanged("password"))
        runCurrent()

        capsule.effects.test {
            capsule.send(LoginIntent.SubmitClicked)
            advanceTimeBy(1_000)
            runCurrent()

            assertEquals(LoginEffect.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertIs<OperationState.Success<Unit>>(capsule.state.value.loginOperation)
    }

    @Test
    fun `failed login sets Failed and emits ShowMessage`() = runTest {
        val capsule = createCapsule(authRepository = FakeAuthRepository())

        capsule.send(LoginIntent.EmailChanged("wrong@capsule.dev"))
        capsule.send(LoginIntent.PasswordChanged("wrong"))
        runCurrent()

        capsule.effects.test {
            capsule.send(LoginIntent.SubmitClicked)
            advanceTimeBy(1_000)
            runCurrent()

            val effect = awaitItem()
            assertIs<LoginEffect.ShowMessage>(effect)
            cancelAndIgnoreRemainingEvents()
        }

        assertIs<OperationState.Failed>(capsule.state.value.loginOperation)
    }

    @Test
    fun `register click emits NavigateToRegistration`() = runTest {
        val capsule = createCapsule(authRepository = FakeAuthRepository())

        capsule.effects.test {
            capsule.send(LoginIntent.RegisterClicked)
            runCurrent()
            assertEquals(LoginEffect.NavigateToRegistration, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `forgot password click emits NavigateToForgotPassword`() = runTest {
        val capsule = createCapsule(authRepository = FakeAuthRepository())

        capsule.effects.test {
            capsule.send(LoginIntent.ForgotPasswordClicked)
            runCurrent()
            assertEquals(LoginEffect.NavigateToForgotPassword, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.createCapsule(authRepository: AuthRepository): LoginCapsule {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return LoginCapsule(
            initialState = LoginState(),
            scope = backgroundScope,
            operationHandler = LoginOperationHandler(authRepository),
            config = CapsuleConfig(operationDispatcher = dispatcher)
        )
    }
}
