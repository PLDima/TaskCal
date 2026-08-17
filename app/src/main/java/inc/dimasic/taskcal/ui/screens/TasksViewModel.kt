package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.usecase.TaskListItem
import inc.dimasic.taskcal.usecase.TasksUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class TasksFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TasksFilter.ALL)
    val selectedFilter: StateFlow<TasksFilter> = _selectedFilter.asStateFlow()

    val tasks: StateFlow<List<TaskListItem>> =
        combine(tasksUseCase.allTasks(), _selectedFilter) { tasks, filter ->
            when (filter) {
                TasksFilter.ALL -> tasks
                TasksFilter.ACTIVE -> tasks.filter { it.status != Status.DONE }
                TasksFilter.COMPLETED -> tasks.filter { it.status == Status.DONE }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectFilter(filter: TasksFilter) {
        _selectedFilter.value = filter
    }

    fun toggleTaskCompletion(occurrenceId: String) {
        tasksUseCase.toggleTaskCompletion(occurrenceId)
    }
}
