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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import edu.uniquindio.co.tribooo.core.component.AuthTextField

@Composable
fun PasswordRecoveryScreen(
    onNavigateToLogin: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var codeSent by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
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
                    "Enviamos un código a ${email.ifBlank { "tu correo electrónico" }}. Revisa la bandeja de entrada y espera el código para continuar."
                } else {
                    "Escribe el correo asociado a tu cuenta. Te enviaremos un código para recuperar el acceso."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (!codeSent) {
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo",
                    leadingIcon = Icons.Rounded.Email,
                    placeholder = "tunombre@correo.com",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.padding(top = 28.dp)
                )

                Button(
                    onClick = { codeSent = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Enviar código", modifier = Modifier.padding(vertical = 8.dp))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { codeSent = false }) {
                        Text("Usar otro correo")
                    }
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Volver al inicio")
                    }
                }
            }
        }
    }
}
