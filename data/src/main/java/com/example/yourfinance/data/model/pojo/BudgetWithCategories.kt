package com.example.yourfinance.data.model.pojo

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.CategoryEntity

data class BudgetWithCategories(
    @Embedded val budget: BudgetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BudgetCategoriesCrossRef::class,
            parentColumn = "budgetId",
            entityColumn = "categoryId"
        )
    )
    val categories: MutableList<CategoryEntity>
)
