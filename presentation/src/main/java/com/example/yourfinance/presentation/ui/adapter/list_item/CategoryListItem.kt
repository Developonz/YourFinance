package com.example.yourfinance.presentation.ui.adapter.list_item

import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.Subcategory

sealed class CategoryListItem {
    abstract val viewType: Int
    abstract val id: String // Уникальный идентификатор для DiffUtil
    abstract val isSelected: Boolean // Добавлено для отслеживания состояния выбора

    data class CategoryItem(val category: Category, override val isSelected: Boolean = false) : CategoryListItem() {
        override val viewType: Int = VIEW_TYPE_CATEGORY
        override val id: String = "cat_${category.id}"
    }
    data class SubcategoryItem(val subcategory: Subcategory, override val isSelected: Boolean = false) : CategoryListItem() {
        override val viewType: Int = VIEW_TYPE_SUBCATEGORY
        override val id: String = "subcat_${subcategory.id}"
    }
    object SettingsButtonItem : CategoryListItem() {
        override val viewType: Int = VIEW_TYPE_SETTINGS_BUTTON
        override val id: String = "settings_button_item"
        override val isSelected: Boolean = false // Кнопка настроек не может быть "выбрана" таким же образом
    }

    companion object {
        const val VIEW_TYPE_CATEGORY = 1
        const val VIEW_TYPE_SUBCATEGORY = 2
        const val VIEW_TYPE_SETTINGS_BUTTON = 3
    }
}