package com.example.yourfinance.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.data.source.TransactionDao
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(private val dao: TransactionDao) : TransactionRepository {

    override fun fetchTransactions(): LiveData<List<Transaction>> {
        val mediator = MediatorLiveData<List<Transaction>>()

        val fullPayments = dao.getAllPayment()
        val fullTransfers = dao.getAllTransfer()
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

    override suspend fun createPayment(payment: Payment) {
        withContext(Dispatchers.IO) {
            Log.i("TESTDB", payment.moneyAccount.title + " ")
            dao.insertPaymentTransaction(payment.toData())
        }
    }

    override suspend fun createTransfer(transfer: Transfer) {
        withContext(Dispatchers.IO) {
            dao.insertTransferTransaction(transfer.toData())
        }
    }

    override suspend fun loadPaymentById(id: Long): Payment? {
        return dao.loadPaymentById(id)?.toDomain()
    }

    override suspend fun loadTransferById(id: Long): Transfer? {
        return dao.loadTransferById(id)?.toDomain()
    }

    override suspend fun updatePayment(payment: Payment) {
        withContext(Dispatchers.IO) {
            dao.updatePayment(payment.toData())
        }
    }

    override suspend fun updateTransfer(transfer: Transfer) {
        withContext(Dispatchers.IO) {
            dao.updateTransfer(transfer.toData())
        }
    }
}