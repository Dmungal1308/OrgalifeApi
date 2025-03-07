package com.data.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table


object Users : Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val token = varchar("token", 512).nullable()
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class User(
    val id: Int,
    val email: String,
    val password: String,
    val token: String? = null
)
