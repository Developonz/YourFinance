package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType

class Subcategory(
    title: String,
    categoryType: CategoryType,
    val parentId: Long,
    id: Long = 0,
) : com.example.yourfinance.domain.model.entity.category.Category(title, categoryType, id)
