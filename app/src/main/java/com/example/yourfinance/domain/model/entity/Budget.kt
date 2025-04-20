package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.util.StringHelper.Companion.getUpperFirstChar

data class Budget(
    private var _title: String,
    var balance: Double,
    var period: PeriodLite,
    val categories: MutableList<Category> = mutableListOf(),
    val id: Long = 0
) {
    var title: String
        get() = _title
        set(value) {
            _title = getUpperFirstChar(value)
        }
    init {
        title = title
    }
}