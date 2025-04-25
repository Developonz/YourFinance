package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.data.model.pojo.FullTransfer

@Dao
abstract class TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Можно указать стратегию конфликта
    abstract fun insertPaymentTransaction(trans: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTransferTransaction(trans: TransferEntity)

    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM PaymentEntity ORDER BY time DESC") // Добавим сортировку для примера
    abstract fun getAllPayment(): LiveData<List<FullPayment>>

    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM TransferEntity ORDER BY time DESC") // Добавим сортировку для примера
    abstract fun getAllTransfer() : LiveData<List<FullTransfer>>

    @Delete
    abstract fun deletePayment(payment: PaymentEntity)

    @Delete
    abstract fun deleteTransfer(transfer: TransferEntity)

    @Query("DELETE FROM PaymentEntity WHERE id = :paymentId")
    abstract fun deletePaymentById(paymentId: Long)

    @Query("DELETE FROM TransferEntity WHERE id = :transferId")
    abstract fun deleteTransferById(transferId: Long)

    @Query("SELECT * FROM PaymentEntity where id = :id")
    abstract suspend fun loadPaymentById(id: Long): FullPayment?

    @Query("SELECT * FROM TransferEntity where id = :id")
    abstract suspend fun loadTransferById(id: Long): FullTransfer?

    @Update
    abstract fun updatePayment(payment: PaymentEntity)

    @Update
    abstract fun updateTransfer(transfer: TransferEntity)
}