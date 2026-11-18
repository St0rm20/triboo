package edu.uniquindio.co.tribooo.data.remote.cloudinary

/**
 * URLs de entrega de Cloudinary: `https://res.cloudinary.com/<cloud>/image/upload/v<versión>/<public_id>.<formato>`.
 * La app guarda la `secure_url` sin transformaciones, así que el `public_id` se puede recuperar de ella
 * y Firestore no necesita guardarlo aparte.
 */
object CloudinaryUrls {

    /** `public_id` de una imagen subida a [cloudName]; null si [url] no es de esa cuenta. */
    fun publicIdOf(url: String, cloudName: String): String? {
        if (cloudName.isBlank()) return null
        val prefix = "https://res.cloudinary.com/$cloudName/image/upload/"
        if (!url.startsWith(prefix)) return null
        return PATH_PATTERN.matchEntire(url.removePrefix(prefix).substringBefore('?'))?.groupValues?.get(1)
    }

    fun isHosted(url: String, cloudName: String): Boolean = publicIdOf(url, cloudName) != null

    // Versión opcional ("v1712345678/") y extensión opcional; el public_id puede tener carpetas ("tribooo/events/…")
    private val PATH_PATTERN = Regex("""(?:v\d+/)?([^.]+?)(?:\.[A-Za-z0-9]+)?""")
}
