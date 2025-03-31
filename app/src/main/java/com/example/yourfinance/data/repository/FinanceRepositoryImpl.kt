package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.data.source.FinanceDao
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.domain.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepositoryImpl(private val financeDao: FinanceDao) : FinanceRepository {

    private var _allTransactionsList: LiveData<List<Transaction>> = getAllTransactions()
    val allTransactionsList: LiveData<List<Transaction>>  get() = _allTransactionsList

    private var _allCategoriesList: LiveData<List<CategoryEntity>> = financeDao.getAllCategory()
    val allCategoriesList: LiveData<List<CategoryEntity>>  get() = _allCategoriesList

    private var _allAccountsList: LiveData<List<MoneyAccountEntity>> = financeDao.getAllAccounts()
    val allAccountsList: LiveData<List<MoneyAccountEntity>>  get() = _allAccountsList


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