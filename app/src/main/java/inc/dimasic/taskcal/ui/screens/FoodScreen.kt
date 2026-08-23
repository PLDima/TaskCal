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
import inc.dimasic.taskcal.ui.screens.uiViews.CaloriesTotalsRow
import inc.dimasic.taskcal.ui.screens.uiViews.WeekDatesRow

@Composable
fun FoodScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodViewModel = hiltViewModel(),
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calories by viewModel.caloriesForSelectedDate.collectAsState()

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        WeekDatesRow(
            selectedDate = selectedDate,
            onDateSelected = viewModel::selectDate,
            modifier = Modifier.fillMaxWidth(),
        )

        CaloriesTotalsRow(
            used = calories.caloriesUsed,
            goal = calories.caloriesGoal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
    }
}
