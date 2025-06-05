package com.example.yourfinance.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.FuturePaymentEntity
import com.example.yourfinance.data.model.FutureTransferEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity

@Database(entities = [
    PaymentEntity::class,
    TransferEntity::class,
    MoneyAccountEntity::class,
    CategoryEntity::class,
    BudgetEntity::class,
    BudgetCategoriesCrossRef::class,
    FuturePaymentEntity::class,
    FutureTransferEntity::class
                     ], version = 1)
@TypeConverters(Converters::class)
abstract class FinanceDataBase : RoomDatabase() {
    companion object {
        const val name = "Finance_DB"
    }

    abstract fun getTransactionDao() : TransactionDao

    abstract fun getCategoryDao() : CategoryDao

    abstract fun getMoneyAccountDao() : MoneyAccountDao

    abstract fun getBudgetDao() : BudgetDao

    abstract fun getFutureTransactionDao() : FutureTransactionDao
}