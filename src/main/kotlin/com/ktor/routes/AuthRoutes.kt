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
import org.slf4j.LoggerFactory

fun Route.authRoutes() {
    val logger = LoggerFactory.getLogger("AuthRoutes")
    val userRepository = UserRepository()

    route("/auth") {
        post("/register") {
            val registration = call.receive<RegisterRequest>()
            logger.info("Intento de registro con email: ${registration.email}")

            if (userRepository.findUserByEmail(registration.email) != null) {
                logger.warn("Registro fallido: el usuario ya existe (${registration.email})")
                call.respond(mapOf("error" to "El usuario ya existe"))
                return@post
            }

            val user = userRepository.createUser(registration.email, registration.password)
            if (user != null) {
                logger.info("Usuario registrado exitosamente: ${user.email} con ID ${user.id}")
                call.respond(user)
            } else {
                logger.error("Error al registrar usuario: ${registration.email}")
                call.respond(mapOf("error" to "Error en el registro"))
            }
        }

        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            logger.info("Intento de login con email: ${loginRequest.email}")

            val user = userRepository.findUserByEmail(loginRequest.email)
            if (user == null || user.password != loginRequest.password) {
                logger.warn("Intento de login fallido para: ${loginRequest.email}")
                call.respond(mapOf("error" to "Credenciales inválidas"))
                return@post
            }

            val sessionId = UUID.randomUUID().toString()
            val token = JWT.create()
                .withAudience("orgalifeAudience")
                .withIssuer("orgalifeIssuer")
                .withClaim("email", user.email)
                .withClaim("userId", user.id)
                .withClaim("sessionId", sessionId)
                .withExpiresAt(Date(System.currentTimeMillis() + 600000))
                .sign(Algorithm.HMAC256("mi_secreto"))

            userRepository.updateUserSession(user.id, sessionId)
            logger.info("Usuario ${user.email} ha iniciado sesión con token generado.")

            call.respond(mapOf("token" to token))
        }
    }
}

