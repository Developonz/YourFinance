package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title

//class Subcategory(
//    title: Title,
//    categoryType: CategoryType,
//    val parentId: Long,
//    id: Long = 0,
//) : Category(title, categoryType, id)

data class Subcategory private constructor(
    private val baseProperties: BaseCategory,
    val parentId: Long
) : ICategoryData by baseProperties {

    constructor(
        title: Title,
        categoryType: CategoryType,
        id: Long = 0,
        parentId: Long
    ) : this(BaseCategory(title, categoryType, id), parentId)

}