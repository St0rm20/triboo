package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveAttendancesUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) {
    operator fun invoke(): StateFlow<List<Attendance>> = attendanceRepository.attendances
}
