package com.example.yourfinance

import android.app.Application
import androidx.room.Room
import com.example.yourfinance.db.FinanceDataBase


class MainApplication : Application() {
    companion object {
        lateinit var database: FinanceDataBase
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            FinanceDataBase::class.java,
            FinanceDataBase.name
        ).build()
    }
}