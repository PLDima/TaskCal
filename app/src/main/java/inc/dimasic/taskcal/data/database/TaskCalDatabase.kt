package inc.dimasic.taskcal.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, TaskCompletionEntity::class, FoodEntryEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class TaskCalDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun foodEntryDao(): FoodEntryDao
}
