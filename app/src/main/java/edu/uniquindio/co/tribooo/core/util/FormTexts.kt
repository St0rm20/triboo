package edu.uniquindio.co.tribooo.core.util

/** Textos de ayuda, error y aviso compartidos por todos los formularios. */
object FormTexts {
    const val REQUIRED_LEGEND = "* Obligatorio"
    const val SUBMIT_INVALID = "Revisa los campos marcados."

    // Errores
    const val NAME_REQUIRED = "El nombre es obligatorio"
    const val NAME_TOO_SHORT = "El nombre debe tener al menos 3 caracteres"
    const val CITY_REQUIRED = "La ciudad es obligatoria"
    const val ADDRESS_REQUIRED = "La dirección es obligatoria"
    const val NEIGHBORHOOD_REQUIRED = "Indica tu barrio"
    const val EMAIL_REQUIRED = "El email es obligatorio"
    const val EMAIL_INVALID = "El email no es válido"
    const val PASSWORD_REQUIRED = "La contraseña es obligatoria"
    const val PASSWORD_TOO_SHORT = "La contraseña debe tener al menos 6 caracteres"
    const val NEW_PASSWORD_TOO_SHORT = "La contraseña debe tener al menos 8 caracteres"
    const val NEW_PASSWORD_NEEDS_UPPERCASE = "Incluye al menos una letra mayúscula"
    const val CONFIRM_PASSWORD_REQUIRED = "Debes confirmar la contraseña"
    const val PASSWORDS_DIFFER = "Las contraseñas no coinciden"
    const val TERMS_REQUIRED = "Debes aceptar los términos para continuar"
    const val EVENT_TITLE_REQUIRED = "Ponle un nombre claro al evento"
    const val CATEGORY_REQUIRED = "Elige una categoría"
    const val PLACE_REQUIRED = "Elige o escribe el lugar del evento"
    const val PLACE_NAME_REQUIRED = "Escribe el nombre del lugar"
    const val SCHEDULE_INVALID = "La hora de fin debe ser posterior a la de inicio"
    const val CONTRIBUTION_AMOUNT_REQUIRED = "Indica el monto del aporte o desactiva el costo"
    const val CONTRIBUTION_CAUSE_REQUIRED = "Indica a qué se destina el aporte"
    const val COMMENT_REQUIRED = "Escribe un comentario antes de publicarlo"
    const val REPLY_REQUIRED = "Escribe una respuesta antes de publicarla"
    const val REPORT_REASON_REQUIRED = "Elige el motivo del reporte para poder enviarlo."
    const val REJECT_REASON_REQUIRED = "Escribe el motivo del rechazo"

    // Pistas bajo cada campo
    const val NAME_HINT = "Mínimo 3 caracteres"
    const val ADDRESS_HINT = "Ej. Calle 12 # 5-30"
    const val NEIGHBORHOOD_HINT = "Ej. Laureles"
    const val EMAIL_HINT = "Ej. nombre@correo.com"
    const val PASSWORD_HINT = "Mínimo 6 caracteres"
    const val NEW_PASSWORD_HINT = "Mínimo 8 caracteres y una mayúscula"
    const val CONFIRM_PASSWORD_HINT = "Repite la misma contraseña"
    const val EVENT_TITLE_HINT = "Un nombre corto que diga qué se hará"
    const val EVENT_DESCRIPTION_HINT = "Opcional · qué se hará, qué llevar, para quién es"
    const val CONTRIBUTION_AMOUNT_HINT = "Solo números, en pesos colombianos"
    const val CONTRIBUTION_CAUSE_HINT = "Moderación verificará que sea una causa benéfica"
    const val PLACE_NAME_HINT = "Ej. Parque La Libertad"
    const val PLACE_NEIGHBORHOOD_HINT = "Si lo dejas vacío se usa tu ciudad"
    const val BIO_HINT = "Opcional · cuéntale a la comunidad qué te interesa"
    const val COMMENT_HINT = "Obligatorio para publicar · la calificación es opcional"
    const val REPLY_HINT = "Tu respuesta la verá quien escribió la reseña"
    const val REPORT_DETAIL_HINT = "Opcional · fechas, contactos o enlaces sospechosos"
    const val REJECT_REASON_HINT = "Mínimo 15 caracteres · el autor lo recibirá tal cual"

    // Retroalimentación positiva
    const val EMAIL_VALID = "Correo con formato válido"
    const val PASSWORDS_MATCH = "Las contraseñas coinciden"

    // Avisos
    const val LOGIN_SUCCESS = "Inicio de sesión exitoso"
    const val ACCOUNT_CREATED = "Cuenta creada. Inicia sesión."

    fun limitReached(max: Int) = "Llegaste al límite de $max caracteres"

    fun imageLimitExceeded(max: Int) = "Máximo $max imágenes; se usaron las primeras $max"

    fun recoverySent(email: String) = "Código enviado a $email"

    fun recoveryReminder(email: String) = "Revisa $email: ahí está el código para recuperar tu contraseña."
}
