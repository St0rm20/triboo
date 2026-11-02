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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.R
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
        title = { Text(stringResource(R.string.attendance_cost_title)) },
        text = {
            Column {
                Text(stringResource(R.string.attendance_cost_message, step.event.title, amount))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(stringResource(R.string.attendance_cost_per_person, amount), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        if (cause != null) {
                            Text(stringResource(R.string.attendance_cost_cause, cause), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onContinue) { Text(stringResource(R.string.attendance_cost_confirm)) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@Composable
private fun FreeEventDialog(step: AttendanceStep.Free, onCancel: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(stringResource(R.string.attendance_free_title)) },
        text = {
            Text(stringResource(R.string.attendance_free_message, step.event.title))
        },
        confirmButton = { TextButton(onClick = onContinue) { Text(stringResource(R.string.common_accept)) } },
        dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.common_cancel)) } }
    )
}
