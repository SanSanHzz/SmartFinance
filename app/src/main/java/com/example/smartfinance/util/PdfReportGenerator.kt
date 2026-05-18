package com.example.smartfinance.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.smartfinance.data.model.TransactionEntity
import com.example.smartfinance.data.model.TransactionType
import java.io.File
import java.io.FileOutputStream

data class ReportData(
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val healthPercentage: Float,
    val categories: List<Pair<String, Double>>,
    val totalExpenses: Double,
    val period: String,
    val transactions: List<TransactionEntity> = emptyList()
)

data class MascotInfo(val name: String, val message: String, val style: Int)

private const val MASCOT_CAT = 0
private const val MASCOT_BEAR = 1
private const val MASCOT_RACCOON = 2
private const val MASCOT_CHEETAH = 3
private const val MASCOT_CHICK = 4

fun getMascotForReport(topCategory: String?): MascotInfo = when {
    topCategory == null -> MascotInfo("Chick", "Start tracking your expenses!", MASCOT_CHICK)
    topCategory.contains("Food", ignoreCase = true) ->
        MascotInfo("Bear", "You're splurging on cravings this month!", MASCOT_BEAR)
    topCategory.contains("Shopping", ignoreCase = true) ->
        MascotInfo("Raccoon", "The delivery driver knows your name by heart now.", MASCOT_RACCOON)
    topCategory.contains("Transport", ignoreCase = true) ->
        MascotInfo("Cheetah", "Always on the move! Your wallet feels the speed.", MASCOT_CHEETAH)
    else -> MascotInfo("Cat", "Curious about where your money goes?", MASCOT_CAT)
}

object PdfReportGenerator {

    private val bg = Color.rgb(18, 18, 18)
    private val cardBg = Color.rgb(30, 30, 36)
    private val cardBgAlt = Color.rgb(39, 39, 42)
    private val textPrimary = Color.rgb(228, 228, 231)
    private val textMuted = Color.rgb(161, 161, 170)
    private val accentViolet = Color.rgb(157, 78, 221)
    private val accentMint = Color.rgb(82, 183, 136)
    private val accentAmber = Color.rgb(245, 158, 11)
    private val greenTint = Color.rgb(34, 197, 94)
    private val redTint = Color.rgb(239, 68, 68)
    private val separator = Color.rgb(63, 63, 70)

    private val chartColors = listOf(
        Color.rgb(196, 129, 145), // Dusty Rose
        Color.rgb(129, 199, 132), // Sage Green
        Color.rgb(77, 182, 172),  // Muted Teal
        Color.rgb(186, 134, 175), // Mauve
        Color.rgb(255, 183, 77),  // Warm Amber
        Color.rgb(130, 177, 255), // Soft Blue
        Color.rgb(200, 200, 210)  // Silver
    )

    private val roboto = Typeface.SANS_SERIF
    private val robotoBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

