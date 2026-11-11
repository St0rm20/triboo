package edu.uniquindio.co.tribooo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.uniquindio.co.tribooo.core.util.ResourceProvider
import edu.uniquindio.co.tribooo.core.util.ResourceProviderImpl
import jakarta.inject.Singleton

/** Enlaza los servicios de la app que no son repositorios del dominio. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindResourceProvider(impl: ResourceProviderImpl): ResourceProvider
}
