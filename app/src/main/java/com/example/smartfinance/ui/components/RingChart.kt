package com.example.smartfinance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RingChart(
    percentage: Float,
    activeColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    trackColor: Color = Color.Gray.copy(alpha = 0.2f),
    strokeWidth: Dp = 14.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val stroke = strokeWidth.toPx()
        val arcSize = Size(canvasSize - stroke, canvasSize - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = (percentage / 100f) * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
