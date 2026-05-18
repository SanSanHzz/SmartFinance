package com.example.smartfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.smartfinance.ui.components.MascotAvatar
import com.example.smartfinance.util.CategoryUtils
import com.example.smartfinance.ui.components.PieChart
import com.example.smartfinance.ui.components.getMascotForCategory
import com.example.smartfinance.ui.components.pieColors
import com.example.smartfinance.ui.components.PieSlice
import com.example.smartfinance.R
import com.example.smartfinance.ui.theme.DarkBackground
import com.example.smartfinance.ui.theme.DarkOnSurface
import com.example.smartfinance.ui.theme.DarkOnSurfaceVariant
import com.example.smartfinance.ui.theme.DarkSurface
import com.example.smartfinance.ui.theme.DarkSurfaceVariant
import com.example.smartfinance.ui.theme.Purple
import com.example.smartfinance.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsState()

    val topCategory = state.categoryBreakdown.maxByOrNull { it.total }?.category
    val mascot = getMascotForCategory(topCategory)

    val slices = state.categoryBreakdown.mapIndexed { idx, cat ->
        PieSlice(
            label = CategoryUtils.getDisplayNameWithFallback(cat.category, emptyList()),
            value = cat.total.toFloat(),
            color = pieColors[idx % pieColors.size]
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = DarkOnSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.monthly_breakdown),
                style = MaterialTheme.typography.titleLarge,
                color = DarkOnSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (slices.isNotEmpty()) {
                PieChart(slices = slices)
                Spacer(modifier = Modifier.height(16.dp))
                slices.forEach { slice ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(slice.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${slice.label}: $${String.format("%.2f", slice.value)}",
                            color = DarkOnSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    stringResource(R.string.no_expenses),
                    color = DarkOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = DarkSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.soul_of_expense),
                style = MaterialTheme.typography.titleMedium,
                color = Purple
            )

            MascotAvatar(mascot = mascot)
        }
    }
}
