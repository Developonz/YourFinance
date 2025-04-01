package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.domain.model.Transaction


interface FinanceRepository {
    fun getAllTransactions(): LiveData<List<Transaction>>

    suspend fun insertPayment(paymentEntity: PaymentEntity)

    suspend fun insertTransfer(transferEntity: TransferEntity)
}