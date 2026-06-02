package com.muxiaoqiu.accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muxiaoqiu.accounting.ui.navigation.AppNavigation
import com.muxiaoqiu.accounting.ui.screens.AccountingViewModel
import com.muxiaoqiu.accounting.ui.screens.BookViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AccountingApp
        val bookViewModel = BookViewModel(app.database.bookDao())
        val accountingViewModel = AccountingViewModel(app.database.transactionDao())

        setContent {
            AppNavigation(
                bookViewModel = bookViewModel,
                accountingViewModel = accountingViewModel
            )
        }
    }
}
