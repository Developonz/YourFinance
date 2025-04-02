package com.example.yourfinance.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    val transactionsList: LiveData<List<Transaction>>
        get() = repository.getAllTransactions()
}

