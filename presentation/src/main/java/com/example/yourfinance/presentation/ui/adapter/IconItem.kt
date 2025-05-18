package com.example.yourfinance.presentation.ui.adapter

import androidx.annotation.ColorInt

data class IconItem(
    val resourceId: String,
    val name: String,
    @ColorInt val colorHex: Int? = null
)