package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.TaskState
import inc.dimasic.taskcal.domain.usecase.TasksUseCase
import inc.dimasic.taskcal.domain.usecase.groupedByPriority
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

    val sections: StateFlow<List<TaskSectionUi>> =
        combine(tasksUseCase.allTasks(), _selectedFilter) { tasks, filter ->
            tasks
                .filter { filter.accepts(it.state) }
                .groupedByPriority()
                .map { it.toUi() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectFilter(filter: TasksFilter) {
        _selectedFilter.value = filter
    }

    fun toggleTaskCompletion(task: TaskListItemUi) {
        tasksUseCase.setTaskCompleted(
            taskId = task.taskId,
            occurrenceDate = task.occurrenceDate,
            completed = !task.isDone,
        )
    }
}

private fun TasksFilter.accepts(state: TaskState): Boolean = when (this) {
    TasksFilter.ALL -> true
    TasksFilter.ACTIVE -> state != TaskState.DONE
    TasksFilter.COMPLETED -> state == TaskState.DONE
}
