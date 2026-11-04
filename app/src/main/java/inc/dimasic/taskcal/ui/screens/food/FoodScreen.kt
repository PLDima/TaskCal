package inc.dimasic.taskcal.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.common.utils.defaultRequestHandler
import inc.dimasic.taskcal.domain.model.MealType
import inc.dimasic.taskcal.ui.screens.uiViews.CaloriesTotalsRow
import inc.dimasic.taskcal.ui.screens.uiViews.ChoiceGroup
import inc.dimasic.taskcal.ui.screens.uiViews.MacroBreakdownRow
import inc.dimasic.taskcal.ui.screens.uiViews.WeekDatesRow
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter

@Composable
fun FoodScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = hiltViewModel(),
) {
    val selectedDate by viewModel.selectedDate.collectAsState()

    // The ViewModel is activity-scoped, so it outlives this screen. Re-read on every entry rather
    // than once on creation, so reopening the tab shows the day's real figures instead of whatever
    // was loaded last time.
    LaunchedEffect(Unit) { viewModel.refresh() }

    var mealDialog by remember { mutableStateOf<MealDialogTarget?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            WeekDatesRow(
                selectedDate = selectedDate,
                onDateSelected = viewModel::selectDate,
                modifier = Modifier.fillMaxWidth(),
            )

            viewModel.getState().defaultRequestHandler(
                onSuccess = { day ->
                    FoodDayContent(
                        foodDay = day,
                        onEditEntry = { entry -> mealDialog = MealDialogTarget.Existing(entry) },
                        onDeleteEntry = { entry -> viewModel.deleteMeal(entry.id) },
                        modifier = Modifier.weight(1f),
                    )
                },
            )
        }

        FloatingActionButton(
            onClick = { mealDialog = MealDialogTarget.New },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Log meal")
        }
    }

    mealDialog?.let { target ->
        val editing = (target as? MealDialogTarget.Existing)?.entry
        LogMealDialog(
            entry = editing,
            onConfirm = { type, name, calories, protein, carbs, fat ->
                viewModel.saveMeal(
                    // Reusing the id is what turns the save into an edit of the same row.
                    id = editing?.id,
                    mealType = type,
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    onSaved = { mealDialog = null },
                )
            },
            onDismiss = { mealDialog = null },
        )
    }
}

/**
 * What the meal dialog is open for. Null is closed — a plain nullable entry could not tell "closed"
 * apart from "open to log something new", since both carry no entry.
 */
private sealed interface MealDialogTarget {
    data object New : MealDialogTarget
    data class Existing(val entry: FoodEntryUi) : MealDialogTarget
}

@Composable
private fun LogMealDialog(
    entry: FoodEntryUi?,
    onConfirm: (MealType, String, Int, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit,
) {
    // Prefilled when editing. A new entry carries no meal context, so it opens on the first meal
    // and the user picks. Only composed while open, so these initialisers re-run on each open.
    var mealType by remember { mutableStateOf(entry?.mealType ?: MealType.BREAKFAST) }
    var name by remember { mutableStateOf(entry?.name.orEmpty()) }
    var calories by remember { mutableStateOf(entry?.calories?.toString().orEmpty()) }
    var protein by remember { mutableStateOf(entry?.protein?.toInputText().orEmpty()) }
    var carbs by remember { mutableStateOf(entry?.carbs?.toInputText().orEmpty()) }
    var fat by remember { mutableStateOf(entry?.fat?.toInputText().orEmpty()) }

    val parsedCalories = calories.toIntOrNull()
    val canSave = name.isNotBlank() && parsedCalories != null && parsedCalories > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry == null) "Log meal" else "Edit meal") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                MacroField(value = calories, onValueChange = { calories = it }, label = "Calories (kcal)")
                MacroField(value = protein, onValueChange = { protein = it }, label = "Protein (g)")
                MacroField(value = carbs, onValueChange = { carbs = it }, label = "Carbs (g)")
                MacroField(value = fat, onValueChange = { fat = it }, label = "Fat (g)")

                ChoiceGroup(
                    label = "Meal",
                    options = MealType.entries,
                    selected = mealType,
                    optionLabel = { it.label },
                    onSelected = { mealType = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Guarded by `canSave`, so calories has already parsed. A blank macro is a
                    // zero rather than a reason to block the save.
                    onConfirm(
                        mealType,
                        name.trim(),
                        parsedCalories ?: 0,
                        protein.toDoubleOrNull() ?: 0.0,
                        carbs.toDoubleOrNull() ?: 0.0,
                        fat.toDoubleOrNull() ?: 0.0,
                    )
                },
                enabled = canSave,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** A whole number reads better as "45" than "45.0" in an input field; real fractions keep theirs. */
private fun Double.toInputText(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()

@Composable
private fun MacroField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun FoodDayContent(
    foodDay: FoodDayUi,
    onEditEntry: (FoodEntryUi) -> Unit,
    onDeleteEntry: (FoodEntryUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        // Enough room to scroll the last row clear of the floating button.
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                CaloriesTotalsRow(
                    used = foodDay.caloriesUsed,
                    goal = foodDay.caloriesGoal,
                    modifier = Modifier.fillMaxWidth(),
                )
                MacroBreakdownRow(
                    macros = foodDay.macros,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                )
            }
        }

        if (foodDay.mealSections.isEmpty()) {
            item {
                Text(
                    text = "Nothing logged yet.\nAdd a meal to start tracking.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ItemBackground)
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                )
            }
        }

        foodDay.mealSections.forEach { section ->
            item {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ItemBackground),
                ) {
                    section.entries.forEachIndexed { index, entry ->
                        FoodEntryRow(
                            entry = entry,
                            onEdit = { onEditEntry(entry) },
                            onDelete = { onDeleteEntry(entry) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        // Dividers go between rows only — nothing follows the last one now that
                        // the add row is gone.
                        if (index != section.entries.lastIndex) {
                            HorizontalDivider(color = ItemSplitter, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodEntryRow(
    entry: FoodEntryUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = entry.macrosLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Food entry options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}
