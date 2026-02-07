package com.matedroid.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

private const val MAX_DISPLAY_POINTS = 150

/**
 * A dual-axis line chart showing two data series with independent Y axes.
 * Left Y axis shows the first series (e.g., Voltage), right Y axis shows the second (e.g., Current).
 */
@Composable
fun DualAxisLineChart(
    dataLeft: List<Float>,
    dataRight: List<Float>,
    modifier: Modifier = Modifier,
    colorLeft: Color = MaterialTheme.colorScheme.tertiary,
    colorRight: Color = MaterialTheme.colorScheme.secondary,
    unitLeft: String = "V",
    unitRight: String = "A",
    timeLabels: List<String> = emptyList(),
    chartHeight: Dp = 120.dp
) {
    if (dataLeft.size < 2 && dataRight.size < 2) return

    val surfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val chartDataLeft = remember(dataLeft) { prepareDualChartData(dataLeft) }
    val chartDataRight = remember(dataRight) { prepareDualChartData(dataRight) }

    var selectedPoint by remember { mutableStateOf<DualSelectedPoint?>(null) }

    val timeLabelHeightDp = if (timeLabels.isNotEmpty()) 20.dp else 0.dp
    val totalHeightDp = chartHeight + timeLabelHeightDp

    // Use the larger dataset size for X positioning
    val dataSize = maxOf(dataLeft.size, dataRight.size)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeightDp)
                .pointerInput(chartDataLeft, chartDataRight) {
                    detectTapGestures { offset ->
                        val width = size.width.toFloat()
                        val chartHeightPx = chartHeight.toPx()

                        if (offset.y > chartHeightPx) {
                            selectedPoint = null
                            return@detectTapGestures
                        }

                        val stepX = width / (dataSize - 1).coerceAtLeast(1)
                        val tappedIndex = ((offset.x / stepX).roundToInt()).coerceIn(0, dataSize - 1)

                        val leftVal = chartDataLeft.displayPoints.getOrNull(tappedIndex)
                        val rightVal = chartDataRight.displayPoints.getOrNull(tappedIndex)

                        val pointX = tappedIndex * stepX

                        selectedPoint = if (selectedPoint?.index == tappedIndex) {
                            null
                        } else {
                            DualSelectedPoint(
                                index = tappedIndex,
                                valueLeft = leftVal,
                                valueRight = rightVal,
                                position = Offset(pointX, offset.y)
                            )
                        }
                    }
                }
        ) {
            val width = size.width
            val chartHeightPx = chartHeight.toPx()
            val timeLabelHeightPx = timeLabelHeightDp.toPx()
            val rightLabelWidth = 70f // Reserve space for right Y labels

            // Draw grid lines
            val gridLineCount = 4
            for (i in 0..gridLineCount) {
                val y = chartHeightPx * i / gridLineCount
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Draw left series path
            if (chartDataLeft.displayPoints.size >= 2) {
                drawPath(
                    path = createLinePath(chartDataLeft, width - rightLabelWidth, chartHeightPx),
                    color = colorLeft,
                    style = Stroke(width = 2.5f)
                )
            }

            // Draw right series path
            if (chartDataRight.displayPoints.size >= 2) {
                drawPath(
                    path = createLinePath(chartDataRight, width - rightLabelWidth, chartHeightPx),
                    color = colorRight,
                    style = Stroke(width = 2.5f)
                )
            }

            // Draw left Y-axis labels (4 labels)
            drawDualYAxisLabels(
                surfaceColor = surfaceColor,
                chartData = chartDataLeft,
                unit = unitLeft,
                height = chartHeightPx,
                isLeft = true,
                color = colorLeft
            )

            // Draw right Y-axis labels (4 labels)
            drawDualYAxisLabels(
                surfaceColor = surfaceColor,
                chartData = chartDataRight,
                unit = unitRight,
                height = chartHeightPx,
                isLeft = false,
                color = colorRight,
                width = width
            )

            // Draw time labels
            if (timeLabels.size == 5) {
                drawDualTimeLabels(surfaceColor, timeLabels, width - rightLabelWidth, chartHeightPx, timeLabelHeightPx)
            }

            // Draw selected point indicators
            selectedPoint?.let { point ->
                val stepX = (width - rightLabelWidth) / (dataSize - 1).coerceAtLeast(1)
                val pointX = point.index * stepX

                point.valueLeft?.let { v ->
                    val y = chartHeightPx * (1 - (v - chartDataLeft.minValue) / chartDataLeft.range)
                    drawCircle(color = colorLeft, radius = 6.dp.toPx(), center = Offset(pointX, y))
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(pointX, y))
                }
                point.valueRight?.let { v ->
                    val y = chartHeightPx * (1 - (v - chartDataRight.minValue) / chartDataRight.range)
                    drawCircle(color = colorRight, radius = 6.dp.toPx(), center = Offset(pointX, y))
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(pointX, y))
                }
            }
        }

        // Tooltip popup - dismisses on tap outside
        selectedPoint?.let { point ->
            val density = LocalDensity.current
            Popup(
                offset = with(density) {
                    IntOffset(
                        x = point.position.x.roundToInt(),
                        y = (point.position.y - 48.dp.toPx()).roundToInt()
                    )
                },
                onDismissRequest = { selectedPoint = null },
                properties = PopupProperties(focusable = true)
            ) {
                val lines = mutableListOf<String>()
                if (point.valueLeft != null) lines.add("%.1f".format(point.valueLeft) + " $unitLeft")
                if (point.valueRight != null) lines.add("%.1f".format(point.valueRight) + " $unitRight")
                val text = lines.joinToString("  |  ")

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            Color(0xCC323232),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private data class DualChartData(
    val displayPoints: List<Float>,
    val minValue: Float,
    val maxValue: Float,
    val range: Float
)

private data class DualSelectedPoint(
    val index: Int,
    val valueLeft: Float?,
    val valueRight: Float?,
    val position: Offset
)

private fun prepareDualChartData(data: List<Float>): DualChartData {
    val displayPoints = if (data.size > MAX_DISPLAY_POINTS) {
        downsampleLTTBDual(data, MAX_DISPLAY_POINTS)
    } else {
        data
    }

    val minValue = displayPoints.minOrNull() ?: 0f
    val maxValue = displayPoints.maxOrNull() ?: 1f
    val range = (maxValue - minValue).coerceAtLeast(1f)

    return DualChartData(displayPoints, minValue, maxValue, range)
}

private fun downsampleLTTBDual(data: List<Float>, targetPoints: Int): List<Float> {
    if (data.size <= targetPoints) return data
    if (targetPoints < 3) return listOf(data.first(), data.last())

    val result = mutableListOf<Float>()
    result.add(data.first())

    val bucketSize = (data.size - 2).toFloat() / (targetPoints - 2)
    var prevSelectedIndex = 0

    for (i in 0 until targetPoints - 2) {
        val bucketStart = ((i * bucketSize) + 1).toInt()
        val bucketEnd = (((i + 1) * bucketSize) + 1).toInt().coerceAtMost(data.size - 1)

        val nextBucketStart = bucketEnd
        val nextBucketEnd = (((i + 2) * bucketSize) + 1).toInt().coerceAtMost(data.size)

        var avgX = 0f
        var avgY = 0f
        var count = 0
        for (j in nextBucketStart until nextBucketEnd) {
            avgX += j.toFloat()
            avgY += data[j]
            count++
        }
        if (count > 0) { avgX /= count; avgY /= count }
        else { avgX = nextBucketStart.toFloat(); avgY = data.getOrElse(nextBucketStart) { data.last() } }

        val prevX = prevSelectedIndex.toFloat()
        val prevY = data[prevSelectedIndex]
        var maxArea = -1f
        var selectedIndex = bucketStart

        for (j in bucketStart until bucketEnd) {
            val area = abs(
                (prevX - avgX) * (data[j] - prevY) -
                (prevX - j.toFloat()) * (avgY - prevY)
            ) * 0.5f
            if (area > maxArea) { maxArea = area; selectedIndex = j }
        }

        result.add(data[selectedIndex])
        prevSelectedIndex = selectedIndex
    }

    result.add(data.last())
    return result
}

private fun createLinePath(chartData: DualChartData, width: Float, height: Float): Path {
    val path = Path()
    val points = chartData.displayPoints
    if (points.size < 2) return path

    val stepX = width / (points.size - 1).coerceAtLeast(1)

    points.forEachIndexed { index, value ->
        val x = index * stepX
        val y = height * (1 - (value - chartData.minValue) / chartData.range)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    return path
}

private fun DrawScope.drawDualYAxisLabels(
    surfaceColor: Color,
    chartData: DualChartData,
    unit: String,
    height: Float,
    isLeft: Boolean,
    color: Color,
    width: Float = 0f
) {
    drawContext.canvas.nativeCanvas.apply {
        val textPaint = Paint().apply {
            this.color = color.copy(alpha = 0.8f).toArgb()
            textSize = 24f
            isAntiAlias = true
            textAlign = if (isLeft) Paint.Align.LEFT else Paint.Align.RIGHT
        }

        val labelPositions = listOf(1, 2, 3, 4)
        val gridLineCount = 4

        for (i in labelPositions) {
            val y = height * i / gridLineCount
            val value = chartData.maxValue - (chartData.range * i / gridLineCount)
            val label = "%.0f".format(value) + " $unit"

            val textY = when (i) {
                gridLineCount -> y - 4f
                else -> y + textPaint.textSize / 3
            }

            val x = if (isLeft) 8f else width - 8f
            drawText(label, x, textY, textPaint)
        }
    }
}

private fun DrawScope.drawDualTimeLabels(
    surfaceColor: Color,
    timeLabels: List<String>,
    chartWidth: Float,
    chartHeight: Float,
    timeLabelHeight: Float
) {
    drawContext.canvas.nativeCanvas.apply {
        val textPaint = Paint().apply {
            color = surfaceColor.copy(alpha = 0.7f).toArgb()
            textSize = 26f
            isAntiAlias = true
        }

        val timeY = chartHeight + timeLabelHeight - 4f
        val positions = listOf(0f, chartWidth * 0.25f, chartWidth * 0.5f, chartWidth * 0.75f, chartWidth)

        timeLabels.forEachIndexed { index, label ->
            if (label.isNotEmpty()) {
                val textWidth = textPaint.measureText(label)
                val x = when (index) {
                    0 -> 0f
                    4 -> positions[index] - textWidth
                    else -> positions[index] - textWidth / 2
                }
                drawText(label, x.coerceAtLeast(0f), timeY, textPaint)
            }
        }
    }
}

