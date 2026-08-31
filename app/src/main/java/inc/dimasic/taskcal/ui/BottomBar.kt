package inc.dimasic.taskcal.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class BottomTab(val label: String) {
    TODAY("Today"),
    TASKS("Tasks"),
    FOOD("Food"),
    STATISTIC("Statistic"),
}

private const val BAR_HEIGHT_DP = 72

@Composable
fun BottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 3.dp, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT_DP.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BottomBarItem(
                tab = BottomTab.TODAY,
                icon = Icons.Filled.CalendarToday,
                selected = selectedTab == BottomTab.TODAY,
                onClick = { onTabSelected(BottomTab.TODAY) },
            )
            BottomBarItem(
                tab = BottomTab.TASKS,
                icon = Icons.Filled.Checklist,
                selected = selectedTab == BottomTab.TASKS,
                onClick = { onTabSelected(BottomTab.TASKS) },
            )
            BottomBarItem(
                tab = BottomTab.FOOD,
                icon = Icons.Filled.Restaurant,
                selected = selectedTab == BottomTab.FOOD,
                onClick = { onTabSelected(BottomTab.FOOD) },
            )
            BottomBarItem(
                tab = BottomTab.STATISTIC,
                icon = Icons.Filled.BarChart,
                selected = selectedTab == BottomTab.STATISTIC,
                onClick = { onTabSelected(BottomTab.STATISTIC) },
            )
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    tab: BottomTab,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = tab.label, tint = color)
        Text(text = tab.label, color = color, style = MaterialTheme.typography.labelSmall)
    }
}
