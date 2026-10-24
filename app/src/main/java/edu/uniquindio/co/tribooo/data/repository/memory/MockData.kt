package edu.uniquindio.co.tribooo.data.repository.memory

import edu.uniquindio.co.tribooo.core.util.APP_ZONE
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Category
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Contribution
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.ImageEvent
import edu.uniquindio.co.tribooo.domain.model.Interest
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.ModerationAction
import edu.uniquindio.co.tribooo.domain.model.ModerationReview
import edu.uniquindio.co.tribooo.domain.model.Notification
import edu.uniquindio.co.tribooo.domain.model.NotificationType
import edu.uniquindio.co.tribooo.domain.model.PlaceSuggestion
import edu.uniquindio.co.tribooo.domain.model.Report
import edu.uniquindio.co.tribooo.domain.model.ReportReasons
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Datos de prueba de la fuente de datos en memoria (mientras no exista backend).
 * Solo los usan los repositorios de este paquete como estado inicial.
 * Las fechas de los eventos son relativas al día actual para que el feed siempre tenga eventos próximos.
 */
internal object MockData {

    /** Ubicación del usuario actual (centro de Armenia). Se usa para calcular distancias. */
    val currentLocation = Location(latitude = 4.5339, longitude = -75.6811)

    /** Ciudades disponibles en el registro. */
    val cities = listOf(
        "Armenia", "Barranquilla", "Bogotá", "Bucaramanga", "Buenavista", "Calarcá", "Cali", "Cartagena",
        "Circasia", "Córdoba", "Cúcuta", "Filandia", "Génova", "Ibagué", "La Tebaida", "Manizales",
        "Medellín", "Montenegro", "Montería", "Neiva", "Pasto", "Pereira", "Pijao", "Popayán",
        "Quimbaya", "Salento", "Santa Marta", "Tunja", "Valledupar", "Villavicencio"
    )

    /** Usuaria de prueba con eventos, comentarios y seguidos (inicia sesión con su email y "123456"). */
    val daniela = User(
        id = "u-daniela",
        name = "Daniela Restrepo",
        email = "daniela.restrepo@triboo.co",
        password = "123456",
        city = "Armenia",
        neighborhood = "Granada",
        bio = "Organizo ciclopaseos y bazares en el barrio."
    )

    private fun organizer(id: String, name: String, neighborhood: String, bio: String) = User(
        id = id,
        name = name,
        email = "$id@triboo.co",
        password = "123456",
        city = "Armenia",
        neighborhood = neighborhood,
        bio = bio
    )

    private val museoQuimbaya = organizer(
        "u-museo", "Museo Quimbaya", "Av. Bolívar",
        "Guardamos y contamos la historia del Quindío con exposiciones, talleres y visitas guiadas abiertas a toda la comunidad."
    )
    private val senderistas = organizer(
        "u-senderistas", "Senderistas Armenia", "Norte",
        "Grupo de vecinos que camina los senderos del Quindío cada semana. Caminatas seguras, con guía y para todos los niveles."
    )
    private val jacLosQuindos = organizer(
        "u-jac-quindos", "Junta de Acción Comunal Los Quindos", "Los Quindos",
        "Trabajamos por el barrio con bingos, bazares y jornadas comunitarias para mejorar nuestro salón comunal."
    )
    private val mesaCultural = organizer(
        "u-mesa-cultural", "Mesa Cultural Armenia", "Centro",
        "Colectivo que reúne a artistas, caficultores y gestores culturales para mantener vivas las ferias y fiestas de la ciudad."
    )
    private val uqLabs = organizer(
        "u-uqlabs", "Semillero UQ Labs", "Universidad",
        "Semillero de estudiantes y profesores que lleva la innovación y el prototipado a los problemas reales de los barrios."
    )
    private val camaraComercio = organizer(
        "u-camara", "Cámara de Comercio", "Centro",
        "Acompañamos a los empresarios y emprendedores del Quindío con charlas, ferias y espacios de formación."
    )
    private val colectivoBambuco = organizer(
        "u-bambuco", "Colectivo Bambuco", "Centro",
        "Músicos que defienden el bambuco y la música andina con conciertos y una escuela abierta para nuevas generaciones."
    )
    private val redAmbiental = organizer(
        "u-red-ambiental", "Red Ambiental Quindío", "La Florida",
        "Voluntarios que cuidan las quebradas y los parques de la ciudad con jornadas de limpieza y siembra."
    )
    private val armeniaCivica = organizer(
        "u-armenia-civica", "Armenia Cívica", "Norte",
        "Comunidad de ciudadanos, programadores y funcionarios que usa datos abiertos para resolver retos de la ciudad."
    )

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

