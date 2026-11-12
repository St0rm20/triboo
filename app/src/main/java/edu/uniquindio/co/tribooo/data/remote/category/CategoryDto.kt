package edu.uniquindio.co.tribooo.data.remote.category

import androidx.annotation.Keep
import edu.uniquindio.co.tribooo.domain.model.Category

// Representación de una Category tal como se guarda en Firestore (colección "categories", documento = id).
// Es un catálogo: se siembra con Category.defaultCategories
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class CategoryDto(
    val id: String = "",
    // Clave estable con la que la app busca el nombre traducido en los recursos
    val key: String = "",
    // Nombre del ícono de Material Symbols
    val icon: String = "",
    // Color en hexadecimal, p. ej. "#074A24"
    val color: String = ""
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): Category = Category(
        id = id,
        key = key,
        icon = icon,
        color = color
    )

    companion object {
        const val COLLECTION = "categories"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore
        fun fromDomain(category: Category): CategoryDto = CategoryDto(
            id = category.id,
            key = category.key,
            icon = category.icon,
            color = category.color
        )
    }
}
