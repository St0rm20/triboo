package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.data.remote.eventverdict.EventVerdictDto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.EventVerdict
import edu.uniquindio.co.tribooo.domain.repository.EventVerdictRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/** Veredictos del asistente de moderación (colección "eventVerdicts"); el id del documento es el del evento. */
@Singleton
class EventVerdictRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : EventVerdictRepository {

    private val collection = firestore.collection(EventVerdictDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<EventVerdictDto>>(emptyList())

    override val verdicts: StateFlow<List<EventVerdict>> = dtos.map { dtos -> dtos.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        scope.listenWhileSignedIn(authState.uid, dtos, EventVerdictDto::class.java, withId = { dto, id -> dto.copy(eventId = id) }) { collection }
    }

    override suspend fun save(verdict: EventVerdict) = firestoreCall {
        collection.document(verdict.eventId).set(EventVerdictDto.fromDomain(verdict)).await()
        Unit
    }
}
