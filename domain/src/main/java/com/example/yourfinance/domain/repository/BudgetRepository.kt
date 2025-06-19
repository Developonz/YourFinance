package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.Budget

interface BudgetRepository {
    fun fetchBudgets(): LiveData<List<Budget>>
    suspend fun insertBudgetWithCategories(budget: Budget)
    suspend fun updateBudgetWithCategories(budget: Budget)
    suspend fun loadBudgetById(budgetId: Long): Budget?
    suspend fun deleteBudgetWithRelations(budgetId: Long)
}