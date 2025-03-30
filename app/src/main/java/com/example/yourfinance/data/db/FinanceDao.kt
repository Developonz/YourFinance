package com.example.yourfinance.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.yourfinance.data.entities.CategoryEntity
import com.example.yourfinance.data.entities.MoneyAccountEntity
import com.example.yourfinance.data.entities.PaymentEntity
import com.example.yourfinance.data.entities.TransferEntity


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
    abstract fun getAllPayment(): LiveData<List<PaymentEntity>>

    @Transaction
    @Query("SELECT * FROM TransferEntity")
    abstract fun getAllTransfer() : LiveData<List<TransferEntity>>

    @Query("SELECT * FROM CategoryEntity")
    abstract fun getAllCategory() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM MoneyAccountEntity")
    abstract fun getAllAccounts() : LiveData<List<MoneyAccountEntity>>


    fun getAllTransactions(): MutableLiveData<List<com.example.yourfinance.domain.model.Transaction>> {
        val mediator = MediatorLiveData<List<com.example.yourfinance.domain.model.Transaction>>()

        val paymentsLiveData = getAllPayment()
        val transfersLiveData = getAllTransfer()

        fun update() {
            val combinedTransactions = mutableListOf<com.example.yourfinance.domain.model.Transaction>()
            val fullPayments = paymentsLiveData.value ?: emptyList()
            val fullTransfers = transfersLiveData.value ?: emptyList()

            combinedTransactions.addAll(fullPayments)
            combinedTransactions.addAll(fullTransfers)


            if (mediator.value != combinedTransactions) {
                mediator.value = combinedTransactions
            }
        }

        mediator.addSource(paymentsLiveData) { update() }
        mediator.addSource(transfersLiveData) { update() }

        return mediator
    }
}
