package inc.dimasic.taskcal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, TaskCompletionEntity::class, FoodEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TaskCalDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskCompletionDao(): TaskCompletionDao
    abstract fun foodEntryDao(): FoodEntryDao
}
