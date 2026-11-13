package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.follow.FollowDto
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
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

/** Seguimientos entre usuarios (colección "follows"). */
@Singleton
class FollowRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    userRepository: UserRepository,
    authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : FollowRepository {

    private val collection = firestore.collection(FollowDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<FollowDto>>(emptyList())

    // Ambos usuarios se resuelven con la lista que ya mantiene su repositorio
    override val follows: StateFlow<List<Follow>> = combine(dtos, userRepository.users) { dtos, users ->
        val usersById = users.associateBy { it.id }
        dtos.mapNotNull { dto ->
            val follower = usersById[dto.followerId] ?: return@mapNotNull null
            val followed = usersById[dto.followedId] ?: return@mapNotNull null
            dto.toDomain(follower, followed)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        scope.listenWhileSignedIn(authState.uid, dtos, FollowDto::class.java) { collection }
    }

    // El id del documento es "{followerId}_{followedId}": seguir dos veces no duplica
    override suspend fun save(follow: Follow) = firestoreCall {
        val dto = FollowDto.fromDomain(follow)
        collection.document(FollowDto.documentId(dto.followerId, dto.followedId)).set(dto).await()
        Unit
    }

    override suspend fun delete(followerId: String, followedId: String) = firestoreCall {
        collection.document(FollowDto.documentId(followerId, followedId)).delete().await()
        Unit
    }
}
