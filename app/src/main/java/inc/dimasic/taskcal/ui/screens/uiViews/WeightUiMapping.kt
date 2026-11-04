package inc.dimasic.taskcal.ui.screens.uiViews

import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.usecase.model.WeightSummary
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val measuredFormatter = DateTimeFormatter.ofPattern("MMM d")

/** Differences smaller than this read as "at goal" rather than as a fraction of a kilogram to go. */
private const val AT_GOAL_TOLERANCE_KG = 0.05

internal fun WeightSummary.toUi(units: UnitSystem, changeWording: String = "this period"): WeightSummaryUi =
    WeightSummaryUi(
        currentLabel = currentKg?.let { units.weightLabel(it) } ?: "No weigh-in yet",
        measuredLabel = measuredOn?.let { "Measured ${it.format(measuredFormatter)}" },
        goalLabel = "Goal ${units.weightLabel(goalKg)}",
        toGoalLabel = toGoalKg?.let { toGoal ->
            when {
                abs(toGoal) < AT_GOAL_TOLERANCE_KG -> "At goal"
                toGoal > 0 -> "${units.weightLabel(toGoal)} to go"
                else -> "${units.weightLabel(-toGoal)} under goal"
            }
        },
        changeLabel = changeKg?.let { "%+.1f %s $changeWording".format(units.fromKilograms(it), units.weightUnit) },
    )
