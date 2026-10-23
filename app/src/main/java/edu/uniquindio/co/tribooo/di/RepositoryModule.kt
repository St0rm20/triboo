package edu.uniquindio.co.tribooo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.uniquindio.co.tribooo.data.repository.local.SessionRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.AttendanceRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.CommentRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.EventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.FollowRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.InterestRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.LocationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.NotificationRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.SavedEventRepositoryImpl
import edu.uniquindio.co.tribooo.data.repository.memory.UserRepositoryImpl
import edu.uniquindio.co.tribooo.domain.repository.AttendanceRepository
import edu.uniquindio.co.tribooo.domain.repository.CommentRepository
import edu.uniquindio.co.tribooo.domain.repository.EventRepository
import edu.uniquindio.co.tribooo.domain.repository.FollowRepository
import edu.uniquindio.co.tribooo.domain.repository.InterestRepository
import edu.uniquindio.co.tribooo.domain.repository.LocationRepository
import edu.uniquindio.co.tribooo.domain.repository.NotificationRepository
import edu.uniquindio.co.tribooo.domain.repository.SavedEventRepository
import edu.uniquindio.co.tribooo.domain.repository.SessionRepository
import edu.uniquindio.co.tribooo.domain.repository.UserRepository
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
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
