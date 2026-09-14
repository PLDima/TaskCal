package inc.dimasic.taskcal.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import inc.dimasic.taskcal.domain.model.Task
import inc.dimasic.taskcal.domain.usecase.TasksUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val tasksUseCase: TasksUseCase,
) : ViewModel() {

    private val _editor = MutableStateFlow<TaskEditorState?>(null)

    /** Null until the task being edited has been read; a new task is ready immediately. */
    val editor: StateFlow<TaskEditorState?> = _editor.asStateFlow()

    private var session = 0

    /**
     * Prepares the form for a new task, or for editing [taskId]. Clears the previous form first
     * so a task that takes a moment to load never shows the last visit's fields in the meantime.
     */
    fun open(taskId: String?) {
        _editor.value = null
        viewModelScope.launch {
            val form = taskId?.let { tasksUseCase.task(it) }?.toForm() ?: TaskFormUi.Empty
            _editor.value = TaskEditorState(form = form, session = ++session)
        }
    }

    /** Persists the form as a new task, or over [taskId], then invokes [onSaved] once it's written. */
    fun save(taskId: String?, form: TaskFormUi, onSaved: () -> Unit) {
        if (form.title.isBlank()) return
        viewModelScope.launch {
            if (taskId == null) {
                tasksUseCase.addTask(
                    title = form.title.trim(),
                    description = form.description.trim(),
                    priority = form.priority,
                    recurrenceType = form.recurrenceType,
                    assignedDate = form.date,
                    dueTime = form.time,
                )
            } else {
                tasksUseCase.updateTask(
                    taskId = taskId,
                    title = form.title.trim(),
                    description = form.description.trim(),
                    priority = form.priority,
                    recurrenceType = form.recurrenceType,
                    assignedDate = form.date,
                    dueTime = form.time,
                )
            }
            onSaved()
        }
    }
}

private fun Task.toForm() = TaskFormUi(
    title = title,
    description = description,
    priority = priority,
    recurrenceType = recurrenceType,
    date = assignedDateTime?.toLocalDate(),
    time = assignedDateTime?.toLocalTime()?.takeIf { hasDueTime },
)
