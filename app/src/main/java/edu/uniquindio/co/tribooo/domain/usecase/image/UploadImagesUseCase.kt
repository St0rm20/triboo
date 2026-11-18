package edu.uniquindio.co.tribooo.domain.usecase.image

import edu.uniquindio.co.tribooo.domain.model.ImageFolder
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.supervisorScope

class UploadImagesUseCase @Inject constructor(
    private val uploadImage: UploadImageUseCase,
    private val deleteImages: DeleteImagesUseCase
) {
    /**
     * URLs públicas de [sources] en el mismo orden (la primera sigue siendo la portada). Sube en paralelo
     * las que hagan falta ([UploadImageUseCase]). Es todo o nada: si una falla, borra las que alcanzó a subir
     * y lanza el error de esa.
     */
    suspend operator fun invoke(sources: List<String>, folder: ImageFolder, copy: Boolean = false): List<String> {
        // supervisorScope: el fallo de una subida no cancela las demás, así se sabe cuáles hay que borrar
        val outcomes = supervisorScope {
            sources.map { source -> async { uploadImage(source, folder, copy) } }
                .map { upload -> runCatching { upload.await() } }
        }
        currentCoroutineContext().ensureActive()

        val failure = outcomes.firstNotNullOfOrNull { it.exceptionOrNull() } ?: return outcomes.map { it.getOrThrow() }
        deleteImages(newlyUploaded(sources, outcomes.mapNotNull { it.getOrNull() }))
        throw failure
    }

    companion object {
        /** De [urls] (resultado de una subida), las que no venían ya en [sources]: las que se acaban de crear. */
        fun newlyUploaded(sources: Collection<String>, urls: Collection<String>): List<String> {
            val previous = sources.toSet()
            return urls.filterNot { it in previous }
        }
    }
}
