package com.example.yourfinance.data

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.data.db.FinanceDao
import com.example.yourfinance.data.entities.CategoryEntity
import com.example.yourfinance.data.entities.MoneyAccountEntity
import com.example.yourfinance.data.entities.PaymentEntity
import com.example.yourfinance.data.entities.TransferEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepository(private val financeDao: FinanceDao) {

    private var _allTransactionsList: LiveData<List<Transaction>> = financeDao.getAllTransactions()
    val allTransactionsList: LiveData<List<Transaction>>  get() = _allTransactionsList

    private var _allCategoriesList: LiveData<List<CategoryEntity>> = financeDao.getAllCategory()
    val allCategoriesList: LiveData<List<CategoryEntity>>  get() = _allCategoriesList

    private var _allAccountsList: LiveData<List<MoneyAccountEntity>> = financeDao.getAllAccounts()
    val allAccountsList: LiveData<List<MoneyAccountEntity>>  get() = _allAccountsList

    suspend fun insertPayment(paymentEntity: PaymentEntity) {
        withContext(Dispatchers.IO) {
            financeDao.insertPaymentTransaction(paymentEntity)
        }
    }

    suspend fun insertTransfer(transferEntity: TransferEntity) {
        withContext(Dispatchers.IO) {
            financeDao.insertTransferTransaction(transferEntity)
        }
    }

    suspend fun insertAccount(account: MoneyAccountEntity) {
        withContext(Dispatchers.IO) {
            financeDao.insertAccount(account)
        }
    }

    suspend fun insertCategory(categoryEntity: CategoryEntity) {
        withContext(Dispatchers.IO) {
            financeDao.insertCategory(categoryEntity)
        }
    }

    // Методы для получения данных
    fun getAllPayments(): LiveData<List<PaymentEntity>> = financeDao.getAllPayment()

    fun getAllTransfers(): LiveData<List<TransferEntity>> = financeDao.getAllTransfer()

    fun getAllCategories(): LiveData<List<CategoryEntity>> = financeDao.getAllCategory()

    fun getAllAccounts(): LiveData<List<MoneyAccountEntity>> = financeDao.getAllAccounts()

    fun getAllTransactions(): LiveData<List<Transaction>> = financeDao.getAllTransactions()

    fun getCategory(id: Long): CategoryEntity? {
        return allCategoriesList.value?.firstOrNull { it.id == id }
    }

    fun getAccount(id: Long) : MoneyAccountEntity? {
        return allAccountsList.value?.firstOrNull {it.id == id}
    }
}