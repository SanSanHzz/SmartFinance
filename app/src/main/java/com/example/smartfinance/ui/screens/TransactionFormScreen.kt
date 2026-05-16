package com.example.smartfinance.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.smartfinance.R
import com.example.smartfinance.data.model.TransactionType
import com.example.smartfinance.ui.theme.DarkBackground
import com.example.smartfinance.ui.theme.DarkOnSurface
import com.example.smartfinance.ui.theme.DarkOnSurfaceVariant
import com.example.smartfinance.ui.theme.DarkSurface
import com.example.smartfinance.ui.theme.DarkSurfaceVariant
import com.example.smartfinance.ui.theme.HealthyGreen
import com.example.smartfinance.ui.theme.Orange
import com.example.smartfinance.ui.theme.Teal
import com.example.smartfinance.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: MainViewModel,
    prefilledType: TransactionType?,
    prefilledName: String?,
    onNavigateBack: () -> Unit
) {
    var type by remember { mutableStateOf(prefilledType ?: TransactionType.Expense) }
    var name by remember { mutableStateOf(prefilledName ?: "") }
    var amount by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food", "Online Shopping", "Transport", "Salary", "Entertainment",
        "Utilities", "Rent", "Health", "Education", "Other"
    )

    val isFormValid = name.isNotBlank() &&
            amount.toDoubleOrNull() != null &&
            amount.toDouble() > 0 &&
            selectedCategory.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction_title)) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = type == TransactionType.Income,
                    onClick = { type = TransactionType.Income },
                    label = { Text(stringResource(R.string.income)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HealthyGreen.copy(alpha = 0.2f),
                        selectedLabelColor = HealthyGreen
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = type == TransactionType.Expense,
                    onClick = { type = TransactionType.Expense },
                    label = { Text(stringResource(R.string.expenses)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Orange.copy(alpha = 0.2f),
                        selectedLabelColor = Orange
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.name) + " *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface,
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = DarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(stringResource(R.string.amount) + " *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface,
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = DarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text(stringResource(R.string.place_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface,
                    focusedBorderColor = Teal,
                    unfocusedBorderColor = DarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.category) + " *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkOnSurface,
                        unfocusedTextColor = DarkOnSurface,
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = DarkSurfaceVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.addTransaction(
                        type = type,
                        name = name.trim(),
                        amount = amount.toDouble(),
                        place = place.trim().ifEmpty { null },
                        category = selectedCategory
                    )
                    onNavigateBack()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(stringResource(R.string.save_transaction))
            }
        }
    }
}
