package edu.uniquindio.co.tribooo.features.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.AuthTextField
import edu.uniquindio.co.tribooo.core.component.ConfirmAlertDialog
import edu.uniquindio.co.tribooo.core.component.DropdownMenu
import edu.uniquindio.co.tribooo.core.component.FormErrorText
import edu.uniquindio.co.tribooo.core.component.RequiredLegend
import edu.uniquindio.co.tribooo.core.component.errorSemantics
import edu.uniquindio.co.tribooo.core.component.formTarget
import edu.uniquindio.co.tribooo.core.component.requiredLabel
import edu.uniquindio.co.tribooo.core.util.FormTexts
import edu.uniquindio.co.tribooo.core.theme.semanticColors
import edu.uniquindio.co.tribooo.core.util.RequestResult
import edu.uniquindio.co.tribooo.core.util.passwordScore
import edu.uniquindio.co.tribooo.core.util.passwordStrengthFraction
import edu.uniquindio.co.tribooo.core.util.passwordStrengthLabel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = state.registerResult is RequestResult.Loading
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    fun focusOn(field: RegisterField) = state.focusField == field

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver al inicio de sesión"
                )
            }

            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Con una cuenta puedes confirmar asistencia, guardar eventos y publicar los tuyos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RequiredLegend()

                AuthTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nombre y apellido",
                    leadingIcon = Icons.Rounded.Person,
                    placeholder = "Ana Milena Torres",
                    required = true,
                    hint = FormTexts.NAME_HINT,
                    error = state.nameError,
                    onFocusLost = { viewModel.onFieldFocusLost(RegisterField.NAME) },
                    modifier = Modifier.formTarget(focusOn(RegisterField.NAME), viewModel::onFocusHandled)
                )

                DropdownMenu(
                    label = requiredLabel("Ciudad", true),
                    options = viewModel.cities,
                    selectedOption = state.city,
                    onOptionSelected = viewModel::onCityChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .formTarget(focusOn(RegisterField.CITY), viewModel::onFocusHandled, takesFocus = false),
                    leadingIcon = Icons.Rounded.LocationCity
                )
                state.cityError?.let { FormErrorText(it, Modifier.padding(start = 16.dp)) }

                AuthTextField(
                    value = state.address,
                    onValueChange = viewModel::onAddressChange,
                    label = "Dirección",
                    leadingIcon = Icons.Rounded.Home,
                    placeholder = "Dirección de residencia",
                    required = true,
                    hint = FormTexts.ADDRESS_HINT,
                    error = state.addressError,
                    onFocusLost = { viewModel.onFieldFocusLost(RegisterField.ADDRESS) },
                    modifier = Modifier.formTarget(focusOn(RegisterField.ADDRESS), viewModel::onFocusHandled)
                )

                AuthTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Correo",
                    leadingIcon = Icons.Rounded.Email,
                    placeholder = "tunombre@correo.com",
                    required = true,
                    hint = FormTexts.EMAIL_HINT,
                    error = state.emailError,
                    isValid = state.isEmailValid,
                    onFocusLost = { viewModel.onFieldFocusLost(RegisterField.EMAIL) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.formTarget(focusOn(RegisterField.EMAIL), viewModel::onFocusHandled)
                )

                AuthTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Contraseña",
                    leadingIcon = Icons.Rounded.Lock,
                    placeholder = "••••••••",
                    required = true,
                    hint = FormTexts.NEW_PASSWORD_HINT,
                    error = state.passwordError,
                    onFocusLost = { viewModel.onFieldFocusLost(RegisterField.PASSWORD) },
                    modifier = Modifier.formTarget(focusOn(RegisterField.PASSWORD), viewModel::onFocusHandled),
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = if (isPasswordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    }
                )

                PasswordStrengthMeter(password = state.password)

                AuthTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Rounded.Lock,
                    placeholder = "Repite tu contraseña",
                    required = true,
                    hint = FormTexts.CONFIRM_PASSWORD_HINT,
                    error = state.confirmPasswordError,
                    success = FormTexts.PASSWORDS_MATCH.takeIf { state.passwordsMatch },
                    onFocusLost = { viewModel.onFieldFocusLost(RegisterField.CONFIRM_PASSWORD) },
                    modifier = Modifier.formTarget(focusOn(RegisterField.CONFIRM_PASSWORD), viewModel::onFocusHandled),
                    visualTransformation = if (isConfirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isConfirmPasswordVisible = !isConfirmPasswordVisible
                            }
                        ) {
                            Icon(
                                imageVector = if (isConfirmPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = if (isConfirmPasswordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    }
                )

                TermsCheckbox(
                    accepted = state.termsAccepted,
                    error = state.termsError,
                    onAcceptedChange = viewModel::onTermsChange,
                    onOpenTerms = { showTerms = true },
                    modifier = Modifier.formTarget(
                        focusOn(RegisterField.TERMS),
                        viewModel::onFocusHandled,
                        takesFocus = false
                    )
                )
            }

            Button(
                onClick = { viewModel.onRegisterClick() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear cuenta", modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text("Iniciar sesión")
                }
            }
        }
    }

    if (showTerms) {
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text("Términos de uso y privacidad") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("• Usa la plataforma de forma respetuosa con la comunidad.")
                    Text("• Solo se publican eventos benéficos y sin fines de lucro.")
                    Text("• Tus datos se tratan solo para el funcionamiento básico de la app.")
                }
            },
            confirmButton = { TextButton(onClick = { showTerms = false }) { Text("Entendido") } }
        )
    }

    if (state.showConfirmDialog) {
        ConfirmAlertDialog(
            title = "Confirmar registro",
            message = "¿Estás seguro de que deseas crear tu cuenta?",
            confirmLabel = "Sí, registrarme",
            dismissLabel = "Cancelar",
            onConfirm = { viewModel.onConfirmRegister() },
            onDismiss = { viewModel.onDismissConfirmDialog() }
        )
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(state.registerResult) {
        when (val result = state.registerResult) {
            is RequestResult.Success -> {
                // La cuenta queda creada: se limpia el formulario y se entra por Login,
                // que muestra el aviso enviado por SnackbarMessenger
                viewModel.resetForm()
                onNavigateToLogin()
            }

            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(result.errorMessage)
                viewModel.resetRegisterResult()
            }

            else -> Unit
        }
    }
}

