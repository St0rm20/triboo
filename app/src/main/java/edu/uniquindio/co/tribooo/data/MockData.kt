package edu.uniquindio.co.tribooo.data

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.User
import java.time.LocalDate
import java.time.LocalTime

/** Lugar sugerido para el selector de ubicación al crear un evento. */
data class PlaceSuggestion(
    val name: String,
    val neighborhood: String,
    val detail: String,
    val location: Location
)

/**
 * Datos de prueba de la aplicación (mientras no exista backend).
 * Las fechas de los eventos son relativas al día actual para que el feed siempre tenga eventos próximos.
 */
object MockData {

    /** Ubicación del usuario actual (centro de Armenia). Se usa para calcular distancias. */
    val currentLocation = Location(latitude = 4.5339, longitude = -75.6811)

    /** Usuario con sesión iniciada. */
    val currentUser = User(
        id = "u-daniela",
        name = "Daniela Restrepo",
        email = "daniela.restrepo@triboo.co",
        password = "123456",
        city = "Armenia",
        neighborhood = "Granada",
        bio = "Organizo ciclopaseos y bazares en el barrio."
    )

    private fun organizer(id: String, name: String, neighborhood: String) = User(
        id = id,
        name = name,
        email = "$id@triboo.co",
        password = "123456",
        city = "Armenia",
        neighborhood = neighborhood
    )

    private val museoQuimbaya = organizer("u-museo", "Museo Quimbaya", "Av. Bolívar")
    private val senderistas = organizer("u-senderistas", "Senderistas Armenia", "Norte")
    private val jacLosQuindos = organizer("u-jac-quindos", "Junta de Acción Comunal Los Quindos", "Los Quindos")
    private val mesaCultural = organizer("u-mesa-cultural", "Mesa Cultural Armenia", "Centro")
    private val uqLabs = organizer("u-uqlabs", "Semillero UQ Labs", "Universidad")
    private val camaraComercio = organizer("u-camara", "Cámara de Comercio", "Centro")
    private val colectivoBambuco = organizer("u-bambuco", "Colectivo Bambuco", "Centro")
    private val redAmbiental = organizer("u-red-ambiental", "Red Ambiental Quindío", "La Florida")
    private val armeniaCivica = organizer("u-armenia-civica", "Armenia Cívica", "Norte")

    /** Vecinos con nombre, usados como primeros asistentes (avatares del feed). */
    private val neighbors = listOf(
        "María Jaramillo", "Camilo Arango", "Felipe López", "Natalia Ríos", "Tomás Cardona",
        "Sara Valencia", "Luisa Mejía", "Juan Castaño", "Andrés Gómez", "Manuela Pineda",
        "Gloria Ospina", "Karen Muñoz", "Bruno Rendón", "Valeria Quintero", "Laura Toro"
    ).mapIndexed { i, name ->
        User(
            id = "u-vecino-$i",
            name = name,
            email = "vecino$i@triboo.co",
            password = "123456",
            city = "Armenia",
            neighborhood = "Centro"
        )
    }

    val users: List<User> = listOf(
        currentUser, museoQuimbaya, senderistas, jacLosQuindos, mesaCultural, uqLabs,
        camaraComercio, colectivoBambuco, redAmbiental, armeniaCivica
    ) + neighbors

    private fun event(
        id: String,
        title: String,
        category: Category,
        daysAhead: Long,
        start: LocalTime,
        end: LocalTime,
        place: String,
        neighborhood: String,
        location: Location,
        organizer: User,
        description: String,
        durationDays: Long = 0,
        cupo: Int? = null,
        contribution: Contribution? = null,
        status: EventStatus = EventStatus.ACTIVO
    ): Event {
        val day = LocalDate.now(APP_ZONE).plusDays(daysAhead)
        val startDate = day.atTime(start).atZone(APP_ZONE).toInstant()
        return Event(
            id = id,
            title = title,
            description = description,
            startDate = startDate,
            endDate = day.plusDays(durationDays).atTime(end).atZone(APP_ZONE).toInstant(),
            multidia = durationDays > 0,
            place = place,
            neighborhood = neighborhood,
            location = location,
            cupo = cupo,
            contribution = contribution,
            status = status,
            verified = status == EventStatus.ACTIVO,
            publicationDate = startDate.minusSeconds(10L * 24 * 3600),
            organizer = organizer,
            category = category
        )
    }

