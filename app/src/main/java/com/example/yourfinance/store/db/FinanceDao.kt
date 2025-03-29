package com.example.yourfinance.store.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.entities.Transfer


@Dao
abstract class FinanceDao {
    @Transaction
    @Insert
    abstract fun insertPaymentTransaction(trans: Payment)
    @Transaction
    @Insert
    abstract fun insertTransferTransaction(trans: Transfer)
    @Transaction
    @Insert
    abstract fun insertAccount(acc: MoneyAccount)
    @Transaction
    @Insert
    abstract fun insertCategory(category: Category)

    @Transaction
    @Query("SELECT * FROM Payment")
    abstract fun getAllPayment(): LiveData<List<Payment>>

    @Transaction
    @Query("SELECT * FROM Transfer")
    abstract fun getAllTransfer() : LiveData<List<Transfer>>

    @Query("SELECT * FROM Category")
    abstract fun getAllCategory() : LiveData<List<Category>>

    @Query("SELECT * FROM MoneyAccount")
    abstract fun getAllAccounts() : LiveData<List<MoneyAccount>>


    fun getAllTransactions(): MutableLiveData<List<com.example.yourfinance.model.Transaction>> {
        val mediator = MediatorLiveData<List<com.example.yourfinance.model.Transaction>>()

        val paymentsLiveData = getAllPayment()
        val transfersLiveData = getAllTransfer()

        fun update() {
            val combinedTransactions = mutableListOf<com.example.yourfinance.model.Transaction>()
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
