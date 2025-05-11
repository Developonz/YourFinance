package com.example.yourfinance.domain.usecase.transaction

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

class FetchTransactionsUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {
    operator fun invoke(startDate: LocalDate?, endDate: LocalDate?) : LiveData<List<Transaction>> {
        return transactionRepository.fetchTransactions(startDate, endDate)
    }
}