package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransferUseCase @Inject constructor(val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(transfer: Transfer) {
        transactionRepository.updateTransfer(transfer)
    }
}