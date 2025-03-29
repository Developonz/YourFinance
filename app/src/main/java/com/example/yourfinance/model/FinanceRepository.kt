package com.example.yourfinance.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.yourfinance.db.FinanceDao
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.entities.Transfer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepository(private val financeDao: FinanceDao) {

    private var _allTransactionsList: LiveData<List<Transaction>> = financeDao.getAllTransactions()
    val allTransactionsList: LiveData<List<Transaction>>  get() = _allTransactionsList

    private var _allCategoriesList: LiveData<List<Category>> = financeDao.getAllCategory()
    val allCategoriesList: LiveData<List<Category>>  get() = _allCategoriesList

    private var _allAccountsList: LiveData<List<MoneyAccount>> = financeDao.getAllAccounts()
    val allAccountsList: LiveData<List<MoneyAccount>>  get() = _allAccountsList

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
    fun getAllPayments(): LiveData<List<Payment>> = financeDao.getAllPayment()

    fun getAllTransfers(): LiveData<List<Transfer>> = financeDao.getAllTransfer()

    fun getAllCategories(): LiveData<List<Category>> = financeDao.getAllCategory()

    fun getAllAccounts(): LiveData<List<MoneyAccount>> = financeDao.getAllAccounts()

    fun getAllTransactions(): LiveData<List<Transaction>> = financeDao.getAllTransactions()

    fun getCategory(id: Long): Category? {
        return allCategoriesList.value?.firstOrNull { it.id == id }
    }

    fun getAccount(id: Long) : MoneyAccount? {
        return allAccountsList.value?.firstOrNull {it.id == id}
    }
}