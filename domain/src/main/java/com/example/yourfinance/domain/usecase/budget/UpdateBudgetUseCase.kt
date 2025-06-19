package com.example.yourfinance.domain.usecase.budget

import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.BudgetRepository
import javax.inject.Inject

class UpdateBudgetUseCase @Inject constructor(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repository.updateBudgetWithCategories(budget)
}