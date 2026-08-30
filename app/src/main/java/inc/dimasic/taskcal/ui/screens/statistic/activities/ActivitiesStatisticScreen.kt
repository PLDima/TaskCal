package inc.dimasic.taskcal.ui.screens.statistic.activities

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import inc.dimasic.taskcal.model.ActivitiesPeriod
import inc.dimasic.taskcal.repository.ActivityTotals
import inc.dimasic.taskcal.ui.screens.statistic.statistic_views.PeriodNavigator
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import kotlin.math.roundToInt

@Composable
fun ActivitiesStatisticScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesStatisticViewModel = hiltViewModel(),
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val dateRangeLabel by viewModel.dateRangeLabel.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val characteristics by viewModel.characteristics.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        ActivitiesPeriodTabs(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = viewModel::selectPeriod,
            modifier = Modifier.fillMaxWidth(),
        )

        PeriodNavigator(
            dateRangeLabel = dateRangeLabel,
            onPreviousPeriod = viewModel::showPreviousPeriod,
            onNextPeriod = viewModel::showNextPeriod,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            ActivityRingsRow(
                totals = totals,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            CharacteristicsSection(
                characteristics = characteristics,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun CharacteristicsSection(
    characteristics: List<ActivityCharacteristic>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground)
            .padding(horizontal = 16.dp),
    ) {
        characteristics.forEachIndexed { index, characteristic ->
            CharacteristicRow(
                characteristic = characteristic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
            )
            if (index != characteristics.lastIndex) {
                HorizontalDivider(color = ItemSplitter)
            }
        }
    }
}

@Composable
private fun CharacteristicRow(
    characteristic: ActivityCharacteristic,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = characteristic.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = characteristic.valueLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            characteristic.goalPercent?.let { percent ->
                Text(
                    text = "$percent% of daily goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        characteristic.fraction?.let { fraction ->
            CharacteristicProgressBar(
                fraction = fraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun CharacteristicProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun ActivityRingsRow(
    totals: ActivityTotals,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActivityRing(
            value = totals.caloriesBurned,
            goal = totals.caloriesGoal,
            caption = "Calories burned",
        )
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            color = ItemSplitter,
        )
        ActivityRing(
            value = totals.steps,
            goal = totals.stepsGoal,
            caption = "Steps",
        )
    }
}

@Composable
private fun ActivityRing(
    value: Int,
    goal: Int,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val fraction = if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val remaining = (goal - value).coerceAtLeast(0)
    val over = (value - goal).coerceAtLeast(0)
    val percent = (fraction * 100).roundToInt()
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val ringSize = 104.dp

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(ringSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(ringSize)) {
                val strokeWidth = 9.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Text(
                    text = "/$goal",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = if (value > goal) "$over over" else "$remaining left",
            style = MaterialTheme.typography.labelSmall,
            color = if (value > goal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActivitiesPeriodTabs(
    selectedPeriod: ActivitiesPeriod,
    onPeriodSelected: (ActivitiesPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ActivitiesPeriod.entries.forEach { period ->
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
