package inc.dimasic.taskcal.ui.screens.uiViews

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inc.dimasic.taskcal.domain.usecase.model.ChartPoint
import inc.dimasic.taskcal.ui.theme.ItemSplitter
@Composable
fun CommonChart(
    points: List<ChartPoint>,
    series: List<ChartSeries>,
    maxValue: Int,
    yAxisMarks: List<Int>,
    modifier: Modifier = Modifier,
    yAxisValueLabel: (Int) -> String = { it.toString() },
) {
    val density = LocalDensity.current
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridLineColor = ItemSplitter

    val yAxisPaint = remember(axisLabelColor) {
        android.graphics.Paint().apply {
            color = axisLabelColor.toArgb()
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
        }
    }
    val xAxisPaint = remember(axisLabelColor) {
        android.graphics.Paint().apply {
            color = axisLabelColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Column(modifier = modifier) {
        ChartLegend(series = series)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(220.dp),
        ) {
            val axisTextSizePx = with(density) { 11.sp.toPx() }
            yAxisPaint.textSize = axisTextSizePx
            xAxisPaint.textSize = axisTextSizePx

            val leftAxisWidth = with(density) { 28.dp.toPx() }
            val bottomAxisHeight = with(density) { 20.dp.toPx() }
            val chartLeft = leftAxisWidth
            val chartWidth = size.width - leftAxisWidth
            val chartHeight = size.height - bottomAxisHeight

            yAxisMarks.forEach { mark ->
                val y = chartHeight - (chartHeight * mark / maxValue.toFloat())
                drawLine(
                    color = gridLineColor,
                    start = Offset(chartLeft, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
                drawContext.canvas.nativeCanvas.drawText(
                    yAxisValueLabel(mark),
                    0f,
                    y + axisTextSizePx / 3f,
                    yAxisPaint,
                )
            }

            if (points.isNotEmpty() && series.isNotEmpty()) {
                val slotWidth = chartWidth / points.size
                val barAreaWidth = slotWidth * 0.7f
                val barSpacing = barAreaWidth * 0.08f
                val barWidth = (barAreaWidth - barSpacing * (series.size - 1)) / series.size

                points.forEachIndexed { index, point ->
                    val slotCenterX = chartLeft + slotWidth * index + slotWidth / 2f
                    val groupLeft = slotCenterX - barAreaWidth / 2f

                    series.indices.forEach { seriesIndex ->
                        val value = point.values.getOrElse(seriesIndex) { 0 }
                        val fraction = (value / maxValue.toFloat()).coerceIn(0f, 1f)
                        val barHeight = chartHeight * fraction
                        val left = groupLeft + seriesIndex * (barWidth + barSpacing)
                        if (barHeight > 0f) {
                            drawRoundRect(
                                color = series[seriesIndex].color,
                                topLeft = Offset(left, chartHeight - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            )
                        }
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        point.label,
                        slotCenterX,
                        size.height - (bottomAxisHeight - axisTextSizePx) / 2f,
                        xAxisPaint,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegend(series: List<ChartSeries>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        series.forEach { LegendItem(color = it.color, label = it.label) }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}