    /** Administrador de moderación (revisa los eventos enviados). */
    private val moderador = User(
        id = "u-moderacion",
        name = "Mauricio Salazar",
        email = "moderacion@triboo.co",
        password = "admin1234",
        city = "Armenia",
        neighborhood = "Centro",
        role = UserRole.ADMIN
    )

    val users: List<User> = listOf(
        daniela, museoQuimbaya, senderistas, jacLosQuindos, mesaCultural, uqLabs,
        camaraComercio, colectivoBambuco, redAmbiental, armeniaCivica, moderador
    ) + neighbors

    private fun unsplash(photoId: String) = "https://images.unsplash.com/$photoId?w=900&q=70&auto=format&fit=crop"

    /** Portadas de prueba por id de evento. */
    private val coverImages = mapOf(
        "7" to unsplash("photo-1488646953014-85cb44e25828"),
        "10" to unsplash("photo-1509099836639-18ba1795216d"),
        "6" to unsplash("photo-1506905925346-21bda4d32df4"),
        "15" to unsplash("photo-1511632765486-a01980e01a18"),
        "1" to unsplash("photo-1517649763962-0c623066013b"),
        "2" to unsplash("photo-1559056199-641a0ac8b55e"),
        "3" to unsplash("photo-1531482615713-2afd69097998"),
        "9" to unsplash("photo-1540575467063-178a50c2df87"),
        "5" to unsplash("photo-1470229722913-7c0e2dbbafd3"),
        "4" to unsplash("photo-1441974231531-c6227db76b6e"),
        "8" to unsplash("photo-1522071820081-009f0129c71c")
    )

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
            category = category,
            imageUrl = coverImages[id]
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
            organizer = daniela,
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
            organizer = daniela, cupo = 120,
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
            organizer = daniela, cupo = 64, status = EventStatus.PENDIENTE,
            description = "Torneo por parejas entre barrios de Armenia. Inscripción por equipo el mismo día."
        ),
        event(
            id = "m6", title = "Rifa de electrodomésticos", category = Category.SOCIAL,
            daysAhead = 6, start = LocalTime.of(18, 0), end = LocalTime.of(20, 0),
            place = "Salón comunal Granada", neighborhood = "Granada", location = Location(4.5417, -75.6856),
            organizer = daniela, status = EventStatus.RECHAZADO,
            description = "Rifa de electrodomésticos entre los vecinos del barrio."
        ),
        // Eventos del usuario actual que ya terminaron
        event(
            id = "m1", title = "Bicicletada Familiar del Barrio Granada", category = Category.DEPORTES,
            daysAhead = -45, start = LocalTime.of(8, 0), end = LocalTime.of(11, 30),
            place = "Parque Granada", neighborhood = "Granada", location = Location(4.5268, -75.6829),
            organizer = daniela, cupo = 60, status = EventStatus.FINALIZADO,
            description = "Recorrido tranquilo por las calles del barrio, pensado para ir en familia y con niños."
        ),
        event(
            id = "m2", title = "Feria de Emprendedores Granada", category = Category.SOCIAL,
            daysAhead = -90, start = LocalTime.of(10, 0), end = LocalTime.of(16, 0),
            place = "Salón comunal Granada", neighborhood = "Granada", location = Location(4.5271, -75.6836),
            organizer = daniela, status = EventStatus.FINALIZADO,
            description = "Muestra de productos y servicios de emprendedores del barrio, con entrada libre."
        ),
        // Eventos de otros organizadores que esperan moderación (del más antiguo al más nuevo)
        event(
            id = "p1", title = "Club de Lectura Latinoamericana", category = Category.CULTURA,
            daysAhead = 9, start = LocalTime.of(18, 30), end = LocalTime.of(20, 0),
            place = "Biblioteca del Quindío", neighborhood = "Centro", location = Location(4.5352, -75.6760),
            organizer = neighbors[3], cupo = 25, status = EventStatus.PENDIENTE,
            description = "Encuentro quincenal para conversar sobre una novela latinoamericana. Este mes leemos \"Pedro Páramo\" de Juan Rulfo; no hace falta haberla terminado para participar."
        ).copy(publicationDate = minutesAgo(26 * 60)),
        event(
            id = "p2", title = "Torneo de Microfútbol Barrio Laureles", category = Category.DEPORTES,
            daysAhead = 12, start = LocalTime.of(8, 0), end = LocalTime.of(17, 0),
            place = "Cancha múltiple Laureles", neighborhood = "Laureles", location = Location(4.5207, -75.6921),
            organizer = neighbors[8], cupo = 96, status = EventStatus.PENDIENTE,
            description = "Torneo de microfútbol por equipos de 6 jugadores, categoría libre. Inscripción gratuita en la cancha desde las 7:00 y premiación con trofeos para los tres primeros puestos."
        ).copy(publicationDate = minutesAgo(9 * 60)),
        event(
            id = "p3", title = "Rifa y bazar navideño con boletas", category = Category.SOCIAL,
            daysAhead = 22, start = LocalTime.of(15, 0), end = LocalTime.of(21, 0),
            place = "Salón comunal La Patria", neighborhood = "La Patria", location = Location(4.5148, -75.6712),
            organizer = neighbors[7], status = EventStatus.PENDIENTE,
            contribution = Contribution(20000.0, "Ganancia personal de los organizadores"),
            description = "Bazar con comidas y manualidades y gran rifa de una moto y un televisor. Las boletas cuestan $20.000 y se venden por adelantado; lo recaudado es para los organizadores."
        ).copy(publicationDate = minutesAgo(3 * 60)),
        event(
            id = "p4", title = "Curso intensivo de inversión cripto", category = Category.ACADEMICO,
            daysAhead = 5, start = LocalTime.of(9, 0), end = LocalTime.of(13, 0),
            place = "Hotel Portal del Quindío", neighborhood = "Norte", location = Location(4.5512, -75.6598),
            organizer = neighbors[12], cupo = 50, status = EventStatus.PENDIENTE,
            contribution = Contribution(150000.0, "Membresía del grupo de inversión"),
            description = "Aprende a duplicar tu dinero en 30 días con nuestras señales de trading. Rentabilidad garantizada del 40% mensual; al final del curso podrás comprar la membresía VIP con cupos limitados."
        ).copy(publicationDate = minutesAgo(40))
    )

    /** Número de asistentes confirmados por evento (según el prototipo). */
    private val attendeeCounts = mapOf(
        "7" to 91, "10" to 118, "6" to 63, "15" to 124, "1" to 87, "2" to 342,
        "3" to 38, "9" to 54, "5" to 156, "4" to 24, "8" to 71, "m1" to 34, "m2" to 52
    )

    /**
     * Personas de un evento: las primeras son vecinos con nombre y el resto
     * se completa con usuarios de relleno hasta alcanzar [count].
     */
    private fun crowd(event: Event, eventIndex: Int, count: Int, tag: String): List<User> =
        (0 until count).map { n ->
            if (n < 3) {
                neighbors[(eventIndex * 3 + n) % neighbors.size]
            } else {
                User(
                    id = "u-$tag-${event.id}-$n",
                    name = "Vecino ${n + 1}",
                    email = "$tag-${event.id}-$n@triboo.co",
                    password = "123456",
                    city = "Armenia",
                    neighborhood = event.neighborhood
                )
            }
        }

    /** Asistencias confirmadas, con el conteo del prototipo. */
    val attendances: List<Attendance> = events.flatMapIndexed { index, event ->
        crowd(event, index, attendeeCounts[event.id] ?: 0, "relleno").map { Attendance(user = it, event = event) }
    }

    /** Marcas de "Me interesa": en el prototipo son algo más que los asistentes. */
    val interests: List<Interest> = events.flatMapIndexed { index, event ->
        val going = attendeeCounts[event.id] ?: return@flatMapIndexed emptyList()
        val count = (going * 1.6).toInt() + 24
        crowd(event, index + 1, count, "interes").map { Interest(user = it, event = event) }
    }

    private fun eventById(id: String) = events.first { it.id == id }

    /** Fotos adicionales de algunos eventos (la portada va en [Event.imageUrl]). */
    val images: List<ImageEvent> = mapOf(
        "1" to listOf("photo-1511632765486-a01980e01a18", "photo-1522071820081-009f0129c71c"),
        "2" to listOf("photo-1509099836639-18ba1795216d", "photo-1470229722913-7c0e2dbbafd3"),
        "6" to listOf("photo-1441974231531-c6227db76b6e", "photo-1488646953014-85cb44e25828")
    ).flatMap { (eventId, photoIds) ->
        photoIds.mapIndexed { i, photoId ->
            ImageEvent(id = "img-$eventId-$i", event = eventById(eventId), url = unsplash(photoId), order = i + 1)
        }
    }

    private fun minutesAgo(minutes: Long): Instant = Instant.now().minus(Duration.ofMinutes(minutes))

    /** Instante de hace [days] días a las [hour]:00 (hora de la app), para fechas como "ayer". */
    private fun daysAgoAt(days: Long, hour: Int): Instant =
        LocalDate.now(APP_ZONE).minusDays(days).atTime(hour, 0).atZone(APP_ZONE).toInstant()

    /** Reseña del evento finalizado "m1" que ya tiene respuesta del organizador. */
    private val reviewWithReply = Comment(
        id = "c-m1-1", event = eventById("m1"), author = neighbors[0], date = daysAgoAt(44, 9), rating = 5,
        text = "Muy bien organizada, los niños disfrutaron todo el recorrido."
    )

    /** Comentarios de prueba (los del ciclopaseo son los del prototipo). */
    val comments: List<Comment> = listOf(
        Comment(
            id = "c-1-1", event = eventById("1"), author = neighbors[1], date = minutesAgo(120),
            text = "¿El punto de encuentro es la portería norte o la sur del parque?"
        ),
        Comment(
            id = "c-1-2", event = eventById("1"), author = daniela, date = minutesAgo(60),
            text = "Portería norte, junto a la ciclorruta. Salimos 18:15 en punto."
        ),
        Comment(
            id = "c-1-3", event = eventById("1"), author = neighbors[3], date = minutesAgo(40), rating = 5,
            text = "Perfecto, llevo botiquín y bomba de aire por si alguien pincha."
        ),
        Comment(
            id = "c-2-1", event = eventById("2"), author = neighbors[5], date = minutesAgo(300), rating = 4,
            text = "El año pasado estuvo buenísima la catación. ¿Habrá café para llevar en grano?"
        ),
        Comment(
            id = "c-6-1", event = eventById("6"), author = neighbors[8], date = minutesAgo(26 * 60),
            text = "¿Es apta para niños de 10 años?"
        ),
        Comment(
            id = "c-6-2", event = eventById("6"), author = senderistas, date = minutesAgo(25 * 60),
            text = "Sí, el recorrido es tranquilo. Que vengan con un adulto y buen calzado."
        ),
        Comment(
            id = "c-10-1", event = eventById("10"), author = neighbors[9], date = minutesAgo(300),
            text = "¿Reciben donaciones de ropa para niños?"
        ),
        Comment(
            id = "c-1-4", event = eventById("1"), author = neighbors[7], date = daysAgoAt(2, 10),
            text = "¿Se puede ir con bicicleta eléctrica?"
        ),
        // Reseñas de los eventos finalizados
        reviewWithReply,
        Comment(
            id = "c-m1-2", event = eventById("m1"), author = neighbors[2], date = daysAgoAt(44, 15), rating = 5,
            text = "Ruta segura y buen acompañamiento. Repetimos cuando quieran."
        ),
        Comment(
            id = "c-m1-3", event = eventById("m1"), author = neighbors[5], date = daysAgoAt(43, 10), rating = 4,
            text = "Excelente ambiente, solo faltó más agua en el punto final."
        ),
        Comment(
            id = "c-m2-1", event = eventById("m2"), author = neighbors[8], date = daysAgoAt(89, 17), rating = 5,
            text = "Encontré productos buenísimos de gente del barrio."
        ),
        Comment(
            id = "c-m2-2", event = eventById("m2"), author = neighbors[11], date = daysAgoAt(89, 18), rating = 4,
            text = "Buena feria, ojalá hubiera más puestos de comida."
        ),
        Comment(
            id = "c-m2-3", event = eventById("m2"), author = neighbors[3], date = daysAgoAt(88, 11), rating = 5,
            text = "Muy buena organización y gran variedad de emprendimientos."
        ),
        // Respuesta del organizador a una reseña
        Comment(
            id = "c-m1-1-r", event = eventById("m1"), author = daniela, date = daysAgoAt(43, 18),
            text = "¡Gracias por acompañarnos! Ya estamos preparando la próxima bicicletada.",
            parentComment = reviewWithReply
        )
    )

    /** Revisiones de moderación: el rechazo del evento "m6" y el caso resuelto del "9". */
    val moderationReviews: List<ModerationReview> = listOf(
        ModerationReview(
            id = "mr-m6",
            event = eventById("m6"),
            admin = moderador,
            action = ModerationAction.RECHAZAR,
            reason = "El evento tiene como actividad central una rifa de electrodomésticos y no indica " +
                "si las boletas tienen costo ni a qué fin benéfico se destinan. La plataforma solo permite " +
                "sorteos sin costo o con fines benéficos declarados. El resto de la propuesta cumple las normas.",
            date = daysAgoAt(3, 15)
        ),
        ModerationReview(
            id = "mr-9",
            event = eventById("9"),
            admin = moderador,
            action = ModerationAction.RESOLVER,
            date = daysAgoAt(5, 16)
        )
    )

    /** Reportes de vecinos: dos eventos activos con casos abiertos y uno ya resuelto. */
    val reports: List<Report> = listOf(
        // Salón comunal no disponible y contacto que no responde (evento "15")
        Report("r-15-1", eventById("15"), neighbors[1], ReportReasons.NO_LONGER_EXISTS,
            "Fui al salón comunal y me dijeron que ese día está ocupado por otro evento.", date = minutesAgo(40)),
        Report("r-15-2", eventById("15"), neighbors[5], ReportReasons.WRONG_PLACE_OR_CONTACT,
            "El número de contacto no responde desde hace días.", date = minutesAgo(6 * 60)),
        Report("r-15-3", eventById("15"), neighbors[9], ReportReasons.WRONG_PLACE_OR_CONTACT, date = daysAgoAt(1, 20)),
        Report("r-15-4", eventById("15"), neighbors[11], ReportReasons.NO_LONGER_EXISTS,
            "La junta comunal dice que el bingo se aplazó.", date = daysAgoAt(2, 11)),
        // Reventa de entradas (evento "5")
        Report("r-5-1", eventById("5"), neighbors[13], ReportReasons.CHARGES_OR_ADS,
            "Alguien en redes está vendiendo entradas a $40.000 y el concierto es de aporte voluntario.", date = minutesAgo(90)),
        Report("r-5-2", eventById("5"), neighbors[14], ReportReasons.CHARGES_OR_ADS, date = minutesAgo(5 * 60)),
        // Caso ya cerrado por moderación (evento "9")
        Report("r-9-1", eventById("9"), neighbors[2], ReportReasons.NO_LONGER_EXISTS,
            "Decía que cambió de salón, pero sigue siendo en la Cámara de Comercio.", date = daysAgoAt(6, 10), resolved = true)
    )

    /** Organizadores que sigue el usuario actual. */
    val follows: List<Follow> = listOf(
        Follow(follower = daniela, followed = mesaCultural),
        Follow(follower = neighbors[6], followed = daniela),
        Follow(follower = neighbors[4], followed = daniela),
        Follow(follower = neighbors[10], followed = daniela),
        Follow(follower = neighbors[0], followed = daniela),
        Follow(follower = neighbors[2], followed = daniela),
        Follow(follower = neighbors[12], followed = daniela),
        // Seguidor mutuo
        Follow(follower = daniela, followed = neighbors[0])
    )

    /**
     * Notificaciones del usuario actual. En los de comentario la referencia es el id del comentario
     * y en los de seguidor, el id de quien lo sigue.
     */
    val notifications: List<Notification> = listOf(
        Notification("n-1", daniela, NotificationType.FOLLOWER, neighbors[6].id, read = false, date = minutesAgo(25)),
        Notification("n-2", daniela, NotificationType.COMMENT, "c-1-3", read = false, date = minutesAgo(39)),
        Notification("n-3", daniela, NotificationType.COMMENT, "c-1-1", read = false, date = minutesAgo(119)),
        Notification("n-4", daniela, NotificationType.COMMENT, "c-10-1", read = true, date = minutesAgo(299)),
        Notification("n-5", daniela, NotificationType.FOLLOWER, neighbors[4].id, read = true, date = daysAgoAt(1, 19)),
        Notification("n-6", daniela, NotificationType.COMMENT, "c-1-4", read = true, date = daysAgoAt(2, 10)),
        Notification("n-7", daniela, NotificationType.FOLLOWER, neighbors[10].id, read = true, date = daysAgoAt(4, 9))
    )

    /** Eventos guardados por el usuario actual. */
    val saved: List<Saved> = listOf(
        Saved(user = daniela, event = eventById("6")),
        Saved(user = daniela, event = eventById("2")),
        Saved(user = daniela, event = eventById("5")),
        Saved(user = daniela, event = eventById("3"))
    )

    /** Lugares sugeridos en el selector de ubicación. */
    val placeSuggestions = listOf(
        PlaceSuggestion("Parque de la Vida", "Norte", "Av. Bolívar · portería norte", Location(4.5446, -75.6658)),
        PlaceSuggestion("Biblioteca del Quindío", "Centro", "Cra 14 #23-15 · sala comunal", Location(4.5352, -75.6760)),
        PlaceSuggestion("Cancha La Fachada", "La Fachada", "Barrio La Fachada · entrada principal", Location(4.5180, -75.6890)),
        PlaceSuggestion("Plaza de Bolívar", "Centro", "Carrera 14 con calle 20", Location(4.5339, -75.6811)),
        PlaceSuggestion("Parque Sucre", "Centro", "Calle 21 con carrera 16", Location(4.5367, -75.6735)),
        PlaceSuggestion("Parque de los Fundadores", "Centro", "Carrera 14 con calle 15", Location(4.5316, -75.6787)),
        PlaceSuggestion("Parque La Libertad", "La Libertad", "Calle 2 Norte con carrera 19", Location(4.5290, -75.6897)),
        PlaceSuggestion("Parque Los Arrieros", "Los Arrieros", "Avenida Centenario", Location(4.5252, -75.6826)),
        PlaceSuggestion("Centro Cultural Metropolitano de Convenciones", "Norte", "Avenida Bolívar", Location(4.5421, -75.6736)),
        PlaceSuggestion("Estadio Centenario", "Centenario", "Avenida Centenario · entrada principal", Location(4.5478, -75.6705)),
        PlaceSuggestion("Universidad del Quindío", "La Ciudadela", "Carrera 15 con calle 12 Norte", Location(4.5528, -75.6578)),
        PlaceSuggestion("Museo del Oro Quimbaya", "Av. Bolívar", "Avenida Bolívar con calle 40 Norte", Location(4.5504, -75.6672))
    )
}
