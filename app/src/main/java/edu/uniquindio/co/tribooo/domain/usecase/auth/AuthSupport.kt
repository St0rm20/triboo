package edu.uniquindio.co.tribooo.domain.usecase.auth

import edu.uniquindio.co.tribooo.domain.model.AuthAccount
import edu.uniquindio.co.tribooo.domain.model.AuthException
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import kotlin.coroutines.cancellation.CancellationException

/** Ejecuta [block] y entrega como [Result.failure] solo los fallos de autenticación; la cancelación se propaga. */
internal inline fun <T> authResult(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: AuthException) {
    Result.failure(e)
}

/**
 * Perfil de la cuenta autenticada. Si aún no existe (primer ingreso con Google, o un perfil que se perdió)
 * se crea con los datos que entrega el proveedor; ciudad y barrio quedan vacíos para completarlos en Perfil.
 */
internal suspend fun UserRepository.findOrCreateProfile(account: AuthAccount): User {
    findById(account.uid)?.let { return it }
    val user = User(
        id = account.uid,
        name = account.displayName?.takeIf { it.isNotBlank() } ?: account.email.substringBefore('@'),
        email = account.email,
        city = "",
        neighborhood = "",
        photo = account.photoUrl
    )
    save(user)
    return user
}
