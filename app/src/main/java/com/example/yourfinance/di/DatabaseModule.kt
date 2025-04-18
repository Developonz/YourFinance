package com.example.yourfinance.di

import android.content.Context
import androidx.room.Room
import com.example.yourfinance.data.source.BudgetDao
import com.example.yourfinance.data.source.CategoryDao
import com.example.yourfinance.data.source.FinanceDataBase
import com.example.yourfinance.data.source.MoneyAccountDao
import com.example.yourfinance.data.source.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTransactionDao(
        database: FinanceDataBase
    ): TransactionDao {
        return database.getTransactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(
        database: FinanceDataBase
    ): CategoryDao {
        return database.getCategoryDao()
    }

    @Provides
    @Singleton
    fun provideMoneyAccountDao(
        database: FinanceDataBase
    ): MoneyAccountDao {
        return database.getMoneyAccountDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(
        database: FinanceDataBase
    ): BudgetDao {
        return database.getBudgetDao()
    }

    @Provides
    @Singleton
    fun provideFinanceDataBase(@ApplicationContext context: Context): FinanceDataBase {
        return Room.databaseBuilder(
            context,
            FinanceDataBase::class.java,
            FinanceDataBase.name
        ).build()
    }
}