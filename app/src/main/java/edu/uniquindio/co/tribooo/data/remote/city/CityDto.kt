package edu.uniquindio.co.tribooo.data.remote.city

import androidx.annotation.Keep

// Ciudad disponible en el registro y en el perfil, tal como se guarda en Firestore (colección "cities").
// Es un catálogo; en el dominio una ciudad es solo su nombre
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class CityDto(
    val id: String = "",
    val name: String = ""
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): String = name

    companion object {
        const val COLLECTION = "cities"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore; el id es el nombre en minúsculas
        fun fromDomain(name: String): CityDto = CityDto(id = name.trim().lowercase(), name = name.trim())
    }
}