    fun generateReport(context: Context, data: ReportData): Uri {
        val document = PdfDocument()
        val pw = 595f
        val ph = 842f
        val m = 28f
        val cardRadius = 12f
        var y: Float
        val app = context

        // ===== PAGE 1 =====
        val p1 = document.startPage(PdfDocument.PageInfo.Builder(pw.toInt(), ph.toInt(), 1).create())
        val c = p1.canvas
        c.drawColor(bg)
        y = m + 8f

        // ---- Header ----
        val titlePaint = Paint().apply { color = accentMint; textSize = 30f; typeface = robotoBold; isAntiAlias = true }
        c.drawText("SmartFinance", m, y, titlePaint)
        y += 22f
        val metaPaint = Paint().apply { color = textMuted; textSize = 11f; typeface = roboto; isAntiAlias = true }
        c.drawText("Monthly Financial Health Report  —  ${data.period}", m, y, metaPaint)
        y += 28f
        val dividerPaint = Paint().apply { color = separator; strokeWidth = 1f }
        c.drawLine(m, y, pw - m, y, dividerPaint)
        y += 24f

        // ---- KPI Dashboard Card ----
        val kpiCard = RectF(m, y, pw - m, y + 90f)
        val cardBgPaint = Paint().apply { color = cardBg; isAntiAlias = true }
        c.drawRoundRect(kpiCard, cardRadius, cardRadius, cardBgPaint)

        val metricW = (pw - m * 2f) / 3f
        val labelPaint = Paint().apply { color = textMuted; textSize = 9f; typeface = roboto; isAntiAlias = true }
        val amtPaint = Paint().apply { textSize = 18f; typeface = robotoBold; isAntiAlias = true }
        val healthBgPaint = Paint().apply { isAntiAlias = true }

        // Income
        c.drawText("TOTAL INCOME", m + 16f, y + 28f, labelPaint)
        amtPaint.color = greenTint
        c.drawText("$${String.format("%.0f", data.monthlyIncome)}", m + 16f, y + 58f, amtPaint)

        // Expenses
        val expX = m + metricW
        c.drawText("TOTAL EXPENSES", expX + 16f, y + 28f, labelPaint)
        amtPaint.color = accentAmber
        c.drawText("$${String.format("%.0f", data.monthlyExpenses)}", expX + 16f, y + 58f, amtPaint)

        // Health badge
        val healthX = m + metricW * 2f
        c.drawText("FINANCIAL HEALTH", healthX + 16f, y + 28f, labelPaint)
        val hc = when {
            data.healthPercentage <= 50f -> greenTint
            data.healthPercentage <= 85f -> accentAmber
            else -> redTint
        }
        val badgeRect = RectF(healthX + 16f, y + 36f, healthX + metricW - 16f, y + 66f)
        healthBgPaint.color = Color.argb(30, Color.red(hc), Color.green(hc), Color.blue(hc))
        c.drawRoundRect(badgeRect, 8f, 8f, healthBgPaint)
        val badgePaint = Paint().apply { color = hc; textSize = 16f; typeface = robotoBold; isAntiAlias = true }
        c.drawText("${data.healthPercentage.toInt()}%", healthX + metricW / 2f - 12f, y + 56f, badgePaint)

        y = kpiCard.bottom + 28f

        // ---- Section Title ----
        val sectionPaint = Paint().apply { color = accentViolet; textSize = 14f; typeface = robotoBold; isAntiAlias = true }
        c.drawLine(m, y, pw - m, y, dividerPaint)
        y += 4f
        c.drawText("EXPENSES BY CATEGORY", m, y + 14f, sectionPaint)
        y += 32f

        // ---- Pie Chart ----
        if (data.categories.isNotEmpty() && data.totalExpenses > 0) {
            val total = data.totalExpenses.toFloat()
            val chartCard = RectF(m, y, pw - m, y + 190f)
            c.drawRoundRect(chartCard, cardRadius, cardRadius, cardBgPaint)

            val cx = m + 120f
            val cy = chartCard.top + 95f
            val radius = 72f
            var startAngle = -90f

            data.categories.forEachIndexed { idx, (_, amount) ->
                val sweep = (amount.toFloat() / total) * 360f
                val arcPaint = Paint().apply {
                    color = chartColors[idx % chartColors.size]; isAntiAlias = true
                    style = Paint.Style.FILL
                }
                c.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                    startAngle, sweep, true, arcPaint)
                startAngle += sweep
            }

            // Legend
            var ly = chartCard.top + 20f
            val lx = cx + radius + 30f
            val sqPaint = Paint().apply { isAntiAlias = true }
            val legTextPaint = Paint().apply { color = textPrimary; textSize = 10f; typeface = roboto; isAntiAlias = true }
            val legPctPaint = Paint().apply { color = textMuted; textSize = 9f; typeface = roboto; isAntiAlias = true }

            data.categories.forEachIndexed { idx, (cat, amount) ->
                val pct = if (total > 0) (amount / data.monthlyExpenses * 100) else 0.0
                sqPaint.color = chartColors[idx % chartColors.size]
                c.drawRect(lx, ly, lx + 10f, ly + 10f, sqPaint)
                val label = if (cat.length > 12) cat.take(12) else cat
                c.drawText(label, lx + 16f, ly + 9f, legTextPaint)
                c.drawText("${String.format("%.1f", pct)}%", lx + 16f, ly + 22f, legPctPaint)
                ly += 28f
            }
            y = chartCard.bottom + 20f
        }

        // ---- Mascot Section ----
        val topCategory = data.categories.maxByOrNull { it.second }?.first
        val mascot = getMascotForReport(topCategory)
        val mascotCard = RectF(m, y, pw - m, y + 160f)
        c.drawRoundRect(mascotCard, cardRadius, cardRadius, cardBgPaint)

