package com.muxiaoqiu.accounting

import android.app.Application
import com.muxiaoqiu.accounting.data.database.AppDatabase

class AccountingApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
