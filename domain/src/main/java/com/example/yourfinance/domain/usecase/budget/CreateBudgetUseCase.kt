package com.example.yourfinance.domain.usecase.budget

import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.BudgetRepository
import javax.inject.Inject

class CreateBudgetUseCase @Inject constructor(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repository.insertBudgetWithCategories(budget)
}