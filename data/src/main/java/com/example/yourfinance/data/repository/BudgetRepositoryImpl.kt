package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.source.BudgetDao
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(private val budgetDao: BudgetDao) : BudgetRepository {
    override fun fetchBudgets(): LiveData<List<Budget>> {
        return MediatorLiveData<List<Budget>>().apply {
            addSource(budgetDao.getAllBudgets()) { value = it.map { budget -> budget.toDomain() } }
        }
    }

    override suspend fun insertBudgetWithCategories(budget: Budget) {
        withContext(Dispatchers.IO) {
            val categoryIds = budget.categories.map { it.id }
            budgetDao.insertBudgetWithCategories(budget.toData(), categoryIds)
        }
    }

    override suspend fun updateBudgetWithCategories(budget: Budget) {
        withContext(Dispatchers.IO) {
            val categoryIds = budget.categories.map { it.id }
            budgetDao.updateBudgetWithCategories(budget.toData(), categoryIds)
        }
    }

    override suspend fun loadBudgetById(budgetId: Long): Budget? {
        return withContext(Dispatchers.IO) {
            budgetDao.loadBudgetById(budgetId)?.toDomain()
        }
    }

    override suspend fun deleteBudgetWithRelations(budgetId: Long) {
        withContext(Dispatchers.IO) {
            budgetDao.deleteBudgetWithRelations(budgetId)
        }
    }
}