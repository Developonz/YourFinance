package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar


data class Category(
    private var _title: String,
    val categoryType: CategoryType,
    val id: Long = 0,
) {

    var title
        get() = _title
        set(value) {
            _title = getUpperFirstChar(value)
        }
    init {
        title = title
    }
}

