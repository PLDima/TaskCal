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

/**
 * Adds the food log. Written out rather than falling back to a destructive migration, since by now
 * the tasks table holds real data that dropping the database would take with it.
 */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `food_entries` (
                `id` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `mealType` TEXT NOT NULL,
                `calories` INTEGER NOT NULL,
                `protein` REAL NOT NULL,
                `carbs` REAL NOT NULL,
                `fat` REAL NOT NULL,
                `note` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): TaskCalDatabase =
        Room.databaseBuilder(context, TaskCalDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun taskDao(database: TaskCalDatabase): TaskDao = database.taskDao()

    @Provides
    fun taskCompletionDao(database: TaskCalDatabase): TaskCompletionDao = database.taskCompletionDao()

    @Provides
    fun foodEntryDao(database: TaskCalDatabase): FoodEntryDao = database.foodEntryDao()
}
