package com.example.yourfinance.domain.model.entity.category

data class FullCategory (
    val category: Category,
    val subcategories: List<com.example.yourfinance.domain.model.entity.category.Subcategory> = emptyList()
)