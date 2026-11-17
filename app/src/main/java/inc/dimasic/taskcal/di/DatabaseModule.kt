package inc.dimasic.taskcal.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import inc.dimasic.taskcal.data.local.database.FoodEntryDao
import inc.dimasic.taskcal.data.local.database.TaskCalDatabase
import inc.dimasic.taskcal.data.local.database.TaskCompletionDao
import inc.dimasic.taskcal.data.local.database.TaskDao
import javax.inject.Singleton

private const val DATABASE_NAME = "taskcal.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): TaskCalDatabase =
        Room.databaseBuilder(context, TaskCalDatabase::class.java, DATABASE_NAME)
            .build()

    @Provides
    fun taskDao(database: TaskCalDatabase): TaskDao = database.taskDao()

    @Provides
    fun taskCompletionDao(database: TaskCalDatabase): TaskCompletionDao = database.taskCompletionDao()

    @Provides
    fun foodEntryDao(database: TaskCalDatabase): FoodEntryDao = database.foodEntryDao()
}