/** Barra fina de seguridad con su etiqueta; es solo informativa y no bloquea el registro. */
@Composable
private fun PasswordStrengthMeter(password: String) {
    val colors = MaterialTheme.colorScheme
    val semantic = semanticColors()
    val score = passwordScore(password)
    val barColor = when (score) {
        0 -> colors.surfaceVariant
        1 -> colors.error
        2 -> semantic.amber
        3 -> semantic.green
        else -> colors.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(passwordStrengthFraction(score))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
        Text(
            text = passwordStrengthLabel(score),
            style = MaterialTheme.typography.labelMedium,
            color = if (score == 0) colors.onSurfaceVariant else barColor
        )
    }
}

/** Casilla de aceptación de términos; "términos de uso" abre el detalle. */
@Composable
private fun TermsCheckbox(
    accepted: Boolean,
    error: String?,
    onAcceptedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val message = buildAnnotatedString {
        append("Acepto los ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms",
                styles = TextLinkStyles(
                    SpanStyle(color = colors.primary, fontWeight = FontWeight.Medium, textDecoration = TextDecoration.Underline)
                )
            ) { onOpenTerms() }
        ) { append("términos de uso") }
        append(" y el tratamiento de mis datos según la política de privacidad")
    }

    Column(modifier = modifier.errorSemantics(error)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (accepted) colors.primary else Color.Transparent)
                    .border(
                        BorderStroke(2.dp, if (accepted) colors.primary else if (error != null) colors.error else colors.outline),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(role = Role.Checkbox, onClickLabel = "Aceptar términos") { onAcceptedChange(!accepted) },
                contentAlignment = Alignment.Center
            ) {
                if (accepted) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        }
        error?.let { FormErrorText(it, Modifier.padding(top = 6.dp)) }
    }
}
