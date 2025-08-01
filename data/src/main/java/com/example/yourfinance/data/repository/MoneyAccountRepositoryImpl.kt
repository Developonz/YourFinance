package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.source.MoneyAccountDao
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoneyAccountRepositoryImpl @Inject constructor(private val dao: MoneyAccountDao) : MoneyAccountRepository {
    override fun getAllAccounts(): LiveData<List<MoneyAccount>> {
        val mediator = MediatorLiveData<List<MoneyAccount>>()
        val accounts = dao.getAllAccounts()
        mediator.addSource(accounts) {
            mediator.value = (accounts.value?: emptyList()).map { it.toDomain() }
        }
        return mediator
    }

    override suspend fun insertAccount(account: MoneyAccount) : Long {
        var id: Long = 0
        withContext(Dispatchers.IO) {
            id = dao.insertAccount(account.toData())
        }
        return id
    }

    override suspend fun deleteAccount(account: MoneyAccount) {
        withContext(Dispatchers.IO) {
            dao.deleteAccountById(account.id)
        }
    }

    override suspend fun getAccountById(id: Long): MoneyAccount? {
        return withContext(Dispatchers.IO) {
            dao.getAccountById(id)?.toDomain()
        }
    }

    override suspend fun updateAccount(account: MoneyAccount) {
        withContext(Dispatchers.IO) {
            dao.updateAccount(account.toData())
        }
    }

    override suspend fun setDefaultAccount(accountId: Long) {
        dao.setDefaultAccount(accountId)
    }
}