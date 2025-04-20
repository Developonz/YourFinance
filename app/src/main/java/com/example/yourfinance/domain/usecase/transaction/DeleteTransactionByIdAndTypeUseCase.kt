package com.example.yourfinance.domain.usecase.transaction

import com.example.yourfinance.domain.model.TransactionType
import javax.inject.Inject

class DeleteTransactionByIdAndTypeUseCase @Inject constructor() {

    operator fun invoke(id: Long, type: TransactionType) : Boolean {
        return true
    }
}