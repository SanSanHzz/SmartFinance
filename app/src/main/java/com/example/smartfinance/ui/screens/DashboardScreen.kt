package com.example.smartfinance.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartfinance.R
import com.example.smartfinance.data.model.TransactionType
import com.example.smartfinance.ui.components.RingChart
import com.example.smartfinance.ui.theme.CriticalRed
import com.example.smartfinance.ui.theme.DarkBackground
import com.example.smartfinance.ui.theme.DarkOnSurface
import com.example.smartfinance.ui.theme.DarkOnSurfaceVariant
import com.example.smartfinance.ui.theme.DarkSurface
import com.example.smartfinance.ui.theme.DarkSurfaceVariant
import com.example.smartfinance.ui.theme.HealthyGreen
import com.example.smartfinance.ui.theme.ModerateYellow
import com.example.smartfinance.ui.theme.Orange
import com.example.smartfinance.ui.theme.Purple
import com.example.smartfinance.ui.theme.Teal
import com.example.smartfinance.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToForm: (type: TransactionType, prefillName: String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onSeedData: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.dashboardState.collectAsState()
    val reportUri by viewModel.reportUri.collectAsState()

    val healthColor = when {
        state.healthPercentage <= 50f -> HealthyGreen
        state.healthPercentage <= 85f -> ModerateYellow
        else -> CriticalRed
    }

    LaunchedEffect(reportUri) {
        reportUri?.let { uriStr ->
            val uri = android.net.Uri.parse(uriStr)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Report"))
            viewModel.clearReportUri()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.smartfinance_title)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings), tint = DarkOnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = DarkOnSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToForm(TransactionType.Expense, "") },
                containerColor = Teal
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FinancialCard(stringResource(R.string.income), state.monthlyIncome, HealthyGreen)
                    FinancialCard(stringResource(R.string.expenses), state.monthlyExpenses, Orange)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.financial_health), style = MaterialTheme.typography.titleMedium, color = DarkOnSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Box(contentAlignment = Alignment.Center) {
                    RingChart(percentage = state.healthPercentage, activeColor = healthColor)
                    Text("${state.healthPercentage.toInt()}%", style = MaterialTheme.typography.headlineMedium, color = healthColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.topIncomeNames.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.quick_create_income),
                        style = MaterialTheme.typography.titleSmall,
                        color = DarkOnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.topIncomeNames.forEach { name ->
                            FilledTonalButton(
                                onClick = { onNavigateToForm(TransactionType.Income, name) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant, contentColor = HealthyGreen)
                            ) {
                                Text("+ $name", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.monthlyIncome == 0.0 && state.monthlyExpenses == 0.0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.no_transactions), style = MaterialTheme.typography.bodyLarge, color = DarkOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onSeedData, colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal)) {
                        Text(stringResource(R.string.load_sample_data))
                    }
                }

                if (state.topExpenseNames.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.quick_create_expense),
                        style = MaterialTheme.typography.titleSmall,
                        color = DarkOnSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.topExpenseNames.forEach { name ->
                            FilledTonalButton(
                                onClick = { onNavigateToForm(TransactionType.Expense, name) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant, contentColor = Orange)
                            ) {
                                Text("+ $name", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple)
                    ) {
                        Text(stringResource(R.string.view_analytics))
                    }
                    OutlinedButton(
                        onClick = { viewModel.generateReport(context) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialCard(label: String, amount: Double, color: Color) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = DarkOnSurface
            )
        }
    }
}
