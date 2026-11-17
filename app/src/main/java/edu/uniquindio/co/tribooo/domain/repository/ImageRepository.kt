package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.ImageFolder

/**
 * Alojamiento de las imágenes que suben los usuarios. Firestore solo guarda la URL pública que retorna [upload].
 * Lanza `DataException` si la petición falla.
 */
interface ImageRepository {

    /**
     * Sube la imagen de [source] a [folder] y retorna su URL pública (HTTPS).
     * [source] es una URI local (`content://`, `file://`, la que entrega el selector del sistema)
     * o la URL de una imagen remota, que se copia.
     */
    suspend fun upload(source: String, folder: ImageFolder): String

    /** Borra la imagen de [url]. No hace nada si no está alojada aquí ([isHosted]) o si ya no existe. */
    suspend fun delete(url: String)

    /** true si [url] es una imagen alojada por este repositorio (y por tanto no hay que volver a subirla). */
    fun isHosted(url: String): Boolean
}
