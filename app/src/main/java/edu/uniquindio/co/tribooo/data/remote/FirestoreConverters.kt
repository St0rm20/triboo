package edu.uniquindio.co.tribooo.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import edu.uniquindio.co.tribooo.domain.model.Location
import java.time.Instant

// Conversiones entre los tipos nativos de Firestore y los del dominio, compartidas por todos los DTO

/** Las fechas se guardan como Timestamp de Firestore para poder ordenar y filtrar por ellas en las consultas. */
internal fun Instant.toTimestamp(): Timestamp = Timestamp(this)

/** Las coordenadas se guardan como GeoPoint, el tipo nativo de Firestore para ubicaciones. */
internal fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

internal fun GeoPoint.toLocation(): Location = Location(latitude = latitude, longitude = longitude)

/** Id determinista de los documentos que relacionan a un usuario con un evento: evita duplicados sin consultar antes. */
internal fun pairDocumentId(firstId: String, secondId: String): String = "${firstId}_${secondId}"