        // Draw vector mascot
        val mcX = pw / 2f
        val mcY = mascotCard.top + 65f
        drawMascot(c, mcX, mcY, 24f, mascot.style)

        val mascotNamePaint = Paint().apply {
            color = textPrimary; textSize = 14f; typeface = robotoBold; isAntiAlias = true; textAlign = Paint.Align.CENTER
        }
        c.drawText(mascot.name, pw / 2f, mascotCard.top + 100f, mascotNamePaint)

        val mascotMsgPaint = Paint().apply {
            color = textMuted; textSize = 10f; typeface = roboto; isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        c.drawText("\u201C${mascot.message}\u201D", pw / 2f, mascotCard.top + 125f, mascotMsgPaint)
        y = mascotCard.bottom + 20f

        // Categories summary
        val detailCard = RectF(m, y, pw - m, y + 40f + data.categories.size * 16f)
        c.drawRoundRect(detailCard, cardRadius, cardRadius, cardBgPaint)
        var dy = detailCard.top + 18f
        val catNamePaint = Paint().apply { color = textMuted; textSize = 9f; typeface = roboto; isAntiAlias = true }
        val catValPaint = Paint().apply { color = textPrimary; textSize = 10f; typeface = roboto; isAntiAlias = true }
        val colorSwatchPaint = Paint().apply { isAntiAlias = true }
        data.categories.forEachIndexed { idx, (cat, amount) ->
            val pct = if (data.monthlyExpenses > 0) (amount / data.monthlyExpenses * 100) else 0.0
            colorSwatchPaint.color = chartColors[idx % chartColors.size]
            c.drawRect(m + 16f, dy, m + 26f, dy + 10f, colorSwatchPaint)
            c.drawText(cat, m + 34f, dy + 9f, catNamePaint)
            c.drawText("$${String.format("%.2f", amount)} (${String.format("%.1f", pct)}%)",
                pw - m - 16f, dy + 9f, catValPaint)
            dy += 16f
        }

        document.finishPage(p1)

        // ===== PAGE 2: Transactions =====
        if (data.transactions.isNotEmpty()) {
            val p2 = document.startPage(PdfDocument.PageInfo.Builder(pw.toInt(), ph.toInt(), 2).create())
            val c2 = p2.canvas
            c2.drawColor(bg)
            var ry = m + 16f

            c2.drawLine(m, ry, pw - m, ry, dividerPaint)
            ry += 4f
            c2.drawText("ALL TRANSACTIONS", m, ry + 14f, sectionPaint)
            ry += 32f

            // Table header
            val hdrName = Paint().apply { color = accentViolet; textSize = 9f; typeface = robotoBold; isAntiAlias = true }
            val hdrAmt = Paint().apply {
                color = accentViolet; textSize = 9f; typeface = robotoBold; isAntiAlias = true; textAlign = Paint.Align.RIGHT
            }
            val colName = 0f
            val colCat = 180f
            val colAmt = pw - m - 80f
            val colType = pw - m

            c2.drawText("NAME", colName + m, ry, hdrName)
            c2.drawText("CATEGORY", colCat + m, ry, hdrName)
            c2.drawText("AMOUNT", colAmt, ry, hdrAmt)
            c2.drawText("TYPE", colType, ry, hdrName)
            ry += 6f
            c2.drawLine(m, ry, pw - m, ry, dividerPaint)
            ry += 8f

            val rowPaint = Paint().apply { color = textPrimary; textSize = 9f; typeface = roboto; isAntiAlias = true }
            val rowAltPaint = Paint().apply { color = cardBgAlt }
            val rowAmtPaint = Paint().apply {
                color = textPrimary; textSize = 9f; typeface = roboto; isAntiAlias = true; textAlign = Paint.Align.RIGHT
            }
            val typeIncPaint = Paint().apply {
                color = greenTint; textSize = 8f; typeface = roboto; isAntiAlias = true; textAlign = Paint.Align.RIGHT
            }
            val typeExpPaint = Paint().apply {
                color = accentAmber; textSize = 8f; typeface = roboto; isAntiAlias = true; textAlign = Paint.Align.RIGHT
            }
            val rowBg = Paint().apply { isAntiAlias = true }
            val rowH = 16f
            var alt = false

            for (t in data.transactions) {
                val rowTop = ry
                val rowBot = ry + rowH
                if (rowBot > ph - m) break

                if (alt) {
                    rowBg.color = cardBgAlt
                    c2.drawRoundRect(RectF(m, rowTop, pw - m, rowBot), 4f, 4f, rowBg)
                }
                alt = !alt

                c2.drawText(t.name.take(18), colName + m + 4f, ry + 11f, rowPaint)
                c2.drawText(t.category.take(12), colCat + m + 4f, ry + 11f, rowPaint)
                c2.drawText("$${String.format("%.2f", t.amount)}", colAmt, ry + 11f, rowAmtPaint)
                val typePaint = if (t.type == TransactionType.Income) typeIncPaint else typeExpPaint
                val typeText = if (t.type == TransactionType.Income) "Income" else "Expense"
                c2.drawText(typeText, colType, ry + 11f, typePaint)

                ry = rowBot
            }

            document.finishPage(p2)
        }

        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val file = File(dir, "SmartFinance_Report_${data.period.replace("/", "_")}.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun drawMascot(c: Canvas, cx: Float, cy: Float, size: Float, style: Int) {
        val paint = Paint().apply { isAntiAlias = true }
        when (style) {
            MASCOT_CAT -> drawCat(c, cx, cy, size, paint)
            MASCOT_BEAR -> drawBear(c, cx, cy, size, paint)
            MASCOT_RACCOON -> drawRaccoon(c, cx, cy, size, paint)
            MASCOT_CHEETAH -> drawCheetah(c, cx, cy, size, paint)
            MASCOT_CHICK -> drawChick(c, cx, cy, size, paint)
        }
    }

    private fun drawCat(c: Canvas, cx: Float, cy: Float, s: Float, p: Paint) {
        p.color = Color.rgb(180, 170, 200)
        c.drawCircle(cx, cy + s * 0.1f, s, p)
        // ears
        val earPath = Path().apply {
            moveTo(cx - s * 0.7f, cy - s * 0.6f)
            lineTo(cx - s * 0.2f, cy - s * 0.3f)
            lineTo(cx - s * 0.4f, cy + s * 0.1f)
            close()
        }
        c.drawPath(earPath, p)
        val earPath2 = Path().apply {
            moveTo(cx + s * 0.7f, cy - s * 0.6f)
            lineTo(cx + s * 0.2f, cy - s * 0.3f)
            lineTo(cx + s * 0.4f, cy + s * 0.1f)
            close()
        }
        c.drawPath(earPath2, p)
        // eyes
        p.color = textPrimary
        c.drawCircle(cx - s * 0.25f, cy + s * 0.05f, s * 0.08f, p)
        c.drawCircle(cx + s * 0.25f, cy + s * 0.05f, s * 0.08f, p)
        // nose
        p.color = Color.rgb(240, 150, 180)
        c.drawCircle(cx, cy + s * 0.2f, s * 0.06f, p)
        // whiskers
        p.color = textMuted; p.strokeWidth = 1.2f; p.style = Paint.Style.STROKE
        c.drawLine(cx - s * 0.2f, cy + s * 0.25f, cx - s * 0.7f, cy + s * 0.15f, p)
        c.drawLine(cx - s * 0.2f, cy + s * 0.3f, cx - s * 0.7f, cy + s * 0.3f, p)
        c.drawLine(cx + s * 0.2f, cy + s * 0.25f, cx + s * 0.7f, cy + s * 0.15f, p)
        c.drawLine(cx + s * 0.2f, cy + s * 0.3f, cx + s * 0.7f, cy + s * 0.3f, p)
        p.style = Paint.Style.FILL; p.strokeWidth = 0f
        // body
        val bodyPaint = Paint().apply { color = Color.rgb(160, 150, 180); isAntiAlias = true }
        c.drawOval(cx - s * 0.6f, cy + s * 0.4f, cx + s * 0.6f, cy + s * 1.4f, bodyPaint)
        // tail
        val tailPath = Path().apply {
            moveTo(cx + s * 0.5f, cy + s * 0.8f)
            cubicTo(cx + s * 1.3f, cy + s * 0.4f, cx + s * 1.5f, cy + s * 1.2f, cx + s * 0.8f, cy + s * 1.3f)
        }
        val tailPaint = Paint().apply { color = Color.rgb(160, 150, 180); isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 4f; strokeCap = Paint.Cap.ROUND }
        c.drawPath(tailPath, tailPaint)
    }

    private fun drawBear(c: Canvas, cx: Float, cy: Float, s: Float, p: Paint) {
        p.color = Color.rgb(139, 90, 43)
        // ears
        c.drawCircle(cx - s * 0.6f, cy - s * 0.5f, s * 0.25f, p)
        c.drawCircle(cx + s * 0.6f, cy - s * 0.5f, s * 0.25f, p)
        // inner ears
        p.color = Color.rgb(180, 130, 70)
        c.drawCircle(cx - s * 0.6f, cy - s * 0.5f, s * 0.12f, p)
        c.drawCircle(cx + s * 0.6f, cy - s * 0.5f, s * 0.12f, p)
        // head
        p.color = Color.rgb(139, 90, 43)
        c.drawCircle(cx, cy, s * 0.8f, p)
        // muzzle
        p.color = Color.rgb(180, 140, 80)
        c.drawOval(cx - s * 0.35f, cy + s * 0.05f, cx + s * 0.35f, cy + s * 0.5f, p)
        // eyes
        p.color = textPrimary
        c.drawCircle(cx - s * 0.25f, cy - s * 0.1f, s * 0.07f, p)
        c.drawCircle(cx + s * 0.25f, cy - s * 0.1f, s * 0.07f, p)
        // nose
        p.color = Color.rgb(30, 20, 10)
        c.drawCircle(cx, cy + s * 0.2f, s * 0.06f, p)
        // body
        val bodyPaint = Paint().apply { color = Color.rgb(120, 75, 35); isAntiAlias = true }
        c.drawOval(cx - s * 0.7f, cy + s * 0.6f, cx + s * 0.7f, cy + s * 1.5f, bodyPaint)
    }

    private fun drawRaccoon(c: Canvas, cx: Float, cy: Float, s: Float, p: Paint) {
        p.color = Color.rgb(120, 120, 120)
        // ears
        val earP = Path().apply {
            moveTo(cx - s * 0.6f, cy - s * 0.5f)
            lineTo(cx - s * 0.2f, cy - s * 0.8f)
            lineTo(cx - s * 0.1f, cy - s * 0.3f); close()
        }
        c.drawPath(earP, p)
        val earP2 = Path().apply {
            moveTo(cx + s * 0.6f, cy - s * 0.5f)
            lineTo(cx + s * 0.2f, cy - s * 0.8f)
            lineTo(cx + s * 0.1f, cy - s * 0.3f); close()
        }
        c.drawPath(earP2, p)
        // head
        c.drawCircle(cx, cy, s * 0.75f, p)
        // mask
        p.color = Color.rgb(50, 50, 55)
        c.drawOval(cx - s * 0.5f, cy - s * 0.1f, cx + s * 0.5f, cy + s * 0.35f, p)
        // eyes
        p.color = textPrimary
        c.drawCircle(cx - s * 0.2f, cy + s * 0.05f, s * 0.07f, p)
        c.drawCircle(cx + s * 0.2f, cy + s * 0.05f, s * 0.07f, p)
        // nose
        p.color = Color.rgb(30, 30, 30)
        c.drawCircle(cx, cy + s * 0.25f, s * 0.05f, p)
        // body
        val bodyP = Paint().apply { color = Color.rgb(100, 100, 100); isAntiAlias = true }
        c.drawOval(cx - s * 0.6f, cy + s * 0.5f, cx + s * 0.6f, cy + s * 1.4f, bodyP)
        // stripes
        val stripeP = Paint().apply { color = Color.rgb(80, 80, 85); isAntiAlias = true; strokeWidth = 3f; style = Paint.Style.STROKE }
        c.drawLine(cx - s * 0.3f, cy + s * 0.7f, cx - s * 0.4f, cy + s * 1.2f, stripeP)
        c.drawLine(cx + s * 0.3f, cy + s * 0.7f, cx + s * 0.4f, cy + s * 1.2f, stripeP)
    }

    private fun drawCheetah(c: Canvas, cx: Float, cy: Float, s: Float, p: Paint) {
        p.color = Color.rgb(210, 160, 70)
        // head
        c.drawOval(cx - s * 0.7f, cy - s * 0.5f, cx + s * 0.7f, cy + s * 0.5f, p)
        // ears
        val earP = Path().apply {
            moveTo(cx - s * 0.5f, cy - s * 0.4f)
            lineTo(cx - s * 0.4f, cy - s * 0.8f)
            lineTo(cx - s * 0.15f, cy - s * 0.4f); close()
        }
        c.drawPath(earP, p)
        val earP2 = Path().apply {
            moveTo(cx + s * 0.5f, cy - s * 0.4f)
            lineTo(cx + s * 0.4f, cy - s * 0.8f)
            lineTo(cx + s * 0.15f, cy - s * 0.4f); close()
        }
        c.drawPath(earP2, p)
        // eyes
        p.color = textPrimary
        c.drawCircle(cx - s * 0.2f, cy - s * 0.05f, s * 0.06f, p)
        c.drawCircle(cx + s * 0.2f, cy - s * 0.05f, s * 0.06f, p)
        // nose line
        p.color = Color.rgb(180, 120, 40)
        p.strokeWidth = 2f; p.style = Paint.Style.STROKE
        c.drawLine(cx, cy + s * 0.05f, cx, cy + s * 0.2f, p)
        p.style = Paint.Style.FILL; p.strokeWidth = 0f
        // spots
        val spotP = Paint().apply { color = Color.rgb(180, 100, 30); isAntiAlias = true }
        c.drawCircle(cx - s * 0.3f, cy + s * 0.1f, s * 0.04f, spotP)
        c.drawCircle(cx + s * 0.25f, cy + s * 0.15f, s * 0.04f, spotP)
        c.drawCircle(cx - s * 0.1f, cy + s * 0.2f, s * 0.03f, spotP)
        // body
        val bodyP = Paint().apply { color = Color.rgb(200, 150, 60); isAntiAlias = true }
        c.drawOval(cx - s * 0.6f, cy + s * 0.4f, cx + s * 0.6f, cy + s * 1.4f, bodyP)
        // more spots on body
        c.drawCircle(cx - s * 0.3f, cy + s * 0.7f, s * 0.04f, spotP)
        c.drawCircle(cx + s * 0.2f, cy + s * 0.8f, s * 0.04f, spotP)
        c.drawCircle(cx - s * 0.1f, cy + s * 1.0f, s * 0.03f, spotP)
        // tail
        val tailP = Paint().apply { color = Color.rgb(200, 150, 60); isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 3f; strokeCap = Paint.Cap.ROUND }
        val tailPath = Path().apply {
            moveTo(cx + s * 0.5f, cy + s * 0.6f)
            cubicTo(cx + s * 1.2f, cy + s * 0.2f, cx + s * 1.3f, cy + s * 1.0f, cx + s * 0.7f, cy + s * 1.2f)
        }
        c.drawPath(tailPath, tailP)
    }

    private fun drawChick(c: Canvas, cx: Float, cy: Float, s: Float, p: Paint) {
        p.color = Color.rgb(255, 230, 100)
        // body (circle)
        c.drawCircle(cx, cy + s * 0.1f, s * 0.8f, p)
        // eyes
        p.color = textPrimary
        c.drawCircle(cx - s * 0.2f, cy - s * 0.05f, s * 0.06f, p)
        c.drawCircle(cx + s * 0.2f, cy - s * 0.05f, s * 0.06f, p)
        // beak
        p.color = Color.rgb(255, 150, 50)
        val beakPath = Path().apply {
            moveTo(cx - s * 0.1f, cy + s * 0.1f)
            lineTo(cx + s * 0.1f, cy + s * 0.1f)
            lineTo(cx, cy + s * 0.3f); close()
        }
        c.drawPath(beakPath, p)
        // feet
        val footP = Paint().apply { color = Color.rgb(255, 140, 40); isAntiAlias = true }
        val f1 = Path().apply {
            moveTo(cx - s * 0.2f, cy + s * 0.8f)
            lineTo(cx - s * 0.4f, cy + s * 1.1f)
            lineTo(cx - s * 0.15f, cy + s * 1.05f); close()
        }
        c.drawPath(f1, footP)
        val f2 = Path().apply {
            moveTo(cx + s * 0.2f, cy + s * 0.8f)
            lineTo(cx + s * 0.4f, cy + s * 1.1f)
            lineTo(cx + s * 0.15f, cy + s * 1.05f); close()
        }
        c.drawPath(f2, footP)
        // cheek blush
        val blushP = Paint().apply { color = Color.argb(80, 255, 100, 100); isAntiAlias = true }
        c.drawCircle(cx - s * 0.35f, cy + s * 0.1f, s * 0.1f, blushP)
        c.drawCircle(cx + s * 0.35f, cy + s * 0.1f, s * 0.1f, blushP)
    }
}
