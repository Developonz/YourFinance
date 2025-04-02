package com.example.yourfinance.domain.model.entity.category

data class FullCategory (
    val category: Category,
    val subcategories: List<Subcategory> = emptyList()
)