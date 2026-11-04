package inc.dimasic.taskcal.domain.usecase

import kotlin.math.roundToInt

/**
 * Measuring a value against the goal set for it.
 *
 * Progress bars, rings and "% of goal" labels all need the same answer, so the rule lives here
 * once instead of being re-derived in each composable.
 */

/** How far along the goal, from 0 to 1. Overshooting a goal still fills the bar exactly once. */
fun goalFraction(value: Int, goal: Int): Float =
    if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

/** As above, for the gram amounts that macro goals are measured in. */
fun goalFraction(value: Double, goal: Double): Float =
    if (goal > 0) (value / goal).toFloat().coerceIn(0f, 1f) else 0f

/** Percentage of the goal reached, to the nearest whole. Not capped — going past a goal reads above 100%. */
fun goalPercent(value: Int, goal: Int): Int =
    if (goal > 0) (value * 100.0 / goal).roundToInt() else 0

/** As above, for the gram amounts that macro goals are measured in. */
fun goalPercent(value: Double, goal: Double): Int =
    if (goal > 0) (value * 100.0 / goal).roundToInt() else 0

/** Amount still to go, or zero once the goal is met. */
fun goalRemaining(value: Int, goal: Int): Int = (goal - value).coerceAtLeast(0)

/** Amount past the goal, or zero while still short of it. */
fun goalOvershoot(value: Int, goal: Int): Int = (value - goal).coerceAtLeast(0)
