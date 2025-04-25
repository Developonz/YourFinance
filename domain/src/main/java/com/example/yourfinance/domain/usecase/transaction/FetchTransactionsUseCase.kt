package com.example.yourfinance.domain.usecase.transaction

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class FetchTransactionsUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {
    operator fun invoke() : LiveData<List<Transaction>> {
        return transactionRepository.fetchTransactions()
    }
}