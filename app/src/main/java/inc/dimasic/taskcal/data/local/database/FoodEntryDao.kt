package inc.dimasic.taskcal.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Query("SELECT * FROM food_entries")
    fun observeAll(): Flow<List<FoodEntryEntity>>

    /** [date] is an ISO-8601 `LocalDate` string, matching how [FoodEntryEntity.date] is stored. */
    @Query("SELECT * FROM food_entries WHERE date = :date")
    suspend fun forDate(date: String): List<FoodEntryEntity>

    /** Writing a row whose id already exists replaces it, so this covers editing too. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun delete(id: String)
}
