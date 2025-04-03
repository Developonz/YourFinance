package com.example.yourfinance.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.BudgetRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: MoneyAccountRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    val transactionsList: LiveData<List<Transaction>> = transactionRepository.getAllTransactions()

    val accountsList: LiveData<List<MoneyAccount>>  = accountRepository.getAllAccounts()

    val budgetsList: LiveData<List<Budget>> = budgetRepository.getAllBudgets()


    val combinedList = MediatorLiveData<Pair<List<Transaction>, List<MoneyAccount>>>().apply {
        addSource(transactionsList) { transactions ->
            value = transactions to (accountsList.value ?: emptyList())
        }
        addSource(accountsList) { accounts ->
            value = (transactionsList.value ?: emptyList()) to accounts
        }
    }
}

