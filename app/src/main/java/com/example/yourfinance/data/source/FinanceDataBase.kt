package com.example.yourfinance.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.SubcategoryEntity
import com.example.yourfinance.data.model.TransferEntity

@Database(entities = [PaymentEntity::class, TransferEntity::class, MoneyAccountEntity::class, CategoryEntity::class, BudgetEntity::class, BudgetCategoriesCrossRef::class, SubcategoryEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class FinanceDataBase : RoomDatabase() {
    companion object {
        const val name = "Finance_DB"
    }
    abstract fun getFinanceDao() : FinanceDao
}