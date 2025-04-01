package com.example.yourfinance.di

import com.example.yourfinance.data.repository.FinanceRepositoryImpl
import com.example.yourfinance.domain.repository.FinanceRepository
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
    abstract fun provideFinanceRepository(
        repository: FinanceRepositoryImpl)
    : FinanceRepository
}