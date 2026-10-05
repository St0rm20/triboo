package edu.uniquindio.co.tribooo.features.createevent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.uniquindio.co.tribooo.core.component.CategoryChip
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.dateTimeLabel
import edu.uniquindio.co.tribooo.core.util.formatMoney
import edu.uniquindio.co.tribooo.core.util.label
import edu.uniquindio.co.tribooo.core.util.shortLabelWithYear
import edu.uniquindio.co.tribooo.data.PlaceSuggestion
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/** Selector abierto en la pantalla. Algunos encadenan al siguiente (fecha → hora). */
private enum class PickerTarget { START_DATE, START_TIME, END_DATE, END_TIME, PLACE }

@Composable
fun CreateEventScreen(
    onClose: () -> Unit,
    onEventPublished: (message: String) -> Unit,
    viewModel: CreateEventViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var picker by rememberSaveable { mutableStateOf<PickerTarget?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris -> viewModel.onImagesSelected(uris.map { it.toString() }) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CreateEventTopBar(onClose = onClose, onPublish = viewModel::publish)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InfoBanner(
                text = "Todo evento pasa por moderación antes de aparecer en el feed. Suele tardar menos de 24 h.",
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer
            )

            TitleField(state = state, onValueChange = viewModel::onTitleChange)

            CategoryField(
                state = state,
                viewModel = viewModel
            )

            Column {
                FieldLabel("Descripción")
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("Qué se hará, qué llevar, para quién es…") },
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors(),
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                )
            }

            ScheduleAndPlaceCard(
                state = state,
                onMultiDayChange = viewModel::onMultiDayChange,
                onOpenPicker = { picker = it }
            )

            CupoCard(state = state, viewModel = viewModel)

            ContributionCard(state = state, viewModel = viewModel)

            ImagesButton(
                count = state.imageUris.size,
                onClick = {
                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }
    }

    when (val target = picker) {
        PickerTarget.START_DATE, PickerTarget.END_DATE -> {
            val isStart = target == PickerTarget.START_DATE
            EventDatePickerDialog(
                initialDate = if (isStart) state.startDate else state.endDate,
                minDate = if (isStart) LocalDate.now(APP_ZONE) else state.startDate,
                onDismiss = { picker = null },
                onConfirm = { date ->
                    if (isStart) viewModel.onStartDateChange(date) else viewModel.onEndDateChange(date)
                    // En eventos de varios días se pide también la hora
                    picker = when {
                        isStart && state.multiDay -> PickerTarget.START_TIME
                        !isStart -> PickerTarget.END_TIME
                        else -> null
                    }
                }
            )
        }

        PickerTarget.START_TIME, PickerTarget.END_TIME -> {
            val isStart = target == PickerTarget.START_TIME
            EventTimePickerDialog(
                title = if (isStart) "Hora de inicio" else "Hora de fin",
                initialTime = if (isStart) state.startTime else state.endTime,
                onDismiss = { picker = null },
                onConfirm = { time ->
                    if (isStart) viewModel.onStartTimeChange(time) else viewModel.onEndTimeChange(time)
                    // En eventos de un día el horario se elige como inicio → fin
                    picker = if (isStart && !state.multiDay) PickerTarget.END_TIME else null
                }
            )
        }

        PickerTarget.PLACE -> PlacePickerDialog(
            places = viewModel.placeSuggestions,
            selected = state.place,
            onDismiss = { picker = null },
            onConfirm = {
                viewModel.onPlaceSelected(it)
                picker = null
            }
        )

        null -> Unit
    }

    LaunchedEffect(state.publishResult) {
        when (val result = state.publishResult) {
            is RequestResult.Success -> {
                viewModel.resetPublishResult()
                onEventPublished(result.message)
            }

            is RequestResult.Failure -> {
                viewModel.resetPublishResult()
                snackbarHostState.showSnackbar(result.errorMessage)
            }

            else -> Unit
        }
    }
}

