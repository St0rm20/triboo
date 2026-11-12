package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.firestore.FirebaseFirestore
import edu.uniquindio.co.tribooo.data.remote.attendance.AttendanceDto
import edu.uniquindio.co.tribooo.data.remote.STOP_TIMEOUT_MILLIS
import edu.uniquindio.co.tribooo.data.remote.firestoreCall
import edu.uniquindio.co.tribooo.data.remote.listenInto
import edu.uniquindio.co.tribooo.di.ApplicationScope
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
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

/** Asistencias confirmadas (colección "attendances"). */
@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    firestore: FirebaseFirestore,
    userRepository: UserRepository,
    eventRepository: EventRepository,
    @ApplicationScope scope: CoroutineScope
) : AttendanceRepository {

    private val collection = firestore.collection(AttendanceDto.COLLECTION)

    // Documentos tal como los entrega el listener
    private val dtos = MutableStateFlow<List<AttendanceDto>>(emptyList())

    // El usuario y el evento se resuelven con las listas que ya mantienen sus repositorios, sin lecturas extra
    override val attendances: StateFlow<List<Attendance>> = combine(dtos, userRepository.users, eventRepository.events) { dtos, users, events ->
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
        collection.listenInto(dtos, AttendanceDto::class.java)
    }

    // El id del documento es "{userId}_{eventId}": guardar dos veces no duplica
    override suspend fun save(attendance: Attendance) = firestoreCall {
        val dto = AttendanceDto.fromDomain(attendance)
        collection.document(AttendanceDto.documentId(dto.userId, dto.eventId)).set(dto).await()
        Unit
    }

    override suspend fun delete(userId: String, eventId: String) = firestoreCall {
        collection.document(AttendanceDto.documentId(userId, eventId)).delete().await()
        Unit
    }
}
