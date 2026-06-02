package com.muxiaoqiu.accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muxiaoqiu.accounting.ui.navigation.AppNavigation
import com.muxiaoqiu.accounting.ui.screens.AccountingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AccountingApp
        val viewModel = AccountingViewModel(app.database.transactionDao())

        setContent {
            AppNavigation(viewModel = viewModel)
        }
    }
}
