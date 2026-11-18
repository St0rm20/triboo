package edu.uniquindio.co.tribooo.domain.usecase.image

import edu.uniquindio.co.tribooo.domain.model.DataException
import edu.uniquindio.co.tribooo.domain.repository.ImageRepository
import jakarta.inject.Inject

class DeleteImagesUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * Borra las imágenes alojadas de [urls] e ignora el resto (URIs locales, fotos de Google).
     * Es una limpieza: si un borrado falla la imagen queda huérfana, pero la operación del usuario
     * (borrar el evento, cambiar la foto) ya se hizo y no se revierte. Retorna cuántas se borraron.
     */
    suspend operator fun invoke(urls: Collection<String>): Int =
        urls.distinct().filter(imageRepository::isHosted).count { url ->
            try {
                imageRepository.delete(url)
                true
            } catch (e: DataException) {
                false
            }
        }
}
