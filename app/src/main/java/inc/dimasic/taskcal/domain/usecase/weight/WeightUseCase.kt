package inc.dimasic.taskcal.domain.usecase.weight

import inc.dimasic.taskcal.domain.model.DateRange
import inc.dimasic.taskcal.domain.usecase.model.WeightSummary

interface WeightUseCase {
    suspend fun summary(changeOver: DateRange? = null): WeightSummary
}
