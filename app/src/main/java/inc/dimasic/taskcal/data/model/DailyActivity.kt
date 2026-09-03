package inc.dimasic.taskcal.data.model

import java.time.LocalDate

/** One day's recorded activity, as stored. Summing days into a period is a domain concern. */
data class DailyActivity(
    val date: LocalDate,
    val caloriesBurned: Int,
    val steps: Int,
    val distanceMeters: Int,
    val activeMinutes: Int,
    val avgHeartRate: Int,
)
