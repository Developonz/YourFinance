package com.example.yourfinance.presentation.ui.fragment.manager.budget_manager

import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.entity.Budget
import java.math.BigDecimal

sealed class BudgetManagerListItem {
    data class HeaderItem(
        val period: PeriodLite,
        val totalSpent: BigDecimal,
        val totalBalance: BigDecimal
    ) : BudgetManagerListItem()

    data class BudgetItem(
        val budget: Budget
    ) : BudgetManagerListItem()
}