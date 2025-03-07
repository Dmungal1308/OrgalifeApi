package com.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object Exercises : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val description = varchar("description", 1024).nullable()
    // Aquí guardamos la imagen en base64
    val imageBase64 = text("image_base64").nullable()
    // Relación con la tabla de usuarios (dueño)
    val ownerId = integer("owner_id").references(Users.id)
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class Exercise(
    val id: Int,
    val name: String,
    val description: String?,
    val imageBase64: String?,
    val ownerId: Int
)
