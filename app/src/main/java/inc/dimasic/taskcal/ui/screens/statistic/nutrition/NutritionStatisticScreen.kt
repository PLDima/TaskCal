package inc.dimasic.taskcal.ui.screens.statistic.nutrition

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import inc.dimasic.taskcal.model.MacroType
import inc.dimasic.taskcal.model.NutritionPeriod
import inc.dimasic.taskcal.ui.screens.uiViews.ChartPoint
import inc.dimasic.taskcal.ui.screens.uiViews.ChartSeries
import inc.dimasic.taskcal.ui.screens.uiViews.CommonChart
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import inc.dimasic.taskcal.ui.theme.MacroCarbsColor
import inc.dimasic.taskcal.ui.theme.MacroFatColor
import inc.dimasic.taskcal.ui.theme.MacroProteinColor
import inc.dimasic.taskcal.ui.theme.StatusDoneGreen
import inc.dimasic.taskcal.usecase.PeriodCaloriesAverages

private const val BALANCE_TINT_ALPHA = 0.12f
private const val CHART_MAX_CALORIES = 4000
private val CHART_Y_AXIS_MARKS = listOf(1000, 2000, 3000, 4000)

private fun MacroType.color(): Color = when (this) {
    MacroType.PROTEIN -> MacroProteinColor
    MacroType.FAT -> MacroFatColor
    MacroType.CARBS -> MacroCarbsColor
}

private fun MacroType.yAxisMarks(): List<Int> {
    val step = chartMaxValue / 4
    return listOf(step, step * 2, step * 3, chartMaxValue)
}

@Composable
fun NutritionStatisticScreen(
    modifier: Modifier = Modifier,
    viewModel: NutritionStatisticViewModel = hiltViewModel(),
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val dateRangeLabel by viewModel.dateRangeLabel.collectAsState()
    val averages by viewModel.periodCaloriesAverages.collectAsState()
    val caloriesChart by viewModel.caloriesChart.collectAsState()
    val summary by viewModel.caloriesSummary.collectAsState()
    val expandedMacro by viewModel.expandedMacro.collectAsState()
    val macroChart by viewModel.macroChart.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        PeriodTabs(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = viewModel::selectPeriod,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::showPreviousPeriod) {
                Icon(imageVector = Icons.Filled.ChevronLeft, contentDescription = "Previous period")
            }
            Text(
                text = dateRangeLabel,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::showNextPeriod) {
                Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Next period")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            AverageCaloriesRow(averages = averages, modifier = Modifier.fillMaxWidth())

            NutritionSummarySection(
                summary = summary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            Text(
                text = "Calories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )

            CommonChart(
                points = caloriesChart.map { ChartPoint(label = it.label, values = listOf(it.consumed, it.burned)) },
                series = listOf(
                    ChartSeries(label = "Consumed", color = MaterialTheme.colorScheme.primary),
                    ChartSeries(label = "Burned", color = StatusDoneGreen),
                ),
                maxValue = CHART_MAX_CALORIES,
                yAxisMarks = CHART_Y_AXIS_MARKS,
                yAxisValueLabel = { "${it / 1000}k" },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ItemBackground)
                    .padding(16.dp),
            )

            Text(
                text = "Macros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp),
            )

            MacroButtonsRow(
                expandedMacro = expandedMacro,
                onMacroSelected = viewModel::toggleMacro,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            AnimatedVisibility(
                visible = expandedMacro != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                val macro = expandedMacro
                if (macro != null) {
                    CommonChart(
                        points = macroChart.map { ChartPoint(label = it.label, values = listOf(it.value)) },
                        series = listOf(ChartSeries(label = macro.label, color = macro.color())),
                        maxValue = macro.chartMaxValue,
                        yAxisMarks = macro.yAxisMarks(),
                        yAxisValueLabel = { "$it${macro.unit}" },
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
}

@Composable
private fun MacroButtonsRow(
    expandedMacro: MacroType?,
    onMacroSelected: (MacroType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MacroType.entries.forEach { macro ->
            MacroButton(
                macro = macro,
                isExpanded = macro == expandedMacro,
                onClick = { onMacroSelected(macro) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MacroButton(
    macro: MacroType,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isExpanded) macro.color().copy(alpha = 0.18f) else ItemBackground,
        label = "macroButtonBackground",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) macro.color() else Color.Transparent,
        label = "macroButtonBorder",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = macro.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NutritionSummarySection(
    summary: NutritionSummaryUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground)
            .padding(horizontal = 16.dp),
    ) {
        SummaryRow(label = "Total consumed", value = "${summary.totalConsumed} kcal")
        SummaryDivider()
        SummaryRow(label = "Total burned", value = "${summary.totalBurned} kcal")
        SummaryDivider()
        SummaryRow(
            label = "Average balance",
            value = "${summary.avgBalance} kcal",
            valueColor = balanceColor(summary.avgBalance),
        )
        SummaryDivider()
        SummaryRow(label = "Best day", value = summary.bestDayLabel)
        SummaryDivider()
        SummaryRow(label = "Worst day", value = summary.worstDayLabel)
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}

@Composable
private fun SummaryDivider() {
    HorizontalDivider(color = ItemSplitter)
}

@Composable
private fun AverageCaloriesRow(
    averages: PeriodCaloriesAverages,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AverageField(label = "Avg. consumed", value = averages.avgConsumed, modifier = Modifier.weight(1f))
        AverageDivider()
        AverageField(label = "Avg. burned", value = averages.avgBurned, modifier = Modifier.weight(1f))
        AverageDivider()
        AverageField(
            label = "Avg. balance",
            value = averages.avgBalance,
            valueColor = balanceColor(averages.avgBalance),
            background = balanceBackground(averages.avgBalance),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun balanceColor(balance: Int): Color = when {
    balance < 0 -> StatusDoneGreen
    balance > 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
private fun balanceBackground(balance: Int): Color = when {
    balance < 0 -> StatusDoneGreen.copy(alpha = BALANCE_TINT_ALPHA)
    balance > 0 -> MaterialTheme.colorScheme.error.copy(alpha = BALANCE_TINT_ALPHA)
    else -> Color.Transparent
}

@Composable
private fun AverageDivider() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight(),
        color = ItemSplitter,
    )
}

@Composable
private fun AverageField(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    background: Color = Color.Transparent,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(background)
            .padding(horizontal = 8.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
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
private fun PeriodTabs(
    selectedPeriod: NutritionPeriod,
    onPeriodSelected: (NutritionPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NutritionPeriod.entries.forEach { period ->
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
