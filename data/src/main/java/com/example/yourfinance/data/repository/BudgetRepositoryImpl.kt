package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.source.BudgetDao
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(private val budgetDao: BudgetDao) : BudgetRepository {
    override fun fetchBudgets(): LiveData<List<Budget>> {
        val mediator = MediatorLiveData<List<Budget>>()
        val budgets = budgetDao.getAllBudgets()
        mediator.addSource(budgets) {
            mediator.value = (budgets.value ?: emptyList()).map { it.toDomain() }
        }
        return mediator
    }

    override suspend fun insertBudget(budget: Budget) {
        withContext(Dispatchers.IO) {
            budgetDao.insertBudget(budget.toData())
        }
    }
}