package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.data.source.FinanceDao
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FinanceRepositoryImpl @Inject constructor(private val financeDao: FinanceDao) : FinanceRepository {

    override fun getAllTransactions(): LiveData<List<Transaction>> {
        val mediator = MediatorLiveData<List<Transaction>>()

        val fullPayments = financeDao.getAllPayment()
        val fullTransfers = financeDao.getAllTransfer()
        fun update() {
            val combinedTransactions = mutableListOf<Transaction>()
            val tmpFullPayments = fullPayments.value ?: emptyList()
            val tmpFullTransfers = fullTransfers.value ?: emptyList()
            combinedTransactions.addAll(tmpFullPayments.map { it.toDomain() })
            combinedTransactions.addAll(tmpFullTransfers.map { it.toDomain() })

            if (mediator.value != combinedTransactions) {
                mediator.value = combinedTransactions
            }
        }

        mediator.addSource(fullPayments) { update() }
        mediator.addSource(fullTransfers) { update() }

        return mediator
    }

    override suspend fun insertPayment(paymentEntity: PaymentEntity) {
        withContext(Dispatchers.IO) {
            financeDao.insertPaymentTransaction(paymentEntity)
        }
    }

    override suspend fun insertTransfer(transferEntity: TransferEntity) {
        withContext(Dispatchers.IO) {
            financeDao.insertTransferTransaction(transferEntity)
        }
    }
}