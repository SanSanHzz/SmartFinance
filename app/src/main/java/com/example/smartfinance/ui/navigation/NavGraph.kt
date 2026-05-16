package com.example.smartfinance.ui.navigation

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartfinance.data.model.TransactionType
import com.example.smartfinance.ui.screens.AccountScreen
import com.example.smartfinance.ui.screens.AnalyticsScreen
import com.example.smartfinance.ui.screens.AssetsListScreen
import com.example.smartfinance.ui.screens.DashboardScreen
import com.example.smartfinance.ui.screens.EditTransactionScreen
import com.example.smartfinance.ui.screens.SettingsScreen
import com.example.smartfinance.ui.screens.TransactionFormScreen
import com.example.smartfinance.viewmodel.MainViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val restartApp = remember {
        {
            (context as? Activity)?.recreate() ?: Unit
        }
    }

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToForm = { type, name ->
                    navController.navigate(
                        "add_transaction?type=${type.name}&name=${Uri.encode(name)}"
                    )
                },
                onNavigateToAnalytics = {
                    navController.navigate("analytics")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToAccount = {
                    navController.navigate("account")
                },
                onSeedData = { viewModel.seedSampleData() }
            )
        }
        composable(
            route = "add_transaction?type={type}&name={name}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "Expense"
                },
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: "Expense"
            val name = backStackEntry.arguments?.getString("name") ?: ""
            TransactionFormScreen(
                viewModel = viewModel,
                prefilledType = TransactionType.valueOf(typeStr),
                prefilledName = name,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("analytics") {
            AnalyticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAssetsList = { type ->
                    navController.navigate("assets_list?type=$type")
                },
                onRestartApp = restartApp
            )
        }
        composable("account") {
            AccountScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "assets_list?type={type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "Expense"
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: "Expense"
            AssetsListScreen(
                viewModel = viewModel,
                type = TransactionType.valueOf(typeStr),
                onNavigateBack = { navController.popBackStack() },
                onEditTransaction = { id ->
                    navController.navigate("edit_transaction/$id")
                }
            )
        }
        composable(
            route = "edit_transaction/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            EditTransactionScreen(
                viewModel = viewModel,
                transactionId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
