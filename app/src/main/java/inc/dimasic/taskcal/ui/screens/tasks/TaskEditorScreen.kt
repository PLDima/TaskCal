package inc.dimasic.taskcal.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import inc.dimasic.taskcal.ui.screens.uiViews.ChoiceGroup
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val scheduleDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private enum class ScheduleOption(val label: String) {
    NONE("None"),
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    PICK_DATE("Pick date"),
}

private fun ScheduleOption.toDate(pickedDate: LocalDate?): LocalDate? = when (this) {
    ScheduleOption.NONE -> null
    ScheduleOption.TODAY -> LocalDate.now()
    ScheduleOption.TOMORROW -> LocalDate.now().plusDays(1)
    ScheduleOption.PICK_DATE -> pickedDate
}

/** The option that reproduces [date]: the named day when it is one, otherwise a picked date. */
private fun LocalDate?.toScheduleOption(): ScheduleOption = when (this) {
    null -> ScheduleOption.NONE
    LocalDate.now() -> ScheduleOption.TODAY
    LocalDate.now().plusDays(1) -> ScheduleOption.TOMORROW
    else -> ScheduleOption.PICK_DATE
}

private enum class TimeOption(val label: String) {
    NONE("None"),
    PICK_TIME("Pick time"),
}

/**
 * Creates a task, or edits [taskId] when given. The form is keyed on the editor session, so each
 * visit starts from the task's current values rather than a previous visit's unsaved edits.
 */
@Composable
fun TaskEditorScreen(
    taskId: String?,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskEditorViewModel = hiltViewModel(),
) {
    LaunchedEffect(taskId) { viewModel.open(taskId) }
    val editor by viewModel.editor.collectAsState()

    val state = editor ?: return
    key(state.session) {
        TaskForm(
            initial = state.form,
            onSave = { form -> viewModel.save(taskId, form, onSaved) },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskForm(
    initial: TaskFormUi,
    onSave: (TaskFormUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf(initial.title) }
    var description by rememberSaveable { mutableStateOf(initial.description) }
    var priority by rememberSaveable { mutableStateOf(initial.priority) }
    var recurrence by rememberSaveable { mutableStateOf(initial.recurrenceType) }
    var schedule by rememberSaveable { mutableStateOf(initial.date.toScheduleOption()) }
    var pickedDate by rememberSaveable { mutableStateOf(initial.date.takeIf { schedule == ScheduleOption.PICK_DATE }) }
    var pickedTime by rememberSaveable { mutableStateOf(initial.time) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val date = schedule.toDate(pickedDate)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
            )

            ChoiceGroup(
                label = "Priority",
                options = Priority.entries,
                selected = priority,
                optionLabel = { it.label },
                onSelected = { priority = it },
            )
            ChoiceGroup(
                label = "Repeat",
                options = RecurrenceType.entries,
                selected = recurrence,
                optionLabel = { it.label },
                onSelected = { recurrence = it },
            )
            ChoiceGroup(
                label = "Schedule",
                options = ScheduleOption.entries,
                selected = schedule,
                optionLabel = { option ->
                    if (option == ScheduleOption.PICK_DATE && pickedDate != null) {
                        pickedDate!!.format(scheduleDateFormatter)
                    } else {
                        option.label
                    }
                },
                onSelected = { option ->
                    schedule = option
                    if (option == ScheduleOption.PICK_DATE) showDatePicker = true
                },
            )
            // A time of day only means something once there's a date for it to fall on.
            if (date != null) {
                ChoiceGroup(
                    label = "Time",
                    options = TimeOption.entries,
                    selected = if (pickedTime == null) TimeOption.NONE else TimeOption.PICK_TIME,
                    optionLabel = { option ->
                        if (option == TimeOption.PICK_TIME && pickedTime != null) {
                            pickedTime!!.format(timeFormatter)
                        } else {
                            option.label
                        }
                    },
                    onSelected = { option ->
                        when (option) {
                            TimeOption.NONE -> pickedTime = null
                            TimeOption.PICK_TIME -> showTimePicker = true
                        }
                    },
                )
            }
        }

        Button(
            onClick = {
                onSave(
                    TaskFormUi(
                        title = title,
                        description = description,
                        priority = priority,
                        recurrenceType = recurrence,
                        date = date,
                        time = pickedTime.takeIf { date != null },
                    ),
                )
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text("Save task")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (pickedDate ?: LocalDate.now()).toUtcMillis(),
        )
        val dismiss = {
            showDatePicker = false
            if (pickedDate == null) schedule = ScheduleOption.NONE
        }
        DatePickerDialog(
            onDismissRequest = dismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            pickedDate = millis.toUtcLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = dismiss) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initialTime = pickedTime ?: LocalTime.of(9, 0)
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Due time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
