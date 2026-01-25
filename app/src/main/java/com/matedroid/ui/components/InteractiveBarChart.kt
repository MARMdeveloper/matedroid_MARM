package com.matedroid.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarChartData(
    val label: String,
    val value: Double,
    val displayValue: String = value.toString(),
    val color: Color? = null // for C bar color
)

@Composable
fun InteractiveBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showEveryNthLabel: Int = 1,
    valueFormatter: (Double) -> String = { "%.0f".format(it) },
    yAxisFormatter: (Double) -> String = { if (it >= 1000) "%.0fk".format(it / 1000) else "%.0f".format(it) }
) {
    if (data.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOfOrNull { it.value } ?: 1.0
    val density = LocalDensity.current

    // Reset selection when data changes to avoid IndexOutOfBoundsException
    var selectedBarIndex by remember(data) { mutableStateOf<Int?>(null) }
    var tooltipPosition by remember(data) { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val yAxisWidth = with(density) { 32.dp.toPx() }
                        val chartWidth = size.width - yAxisWidth
                        val barWidth = chartWidth / data.size

                        // Check if tap is in chart area
                        if (offset.x > yAxisWidth) {
                            val barIndex = ((offset.x - yAxisWidth) / barWidth).toInt()
                            if (barIndex in data.indices) {
                                if (selectedBarIndex == barIndex) {
                                    selectedBarIndex = null
                                } else {
                                    selectedBarIndex = barIndex
                                    tooltipPosition = Offset(
                                        yAxisWidth + barIndex * barWidth + barWidth / 2,
                                        offset.y
                                    )
                                }
                            }
                        } else {
                            selectedBarIndex = null
                        }
                    }
                }
        ) {
            val yAxisWidth = 32.dp.toPx()
            val chartWidth = size.width - yAxisWidth
            val barWidth = chartWidth / data.size
            val maxBarHeight = size.height - 20.dp.toPx()

            // Draw Y-axis labels
            drawYAxisLabel(
                textMeasurer = textMeasurer,
                text = yAxisFormatter(maxValue),
                x = yAxisWidth - 4.dp.toPx(),
                y = 0f,
                color = labelColor,
                alignTop = true
            )

            drawYAxisLabel(
                textMeasurer = textMeasurer,
                text = "0",
                x = yAxisWidth - 4.dp.toPx(),
                y = maxBarHeight,
                color = labelColor,
                alignTop = false
            )

            // Draw bars
            data.forEachIndexed { index, barData ->
                val barHeight = if (maxValue > 0) {
                    (barData.value / maxValue * maxBarHeight).toFloat()
                } else {
                    0f
                }

                val isSelected = index == selectedBarIndex
                val baseBarColor = barData.color ?: barColor
                val currentBarColor = if (isSelected) baseBarColor.copy(alpha = 0.7f) else baseBarColor

                if (barHeight > 0) {
                    drawRect(
                        color = currentBarColor,
                        topLeft = Offset(
                            x = yAxisWidth + index * barWidth + barWidth * 0.15f,
                            y = maxBarHeight - barHeight
                        ),
                        size = Size(
                            width = barWidth * 0.7f,
                            height = barHeight
                        )
                    )
                }

                // Draw X-axis label
                if (data.size <= 6 || index % showEveryNthLabel == 0) {
                    drawXAxisLabel(
                        textMeasurer = textMeasurer,
                        text = barData.label,
                        x = yAxisWidth + index * barWidth + barWidth / 2,
                        y = size.height - 2.dp.toPx(),
                        color = labelColor
                    )
                }
            }
        }

        // Tooltip - with bounds check for safety
        selectedBarIndex?.takeIf { it in data.indices }?.let { index ->
            val barData = data[index]
            val yAxisWidth = with(density) { 32.dp.toPx() }
            val chartWidth = with(density) { (modifier.toString().toFloatOrNull() ?: 300f) }
            val barWidth = chartWidth / data.size

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (tooltipPosition.x - 40.dp.toPx()).toInt(),
                            y = 0
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = barData.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Text(
                        text = valueFormatter(barData.value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawYAxisLabel(
    textMeasurer: TextMeasurer,
    text: String,
    x: Float,
    y: Float,
    color: Color,
    alignTop: Boolean
) {
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = 9.sp,
            textAlign = TextAlign.End
        )
    )
    val yOffset = if (alignTop) 0f else -textLayoutResult.size.height.toFloat()
    drawText(
        textLayoutResult = textLayoutResult,
        color = color,
        topLeft = Offset(
            x = x - textLayoutResult.size.width,
            y = y + yOffset
        )
    )
}

private fun DrawScope.drawXAxisLabel(
    textMeasurer: TextMeasurer,
    text: String,
    x: Float,
    y: Float,
    color: Color
) {
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        color = color,
        topLeft = Offset(
            x = x - textLayoutResult.size.width / 2,
            y = y - textLayoutResult.size.height
        )
    )
}
