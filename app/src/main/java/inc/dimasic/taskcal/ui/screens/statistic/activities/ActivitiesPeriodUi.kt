package inc.dimasic.taskcal.ui.screens.statistic.activities

import inc.dimasic.taskcal.ui.screens.uiViews.GoalProgressUi
import inc.dimasic.taskcal.ui.screens.uiViews.WeightSummaryUi

/** The Activities statistics content for one period, with every figure already measured and worded. */
data class ActivitiesPeriodUi(
    val dateRangeLabel: String,
    val caloriesBurned: GoalProgressUi,
    val steps: GoalProgressUi,
    val characteristics: List<ActivityCharacteristic>,
    val weight: WeightSummaryUi,
)
