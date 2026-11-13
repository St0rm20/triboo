package edu.uniquindio.co.tribooo.data.repository.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import edu.uniquindio.co.tribooo.domain.model.AuthAccount
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.AuthException
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/** Autenticación con Firebase Authentication: email/contraseña y Google (ID token de Credential Manager). */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun register(email: String, password: String): AuthAccount = firebaseCall {
        auth.createUserWithEmailAndPassword(email, password).await().requireAccount()
    }

    override suspend fun login(email: String, password: String): AuthAccount = firebaseCall {
        auth.signInWithEmailAndPassword(email, password).await().requireAccount()
    }

    override suspend fun loginWithGoogle(idToken: String): AuthAccount = firebaseCall {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await().requireAccount()
    }

    override suspend fun sendPasswordResetEmail(email: String) = firebaseCall {
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    // Firebase exige un inicio de sesión de hace menos de 5 minutos; se deja margen para borrar los datos antes
    override suspend fun ensureRecentLogin() {
        val user = auth.currentUser ?: throw AuthException(AuthErrorReason.UNKNOWN)
        val lastSignIn = user.metadata?.lastSignInTimestamp ?: 0L
        if (System.currentTimeMillis() - lastSignIn > RECENT_LOGIN_MILLIS) {
            throw AuthException(AuthErrorReason.RECENT_LOGIN_REQUIRED)
        }
    }

    override suspend fun deleteCurrentAccount() = firebaseCall {
        val user = auth.currentUser ?: throw AuthException(AuthErrorReason.UNKNOWN)
        user.delete().await()
        Unit
    }

    override fun logout() = auth.signOut()

    /** Traduce las excepciones de Firebase a [AuthException] para que el dominio no dependa de Firebase. */
    private suspend inline fun <T> firebaseCall(block: () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: AuthException) {
        throw e
    } catch (e: Exception) {
        throw AuthException(e.toReason(), e)
    }

    private fun AuthResult.requireAccount(): AuthAccount =
        user?.toAccount() ?: throw AuthException(AuthErrorReason.UNKNOWN)

    private fun FirebaseUser.toAccount() = AuthAccount(
        uid = uid,
        email = email.orEmpty(),
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )

    // El orden importa: las excepciones más específicas heredan de las más generales
    private fun Exception.toReason(): AuthErrorReason = when (this) {
        is FirebaseAuthWeakPasswordException -> AuthErrorReason.WEAK_PASSWORD
        is FirebaseAuthRecentLoginRequiredException -> AuthErrorReason.RECENT_LOGIN_REQUIRED
        is FirebaseAuthUserCollisionException -> AuthErrorReason.EMAIL_ALREADY_IN_USE
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AuthErrorReason.INVALID_CREDENTIALS
        is FirebaseNetworkException -> AuthErrorReason.NETWORK
        else -> AuthErrorReason.UNKNOWN
    }
}

private const val RECENT_LOGIN_MILLIS = 4 * 60 * 1000L
