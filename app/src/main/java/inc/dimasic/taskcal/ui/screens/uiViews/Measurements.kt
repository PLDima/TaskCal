package inc.dimasic.taskcal.ui.screens.uiViews

import inc.dimasic.taskcal.domain.model.UnitSystem
import kotlin.math.roundToInt

/**
 * Wording and entering measurements in the user's chosen units. Storage is always metric; these
 * convert at the edge, in ViewModels, so composables only ever see a finished label or the value
 * to put in an input field.
 */

private const val KILOGRAMS_PER_POUND = 0.45359237
private const val METERS_PER_MILE = 1_609.344
private const val CENTIMETERS_PER_INCH = 2.54
private const val INCHES_PER_FOOT = 12

internal val UnitSystem.weightUnit: String
    get() = when (this) {
        UnitSystem.METRIC -> "kg"
        UnitSystem.IMPERIAL -> "lb"
    }

internal val UnitSystem.heightUnit: String
    get() = when (this) {
        UnitSystem.METRIC -> "cm"
        UnitSystem.IMPERIAL -> "in"
    }

/** e.g. "72.4 kg" or "159.6 lb". */
internal fun UnitSystem.weightLabel(kilograms: Double): String =
    "%.1f %s".format(fromKilograms(kilograms), weightUnit)

/** e.g. "5.0 km" or "3.1 mi". */
internal fun UnitSystem.distanceLabel(meters: Int): String = when (this) {
    UnitSystem.METRIC -> "%.1f km".format(meters / 1_000.0)
    UnitSystem.IMPERIAL -> "%.1f mi".format(meters / METERS_PER_MILE)
}

/** e.g. "180 cm" or "5'11\"". */
internal fun UnitSystem.heightLabel(centimeters: Int): String = when (this) {
    UnitSystem.METRIC -> "$centimeters cm"
    UnitSystem.IMPERIAL -> {
        val totalInches = (centimeters / CENTIMETERS_PER_INCH).roundToInt()
        "${totalInches / INCHES_PER_FOOT}'${totalInches % INCHES_PER_FOOT}\""
    }
}

internal fun UnitSystem.fromKilograms(kilograms: Double): Double = when (this) {
    UnitSystem.METRIC -> kilograms
    UnitSystem.IMPERIAL -> kilograms / KILOGRAMS_PER_POUND
}

internal fun UnitSystem.toKilograms(value: Double): Double = when (this) {
    UnitSystem.METRIC -> value
    UnitSystem.IMPERIAL -> value * KILOGRAMS_PER_POUND
}

internal fun UnitSystem.fromCentimeters(centimeters: Int): Int = when (this) {
    UnitSystem.METRIC -> centimeters
    UnitSystem.IMPERIAL -> (centimeters / CENTIMETERS_PER_INCH).roundToInt()
}

internal fun UnitSystem.toCentimeters(value: Int): Int = when (this) {
    UnitSystem.METRIC -> value
    UnitSystem.IMPERIAL -> (value * CENTIMETERS_PER_INCH).roundToInt()
}
