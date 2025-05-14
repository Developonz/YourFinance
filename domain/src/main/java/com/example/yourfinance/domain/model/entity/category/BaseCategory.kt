package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title

data class BaseCategory(
    private var _title: Title,
    override val categoryType: CategoryType,
    override val id: Long = 0,
    override var iconResourceId: Int?,
    override var colorHex: String?
) : ICategoryData {
    override var title: String
        get() = _title.value
        set(value) { _title = Title(value) }
}