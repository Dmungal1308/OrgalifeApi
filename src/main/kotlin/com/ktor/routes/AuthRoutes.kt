package ktor.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.data.repository.UserRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ktor.routes.RegisterRequest
import ktor.routes.LoginRequest
import java.util.Date
import java.util.UUID

fun Route.authRoutes() {
    val userRepository = UserRepository()

    route("/auth") {
        post("/register") {
            val registration = call.receive<RegisterRequest>()
            // Verificamos si ya existe
            if (userRepository.findUserByEmail(registration.email) != null) {
                call.respond(mapOf("error" to "El usuario ya existe"))
                return@post
            }
            val user = userRepository.createUser(registration.email, registration.password)
            if (user != null) {
                call.respond(user)
            } else {
                call.respond(mapOf("error" to "Error en el registro"))
            }
        }

        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            val user = userRepository.findUserByEmail(loginRequest.email)
            if (user == null || user.password != loginRequest.password) {
                call.respond(mapOf("error" to "Credenciales inválidas"))
                return@post
            } else {
                // Generamos el token JWT
                val sessionId = UUID.randomUUID().toString()
                val token = JWT.create()
                    .withAudience("orgalifeAudience")
                    .withIssuer("orgalifeIssuer")
                    .withClaim("email", user.email)
                    .withClaim("userId", user.id)   // Esta línea añade el userId
                    .withClaim("sessionId", sessionId)
                    .withExpiresAt(Date(System.currentTimeMillis() + 600000)) // 10 minutos
                    .sign(Algorithm.HMAC256("mi_secreto"))

                userRepository.updateUserSession(user.id, sessionId)

                // Devuelve el token al cliente
                call.respond(mapOf("token" to token))
            }
        }

    }
}
