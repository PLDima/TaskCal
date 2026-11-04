package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import inc.dimasic.taskcal.ui.theme.ItemBackground
import inc.dimasic.taskcal.ui.theme.ItemSplitter

@Composable
fun CaloriesSection(
    calories: CaloriesSectionUi,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "Calories", style = MaterialTheme.typography.titleMedium)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ItemBackground)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoalRing(progress = calories.goal, caption = "Goal")
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    color = ItemSplitter
                )
                GoalRing(progress = calories.activity, caption = "Activity result")
            }

            MacroBreakdownRow(
                macros = calories.macros,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
