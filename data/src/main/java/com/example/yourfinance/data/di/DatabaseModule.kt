//package com.example.yourfinance.data.di
//
//import android.content.Context
//import androidx.room.Room
//import com.example.yourfinance.data.source.BudgetDao
//import com.example.yourfinance.data.source.CategoryDao
//import com.example.yourfinance.data.source.FinanceDataBase
//import com.example.yourfinance.data.source.FutureTransactionDao
//import com.example.yourfinance.data.source.MoneyAccountDao
//import com.example.yourfinance.data.source.TransactionDao
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class) // Предоставляет зависимости на уровне приложения
//object DatabaseModule {
//
//    @Provides
//    @Singleton // Гарантирует, что будет только один экземпляр БД
//    fun provideFinanceDatabase(@ApplicationContext appContext: Context): FinanceDataBase {
//        return Room.databaseBuilder(
//            appContext,
//            FinanceDataBase::class.java,
//            FinanceDataBase.name
//        )
//            // .addMigrations(...) // Если у вас есть миграции
//            // .fallbackToDestructiveMigration() // Для разработки, если нужно
//            .build()
//    }
//
//    @Provides
//    fun provideTransactionDao(database: FinanceDataBase): TransactionDao {
//        return database.getTransactionDao()
//    }
//
//    @Provides
//    fun provideCategoryDao(database: FinanceDataBase): CategoryDao {
//        return database.getCategoryDao()
//    }
//
//    @Provides
//    fun provideMoneyAccountDao(database: FinanceDataBase): MoneyAccountDao {
//        return database.getMoneyAccountDao()
//    }
//
//    @Provides
//    fun provideBudgetDao(database: FinanceDataBase): BudgetDao {
//        return database.getBudgetDao()
//    }
//
//    @Provides
//    fun provideFutureTransactionDao(database: FinanceDataBase): FutureTransactionDao {
//        return database.getFutureTransactionDao()
//    }
//}