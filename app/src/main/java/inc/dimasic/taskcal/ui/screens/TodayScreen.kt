package inc.dimasic.taskcal.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.ui.screens.uiViews.CaloriesSection
import inc.dimasic.taskcal.ui.screens.uiViews.TasksSection
import inc.dimasic.taskcal.ui.screens.uiViews.WeekDatesRow

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val calories by viewModel.caloriesForSelectedDate.collectAsState()

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        WeekDatesRow(
            selectedDate = selectedDate,
            onDateSelected = viewModel::selectDate,
            modifier = Modifier.fillMaxWidth(),
        )

        TasksSection(
            tasks = tasks,
            onSeeAllClick = {
                // TODO: navigate to Tasks screen
            },
            onTaskCheckedChange = { task -> viewModel.toggleTaskCompletion(task.occurrenceId) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )

        CaloriesSection(
            calories = calories,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
        )
    }
}
