package com.example.yourfinance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.MainApplication
import com.example.yourfinance.model.Transaction

class TransactionsViewModel : ViewModel() {

    val dao = MainApplication.database.getFinanceDao()
    private var _transactionsList  = dao.getAllTransactions()
    val transactionsList: LiveData<List<Transaction>> get() = _transactionsList



}

