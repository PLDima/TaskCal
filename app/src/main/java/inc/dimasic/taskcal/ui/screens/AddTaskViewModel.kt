package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.usecase.TasksUseCase
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    /** Persists the task, then invokes [onSaved] so the screen navigates only once it's written. */
    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDate: LocalDate?,
        onSaved: () -> Unit,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            tasksUseCase.addTask(title, description, priority, recurrenceType, assignedDate)
            onSaved()
        }
    }
}
