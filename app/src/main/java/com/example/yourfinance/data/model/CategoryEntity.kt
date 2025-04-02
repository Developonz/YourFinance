package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.CategoryType


@Entity
open class CategoryEntity(
    open var title: String,
    open val categoryType: CategoryType,
    @PrimaryKey(autoGenerate = true)
    open val id: Long = 0
)

