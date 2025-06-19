package com.example.yourfinance.presentation.ui.fragment.manager.budget_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.usecase.budget.CalculateBudgetDetailsUseCase
import com.example.yourfinance.domain.usecase.budget.CreateBudgetUseCase
import com.example.yourfinance.domain.usecase.budget.DeleteBudgetUseCase
import com.example.yourfinance.domain.usecase.budget.FetchBudgetsUseCase
import com.example.yourfinance.domain.usecase.budget.LoadBudgetByIdUseCase
import com.example.yourfinance.domain.usecase.budget.UpdateBudgetUseCase
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.transaction.FetchTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetManagerViewModel @Inject constructor(
    fetchBudgetsUseCase: FetchBudgetsUseCase,
    fetchCategoriesUseCase: FetchCategoriesUseCase,
    fetchTransactionsUseCase: FetchTransactionsUseCase,
    private val calculateBudgetDetailsUseCase: CalculateBudgetDetailsUseCase,
    private val createBudgetUseCase: CreateBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase,
    private val loadBudgetByIdUseCase: LoadBudgetByIdUseCase
) : ViewModel() {

    // Источник 1: "Сырые" бюджеты
    private val rawBudgets: LiveData<List<Budget>> = fetchBudgetsUseCase()

    // Источник 2: Все транзакции
    private val allTransactions: LiveData<List<Transaction>> = fetchTransactionsUseCase(null, null)

    // LiveData с категориями расходов, нужна только для экрана выбора
    val expenseCategories: LiveData<List<Category>> = fetchCategoriesUseCase().map { list ->
        list.filter { it.categoryType == com.example.yourfinance.domain.model.CategoryType.EXPENSE }
    }

    // MediatorLiveData, который будет пересчитывать бюджеты при изменении любого из источников
    val budgetsWithDetails = MediatorLiveData<List<Budget>>()

    init {
        fun update() {
            val budgets = rawBudgets.value
            if (budgets == null) return

            viewModelScope.launch {
                // Вызываем правильный UseCase без лишних параметров
                val detailedBudgets = budgets.map { budget ->
                    calculateBudgetDetailsUseCase.invoke(budget)
                }
                budgetsWithDetails.value = detailedBudgets
            }
        }

        budgetsWithDetails.addSource(rawBudgets) { update() }
        budgetsWithDetails.addSource(allTransactions) { update() }
    }

    val groupedBudgets: LiveData<List<BudgetManagerListItem>> = budgetsWithDetails.map { budgets ->
        if (budgets.isNullOrEmpty()) {
            emptyList()
        } else {
            val grouped = mutableListOf<BudgetManagerListItem>()
            budgets.groupBy { it.period }
                .toSortedMap(compareBy { it.ordinal })
                .forEach { (period, budgetList) ->
                    val totalSpent = budgetList.sumOf { it.spent }
                    val totalBalance = budgetList.sumOf { it.budgetLimit }
                    grouped.add(BudgetManagerListItem.HeaderItem(period, totalSpent, totalBalance))
                    grouped.addAll(budgetList.map { BudgetManagerListItem.BudgetItem(it) })
                }
            grouped
        }
    }

    val selectableCategories: LiveData<List<SelectableCategoryItem>> = expenseCategories.map { categories ->
        val items = mutableListOf<SelectableCategoryItem>()
        items.add(SelectableCategoryItem.AllCategories)
        categories.forEach { category ->
            items.add(SelectableCategoryItem.Parent(category))
            category.children.forEach { subcategory ->
                items.add(SelectableCategoryItem.Child(subcategory))
            }
        }
        items
    }

    fun createBudget(budget: Budget) = viewModelScope.launch { createBudgetUseCase(budget) }
    fun updateBudget(budget: Budget) = viewModelScope.launch { updateBudgetUseCase(budget) }
    fun deleteBudget(budgetId: Long) = viewModelScope.launch { deleteBudgetUseCase(budgetId) }
    suspend fun loadBudget(budgetId: Long): Budget? = loadBudgetByIdUseCase(budgetId)
}