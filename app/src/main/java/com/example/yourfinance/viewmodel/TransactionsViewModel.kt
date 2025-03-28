package com.example.yourfinance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.MainApplication
import com.example.yourfinance.model.FinanceRepository
import com.example.yourfinance.model.Transaction

class TransactionsViewModel : ViewModel() {
    val transactionsList: LiveData<List<Transaction>>
        get() = MainApplication.repository.allTransactionsList

}

