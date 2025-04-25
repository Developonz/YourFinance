package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class LoadTransferByIdUseCase @Inject constructor(val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(id: Long) : Transfer? {
        return transactionRepository.loadTransferById(id)
    }
}