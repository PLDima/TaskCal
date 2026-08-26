package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter
import inc.dimasic.taskcal.ui.theme.StatusDoneGreen

private const val LEFT_TINT_ALPHA = 0.12f

@Composable
fun CaloriesTotalsRow(
    used: Int,
    goal: Int,
    modifier: Modifier = Modifier,
) {
    val left = goal - used

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .background(ItemBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TotalsField(label = "Used", value = used, modifier = Modifier.weight(1f))
        TotalsDivider()
        TotalsField(label = "Goal", value = goal, modifier = Modifier.weight(1f))
        TotalsDivider()
        TotalsField(
            label = "Left",
            value = left,
            valueColor = leftValueColor(left),
            background = leftBackgroundColor(left),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun leftValueColor(left: Int): Color = when {
    left > 0 -> StatusDoneGreen
    left < 0 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

@Composable
private fun leftBackgroundColor(left: Int): Color = when {
    left > 0 -> StatusDoneGreen.copy(alpha = LEFT_TINT_ALPHA)
    left < 0 -> MaterialTheme.colorScheme.error.copy(alpha = LEFT_TINT_ALPHA)
    else -> Color.Transparent
}

@Composable
private fun TotalsDivider() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight(),
        color = ItemSplitter,
    )
}

@Composable
private fun TotalsField(
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
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
