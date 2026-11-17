package edu.uniquindio.co.tribooo.data.remote.placesuggestion

import androidx.annotation.Keep
import com.google.firebase.firestore.GeoPoint
import edu.uniquindio.co.tribooo.data.remote.toGeoPoint
import edu.uniquindio.co.tribooo.data.remote.toLocation
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion

// Lugar sugerido en el selector de ubicación, tal como se guarda en Firestore (colección "placeSuggestions").
// Es un catálogo; el modelo de dominio no tiene id, así que el id solo existe en Firestore
// Todos los campos tienen valor por defecto para garantizar el constructor vacío
// que Firestore necesita al convertir un documento con toObject()
@Keep
data class PlaceSuggestionDto(
    val id: String = "",
    val name: String = "",
    val neighborhood: String = "",
    val detail: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0)
) {
    // Convierte el DTO (datos de Firestore) al modelo de dominio
    fun toDomain(): PlaceSuggestion = PlaceSuggestion(
        name = name,
        neighborhood = neighborhood,
        detail = detail,
        location = location.toLocation()
    )

    companion object {
        const val COLLECTION = "placeSuggestions"

        // Convierte el modelo de dominio al DTO que se guardará en Firestore; [id] lo asigna quien siembra el catálogo
        fun fromDomain(place: PlaceSuggestion, id: String = ""): PlaceSuggestionDto = PlaceSuggestionDto(
            id = id,
            name = place.name,
            neighborhood = place.neighborhood,
            detail = place.detail,
            location = place.location.toGeoPoint()
        )
    }
}
