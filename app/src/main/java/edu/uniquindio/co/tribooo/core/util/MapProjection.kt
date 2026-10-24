package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.domain.model.Location

/** Margen (fracción del ancho y del alto) que se deja libre en cada borde del mapa. */
const val MAP_MARGIN = 0.1f

/** Posición en el mapa como fracción del ancho ([x], de izquierda a derecha) y del alto ([y], de arriba abajo). */
data class MapPoint(val x: Float, val y: Float)

/**
 * Proyecta coordenadas al rectángulo del mapa simulado: el rectángulo que cubre [locations]
 * se ajusta al mapa dejando [MAP_MARGIN] libre en cada borde. El norte queda arriba.
 */
class MapProjection(locations: List<Location>) {

    private val minLat = locations.minOfOrNull { it.latitude } ?: 0.0
    private val maxLat = locations.maxOfOrNull { it.latitude } ?: 0.0
    private val minLon = locations.minOfOrNull { it.longitude } ?: 0.0
    private val maxLon = locations.maxOfOrNull { it.longitude } ?: 0.0

    fun project(location: Location): MapPoint = MapPoint(
        x = place(fraction(location.longitude, minLon, maxLon)),
        y = place(1f - fraction(location.latitude, minLat, maxLat))
    )

    /** Posición relativa dentro del rango; el centro si el rango es un solo punto. */
    private fun fraction(value: Double, min: Double, max: Double): Float =
        if (max == min) 0.5f else ((value - min) / (max - min)).toFloat()

    private fun place(fraction: Float): Float = MAP_MARGIN + fraction * (1f - 2 * MAP_MARGIN)
}
