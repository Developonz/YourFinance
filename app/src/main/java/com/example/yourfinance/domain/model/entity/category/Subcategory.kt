package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType

class Subcategory(
    title: String,
    categoryType: CategoryType,
    id: Long,
    val parentId: Long
) : Category(title, categoryType, id)
