package edu.uniquindio.co.tribooo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.uniquindio.co.tribooo.BuildConfig
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

/** Cliente HTTP y servicios Retrofit para las APIs externas (por ahora, OpenRouter). */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Los modelos gratuitos pueden tardar en responder cuando hay cola
    private const val READ_TIMEOUT_SECONDS = 60L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        // No envía los campos opcionales en null: OpenRouter usa sus valores por defecto
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(OpenRouterAuthInterceptor(BuildConfig.OPENROUTER_API_KEY))
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
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OpenRouterApi::class.java)
}
