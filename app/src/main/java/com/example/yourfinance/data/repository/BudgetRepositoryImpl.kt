package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.source.FinanceDao
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(private val dao: FinanceDao) : BudgetRepository {
    override fun getAllBudgets(): LiveData<List<Budget>> {
        val mediator = MediatorLiveData<List<Budget>>()
        val budgets = dao.getAllBudgets()
        mediator.addSource(budgets) {(budgets.value ?: emptyList()).map { it.toDomain() }}
        return mediator
    }

    override suspend fun insertAccount(budget: Budget) {
        withContext(Dispatchers.IO) {
            dao.insertBudget(budget.toData())
        }
    }
}