    val events: List<Event> = listOf(
        event(
            id = "7", title = "Exposición: Memoria del Paisaje Cafetero", category = Category.CULTURA,
            daysAhead = 1, durationDays = 25, start = LocalTime.of(10, 0), end = LocalTime.of(18, 0),
            place = "Museo del Oro Quimbaya", neighborhood = "Av. Bolívar", location = Location(4.5504, -75.6672),
            organizer = museoQuimbaya,
            description = "Fotografías, mapas y objetos que cuentan cómo el café transformó el paisaje del Quindío. Entrada libre de martes a domingo."
        ),
        event(
            id = "10", title = "Bazar Solidario Barrio Granada", category = Category.SOCIAL,
            daysAhead = 3, start = LocalTime.of(14, 0), end = LocalTime.of(20, 0),
            place = "Salón comunal Granada", neighborhood = "Granada", location = Location(4.5271, -75.6836),
            organizer = currentUser,
            contribution = Contribution(5000.0, "Mercados para 40 familias del barrio Granada"),
            description = "Comidas típicas, ropa de segunda y juegos para niños. Lo recaudado se destina a mercados para familias del barrio."
        ),
        event(
            id = "6", title = "Caminata Ecológica Cerro El Mirador", category = Category.DEPORTES,
            daysAhead = 4, start = LocalTime.of(6, 30), end = LocalTime.of(10, 0),
            place = "Entrada Cerro El Mirador", neighborhood = "Vía Circasia", location = Location(4.5770, -75.6654),
            organizer = senderistas,
            description = "Caminata de dificultad media con guía local y avistamiento de aves. Trae agua, bloqueador y zapatos con buen agarre."
        ),
        event(
            id = "15", title = "Bingo Bailable de la Junta Comunal", category = Category.SOCIAL,
            daysAhead = 8, start = LocalTime.of(19, 0), end = LocalTime.of(23, 0),
            place = "Salón comunal Los Quindos", neighborhood = "Los Quindos", location = Location(4.5293, -75.6938),
            organizer = jacLosQuindos, cupo = 180,
            contribution = Contribution(10000.0, "Fondo de mejoras del salón comunal Los Quindos"),
            description = "Bingo con premios donados por el comercio del barrio y orquesta en vivo al final de la noche."
        ),
        event(
            id = "1", title = "Ciclopaseo Nocturno del Quindío", category = Category.DEPORTES,
            daysAhead = 10, start = LocalTime.of(18, 0), end = LocalTime.of(20, 30),
            place = "Parque de la Vida", neighborhood = "Norte", location = Location(4.5393, -75.6717),
            organizer = currentUser, cupo = 120,
            description = "Recorrido nocturno de 18 km por la Av. Bolívar y el Parque de la Vida, a ritmo tranquilo y con acompañamiento de la Secretaría de Tránsito. Trae luces delanteras y traseras, casco y agua. Hay mecánico de apoyo al final del pelotón."
        ),
        event(
            id = "2", title = "Feria del Café de la Calle Real", category = Category.SOCIAL,
            daysAhead = 11, start = LocalTime.of(9, 0), end = LocalTime.of(17, 0),
            place = "Calle Real", neighborhood = "Centro", location = Location(4.5309, -75.6642),
            organizer = mesaCultural,
            description = "Más de 60 caficultores del Quindío con catación abierta, tostión en vivo y música de la Escuela de Bambuco. Entrada libre para toda la familia; la vía se cierra al tráfico desde las 8:00."
        ),
        event(
            id = "3", title = "Taller de Prototipado Rápido", category = Category.ACADEMICO,
            daysAhead = 14, start = LocalTime.of(15, 0), end = LocalTime.of(18, 0),
            place = "Uniquindío, Bloque de Ingeniería", neighborhood = "Universidad", location = Location(4.5596, -75.6662),
            organizer = uqLabs, cupo = 40,
            description = "Taller práctico de prototipado rápido para proyectos comunitarios: del problema al prototipo en papel en tres horas. No requiere experiencia previa, solo traer un problema real de tu barrio."
        ),
        event(
            id = "9", title = "Charla: Economía del Café Local", category = Category.ACADEMICO,
            daysAhead = 14, start = LocalTime.of(16, 0), end = LocalTime.of(17, 30),
            place = "Cámara de Comercio", neighborhood = "Centro", location = Location(4.5249, -75.6655),
            organizer = camaraComercio, cupo = 90,
            description = "Conversatorio sobre precios, cooperativas y nuevos mercados para el café de origen quindiano."
        ),
        event(
            id = "5", title = "Concierto de Cuerdas Andinas", category = Category.CULTURA,
            daysAhead = 16, start = LocalTime.of(19, 0), end = LocalTime.of(21, 0),
            place = "Teatro Azul", neighborhood = "Centro", location = Location(4.5372, -75.6625),
            organizer = colectivoBambuco, cupo = 200,
            contribution = Contribution(15000.0, "Becas de la Escuela de Bambuco"),
            description = "Tiples, bandolas y guitarras interpretan bambucos y pasillos del Eje Cafetero."
        ),
        event(
            id = "4", title = "Jornada de Limpieza Quebrada La Florida", category = Category.VOLUNTARIADO,
            daysAhead = 17, start = LocalTime.of(7, 0), end = LocalTime.of(11, 0),
            place = "Puente La Florida", neighborhood = "La Florida", location = Location(4.5043, -75.6640),
            organizer = redAmbiental, cupo = 60,
            description = "Recolección de residuos en la ribera de la quebrada. Se entregan guantes y bolsas; trae ropa que se pueda ensuciar."
        ),
        event(
            id = "8", title = "Hackatón Cívica Armenia 24h", category = Category.ACADEMICO,
            daysAhead = 24, durationDays = 1, start = LocalTime.of(8, 0), end = LocalTime.of(8, 0),
            place = "Centro de Innovación", neighborhood = "Norte", location = Location(4.5606, -75.6543),
            organizer = armeniaCivica, cupo = 80,
            description = "24 horas para construir soluciones a retos reales de la ciudad, con mentores y datos abiertos de la Alcaldía."
        ),
        // Eventos del usuario actual que no aparecen en el feed (no están activos)
        event(
            id = "m3", title = "Torneo de Tejo Interbarrios", category = Category.DEPORTES,
            daysAhead = 20, start = LocalTime.of(15, 0), end = LocalTime.of(21, 0),
            place = "Cancha de tejo El Bosque", neighborhood = "El Bosque", location = Location(4.5456, -75.7014),
            organizer = currentUser, cupo = 64, status = EventStatus.PENDIENTE,
            description = "Torneo por parejas entre barrios de Armenia. Inscripción por equipo el mismo día."
        ),
        event(
            id = "m6", title = "Rifa de electrodomésticos", category = Category.SOCIAL,
            daysAhead = 6, start = LocalTime.of(18, 0), end = LocalTime.of(20, 0),
            place = "Salón comunal Granada", neighborhood = "Granada", location = Location(4.5417, -75.6856),
            organizer = currentUser, status = EventStatus.RECHAZADO,
            description = "Rifa de electrodomésticos entre los vecinos del barrio."
        )
    )

