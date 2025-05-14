package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title


data class Subcategory private constructor(
    private val baseProperties: BaseCategory,
    val parentId: Long
) : ICategoryData by baseProperties {


    constructor(
        title: Title,
        categoryType: CategoryType,
        parentId: Long,
        colorHex: String?,
        id: Long = 0,
    ) : this(BaseCategory(title, categoryType, id, colorHex = colorHex), parentId)

}