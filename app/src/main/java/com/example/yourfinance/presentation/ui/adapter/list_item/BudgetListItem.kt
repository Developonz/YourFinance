package com.example.yourfinance.presentation.ui.adapter.list_item

import com.example.yourfinance.domain.model.entity.Budget

sealed class BudgetListItem {
    class BudgetItem(val budget: Budget) : BudgetListItem()
    data object EmptyList : BudgetListItem()
    data object CreateBudget : BudgetListItem()
}