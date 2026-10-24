package edu.uniquindio.co.tribooo.features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.uniquindio.co.tribooo.core.component.FormInvalidNotice
import edu.uniquindio.co.tribooo.core.component.FormTextField
import edu.uniquindio.co.tribooo.core.component.InitialsAvatar
import edu.uniquindio.co.tribooo.core.component.RequiredLegend
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.component.formTarget
import edu.uniquindio.co.tribooo.core.util.FormTexts
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
    onPhotoSelected: (String) -> Unit,
    onPhotoRemoved: () -> Unit,
    onFieldFocusLost: (EditProfileField) -> Unit,
    onFocusHandled: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPhotoSelected(it.toString()) }
    }

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
            RequiredLegend(modifier = Modifier.padding(top = 6.dp))

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        InitialsAvatar(
                            initials = state.editName.initials(),
                            container = colors.tertiaryContainer,
                            content = colors.onTertiaryContainer,
                            size = 56.dp,
                            shape = RoundedCornerShape(20.dp),
                            fontSize = 20.sp,
                            photo = state.editPhoto
                        )
                        Text(
                            text = if (state.editPhoto == null) {
                                "Sin foto: se muestran tus iniciales."
                            } else {
                                "Foto lista. Reemplázala o vuelve a tus iniciales."
                            },
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, colors.outline)
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (state.editPhoto == null) "Subir foto" else "Cambiar foto",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (state.editPhoto != null) {
                            TextButton(onClick = onPhotoRemoved) { Text("Quitar", fontSize = 14.sp, fontWeight = FontWeight.Medium) }
                        }
                    }
                }
            }

            FormTextField(
                value = state.editName,
                onValueChange = onNameChange,
                label = "Nombre completo",
                required = true,
                placeholder = "Nombre y apellido",
                hint = FormTexts.NAME_HINT,
                error = state.nameError,
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                containerColor = colors.surfaceContainer,
                onFocusLost = { onFieldFocusLost(EditProfileField.NAME) },
                modifier = Modifier
                    .padding(top = 18.dp)
                    .formTarget(state.editFocusField == EditProfileField.NAME, onFocusHandled)
            )

            FieldLabel("Ciudad *", topPadding = 16.dp)
            WrapRow {
                cities.forEach { city ->
                    CityChip(city = city, selected = city == state.editCity, onClick = { onCityChange(city) })
                }
            }

            FormTextField(
                value = state.editNeighborhood,
                onValueChange = onNeighborhoodChange,
                label = "Barrio",
                required = true,
                placeholder = "Ej. Laureles",
                hint = FormTexts.NEIGHBORHOOD_HINT,
                error = state.neighborhoodError,
                singleLine = true,
                shape = RoundedCornerShape(17.dp),
                containerColor = colors.surfaceContainer,
                onFocusLost = { onFieldFocusLost(EditProfileField.NEIGHBORHOOD) },
                modifier = Modifier
                    .padding(top = 18.dp)
                    .formTarget(state.editFocusField == EditProfileField.NEIGHBORHOOD, onFocusHandled)
            )

            FormTextField(
                value = state.editBio,
                onValueChange = onBioChange,
                label = "Sobre mí",
                placeholder = "Cuéntale a la comunidad qué tipo de eventos organizas o te interesan.",
                hint = FormTexts.BIO_HINT,
                maxLength = MAX_BIO_LENGTH,
                minLines = 4,
                shape = RoundedCornerShape(17.dp),
                containerColor = colors.surfaceContainer,
                modifier = Modifier.padding(top = 18.dp)
            )

            if (state.showInvalidNotice) FormInvalidNotice(modifier = Modifier.padding(top = 16.dp))

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
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(17.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                    } else {
                        Text("Guardar cambios", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String, topPadding: Dp) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = topPadding, bottom = 7.dp)
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
