package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.AuthAccount
import edu.uniquindio.co.tribooo.domain.model.AuthErrorReason
import edu.uniquindio.co.tribooo.domain.model.AuthException
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * Firebase Authentication en memoria para las pruebas. Trae la cuenta de Daniela ("u-daniela", "123456"),
 * que coincide con el perfil sembrado en los datos de prueba.
 */
class FakeAuthRepository : AuthRepository {

    private val accounts = mutableMapOf<String, Pair<AuthAccount, String>>()

    /** ID tokens de Google aceptados y la cuenta a la que pertenecen. */
    val googleAccounts = mutableMapOf<String, AuthAccount>()

    /** Demora simulada del envío del correo de recuperación, para ver el estado "enviando". */
    var resetEmailLatencyMillis = 0L

    /** Si no es null, la siguiente operación falla con esta causa. */
    var failNextWith: AuthErrorReason? = null

    var currentAccount: AuthAccount? = null
        private set

    var deletedAccounts = mutableListOf<String>()
        private set

    init {
        addAccount("u-daniela", "daniela.restrepo@triboo.co", "123456")
    }

    fun addAccount(uid: String, email: String, password: String) {
        accounts[email.lowercase()] = AuthAccount(uid = uid, email = email) to password
    }

    fun signInAs(account: AuthAccount) {
        currentAccount = account
    }

    private fun failIfRequested() {
        failNextWith?.let {
            failNextWith = null
            throw AuthException(it)
        }
    }

    override suspend fun register(email: String, password: String): AuthAccount {
        failIfRequested()
        if (accounts.containsKey(email.lowercase())) throw AuthException(AuthErrorReason.EMAIL_ALREADY_IN_USE)
        val account = AuthAccount(uid = "auth-${accounts.size + 1}", email = email)
        accounts[email.lowercase()] = account to password
        currentAccount = account
        return account
    }

    override suspend fun login(email: String, password: String): AuthAccount {
        failIfRequested()
        val (account, stored) = accounts[email.lowercase()] ?: throw AuthException(AuthErrorReason.INVALID_CREDENTIALS)
        if (stored != password) throw AuthException(AuthErrorReason.INVALID_CREDENTIALS)
        currentAccount = account
        return account
    }

    override suspend fun loginWithGoogle(idToken: String): AuthAccount {
        failIfRequested()
        val account = googleAccounts[idToken] ?: throw AuthException(AuthErrorReason.INVALID_CREDENTIALS)
        currentAccount = account
        return account
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        delay(resetEmailLatencyMillis)
        failIfRequested()
    }

    override suspend fun deleteCurrentAccount() {
        failIfRequested()
        val account = currentAccount ?: throw AuthException(AuthErrorReason.UNKNOWN)
        accounts.remove(account.email.lowercase())
        deletedAccounts += account.uid
        currentAccount = null
    }

    override fun logout() {
        currentAccount = null
    }
}
