package com.example.yourfinance.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.yourfinance.data.model.FuturePaymentEntity
import com.example.yourfinance.data.model.FutureTransferEntity

@Dao
abstract class FutureTransactionDao() {

    @Insert
    abstract fun insertFuturePaymentTransaction(futurePayment: FuturePaymentEntity) : Long

    @Query("SELECT COUNT(*) FROM FuturePaymentEntity where id = :id")
    abstract fun loadCountFuturePaymentTransaction(id: Long) : Int

    @Query("Delete from FuturePaymentEntity where id = :id")
    abstract fun deleteFuturePaymentTransaction(id: Long)

    @Insert
    abstract fun insertFutureTransferTransaction(futureTransfer: FutureTransferEntity) : Long

    @Query("SELECT COUNT(*) FROM FutureTransferEntity where id = :id")
    abstract fun loadCountFutureTransferTransaction(id: Long) : Int

    @Query("DELETE from FutureTransferEntity where id = :id")
    abstract fun deleteFutureTransferTransaction(id: Long)

    @Query("SELECT * FROM FuturePaymentEntity")
    abstract suspend fun getAllFuturePaymentsForExport(): List<FuturePaymentEntity>

    @Query("SELECT * FROM FutureTransferEntity")
    abstract suspend fun getAllFutureTransfersForExport(): List<FutureTransferEntity>

    @Query("DELETE FROM FuturePaymentEntity")
    abstract suspend fun clearAllFuturePayments()

    @Query("DELETE FROM FutureTransferEntity")
    abstract suspend fun clearAllFutureTransfers()
}