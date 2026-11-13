package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.data.remote.user.UserDto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.User
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

/**
 * Perfiles de usuario en la colección "users"; el id del documento es el uid de Firebase Authentication.
 * El correo no se guarda en Firestore: solo se conoce el del usuario con sesión, que entrega Firebase Authentication.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    private val authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : UserRepository {

    private val collection = firestore.collection(UserDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<UserDto>>(emptyList())

    // Eagerly: el resto de repositorios resuelve sus referencias con esta lista y lee su valor actual
    override val users: StateFlow<List<User>> = combine(dtos, authState.uid) { dtos, uid ->
        dtos.map { dto -> dto.toDomain(emailOf(dto.id, uid)) }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        // Escucha la colección en tiempo real mientras haya sesión
        scope.listenWhileSignedIn(authState.uid, dtos, UserDto::class.java, withId = { dto, id -> dto.copy(id = id) }) { collection }
    }

    override suspend fun save(user: User) = firestoreCall {
        collection.document(user.id).set(UserDto.fromDomain(user)).await()
        Unit
    }

    override suspend fun update(user: User) = save(user)

    override suspend fun delete(id: String) = firestoreCall {
        collection.document(id).delete().await()
        Unit
    }

    override suspend fun findById(id: String): User? {
        // Primero la lista que ya mantiene el listener; solo si no está se consulta el documento
        users.value.firstOrNull { it.id == id }?.let { return it }
        return firestoreCall {
            val snapshot = collection.document(id).get().await()
            snapshot.toObject(UserDto::class.java)?.copy(id = snapshot.id)?.toDomain(emailOf(snapshot.id, authState.uid.value))
        }
    }

    /** Solo encuentra al usuario con sesión: los correos de los demás no están en Firestore. */
    override suspend fun findByEmail(email: String): User? {
        val uid = authState.uid.value ?: return null
        if (!email.trim().equals(authState.email, ignoreCase = true)) return null
        return findById(uid)
    }

    private fun emailOf(userId: String, sessionUid: String?): String =
        if (userId == sessionUid) authState.email.orEmpty() else ""
}
