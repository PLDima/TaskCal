package inc.dimasic.taskcal.ui.screens.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.ui.screens.uiViews.CaloriesSection
import inc.dimasic.taskcal.ui.screens.uiViews.WeightCard
import inc.dimasic.taskcal.ui.screens.uiViews.TasksSection
import inc.dimasic.taskcal.ui.screens.uiViews.WeekDatesRow

@Composable
fun TodayScreen(
    onAddTask: () -> Unit,
    onShowTasks: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasksForSelectedDate.collectAsState()
    val calories by viewModel.caloriesForSelectedDate.collectAsState()
    val weight by viewModel.weight.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        WeekDatesRow(
            selectedDate = selectedDate,
            onDateSelected = viewModel::selectDate,
            modifier = Modifier.fillMaxWidth(),
        )

        TasksSection(
            tasks = tasks,
            onSeeAllClick = onShowTasks,
            onAddClick = onAddTask,
            onTaskCheckedChange = { task -> viewModel.toggleTaskCompletion(task) },
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

        weight?.let {
            WeightCard(
                weight = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            )
        }
    }
}
