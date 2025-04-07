package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.MoneyAccount

interface MoneyAccountRepository {
    fun getAllAccounts(): LiveData<List<MoneyAccount>>

    suspend fun insertAccount(account: MoneyAccount) : Long

    suspend fun deleteAccount(account: MoneyAccount)

    suspend fun getAccountById(id: Long): MoneyAccount?

    suspend fun updateAccount(account: MoneyAccount)
}