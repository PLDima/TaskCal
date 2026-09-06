package inc.dimasic.taskcal.data.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Query("SELECT * FROM food_entries")
    fun observeAll(): Flow<List<FoodEntryEntity>>

    /** [date] is an ISO-8601 `LocalDate` string, matching how [FoodEntryEntity.date] is stored. */
    @Query("SELECT * FROM food_entries WHERE date = :date")
    suspend fun forDate(date: String): List<FoodEntryEntity>
}
