package com.example.yourfinance.domain.usecase.budget

import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.TransactionRepository
import javax.inject.Inject

class CalculateBudgetDetailsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend fun invoke(budget: Budget): Budget {
        val (startDate, endDate) = budget.calculateCurrentPeriodDates()
        var categoryIds = budget.categories.map { it.id }

        if (categoryIds.isEmpty()) {
            val allExpenseBaseCategories = categoryRepository.getAllExpenseBaseCategories()
            categoryIds = allExpenseBaseCategories.map { it.id }
        }

        budget.spent = transactionRepository.getSpentAmountForCategories(categoryIds, startDate, endDate)
        budget.startDate = startDate
        budget.endDate = endDate
        return budget
    }
}