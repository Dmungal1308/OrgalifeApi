package com.domain.infraestructure

import com.ktor.ApplicationContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

class Utils {
    companion object {

        // Metodo que decodifica una imagen en base64, la guarda en disco y retorna el nombre del archivo generado.
        fun createBase64ToImg(img: String, userId: String): String? {
            val groupExtension = listOf("jpg", "jpeg", "gif")
            // 1. Se utiliza una expresión regular para separar el tipo (por ejemplo, image/jpeg) y el cuerpo (la parte base64) de la imagen.
            val regex = "data:(image/[^;]+);base64,(.+)".toRegex()
            val result = regex.find(img)

            return if (result != null) {
                // 2. Se extrae el tipo de imagen y se obtiene la extensión.
                val type = result.groupValues[1]
                var ext: String = type.split("/")[1]
                val body = result.groupValues[2]
                if (ext !in groupExtension)
                    return null
                try {
                    if (ext == "jpg")
                        ext = "jpeg" // Ajuste para evitar problemas con ImageIO.write

                    // 3. Se decodifica el string base64 a un arreglo de bytes
                    val imgBytes = Base64.getDecoder().decode(body)
                    // 4. Se crea un flujo de bytes a partir del arreglo
                    val inputStream = ByteArrayInputStream(imgBytes)
                    // 5. Se convierte el flujo en una imagen (BufferedImage)
                    val bufferImage: BufferedImage = ImageIO.read(inputStream)
                    // 6. Se obtiene la ruta base de imágenes desde la configuración y se añade el userId para formar el path final
                    val basePath = ApplicationContext.context.environment.config
                        .property("ktor.path.images").getString()
                    val path = "$basePath/$userId"
                    val dir = File(path)
                    // 7. Se verifica que el directorio exista (debe haberse creado previamente)
                    if (dir.isDirectory) {
                        // 8. Se construye un nombre de archivo único usando la fecha actual y el userId
                        val nameFile = "${userId}_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.$ext"
                        // 9. Se crea el archivo en la ruta construida y se escribe la imagen en disco
                        val fileImag = File("$path/$nameFile")
                        ImageIO.write(bufferImage, ext, fileImag)
                        // 10. Se retorna el nombre del archivo creado
                        return nameFile
                    } else {
                        return null
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            } else null
        }

        // Metodo que crea un directorio para el usuario en la ruta base configurada.
        fun createDir(userId: String): Boolean {
            val basePath = ApplicationContext.context.environment.config
                .property("ktor.path.images").getString()
            // Se crea una carpeta con el nombre del userId dentro de la ruta base.
            val dir = File(basePath, userId)
            return if (!dir.exists()) {
                // Se crean todos los directorios necesarios
                dir.mkdirs()
            } else {
                true
            }
        }

        // Metodo para eliminar un archivo de imagen de un usuario.
        fun deleteImage(userId: String, name: String): Boolean {
            return try {
                val basePath = ApplicationContext.context.environment.config
                    .property("ktor.path.images").getString()
                val path = "$basePath/$userId"
                val img = File(path, name)
                if (img.exists()) {
                    img.delete()
                    true
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        // Metodo para eliminar todo el directorio de un usuario.
        fun deleteDirectory(userId: String): Boolean {
            return try {
                val basePath = ApplicationContext.context.environment.config
                    .property("ktor.path.images").getString()
                val path = "$basePath/$userId"
                val dir = File(path)
                if (dir.exists()){
                    dir.deleteRecursively()
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
