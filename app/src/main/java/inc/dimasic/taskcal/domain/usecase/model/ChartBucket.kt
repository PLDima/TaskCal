package inc.dimasic.taskcal.domain.usecase.model

import inc.dimasic.taskcal.domain.model.DateRange

/** One column of a period chart: what it's labelled and which days it covers. */
data class ChartBucket(
    val label: String,
    val range: DateRange,
)
