package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.CategoryType


@Entity
data class CategoryEntity(
    var title: String,
    val categoryType: CategoryType,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

