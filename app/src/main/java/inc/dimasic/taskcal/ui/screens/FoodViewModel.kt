package inc.dimasic.taskcal.ui.screens

import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.common.CommonViewModel
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.usecase.CaloriesUseCase
import inc.dimasic.taskcal.domain.usecase.FoodUseCase
import inc.dimasic.taskcal.domain.usecase.model.MealGroup
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val caloriesUseCase: CaloriesUseCase,
    private val foodUseCase: FoodUseCase,
) : CommonViewModel<FoodDayUi>() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        refresh()
    }

    /**
     * Re-reads everything for the selected date. The screen calls this on entry rather than the
     * ViewModel loading once in `init`: this ViewModel is activity-scoped, so it outlives the
     * screen, and a stale day's figures would otherwise sit there until the date changed.
     *
     * The two reads run alongside each other rather than one after the other: the calorie summary
     * waits on Health Connect, and there is no reason the meal list should queue behind it.
     */
    fun refresh() {
        val date = _selectedDate.value
        launchRequest {
            coroutineScope {
                val calories = async { caloriesUseCase.caloriesForDate(date) }
                val meals = async { foodUseCase.mealsForDate(date) }
                FoodDayUi(
                    calories = calories.await(),
                    mealSections = meals.await().map { it.toUi() },
                )
            }
        }
    }
}

/** Snacks read better pluralised as a heading, though the meal itself is named in the singular. */
private val MealType.sectionTitle: String
    get() = if (this == MealType.SNACK) "Snacks" else label

private fun MealGroup.toUi() = MealSectionUi(
    title = mealType.sectionTitle,
    addLabel = "Add ${mealType.label.lowercase()}",
    entries = entries.map { entry ->
        FoodEntryUi(
            name = entry.note,
            macrosLabel = "${entry.calories} kcal · ${entry.protein.roundToInt()}P " +
                "${entry.carbs.roundToInt()}C ${entry.fat.roundToInt()}F",
        )
    },
)
