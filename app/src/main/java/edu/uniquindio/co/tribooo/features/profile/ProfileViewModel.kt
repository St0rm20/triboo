package edu.uniquindio.co.tribooo.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.domain.model.Attendance
import edu.uniquindio.co.tribooo.domain.model.Comment
import edu.uniquindio.co.tribooo.domain.model.Event
import edu.uniquindio.co.tribooo.domain.model.EventStatus
import edu.uniquindio.co.tribooo.domain.model.Follow
import edu.uniquindio.co.tribooo.domain.model.Saved
import edu.uniquindio.co.tribooo.domain.model.User
import edu.uniquindio.co.tribooo.domain.model.UserRole
import edu.uniquindio.co.tribooo.domain.usecase.attendance.ObserveAttendancesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.LogoutUseCase
import edu.uniquindio.co.tribooo.domain.usecase.auth.ObserveCurrentUserUseCase
import edu.uniquindio.co.tribooo.domain.usecase.comment.ObserveCommentsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.event.ObserveEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.location.GetCitiesUseCase
import edu.uniquindio.co.tribooo.domain.usecase.saved.ObserveSavedEventsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.ObserveFollowsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.user.UpdateProfileUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/** Cifras del perfil; según el rol se muestran las de usuario o las de moderación. */
data class ProfileStats(
    val followers: Int = 0,
    val attendances: Int = 0,
    val created: Int = 0,
    val saved: Int = 0,
    /** Promedio de las calificaciones recibidas en sus eventos; null si aún no hay. */
    val rating: Double? = null,
    val pendingReview: Int = 0,
    val rejected: Int = 0,
    // Aún no existe el repositorio de moderación
    val resolved: Int = 0
) {
    val ratingLabel: String get() = rating?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
}

/** Calcula las cifras del perfil de [user] a partir de los datos de la app. */
internal fun calculateProfileStats(
    user: User,
    events: List<Event>,
    attendances: List<Attendance>,
    follows: List<Follow>,
    saved: List<Saved>,
    comments: List<Comment>
): ProfileStats {
    val ratings = comments
        .filter { it.event.organizer.id == user.id && it.author.id != user.id }
        .mapNotNull { it.rating }

    return ProfileStats(
        followers = follows.count { it.followed.id == user.id },
        attendances = attendances.count { it.user.id == user.id },
        created = events.count { it.organizer.id == user.id },
        saved = saved.count { it.user.id == user.id },
        rating = if (ratings.isEmpty()) null else ratings.average(),
        pendingReview = events.count { it.status == EventStatus.PENDIENTE },
        rejected = events.count { it.status == EventStatus.RECHAZADO }
    )
}

data class ProfileUiState(
    val user: User? = null,
    val stats: ProfileStats = ProfileStats(),
    val isAdmin: Boolean = false,
    // Formulario de datos personales
    val showEditDialog: Boolean = false,
    val editName: String = "",
    val editCity: String = "",
    val editNeighborhood: String = "",
    val editBio: String = "",
    val nameError: String? = null,
    val neighborhoodError: String? = null,
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val loggedOut: Boolean = false
)

/** Datos de los repositorios que alimentan las cifras (se agrupan porque `combine` admite máximo 5 flujos). */
private data class ProfileSources(
    val events: List<Event>,
    val attendances: List<Attendance>,
    val follows: List<Follow>,
    val saved: List<Saved>,
    val comments: List<Comment>
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeEvents: ObserveEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeFollows: ObserveFollowsUseCase,
    observeSavedEvents: ObserveSavedEventsUseCase,
    observeComments: ObserveCommentsUseCase,
    getCities: GetCitiesUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val cities: List<String> = getCities()

    // Estado propio de la pantalla (diálogos, formulario y mensajes); lo demás viene de los repositorios
    private val local = MutableStateFlow(ProfileUiState())

    private val sources = combine(
        observeEvents(),
        observeAttendances(),
        observeFollows(),
        observeSavedEvents(),
        observeComments()
    ) { events, attendances, follows, saved, comments ->
        ProfileSources(events, attendances, follows, saved, comments)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        observeCurrentUser(),
        sources,
        local
    ) { user, sources, local ->
        local.copy(
            user = user,
            isAdmin = user?.role == UserRole.ADMIN,
            stats = user?.let {
                calculateProfileStats(it, sources.events, sources.attendances, sources.follows, sources.saved, sources.comments)
            } ?: ProfileStats()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    // Datos personales

    fun onEditClick() {
        val user = uiState.value.user ?: return
        local.update {
            it.copy(
                showEditDialog = true,
                editName = user.name,
                editCity = user.city,
                editNeighborhood = user.neighborhood,
                editBio = user.bio.orEmpty(),
                nameError = null,
                neighborhoodError = null
            )
        }
    }

    fun onNameChange(value: String) = local.update { it.copy(editName = value, nameError = null) }

    fun onCityChange(value: String) = local.update { it.copy(editCity = value) }

    fun onNeighborhoodChange(value: String) = local.update { it.copy(editNeighborhood = value, neighborhoodError = null) }

    fun onBioChange(value: String) = local.update { it.copy(editBio = value) }

    fun onDismissEditDialog() = local.update { it.copy(showEditDialog = false) }

    fun onSaveEdit() {
        val user = uiState.value.user ?: return
        val form = local.value

        val nameError = if (form.editName.trim().length < 3) "Escribe tu nombre y apellido" else null
        val neighborhoodError = if (form.editNeighborhood.isBlank()) "Indica tu barrio" else null
        if (nameError != null || neighborhoodError != null) {
            local.update { it.copy(nameError = nameError, neighborhoodError = neighborhoodError) }
            return
        }

        viewModelScope.launch {
            updateProfileUseCase(user, form.editName, form.editCity, form.editNeighborhood, form.editBio)
            local.update { it.copy(showEditDialog = false, snackbarMessage = "Datos personales actualizados.") }
        }
    }

    // Cerrar sesión

    fun onLogoutClick() = local.update { it.copy(showLogoutDialog = true) }

    fun onDismissLogoutDialog() = local.update { it.copy(showLogoutDialog = false) }

    fun onConfirmLogout() {
        local.update { it.copy(showLogoutDialog = false) }
        viewModelScope.launch {
            logoutUseCase()
            local.update { it.copy(loggedOut = true) }
        }
    }

    // Eliminar cuenta (como en el prototipo, solo se muestra el aviso; no se borran datos)

    fun onDeleteAccountClick() = local.update { it.copy(showDeleteDialog = true) }

    fun onDismissDeleteDialog() = local.update { it.copy(showDeleteDialog = false) }

    fun onConfirmDeleteAccount() {
        local.update {
            it.copy(showDeleteDialog = false, snackbarMessage = "Cuenta marcada para eliminación en 14 días.")
        }
    }

    fun onSnackbarShown() = local.update { it.copy(snackbarMessage = null) }
}
