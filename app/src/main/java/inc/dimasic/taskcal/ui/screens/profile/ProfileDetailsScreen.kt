package inc.dimasic.taskcal.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.domain.model.Gender
import inc.dimasic.taskcal.ui.screens.uiViews.ChoiceGroup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

/** Gender is optional; the extra choice is how the user says so. */
private val GENDER_OPTIONS: List<Gender?> = listOf(null) + Gender.entries

/** Edits the personal details; [onSaved] fires once they're written. */
@Composable
fun ProfileDetailsScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val details by viewModel.detailsForm.collectAsState()

    // Keyed on the stored details, so the form starts from them and isn't reset by its own save.
    key(details) {
        DetailsForm(
            initial = details,
            onSave = { name, gender, dateOfBirth, height ->
                viewModel.updateDetails(name, gender, dateOfBirth, height, onSaved = onSaved)
            },
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsForm(
    initial: ProfileDetailsFormUi,
    onSave: (name: String, gender: Gender?, dateOfBirth: LocalDate?, height: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var gender by rememberSaveable { mutableStateOf(initial.gender) }
    var dateOfBirth by rememberSaveable { mutableStateOf(initial.dateOfBirth) }
    var heightText by rememberSaveable { mutableStateOf(initial.height?.toString().orEmpty()) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val height = heightText.toIntOrNull()
    val heightIsValid = heightText.isBlank() || (height != null && height > 0)

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
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            ChoiceGroup(
                label = "Gender",
                options = GENDER_OPTIONS,
                selected = gender,
                optionLabel = { it?.label ?: "Not set" },
                onSelected = { gender = it },
            )

            Column {
                Text(text = "Date of birth", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Text(dateOfBirth?.format(dateOfBirthFormatter) ?: "Pick a date")
                    }
                    if (dateOfBirth != null) {
                        TextButton(onClick = { dateOfBirth = null }) { Text("Clear") }
                    }
                }
            }

            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height (${initial.heightUnit})") },
                singleLine = true,
                isError = !heightIsValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(
            onClick = { onSave(name, gender, dateOfBirth, height) },
            enabled = heightIsValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text("Save details")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (dateOfBirth ?: LocalDate.now().minusYears(30)).toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { dateOfBirth = it.toUtcLocalDate() }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
