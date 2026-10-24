package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.VolunteerActivism
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
import edu.uniquindio.co.tribooo.core.util.formatMoney
import edu.uniquindio.co.tribooo.domain.usecase.attendance.AttendanceStep
import edu.uniquindio.co.tribooo.domain.usecase.attendance.declaredCause

/** Diálogo del paso [step] para confirmar asistencia; no muestra nada si [step] es null. */
@Composable
fun AttendanceDialogs(
    step: AttendanceStep?,
    onCancel: () -> Unit,
    onContinue: () -> Unit
) {
    when (step) {
        null -> Unit
        is AttendanceStep.Conflict -> ScheduleConflictDialog(step, onDismiss = onCancel, onContinue = onContinue)
        is AttendanceStep.Cost -> ContributionDialog(step, onCancel, onContinue)
        is AttendanceStep.Free -> FreeEventDialog(step, onCancel, onContinue)
    }
}

@Composable
private fun ContributionDialog(step: AttendanceStep.Cost, onCancel: () -> Unit, onContinue: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val amount = step.contribution.amount.formatMoney()
    val cause = step.contribution.declaredCause

    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Rounded.VolunteerActivism, contentDescription = null, tint = colors.tertiary) },
        title = { Text("Este evento tiene un aporte") },
        text = {
            Column {
                Text(
                    "“${step.event.title}” pide un aporte de $amount por persona, que se paga en el lugar. " +
                        "Es un evento benéfico: la plataforma no admite eventos con fines lucrativos."
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text("$amount por persona", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        if (cause != null) {
                            Text("Destino declarado: $cause", fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Aceptar y confirmar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}

@Composable
private fun FreeEventDialog(step: AttendanceStep.Free, onCancel: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("¿Confirmar tu asistencia?") },
        text = {
            Text("“${step.event.title}” es de entrada libre: no tiene ningún costo ni aporte. Podrás cancelar hasta 2 h antes.")
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Aceptar") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancelar") } }
    )
}
