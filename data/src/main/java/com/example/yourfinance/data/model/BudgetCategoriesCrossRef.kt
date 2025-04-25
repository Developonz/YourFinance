package com.example.yourfinance.data.model

import androidx.room.Entity

@Entity(primaryKeys = ["budgetId", "categoryId"])
class BudgetCategoriesCrossRef(
    val budgetId: Long,
    val categoryId: Long
)