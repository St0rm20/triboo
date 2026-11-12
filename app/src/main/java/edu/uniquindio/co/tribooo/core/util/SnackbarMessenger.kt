package edu.uniquindio.co.tribooo.core.util

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update

/** Pantallas que reciben avisos enviados desde otra que se cierra antes de mostrarlos. */
enum class MessageTarget { LOGIN, FEED }

/**
 * Entrega un aviso una sola vez a la pantalla de destino. Quien lo envía publica y navega sin
 * esperar: el aviso queda pendiente hasta que [messages] del destino lo consume, aunque todavía
 * no esté en pantalla.
 */
@Singleton
class SnackbarMessenger @Inject constructor() {

    private val pending = MutableStateFlow<Map<MessageTarget, List<String>>>(emptyMap())

    fun post(target: MessageTarget, message: String) {
        pending.update { it + (target to it[target].orEmpty() + message) }
    }

    /** Emite los avisos pendientes de [target] y los que lleguen después; cada uno se entrega una vez. */
    fun messages(target: MessageTarget): Flow<String> = pending.transform {
        take(target).forEach { message -> emit(message) }
    }

    private fun take(target: MessageTarget): List<String> {
        while (true) {
            val current = pending.value
            val messages = current[target] ?: return emptyList()
            if (pending.compareAndSet(current, current - target)) return messages
        }
    }
}
