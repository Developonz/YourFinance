package com.example.yourfinance.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.entities.Transfer
import com.example.yourfinance.utils.Converters

@Database(entities = [Payment::class, Transfer::class, MoneyAccount::class, Category::class], version = 1)
@TypeConverters(Converters::class)
abstract class FinanceDataBase : RoomDatabase() {
    companion object {
        const val name = "Finance_DB"
    }
    abstract fun getFinanceDao() : FinanceDao
}