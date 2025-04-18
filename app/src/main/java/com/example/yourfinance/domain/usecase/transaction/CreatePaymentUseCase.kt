package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class CreatePaymentUseCase @Inject constructor(private val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(payment: Payment) {
        transactionRepository.insertPayment(payment)
    }
}