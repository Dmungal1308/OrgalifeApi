package ktor.routes

import com.data.repository.ExerciseRepository
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ktor.routes.ExerciseRequest
import org.slf4j.LoggerFactory

fun Route.exerciseRoutes() {
    val logger = LoggerFactory.getLogger("ExerciseRoutes")
    val exerciseRepository = ExerciseRepository()

    authenticate("auth-jwt") {
        route("/exercises") {
            get {
                val exercises = exerciseRepository.getAllExercises()
                logger.info("Ejercicios recibidos: ${exercises.size}")
                call.respond(exercises)
            }
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(mapOf("error" to "ID inválido"))
                    return@get
                }
                val exercise = exerciseRepository.getExerciseById(id)
                if (exercise == null) {
                    call.respond(mapOf("error" to "Exercise no encontrado"))
                } else {
                    call.respond(exercise)
                }
            }
            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(mapOf("error" to "Token inválido"))
                    return@post
                }
                try {
                    val req = call.receive<ExerciseRequest>()
                    logger.info("ExerciseRequest recibido: $req")
                    val exercise = exerciseRepository.createExercise(
                        name = req.name,
                        description = req.description,
                        imageBase64 = req.imageBase64,
                        ownerId = userId
                    )
                    if (exercise != null) {
                        call.respond(exercise)
                    } else {
                        call.respond(mapOf("error" to "No se pudo crear el Exercise"))
                    }
                } catch (e: Exception) {
                    logger.error("Error al recibir ExerciseRequest", e)
                    call.respond(mapOf("error" to "Error al parsear la petición"))
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(mapOf("error" to "ID inválido"))
                    return@put
                }
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(mapOf("error" to "Token inválido"))
                    return@put
                }
                // Verificamos que el ejercicio existe y que el usuario autenticado es el owner
                val existingExercise = exerciseRepository.getExerciseById(id)
                if (existingExercise == null) {
                    call.respond(mapOf("error" to "Ejercicio no encontrado"))
                    return@put
                }
                if (existingExercise.ownerId != userId) {
                    call.respond(mapOf("error" to "No autorizado"))
                    return@put
                }
                val req = call.receive<ExerciseRequest>()
                val updatedExercise = exerciseRepository.updateExercise(
                    id,
                    req.name,
                    req.description,
                    req.imageBase64,
                    userId
                )
                if (updatedExercise != null) {
                    // Devolvemos el objeto actualizado
                    call.respond(updatedExercise)
                } else {
                    call.respond(mapOf("error" to "No se pudo actualizar el Exercise"))
                }


            }
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(mapOf("error" to "ID inválido"))
                    return@delete
                }
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(mapOf("error" to "Token inválido"))
                    return@delete
                }
                val existingExercise = exerciseRepository.getExerciseById(id)
                if (existingExercise == null) {
                    call.respond(mapOf("error" to "Ejercicio no encontrado"))
                    return@delete
                }
                if (existingExercise.ownerId != userId) {
                    call.respond(mapOf("error" to "No autorizado"))
                    return@delete
                }
                val deleted = exerciseRepository.deleteExercise(id)
                if (deleted) {
                    call.respond(mapOf("success" to true))
                } else {
                    call.respond(mapOf("error" to "No se pudo eliminar el Exercise"))
                }
            }
        }
    }
}
