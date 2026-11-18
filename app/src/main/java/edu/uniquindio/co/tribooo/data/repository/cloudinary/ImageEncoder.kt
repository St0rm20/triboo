package edu.uniquindio.co.tribooo.data.repository.cloudinary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/** Imagen lista para subir: los bytes y su tipo MIME. */
class EncodedImage(val bytes: ByteArray, val mimeType: String, val fileName: String)

/**
 * Lee una imagen local y la reduce antes de subirla: las fotos de la cámara pesan varios MB y el plan
 * gratuito de Cloudinary limita el tamaño de cada archivo. ImageDecoder aplica la rotación EXIF
 * y entiende HEIC, así que el resultado siempre es JPEG (o PNG si tiene transparencia).
 */
class ImageEncoder @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Lanza `IOException` si la URI no existe o no es una imagen. */
    suspend fun encode(uri: String): EncodedImage = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val (width, height) = info.size.width to info.size.height
            val longest = max(width, height)
            if (longest > MAX_DIMENSION) {
                val scale = MAX_DIMENSION.toFloat() / longest
                decoder.setTargetSize((width * scale).roundToInt().coerceAtLeast(1), (height * scale).roundToInt().coerceAtLeast(1))
            }
            // Software: un bitmap de hardware no se puede comprimir
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        try {
            val png = bitmap.hasAlpha()
            val bytes = ByteArrayOutputStream().use { out ->
                bitmap.compress(if (png) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                out.toByteArray()
            }
            if (png) EncodedImage(bytes, "image/png", "image.png") else EncodedImage(bytes, "image/jpeg", "image.jpg")
        } finally {
            bitmap.recycle()
        }
    }

    private companion object {
        // Suficiente para verse nítida a pantalla completa en un teléfono
        const val MAX_DIMENSION = 1920
        const val JPEG_QUALITY = 85
    }
}
