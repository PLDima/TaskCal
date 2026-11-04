package inc.dimasic.taskcal.data.database

import kotlinx.coroutines.flow.flowOf

/** In-memory [FoodEntryDao], so the real [inc.dimasic.taskcal.data.repository.FoodRepository] can run without Room. */
internal class FakeFoodEntryDao(private val entries: List<FoodEntryEntity>) : FoodEntryDao {
    override fun observeAll() = flowOf(entries)
    override suspend fun forDate(date: String) = entries.filter { it.date == date }
    override suspend fun upsert(entry: FoodEntryEntity) = error("not needed for this test")
    override suspend fun delete(id: String) = error("not needed for this test")
}
