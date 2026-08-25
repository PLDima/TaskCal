package inc.dimasic.taskcal.ui.screens.statistic

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class StatisticTab(val label: String) {
    NUTRITION("Nutrition"),
    TASKS("Tasks"),
    ACTIVITIES("Activities"),
}

@Composable
fun StatisticScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(StatisticTab.NUTRITION) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        StatisticTabs(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth(),
        )

        StatisticTabContent(
            tab = selectedTab,
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

@Composable
private fun StatisticTabContent(tab: StatisticTab, modifier: Modifier = Modifier) {
    when (tab) {
        StatisticTab.NUTRITION -> NutritionStatisticScreen(modifier = modifier)
        StatisticTab.TASKS -> TasksStatisticScreen(modifier = modifier)
        StatisticTab.ACTIVITIES -> ActivitiesStatisticScreen(modifier = modifier)
    }
}

@Composable
private fun StatisticTabs(
    selectedTab: StatisticTab,
    onTabSelected: (StatisticTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StatisticTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                label = "tabBackground",
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "tabTextColor",
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onTabSelected(tab) },
                    )
                    .padding(vertical = 8.dp),
            )
        }
    }
}
