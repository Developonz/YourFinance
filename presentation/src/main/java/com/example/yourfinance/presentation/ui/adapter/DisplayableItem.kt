package com.example.yourfinance.presentation.ui.adapter

sealed class DisplayableItem {
    // Уникальный идентификатор для DiffUtil
    abstract val diffId: String

    data class HeaderItem(val title: String) : DisplayableItem() {
        override val diffId: String = "header_$title"
    }

    data class ContentItem(
        val iconItem: IconItem,
        var isSelected: Boolean = false // Состояние выделения теперь часть элемента
    ) : DisplayableItem() {
        override val diffId: String = "content_${iconItem.resourceId}"
    }
}