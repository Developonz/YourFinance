package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class LoadPaymentByIdUseCase @Inject constructor(val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(id: Long) : Payment? {
        return transactionRepository.loadPaymentById(id)
    }
}