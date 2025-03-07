package com.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.data.models.Users
import com.data.models.Exercises

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            // Si Ktor corre fuera de Docker, usa localhost
            jdbcUrl = "jdbc:mariadb://localhost:3306/orgalife"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = "orgalife"
            password = "orgalife"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        // Crear tablas si no existen
        transaction {
            try {
                SchemaUtils.create(Users, Exercises)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}