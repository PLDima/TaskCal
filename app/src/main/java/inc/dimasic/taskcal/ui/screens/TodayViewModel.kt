package inc.dimasic.taskcal.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.model.Priority
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.model.Task
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TodayViewModel @Inject constructor() : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _tasks = MutableStateFlow(mockTasks())

    val tasksForSelectedDate: StateFlow<List<Task>> =
        combine(_tasks, _selectedDate) { tasks, date ->
            tasks.filter { it.date == date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id != taskId) {
                task
            } else {
                task.copy(status = if (task.status == Status.DONE) Status.TODO else Status.DONE)
            }
        }
    }

    private fun mockTasks(): List<Task> {
        val today = LocalDate.now()
        return listOf(
            Task(
                name = "Finish quarterly report",
                description = "Compile Q2 numbers and send to finance",
                priority = Priority.HIGH,
                status = Status.TODO,
                date = today,
            ),
            Task(
                name = "Review PR #482",
                description = "Check the new auth flow changes",
                priority = Priority.HIGH,
                status = Status.IN_PROGRESS,
                date = today,
            ),
            Task(
                name = "Clean up inbox",
                description = "Archive old threads",
                priority = Priority.LOW,
                status = Status.DONE,
                date = today,
            ),
            Task(
                name = "Schedule dentist appointment",
                description = "Call clinic before Friday",
                priority = Priority.MEDIUM,
                status = Status.TODO,
                date = today.plusDays(1),
            ),
            Task(
                name = "Buy groceries",
                description = "Milk, eggs, bread, coffee",
                priority = Priority.LOW,
                status = Status.TODO,
                date = today,
            ),
            Task(
                name = "Work shift",
                description = "Survive with fraps",
                priority = Priority.HIGH,
                status = Status.TODO,
                date = today,
            ),
        )
    }
}
