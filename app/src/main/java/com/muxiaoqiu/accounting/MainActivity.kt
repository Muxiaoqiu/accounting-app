package com.muxiaoqiu.accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muxiaoqiu.accounting.ui.navigation.AppNavigation
import com.muxiaoqiu.accounting.ui.screens.AccountingViewModel
import com.muxiaoqiu.accounting.ui.screens.BookViewModel
import com.muxiaoqiu.accounting.ui.screens.CategoryViewModel
import com.muxiaoqiu.accounting.ui.theme.AccountingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AccountingApp
        val bookViewModel = BookViewModel(app.database.bookDao())
        val accountingViewModel = AccountingViewModel(app.database.transactionDao())
        val categoryViewModel = CategoryViewModel(app.database.categoryDao())

        setContent {
            AccountingTheme {
                AppNavigation(
                    bookViewModel = bookViewModel,
                    accountingViewModel = accountingViewModel,
                    categoryViewModel = categoryViewModel
                )
            }
        }
    }
}
