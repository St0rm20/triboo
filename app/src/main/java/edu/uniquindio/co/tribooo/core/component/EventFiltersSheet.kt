package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.EventFilters
import edu.uniquindio.co.tribooo.domain.model.MAX_DISTANCE_KM
import edu.uniquindio.co.tribooo.domain.model.MIN_DISTANCE_KM
import edu.uniquindio.co.tribooo.domain.model.WhenFilter

private val WHEN_OPTIONS = listOf(
    WhenFilter.ANY to "Cualquiera",
    WhenFilter.WEEK to "Esta semana",
    WhenFilter.MONTH to "Este mes"
)

/**
 * Hoja "Filtros" compartida por el Feed y Explorar. Trabaja sobre un borrador local:
 * solo "Aplicar" entrega los filtros ([onApply]); "Cancelar" y cerrar la descartan.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFiltersSheet(
    filters: EventFilters,
    categories: List<Category>,
    onApply: (EventFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var draft by remember { mutableStateOf(filters) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Filtros", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                TextButton(onClick = { draft = EventFilters(query = draft.query) }) { Text("Limpiar") }
            }

            SectionTitle("Categorías")
            WrapRow {
                categories.forEach { category ->
                    CategoryChip(
                        category = category,
                        selected = category.key in draft.categories,
                        onClick = {
                            draft = draft.copy(
                                categories = if (category.key in draft.categories) draft.categories - category.key
                                else draft.categories + category.key
                            )
                        }
                    )
                }
            }

            SectionTitle("Cuándo")
            WrapRow {
                WHEN_OPTIONS.forEach { (option, label) ->
                    WhenOption(label = label, selected = draft.whenFilter == option) {
                        draft = draft.copy(whenFilter = option)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionTitle("Distancia")
                Text("Hasta ${draft.maxDistanceKm} km", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.primary)
            }
            Slider(
                value = draft.maxDistanceKm.toFloat(),
                onValueChange = { draft = draft.copy(maxDistanceKm = it.toInt()) },
                valueRange = MIN_DISTANCE_KM.toFloat()..MAX_DISTANCE_KM.toFloat(),
                steps = MAX_DISTANCE_KM - MIN_DISTANCE_KM - 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Solo con cupo disponible", fontSize = 15.sp, color = colors.onSurface)
                    Text("Oculta eventos llenos", fontSize = 13.sp, color = colors.onSurfaceVariant)
                }
                Switch(checked = draft.onlyAvailable, onCheckedChange = { draft = draft.copy(onlyAvailable = it) })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = { onApply(draft) }, modifier = Modifier.weight(1f)) { Text("Aplicar") }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Opción de selección única: con check y tono secundario cuando está elegida. */
@Composable
private fun WhenOption(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.secondaryContainer else Color.Transparent,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
