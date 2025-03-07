package com.ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ktor.routes.authRoutes
import ktor.routes.exerciseRoutes

fun Application.configureRouting() {
    routing {
        authRoutes()      // Rutas de registro/login
        exerciseRoutes()  // Rutas CRUD de Exercise
    }
}
