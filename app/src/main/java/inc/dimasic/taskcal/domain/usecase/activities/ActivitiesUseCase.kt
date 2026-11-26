package inc.dimasic.taskcal.domain.usecase.activities

import inc.dimasic.taskcal.domain.model.ActivitiesPeriod
import inc.dimasic.taskcal.domain.usecase.model.PeriodActivityTotals

interface ActivitiesUseCase {
    suspend fun totalsForPeriod(period: ActivitiesPeriod, offset: Int): PeriodActivityTotals
}