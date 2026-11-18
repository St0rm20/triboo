package edu.uniquindio.co.tribooo.domain.model

/** Idioma de la app elegido por el usuario; [SYSTEM] sigue el del dispositivo. */
enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    SPANISH("es"),
    ENGLISH("en");

    companion object {
        /** Idioma de una etiqueta BCP 47 ("es", "en-US"...); vacía o desconocida → [SYSTEM]. */
        fun fromTag(tag: String): AppLanguage = when {
            tag.startsWith("es", ignoreCase = true) -> SPANISH
            tag.startsWith("en", ignoreCase = true) -> ENGLISH
            else -> SYSTEM
        }
    }
}
