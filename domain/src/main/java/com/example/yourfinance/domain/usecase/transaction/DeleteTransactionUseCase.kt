package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {

    operator suspend fun invoke(transaction: Transaction) {
        transactionRepository.deleteTransaction(transaction)
    }
}