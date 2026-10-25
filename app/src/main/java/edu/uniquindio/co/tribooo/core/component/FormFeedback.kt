package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import edu.uniquindio.co.tribooo.core.theme.semanticColors
import edu.uniquindio.co.tribooo.core.util.FormTexts

/** Expone [error] a los lectores de pantalla; sin error no cambia nada. */
fun Modifier.errorSemantics(error: String?): Modifier =
    if (error == null) this else semantics { error(error) }

/** Leyenda "* Obligatorio" que va al inicio de los formularios con campos obligatorios. */
@Composable
fun RequiredLegend(modifier: Modifier = Modifier) {
    Text(
        text = FormTexts.REQUIRED_LEGEND,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/** Error con ícono y mensaje: nunca se comunica solo con el color. */
@Composable
fun FormErrorText(message: String, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.error
    Row(
        modifier = modifier.errorSemantics(message),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Rounded.Error, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

/** Aviso dentro de una hoja o formulario cuando se intentó enviar con campos inválidos. */
@Composable
fun FormInvalidNotice(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.errorContainer,
        contentColor = colors.onErrorContainer,
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Error, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(FormTexts.SUBMIT_INVALID, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Contador "n/máx"; pasa a color de error al llegar al límite. */
@Composable
fun CharCounter(length: Int, max: Int, modifier: Modifier = Modifier) {
    val atLimit = length >= max
    Text(
        text = "$length/$max",
        style = MaterialTheme.typography.bodySmall,
        color = if (atLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Texto de apoyo bajo un campo. Prioridad: [error], aviso de límite alcanzado, [success] y, si no
 * hay nada de eso, la pista [hint]. [counter] (longitud actual y máximo) va siempre a la derecha.
 */
@Composable
fun FieldSupport(
    error: String?,
    hint: String?,
    modifier: Modifier = Modifier,
    success: String? = null,
    counter: Pair<Int, Int>? = null
) {
    val atLimit = counter != null && counter.first >= counter.second
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            when {
                error != null -> FormErrorText(error)
                atLimit -> FormErrorText(FormTexts.limitReached(counter.second))
                success != null -> {
                    val green = semanticColors().green
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = green, modifier = Modifier.size(16.dp))
                        Text(success, style = MaterialTheme.typography.bodySmall, color = green)
                    }
                }
                hint != null -> Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (counter != null) CharCounter(counter.first, counter.second)
    }
}

/**
 * Marca un campo como destino del foco tras un envío inválido: cuando [active] pasa a true lo
 * trae a la vista y, si [takesFocus], le da el foco; luego avisa con [onHandled].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.formTarget(active: Boolean, onHandled: () -> Unit, takesFocus: Boolean = true): Modifier {
    val focusRequester = remember { FocusRequester() }
    val bringIntoView = remember { BringIntoViewRequester() }
    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        bringIntoView.bringIntoView()
        if (takesFocus) focusRequester.requestFocus()
        onHandled()
    }
    return this.bringIntoViewRequester(bringIntoView).focusRequester(focusRequester)
}
