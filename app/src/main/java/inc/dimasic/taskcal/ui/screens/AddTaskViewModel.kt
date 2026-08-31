package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.domain.Priority
import inc.dimasic.taskcal.model.domain.RecurrenceType
import inc.dimasic.taskcal.usecase.TasksUseCase
import java.time.LocalDate
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
        scheduledDate: LocalDate?,
    ) {
        if (title.isBlank()) return
        tasksUseCase.addTask(title, description, priority, recurrenceType, scheduledDate)
    }
}
