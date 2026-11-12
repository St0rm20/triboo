package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.user.UserDto
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/** Perfiles de usuario en la colección "users"; el id del documento es el uid de Firebase Authentication. */
@Singleton
class UserRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore
) : UserRepository {

    private val collection = firestore.collection(UserDto.COLLECTION)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    init {
        // Escucha la colección en tiempo real; el resto de repositorios resuelve sus referencias con esta lista
        collection.addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _users.value = it.documents
                    .mapNotNull { snap -> snap.toObject(UserDto::class.java)?.copy(id = snap.id) }
                    .map { dto -> dto.toDomain() }
            }
        }
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
        _users.value.firstOrNull { it.id == id }?.let { return it }
        return firestoreCall {
            val snapshot = collection.document(id).get().await()
            snapshot.toObject(UserDto::class.java)?.copy(id = snapshot.id)?.toDomain()
        }
    }

    override suspend fun findByEmail(email: String): User? {
        _users.value.firstOrNull { it.email.equals(email, ignoreCase = true) }?.let { return it }
        return firestoreCall {
            val snapshot = collection.whereEqualTo("email", email.trim()).limit(1).get().await()
            snapshot.documents.firstOrNull()?.let { it.toObject(UserDto::class.java)?.copy(id = it.id)?.toDomain() }
        }
    }
}
