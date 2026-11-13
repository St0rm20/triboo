package edu.uniquindio.co.tribooo.data.remote.openrouter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Cuerpo de `POST /chat/completions` (solo los campos que usa la app). */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    /** Modelos de respaldo, en orden: OpenRouter pasa al siguiente si uno falla o no está disponible. */
    val models: List<String>? = null,
    val messages: List<ChatMessageDto>,
    val temperature: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    /** Solo aplica a modelos con razonamiento; los demás lo ignoran. */
    val reasoning: ReasoningDto? = null
)

/** Con `enabled = false` el modelo responde directo, sin gastar tokens en razonar. */
@Serializable
data class ReasoningDto(val enabled: Boolean)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
) {
    companion object {
        fun system(content: String) = ChatMessageDto(role = "system", content = content)
        fun user(content: String) = ChatMessageDto(role = "user", content = content)
    }
}

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<ChatChoiceDto> = emptyList()
)

@Serializable
data class ChatChoiceDto(
    val index: Int = 0,
    @SerialName("finish_reason") val finishReason: String? = null,
    val message: ChatResponseMessageDto
)

@Serializable
data class ChatResponseMessageDto(
    val role: String,
    /** Texto plano o, en algunos proveedores, una lista de partes `{type, text}`. */
    val content: JsonElement? = null
) {
    /** Texto de la respuesta sin importar en cuál de las dos formas llegó; null si no trae texto. */
    fun text(): String? = when (val value = content) {
        is JsonPrimitive -> value.contentOrNull
        is JsonArray -> value.mapNotNull { part -> (part as? JsonObject)?.get("text")?.jsonPrimitive?.contentOrNull }
            .joinToString("")
        else -> null
    }?.trim()?.ifEmpty { null }
}
