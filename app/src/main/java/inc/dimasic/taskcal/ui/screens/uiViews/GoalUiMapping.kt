package inc.dimasic.taskcal.ui.screens.uiViews

import inc.dimasic.taskcal.domain.usecase.goalFraction
import inc.dimasic.taskcal.domain.usecase.goalOvershoot
import inc.dimasic.taskcal.domain.usecase.goalPercent
import inc.dimasic.taskcal.domain.usecase.goalRemaining
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import kotlin.math.roundToInt

/**
 * Shared mapping from goal-measured domain figures onto what the rings and bars render. Called
 * from ViewModels only; the composables never see the domain values.
 */

/** [remainingLabel] and [overLabel] follow the number, e.g. "left" → "935 left". */
internal fun goalProgressUi(value: Int, goal: Int, remainingLabel: String, overLabel: String): GoalProgressUi {
    val overshoot = goalOvershoot(value, goal)
    val isOver = value > goal
    return GoalProgressUi(
        value = value,
        goal = goal,
        fraction = goalFraction(value, goal),
        percent = goalPercent(value, goal),
        isOver = isOver,
        statusLabel = if (isOver) "$overshoot $overLabel" else "${goalRemaining(value, goal)} $remainingLabel",
    )
}

internal fun macroProgressUi(label: String, used: Double, goal: Double) = MacroProgressUi(
    label = label,
    amountLabel = "${used.roundToInt()}/${goal.coerceAtLeast(0.0).roundToInt()}g",
    fraction = goalFraction(used, goal),
    percent = goalPercent(used, goal),
)

/** Protein, fat, carbs — the order the breakdown row shows them in. */
internal fun CaloriesSummary.toMacrosUi(): List<MacroProgressUi> = listOf(
    macroProgressUi("Protein", proteinUsed, proteinGoal),
    macroProgressUi("Fat", fatUsed, fatGoal),
    macroProgressUi("Carbs", carbsUsed, carbsGoal),
)

internal fun CaloriesSummary.toSectionUi() = CaloriesSectionUi(
    goal = goalProgressUi(caloriesUsed, caloriesGoal, remainingLabel = "Today balance", overLabel = "kcal over"),
    activity = goalProgressUi(caloriesUsed, caloriesBurned, remainingLabel = "Today balance", overLabel = "kcal over"),
    macros = toMacrosUi(),
)
