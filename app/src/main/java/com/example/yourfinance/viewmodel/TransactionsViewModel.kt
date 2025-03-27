package com.example.yourfinance.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.MainActivity
import com.example.yourfinance.MainApplication

class TransactionsViewModel : ViewModel() {

    val dao = MainApplication.database.getFinanceDao()
    val transactionsList = dao.getAllPayment()
}