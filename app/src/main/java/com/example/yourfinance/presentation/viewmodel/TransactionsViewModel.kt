package com.example.yourfinance.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.MainApplication
import com.example.yourfinance.domain.model.Transaction


class TransactionsViewModel : ViewModel() {
    val transactionsList: LiveData<List<Transaction>>
        get() = MainApplication.repository.allTransactionsList

}

