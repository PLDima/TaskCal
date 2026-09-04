package inc.dimasic.taskcal.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds repository interfaces to their concrete, real-data-source implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun activitiesRepository(impl: HealthConnectActivitiesRepository): ActivitiesRepository
}
