package com.ktor.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.data.repository.UserRepository

fun Application.configureSecurity() {
    // Interceptor para extraer y guardar el token
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "orgalife"
            verifier(
                JWT.require(Algorithm.HMAC256("mi_secreto"))
                    .withAudience("orgalifeAudience")
                    .withIssuer("orgalifeIssuer")
                    .build()
            )
            // Aquí la validación usa el payload; no tenemos call en el credential
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                val sessionIdFromToken = credential.payload.getClaim("sessionId").asString()
                if (email != null && sessionIdFromToken != null) {
                    val user = UserRepository().findUserByEmail(email)
                    if (user != null && user.token == sessionIdFromToken) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

        }
    }
}
