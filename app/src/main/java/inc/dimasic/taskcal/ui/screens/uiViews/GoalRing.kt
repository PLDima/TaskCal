package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val RING_SIZE = 104.dp
private val RING_STROKE = 9.dp

/** A value against its goal as a ring: figure and goal inside, caption and status beneath. */
@Composable
fun GoalRing(
    progress: GoalProgressUi,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(RING_SIZE),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(RING_SIZE)) {
                val strokeWidth = RING_STROKE.toPx()
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
                    sweepAngle = 360f * progress.fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${progress.value}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Text(
                    text = "/${progress.goal}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                )
                Text(
                    text = "${progress.percent}%",
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
            text = progress.statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (progress.isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
