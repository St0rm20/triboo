package edu.uniquindio.co.tribooo.data.remote

import com.google.firebase.auth.FirebaseAuth
import edu.uniquindio.co.tribooo.di.ApplicationScope
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn

/**
 * Sesión de Firebase Authentication vista desde la capa de datos. Las reglas de Firestore exigen sesión,
 * así que los repositorios registran sus listeners según [uid] y los vuelven a registrar al cambiar de usuario.
 */
@Singleton
class AuthState @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationScope scope: CoroutineScope
) {
    /** Uid del usuario con sesión abierta; null sin sesión. */
    val uid: StateFlow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, auth.currentUser?.uid)

    /** Correo de la sesión actual: vive solo en Firebase Authentication, no en Firestore. */
    val email: String?
        get() = auth.currentUser?.email
}
