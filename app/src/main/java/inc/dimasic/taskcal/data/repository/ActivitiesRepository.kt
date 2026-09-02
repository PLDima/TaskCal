package inc.dimasic.taskcal.data.repository

import inc.dimasic.taskcal.data.model.DailyActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores recorded activity, one entry per day. Choosing which days a period covers and summing
 * them against the user's goals are domain decisions and live in
 * [inc.dimasic.taskcal.domain.usecase.ActivitiesUseCase] instead.
 */
@Singleton
class ActivitiesRepository @Inject constructor() {

    private val _dailyActivity = MutableStateFlow(mockDays())

    val dailyActivity: Flow<List<DailyActivity>> = _dailyActivity.asStateFlow()

    private fun mockDays(): List<DailyActivity> {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (0..6).mapNotNull { dayIndex ->
            val date = monday.plusDays(dayIndex.toLong())
            if (date.isAfter(today)) return@mapNotNull null
            val steps = 6_500 + (dayIndex * 1_700) % 6_000
            DailyActivity(
                date = date,
                caloriesBurned = 420 + (dayIndex * 130) % 420,
                steps = steps,
                distanceMeters = (steps * 0.75).toInt(),
                activeMinutes = 18 + (dayIndex * 7) % 30,
                avgHeartRate = 92 + (dayIndex * 4) % 28,
            )
        }
    }
}
