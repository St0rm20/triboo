package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.Attendance
import kotlinx.coroutines.flow.StateFlow

interface AttendanceRepository {
    val attendances: StateFlow<List<Attendance>>
    suspend fun save(attendance: Attendance)
    suspend fun delete(userId: String, eventId: String)
}