    /** Número de asistentes confirmados por evento (según el prototipo). */
    private val attendeeCounts = mapOf(
        "7" to 91, "10" to 118, "6" to 63, "15" to 124, "1" to 87, "2" to 342,
        "3" to 38, "9" to 54, "5" to 156, "4" to 24, "8" to 71
    )

    /**
     * Asistencias confirmadas. Los primeros asistentes de cada evento son vecinos con nombre
     * y el resto se completa con usuarios de relleno hasta alcanzar el conteo del prototipo.
     */
    val attendances: List<Attendance> = events.flatMapIndexed { index, event ->
        val count = attendeeCounts[event.id] ?: 0
        (0 until count).map { n ->
            val user = if (n < 3) {
                neighbors[(index * 3 + n) % neighbors.size]
            } else {
                User(
                    id = "u-relleno-${event.id}-$n",
                    name = "Vecino ${n + 1}",
                    email = "relleno-${event.id}-$n@triboo.co",
                    password = "123456",
                    city = "Armenia",
                    neighborhood = event.neighborhood
                )
            }
            Attendance(user = user, event = event)
        }
    }

    /** Lugares sugeridos en el selector de ubicación. */
    val placeSuggestions = listOf(
        PlaceSuggestion("Parque de la Vida", "Norte", "Av. Bolívar · portería norte", Location(4.5446, -75.6658)),
        PlaceSuggestion("Biblioteca del Quindío", "Centro", "Cra 14 #23-15 · sala comunal", Location(4.5352, -75.6760)),
        PlaceSuggestion("Cancha La Fachada", "La Fachada", "Barrio La Fachada · entrada principal", Location(4.5180, -75.6890))
    )
}
