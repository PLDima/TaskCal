package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch

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
     * Logs an item against the day currently being viewed, then reloads. [onSaved] fires once the
     * write has landed, so the dialog dismisses only after the entry exists.
     *
     * Deliberately not routed through `launchRequest`: saving a row is not this screen's content
     * load, and putting it there would throw the whole screen behind the blocking loading dialog.
     * The `refresh()` that follows is what legitimately shows it.
     */
    fun logMeal(
        mealType: MealType,
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            foodUseCase.addEntry(
                date = _selectedDate.value,
                mealType = mealType,
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
            )
            onSaved()
            refresh()
        }
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
    entries = entries.map { entry ->
        FoodEntryUi(
            name = entry.note,
            macrosLabel = "${entry.calories} kcal · ${entry.protein.roundToInt()}P " +
                "${entry.carbs.roundToInt()}C ${entry.fat.roundToInt()}F",
        )
    },
)
