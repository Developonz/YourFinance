package com.example.yourfinance

import android.app.Application
import androidx.room.Room
import com.example.yourfinance.store.db.FinanceDataBase
import com.example.yourfinance.store.FinanceRepository


class MainApplication : Application() {
    companion object {
        lateinit var database: FinanceDataBase
        private lateinit var _repository: FinanceRepository
        val repository: FinanceRepository get() = _repository
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            FinanceDataBase::class.java,
            FinanceDataBase.name
        ).build()

        _repository = FinanceRepository(database.getFinanceDao())
    }
}