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
                logger.info("Lista de ejercicios consultada, total: ${exercises.size}")
                call.respond(exercises)
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    logger.warn("Intento de creación de ejercicio con token inválido.")
                    call.respond(mapOf("error" to "Token inválido"))
                    return@post
                }

                val req = call.receive<ExerciseRequest>()
                logger.info("Solicitud para crear ejercicio: ${req.name} por usuario ID: $userId")

                val exercise = exerciseRepository.createExercise(
                    name = req.name,
                    description = req.description,
                    imageBase64 = req.imageBase64,
                    ownerId = userId
                )

                if (exercise != null) {
                    logger.info("Ejercicio creado exitosamente con ID: ${exercise.id}")
                    call.respond(exercise)
                } else {
                    logger.error("Error al crear ejercicio: ${req.name}")
                    call.respond(mapOf("error" to "No se pudo crear el ejercicio"))
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    logger.warn("Solicitud inválida para editar ejercicio sin ID válido.")
                    call.respond(mapOf("error" to "ID inválido"))
                    return@put
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    call.respond(mapOf("error" to "Token inválido"))
                    return@put
                }

                val existingExercise = exerciseRepository.getExerciseById(id)
                if (existingExercise == null) {
                    logger.warn("Intento de edición fallido, ejercicio con ID $id no encontrado.")
                    call.respond(mapOf("error" to "Ejercicio no encontrado"))
                    return@put
                }

                if (existingExercise.ownerId != userId) {
                    logger.warn("Usuario $userId intentó editar un ejercicio que no es suyo (ID: $id)")
                    call.respond(mapOf("error" to "No autorizado"))
                    return@put
                }

                val req = call.receive<ExerciseRequest>()
                logger.info("Usuario $userId actualiza el ejercicio ID: $id")

                val updatedExercise = exerciseRepository.updateExercise(
                    id,
                    req.name,
                    req.description,
                    req.imageBase64,
                    userId
                )

                if (updatedExercise != null) {
                    logger.info("Ejercicio con ID $id actualizado exitosamente.")
                    call.respond(updatedExercise)
                } else {
                    logger.error("Error al actualizar ejercicio con ID $id.")
                    call.respond(mapOf("error" to "No se pudo actualizar el ejercicio"))
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    logger.warn("Solicitud inválida para eliminar ejercicio sin ID válido.")
                    call.respond(mapOf("error" to "ID inválido"))
                    return@delete
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                if (userId == null) {
                    logger.warn("Intento de eliminación de ejercicio sin autenticación válida.")
                    call.respond(mapOf("error" to "Token inválido"))
                    return@delete
                }

                val existingExercise = exerciseRepository.getExerciseById(id)
                if (existingExercise == null) {
                    logger.warn("Intento de eliminación fallido, ejercicio con ID $id no encontrado.")
                    call.respond(mapOf("error" to "Ejercicio no encontrado"))
                    return@delete
                }

                if (existingExercise.ownerId != userId) {
                    logger.warn("Usuario $userId intentó eliminar un ejercicio que no es suyo (ID: $id)")
                    call.respond(mapOf("error" to "No autorizado"))
                    return@delete
                }

                val deleted = exerciseRepository.deleteExercise(id)
                if (deleted) {
                    logger.info("Ejercicio con ID $id eliminado correctamente.")
                    call.respond(mapOf("success" to true))
                } else {
                    logger.error("No se pudo eliminar el ejercicio con ID $id.")
                    call.respond(mapOf("error" to "No se pudo eliminar el ejercicio"))
                }
            }
        }
    }
}

