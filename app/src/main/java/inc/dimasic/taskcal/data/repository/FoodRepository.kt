package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.local.database.FoodEntryDao
import inc.dimasic.taskcal.data.local.database.toDomain
import inc.dimasic.taskcal.data.local.database.toEntity
import inc.dimasic.taskcal.domain.model.FoodEntry
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed store of logged food entries. Selecting the entries for a period, and grouping them
 * into meals, are domain decisions and live in
 * [inc.dimasic.taskcal.domain.usecase.food.FoodUseCase] instead.
 */
@Singleton
class FoodRepository @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
) {

    /**
     * Every entry, as a live stream. The period-based statistics screens read from here; the day
     * screens use [entriesForDate], which is a one-shot read they can load alongside everything
     * else they need.
     */
    val foodEntries: Flow<List<FoodEntry>> =
        foodEntryDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun entriesForDate(date: LocalDate): List<FoodEntry> =
        foodEntryDao.forDate(date.toString()).map { it.toDomain() }

    /** Writes the entry, replacing whatever is stored under the same [FoodEntry.id]. */
    suspend fun saveEntry(entry: FoodEntry) {
        foodEntryDao.upsert(entry.toEntity())
    }

    suspend fun deleteEntry(id: String) {
        foodEntryDao.delete(id)
    }
}
