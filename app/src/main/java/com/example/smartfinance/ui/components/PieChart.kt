package com.example.smartfinance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val pieColors = listOf(
    Color(0xFFFF6384),
    Color(0xFF36A2EB),
    Color(0xFFFFCE56),
    Color(0xFF4BC0C0),
    Color(0xFF9966FF),
    Color(0xFFFF9F40),
    Color(0xFFC9CBCF),
)

data class PieSlice(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size
        val diameter = canvasSize.width.coerceAtMost(canvasSize.height)
        val topLeft = Offset(
            (canvasSize.width - diameter) / 2f,
            (canvasSize.height - diameter) / 2f
        )
        val arcSize = Size(diameter, diameter)
        var startAngle = -90f

        slices.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = topLeft,
                size = arcSize,
                style = Fill
            )
            startAngle += sweepAngle
        }
    }
}
