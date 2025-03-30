package com.example.yourfinance.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar

@Entity
data class CategoryEntity(
    val categoryType: CategoryType,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {

    enum class CategoryType {
        income,
        expense;
    }

    @ColumnInfo(name = "title")
    var title = ""
        set(value) {
            field = getUpperFirstChar(value)
        }

    constructor(title: String, type: CategoryType) : this(type) {
        this.title = getUpperFirstChar(title)
    }
}

