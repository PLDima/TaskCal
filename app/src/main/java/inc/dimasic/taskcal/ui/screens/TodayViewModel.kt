package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.Priority
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.model.Task
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class TodayViewModel @Inject constructor() : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _tasks = MutableStateFlow(mockTasks())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun mockTasks(): List<Task> = listOf(
        Task(
            name = "Finish quarterly report",
            description = "Compile Q2 numbers and send to finance",
            priority = Priority.HIGH,
            status = Status.TODO,
        ),
        Task(
            name = "Review PR #482",
            description = "Check the new auth flow changes",
            priority = Priority.HIGH,
            status = Status.IN_PROGRESS,
        ),
        Task(
            name = "Schedule dentist appointment",
            description = "Call clinic before Friday",
            priority = Priority.MEDIUM,
            status = Status.TODO,
        ),
        Task(
            name = "Buy groceries",
            description = "Milk, eggs, bread, coffee",
            priority = Priority.LOW,
            status = Status.TODO,
        ),
        Task(
            name = "Clean up inbox",
            description = "Archive old threads",
            priority = Priority.LOW,
            status = Status.DONE,
        ),
    )
}
