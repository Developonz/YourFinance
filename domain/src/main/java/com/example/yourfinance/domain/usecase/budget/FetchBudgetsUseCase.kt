package com.example.yourfinance.domain.usecase.budget

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.BudgetRepository
import javax.inject.Inject

class FetchBudgetsUseCase @Inject constructor(private val budgetRepository: BudgetRepository ) {
    operator fun invoke() : LiveData<List<Budget>> {
        return budgetRepository.fetchBudgets()
    }
}