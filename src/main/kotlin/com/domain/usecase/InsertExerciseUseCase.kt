package com.domain.usecase

import com.domain.infraestructure.Utils
import com.data.models.Exercise
import com.data.repository.ExerciseRepository
import com.ktor.ApplicationContext

class InsertExerciseUseCase(private val repository: ExerciseRepository) {

    // El objeto exercise a insertar; se asigna desde el endpoint.
    var exercise: Exercise? = null

    // Al invocar la función se procesa la imagen y se inserta el exercise en la BD
    suspend operator fun invoke(): Exercise? {
        // 1. Convertimos el ownerId a String para usarlo como nombre de la carpeta.
        val ownerFolder = exercise!!.ownerId.toString()

        // 2. Se crea el directorio para el usuario utilizando el userId
        val isCreateDir = Utils.createDir(ownerFolder)
        if (isCreateDir) {
            // 3. Se obtiene la imagen en base64 del objeto.
            val img = exercise!!.imageBase64
            if (!img.isNullOrBlank()) {
                // 4. Se procesa la imagen: se decodifica, guarda en disco y se obtiene el nombre del archivo.
                //    Se reemplaza el campo imageBase64 por el nombre del archivo generado.
                exercise = exercise!!.copy(imageBase64 = Utils.createBase64ToImg(img, ownerFolder))
            }
        } else {
            throw IllegalStateException("No se pudo crear el directorio para el usuario. Puede que ya exista o haya un error.")
        }

        // 5. Se inserta el exercise en la base de datos utilizando el repositorio
        val newExercise = repository.createExercise(
            name = exercise!!.name,
            description = exercise!!.description,
            imageBase64 = exercise!!.imageBase64,
            ownerId = exercise!!.ownerId
        )

        newExercise?.let { ex ->
            // 6. Si se generó el nombre de imagen (archivo), se construye la URL completa para acceder a ella.
            if (!ex.imageBase64.isNullOrBlank()) {
                val local = ApplicationContext.context.environment.config
                    .property("ktor.urlPath.baseUrl").getString()
                val relativePath = ApplicationContext.context.environment.config
                    .property("ktor.urlPath.images").getString()
                // Se forma la URL combinando baseUrl, la ruta de imágenes, el ownerId y el nombre del archivo.
                val fullUrl = "$local/$relativePath/${ex.ownerId}/${ex.imageBase64}"
                // Se actualiza el objeto exercise con la URL completa de la imagen.
                exercise = ex.copy(imageBase64 = fullUrl)
            }
        }

        // 7. Se retorna el objeto exercise (ya procesado e insertado)
        return exercise
    }
}
