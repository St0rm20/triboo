package edu.uniquindio.co.tribooo.features.eventdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.WrongLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.usecase.report.SubmitReportUseCase
import kotlinx.coroutines.launch

private fun reasonIcon(reason: String): ImageVector = when (reason) {
    ReportReasons.FALSE_INFO -> Icons.Rounded.Report
    ReportReasons.NO_LONGER_EXISTS -> Icons.Rounded.EventBusy
    ReportReasons.CHARGES_OR_ADS -> Icons.Rounded.Sell
    ReportReasons.WRONG_PLACE_OR_CONTACT -> Icons.Rounded.WrongLocation
    ReportReasons.OFFENSIVE_OR_UNSAFE -> Icons.Rounded.GppBad
    else -> Icons.Rounded.ContentCopy
}

/** Hoja "Reportar este evento": motivo de selección única y detalle opcional. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportEventSheet(
    event: Event,
    draft: ReportDraft,
    onAction: (EventDetailAction) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismiss = { onAction(EventDetailAction.HideReport) }
    // Cierra con la animación de la hoja antes de quitarla de la composición
    val close: () -> Unit = { scope.launch { sheetState.hide() }.invokeOnCompletion { dismiss() } }

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text("Reportar este evento", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                Text(
                    event.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text("Organiza ${event.organizer.name}", fontSize = 12.sp, color = colors.onSurfaceVariant)
            }

            Text("Motivo del reporte", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReportReasons.all.forEach { reason ->
                    ReasonOption(
                        reason = reason,
                        selected = draft.reason == reason,
                        onSelect = { onAction(EventDetailAction.ReportReasonChange(reason)) }
                    )
                }
            }
            draft.error?.let { Text(it, fontSize = 12.sp, color = colors.error) }

            OutlinedTextField(
                value = draft.detail,
                onValueChange = { onAction(EventDetailAction.ReportDetailChange(it)) },
                label = { Text("Detalle · opcional") },
                placeholder = { Text("Cuéntale a moderación qué viste: fechas, contactos, enlaces sospechosos.") },
                supportingText = { Text("${draft.detail.length}/${SubmitReportUseCase.MAX_DETAIL_LENGTH}") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = close) { Text("Cancelar") }
                Button(
                    onClick = { onAction(EventDetailAction.SendReport) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Enviar reporte", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ReasonOption(reason: String, selected: Boolean, onSelect: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.errorContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onErrorContainer else colors.onSurface,
        border = if (selected) BorderStroke(1.dp, colors.error) else null,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(reasonIcon(reason), contentDescription = null, modifier = Modifier.size(22.dp))
            Text(reason, fontSize = 14.sp, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
        }
    }
}
