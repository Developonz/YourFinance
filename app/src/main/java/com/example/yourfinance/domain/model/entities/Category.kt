package com.example.yourfinance.domain.model.entities

import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar


data class Category(
    val categoryType: CategoryType,
    val id: Long
) {

    enum class CategoryType {
        income,
        expense;
    }

    var title = ""
        set(value) {
            field = getUpperFirstChar(value)
        }

    constructor(title: String, type: CategoryType, id: Long) : this(type, id) {
        this.title = getUpperFirstChar(title)
    }
}

