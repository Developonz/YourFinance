package com.example.yourfinance.data.model.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.SubcategoryEntity

data class CategoryWithSubcategories(
    @Embedded
    val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val subcategories: List<SubcategoryEntity>
)