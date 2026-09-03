package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inc.dimasic.taskcal.domain.usecase.goalFraction
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import kotlin.math.roundToInt

@Composable
fun MacroBreakdownRow(
    proteinUsed: Double,
    proteinGoal: Double,
    fatUsed: Double,
    fatGoal: Double,
    carbsUsed: Double,
    carbsGoal: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MacroRow(
            label = "Protein",
            used = proteinUsed,
            goal = proteinGoal,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            color = ItemSplitter,
        )
        MacroRow(
            label = "Fat",
            used = fatUsed,
            goal = fatGoal,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            color = ItemSplitter,
        )
        MacroRow(
            label = "Carbs",
            used = carbsUsed,
            goal = carbsGoal,
            modifier = Modifier.weight(1f),
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
    val remaining = goal.coerceAtLeast(0.0)
    val fraction = goalFraction(used, goal)
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
