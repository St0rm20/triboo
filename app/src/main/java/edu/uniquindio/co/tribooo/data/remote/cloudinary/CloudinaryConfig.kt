package edu.uniquindio.co.tribooo.data.remote.cloudinary

/** Credenciales de la cuenta de Cloudinary (se leen de local.properties a través de BuildConfig). */
class CloudinaryConfig(
    val cloudName: String,
    val apiKey: String,
    val apiSecret: String
) {
    val isConfigured: Boolean get() = cloudName.isNotBlank() && apiKey.isNotBlank() && apiSecret.isNotBlank()

    companion object {
        /** Carpeta raíz de la app dentro de la cuenta: todos los `public_id` empiezan por ella. */
        const val ROOT_FOLDER = "tribooo"
    }
}
