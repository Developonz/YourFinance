package com.example.yourfinance.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {
    val transactionsList: LiveData<List<Transaction>>
        get() = repository.getAllTransactions()
}

