package com.example.yourfinance.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.yourfinance.data.model.FuturePaymentEntity
import com.example.yourfinance.data.model.FutureTransferEntity

@Dao
abstract class FutureTransactionDao {

    @Insert
    abstract fun insertFuturePaymentTransaction(futurePayment: FuturePaymentEntity)

    @Query("SELECT COUNT(*) FROM FuturePaymentEntity where id = :id")
    abstract fun loadCountFuturePaymentTransaction(id: Long) : Int

    @Query("Delete from FuturePaymentEntity where id = :id")
    abstract fun deleteFuturePaymentTransaction(id: Long)

    @Insert
    abstract fun insertFutureTransferTransaction(futureTransfer: FutureTransferEntity)

    @Query("SELECT COUNT(*) FROM FutureTransferEntity where id = :id")
    abstract fun loadCountFutureTransferTransaction(id: Long) : Int

    @Query("DELETE from FutureTransferEntity where id = :id")
    abstract fun deleteFutureTransferTransaction(id: Long)

}