@Composable
private fun CreateEventTopBar(onClose: () -> Unit, onPublish: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
            }
            Text("Crear evento", fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Button(onClick = onPublish, shape = RoundedCornerShape(14.dp), modifier = Modifier.height(40.dp)) {
                Text("Publicar")
            }
        }
    }
}

@Composable
private fun InfoBanner(text: String, container: Color, content: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.Shield, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Text(text, fontSize = 12.sp, lineHeight = 18.sp, color = content)
    }
}

@Composable
private fun FieldLabel(text: String, isError: Boolean = false) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)

@Composable
private fun TitleField(state: CreateEventUiState, onValueChange: (String) -> Unit) {
    Column {
        FieldLabel("Nombre del evento *", isError = state.titleError != null)
        OutlinedTextField(
            value = state.title,
            onValueChange = onValueChange,
            placeholder = { Text("Ej. Ciclopaseo nocturno por la Av. Bolívar") },
            singleLine = true,
            isError = state.titleError != null,
            supportingText = state.titleError?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryField(state: CreateEventUiState, viewModel: CreateEventViewModel) {
    Column {
        FieldLabel("Categoría *", isError = state.categoryError)
        WrapRow {
            viewModel.categories.forEach { category ->
                CategoryChip(
                    category = category,
                    selected = state.category?.key == category.key,
                    onClick = { viewModel.onCategorySelected(category) },
                    height = 40.dp,
                    cornerRadius = 13.dp
                )
            }
        }
    }
}

@Composable
private fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PickerRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    labelIsError: Boolean = false,
    trailingIcon: ImageVector = Icons.Rounded.Edit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = if (labelIsError) colors.error else colors.onSurfaceVariant)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(trailingIcon, contentDescription = null, tint = colors.outline, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ScheduleAndPlaceCard(
    state: CreateEventUiState,
    onMultiDayChange: (Boolean) -> Unit,
    onOpenPicker: (PickerTarget) -> Unit
) {
    val divider = MaterialTheme.colorScheme.surfaceContainer
    SectionCard {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            ToggleRow(
                icon = Icons.Rounded.DateRange,
                title = "Varios días",
                subtitle = if (state.multiDay) "Fechas y horas de inicio y fin" else "Un solo día con hora de inicio y fin",
                checked = state.multiDay,
                onCheckedChange = onMultiDayChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = divider)

            if (state.multiDay) {
                PickerRow(
                    icon = Icons.Rounded.Event,
                    label = "Inicio",
                    value = dateTimeLabel(state.startDate, state.startTime),
                    onClick = { onOpenPicker(PickerTarget.START_DATE) }
                )
                HorizontalDivider(color = divider)
                PickerRow(
                    icon = Icons.Rounded.EventAvailable,
                    label = "Fin",
                    value = dateTimeLabel(state.endDate, state.endTime),
                    labelIsError = !state.isRangeValid,
                    onClick = { onOpenPicker(PickerTarget.END_DATE) }
                )
            } else {
                PickerRow(
                    icon = Icons.Rounded.Event,
                    label = "Fecha",
                    value = state.startDate.shortLabelWithYear(),
                    onClick = { onOpenPicker(PickerTarget.START_DATE) }
                )
                HorizontalDivider(color = divider)
                PickerRow(
                    icon = Icons.Rounded.Schedule,
                    label = "Horario",
                    value = "${state.startTime.label()} – ${state.endTime.label()}",
                    labelIsError = !state.isRangeValid,
                    onClick = { onOpenPicker(PickerTarget.START_TIME) }
                )
            }
            HorizontalDivider(color = divider)

            PickerRow(
                icon = Icons.Rounded.AddLocationAlt,
                label = "Ubicación *",
                value = state.place?.name ?: "Elegir lugar",
                labelIsError = state.placeError,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                onClick = { onOpenPicker(PickerTarget.PLACE) }
            )
        }
    }
}

@Composable
private fun CupoCard(state: CreateEventUiState, viewModel: CreateEventViewModel) {
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            ToggleRow(
                icon = Icons.Rounded.Groups,
                title = "Cupo máximo",
                subtitle = "Opcional · limita las confirmaciones",
                checked = state.cupoEnabled,
                onCheckedChange = viewModel::onCupoEnabledChange
            )
            if (state.cupoEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepperButton(Icons.Rounded.Remove, "Disminuir cupo", viewModel::decreaseCupo)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(state.cupo.toString(), fontSize = 24.sp)
                        Text(
                            " personas",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    StepperButton(Icons.Rounded.Add, "Aumentar cupo", viewModel::increaseCupo)
                }
            }
        }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ContributionCard(state: CreateEventUiState, viewModel: CreateEventViewModel) {
    val colors = MaterialTheme.colorScheme
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            ToggleRow(
                icon = Icons.Rounded.VolunteerActivism,
                title = "Aporte con causa",
                subtitle = if (state.contributionEnabled) {
                    "Solo eventos benéficos · el monto se paga en el lugar"
                } else {
                    "Opcional · solo para eventos benéficos, sin ánimo de lucro"
                },
                checked = state.contributionEnabled,
                onCheckedChange = viewModel::onContributionEnabledChange
            )
            if (state.contributionEnabled) {
                OutlinedTextField(
                    value = if (state.contributionAmount > 0) state.contributionAmount.toString() else "",
                    onValueChange = viewModel::onContributionAmountChange,
                    placeholder = { Text("5000") },
                    prefix = { Text("$ ") },
                    suffix = { Text("COP por persona", fontSize = 12.sp) },
                    singleLine = true,
                    isError = state.contributionError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceContainerLow,
                        unfocusedContainerColor = colors.surfaceContainerLow,
                        errorContainerColor = colors.surfaceContainerLow,
                        unfocusedBorderColor = colors.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.contributionPresets.forEach { amount ->
                        val selected = state.contributionAmount == amount
                        Surface(
                            onClick = { viewModel.onContributionPresetSelected(amount) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) colors.secondaryContainer else Color.Transparent,
                            contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface,
                            border = if (selected) null else BorderStroke(1.dp, colors.outline),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                Text(amount.formatMoney(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                state.contributionError?.let { error ->
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Error, contentDescription = null, tint = colors.error, modifier = Modifier.size(15.dp))
                        Text(error, fontSize = 12.sp, color = colors.error)
                    }
                }
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    InfoBanner(
                        text = "Solo eventos benéficos pueden pedir un aporte. La plataforma no admite eventos con fines lucrativos: moderación puede pedirte el destino de los fondos.",
                        container = colors.tertiaryContainer,
                        content = colors.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagesButton(count: Int, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .drawBehind {
                drawRoundRect(
                    color = colors.outline,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                    )
                )
            }
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = colors.primary, modifier = Modifier.size(23.dp))
        Column {
            Text(
                text = when (count) {
                    0 -> "Agregar imágenes"
                    1 -> "1 imagen seleccionada"
                    else -> "$count imágenes seleccionadas"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (count == 0) "Hasta 5 fotos · la primera será la portada" else "Toca para cambiar la selección",
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    // El DatePicker trabaja con milisegundos en UTC
    val minMillis = minDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    key(initialDate, minDate) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    key(title) {
        val pickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                    Text("Aceptar")
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun PlacePickerDialog(
    places: List<PlaceSuggestion>,
    selected: PlaceSuggestion?,
    onDismiss: () -> Unit,
    onConfirm: (PlaceSuggestion) -> Unit
) {
    var current by remember { mutableStateOf(selected) }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubicación del evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Elige el punto de encuentro. Los asistentes verán esta ubicación.",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant
                )
                places.forEach { place ->
                    val isSelected = current?.name == place.name
                    Surface(
                        onClick = { current = place },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) colors.secondaryContainer else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.Place, contentDescription = null, tint = colors.primary)
                            Column {
                                Text(place.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(place.detail, fontSize = 12.sp, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = current != null, onClick = { current?.let(onConfirm) }) {
                Text("Usar esta ubicación")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
