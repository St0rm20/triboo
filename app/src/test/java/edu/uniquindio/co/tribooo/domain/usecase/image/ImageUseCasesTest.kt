package edu.uniquindio.co.tribooo.domain.usecase.image

import edu.uniquindio.co.tribooo.data.repository.memory.ImageRepositoryImpl
import edu.uniquindio.co.tribooo.domain.model.DataErrorReason
import edu.uniquindio.co.tribooo.domain.model.DataException
import edu.uniquindio.co.tribooo.domain.model.ImageFolder
import edu.uniquindio.co.tribooo.domain.repository.ImageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ImageUseCasesTest {

    /** Repositorio en memoria que falla al subir las fuentes de [failing] y al borrar las de [undeletable]. */
    private class FlakyImages(
        private val failing: Set<String> = emptySet(),
        private val undeletable: Set<String> = emptySet()
    ) {
        private val memory = ImageRepositoryImpl()

        val repository = object : ImageRepository {
            override suspend fun upload(source: String, folder: ImageFolder): String {
                if (source in failing) throw DataException(DataErrorReason.NETWORK)
                return memory.upload(source, folder)
            }
            override suspend fun delete(url: String) {
                if (url in undeletable) throw DataException(DataErrorReason.NETWORK)
                memory.delete(url)
            }
            override fun isHosted(url: String) = memory.isHosted(url)
        }
        val hosted get() = memory.hosted.value
    }

    private fun uploadImages(images: FlakyImages) =
        UploadImagesUseCase(UploadImageUseCase(images.repository), DeleteImagesUseCase(images.repository))

    @Test
    fun uploadsLocalImagesAndKeepsHostedOnesInOrder() = runBlocking {
        val images = FlakyImages()
        val hosted = images.repository.upload("content://previa", ImageFolder.EVENTS)

        val urls = uploadImages(images)(listOf("content://1", hosted, "content://2"), ImageFolder.EVENTS)

        assertEquals(3, urls.size)
        assertEquals(hosted, urls[1])
        assertTrue(urls.all { images.repository.isHosted(it) })
        assertEquals(3, images.hosted.size)
    }

    @Test
    fun copyUploadsHostedImagesAgain() = runBlocking {
        val images = FlakyImages()
        val hosted = images.repository.upload("content://previa", ImageFolder.EVENTS)

        val urls = uploadImages(images)(listOf(hosted), ImageFolder.EVENTS, copy = true)

        assertNotEquals(hosted, urls.single())
        assertEquals(2, images.hosted.size)
    }

    @Test
    fun aFailedUploadRemovesTheOnesThatSucceededButNotThePreviousOnes() = runBlocking {
        val images = FlakyImages(failing = setOf("content://mala"))
        val hosted = images.repository.upload("content://previa", ImageFolder.EVENTS)

        try {
            uploadImages(images)(listOf("content://1", hosted, "content://mala", "content://2"), ImageFolder.EVENTS)
            fail("Debía lanzar la excepción de la subida fallida")
        } catch (e: DataException) {
            assertEquals(DataErrorReason.NETWORK, e.reason)
        }

        assertEquals(setOf(hosted), images.hosted)
    }

    @Test
    fun deleteSkipsForeignUrlsAndToleratesFailures() = runBlocking {
        val orphan = "${ImageRepositoryImpl.HOST}profiles/huerfana.jpg"
        val images = FlakyImages(undeletable = setOf(orphan))
        val kept = images.repository.upload("content://1", ImageFolder.PROFILES)
        val removed = images.repository.upload("content://2", ImageFolder.PROFILES)

        // Un borrado que falla no lanza: esa imagen queda huérfana y las demás se borran igual
        val deleted = DeleteImagesUseCase(images.repository)(
            listOf(removed, removed, orphan, "https://lh3.googleusercontent.com/a/foto.jpg", "content://local")
        )

        assertEquals(1, deleted)
        assertEquals(setOf(kept), images.hosted)
    }
}
