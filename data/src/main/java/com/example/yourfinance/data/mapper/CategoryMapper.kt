package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.SubcategoryEntity
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = this.id,
        _title = this.title,
        categoryType = this.categoryType
    )
}

fun SubcategoryEntity.toDomain(): Subcategory {
    return Subcategory(
        id = this.id,
        title = this.title,
        categoryType = this.categoryType,
        parentId = this.parentId
    )
}

fun CategoryWithSubcategories.toDomain() : FullCategory {
    return FullCategory(
        category = this.category.toDomain(),
        subcategories = this.subcategories.map {it.toDomain()}
    )
}

fun Category.toData(): CategoryEntity {
    return CategoryEntity(
        id = this.id,
        title = this.title,
        categoryType = this.categoryType
    )
}

fun Subcategory.toData() : SubcategoryEntity {
    return SubcategoryEntity(
        id = this.id,
        title = this.title,
        categoryType = this.categoryType,
        parentId = this.parentId
    )
}