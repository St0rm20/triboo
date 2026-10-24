package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.core.util.eventHoursLabel
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep

/** Avisa que el evento que se quiere confirmar se cruza con otro ya confirmado. */
@Composable
fun ScheduleConflictDialog(
    conflict: AttendanceStep.Conflict,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val event = conflict.event
    val other = conflict.other

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.EventRepeat, contentDescription = null, tint = colors.tertiary) },
        title = { Text("Choque de horario") },
        text = {
            Column {
                Text("“${event.title}” (${eventHoursLabel(event.startDate, event.endDate)}) se cruza con otro evento que ya confirmaste ese día.")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(other.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                        Text(
                            eventWhenLabel(other.startDate, other.endDate),
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continuar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("No confirmar") } }
    )
}
