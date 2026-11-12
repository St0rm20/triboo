package edu.uniquindio.co.tribooo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import jakarta.inject.Inject

/** Punto de entrada de Hilt: genera el contenedor de dependencias de toda la app. */
@HiltAndroidApp
class TriboooApplication : Application() {

    // Se inyecta al arrancar para que los catálogos (ciudades y lugares) empiecen a descargarse
    // mientras se muestra el splash, antes de que el registro o el formulario de eventos los necesiten
    @Inject
    lateinit var locationRepository: LocationRepository
}
