package edu.uniquindio.co.tribooo.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uniquindio.co.tribooo.core.component.AuthTextField
import edu.uniquindio.co.tribooo.core.component.RequiredLegend
import edu.uniquindio.co.tribooo.core.component.formTarget
import edu.uniquindio.co.tribooo.core.util.FormTexts

@Composable
fun PasswordRecoveryScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: PasswordRecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val codeSent = state.sentTo != null
    val backToLogin = {
        viewModel.onBackToLogin()
        onNavigateToLogin()
    }

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

            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (codeSent) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (codeSent) {
                        Icons.Rounded.MarkEmailRead
                    } else {
                        Icons.Rounded.Email
                    },
                    contentDescription = null,
                    tint = if (codeSent) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    },
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = if (codeSent) "Revisa tu correo" else "Recupera tu contraseña",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 22.dp)
            )

            Text(
                text = if (codeSent) {
                    "Enviamos un código a ${state.sentTo}. Revisa la bandeja de entrada y espera el código para continuar."
                } else {
                    "Escribe el correo asociado a tu cuenta. Te enviaremos un código para recuperar el acceso."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!codeSent) {
                RequiredLegend(modifier = Modifier.padding(top = 24.dp))

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
                    onFocusLost = viewModel::onEmailFocusLost,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .formTarget(state.focusEmail, viewModel::onFocusHandled)
                )

                Button(
                    onClick = viewModel::onSend,
                    enabled = !state.isSending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Enviar código", modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = viewModel::onUseAnotherEmail) {
                        Text("Usar otro correo")
                    }
                    TextButton(onClick = backToLogin) {
                        Text("Volver al inicio")
                    }
                }
            }
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }
}
