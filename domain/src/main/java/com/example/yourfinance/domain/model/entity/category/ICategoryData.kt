package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType

interface ICategoryData {
    val id: Long
    var title: String
    val categoryType: CategoryType
    var colorHex: String?
}