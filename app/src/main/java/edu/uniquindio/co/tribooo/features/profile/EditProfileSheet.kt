package edu.uniquindio.co.tribooo.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.util.initials

/**
 * Ventana "Editar datos personales". Sin estado propio: el formulario y sus errores vienen en
 * [state] y cada cambio se reporta al ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    state: ProfileUiState,
    cities: List<String>,
    onNameChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onNeighborhoodChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSave: () -> Unit,
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
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Editar datos personales", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = colors.onSurfaceVariant)
                }
            }
            Text(
                "Tu nombre y tu barrio se muestran a los vecinos en tus eventos y comentarios.",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = colors.onSurfaceVariant
            )

            // Sin carga de imágenes: el avatar siempre son las iniciales del nombre que se escribe
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    InitialsAvatar(
                        initials = state.editName.initials(),
                        container = colors.tertiaryContainer,
                        content = colors.onTertiaryContainer,
                        size = 56.dp,
                        shape = RoundedCornerShape(20.dp),
                        fontSize = 20.sp
                    )
                    Text("Sin foto: se muestran tus iniciales.", fontSize = 12.sp, lineHeight = 18.sp, color = colors.onSurfaceVariant)
                }
            }

            FieldLabel("Nombre completo", isError = state.nameError != null, topPadding = 18.dp)
            SheetTextField(
                value = state.editName,
                onValueChange = onNameChange,
                placeholder = "Nombre y apellido",
                error = state.nameError
            )

            FieldLabel("Ciudad", isError = false, topPadding = 16.dp)
            WrapRow {
                cities.forEach { city ->
                    CityChip(city = city, selected = city == state.editCity, onClick = { onCityChange(city) })
                }
            }

            FieldLabel("Barrio", isError = state.neighborhoodError != null, topPadding = 18.dp)
            SheetTextField(
                value = state.editNeighborhood,
                onValueChange = onNeighborhoodChange,
                placeholder = "Ej. Laureles",
                error = state.neighborhoodError
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sobre mí", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.onSurfaceVariant)
                Text("${state.editBio.length} / $MAX_BIO_LENGTH", fontSize = 12.sp, color = colors.onSurfaceVariant)
            }
            SheetTextField(
                value = state.editBio,
                onValueChange = onBioChange,
                placeholder = "Cuéntale a la comunidad qué tipo de eventos organizas o te interesan.",
                error = null,
                singleLine = false,
                modifier = Modifier.height(112.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(17.dp),
                    border = BorderStroke(1.dp, colors.outline),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) { Text("Cancelar", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(17.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                ) { Text("Guardar cambios", fontSize = 15.sp, fontWeight = FontWeight.Medium) }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String, isError: Boolean, topPadding: androidx.compose.ui.unit.Dp) {
    val colors = MaterialTheme.colorScheme
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = if (isError) colors.error else colors.onSurfaceVariant,
        modifier = Modifier.padding(top = topPadding, bottom = 7.dp)
    )
}

@Composable
private fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        shape = RoundedCornerShape(17.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceContainer,
            unfocusedContainerColor = colors.surfaceContainer,
            errorContainerColor = colors.surfaceContainer,
            unfocusedBorderColor = colors.outlineVariant
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Chip de ciudad de selección única: relleno secundario cuando está elegida y borde cuando no. */
@Composable
private fun CityChip(city: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        color = if (selected) colors.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent,
        contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outline),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(city, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
