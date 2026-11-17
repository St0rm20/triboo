package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.AuthAccount

/**
 * Credenciales de los usuarios (email/contraseña y Google). Las contraseñas nunca pasan por el [UserRepository]:
 * las guarda y valida el proveedor de identidad. Todas las operaciones lanzan
 * [edu.uniquindio.co.tribooo.domain.model.AuthException] si fallan.
 */
interface AuthRepository {
    /** Crea la cuenta con email y contraseña; el proveedor deja la sesión abierta. */
    suspend fun register(email: String, password: String): AuthAccount

    suspend fun login(email: String, password: String): AuthAccount

    /** Inicia sesión (o crea la cuenta la primera vez) con el ID token que entrega Google. */
    suspend fun loginWithGoogle(idToken: String): AuthAccount

    /** Envía al correo el enlace para restablecer la contraseña. */
    suspend fun sendPasswordResetEmail(email: String)

    /**
     * Falla con [edu.uniquindio.co.tribooo.domain.model.AuthErrorReason.RECENT_LOGIN_REQUIRED] si la sesión no es
     * lo bastante reciente para eliminar la cuenta. Permite comprobarlo antes de borrar los datos del usuario.
     */
    suspend fun ensureRecentLogin()

    /** Elimina la cuenta con sesión abierta; puede exigir haber iniciado sesión hace poco. */
    suspend fun deleteCurrentAccount()

    fun logout()
}
