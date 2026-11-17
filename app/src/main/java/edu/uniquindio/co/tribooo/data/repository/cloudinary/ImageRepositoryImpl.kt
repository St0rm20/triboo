package edu.uniquindio.co.tribooo.data.repository.cloudinary

import android.util.Log
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinaryApi
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinaryConfig
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinarySigner
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinaryUrls
import edu.uniquindio.co.tribooo.domain.model.DataErrorReason
import edu.uniquindio.co.tribooo.domain.model.DataException
import edu.uniquindio.co.tribooo.domain.model.ImageFolder
import edu.uniquindio.co.tribooo.domain.repository.ImageRepository
import jakarta.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

/**
 * [ImageRepository] sobre Cloudinary. Cada imagen recibe un `public_id` único dentro de
 * `tribooo/<carpeta>/`, y se borra recuperando ese `public_id` de su URL ([CloudinaryUrls]).
 */
class ImageRepositoryImpl @Inject constructor(
    private val api: CloudinaryApi,
    private val signer: CloudinarySigner,
    private val config: CloudinaryConfig,
    private val encoder: ImageEncoder
) : ImageRepository {

    override suspend fun upload(source: String, folder: ImageFolder): String {
        ensureConfigured()
        val file = if (source.isRemote()) {
            // Cloudinary descarga la imagen él mismo: sirve para copiar una que ya está alojada
            MultipartBody.Part.createFormData(FILE, source)
        } else {
            val image = try {
                encoder.encode(source)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // La URI ya no existe o no es una imagen: no es un fallo de red
                throw DataException(DataErrorReason.NOT_FOUND, e)
            }
            MultipartBody.Part.createFormData(FILE, image.fileName, image.bytes.toRequestBody(image.mimeType.toMediaType()))
        }
        val params = signer.sign(mapOf(PUBLIC_ID to newPublicId(folder)))
        return cloudinaryCall { api.upload(file, params.mapValues { (_, value) -> value.toPart() }) }.secureUrl
    }

    override suspend fun delete(url: String) {
        val publicId = CloudinaryUrls.publicIdOf(url, config.cloudName) ?: return
        ensureConfigured()
        // invalidate: la CDN deja de entregar la copia en caché
        val response = cloudinaryCall { api.destroy(signer.sign(mapOf(PUBLIC_ID to publicId, INVALIDATE to "true"))) }
        if (!response.isDeleted) throw DataException(DataErrorReason.UNKNOWN)
    }

    override fun isHosted(url: String): Boolean = CloudinaryUrls.isHosted(url, config.cloudName)

    private fun ensureConfigured() {
        if (!config.isConfigured) {
            Log.w(TAG, "Faltan CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY o CLOUDINARY_API_SECRET en local.properties")
            throw DataException(DataErrorReason.UNKNOWN)
        }
    }

    private fun newPublicId(folder: ImageFolder): String =
        "${CloudinaryConfig.ROOT_FOLDER}/${FOLDER_NAMES.getValue(folder)}/${UUID.randomUUID()}"

    private fun String.isRemote(): Boolean = startsWith("https://") || startsWith("http://")

    private fun String.toPart(): RequestBody = toRequestBody(TEXT_PLAIN)

    private suspend fun <T> cloudinaryCall(block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        // El motivo viene en la cabecera X-Cld-Error; solo se registra, el usuario ve un mensaje genérico
        Log.w(TAG, "Cloudinary ${e.code()}: ${e.response()?.headers()?.get("X-Cld-Error")}")
        throw DataException(e.code().toDataErrorReason(), e)
    } catch (e: IOException) {
        throw DataException(DataErrorReason.NETWORK, e)
    } catch (e: Exception) {
        throw DataException(DataErrorReason.UNKNOWN, e)
    }

    private fun Int.toDataErrorReason(): DataErrorReason = when (this) {
        401, 403 -> DataErrorReason.PERMISSION_DENIED
        404 -> DataErrorReason.NOT_FOUND
        408 -> DataErrorReason.NETWORK
        // Cloudinary responde 420 al superar el límite de peticiones
        420, 429 -> DataErrorReason.RATE_LIMITED
        else -> DataErrorReason.UNKNOWN
    }

    private companion object {
        const val TAG = "Cloudinary"
        const val FILE = "file"
        const val PUBLIC_ID = "public_id"
        const val INVALIDATE = "invalidate"
        val TEXT_PLAIN = "text/plain".toMediaType()

        val FOLDER_NAMES = mapOf(
            ImageFolder.EVENTS to "events",
            ImageFolder.PROFILES to "profiles"
        )
    }
}
