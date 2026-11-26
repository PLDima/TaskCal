package inc.dimasic.taskcal.ui.screens.food

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.common.CommonViewModel
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.usecase.calories.CaloriesUseCase
import inc.dimasic.taskcal.domain.usecase.food.FoodUseCase
import inc.dimasic.taskcal.domain.usecase.model.MealGroup
import inc.dimasic.taskcal.ui.screens.uiViews.toMacrosUi
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
     * Saves an item against the day currently being viewed, then reloads. A null [id] logs a new
     * entry; the id of an existing one replaces it, which is what the edit dialog passes.
     * [onSaved] fires once the write has landed, so the dialog dismisses only after it exists.
     *
     * Deliberately not routed through `launchRequest`: saving a row is not this screen's content
     * load, and putting it there would throw the whole screen behind the blocking loading dialog.
     * The `refresh()` that follows is what legitimately shows it.
     */
    fun saveMeal(
        id: String? = null,
        mealType: MealType,
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            foodUseCase.saveEntry(
                id = id,
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

    fun deleteMeal(id: String) {
        viewModelScope.launch {
            foodUseCase.deleteEntry(id)
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
                val summary = calories.await()
                FoodDayUi(
                    caloriesUsed = summary.caloriesUsed,
                    caloriesGoal = summary.caloriesGoal,
                    macros = summary.toMacrosUi(),
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
            id = entry.id,
            name = entry.name,
            macrosLabel = "${entry.calories} kcal · ${entry.protein.roundToInt()}P " +
                    "${entry.carbs.roundToInt()}C ${entry.fat.roundToInt()}F",
            mealType = entry.mealType,
            calories = entry.calories,
            protein = entry.protein,
            carbs = entry.carbs,
            fat = entry.fat,
        )
    },
)
