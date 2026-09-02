package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.domain.usecase.TasksUseCase
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    fun addTask(
        title: String,
        description: String,
        priority: Priority,
        recurrenceType: RecurrenceType,
        assignedDateTime: LocalDateTime?,
    ) {
        if (title.isBlank()) return
        tasksUseCase.addTask(title, description, priority, recurrenceType, assignedDateTime)
    }
}
