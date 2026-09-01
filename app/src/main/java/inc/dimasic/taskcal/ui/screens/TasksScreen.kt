package inc.dimasic.taskcal.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.model.domain.Priority
import inc.dimasic.taskcal.model.domain.TaskState
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import inc.dimasic.taskcal.ui.theme.StatusDoneGreen
import inc.dimasic.taskcal.usecase.TaskListItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val dateLabelFormatter = DateTimeFormatter.ofPattern("MMM d")

private fun LocalDate?.toRelativeLabel(): String {
    if (this == null) return "Not specified"
    val today = LocalDate.now()
    return when (ChronoUnit.DAYS.between(today, this)) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> format(dateLabelFormatter)
    }
}

private data class TaskSection(
    val title: String,
    val tasks: List<TaskListItem>,
)

private fun List<TaskListItem>.toPrioritySections(): List<TaskSection> {
    val byPriority = groupBy { it.priority }
    return listOfNotNull(
        byPriority[Priority.HIGH]?.let { TaskSection("High priority", it) },
        byPriority[Priority.MEDIUM]?.let { TaskSection("Medium priority", it) },
        byPriority[Priority.LOW]?.let { TaskSection("Low priority", it) },
    )
}

@Composable
fun TasksScreen(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val sections = tasks.toPrioritySections()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            FilterTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = viewModel::selectFilter,
                modifier = Modifier.fillMaxWidth(),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                sections.forEach { section ->
                item {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ItemBackground),
                    ) {
                        section.tasks.forEachIndexed { index, task ->
                            TaskRow(
                                task = task,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (index != section.tasks.lastIndex) {
                                HorizontalDivider(color = ItemSplitter, thickness = 1.dp)
                            }
                        }
                    }
                }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add task")
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: TasksFilter,
    onFilterSelected: (TasksFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TasksFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                label = "tabBackground",
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "tabTextColor",
            )
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onFilterSelected(filter) },
                    )
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskListItem,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = task.state == TaskState.DONE
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundCheckbox(checked = isDone, onCheckedChange = onCheckedChange)

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(priorityColor(task.priority)),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = task.date.toRelativeLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Task options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                // TODO: wire up edit/delete task actions
                DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false })
            }
        }
    }
}

@Composable
private fun RoundCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .then(
                if (checked) {
                    Modifier.background(StatusDoneGreen)
                } else {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = onCheckedChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> MaterialTheme.colorScheme.error
    Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
    Priority.LOW -> MaterialTheme.colorScheme.primary
}
