package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.commitInBatches
import edu.uniquindio.co.tribooo.data.remote.documentsFlow
import edu.uniquindio.co.tribooo.data.remote.event.EventDto
import edu.uniquindio.co.tribooo.data.remote.eventimage.EventImageDto
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/**
 * Eventos (colección "events") y su galería (colección "eventImages").
 * Los listeners se registran con callbackFlow solo mientras alguna pantalla observa los eventos
 * y se eliminan [STOP_TIMEOUT_MILLIS] después de que la última deja de hacerlo.
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    @ApplicationScope scope: CoroutineScope
) : EventRepository {

    private val collection = firestore.collection(EventDto.COLLECTION)
    private val imagesCollection = firestore.collection(EventImageDto.COLLECTION)

    // El organizador se resuelve con la lista de usuarios que ya mantiene su repositorio
    override val events: StateFlow<List<Event>> = combine(
        collection.documentsFlow(EventDto::class.java) { dto, id -> dto.copy(id = id) },
        userRepository.users
    ) { dtos, users ->
        val usersById = users.associateBy { it.id }
        dtos.mapNotNull { dto -> usersById[dto.organizerId]?.let { dto.resolve(it) } }
    }
        // Si el listener falla se conserva la última lista; se vuelve a intentar cuando alguien observe de nuevo
        .catch { }
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    override val images: StateFlow<List<ImageEvent>> = combine(
        imagesCollection.documentsFlow(EventImageDto::class.java) { dto, id -> dto.copy(id = id) },
        events
    ) { dtos, events ->
        val eventsById = events.associateBy { it.id }
        dtos.mapNotNull { dto -> eventsById[dto.eventId]?.let { dto.toDomain(it) } }
    }
        .catch { }
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    override suspend fun save(event: Event, imageUrls: List<String>) = firestoreCall {
        // Firestore genera el id si el evento aún no tiene uno
        val document = if (event.id.isBlank()) collection.document() else collection.document(event.id)
        // La primera imagen es la portada; el resto va a la galería
        val stored = event.copy(id = document.id, imageUrl = imageUrls.firstOrNull() ?: event.imageUrl)

        val batch = firestore.batch()
        batch.set(document, EventDto.fromDomain(stored))
        galleryOf(stored.id, imageUrls).forEach { batch.set(imagesCollection.document(it.id), it) }
        batch.commit().await()
        Unit
    }

    override suspend fun findById(id: String): Event? {
        // Primero la lista que ya mantiene el listener; solo si no está se consulta el documento
        events.value.firstOrNull { it.id == id }?.let { return it }
        return firestoreCall {
            val snapshot = collection.document(id).get().await()
            val dto = snapshot.toObject(EventDto::class.java)?.copy(id = snapshot.id) ?: return@firestoreCall null
            userRepository.findById(dto.organizerId)?.let { dto.resolve(it) }
        }
    }

    override suspend fun update(event: Event, imageUrls: List<String>?) = firestoreCall {
        val exists = events.value.any { it.id == event.id } || collection.document(event.id).get().await().exists()
        if (!exists) return@firestoreCall

        val stored = if (imageUrls == null) event else event.copy(imageUrl = imageUrls.firstOrNull())
        val batch = firestore.batch()
        batch.set(collection.document(stored.id), EventDto.fromDomain(stored))
        if (imageUrls != null) {
            // Se reemplaza toda la galería
            currentImageIds(stored.id).forEach { batch.delete(imagesCollection.document(it)) }
            galleryOf(stored.id, imageUrls).forEach { batch.set(imagesCollection.document(it.id), it) }
        }
        batch.commit().await()
        Unit
    }

    override suspend fun delete(eventId: String) = firestoreCall {
        val imageIds = currentImageIds(eventId)
        firestore.commitInBatches(imageIds) { delete(imagesCollection.document(it)) }
        collection.document(eventId).delete().await()
        Unit
    }

    /** Ids de las imágenes guardadas del evento (se consultan: la lista en memoria puede no estar al día). */
    private suspend fun currentImageIds(eventId: String): List<String> =
        imagesCollection.whereEqualTo("eventId", eventId).get().await().documents.map { it.id }

    /** Imágenes de la galería: todas menos la primera, que es la portada. */
    private fun galleryOf(eventId: String, imageUrls: List<String>): List<EventImageDto> =
        imageUrls.drop(1).mapIndexed { index, url ->
            EventImageDto(id = imagesCollection.document().id, eventId = eventId, url = url, order = index)
        }

    /** La categoría sale del catálogo de la app; un evento con una categoría desconocida no se muestra. */
    private fun EventDto.resolve(organizer: User): Event? =
        Category.defaultCategories.firstOrNull { it.id == categoryId }?.let { toDomain(organizer, it) }
}
