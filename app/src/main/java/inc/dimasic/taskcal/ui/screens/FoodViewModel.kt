package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.domain.usecase.CaloriesUseCase
import inc.dimasic.taskcal.domain.usecase.FoodUseCase
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import inc.dimasic.taskcal.domain.usecase.model.MealGroup
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val caloriesUseCase: CaloriesUseCase,
    private val foodUseCase: FoodUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val caloriesForSelectedDate: StateFlow<CaloriesSummary> =
        caloriesUseCase.caloriesForDate(_selectedDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaloriesSummary.Empty)

    val mealSections: StateFlow<List<MealSectionUi>> =
        foodUseCase.mealsForDate(_selectedDate)
            .map { meals -> meals.map { it.toUi() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
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
