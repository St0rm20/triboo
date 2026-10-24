package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.text.Normalizer

/** Altura máxima del menú; con más opciones se hace scroll. */
private val MenuMaxHeight = 280.dp

private val ACCENTS = "\\p{Mn}+".toRegex()

/** Minúsculas y sin tildes, para comparar textos. */
private fun String.normalized(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(ACCENTS, "").lowercase()

/** Opciones que contienen [query], sin distinguir mayúsculas ni tildes (todas si está en blanco). */
fun filterOptions(options: List<String>, query: String): List<String> {
    val text = query.trim().normalized()
    return if (text.isEmpty()) options else options.filter { it.normalized().contains(text) }
}

/**
 * Campo desplegable con búsqueda. Solo acepta una de las [options]: si se deja un texto que no
 * coincide con ninguna, se restaura [selectedOption] al cerrar el menú o perder el foco.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(selectedOption) }
    // Mientras no se escriba se muestran todas las opciones, aunque haya una seleccionada
    var isSearching by remember { mutableStateOf(false) }

    // Si el valor cambia desde afuera, el campo lo refleja
    LaunchedEffect(selectedOption) { query = selectedOption }

    fun restore() {
        query = selectedOption
        isSearching = false
        isExpanded = false
    }

    val filtered = if (isSearching) filterOptions(options, query) else options

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { expanded ->
            if (expanded) isExpanded = true else restore()
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                isSearching = true
                isExpanded = true
            },
            singleLine = true,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) restore() }
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { restore() },
            modifier = Modifier.heightIn(max = MenuMaxHeight)
        ) {
            if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Sin resultados") },
                    onClick = {},
                    enabled = false
                )
            }
            filtered.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        query = option
                        isSearching = false
                        isExpanded = false
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}
