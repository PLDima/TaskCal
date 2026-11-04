package inc.dimasic.taskcal.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
