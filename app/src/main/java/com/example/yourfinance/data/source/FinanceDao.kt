package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.data.model.pojo.FullTransfer


@Dao
abstract class FinanceDao {
    @Transaction
    @Insert
    abstract fun insertPaymentTransaction(trans: PaymentEntity)
    @Transaction
    @Insert
    abstract fun insertTransferTransaction(trans: TransferEntity)
    @Transaction
    @Insert
    abstract fun insertAccount(acc: MoneyAccountEntity)
    @Transaction
    @Insert
    abstract fun insertCategory(categoryEntity: CategoryEntity)

    @Transaction
    @Query("SELECT * FROM PaymentEntity")
    abstract fun getAllPayment(): LiveData<List<FullPayment>>

    @Transaction
    @Query("SELECT * FROM TransferEntity")
    abstract fun getAllTransfer() : LiveData<List<FullTransfer>>

    @Query("SELECT * FROM CategoryEntity")
    abstract fun getAllCategory() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM MoneyAccountEntity")
    abstract fun getAllAccounts() : LiveData<List<MoneyAccountEntity>>


}
