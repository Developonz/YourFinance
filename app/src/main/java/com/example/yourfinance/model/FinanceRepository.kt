package com.example.yourfinance.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.yourfinance.db.FinanceDao
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.entities.Transfer
import com.example.yourfinance.model.pojo.FullPayment
import com.example.yourfinance.model.pojo.FullTransfer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepository(private val financeDao: FinanceDao) {

    private var _allTransactionsList: MutableLiveData<List<Transaction>> = financeDao.getAllTransactions()
    val allTransactionsList: LiveData<List<Transaction>>  get() = _allTransactionsList



    suspend fun insertPayment(payment: Payment) {
        withContext(Dispatchers.IO) {
            financeDao.insertPaymentTransaction(payment)
        }
    }

    suspend fun insertTransfer(transfer: Transfer) {
        withContext(Dispatchers.IO) {
            financeDao.insertTransferTransaction(transfer)
        }
    }

    suspend fun insertAccount(account: MoneyAccount) {
        withContext(Dispatchers.IO) {
            financeDao.insertAccount(account)
        }
    }

    suspend fun insertCategory(category: Category) {
        withContext(Dispatchers.IO) {
            financeDao.insertCategory(category)
        }
    }

    // Методы для получения данных
    fun getAllPayments(): LiveData<List<FullPayment>> = financeDao.getAllPayment()

    fun getAllTransfers(): LiveData<List<FullTransfer>> = financeDao.getAllTransfer()

    fun getAllCategories(): LiveData<List<Category>> = financeDao.getAllCategory()

    fun getAllAccounts(): LiveData<List<MoneyAccount>> = financeDao.getAllAccounts()

    fun getAllTransactions(): LiveData<List<Transaction>> = financeDao.getAllTransactions()
}