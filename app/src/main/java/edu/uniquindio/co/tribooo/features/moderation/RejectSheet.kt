package edu.uniquindio.co.tribooo.features.moderation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.util.REJECTION_QUICK_REASONS
import edu.uniquindio.co.tribooo.core.util.reasonCounterLabel

/**
 * Hoja "Motivo del rechazo". Sin estado propio: el texto y su error viven en [sheet]
 * y cada cambio se reporta al ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RejectSheet(
    sheet: RejectSheetState,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.Block, contentDescription = null, tint = colors.error, modifier = Modifier.size(24.dp))
                Text("Motivo del rechazo", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
            }
            Text(
                "Obligatorio. El autor recibe este texto y la publicación deja de aparecer en el feed público.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = colors.onSurfaceVariant
            )

            Surface(shape = RoundedCornerShape(16.dp), color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(sheet.title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    Text(sheet.authorName, fontSize = 13.sp, color = colors.onSurfaceVariant)
                }
            }

            Text("Motivos frecuentes", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.onSurfaceVariant)
            WrapRow {
                REJECTION_QUICK_REASONS.forEach { reason ->
                    Surface(
                        onClick = { onReasonChange(reason) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, colors.outline)
                    ) {
                        Text(
                            reason,
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Column {
                OutlinedTextField(
                    value = sheet.reason,
                    onValueChange = onReasonChange,
                    placeholder = { Text("Explica al autor qué debe corregir para volver a enviarlo.") },
                    isError = sheet.error != null,
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
                    Text(
                        text = sheet.error.orEmpty(),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colors.error,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = reasonCounterLabel(sheet.reason.trim().length),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
                    modifier = Modifier.weight(1.4f)
                ) {
                    Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Rechazar publicación", modifier = Modifier.padding(start = 6.dp), maxLines = 1)
                }
            }
        }
    }
}
