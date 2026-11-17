package inc.dimasic.taskcal.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.usecase.calories.CaloriesUseCase
import inc.dimasic.taskcal.domain.usecase.profile.ProfileUseCase
import inc.dimasic.taskcal.domain.usecase.tasks.TasksUseCase
import inc.dimasic.taskcal.domain.usecase.weight.WeightUseCase
import inc.dimasic.taskcal.domain.usecase.model.CaloriesSummary
import inc.dimasic.taskcal.ui.screens.uiViews.CaloriesSectionUi
import inc.dimasic.taskcal.ui.screens.uiViews.WeightSummaryUi
import inc.dimasic.taskcal.ui.screens.uiViews.toSectionUi
import inc.dimasic.taskcal.ui.screens.uiViews.toUi
import inc.dimasic.taskcal.ui.screens.tasks.TaskListItemUi
import inc.dimasic.taskcal.ui.screens.tasks.toUi
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
    private val weightUseCase: WeightUseCase,
    profileUseCase: ProfileUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val tasksForSelectedDate: StateFlow<List<TaskListItemUi>> =
        tasksUseCase.tasksForDate(_selectedDate)
            .map { tasks -> tasks.map { it.toUi() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val caloriesForSelectedDate: StateFlow<CaloriesSectionUi> =
        _selectedDate.map { caloriesUseCase.caloriesForDate(it).toSectionUi() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CaloriesSummary.Empty.toSectionUi())

    /** The weight doesn't follow the selected day; it's whatever was last weighed in. */
    val weight: StateFlow<WeightSummaryUi?> =
        profileUseCase.unitSystem
            .map { units -> weightUseCase.summary().toUi(units) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
