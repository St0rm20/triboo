package edu.uniquindio.co.tribooo.data.remote.cloudinary

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

/**
 * Upload API de Cloudinary para imágenes. La URL base es [baseUrl] (incluye el cloud name) y cada petición
 * va firmada en el cuerpo (`api_key`, `timestamp`, `signature`) con [CloudinarySigner].
 * Lanza `HttpException` si el código no es 2xx.
 */
interface CloudinaryApi {

    /**
     * Sube una imagen. [file] es el binario o la URL de una imagen remota (Cloudinary la descarga);
     * [params] son los campos firmados.
     */
    @Multipart
    @POST("image/upload")
    suspend fun upload(
        @Part file: MultipartBody.Part,
        @PartMap params: Map<String, @JvmSuppressWildcards RequestBody>
    ): UploadResponse

    /** Borra una imagen por su `public_id`; responde `"not found"` si ya no existía. */
    @FormUrlEncoded
    @POST("image/destroy")
    suspend fun destroy(@FieldMap params: Map<String, String>): DestroyResponse

    companion object {
        fun baseUrl(cloudName: String) = "https://api.cloudinary.com/v1_1/$cloudName/"
    }
}
