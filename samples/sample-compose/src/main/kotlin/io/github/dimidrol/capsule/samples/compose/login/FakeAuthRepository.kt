package io.github.dimidrol.capsule.samples.compose.login

import kotlinx.coroutines.delay

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String) {
        delay(800)
        if (email != "demo@capsule.dev" || password != "password") {
            throw IllegalArgumentException("Invalid credentials")
        }
    }
}
