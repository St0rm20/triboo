package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.ImageFolder
import edu.uniquindio.co.tribooo.domain.repository.ImageRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/** Imágenes "alojadas" en memoria: [upload] inventa una URL bajo [HOST] y no sube nada. */
@Singleton
class ImageRepositoryImpl @Inject constructor() : ImageRepository {

    private val _hosted = MutableStateFlow<Set<String>>(emptySet())

    /** URLs subidas que aún no se han borrado. */
    val hosted: StateFlow<Set<String>> = _hosted.asStateFlow()

    override suspend fun upload(source: String, folder: ImageFolder): String {
        val url = "$HOST${folder.name.lowercase()}/${UUID.randomUUID()}.jpg"
        _hosted.update { it + url }
        return url
    }

    override suspend fun delete(url: String) {
        _hosted.update { it - url }
    }

    override fun isHosted(url: String): Boolean = url.startsWith(HOST)

    companion object {
        const val HOST = "https://images.tribooo.test/"
    }
}
