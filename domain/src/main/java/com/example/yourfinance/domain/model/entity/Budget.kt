package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.BaseCategory

data class Budget(
    private var _title: Title,
    var balance: Double,
    var period: PeriodLite,
    val categories: MutableList<BaseCategory> = mutableListOf(),
    val id: Long = 0
) {
    var title: String
        get() = _title.value
        set(value) { _title = Title(value) }
}