package edu.uniquindio.co.tribooo.core.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import edu.uniquindio.co.tribooo.R
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.ReportReasons

/** Recurso del texto de un motivo de reporte; null si [reason] no es una clave de [ReportReasons]. */
@StringRes
fun reportReasonRes(reason: String): Int? = when (reason) {
    ReportReasons.FALSE_INFO -> R.string.report_reason_false_info
    ReportReasons.NO_LONGER_EXISTS -> R.string.report_reason_no_longer_exists
    ReportReasons.CHARGES_OR_ADS -> R.string.report_reason_charges_or_ads
    ReportReasons.WRONG_PLACE_OR_CONTACT -> R.string.report_reason_wrong_place_or_contact
    ReportReasons.OFFENSIVE_OR_UNSAFE -> R.string.report_reason_offensive_or_unsafe
    ReportReasons.SPAM_OR_DUPLICATE -> R.string.report_reason_spam_or_duplicate
    else -> null
}

/** Texto del motivo en el idioma actual; si no es una clave conocida se muestra tal cual. */
fun reportReasonLabel(rp: ResourceProvider, reason: String): String =
    reportReasonRes(reason)?.let { rp.getString(it) } ?: reason

/** Recurso del nombre de una categoría según su [Category.key]. */
@StringRes
fun Category.labelRes(): Int = when (key) {
    Category.DEPORTES.key -> R.string.category_sports
    Category.CULTURA.key -> R.string.category_culture
    Category.ACADEMICO.key -> R.string.category_academic
    Category.VOLUNTARIADO.key -> R.string.category_volunteering
    else -> R.string.category_social
}

@Composable
fun Category.localizedName(): String = stringResource(labelRes())
