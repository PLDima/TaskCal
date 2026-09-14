package inc.dimasic.taskcal.data.model

import java.time.LocalDate

/** One weigh-in as the activity source recorded it. */
data class RecordedWeight(
    val date: LocalDate,
    val kilograms: Double,
)
