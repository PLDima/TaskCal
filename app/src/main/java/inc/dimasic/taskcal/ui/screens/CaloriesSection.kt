package inc.dimasic.taskcal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import kotlin.math.roundToInt

@Composable
fun CaloriesSection(
    calories: CaloriesSummary,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Calories", style = MaterialTheme.typography.titleMedium)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ItemBackground)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CaloriesProgress(
                    used = calories.caloriesUsed,
                    total = calories.caloriesGoal,
                    caption = "Goal",
                )
                VerticalDivider(modifier = Modifier.fillMaxHeight(), color = ItemSplitter)
                CaloriesProgress(
                    used = calories.caloriesUsed,
                    total = calories.caloriesBurned,
                    caption = "Activity result",
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MacroRow(
                    label = "Protein",
                    used = calories.proteinUsed,
                    goal = calories.proteinGoal,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                    color = ItemSplitter,
                )
                MacroRow(
                    label = "Fat",
                    used = calories.fatUsed,
                    goal = calories.fatGoal,
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                    color = ItemSplitter,
                )
                MacroRow(
                    label = "Carbs",
                    used = calories.carbsUsed,
                    goal = calories.carbsGoal,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CaloriesProgress(
    used: Int,
    total: Int,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val fraction = if (total > 0) (used.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val remaining = (total - used).coerceAtLeast(0)
    val over = (used - total).coerceAtLeast(0)
    val percent = (fraction * 100).roundToInt()
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val size = 104.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 9.dp.toPx()
                val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
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
                    text = "$used",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Text(
                    text = "/$total",
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
            text = if (used > total) "$over kcal over" else "$remaining kcal left",
            style = MaterialTheme.typography.labelSmall,
            color = if (used > total) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MacroRow(
    label: String,
    used: Double,
    goal: Double,
    modifier: Modifier = Modifier,
) {
    val remaining = (goal - used).coerceAtLeast(0.0)
    val fraction = if (goal > 0) (used / goal).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (fraction * 100).roundToInt()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Text(
            text = "${used.roundToInt()}/${remaining.roundToInt()}g",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        MacroProgressBar(fraction = fraction, modifier = Modifier.width(56.dp))
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun MacroProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

data class CaloriesSummary(
    val caloriesUsed: Int,
    val caloriesGoal: Int,
    val caloriesBurned: Int,
    val proteinUsed: Double,
    val proteinGoal: Double,
    val carbsUsed: Double,
    val carbsGoal: Double,
    val fatUsed: Double,
    val fatGoal: Double,
) {
    companion object {
        val Empty = CaloriesSummary(0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }
}
