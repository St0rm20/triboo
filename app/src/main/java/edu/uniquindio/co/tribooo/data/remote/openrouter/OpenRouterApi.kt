package edu.uniquindio.co.tribooo.data.remote.openrouter

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API de OpenRouter (compatible con OpenAI). La URL base es [BASE_URL] y la autenticación
 * (`Authorization: Bearer`) la agrega [OpenRouterAuthInterceptor].
 */
interface OpenRouterApi {

    /** Respuesta completa, sin streaming. Lanza `HttpException` si el código no es 2xx. */
    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse

    companion object {
        const val BASE_URL = "https://openrouter.ai/api/v1/"
    }
}
