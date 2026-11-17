package edu.uniquindio.co.tribooo.data.remote.user

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import edu.uniquindio.co.tribooo.data.remote.toTimestamp
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole

// Representación de un User tal como se guarda en Firestore (colección "users", documento = uid de Firebase Auth).
// No incluye contraseña ni correo: los guarda Firebase Authentication. Así ningún usuario puede leer
// el correo de otro al escuchar la colección.
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class UserDto(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val neighborhood: String = "",
    val bio: String? = null,
    val photo: String? = null,
    val role: String = UserRole.USER.name,
    val dateRegistered: Timestamp = Timestamp.now()
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio; [email] llega de Firebase Authentication
    fun toDomain(email: String): User = User(
        id = id,
        name = name,
        email = email,
        city = city,
        neighborhood = neighborhood,
        bio = bio,
        photo = photo,
        // Si llega un rol desconocido, se usa USER en lugar de romper la aplicación
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
        dateRegistered = dateRegistered.toInstant()
    )

    companion object {
        const val COLLECTION = "users"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(user: User): UserDto = UserDto(
            id = user.id,
            name = user.name,
            city = user.city,
            neighborhood = user.neighborhood,
            bio = user.bio,
            photo = user.photo,
            role = user.role.name,
            dateRegistered = user.dateRegistered.toTimestamp()
        )
    }
}
