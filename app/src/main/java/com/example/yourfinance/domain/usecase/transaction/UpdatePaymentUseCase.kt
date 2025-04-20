package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdatePaymentUseCase @Inject constructor(val transactionRepository: TransactionRepository) {
    suspend operator fun invoke(payment: Payment) {
        transactionRepository.updatePayment(payment)
    }
}