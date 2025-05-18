package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title


data class Category private constructor(
    private val baseProperties: BaseCategory,
    val children: MutableList<Subcategory> = mutableListOf()
) : ICategoryData by baseProperties {


    constructor(
        title: Title,
        categoryType: CategoryType,
        id: Long = 0,
        iconResourceId: String? = null,
        colorHex: Int? = null,
        children: MutableList<Subcategory> = mutableListOf()
    ) : this(BaseCategory(title, categoryType, id, iconResourceId, colorHex), children)

}

