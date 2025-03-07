package com.data.repository

import com.data.models.Exercise
import com.data.models.Exercises
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ExerciseRepository {

    fun createExercise(
        name: String,
        description: String?,
        imageBase64: String?,
        ownerId: Int
    ): Exercise? = transaction {
        val insertStatement = Exercises.insert {
            it[Exercises.name] = name
            it[Exercises.description] = description
            it[Exercises.imageBase64] = imageBase64
            it[Exercises.ownerId] = ownerId
        }
        insertStatement.resultedValues?.firstOrNull()?.let {
            Exercise(
                id = it[Exercises.id],
                name = it[Exercises.name],
                description = it[Exercises.description],
                imageBase64 = it[Exercises.imageBase64],
                ownerId = it[Exercises.ownerId]
            )
        }
    }

    fun getAllExercises(): List<Exercise> = transaction {
        Exercises.selectAll().map {
            Exercise(
                id = it[Exercises.id],
                name = it[Exercises.name],
                description = it[Exercises.description],
                imageBase64 = it[Exercises.imageBase64],
                ownerId = it[Exercises.ownerId]
            )
        }
    }

    fun getExerciseById(id: Int): Exercise? = transaction {
        Exercises.select { Exercises.id eq id }
            .map {
                Exercise(
                    id = it[Exercises.id],
                    name = it[Exercises.name],
                    description = it[Exercises.description],
                    imageBase64 = it[Exercises.imageBase64],
                    ownerId = it[Exercises.ownerId]
                )
            }
            .singleOrNull()
    }

    fun updateExercise(
        id: Int,
        name: String,
        description: String?,
        imageBase64: String?,
        ownerId: Int
    ): Exercise? = transaction {
        val updatedRows = Exercises.update({ Exercises.id eq id }) {
            it[Exercises.name] = name
            it[Exercises.description] = description
            it[Exercises.imageBase64] = imageBase64
            it[Exercises.ownerId] = ownerId
        }
        if (updatedRows > 0) {
            // Seleccionamos y devolvemos el registro actualizado
            Exercises.select { Exercises.id eq id }
                .map {
                    Exercise(
                        id = it[Exercises.id],
                        name = it[Exercises.name],
                        description = it[Exercises.description],
                        imageBase64 = it[Exercises.imageBase64],
                        ownerId = it[Exercises.ownerId]
                    )
                }
                .singleOrNull()
        } else {
            null
        }
    }


    fun deleteExercise(id: Int): Boolean = transaction {
        Exercises.deleteWhere { Exercises.id eq id } > 0
    }
}
