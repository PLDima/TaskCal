package inc.dimasic.taskcal.ui.screens.statistic.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.domain.model.TasksPeriod
import inc.dimasic.taskcal.domain.usecase.model.TaskStatusBreakdown
import inc.dimasic.taskcal.domain.usecase.model.TasksSummary
import inc.dimasic.taskcal.ui.screens.statistic.statistic_views.PeriodNavigator
import inc.dimasic.taskcal.ui.screens.uiViews.ChartSeries
import inc.dimasic.taskcal.ui.screens.uiViews.CommonChart
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter

private val ON_TIME_CHART_Y_AXIS_MARKS = listOf(0, 25, 50, 75, 100)
private const val ON_TIME_CHART_MAX_VALUE = 100

@Composable
fun TasksStatisticScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksStatisticViewModel = hiltViewModel(),
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dateRangeLabel by viewModel.dateRangeLabel.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val onTimeRateChart by viewModel.onTimeRateChart.collectAsState()
    val taskStatus by viewModel.taskStatus.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TasksPeriodTabs(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = viewModel::selectPeriod,
            modifier = Modifier.fillMaxWidth(),
        )

        PeriodNavigator(
            dateRangeLabel = dateRangeLabel,
            onPreviousPeriod = viewModel::showPreviousPeriod,
            onNextPeriod = viewModel::showNextPeriod,
            enabled = selectedPeriod != TasksPeriod.ALL_TIME,
            modifier = Modifier.fillMaxWidth(),
        )

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                OverviewSection(summary = summary, modifier = Modifier.fillMaxWidth())

                TaskStatusSection(
                    taskStatus = taskStatus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                )

                Text(
                    text = "On Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp),
                )

                CommonChart(
                    points = onTimeRateChart,
                    series = listOf(ChartSeries(label = "On time", color = MaterialTheme.colorScheme.primary)),
                    maxValue = ON_TIME_CHART_MAX_VALUE,
                    yAxisMarks = ON_TIME_CHART_Y_AXIS_MARKS,
                    yAxisValueLabel = { "$it%" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ItemBackground)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(
    summary: TasksSummary,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverviewField(label = "Assigned", value = "${summary.available}", modifier = Modifier.weight(1f))
            OverviewDivider()
            OverviewField(label = "Completed", value = "${summary.completed}", modifier = Modifier.weight(1f))
            OverviewDivider()
            OverviewField(
                label = "On time rate",
                value = "${summary.onTimeRatePercent}%",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TaskStatusSection(
    taskStatus: TaskStatusBreakdown,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Task status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverviewField(label = "Completed", value = "${taskStatus.completed}", modifier = Modifier.weight(1f))
            OverviewDivider()
            OverviewField(label = "Missed", value = "${taskStatus.missed}", modifier = Modifier.weight(1f))
            OverviewDivider()
            OverviewField(label = "Pending", value = "${taskStatus.pending}", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun OverviewDivider() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight(),
        color = ItemSplitter,
    )
}

@Composable
private fun TasksPeriodTabs(
    selectedPeriod: TasksPeriod,
    onPeriodSelected: (TasksPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TasksPeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                label = "periodTabBackground",
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "periodTabTextColor",
            )
            Text(
                text = period.label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .padding(vertical = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onPeriodSelected(period) },
                    )
            )
        }
    }
}
