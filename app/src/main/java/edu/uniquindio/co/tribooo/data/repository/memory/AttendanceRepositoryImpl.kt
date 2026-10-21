package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class AttendanceRepositoryImpl @Inject constructor() : AttendanceRepository {

    private val _attendances = MutableStateFlow(MockData.attendances)
    override val attendances: StateFlow<List<Attendance>> = _attendances.asStateFlow()

    override suspend fun save(attendance: Attendance) {
        _attendances.update { it + attendance }
    }

    override suspend fun delete(userId: String, eventId: String) {
        _attendances.update { current -> current.filterNot { it.user.id == userId && it.event.id == eventId } }
    }
}
