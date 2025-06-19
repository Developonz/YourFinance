package com.example.yourfinance.domain.usecase.budget

import com.example.yourfinance.domain.repository.BudgetRepository
import javax.inject.Inject

class LoadBudgetByIdUseCase @Inject constructor(private val repository: BudgetRepository) {
    suspend operator fun invoke(budgetId: Long) = repository.loadBudgetById(budgetId)
}