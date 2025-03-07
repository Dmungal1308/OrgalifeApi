package com.data.repository

import com.data.models.User
import com.data.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    fun createUser(email: String, password: String): User? = transaction {
        val insertStatement = Users.insert {
            it[Users.email] = email
            it[Users.password] = password
        }
        insertStatement.resultedValues?.firstOrNull()?.let {
            User(
                id = it[Users.id],
                email = it[Users.email],
                password = it[Users.password]
            )
        }
    }

    fun findUserByEmail(email: String): User? = transaction {
        Users.select { Users.email eq email }
            .map {
                User(
                    id = it[Users.id],
                    email = it[Users.email],
                    password = it[Users.password],
                    token = it[Users.token] // <- Aquí mapeas la columna token
                )
            }
            .singleOrNull()
    }


    fun getUserById(id: Int): User? = transaction {
        Users.select { Users.id eq id }
            .map {
                User(
                    id = it[Users.id],
                    email = it[Users.email],
                    password = it[Users.password],
                    token = it[Users.token]
                )
            }
            .singleOrNull()
    }


    fun updateUser(id: Int, email: String, password: String): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.email] = email
            it[Users.password] = password
        } > 0
    }

    fun deleteUser(id: Int): Boolean = transaction {
        Users.deleteWhere { Users.id eq id } > 0
    }

    fun updateUserToken(userId: Int, token: String): Boolean = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.token] = token
        } > 0
    }

    fun updateUserSession(userId: Int, sessionId: String): Boolean = transaction {
        Users.update({ Users.id eq userId }) {
            it[Users.token] = sessionId // Usamos la columna "token" para almacenar el sessionId
        } > 0
    }

}
