package edu.uniquindio.co.tribooo.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.uniquindio.co.tribooo.core.util.Validators
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
import edu.uniquindio.co.tribooo.domain.usecase.moderation.ObserveModerationCountsUseCase
import edu.uniquindio.co.tribooo.domain.usecase.notification.ObserveUnreadNotificationsCountUseCase
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
    val reports: Int = 0,
    val resolved: Int = 0,
    val unreadNotifications: Int = 0
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

/** Máximo de caracteres de la presentación ("Sobre mí"), como en el prototipo. */
const val MAX_BIO_LENGTH = 220

/** Campos validables de "Editar datos personales", en el orden en que se revisan al guardar. */
enum class EditProfileField { NAME, NEIGHBORHOOD }

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
    /** Foto del borrador (URI del selector del sistema); null si no hay. Se guarda con "Guardar cambios". */
    val editPhoto: String? = null,
    val editTouched: Set<EditProfileField> = emptySet(),
    val editSubmitAttempted: Boolean = false,
    /** Campo que debe recibir el foco tras intentar guardar con errores. */
    val editFocusField: EditProfileField? = null,
    val isSaving: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val loggedOut: Boolean = false
) {
    private fun rawError(field: EditProfileField): String? = when (field) {
        EditProfileField.NAME -> Validators.name(editName)
        EditProfileField.NEIGHBORHOOD -> Validators.neighborhood(editNeighborhood)
    }

    private fun shownError(field: EditProfileField): String? =
        rawError(field).takeIf { editSubmitAttempted || field in editTouched }

    val nameError: String? get() = shownError(EditProfileField.NAME)
    val neighborhoodError: String? get() = shownError(EditProfileField.NEIGHBORHOOD)

    val firstInvalidEditField: EditProfileField? get() = EditProfileField.entries.firstOrNull { rawError(it) != null }

    /** Se intentó guardar y aún quedan campos inválidos. */
    val showInvalidNotice: Boolean get() = editSubmitAttempted && firstInvalidEditField != null
}

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
    observeUnreadNotifications: ObserveUnreadNotificationsCountUseCase,
    observeModerationCounts: ObserveModerationCountsUseCase,
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
        local,
        observeUnreadNotifications(),
        observeModerationCounts()
    ) { user, sources, local, unread, moderation ->
        val isAdmin = user?.role == UserRole.ADMIN
        val stats = user?.let {
            calculateProfileStats(it, sources.events, sources.attendances, sources.follows, sources.saved, sources.comments)
        }?.copy(unreadNotifications = unread) ?: ProfileStats()
        local.copy(
            user = user,
            isAdmin = isAdmin,
            // Las cifras del administrador salen de moderación
            stats = if (isAdmin) {
                stats.copy(
                    pendingReview = moderation.pending,
                    rejected = moderation.rejected,
                    reports = moderation.reports,
                    resolved = moderation.resolved
                )
            } else stats
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
                editPhoto = user.photo,
                editTouched = emptySet(),
                editSubmitAttempted = false,
                editFocusField = null,
                isSaving = false
            )
        }
    }

    fun onNameChange(value: String) = local.update { it.copy(editName = value) }

    fun onCityChange(value: String) = local.update { it.copy(editCity = value) }

    fun onNeighborhoodChange(value: String) = local.update { it.copy(editNeighborhood = value) }

    fun onEditFieldFocusLost(field: EditProfileField) = local.update { it.copy(editTouched = it.editTouched + field) }

    fun onEditFocusHandled() = local.update { it.copy(editFocusField = null) }

    fun onBioChange(value: String) = local.update { it.copy(editBio = value.take(MAX_BIO_LENGTH)) }

    fun onPhotoSelected(uri: String) = local.update { it.copy(editPhoto = uri) }

    fun onPhotoRemoved() = local.update { it.copy(editPhoto = null) }

    fun onDismissEditDialog() = local.update { it.copy(showEditDialog = false) }

    fun onSaveEdit() {
        val user = uiState.value.user ?: return
        val form = local.value
        if (form.isSaving) return

        val invalid = form.firstInvalidEditField
        if (invalid != null) {
            local.update { it.copy(editSubmitAttempted = true, editFocusField = invalid) }
            return
        }

        local.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            updateProfileUseCase(user, form.editName, form.editCity, form.editNeighborhood, form.editBio, form.editPhoto)
            local.update { it.copy(showEditDialog = false, isSaving = false, snackbarMessage = "Datos personales actualizados.") }
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
