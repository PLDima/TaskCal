package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.usecase.CaloriesUseCase
import inc.dimasic.taskcal.domain.usecase.TasksUseCase
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
    private val caloriesUseCase: CaloriesUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val tasksForSelectedDate: StateFlow<List<TaskListItemUi>> =
        tasksUseCase.tasksForDate(_selectedDate)
            .map { tasks -> tasks.map { it.toUi() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val caloriesForSelectedDate: StateFlow<CaloriesSummary> =
        caloriesUseCase.caloriesForDate(_selectedDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaloriesSummary.Empty)

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleTaskCompletion(task: TaskListItemUi) {
        viewModelScope.launch {
            tasksUseCase.setTaskCompleted(
                taskId = task.taskId,
                occurrenceDate = task.occurrenceDate,
                completed = !task.isDone,
            )
        }
    }
}
