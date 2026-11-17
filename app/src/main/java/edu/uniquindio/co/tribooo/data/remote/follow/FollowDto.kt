package edu.uniquindio.co.tribooo.data.remote.follow

import androidx.annotation.Keep
import edu.uniquindio.co.tribooo.data.remote.pairDocumentId
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.User

// Representación de un Follow tal como se guarda en Firestore
// (colección "follows", documento = "{followerId}_{followedId}").
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class FollowDto(
    // Usuario que sigue
    val followerId: String = "",
    // Usuario seguido
    val followedId: String = ""
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio con ambos usuarios ya resueltos
    fun toDomain(follower: User, followed: User): Follow = Follow(follower = follower, followed = followed)

    companion object {
        const val COLLECTION = "follows"

        fun documentId(followerId: String, followedId: String): String = pairDocumentId(followerId, followedId)

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(follow: Follow): FollowDto = FollowDto(
            followerId = follow.follower.id,
            followedId = follow.followed.id
        )
    }
}
