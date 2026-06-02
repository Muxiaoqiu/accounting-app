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
import com.muxiaoqiu.accounting.ui.screens.HomeScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val BOOKS = "books"
    const val HOME = "home/{bookId}/{bookName}"
    const val ADD = "add/{bookId}"

    fun home(bookId: Long, bookName: String) = "home/$bookId/${URLEncoder.encode(bookName, "UTF-8")}"
    fun add(bookId: Long) = "add/$bookId"
}

@Composable
fun AppNavigation(
    bookViewModel: BookViewModel,
    accountingViewModel: AccountingViewModel
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
                onAddClick = { navController.navigate(Routes.add(bookId)) },
                onBack = { navController.popBackStack() }
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
                onBack = { navController.popBackStack() }
            )
        }
    }
}
