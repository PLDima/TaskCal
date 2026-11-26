package inc.dimasic.taskcal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import inc.dimasic.taskcal.data.repository.ActivitiesRepository
import inc.dimasic.taskcal.data.repository.ApiRemoteRepository
import inc.dimasic.taskcal.data.repository.DataStoreSettingsRepository
import inc.dimasic.taskcal.data.repository.HealthConnectActivitiesRepository
import inc.dimasic.taskcal.data.repository.HealthConnectWeightRepository
import inc.dimasic.taskcal.data.repository.RemoteRepository
import inc.dimasic.taskcal.data.repository.SettingsRepository
import inc.dimasic.taskcal.data.repository.WeightRepository

/** Binds repository interfaces to their concrete, real-data-source implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun activitiesRepository(impl: HealthConnectActivitiesRepository): ActivitiesRepository

    @Binds
    abstract fun settingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun weightRepository(impl: HealthConnectWeightRepository): WeightRepository

    @Binds
    abstract fun remoteRepository(impl: ApiRemoteRepository): RemoteRepository
}