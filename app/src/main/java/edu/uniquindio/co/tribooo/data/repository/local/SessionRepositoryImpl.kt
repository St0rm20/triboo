package edu.uniquindio.co.tribooo.data.repository.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.model.UserSession
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/** La sesión se guarda con DataStore: sobrevive al cierre de la app hasta que el usuario la cierre. */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SessionRepository {

    // Claves para las preferencias
    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
    }

    // Flujo para observar los datos de la sesión
    override val session: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        val userId = prefs[Keys.USER_ID]
        val role = prefs[Keys.ROLE]?.let { name -> UserRole.entries.firstOrNull { it.name == name } }

        // Si falta algún dato (o el rol guardado ya no existe) no hay sesión
        if (userId.isNullOrBlank() || role == null) null else UserSession(userId = userId, role = role)
    }

    override suspend fun saveSession(userId: String, role: UserRole) {
        // Guarda los datos de la sesión en DataStore (clave-valor)
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
            prefs[Keys.ROLE] = role.name
        }
    }

    override suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
