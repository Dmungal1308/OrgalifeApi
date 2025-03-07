package ktor.routes

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseRequest(
    val name: String,
    val description: String? = null,
    val imageBase64: String? = null
)

