package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.BaseCategory
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.Subcategory

fun CategoryEntity.toDomain(): BaseCategory {
    return BaseCategory(
        id = this.id,
        _title = Title(this.title),
        categoryType = this.categoryType,
        iconResourceId = this.iconResourceId,
        colorHex = this.colorHex
    )
}


fun CategoryEntity.toDomainSubcategory(): Subcategory {
    return Subcategory(
        id = this.id,
        title = Title(this.title),
        categoryType = this.categoryType,
        parentId = this.parentId!!,
        iconResourceId = this.iconResourceId,
        colorHex = this.colorHex
    )
}


fun CategoryWithSubcategories.toDomainCategory(): Category {
    val domainSubcategories = this.subcategories.map { subEntity ->
        subEntity.toDomainSubcategory()
    }.toMutableList()

    return Category(
        id = this.category.id,
        title = Title(this.category.title),
        categoryType = this.category.categoryType,
        iconResourceId = this.category.iconResourceId,
        colorHex = this.category.colorHex,
        children = domainSubcategories
    )
}


fun Category.toData(): CategoryEntity {
    return CategoryEntity(
        id = this.id,
        title = this.title,
        categoryType = this.categoryType,
        parentId = null,
        iconResourceId = this.iconResourceId,
        colorHex = this.colorHex
    )
}


fun Subcategory.toData(): CategoryEntity {
    return CategoryEntity(
        id = this.id,
        title = this.title,
        categoryType = this.categoryType,
        parentId = this.parentId,
        iconResourceId = null,
        colorHex = this.colorHex
    )
}