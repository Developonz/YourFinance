package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FinanceRepository {
    fun getAllTransactions(): LiveData<List<Transaction>>

    suspend fun insertPayment(paymentEntity: PaymentEntity)

    suspend fun insertTransfer(transferEntity: TransferEntity)
}