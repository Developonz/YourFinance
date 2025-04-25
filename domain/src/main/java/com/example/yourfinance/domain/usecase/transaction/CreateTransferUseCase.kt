package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class CreateTransferUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(transfer: Transfer) {
        transactionRepository.createTransfer(transfer)
    }
}