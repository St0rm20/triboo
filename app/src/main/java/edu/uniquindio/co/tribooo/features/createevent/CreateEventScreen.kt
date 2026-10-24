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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.CategoryChip
import edu.uniquindio.co.tribooo.core.component.WrapRow
import edu.uniquindio.co.tribooo.core.component.FormErrorText
import edu.uniquindio.co.tribooo.core.component.FormTextField
import edu.uniquindio.co.tribooo.core.component.RequiredLegend
import edu.uniquindio.co.tribooo.core.component.formTarget
import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.core.util.FormLimits
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.util.Validators
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.dateTimeLabel
import edu.uniquindio.co.tribooo.core.util.formatMoney
import edu.uniquindio.co.tribooo.core.util.label
import edu.uniquindio.co.tribooo.core.util.shortLabelWithYear
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.usecase.location.MAX_NEIGHBORHOOD_LENGTH
import edu.uniquindio.co.tribooo.domain.usecase.location.MAX_PLACE_NAME_LENGTH
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun CreateEventScreen(
    onClose: () -> Unit,
    onEventPublished: (message: String) -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = FormLimits.MAX_IMAGES)
    ) { uris -> viewModel.onImagesSelected(uris.map { it.toString() }) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CreateEventTopBar(
                mode = state.mode,
                isPublishing = state.isPublishing,
                onClose = onClose,
                onPublish = viewModel::publish
            )
        }
    ) { innerPadding ->
        if (state.isLoading || state.loadError != null) {
            LoadStatus(error = state.loadError, onClose = onClose, modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }
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

            RequiredLegend()

            TitleField(state = state, viewModel = viewModel)

            CategoryField(
                state = state,
                viewModel = viewModel
            )

            FormTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Descripción",
                placeholder = "Qué se hará, qué llevar, para quién es…",
                hint = FormTexts.EVENT_DESCRIPTION_HINT,
                maxLength = FormLimits.EVENT_DESCRIPTION,
                minLines = 3
            )

            ScheduleAndPlaceCard(
                state = state,
                onMultiDayChange = viewModel::onMultiDayChange,
                onOpenPicker = viewModel::openPicker,
                onFocusHandled = viewModel::onFocusHandled
            )

            CupoCard(state = state, viewModel = viewModel)

            ContributionCard(state = state, viewModel = viewModel)

            ImagesButton(
                count = state.imageUris.size,
                notice = state.imageNotice,
                onClick = {
                    imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
        }
    }

    when (val target = state.picker) {
        PickerTarget.START_DATE, PickerTarget.END_DATE -> {
            val isStart = target == PickerTarget.START_DATE
            EventDatePickerDialog(
                initialDate = if (isStart) state.startDate else state.endDate,
                minDate = if (isStart) LocalDate.now(APP_ZONE) else state.startDate,
                onDismiss = viewModel::closePicker,
                onConfirm = { date ->
                    if (isStart) viewModel.onStartDateChange(date) else viewModel.onEndDateChange(date)
                }
            )
        }

        PickerTarget.START_TIME, PickerTarget.END_TIME -> {
            val isStart = target == PickerTarget.START_TIME
            EventTimePickerDialog(
                title = if (isStart) "Hora de inicio" else "Hora de fin",
                initialTime = if (isStart) state.startTime else state.endTime,
                onDismiss = viewModel::closePicker,
                onConfirm = { time ->
                    if (isStart) viewModel.onStartTimeChange(time) else viewModel.onEndTimeChange(time)
                }
            )
        }

        PickerTarget.PLACE -> PlacePickerDialog(
            selected = state.place,
            suggestionsFor = viewModel::suggestionsFor,
            onDismiss = viewModel::closePicker,
            onConfirm = viewModel::onPlaceConfirmed
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
private fun LoadStatus(error: String?, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        if (error == null) {
            CircularProgressIndicator()
        } else {
            Text(error, textAlign = TextAlign.Center)
            Button(onClick = onClose) { Text("Cerrar") }
        }
    }
}

@Composable
private fun CreateEventTopBar(
    mode: EventFormMode?,
    isPublishing: Boolean,
    onClose: () -> Unit,
    onPublish: () -> Unit
) {
    val (title, action) = when (mode) {
        null -> "Crear evento" to "Publicar"
        EventFormMode.EDIT -> "Editar evento" to "Guardar"
        EventFormMode.RESUBMIT -> "Reenviar evento" to "Reenviar"
        EventFormMode.DUPLICATE -> "Duplicar evento" to "Publicar"
    }
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
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Button(
                onClick = onPublish,
                enabled = !isPublishing,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(40.dp)
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(action)
                }
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
private fun TitleField(state: CreateEventUiState, viewModel: CreateEventViewModel) {
    FormTextField(
        value = state.title,
        onValueChange = viewModel::onTitleChange,
        label = "Nombre del evento",
        required = true,
        placeholder = "Ej. Ciclopaseo nocturno por la Av. Bolívar",
        hint = FormTexts.EVENT_TITLE_HINT,
        error = state.titleError,
        singleLine = true,
        onFocusLost = { viewModel.onFieldFocusLost(EventField.TITLE) },
        modifier = Modifier.formTarget(state.focusField == EventField.TITLE, viewModel::onFocusHandled)
    )
}

@Composable
private fun CategoryField(state: CreateEventUiState, viewModel: CreateEventViewModel) {
    Column(
        modifier = Modifier.formTarget(
            state.focusField == EventField.CATEGORY,
            viewModel::onFocusHandled,
            takesFocus = false
        )
    ) {
        FieldLabel("Categoría *", isError = state.categoryError != null)
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
        state.categoryError?.let { FormErrorText(it, Modifier.padding(start = 4.dp, top = 6.dp)) }
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
    onOpenPicker: (PickerTarget) -> Unit,
    onFocusHandled: () -> Unit
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
                    labelIsError = state.scheduleError != null,
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
                    labelIsError = state.scheduleError != null,
                    onClick = { onOpenPicker(PickerTarget.START_TIME) }
                )
            }
            Box(
                modifier = Modifier.formTarget(
                    state.focusField == EventField.SCHEDULE,
                    onFocusHandled,
                    takesFocus = false
                )
            ) {
                state.scheduleError?.let {
                    FormErrorText(it, Modifier.padding(start = 52.dp, end = 16.dp, bottom = 8.dp))
                }
            }
            HorizontalDivider(color = divider)

            PickerRow(
                icon = Icons.Rounded.AddLocationAlt,
                label = "Ubicación *",
                value = state.place?.name ?: "Elegir lugar",
                labelIsError = state.placeError != null,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                onClick = { onOpenPicker(PickerTarget.PLACE) }
            )
            Box(
                modifier = Modifier.formTarget(
                    state.focusField == EventField.PLACE,
                    onFocusHandled,
                    takesFocus = false
                )
            ) {
                state.placeError?.let {
                    FormErrorText(it, Modifier.padding(start = 52.dp, end = 16.dp, bottom = 8.dp))
                }
            }
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
                FormTextField(
                    value = if (state.contributionAmount > 0) state.contributionAmount.toString() else "",
                    onValueChange = viewModel::onContributionAmountChange,
                    label = "Monto del aporte",
                    required = true,
                    placeholder = "5000",
                    prefix = "$ ",
                    suffix = "COP por persona",
                    hint = FormTexts.CONTRIBUTION_AMOUNT_HINT,
                    error = state.contributionError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    containerColor = colors.surfaceContainerLow,
                    onFocusLost = { viewModel.onFieldFocusLost(EventField.CONTRIBUTION_AMOUNT) },
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .formTarget(state.focusField == EventField.CONTRIBUTION_AMOUNT, viewModel::onFocusHandled)
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
                FormTextField(
                    value = state.contributionCause,
                    onValueChange = viewModel::onContributionCauseChange,
                    label = "¿A qué se destina el aporte?",
                    required = true,
                    placeholder = "Ej. Becas de la Escuela de Bambuco",
                    hint = FormTexts.CONTRIBUTION_CAUSE_HINT,
                    error = state.contributionCauseError,
                    maxLength = FormLimits.CONTRIBUTION_CAUSE,
                    singleLine = true,
                    containerColor = colors.surfaceContainerLow,
                    onFocusLost = { viewModel.onFieldFocusLost(EventField.CONTRIBUTION_CAUSE) },
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .formTarget(state.focusField == EventField.CONTRIBUTION_CAUSE, viewModel::onFocusHandled)
                )
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
private fun ImagesButton(count: Int, notice: String?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
    notice?.let { FormErrorText(it, Modifier.padding(start = 4.dp)) }
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
    selected: PlaceSuggestion?,
    suggestionsFor: (query: String) -> List<PlaceSuggestion>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, neighborhood: String) -> Unit
) {
    var name by remember { mutableStateOf(selected?.name.orEmpty()) }
    var neighborhood by remember { mutableStateOf(selected?.neighborhood.orEmpty()) }
    var nameTouched by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubicación del evento") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Escribe el punto de encuentro. Los asistentes verán esta ubicación.",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant
                )
                FormTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = "Nombre del lugar",
                    required = true,
                    hint = FormTexts.PLACE_NAME_HINT,
                    error = Validators.placeName(name).takeIf { nameTouched },
                    maxLength = MAX_PLACE_NAME_LENGTH,
                    singleLine = true,
                    containerColor = Color.Transparent,
                    onFocusLost = { nameTouched = true }
                )
                FormTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = "Barrio o sector",
                    hint = FormTexts.PLACE_NEIGHBORHOOD_HINT,
                    maxLength = MAX_NEIGHBORHOOD_LENGTH,
                    singleLine = true,
                    containerColor = Color.Transparent
                )
                suggestionsFor(name).forEach { place ->
                    Surface(
                        onClick = {
                            name = place.name
                            neighborhood = place.neighborhood
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, colors.outlineVariant),
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
            TextButton(
                onClick = {
                    if (Validators.placeName(name) == null) onConfirm(name, neighborhood) else nameTouched = true
                }
            ) {
                Text("Usar este lugar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
