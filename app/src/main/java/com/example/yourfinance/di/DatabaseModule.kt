package com.example.yourfinance.di

import android.content.Context
import androidx.room.Room
import com.example.yourfinance.data.source.FinanceDao
import com.example.yourfinance.data.source.FinanceDataBase
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
    fun provideFinanceDao(
        database: FinanceDataBase
    ): FinanceDao {
        return database.getFinanceDao()
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