package inc.dimasic.taskcal.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.TaskState
import inc.dimasic.taskcal.domain.usecase.tasks.TasksUseCase
import inc.dimasic.taskcal.domain.usecase.tasks.groupedByPriority
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TasksFilter(val label: String) {
    ACTIVE("Active"),
    COMPLETED("Completed"),
    ALL("All"),
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TasksFilter.ACTIVE)
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
        viewModelScope.launch {
            tasksUseCase.setTaskCompleted(
                taskId = task.taskId,
                occurrenceDate = task.occurrenceDate,
                completed = !task.isDone,
            )
        }
    }

    fun deleteTask(task: TaskListItemUi) {
        viewModelScope.launch {
            tasksUseCase.deleteTask(task.taskId)
        }
    }
}

private fun TasksFilter.accepts(state: TaskState): Boolean = when (this) {
    TasksFilter.ALL -> true
    TasksFilter.ACTIVE -> state != TaskState.DONE
    TasksFilter.COMPLETED -> state == TaskState.DONE
}
