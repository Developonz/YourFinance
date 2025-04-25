package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.pojo.BudgetWithCategories
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.category.Category

fun BudgetWithCategories.toDomain() : Budget {
    val tmpCategories: MutableList<Category> = mutableListOf()
    tmpCategories.addAll(this.catigories.map { it.toDomain() })
    return Budget(
        _title = Title(this.budget.title),
        balance = this.budget.balance,
        categories = tmpCategories,
        period = this.budget.period,
        id = this.budget.id
    )
}

fun Budget.toData() : BudgetEntity {
    return BudgetEntity(
        title = this.title,
        balance = this.balance,
        period = this.period,
        id = this.id
    )
}