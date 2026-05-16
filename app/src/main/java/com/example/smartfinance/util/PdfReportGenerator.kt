package com.example.smartfinance.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

data class ReportData(
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val healthPercentage: Float,
    val categories: List<Pair<String, Double>>,
    val totalExpenses: Double,
    val period: String
)

object PdfReportGenerator {

    fun generateReport(context: Context, data: ReportData): Uri {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            color = Color.rgb(3, 218, 197)
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(187, 134, 252)
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
        }
        val valuePaint = Paint().apply {
            color = Color.rgb(76, 175, 80)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        var y = 50f

        // Title
        canvas.drawText("SmartFinance", 40f, y, titlePaint)
        y += 40f
        canvas.drawText("Monthly Report - ${data.period}", 40f, y, headerPaint)
        y += 50f

        // Summary
        canvas.drawText("Summary", 40f, y, headerPaint)
        y += 30f
        val expensePaint = Paint().apply {
            color = Color.rgb(255, 183, 77)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Total Income:  $$${String.format("%.2f", data.monthlyIncome)}", 60f, y, valuePaint)
        y += 28f
        canvas.drawText("Total Expenses:  $$${String.format("%.2f", data.monthlyExpenses)}", 60f, y, expensePaint)
        y += 28f

        val healthColor = when {
            data.healthPercentage <= 50f -> Color.rgb(76, 175, 80)
            data.healthPercentage <= 85f -> Color.rgb(255, 193, 7)
            else -> Color.rgb(244, 67, 54)
        }
        val healthPaint = Paint().apply {
            color = healthColor
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Financial Health: ${data.healthPercentage.toInt()}%", 60f, y, healthPaint)
        y += 50f

        // Pie chart
        if (data.categories.isNotEmpty()) {
            canvas.drawText("Expenses by Category", 40f, y, headerPaint)
            y += 30f

            val chartColors = listOf(
                Color.rgb(255, 99, 132), Color.rgb(54, 162, 235),
                Color.rgb(255, 206, 86), Color.rgb(75, 192, 192),
                Color.rgb(153, 102, 255), Color.rgb(255, 159, 64)
            )
            val total = data.totalExpenses.toFloat()
            val cx = 200f
            val cy = y + 80f
            val radius = 80f
            var startAngle = -90f

            data.categories.forEachIndexed { idx, (cat, amount) ->
                val sweep = (amount.toFloat() / total) * 360f
                val arcPaint = Paint().apply {
                    color = chartColors[idx % chartColors.size]
                    style = Paint.Style.FILL
                }
                canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                    startAngle, sweep, true, arcPaint)
                startAngle += sweep
            }

            y = cy + radius + 30f

            // Legend
            data.categories.forEachIndexed { idx, (cat, amount) ->
                val legendPaint = Paint().apply {
                    color = chartColors[idx % chartColors.size]
                }
                canvas.drawRect(60f, y, 80f, y + 12f, legendPaint)
                val textPaint = Paint().apply {
                    color = Color.BLACK
                    textSize = 14f
                }
                val pct = (amount / data.monthlyExpenses * 100)
                canvas.drawText("$cat: ${String.format("%.1f", pct)}% ($${String.format("%.2f", amount)})",
                    90f, y + 12f, textPaint)
                y += 22f
            }
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val file = File(dir, "SmartFinance_Report_${data.period.replace("/", "_")}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
