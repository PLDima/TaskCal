package inc.dimasic.taskcal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.ui.screens.uiViews.CaloriesTotalsRow
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
    val calories by viewModel.caloriesForSelectedDate.collectAsState()
    val sections by viewModel.mealSections.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        WeekDatesRow(
            selectedDate = selectedDate,
            onDateSelected = viewModel::selectDate,
            modifier = Modifier.fillMaxWidth(),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CaloriesTotalsRow(
                        used = calories.caloriesUsed,
                        goal = calories.caloriesGoal,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    MacroBreakdownRow(
                        proteinUsed = calories.proteinUsed,
                        proteinGoal = calories.proteinGoal,
                        fatUsed = calories.fatUsed,
                        fatGoal = calories.fatGoal,
                        carbsUsed = calories.carbsUsed,
                        carbsGoal = calories.carbsGoal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                    )
                }
            }

            sections.forEach { section ->
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
                        section.entries.forEach { entry ->
                            FoodEntryRow(
                                entry = entry,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            HorizontalDivider(color = ItemSplitter, thickness = 1.dp)
                        }
                        AddFoodRow(
                            label = section.addLabel,
                            onClick = {
                                // TODO: add food entry logic
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodEntryRow(
    entry: FoodEntryUi,
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
                // TODO: wire up edit/delete food entry actions
                DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false })
            }
        }
    }
}

@Composable
private fun AddFoodRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
