package com.example.smartfinance.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.smartfinance.R

object CategoryUtils {
    val defaultCategories = listOf(
        "Food", "Online Shopping", "Transport", "Salary", "Entertainment",
        "Utilities", "Rent", "Health", "Education", "Other"
    )

    @Composable
    fun getDisplayName(categoryKey: String): String {
        return when (categoryKey) {
            "Food" -> stringResource(R.string.cat_food)
            "Online Shopping" -> stringResource(R.string.cat_online_shopping)
            "Transport" -> stringResource(R.string.cat_transport)
            "Salary" -> stringResource(R.string.cat_salary)
            "Entertainment" -> stringResource(R.string.cat_entertainment)
            "Utilities" -> stringResource(R.string.cat_utilities)
            "Rent" -> stringResource(R.string.cat_rent)
            "Health" -> stringResource(R.string.cat_health)
            "Education" -> stringResource(R.string.cat_education)
            "Other" -> stringResource(R.string.cat_other)
            else -> categoryKey
        }
    }

    @Composable
    fun getDisplayNameWithFallback(categoryKey: String, customCategories: List<String>): String {
        if (categoryKey in defaultCategories) {
            return getDisplayName(categoryKey)
        }
        return categoryKey
    }
}
