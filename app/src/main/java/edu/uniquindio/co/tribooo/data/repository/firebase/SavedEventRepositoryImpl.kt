package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.saved.SavedDto
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/** Eventos guardados por los usuarios (colección "saved"). */
@Singleton
class SavedEventRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    userRepository: UserRepository,
    eventRepository: EventRepository,
    authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : SavedEventRepository {

    private val collection = firestore.collection(SavedDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<SavedDto>>(emptyList())

    // El usuario y el evento se resuelven con las listas que ya mantienen sus repositorios, sin lecturas extra
    override val saved: StateFlow<List<Saved>> = combine(dtos, userRepository.users, eventRepository.events) { dtos, users, events ->
        val usersById = users.associateBy { it.id }
        val eventsById = events.associateBy { it.id }
        dtos.mapNotNull { dto ->
            val user = usersById[dto.userId] ?: return@mapNotNull null
            val event = eventsById[dto.eventId] ?: return@mapNotNull null
            dto.toDomain(user, event)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        // Escucha los cambios de la colección en tiempo real
        scope.listenWhileSignedIn(authState.uid, dtos, SavedDto::class.java) { collection }
    }

    // El id del documento es "{userId}_{eventId}": guardar dos veces no duplica
    override suspend fun save(saved: Saved) = firestoreCall {
        val dto = SavedDto.fromDomain(saved)
        collection.document(SavedDto.documentId(dto.userId, dto.eventId)).set(dto).await()
        Unit
    }

    override suspend fun delete(userId: String, eventId: String) = firestoreCall {
        collection.document(SavedDto.documentId(userId, eventId)).delete().await()
        Unit
    }
}
