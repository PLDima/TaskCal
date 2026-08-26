package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.FoodEntry
import inc.dimasic.taskcal.usecase.CaloriesSummary
import inc.dimasic.taskcal.usecase.CaloriesUseCase
import inc.dimasic.taskcal.usecase.FoodUseCase
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    val foodEntriesForSelectedDate: StateFlow<List<FoodEntry>> =
        foodUseCase.entriesForDate(_selectedDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
}
