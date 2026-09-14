package inc.dimasic.taskcal.data.datastore

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.domain.model.UnitSystem
import inc.dimasic.taskcal.domain.model.UserProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * A fresh install has nothing stored, so every goal must fall back to its default; and a goal
 * that was stored must win over that default without disturbing the others.
 */
class UserProfilePreferencesTest {

    @Test
    fun `nothing stored reads as the default profile`() {
        assertEquals(UserProfile.Default, emptyPreferences().toUserProfile())
    }

    @Test
    fun `a stored goal overrides its default and leaves the rest alone`() {
        val preferences = preferencesOf(
            UserProfileKeys.STEPS_GOAL to 12_000,
            UserProfileKeys.PROTEIN_GOAL to 160.0,
        )

        val profile = preferences.toUserProfile()

        assertEquals(12_000, profile.stepsGoal)
        assertEquals(160.0, profile.nutritionGoal.protein, 0.0)
        assertEquals(UserProfile.Default.distanceGoalMeters, profile.distanceGoalMeters)
        assertEquals(UserProfile.Default.nutritionGoal.calories, profile.nutritionGoal.calories)
    }

    @Test
    fun `every goal has its own key`() {
        val preferences = preferencesOf(
            UserProfileKeys.STEPS_GOAL to 1,
            UserProfileKeys.WEIGHT_GOAL to 3.0,
            UserProfileKeys.CALORIES_GOAL to 4,
            UserProfileKeys.PROTEIN_GOAL to 5.0,
            UserProfileKeys.CARBS_GOAL to 6.0,
            UserProfileKeys.FAT_GOAL to 7.0,
        )

        val profile = preferences.toUserProfile()

        // Deliberately all different, so a key wired to the wrong field shows up as a failure.
        assertEquals(1, profile.stepsGoal)
        assertEquals(3.0, profile.weightGoal, 0.0)
        assertEquals(4, profile.nutritionGoal.calories)
        assertEquals(5.0, profile.nutritionGoal.protein, 0.0)
        assertEquals(6.0, profile.nutritionGoal.carbs, 0.0)
        assertEquals(7.0, profile.nutritionGoal.fat, 0.0)
    }

    @Test
    fun `personal details are optional and read back as typed values`() {
        val preferences = preferencesOf(
            UserProfileKeys.NAME to "Alex",
            UserProfileKeys.GENDER to Gender.OTHER.name,
            UserProfileKeys.DATE_OF_BIRTH to "1995-05-20",
            UserProfileKeys.HEIGHT_CM to 175,
        )

        val profile = preferences.toUserProfile()

        assertEquals("Alex", profile.name)
        assertEquals(Gender.OTHER, profile.gender)
        assertEquals(LocalDate.of(1995, 5, 20), profile.dateOfBirth)
        assertEquals(175, profile.heightCm)

        val unset = emptyPreferences().toUserProfile()
        assertEquals("", unset.name)
        assertNull(unset.gender)
        assertNull(unset.dateOfBirth)
        assertNull(unset.heightCm)
    }

    @Test
    fun `units are metric until chosen`() {
        assertEquals(UnitSystem.METRIC, emptyPreferences().toUnitSystem())
        assertEquals(UnitSystem.IMPERIAL, preferencesOf(UserProfileKeys.UNIT_SYSTEM to "IMPERIAL").toUnitSystem())
    }
}
