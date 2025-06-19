package com.example.yourfinance.presentation.ui.fragment.manager.budget_manager

import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.Subcategory

sealed class SelectableCategoryItem {
    abstract val id: Long
    abstract val title: String

    // Специальный объект для "Всех категорий"
    object AllCategories : SelectableCategoryItem() {
        override val id: Long = -1L // Уникальный фиктивный ID
        override val title: String = "Все категории"
    }

    data class Parent(val category: Category) : SelectableCategoryItem() {
        override val id: Long get() = category.id
        override val title: String get() = category.title
    }
    data class Child(val subcategory: Subcategory) : SelectableCategoryItem() {
        override val id: Long get() = subcategory.id
        override val title: String get() = subcategory.title
    }
}