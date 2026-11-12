package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.listenInto
import edu.uniquindio.co.tribooo.data.remote.moderationreview.ModerationReviewDto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
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

/** Historial de decisiones de moderación (colección "moderationReviews"); solo se crean documentos. */
@Singleton
class ModerationReviewRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    userRepository: UserRepository,
    eventRepository: EventRepository,
    @ApplicationScope scope: CoroutineScope
) : ModerationReviewRepository {

    private val collection = firestore.collection(ModerationReviewDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<ModerationReviewDto>>(emptyList())

    override val reviews: StateFlow<List<ModerationReview>> = combine(dtos, userRepository.users, eventRepository.events) { dtos, users, events ->
        val usersById = users.associateBy { it.id }
        val eventsById = events.associateBy { it.id }
        dtos.mapNotNull { dto ->
            val event = eventsById[dto.eventId] ?: return@mapNotNull null
            val admin = usersById[dto.adminId] ?: return@mapNotNull null
            dto.toDomain(event, admin)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        collection.listenInto(dtos, ModerationReviewDto::class.java) { dto, id -> dto.copy(id = id) }
    }

    override suspend fun save(review: ModerationReview) = firestoreCall {
        collection.document(review.id).set(ModerationReviewDto.fromDomain(review)).await()
        Unit
    }
}
