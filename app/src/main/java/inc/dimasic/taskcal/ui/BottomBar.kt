package inc.dimasic.taskcal.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.FloatingActionButton
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
    ADD("Add"),
    FOOD("Food"),
    STATISTIC("Statistic"),
}

private const val BAR_HEIGHT_DP = 72
private const val FAB_SIZE_DP = 56
private const val FAB_RAISE_DP = 20

@Composable
fun BottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
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
                Box(modifier = Modifier.weight(1f))
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

        FloatingActionButton(
            onClick = { onTabSelected(BottomTab.ADD) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-FAB_RAISE_DP).dp)
                .size(FAB_SIZE_DP.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = BottomTab.ADD.label)
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
