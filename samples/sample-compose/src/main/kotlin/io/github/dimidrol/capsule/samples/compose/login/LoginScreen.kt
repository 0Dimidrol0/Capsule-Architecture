package io.github.dimidrol.capsule.samples.compose.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dimidrol.capsule.core.OperationState
import io.github.dimidrol.capsule.navigation.compose.CapsuleNavCommand
import io.github.dimidrol.capsule.navigation.compose.CapsuleNavigator
import io.github.dimidrol.capsule.navigation.compose.HandleCapsuleEffects

@Composable
fun LoginScreen(
    navigator: CapsuleNavigator,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    HandleCapsuleEffects(effects = viewModel.effects) { effect ->
        when (effect) {
            LoginEffect.NavigateToForgotPassword -> navigator.handle(CapsuleNavCommand.Navigate("forgot"))
            LoginEffect.NavigateToHome -> navigator.handle(CapsuleNavCommand.Navigate("home"))
            LoginEffect.NavigateToRegistration -> navigator.handle(CapsuleNavCommand.Navigate("register"))
            is LoginEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    LoginScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEmailChanged = { viewModel.send(LoginIntent.EmailChanged(it)) },
        onPasswordChanged = { viewModel.send(LoginIntent.PasswordChanged(it)) },
        onSubmit = { viewModel.send(LoginIntent.SubmitClicked) },
        onForgotPassword = { viewModel.send(LoginIntent.ForgotPasswordClicked) },
        onRegister = { viewModel.send(LoginIntent.RegisterClicked) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    state: LoginState,
    snackbarHostState: SnackbarHostState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Capsule Login",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = onEmailChanged,
                label = { Text("Email") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.loginOperation !is OperationState.Running,
                onClick = onSubmit
            ) {
                if (state.loginOperation is OperationState.Running) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Sign in")
            }

            if (state.loginOperation is OperationState.Failed) {
                Text(
                    text = state.loginOperation.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            TextButton(onClick = onForgotPassword) {
                Text("Forgot password")
            }

            TextButton(onClick = onRegister) {
                Text("Create account")
            }

            Text(
                text = "Demo credentials: demo@capsule.dev / password",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
