package edu.uniquindio.co.tribooo.domain.usecase.image

import edu.uniquindio.co.tribooo.domain.model.ImageFolder
import edu.uniquindio.co.tribooo.domain.repository.ImageRepository
import jakarta.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    /**
     * URL pública de [source]: lo sube si es una imagen local y retorna igual una que ya está alojada.
     * Con [copy] también sube una copia de las alojadas (p. ej. al duplicar un evento, para que cada uno
     * sea dueño de sus imágenes y borrar uno no deje al otro sin ellas).
     */
    suspend operator fun invoke(source: String, folder: ImageFolder, copy: Boolean = false): String =
        if (!copy && imageRepository.isHosted(source)) source else imageRepository.upload(source, folder)
}
