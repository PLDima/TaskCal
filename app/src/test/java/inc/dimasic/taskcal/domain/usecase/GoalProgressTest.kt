package inc.dimasic.taskcal.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

/** One rule for every ring, bar and "% of goal" label, so they can never disagree with each other. */
class GoalProgressTest {

    @Test
    fun `fraction fills the bar exactly once, however far past the goal`() {
        assertEquals(0.5f, goalFraction(50, 100), 0f)
        assertEquals(1f, goalFraction(150, 100), 0f)
        assertEquals(0.5f, goalFraction(70.0, 140.0), 0f)
    }

    @Test
    fun `percent rounds to the nearest whole and is not capped`() {
        // 1565 of 2500 is 62.6% — the ring and the row must both say 63.
        assertEquals(63, goalPercent(1565, 2500))
        assertEquals(150, goalPercent(150, 100))
        assertEquals(63, goalPercent(156.5, 250.0))
    }

    @Test
    fun `remaining and overshoot never go negative`() {
        assertEquals(30, goalRemaining(70, 100))
        assertEquals(0, goalRemaining(120, 100))
        assertEquals(0, goalOvershoot(70, 100))
        assertEquals(20, goalOvershoot(120, 100))
    }

    @Test
    fun `a goal of zero reads as no progress rather than dividing by it`() {
        assertEquals(0f, goalFraction(10, 0), 0f)
        assertEquals(0, goalPercent(10, 0))
        assertEquals(0, goalRemaining(10, 0))
        assertEquals(10, goalOvershoot(10, 0))
    }
}
