package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar

data class Budget(
    private var _title: String,
    var balance: Double,
    val categories: MutableList<Category>,
    var period: PeriodLite,
    val id: Long
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