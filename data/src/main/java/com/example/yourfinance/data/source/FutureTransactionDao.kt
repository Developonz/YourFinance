package com.example.yourfinance.data.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.yourfinance.data.model.FutureTransactionsEntity

@Dao
abstract class FutureTransactionDao {

    @Insert
    abstract fun insertFuturePaymentTransaction(futurePayment: FutureTransactionsEntity)

    @Query("SELECT COUNT(*) FROM FutureTransactionsEntity where id = :id")
    abstract fun loadCountFuturePaymentTransaction(id: Long) : Int

    @Query("Delete from FutureTransactionsEntity where id = :id")
    abstract fun deleteFuturePaymentTransaction(id: Long)

}