package inc.dimasic.taskcal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.domain.model.Priority
import inc.dimasic.taskcal.domain.model.RecurrenceType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val scheduleDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/** Tasks carry a time as well as a date; nothing collects one yet, so they default to 9am. */
private val DEFAULT_TASK_TIME: LocalTime = LocalTime.of(9, 0)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onTaskCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddTaskViewModel = hiltViewModel(),
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableStateOf(Priority.MEDIUM) }
    var recurrence by rememberSaveable { mutableStateOf(RecurrenceType.NONE) }
    var schedule by rememberSaveable { mutableStateOf(ScheduleOption.NONE) }
    var pickedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

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
        }

        // A repeating task expands from its start date, so it needs one even when the user picked
        // no schedule — without an anchor it would never produce an occurrence to show.
        val startDate = schedule.toDate(pickedDate)
            ?: LocalDate.now().takeIf { recurrence != RecurrenceType.NONE }

        Button(
            onClick = {
                viewModel.addTask(
                    title = title.trim(),
                    description = description.trim(),
                    priority = priority,
                    recurrenceType = recurrence,
                    assignedDateTime = startDate?.atTime(DEFAULT_TASK_TIME),
                )
                onTaskCreated()
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
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun <T> ChoiceGroup(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Text(
                    text = optionLabel(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { onSelected(option) },
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}
