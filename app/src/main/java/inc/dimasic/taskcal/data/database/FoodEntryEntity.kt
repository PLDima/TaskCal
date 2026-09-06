package inc.dimasic.taskcal.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * How a [inc.dimasic.taskcal.domain.model.FoodEntry] is stored. Room-only; the domain model carries
 * no persistence annotations. See [FoodEntryMapping] for the conversion.
 *
 * [date] is an ISO-8601 `LocalDate` string and [mealType] is an enum name.
 */
@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey val id: String,
    val date: String,
    val mealType: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val note: String,
)
