package com.example.yourfinance.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yourfinance.data.entities.CategoryEntity
import com.example.yourfinance.data.entities.MoneyAccountEntity
import com.example.yourfinance.data.entities.PaymentEntity
import com.example.yourfinance.data.entities.TransferEntity

@Database(entities = [PaymentEntity::class, TransferEntity::class, MoneyAccountEntity::class, CategoryEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class FinanceDataBase : RoomDatabase() {
    companion object {
        const val name = "Finance_DB"
    }
    abstract fun getFinanceDao() : FinanceDao
}