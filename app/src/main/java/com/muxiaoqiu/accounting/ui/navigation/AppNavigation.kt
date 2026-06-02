package com.muxiaoqiu.accounting.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muxiaoqiu.accounting.ui.screens.AccountingViewModel
import com.muxiaoqiu.accounting.ui.screens.AddTransactionScreen
import com.muxiaoqiu.accounting.ui.screens.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD = "add"
}

@Composable
fun AppNavigation(viewModel: AccountingViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Routes.ADD) }
            )
        }
        composable(Routes.ADD) {
            AddTransactionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
