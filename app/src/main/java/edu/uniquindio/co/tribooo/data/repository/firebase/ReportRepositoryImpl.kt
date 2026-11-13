package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.commitInBatches
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.AuthState
import edu.uniquindio.co.tribooo.data.remote.listenWhileSignedIn
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.data.remote.report.ReportDto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/** Reportes de eventos hechos por los vecinos (colección "reports"). */
@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    userRepository: UserRepository,
    eventRepository: EventRepository,
    authState: AuthState,
    @ApplicationScope scope: CoroutineScope
) : ReportRepository {

    private val collection = firestore.collection(ReportDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<ReportDto>>(emptyList())

    // Ordenados del más reciente al más antiguo
    override val reports: StateFlow<List<Report>> = combine(dtos, userRepository.users, eventRepository.events) { dtos, users, events ->
        val usersById = users.associateBy { it.id }
        val eventsById = events.associateBy { it.id }
        dtos.mapNotNull { dto ->
            val event = eventsById[dto.eventId] ?: return@mapNotNull null
            val reporter = usersById[dto.reporterId] ?: return@mapNotNull null
            dto.toDomain(event, reporter)
        }.sortedByDescending { it.date }
    }.stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    init {
        // El administrador ve todos los reportes; los demás solo los suyos (las reglas no dejan leer los ajenos)
        val viewer = combine(authState.uid, userRepository.users) { uid, users ->
            uid?.let { ReportViewer(it, isAdmin = users.any { user -> user.id == it && user.role == UserRole.ADMIN }) }
        }.distinctUntilChanged()
        scope.listenWhileSignedIn(viewer, dtos, ReportDto::class.java, withId = { dto, id -> dto.copy(id = id) }) { viewer ->
            if (viewer.isAdmin) collection else collection.whereEqualTo("reporterId", viewer.uid)
        }
    }

    override suspend fun save(report: Report) = firestoreCall {
        collection.document(report.id).set(ReportDto.fromDomain(report)).await()
        Unit
    }

    override suspend fun markResolved(eventId: String) = firestoreCall {
        // Los reportes abiertos salen de lo que ya trajo el listener; solo se escriben los que cambian
        val open = dtos.value.filter { it.eventId == eventId && !it.resolved }
        firestore.commitInBatches(open) { update(collection.document(it.id), "resolved", true) }
    }
}

/** Quién observa los reportes: define qué consulta permiten las reglas. */
private data class ReportViewer(val uid: String, val isAdmin: Boolean)
