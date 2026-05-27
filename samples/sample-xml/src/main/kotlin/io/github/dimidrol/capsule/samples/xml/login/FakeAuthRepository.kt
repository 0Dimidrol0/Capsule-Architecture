package io.github.dimidrol.capsule.samples.xml.login

import kotlinx.coroutines.delay

interface AuthRepository {
    suspend fun login(email: String, password: String)
}

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String) {
        delay(500)
        if (email != "demo@capsule.dev" || password != "password") {
            throw IllegalArgumentException("Invalid credentials")
        }
    }
}
