package edu.uniquindio.co.tribooo.data.remote.openrouter

import okhttp3.Interceptor
import okhttp3.Response

/** Agrega la API key y el nombre con el que la app aparece en el panel de OpenRouter. */
class OpenRouterAuthInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .header("X-OpenRouter-Title", APP_TITLE)
            .build()
    )

    private companion object {
        const val APP_TITLE = "Tribooo"
    }
}
