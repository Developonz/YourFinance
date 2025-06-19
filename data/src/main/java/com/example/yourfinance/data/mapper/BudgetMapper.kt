package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.pojo.BudgetWithCategories
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.Budget

fun BudgetWithCategories.toDomain() : Budget {
    return Budget(
        _title = Title(this.budget.title),
        budgetLimit = this.budget.budgetLimit,
        categories = this.categories.map { it.toDomain() }.toMutableList(),
        period = this.budget.period,
        id = this.budget.id
    )
}

fun Budget.toData() : BudgetEntity {
    return BudgetEntity(
        title = this.title,
        budgetLimit = this.budgetLimit,
        period = this.period,
        id = this.id
    )
}