package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.data.remote.reportsummary.ReportSummaryDto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.ReportSummary
import edu.uniquindio.co.tribooo.domain.repository.ReportSummaryRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/** Resúmenes de reportes del asistente de moderación (colección "reportSummaries"); el id del documento es el del evento. */
@Singleton
class ReportSummaryRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : ReportSummaryRepository {

    private val collection = firestore.collection(ReportSummaryDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<ReportSummaryDto>>(emptyList())

    override val summaries: StateFlow<List<ReportSummary>> = dtos.map { dtos -> dtos.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        scope.listenWhileSignedIn(authState.uid, dtos, ReportSummaryDto::class.java, withId = { dto, id -> dto.copy(eventId = id) }) { collection }
    }

    override suspend fun save(summary: ReportSummary) = firestoreCall {
        collection.document(summary.eventId).set(ReportSummaryDto.fromDomain(summary)).await()
        Unit
    }
}
