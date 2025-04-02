package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer


interface TransactionRepository {
    fun getAllTransactions(): LiveData<List<Transaction>>

    suspend fun insertPayment(payment: Payment)

    suspend fun insertTransfer(transfer: Transfer)
}