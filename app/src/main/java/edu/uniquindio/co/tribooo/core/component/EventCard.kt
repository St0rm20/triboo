package edu.uniquindio.co.tribooo.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import edu.uniquindio.co.tribooo.core.theme.categoryColors
import edu.uniquindio.co.tribooo.core.util.CUPO_WARNING
import edu.uniquindio.co.tribooo.core.util.atAppZone
import edu.uniquindio.co.tribooo.core.util.cupoLabel
import edu.uniquindio.co.tribooo.core.util.eventWhenLabel
import edu.uniquindio.co.tribooo.core.util.formatThousands
import edu.uniquindio.co.tribooo.core.util.distanceKmTo
import edu.uniquindio.co.tribooo.core.util.initials
import edu.uniquindio.co.tribooo.core.util.isFull
import edu.uniquindio.co.tribooo.core.util.monthShort
import edu.uniquindio.co.tribooo.core.util.occupancy
import edu.uniquindio.co.tribooo.core.util.waitlistLabel
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.Location
import edu.uniquindio.co.tribooo.domain.model.WaitlistEntry
import edu.uniquindio.co.tribooo.domain.usecase.waitlist.waitlistPosition

/** Evento listo para mostrarse en una tarjeta del feed. */
data class FeedEventItem(
    val event: Event,
    val attendeesCount: Int,
    val attendeeInitials: List<String>,
    val isAttending: Boolean,
    val isMine: Boolean,
    val distanceKm: Double,
    val isWaitlisted: Boolean = false,
    /** Puesto en la lista de espera del evento, desde 1; null si el usuario no está en ella. */
    val waitlistPosition: Int? = null
) {
    val isFull: Boolean get() = event.isFull(attendeesCount)

    /** El cupo está lleno y el usuario puede anotarse en la lista de espera. */
    val canJoinWaitlist: Boolean get() = isFull && !isAttending && !isWaitlisted && !isMine
    val occupancy: Float get() = event.occupancy(attendeesCount)
}

/** Arma las tarjetas de [events] (se conserva su orden) con asistentes, asistencia y lista de espera propias y distancia a [location]. */
fun toCardItems(
    events: List<Event>,
    attendances: List<Attendance>,
    waitlist: List<WaitlistEntry>,
    currentUserId: String?,
    location: Location
): List<FeedEventItem> {
    val attendeesByEvent = attendances.groupBy { it.event.id }
    return events.map { event ->
        val attendees = attendeesByEvent[event.id].orEmpty()
        val position = currentUserId?.let { waitlistPosition(waitlist, it, event.id) }
        FeedEventItem(
            event = event,
            attendeesCount = attendees.size,
            attendeeInitials = attendees.take(3).map { it.user.name.initials() },
            isAttending = attendees.any { it.user.id == currentUserId },
            isMine = event.organizer.id == currentUserId,
            distanceKm = location.distanceKmTo(event.location),
            isWaitlisted = position != null,
            waitlistPosition = position
        )
    }
}

/** Tarjeta de evento del feed y de la lista de Explorar. */
@Composable
fun EventCard(item: FeedEventItem, onClick: () -> Unit, onToggleAttendance: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val event = item.event
    val start = event.startDate.atAppZone().toLocalDate()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Portada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            ) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surfaceContainerHigh)
                )

                val tones = categoryColors(event.category)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tones.container)
                        .padding(start = 9.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(event.category.iconVector(), null, tint = tones.content, modifier = Modifier.size(16.dp))
                    Text(event.category.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = tones.content)
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceContainerLowest)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .widthIn(min = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        start.monthShort().uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                        color = colors.onSurfaceVariant
                    )
                    Text(
                        start.dayOfMonth.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface
                    )
                }

                if (item.isMine) {
                    Text(
                        text = "Tú organizas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onTertiaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.tertiaryContainer)
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                Column(
                    modifier = Modifier.padding(top = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    InfoRow(Icons.Rounded.Schedule, eventWhenLabel(event.startDate, event.endDate))
                    InfoRow(Icons.Rounded.Place, "${event.place} · ${event.neighborhood}")
                }

                if (event.cupo != null) CupoBar(item = item)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        val avatarColors = listOf(
                            colors.primaryContainer to colors.onPrimaryContainer,
                            colors.secondaryContainer to colors.onSecondaryContainer,
                            colors.tertiaryContainer to colors.onTertiaryContainer
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy((-7).dp)) {
                            item.attendeeInitials.forEachIndexed { index, initials ->
                                val (bg, fg) = avatarColors[index % avatarColors.size]
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .border(2.dp, colors.surfaceContainerLowest, CircleShape)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initials, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = fg)
                                }
                            }
                        }
                        Text(
                            text = "${item.attendeesCount.formatThousands()} asistirán",
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    AttendButton(item = item, onClick = onToggleAttendance)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CupoBar(item: FeedEventItem) {
    val colors = MaterialTheme.colorScheme
    val tight = item.occupancy >= CUPO_WARNING
    val label = item.event.cupoLabel(item.attendeesCount)
    val accent = if (tight) colors.error else colors.primary

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val fg = if (tight) colors.error else colors.onSurfaceVariant
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
            Text("${(item.occupancy * 100).toInt()}% ocupado", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = fg)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.occupancy)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
        }
    }
}

@Composable
private fun AttendButton(item: FeedEventItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val waitlist = item.isFull && !item.isAttending
    val position = item.waitlistPosition.takeIf { waitlist }
    val queued = position != null
    Surface(
        onClick = onClick,
        enabled = !waitlist || item.canJoinWaitlist || queued,
        shape = RoundedCornerShape(14.dp),
        color = when {
            item.isAttending -> colors.errorContainer
            queued -> colors.secondaryContainer
            waitlist -> colors.surfaceContainerHigh
            else -> colors.primary
        },
        contentColor = when {
            item.isAttending -> colors.onErrorContainer
            queued -> colors.onSecondaryContainer
            waitlist -> colors.onSurfaceVariant
            else -> colors.onPrimary
        },
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (item.isAttending) Icons.Rounded.EventBusy else Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = when {
                    item.isAttending -> "Cancelar"
                    position != null -> waitlistLabel(position)
                    waitlist -> "Lista de espera"
                    else -> "Asistiré"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
