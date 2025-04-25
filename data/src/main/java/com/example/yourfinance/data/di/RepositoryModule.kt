package com.example.yourfinance.data.di


import com.example.yourfinance.data.repository.BudgetRepositoryImpl
import com.example.yourfinance.data.repository.CategoryRepositoryImpl
import com.example.yourfinance.data.repository.MoneyAccountRepositoryImpl
import com.example.yourfinance.data.repository.TransactionRepositoryImpl
import com.example.yourfinance.domain.repository.BudgetRepository
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideTransactionRepository(
        repository: TransactionRepositoryImpl
    ) : TransactionRepository

    @Binds
    @Singleton
    abstract fun provideCategoryRepository(
        repository: CategoryRepositoryImpl
    ) : CategoryRepository

    @Binds
    @Singleton
    abstract fun provideMoneyAccountRepository(
        repository: MoneyAccountRepositoryImpl
    ) : MoneyAccountRepository

    @Binds
    @Singleton
    abstract fun provideBudgetRepository(
        repository: BudgetRepositoryImpl
    ) : BudgetRepository
}