package inc.dimasic.taskcal.data.remote.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NutritionGoalDto(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
)