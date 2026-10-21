package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Location
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Distancia en km hasta otra coordenada (fórmula de Haversine). */
fun Location.distanceKmTo(other: Location): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(other.latitude - latitude)
    val dLon = Math.toRadians(other.longitude - longitude)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(latitude)) * cos(Math.toRadians(other.latitude)) * sin(dLon / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(a))
}

/** "1.2 km" */
fun Double.kmLabel(): String = String.format(Locale.US, "%.1f km", this)
