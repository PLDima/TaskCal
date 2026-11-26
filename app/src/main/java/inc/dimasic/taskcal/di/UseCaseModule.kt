package inc.dimasic.taskcal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import inc.dimasic.taskcal.domain.usecase.activities.ActivitiesUseCase
import inc.dimasic.taskcal.domain.usecase.activities.ActivitiesUseCaseImpl
import inc.dimasic.taskcal.domain.usecase.calories.CaloriesUseCase
import inc.dimasic.taskcal.domain.usecase.calories.CaloriesUseCaseImpl
import inc.dimasic.taskcal.domain.usecase.food.FoodUseCase
import inc.dimasic.taskcal.domain.usecase.food.FoodUseCaseImpl
import inc.dimasic.taskcal.domain.usecase.healthconnect.HealthConnectUseCase
import inc.dimasic.taskcal.domain.usecase.healthconnect.HealthConnectUseCaseImpl
import inc.dimasic.taskcal.domain.usecase.profile.ProfileUseCase
import inc.dimasic.taskcal.domain.usecase.profile.ProfileUseCaseImpl
import inc.dimasic.taskcal.domain.usecase.tasks.TasksUseCase
import inc.dimasic.taskcal.domain.usecase.tasks.TasksUseCaseImpl
import inc.dimasic.taskcal.domain.usecase.weight.WeightUseCase
import inc.dimasic.taskcal.domain.usecase.weight.WeightUseCaseImpl

/** Binds use case interfaces to their implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun activitiesUseCase(impl: ActivitiesUseCaseImpl): ActivitiesUseCase

    @Binds
    abstract fun caloriesUseCase(impl: CaloriesUseCaseImpl): CaloriesUseCase

    @Binds
    abstract fun foodUseCase(impl: FoodUseCaseImpl): FoodUseCase

    @Binds
    abstract fun healthConnectUseCase(impl: HealthConnectUseCaseImpl): HealthConnectUseCase

    @Binds
    abstract fun profileUseCase(impl: ProfileUseCaseImpl): ProfileUseCase

    @Binds
    abstract fun tasksUseCase(impl: TasksUseCaseImpl): TasksUseCase

    @Binds
    abstract fun weightUseCase(impl: WeightUseCaseImpl): WeightUseCase
}