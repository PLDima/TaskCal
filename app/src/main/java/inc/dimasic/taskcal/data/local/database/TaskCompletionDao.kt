package inc.dimasic.taskcal.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCompletionDao {

    @Query("SELECT * FROM task_completions")
    fun observeAll(): Flow<List<TaskCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: TaskCompletionEntity)

    @Query("DELETE FROM task_completions WHERE taskId = :taskId AND occurrenceDate = :occurrenceDate")
    suspend fun delete(taskId: String, occurrenceDate: String)
}
