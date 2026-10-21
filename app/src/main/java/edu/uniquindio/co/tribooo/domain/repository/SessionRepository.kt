package edu.uniquindio.co.tribooo.domain.repository

import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

/** Sesión guardada en el dispositivo (null si nadie ha iniciado sesión). */
interface SessionRepository {
    val session: Flow<UserSession?>
    suspend fun saveSession(userId: String, role: UserRole)
    suspend fun clearSession()
}
