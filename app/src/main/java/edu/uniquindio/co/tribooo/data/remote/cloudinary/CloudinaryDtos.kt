package edu.uniquindio.co.tribooo.data.remote.cloudinary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Respuesta de `POST /image/upload` (solo los campos que usa la app). */
@Serializable
data class UploadResponse(
    @SerialName("public_id") val publicId: String,
    /** URL HTTPS de entrega; es la que se guarda en Firestore. */
    @SerialName("secure_url") val secureUrl: String,
    val version: Long? = null,
    val format: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val bytes: Long? = null
)

/** Respuesta de `POST /image/destroy`. */
@Serializable
data class DestroyResponse(val result: String) {
    /** `"not found"` también cuenta: la imagen ya no está, que es lo que se buscaba. */
    val isDeleted: Boolean get() = result == RESULT_OK || result == RESULT_NOT_FOUND

    private companion object {
        const val RESULT_OK = "ok"
        const val RESULT_NOT_FOUND = "not found"
    }
}
