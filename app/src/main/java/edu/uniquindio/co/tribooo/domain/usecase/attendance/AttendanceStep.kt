package edu.uniquindio.co.tribooo.domain.usecase.attendance

import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event

/** Diálogo que el usuario debe aceptar antes de que se confirme su asistencia a [event]. */
sealed interface AttendanceStep {
    val event: Event

    /** [event] se cruza con [other], un evento al que el usuario ya confirmó. */
    data class Conflict(override val event: Event, val other: Event) : AttendanceStep

    /** [event] pide un aporte. */
    data class Cost(override val event: Event, val contribution: Contribution) : AttendanceStep

    /** [event] es de entrada libre. */
    data class Free(override val event: Event) : AttendanceStep
}

/** Primer paso al pedir confirmar asistencia: el choque de horario si lo hay; si no, el del aporte. */
fun startAttendanceFlow(event: Event, conflict: Event?): AttendanceStep =
    if (conflict != null) AttendanceStep.Conflict(event, conflict) else costStep(event)

/** Paso que sigue tras aceptar [current]; null significa que ya se puede confirmar la asistencia. */
fun nextAttendanceStep(current: AttendanceStep): AttendanceStep? = when (current) {
    is AttendanceStep.Conflict -> costStep(current.event)
    is AttendanceStep.Cost, is AttendanceStep.Free -> null
}

private fun costStep(event: Event): AttendanceStep =
    event.contribution?.let { AttendanceStep.Cost(event, it) } ?: AttendanceStep.Free(event)

/** Destino declarado del aporte; null si no se indicó (los eventos anteriores al campo del formulario pueden no tenerlo). */
val Contribution.declaredCause: String? get() = cause?.takeIf { it.isNotBlank() }
