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

class TransactionRepositoryImpl @Inject constructor(private val financeDao: TransactionDao) : TransactionRepository {

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

    override suspend fun insertPayment(payment: Payment) {
        withContext(Dispatchers.IO) {
            Log.i("TESTDB", payment.moneyAccount.title + " ")
            financeDao.insertPaymentTransaction(payment.toData())
        }
    }

    override suspend fun insertTransfer(transfer: Transfer) {
        withContext(Dispatchers.IO) {
            financeDao.insertTransferTransaction(transfer.toData())
        }
    }
}