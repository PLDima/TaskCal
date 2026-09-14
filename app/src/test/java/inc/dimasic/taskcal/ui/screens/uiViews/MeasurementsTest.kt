package inc.dimasic.taskcal.ui.screens.uiViews

import inc.dimasic.taskcal.domain.model.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Test

/** Storage is metric; these are the conversions at the edge, and the labels they produce. */
class MeasurementsTest {

    @Test
    fun `metric passes values through untouched`() {
        assertEquals("72.4 kg", UnitSystem.METRIC.weightLabel(72.4))
        assertEquals("5.0 km", UnitSystem.METRIC.distanceLabel(5_000))
        assertEquals("180 cm", UnitSystem.METRIC.heightLabel(180))
        assertEquals(72.4, UnitSystem.METRIC.toKilograms(72.4), 0.0)
        assertEquals(180, UnitSystem.METRIC.toCentimeters(180))
    }

    @Test
    fun `imperial words weight, distance and height in their own units`() {
        assertEquals("159.6 lb", UnitSystem.IMPERIAL.weightLabel(72.4))
        assertEquals("3.1 mi", UnitSystem.IMPERIAL.distanceLabel(5_000))
        assertEquals("5'11\"", UnitSystem.IMPERIAL.heightLabel(180))
    }

    @Test
    fun `an entered imperial value round-trips through kilograms and centimetres`() {
        val kilograms = UnitSystem.IMPERIAL.toKilograms(160.0)
        assertEquals(72.57, kilograms, 0.01)
        assertEquals(160.0, UnitSystem.IMPERIAL.fromKilograms(kilograms), 0.0001)

        val centimeters = UnitSystem.IMPERIAL.toCentimeters(71)
        assertEquals(180, centimeters)
        assertEquals(71, UnitSystem.IMPERIAL.fromCentimeters(centimeters))
    }
}
