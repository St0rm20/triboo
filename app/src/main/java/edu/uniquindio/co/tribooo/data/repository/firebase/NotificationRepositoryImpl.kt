package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.commitInBatches
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.data.remote.notification.NotificationDto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
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

/** Avisos para los usuarios (colección "notifications"). */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    userRepository: UserRepository,
    authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : NotificationRepository {

    private val collection = firestore.collection(NotificationDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<NotificationDto>>(emptyList())

    override val notifications: StateFlow<List<Notification>> = combine(dtos, userRepository.users) { dtos, users ->
        val usersById = users.associateBy { it.id }
        dtos.mapNotNull { dto -> usersById[dto.destinationUserId]?.let { dto.toDomain(it) } }
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        // Solo las del usuario con sesión: las reglas no dejan leer las de otros
        scope.listenWhileSignedIn(authState.uid, dtos, NotificationDto::class.java, withId = { dto, id -> dto.copy(id = id) }) { uid ->
            collection.whereEqualTo("destinationUserId", uid)
        }
    }

    override suspend fun save(notification: Notification) = firestoreCall {
        collection.document(notification.id).set(NotificationDto.fromDomain(notification)).await()
        Unit
    }

    override suspend fun markAsRead(id: String) = firestoreCall {
        collection.document(id).update("read", true).await()
        Unit
    }

    override suspend fun markAllAsRead(userId: String) = firestoreCall {
        // Solo se escriben las que siguen sin leer, según lo que ya trajo el listener
        val unread = dtos.value.filter { it.destinationUserId == userId && !it.read }
        firestore.commitInBatches(unread) { update(collection.document(it.id), "read", true) }
    }
}
