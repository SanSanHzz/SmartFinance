package com.example.smartfinance.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartfinance.R
import com.example.smartfinance.data.model.TransactionType
import com.example.smartfinance.ui.theme.CriticalRed
import com.example.smartfinance.ui.theme.DarkBackground
import com.example.smartfinance.ui.theme.DarkOnSurface
import com.example.smartfinance.ui.theme.DarkSurface
import com.example.smartfinance.ui.theme.DarkSurfaceVariant
import com.example.smartfinance.ui.theme.HealthyGreen
import com.example.smartfinance.ui.theme.Orange
import com.example.smartfinance.ui.theme.Teal
import com.example.smartfinance.util.CategoryUtils
import com.example.smartfinance.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: MainViewModel,
    prefilledType: TransactionType?,
    prefilledName: String?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(prefilledType ?: TransactionType.Expense) }
    var txName by remember { mutableStateOf(prefilledName ?: "") }
    var amount by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showNewDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf("") }

    val prefs = context.getSharedPreferences("smartfinance", Context.MODE_PRIVATE)
    val customCats = (prefs.getStringSet("custom_categories", emptySet()) ?: emptySet()).toMutableList()
    val allCategories = CategoryUtils.defaultCategories + customCats

    fun saveCustomCategories(updated: Set<String>) {
        prefs.edit().putStringSet("custom_categories", updated).apply()
    }

    val isFormValid = txName.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDouble() > 0 && selectedCategory.isNotBlank()

    // New category dialog
    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title = { Text(stringResource(R.string.add_custom_category)) },
            text = {
                OutlinedTextField(
                    value = newCategoryName, onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.new_category_name)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkOnSurface, unfocusedTextColor = DarkOnSurface, focusedBorderColor = Teal, unfocusedBorderColor = DarkSurfaceVariant)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newCategoryName.trim()
                    if (trimmed.isNotBlank()) {
                        saveCustomCategories((prefs.getStringSet("custom_categories", emptySet()) ?: emptySet()) + trimmed)
                        selectedCategory = trimmed; showNewDialog = false; newCategoryName = ""; categoryExpanded = false
                    }
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = { TextButton(onClick = { showNewDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    // Edit category dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName, onValueChange = { newCategoryName = it },
                    label = { Text("New name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkOnSurface, unfocusedTextColor = DarkOnSurface, focusedBorderColor = Teal, unfocusedBorderColor = DarkSurfaceVariant)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newCategoryName.trim()
                    if (trimmed.isNotBlank()) {
                        val cats = (prefs.getStringSet("custom_categories", emptySet()) ?: emptySet()).toMutableSet()
                        cats.remove(editingCategory); cats.add(trimmed)
                        saveCustomCategories(cats)
                        if (selectedCategory == editingCategory) selectedCategory = trimmed
                        showEditDialog = false; newCategoryName = ""; categoryExpanded = false
                    }
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_transaction_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface, titleContentColor = DarkOnSurface)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = type == TransactionType.Income, onClick = { type = TransactionType.Income }, label = { Text(stringResource(R.string.income)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HealthyGreen.copy(alpha = 0.2f), selectedLabelColor = HealthyGreen))
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = type == TransactionType.Expense, onClick = { type = TransactionType.Expense }, label = { Text(stringResource(R.string.expenses)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Orange.copy(alpha = 0.2f), selectedLabelColor = Orange))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = txName, onValueChange = { txName = it }, label = { Text(stringResource(com.example.smartfinance.R.string.name) + " *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkOnSurface, unfocusedTextColor = DarkOnSurface, focusedBorderColor = Teal, unfocusedBorderColor = DarkSurfaceVariant))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(stringResource(R.string.amount) + " *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkOnSurface, unfocusedTextColor = DarkOnSurface, focusedBorderColor = Teal, unfocusedBorderColor = DarkSurfaceVariant))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = place, onValueChange = { place = it }, label = { Text(stringResource(R.string.place_optional)) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkOnSurface, unfocusedTextColor = DarkOnSurface, focusedBorderColor = Teal, unfocusedBorderColor = DarkSurfaceVariant))
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                val displayText = if (selectedCategory.isNotBlank()) CategoryUtils.getDisplayNameWithFallback(selectedCategory, customCats) else ""
                OutlinedTextField(value = displayText, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.category) + " *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = DarkOnSurface, unfocusedTextColor = DarkOnSurface, focusedBorderColor = Teal, unfocusedBorderColor = DarkSurfaceVariant))
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    allCategories.forEach { cat ->
                        val isCustom = cat in customCats
                        DropdownMenuItem(
                            text = {
                                androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text(CategoryUtils.getDisplayNameWithFallback(cat, customCats), modifier = Modifier.weight(1f))
                                    if (isCustom) {
                                        IconButton(onClick = { editingCategory = cat; newCategoryName = cat; showEditDialog = true; categoryExpanded = false }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Teal)
                                        }
                                        IconButton(onClick = {
                                            val cats = (prefs.getStringSet("custom_categories", emptySet()) ?: emptySet()).toMutableSet()
                                            cats.remove(cat); saveCustomCategories(cats)
                                            if (selectedCategory == cat) selectedCategory = ""
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete", tint = CriticalRed)
                                        }
                                    }
                                }
                            },
                            onClick = { selectedCategory = cat; categoryExpanded = false }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(text = { Text(stringResource(R.string.add_custom_category), color = Teal) }, onClick = { showNewDialog = true })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                    viewModel.addTransaction(type, txName.trim(), amount.toDouble(), place.trim().ifEmpty { null }, selectedCategory); onNavigateBack()
            }, enabled = isFormValid, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                Text(stringResource(R.string.save_transaction))
            }
        }
    }
}
