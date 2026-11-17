package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass
import inc.dimasic.taskcal.domain.model.MealType
import java.time.LocalDate

/** Wire form of a logged food entry. [id] may be left out of a request because it is in the URL. */
@JsonClass(generateAdapter = true)
data class FoodEntryDto(
    val id: String? = null,
    val date: LocalDate,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val name: String,
)