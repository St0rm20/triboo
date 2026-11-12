package edu.uniquindio.co.tribooo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.uniquindio.co.tribooo.data.repository.firebase.AttendanceRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.AuthRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.CommentRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.EventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.FollowRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.InterestRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.ModerationReviewRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.NotificationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.ReportRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.SavedEventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.UserRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.firebase.WaitlistRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.local.SessionRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.EventFilterRepositoryImpl
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.AuthRepository
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.EventFilterRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
import edu.uniquindio.co.tribooo.domain.repository.InterestRepository
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import edu.uniquindio.co.tribooo.domain.repository.ModerationReviewRepository
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.repository.ReportRepository
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
import edu.uniquindio.co.tribooo.domain.repository.WaitlistRepository
import jakarta.inject.Singleton

/**
 * Le indica a Hilt qué implementación usar para cada interfaz del dominio.
 * Para cambiar de fuente de datos (p. ej. Firebase) basta con enlazar otras implementaciones aquí.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository

    @Binds
    @Singleton
    abstract fun bindInterestRepository(impl: InterestRepositoryImpl): InterestRepository

    @Binds
    @Singleton
    abstract fun bindSavedEventRepository(impl: SavedEventRepositoryImpl): SavedEventRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindFollowRepository(impl: FollowRepositoryImpl): FollowRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindModerationReviewRepository(impl: ModerationReviewRepositoryImpl): ModerationReviewRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindEventFilterRepository(impl: EventFilterRepositoryImpl): EventFilterRepository

    @Binds
    @Singleton
    abstract fun bindWaitlistRepository(impl: WaitlistRepositoryImpl): WaitlistRepository
}
