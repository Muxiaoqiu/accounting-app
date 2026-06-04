package com.muxiaoqiu.accounting.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.muxiaoqiu.accounting.ui.screens.AccountingViewModel
import com.muxiaoqiu.accounting.ui.screens.AddTransactionScreen
import com.muxiaoqiu.accounting.ui.screens.BookListScreen
import com.muxiaoqiu.accounting.ui.screens.BookViewModel
import com.muxiaoqiu.accounting.ui.screens.BudgetScreen
import com.muxiaoqiu.accounting.ui.screens.BudgetViewModel
import com.muxiaoqiu.accounting.ui.screens.CategoryManageScreen
import com.muxiaoqiu.accounting.ui.screens.CategoryViewModel
import com.muxiaoqiu.accounting.ui.screens.HomeScreen
import com.muxiaoqiu.accounting.ui.screens.StatisticsScreen
import com.muxiaoqiu.accounting.ui.screens.StatisticsViewModel
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val BOOKS = "books"
    const val HOME = "home/{bookId}/{bookName}"
    const val ADD = "add/{bookId}"
    const val CATEGORIES = "categories"
    const val STATS = "stats/{bookId}"
    const val BUDGET = "budget/{bookId}"

    fun home(bookId: Long, bookName: String) = "home/$bookId/${URLEncoder.encode(bookName, "UTF-8")}"
    fun add(bookId: Long) = "add/$bookId"
    fun stats(bookId: Long) = "stats/$bookId"
    fun budget(bookId: Long) = "budget/$bookId"
}

@Composable
fun AppNavigation(
    bookViewModel: BookViewModel,
    accountingViewModel: AccountingViewModel,
    categoryViewModel: CategoryViewModel,
    statisticsViewModel: StatisticsViewModel,
    budgetViewModel: BudgetViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.BOOKS
    ) {
        composable(Routes.BOOKS) {
            BookListScreen(
                viewModel = bookViewModel,
                onBookClick = { bookId, bookName ->
                    navController.navigate(Routes.home(bookId, bookName))
                }
            )
        }
        composable(
            route = Routes.HOME,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("bookName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            val bookName = URLDecoder.decode(
                backStackEntry.arguments?.getString("bookName") ?: "", "UTF-8"
            )
            HomeScreen(
                bookId = bookId,
                bookName = bookName,
                viewModel = accountingViewModel,
                categoryViewModel = categoryViewModel,
                budgetViewModel = budgetViewModel,
                onAddClick = { navController.navigate(Routes.add(bookId)) },
                onBack = { navController.popBackStack() },
                onStatsClick = { navController.navigate(Routes.stats(bookId)) },
                onBudgetClick = { navController.navigate(Routes.budget(bookId)) }
            )
        }
        composable(
            route = Routes.ADD,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            AddTransactionScreen(
                bookId = bookId,
                viewModel = accountingViewModel,
                categoryViewModel = categoryViewModel,
                onBack = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Routes.CATEGORIES) }
            )
        }
        composable(Routes.CATEGORIES) {
            CategoryManageScreen(
                viewModel = categoryViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.STATS,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            StatisticsScreen(
                bookId = bookId,
                viewModel = statisticsViewModel,
                categoryViewModel = categoryViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.BUDGET,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            BudgetScreen(
                bookId = bookId,
                viewModel = budgetViewModel,
                categoryViewModel = categoryViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
