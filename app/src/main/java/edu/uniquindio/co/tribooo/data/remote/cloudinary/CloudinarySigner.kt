package edu.uniquindio.co.tribooo.data.remote.cloudinary

import java.security.MessageDigest

/**
 * Firma las peticiones de la Upload API: SHA-1 en hexadecimal de los parámetros ordenados por nombre
 * (`a=1&b=2`) seguidos del API secret. `file`, `cloud_name`, `resource_type` y `api_key` no se firman.
 * Cloudinary acepta la firma durante una hora desde su `timestamp`.
 *
 * Nota: Cloudinary recomienda firmar en un servidor; aquí el secret viaja dentro del APK.
 */
class CloudinarySigner(
    private val apiKey: String,
    private val apiSecret: String,
    /** Segundos Unix; se inyecta para poder probar la firma. */
    private val clock: () -> Long = { System.currentTimeMillis() / 1000 }
) {
    /** [params] más `timestamp`, `api_key` y `signature`, listos para enviar. */
    fun sign(params: Map<String, String>): Map<String, String> {
        val signed = params + (TIMESTAMP to clock().toString())
        return signed + mapOf(API_KEY to apiKey, SIGNATURE to signatureOf(signed))
    }

    internal fun signatureOf(params: Map<String, String>): String {
        val toSign = params
            .filterKeys { it !in UNSIGNED_PARAMS }
            .toSortedMap()
            .entries
            .joinToString("&") { (name, value) -> "$name=$value" }
        return MessageDigest.getInstance("SHA-1")
            .digest((toSign + apiSecret).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val TIMESTAMP = "timestamp"
        const val API_KEY = "api_key"
        const val SIGNATURE = "signature"
        val UNSIGNED_PARAMS = setOf("file", "cloud_name", "resource_type", API_KEY)
    }
}
