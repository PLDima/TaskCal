package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import inc.dimasic.taskcal.model.Priority
import inc.dimasic.taskcal.model.Status
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import inc.dimasic.taskcal.ui.theme.StatusDoneGreen
import inc.dimasic.taskcal.repository.TaskListItem

private const val VISIBLE_TASK_COUNT = 4

@Composable
fun TasksSection(
    tasks: List<TaskListItem>,
    onSeeAllClick: () -> Unit,
    onTaskCheckedChange: (TaskListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleTasks = tasks
        .sortedByDescending { it.priority.ordinal }
        .take(VISIBLE_TASK_COUNT)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Tasks", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = {
                // TODO: add task logic
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground),
        ) {
            visibleTasks.forEachIndexed { index, task ->
                TaskItem(
                    task = task,
                    onCheckedChange = { onTaskCheckedChange(task) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (index != visibleTasks.lastIndex) {
                    HorizontalDivider(color = ItemSplitter, thickness = 1.dp)
                }
            }
        }

        Text(
            text = "See all",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable(onClick = onSeeAllClick),
        )
    }
}

@Composable
private fun TaskItem(
    task: TaskListItem,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = task.status == Status.DONE

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(priorityColor(task.priority)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RoundCheckbox(checked = isDone, onCheckedChange = onCheckedChange)
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
