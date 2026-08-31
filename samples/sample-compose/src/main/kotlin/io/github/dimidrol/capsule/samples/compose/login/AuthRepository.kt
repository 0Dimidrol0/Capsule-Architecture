package io.github.dimidrol.capsule.samples.compose.login

interface AuthRepository {
    suspend fun login(email: String, password: String)
}
