package edu.uniquindio.co.tribooo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.uniquindio.co.tribooo.BuildConfig
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinaryApi
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinaryConfig
import edu.uniquindio.co.tribooo.data.remote.cloudinary.CloudinarySigner
import edu.uniquindio.co.tribooo.data.remote.openrouter.OpenRouterApi
import edu.uniquindio.co.tribooo.data.remote.openrouter.OpenRouterAuthInterceptor
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Cliente HTTP y servicios Retrofit para las APIs externas (OpenRouter y Cloudinary). */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Los modelos gratuitos pueden tardar en responder cuando hay cola, y una subida lenta también
    private const val READ_TIMEOUT_SECONDS = 60L

    // Sin cloud name la URL base quedaría con "//"; el repositorio igual rechaza las peticiones sin configuración
    private const val MISSING_CLOUD_NAME = "missing-cloud-name"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        // No envía los campos opcionales en null: OpenRouter usa sus valores por defecto
        explicitNulls = false
    }

    /**
     * Cliente base, sin credenciales. Cada API agrega las suyas con `newBuilder()`, que comparte el pool
     * de conexiones: así la key de un servicio nunca viaja a otro.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader("Authorization")
                    }
                )
            }
        }
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideOpenRouterApi(client: OkHttpClient, json: Json): OpenRouterApi = Retrofit.Builder()
        .baseUrl(OpenRouterApi.BASE_URL)
        .client(client.newBuilder().addInterceptor(OpenRouterAuthInterceptor(BuildConfig.OPENROUTER_API_KEY)).build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OpenRouterApi::class.java)

    @Provides
    @Singleton
    fun provideCloudinaryConfig(): CloudinaryConfig = CloudinaryConfig(
        cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
        apiKey = BuildConfig.CLOUDINARY_API_KEY,
        apiSecret = BuildConfig.CLOUDINARY_API_SECRET
    )

    @Provides
    @Singleton
    fun provideCloudinarySigner(config: CloudinaryConfig): CloudinarySigner =
        CloudinarySigner(apiKey = config.apiKey, apiSecret = config.apiSecret)

    // Cloudinary firma en el cuerpo de cada petición (CloudinarySigner): el cliente no lleva credenciales
    @Provides
    @Singleton
    fun provideCloudinaryApi(client: OkHttpClient, json: Json, config: CloudinaryConfig): CloudinaryApi = Retrofit.Builder()
        .baseUrl(CloudinaryApi.baseUrl(config.cloudName.ifBlank { MISSING_CLOUD_NAME }))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(CloudinaryApi::class.java)
}
