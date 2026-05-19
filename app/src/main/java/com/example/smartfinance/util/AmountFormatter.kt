package com.example.smartfinance.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object AmountFormatter {

    fun format(raw: String): String {
        val digitsAndComma = raw.filter { it.isDigit() || it == ',' }
        val parts = digitsAndComma.split(",")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else ""
        if (intPart.isEmpty() && decPart.isEmpty()) return ""
        val formattedInt = if (intPart.isNotEmpty()) {
            intPart.reversed().chunked(3).joinToString(".").reversed()
        } else "0"
        return if (decPart.isNotEmpty()) "$formattedInt,$decPart" else formattedInt
    }

    fun parseToDouble(formatted: String): Double? {
        val normalized = formatted.replace(".", "").replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    fun formatDouble(value: Double): String {
        val withDecimals = String.format("%.2f", value) // "3200.50"
        val withComma = withDecimals.replace('.', ',')   // "3200,50"
        return format(withComma)                          // "3.200,50"
    }
}

class AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = AmountFormatter.format(raw)
        if (formatted == raw) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset >= raw.length) return formatted.length
                    val rawSoFar = raw.take(offset)
                    val digitsCount = rawSoFar.count { it.isDigit() || it == ',' }
                    val result = AmountFormatter.format(rawSoFar)
                    return result.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset >= formatted.length) return raw.length
                    val formattedSoFar = formatted.take(offset)
                    val digitsCount = formattedSoFar.count { it.isDigit() || it == ',' }
                    var found = 0
                    for (i in raw.indices) {
                        if (raw[i].isDigit() || raw[i] == ',') found++
                        if (found == digitsCount) return i + 1
                    }
                    return raw.length
                }
            }
        )
    }
}
