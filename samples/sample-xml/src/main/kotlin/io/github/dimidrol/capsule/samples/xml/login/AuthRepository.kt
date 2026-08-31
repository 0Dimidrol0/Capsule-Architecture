package io.github.dimidrol.capsule.samples.xml.login

interface AuthRepository {
    suspend fun login(email: String, password: String)
}
