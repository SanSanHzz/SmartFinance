package com.example.smartfinance.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MascotInfo(
    val emoji: String,
    val name: String,
    val message: String
)

fun getMascotForCategory(topCategory: String?): MascotInfo = when {
    topCategory == null -> MascotInfo("🐣", "Chick", "Start tracking your expenses!")
    topCategory.contains("Food", ignoreCase = true) ->
        MascotInfo("🐻", "Bear", "You're splurging on cravings this month!")
    topCategory.contains("Shopping", ignoreCase = true) ->
        MascotInfo("🦝", "Raccoon", "The delivery driver knows your name by heart now.")
    topCategory.contains("Transport", ignoreCase = true) ->
        MascotInfo("🐆", "Cheetah", "Always on the move! Your wallet feels the speed.")
    else -> MascotInfo("🐱", "Cat", "Curious about where your money goes?")
}

@Composable
fun MascotAvatar(
    mascot: MascotInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mascot.emoji,
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mascot.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mascot.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
