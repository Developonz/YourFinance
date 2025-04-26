package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.Budget

interface BudgetRepository {
    fun fetchBudgets(): LiveData<List<Budget>>

    suspend fun insertBudget(budget: Budget)
}