package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.domain.model.entity.Category

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = this.id,
        _title = this.title,
        categoryType = this.categoryType
    )
}

fun Category.toData(): CategoryEntity {
    return CategoryEntity(
        title = this.title,
        categoryType = this.categoryType
    )
}